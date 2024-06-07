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
import java.util.regex.Pattern;


/**
 * Represents the configuration of the Self Enrol LTI tool. Loads from a JSON file.
 * 
 * @author jon
 */
public class SelfEnrolConfiguration implements Serializable
{
  final Pattern courseSearchValidation;
  final String courseSearchFilter;
  final Pattern organizationSearchValidation;
  final String organizationSearchFilter;
  final String trainingSearchSpecification;
  final String trainingSearchFilter;
  final String smtpHost;
  final String adminEmailAddress;
  final String courseAdvice;
  final String organizationAdvice;
  final String trainingAdvice;
  final String courseEmail;
  final String organizationEmail;

  public SelfEnrolConfiguration( 
          @JsonProperty( "courseSearchValidation" )       String courseSearchValidation, 
          @JsonProperty( "courseSearchFilter" )           String courseSearchFilter, 
          @JsonProperty( "organizationSearchValidation" ) String organizationSearchValidation, 
          @JsonProperty( "organizationSearchFilter" )     String organizationSearchFilter,
          @JsonProperty( "trainingSearchSpecification" )  String trainingSearchSpecification,
          @JsonProperty( "trainingSearchFilter" )         String trainingSearchFilter,
          @JsonProperty( "smtpHost" )                     String smtpHost,
          @JsonProperty( "adminEmailAddress" )            String adminEmailAddress,
          @JsonProperty( "courseAdvice" )                 String courseAdvice,
          @JsonProperty( "organizationAdvice" )           String organizationAdvice,
          @JsonProperty( "trainingAdvice" )               String trainingAdvice,
          @JsonProperty( "courseEmail" )                  String courseEmail,
          @JsonProperty( "organizationEmail" )            String organizationEmail
          )
  {
    this.courseSearchValidation       = Pattern.compile( courseSearchValidation );
    this.courseSearchFilter           = courseSearchFilter;
    this.organizationSearchValidation = Pattern.compile( organizationSearchValidation );
    this.organizationSearchFilter     = organizationSearchFilter;
    this.trainingSearchSpecification  = trainingSearchSpecification;
    this.trainingSearchFilter         = trainingSearchFilter;
    this.smtpHost                     = smtpHost;
    this.adminEmailAddress            = adminEmailAddress;
    this.courseAdvice                 = courseAdvice;
    this.organizationAdvice           = organizationAdvice;
    this.trainingAdvice               = trainingAdvice;
    this.courseEmail                  = courseEmail;
    this.organizationEmail            = organizationEmail;
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

  public String getTrainingSearchSpecification()
  {
    return trainingSearchSpecification;
  }

  public String getTrainingSearchFilter()
  {
    return trainingSearchFilter;
  }
  
  public String getSmtpHost()
  {
    return smtpHost;
  }

  public String getAdminEmailAddress()
  {
    return adminEmailAddress;
  }

  public String getCourseAdvice()
  {
    return courseAdvice;
  }

  public String getOrganizationAdvice()
  {
    return organizationAdvice;
  }

  public String getTrainingAdvice()
  {
    return trainingAdvice;
  }

  public String getCourseEmail()
  {
    return courseEmail;
  }

  public String getOrganizationEmail()
  {
    return organizationEmail;
  }

  
  public static SelfEnrolConfiguration getDefaultConfig()
  {
    return new SelfEnrolConfiguration(
            "^\\d\\d\\d$",
            "^@$",
            "^\\d\\d\\d$",
            "^@$",
            "TRAINING",
            "^TRAINING$",
            "mailrelayhere.com",
            "admin@mailrelayhere.com",
            "Course advice HTML here.",
            "Org advice HTML here.",
            "Training advice HTML here.",
            "Email text here.",
            "Email text here."
    );
  }
}
