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
package uk.ac.leedsbeckett.ltitools.selfenrol;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import org.apache.commons.lang3.StringUtils;
import uk.ac.leedsbeckett.ltitoolset.backchannel.JsonResult;
import uk.ac.leedsbeckett.ltitoolset.backchannel.blackboard.BlackboardPlatform;
import uk.ac.leedsbeckett.ltitoolset.backchannel.blackboard.data.CourseV2;
import uk.ac.leedsbeckett.ltitoolset.backchannel.blackboard.data.GetCoursesV3Results;
import uk.ac.leedsbeckett.ltitoolset.backchannel.blackboard.data.RestExceptionMessage;
import uk.ac.leedsbeckett.ltitoolset.websocket.ToolEndpoint;
import uk.ac.leedsbeckett.ltitoolset.websocket.ToolMessage;
import uk.ac.leedsbeckett.ltitoolset.websocket.ToolMessageDecoder;
import uk.ac.leedsbeckett.ltitoolset.websocket.ToolMessageEncoder;
import uk.ac.leedsbeckett.ltitoolset.websocket.annotations.EndpointMessageHandler;
import uk.ac.leedsbeckett.ltitoolset.websocket.HandlerAlertException;
import uk.ac.leedsbeckett.ltitoolset.websocket.annotations.EndpointJavascriptProperties;

/**
 * The web socket server endpoint that implements all the logic of this tool.
 * It is annotated in two ways - so that the container (e.g. tomcat) will map
 * URLs on to the class - and so that the build process can create javascript
 * for the client side of the web socket.
 * 
 * @author maber01
 */
@ServerEndpoint( 
        value="/socket/selfenrol", 
        decoders=ToolMessageDecoder.class, 
        encoders=ToolMessageEncoder.class )
@EndpointJavascriptProperties(
        module="selfenrol",
        prefix="Se",
        messageEnum="uk.ac.leedsbeckett.ltitools.selfenrol.SeServerMessageName"
)
public class SeEndpoint extends ToolEndpoint
{
  static final Logger logger = Logger.getLogger(SeEndpoint.class.getName() );
  
  SelfEnrolTool tool;
  SeToolLaunchState seState;
  
  // Don't store a reference to the resource or other data here.
  // It will get out of sync with instances held by other endpoint instances.
  // Rely on efficient caching and fetching at the start of every transaction.

  /**
   * Most work is done by the super-class. This sub-class fetches references
   * to tool specific objects.
   * 
   * @param session The session this endpoint belongs to.
   * @throws IOException If opening should be aborted.
   */
  @OnOpen
  @Override
  public void onOpen(Session session) throws IOException
  {
    super.onOpen( session );
    
    seState = (SeToolLaunchState)getState().getToolLaunchState();
    tool = (SelfEnrolTool)getToolCoordinator().getTool( getState().getToolKey() );
  }
  
  /**
   * Simply invokes super-class.
   * 
   * @param session The session this endpoint belongs to.
   * @throws IOException Unlikely to be thrown.
   */
  @OnClose
  @Override
  public void onClose(Session session) throws IOException
  {
    super.onClose( session );
  }

  /**
   * At present justs puts a line in the log.
   * 
   * @param session The session this endpoint belongs to.
   * @param throwable The throwable that caused the issue.
   */
  @OnError
  public void onError(Session session, Throwable throwable)
  {
    logger.log( Level.SEVERE, "Web socket error.", throwable );
  }  

  /**
   * Simply passes on responsibility for processing to the super-class.
   * 
   * @param session The session this endpoint belongs to.
   * @param message The incoming message from the client end.
   * @throws IOException Indicates failure to process.
   */
  @OnMessage
  @Override
  public void onMessage(Session session, ToolMessage message) throws IOException
  {
    super.onMessage( session, message );
  }

  /**
   * Client requested the resource data.
   * 
   * @param session The session this endpoint belongs to.
   * @param message The incoming message from the client end.
   * @param search  The detail of the requested search.
   * @throws IOException Indicates failure to process. 
   * @throws uk.ac.leedsbeckett.ltitoolset.websocket.HandlerAlertException 
   */
  @EndpointMessageHandler()
  public void handleSearch( Session session, ToolMessage message, SeSearch search )
          throws IOException, HandlerAlertException
  {
    BlackboardPlatform bp = getBlackboardPlatform();
    JsonResult result = bp.getV3Courses( null, "Course" );
    if ( !result.isComplete() )
      throw new HandlerAlertException( "Technical problem running search.", message.getId() );
    if ( !result.isSuccessful() )
    {
      RestExceptionMessage m = (RestExceptionMessage)result.getResult();
      throw new HandlerAlertException( "Search specification was malformed. " + m.getMessage(), message.getId() );
    }
    GetCoursesV3Results results = (GetCoursesV3Results)result.getResult();
    logger.log(Level.INFO, "Found {0}", results.getResults().size());
    SeCourseInfoList list = new SeCourseInfoList();
    for ( CourseV2 c : results.getResults() )
      list.add( new SeCourseInfo( c.getExternalId(), c.getName(), c.getDescription() ) );

    ToolMessage tmf = new ToolMessage( message.getId(), SeServerMessageName.CourseInfoList, list );
    sendToolMessage( session, tmf );
  }
  
  
  /**
   * This gets called when a handler throws a HandlerAlertException and decides
   * how to alert the user.
   * 
   * @param session The web socket session.
   * @param haex The exception that was thrown.
   * @throws IOException If the attempt to alert the user fails.
   */
  @Override
  public void processHandlerAlert( Session session, HandlerAlertException haex ) throws IOException
  {
    sendToolMessage( session, new ToolMessage( haex.getMessageId(), SeServerMessageName.Alert, haex.getMessage() ) );    
  }
}
