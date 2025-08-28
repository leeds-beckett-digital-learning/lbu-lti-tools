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
package uk.ac.leedsbeckett.ltitools.sharepointsub.store;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import uk.ac.leedsbeckett.jesharepoint.SharepointSettings;
import uk.ac.leedsbeckett.ltitoolset.resources.PlatformCourseKey;

/**
 *
 * @author maber01
 */
public class StoreCluster
{
  Path basePath;
  
  CourseStore courseStore;
  PlatformConfigurationStore configStore;

  public StoreCluster( Path basePath )
  {
    this.basePath = basePath;
    Path p = basePath.resolve( "platforms" );
    courseStore = new CourseStore( p );
    configStore   = new PlatformConfigurationStore( p );
  }

  public Configuration getPlatformConfiguration( String platform, boolean create ) throws IOException
  {
    ConfigurationEntry entry = configStore.get( platform, true );
    if ( entry.getConfig() == null )
    {
      entry.setConfig( Configuration.getDefaultConfig() );
      configStore.update( entry );
    }
    return entry.getConfig();
  }
  
  public void updatePlatformConfiguration( String platformId, Configuration c ) throws IOException
  {
    ConfigurationEntry entry = configStore.get( platformId, false );
    entry.setConfig( c );
    configStore.update( entry );
  }
  
  public CourseSettings getCourseSettings( PlatformCourseKey key, boolean create )
  {
    return courseStore.get( key, create );
  }
  
  public void updateCourseSettings( CourseSettings cs ) throws IOException
  {
    courseStore.update( cs );
  }  
  
  public List<PlatformCourseKey> getAllPlatformCourseKeys( String platformKey )
  {
    return courseStore.getAllPlatformCourseKeys( platformKey );
  }

  public List<String> getAllPlatformKeys()
  {
    return configStore.getAllPlatformKeys();
  }  

  public SharepointSettings getPlatformSharepointSettings( String key )
  {
    Path p = configStore.getSharepointSettingsPath( key );
    if ( p == null ) return null;
    if ( !Files.exists( p ) ) return null;
    return new SharepointSettings( p );
  }
}
