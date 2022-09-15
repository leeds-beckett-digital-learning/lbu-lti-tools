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

import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.logging.Logger;
import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import uk.ac.leedsbeckett.ltitools.tool.peergroupassessment.PeerGroupResourceStore;
import uk.ac.leedsbeckett.lti.config.LtiConfiguration;
import javax.servlet.ServletContext;
import javax.websocket.WebSocketContainer;
import javax.websocket.server.ServerContainer;
import uk.ac.leedsbeckett.lti.state.LtiStateStore;
import uk.ac.leedsbeckett.ltitools.state.AppLtiState;
import uk.ac.leedsbeckett.ltitools.state.AppLtiStateSupplier;

/**
 * A context object which is specific to our application, is instantiated
 * with the servlet context and holds application-wide information.
 * 
 * @author jon
 */
public class ApplicationContext
{
  static final Logger logger = Logger.getLogger( ApplicationContext.class.getName() );
  
  public static final String KEY = ApplicationContext.class.getCanonicalName();
  static HashMap<WebSocketContainer,ApplicationContext> servercontainermap = new HashMap<>();
  
  ServletContext servletcontext;
  
  // Our context data is split into these three objects
  LtiConfiguration lticonfig;
  PeerGroupResourceStore store;
  LtiStateStore<AppLtiState> ltistatestore;
  
  public ApplicationContext( ServletContext context )
  {
    servletcontext = context;
    context.setAttribute( KEY, this );    
    lticonfig = new LtiConfiguration();
    store = new PeerGroupResourceStore( Paths.get( context.getRealPath( "/WEB-INF/cache/" ) ) );
    
    Cache<String, AppLtiState> cache;
    CacheManager manager = Caching.getCachingProvider().getCacheManager();
    MutableConfiguration<String, AppLtiState> cacheconfig = 
            new MutableConfiguration<String, AppLtiState>()
                    .setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(Duration.ONE_HOUR));
    cache = manager.createCache( "appltistate", cacheconfig );
    ltistatestore = new LtiStateStore<>( cache, new AppLtiStateSupplier() );
    
    ServerContainer sc = (ServerContainer)context.getAttribute( ServerContainer.class.getName() );
    if ( sc != null )
      servercontainermap.put( sc, this );
  }
  
  /**
   * A static method that will retrieve an instance from an attribute of
   * a ServletContext.
   * 
   * @param context The ServletContext from which to fetch the instance.
   * @return The instance or null if not found.
   */
  public static ApplicationContext getFromServletContext( ServletContext context )
  {
    return (ApplicationContext)context.getAttribute( KEY );
  }

  public static ApplicationContext getFromWebSocketContainer( WebSocketContainer wscontainer )
  {
    return servercontainermap.get( wscontainer );
  }

  /**
   * Fetch the application-wide LTIConfiguration.
   * 
   * @return The instance.
   */
  public LtiConfiguration getConfig()
  {
    return lticonfig;
  }

  /**
   * Fetch the application-wide ResourceStore
   * 
   * @return The instance.
   */
  public PeerGroupResourceStore getStore()
  {
    return store;
  }

  /**
   * Fetch the application-wide state store.
   * 
   * @return 
   */
  public LtiStateStore<AppLtiState> getStateStore()
  {
    return ltistatestore;
  }
}
