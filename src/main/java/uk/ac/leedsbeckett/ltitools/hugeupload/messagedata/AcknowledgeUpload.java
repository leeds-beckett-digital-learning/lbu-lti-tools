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
public class AcknowledgeUpload
{
  final boolean complete;
  final Integer nextChunk;
  final Long start;
  final Long end;

  public AcknowledgeUpload( boolean complete, Integer nextChunk, Long start, Long end )
  {
    this.complete = complete;
    this.nextChunk = nextChunk;
    this.start = start;
    this.end = end;
  }
  
  public boolean isComplete()
  {
    return complete;
  }

  public Integer getNextChunk()
  {
    return nextChunk;
  }

  public Long getStart()
  {
    return start;
  }

  public Long getEnd()
  {
    return end;
  }
}
