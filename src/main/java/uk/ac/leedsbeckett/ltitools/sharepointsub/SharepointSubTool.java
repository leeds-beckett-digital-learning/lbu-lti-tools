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
package uk.ac.leedsbeckett.ltitools.sharepointsub;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import uk.ac.leedsbeckett.lti.claims.LtiClaims;
import uk.ac.leedsbeckett.lti.claims.LtiRoleClaims;
import uk.ac.leedsbeckett.ltitools.sharepointsub.store.Configuration;
import uk.ac.leedsbeckett.ltitools.sharepointsub.store.CourseSettings;
import uk.ac.leedsbeckett.ltitools.sharepointsub.store.StoreCluster;
import uk.ac.leedsbeckett.ltitools.sharepointsub.tasks.Hourly;
import uk.ac.leedsbeckett.ltitools.sharepointsub.tasks.ScanAllTask;
import uk.ac.leedsbeckett.ltitoolset.Tool;
import uk.ac.leedsbeckett.ltitoolset.ToolCoordinator;
import uk.ac.leedsbeckett.ltitoolset.ToolLaunchState;
import uk.ac.leedsbeckett.ltitoolset.ToolSetLtiState;
import uk.ac.leedsbeckett.ltitoolset.annotations.ToolFacet;
import uk.ac.leedsbeckett.ltitoolset.annotations.ToolInstantiationLevel;
import uk.ac.leedsbeckett.ltitoolset.annotations.ToolProperties;
import uk.ac.leedsbeckett.ltitoolset.config.PlatformConfiguration;
import uk.ac.leedsbeckett.ltitoolset.deeplinking.DeepLinkingLaunchState;
import uk.ac.leedsbeckett.ltitoolset.resources.PlatformCourseKey;
import uk.ac.leedsbeckett.ltitoolset.websocket.ToolEndpoint;

/**
 *
 * @author maber01
 */
@ToolProperties( id = "sharepointsub", title = "LBU Sharepoint Submission", defaultFacetId="course" )
@ToolFacet( id = "config", title = "LBU Sharepoint Submission Configuration", launchURI = "/sharepointsub/config.jsp", instantiationLevel = ToolInstantiationLevel.PLATFORM )
@ToolFacet( id = "course", title = "LBU Sharepoint Submission Course Settings", launchURI = "/sharepointsub/course.jsp", instantiationLevel = ToolInstantiationLevel.COURSE )
public class SharepointSubTool extends Tool
{
  static final Logger logger = Logger.getLogger( SharepointSubTool.class.getName() );
  
  ServletContext context;
  StoreCluster store;

  ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
  Hourly hourlytask = new Hourly();
  
  @Override
  public void init( ServletContext ctx )
  {
    this.context = ctx;
    store = new StoreCluster( Paths.get( context.getRealPath( "/WEB-INF/tool/sharepointsub/" ) ) );
    ScheduledFuture<?> f = scheduler.scheduleAtFixedRate( hourlytask, 5, 10, TimeUnit.MINUTES );
  }

  public Configuration getPlatformConfig( String platform ) throws IOException
  {
    return store.getPlatformConfiguration( platform, true );
  }
  
  public void savePlatformConfig( String platform, Configuration config ) throws IOException
  {
    store.updatePlatformConfiguration( platform, config );
  }

  public CourseSettings getPlatformCourseSettings( PlatformCourseKey key ) throws IOException
  {
    return store.getCourseSettings( key, true );
  }
  
  public void savePlatformCourseSettings( CourseSettings cs ) throws IOException
  {
    store.updateCourseSettings( cs );
  }

  @Override
  public ToolLaunchState supplyToolLaunchState()
  {
    return new SpSubToolLaunchState();
  }

  @Override
  public void initToolLaunchState( PlatformConfiguration platformConfiguration, ToolLaunchState toolstate, LtiClaims lticlaims, ToolSetLtiState state )
  {
    super.initToolLaunchState( platformConfiguration, toolstate, lticlaims, state );
    SpSubToolLaunchState spsubstate = (SpSubToolLaunchState)toolstate;
    if ( lticlaims.getLtiRoles().isInRole( LtiRoleClaims.MEMBERSHIP_INSTRUCTOR_ROLE ) )
      spsubstate.setAllowedToManage( true );
    // Instructors can be in a group and enter data if they want
    // Perhaps for a 'test' group so they can try things out.
    if ( lticlaims.getLtiRoles().isInRole( LtiRoleClaims.MEMBERSHIP_LEARNER_ROLE) ||
          lticlaims.getLtiRoles().isInRole( LtiRoleClaims.MEMBERSHIP_INSTRUCTOR_ROLE )  )
      spsubstate.setAllowedToParticipate( true );
    if ( lticlaims.getLtiLaunchPresentation() != null )
    {
      logger.log(Level.FINE, "Launch presentation claim is present." );
      if ( lticlaims.getLtiLaunchPresentation().getReturnUrl() != null )
      {
        logger.log( Level.FINE, "Launch presentation claim has return URL. {0}", lticlaims.getLtiLaunchPresentation().getReturnUrl() );
        spsubstate.setReturnURL( lticlaims.getLtiLaunchPresentation().getReturnUrl() );
      }
    }
  }

  
  @Override
  public boolean allowDeepLink( String facetID, DeepLinkingLaunchState deepstate )
  {
    return false;
  }

  @Override
  public Class<? extends ToolEndpoint> getEndpointClass()
  {
    return SpSubEndpoint.class;
  }

  @Override
  public boolean usesBlackboardRest()
  {
    return true;
  }
  
  public StoreCluster getSpSubStore()
  {
    return store;
  }
  
  public void runScanAllTask()
  {
    ScanAllTask task = new ScanAllTask( ToolCoordinator.get( context ), store );
    scheduler.schedule( task, 1, TimeUnit.MINUTES );    
  }
}
