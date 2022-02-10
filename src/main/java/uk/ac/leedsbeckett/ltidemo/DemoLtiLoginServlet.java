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
