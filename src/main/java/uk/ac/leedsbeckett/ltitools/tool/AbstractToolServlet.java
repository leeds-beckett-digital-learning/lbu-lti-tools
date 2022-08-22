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

import uk.ac.leedsbeckett.lti.state.LtiStateStore;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import uk.ac.leedsbeckett.ltitools.app.ApplicationContext;
import uk.ac.leedsbeckett.ltitools.state.AppLtiState;
import uk.ac.leedsbeckett.lti.state.LtiState;

/**
 * An abstract superclass of the servlets that implement the tool. Provides
 * common functionality.
 * 
 * @author jon
 */
public abstract class AbstractToolServlet extends HttpServlet
{
  
  /**
   * This provides subclasses with the ability to fetch the user's state
   * object. This is found by looking for a state ID string as a parameter in
   * the query string or form data. It finds the state store from the servlet
   * context and looks up the state.
   * 
   * @param request The HTTP servlet request.
   * @return The state, if found or NULL. If NULL an error will have already been sent to browser.
   * @throws ServletException If problem occurred in processing.
   * @throws IOException If it wasn't possible to send an error page over the network.
   */
  protected AppLtiState getState( HttpServletRequest request )
          throws ServletException, IOException
  {
    String stateid = request.getParameter( "state_id" );
    if ( stateid == null )
      throw new ServletException( "State ID missing." );

    ApplicationContext appcontext = ApplicationContext.getFromServletContext( request.getServletContext() );
    LtiStateStore statestore = appcontext.getStateStore();
    if ( statestore == null )
      throw new ServletException( "State store missing." );
    
    LtiState state = statestore.getState( stateid );
    if ( state == null )
      throw new ServletException( "State missing. " + stateid );
    
    if ( !(state instanceof AppLtiState) )
      throw new ServletException( "Wrong type of state. " + state.getClass().getName() );
    
    return (AppLtiState)state;
  }  
}
