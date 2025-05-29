/*
 * Copyright 2025 maber01.
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
package uk.ac.leedsbeckett.ltitools.hugeupload.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.ac.leedsbeckett.ltitoolset.util.FourStringKey;

/**
 *
 * @author maber01
 */
public class HuFileMetadataKey extends FourStringKey
{
  public HuFileMetadataKey( 
          @JsonProperty("platformId")     String platformId, 
          @JsonProperty("courseId")       String courseId,
          @JsonProperty("toolResourceId") String toolResourceId,
          @JsonProperty("fileName")       String fileName )
  {
    super( platformId, courseId, toolResourceId, fileName );
  }

  @JsonIgnore
  public HuFileMetadataKey( HuResourceKey rkey, String fileName )
  {
    super( rkey.getPlatformId(), rkey.getCourseId(), rkey.getToolResourceId(), fileName );
  }
  
  /**
   * Get the ID of the platform that created this identifier.
   * 
   * @return The platform ID.
   */
  public String getPlatformId()
  {
    return getA();
  }  

  /**
   * Get the ID of the course where the resource is located.
   * 
   * @return The course ID.
   */
  public String getCourseId()
  {
    return getB();
  }  
  
  /**
   * Get the ID (unique in the spec. platform/course) of the HU resource.
   * 
   * @return The tool resource ID.
   */
  public String getToolResourceId()
  {
    return getC();
  }  
  
  public String getFileName()
  {
    return getD();
  }
}
