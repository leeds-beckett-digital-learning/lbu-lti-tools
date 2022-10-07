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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import uk.ac.leedsbeckett.ltitoolset.ToolCoordinator;
import uk.ac.leedsbeckett.ltitoolset.ToolSetLtiState;
import uk.ac.leedsbeckett.ltitoolset.websocket.ToolEndpoint;
import uk.ac.leedsbeckett.ltitoolset.websocket.ToolMessage;
import uk.ac.leedsbeckett.ltitoolset.websocket.ToolMessageDecoder;
import uk.ac.leedsbeckett.ltitoolset.websocket.ToolMessageEncoder;
import uk.ac.leedsbeckett.ltitoolset.websocket.annotations.EndpointMessageHandler;
import uk.ac.leedsbeckett.ltitools.peergroupassessment.messagedata.Id;
import uk.ac.leedsbeckett.ltitools.peergroupassessment.resourcedata.PeerGroupResource.Group;

/**
 *
 * @author maber01
 */
@ServerEndpoint( 
        value="/socket/peergroupassessment", 
        decoders=ToolMessageDecoder.class, 
        encoders=ToolMessageEncoder.class )
public class PgaEndpoint extends ToolEndpoint
{
  static final Logger logger = Logger.getLogger(PgaEndpoint.class.getName() );

  ToolSetLtiState state;
  ToolCoordinator toolCoordinator;
  PeerGroupAssessmentTool tool;
  PgaToolLaunchState pgaState;
  StoreCluster store;
  PeerGroupForm defaultForm;
  PeerGroupResource pgaResource;
  
  
  @OnOpen
  public void onOpen(Session session) throws IOException
  {
    logger.info( "Opened websocket session uri = " + session.getRequestURI().toASCIIString() );        
    toolCoordinator = ToolCoordinator.get( session.getContainer() );
    List<String> list = session.getRequestParameterMap().get( "state" );
    if ( list != null )
    {
      String stateid = list.get( 0 );
      logger.log(Level.INFO, "State ID = {0}", stateid);
      state = toolCoordinator.getLtiStateStore().getState( stateid );
      logger.info( state.getPersonName() );
      pgaState = (PgaToolLaunchState)state.getAppSessionState();
      tool = (PeerGroupAssessmentTool)toolCoordinator.getTool( state.getToolKey() );
      store = tool.getPeerGroupAssessmentStore();
      pgaResource = store.getResource( pgaState.getResourceKey(), true );
      toolCoordinator.addWsSession( pgaState.getResourceKey(), session );
      defaultForm = store.getDefaultForm();
    }
  }
  
  @OnClose
  public void onClose(Session session) throws IOException
  {
    logger.info( "Closed websocket session " );
    toolCoordinator.removeWsSession( pgaState.getResourceKey(), session );
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
      ToolMessage tm = new ToolMessage( message.getId(), "resourceproperties", pgaResource.getProperties() );
      sendToolMessageToResourceUsers( tm );
    }
    catch ( IOException e )
    {
      logger.log(  Level.SEVERE, "Unable to store changes.", e );
    }
  }  

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
        ToolMessage tm = new ToolMessage( message.getId(), "group", g );
        sendToolMessageToResourceUsers( tm );
      }
      catch ( IOException e )
      {
        logger.log(  Level.SEVERE, "Unable to store changes.", e );
      }
    }
  }
  
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
        ToolMessage tm = new ToolMessage( message.getId(), "group", p );
        sendToolMessageToResourceUsers( tm );
      }
      catch ( IOException e )
      {
        logger.log(  Level.SEVERE, "Unable to store changes.", e );
      }
    }
  }  
  
  
  @EndpointMessageHandler()
  public void handleMembership( Session session, ToolMessage message, PgaAddMembership m ) throws IOException
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
  
  @EndpointMessageHandler()
  public void handleGetFormAndData( Session session, ToolMessage message, Id gid ) throws IOException
  {
    logger.log( Level.INFO, "handleGetFormAndData" );
    PeerGroupDataKey key = new PeerGroupDataKey( pgaResource.getKey(), gid.getId() );
    PgaFormAndData fad = new PgaFormAndData( defaultForm, store.getData( key, true ) );
    ToolMessage tm = new ToolMessage( message.getId(), "formanddata", fad );
    session.getAsyncRemote().sendObject( tm );
  }
  
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
    ToolMessage tm = new ToolMessage( message.getId(), "data", data );
    sendToolMessageToResourceUsers( tm );
  }
  
  public void sendToolMessageToResourceUsers( ToolMessage tm )
  {
    for ( Session s : toolCoordinator.getWsSessionsForResource( pgaResource.getKey() ) )
    {
      logger.info( "Telling a client." );
      s.getAsyncRemote().sendObject( tm );
    }
  }
  
}
