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

package uk.ac.leedsbeckett.ltitools.tool.peergroupassessment;

import java.io.Serializable;
import uk.ac.leedsbeckett.ltitools.state.AppSessionState;
import uk.ac.leedsbeckett.ltitools.tool.ResourceKey;


/**
 * State information which is needed by the CourseResourceServlet
 * 
 * course.
 * 
 * @author jon
 */
public class PeerGroupAssessmentState implements Serializable, AppSessionState
{
  /**
   * Many users (many states) may reference the same resource. It is 
   * important that it doesn't hold a reference to the resource. So, it 
   * holds a unique key to the resource. The resources themselves are
   * put in a different cache.
   */
  private ResourceKey resourceKey;
  
  private String courseId;
  private String courseTitle;
    
  /**
   * Rights of user with respect to the resource being accessed.
   */
  private boolean allowedToClearResource=false;

  /**
   * Get the key of the resource that this state relates to.
   * 
   * @return 
   */
  public ResourceKey getResourceKey()
  {
    return resourceKey;
  }

  /**
   * This state object relates to a resource on the server. This method
   * sets the key for that resource.
   * 
   * @param resourceKey 
   */
  public void setResourceKey(ResourceKey resourceKey)
  {
    this.resourceKey = resourceKey;
  }

  /**
   * Simple getter.
   * 
   * @return The LTI course ID.
   */
  public String getCourseId()
  {
    return courseId;
  }

  /**
   * Simple setter.
   * 
   * @param courseId The LTI course ID.
   */
  public void setCourseId( String courseId )
  {
    this.courseId = courseId;
  }
  
  /**
   * Simple getter.
   * 
   * @return The LTI title of the course.
   */
  public String getCourseTitle()
  {
    return courseTitle;
  }

  /**
   * Simple setter.
   * 
   * @param courseTitle The LTI title of the course.
   */
  public void setCourseTitle( String courseTitle )
  {
    this.courseTitle = courseTitle;
  }

  /**
   * Is the user connected to this state object allowed to clear the resource?
   * 
   * @return Is allowed?
   */
  public boolean isAllowedToClearResource()
  {
    return allowedToClearResource;
  }

  /**
   * Simple setter.
   * 
   * @param allowedToClearResource Is the user allowed to clear the resource.
   */
  public void setAllowedToClearResource( boolean allowedToClearResource )
  {
    this.allowedToClearResource = allowedToClearResource;
  }
}
