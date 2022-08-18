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

package uk.ac.leedsbeckett.ltitools.launch;

import uk.ac.leedsbeckett.ltitools.app.ApplicationContext;
import uk.ac.leedsbeckett.ltitools.app.FixedLtiConfiguration;
import uk.ac.leedsbeckett.ltitools.state.LaunchState;
import uk.ac.leedsbeckett.ltitools.tool.peergroupassessment.PeerGroupAssessmentState;
import uk.ac.leedsbeckett.ltitools.state.AppState;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import uk.ac.leedsbeckett.ltitools.tool.peergroupassessment.PeerGroupResource;
import uk.ac.leedsbeckett.ltitools.tool.peergroupassessment.PeerGroupResourceStore;
import uk.ac.leedsbeckett.lti.claims.LtiClaims;
import uk.ac.leedsbeckett.lti.servlet.LtiLaunchServlet;
import uk.ac.leedsbeckett.lti.state.LtiState;
import uk.ac.leedsbeckett.lti.state.LtiStateStore;

/**
 * This demo's implementation of the LTI launch servlet. This implementation
 * will map onto a url pattern.
 * 
 * @author jon
 */
@WebServlet( name = "DemoLtiLaunchServlet", urlPatterns = { FixedLtiConfiguration.LAUNCH_PATTERN } )
public class AppLtiLaunchServlet extends LtiLaunchServlet
{
  
  /**
   * The parent class calls this method after it has processed and validated 
   * the launch request. The job here is to look at the claims in the LTI
   * launch and decide how to prepare state and how to forward the user to
   * the servlet or JSP page that actually implements the tool.
   * 
   * @param lticlaims The validated LTI claims for this launch request.
   * @param ltistate The LTI state that was created by the preceding login request.
   * @param request The HTTP request.
   * @param response The HTTP response.
   * @throws ServletException If there is an internal problem forwarding the user's browser.
   * @throws IOException If the network connection is broken while sending the forwarding response.
   */
  @Override
  protected void processLaunchRequest( LtiClaims lticlaims, LtiState ltistate, HttpServletRequest request, HttpServletResponse response )
          throws ServletException, IOException
  {
    ApplicationContext appcontext = ApplicationContext.getFromServletContext( request.getServletContext() );
    
    if ( !(ltistate instanceof AppState ) )
      throw new ServletException( "Wrong type of LtiState." );
    AppState state = (AppState)ltistate;
    this.log( "AppLtiLaunchServlet.processLaunchRequest() " + state.getId() );
    String toolname = lticlaims.getLtiCustom().getAsString( "digles.leedsbeckett.ac.uk#tool_name" );
    String tooltype = lticlaims.getLtiCustom().getAsString( "digles.leedsbeckett.ac.uk#tool_type" );
    
    
    if ( "coursecontent".equals( tooltype ) && "peergrpassess".equals( toolname ) )
    {
      PeerGroupAssessmentState courselaunch;
      PeerGroupResourceStore resourcestore = appcontext.getStore();
      courselaunch = new PeerGroupAssessmentState();
      courselaunch.setPersonName( lticlaims.get( "name" ).toString() );
      courselaunch.setPlatformName( lticlaims.getLtiToolPlatform().getUrl() );      
      courselaunch.setCourseId( lticlaims.getLtiContext().getId() );
      courselaunch.setCourseTitle( lticlaims.getLtiContext().getLabel() );
      courselaunch.setResourceId( lticlaims.getLtiResource().getId() );
      courselaunch.setRoles( lticlaims.getLtiRoles() );
      PeerGroupResource resource = resourcestore.get( courselaunch.getPlatformName(), courselaunch.getResourceId(), true );
      courselaunch.setResource( resource );
      if ( lticlaims.getLtiRoles().isInStandardInstructorRole() )
        courselaunch.setAllowedToClearResource( true );
      state.setPeerGroupAssessmentState( courselaunch );
      //response.sendRedirect( response.encodeRedirectURL( request.getContextPath() + "/peergroupassessment/index.jsp?state_id=" + state.getId() ) );
      response.sendRedirect( response.encodeRedirectURL( request.getContextPath() + "/peergrpassess?state_id=" + state.getId() ) );
      return;
    }
    
    // What if we couldn't work out what to do?
    // In this demo, send some debugging information in an HTML page.
    
    response.setContentType( "text/html;charset=UTF-8" );
    try (  PrintWriter out = response.getWriter() )
    {
      /* TODO output your page here. You may use following sample code. */
      out.println( "<!DOCTYPE html>" );
      out.println( "<html>" );
      out.println( "<head>" );
      out.println( "<title>Servlet LaunchServlet</title>" );      
      out.println( "<style>" );
      out.println( "li { padding: 1em 1em 1em 1em; }" );
      out.println( "</style>" );
      out.println( "</head>" );
      out.println( "<body>" );
      out.println( "<h1>Servlet LaunchServlet at " + request.getContextPath() + "</h1>" );

      out.println( "<p>The LTI Launch was not configured properly. The following may help understand what happened.</p> " );

      out.println( "<h2>About the Launch Request</h2>" );
      out.println( "<ul>" );
      out.println( "<li>Tool platform guid<br/>" + lticlaims.getLtiToolPlatform().getGuid() + "</li>" );
      out.println( "<li>Tool platform url<br/>"  + lticlaims.getLtiToolPlatform().getUrl()  + "</li>" );
      out.println( "<li>Context label<br/>"      + lticlaims.getLtiContext().getLabel()     + "</li>" );
      out.println( "<li>Context title<br/>"      + lticlaims.getLtiContext().getTitle()     + "</li>" );
      String type = lticlaims.getLtiContext().getType( 0 );
      if ( type != null )
        out.println( "<li>Context type<br/>"     + type                        + "</li>" );
      out.println( "</ul>" );
      
      
      out.println( "<h2>Technical breakdown of launch request</h2>" );
      out.println( "<table>");
      out.println( "<tr><th>toolname</th><td>" + toolname + "</td></tr>" );
      out.println( "<tr><th>tooltype</th><td>" + tooltype + "</td></tr>" );
      out.println( "</table>");

      out.println( "<h3>LTI Claims</h3>" );
      
      out.println( "<pre>" );
      ArrayList<String> keylist = new ArrayList<>();
      for ( String k :  lticlaims.keySet() )
        keylist.add( k );
      keylist.sort( Comparator.comparing( String::toString ) );
      for ( String k : keylist )
        out.println( k + " = " + lticlaims.get( k ) + "\n" );
      out.println( "</pre>" );
      
      
      out.println( "</body>" );
      out.println( "</html>" );
    }
  }

  /**
   * This implementation ensures that the library code knows how to store
   * LTI state.
   * 
   * @param context The servlet context in whose attributes the store can be found.
   * @return The store.
   */
  @Override
  protected LtiStateStore getLtiStateStore( ServletContext context )
  {
    ApplicationContext appcontext = ApplicationContext.getFromServletContext( context );
    return appcontext.getStateStore();
  }
  
  
}
