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

import uk.ac.leedsbeckett.ltitools.peergroupassessment.*;
import uk.ac.leedsbeckett.ltitools.peergroupassessment.store.StoreCluster;
import java.nio.file.Paths;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import uk.ac.leedsbeckett.lti.claims.LtiClaims;
import uk.ac.leedsbeckett.ltitoolset.Tool;
import uk.ac.leedsbeckett.ltitoolset.ToolLaunchState;
import uk.ac.leedsbeckett.ltitoolset.ToolSetLtiState;
import uk.ac.leedsbeckett.ltitoolset.annotations.ToolMapping;

/**
 * This class helps set up websocket endpoints and page requests based on
 * an LTI launch.
 * 
 * @author maber01
 */
@ToolMapping( name = "selfenrol", type = "system", launchURI = "/selfenrol/index.jsp" )
public class SelfEnrolTool extends Tool
{
  static final Logger logger = Logger.getLogger(SelfEnrolTool.class.getName() );
  
  ServletContext context;

  /**
   * Empty constructor at present.
   */
  public SelfEnrolTool()
  {
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
   * @param toolstate The tool state that needs initializing.
   * @param lticlaims The validated LTI claims.
   * @param state The general LTI state.
   */
  @Override
  public void initToolLaunchState( ToolLaunchState toolstate, LtiClaims lticlaims, ToolSetLtiState state )
  {
    super.initToolLaunchState( toolstate, lticlaims, state );
    SeToolLaunchState sestate = (SeToolLaunchState)toolstate;
    if ( lticlaims.getLtiRoles().isInStandardInstructorRole() )
      sestate.setAllowedToManage( true );
    if ( lticlaims.getLtiRoles().isInStandardLearnerRole() )
      sestate.setAllowedToParticipate( true );
  }
}
