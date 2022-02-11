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

package uk.ac.leedsbeckett.ltidemo.admin;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import uk.ac.leedsbeckett.lti.LtiConfiguration;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.io.FileUtils;
import uk.ac.leedsbeckett.ltidemo.app.DemoApplicationContext;

/**
 * This class provides logic for use within an admin JSP page.
 * 
 * @author jon
 */
public class AdminOutcomes
{
  HttpServletRequest request;
  String action;
  String rawconfig;
  String importantmessage="";

  /**
   * Get the HTTP request associated with the JSP page that uses this object.
   * @return 
   */
  public HttpServletRequest getRequest()
  {
    return request;
  }

  /**
   * The JSP will call this to initiate processing and then call the getter
   * methods to retrieve outcomes of the processing.
   * 
   * @param request The HttpRequest associated with the JSP's servlet.
   */
  public void setRequest( HttpServletRequest request )
  {
    this.request = request;
    
    // Retrieve information about the application
    DemoApplicationContext appcontext = DemoApplicationContext.getFromServletContext( request.getServletContext() );
    LtiConfiguration config = appcontext.getConfig();
    
    // Find out if there was a form field called 'action'
    if ( request != null )
      action = request.getParameter( "action" );
    
    // If a saveconfig action was specified take the input from the form and
    // save it over the config file. Then tell the LTI library to reload it.
    if ( "saveconfig".equals( action ) )
    {
      String name = config.getConfigFileName();
      try
      {
        saveToFile( name, request.getParameter( "config" ) );
        config.load( name );
        importantmessage = "Configuration successfully saved.";
      }
      catch ( IOException ioe )
      {
        importantmessage = "Configuration saving failed. " + ioe.getMessage();
      }
    }
    
    // Regardless, fetch the current config now.
    rawconfig = config.getRawConfiguration();
  }

  /**
   * Simply write the text content into a file with the given name.
   * @param name Name of the file.
   * @param content The content to put in the file using UTF encoding.
   * @throws IOException If there is a problem writing to the file.
   */
  void saveToFile( String name, String content ) throws IOException
  {
    FileUtils.writeStringToFile( new File( name ), content, StandardCharsets.UTF_8 );
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
  
  /**
   * Get the important message.
   * 
   * @return An important message or an empty string.
   */
  public String getImportantMessage()
  {
    return importantmessage;
  }
}
