/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uk.ac.leedsbeckett.ltidemo;

import uk.ac.leedsbeckett.lti.LtiConfiguration;
import uk.ac.leedsbeckett.lti.state.LtiState;
import uk.ac.leedsbeckett.lti.state.LtiStateStore;

/**
 *
 * @author jon
 */
public class DemoLtiStateStore extends LtiStateStore
{
  @Override
  protected LtiState newState( LtiConfiguration.Client client )
  {
    return new DemoState( client );    
  }  
}
