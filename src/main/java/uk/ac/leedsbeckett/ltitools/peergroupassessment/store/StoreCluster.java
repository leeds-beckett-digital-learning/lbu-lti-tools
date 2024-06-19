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
package uk.ac.leedsbeckett.ltitools.peergroupassessment.store;

import uk.ac.leedsbeckett.ltitools.peergroupassessment.inputdata.PeerGroupData;
import uk.ac.leedsbeckett.ltitools.peergroupassessment.formdata.PeerGroupForm;
import uk.ac.leedsbeckett.ltitools.peergroupassessment.inputdata.PeerGroupDataKey;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import uk.ac.leedsbeckett.ltitools.peergroupassessment.messagedata.Id;
import uk.ac.leedsbeckett.ltitools.peergroupassessment.messagedata.PgaDataList;
import uk.ac.leedsbeckett.ltitoolset.resources.PlatformResourceKey;
import uk.ac.leedsbeckett.ltitools.peergroupassessment.resourcedata.PeerGroupResource;

/**
 *
 * @author maber01
 */
public class StoreCluster
{
  Path basePath;
  
  ResourceStore resourceStore;
  InputDataStore dataStore;
  FormStore formstore;
  PlatformConfigurationStore configStore;

  public StoreCluster( Path basePath )
  {
    this.basePath = basePath;
    resourceStore = new ResourceStore( basePath.resolve( "resources" ) );
    dataStore     = new InputDataStore(     basePath.resolve( "data"      ) );
    formstore     = new FormStore(     basePath.resolve( "forms"     ) );
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
  
  public PeerGroupResource getResource( PlatformResourceKey key, boolean create )
  {
    return resourceStore.get( key, create );
  }
  
  public void updateResource( PeerGroupResource r ) throws IOException
  {
    resourceStore.update( r );
  }
  
  /**
   * Currently ignores the form ID and returns a fixed, default form.
   * 
   * @param formid
   * @return 
   */
  public PeerGroupForm getForm( String formid )
  {
    return formstore.getDefault();
  }
  
  public PeerGroupData getData( PeerGroupDataKey key, boolean create )
  {
    return dataStore.get( key, create );
  }

  public PgaDataList getAllData( PeerGroupResource resource )
  {
    PgaDataList list = new PgaDataList();
    for ( String gid : resource.groupIdsInOrder )
    {
      PeerGroupDataKey key = new PeerGroupDataKey( resource.getKey(), gid );
      PeerGroupData data = dataStore.get( key, false );
      if ( data != null )
        list.add( data );
    }
    return list;
  }

  public void updateData( PeerGroupData d ) throws IOException
  {
    dataStore.update( d );
  }
  
}
