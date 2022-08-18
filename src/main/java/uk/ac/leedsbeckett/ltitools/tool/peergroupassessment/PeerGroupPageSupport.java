/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uk.ac.leedsbeckett.ltitools.tool.peergroupassessment;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import uk.ac.leedsbeckett.ltitools.tool.PageSupport;

/**
 *
 * @author jon
 */
public class PeerGroupPageSupport extends PageSupport
{
  PeerGroupAssessmentState pgaState;
  PeerGroupResource pgaResourse;
  
  @Override
  public void setRequest(HttpServletRequest request) throws ServletException
  {
    super.setRequest(request); 
    pgaState = state.getPeerGroupAssessmentState();
    if ( pgaState == null )
      throw new ServletException( "Could not find data about the requested resource." );    
    pgaResourse = pgaState.getResource();
  }

  public PeerGroupResource getPgaResourse()
  {
    return pgaResourse;
  }
}
