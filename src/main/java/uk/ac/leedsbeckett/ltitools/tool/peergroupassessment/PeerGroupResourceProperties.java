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
package uk.ac.leedsbeckett.ltitools.tool.peergroupassessment;

import java.io.Serializable;

/**
 *
 * @author maber01
 */
public class PeerGroupResourceProperties implements Serializable
{
  String title;
  String description;
  PeerGroupResource.Stage stage;  

  public PeerGroupResourceProperties()
  {
    title = "Initial Title";
    description = "Initial Description";
    stage = PeerGroupResource.Stage.SETUP;
  }
  
  public String getTitle()
  {
    return title;
  }

  public void setTitle( String title )
  {
    this.title = title;
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription( String description )
  {
    this.description = description;
  }

  public PeerGroupResource.Stage getStage()
  {
    return stage;
  }

  public void setStage( PeerGroupResource.Stage stage )
  {
    this.stage = stage;
  }
  
}
