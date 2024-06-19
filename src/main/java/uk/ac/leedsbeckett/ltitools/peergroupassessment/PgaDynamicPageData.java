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
package uk.ac.leedsbeckett.ltitools.peergroupassessment;

import uk.ac.leedsbeckett.ltitoolset.page.DynamicPageData;

/**
 *
 * @author maber01
 */
public class PgaDynamicPageData extends DynamicPageData
{
  private boolean allowedToManage;
  private boolean allowedToParticipate;
  private boolean allowedToConfigure;

  public boolean isAllowedToManage()
  {
    return allowedToManage;
  }

  public void setAllowedToManage( boolean allowedToManage )
  {
    this.allowedToManage = allowedToManage;
  }

  public boolean isAllowedToParticipate()
  {
    return allowedToParticipate;
  }

  public void setAllowedToParticipate( boolean allowedToParticipate )
  {
    this.allowedToParticipate = allowedToParticipate;
  }

  public boolean isAllowedToConfigure()
  {
    return allowedToConfigure;
  }

  public void setAllowedToConfigure( boolean allowedToConfigure )
  {
    this.allowedToConfigure = allowedToConfigure;
  }
  
  
}
