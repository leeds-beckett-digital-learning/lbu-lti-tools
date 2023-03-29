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

import uk.ac.leedsbeckett.ltitools.peergroupassessment.resourcedata.PeerGroupResource;
import uk.ac.leedsbeckett.ltitools.peergroupassessment.store.StoreCluster;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.websocket.server.ServerEndpoint;
import uk.ac.leedsbeckett.ltitoolset.page.ToolPageSupport;

/**
 * Support class for PeerGroupPage related JSP pages.
 * 
 * @author jon
 */
public class PgaPageSupport extends ToolPageSupport<PgaDynamicPageData>
{
  static final Logger logger = Logger.getLogger(PgaPageSupport.class.getName() );
  
  PeerGroupAssessmentTool tool;
  StoreCluster store;
  PgaToolLaunchState pgaState;
  PeerGroupResource pgaResource;
  
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
    
    pgaState = (PgaToolLaunchState)state.getToolLaunchState();
    if ( pgaState == null )
      throw new ServletException( "Could not find peer group assessment tool session data." );
    logger.log( Level.FINE, "resource key = {0}", pgaState.getResourceKey() );
    tool = (PeerGroupAssessmentTool)toolCoordinator.getTool( state.getToolKey() );
    store = tool.getPeerGroupAssessmentStore();
    pgaResource = store.getResource( pgaState.getResourceKey(), true );

    dynamicPageData.setAllowedToParticipate( pgaState.isAllowedToParticipate() );
    dynamicPageData.setAllowedToManage( pgaState.isAllowedToManage() );
  }

  /**
   * Gets the PGA resource. Bear in mind that the resource is likely to change
   * while the user is still working on the page.
   * 
   * @return The resource in its state when the page loads.
   */
  public PeerGroupResource getPgaResource()
  {
    return pgaResource;
  }
 
  /**
   * The ID of the user who launched the page.
   * 
   * @return The ID according the to the launching platform.
   */
  public String getPersonId()
  {
    return pgaState.getPersonId();
  }
  
  /**
   * The human name of the usre who launched the page.
   * 
   * @return The name.
   */
  public String getPersonName()
  {
    return pgaState.getPersonName();
  }
  
  /**
   * Is the user allowed to participate by joining a group and entering data?
   * Derived from LTI role claims.
   * 
   * @return True if the user is a participant.
   */
  public boolean isAllowedToParticipate()
  {
    return pgaState.isAllowedToParticipate();
  }

  /**
   * Is the user allowed to manage the PGA? Derived from LTI role claims.
   * 
   * @return True if the user is a manager.
   */
  public boolean isAllowedToManage()
  {
    return pgaState.isAllowedToManage();
  }
  
  /**
   * Gets the URI of the web socket that implements all the logic of
   * this tool.
   * 
   * @return The URI.
   */
  public String getWebsocketUri()
  {
    return dynamicPageData.getWebSocketUri();
  }

  /**
   * Used by JSP page to find out if the page support has logging level set
   * to FINE or even more detailed.
   * 
   * @return True if FINE or finer.
   */
  public boolean isDebugging()
  {
    return logger.isLoggable( Level.FINE );
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
      sb.append( "\nResource\n" );    
      sb.append( "=============\n" );
      sb.append( mapper.writerWithDefaultPrettyPrinter().writeValueAsString( pgaResource ) );
    }
    catch ( JsonProcessingException ex )
    {
      logger.log( Level.SEVERE, "Unable to dump data as JSON.", ex );
      return "Unable to dump data as JSON.\n";
    }
    return sb.toString();
  }


  @Override
  public PgaDynamicPageData makeDynamicPageData()
  {
    return new PgaDynamicPageData();
  }
}
