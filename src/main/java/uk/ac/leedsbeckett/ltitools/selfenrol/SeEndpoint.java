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
import java.util.HashSet;
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
import uk.ac.leedsbeckett.ltitools.mail.MailSender;
import uk.ac.leedsbeckett.ltitoolset.backchannel.JsonResult;
import uk.ac.leedsbeckett.ltitoolset.backchannel.blackboard.BlackboardBackchannel;
import uk.ac.leedsbeckett.ltitoolset.backchannel.blackboard.BlackboardBackchannelKey;
import uk.ac.leedsbeckett.ltitoolset.backchannel.blackboard.data.Availability;
import uk.ac.leedsbeckett.ltitoolset.backchannel.blackboard.data.CourseMembershipV1;
import uk.ac.leedsbeckett.ltitoolset.backchannel.blackboard.data.CourseMembershipV1Input;
import uk.ac.leedsbeckett.ltitoolset.backchannel.blackboard.data.CourseV2;
import uk.ac.leedsbeckett.ltitoolset.backchannel.blackboard.data.GetCoursesV3Results;
import uk.ac.leedsbeckett.ltitoolset.backchannel.blackboard.data.GetUsersV1Results;
import uk.ac.leedsbeckett.ltitoolset.backchannel.blackboard.data.RestExceptionMessage;
import uk.ac.leedsbeckett.ltitoolset.backchannel.blackboard.data.UserV1;
import uk.ac.leedsbeckett.ltitoolset.websocket.ToolMessage;
import uk.ac.leedsbeckett.ltitoolset.websocket.ToolMessageDecoder;
import uk.ac.leedsbeckett.ltitoolset.websocket.ToolMessageEncoder;
import uk.ac.leedsbeckett.ltitoolset.websocket.annotations.EndpointMessageHandler;
import uk.ac.leedsbeckett.ltitoolset.websocket.HandlerAlertException;
import uk.ac.leedsbeckett.ltitoolset.websocket.ToolEndpoint;
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

  static final String[] AUTHTYPES= { 
    "coursedirector", 
    "moduleleader", 
    "admin", 
    "directorpermit",
    "leaderpermit",
    "sysadmin" };
  
  SelfEnrolTool tool;
  SeToolLaunchState seState;

  BlackboardBackchannelKey bbbckey;
  String platformName=null;
        
  // match anything because search spec is set by admin.
  Pattern trainingSearchValidation = Pattern.compile( ".*" ); 

  // Info about this user's most recent search. Used to screen the request
  // to enrol looking for faked course ID.
  final HashSet<String> mostRecentSearchResults = new HashSet<>();
  // Needed to know what role to enrol with.
  String mostRecentScope = "";
  
  String mostRecentEmail = "";
  String mostRecentName = "";
  
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
    
    platformName = getState().getPlatformName();
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
    if ( !seState.isAllowedToParticipate() )
      throw new HandlerAlertException( "You are not permitted to search here.", message.getId() );
    
    mostRecentSearchResults.clear();
    // In case of unsuccessful search clear this:
    mostRecentScope = "";
    
    String scope = search.getScope();
    String specification = search.getSpecification();
    String availability = null;
    boolean org;
    
    Pattern validation;
    String strfilter;
    Pattern filter;
    
    if ( null == scope )
      throw new HandlerAlertException( "Unknown search scope.", message.getId() );

    SelfEnrolConfiguration config = tool.getPlatformConfig( platformName );
    switch ( scope )
    {
      case "course":
        validation = config.getCourseSearchValidation();
        strfilter = config.getCourseSearchFilter().replace( "@", specification );
        org = false;
        break;
      case "organization":
        validation = config.getOrganizationSearchValidation();
        strfilter = config.getOrganizationSearchFilter().replace( "@", specification );
        org = true;
        break;
      case "training":
        specification = config.getTrainingSearchSpecification();
        validation = trainingSearchValidation;
        strfilter = config.getTrainingSearchFilter().replace( "@", specification );
        org = false;
        availability = "Yes";
        break;
      default:
        throw new HandlerAlertException( "Unknown search scope.", message.getId() );
    }

    if ( !validation.matcher( specification ).matches() )
      throw new HandlerAlertException( "The search specification is not valid.", message.getId() );
    
    filter = Pattern.compile( strfilter );
    if ( filter == null )
      throw new HandlerAlertException( "Unable to create a result filter (regular expression).", message.getId() );
    
    BlackboardBackchannel bp = (BlackboardBackchannel)getBackchannel( bbbckey );
    JsonResult result = bp.getV3Courses( specification, org, availability );
    if ( result.getResult() == null )
      throw new HandlerAlertException( "Technical problem running search.", message.getId() );
    if ( !result.isSuccessful() )
    {
      if ( result.getResult() instanceof RestExceptionMessage )
      {
        RestExceptionMessage rem = (RestExceptionMessage)result.getResult();
        throw new HandlerAlertException( "Unable to enrol user. " + rem.getStatus() + " " + rem.getMessage(), message.getId() );
      }
      else
        throw new HandlerAlertException( "Unable to get membership data from the platform. Unknown error.", message.getId() );
    }
    
    GetCoursesV3Results results = (GetCoursesV3Results)result.getResult();
    logger.log(Level.INFO, "Found {0}", results.getResults().size());
    SeCourseInfoList list = new SeCourseInfoList();
    for ( CourseV2 c : results.getResults() )
    {
      // We need to work with the external ID to implement filtering based on it.
      String id = c.getExternalId();
      if ( id == null )
        throw new HandlerAlertException( "No external ID in course results. (Perhaps because user agent lacks permissions.)", message.getId() );
      if ( filter.matcher( id ).matches() )
      {
        list.add( new SeCourseInfo( id, c.getName(), c.getDescription() ) );
        mostRecentSearchResults.add( id );
      }
    }

    // only set this on successful searches.
    mostRecentScope = scope;

    ToolMessage tmf = new ToolMessage( message.getId(), SeServerMessageName.CourseInfoList, list );
    sendToolMessage( session, tmf );
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
  public void handleUserSearch( Session session, ToolMessage message, SeUserSearch search )
          throws IOException, HandlerAlertException
  {
    mostRecentEmail = "";
    mostRecentName = "";
    
    if ( !seState.isAllowedToParticipate() )
      throw new HandlerAlertException( "You are not permitted to search here.", message.getId() );
    
    mostRecentEmail = search.getEmail();
    // SelfEnrolConfiguration config = tool.getPlatformConfig( platformName );
    // ToDo validation of email address using options from configuration

    BlackboardBackchannel bp = (BlackboardBackchannel)getBackchannel( bbbckey );
    JsonResult result = bp.getV1UsersByEmail( mostRecentEmail );
    if ( result.getResult() == null )
      throw new HandlerAlertException( "Technical problem running search.", message.getId() );
    if ( !result.isSuccessful() )
    {
      if ( result.getResult() instanceof RestExceptionMessage )
      {
        RestExceptionMessage rem = (RestExceptionMessage)result.getResult();
        throw new HandlerAlertException( "Unable to find users with email. " + rem.getStatus() + " " + rem.getMessage(), message.getId() );
      }
      else
        throw new HandlerAlertException( "Unable to users by email from the platform. Unknown error.", message.getId() );
    }
    
    GetUsersV1Results results = (GetUsersV1Results)result.getResult();
    if ( !results.getResults().isEmpty() )
    {
      for ( UserV1 user : results.getResults() )
      {
        String fullname = user.getName().getGiven() + " " + user.getName().getFamily();
        logger.info( fullname );
        for ( String role : user.getInstitutionRoleIds() )
          if ( "STAFF".equals( role ) )
          {
            mostRecentName = fullname;
            break;
          }
      }
    }
    
    ToolMessage tmf = new ToolMessage( message.getId(), SeServerMessageName.UserInfo, mostRecentName );
    sendToolMessage( session, tmf );
  }


  
  @EndpointMessageHandler()
  public void handleEnrolRequest( Session session, ToolMessage message, SeEnrolRequest request )
          throws IOException, HandlerAlertException
  {
    if ( !seState.isAllowedToParticipate() )
      throw new HandlerAlertException( "You are not permitted to enrol here.", message.getId() );
    
    String id = request.getCourseId();
    
    if ( StringUtils.isEmpty( id ) )
      throw new HandlerAlertException( "No course ID was received.", message.getId() );

    if ( !this.mostRecentSearchResults.contains(  id ) )
      throw new HandlerAlertException( "Specified course ID was not found in the most recent search results.", message.getId() );

    SelfEnrolConfiguration config = tool.getPlatformConfig( platformName );
    
    String role;
    String emailbody="";
    boolean authneeded=false;
    boolean authnotself=false;
    switch ( mostRecentScope )
    {
      case "course":
        role="Instructor";
        authneeded=true;
        emailbody=config.getCourseEmail();
        break;
      case "organization":
        role="Instructor";
        authneeded=true;
        emailbody=config.getOrganizationEmail();
        break;
      case "training":
        role="Student";
        emailbody="";
        break;
      default:
        throw new HandlerAlertException( "Unknown search scope.", message.getId() );
    }

    if ( authneeded )
    {
      boolean found=false;
      for ( String s : AUTHTYPES )
        if ( s.equals( request.getAuthType() ) )
        {
          found = true;
          break;
        }
      if ( !found )
        throw new HandlerAlertException( "Unknown authorisation type.", message.getId() );
      if ( "directorpermit".equals( request.getAuthType() ) || 
             "leaderpermit".equals( request.getAuthType() ) )
      {
        authnotself=true;
        if ( request.getAuthName() == null || request.getAuthName().trim().length() == 0 )
          throw new HandlerAlertException( "Name of authorising person missing.", message.getId() );
        if ( request.getAuthEmail() == null || request.getAuthEmail().trim().length() == 0 )
          throw new HandlerAlertException( "Email of authorising person missing.", message.getId() );
        if ( !mostRecentName.equals( request.getAuthName() ) )
          throw new HandlerAlertException( "Name of authorising person doesn't match previous user search.", message.getId() );
        if ( !mostRecentEmail.equals( request.getAuthEmail() ) )
          throw new HandlerAlertException( "Email of authorising person doesn't match previous user search.", message.getId() );
        String[] parts = request.getAuthEmail().trim().split( "@" );
        if ( parts.length != 2 )
          throw new HandlerAlertException( "Email seems invalid.", message.getId() );
        if ( !"leedsbeckett.ac.uk".equals( parts[1] ) )
          throw new HandlerAlertException( "Email domain is not \"leedsbeckett.ac.uk\"", message.getId() );
      }
    }
    
    CourseMembershipV1Input cmi = new CourseMembershipV1Input( 
            null, // unspecified child id - will enrol on parent course 
            null, // dataSource (of the new membership object)
            new Availability("Yes"), 
            role );

    BlackboardBackchannel bp = (BlackboardBackchannel)getBackchannel( bbbckey );
    JsonResult result = bp.putV1CourseMemberships( 
            "externalId:" + id, 
            "uuid:" + seState.getPersonId(), 
            cmi );
    if ( result.getResult() == null )
      throw new HandlerAlertException( "Technical problem attempting to enrol.", message.getId() );
    logger.info( result.getResult().getClass().toString() );
    if ( !result.isSuccessful() )
    {
      if ( result.getResult() instanceof RestExceptionMessage )
      {
        RestExceptionMessage rem = (RestExceptionMessage)result.getResult();
        if ( "409".equals( rem.getStatus() ) )
          throw new HandlerAlertException( "You are already enrolled on the course. ", message.getId() );        
        else
          throw new HandlerAlertException( "Unable to enrol user. " + rem.getStatus() + " " + rem.getMessage(), message.getId() );
      }
      else
        throw new HandlerAlertException( "Unable to enrol user. Unknown error.", message.getId() );
    }
    
    // Success so tell client
    CourseMembershipV1 memb = (CourseMembershipV1)result.getResult();  
    ToolMessage tmf = new ToolMessage( message.getId(), SeServerMessageName.EnrolSuccess, new SeEnrolSuccess( memb.getId() ) );
    sendToolMessage( session, tmf );

    if ( StringUtils.isEmpty( emailbody ) )
      return;
    
    // Now send an email...
    result = bp.getV1Users( "uuid:" + seState.getPersonId() );
    if ( result.getResult() == null )
      throw new HandlerAlertException( "Technical problem attempting to find user contact details.", message.getId() );
    logger.info( result.getResult().getClass().toString() );
    if ( !result.isSuccessful() )
    {
      if ( result.getResult() instanceof ServiceStatus )
      {
        ServiceStatus ss = (ServiceStatus)result.getResult();
        logger.log(Level.SEVERE, "Unable to get contact details from the platform. {0} {1}", new Object[ ]{ss.getStatus(), ss.getMessage()});
      }
      else
        logger.severe( "Unable to get contact details from the platform. " );
      return;
    }

    UserV1 user = (UserV1)result.getResult();
    logger.fine( user.getExternalId() );
    logger.fine( user.getContact().getEmail() );

    String reasontext = "-";
    switch ( request.getAuthType() )
    {
      case "coursedirector":  reasontext = "I am course director"; break;
      case "moduleleader":    reasontext = "I am module leader"; break;
      case "admin":           reasontext = "I am course administrator"; break;
      case "directorpermit":  reasontext = "Authorised by course director"; break;
      case "leaderpermit":    reasontext = "Authorised by module leader"; break;
      case "sysadmin":        reasontext = "I am a sys admin"; break;
    }
    
    emailbody = emailbody.replaceAll( "\r", "" );
    emailbody = emailbody.replaceAll( "</p>", "\n" );  // End of para replaced with new line
    emailbody = emailbody.replaceAll( "<[^>]*>", "" ); // All other tags stripped out
    emailbody = emailbody.replaceAll( "&nbsp;", " " ); // nbsp Entity replaced with space.
    emailbody = emailbody.replaceAll( "\\$\\{name}", user.getName().getGiven() + " " + user.getName().getFamily() );
    emailbody = emailbody.replaceAll( "\\$\\{username}", user.getExternalId() );
    emailbody = emailbody.replaceAll( "\\$\\{coursename}", id );    
    emailbody = emailbody.replaceAll( "\\$\\{reason}", request.getAuthType() );    
    emailbody = emailbody.replaceAll( "\\$\\{reasontext}", reasontext );    
    switch ( request.getAuthType() )
    {
      case "directorpermit":
      case "leaderpermit":
        emailbody = emailbody.replaceAll( "\\$\\{authname}", request.getAuthName() );    
        emailbody = emailbody.replaceAll( "\\$\\{authemail}", request.getAuthEmail() );    
        break;
      default:
        emailbody = emailbody.replaceAll( "\\$\\{authname}", "self" );    
        emailbody = emailbody.replaceAll( "\\$\\{authemail}", "-" );    
    }
    
    MailSender sender = tool.getMailSender( platformName );
    sender.processOneEmail( 
            user.getContact().getEmail(),
            authnotself?request.getAuthEmail().trim():null,
            "Automated Message - Staff Self Enrol Tool {" + request.getAuthType() + "}", 
            emailbody,
            "text/plain",
            true
    );
  }

  @EndpointMessageHandler()
  public void handleConfigurationRequest( Session session, ToolMessage message )
          throws IOException, HandlerAlertException
  {
    if ( !seState.isAllowedToConfigure() )
      throw new HandlerAlertException( "Recieved request for configuration from user who is not allowed to configure the tool.", message.getId() );
    
    logger.info( "Fetching config for platform " + platformName );
    SelfEnrolConfiguration config = tool.getPlatformConfig( platformName );
    ToolMessage tmf = new ToolMessage( message.getId(), SeServerMessageName.Configuration, new SeConfigurationMessage( config ) );
    sendToolMessage( session, tmf );
  }
  
  @EndpointMessageHandler()
  public void handleConfigure( Session session, ToolMessage message, SeConfigurationMessage configMessage )
          throws IOException, HandlerAlertException
  {
    if ( !seState.isAllowedToConfigure() )
      throw new HandlerAlertException( "Recieved request to save new configuration from user who is not allowed to configure the tool.", message.getId() );
            
    SelfEnrolConfiguration config = configMessage.getConfiguration();
    if ( config == null )
      throw new HandlerAlertException( "Null configuration was received.", message.getId() );
    
    try
    {  
      tool.savePlatformConfig( platformName, config);
    }
    catch ( Exception e )
    {
      logger.log( Level.SEVERE, "Unable to save configuration for platform " + platformName, e );
      throw new HandlerAlertException( "Unable to save configuration.", message.getId() );
    }
    
    ToolMessage tmf = new ToolMessage( message.getId(), SeServerMessageName.ConfigurationSuccess, "Saved" );
    sendToolMessage( session, tmf );
    
    // To do - send message to all users now accessing tool from the same platform
    // for now just for confirmation to current user.
    ToolMessage tmc = new ToolMessage( message.getId(), SeServerMessageName.Configuration, new SeConfigurationMessage( config ) );
    sendToolMessage( session, tmc );
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
