/*
 * Copyright 2024 maber01.
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
package uk.ac.leedsbeckett.ltitools.selfenrol;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import uk.ac.leedsbeckett.ltitoolset.store.Entry;

/**
 *
 * @author maber01
 */
public class SePlatformConfigurationEntry implements Serializable, Entry<String>
{
  String key;
  SelfEnrolConfiguration config;
  
  public SePlatformConfigurationEntry( @JsonProperty("key") String key )
  {
    this.key = key;
  }
  
  
  @Override
  public String getKey()
  {
    return key;
  }

  @Override
  public void setKey( String key )
  {
    if ( this.key != null )
      throw new IllegalArgumentException( "Not allowed to change resource key." );
    this.key = key;
  }

  public SelfEnrolConfiguration getConfig()
  {
    return config;
  }

  public void setConfig( SelfEnrolConfiguration config )
  {
    this.config = config;
  }

  
  @Override
  public void initialize()
  {
  }  
}
