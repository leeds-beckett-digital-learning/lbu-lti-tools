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
package uk.ac.leedsbeckett.ltitools.tool.peergroupassessment;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import uk.ac.leedsbeckett.ltitools.tool.ResourceKey;

/**
 *
 * @author maber01
 */
public class PeerGroupAssessmentStore
{
  Path basePath;
  
  PeerGroupResourceStore resourceStore;
  PeerGroupDataStore dataStore;
  PeerGroupFormStore formstore;

  public PeerGroupAssessmentStore( Path basePath )
  {
    this.basePath = basePath;
    resourceStore = new PeerGroupResourceStore( basePath.resolve( "resources" ) );
    dataStore     = new PeerGroupDataStore(     basePath.resolve( "data"      ) );
    formstore     = new PeerGroupFormStore(     basePath.resolve( "forms"     ) );
  }

  public PeerGroupResource getResource( ResourceKey key, boolean create )
  {
    return resourceStore.get( key, create );
  }
  
  public void updateResource( PeerGroupResource r ) throws IOException
  {
    resourceStore.update( r );
  }
  
  public PeerGroupForm getDefaultForm()
  {
    return formstore.getDefault();
  }
  
  public PeerGroupData getData( PeerGroupDataKey key, boolean create )
  {
    return dataStore.get( key, create );
  }
}
