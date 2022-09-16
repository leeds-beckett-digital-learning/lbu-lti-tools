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
import uk.ac.leedsbeckett.ltitools.tool.websocket.ToolMessage;
import uk.ac.leedsbeckett.ltitools.tool.websocket.ToolMessageDecoder;
import uk.ac.leedsbeckett.ltitools.tool.websocket.ToolMessageEncoder;
import uk.ac.leedsbeckett.ltitools.tool.websocket.ToolMessageTypeSet;

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
  PeerGroupAssessmentState pgaState;
  PeerGroupResourceStore store;
  PeerGroupResource pgaResource;
  
  @OnOpen
  public void onOpen(Session session) throws IOException
  {
    logger.info( "Opened websocket session uri = " + session.getRequestURI().toASCIIString() );
    
    ToolMessageTypeSet.addType( PeerGroupResource.class.getName() );
    ToolMessageTypeSet.addType( PeerGroupResourceProperties.class.getName() );
    appcontext = ApplicationContext.getFromWebSocketContainer( session.getContainer() );
    List<String> list = session.getRequestParameterMap().get( "state" );
    if ( list != null )
    {
      String stateid = list.get( 0 );
      logger.info( "State ID = " + stateid );
      state = appcontext.getStateStore().getState( stateid );
      logger.info( state.getPersonName() );
      pgaState = state.getPeerGroupAssessmentState();
      store = appcontext.getStore();
      pgaResource = store.get( pgaState.getResourceKey(), true );
      appcontext.addWsSession( pgaState.getResourceKey(), session );
    }
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
    if ( "hello".equals( message.getMessageType() ) )
    {
      ToolMessage tm = new ToolMessage( message.getId(), "acknowledge", null );
      session.getAsyncRemote().sendObject( tm );
    }

    if ( "getresource".equals( message.getMessageType() ) )
    {
      logger.log( Level.INFO, "Sending resource [{0}]", pgaResource.getTitle() );
      ToolMessage tm = new ToolMessage( message.getId(), "resource", pgaResource );
      session.getAsyncRemote().sendObject( tm );
    }

    if ( "setresourceproperties".equals( message.getMessageType() ) )
    {
      Object payload = message.getPayload();
      if ( PeerGroupResourceProperties.class.equals( payload.getClass() ) )
      {
        PeerGroupResourceProperties p = (PeerGroupResourceProperties)payload;
        logger.log( Level.INFO, "State [{0}]",       p.stage.toString() );
        logger.log( Level.INFO, "Title [{0}]",       p.title );
        logger.log( Level.INFO, "Description [{0}]", p.description );
        pgaResource.setProperties( p );
        try
        {
          store.update( pgaResource );
          ToolMessage tm = new ToolMessage( message.getId(), "resourceproperties", pgaResource.getProperties() );
          for ( Session s : appcontext.getWsSessionsForResource( pgaResource.getResourceKey() ) )
          {
            logger.info( "Telling a client." );
            s.getAsyncRemote().sendObject( tm );
          }
        }
        catch ( IOException e )
        {
          logger.log(  Level.SEVERE, "Unable to store changes.", e );
        }
      }
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
}
