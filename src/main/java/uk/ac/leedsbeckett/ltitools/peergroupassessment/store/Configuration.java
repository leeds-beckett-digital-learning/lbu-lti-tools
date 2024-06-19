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

package uk.ac.leedsbeckett.ltitools.peergroupassessment.store;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * Represents the configuration of the Self Enrol LTI tool. Loads from a JSON file.
 * 
 * @author jon
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Configuration
{
  private final boolean membershipInstructorDeepLinkPermitted;
  

  @JsonCreator
  private Configuration( 
          @JsonProperty( value = "membershipInstructorDeepLinkPermitted", required = true ) 
                  boolean membershipInstructorDeepLinkPermitted
          )
  {
    this.membershipInstructorDeepLinkPermitted = membershipInstructorDeepLinkPermitted;
  }

  public boolean isMembershipInstructorDeepLinkPermitted()
  {
    return membershipInstructorDeepLinkPermitted;
  }
    
  public static Configuration getDefaultConfig()
  {
    return new Configuration( false );
  }
}
