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
package uk.ac.leedsbeckett.ltitools.peergroupassessment.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import uk.ac.leedsbeckett.ltitoolset.ResourceKey;

/**
 *
 * @author maber01
 */
public class PeerGroupDataKey implements Serializable
{
  final ResourceKey resourceKey;
  final String groupId;

  public PeerGroupDataKey( 
          @JsonProperty("resourceKey") ResourceKey resourceKey, 
          @JsonProperty("groupId") String groupId )
  {
    this.resourceKey = resourceKey;
    this.groupId = groupId;
  }

  public ResourceKey getResourceKey()
  {
    return resourceKey;
  }

  public String getGroupId()
  {
    return groupId;
  }

    @Override
    public String toString() {
      return resourceKey.toString() + "    " + groupId;
    }

    @Override
    public boolean equals(Object obj)
    {
      if ( !(obj instanceof PeerGroupDataKey ) )
        return false;
      PeerGroupDataKey other = (PeerGroupDataKey)obj;
      return this.toString().equals( other.toString() );
    }

    @Override
    public int hashCode()
    {
      return resourceKey.hashCode() | groupId.hashCode();
    }
  
}
