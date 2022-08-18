/*
 * Copyright 2022 Leeds Beckett University.
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

package uk.ac.leedsbeckett.ltitools.tool;

import uk.ac.leedsbeckett.ltitools.admin.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.servlet.ServletException;
import uk.ac.leedsbeckett.lti.LtiConfiguration;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.io.FileUtils;
import uk.ac.leedsbeckett.lti.state.LtiState;
import uk.ac.leedsbeckett.lti.state.LtiStateStore;
import uk.ac.leedsbeckett.ltitools.app.ApplicationContext;
import uk.ac.leedsbeckett.ltitools.state.AppState;

/**
 * This class provides logic for use within an admin JSP page.
 * 
 * @author jon
 */
public class PageSupport
{
  HttpServletRequest request;
  String importantmessage="";

  protected AppState state;

  /**
   * Get the HTTP request associated with the JSP page that uses this object.
   * @return 
   */
  public HttpServletRequest getRequest()
  {
    return request;
  }

  /**
   * The JSP will call this to initiate processing and then call the getter
   * methods to retrieve outcomes of the processing.
   * 
   * @param request The HttpRequest associated with the JSP's servlet.
   * @throws javax.servlet.ServletException
   */
  public void setRequest( HttpServletRequest request ) throws ServletException
  {
    this.request = request;
    String stateid = request.getParameter( "state_id" );
    if ( stateid == null )
      throw new ServletException( "State ID missing." );

    ApplicationContext appcontext = ApplicationContext.getFromServletContext( request.getServletContext() );
    LtiStateStore statestore = appcontext.getStateStore();
    if ( statestore == null )
      throw new ServletException( "State store missing." );
    
    LtiState ltistate = statestore.getState( stateid );
    if ( state == null )
      throw new ServletException( "State missing. " + stateid );
    
    if ( !(state instanceof AppState) )
      throw new ServletException( "Wrong type of state. " + state.getClass().getName() );        
  }

  /**
   * Get the important message.
   * 
   * @return An important message or an empty string.
   */
  public String getImportantMessage()
  {
    return importantmessage;
  }
}
