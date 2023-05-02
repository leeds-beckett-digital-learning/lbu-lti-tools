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
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
  
  PgaProperties properties = new PgaProperties( "Initial Title", "Initial Description", Stage.SETUP );
  public final Map<String,Group> groupsById = new HashMap<>();
  final Group groupOfUnattached = new Group();
  public final Map<String,String> groupIdsByMember = new HashMap<>();
  public ArrayList<String> groupIdsInOrder = new ArrayList<>();
  public final Map<String,String> groupIdsByExternalId = new HashMap<>();
  
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
  
  @JsonIgnore    
  public String getFormId()
  {
    return "default";
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
    if ( id == null ) return null;
    if ( !groupIdsByMember.containsKey( id ) ) return null;
    String gid = groupIdsByMember.get( id );
    if ( gid == null ) return groupOfUnattached;
    return getGroupById( gid );
  }
  
  public Group getGroupByExternalId( String id )
  {
    if ( id == null ) return null;
    if ( !groupIdsByExternalId.containsKey( id ) ) return null;
    String gid = groupIdsByExternalId.get( id );
    if ( gid == null ) return null;
    return getGroupById( gid );
  }
  
  public boolean isMember( String id )
  {
    if ( id == null ) return false;
    return groupIdsByMember.containsKey( id );
  }
  
  public int compareGroupsUsingIds( String o1, String o2 )
  {
    Group g1 = groupsById.get( o1 );
    Group g2 = groupsById.get( o2 );
    int r = g1.title.compareTo( g2.title );
    if ( r != 0 ) return r;
    return g1.id.compareTo( g2.id );
  }
  
  public Group addGroup( String gtitle )
  {
    return addGroup( gtitle, null );
  }
  
  public Group addGroup( String gtitle, String extId )
  {
    String gid = UUID.randomUUID().toString();
    Group g = new Group();
    g.setId( gid );
    g.setTitle( gtitle );
    g.setExternalId( extId );
    groupsById.put( gid, g );
    groupIdsInOrder.add( g.id );
    if ( extId != null )
      groupIdsByExternalId.put( extId, gid );
    sortGroups();
    return g;
  }

  public void deleteGroup( String gid )
  {
    if ( gid == null || !groupsById.containsKey( gid ) )
      return;
    
    Group g = groupsById.get( gid );
    // Move all members to unattached
    List<Member> members = g.getMembers();
    for ( Member m : members )
      addMember( null, m.getLtiId(), m.getName() );
    if ( g.getExternalId() != null )
      groupIdsByExternalId.remove( g.externalId );
    groupsById.remove( gid );
    groupIdsInOrder.remove( gid );
  }
  
  public void sortGroups()
  {
    groupIdsInOrder.sort( ( String o1, String o2 ) -> compareGroupsUsingIds( o1, o2 ) );    
  }
  
  public Collection<String> addMember( String gid, String uid, String name )
  {
    LinkedList<String> affectedGids = new LinkedList<>();
    Group oldgroup = getGroupByMemberId( uid );
    if ( oldgroup != null )
    {
      affectedGids.add( oldgroup.getId() );
      oldgroup.removeMember( uid );
    }
    
    Group g;    
    if ( gid == null )
      g = groupOfUnattached;
    else
      g = getGroupById( gid );
    g.addMember( uid, name );
    groupIdsByMember.put( uid, gid );
    affectedGids.add( g.getId() );
    return affectedGids;
  }

  /**
   * Adds a number of member IDs to a group and removes those members from
   * any other groups. Returns a collection of groups that changed.
   * 
   * @param pgcm
   * @return 
   */
  public Collection<String> addMemberships( PgaAddMembership pgcm )
  {
    LinkedList<String> affectedGids = new LinkedList<>();
    for ( Member m : pgcm.getPids() )
      affectedGids.addAll( addMember( pgcm.getId(), m.getLtiId(), m.getName() ) );
    return affectedGids;
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
    String externalId;
    
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

    public String getExternalId()
    {
      return externalId;
    }

    public void setExternalId( String externalId )
    {
      this.externalId = externalId;
    }
    
    public void addMember( String id, String name )
    {
      synchronized ( membersbyid )
      {
        membersinorder = null;
        Member m = new Member( id, name );
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
    final String ltiId;
    final String name;

    public Member( @JsonProperty("ltiId")  String ltiId, 
                   @JsonProperty("name")   String name )
    {
      this.ltiId = ltiId;
      this.name = name;
    }

    public String getLtiId()
    {
      return ltiId;
    }

    public String getName()
    {
      return name;
    }
  }  
}
