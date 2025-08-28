/*
 * Copyright 2025 maber01.
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
package uk.ac.leedsbeckett.ltitools.sharepointsub.bbdata;

import uk.ac.leedsbeckett.jesharepoint.sptypes.SpUser;

/**
 *
 * @author maber01
 */
public class SitePerson
{
  String bbId;
  String email;
  SpUser user;

  public String getBbId()
  {
    return bbId;
  }

  public void setBbId( String bbId )
  {
    this.bbId = bbId;
  }

  public String getEmail()
  {
    return email;
  }

  public void setEmail( String email )
  {
    this.email = email;
  }

  public SpUser getUser()
  {
    return user;
  }

  public void setUser( SpUser user )
  {
    this.user = user;
  }
  
}
