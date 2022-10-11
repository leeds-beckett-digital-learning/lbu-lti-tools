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
package uk.ac.leedsbeckett.ltitools.peergroupassessment;

import uk.ac.leedsbeckett.ltitools.peergroupassessment.resourcedata.PgaProperties;
import uk.ac.leedsbeckett.ltitools.peergroupassessment.resourcedata.PeerGroupResource;
import uk.ac.leedsbeckett.ltitools.peergroupassessment.store.StoreCluster;
import uk.ac.leedsbeckett.ltitools.peergroupassessment.inputdata.PeerGroupData;
import uk.ac.leedsbeckett.ltitools.peergroupassessment.formdata.PeerGroupForm;
import uk.ac.leedsbeckett.ltitools.peergroupassessment.messagedata.PgaChangeGroup;
import uk.ac.leedsbeckett.ltitools.peergroupassessment.messagedata.PgaFormAndData;
import uk.ac.leedsbeckett.ltitools.peergroupassessment.messagedata.PgaAddMembership;
import uk.ac.leedsbeckett.ltitools.peergroupassessment.inputdata.PeerGroupDataKey;
import uk.ac.leedsbeckett.ltitools.peergroupassessment.messagedata.PgaChangeDatum;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import uk.ac.leedsbeckett.ltitoolset.websocket.ToolEndpoint;
import uk.ac.leedsbeckett.ltitoolset.websocket.ToolMessage;
import uk.ac.leedsbeckett.ltitoolset.websocket.ToolMessageDecoder;
import uk.ac.leedsbeckett.ltitoolset.websocket.ToolMessageEncoder;
import uk.ac.leedsbeckett.ltitoolset.websocket.annotations.EndpointMessageHandler;
import uk.ac.leedsbeckett.ltitools.peergroupassessment.messagedata.Id;
import uk.ac.leedsbeckett.ltitools.peergroupassessment.resourcedata.PeerGroupResource.Group;
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
        value="/socket/peergroupassessment", 
        decoders=ToolMessageDecoder.class, 
        encoders=ToolMessageEncoder.class )
@EndpointJavascriptProperties(
        module="peergroupassessment",
        prefix="Pga",
        messageEnum="uk.ac.leedsbeckett.ltitools.peergroupassessment.PgaServerMessageName"
)
public class PgaEndpoint extends ToolEndpoint
{
  static final Logger logger = Logger.getLogger(PgaEndpoint.class.getName() );
  
  PeerGroupAssessmentTool tool;
  PgaToolLaunchState pgaState;
  StoreCluster store;
  PeerGroupForm defaultForm;
  PeerGroupResource pgaResource;
  

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
    
    pgaState = (PgaToolLaunchState)getState().getToolLaunchState();
    tool = (PeerGroupAssessmentTool)getToolCoordinator().getTool( getState().getToolKey() );
    store = tool.getPeerGroupAssessmentStore();
    pgaResource = store.getResource( pgaState.getResourceKey(), true );
    defaultForm = store.getDefaultForm();
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

  /**
   * Simply passes on responsibility for processing to the super-class.
   * 
   * @param session The session this endpoint belongs to.
   * @param message The incoming message from the client end.
   * @throws IOException Indicates failure to process.
   */
  @OnMessage
  @Override
  public void onMessage(Session session, ToolMessage message) throws IOException
  {
    super.onMessage( session, message );
  }

  /**
   * Client requested the resource data.
   * 
   * @param session The session this endpoint belongs to.
   * @param message The incoming message from the client end.
   * @throws IOException Indicates failure to process. 
   */
  @EndpointMessageHandler()
  public void handleGetResource( Session session, ToolMessage message ) throws IOException
  {
    logger.log( Level.INFO, "Sending resource [{0}]", pgaResource.getTitle() );
    sendToolMessage( session, new ToolMessage( message.getId(), PgaServerMessageName.Resource, pgaResource ) );
  }
  
  /**
   * The client wants to set the basic properties of the peer group assessment
   * resource.
   * 
   * @param session  The session this endpoint belongs to.
   * @param message The incoming message from the client end.
   * @param p The PGA properties. (Title etc...)
   * @throws IOException Indicates failure to process. 
   */
  @EndpointMessageHandler()
  public void handleSetResourceProperties( Session session, ToolMessage message, PgaProperties p ) throws IOException
  {
    logger.log( Level.INFO, "State       [{0}]", p.getStage().toString() );
    logger.log( Level.INFO, "Title       [{0}]", p.getTitle() );
    logger.log( Level.INFO, "Description [{0}]", p.getDescription() );
    pgaResource.setProperties( p );
    try
    {
      store.updateResource( pgaResource );
      ToolMessage tm = new ToolMessage( message.getId(), PgaServerMessageName.ResourceProperties, pgaResource.getProperties() );
      sendToolMessageToResourceUsers( tm );
    }
    catch ( IOException e )
    {
      logger.log(  Level.SEVERE, "Unable to store changes.", e );
    }
  }  

  /**
   * The client wants to change the properties of a group.
   * 
   * @param session The session this endpoint belongs to.
   * @param message The incoming message from the client end.
   * @param p The group id and desired group properties.
   * @throws IOException Indicates failure to process. 
   */
  @EndpointMessageHandler()
  public void handleSetGroupProperties( Session session, ToolMessage message, PgaChangeGroup p ) throws IOException
  {
    logger.log( Level.INFO, "ID [{0}]",       p.getId() );
    logger.log( Level.INFO, "Title [{0}]",    p.getTitle() );
    Group g = pgaResource.getGroupById( p.getId() );
    if ( g != null )
    {
      logger.log( Level.INFO, "Member count [{0}]", Integer.toString( g.getMembers().size() ) );
      g.setTitle( p.getTitle() );
      logger.log( Level.INFO, "Member count [{0}]", Integer.toString( g.getMembers().size() ) );
      try
      {
        store.updateResource( pgaResource );
        logger.log( Level.INFO, "Member count [{0}]", Integer.toString( g.getMembers().size() ) );
        PgaChangeGroup change = new PgaChangeGroup( g.getId(), g.getTitle() );
        ToolMessage tm = new ToolMessage( message.getId(), PgaServerMessageName.Group, change );
        sendToolMessageToResourceUsers( tm );
      }
      catch ( IOException e )
      {
        logger.log(  Level.SEVERE, "Unable to store changes.", e );
      }
    }
  }
  
  /**
   * The client wants a new group to be created within the PGA.
   * 
   * @param session The session this endpoint belongs to.
   * @param message The incoming message from the client end.
   * @throws IOException Indicates failure to process. 
   */
  @EndpointMessageHandler()
  public void handleAddGroup( Session session, ToolMessage message ) throws IOException
  {
    Group g = pgaResource.addGroup( "New Group" );
    if ( g != null )
    {
      try
      {
        store.updateResource( pgaResource );
        PgaChangeGroup p = new PgaChangeGroup( g.getId(), g.getTitle() );
        ToolMessage tm = new ToolMessage( message.getId(), PgaServerMessageName.Group, p );
        sendToolMessageToResourceUsers( tm );
      }
      catch ( IOException e )
      {
        logger.log(  Level.SEVERE, "Unable to store changes.", e );
      }
    }
  }  
  
  /**
   * The client wants to add participants to a specified group. Participants
   * will be removed from any other groups. Should be possible to use this to
   * move people into the 'unattached' group. (To do).
   * 
   * @param session The session this endpoint belongs to.
   * @param message The incoming message from the client end.
   * @param m The ID of the group and the IDs of participants.
   * @throws IOException Indicates failure to process. 
   */
  @EndpointMessageHandler()
  public void handleMembership( Session session, ToolMessage message, PgaAddMembership m ) throws IOException
  {
    logger.log( Level.INFO, "Id   [{0}]",       m.getId() );
    try
    {
      pgaResource.addMemberships( m );
      store.updateResource( pgaResource );
      logger.log( Level.INFO, "Sending resource [{0}]", pgaResource.getTitle() );
      ToolMessage tm = new ToolMessage( message.getId(), PgaServerMessageName.Resource, pgaResource );
      sendToolMessageToResourceUsers( tm );
    }
    catch ( IOException e )
    {
      logger.log(  Level.SEVERE, "Unable to store changes.", e );
    }
  }
  
  /**
   * The user wants copies of the form associated with this PGA and the 
   * user data for a specific group.
   * 
   * @param session The session this endpoint belongs to.
   * @param message The incoming message from the client end.
   * @param gid The ID of the desired group.
   * @throws IOException Indicates failure to process. 
   */
  @EndpointMessageHandler()
  public void handleGetFormAndData( Session session, ToolMessage message, Id gid ) throws IOException
  {
    logger.log( Level.INFO, "handleGetFormAndData" );
    PeerGroupDataKey key = new PeerGroupDataKey( pgaResource.getKey(), gid.getId() );
    PgaFormAndData fad = new PgaFormAndData( defaultForm, store.getData( key, true ) );
    sendToolMessage( session, new ToolMessage( message.getId(), PgaServerMessageName.FormAndData, fad ) );
  }
  
  /**
   * The client wants to record user data.
   * 
   * @param session The session this endpoint belongs to.
   * @param message The incoming message from the client end.
   * @param datum IDs that identify where to change data and the data value.
   * @throws IOException Indicates failure to process. 
   */
  @EndpointMessageHandler()
  public void handleChangeDatum( Session session, ToolMessage message, PgaChangeDatum datum ) throws IOException
  {
    logger.log( 
            Level.INFO, 
            "handleChangeDatum() {0} {1} {2} {3}", 
            new Object[ ]{ datum.getGroupId(), datum.getFieldId(), datum.getMemberId(), datum.getValue() } );
    
    PeerGroupDataKey key = new PeerGroupDataKey( pgaResource.getKey(), datum.getGroupId() );
    PeerGroupData data = store.getData( key, true );
    data.setParticipantDatum( datum );
    store.updateData( data );
    
    // Tell all current clients the data for this group. (Even though most of them
    // will not be interested in this group.)
    ToolMessage tm = new ToolMessage( message.getId(), PgaServerMessageName.Data, data );
    sendToolMessageToResourceUsers( tm );
  }  
}
