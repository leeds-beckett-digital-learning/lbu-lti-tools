/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uk.ac.leedsbeckett.ltitools.tool.peergroupassessment;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import uk.ac.leedsbeckett.ltitools.tool.LtiPageSupport;

/**
 * Support class for PeerGroupPage related JSP pages.
 * @author jon
 */
public class PeerGroupPageSupport extends LtiPageSupport
{
  static final Logger logger = Logger.getLogger( PeerGroupPageSupport.class.getName() );
  
  PeerGroupAssessmentState pgaState;
  PeerGroupResource pgaResource;
  
  @Override
  public void setRequest(HttpServletRequest request) throws ServletException
  {
    super.setRequest( request );
    logger.log( Level.FINE, "setRequest() state id = {0}", state.getId() );
    pgaState = state.getPeerGroupAssessmentState();
    if ( pgaState == null )
      throw new ServletException( "Could not find peer group assessment tool session data." );
    pgaResource = this.appcontext.getStore().get( pgaState.getResourceKey(), true );
  }

  public PeerGroupResource getPgaResource()
  {
    return pgaResource;
  }
}
