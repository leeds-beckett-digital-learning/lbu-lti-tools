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

/**
 * This demo's customised subclass of LtiState which can store additional
 * information which is specific to this tool.
 * 
 * @author jon
 */
public class DemoState extends LtiState
{
  /**
   * Data that relates to the course-content servlet.
   */
  CourseLaunchState   courseLaunchState = null;
  
  /**
   * Data that relates to the platform wide servlet.
   */
  LaunchState       platformLaunchState = null;
  
  /**
   * Constructor of this state must make sure the superclass constructor
   * is called.
   * 
   * @param client The LTI issuer client configuration.
   */
  public DemoState( LtiConfiguration.Client client )
  {
    super( client );
  }
  
  /**
   * Get the platform launch state from within this LTI state.
   * 
   * @return The platform state.
   */
  public LaunchState getPlatformLaunchState()
  {
    return platformLaunchState;
  }

  /**
   * Set the platform launch state during launch.
   * 
   * @param platformLaunchState The platform state.
   */
  public void setPlatformLaunchState( LaunchState platformLaunchState )
  {
    this.platformLaunchState = platformLaunchState;
  }

  /**
   * Get the course content launch state from within this LTI state.
   * 
   * @return The course content state.
   */
  public CourseLaunchState getCourseLaunchState()
  {
    return courseLaunchState;
  }

  /**
   * Set the course content state.
   * 
   * @param launchState The course content state.
   */
  public void setCourseLaunchState( CourseLaunchState launchState )
  {
    this.courseLaunchState = launchState;
  }  
  
}
