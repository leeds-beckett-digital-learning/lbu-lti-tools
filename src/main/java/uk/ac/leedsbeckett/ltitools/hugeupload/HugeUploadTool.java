/*
 * Copyright 2025 maber01.
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
package uk.ac.leedsbeckett.ltitools.hugeupload;

import uk.ac.leedsbeckett.ltitools.hugeupload.data.HuStoreCluster;
import uk.ac.leedsbeckett.ltitools.hugeupload.data.Configuration;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import uk.ac.leedsbeckett.lti.claims.LtiClaims;
import uk.ac.leedsbeckett.lti.claims.LtiRoleClaims;
import uk.ac.leedsbeckett.lti.resourcelink.LtiResourceLinkIFrame;
import uk.ac.leedsbeckett.ltitoolset.Tool;
import uk.ac.leedsbeckett.ltitoolset.ToolLaunchState;
import uk.ac.leedsbeckett.ltitoolset.ToolSetLtiState;
import uk.ac.leedsbeckett.ltitoolset.annotations.ToolInstantiationLevel;
import uk.ac.leedsbeckett.ltitoolset.config.PlatformConfiguration;
import uk.ac.leedsbeckett.ltitoolset.deeplinking.DeepLinkingLaunchState;
import uk.ac.leedsbeckett.ltitoolset.websocket.ToolEndpoint;
import uk.ac.leedsbeckett.ltitoolset.annotations.ToolFacet;
import uk.ac.leedsbeckett.ltitoolset.annotations.ToolProperties;

/**
 *
 * @author maber01
 */
@ToolProperties( id = "hugeupload", title = "LBU Huge Upload", defaultFacetId="course" )
@ToolFacet( id = "course", title = "LBU Huge Upload Course Settings", launchURI = "/hugeupload/course.jsp", instantiationLevel = ToolInstantiationLevel.COURSE )
@ToolFacet( id = "item",   title = "LBU Huge Upload", launchURI = "/hugeupload/item.jsp", instantiationLevel = ToolInstantiationLevel.TOOL_RESOURCE )
public class HugeUploadTool extends Tool
{
  static final Logger logger = Logger.getLogger( HugeUploadTool.class.getName() );
  
  
  ServletContext context;
  HuStoreCluster huStore;
  
  @Override
  public boolean usesBlackboardRest()
  {
    return false;
  }

  @Override
  public void init( ServletContext sc )
  {
    this.context = sc;
    huStore = new HuStoreCluster( Paths.get( context.getRealPath( "/WEB-INF/tool/hugeupload/" ) ) );
  }

  public Configuration getPlatformConfig( String platform ) throws IOException
  {
    return huStore.getPlatformConfiguration( platform, true );
  }
  
  public void savePlatformConfig( String platform, Configuration config ) throws IOException
  {
    huStore.updatePlatformConfiguration( platform, config );
  }


  @Override
  public ToolLaunchState supplyToolLaunchState()
  {
    return new HuToolLaunchState();
  }

  /**
   * Initialize the PgaToolLaunchState. It is important to call the super-class
   * to ensure that the tool launch state sub-class fields are set up first.
   * 
   * @param platformConfiguration
   * @param toolstate The tool state that needs initializing.
   * @param lticlaims The validated LTI claims.
   * @param state The general LTI state.
   */
  @Override
  public void initToolLaunchState( PlatformConfiguration platformConfiguration, ToolLaunchState toolstate, LtiClaims lticlaims, ToolSetLtiState state )
  {
    super.initToolLaunchState( platformConfiguration, toolstate, lticlaims, state );
    HuToolLaunchState hustate = (HuToolLaunchState)toolstate;
    if ( lticlaims.getLtiRoles().isInRole( LtiRoleClaims.MEMBERSHIP_INSTRUCTOR_ROLE ) )
      hustate.setAllowedToManage( true );
    // Instructors can be in a group and enter data if they want
    // Perhaps for a 'test' group so they can try things out.
    if ( lticlaims.getLtiRoles().isInRole( LtiRoleClaims.MEMBERSHIP_LEARNER_ROLE) ||
          lticlaims.getLtiRoles().isInRole( LtiRoleClaims.MEMBERSHIP_INSTRUCTOR_ROLE )  )
      hustate.setAllowedToParticipate( true );
  }
  
  
  
  @Override
  public boolean allowDeepLink( String facetid, DeepLinkingLaunchState dlls )
  {
    try
    {
      Configuration c = getPlatformConfig( dlls.getPlatformResourceKey().getPlatformId() );    
      return ( 
               c.isMembershipInstructorDeepLinkPermitted() && 
               dlls.rc.isInRole( LtiRoleClaims.MEMBERSHIP_INSTRUCTOR_ROLE )
             )
             ||
             ( 
               c.isMembershipStudentDeepLinkPermitted() && 
               dlls.rc.isInRole( LtiRoleClaims.MEMBERSHIP_LEARNER_ROLE )
             )
             ||
              dlls.isAllowedToConfigure();
    }
    catch ( IOException ex )
    {
      logger.log( Level.SEVERE, null, ex );
      return false;
    }
  }

  @Override
  public boolean createToolResource( String toolResourceId, String facetId, DeepLinkingLaunchState dlls )
  {
    logger.log(Level.INFO, "facetId = [{0}]", facetId );
    if ( !"item".equals( facetId ) )
    {
      logger.log(Level.INFO, "Failed because not [item] facet");
      return false;
    }

    // Should be saving the toolResourceId to a store along with other tool data.
    logger.log( Level.INFO, "Success" );
    return true;
  }

  
  
  @Override
  public LtiResourceLinkIFrame getDeepLinkingIFrameOptions()
  {
    // Might depend on current configuration and might vary
    // during lifetime of this tool instance.
    return new LtiResourceLinkIFrame( 400, 600, null );
  }

  @Override
  public Class<? extends ToolEndpoint> getEndpointClass()
  {
    return HugeUploadEndpoint.class;
  }
  
  /**
   * Fetch the ResourceStore that this tool will use.
   * 
   * @return The instance.
   */
  public HuStoreCluster getHuStore()
  {
    return huStore;
  }
  
}
