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

/**
 *
 * @author maber01
 */
public class ParticipantData implements Serializable
{
    final String participantId;
    boolean endorsed;
    HashMap<String,ParticipantDatum> participantData;
    
    public ParticipantData( @JsonProperty("participantId") String participantId )
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
