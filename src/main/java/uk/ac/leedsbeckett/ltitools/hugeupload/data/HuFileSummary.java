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

/**
 *
 * @author maber01
 */
public class HuFileSummary
{
  String fileName;
  String sha512Digest;
  int chunkCount = -1;
  int mappedChunkCount = -1;
  int uploadedChunkCount = -1;

  public String getFileName()
  {
    return fileName;
  }

  public void setFileName( String fileName )
  {
    this.fileName = fileName;
  }

  public String getSha512Digest()
  {
    return sha512Digest;
  }

  public void setSha512Digest( String sha512Digest )
  {
    this.sha512Digest = sha512Digest;
  }

  public int getChunkCount()
  {
    return chunkCount;
  }

  public void setChunkCount( int chunkCount )
  {
    this.chunkCount = chunkCount;
  }

  public int getMappedChunkCount()
  {
    return mappedChunkCount;
  }

  public void setMappedChunkCount( int mappedChunkCount )
  {
    this.mappedChunkCount = mappedChunkCount;
  }

  public int getUploadedChunkCount()
  {
    return uploadedChunkCount;
  }

  public void setUploadedChunkCount( int uploadedChunkCount )
  {
    this.uploadedChunkCount = uploadedChunkCount;
  }
  
}
