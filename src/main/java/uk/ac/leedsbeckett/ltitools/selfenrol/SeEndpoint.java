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
import java.util.regex.Pattern;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import org.apache.commons.lang3.StringUtils;
import uk.ac.leedsbeckett.lti.services.data.ServiceStatus;
import uk.ac.leedsbeckett.ltitoolset.backchannel.JsonResult;
import uk.ac.leedsbeckett.ltitoolset.backchannel.blackboard.BlackboardBackchannel;
import uk.ac.leedsbeckett.ltitoolset.backchannel.blackboard.BlackboardBackchannelKey;
import uk.ac.leedsbeckett.ltitoolset.backchannel.blackboard.data.Availability;
import uk.ac.leedsbeckett.ltitoolset.backchannel.blackboard.data.CourseMembershipV1;
import uk.ac.leedsbeckett.ltitoolset.backchannel.blackboard.data.CourseMembershipV1Input;
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

  BlackboardBackchannelKey bbbckey;
  
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
    bbbckey = new BlackboardBackchannelKey( getPlatformHost() );
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
    String scope = search.getScope();
    String specification = search.getSpecification();
    boolean org;
    
    Pattern validation;
    String strfilter;
    Pattern filter;
    
    if ( null == scope )
      throw new HandlerAlertException( "Unknown search scope.", message.getId() );
    else switch ( scope )
    {
      case "course":
        validation = tool.getConfig().getCourseSearchValidation();
        strfilter = tool.getConfig().getCourseSearchFilter().replace( "@", specification );
        org = false;
        break;
      case "organization":
        validation = tool.getConfig().getOrganizationSearchValidation();
        strfilter = tool.getConfig().getOrganizationSearchFilter().replace( "@", specification );
        org = true;
        break;
      default:
        throw new HandlerAlertException( "Unknown search scope.", message.getId() );
    }

    if ( !validation.matcher( specification ).matches() )
      throw new HandlerAlertException( "The search specification is not valid.", message.getId() );
    
    filter = Pattern.compile( strfilter.replace( "@", Pattern.quote( specification ) ) );
    
    BlackboardBackchannel bp = (BlackboardBackchannel)getBackchannel( bbbckey );
    JsonResult result = bp.getV3Courses( specification, org );
    if ( result.getResult() == null )
      throw new HandlerAlertException( "Technical problem running search.", message.getId() );
    if ( !result.isSuccessful() )
    {
      if ( result.getResult() instanceof ServiceStatus )
      {
        ServiceStatus ss = (ServiceStatus)result.getResult();
        throw new HandlerAlertException( "Unable to get membership data from the platform. " + ss.getStatus() + " " + ss.getMessage(), message.getId() );
      }
      else
        throw new HandlerAlertException( "Unable to get membership data from the platform. Unknown error.", message.getId() );
    }
    
    GetCoursesV3Results results = (GetCoursesV3Results)result.getResult();
    logger.log(Level.INFO, "Found {0}", results.getResults().size());
    SeCourseInfoList list = new SeCourseInfoList();
    for ( CourseV2 c : results.getResults() )
    {
      String id = c.getExternalId();
      if ( filter.matcher( id ).matches() )
        list.add( new SeCourseInfo( id, c.getName(), c.getDescription() ) );
    }

    ToolMessage tmf = new ToolMessage( message.getId(), SeServerMessageName.CourseInfoList, list );
    sendToolMessage( session, tmf );
  }
  
  @EndpointMessageHandler()
  public void handleEnrolRequest( Session session, ToolMessage message, SeEnrolRequest request )
          throws IOException, HandlerAlertException
  {
    String id = request.getCourseId();
    
    if ( StringUtils.isEmpty( id ) )
      throw new HandlerAlertException( "No course ID was received.", message.getId() );
    
    CourseMembershipV1Input cmi = new CourseMembershipV1Input( 
            null, null, new Availability("Yes"), "Instructor" );

    BlackboardBackchannel bp = (BlackboardBackchannel)getBackchannel( bbbckey );
    JsonResult result = bp.putV1CourseMemberships( id, seState.getPersonId(), cmi );
    if ( result.getResult() == null )
      throw new HandlerAlertException( "Technical problem attempting to enrol.", message.getId() );
    logger.info( result.getResult().getClass().toString() );
    if ( !result.isSuccessful() )
    {
      if ( result.getResult() instanceof ServiceStatus )
      {
        ServiceStatus ss = (ServiceStatus)result.getResult();
        throw new HandlerAlertException( "Unable to get membership data from the platform. " + ss.getStatus() + " " + ss.getMessage(), message.getId() );
      }
      else
        throw new HandlerAlertException( "Unable to get membership data from the platform. Unknown error.", message.getId() );
    }
    
    CourseMembershipV1 memb = (CourseMembershipV1)result.getResult();
            
    ToolMessage tmf = new ToolMessage( message.getId(), SeServerMessageName.EnrolSuccess, new SeEnrolSuccess( memb.getId() ) );
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
