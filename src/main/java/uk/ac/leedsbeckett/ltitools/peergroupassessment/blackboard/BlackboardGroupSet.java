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
package uk.ac.leedsbeckett.ltitools.peergroupassessment.blackboard;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author maber01
 */
public class BlackboardGroupSet extends BlackboardGroup implements Serializable
{
  private final ArrayList<BlackboardGroup> groups;
  
  public BlackboardGroupSet( 
          @JsonProperty("id")     String id, 
          @JsonProperty("uuid")   String uuid, 
          @JsonProperty("name")   String name,
          @JsonProperty("groups") ArrayList<BlackboardGroup> groups )
  {
    super( id, uuid, name );
    this.groups = groups;
  }

  public ArrayList<BlackboardGroup> getGroups()
  {
    return groups;
  }
  
  public void addGroup( BlackboardGroup group )
  {
    groups.add( group );
  }
}
