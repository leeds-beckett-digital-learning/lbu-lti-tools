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
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.leedsbeckett.jesharepoint.AccessRoleEnum;
import uk.ac.leedsbeckett.jesharepoint.Sharepoint;
import uk.ac.leedsbeckett.jesharepoint.odata.containers.EntityCollection;
import uk.ac.leedsbeckett.jesharepoint.sptypes.SpFolder;
import uk.ac.leedsbeckett.jesharepoint.sptypes.SpGroup;
import uk.ac.leedsbeckett.jesharepoint.sptypes.SpUser;
import uk.ac.leedsbeckett.ltitools.sharepointsub.bbdata.ModuleData;
import uk.ac.leedsbeckett.ltitools.sharepointsub.bbdata.ModulePerson;
import uk.ac.leedsbeckett.ltitools.sharepointsub.bbdata.PlatformData;
import uk.ac.leedsbeckett.ltitools.sharepointsub.bbdata.SitePerson;
import uk.ac.leedsbeckett.ltitools.sharepointsub.store.Configuration;
import uk.ac.leedsbeckett.ltitools.sharepointsub.store.CourseSettings;
import uk.ac.leedsbeckett.ltitools.sharepointsub.store.Deadline;
import uk.ac.leedsbeckett.ltitools.sharepointsub.store.Dropbox;
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
    long start = System.currentTimeMillis();
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
    long end = System.currentTimeMillis();
    logger.info( "Scan all task complete after " + (end-start) + "ms" );
  }
  
  private void processPlatform( BlackboardBackchannel bb, String platformid )
  {
    PlatformData pd = loadPlatform( bb, platformid ); 
    if ( pd == null ) return;
    
    // Now do stuff in sharepoint
    processSiteMembersGroup( pd );
    for ( ModuleData md : pd.modules )
      processModule( pd, md ); 
    
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
    md.courseSettings = courseSettings;
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
        String email = bbuser.getContact().getEmail().toLowerCase();
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

  public void processModule( PlatformData pd, ModuleData md )
  {
    try
    {
      String modulefolderurl = "/sites/HugeFileSubmission/Shared Documents/modules/" + md.modulename;
      md.folder = pd.sp.getOrCreateFolder( modulefolderurl );
      logger.info( "Found module folder " + md.folder.ServerRelativeUrl );
      
      Boolean hasUnique = pd.sp.getFolderItemBooleanProperty( "HasUniqueRoleAssignments", md.folder ); 
      if ( Boolean.FALSE.equals( hasUnique ) )
        pd.sp.setFolderUniqueRoleAssignments( md.folder, true );
      
      processModuleGroups( pd, md );

      logger.info( "Set access on module folder " + md.folder.ServerRelativeUrl );
      pd.sp.setFolderRoleAssignments( md.folder, md.groups[0].Id, AccessRoleEnum.VIEW);
      pd.sp.setFolderRoleAssignments( md.folder, md.groups[1].Id, AccessRoleEnum.VIEW);

      for ( Dropbox dropbox : md.courseSettings.getDropboxMap().values() )
        processDropBox( pd, md, dropbox );
    }
    catch ( IOException | URISyntaxException ex )
    {
      logger.log( Level.SEVERE, null, ex );
    }
  }

  public void processModuleGroups( PlatformData pd, ModuleData md )
  {
    try
    {
      String[] roles = { "Students", "Markers" };
      for ( int i=0; i<roles.length; i++ )
      {
        HashSet<String> wantedSet = new HashSet<>();
        for ( ModulePerson person : md.persons )
          if ( (i==0 && person.isStudent() ) || (i==1 && person.isMarker() ) )
            wantedSet.add( person.getEmail() );
        
        String groupName = pd.sharepointSettings.getModuleGroupPrefix() + md.modulename + " " + roles[i];
        md.groups[i] = pd.sp.getOrCreateGroup( groupName );
        logger.info( "Working on group " + md.groups[i].Title );
        
        EntityCollection<SpUser> users = pd.sp.getGroupMembers( md.groups[i] );
        HashSet<String> currentSet = new HashSet<>();
        for ( SpUser u : users.getEntities() )
          currentSet.add( u.UserPrincipalName );

        HashSet<String> toAddSet = new HashSet<>( wantedSet );
        toAddSet.removeAll( currentSet );

        // Removing users from groups not implemented (yet).
        //HashSet<String> toRemoveSet = new HashSet<>( currentSet );
        //toRemoveSet.removeAll( wantedSet );        
        for ( String email : toAddSet )
          pd.sp.createGroupUser( md.groups[i], email );
      }
    }
    catch ( IOException | URISyntaxException ex )
    {
      logger.log( Level.SEVERE, null, ex );
    }
  }
  
  public void processDropBox( PlatformData pd, ModuleData md, Dropbox dropbox )
  {
    try
    {
      String dropboxfolderurl = md.folder.ServerRelativeUrl + "/" + dropbox.getName();
      SpFolder folder = pd.sp.getOrCreateFolder( dropboxfolderurl );
      logger.info( "Found dropbox " + folder.ServerRelativeUrl );
      Boolean hasUnique = pd.sp.getFolderItemBooleanProperty( "HasUniqueRoleAssignments", folder );
      if ( Boolean.TRUE.equals( hasUnique ) )
        pd.sp.setFolderUniqueRoleAssignments( folder, false );
      for ( ModulePerson person : md.persons )
        if ( person.isStudent() )
          processStudentFolder( pd, md, dropbox, folder, person );
    }
    catch ( IOException | URISyntaxException ex )
    {
      logger.log( Level.SEVERE, null, ex );
    }
  }

  public void processStudentFolder( PlatformData pd, ModuleData md, Dropbox dropbox, SpFolder parentFolder, ModulePerson person )
  {
    try
    {
      SitePerson sitePerson = pd.sitepersonmapbyemail.get( person.getEmail() );
      if ( sitePerson == null )
      {
        logger.warning( "Unable to find sharepoint person for email " + person.getEmail() );
        return;
      }
      if ( sitePerson.getUser() == null )
      {
        logger.warning( "Sharepoint person has no SpUser object for email " + person.getEmail() );
        return;
      }
      String studentfolderurl = parentFolder.ServerRelativeUrl + "/" + person.getName();
      SpFolder folder = pd.sp.getOrCreateFolder( studentfolderurl );
      Boolean hasUnique = pd.sp.getFolderItemBooleanProperty( "HasUniqueRoleAssignments", folder );
      if ( Boolean.FALSE.equals( hasUnique ) )
        pd.sp.setFolderUniqueRoleAssignments( folder, true );
      logger.info( "Set unique access on student folder " + folder.ServerRelativeUrl );
      // Access level based on deadline.
      Deadline personalDeadline = dropbox.getPersonalDeadline( person.getEmail() );
      logger.info( "person " + person.getEmail() + " with deadline " + personalDeadline.toString() );
      long lDeadline = personalDeadline.toEpochMilli( md.courseSettings.getZoneId() );
      long now = System.currentTimeMillis();
      AccessRoleEnum access = (now < lDeadline) ? AccessRoleEnum.EDIT : AccessRoleEnum.VIEW;
      
      pd.sp.setFolderRoleAssignments( folder, sitePerson.getUser().Id, access );
      logger.info( "Added student to access on student folder " + folder.ServerRelativeUrl );
      pd.sp.setFolderRoleAssignments( folder, md.groups[1].Id, AccessRoleEnum.VIEW );
      logger.info( "Added markers to access on student folder " + folder.ServerRelativeUrl );
    }
    catch ( IOException | URISyntaxException ex )
    {
      logger.log( Level.SEVERE, null, ex );
    }
  }

  
}
