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

package uk.ac.leedsbeckett.ltitools.tool.peergroupassessment.store;

import uk.ac.leedsbeckett.ltitools.tool.peergroupassessment.data.PeerGroupResource;
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
import uk.ac.leedsbeckett.ltitools.tool.ResourceKey;
import uk.ac.leedsbeckett.ltitools.tool.store.Entry;
import uk.ac.leedsbeckett.ltitools.tool.store.Store;

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
public class PeerGroupResourceStore extends Store<ResourceKey,PeerGroupResource>
{
  static final Logger logger = Logger.getLogger( PeerGroupResourceStore.class.getName() );

  Path basepath;
  
  public PeerGroupResourceStore( Path basepath )
  {
    super( "peergroupresourcestore" );
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
  public PeerGroupResource create( ResourceKey key )
  {
    return new PeerGroupResource( key );
  }

  @Override
  public Class<PeerGroupResource> getEntryClass()
  {
    return PeerGroupResource.class;
  }
  
  @Override
  public Path getPath( ResourceKey key )
  {
    Path d = basepath.resolve( URLEncoder.encode( key.getPlatformId(), StandardCharsets.UTF_8 ) );
    return d.resolve( URLEncoder.encode( key.getResourceId(), StandardCharsets.UTF_8 ) );
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
    PeerGroupResourceStore store = new PeerGroupResourceStore( Paths.get( "/Users/maber01/peerstore/") );
    ResourceKey rk = new ResourceKey( "platform", "1" );
    PeerGroupResource r = store.get( rk, true );    
  }
}
