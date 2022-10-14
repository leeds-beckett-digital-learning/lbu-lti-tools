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
package uk.ac.leedsbeckett.ltitools.peergroupassessment.messagedata;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.ArrayList;
import uk.ac.leedsbeckett.ltitools.peergroupassessment.resourcedata.PeerGroupResource.Member;

/**
 *
 * @author maber01
 */
public class PgaAddMembership implements Serializable
{
  final String id;
  final ArrayList<Member> pids;

  public PgaAddMembership( 
          @JsonProperty("id") String id, 
          @JsonProperty("pids") ArrayList<Member> pids )
  {
    this.id = id;
    this.pids = pids;
  }

  public String getId()
  {
    return id;
  }

  public ArrayList<Member> getPids()
  {
    return pids;
  }
  
  public boolean isOnlySelf( String id )
  {
    return 
            pids != null && 
            pids.size() == 1 && 
            pids.get( 0 ).getLtiId().equals( id );
  }
}

