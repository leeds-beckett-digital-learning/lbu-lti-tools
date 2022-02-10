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
