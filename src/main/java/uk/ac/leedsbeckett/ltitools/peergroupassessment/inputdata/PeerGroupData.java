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
package uk.ac.leedsbeckett.ltitools.peergroupassessment.inputdata;

import uk.ac.leedsbeckett.ltitools.peergroupassessment.messagedata.PgaChangeDatum;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import uk.ac.leedsbeckett.ltitools.peergroupassessment.formdata.PeerGroupForm;
import uk.ac.leedsbeckett.ltitools.peergroupassessment.formdata.PeerGroupForm.Field;
import static uk.ac.leedsbeckett.ltitools.peergroupassessment.inputdata.PgaEndorsementStatus.FULLYENDORSED;
import static uk.ac.leedsbeckett.ltitools.peergroupassessment.inputdata.PgaEndorsementStatus.NOTENDORSED;
import static uk.ac.leedsbeckett.ltitools.peergroupassessment.inputdata.PgaEndorsementStatus.PARTLYENDORSED;
import uk.ac.leedsbeckett.ltitools.peergroupassessment.resourcedata.PeerGroupResource;
import uk.ac.leedsbeckett.ltitools.peergroupassessment.resourcedata.PeerGroupResource.Group;
import uk.ac.leedsbeckett.ltitools.peergroupassessment.resourcedata.PeerGroupResource.Member;
import uk.ac.leedsbeckett.ltitoolset.store.Entry;

/**
 *
 * @author maber01
 */
public class PeerGroupData implements Serializable, Entry<PeerGroupDataKey>
{
  PeerGroupDataKey key;
  PgaEndorsementStatus status;
  HashMap<String,ParticipantData> participantData;

  public PeerGroupData( @JsonProperty("key") PeerGroupDataKey key )
  {
    this.key = key;
  }

  @Override
  public PeerGroupDataKey getKey()
  {
    return key;
  }

  public PgaEndorsementStatus getStatus()
  {
    return status;
  }

  public void setStatus( PgaEndorsementStatus status )
  {
    this.status = status;
  }

  public HashMap<String, ParticipantData> getParticipantData()
  {
    return participantData;
  }

  public void removeUnwantedParticipantData( PeerGroupResource.Group group )
  {
    // copy of people IDs in data object
    ArrayList<String> ids = new ArrayList<>( this.participantData.keySet() );
    // Check each one
    for ( String id : ids )
      // If not in the group according to resource
      if ( id != null && !group.isMember( id ) )
        // Remove the data
        this.participantData.remove( id );    
  }

  public void setParticipantData( HashMap<String, ParticipantData> participantData )
  {
    this.participantData = participantData;
  }

  public void setParticipantDatum( PgaChangeDatum change, PeerGroupForm.Field field )
  {
    synchronized ( participantData )
    {
      boolean valid = true;
      if ( field.getType() == PeerGroupForm.FieldType.INTEGER )
      {
        try
        {
          valid = true;
          int n = Integer.parseInt( change.getValue() );
          if ( n < field.getMinimum() || n > field.getMaximum() ) valid = false;
        }
        catch ( NumberFormatException nfe ) { valid = false; }
      }
    
      ParticipantData pd = participantData.get( change.getMemberId() );
      if ( pd == null )
      {
        pd = new ParticipantData( change.getMemberId() );
        participantData.put( change.getMemberId(), pd );
        pd.setParticipantData( new HashMap<>() );
      }
      ParticipantDatum datum = pd.participantData.get( change.getFieldId() );
      if ( datum == null )
      {
        datum = new ParticipantDatum();
        pd.participantData.put( change.getFieldId(), datum );
      }
      datum.value = change.getValue();
      datum.valid = valid;
    }
  }
  
  public boolean isEndorsedByParticipant( String memberId )
  {
    synchronized ( participantData )
    {
      ParticipantData pd = participantData.get( memberId );
      if ( pd == null )
        return false;
      return pd.getEndorsedDate() != null;
    }
  }
  
  /**
   * Set the endorsement date for a participant in a group.
   * 
   * @param memberId
   * @param value
   * @param manager
   * @param group This is the group so this routine can check if none/some/all participants have endorsed.
   */
  public void setEndorsementDate( String memberId, Date value, boolean manager, Group group )
  {
    synchronized ( participantData )
    {
      ParticipantData pd = participantData.get( memberId );
      if ( pd == null )
      {
        pd = new ParticipantData( memberId );
        participantData.put( memberId, pd );
        pd.setParticipantData( new HashMap<>() );
      }
      if ( manager )
        pd.setManagerEndorsedDate( value );
      else
        pd.setEndorsedDate( value );
      
      boolean hasblanks = false;
      boolean hasfills  = false;
      for ( ParticipantData each : participantData.values() )
      {
        // This is necessary because data is left in this structure if a
        // participant is removed. (So, the participant can be put back
        // with no loss of data if removed in error.)
        if ( !group.isMember( each.getParticipantId() ) )
          continue;
        if ( each.endorsedDate == null && each.managerEndorsedDate == null )
          hasblanks = true;
        else
          hasfills = true;
      }
      
      if ( hasfills )
      {
        if ( hasblanks )
          setStatus( PARTLYENDORSED );
        else 
          setStatus( FULLYENDORSED );
      }
      else
        setStatus( NOTENDORSED );
    }
  }

  public void clearEndorsements()
  {
    synchronized ( participantData )
    {
      for ( ParticipantData pd : participantData.values() )
      {
        pd.endorsedDate        = null;
        pd.managerEndorsedDate = null;
      }
      setStatus( NOTENDORSED );
    }
  }

  public boolean isEndorsed( Group group, PeerGroupForm form )
  {
    for ( Member member : group.getMembers() )
    {
      ParticipantData pd = this.getParticipantData().get( member.getLtiId() );
      if ( pd == null ) continue;
      if ( pd.getEndorsedDate() != null ) return true;
      if ( pd.getManagerEndorsedDate() != null ) return true;
    }
    return false;
  }
  
  public boolean isFullyEndorsed( Group group, PeerGroupForm form )
  {
    for ( Member member : group.getMembers() )
    {
      ParticipantData pd = this.getParticipantData().get( member.getLtiId() );
      if ( pd == null ) return false;
      if ( pd.getEndorsedDate() == null && 
           pd.getManagerEndorsedDate() == null ) return false;
    }
    return true;
  }
  
  public boolean isAllDataValid( Group group, PeerGroupForm form )
  {
    for ( Member member : group.getMembers() )
    {
      ParticipantData pd = this.getParticipantData().get( member.getLtiId() );
      if ( pd == null ) return false;
      for ( Field field : form.getFields().values() )
      {
        ParticipantDatum datum = pd.getParticipantData().get( field.getId() );
        if ( datum == null ) return false;
        if ( !datum.isValid() ) return false;
      }
    }
    return true;
  }
  
  @Override
  public void setKey( PeerGroupDataKey key )
  {
    this.key = key;
  }

  @Override
  public void initialize()
  {
    status = PgaEndorsementStatus.NOTENDORSED;
    participantData = new HashMap<>();
  }
}
