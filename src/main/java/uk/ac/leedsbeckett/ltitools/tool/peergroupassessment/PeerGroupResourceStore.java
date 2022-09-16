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

package uk.ac.leedsbeckett.ltitools.tool.peergroupassessment;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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
import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import uk.ac.leedsbeckett.ltitools.tool.ResourceKey;

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
public class PeerGroupResourceStore
{
  static final Logger logger = Logger.getLogger( PeerGroupResourceStore.class.getName() );
  private static final ObjectMapper objectmapper = new ObjectMapper();
  static
  {
    objectmapper.enable( SerializationFeature.INDENT_OUTPUT );
    objectmapper.disable( SerializationFeature.FAIL_ON_EMPTY_BEANS );
    objectmapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
  }
  
  Path basepath;
  Cache<ResourceKey,PeerGroupResource> cache;
  
  public PeerGroupResourceStore( Path basepath )
  {
    this.basepath = basepath;
    try
    {
      Files.createDirectories( basepath );
    }
    catch (IOException ex)
    {
      logger.log(Level.SEVERE, null, ex);
    }
    
    logger.log(Level.FINE, "Caching provider class {0}", Caching.getCachingProvider().getClass().getName() );
    CacheManager manager = Caching.getCachingProvider().getCacheManager();
    MutableConfiguration<ResourceKey, PeerGroupResource> config = 
            new MutableConfiguration<ResourceKey, PeerGroupResource>()
                    .setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(Duration.ONE_HOUR));
    cache = manager.createCache( "peergroupresource", config );
  }
  
  /**
   * Find a resource keyed by platform ID and resource ID with option to
   * create the resource if it doesn't exist yet.
   * 
   * @param key
   * @param create Set true if the resource should be created if it doesn't already exist.
   * @return The resource or null if it wasn't found and creation wasn't requested.
   */
  public synchronized PeerGroupResource get( ResourceKey key, boolean create )
  {
    PeerGroupResource r = cache.get( key );
    
    if ( r != null )
    {
      logger.log( Level.FINE, "From cache - PeerGroupResource {0}", key.toString() );
      return r;      
    }
    
    logger.log( Level.FINE, "Not in cache - PeerGroupResource {0}", key.toString() );
    try
    {
      r = loadResource( key );
      if ( r == null )
      {
        r = new PeerGroupResource();
        r.setResourceKey( key );
        // an entirely new resource so set it up
        r.initialize();
        saveResource( key, r );
      }
    }
    catch (IOException ex)
    {
      logger.log(Level.SEVERE, null, ex);
      return null;
    }
    
    cache.put( key, r );
    if ( !cache.containsKey(key) )
      logger.log( Level.SEVERE, "Put resource in cache but key is not present {0}", key.toString() );

    return r;
  }
    
  public void update( PeerGroupResource pgr ) throws IOException
  {
    if ( pgr.getResourceKey() == null )
      throw new IllegalArgumentException( "Cannot update resource that lacks a key." );
    saveResource( pgr.getResourceKey(), pgr );
  }
  
  Path getResourcePath( ResourceKey key )
  {
    Path d = basepath.resolve( URLEncoder.encode( key.getPlatformId(), StandardCharsets.UTF_8 ) );
    return d.resolve( URLEncoder.encode( key.getResourceId(), StandardCharsets.UTF_8 ) );
  }
  
  PeerGroupResource loadResource( ResourceKey key ) throws IOException
  {
    Path filepath = getResourcePath( key );
    if ( Files.exists( filepath ) )
    {
      logger.log( Level.FINE, "Loading PeerGroupResource {0}", filepath );
      return objectmapper.readValue( filepath.toFile(), PeerGroupResource.class );
    }
    return null;
  }
  
  void saveResource( ResourceKey key, PeerGroupResource r ) throws IOException
  {
    Path filepath = getResourcePath( key );
    Files.createDirectories( filepath.getParent() );
    logger.log( Level.FINE, "Saving PeerGroupResource {0}", filepath );
    objectmapper.writeValue( filepath.toFile(), r );
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
