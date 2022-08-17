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

package uk.ac.leedsbeckett.ltitools.state;

import uk.ac.leedsbeckett.lti.LtiConfiguration;
import uk.ac.leedsbeckett.lti.state.LtiState;
import uk.ac.leedsbeckett.lti.state.LtiStateStore;

/**
 * A customised subclass of the standard LtiStateStore. The only different
 * from the base class is that it creates a DemoState which is a subclass
 * of the standard LtiState. This ensures that we can put customised data
 * and functionality into the state that suits this tool.
 * 
 * @author jon
 */
public class AppLtiStateStore extends LtiStateStore
{
  /**
   * Makes sure that the state object used throughout the LTI handling
   * is a customised LtiState.
   * 
   * @param client The issuer client configuration.
   * @return An implementation of LtiState.
   */
  @Override
  protected LtiState newState( LtiConfiguration.Client client )
  {
    return new AppState( client );    
  }  
}
