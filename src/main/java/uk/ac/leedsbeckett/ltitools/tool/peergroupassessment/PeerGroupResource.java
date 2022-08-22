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


/**
 * An object that represents the resource which the user is accessing after the
 * LTI launch. A simple stack of log entries.
 * 
 * @author jon
 */
public class PeerGroupResource implements Serializable
{
  String title;
  String description;
  
  public PeerGroupResource()
  {
    title = "Initial Title";
    description = "Initial Description";
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  } 

  @Override
  public String toString()
  {
    return getTitle();
  }
  
  
}
