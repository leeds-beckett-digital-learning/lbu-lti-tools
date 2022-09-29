/*
 * Copyright 2022 maber01.
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
package uk.ac.leedsbeckett.ltitools.tool.websocket;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.Session;
import org.apache.commons.text.StringSubstitutor;
import uk.ac.leedsbeckett.ltitools.tool.websocket.annotations.EndpointMessageHandler;

/**
 *
 * @author maber01
 */
public abstract class ToolEndpoint
{
  static final Logger logger = Logger.getLogger( ToolEndpoint.class.getName() );

  static final String JS_SUPERCLASS = 
    "\n\nclass Message\n" +
    "{\n" +
    "  constructor( messageType, payloadType )\n" +
    "  {\n" +
    "    this.id = nextid++;\n" +
    "    this.messageType = messageType?messageType:null;\n" +
    "    this.payloadType = payloadType?payloadType:null;\n" +
    "    this.replyToId   = null;\n" +
    "    this.payload     = null;\n" +
    "  }\n" +
    "  \n" +
    "  toString()\n" +
    "  {\n" +
    "    var str = \"toolmessageversion1.0\\n\";\n" +
    "    str += \"id:\" + this.id + \"\\n\";\n" +
    "    if ( this.replyToId )\n" +
    "      str += \"replytoid:\" + this.replyToId + \"\\n\";\n" +
    "    if ( this.messageType )\n" +
    "      str += \"messagetype:\" + this.messageType + \"\\n\";\n" +
    "    if ( this.payloadType && this.payload )\n" +
    "    {\n" +
    "      str += \"payloadtype:\" + this.payloadType + \"\\npayload:\\n\" ;\n" +
    "      str += JSON.stringify( this.payload );\n" +
    "    }\n" +
    "    return str;\n" +
    "  }\n" +
    "}\n\n\n";
          
  static final String JS_TEMPLATE = 
    "class ${classname}Message extends Message\n" + 
    "{\n" + 
    "  constructor()\n" + 
    "  {\n" + 
    "    super( \"${messagetype}\", null );\n" + 
    "    this.payload = null;\n" + 
    "  }\n" + 
    "}\n\n";

  static final String JS_TEMPLATE_PAYLOAD = 
    "class ${classname}Message extends Message\n" + 
    "{\n" + 
    "  constructor( ${parameters} )\n" + 
    "  {\n" + 
    "    super( \"${messagetype}\", \"${payloadtype}\" );\n" + 
    "    this.payload = { ${payload} };\n" + 
    "  }\n" + 
    "}\n\n";

  
  final HashMap<String,HandlerMethodRecord> handlerMethods = new HashMap<>();
  
  public ToolEndpoint()
  {
    for ( Method method : this.getClass().getMethods() )
    {
      logger.log( Level.INFO, "Checking method {0}", method.getName() );
      for ( EndpointMessageHandler handler : method.getAnnotationsByType( EndpointMessageHandler.class ) )
      {
        logger.log(Level.INFO, "Method has EndpointMessageHandler annotation and name = {0}", handler.name());
        Class<?>[] classarray = method.getParameterTypes();
        logger.log(Level.INFO, "Method parameter count = {0}", classarray.length);
        if ( ( classarray.length == 2 || classarray.length == 3 ) && 
                classarray[0].equals( Session.class ) &&
                classarray[1].equals( ToolMessage.class ) )
        {
          logger.log(Level.INFO, "Parameters match signature and second parameter class is {0}", classarray.length == 3?classarray[2]:"not present");
          if ( classarray.length == 3 )
            ToolMessageTypeSet.addType( classarray[2].getName() );
          String name = handler.name();
          if ( name == null || name.length() == 0 )
          {
            name = method.getName();
            if ( name.startsWith( "handle") )
              name = name.substring( "handle".length() );
            char c = name.charAt( 0 );
            if ( Character.isAlphabetic( c ) && Character.isLowerCase( c ) )
              name = "" + Character.toUpperCase( c ) + name.substring( 1 );
          }
          logger.log( Level.INFO, "Using message name = {0}", name );
          if ( handlerMethods.containsKey( name ) )
            logger.log( Level.SEVERE, "Message handlers with duplicate names = ", name );
          else
          {
            HandlerMethodRecord record = new HandlerMethodRecord( 
                          name, 
                          method, 
                          classarray.length == 3?classarray[2]:null );
            logger.log( Level.INFO, "Javascript:" );
            logger.log( Level.INFO, record.getJavaScriptClass() );
            handlerMethods.put( name, record );
          }
        }
      }
    }
    logger.log( Level.INFO, "Javascript:" );
    logger.log( Level.INFO, getJavaScript() );
  }
  
  public String getJavaScript()
  {
    StringBuilder sb = new StringBuilder();
    sb.append( JS_SUPERCLASS );
    for ( HandlerMethodRecord record : handlerMethods.values() )
      sb.append( record.getJavaScriptClass() );
    return sb.toString();
  }
  
  public boolean dispatchMessage( Session session, ToolMessage message ) throws IOException
  {
    logger.log( Level.INFO, "dispatchMessage type = " + message.getMessageType() );
    HandlerMethodRecord record = handlerMethods.get( message.getMessageType() );
    if ( record == null ) return false;
    
    Class pc = record.getParameterClass();
    logger.log( Level.INFO, "dispatchMessage found handler record " + pc );
    
    if ( pc != null )
    {
      if ( message.getPayload() == null ) return false;
      logger.log( Level.INFO, "dispatchMessage payload class is " + message.getPayload().getClass() );
      if ( !(message.getPayload().getClass().isAssignableFrom( record.getParameterClass() ) ) )
        return false;
    }
    
    logger.log( Level.INFO, "Invoking method." );
    try
    {
      if ( pc == null )
        record.method.invoke( this, session, message );
      else
        record.method.invoke( this, session, message, message.getPayload() );
    }
    catch ( IllegalAccessException | IllegalArgumentException ex )
    {
      logger.log( Level.SEVERE, "Web socket message handler error.", ex );
    }
    catch ( InvocationTargetException ex )
    {
      // method threw exception
      Throwable original = ex.getCause();
      if ( original instanceof IOException )
        throw (IOException)original;
      logger.log( Level.SEVERE, "Web socket message handler error.", ex );
    }
    
    return true;
  }
  
  public class HandlerMethodRecord
  {
    final String name;
    final Method method;
    final Class<?> parameterClass;
    
    public HandlerMethodRecord( String name, Method method, Class<?> parameterClass )
    {
      this.name = name;
      this.method = method;
      this.parameterClass = parameterClass;
    }

    public String getName()
    {
      return name;
    }

    public Method getMethod()
    {
      return method;
    }

    public Class<?> getParameterClass()
    {
      return parameterClass;
    }
  
    public String getJavaScriptClass()
    {
      StringBuilder sba = new StringBuilder();
      StringBuilder sbb = new StringBuilder();
      boolean first=true;
      
      if ( parameterClass != null )
      {
        Constructor<?>[] constructors = parameterClass.getConstructors();
        if ( constructors == null || constructors.length != 1 )
          return null;
        
        Annotation[][] anns = constructors[0].getParameterAnnotations();
        String names[] = new String[anns.length];
        for ( int i=0; i<anns.length; i++ )
        {
          for ( Annotation a : anns[i] )
            if ( a instanceof JsonProperty )
              names[i] = ((JsonProperty)a).value();
          if ( names[i] == null )
            return null;
        }
        
        for ( String n : names )
        {
          if ( first )
            first=false;
          else
          {
            sba.append( ", " );
            sbb.append( ", " );
          }
          sba.append( n );
          sbb.append( "\"" );
          sbb.append( n );
          sbb.append( "\": " );
          sbb.append( n );
        }
      }

      Map<String,String> map = new HashMap<>();
      StringSubstitutor sub = new StringSubstitutor( map );
      map.put( "classname",   this.getName() );
      map.put( "messagetype", this.getName() );
      if ( parameterClass == null )
        return sub.replace( JS_TEMPLATE );        
      
      map.put( "payloadtype", this.getParameterClass().getName() );
      map.put( "parameters",  sba.toString() );
      map.put( "payload",     sbb.toString() );
      return sub.replace( JS_TEMPLATE_PAYLOAD );
    }
  }
  
}
