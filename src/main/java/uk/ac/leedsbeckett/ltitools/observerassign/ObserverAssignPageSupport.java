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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import uk.ac.leedsbeckett.ltitoolset.page.ToolPageSupport;

/**
 *
 * @author maber01
 */
public class ObserverAssignPageSupport extends ToolPageSupport<ObserverAssignPageData>
{
  static final Logger logger = Logger.getLogger(ObserverAssignPageSupport.class.getName() );
  
  ObserverAssignTool tool;
  OaToolLaunchState oaState;
  String websocketuri;

  ObserverAssignPlatformConfiguration config;
  
  /**
   * Get ready to provide services for the java server page.
   * 
   * @param request The request behind this JSP delivery.
   * @throws ServletException Thrown when the request is aborted.
   */
  @Override
  public void setRequest(HttpServletRequest request) throws ServletException
  {
    super.setRequest( request );
    logger.log( Level.FINE, "setRequest() state id = {0}", state.getId() );
    if ( logger.isLoggable( Level.FINE ) )
    {
      ObjectMapper mapper = new ObjectMapper();
      logger.fine( "Launch State" );
      try
      { logger.fine( mapper.writerWithDefaultPrettyPrinter().writeValueAsString( state ) ); }
      catch ( JsonProcessingException ex )
      { logger.log( Level.SEVERE, "Problem dumping state.", ex ); }
    }
    
    oaState = (OaToolLaunchState)state.getToolLaunchState();
    if ( oaState == null )
      throw new ServletException( "Could not find self enrol. session data." );
    logger.log(Level.FINE, "resource key = {0}", oaState.getPlatformResourceKey() );
    tool = (ObserverAssignTool)toolCoordinator.getTool( state.getToolId() );
    try
    {
      config = tool.getPlatformConfig( state.getPlatformName() );
    }
    catch ( IOException ex )
    {
      logger.log( Level.SEVERE, null, ex );
      throw new ServletException( "Unable to load configuration for platform " + state.getPlatformName() );
    }
    websocketuri = getBaseUri() + oaState.getRelativeWebSocketUri();

    dynamicPageData.setDebugging( logger.isLoggable( Level.FINE ) );
    dynamicPageData.setAllowedToAssignObserver( oaState.isAllowedToAssignObserver() );
    dynamicPageData.setAllowedToConfigure( oaState.isAllowedToConfigure() );
  }

  /**
   * The ID of the user who launched the page.
   * 
   * @return The ID according the to the launching platform.
   */
  public String getPersonId()
  {
    return oaState.getPersonId();
  }
  
  /**
   * The human name of the usre who launched the page.
   * 
   * @return The name.
   */
  public String getPersonName()
  {
    return oaState.getPersonName();
  }
  
  /**
   * Is the user allowed to assign observers to themselves?
   * 
   * @return True if the user is allowed.
   */
  public boolean isAllowedToAssignObserver()
  {
    return dynamicPageData.isAllowedToAssignObserver();
  }
  
  /**
   * Is the user allowed to assign observers to themselves?
   * 
   * @return True if the user is allowed.
   */
  public boolean isAllowedToOfferObserver()
  {
    return dynamicPageData.isAllowedToOfferObserver();
  }
  
  /**
   * Is the user allowed to configure the tool for the launching platform?
   * Derived from LTI role claims.
   * 
   * @return True if the user is a participant.
   */
  public boolean isAllowedToConfigure()
  {
    return dynamicPageData.isAllowedToConfigure();
  }
  
  /**
   * Gets the URI of the web socket that implements all the logic of
   * this tool.
   * 
   * @return The URI.
   */
  public String getWebsocketUri()
  {
    return websocketuri;
  }
  
  /**
   * Used by JSP page to find out if the page support has logging level set
   * to FINE or even more detailed.
   * 
   * @return True if FINE or finer.
   */
  public boolean isDebugging()
  {
    return dynamicPageData.isDebugging();
  }
  
  /**
   * Builds a dump of debugging information.
   * 
   * @return A long plain text string.
   */
  public String getDump()
  {
    StringBuilder sb = new StringBuilder();
    try
    {
      ObjectMapper mapper = new ObjectMapper();
      mapper.enable( SerializationFeature.INDENT_OUTPUT );
      mapper.disable( SerializationFeature.FAIL_ON_EMPTY_BEANS );
      mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
      sb.append( "Launch State\n" );    
      sb.append( "=============\n" );
      sb.append( mapper.writerWithDefaultPrettyPrinter().writeValueAsString( state ) );
      sb.append( "\n\n" );    
      sb.append( "Web Socket\n" );
      sb.append( "=============\n" );
      sb.append( getWebsocketUri() );
    }
    catch ( JsonProcessingException ex )
    {
      logger.log( Level.SEVERE, "Unable to dump data as JSON.", ex );
      return "Unable to dump data as JSON.\n";
    }
    return sb.toString();
  }

  @Override
  public ObserverAssignPageData makeDynamicPageData()
  {
    return new ObserverAssignPageData();
  }
  
}
