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

package uk.ac.leedsbeckett.ltitools.hugeupload.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * Represents the configuration of the Huge Upload tool within a specific 
 * course on a specific platform. Loads from a JSON file.
 * 
 * @author jon
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CourseConfiguration
{
  public static CourseConfiguration getDefaultCourseConfiguration()
  {
    return new CourseConfiguration( false, false, new UploadAllowance[0], new UploadAllowance[0] );
  }
  
  private final boolean studentUploadAllowed;
  private final boolean instructorUploadAllowed;
  private final UploadAllowance[] studentUploadAllowances;
  private final UploadAllowance[] instructorUploadAllowances;

  @JsonCreator
  public CourseConfiguration( 
          @JsonProperty( value = "studentUploadAllowed",       required = true ) boolean studentUploadAllowed, 
          @JsonProperty( value = "instructorUploadAllowed",    required = true ) boolean instructorUploadAllowed, 
          @JsonProperty( value = "studentUploadAllowances",    required = true ) UploadAllowance[] studentUploadAllowances, 
          @JsonProperty( value = "instructorUploadAllowances", required = true ) UploadAllowance[] instructorUploadAllowances )
  {
    this.studentUploadAllowed = studentUploadAllowed;
    this.instructorUploadAllowed = instructorUploadAllowed;
    this.studentUploadAllowances = studentUploadAllowances;
    this.instructorUploadAllowances = instructorUploadAllowances;
  }

  public boolean isStudentUploadAllowed()
  {
    return studentUploadAllowed;
  }

  public boolean isInstructorUploadAllowed()
  {
    return instructorUploadAllowed;
  }

  public UploadAllowance[] getStudentUploadAllowances()
  {
    return studentUploadAllowances;
  }

  public UploadAllowance[] getInstructorUploadAllowances()
  {
    return instructorUploadAllowances;
  }
}
