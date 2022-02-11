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

package uk.ac.leedsbeckett.ltidemo.tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import uk.ac.leedsbeckett.ltidemo.state.CourseLaunchState;
import uk.ac.leedsbeckett.ltidemo.state.DemoState;
import uk.ac.leedsbeckett.lti.state.LtiState;

/**
 * This is a fairly trivial tool. It presents a single page on which user's
 * can add log entries. All users enrolled on the course work on a shared
 * page. User's with the right role can clear the entries.
 * 
 * @author jon
 */
@WebServlet( name = "CourseResourceServlet", urlPatterns =
{
  "/courseresource"
} )
public class CourseResourceServlet extends AbstractDemoToolServlet
{
  SimpleDateFormat dateformat = new SimpleDateFormat();
  
  /**
   * Uses state object to find an object that represents a shared object
   * containing logs entries. Presents the log entries and can add new
   * entries.
   *
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  protected void processRequest( HttpServletRequest request, HttpServletResponse response )
          throws ServletException, IOException
  {    
    // Super class provides this functionality.
    DemoState state = getState( request, response );
    if ( state == null ) return;
    
    CourseLaunchState course = state.getCourseLaunchState();
    if ( course == null )
    {
      response.sendError( 500, "Could not find data about the requested resource." );
      return;
    }
    
    // Find the shared object that contains log entries
    Resource resource = course.getResource();
    
    // If an action has been specified in form data take appropriate
    // action.
    String action = request.getParameter( "action" );
    if ( "add".equals( action ) )
      resource.addEntry( course.getPersonName() );
    else if ( "clear".equals( action ) )
    {
      if ( course.isAllowedToClearResource() )
        resource.clearEntries( course.getPersonName() );
    }
                
    // Now send the HTML output to the user
    response.setContentType( "text/html;charset=UTF-8" );
    try (  PrintWriter out = response.getWriter() )
    {
      out.println( "<!DOCTYPE html>" );
      out.println( "<html>" );
      out.println( "<head>" );
      out.println( "<title>Servlet CourseResourceServlet</title>" );      
      out.println( "</head>" );
      out.println( "<body>" );
      out.println( "<h1>A Course Level Tool On " + request.getServerName() + "</h1>" );
      
      out.println( "<h2>The Resource</h2>" );
      out.println( "<p>This is an extremely simple, not very useful web resource "    );
      out.println( "which serves to demonstrate how to build an LTI 1.3 tool. "       );
      out.println( "It consists of a simple log of activity. All users can click a "  );
      out.println( "button and add an entry and users with 'instructor' role in the " );
      out.println( "course that launched this resource can clear entries.</p>"        );
      
      if ( resource == null )
      {
        out.println( "Unable to load the resource." );
      }
      else
      {
        out.println( "<ol>" );
        for ( ResourceEntry entry : resource.getEntries() )
        {
          out.print( "<li><em>" );
          out.print( dateformat.format( new Date( entry.getTimestamp() ) ) );
          out.print( "</em> <strong>" );
          out.print( entry.getPerson() );
          out.print( "</strong> {" );
          out.print( entry.getMessage() );
          out.println( "}</li>" );
        }
        out.println( "</ol>" );
      }
      
      String baseurl = request.getContextPath() + "/courseresource";
      
      out.print( "<form method=\"get\" action=\"" );
      out.print( response.encodeURL( baseurl ) );
      out.println( "\">" );
      out.println( "<input type=\"hidden\" name=\"state_id\" value=\"" + state.getId() + "\"/>" );
      out.println( "<input type=\"submit\"                   value=\"Reload\"/>" );
      out.println( "</form>" );
      
      out.print( "<form method=\"get\" action=\"" );
      out.print( response.encodeURL( baseurl ) );
      out.println( "\">" );
      out.println( "<input type=\"hidden\" name=\"state_id\" value=\"" + state.getId() + "\"/>" );
      out.println( "<input type=\"hidden\" name=\"action\"   value=\"add\"/>" );
      out.println( "<input type=\"submit\"                   value=\"Add Entry\"/>" );
      out.println( "</form>" );
      
      if ( course.isAllowedToClearResource() )
      {
        out.print( "<form method=\"get\" action=\"" );
        out.print( response.encodeURL( baseurl ) );
        out.println( "\">" );
        out.println( "<input type=\"hidden\" name=\"state_id\" value=\"" + state.getId() + "\"/>" );
        out.println( "<input type=\"hidden\" name=\"action\"   value=\"clear\"/>" );
        out.println( "<input type=\"submit\"                   value=\"Clear Entries\"/>" );
        out.println( "</form>" );
      }

      
      out.println( "<h2>About the Resource</h2>" );
      out.println( "<p>Information of interest to developers.</p>" );
      out.println( "<p>According to <strong>" + course.getPlatformName() + "</strong> " );
      out.println( "you are <strong>" + course.getPersonName() + "</strong></p>" );
      out.println( "<p>You are accessing a resource with link ID <strong>" + course.getResourceId() + "</strong>, ");
      out.println( "In the course <strong>" + course.getCourseTitle() + "</strong></p>" );
      out.println( "<p>Your roles for this resource</p><ul>" );
      for ( int i=0; i<course.getRoles().getSize(); i++ )
        out.println( "<li><strong>" + course.getRoles().getAsString( i ) + "</strong></li>" );
      out.println( "</ul>" );
            
      out.println( "</body>" );
      out.println( "</html>" );
    }
  }

  
  // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
  /**
   * Handles the HTTP <code>GET</code> method.
   *
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  @Override
  protected void doGet( HttpServletRequest request, HttpServletResponse response )
          throws ServletException, IOException
  {
    processRequest( request, response );
  }

  /**
   * Handles the HTTP <code>POST</code> method.
   *
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  @Override
  protected void doPost( HttpServletRequest request, HttpServletResponse response )
          throws ServletException, IOException
  {
    processRequest( request, response );
  }

  /**
   * Returns a short description of the servlet.
   *
   * @return a String containing servlet description
   */
  @Override
  public String getServletInfo()
  {
    return "Short description";
  }// </editor-fold>

}
