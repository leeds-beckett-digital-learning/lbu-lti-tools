/*
 * Copyright 2026 maber01.
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
package uk.ac.leedsbeckett.ltitools.observerassign;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author maber01
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ObserverAssignPlatformConfiguration
{
  private final String smtpHost;
  private final String adminEmailAddress;

  public ObserverAssignPlatformConfiguration(
          @JsonProperty( value ="smtpHost", required = true )                        String smtpHost,
          @JsonProperty( value ="adminEmailAddress", required = true )               String adminEmailAddress )
  {
    this.smtpHost = smtpHost;
    this.adminEmailAddress = adminEmailAddress;
  }

  public String getSmtpHost() {
    return smtpHost;
  }

  public String getAdminEmailAddress() {
    return adminEmailAddress;
  }

  public static ObserverAssignPlatformConfiguration getDefaultConfig()
  {
    return new ObserverAssignPlatformConfiguration(
            "mailrelayhere.com",
            "admin@mailrelayhere.com"
    );
  }
  
}
