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

package uk.ac.leedsbeckett.ltitools.peergroupassessment.store;

import uk.ac.leedsbeckett.ltitools.peergroupassessment.data.PeerGroupData;
import uk.ac.leedsbeckett.ltitools.peergroupassessment.data.PeerGroupDataKey;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import uk.ac.leedsbeckett.ltitoolset.ResourceKey;
import uk.ac.leedsbeckett.ltitoolset.store.Store;

/**
 * A store of resources which can be retrieved using keys. At present all
 * resources stay in the store until the store is garbage collected after the
 * web application shuts down. All resources are lost entirely at shut down
 * in this demo. A proper implementation would store data on file or in a 
 * database and would purge memory of resources that haven't been used for a
 * while.
 * 
 * @author jon
 */
public class PeerGroupDataStore extends Store<PeerGroupDataKey,PeerGroupData>
{
  static final Logger logger = Logger.getLogger(PeerGroupDataStore.class.getName() );

  Path basepath;
  
  public PeerGroupDataStore( Path basepath )
  {
    super( "peergroupdatastore" );
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
  public PeerGroupData create( PeerGroupDataKey key )
  {
    return new PeerGroupData( key );
  }

  @Override
  public Class<PeerGroupData> getEntryClass()
  {
    return PeerGroupData.class;
  }
  
  @Override
  public Path getPath( PeerGroupDataKey key )
  {
    Path d = basepath.resolve( URLEncoder.encode( key.getResourceKey().getPlatformId(), StandardCharsets.UTF_8 ) );
    Path dd = d.resolve(       URLEncoder.encode( key.getResourceKey().getResourceId(), StandardCharsets.UTF_8 ) );
    return dd.resolve(         URLEncoder.encode( key.getGroupId(), StandardCharsets.UTF_8 ) );
  }
  
  
  public static void main( String[] args )
  {
    ConsoleHandler handler = new ConsoleHandler();
    handler.setLevel( Level.ALL );
    handler.setFormatter( new SimpleFormatter() );
    
    logger.setUseParentHandlers( false );
    logger.addHandler( handler );
    logger.setLevel( Level.ALL );
    
    logger.info( "Starting." );
    PeerGroupDataStore store = new PeerGroupDataStore( Paths.get( "/Users/maber01/peerstore/data/") );
    PeerGroupDataKey rk = new PeerGroupDataKey( new ResourceKey( "platform", "1" ), "222" );
    PeerGroupData r = store.get( rk, true );    
  }
}
