/*
 * Copyright 2022 maber01.
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
package uk.ac.leedsbeckett.ltitools.tool;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.HandlesTypes;
import javax.websocket.WebSocketContainer;
import javax.websocket.server.ServerContainer;
import uk.ac.leedsbeckett.ltitools.tool.annotations.LtiTool;

/**
 *
 * @author maber01
 */
@HandlesTypes( {LtiTool.class} )
public class ToolManager implements ServletContainerInitializer
{
  static final Logger logger = Logger.getLogger( ToolManager.class.getName() );
  public static final String KEY = ToolManager.class.getCanonicalName();
  static HashMap<WebSocketContainer,ToolManager> servercontainermap = new HashMap<>();

  /**
   * A static method that will retrieve an instance from an attribute of
   * a ServletContext.
   * 
   * @param context The ServletContext from which to fetch the instance.
   * @return The instance or null if not found.
   */
  public static ToolManager getFromServletContext( ServletContext context )
  {
    return (ToolManager)context.getAttribute( KEY );
  }

  public static ToolManager getFromWebSocketContainer( WebSocketContainer wscontainer )
  {
    return servercontainermap.get( wscontainer );
  }

  
  
  HashMap<ToolKey,Tool> toolMap = new HashMap<>();
  
  @Override
  public void onStartup( Set<Class<?>> c, ServletContext ctx ) throws ServletException
  {
    ctx.setAttribute( KEY, this );
    ServerContainer sc = (ServerContainer)ctx.getAttribute( ServerContainer.class.getName() );
    if ( sc != null )
      servercontainermap.put( sc, this );
    
    logger.log( Level.INFO, "My initalizer startup method." );
    for ( Class<?> cl : c )
    {
      logger.log( Level.INFO, "My initalizer found this class: {0}", cl.getName() );
      LtiTool[] aarray = cl.getAnnotationsByType( LtiTool.class );
      if ( aarray == null || aarray.length != 1 )
      {
        logger.log( Level.SEVERE, "This tool class, {0} doesn't have exactly one LtiTool annotation.", cl.getName() );
        continue;
      }
      
      if ( !Tool.class.isAssignableFrom( cl ) )
      {
        logger.log( Level.SEVERE, "This tool class, {0} is not an implementation of Tool interface.", cl.getName() );
        continue;        
      }
      
      LtiTool ltiToolAnnotation = aarray[0];
      try
      {
        Tool tool = (Tool) cl.getDeclaredConstructor().newInstance();
        tool.init( ctx );
        toolMap.put( new ToolKey( ltiToolAnnotation ), tool );
      }
      catch ( IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | SecurityException | InvocationTargetException ex )
      {
        logger.log( Level.SEVERE, "This tool class, {0} could not be instantiated.", cl.getName() );
        logger.log( Level.SEVERE, "Exception thrown.", ex );
      }      
    }
  }

  public Tool getTool( ToolKey key )
  {
    return toolMap.get( key );
  }
  
  public Tool getTool( String type, String name )
  {
    return toolMap.get( new ToolKey( type, name ) );
  }

}
