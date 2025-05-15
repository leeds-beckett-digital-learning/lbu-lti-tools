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

/**
 *
 * @author maber01
 */
public class HuBinaryChunkUploadReq
{
  int chunkNo;
  long start;
  long end;
  String hash;
  
  int recentChunkAck = -1;

  public int getChunkNo()
  {
    return chunkNo;
  }

  public void setChunkNo( int chunkNo )
  {
    this.chunkNo = chunkNo;
  }

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

  public int getRecentChunkAck()
  {
    return recentChunkAck;
  }

  public void setRecentChunkAck( int recentChunkAck )
  {
    this.recentChunkAck = recentChunkAck;
  }
}
