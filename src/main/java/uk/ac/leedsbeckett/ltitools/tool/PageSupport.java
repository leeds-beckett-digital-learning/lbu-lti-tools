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

package uk.ac.leedsbeckett.ltitools.tool;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import uk.ac.leedsbeckett.ltitools.app.ApplicationContext;

/**
 * This class provides common logic for supporting all the JSP pages.
 * 
 * @author jon
 */
public abstract class PageSupport
{
  protected HttpServletRequest request;
  protected String importantmessage="";
  
  protected ApplicationContext appcontext;

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
   * @throws javax.servlet.ServletException
   */
  public void setRequest( HttpServletRequest request ) throws ServletException
  {
    this.request = request;
    appcontext = ApplicationContext.getFromServletContext( request.getServletContext() );
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
