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

import uk.ac.leedsbeckett.ltitools.state.AppLtiStateStore;
import uk.ac.leedsbeckett.ltitools.tool.peergroupassessment.PeerGroupResourceStore;
import uk.ac.leedsbeckett.lti.LtiConfiguration;
import javax.servlet.ServletContext;

/**
 * A context object which is specific to our application, is instantiated
 * with the servlet context and holds application-wide information.
 * 
 * @author jon
 */
public class ApplicationContext
{
  public static final String KEY = ApplicationContext.class.getCanonicalName();
  
  // Our context data is split into these three objects
  LtiConfiguration config = new LtiConfiguration();
  PeerGroupResourceStore store = new PeerGroupResourceStore();
  AppLtiStateStore statestore = new AppLtiStateStore();
  
  /**
   * Get this object to add itself to a ServletContext as an attribute.
   * 
   * @param context The ServletContext to add to.
   */
  public void addToServletContext( ServletContext context )
  {
    context.setAttribute( KEY, this );
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

  /**
   * Fetch the application-wide LTIConfiguration.
   * 
   * @return The instance.
   */
  public LtiConfiguration getConfig()
  {
    return config;
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
  public AppLtiStateStore getStateStore()
  {
    return statestore;
  }
}
