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
package uk.ac.leedsbeckett.ltitools.peergroupassessment;

import uk.ac.leedsbeckett.ltitools.peergroupassessment.store.StoreCluster;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import uk.ac.leedsbeckett.lti.claims.LtiClaims;
import uk.ac.leedsbeckett.ltitoolset.ResourceKey;
import uk.ac.leedsbeckett.ltitoolset.Tool;
import uk.ac.leedsbeckett.ltitoolset.ToolLaunchState;
import uk.ac.leedsbeckett.ltitoolset.ToolSetLtiState;
import uk.ac.leedsbeckett.ltitoolset.annotations.ToolMapping;

/**
 *
 * @author maber01
 */
@ToolMapping( name = "peergrpassess", type = "coursecontent", launchURI = "/peergroupassessment/index.jsp" )
public class PeerGroupAssessmentTool extends Tool
{
  static final Logger logger = Logger.getLogger( PeerGroupAssessmentTool.class.getName() );
  
  ServletContext context;
  StoreCluster pgaStore;

  public PeerGroupAssessmentTool()
  {
  }
  
  @Override
  public void init( ServletContext context )
  {
    this.context = context;
    pgaStore = new StoreCluster( Paths.get( context.getRealPath( "/WEB-INF/tool/peergroupassessment/" ) ) );
  }
  
  /**
   * Fetch the application-wide ResourceStore
   * 
   * @return The instance.
   */
  public StoreCluster getPeerGroupAssessmentStore()
  {
    return pgaStore;
  }
  
  @Override
  public ToolLaunchState supplyToolLaunchState( LtiClaims lticlaims, ToolSetLtiState state )
  {
    PgaToolLaunchState pgastate = new PgaToolLaunchState();
    return pgastate;
  }
  
  @Override
  public void initToolLaunchState( ToolLaunchState toolstate, LtiClaims lticlaims, ToolSetLtiState state )
  {
    super.initToolLaunchState( toolstate, lticlaims, state );
    PgaToolLaunchState pgastate = (PgaToolLaunchState)toolstate;
    if ( lticlaims.getLtiRoles().isInStandardInstructorRole() )
      pgastate.setAllowedToManage( true );
    if ( lticlaims.getLtiRoles().isInStandardLearnerRole() )
      pgastate.setAllowedToParticipate( true );
  }
}
