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

package uk.ac.leedsbeckett.ltitools.sharepointsub.store;

import uk.ac.leedsbeckett.ltitools.peergroupassessment.resourcedata.PeerGroupResource;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.stream.Collectors;
import uk.ac.leedsbeckett.ltitoolset.resources.PlatformCourseKey;
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
public class CourseStore extends Store<PlatformCourseKey,CourseSettings>
{
  static final Logger logger = Logger.getLogger(CourseStore.class.getName() );

  Path basepath;
  
  public CourseStore( Path basepath )
  {
    super( "sharepointsubcoursestore" );
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
  public CourseSettings create( PlatformCourseKey key )
  {
    return new CourseSettings( key, false, "Europe/London", null );
  }

  @Override
  public Class<CourseSettings> getEntryClass()
  {
    return CourseSettings.class;
  }
  
  @Override
  public Path getPath( PlatformCourseKey key )
  {
    return basepath.resolve( toFileName( key.getPlatformId() ) )
                   .resolve( "courses" )
                   .resolve( toFileName( key.getCourseId() ) )
                   .resolve( "config.json" );
  }
  
  public List<PlatformCourseKey> getAllPlatformCourseKeys( String platformKey )
  {
    ArrayList<PlatformCourseKey> list = new ArrayList<>();
    Path coursespath = basepath.resolve( toFileName( platformKey ) ).resolve( "courses" );
    try
    {
      for ( Path subdir : Files.list( coursespath ).collect( Collectors.toList() ) )
      {
        Path file = subdir.resolve( "config.json" );
        if ( Files.exists( file ) )
          list.add( new PlatformCourseKey( platformKey, fromFileName( subdir.getFileName().toString() ) ) );
      }
    }
    catch ( IOException ex )
    {
      Logger.getLogger( CourseStore.class.getName() ).log( Level.SEVERE, null, ex );
    }
    
    return list;
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
    CourseStore store = new CourseStore( Paths.get( "/Users/maber01/peerstore/") );
    PlatformCourseKey rk = new PlatformCourseKey( "platform", "1" );
    CourseSettings r = store.get( rk, true );    
  }
}
