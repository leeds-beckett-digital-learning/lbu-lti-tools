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
public class PeerGroupFormStore
{
  static final Logger logger = Logger.getLogger(PeerGroupFormStore.class.getName() );
  private static final ObjectMapper objectmapper = new ObjectMapper();
  static
  {
    objectmapper.enable( SerializationFeature.INDENT_OUTPUT );
    objectmapper.disable( SerializationFeature.FAIL_ON_EMPTY_BEANS );
    objectmapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
  }
  
  Path basepath;
  PeerGroupForm defaultForm;
  
  public PeerGroupFormStore( Path basepath )
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
  }
  
  public synchronized PeerGroupForm getDefault()
  {
    if ( defaultForm != null )
      return defaultForm;
    
    try
    {
      defaultForm = loadForm( "default" );
      return defaultForm;
    }
    catch (IOException ex)
    {
      logger.log(Level.SEVERE, null, ex);
      return null;
    }  
  }
    
  Path getFormPath( String name )
  {
    return basepath.resolve( URLEncoder.encode( name + ".json", StandardCharsets.UTF_8 ) );
  }
  
  PeerGroupForm loadForm( String name ) throws IOException
  {
    Path filepath = getFormPath( name );
    if ( Files.exists( filepath ) )
    {
      logger.log( Level.FINE, "Loading PeerGroupForm {0}", filepath );
      return objectmapper.readValue( filepath.toFile(), PeerGroupForm.class );
    }
    return null;
  }  
}
