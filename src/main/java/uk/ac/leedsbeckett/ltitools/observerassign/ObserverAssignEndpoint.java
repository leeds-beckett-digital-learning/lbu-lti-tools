/*
 * Copyright 2026 maber01.
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
package uk.ac.leedsbeckett.ltitools.observerassign;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import uk.ac.leedsbeckett.ltitools.observerassign.messages.OaConfigurationMessage;
import uk.ac.leedsbeckett.ltitools.observerassign.messages.OaServerMessageName;
import uk.ac.leedsbeckett.ltitools.selfenrol.SeEndpoint;
import uk.ac.leedsbeckett.ltitoolset.backchannel.blackboard.BlackboardBackchannelKey;
import uk.ac.leedsbeckett.ltitoolset.websocket.HandlerAlertException;
import uk.ac.leedsbeckett.ltitoolset.websocket.ToolEndpoint;
import uk.ac.leedsbeckett.ltitoolset.websocket.ToolMessage;
import uk.ac.leedsbeckett.ltitoolset.websocket.annotations.EndpointJavascriptProperties;
import uk.ac.leedsbeckett.ltitoolset.websocket.annotations.EndpointMessageHandler;

/**
 *
 * @author maber01
 */
@ServerEndpoint( value="/socket/observerassign" )
@EndpointJavascriptProperties(
        module="observerassign",
        prefix="Oa",
        messageEnum="uk.ac.leedsbeckett.ltitools.observerassign.messages.OaServerMessageName"
)
public class ObserverAssignEndpoint extends ToolEndpoint
{
  static final Logger logger = Logger.getLogger(SeEndpoint.class.getName() );

  ObserverAssignTool tool;
  OaToolLaunchState oaState;
  BlackboardBackchannelKey bbbckey;
  String platformName=null;
  
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
    oaState      = (OaToolLaunchState)getState().getToolLaunchState();
    tool         = (ObserverAssignTool)getToolCoordinator().getTool( getState().getToolId() );
    bbbckey      = new BlackboardBackchannelKey( getPlatformHost() );
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
 

  @EndpointMessageHandler()
  public void handleConfigurationRequest( Session session, ToolMessage message )
          throws IOException, HandlerAlertException
  {
    if ( !oaState.isAllowedToConfigure() )
      throw new HandlerAlertException( "Recieved request for configuration from user who is not allowed to configure the tool.", message );
    
    logger.info( "Fetching config for platform " + platformName );
    ObserverAssignPlatformConfiguration config = tool.getPlatformConfig( platformName );
    ToolMessage tmf = new ToolMessage( message, OaServerMessageName.Configuration, new OaConfigurationMessage( config ) );
    sendToolMessage( session, tmf );
  }
  
  @EndpointMessageHandler()
  public void handleConfigure( Session session, ToolMessage message, OaConfigurationMessage configMessage )
          throws IOException, HandlerAlertException
  {
    if ( !oaState.isAllowedToConfigure() )
      throw new HandlerAlertException( "Recieved request to save new configuration from user who is not allowed to configure the tool.", message );
            
    ObserverAssignPlatformConfiguration config = configMessage.getConfiguration();
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
    
    ToolMessage tmf = new ToolMessage( message, OaServerMessageName.ConfigurationSuccess, "Saved" );
    sendToolMessage( session, tmf );
    
    // To do - send message to all users now accessing tool from the same platform
    // for now just for confirmation to current user.
    ToolMessage tmc = new ToolMessage( message, OaServerMessageName.Configuration, new OaConfigurationMessage( config ) );
    sendToolMessage( session, tmc );
  }  
}
