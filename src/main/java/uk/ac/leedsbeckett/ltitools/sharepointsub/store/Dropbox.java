/*
 * Copyright 2025 maber01.
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
package uk.ac.leedsbeckett.ltitools.sharepointsub.store;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;

/**
 *
 * @author maber01
 */
public class Dropbox
{
  String name;
  Deadline defaultDeadline;
  HashMap<String,Deadline> studentDeadlineMap;

  public Dropbox( 
          @JsonProperty("name")               String name, 
          @JsonProperty("defaultDeadline")    Deadline defaultDeadline, 
          @JsonProperty("studentDeadlineMap") HashMap<String, Deadline> studentDeadlineMap
        )
  {
    this.name = name;
    this.defaultDeadline = defaultDeadline;
    this.studentDeadlineMap = studentDeadlineMap;
    if ( this.studentDeadlineMap == null )
      this.studentDeadlineMap = new HashMap<>();
  }

  public String getName()
  {
    return name;
  }

  public Deadline getDefaultDeadline()
  {
    return defaultDeadline;
  }

  public HashMap<String, Deadline> getStudentDeadlineMap()
  {
    return studentDeadlineMap;
  }
}
