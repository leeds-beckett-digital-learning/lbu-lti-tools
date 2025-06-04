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
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import org.apache.commons.codec.digest.DigestUtils;
import uk.ac.leedsbeckett.ltitools.hugeupload.data.Configuration;
import uk.ac.leedsbeckett.ltitools.hugeupload.data.CourseConfiguration;
import uk.ac.leedsbeckett.ltitools.hugeupload.data.HuCourseKey;
import uk.ac.leedsbeckett.ltitools.hugeupload.data.HuFileMetadata;
import uk.ac.leedsbeckett.ltitools.hugeupload.data.HuFileMetadataKey;
import uk.ac.leedsbeckett.ltitools.hugeupload.data.HuResourceKey;
import uk.ac.leedsbeckett.ltitools.hugeupload.data.HugeUploadResource;
import uk.ac.leedsbeckett.ltitools.hugeupload.messagedata.AcknowledgeUpload;
import uk.ac.leedsbeckett.ltitools.hugeupload.messagedata.HuBinaryChunkDownload;
import uk.ac.leedsbeckett.ltitools.hugeupload.messagedata.HuBinaryChunkDownloadRequest;
import uk.ac.leedsbeckett.ltitools.hugeupload.messagedata.HuBinaryChunkUpload;
import uk.ac.leedsbeckett.ltitools.hugeupload.messagedata.HuBinaryChunkUploadAck;
import uk.ac.leedsbeckett.ltitools.hugeupload.messagedata.HuBinaryChunkUploadRequest;
import uk.ac.leedsbeckett.ltitools.hugeupload.messagedata.HuBinaryTestMessage;
import uk.ac.leedsbeckett.ltitools.hugeupload.messagedata.HuConfigurationMessage;
import uk.ac.leedsbeckett.ltitools.hugeupload.messagedata.HuFileMap;
import uk.ac.leedsbeckett.ltitools.hugeupload.messagedata.HuFileMapChunk;
import uk.ac.leedsbeckett.ltitools.hugeupload.messagedata.HuFileMapComplete;
import uk.ac.leedsbeckett.ltitools.hugeupload.messagedata.HuFileMapProgress;
import uk.ac.leedsbeckett.ltitools.hugeupload.messagedata.HuFileMapStart;
import uk.ac.leedsbeckett.ltitools.hugeupload.messagedata.HuFileUploadProgress;
import uk.ac.leedsbeckett.ltitools.hugeupload.messagedata.HuFileUploadStart;
import uk.ac.leedsbeckett.ltitools.hugeupload.messagedata.HuUploadState;
import uk.ac.leedsbeckett.ltitoolset.websocket.ToolEndpoint;
import uk.ac.leedsbeckett.ltitoolset.websocket.ToolMessage;
import uk.ac.leedsbeckett.ltitoolset.websocket.annotations.EndpointMessageHandler;
import uk.ac.leedsbeckett.ltitoolset.websocket.HandlerAlertException;
import uk.ac.leedsbeckett.ltitoolset.websocket.HandlerException;
import uk.ac.leedsbeckett.ltitoolset.websocket.annotations.EndpointJavascriptProperties;
import uk.ac.leedsbeckett.ltitoolset.websocket.annotations.HandlerPromisesReply;

/**
 * The web socket server endpoint that implements all the logic of this tool.
 * It is annotated in two ways - so that the container (e.g. tomcat) will map
 * URLs on to the class - and so that the build process can create javascript
 * for the client side of the web socket.
 * 
 * @author maber01
 */
@ServerEndpoint( value="/socket/hugeupload" )
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

  
  private boolean validateSha1( byte[] data, String checkSum )
  {
    try
    {
      MessageDigest md = MessageDigest.getInstance("SHA-1");
      md.update( data );
      byte[] digest = md.digest();
      return checkSum.equals( Base64.getEncoder().encodeToString( digest ) );
    }
    catch ( NoSuchAlgorithmException ex )
    {
      Logger.getLogger( HugeUploadEndpoint.class.getName() ).log( Level.SEVERE, null, ex );
      return false;
    }
  }
  
  
  class ItemData
  {
    HuResourceKey rKey;
    HugeUploadResource huResource;
    HuFileMetadataKey fkey;
    HuFileMetadata fmdata;
    public ItemData( ToolMessage message, String fileName ) throws HandlerAlertException
    {
      if ( !"item".equals( huState.getToolFacetId() ) )
        throw new HandlerAlertException( "Recieved message on an inappropriate facet of the tool.", message );
      rKey = huState.getHuResourceKey();
      if ( rKey == null )
        throw new HandlerAlertException( "Recieved message but cannot find corresponding resource data.", message );
      huResource = store.getResource( rKey, true );
      if ( huResource == null )
        throw new HandlerAlertException( "Unable to find resource data.", message );
      if ( fileName != null )
      {
        fkey = new HuFileMetadataKey( rKey, fileName );
        fmdata = store.getFileMetadata( fkey, true );
      }
    }
  }
  
  @EndpointMessageHandler()
  @HandlerPromisesReply()
  public void handleFileMapStart( Session session, ToolMessage message, HuFileMapStart fileMap ) 
          throws IOException, HandlerException
  {
    if ( fileMap == null || fileMap.getFileName() == null )
      throw new HandlerException( "Invalid payload in message.", message );
    ItemData d = new ItemData( message, fileMap.getFileName() );

    logger.log(Level.FINE, "Rxed replacement? {0} name = {1}", new Object[ ]{fileMap.isDuplicate(), fileMap.getFileName() });

    if ( fileMap.isDuplicate() && d.fmdata.getFileMap() == null )
      throw new HandlerException( "Duplicate was indicated but there is no existing map.", message );
    
    HuFileMap newMap = new HuFileMap();
    // Initialise a new map
    newMap.setMap( new ArrayList<>() );
    newMap.setDuplicate( fileMap.isDuplicate() );
    d.fmdata.setNewFileMap( newMap );
    store.updateFileMetadata( d.fmdata );
    sendToolMessage( session, new ToolMessage( message, HuServerMessageName.Acknowledge ) );
  }
  
  @EndpointMessageHandler()
  @HandlerPromisesReply()
  public void handleFileMapProgress( Session session, ToolMessage message, HuFileMapProgress fileMap )
          throws IOException, HandlerException
  {
    if ( fileMap == null || fileMap.getFileName() == null )
      throw new HandlerException( "Invalid payload in message.", message );
    ItemData d = new ItemData( message, fileMap.getFileName() );
    logger.log(Level.FINE, "Rxed chunkNumber {0} hash = {1}", new Object[ ]{fileMap.getChunkNumber(), fileMap.getChunk().getHash()});
    
    HuFileMap newmap = d.fmdata.getNewFileMap();
    HuFileMapChunk newchunk = fileMap.getChunk();
    if ( newmap.isDuplicate() )
    {
      logger.log( Level.FINE, "Checking that this chunk matches previously mapped one." );
      HuFileMap oldmap = d.fmdata.getFileMap();
      HuFileMapChunk oldchunk = oldmap.getMap().get( fileMap.getChunkNumber() );
      logger.log(Level.FINE, "Old chunk.{0}", oldchunk);
      logger.log(Level.FINE, "New chunk.{0}", newchunk);
      if ( oldchunk != null && !oldchunk.equals( newchunk ) )
        throw new HandlerException( "The selected file does not match the previously mapped file.", message );
    }
    
    while ( newmap.getMap().size() <= fileMap.getChunkNumber() )
      newmap.getMap().add( null );
    newmap.getMap().set( fileMap.getChunkNumber(), newchunk );
    store.updateFileMetadata( d.fmdata );    
    sendToolMessage( session, new ToolMessage( message, HuServerMessageName.Acknowledge ) );
  }
  
  @EndpointMessageHandler()
  @HandlerPromisesReply()
  public void handleFileMapComplete( Session session, ToolMessage message, HuFileMapComplete fileMap )
          throws IOException, HandlerException
  {
    if ( fileMap == null || fileMap.getFileName() == null )
      throw new HandlerException( "Invalid payload in message.", message );
    ItemData d = new ItemData( message, fileMap.getFileName() );
    logger.log(Level.FINE, "name = {0} digest = {1}", new Object[ ]{fileMap.getFileName(), fileMap.getWholeFileDigest() });
    HuFileMap newmap = d.fmdata.getNewFileMap();
    
    // Check the whole file digest against previous import.
    if ( newmap.isDuplicate() )
    {
      HuFileMap oldmap = d.fmdata.getFileMap();
      if ( !oldmap.getSha512digest().equals( fileMap.getWholeFileDigest() ) )
        throw new HandlerAlertException( "The selected file does not match the previously mapped file.", message );
    }

    newmap.setSha512digest( fileMap.getWholeFileDigest() );
    d.fmdata.setFileMap( newmap );
    d.fmdata.setNewFileMap( null );
    store.updateFileMetadata( d.fmdata );    
    sendToolMessage( session, new ToolMessage( message, HuServerMessageName.Acknowledge ) );
  }
  
  @EndpointMessageHandler()
  @HandlerPromisesReply()
  public void handleFileUploadStart( Session session, ToolMessage message, HuFileUploadStart upStart ) 
          throws IOException, HandlerException
  {
    if ( upStart == null || upStart.getFileName() == null )
      throw new HandlerException( "Invalid payload in message.", message );
    ItemData d = new ItemData( message, upStart.getFileName() );

    logger.log(Level.FINE, "Starting upload {0} name = {1}", new Object[ ]{upStart.getFileName() });
    
    HuUploadState upstate = d.fmdata.getUploadState();
    if ( upstate == null )
    {
      upstate = new HuUploadState();
      d.fmdata.setUploadState( upstate );
      store.updateFileMetadata( d.fmdata );
    }
    
    // Delete existing upload.
    Path path = store.getFilePath( d.fkey, upStart.getFileName() );    
    if ( Files.exists( path ) )
      Files.delete( path );
    
    if ( upstate.isFullyUploaded() )
      throw new HandlerException( "Already fully uploaded.", message );
    
    for ( int i=0; i < d.fmdata.getFileMap().getMap().size(); i++ )
      if ( !upstate.isChunkUploaded( i ) )
      {
        HuFileMapChunk chunk = d.fmdata.getFileMap().getMap().get( i );
        sendToolMessage( session, new ToolMessage( message, HuServerMessageName.AcknowledgeUpload, 
                new AcknowledgeUpload( false, i, chunk.getStart(), chunk.getEnd() ) ) );    
        return;
      }
    
    sendToolMessage( session, new ToolMessage( message, HuServerMessageName.AcknowledgeUpload, 
            new AcknowledgeUpload( true, null, null, null ) ) );    
  }  
  
  @EndpointMessageHandler()
  @HandlerPromisesReply()
  public void handleFileUploadProgress( Session session, ToolMessage message, HuFileUploadProgress upProgress )
          throws IOException, HandlerException
  {
    if ( upProgress == null || upProgress.getFileName() == null )
      throw new HandlerException( "Invalid payload in message.", message );
    ItemData d = new ItemData( message, upProgress.getFileName() );
    logger.log(Level.FINE, "Rxed chunkNumber {0} filename = {1}", new Object[ ]{upProgress.getChunkNumber(), upProgress.getFileName()});
  
    // Right data? Matches map?
    HuFileMapChunk chunkMap = d.fmdata.getFileMap().getMap().get( upProgress.getChunkNumber() );
    if ( !this.validateSha1( upProgress.getData(), chunkMap.getHash() ) )
      throw new HandlerException( "Checksum of this chunk doesn't match the imported file.", message );
    logger.log( Level.FINE, "Chunk passed checksum test." );
    
    // Save the data
    Path path = store.getFilePath( d.fkey, upProgress.getFileName() );    
    try ( RandomAccessFile raf = new RandomAccessFile( path.toFile(), "rw" ) )
    {
      logger.log(Level.FINE, "Seeking to {0}", chunkMap.getStart());
      logger.log(Level.FINE, "Buffer length {0}", upProgress.getData().length);
      raf.seek( chunkMap.getStart() );
      raf.write( upProgress.getData() );
    }
    
    HuUploadState upstate = d.fmdata.getUploadState();
    upstate.setUploadedChunkCount( upProgress.getChunkNumber() + 1 );
    // Save this now even though we might save again later
    if ( upstate.getUploadedChunkCount() == d.fmdata.getFileMap().getMap().size() )
      upstate.setFullyUploaded( true );
    store.updateFileMetadata( d.fmdata );
    

    if ( upstate.isFullyUploaded() )
    {
      String strDigest=null;
      // Now compute the whole file hash.
      try ( InputStream is = Files.newInputStream( path ) )
      {
        long t1 = System.currentTimeMillis();
        strDigest = DigestUtils.sha512Hex( is );
        long t2 = System.currentTimeMillis();
        logger.log( Level.FINE, "Time taken to compute file digest: {0}ms", t2-t1 );
      }
      catch ( Exception e )
      {
        logger.log( Level.SEVERE, "Unable to compute digest.", e );
        throw new HandlerException( "Unable to compute digest.", message );
      }
      logger.log( Level.FINE, "File digest from client   : {0}", d.fmdata.getFileMap().getSha512digest() );
      logger.log( Level.FINE, "File digest just computed : {0}", strDigest );
      if ( !d.fmdata.getFileMap().getSha512digest().equals( strDigest ) )
        throw new HandlerException( "The uploaded file's fingerprint doesn't match the file that was mapped before uploading.", message );

      // No more chunks needed, upload is complete
      upstate.setWholeFileFingerprintValidated( true );
      store.updateFileMetadata( d.fmdata );
      sendToolMessage( session, new ToolMessage( message, HuServerMessageName.AcknowledgeUpload, 
              new AcknowledgeUpload( true, null, null, null ) ) );    
    }
    else
    {
      // tell client about the next chunk that ought to be uploaded
      HuFileMapChunk chunk = d.fmdata.getFileMap().getMap().get( upstate.getUploadedChunkCount() );
      sendToolMessage( session, new ToolMessage( message, HuServerMessageName.AcknowledgeUpload, 
              new AcknowledgeUpload( false, upstate.getUploadedChunkCount(), chunk.getStart(), chunk.getEnd() ) ) );    
    }

  }
    
  @EndpointMessageHandler()
  public void handleBinaryChunk( Session session, ToolMessage message, HuBinaryChunkUpload chunkup ) 
          throws IOException, HandlerAlertException
  {
    /*
    
    
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
    HuBinaryChunkUploadRequest upreq = getNextChunkRequest( huResource );
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
    
    */
  }
  
  @EndpointMessageHandler()
  public void handleBinaryChunkDownloadRequest( Session session, ToolMessage message, HuBinaryChunkDownloadRequest downreq ) 
          throws IOException, HandlerAlertException
  {
    /*
    // Whether there is a resource for the session will depend on the facet 
    // being used.
    if ( !"item".equals( huState.getToolFacetId() ) )
      throw new HandlerAlertException( "Recieved message on an inappropriate facet of the tool.", message.getId() );
    if ( downreq == null )
      throw new HandlerAlertException( "No payload in download request.", message.getId() );
    logger.log( Level.INFO, "chunk no = {0}", downreq.getChunkNo() );
    HuResourceKey rKey = huState.getHuResourceKey();
    if ( rKey == null )
      throw new HandlerAlertException( "Cannot find resource data.", message.getId() );

    HugeUploadResource huResource = store.getResource( rKey, true );
    if ( huResource == null )
      throw new HandlerAlertException( "Unable to find resource data.", message.getId() );

    HuFileMapChunk mapchunk = huResource.getFileMap().getMap().get( downreq.getChunkNo() );
    Path pending = store.getResourcePendingFilePath( rKey );
    HuUploadState uploadState = huResource.getUploadState();
    if ( !uploadState.getChunkStates().get( downreq.getChunkNo() ).isUploaded() )
      throw new HandlerAlertException( "Requested chunk has not be uploaded so cannot be downloaded.", message.getId() );
    
    byte[] buffer = new byte[ (int)(mapchunk.getEnd() - mapchunk.getStart()) ];
    try ( RandomAccessFile raf = new RandomAccessFile( pending.toFile(), "r" ) )
    {
      raf.seek( mapchunk.getStart() );
      raf.read( buffer );
    }
    
    HuBinaryChunkDownload chunkdown = new HuBinaryChunkDownload();
    chunkdown.setId( "er...." );
    chunkdown.setChunkNo( downreq.getChunkNo() );
    chunkdown.setChunk( buffer );
    ToolMessage tm = new ToolMessage( message.getId(), HuServerMessageName.BinaryChunkDownload, chunkdown );
    sendToolMessage( session, tm );
    */
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
      throw new HandlerAlertException( "Recieved request for resource data on an inappropriate facet of the tool.", message );
    HuResourceKey rKey = huState.getHuResourceKey();
    if ( rKey == null )
      throw new HandlerAlertException( "Recieved request for resource data but launch didn't provide ID for it.", message );
    
    // No further checks - send data about the resource.
    HugeUploadResource huResource = store.getResource( rKey, true );
    logger.log( Level.INFO, "Sending resource [{0}]", huResource.toString() );
    ToolMessage tm = new ToolMessage( message, HuServerMessageName.Resource, huResource );
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
      throw new HandlerAlertException( "Recieved request for course data on an inappropriate facet of the tool.", message );
    HuCourseKey cKey = huState.getHuCourseKey();
    if ( cKey == null )
      throw new HandlerAlertException( "Recieved request for course data but launch didn't provide ID for it.", message );
    
    // No further checks - send data about the resource.
    CourseConfiguration huCourse = store.getCourseConfiguration( cKey, true );
    logger.log( Level.INFO, "Sending course config [{0}]", huCourse.toString() );
    ToolMessage tm = new ToolMessage( message, HuServerMessageName.Course, huCourse );
    sendToolMessage( session, tm );
  }
  
  
  
  @EndpointMessageHandler()
  public void handleConfigurationRequest( Session session, ToolMessage message )
          throws IOException, HandlerAlertException
  {
    if ( !huState.isAllowedToConfigure() )
      throw new HandlerAlertException( "Recieved request for configuration from user who is not allowed to configure the tool.", message );
    
    logger.info( "Fetching config for platform " + platformName );
    Configuration config = tool.getPlatformConfig( platformName );
    ToolMessage tmf = new ToolMessage( message, HuServerMessageName.Configuration, new HuConfigurationMessage( config ) );
    sendToolMessage( session, tmf );
  }
  
  @EndpointMessageHandler()
  public void handleConfigure( Session session, ToolMessage message, HuConfigurationMessage configMessage )
          throws IOException, HandlerAlertException
  {
    if ( !huState.isAllowedToConfigure() )
      throw new HandlerAlertException( "Recieved request to save new configuration from user who is not allowed to configure the tool.", message );
            
    Configuration config = configMessage.getConfiguration();
    if ( config == null )
      throw new HandlerAlertException( "Null configuration was received.", message );
    
    try
    {  
      tool.savePlatformConfig( platformName, config);
    }
    catch ( Exception e )
    {
      logger.log( Level.SEVERE, "Unable to save configuration for platform " + platformName, e );
      throw new HandlerAlertException( "Unable to save configuration.", message );
    }
    
    ToolMessage tmf = new ToolMessage( message, HuServerMessageName.ConfigurationSuccess, "Saved" );
    sendToolMessage( session, tmf );
    
    // To do - send message to all users now accessing tool from the same platform
    // for now just for confirmation to current user.
    ToolMessage tmc = new ToolMessage( message, HuServerMessageName.Configuration, new HuConfigurationMessage( config ) );
    sendToolMessage( session, tmc );
  }
  
}
