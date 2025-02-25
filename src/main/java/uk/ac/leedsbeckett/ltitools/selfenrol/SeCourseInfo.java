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

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/**
 * Forms part of message to web browser.
 * 
 * @author maber01
 */
public class SeCourseInfo  implements Serializable
{
  private final String externalId;
  private final String name;
  private final String description;
  private final String parentId;
          
  public SeCourseInfo( String externalId, String name, String description, String parentId )
  {
    this.externalId  = externalId;
    this.name        = name;
    this.description = description;
    this.parentId    = parentId;
  }

  public String getExternalId()
  {
    return externalId;
  }

  public String getName()
  {
    return name;
  }

  public String getDescription()
  {
    return description;
  }

  public String getParentId()
  {
    return parentId;
  }
}
