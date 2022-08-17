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

import uk.ac.leedsbeckett.lti.claims.LtiRoleClaims;

/**
 * This contains data that a user of the platform-wide tool might need.
 * 
 * @author jon
 */
public class LaunchState
{
  private String personName;
  private String platformName;
  private LtiRoleClaims roles;

  public String getPersonName()
  {
    return personName;
  }

  public void setPersonName( String personName )
  {
    this.personName = personName;
  }

  public String getPlatformName()
  {
    return platformName;
  }

  public void setPlatformName( String platformName )
  {
    this.platformName = platformName;
  }

  public LtiRoleClaims getRoles()
  {
    return roles;
  }

  public void setRoles( LtiRoleClaims roles )
  {
    this.roles = roles;
  }
  
  
}
