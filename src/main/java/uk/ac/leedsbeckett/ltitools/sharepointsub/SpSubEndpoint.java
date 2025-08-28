/*
 * Copyright 2025 maber01.
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
package uk.ac.leedsbeckett.ltitools.sharepointsub;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import uk.ac.leedsbeckett.ltitools.sharepointsub.messagedata.SpSubConfigurationMessage;
import uk.ac.leedsbeckett.ltitools.sharepointsub.messagedata.SpSubCourseSettingsMessage;
import uk.ac.leedsbeckett.ltitools.sharepointsub.store.Configuration;
import uk.ac.leedsbeckett.ltitools.sharepointsub.store.CourseSettings;
import uk.ac.leedsbeckett.ltitools.sharepointsub.store.Deadline;
import uk.ac.leedsbeckett.ltitools.sharepointsub.store.Dropbox;
import uk.ac.leedsbeckett.ltitools.sharepointsub.store.StoreCluster;
import uk.ac.leedsbeckett.ltitoolset.websocket.HandlerAlertException;
import uk.ac.leedsbeckett.ltitoolset.websocket.ToolEndpoint;
import uk.ac.leedsbeckett.ltitoolset.websocket.ToolMessage;
import uk.ac.leedsbeckett.ltitoolset.websocket.annotations.EndpointJavascriptProperties;
import uk.ac.leedsbeckett.ltitoolset.websocket.annotations.EndpointMessageHandler;

/**
 *
 * @author maber01
 */
@ServerEndpoint( value="/socket/sharepointsub" )
@EndpointJavascriptProperties(
        module="sharepointsub",
        prefix="SpSub",
        messageEnum="uk.ac.leedsbeckett.ltitools.sharepointsub.SpSubServerMessageName" )
public class SpSubEndpoint extends ToolEndpoint
{
  static final Logger logger = Logger.getLogger(SpSubEndpoint.class.getName() );
  
  SharepointSubTool tool;
  SpSubToolLaunchState spsubState;
  StoreCluster store;

  String platformName=null;

  @Override
  public boolean indexByPlatformCourse()
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
    spsubState = (SpSubToolLaunchState)getState().getToolLaunchState();
    tool = (SharepointSubTool)getToolCoordinator().getTool( getState().getToolId() );
    store = tool.getSpSubStore();    
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
    if ( !spsubState.isAllowedToConfigure() )
      throw new HandlerAlertException( "Recieved request for configuration from user who is not allowed to configure the tool.", message );
    
    logger.info( "Fetching config for platform " + platformName );
    Configuration config = tool.getPlatformConfig( platformName );
    ToolMessage tmf = new ToolMessage( message, SpSubServerMessageName.Configuration, new SpSubConfigurationMessage( config ) );
    sendToolMessage( session, tmf );
  }
  
  @EndpointMessageHandler()
  public void handleConfigure( Session session, ToolMessage message, SpSubConfigurationMessage configMessage )
          throws IOException, HandlerAlertException
  {
    if ( !spsubState.isAllowedToConfigure() )
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
    
    ToolMessage tmf = new ToolMessage( message, SpSubServerMessageName.ConfigurationSuccess, "Saved" );
    sendToolMessage( session, tmf );
    
    // To do - send message to all users now accessing tool from the same platform
    // for now just for confirmation to current user.
    ToolMessage tmc = new ToolMessage( message, SpSubServerMessageName.Configuration, new SpSubConfigurationMessage( config ) );
    sendToolMessage( session, tmc );
    
    if ( config.isEnabled() )
      tool.runScanAllTask();
  }


  @EndpointMessageHandler()
  public void handleCourseSettingsRequest( Session session, ToolMessage message )
          throws IOException, HandlerAlertException
  {
    if ( !spsubState.isAllowedToManage() )
      throw new HandlerAlertException( "Recieved request for course settings from user who is not allowed to manage the course.", message );
    
    if ( spsubState.getPlatformCourseKey() == null )
      throw new HandlerAlertException( "Recieved request for course settings but not launched from course.", message );
    
    CourseSettings cs = tool.getPlatformCourseSettings( spsubState.getPlatformCourseKey() );
    if ( cs == null )
      throw new HandlerAlertException( "Course settings not found.", message );

    ToolMessage tmf = new ToolMessage( message, SpSubServerMessageName.CourseSettings, cs );
    sendToolMessage( session, tmf );
  }  
  
  @EndpointMessageHandler()
  public void handleCourseSettings( Session session, ToolMessage message, SpSubCourseSettingsMessage csm )
          throws IOException, HandlerAlertException
  {
    if ( !spsubState.isAllowedToConfigure() )
      throw new HandlerAlertException( "Recieved request to save new course settings from user who is not allowed to configure the tool.", message );
            
    if ( csm == null )
      throw new HandlerAlertException( "Null configuration was received.", message );

    CourseSettings cs = csm.getCourseSettings();
    HashMap<String,Deadline> smap = new HashMap<>();
    smap.put( "thing@whatsit.com", new Deadline( 2025, 9, 8, 12 ) );
    Dropbox dropbox = new Dropbox( "testing2", new Deadline( 2025, 9, 1, 12 ), smap );
    cs.getDropboxMap().put( dropbox.getName(), dropbox );
    
    try
    {  
      tool.savePlatformCourseSettings( cs );
    }
    catch ( Exception e )
    {
      logger.log( Level.SEVERE, "Unable to save course settings for platform " + platformName, e );
      throw new HandlerAlertException( "Unable to save course settings.", message );
    }
    
    ToolMessage tmf = new ToolMessage( message, SpSubServerMessageName.ConfigurationSuccess, "Saved" );
    sendToolMessage( session, tmf );

    // To do - send message to all users now accessing tool from the same platform
    // for now just for confirmation to current user.
    ToolMessage tmcs = new ToolMessage( message, SpSubServerMessageName.CourseSettings, cs );
    sendToolMessage( session, tmcs );
  }
  
}
