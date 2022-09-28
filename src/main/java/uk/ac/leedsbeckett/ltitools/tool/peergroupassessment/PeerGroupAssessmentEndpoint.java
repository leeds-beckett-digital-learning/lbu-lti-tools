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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
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
import uk.ac.leedsbeckett.ltitools.tool.ToolManager;
import uk.ac.leedsbeckett.ltitools.tool.peergroupassessment.PeerGroupResource.Group;
import uk.ac.leedsbeckett.ltitools.tool.websocket.EmptyPayload;
import uk.ac.leedsbeckett.ltitools.tool.websocket.ToolMessage;
import uk.ac.leedsbeckett.ltitools.tool.websocket.ToolMessageDecoder;
import uk.ac.leedsbeckett.ltitools.tool.websocket.ToolMessageEncoder;
import uk.ac.leedsbeckett.ltitools.tool.websocket.ToolMessageTypeSet;
import uk.ac.leedsbeckett.ltitools.tool.websocket.annotations.EndpointMessageHandler;

/**
 *
 * @author maber01
 */
@ServerEndpoint( 
        value="/socket/peergroupassessment", 
        decoders=ToolMessageDecoder.class, 
        encoders=ToolMessageEncoder.class )
public class PeerGroupAssessmentEndpoint
{
  static final Logger logger = Logger.getLogger( PeerGroupAssessmentEndpoint.class.getName() );

  AppLtiState state;
  ApplicationContext appcontext;
  ToolManager toolManager;
  PeerGroupAssessmentTool tool;
  PeerGroupAssessmentState pgaState;
  PeerGroupAssessmentStore store;
  PeerGroupForm defaultForm;
  PeerGroupResource pgaResource;
  
  final HashMap<String,HandlerMethodRecord> handlerMethods = new HashMap<>();
  
  public PeerGroupAssessmentEndpoint()
  {
    for ( Method method : PeerGroupAssessmentEndpoint.class.getMethods() )
    {
      logger.log( Level.INFO, "Checking method {0}", method.getName() );
      for ( EndpointMessageHandler handler : method.getAnnotationsByType( EndpointMessageHandler.class ) )
      {
        logger.log( Level.INFO, "Method has EndpointMessageHandler annotation and name = " + handler.name() );
        Class<?>[] classarray = method.getParameterTypes();
        logger.log( Level.INFO, "Method parameter count = " + classarray.length );
        if ( ( classarray.length == 2 || classarray.length == 3 ) && 
                classarray[0].equals( Session.class ) &&
                classarray[1].equals( ToolMessage.class ) )
        {
          logger.log( Level.INFO, "Parameters match signature and second parameter class is " + 
                  (classarray.length == 3?classarray[2]:"not present") );
          handlerMethods.put( handler.name(), 
                  new HandlerMethodRecord( 
                          handler.name(), 
                          method, 
                          classarray.length == 3?classarray[2]:null ) );
        }
      }
    }
  }
  
  @OnOpen
  public void onOpen(Session session) throws IOException
  {
    logger.info( "Opened websocket session uri = " + session.getRequestURI().toASCIIString() );
    
    ToolMessageTypeSet.addType( String.class.getName() );
    ToolMessageTypeSet.addType( EmptyPayload.class.getName() );
    ToolMessageTypeSet.addType( PeerGroupResource.class.getName() );
    ToolMessageTypeSet.addType( PeerGroupResource.Group.class.getName() );
    ToolMessageTypeSet.addType( PeerGroupChangeGroup.class.getName() );
    ToolMessageTypeSet.addType( PeerGroupChangeDatum.class.getName() );
    ToolMessageTypeSet.addType( PeerGroupAddMembership.class.getName() );
    ToolMessageTypeSet.addType( PeerGroupResourceProperties.class.getName() );
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

  boolean dispatchMessage( Session session, ToolMessage message ) throws IOException
  {
    logger.log( Level.INFO, "dispatchMessage type = " + message.getMessageType() );
    HandlerMethodRecord record = handlerMethods.get( message.getMessageType() );
    if ( record == null ) return false;
    
    Class pc = record.getParameterClass();
    logger.log( Level.INFO, "dispatchMessage found handler record " + pc );
    
    if ( pc != null )
    {
      if ( message.getPayload() == null ) return false;
      logger.log( Level.INFO, "dispatchMessage payload class is " + message.getPayload().getClass() );
      if ( !(message.getPayload().getClass().isAssignableFrom( record.getParameterClass() ) ) )
        return false;
    }
    
    logger.log( Level.INFO, "Invoking method." );
    try
    {
      if ( pc == null )
        record.method.invoke( this, session, message );
      else
        record.method.invoke( this, session, message, message.getPayload() );
    }
    catch ( IllegalAccessException | IllegalArgumentException ex )
    {
      logger.log( Level.SEVERE, "Web socket message handler error.", ex );
    }
    catch ( InvocationTargetException ex )
    {
      // method threw exception
      Throwable original = ex.getCause();
      if ( original instanceof IOException )
        throw (IOException)original;
      logger.log( Level.SEVERE, "Web socket message handler error.", ex );
    }
    
    return true;
  }

  @EndpointMessageHandler(name = "getresource")
  public void handleGetResource( Session session, ToolMessage message ) throws IOException
  {
    if ( "getresource".equals( message.getMessageType() ) )
    {
      logger.log( Level.INFO, "Sending resource [{0}]", pgaResource.getTitle() );
      ToolMessage tm = new ToolMessage( message.getId(), "resource", pgaResource );
      session.getAsyncRemote().sendObject( tm );
    }
  }
  
  @EndpointMessageHandler(name = "setresourceproperties")
  public void handleSetResourceProperties( Session session, ToolMessage message, PeerGroupResourceProperties p ) throws IOException
  {
    logger.log( Level.INFO, "State [{0}]",       p.stage.toString() );
    logger.log( Level.INFO, "Title [{0}]",       p.title );
    logger.log( Level.INFO, "Description [{0}]", p.description );
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
  public void handleAddGroup( Session session, ToolMessage message, PeerGroupChangeGroup p ) throws IOException
  {
    logger.log( Level.INFO, "ID [{0}]",       p.id );
    logger.log( Level.INFO, "Title [{0}]",    p.title );
    Group g = pgaResource.getGroupById( p.id );
    if ( g != null )
    {
      logger.log( Level.INFO, "Member count [{0}]", Integer.toString( g.getMembers().size() ) );
      g.setTitle( p.title );
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
        PeerGroupChangeGroup p = new PeerGroupChangeGroup();
        p.setId( g.getId() );
        p.setTitle( g.getTitle() );
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
  public void handleGetFormAndData( Session session, ToolMessage message, String gid ) throws IOException
  {
    logger.log( Level.INFO, "handleGetFormAndData" );
    PeerGroupDataKey key = new PeerGroupDataKey( pgaResource.getKey(), gid );
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
            new Object[ ]{ datum.groupId, datum.fieldId, datum.memberId, datum.value } );
    
    PeerGroupDataKey key = new PeerGroupDataKey( pgaResource.getKey(), datum.groupId );
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
  
  public class HandlerMethodRecord
  {
    final String name;
    final Method method;
    final Class<?> parameterClass;
    
    public HandlerMethodRecord( String name, Method method, Class<?> parameterClass )
    {
      this.name = name;
      this.method = method;
      this.parameterClass = parameterClass;
    }

    public String getName()
    {
      return name;
    }

    public Method getMethod()
    {
      return method;
    }

    public Class<?> getParameterClass()
    {
      return parameterClass;
    }
    
  }
}
