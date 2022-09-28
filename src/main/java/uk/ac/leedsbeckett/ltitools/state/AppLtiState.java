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

import java.io.Serializable;
import uk.ac.leedsbeckett.lti.config.ClientLtiConfigurationKey;
import uk.ac.leedsbeckett.lti.state.LtiState;
import uk.ac.leedsbeckett.ltitools.tool.Tool;
import uk.ac.leedsbeckett.ltitools.tool.ToolKey;

/**
 * This demo's customised subclass of LtiState which can store additional
 * information which is specific to this tool.
 * 
 * @author jon
 */
public class AppLtiState extends LtiState implements Serializable
{
  /**
   * Data that relates to the course-content servlet.
   */
  AppSessionState   appSessionState = null;
  
  ToolKey toolKey = null;
  
  /**
   * Constructor of this state must make sure the superclass constructor
   * is called.
   * 
   * @param clientKey
   */
  public AppLtiState( ClientLtiConfigurationKey clientKey )
  {
    super( clientKey );
  }

  /**
   * Get the course content launch state from within this LTI state.
   * 
   * @return The course content state.
   */
  public AppSessionState getAppSessionState()
  {
    return appSessionState;
  }

  /**
   * Set the course content state.
   * 
   * @param launchState The course content state.
   */
  public void setAppSessionState( AppSessionState launchState )
  {
    this.appSessionState = launchState;
  }

  public ToolKey getToolKey()
  {
    return toolKey;
  }

  public void setToolKey( ToolKey toolKey )
  {
    this.toolKey = toolKey;
  }
}
