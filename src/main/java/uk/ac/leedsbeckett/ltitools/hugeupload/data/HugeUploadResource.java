/*
 * Copyright 2022 Leeds Beckett University.
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

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import uk.ac.leedsbeckett.ltitools.hugeupload.messagedata.HuFileMap;
import uk.ac.leedsbeckett.ltitools.hugeupload.messagedata.HuUploadState;
import uk.ac.leedsbeckett.ltitoolset.store.Entry;


/**
 * An object that represents the resource which the user is accessing after the
 * LTI launch.
 * 
 * @author jon
 */
public class HugeUploadResource implements Serializable, Entry<HuResourceKey>
{
  HuResourceKey key;
  HuFileMap fileMap = null;
  HuUploadState uploadState = null;
    
  public HugeUploadResource( @JsonProperty("key") HuResourceKey key )
  {
    this.key = key;
  }

  @Override
  public HuResourceKey getKey()
  {
    return key;
  }

  @Override
  public void setKey( HuResourceKey key )
  {
    if ( this.key != null )
      throw new IllegalArgumentException( "Not allowed to change resource key." );
    this.key = key;
  }

  public HuFileMap getFileMap()
  {
    return fileMap;
  }

  public void setFileMap( HuFileMap fileMap )
  {
    this.fileMap = fileMap;
  }

  public HuUploadState getUploadState()
  {
    return uploadState;
  }

  public void setUploadState( HuUploadState uploadState )
  {
    this.uploadState = uploadState;
  }

  
  /**
   * Called by the resource store when an entirely new resource is needed.
   */
  @Override
  public void initialize()
  {
  }

  @Override
  public String toString()
  {
    return "A huge upload resource " + key.toString();
  }
}
