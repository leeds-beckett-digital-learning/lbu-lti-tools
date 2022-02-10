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

package uk.ac.leedsbeckett.ltidemo;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import uk.ac.leedsbeckett.lti.LtiConfiguration;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.io.FileUtils;

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

  public HttpServletRequest getRequest()
  {
    return request;
  }

  public void setRequest( HttpServletRequest request )
  {
    this.request = request;
    DemoApplicationContext appcontext = DemoApplicationContext.getFromServletContext( request.getServletContext() );
    LtiConfiguration config = appcontext.getConfig();
    if ( request != null )
      action = request.getParameter( "action" );
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
    rawconfig = config.getRawConfiguration();
  }

  void saveToFile( String name, String content ) throws IOException
  {
    FileUtils.writeStringToFile( new File( name ), content, StandardCharsets.UTF_8 );
  }
  
  public String getRawConfiguration()
  {
    return rawconfig;
  }

  public String getAction()
  {
    if ( action == null ) return "";
    return action;
  }
  
  public String getImportantMessage()
  {
    return importantmessage;
  }
}
