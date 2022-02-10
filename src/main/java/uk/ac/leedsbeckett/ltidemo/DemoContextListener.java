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
