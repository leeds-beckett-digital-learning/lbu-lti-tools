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
  
  PlatformConfigurationStore configStore;
  HuCourseStore courseStore;
  HuResourceStore resourceStore;
  HuFileMetadataStore fileStore;
  
  public HuStoreCluster( Path basePath )
  {
    this.basePath = basePath;
    Path p = basePath.resolve( "platforms" );
    configStore   = new PlatformConfigurationStore( p );
    courseStore   = new HuCourseStore( p );
    resourceStore = new HuResourceStore( p );
    fileStore     = new HuFileMetadataStore( p );
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

  public HuFileMetadata getFileMetadata( HuFileMetadataKey key, boolean create )
  {
    return fileStore.get( key, create );
  }
  
  public void updateFileMetadata( HuFileMetadata d ) throws IOException
  {
    fileStore.update( d );
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
