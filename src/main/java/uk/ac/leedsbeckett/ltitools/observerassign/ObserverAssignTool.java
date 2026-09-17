/*
 * Copyright 2026 maber01.
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
package uk.ac.leedsbeckett.ltitools.observerassign;

import uk.ac.leedsbeckett.ltitools.observerassign.store.OaPConfigEntry;
import uk.ac.leedsbeckett.ltitools.observerassign.store.OaPConfigStore;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import uk.ac.leedsbeckett.lti.claims.LtiClaims;
import uk.ac.leedsbeckett.lti.claims.LtiRoleClaims;
import uk.ac.leedsbeckett.ltitoolset.Tool;
import uk.ac.leedsbeckett.ltitoolset.ToolLaunchState;
import uk.ac.leedsbeckett.ltitoolset.ToolSetLtiState;
import uk.ac.leedsbeckett.ltitoolset.annotations.ToolFacet;
import uk.ac.leedsbeckett.ltitoolset.annotations.ToolInstantiationLevel;
import uk.ac.leedsbeckett.ltitoolset.annotations.ToolProperties;
import uk.ac.leedsbeckett.ltitoolset.config.PlatformConfiguration;
import uk.ac.leedsbeckett.ltitoolset.deeplinking.DeepLinkingLaunchState;
import uk.ac.leedsbeckett.ltitoolset.websocket.ToolEndpoint;

/**
 *
 * @author maber01
 */
@ToolProperties( id = "observerassign", title = "LBU Observer Assignment", defaultFacetId = "platform" )
@ToolFacet( id = "platform", title = "LBU Self Enrol", launchURI = "/observerassign/index.jsp", instantiationLevel = ToolInstantiationLevel.PLATFORM )
public class ObserverAssignTool extends Tool
{
  static final Logger logger = Logger.getLogger(ObserverAssignTool.class.getName() );

  ServletContext context;
  OaPConfigStore configstore = null;

  
  @Override
  public boolean usesBlackboardRest()
  {
    return true;
  }
  
  @Override
  public void init( ServletContext context )
  {
    this.context = context;
    this.configstore = new OaPConfigStore( Paths.get( context.getRealPath( "/WEB-INF/tool/selfenrol/platformconfig/" ) ) );
  }

  public ObserverAssignPlatformConfiguration getPlatformConfig( String platform ) throws IOException
  {
    OaPConfigEntry entry = configstore.get( platform, true );
    if ( entry.getConfig() == null )
    {
      entry.setConfig( ObserverAssignPlatformConfiguration.getDefaultConfig() );
      configstore.update( entry );
    }
    return entry.getConfig();
  }
  
  public void savePlatformConfig( String platform, ObserverAssignPlatformConfiguration config ) throws IOException
  {
    OaPConfigEntry entry = configstore.get( platform, true );
    entry.setConfig( config );
    configstore.update( entry );
  }

  
  @Override
  public ToolLaunchState supplyToolLaunchState()
  {
    return new OaToolLaunchState();
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
  public void initToolLaunchState( PlatformConfiguration platformConfiguration, ToolLaunchState toolstate, LtiClaims lticlaims, ToolSetLtiState state )
  {
    super.initToolLaunchState( platformConfiguration, toolstate, lticlaims, state );

    OaToolLaunchState oastate = (OaToolLaunchState)toolstate;
    LtiRoleClaims rc = lticlaims.getLtiRoles();
    if ( rc.isInRole( LtiRoleClaims.INSTITUTION_STUDENT_ROLE  )   )
      oastate.setAllowedToAssignObserver( true );
  }

  @Override
  public Class<? extends ToolEndpoint> getEndpointClass()
  {
    return ObserverAssignEndpoint.class;
  }


  @Override
  public boolean allowDeepLink( String facetId, DeepLinkingLaunchState deepstate )
  {
    //try
    //{
      // ObserverAssignPlatformConfiguration oaconfig = getPlatformConfig( deepstate.getPlatformResourceKey().getPlatformId() );    
      return
             /*
             ( 
               oaconfig.isMembershipInstructorDeepLinkPermitted() && 
               deepstate.rc.isInRole( LtiRoleClaims.MEMBERSHIP_INSTRUCTOR_ROLE )
             )
             ||
             */
             deepstate.isAllowedToConfigure();
    /*
    }
    catch ( IOException ex )
    {
      logger.log( Level.SEVERE, null, ex );
      return false;
    }
    */
  }
    
}
