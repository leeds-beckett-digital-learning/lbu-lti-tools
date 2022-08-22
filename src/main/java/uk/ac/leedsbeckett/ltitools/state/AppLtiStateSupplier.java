/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uk.ac.leedsbeckett.ltitools.state;

import uk.ac.leedsbeckett.lti.config.ClientLtiConfigurationKey;
import uk.ac.leedsbeckett.lti.state.LtiStateSupplier;

/**
 *
 * @author jon
 */
public class AppLtiStateSupplier implements LtiStateSupplier<AppLtiState>
{
  @Override
  public AppLtiState get( ClientLtiConfigurationKey ck )
  {
    return new AppLtiState( ck );
  }
}
