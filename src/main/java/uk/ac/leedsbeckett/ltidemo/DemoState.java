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

import uk.ac.leedsbeckett.lti.LtiConfiguration;
import uk.ac.leedsbeckett.lti.state.LaunchState;
import uk.ac.leedsbeckett.lti.state.LtiState;

/**
 *
 * @author jon
 */
public class DemoState extends LtiState
{
  CourseLaunchState   courseLaunchState = null;
  LaunchState       platformLaunchState = null;
  
  public DemoState( LtiConfiguration.Client client )
  {
    super( client );
  }
  
  public LaunchState getPlatformLaunchState()
  {
    return platformLaunchState;
  }

  public void setPlatformLaunchState( LaunchState platformLaunchState )
  {
    this.platformLaunchState = platformLaunchState;
  }

  public CourseLaunchState getCourseLaunchState()
  {
    return courseLaunchState;
  }

  public void setCourseLaunchState( CourseLaunchState launchState )
  {
    this.courseLaunchState = launchState;
  }  
  
}
