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
import java.util.Date;
import java.util.HashMap;
import uk.ac.leedsbeckett.ltitools.peergroupassessment.formdata.PeerGroupForm;
import uk.ac.leedsbeckett.ltitoolset.store.Entry;

/**
 *
 * @author maber01
 */
public class PeerGroupData implements Serializable, Entry<PeerGroupDataKey>
{
  PeerGroupDataKey key;
  Status status;
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

  public Status getStatus()
  {
    return status;
  }

  public void setStatus( Status status )
  {
    this.status = status;
  }

  public HashMap<String, ParticipantData> getParticipantData()
  {
    return participantData;
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
  
  public void setEndorsementDate( String memberId, Date value, boolean manager )
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
    }
  }
  
  @Override
  public void setKey( PeerGroupDataKey key )
  {
    this.key = key;
  }

  @Override
  public void initialize()
  {
    status = Status.OPEN;
    participantData = new HashMap<>();
  }
  
  
  public enum Status
  {
    OPEN,
    CLOSED,
    ENDORSED,
    LOCKED
  }
}
