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

import uk.ac.leedsbeckett.ltitoolset.page.DynamicPageData;

/**
 *
 * @author maber01
 */
public class SeDynamicPageData extends DynamicPageData
{
  boolean allowedToEnrol;
  boolean allowedToConfigure;
  String courseSearchValidation;
  String orgSearchValidation;

  public boolean isAllowedToEnrol()
  {
    return allowedToEnrol;
  }

  public void setAllowedToEnrol( boolean allowedToEnrol )
  {
    this.allowedToEnrol = allowedToEnrol;
  }

  public boolean isAllowedToConfigure()
  {
    return allowedToConfigure;
  }

  public void setAllowedToConfigure( boolean allowedToConfigure )
  {
    this.allowedToConfigure = allowedToConfigure;
  }
  
  public String getCourseSearchValidation()
  {
    return courseSearchValidation;
  }

  public void setCourseSearchValidation( String courseSearchValidation )
  {
    this.courseSearchValidation = courseSearchValidation;
  }

  public String getOrgSearchValidation()
  {
    return orgSearchValidation;
  }

  public void setOrgSearchValidation( String orgSearchValidation )
  {
    this.orgSearchValidation = orgSearchValidation;
  }
}
