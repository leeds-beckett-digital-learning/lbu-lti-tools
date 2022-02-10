/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uk.ac.leedsbeckett.ltidemo;

import javax.servlet.ServletContext;
import javax.servlet.annotation.WebServlet;
import uk.ac.leedsbeckett.lti.LtiConfiguration;
import uk.ac.leedsbeckett.lti.servlet.LtiLoginServlet;
import uk.ac.leedsbeckett.lti.state.LtiStateStore;

/**
 * This demo's implementation of the LTI login servlet.
 * 
 * @author jon
 */
@WebServlet(name = "DemoLtiLoginServlet", urlPatterns = { FixedLtiConfiguration.LOGIN_PATTERN })
public class DemoLtiLoginServlet extends LtiLoginServlet
{

  @Override
  protected LtiStateStore getLtiStateStore( ServletContext context )
  {
    DemoApplicationContext appcontext = DemoApplicationContext.getFromServletContext( context );
    return appcontext.getStateStore();
  }  
  
  protected LtiConfiguration getLtiConfiguration( ServletContext context )
  {
    DemoApplicationContext appcontext = DemoApplicationContext.getFromServletContext( context );
    return appcontext.getConfig();
  }

}
