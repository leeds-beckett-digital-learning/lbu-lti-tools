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

package uk.ac.leedsbeckett.ltitools.selfenrol;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.logging.Logger;
import java.util.regex.Pattern;


/**
 * Represents the configuration of the Self Enrol LTI tool. Loads from a JSON file.
 * 
 * @author jon
 */
public class SelfEnrolConfiguration implements Serializable
{
  static final Logger logger = Logger.getLogger(SelfEnrolConfiguration.class.getName() );
  
  final Pattern courseSearchValidation;
  final String courseSearchFilter;
  final Pattern organizationSearchValidation;
  final String organizationSearchFilter;

  public SelfEnrolConfiguration( 
          @JsonProperty( "courseSearchValidation" )       String courseSearchValidation, 
          @JsonProperty( "courseSearchFilter" )           String courseSearchFilter, 
          @JsonProperty( "organizationSearchValidation" ) String organizationSearchValidation, 
          @JsonProperty( "organizationSearchFilter" )     String organizationSearchFilter )
  {
    this.courseSearchValidation       = Pattern.compile( courseSearchValidation );
    this.courseSearchFilter           = courseSearchFilter;
    this.organizationSearchValidation = Pattern.compile( organizationSearchValidation );
    this.organizationSearchFilter     = organizationSearchFilter;
  }
  
  
  public Pattern getCourseSearchValidation()
  {
    return courseSearchValidation;
  }

  public String getCourseSearchFilter()
  {
    return courseSearchFilter;
  }

  public Pattern getOrganizationSearchValidation()
  {
    return organizationSearchValidation;
  }

  public String getOrganizationSearchFilter()
  {
    return organizationSearchFilter;
  }  
}