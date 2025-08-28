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

/**
 *
 * @author maber01
 */
public class ModulePerson
{
  private String bbId;
  private String email;
  private String role;

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
    if ( email != null )
      this.email = email.toLowerCase();
  }

  public String getRole()
  {
    return role;
  }

  public void setRole( String role )
  {
    this.role = role;
    if ( role != null )
      this.role = role.toLowerCase();
  }
  
  public boolean isStudent()
  {
    return "Student".equalsIgnoreCase( role );
  }
  
  public boolean isMarker()
  {
    return "Marker".equalsIgnoreCase( role );
  }
  
  public String getName()
  {
    if ( email == null ) return null;
    int n = email.indexOf( "@" );
    if ( n < 1 ) return null;
    return email.substring( 0, n );
  }
}
