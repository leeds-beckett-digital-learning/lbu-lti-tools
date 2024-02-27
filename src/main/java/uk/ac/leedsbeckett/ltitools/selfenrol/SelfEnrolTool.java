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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import org.apache.commons.io.FileUtils;
import uk.ac.leedsbeckett.lti.claims.LtiClaims;
import uk.ac.leedsbeckett.lti.claims.LtiRoleClaims;
import uk.ac.leedsbeckett.ltitools.mail.MailSender;
import uk.ac.leedsbeckett.ltitools.peergroupassessment.PgaToolLaunchState;
import uk.ac.leedsbeckett.ltitoolset.Tool;
import uk.ac.leedsbeckett.ltitoolset.ToolLaunchState;
import uk.ac.leedsbeckett.ltitoolset.ToolSetLtiState;
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
 * @author maber01
 */
@ToolMapping( id = "selfenrol", type = "system", title = "LBU Self Enrol", launchURI = "/selfenrol/index.jsp" )
public class SelfEnrolTool extends Tool
{
  static final Logger logger = Logger.getLogger(SelfEnrolTool.class.getName() );
  private static final ObjectMapper objectmapper = new ObjectMapper();
  static
  {
    objectmapper.enable( SerializationFeature.INDENT_OUTPUT );
    objectmapper.disable( SerializationFeature.FAIL_ON_EMPTY_BEANS );
    objectmapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
  }

  
  ServletContext context = null;
  SelfEnrolConfiguration config = null;
  MailSender mailSender;  
  
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
    try
    {
      this.context = context;
      String configpath = context.getRealPath( "/WEB-INF/selfenrolconfig.json" );
      String rawconfig = FileUtils.readFileToString( new File( configpath ), StandardCharsets.UTF_8 );
      config = objectmapper.readValue( rawconfig, SelfEnrolConfiguration.class );
      mailSender = new MailSender( config.getSmtpHost(), config.getAdminEmailAddress() );
    }
    catch ( IOException ex )
    {
      logger.log( Level.SEVERE, "Unable to load Self Enrol tool configuration.", ex );
    }
  }
  
  public SelfEnrolConfiguration getConfig()
  {
    return this.config;
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

  public MailSender getMailSender()
  {
    return mailSender;
  }

  @Override
  public boolean allowDeepLink( DeepLinkingLaunchState deepstate )
  {
    return deepstate.rc.isInRole( LtiRoleClaims.SYSTEM_ADMINISTRATOR_ROLE );
  }
}
