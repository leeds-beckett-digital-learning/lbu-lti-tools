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
package uk.ac.leedsbeckett.ltitools.hugeupload.messagedata;

import java.util.Objects;

/**
 *
 * @author maber01
 */
public class HuFileMapChunk
{
  long start;
  long end;
  String hash;

  public long getStart()
  {
    return start;
  }

  public void setStart( long start )
  {
    this.start = start;
  }

  public long getEnd()
  {
    return end;
  }

  public void setEnd( long end )
  {
    this.end = end;
  }

  public String getHash()
  {
    return hash;
  }

  public void setHash( String hash )
  {
    this.hash = hash;
  }
  
  @Override
  public boolean equals( Object other )
  {
    if ( other == null ) return false;
    if ( !(other instanceof HuFileMapChunk) ) return false;
    HuFileMapChunk otherchunk = (HuFileMapChunk)other;
    return end == otherchunk.end && start == otherchunk.start && hash.equals( otherchunk.hash );
  }

  @Override
  public int hashCode()
  {
    int h = 5;
    h = 67 * h + (int) ( this.start ^ ( this.start >>> 32 ) );
    h = 67 * h + (int) ( this.end ^ ( this.end >>> 32 ) );
    h = 67 * h + Objects.hashCode( this.hash );
    return h;
  }
}
