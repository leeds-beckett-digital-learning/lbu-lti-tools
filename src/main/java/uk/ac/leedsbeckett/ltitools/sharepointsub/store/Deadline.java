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

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 *
 * @author maber01
 */
public class Deadline
{
  private final int year;
  private final int month;
  private final int day;
  private final int hour;
  
  public Deadline( 
          @JsonProperty("year")  int year, 
          @JsonProperty("month") int month, 
          @JsonProperty("day")   int day, 
          @JsonProperty("hour")  int hour )
  {
    this.year = year;
    this.month = month;
    this.day = day;
    this.hour = hour;
  }

  public int getYear()
  {
    return year;
  }

  public int getMonth()
  {
    return month;
  }

  public int getDay()
  {
    return day;
  }

  public int getHour()
  {
    return hour;
  }
  
  public String toString()
  {
    StringBuilder b = new StringBuilder();
    b.append( year );
    b.append( "-" );
    b.append( month );
    b.append( "-" );
    b.append( day );
    b.append( "-" );
    b.append( hour );
    return b.toString();
  }
  
  public long toEpochMilli( ZoneId zid )
  {
    ZonedDateTime zdt = ZonedDateTime.of( year, month, day, hour, 0, 0, 0, zid );
    return zdt.toInstant().toEpochMilli();
  }

  @Override
  public int hashCode()
  {
    return (year * 365 + month) ^ (day*24 + hour);
  }

  @Override
  public boolean equals( Object obj )
  {
    if ( obj == null ) return false;
    if ( !(obj instanceof Deadline) ) return false;
    Deadline other = (Deadline) obj;
    return this.year == other.year && this.month == other.month && this.day == other.day && this.hour == other.hour;
  }
}
