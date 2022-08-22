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

package uk.ac.leedsbeckett.ltitools.admin;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import uk.ac.leedsbeckett.lti.config.LtiConfiguration;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.io.FileUtils;
import uk.ac.leedsbeckett.ltitools.tool.PageSupport;

/**
 * This class provides logic for use within an admin JSP page.
 * 
 * @author jon
 */
public class AdminPageSupport extends PageSupport
{
  static final Logger logger = Logger.getLogger(AdminPageSupport.class.getName() );

  String action;
  String rawconfig;
  Path logconfigpath;
  String logconfig="";
          
 /**
   * The JSP will call this to initiate processing and then call the getter
   * methods to retrieve outcomes of the processing.
   * 
   * @param request The HttpRequest associated with the JSP's servlet.
   */
  @Override
  public void setRequest( HttpServletRequest request ) throws ServletException
  {
    super.setRequest( request );
    
    // Retrieve information about the application
    LtiConfiguration config = appcontext.getConfig();
    logconfigpath = Paths.get( request.getServletContext().getRealPath( "WEB-INF/classes/logging.properties" ) );
    
    // Find out if there was a form field called 'action'
    action = request.getParameter( "action" );
    
    // If a saveconfig action was specified take the input from the form and
    // save it over the config file. Then tell the LTI library to reload it.
    if ( "saveconfig".equals( action ) )
    {
      String name = config.getConfigFileName();
      try
      {
        saveLtiConfigToFile( name, request.getParameter( "config" ) );
        config.load( name );
        saveLoggingConfigToFile( request.getParameter( "logconfig" ) );
        importantmessage = "Configuration successfully saved.";
      }
      catch ( IOException ioe )
      {
        importantmessage = "Configuration saving failed. " + ioe.getMessage();
      }
    }

    // Regardless, fetch the current config now.
    rawconfig = config.getRawConfiguration();
    logger.log( Level.FINE, "Path of logging.properties = {0}", logconfigpath );
    if ( Files.exists( logconfigpath ) )
      try
      {
        logconfig = FileUtils.readFileToString( logconfigpath.toFile(), StandardCharsets.UTF_8 );
      }
      catch (IOException ex)
      {
        logger.log(Level.SEVERE, "Unable to read logging config.", ex);
      }
  }

  /**
   * Simply write the text content into a file with the given name.
   * @param name Name of the file.
   * @param content The content to put in the file using UTF encoding.
   * @throws IOException If there is a problem writing to the file.
   */
  void saveLtiConfigToFile( String name, String content ) throws IOException
  {
    FileUtils.writeStringToFile( new File( name ), content, StandardCharsets.UTF_8 );
  }

  void saveLoggingConfigToFile( String content ) throws IOException
  {
    logconfig = content;
    FileUtils.writeStringToFile( logconfigpath.toFile(), logconfig, StandardCharsets.UTF_8 );
  }
  
  /**
   * Get the full content of the configuration file as a string.
   * 
   * @return The content.
   */
  public String getRawConfiguration()
  {
    return rawconfig;
  }

  public String getLogConfiguration()
  {
    return logconfig;
  }
  
  /**
   * Get the value of the action parameter.
   * 
   * @return The value from the request.
   */
  public String getAction()
  {
    if ( action == null ) return "";
    return action;
  }
}
