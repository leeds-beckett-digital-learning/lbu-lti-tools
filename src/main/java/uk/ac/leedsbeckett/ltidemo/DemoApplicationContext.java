/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uk.ac.leedsbeckett.ltidemo;

import uk.ac.leedsbeckett.ltidemo.tool.ResourceStore;
import uk.ac.leedsbeckett.lti.LtiConfiguration;
import javax.servlet.ServletContext;

/**
 * A context object which is specific to our application, is instantiated
 * with the servlet context and holds application-wide information.
 * 
 * @author jon
 */
public class DemoApplicationContext
{
  public static final String KEY = DemoApplicationContext.class.getCanonicalName();
  
  
  LtiConfiguration config = new LtiConfiguration();
  ResourceStore store = new ResourceStore();
  DemoLtiStateStore statestore = new DemoLtiStateStore();
  
  
  public void addToServletContext( ServletContext context )
  {
    context.setAttribute( KEY, this );
  }
  
  public static DemoApplicationContext getFromServletContext( ServletContext context )
  {
    return (DemoApplicationContext)context.getAttribute( KEY );
  }

  public LtiConfiguration getConfig()
  {
    return config;
  }

  public ResourceStore getStore()
  {
    return store;
  }

  public DemoLtiStateStore getStateStore()
  {
    return statestore;
  }
}
