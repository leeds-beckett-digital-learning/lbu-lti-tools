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
package uk.ac.leedsbeckett.ltitools.tool.peergroupassessment;

import uk.ac.leedsbeckett.ltitools.tool.peergroupassessment.data.PeerGroupResourceProperties;
import uk.ac.leedsbeckett.ltitools.tool.peergroupassessment.data.PeerGroupResource;
import uk.ac.leedsbeckett.ltitools.tool.peergroupassessment.store.PeerGroupAssessmentStore;
import uk.ac.leedsbeckett.ltitools.tool.peergroupassessment.data.PeerGroupData;
import uk.ac.leedsbeckett.ltitools.tool.peergroupassessment.data.PeerGroupForm;
import uk.ac.leedsbeckett.ltitools.tool.peergroupassessment.data.PeerGroupChangeGroup;
import uk.ac.leedsbeckett.ltitools.tool.peergroupassessment.data.PeerGroupFormAndData;
import uk.ac.leedsbeckett.ltitools.tool.peergroupassessment.data.PeerGroupAddMembership;
import uk.ac.leedsbeckett.ltitools.tool.peergroupassessment.data.PeerGroupDataKey;
import uk.ac.leedsbeckett.ltitools.tool.peergroupassessment.data.PeerGroupChangeDatum;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import uk.ac.leedsbeckett.ltitools.app.ApplicationContext;
import uk.ac.leedsbeckett.ltitools.state.AppLtiState;
import uk.ac.leedsbeckett.ltitools.tool.peergroupassessment.data.Id;
import uk.ac.leedsbeckett.ltitools.tool.peergroupassessment.data.PeerGroupResource.Group;
import uk.ac.leedsbeckett.ltitools.tool.websocket.ToolEndpoint;
import uk.ac.leedsbeckett.ltitools.tool.websocket.ToolMessage;
import uk.ac.leedsbeckett.ltitools.tool.websocket.ToolMessageDecoder;
import uk.ac.leedsbeckett.ltitools.tool.websocket.ToolMessageEncoder;
import uk.ac.leedsbeckett.ltitools.tool.websocket.annotations.EndpointMessageHandler;

/**
 *
 * @author maber01
 */
@ServerEndpoint( 
        value="/socket/peergroupassessment", 
        decoders=ToolMessageDecoder.class, 
        encoders=ToolMessageEncoder.class )
public class PeerGroupAssessmentEndpoint extends ToolEndpoint
{
  static final Logger logger = Logger.getLogger( PeerGroupAssessmentEndpoint.class.getName() );

  AppLtiState state;
  ApplicationContext appcontext;
  PeerGroupAssessmentTool tool;
  PeerGroupAssessmentState pgaState;
  PeerGroupAssessmentStore store;
  PeerGroupForm defaultForm;
  PeerGroupResource pgaResource;
  
  
  @OnOpen
  public void onOpen(Session session) throws IOException
  {
    logger.info( "Opened websocket session uri = " + session.getRequestURI().toASCIIString() );    
    appcontext = ApplicationContext.getFromWebSocketContainer( session.getContainer() );
    List<String> list = session.getRequestParameterMap().get( "state" );
    if ( list != null )
    {
      String stateid = list.get( 0 );
      logger.log(Level.INFO, "State ID = {0}", stateid);
      state = appcontext.getStateStore().getState( stateid );
      logger.info( state.getPersonName() );
      pgaState = (PeerGroupAssessmentState)state.getAppSessionState();
      tool = (PeerGroupAssessmentTool)appcontext.getTool( state.getToolKey() );
      store = tool.getPeerGroupAssessmentStore();
      pgaResource = store.getResource( pgaState.getResourceKey(), true );
      appcontext.addWsSession( pgaState.getResourceKey(), session );
      defaultForm = store.getDefaultForm();
    }
  }
  
  @OnClose
  public void onClose(Session session) throws IOException
  {
    logger.info( "Closed websocket session " );
    appcontext.removeWsSession( pgaState.getResourceKey(), session );
  }

  @OnError
  public void onError(Session session, Throwable throwable)
  {
    logger.log( Level.SEVERE, "Web socket error.", throwable );
  }  

  

  
  @OnMessage
  public void onMessage(Session session, ToolMessage message) throws IOException
  {
    logger.info( "Rx Message" );
    if ( !message.isValid() )
    {
      logger.severe( "Endpoint received invalid message: " + message.getRaw() );
      return;
    }
    logger.info( message.getRaw() );
    
    if ( dispatchMessage( session, message ) )
      return;

    logger.log( Level.INFO, "Did not find handler for message." );
  }


  @EndpointMessageHandler()
  public void handleGetResource( Session session, ToolMessage message ) throws IOException
  {
    logger.log( Level.INFO, "Sending resource [{0}]", pgaResource.getTitle() );
    ToolMessage tm = new ToolMessage( message.getId(), "resource", pgaResource );
    session.getAsyncRemote().sendObject( tm );
  }
  
  @EndpointMessageHandler(name = "setresourceproperties")
  public void handleSetResourceProperties( Session session, ToolMessage message, PeerGroupResourceProperties p ) throws IOException
  {
    logger.log( Level.INFO, "State       [{0}]", p.getStage().toString() );
    logger.log( Level.INFO, "Title       [{0}]", p.getTitle() );
    logger.log( Level.INFO, "Description [{0}]", p.getDescription() );
    pgaResource.setProperties( p );
    try
    {
      store.updateResource( pgaResource );
      ToolMessage tm = new ToolMessage( message.getId(), "resourceproperties", pgaResource.getProperties() );
      sendToolMessageToResourceUsers( tm );
    }
    catch ( IOException e )
    {
      logger.log(  Level.SEVERE, "Unable to store changes.", e );
    }
  }  

  @EndpointMessageHandler(name = "setgroupproperties")
  public void handleSetGroupProperties( Session session, ToolMessage message, PeerGroupChangeGroup p ) throws IOException
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
        ToolMessage tm = new ToolMessage( message.getId(), "group", g );
        sendToolMessageToResourceUsers( tm );
      }
      catch ( IOException e )
      {
        logger.log(  Level.SEVERE, "Unable to store changes.", e );
      }
    }
  }
  
  @EndpointMessageHandler(name = "addgroup")
  public void handleAddGroup( Session session, ToolMessage message ) throws IOException
  {
    Group g = pgaResource.addGroup( "New Group" );
    if ( g != null )
    {
      try
      {
        store.updateResource( pgaResource );
        PeerGroupChangeGroup p = new PeerGroupChangeGroup( g.getId(), g.getTitle() );
        ToolMessage tm = new ToolMessage( message.getId(), "group", p );
        sendToolMessageToResourceUsers( tm );
      }
      catch ( IOException e )
      {
        logger.log(  Level.SEVERE, "Unable to store changes.", e );
      }
    }
  }  
  
  
  @EndpointMessageHandler(name = "membership")
  public void handleMembership( Session session, ToolMessage message, PeerGroupAddMembership m ) throws IOException
  {
    logger.log( Level.INFO, "Id   [{0}]",       m.getId() );
    try
    {
      pgaResource.addMemberships( m );
      store.updateResource( pgaResource );
      logger.log( Level.INFO, "Sending resource [{0}]", pgaResource.getTitle() );
      ToolMessage tm = new ToolMessage( message.getId(), "resource", pgaResource );
      sendToolMessageToResourceUsers( tm );
    }
    catch ( IOException e )
    {
      logger.log(  Level.SEVERE, "Unable to store changes.", e );
    }
  }
  
  @EndpointMessageHandler(name = "getformanddata")
  public void handleGetFormAndData( Session session, ToolMessage message, Id gid ) throws IOException
  {
    logger.log( Level.INFO, "handleGetFormAndData" );
    PeerGroupDataKey key = new PeerGroupDataKey( pgaResource.getKey(), gid.getId() );
    PeerGroupFormAndData fad = new PeerGroupFormAndData( defaultForm, store.getData( key, true ) );
    ToolMessage tm = new ToolMessage( message.getId(), "formanddata", fad );
    session.getAsyncRemote().sendObject( tm );
  }
  
  @EndpointMessageHandler(name = "changedatum")
  public void handleChangeDatum( Session session, ToolMessage message, PeerGroupChangeDatum datum ) throws IOException
  {
    logger.log( 
            Level.INFO, 
            "handleChangeDatum() {0} {1} {2} {3}", 
            new Object[ ]{ datum.getGroupId(), datum.getFieldId(), datum.getMemberId(), datum.getValue() } );
    
    PeerGroupDataKey key = new PeerGroupDataKey( pgaResource.getKey(), datum.getGroupId() );
    PeerGroupData data = store.getData( key, true );
    data.setParticipantDatum( datum );
    store.updateData( data );
    ToolMessage tm = new ToolMessage( message.getId(), "data", data );
    sendToolMessageToResourceUsers( tm );
  }
  
  public void sendToolMessageToResourceUsers( ToolMessage tm )
  {
    for ( Session s : appcontext.getWsSessionsForResource( pgaResource.getKey() ) )
    {
      logger.info( "Telling a client." );
      s.getAsyncRemote().sendObject( tm );
    }
  }
  
}
