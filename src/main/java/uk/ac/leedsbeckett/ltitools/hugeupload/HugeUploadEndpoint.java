/*
 * Copyright 2022 maber01.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.leedsbeckett.ltitools.hugeupload;

import uk.ac.leedsbeckett.ltitools.hugeupload.data.HuStoreCluster;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import uk.ac.leedsbeckett.ltitools.hugeupload.data.Configuration;
import uk.ac.leedsbeckett.ltitools.hugeupload.data.CourseConfiguration;
import uk.ac.leedsbeckett.ltitools.hugeupload.data.HuCourseKey;
import uk.ac.leedsbeckett.ltitools.hugeupload.data.HuResourceKey;
import uk.ac.leedsbeckett.ltitools.hugeupload.data.HugeUploadResource;
import uk.ac.leedsbeckett.ltitools.hugeupload.messagedata.HuBinaryChunkUpload;
import uk.ac.leedsbeckett.ltitools.hugeupload.messagedata.HuBinaryChunkUploadAck;
import uk.ac.leedsbeckett.ltitools.hugeupload.messagedata.HuBinaryChunkUploadReq;
import uk.ac.leedsbeckett.ltitools.hugeupload.messagedata.HuBinaryTestMessage;
import uk.ac.leedsbeckett.ltitools.hugeupload.messagedata.HuConfigurationMessage;
import uk.ac.leedsbeckett.ltitools.hugeupload.messagedata.HuFileMap;
import uk.ac.leedsbeckett.ltitools.hugeupload.messagedata.HuFileMapChunk;
import uk.ac.leedsbeckett.ltitools.hugeupload.messagedata.HuUploadChunkState;
import uk.ac.leedsbeckett.ltitools.hugeupload.messagedata.HuUploadState;
import uk.ac.leedsbeckett.ltitoolset.websocket.ToolEndpoint;
import uk.ac.leedsbeckett.ltitoolset.websocket.ToolMessage;
import uk.ac.leedsbeckett.ltitoolset.websocket.ToolMessageDecoder;
import uk.ac.leedsbeckett.ltitoolset.websocket.ToolMessageEncoder;
import uk.ac.leedsbeckett.ltitoolset.websocket.annotations.EndpointMessageHandler;
import uk.ac.leedsbeckett.ltitoolset.websocket.HandlerAlertException;
import uk.ac.leedsbeckett.ltitoolset.websocket.annotations.EndpointJavascriptProperties;

/**
 * The web socket server endpoint that implements all the logic of this tool.
 * It is annotated in two ways - so that the container (e.g. tomcat) will map
 * URLs on to the class - and so that the build process can create javascript
 * for the client side of the web socket.
 * 
 * @author maber01
 */
@ServerEndpoint( 
        value="/socket/hugeupload", 
        decoders=ToolMessageDecoder.class, 
        encoders=ToolMessageEncoder.class )
@EndpointJavascriptProperties(
        module="hugeupload",
        prefix="Hu",
        messageEnum="uk.ac.leedsbeckett.ltitools.hugeupload.HuServerMessageName"
)
public class HugeUploadEndpoint extends ToolEndpoint
{
  static final Logger logger = Logger.getLogger(HugeUploadEndpoint.class.getName() );
  
  HugeUploadTool tool;
  HuToolLaunchState huState;
  HuStoreCluster store;

  String platformName=null;
  
  // Don't store a reference to the resource or other data here.
  // It will get out of sync with instances held by other endpoint instances.
  // Rely on efficient caching and fetching at the start of every transaction.

  
  /**
   * Ask ToolCoordinator to index user sessions with this endpoint by
   * tool resource.
   * 
   * @return 
   */
  @Override
  public boolean indexByToolResource()
  {
    return true;
  }

  
  
  /**
   * Most work is done by the super-class. This sub-class fetches references
   * to tool specific objects.
   * 
   * @param session The session this endpoint belongs to.
   * @throws IOException If opening should be aborted.
   */
  @OnOpen
  @Override
  public void onOpen(Session session) throws IOException
  {
    super.onOpen( session );
    
    platformName = getState().getPlatformName();
    huState = (HuToolLaunchState)getState().getToolLaunchState();
    tool = (HugeUploadTool)getToolCoordinator().getTool( getState().getToolId() );
    store = tool.getHuStore();    
  }
  
  /**
   * Simply invokes super-class.
   * 
   * @param session The session this endpoint belongs to.
   * @throws IOException Unlikely to be thrown.
   */
  @OnClose
  @Override
  public void onClose(Session session) throws IOException
  {
    super.onClose( session );
  }

  /**
   * At present justs puts a line in the log.
   * 
   * @param session The session this endpoint belongs to.
   * @param throwable The throwable that caused the issue.
   */
  @OnError
  public void onError(Session session, Throwable throwable)
  {
    logger.log( Level.SEVERE, "Web socket error.", throwable );
  }  

  @OnMessage
  @Override
  public void onMessage(Session session, String text) throws IOException
  {
    super.onMessage( session, text );
  }

  private HuBinaryChunkUploadReq getNextChunkRequest( HugeUploadResource huResource )
  {
    for ( int i=0; i < huResource.getUploadState().getChunkStates().size(); i++ )
    {
      HuUploadChunkState chunkState = huResource.getUploadState().getChunkStates().get( i );
      if ( !chunkState.isUploaded() )
      {
        HuFileMapChunk chunk = huResource.getFileMap().getMap().get( i );
        HuBinaryChunkUploadReq upreq = new HuBinaryChunkUploadReq();
        upreq.setChunkNo( i );
        upreq.setStart( chunk.getStart() );
        upreq.setEnd( chunk.getEnd() );
        upreq.setHash( chunk.getHash() );
        return upreq;
      }
    }   
    return null;
  }
  
  @EndpointMessageHandler()
  public void handleFileMap( Session session, ToolMessage message, HuFileMap fileMap ) throws IOException, HandlerAlertException
  {
    // Whether there is a resource for the session will depend on the facet 
    // being used.
    if ( !"item".equals( huState.getToolFacetId() ) )
      throw new HandlerAlertException( "Recieved message on an inappropriate facet of the tool.", message.getId() );

    HuResourceKey rKey = huState.getHuResourceKey();
    if ( rKey == null )
      throw new HandlerAlertException( "Recieved request to set file map but cannot find resource data.", message.getId() );
    
    // No further checks - send data about the resource.
    HugeUploadResource huResource = store.getResource( rKey, true );
    if ( huResource == null )
      throw new HandlerAlertException( "Unable to find resource data.", message.getId() );

    logger.log(Level.INFO, "name    = {0}", fileMap.getName() );
    logger.log(Level.INFO, "size    = {0}", fileMap.getSize() );
    logger.log(Level.INFO, "type    = {0}", fileMap.getType() );
    logger.log(Level.INFO, "lastmod = {0}", fileMap.getLastModified() );
    int i=0;
    for ( HuFileMapChunk chunk :  fileMap.getMap() )
    {
      logger.log( 
              Level.INFO, 
              "    chunk {0} = {1} {2} {3}", 
              new Object[ ]{i++, chunk.getStart(), chunk.getEnd(), chunk.getHash()}
      );
    }
    
    
    // ToDo see if the map is indentical to the current one
    // and if so simply resume request chunk uploads

    // validity checking
    
    Path pending = store.getResourcePendingFilePath( rKey );
    if ( pending.toFile().exists() )
      pending.toFile().delete();
    
    // Set the new file map.
    huResource.setFileMap( fileMap );
    // Create and set a new upload state.
    HuUploadState upstate = new HuUploadState();
    upstate.setFullyUploaded( false );
    ArrayList<HuUploadChunkState> chunklist = new ArrayList<>();
    for ( i=0; i < fileMap.getMap().size(); i++ )
      chunklist.add( new HuUploadChunkState() );
    upstate.setChunkStates( chunklist );
    huResource.setUploadState( upstate );
    store.updateResource( huResource );
    // Tell the client that the fileMap has been accepted.
    ToolMessage tm = new ToolMessage( message.getId(), HuServerMessageName.Resource, huResource );
    sendToolMessage( session, tm );
    
    // Send first chunk request.
    HuBinaryChunkUploadReq upreq = getNextChunkRequest( huResource );
    if ( upreq == null )
      return;
    
    tm = new ToolMessage( message.getId(), HuServerMessageName.BinaryChunkUploadReq, upreq );
    sendToolMessage( session, tm );
  }

  @EndpointMessageHandler()
  public void handleBinaryTest( Session session, ToolMessage message, HuBinaryTestMessage bin ) throws IOException, HandlerAlertException
  {
    throw new HandlerAlertException( "BinaryTest message not supported anymore.", message.getId() );
  }
  
  @EndpointMessageHandler()
  public void handleBinaryChunk( Session session, ToolMessage message, HuBinaryChunkUpload chunkup ) 
          throws IOException, HandlerAlertException
  {
    // Whether there is a resource for the session will depend on the facet 
    // being used.
    if ( !"item".equals( huState.getToolFacetId() ) )
      throw new HandlerAlertException( "Recieved message on an inappropriate facet of the tool.", message.getId() );
    if ( chunkup == null )
      throw new HandlerAlertException( "Didn't get any data in the binary chunk upload test message.", message.getId() );
    logger.log( Level.INFO, "id = {0}", chunkup.getId() );
    logger.log( Level.INFO, "chunk no = {0}", chunkup.getChunkNo() );
    if ( chunkup.getChunk() == null )
      throw new HandlerAlertException( "Didn't get any binary data in the binary chunk upload test message.", message.getId() );
    logger.log( Level.INFO, "chunk length = {0}", chunkup.getChunk().length );

    byte[] sample = Arrays.copyOfRange( chunkup.getChunk(), 0, Integer.min( 16, chunkup.getChunk().length ) );
    BigInteger bigint = new BigInteger( sample );
    logger.log(Level.INFO, "start of chunk = 0x{0}", bigint.toString( 16 ));
    
    HuResourceKey rKey = huState.getHuResourceKey();
    if ( rKey == null )
      throw new HandlerAlertException( "Cannot find resource data.", message.getId() );
    HugeUploadResource huResource = store.getResource( rKey, true );
    if ( huResource == null )
      throw new HandlerAlertException( "Unable to find resource data.", message.getId() );

    HuFileMapChunk mapchunk = huResource.getFileMap().getMap().get( chunkup.getChunkNo() );
    Path pending = store.getResourcePendingFilePath( rKey );
    HuUploadState uploadState = huResource.getUploadState();
    
    try ( RandomAccessFile raf = new RandomAccessFile( pending.toFile(), "rw" ) )
    {
      raf.seek( mapchunk.getStart() );
      raf.write( chunkup.getChunk() );
    }

    int uploadChunkCount=0;
    for ( int i=0; i<uploadState.getChunkStates().size(); i++ )
    {
      if ( i == chunkup.getChunkNo() )
        uploadState.getChunkStates().get( i ).setUploaded( true );
      if ( uploadState.getChunkStates().get( i ).isUploaded() )
        uploadChunkCount++;
    }
    if ( uploadChunkCount == uploadState.getChunkStates().size() )
      uploadState.setFullyUploaded( true );
        
    store.updateResource( huResource );

    // Send another chunk request.
    HuBinaryChunkUploadReq upreq = getNextChunkRequest( huResource );
    if ( upreq != null )
    {
      upreq.setRecentChunkAck( chunkup.getChunkNo() );
      ToolMessage tm = new ToolMessage( message.getId(), HuServerMessageName.BinaryChunkUploadReq, upreq );
      sendToolMessage( session, tm );
    }
    else
    {
      // Send update for whole resource
      ToolMessage tm = new ToolMessage( message.getId(), HuServerMessageName.Resource, huResource );
      sendToolMessage( session, tm );
    }
  }
  
  
  /**
   * Client requested the resource data.
   * 
   * @param session The session this endpoint belongs to.
   * @param message The incoming message from the client end.
   * @throws IOException Indicates failure to process. 
   * @throws uk.ac.leedsbeckett.ltitoolset.websocket.HandlerAlertException 
   */
  @EndpointMessageHandler()
  public void handleGetResource( Session session, ToolMessage message ) throws IOException, HandlerAlertException
  {
    // Whether there is a resource for the session will depend on the facet 
    // being used.
    if ( !"item".equals( huState.getToolFacetId() ) )
      throw new HandlerAlertException( "Recieved request for resource data on an inappropriate facet of the tool.", message.getId() );
    HuResourceKey rKey = huState.getHuResourceKey();
    if ( rKey == null )
      throw new HandlerAlertException( "Recieved request for resource data but launch didn't provide ID for it.", message.getId() );
    
    // No further checks - send data about the resource.
    HugeUploadResource huResource = store.getResource( rKey, true );
    logger.log( Level.INFO, "Sending resource [{0}]", huResource.toString() );
    ToolMessage tm = new ToolMessage( message.getId(), HuServerMessageName.Resource, huResource );
    sendToolMessage( session, tm );
  }
  
  /**
   * Client requested the resource data.
   * 
   * @param session The session this endpoint belongs to.
   * @param message The incoming message from the client end.
   * @throws IOException Indicates failure to process. 
   * @throws uk.ac.leedsbeckett.ltitoolset.websocket.HandlerAlertException 
   */
  @EndpointMessageHandler()
  public void handleGetCourse( Session session, ToolMessage message ) throws IOException, HandlerAlertException
  {
    // Whether there is a resource for the session will depend on the facet 
    // being used.
    if ( !"course".equals( huState.getToolFacetId() ) )
      throw new HandlerAlertException( "Recieved request for course data on an inappropriate facet of the tool.", message.getId() );
    HuCourseKey cKey = huState.getHuCourseKey();
    if ( cKey == null )
      throw new HandlerAlertException( "Recieved request for course data but launch didn't provide ID for it.", message.getId() );
    
    // No further checks - send data about the resource.
    CourseConfiguration huCourse = store.getCourseConfiguration( cKey, true );
    logger.log( Level.INFO, "Sending course config [{0}]", huCourse.toString() );
    ToolMessage tm = new ToolMessage( message.getId(), HuServerMessageName.Course, huCourse );
    sendToolMessage( session, tm );
  }
  
  
  
  @EndpointMessageHandler()
  public void handleConfigurationRequest( Session session, ToolMessage message )
          throws IOException, HandlerAlertException
  {
    if ( !huState.isAllowedToConfigure() )
      throw new HandlerAlertException( "Recieved request for configuration from user who is not allowed to configure the tool.", message.getId() );
    
    logger.info( "Fetching config for platform " + platformName );
    Configuration config = tool.getPlatformConfig( platformName );
    ToolMessage tmf = new ToolMessage( message.getId(), HuServerMessageName.Configuration, new HuConfigurationMessage( config ) );
    sendToolMessage( session, tmf );
  }
  
  @EndpointMessageHandler()
  public void handleConfigure( Session session, ToolMessage message, HuConfigurationMessage configMessage )
          throws IOException, HandlerAlertException
  {
    if ( !huState.isAllowedToConfigure() )
      throw new HandlerAlertException( "Recieved request to save new configuration from user who is not allowed to configure the tool.", message.getId() );
            
    Configuration config = configMessage.getConfiguration();
    if ( config == null )
      throw new HandlerAlertException( "Null configuration was received.", message.getId() );
    
    try
    {  
      tool.savePlatformConfig( platformName, config);
    }
    catch ( Exception e )
    {
      logger.log( Level.SEVERE, "Unable to save configuration for platform " + platformName, e );
      throw new HandlerAlertException( "Unable to save configuration.", message.getId() );
    }
    
    ToolMessage tmf = new ToolMessage( message.getId(), HuServerMessageName.ConfigurationSuccess, "Saved" );
    sendToolMessage( session, tmf );
    
    // To do - send message to all users now accessing tool from the same platform
    // for now just for confirmation to current user.
    ToolMessage tmc = new ToolMessage( message.getId(), HuServerMessageName.Configuration, new HuConfigurationMessage( config ) );
    sendToolMessage( session, tmc );
  }
  
  
  
  /**
   * This gets called when a handler throws a HandlerAlertException and decides
   * how to alert the user.
   * 
   * @param session The web socket session.
   * @param haex The exception that was thrown.
   * @throws IOException If the attempt to alert the user fails.
   */
  @Override
  public void processHandlerAlert( Session session, HandlerAlertException haex ) throws IOException
  {
    sendToolMessage( session, new ToolMessage( haex.getMessageId(), HuServerMessageName.Alert, haex.getMessage() ) );    
  }
}
