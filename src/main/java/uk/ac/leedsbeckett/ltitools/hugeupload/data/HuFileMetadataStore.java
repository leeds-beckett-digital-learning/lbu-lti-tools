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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.leedsbeckett.ltitoolset.store.Store;

/**
 *
 * @author maber01
 */
public class HuFileMetadataStore extends Store<HuFileMetadataKey,HuFileMetadata>
{
  static final Logger logger = Logger.getLogger(HuFileMetadataStore.class.getName() );

  Path basepath;
  
  public HuFileMetadataStore( Path basepath )
  {
    super( "hugeuploadfilemetadatastore" );
    this.basepath = basepath;
    try
    {
      Files.createDirectories( basepath );
    }
    catch (IOException ex)
    {
      logger.log(Level.SEVERE, null, ex);
    }
  }

  @Override
  public HuFileMetadata create( HuFileMetadataKey key )
  {
    return new HuFileMetadata( key );
  }

  @Override
  public Class<HuFileMetadata> getEntryClass()
  {
    return HuFileMetadata.class;
  }

  @Override
  public Path getPath( HuFileMetadataKey key )
  {
    return basepath.resolve( toFileName( key.getPlatformId() ) )
                   .resolve( "courses" )
                   .resolve( toFileName( key.getCourseId() ) )
                   .resolve( "resources" )
                   .resolve( toFileName( key.getToolResourceId() ) )
                   .resolve( "files" )
                   .resolve( toFileName( key.getFileName() )  + "_" + "metadata.json" );
  }  
}
