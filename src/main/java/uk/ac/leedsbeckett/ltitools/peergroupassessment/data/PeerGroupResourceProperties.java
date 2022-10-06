/*
 * Copyright 2022 maber01.
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
package uk.ac.leedsbeckett.ltitools.peergroupassessment.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/**
 *
 * @author maber01
 */
public class PeerGroupResourceProperties implements Serializable
{
  final String title;
  final String description;
  final Stage stage;  

  public PeerGroupResourceProperties( 
          @JsonProperty("title")       String title, 
          @JsonProperty("description") String description, 
          @JsonProperty("stage")       Stage stage )
  {
    this.title = title;
    this.description = description;
    this.stage = stage;
  }

  
  public String getTitle()
  {
    return title;
  }

  public String getDescription()
  {
    return description;
  }

  public Stage getStage()
  {
    return stage;
  }  
}
