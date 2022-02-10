/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uk.ac.leedsbeckett.ltidemo;

import uk.ac.leedsbeckett.ltidemo.tool.Resource;
import uk.ac.leedsbeckett.lti.state.LaunchState;

/**
 * State related to the LTI launch of a resource which relates to a specific
 * course.
 * 
 * @author jon
 */
public class CourseLaunchState extends LaunchState
{
  private String resourceId;
  private String courseId;
  private String courseTitle;
  private Resource resource;
  private boolean allowedToClearResource=false;

  public String getResourceId()
  {
    return resourceId;
  }

  public void setResourceId( String resourceId )
  {
    this.resourceId = resourceId;
  }

  public String getCourseId()
  {
    return courseId;
  }

  public void setCourseId( String courseId )
  {
    this.courseId = courseId;
  }
  
  public String getCourseTitle()
  {
    return courseTitle;
  }

  public void setCourseTitle( String courseTitle )
  {
    this.courseTitle = courseTitle;
  }

  public Resource getResource()
  {
    return resource;
  }

  public void setResource( Resource resource )
  {
    this.resource = resource;
  }

  public boolean isAllowedToClearResource()
  {
    return allowedToClearResource;
  }

  public void setAllowedToClearResource( boolean allowedToClearResource )
  {
    this.allowedToClearResource = allowedToClearResource;
  }
}
