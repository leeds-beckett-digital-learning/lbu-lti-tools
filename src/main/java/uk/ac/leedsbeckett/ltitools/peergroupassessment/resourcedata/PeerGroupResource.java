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

package uk.ac.leedsbeckett.ltitools.peergroupassessment.resourcedata;

import uk.ac.leedsbeckett.ltitools.peergroupassessment.messagedata.PgaAddMembership;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import uk.ac.leedsbeckett.ltitoolset.store.Entry;
import uk.ac.leedsbeckett.ltitoolset.ResourceKey;


/**
 * An object that represents the resource which the user is accessing after the
 * LTI launch.
 * 
 * @author jon
 */
@JsonIgnoreProperties({ "groups" })
public class PeerGroupResource implements Serializable, Entry<ResourceKey>
{
  ResourceKey key;
  
  PgaProperties properties = new PgaProperties( "Initial Title", "Initial Decsription", Stage.SETUP );
  public final Map<String,Group> groupsById = new HashMap<>();
  final Group groupOfUnattached = new Group();
  public final Map<String,String> groupIdsByMember = new HashMap<>();
  
  ArrayList<Group> groupsinorder = null;
  
  public PeerGroupResource( @JsonProperty("key") ResourceKey key )
  {
    this.key = key;
  }

  @Override
  public ResourceKey getKey()
  {
    return key;
  }

  @Override
  public void setKey( ResourceKey key )
  {
    if ( this.key != null )
      throw new IllegalArgumentException( "Not allowed to change resource key." );
    this.key = key;
  }
  
  
  /**
   * Called by the resource store when an entirely new resource is needed.
   */
  @Override
  public void initialize()
  {
//    for ( int i=1; i<=5; i++ )
//      addGroup( "Group " + i );
  }

  public PgaProperties getProperties()
  {
    return properties;
  }

  public void setProperties( PgaProperties properties )
  {
    this.properties = properties;
  }
  
  @JsonIgnore    
  public String getTitle() {
    return properties.getTitle();
  }

  @JsonIgnore    
  public String getDescription() {
    return properties.getDescription();
  }

  @JsonIgnore    
  public Stage getStage()
  {
    return properties.getStage();
  }

  @JsonIgnore    
  public boolean isSetupStage()
  {
    return properties.getStage() == Stage.SETUP;
  }
  
  @JsonIgnore    
  public boolean isJoinStage()
  {
    return properties.getStage() == Stage.JOIN;
  }
  
  @JsonIgnore    
  public boolean isDataEntryStage()
  {
    return properties.getStage() == Stage.DATAENTRY;
  }
  
  @JsonIgnore    
  public boolean isResultsStage()
  {
    return properties.getStage() == Stage.RESULTS;
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
    if ( id == null ) return groupOfUnattached;
    String gid = groupIdsByMember.get( id );
    return getGroupById( gid );
  }
  
  public Group addGroup( String gtitle )
  {
    String gid = UUID.randomUUID().toString();
    Group g = new Group();
    g.setId( gid );
    g.setTitle( gtitle );
    groupsById.put( gid, g );
    return g;
  }
  
  public void addMember( String gid, String uid, String name )
  {
    Group oldgroup = getGroupByMemberId( uid );
    if ( oldgroup != null )
      oldgroup.removeMember( uid );
    
    Group g;    
    if ( gid == null )
      g = groupOfUnattached;
    else
      g = getGroupById( gid );
    g.addMember( uid, name );
    if ( gid != null )
      groupIdsByMember.put( uid, gid );
  }

  public void addMemberships( PgaAddMembership pgcm )
  {
    for ( Member m : pgcm.getPids() )
      addMember( pgcm.getId(), m.getLtiId(), m.getName() );
  }  

  @Override
  public String toString()
  {
    return getTitle();
  }

  @JsonIgnoreProperties({ "members" })
  public static class Group implements Serializable
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
    
    public void removeMember( String id )
    {
      synchronized ( membersbyid )
      {
        membersinorder = null;
        membersbyid.remove( id );
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
  
  public static class Member implements Serializable
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
