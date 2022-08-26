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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonRawValue;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * An object that represents the resource which the user is accessing after the
 * LTI launch. A simple stack of log entries.
 * 
 * @author jon
 */

@JsonIgnoreProperties({ "groups" })
public class PeerGroupResource implements Serializable
{
  String title;
  String description;
  public final Map<String,Group> groupsById = new HashMap<>();
  final Group groupOfUnattached = new Group();
  public final Map<String,String> groupIdsByMember = new HashMap<>();
  
  ArrayList<Group> groupsinorder = null;
  
  public PeerGroupResource()
  {
    title = "Initial Title";
    description = "Initial Description";
  }

  /**
   * Called by the resource store when an entirely new resource is needed.
   */
  public void initialize()
  {
    for ( int i=1; i<=5; i++ )
      addGroup( "id" + i, "Group " + i );
    addMember(  null, "001", "Fred Bloggs" );
    addMember( "id1", "002", "Joe Brown"   );
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

  public void registerIfFirstAccess( String id, String name )
  {
    
  }
  
  public List<Group> getGroups()
  {
    synchronized ( groupIdsByMember )
    {
      if ( groupsinorder == null )
      {
        groupsinorder = new ArrayList<>();
        groupsinorder.addAll(groupsById.values() );
        groupsinorder.sort(( Group o1, Group o2 ) -> o1.getTitle().compareTo( o2.getTitle() ));
      }
      return groupsinorder;
    }
  }
  
  public Group getGroupOfUnattached()
  {
    return groupOfUnattached;
  }

  public Group getGroupById( String id )
  {
    return groupsById.get( id );
  }
  
  public Group getGroupByMemberId( String id )
  {
    String gid = groupIdsByMember.get( id );
    if ( gid == null ) return null;
    return getGroupById( gid );
  }
  
  public void addGroup( String gid, String gtitle )
  {
    groupsinorder = null;
    Group g = new Group();
    g.setId( gid );
    g.setTitle( gtitle );
    groupsById.put( gid, g );
  }
  
  public void addMember( String gid, String uid, String name )
  {
    Group g;
    if ( gid == null )
      g = groupOfUnattached;
    else
      g = getGroupById( gid );
    g.addMember( uid, name );
    if ( gid != null )
      groupIdsByMember.put( uid, gid );
  }
  
  @Override
  public String toString()
  {
    return getTitle();
  }
  
  @JsonIgnoreProperties({ "members" })
  public static class Group
  {
    String id;
    String title;
    
    public final Map<String,Member> membersbyid = new HashMap<>();

    transient ArrayList<Member> membersinorder = null;
            
    public String getId()
    {
      return id;
    }

    public void setId( String id )
    {
      this.id = id;
    }

    public String getTitle()
    {
      return title;
    }

    public void setTitle( String title )
    {
      this.title = title;
    }
    
    public void addMember( String id, String name )
    {
      synchronized ( membersbyid )
      {
        membersinorder = null;
        Member m = new Member();
        m.setLtiId( id );
        m.setName( name );
        membersbyid.put( id, m );
      }
    }
    
    public boolean isMember( String id )
    {
      synchronized ( membersbyid )
      {
        return membersbyid.containsKey( id );
      }
    }
    
    public List<Member> getMembers()
    {
      synchronized ( membersbyid )
      {
        if ( membersinorder == null )
        {
          membersinorder = new ArrayList<>( membersbyid.values() );
          membersinorder.sort(( Member o1, Member o2 ) -> o1.getName().compareTo( o2.getName() ));
        }
        return membersinorder;
      }
    }
  }
  
  public static class Member
  {
    String ltiId;
    String name;

    public String getLtiId()
    {
      return ltiId;
    }

    public void setLtiId( String ltiId )
    {
      this.ltiId = ltiId;
    }

    public String getName()
    {
      return name;
    }

    public void setName( String name )
    {
      this.name = name;
    }
  }
}
