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

/**
 *
 * @author maber01
 */
public class PeerGroupChangeDatum implements Serializable
{
  final String groupId;
  final String fieldId;
  final String memberId;
  final String value;

  public PeerGroupChangeDatum( 
          @JsonProperty("groupId")  String groupId, 
          @JsonProperty("fieldId")  String fieldId, 
          @JsonProperty("memberId") String memberId, 
          @JsonProperty("value")    String value )
  {
    this.groupId = groupId;
    this.fieldId = fieldId;
    this.memberId = memberId;
    this.value = value;
  }

  public String getGroupId()
  {
    return groupId;
  }

  public String getFieldId()
  {
    return fieldId;
  }

  public String getMemberId()
  {
    return memberId;
  }

  public String getValue()
  {
    return value;
  }
  
  
}
