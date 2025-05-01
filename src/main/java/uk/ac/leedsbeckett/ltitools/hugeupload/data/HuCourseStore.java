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

package uk.ac.leedsbeckett.ltitools.hugeupload.data;

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
import uk.ac.leedsbeckett.ltitoolset.resources.PlatformResourceKey;
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
public class HuCourseStore extends Store<HuCourseKey,CourseConfigurationEntry>
{
  static final Logger logger = Logger.getLogger(HuCourseStore.class.getName() );

  Path basepath;
  
  public HuCourseStore( Path basepath )
  {
    super( "hugeuploadcoursestore" );
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
  public CourseConfigurationEntry create( HuCourseKey key )
  {
    return new CourseConfigurationEntry( key );
  }

  @Override
  public Class<CourseConfigurationEntry> getEntryClass()
  {
    return CourseConfigurationEntry.class;
  }
  
  @Override
  public Path getPath( HuCourseKey key )
  {
    Path p = basepath.resolve( URLEncoder.encode( key.getPlatformId(), StandardCharsets.UTF_8 ) );
    return p.resolve( URLEncoder.encode( key.getCourseId(), StandardCharsets.UTF_8 ) );
  }  
}
