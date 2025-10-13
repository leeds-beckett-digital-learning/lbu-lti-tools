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
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.leedsbeckett.jesharepoint.AccessRoleEnum;
import uk.ac.leedsbeckett.jesharepoint.Sharepoint;
import uk.ac.leedsbeckett.jesharepoint.odata.containers.EntityCollection;
import uk.ac.leedsbeckett.jesharepoint.sptypes.SpFolder;
import uk.ac.leedsbeckett.jesharepoint.sptypes.SpGroup;
import uk.ac.leedsbeckett.jesharepoint.sptypes.SpUser;
import uk.ac.leedsbeckett.ltitools.sharepointsub.bbdata.CourseDropboxNames;
import uk.ac.leedsbeckett.ltitools.sharepointsub.bbdata.ModuleData;
import uk.ac.leedsbeckett.ltitools.sharepointsub.bbdata.ModulePerson;
import uk.ac.leedsbeckett.ltitools.sharepointsub.bbdata.PlatformData;
import uk.ac.leedsbeckett.ltitools.sharepointsub.bbdata.SitePerson;
import uk.ac.leedsbeckett.ltitools.sharepointsub.store.Configuration;
import uk.ac.leedsbeckett.ltitools.sharepointsub.store.CourseSettings;
import uk.ac.leedsbeckett.ltitools.sharepointsub.store.Deadline;
import uk.ac.leedsbeckett.ltitools.sharepointsub.store.DeadlineUsage;
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
public class UpdateDeadlineTask implements Runnable, BackchannelOwner
{
  static final Logger logger = Logger.getLogger(UpdateDeadlineTask.class.getName() );

  ToolCoordinator toolCoordinator;
  StoreCluster store;
  long updatingDeadline;
  
  public UpdateDeadlineTask( ToolCoordinator toolCoordinator, StoreCluster store, long updatingDeadline )
  {
    this.toolCoordinator = toolCoordinator;
    this.store = store;
    this.updatingDeadline = updatingDeadline;
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
        Logger.getLogger(UpdateDeadlineTask.class.getName() ).log( Level.SEVERE, null, ex );
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
    for ( ModuleData md : pd.modules )
      processModule( pd, md ); 
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
       
    ArrayList<CourseSettings> csList = new ArrayList<>();
    for ( PlatformCourseKey key : store.getAllPlatformCourseKeys( platformid ) )
    {
      CourseSettings cs = loadCourseSettings( pd, key );
      if ( cs != null )
        csList.add( cs );
    }

    // Now load additional data from Blackboard using backchannel
    for ( CourseSettings cs : csList )
      loadPlatformCourse( bb, pd, cs );
    
    return pd;
  }

  private CourseSettings loadCourseSettings( PlatformData pd, PlatformCourseKey key )
  {
    CourseSettings courseSettings = store.getCourseSettings( key, false );
    if ( courseSettings == null ) return null;
    if ( !courseSettings.isEnabled() ) return null;
    if ( courseSettings.getDropboxMap().isEmpty() ) return null;
    ZoneId zid = courseSettings.getZoneId();
    for ( Dropbox d : courseSettings.getDropboxMap().values() )
    {
      logger.log(Level.INFO, "Checking " + key.getCourseId() + "  " + d.getName() );
      logger.log(Level.INFO, "Default deadline " + d.getDefaultDeadline().toString() );
      long dl = d.getDefaultDeadline().toEpochMilli( zid );
      if ( this.updatingDeadline == dl )
      {
        pd.addDeadlineUse( dl, key.getCourseId(), d.getName() );
        return courseSettings;
      }
      
      for ( Deadline deadline : d.getStudentDeadlineMap().values() )
      {
        logger.log(Level.INFO, "Student deadline " + deadline.toString() );
        dl = deadline.toEpochMilli( zid );
        if ( this.updatingDeadline == dl )
        {
          pd.addDeadlineUse( dl, key.getCourseId(), d.getName() );
          return courseSettings;
        }
      }
    }
    // This course is not of interest
    return null;
  }
  
  private void loadPlatformCourse( BlackboardBackchannel bb, PlatformData pd, CourseSettings courseSettings )
  {    
    logger.info( "    CourseID = " + courseSettings.getKey().getCourseId() );
    JsonResult result = bb.getV3Course( "uuid:" + courseSettings.getKey().getCourseId() );
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
        
    result = bb.getV1CourseUsers( courseSettings.getKey().getCourseId(), null );
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
  
  public void processModule( PlatformData pd, ModuleData md )
  {
    try
    {
      String modulefolderurl = "/sites/HugeFileSubmission/Shared Documents/modules/" + md.modulename;
      md.folder = pd.sp.getOrCreateFolder( modulefolderurl );
      logger.info( "Found module folder " + md.folder.ServerRelativeUrl );
      for ( Dropbox dropbox : md.courseSettings.getDropboxMap().values() )
        processDropBox( pd, md, dropbox );
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
      for ( ModulePerson person : md.persons )
      {
        long pdead = dropbox.getPersonalDeadline( person.getEmail() ).toEpochMilli( md.courseSettings.getZoneId() );
        if ( person.isStudent() && this.updatingDeadline == pdead )
          processStudentFolder( pd, md, dropbox, folder, person );
      }
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
      
      logger.info( "person " + person.getEmail() + " deadline passed." );
      AccessRoleEnum access = AccessRoleEnum.VIEW;
      
      pd.sp.setFolderRoleAssignments( folder, sitePerson.getUser().Id, access );
      logger.info( "Changed student access on student folder " + folder.ServerRelativeUrl );
    }
    catch ( IOException | URISyntaxException ex )
    {
      logger.log( Level.SEVERE, null, ex );
    }
  }

  
}
