/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/ServletListener.java to edit this template
 */
package uk.ac.leedsbeckett.ltidemo;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.apache.commons.lang3.StringUtils;

/**
 * Web application life-cycle listener. Used to instantiate our own
 * context object.
 *
 * @author jon
 */
public class DemoContextListener implements ServletContextListener
{

  @Override
  public void contextInitialized( ServletContextEvent event )
  {
    ServletContext context =event.getServletContext();
    
    DemoApplicationContext appcontext = new DemoApplicationContext();
    appcontext.addToServletContext( context );

    String configpath = context.getRealPath( "/WEB-INF/config.json" );
    if ( !StringUtils.isEmpty( configpath ) )
      appcontext.getConfig().load( configpath );
  }

  @Override
  public void contextDestroyed( ServletContextEvent event )
  {
  }
}
