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
package uk.ac.leedsbeckett.ltitools.hugeupload.data;

import java.io.IOException;
import java.nio.file.Path;

/**
 *
 * @author maber01
 */
public class HuStoreCluster
{
  Path basePath;
  
  HuResourceStore resourceStore;
  HuCourseStore courseStore;
  PlatformConfigurationStore configStore;
  
  public HuStoreCluster( Path basePath )
  {
    this.basePath = basePath;
    resourceStore = new HuResourceStore( basePath.resolve( "resources" ) );
    courseStore   = new HuCourseStore( basePath.resolve( "courses" ) );
    configStore   = new PlatformConfigurationStore( basePath.resolve( "platformconfig" ) );
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

  public CourseConfiguration getCourseConfiguration( HuCourseKey courseKey, boolean create ) throws IOException
  {
    CourseConfigurationEntry entry = courseStore.get( courseKey, true );
    if ( entry.getCourseConfig() == null )
    {
      entry.setCourseConfig( CourseConfiguration.getDefaultCourseConfiguration() );
      courseStore.update( entry );
    }
    return entry.getCourseConfig();
  }
  
  public void updateCourseConfiguration( HuCourseKey key, CourseConfiguration c ) throws IOException
  {
    CourseConfigurationEntry entry = courseStore.get( key, false );
    entry.setCourseConfig( c );
    courseStore.update( entry );
  }  

  public HugeUploadResource getResource( HuResourceKey key, boolean create )
  {
    return resourceStore.get( key, create );
  }
  
  public void updateResource( HugeUploadResource r ) throws IOException
  {
    resourceStore.update( r );
  }

  public Path getResourcePendingFilePath( HuResourceKey key )
  {
    Path r = resourceStore.getPath( key );
    return r.getParent().resolve( "pending_" + r.getFileName() + ".bin" );
  }

  public Path getResourceFinalFilePath( HuResourceKey key )
  {
    Path r = resourceStore.getPath( key );
    return r.getParent().resolve( "final_" + r.getFileName() + ".bin" );
  }
}
