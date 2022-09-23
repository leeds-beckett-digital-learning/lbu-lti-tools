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

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.HashMap;
import uk.ac.leedsbeckett.ltitools.tool.store.Entry;

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
  
  
  
  public class ParticipantData implements Serializable
  {
    final String participantId;
    boolean endorsed;
    HashMap<String,ParticipantDatum> participantData;
    
    public ParticipantData( String participantId )
    {
      this.participantId = participantId;
    }
    public String getParticipantId()
    {
      return participantId;
    }
    public boolean isEndorsed()
    {
      return endorsed;
    }
    public void setEndorsed( boolean endorsed )
    {
      this.endorsed = endorsed;
    }
    public HashMap<String, ParticipantDatum> getParticipantData()
    {
      return participantData;
    }
    public void setParticipantData( HashMap<String, ParticipantDatum> participantData )
    {
      this.participantData = participantData;
    }
  }
  
  public class ParticipantDatum implements Serializable
  {
    int value;
    public int getValue()
    {
      return value;
    }
    public void setValue( int value )
    {
      this.value = value;
    }
  }
  
  public enum Status
  {
    OPEN,
    CLOSED,
    ENDORSED,
    LOCKED
  }
}
