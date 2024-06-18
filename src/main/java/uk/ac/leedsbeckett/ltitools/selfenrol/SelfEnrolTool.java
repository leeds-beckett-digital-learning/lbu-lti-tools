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
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import uk.ac.leedsbeckett.lti.claims.LtiClaims;
import uk.ac.leedsbeckett.lti.claims.LtiRoleClaims;
import uk.ac.leedsbeckett.ltitools.mail.MailSender;
import uk.ac.leedsbeckett.ltitoolset.Tool;
import uk.ac.leedsbeckett.ltitoolset.ToolLaunchState;
import uk.ac.leedsbeckett.ltitoolset.ToolSetLtiState;
import uk.ac.leedsbeckett.ltitoolset.annotations.ToolFunctionality;
import uk.ac.leedsbeckett.ltitoolset.annotations.ToolInstantiationType;
import uk.ac.leedsbeckett.ltitoolset.annotations.ToolMapping;
import uk.ac.leedsbeckett.ltitoolset.deeplinking.DeepLinkingLaunchState;
import uk.ac.leedsbeckett.ltitoolset.websocket.ToolEndpoint;

/**
 * This class helps set up websocket endpoints and page requests based on
 * an LTI launch.   
 * 
 * Required REST permissions:
 * 
 *  system.course.properties.MODIFY
 *  system.org.properties.MODIFY
 *  course.user-enroll.EXECUTE
 *  course.user.MODIFY
 *  org.user.MODIFY
 * 
 *  system.user.VIEW (to find email address etc.)
 * 
 * @author maber01
 */
@ToolMapping( id = "selfenrol", type = "system", title = "LBU Self Enrol", launchURI = "/selfenrol/index.jsp" )
@ToolFunctionality( instantiationType = ToolInstantiationType.SINGLETON )
public class SelfEnrolTool extends Tool
{
  static final Logger logger = Logger.getLogger(SelfEnrolTool.class.getName() );

  
  ServletContext context = null;
  //MailSender mailSender;  
  SePlatformConfigurationStore configstore = null;
  
  /**
   * Empty constructor at present.
   */
  public SelfEnrolTool()
  {
  }

  @Override
  public boolean usesBlackboardRest()
  {
    return true;
  }
  
  /**
   * This initializes the tool. The toolapi tool coordinator calls this when
   * the server context loads.
   * 
   * @param context The servlet context that is starting.
   */
  @Override
  public void init( ServletContext context )
  {
    this.context = context;
    this.configstore = new SePlatformConfigurationStore( Paths.get( context.getRealPath( "/WEB-INF/tool/selfenrol/platformconfig/" ) ) );
  }
  
  public SelfEnrolConfiguration getPlatformConfig( String platform ) throws IOException
  {
    SePlatformConfigurationEntry entry = configstore.get( platform, true );
    if ( entry.getConfig() == null )
    {
      entry.setConfig( SelfEnrolConfiguration.getDefaultConfig() );
      configstore.update( entry );
    }
    return entry.getConfig();
  }
  
  public void savePlatformConfig( String platform, SelfEnrolConfiguration config ) throws IOException
  {
    SePlatformConfigurationEntry entry = configstore.get( platform, true );
    entry.setConfig( config );
    configstore.update( entry );
  }

  /**
   * Needs to be constructed and perhaps cached per platform..
   * 
   * @return 
   */
  public MailSender getMailSender( String platform ) throws IOException
  {
    SelfEnrolConfiguration config = getPlatformConfig( platform );
    return new MailSender( config.getSmtpHost(), config.getAdminEmailAddress() );
  }
    
  /**
   * Instantiate the PgaToolLaunchState. The API's launch servlet will call
   * this when a new launch occurs and maps onto this tool.
   * 
   * @return The instantiated sub-class of ToolLaunchState.
   */
  @Override
  public ToolLaunchState supplyToolLaunchState()
  {
    SeToolLaunchState sestate = new SeToolLaunchState();
    return sestate;
  }
  
  /**
   * Initialize the PgaToolLaunchState. It is important to call the super-class
   * to ensure that the tool launch state sub-class fields are set up first.
   * 
   * Not that this is a system tool so the LTI Roles needed to decide on 
   * access are System and Institution roles not membership roles.
   * 
   * @param toolstate The tool state that needs initializing.
   * @param lticlaims The validated LTI claims.
   * @param state The general LTI state.
   */
  @Override
  public void initToolLaunchState( ToolLaunchState toolstate, LtiClaims lticlaims, ToolSetLtiState state )
  {
    super.initToolLaunchState( toolstate, lticlaims, state );
    SeToolLaunchState sestate = (SeToolLaunchState)toolstate;
    LtiRoleClaims rc = lticlaims.getLtiRoles();
    if ( rc.isInRole( LtiRoleClaims.SYSTEM_ADMINISTRATOR_ROLE ) )
      sestate.setAllowedToManage( true );
    if ( rc.isInRole( LtiRoleClaims.SYSTEM_ADMINISTRATOR_ROLE ) ||
         rc.isInRole( LtiRoleClaims.INSTITUTION_STAFF_ROLE    ) ||
         rc.isInRole( LtiRoleClaims.INSTITUTION_FACULTY_ROLE  )    )
      sestate.setAllowedToParticipate( true );
  }

  @Override
  public Class<? extends ToolEndpoint> getEndpointClass()
  {
    return SeEndpoint.class;
  }


  @Override
  public boolean allowDeepLink( DeepLinkingLaunchState deepstate )
  {
    try
    {
      SelfEnrolConfiguration sec = getPlatformConfig( deepstate.getResourceKey().getPlatformId() );    
      return ( 
               sec.isMembershipInstructorDeepLinkPermitted() && 
               deepstate.rc.isInRole( LtiRoleClaims.MEMBERSHIP_INSTRUCTOR_ROLE )
             )
             ||
             deepstate.rc.isInRole( LtiRoleClaims.SYSTEM_ADMINISTRATOR_ROLE );
    }
    catch ( IOException ex )
    {
      Logger.getLogger( SelfEnrolTool.class.getName() ).log( Level.SEVERE, null, ex );
      return false;
    }
  }
}
