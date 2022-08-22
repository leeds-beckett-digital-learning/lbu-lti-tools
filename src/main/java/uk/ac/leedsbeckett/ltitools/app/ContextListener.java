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

package uk.ac.leedsbeckett.ltitools.app;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.apache.commons.lang3.StringUtils;
import uk.ac.leedsbeckett.lti.state.LtiStateStore;

/**
 * Web application life-cycle listener. Used to instantiate our own
 * context object.
 *
 * @author jon
 */
public class ContextListener implements ServletContextListener
{
  static final Logger logger = Logger.getLogger( ContextListener.class.getName() );

  /**
   * This will be called when the web application is initialised. So some
   * data objects are created and stored in the ServletContext.
   * 
   * @param event The event which tells us about the new ServletContext.
   */
  @Override
  public void contextInitialized( ServletContextEvent event )
  {
    org.slf4j.Logger slf4jlogger = org.slf4j.LoggerFactory.getLogger( ContextListener.class );
    slf4jlogger.error( "SLF4J logging is working" );
    
    logger.severe( "LBU LTI tools - context initialised." );
    ServletContext context =event.getServletContext();
    
    ApplicationContext appcontext = new ApplicationContext( context );

    String configpath = context.getRealPath( "/WEB-INF/config.json" );
    if ( !StringUtils.isEmpty( configpath ) )
      appcontext.getConfig().load( configpath );
  }
  
  /**
   * At present out app doesn't need to do any cleaning up, but it could
   * be done here.
   * 
   * @param event 
   */
  @Override
  public void contextDestroyed( ServletContextEvent event )
  {
  }
}
