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

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import uk.ac.leedsbeckett.ltitools.peergroupassessment.messagedata.PgaAddMembership;
import uk.ac.leedsbeckett.ltitools.peergroupassessment.resourcedata.PeerGroupResource;

/**
 *
 * @author maber01
 */
public class BlackboardGroupSetImportPlan
{
  final ArrayList<Group> groups = new ArrayList<>();
  Group currentGroup = null;
  
  public ArrayList<Group> getGroups()
  {
    return groups;
  }
  
  public void addGroup( String externalId, String name )
  {
    currentGroup = new Group( externalId, name );
    groups.add( currentGroup );
  }
  
  public void addUser( String id, String name )
  {
    currentGroup.addUser( id, name );
  }
  
  public class Group
  {
    final String externalId;
    final String name;
    final ArrayList<User> users = new ArrayList<>();

    Group( String externalId, String name )
    {
      this.externalId = externalId;
      this.name = name;
    }

    public String getExternalId()
    {
      return externalId;
    }

    public String getName()
    {
      return name;
    }

    public ArrayList<User> getUsers()
    {
      return users;
    }
    
    public void addUser( String id, String name )
    {
      users.add(  new User( id, name ) );
    }
    
    @JsonIgnore
    public PgaAddMembership getPgaAddMembership( String id )
    {
      ArrayList<PeerGroupResource.Member> pids = new ArrayList<>();
      for ( User u : getUsers() )
        pids.add(  new PeerGroupResource.Member( u.id, u.name ) );
      return new PgaAddMembership( id, pids );
    }
  }
  
  public class User
  {
    final String id;
    final String name;

    User( String id, String name )
    {
      this.id = id;
      this.name = name;
    }

    public String getId()
    {
      return id;
    }

    public String getName()
    {
      return name;
    }
  }
}
