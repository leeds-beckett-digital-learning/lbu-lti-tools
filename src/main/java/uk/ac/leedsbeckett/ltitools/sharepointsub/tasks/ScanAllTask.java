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
package uk.ac.leedsbeckett.ltitools.sharepointsub.tasks;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.leedsbeckett.jesharepoint.Sharepoint;
import uk.ac.leedsbeckett.jesharepoint.sptypes.SpGroup;
import uk.ac.leedsbeckett.jesharepoint.sptypes.SpUser;
import uk.ac.leedsbeckett.ltitools.sharepointsub.bbdata.ModuleData;
import uk.ac.leedsbeckett.ltitools.sharepointsub.bbdata.ModulePerson;
import uk.ac.leedsbeckett.ltitools.sharepointsub.bbdata.PlatformData;
import uk.ac.leedsbeckett.ltitools.sharepointsub.bbdata.SitePerson;
import uk.ac.leedsbeckett.ltitools.sharepointsub.store.Configuration;
import uk.ac.leedsbeckett.ltitools.sharepointsub.store.CourseSettings;
import uk.ac.leedsbeckett.ltitools.sharepointsub.store.StoreCluster;
import uk.ac.leedsbeckett.ltitoolset.ToolCoordinator;
import uk.ac.leedsbeckett.ltitoolset.backchannel.BackchannelOwner;
import uk.ac.leedsbeckett.ltitoolset.backchannel.JsonResult;
import uk.ac.leedsbeckett.ltitoolset.backchannel.blackboard.*;
import uk.ac.leedsbeckett.ltitoolset.backchannel.blackboard.data.*;
import uk.ac.leedsbeckett.ltitoolset.resources.PlatformCourseKey;

/**
 *
 * @author maber01
 */
public class ScanAllTask implements Runnable, BackchannelOwner
{
  static final Logger logger = Logger.getLogger( ScanAllTask.class.getName() );

  ToolCoordinator toolCoordinator;
  StoreCluster store;

  public ScanAllTask( ToolCoordinator toolCoordinator, StoreCluster store )
  {
    this.toolCoordinator = toolCoordinator;
    this.store = store;
  }
  
  private static String getPlatformHost( String platform )
  {
    try
    {
      URL url = new URL( platform );
      return url.getHost();
    }
    catch ( Exception e )
    {
      logger.log( Level.SEVERE, "Unable to find host name in platform ID.", e );
    }
    return null;
  }
  
  @Override
  public void run()
  {
    logger.info( "Scan all task running." );
    for ( String platformid : store.getAllPlatformKeys() )
    {
      BlackboardBackchannel bb = null;
      try
      {
        logger.info( "Platformid = " + platformid );
        Configuration config = store.getPlatformConfiguration( platformid, false );
        if ( config == null ) continue;
        if ( !config.isEnabled() ) continue;
        
        String platformHost = getPlatformHost( platformid );
        logger.info( "Platform host = " + platformHost );
        if ( platformHost == null ) continue;
        
        BlackboardBackchannelKey bbbckey = new BlackboardBackchannelKey( platformHost );
        
        // state can be null for Blackboard
        bb = (BlackboardBackchannel)toolCoordinator.getBackchannel( this, bbbckey, null );  
        if ( bb == null ) continue;
        bb.addOwner( this );
        processPlatform( bb, platformid );
      }
      catch ( IOException ex )
      {
        Logger.getLogger( ScanAllTask.class.getName() ).log( Level.SEVERE, null, ex );
      }
      finally
      {
        bb.removeOwner( this );        
      }
    }
    logger.info( "Scan all task complete." );
  }
  
  private void processPlatform( BlackboardBackchannel bb, String platformid )
  {
    PlatformData pd = loadPlatform( bb, platformid ); 
    if ( pd == null ) return;
    processSiteMembersGroup( pd );
    
    logger.info( "Platform persons: " );
    for ( SitePerson sp : pd.sitepersonmapbyemail.values() )
    {
      logger.log(Level.INFO, "    bbid: {0} email: {1}", 
              new Object[ ]{sp.getBbId(), sp.getEmail()});
      if ( sp.getUser() != null )
        logger.log(Level.INFO, "        SP user title: {0} LoginName: {1}", 
              new Object[ ]{sp.getUser().Title, sp.getUser().LoginName  });
    }
    logger.info( "Platform modules: " );
    for ( ModuleData md : pd.modules )
    {
      logger.log(Level.INFO, "    name: {0}", md.modulename);
      for ( ModulePerson mp : md.persons )
      {
        logger.log(Level.INFO, "        name: {0} email: {1} role: {2}", 
                new Object[ ]{mp.getBbId(), mp.getEmail(), mp.getRole() } );
      }
    }
  }
  
  private PlatformData loadPlatform( BlackboardBackchannel bb, String platformid )
  {
    PlatformData pd = new PlatformData();
    pd.sharepointSettings = store.getPlatformSharepointSettings( platformid );
    if ( pd.sharepointSettings == null )
    {
      logger.log(Level.SEVERE, "Unable to load sharepoint settings for platform ", platformid );
      return null;
    }
    pd.sp = new Sharepoint( pd.sharepointSettings );
       
    for ( PlatformCourseKey key : store.getAllPlatformCourseKeys( platformid ) )      
      loadPlatformCourse( bb, pd, key );
    
    loadEmailAddresses( bb, pd );
    
    return pd;
  }

  private void loadPlatformCourse( BlackboardBackchannel bb, PlatformData pd, PlatformCourseKey key )
  {
    CourseSettings courseSettings = store.getCourseSettings( key, false );
    if ( courseSettings == null ) return;
    if ( !courseSettings.isEnabled() ) return;
    if ( courseSettings.getDropboxMap().isEmpty() ) return;
    
    logger.info( "    CourseID = " + key.getCourseId() );
    JsonResult result = bb.getV3Course( "uuid:" + key.getCourseId() );
    if ( !result.isSuccessful() )
      return;

    CourseV2 course = (CourseV2) result.getResult();
    if ( course == null )
      return;
    
    logger.info( course.getCourseId() );
    ModuleData md = new ModuleData();
    md.modulename = course.getCourseId();
    pd.modules.add( md );
        
    result = bb.getV1CourseUsers( key.getCourseId(), null );
    if ( !result.isSuccessful() )
      return;

    GetCourseUsersV1Results courseUsersResult = (GetCourseUsersV1Results) result.getResult();
    for ( CourseMembershipV1 member : courseUsersResult.getResults() )
    {
      logger.log(Level.INFO, "        User ID = {0}", member.getUserId());
      ModulePerson mperson;
      if ( "Instructor".equals( member.getCourseRoleId() ) || 
              "Student".equals( member.getCourseRoleId() ) )
      {
        mperson = new ModulePerson();
        mperson.setBbId( member.getUserId() );
        mperson.setRole( "Student".equals( member.getCourseRoleId() )?"Student":"Marker" );
        md.persons.add( mperson );
        if ( !pd.sitepersonmapbybbid.containsKey( member.getUserId() ) )
        {
          SitePerson sp = new SitePerson();
          sp.setBbId( member.getUserId() );
          pd.sitepersonmapbybbid.put( member.getUserId(), sp );
        }
      }
    }
  }  
  
  private void loadEmailAddresses( BlackboardBackchannel bb, PlatformData pd )
  {
    logger.info( "    Fetching all the email addresses:" );
    for ( SitePerson sp : pd.sitepersonmapbybbid.values() )
    {
      logger.info( "        User ID = " + sp.getBbId() );
      JsonResult resultU = bb.getV1Users( sp.getBbId() );
      if ( resultU.getResult() != null )
      {
        UserV1 bbuser = (UserV1)resultU.getResult();
        String email = bbuser.getContact().getEmail();
        logger.info( "            email = " + email );
        sp.setEmail( email );
        if ( !pd.sitepersonmapbyemail.containsKey( email ) )
          pd.sitepersonmapbyemail.put( email, sp );
      }
    }
    for ( ModuleData md : pd.modules )
      for ( ModulePerson mp : md.persons )
        mp.setEmail( pd.sitepersonmapbybbid.get( mp.getBbId() ).getEmail() );
  }  
  
  public void processSiteMembersGroup( PlatformData pd )
  {
    try
    {
      SpGroup siteMembersGroup = pd.sp.getGroupWithUsers( pd.sharepointSettings.getMembersGroupName() );
      if ( siteMembersGroup.Users.isDeferred() )
      {
        return;
      }
      for ( int i=0; i<siteMembersGroup.Users.size(); i++ )
      {
        SpUser u = siteMembersGroup.Users.getEntity( i );
        logger.info( "Found " + u.Email );
        if ( pd.sitepersonmapbyemail.containsKey( u.UserPrincipalName ) )
          pd.sitepersonmapbyemail.get( u.UserPrincipalName ).setUser( u );
      }
      
      for ( SitePerson person : pd.sitepersonmapbyemail.values() )
        if ( person.getUser() == null )
          person.setUser( pd.sp.createGroupUser( siteMembersGroup, person.getEmail() ) );
    }
    catch ( IOException | URISyntaxException ex )
    {
      logger.log( Level.SEVERE, null, ex );
    }
  }
  
}
