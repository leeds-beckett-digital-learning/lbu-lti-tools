/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
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
