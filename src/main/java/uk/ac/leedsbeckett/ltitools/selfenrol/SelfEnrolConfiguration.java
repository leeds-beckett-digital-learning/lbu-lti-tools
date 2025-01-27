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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.regex.Pattern;


/**
 * Represents the configuration of the Self Enrol LTI tool. Loads from a JSON file.
 * 
 * @author jon
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SelfEnrolConfiguration
{
  // Optional in serialised object
  @JsonProperty( value ="membershipInstructorDeepLinkPermitted", required = false )
  private final boolean membershipInstructorDeepLinkPermitted;
  
  // Required in serialised object
  private final Pattern courseSearchValidation;
  private final String courseSearchFilter;
  private final Pattern organizationSearchValidation;
  private final String organizationSearchFilter;
  private final String trainingSearchSpecification;
  private final String trainingSearchFilter;
  private final String resourceRole;
  private final String resourceSearchSpecification;
  private final String resourceSearchFilter;
  private final String smtpHost;
  private final String adminEmailAddress;
  private final String courseAdvice;
  private final String organizationAdvice;
  private final String trainingAdvice;
  private final String resourceAdvice;
  private final String courseEmail;
  private final String organizationEmail;

  @JsonCreator
  private SelfEnrolConfiguration( 
          @JsonProperty( value ="courseSearchValidation", required = true )          String courseSearchValidation, 
          @JsonProperty( value ="courseSearchFilter", required = true )              String courseSearchFilter, 
          @JsonProperty( value ="organizationSearchValidation", required = true )    String organizationSearchValidation, 
          @JsonProperty( value ="organizationSearchFilter", required = true )        String organizationSearchFilter,
          @JsonProperty( value ="trainingSearchSpecification", required = true )     String trainingSearchSpecification,
          @JsonProperty( value ="trainingSearchFilter", required = true )            String trainingSearchFilter,
          @JsonProperty( value ="resourceRole", required = false )                   String resourceRole,
          @JsonProperty( value ="resourceSearchSpecification", required = false )    String resourceSearchSpecification,
          @JsonProperty( value ="resourceSearchFilter", required = false )           String resourceSearchFilter,
          @JsonProperty( value ="smtpHost", required = true )                        String smtpHost,
          @JsonProperty( value ="adminEmailAddress", required = true )               String adminEmailAddress,
          @JsonProperty( value ="courseAdvice", required = true )                    String courseAdvice,
          @JsonProperty( value ="organizationAdvice", required = true )              String organizationAdvice,
          @JsonProperty( value ="trainingAdvice", required = true )                  String trainingAdvice,
          @JsonProperty( value ="resourceAdvice", required = false )                 String resourceAdvice,
          @JsonProperty( value ="courseEmail", required = true )                     String courseEmail,
          @JsonProperty( value ="organizationEmail", required = true )               String organizationEmail
          )
  {
    this.membershipInstructorDeepLinkPermitted = false;
    
    this.courseSearchValidation       = Pattern.compile( courseSearchValidation );
    this.courseSearchFilter           = courseSearchFilter;
    this.organizationSearchValidation = Pattern.compile( organizationSearchValidation );
    this.organizationSearchFilter     = organizationSearchFilter;
    this.trainingSearchSpecification  = trainingSearchSpecification;
    this.trainingSearchFilter         = trainingSearchFilter;
    this.resourceRole                 = (resourceRole==null)?"Instructor":resourceRole;
    this.resourceSearchSpecification  = (resourceSearchSpecification==null)?"RESOURCE":resourceSearchSpecification;
    this.resourceSearchFilter         = (resourceSearchFilter==null)?"^RESOURCE$":resourceSearchFilter;
    this.smtpHost                     = smtpHost;
    this.adminEmailAddress            = adminEmailAddress;
    this.courseAdvice                 = courseAdvice;
    this.organizationAdvice           = organizationAdvice;
    this.trainingAdvice               = trainingAdvice;
    this.resourceAdvice               = (resourceAdvice==null)?"Default advice":resourceAdvice;
    this.courseEmail                  = courseEmail;
    this.organizationEmail            = organizationEmail;
  }

  public boolean isMembershipInstructorDeepLinkPermitted()
  {
    return membershipInstructorDeepLinkPermitted;
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

  public String getResourceRole()
  {
    return resourceRole;
  }

  public String getResourceSearchSpecification()
  {
    return resourceSearchSpecification;
  }

  public String getResourceSearchFilter()
  {
    return resourceSearchFilter;
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

  public String getResourceAdvice()
  {
    return resourceAdvice;
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
            "Instructor",
            "RESOURCE",
            "^RESOURCE$",
            "mailrelayhere.com",
            "admin@mailrelayhere.com",
            "Course advice HTML here.",
            "Org advice HTML here.",
            "Training advice HTML here.",
            "Resource advice HTML here.",
            "Email text here.",
            "Email text here."
    );
  }
}
