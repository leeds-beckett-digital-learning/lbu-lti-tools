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

package uk.ac.leedsbeckett.ltitools.hugeupload;

import java.io.Serializable;
import uk.ac.leedsbeckett.ltitools.hugeupload.data.HuCourseKey;
import uk.ac.leedsbeckett.ltitools.hugeupload.data.HuResourceKey;
import uk.ac.leedsbeckett.ltitoolset.ToolLaunchState;


/**
 * State information which is needed by the PGA tool's JSP and endpoints.
.* 
 * @author jon
 */
public class HuToolLaunchState extends ToolLaunchState implements Serializable
{
  private boolean allowedToManage=false;
  private boolean allowedToParticipate=false;
  
  private HuCourseKey huCourseKey = null;
  private HuResourceKey huResourceKey = null;

  private String returnURL = null;
  
  /**
   * Is the user connected to this state object allowed to manage the resource?
   * 
   * @return Is allowed?
   */
  public boolean isAllowedToManage()
  {
    return allowedToManage;
  }

  /**
   * Simple setter.
   * 
   * @param allowedToManage Is the user allowed to manage the resource.
   */
  public void setAllowedToManage( boolean allowedToManage )
  {
    this.allowedToManage = allowedToManage;
  }

  /**
   * Find out if user is allowed to fill in data.
   * 
   * @return 
   */
  public boolean isAllowedToParticipate()
  {
    return allowedToParticipate;
  }

  /**
   * Set permission to fill in data.
   * 
   * @param allowedToParticipate 
   */
  public void setAllowedToParticipate( boolean allowedToParticipate )
  {
    this.allowedToParticipate = allowedToParticipate;
  }
  
  public boolean isAllowedToAccess()
  {
    return allowedToParticipate || allowedToManage;
  }

  public HuCourseKey getHuCourseKey()
  {
    return huCourseKey;
  }

  public void setHuCourseKey( HuCourseKey huCourseKey )
  {
    this.huCourseKey = huCourseKey;
  }

  public void setHuCourseKey()
  {
    if ( getPlatformId() != null && getCourseId() != null )
      huCourseKey = new HuCourseKey( getPlatformId(), getCourseId());
    else
      huCourseKey = null;
  }
  
  public HuResourceKey getHuResourceKey()
  {
    return huResourceKey;
  }

  public void setHuResourceKey( HuResourceKey huResourceKey )
  {
    this.huResourceKey = huResourceKey;
  }

  public void setHuResourceKey()
  {
    if ( getPlatformId() != null && getCourseId() != null && getToolResourceId() != null )
      huResourceKey = new HuResourceKey( getPlatformId(), getCourseId(), getToolResourceId());
    else
      huResourceKey = null;
  }

  public String getReturnURL()
  {
    return returnURL;
  }

  public void setReturnURL( String returnURL )
  {
    this.returnURL = returnURL;
  }
}
