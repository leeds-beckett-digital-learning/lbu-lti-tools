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

package uk.ac.leedsbeckett.ltidemo.state;

import uk.ac.leedsbeckett.ltidemo.tool.Resource;


/**
 * State information which is needed by the CourseResourceServlet
 * 
 * course.
 * 
 * @author jon
 */
public class CourseLaunchState extends LaunchState
{
  private String resourceId;
  private String courseId;
  private String courseTitle;
  
  /**
   * The 'resource' that represents the object that this launch
   * session is going to work on. This is all about the specifics of
   * what this demo tool does.
   */
  private Resource resource;
  
  /**
   * Rights of user with respect to the resource being accessed.
   */
  private boolean allowedToClearResource=false;

  /**
   * Simple getter.
   * 
   * @return The ID of the resource.
   */
  public String getResourceId()
  {
    return resourceId;
  }

  /**
   * Simple setter.
   * 
   * @param resourceId The ID of the resource.
   */
  public void setResourceId( String resourceId )
  {
    this.resourceId = resourceId;
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
   * Simple getter.
   * 
   * @return This tool's resource object.
   */
  public Resource getResource()
  {
    return resource;
  }

  /**
   * Simple setter.
   * 
   * @param resource This tool's resource object.
   */
  public void setResource( Resource resource )
  {
    this.resource = resource;
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
