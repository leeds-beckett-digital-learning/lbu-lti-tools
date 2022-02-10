/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uk.ac.leedsbeckett.ltidemo.tool;

import uk.ac.leedsbeckett.lti.state.LtiStateStore;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import uk.ac.leedsbeckett.ltidemo.DemoApplicationContext;
import uk.ac.leedsbeckett.ltidemo.DemoState;
import uk.ac.leedsbeckett.lti.state.LtiState;

/**
 *
 * @author jon
 */
public abstract class AbstractDemoToolServlet extends HttpServlet
{
  protected DemoState getState( HttpServletRequest request, HttpServletResponse response )
          throws ServletException, IOException
  {
    String stateid = request.getParameter( "state_id" );
    if ( stateid == null )
    {
      response.sendError( 500, "State ID missing." );
      return null;
    }

    DemoApplicationContext appcontext = DemoApplicationContext.getFromServletContext( request.getServletContext() );
    LtiStateStore statestore = appcontext.getStateStore();
    if ( statestore == null )
    {
      response.sendError( 500, "State store missing." );
      return null;
    }
    
    LtiState state = statestore.getState( stateid );
    if ( state == null )
    {
      response.sendError( 500, "State missing. " + stateid );
      return null;
    }
    
    if ( !(state instanceof DemoState) )
    {
      response.sendError( 500, "Wrong type of state. " + state.getClass().getName() );
      return null;
    }
    
    return (DemoState)state;
  }  
}
