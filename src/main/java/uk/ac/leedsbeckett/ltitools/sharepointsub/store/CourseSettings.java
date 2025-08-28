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
package uk.ac.leedsbeckett.ltitools.sharepointsub.store;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import uk.ac.leedsbeckett.ltitoolset.resources.PlatformCourseKey;
import uk.ac.leedsbeckett.ltitoolset.store.Entry;

/**
 *
 * @author maber01
 */
public class CourseSettings implements Entry<PlatformCourseKey>
{
  PlatformCourseKey key;
  boolean enabled;
  String timezone;
  HashMap<String,Dropbox> dropboxMap;

  @JsonIgnore
  ZoneId zid = null;
  
  public CourseSettings( 
          @JsonProperty("key")        PlatformCourseKey key,
          @JsonProperty("enabled")    boolean enabled,
          @JsonProperty("timezone")   String timezone,
          @JsonProperty("dropboxMap") HashMap<String,Dropbox> dropboxMap
          )
  {
    this.key        = key;
    this.enabled    = enabled;
    this.timezone   = timezone;
    
    zid = ( this.timezone == null ) ? null : ZoneId.of( timezone );
    this.dropboxMap = dropboxMap;
    if ( this.dropboxMap == null )
      this.dropboxMap = new HashMap<>();
  }

  @Override
  public PlatformCourseKey getKey()
  {
    return key;
  }

  @Override
  public void setKey( PlatformCourseKey key )
  {
    this.key = key;
  }

  public boolean isEnabled()
  {
    return enabled;
  }

  public void setEnabled( boolean enabled )
  {
    this.enabled = enabled;
  }

  public String getTimezone()
  {
    return timezone;
  }

  public void setTimezone( String timezone )
  {
    this.timezone = timezone;
    zid = ( this.timezone == null ) ? null : ZoneId.of( timezone );
  }

  public HashMap<String, Dropbox> getDropboxMap()
  {
    return dropboxMap;
  }  
  
  @Override
  public void initialize()
  {
  }

  @JsonIgnore
  public long getTimestamp( Deadline deadline )
  {
    ZonedDateTime zdt = ZonedDateTime.of( 
            deadline.getYear(), 
            deadline.getMonth(), 
            deadline.getDay(), 
            deadline.getHour(), 0, 0, 0, zid );
    return zdt.toEpochSecond() * 1000L;
  }
}
