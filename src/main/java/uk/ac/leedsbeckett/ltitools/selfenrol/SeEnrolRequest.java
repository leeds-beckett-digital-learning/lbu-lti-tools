/*
 * Copyright 2023 maber01.
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
package uk.ac.leedsbeckett.ltitools.selfenrol;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/**
 *
 * @author maber01
 */
public class SeEnrolRequest implements Serializable
{
  final String courseId;
  final String authType;
  final String authName;
  final String authEmail;

  public SeEnrolRequest( 
          @JsonProperty("courseId")         String courseId,
          @JsonProperty("authType")         String authType,
          @JsonProperty("authName")         String authName,
          @JsonProperty("authEmail")        String authEmail
  )
  {
    this.courseId  = courseId;
    this.authType  = authType;
    this.authName  = authName;
    this.authEmail = authEmail;
  }

  public String getCourseId()
  {
    return courseId;
  }  

  public String getAuthType()
  {
    return authType;
  }

  public String getAuthName()
  {
    return authName;
  }

  public String getAuthEmail()
  {
    return authEmail;
  }
  
}
