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

import uk.ac.leedsbeckett.ltitools.peergroupassessment.resourcedata.PgaProperties;
import uk.ac.leedsbeckett.ltitools.peergroupassessment.resourcedata.PeerGroupResource;
import uk.ac.leedsbeckett.ltitools.peergroupassessment.store.StoreCluster;
import uk.ac.leedsbeckett.ltitools.peergroupassessment.inputdata.PeerGroupData;
import uk.ac.leedsbeckett.ltitools.peergroupassessment.formdata.PeerGroupForm;
import uk.ac.leedsbeckett.ltitools.peergroupassessment.messagedata.PgaChangeGroup;
import uk.ac.leedsbeckett.ltitools.peergroupassessment.messagedata.PgaAddMembership;
import uk.ac.leedsbeckett.ltitools.peergroupassessment.inputdata.PeerGroupDataKey;
import uk.ac.leedsbeckett.ltitools.peergroupassessment.messagedata.PgaChangeDatum;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import uk.ac.leedsbeckett.lti.services.LtiServiceScope;
import uk.ac.leedsbeckett.lti.services.LtiServiceScopeSet;
import uk.ac.leedsbeckett.lti.services.ags.LtiAssessmentAndGradesServiceClaim;
import uk.ac.leedsbeckett.lti.services.ags.data.LineItem;
import uk.ac.leedsbeckett.lti.services.ags.data.LineItems;
import uk.ac.leedsbeckett.lti.services.ags.data.Score;
import uk.ac.leedsbeckett.lti.services.data.ServiceStatus;
import uk.ac.leedsbeckett.lti.services.nrps.LtiNamesRoleServiceClaim;
import uk.ac.leedsbeckett.lti.services.nrps.data.NrpsMembershipContainer;
import uk.ac.leedsbeckett.lti.services.nrps.data.NrpsMember;
import uk.ac.leedsbeckett.ltitools.peergroupassessment.blackboard.BlackboardGroup;
import uk.ac.leedsbeckett.ltitools.peergroupassessment.blackboard.BlackboardGroupSet;
import uk.ac.leedsbeckett.ltitools.peergroupassessment.blackboard.BlackboardGroupSetImportPlan;
import uk.ac.leedsbeckett.ltitools.peergroupassessment.blackboard.BlackboardGroupSets;
import uk.ac.leedsbeckett.ltitools.peergroupassessment.formdata.PeerGroupForm.Field;
import uk.ac.leedsbeckett.ltitools.peergroupassessment.inputdata.ParticipantData;
import uk.ac.leedsbeckett.ltitools.peergroupassessment.inputdata.ParticipantDatum;
import uk.ac.leedsbeckett.ltitoolset.websocket.MultitonToolEndpoint;
import uk.ac.leedsbeckett.ltitoolset.websocket.ToolMessage;
import uk.ac.leedsbeckett.ltitoolset.websocket.ToolMessageDecoder;
import uk.ac.leedsbeckett.ltitoolset.websocket.ToolMessageEncoder;
import uk.ac.leedsbeckett.ltitoolset.websocket.annotations.EndpointMessageHandler;
import uk.ac.leedsbeckett.ltitools.peergroupassessment.messagedata.Id;
import uk.ac.leedsbeckett.ltitools.peergroupassessment.messagedata.PgaConfigurationMessage;
import uk.ac.leedsbeckett.ltitools.peergroupassessment.messagedata.PgaDataList;
import uk.ac.leedsbeckett.ltitools.peergroupassessment.messagedata.PgaEndorseData;
import uk.ac.leedsbeckett.ltitools.peergroupassessment.messagedata.PgaScoreProgress;
import uk.ac.leedsbeckett.ltitools.peergroupassessment.predicate.AllowedToSeeGroupData;
import uk.ac.leedsbeckett.ltitools.peergroupassessment.resourcedata.PeerGroupResource.Group;
import uk.ac.leedsbeckett.ltitools.peergroupassessment.resourcedata.PeerGroupResource.Member;
import uk.ac.leedsbeckett.ltitools.peergroupassessment.resourcedata.Stage;
import uk.ac.leedsbeckett.ltitools.peergroupassessment.store.Configuration;
import uk.ac.leedsbeckett.ltitoolset.backchannel.JsonResult;
import uk.ac.leedsbeckett.ltitoolset.backchannel.LtiBackchannel;
import uk.ac.leedsbeckett.ltitoolset.backchannel.LtiBackchannelKey;
import uk.ac.leedsbeckett.ltitoolset.backchannel.blackboard.BlackboardBackchannel;
import uk.ac.leedsbeckett.ltitoolset.backchannel.blackboard.BlackboardBackchannelKey;
import uk.ac.leedsbeckett.ltitoolset.backchannel.blackboard.data.GetCourseGroupUsersV2Results;
import uk.ac.leedsbeckett.ltitoolset.backchannel.blackboard.data.GetCourseGroupsV2Results;
import uk.ac.leedsbeckett.ltitoolset.backchannel.blackboard.data.GroupUserV2;
import uk.ac.leedsbeckett.ltitoolset.backchannel.blackboard.data.GroupV2;
import uk.ac.leedsbeckett.ltitoolset.backchannel.blackboard.data.RestExceptionMessage;
import uk.ac.leedsbeckett.ltitoolset.backchannel.blackboard.data.UserV1;
import uk.ac.leedsbeckett.ltitoolset.backchannel.services.LtiAgsBackchannel;
import uk.ac.leedsbeckett.ltitoolset.backchannel.services.LtiNrpsBackchannel;
import uk.ac.leedsbeckett.ltitoolset.websocket.HandlerAlertException;
import uk.ac.leedsbeckett.ltitoolset.websocket.annotations.EndpointJavascriptProperties;

/**
 * The web socket server endpoint that implements all the logic of this tool.
 * It is annotated in two ways - so that the container (e.g. tomcat) will map
 * URLs on to the class - and so that the build process can create javascript
 * for the client side of the web socket.
 * 
 * @author maber01
 */
@ServerEndpoint( 
        value="/socket/peergroupassessment", 
        decoders=ToolMessageDecoder.class, 
        encoders=ToolMessageEncoder.class )
@EndpointJavascriptProperties(
        module="peergroupassessment",
        prefix="Pga",
        messageEnum="uk.ac.leedsbeckett.ltitools.peergroupassessment.PgaServerMessageName"
)
public class PgaEndpoint extends MultitonToolEndpoint
{
  static final Logger logger = Logger.getLogger(PgaEndpoint.class.getName() );
  
  PeerGroupAssessmentTool tool;
  PgaToolLaunchState pgaState;
  StoreCluster store;

  LtiBackchannelKey ltinrpsbackchannelkey;
  LtiBackchannelKey ltiagsbackchannelkey;
  BlackboardBackchannelKey bbbckey;

  String platformName=null;
  
  // Don't store a reference to the resource or other data here.
  // It will get out of sync with instances held by other endpoint instances.
  // Rely on efficient caching and fetching at the start of every transaction.

  /**
   * Most work is done by the super-class. This sub-class fetches references
   * to tool specific objects.
   * 
   * @param session The session this endpoint belongs to.
   * @throws IOException If opening should be aborted.
   */
  @OnOpen
  @Override
  public void onOpen(Session session) throws IOException
  {
    super.onOpen( session );
    
    platformName = getState().getPlatformName();
    pgaState = (PgaToolLaunchState)getState().getToolLaunchState();
    tool = (PeerGroupAssessmentTool)getToolCoordinator().getTool( getState().getToolKey() );
    store = tool.getPeerGroupAssessmentStore();

    logger.log( Level.INFO, "URL for NRPS {0}", pgaState.getNamesRoleServiceUrl() );
    
    // A set of scopes that we intend to use in our LTI backchannel
    LtiServiceScopeSet nrpsset = new LtiServiceScopeSet();
    // Just this one
    nrpsset.addScope( LtiNamesRoleServiceClaim.SCOPE );
    // The key specifies the backchannel in a way we can share
    // with other endpoints.
    if ( pgaState.getNamesRoleServiceUrl() != null )
      ltinrpsbackchannelkey = new LtiBackchannelKey( 
            getPlatformHost(), 
            LtiNrpsBackchannel.class,
            pgaState.getNamesRoleServiceUrl(),
            nrpsset );
    
    LtiServiceScopeSet agsset = new LtiServiceScopeSet();
    agsset.addScope( LtiAssessmentAndGradesServiceClaim.SCOPE_LINEITEM_READONLY );
    if ( pgaState.getAssessmentAndGradesServiceLineItemsUrl() != null )
      ltiagsbackchannelkey = new LtiBackchannelKey( 
            getPlatformHost(), 
            LtiAgsBackchannel.class,
            pgaState.getAssessmentAndGradesServiceLineItemsUrl(),
            agsset );
    bbbckey = new BlackboardBackchannelKey( getPlatformHost() );
  }
  
  /**
   * Simply invokes super-class.
   * 
   * @param session The session this endpoint belongs to.
   * @throws IOException Unlikely to be thrown.
   */
  @OnClose
  @Override
  public void onClose(Session session) throws IOException
  {
    super.onClose( session );
  }

  /**
   * At present justs puts a line in the log.
   * 
   * @param session The session this endpoint belongs to.
   * @param throwable The throwable that caused the issue.
   */
  @OnError
  public void onError(Session session, Throwable throwable)
  {
    logger.log( Level.SEVERE, "Web socket error.", throwable );
  }  

  /**
   * Simply passes on responsibility for processing to the super-class.
   * 
   * @param session The session this endpoint belongs to.
   * @param message The incoming message from the client end.
   * @throws IOException Indicates failure to process.
   */
  @OnMessage
  @Override
  public void onMessage(Session session, ToolMessage message) throws IOException
  {
    super.onMessage( session, message );
  }

  /**
   * Client requested the resource data.
   * 
   * @param session The session this endpoint belongs to.
   * @param message The incoming message from the client end.
   * @throws IOException Indicates failure to process. 
   */
  @EndpointMessageHandler()
  public void handleGetResource( Session session, ToolMessage message ) throws IOException
  {
    // All users can have the resource at all stages.
    PeerGroupResource pgaResource = store.getResource( pgaState.getResourceKey(), true );
    logger.log( Level.INFO, "Sending resource [{0}]", pgaResource.getTitle() );
    // Check if caller is new participant...
    boolean groupnotify=false;
    if ( pgaState.isAllowedToParticipate() )
    {
      logger.log( Level.INFO, "User is allowed to participate so check if new user." );
      Group g = pgaResource.getGroupByMemberId( pgaState.getPersonId() );
      if ( g == null )
      {
        logger.log( Level.INFO, "User was not found in any of the groups. So, add to unattached." );
        pgaResource.addMember( null, pgaState.getPersonId(), pgaState.getPersonName() );
        store.updateResource( pgaResource );
        groupnotify=true;
      }
    }
    ToolMessage tm = new ToolMessage( message.getId(), PgaServerMessageName.Resource, pgaResource );
    
    if ( groupnotify )
      sendToolMessageToResourceUsers( tm );      
    else
      sendToolMessage( session, tm );

    if ( pgaResource.getFormId() == null ) return;
    PeerGroupForm form = store.getForm( pgaResource.getFormId() );
    if ( form == null ) return;
    
    ToolMessage tmf = new ToolMessage( message.getId(), PgaServerMessageName.Form, form );
    sendToolMessage( session, tmf );
  }
  
  /**
   * The client wants to set the basic properties of the peer group assessment
   * resource.
   * 
   * @param session  The session this endpoint belongs to.
   * @param message The incoming message from the client end.
   * @param p The PGA properties. (Title etc...)
   * @throws IOException Indicates failure to process. 
   * @throws HandlerAlertException Thrown when the request is not allowed or doesn't make sense.
   */
  @EndpointMessageHandler()
  public void handleSetResourceProperties( Session session, ToolMessage message, PgaProperties p ) 
          throws IOException, HandlerAlertException
  {
    if ( !pgaState.isAllowedToManage() )
      throw new HandlerAlertException( "Cannot set resource properties, you don't have management access here.", message.getId() );
    PeerGroupResource pgaResource = store.getResource( pgaState.getResourceKey(), true );
    logger.log( Level.INFO, "State       [{0}]", p.getStage().toString() );
    logger.log( Level.INFO, "Title       [{0}]", p.getTitle() );
    logger.log( Level.INFO, "Description [{0}]", p.getDescription() );
    pgaResource.setProperties( p );
    try
    {
      store.updateResource( pgaResource );
      ToolMessage tm = new ToolMessage( message.getId(), PgaServerMessageName.ResourceProperties, pgaResource.getProperties() );
      sendToolMessageToResourceUsers( tm );
    }
    catch ( IOException e )
    {
      logger.log(  Level.SEVERE, "Unable to store changes.", e );
    }
  }  

  /**
   * The client wants to change the properties of a group.
   * 
   * @param session The session this endpoint belongs to.
   * @param message The incoming message from the client end.
   * @param p The group id and desired group properties.
   * @throws IOException Indicates failure to process. 
   * @throws HandlerAlertException  Thrown when the request is not allowed or doesn't make sense.
   */
  @EndpointMessageHandler()
  public void handleSetGroupProperties( Session session, ToolMessage message, PgaChangeGroup p )
          throws IOException, HandlerAlertException
  {
    if ( !pgaState.isAllowedToManage() )
      throw new HandlerAlertException( "Cannot set group properties, you don't have management access here.", message.getId() );    
    PeerGroupResource pgaResource = store.getResource( pgaState.getResourceKey(), true );
    if ( !pgaResource.getStage().equals( Stage.SETUP ) )
      throw new HandlerAlertException( "Can only change group properties during the set-up stage.", message.getId() );
    logger.log( Level.INFO, "ID [{0}]",       p.getId() );
    logger.log( Level.INFO, "Title [{0}]",    p.getTitle() );
    Group g = pgaResource.getGroupById( p.getId() );
    if ( g != null )
    {
      g.setTitle( p.getTitle() );
      pgaResource.sortGroups();
      try
      {
        store.updateResource( pgaResource );
        // PgaChangeGroup change = new PgaChangeGroup( g.getId(), g.getTitle() );
        // Send whole resource because the order of the groups may have changed.
        ToolMessage tm = new ToolMessage( message.getId(), PgaServerMessageName.Resource, pgaResource );
        sendToolMessageToResourceUsers( tm );
      }
      catch ( IOException e )
      {
        logger.log(  Level.SEVERE, "Unable to store changes.", e );
      }
    }
  }
  
  /**
   * The client wants to delete a group.
   * 
   * @param session The session this endpoint belongs to.
   * @param message The incoming message from the client end.
   * @param p The group id and desired group properties.
   * @throws IOException Indicates failure to process. 
   * @throws HandlerAlertException  Thrown when the request is not allowed or doesn't make sense.
   */
  @EndpointMessageHandler()
  public void handleDeleteGroup( Session session, ToolMessage message, PgaChangeGroup p )
          throws IOException, HandlerAlertException
  {
    if ( !pgaState.isAllowedToManage() )
      throw new HandlerAlertException( "Cannot delete group, you don't have management access here.", message.getId() );    
    PeerGroupResource pgaResource = store.getResource( pgaState.getResourceKey(), true );
    if ( !pgaResource.getStage().equals( Stage.SETUP ) )
      throw new HandlerAlertException( "Can only delete groups during the set-up stage.", message.getId() );
    logger.log( Level.INFO, "ID [{0}]",       p.getId() );
    logger.log( Level.INFO, "Title [{0}]",    p.getTitle() );
    Group g = pgaResource.getGroupById( p.getId() );
    if ( g != null )
    {
      pgaResource.deleteGroup( p.getId() );
      try
      {
        store.updateResource( pgaResource );
        // Send whole resource.
        ToolMessage tm = new ToolMessage( message.getId(), PgaServerMessageName.Resource, pgaResource );
        sendToolMessageToResourceUsers( tm );
      }
      catch ( IOException e )
      {
        logger.log(  Level.SEVERE, "Unable to store changes.", e );
      }
    }
  }
  
  /**
   * The client wants a new group to be created within the PGA.
   * 
   * @param session The session this endpoint belongs to.
   * @param message The incoming message from the client end.
   * @throws IOException Indicates failure to process. 
   * @throws HandlerAlertException Thrown when the request is not allowed or doesn't make sense.
   */
  @EndpointMessageHandler()
  public void handleAddGroup( Session session, ToolMessage message )
          throws IOException, HandlerAlertException
  {
    if ( !pgaState.isAllowedToManage() )
      throw new HandlerAlertException( "Cannot add a group, you don't have management access here.", message.getId() );
    
    PeerGroupResource pgaResource = store.getResource( pgaState.getResourceKey(), true );
    if ( !pgaResource.getStage().equals( Stage.SETUP ) )
      throw new HandlerAlertException( "Can only add groups during the set-up stage.", message.getId() );
    Group g = pgaResource.addGroup( "New Group" );
    if ( g != null )
    {
      try
      {
        store.updateResource( pgaResource );
        //PgaChangeGroup p = new PgaChangeGroup( g.getId(), g.getTitle() );
        ToolMessage tm = new ToolMessage( message.getId(), PgaServerMessageName.Resource, pgaResource );
        sendToolMessageToResourceUsers( tm );
      }
      catch ( IOException e )
      {
        logger.log(  Level.SEVERE, "Unable to store changes.", e );
      }
    }
  }  
  
  /**
   * The client wants to add participants to a specified group.Participants
   * will be removed from any other groups. Possible to use this to
   * move people into the 'unattached' group. 
   * 
   * @param session The session this endpoint belongs to.
   * @param message The incoming message from the client end.
   * @param m The ID of the group and the IDs of participants.
   * @throws IOException Indicates failure to process. 
   * @throws HandlerAlertException Thrown when the request is not allowed or doesn't make sense. 
   */
  @EndpointMessageHandler()
  public void handleMembership( Session session, ToolMessage message, PgaAddMembership m )
          throws IOException, HandlerAlertException
  {
    PeerGroupResource pgaResource = store.getResource( pgaState.getResourceKey(), true );

    if ( pgaState.isAllowedToManage() )
    {
      if ( !pgaResource.getStage().equals( Stage.SETUP ) && !pgaResource.getStage().equals( Stage.JOIN ) )
        throw new HandlerAlertException( "Managers can only change group membership during the 'setup' and 'joining' stages.", message.getId() );      
    }
    else if ( pgaState.isAllowedToParticipate() )
    {
      if ( !pgaResource.getStage().equals( Stage.JOIN ) )
        throw new HandlerAlertException( "Can only change group membership during the 'joining' stage.", message.getId() );
      if ( !m.isOnlySelf( pgaState.getPersonId() ) )
        throw new HandlerAlertException( "You can only change your own group membership.", message.getId() );
    }
    else
      throw new HandlerAlertException( "Only participants and managers can change membership.", message.getId() );
    

    logger.log( Level.INFO, "Id   [{0}]",       m.getId() );
    try
    {
      Collection<String> affectedGids = pgaResource.addMemberships( m );
      store.updateResource( pgaResource );
      logger.log( Level.INFO, "Sending resource [{0}]", pgaResource.getTitle() );
      
      // Tell users about the resource change.
      ToolMessage tm = new ToolMessage( message.getId(), PgaServerMessageName.Resource, pgaResource );
      sendToolMessageToResourceUsers( tm );

      for ( String gid : affectedGids )
      {
        // No data for 'unattached' group
        if ( gid == null)
          continue;
        logger.log( Level.INFO, "Sending group user data for group gid [{0}]", gid );
        PeerGroupDataKey key = new PeerGroupDataKey( pgaResource.getKey(), gid );
        PeerGroupData data = store.getData( key, true );
        sendToolMessage( 
                new AllowedToSeeGroupData( gid, pgaResource ),
                new ToolMessage( message.getId(), PgaServerMessageName.Data, data ) );
      }      
    }
    catch ( IOException e )
    {
      logger.log(  Level.SEVERE, "Unable to store changes.", e );
    }
  }
  
  /**
   * The user wants copies of the form associated with this PGA and the 
   * user data for a specific group.
   * 
   * @param session The session this endpoint belongs to.
   * @param message The incoming message from the client end.
   * @param gidObject
   * @throws IOException Indicates failure to process. 
   * @throws HandlerAlertException Thrown when the request is not allowed or doesn't make sense. 
   */
  @EndpointMessageHandler()
  public void handleGetData( Session session, ToolMessage message, Id gidObject )
          throws IOException, HandlerAlertException
  {
    String gid = gidObject.getId();
    PeerGroupResource pgaResource = store.getResource( pgaState.getResourceKey(), true );
    Group myGroup = pgaResource.getGroupByMemberId( pgaState.getPersonId() );

    if (  gid == null )
      throw new HandlerAlertException( "No group ID was specified.", message.getId() );
    Group group = pgaResource.getGroupById( gid );
    
    if ( group == null )
      throw new HandlerAlertException( "Specified group ID is not in this resource.", message.getId() );
    if ( !pgaState.isAllowedToAccess() )
      throw new HandlerAlertException( "You don't have permission to view data here.", message.getId() );      
    if ( !pgaState.isAllowedToManage() && !gid.equals( myGroup.getId() ) )
      throw new HandlerAlertException( "You cannot view data in a group you don't belong to.", message.getId() );

    PeerGroupDataKey key = new PeerGroupDataKey( pgaResource.getKey(), gid );
    PeerGroupData data = store.getData( key, true );

    sendToolMessage( session, new ToolMessage( message.getId(), PgaServerMessageName.Data, data ) );
  }

  /**
   * The user wants copies of the form associated with this PGA and the 
   * user data for a specific group.
   * 
   * @param session The session this endpoint belongs to.
   * @param message The incoming message from the client end.
   * @throws IOException Indicates failure to process. 
   * @throws HandlerAlertException Thrown when the request is not allowed or doesn't make sense. 
   */
  @EndpointMessageHandler()
  public void handleGetAllData( Session session, ToolMessage message )
          throws IOException, HandlerAlertException
  {
    if ( !pgaState.isAllowedToManage() )
      throw new HandlerAlertException( "Only managers of a resource are allowed to look at data across all groups.", message.getId() );
    PeerGroupResource pgaResource = store.getResource( pgaState.getResourceKey(), true );
    PgaDataList list = store.getAllData( pgaResource );
    sendToolMessage( session, new ToolMessage( message.getId(), PgaServerMessageName.DataList, list ) );    
  }
  
  /**
   * The client wants to record user data.
   * 
   * @param session The session this endpoint belongs to.
   * @param message The incoming message from the client end.
   * @param datum IDs that identify where to change data and the data value.
   * @throws IOException Indicates failure to process. 
   * @throws HandlerAlertException Thrown when the request is not allowed or doesn't make sense. 
   */
  @EndpointMessageHandler()
  public void handleChangeDatum( Session session, ToolMessage message, PgaChangeDatum datum )
          throws IOException, HandlerAlertException
  {
    PeerGroupResource pgaResource = store.getResource( pgaState.getResourceKey(), true );
    Group myGroup = pgaResource.getGroupByMemberId( pgaState.getPersonId() );
    PeerGroupForm form = store.getForm( pgaResource.getFormId() );
    
    logger.log( 
            Level.INFO, 
            "handleChangeDatum() {0} {1} {2} {3}", 
            new Object[ ]{ datum.getGroupId(), datum.getFieldId(), datum.getMemberId(), datum.getValue() } );

    if ( !pgaResource.getStage().equals( Stage.DATAENTRY ) )
      throw new HandlerAlertException( "Can only edit data during the 'data entry' stage.", message.getId() );
    if (  datum.getGroupId() == null )
      throw new HandlerAlertException( "No group ID was specified.", message.getId() );
    Group group = pgaResource.getGroupById( datum.getGroupId() );
    if ( group == null )
      throw new HandlerAlertException( "Specified group ID is not in this resource.", message.getId() );
    if ( !pgaState.isAllowedToParticipate() )
      throw new HandlerAlertException( "You don't have permission to change data here.", message.getId() );      
    if ( !datum.getGroupId().equals( myGroup.getId() ) )
      throw new HandlerAlertException( "You cannot change data in a group you don't belong to.", message.getId() );

    
    Field field = form.getFields().get( datum.getFieldId() );
    PeerGroupDataKey key = new PeerGroupDataKey( pgaResource.getKey(), datum.getGroupId() );
    PeerGroupData data = store.getData( key, true );

    if ( data.isEndorsed( group, form ) )
      throw new HandlerAlertException( "You cannot change data values after endorsements have been made.", message.getId() );    

    data.setParticipantDatum( datum, field );
    store.updateData( data );
    
    sendToolMessage( 
            new AllowedToSeeGroupData( datum.getGroupId(), pgaResource ),
            new ToolMessage( message.getId(), PgaServerMessageName.Data, data ) );
  }

  /**
   * The client wants to record user data.
   * 
   * @param session The session this endpoint belongs to.
   * @param message The incoming message from the client end.
   * @param endorse IDs that identify where to add user endorsement.
   * @throws IOException Indicates failure to process. 
   * @throws HandlerAlertException Thrown when the request is not allowed or doesn't make sense. 
   */
  @EndpointMessageHandler()
  public void handleEndorseData( Session session, ToolMessage message, PgaEndorseData endorse )
          throws IOException, HandlerAlertException
  {
    PeerGroupResource pgaResource = store.getResource( pgaState.getResourceKey(), true );
    Group myGroup = pgaResource.getGroupByMemberId( pgaState.getPersonId() );
    logger.log( 
            Level.INFO, 
            "handleChangeDatum() {0} {1}", 
            new Object[ ]{ endorse.getGroupId(), endorse.isManager() } );
    Date now = new Date();
    PeerGroupDataKey key = new PeerGroupDataKey( pgaResource.getKey(), endorse.getGroupId() );
    PeerGroupData data = store.getData( key, true );

    if ( pgaState.isAllowedToManage() )
    {
      if ( !pgaResource.getStage().equals( Stage.DATAENTRY ) && !pgaResource.getStage().equals( Stage.RESULTS ) )
        throw new HandlerAlertException( "Can only change endorsement overrides during the 'data entry' or 'results' stage.", message.getId() );
    }
    else
    {
      if ( !pgaState.isAllowedToParticipate() )
        throw new HandlerAlertException( "You aren't a participant in this resource so you can't endorse data.", message.getId() );
      if ( !pgaResource.getStage().equals( Stage.DATAENTRY ) )
        throw new HandlerAlertException( "Can only endorse data entry during the 'data entry' stage.", message.getId() );     
    }

    if (  endorse.getGroupId() == null )
      throw new HandlerAlertException( "No group ID was specified.", message.getId() );
    Group group = pgaResource.getGroupById( endorse.getGroupId() );
    if ( group == null )
      throw new HandlerAlertException( "Specified group ID is not in this resource.", message.getId() );

    PeerGroupForm form = store.getForm( pgaResource.getFormId() );
    if ( !data.isAllDataValid( group, form ) )
      throw new HandlerAlertException( "You can only endorse data when all fields contain valid values.", message.getId() );    
    
    if ( endorse.isManager() )
    {
      if ( !pgaState.isAllowedToManage() )
        throw new HandlerAlertException( "You don't have permission to override endorsements here.", message.getId() );      
      for ( Member m : group.getMembers() )
      {
        if ( !data.isEndorsedByParticipant( m.getLtiId() ) )
          data.setEndorsementDate( m.getLtiId(), now, true);
      }
    }
    else
    {
      if ( !pgaState.isAllowedToParticipate() )
        throw new HandlerAlertException( "You don't have permission to endorse data here.", message.getId() );
      if ( !endorse.getGroupId().equals( myGroup.getId() ) )
        throw new HandlerAlertException( "You cannot endorse data in a group you don't belong to.", message.getId() );
      if ( !pgaResource.getGroupById( endorse.getGroupId() ).isMember( pgaState.getPersonId() ) )
        return;
      data.setEndorsementDate( pgaState.getPersonId(), now, false);
    }
    
    store.updateData( data );
    
    sendToolMessage( 
            new AllowedToSeeGroupData( endorse.getGroupId(), pgaResource ),
            new ToolMessage( message.getId(), PgaServerMessageName.Data, data ) );
  }

  /**
   * The client wants to record user data.
   * 
   * @param session The session this endpoint belongs to.
   * @param message The incoming message from the client end.
   * @param id ID of group.
   * @throws IOException Indicates failure to process. 
   * @throws HandlerAlertException  Thrown when the request is not allowed or doesn't make sense.
   */
  @EndpointMessageHandler()
  public void handleClearEndorsements( Session session, ToolMessage message, Id id )
          throws IOException, HandlerAlertException
  {
    PeerGroupResource pgaResource = store.getResource( pgaState.getResourceKey(), true );
    logger.log( 
            Level.INFO, 
            "handleClearEndorsements() {0}", 
            new Object[ ]{ id.getId() } );
    
    if ( !pgaState.isAllowedToManage() )
      throw new HandlerAlertException( "Cannot clear endorsements, you don't have management access here.", message.getId() );
    if ( !pgaResource.getStage().equals( Stage.DATAENTRY ) && !pgaResource.getStage().equals( Stage.RESULTS ) )
      throw new HandlerAlertException( "Can only clear endorsements during the 'data entry' or 'results' stage.", message.getId() );

    if (  id.getId() == null )
      throw new HandlerAlertException( "No group ID was specified.", message.getId() );
    Group group = pgaResource.getGroupById( id.getId() );
    if ( group == null )
      throw new HandlerAlertException( "Specified group ID is not in this resource.", message.getId() );
    
    PeerGroupDataKey key = new PeerGroupDataKey( pgaResource.getKey(), id.getId() );
    PeerGroupData data = store.getData( key, true );
    data.clearEndorsements();
    store.updateData( data );
    
    sendToolMessage( 
            new AllowedToSeeGroupData( id.getId(), pgaResource ),
            new ToolMessage( message.getId(), PgaServerMessageName.Data, data ) );
  }

  /**
   * The user wants to import participants from the launching platform.
   * 
   * @param session The session this endpoint belongs to.
   * @param message The incoming message from the client end.
   * @throws IOException Indicates failure to process. 
   * @throws HandlerAlertException Thrown when the request is not allowed or doesn't make sense. 
   */
  @EndpointMessageHandler()
  public void handleGetImport( Session session, ToolMessage message )
          throws IOException, HandlerAlertException
  {
    if ( !pgaState.isAllowedToManage() )
      throw new HandlerAlertException( "Only managers of a resource are allowed to import data.", message.getId() );
    PeerGroupResource resource = store.getResource( pgaState.getResourceKey(), true );

    if ( resource.getStage() != Stage.SETUP && resource.getStage() != Stage.JOIN )
      throw new HandlerAlertException( "You can only import participants in setup and join phases.", message.getId() );

    if ( pgaState.getNamesRoleServiceUrl() == null )
      throw new HandlerAlertException( "The platform that launched this tool did not provide an API web address for a names/role service.", message.getId() );

    // Get a backchannel (which might be new or reused and which knows how
    // to authenticate/authorize itself.
    LtiNrpsBackchannel backchannel = (LtiNrpsBackchannel)getToolCoordinator().getBackchannel(this, ltinrpsbackchannelkey, getState() );

    // Call the backchannel and wait for result.
    JsonResult jresult = backchannel.getNamesRoles();
    logger.log( Level.INFO, "handleGetImport() {0}", jresult.getRawValue() );
    if ( jresult.getResult() == null )
      throw new HandlerAlertException( "Unable to get membership data from the platform.", message.getId() );

    if ( !jresult.isSuccessful() )
    {
      if ( jresult.getResult() instanceof ServiceStatus )
      {
        ServiceStatus ss = (ServiceStatus)jresult.getResult();
        throw new HandlerAlertException( "Unable to get membership data from the platform. " + ss.getStatus() + " " + ss.getMessage(), message.getId() );
      }
      else
        throw new HandlerAlertException( "Unable to get membership data from the platform. Unknown error.", message.getId() );
    }
    
    NrpsMembershipContainer membership = (NrpsMembershipContainer)jresult.getResult();    
    membership.dumpToLog();
    ArrayList<Member> members = new ArrayList<>();
    for ( NrpsMember m : membership.getMembers() )
    {
      // filter out people who are already members.
      if ( !resource.isMember( m.getUserId() ) )
        members.add( new Member( m.getUserId(), m.getName() ) );
    }
    
    if ( members.isEmpty() )
      throw new HandlerAlertException( "No more users to import.", message.getId() );    

    // null group id means add to the 'unattached' group
    PgaAddMembership pgaaddmem = new PgaAddMembership( null, members );
    
    handleMembership( session, message, pgaaddmem );
  }  

  
  /**
   * The user wants to import participants from the launching platform.
   * 
   * @param session The session this endpoint belongs to.
   * @param message The incoming message from the client end.
   * @throws IOException Indicates failure to process. 
   * @throws HandlerAlertException Thrown when the request is not allowed or doesn't make sense. 
   */
  @EndpointMessageHandler()
  public void handleGetBlackboardGroupSets( Session session, ToolMessage message )
          throws IOException, HandlerAlertException
  {
    if ( !pgaState.isAllowedToManage() )
      throw new HandlerAlertException( "Only managers of a resource are allowed to import data from blackboard.", message.getId() );
    PeerGroupResource resource = store.getResource( pgaState.getResourceKey(), true );

    if ( resource.getStage() != Stage.SETUP )
      throw new HandlerAlertException( "You can only import sub-groups in setup phase.", message.getId() );

    BlackboardBackchannel bp = (BlackboardBackchannel)getBackchannel( bbbckey );
    JsonResult result = bp.getV2CourseGroupSets( pgaState.getCourseId() );
    if ( result.getResult() == null )
      throw new HandlerAlertException( "Technical problem fetching group sets.", message.getId() );
    if ( !result.isSuccessful() )
    {
      if ( result.getResult() instanceof ServiceStatus )
      {
        ServiceStatus ss = (ServiceStatus)result.getResult();
        throw new HandlerAlertException( "Unable to get group sets from the platform. " + ss.getStatus() + " " + ss.getMessage(), message.getId() );
      }
      else if ( result.getResult() instanceof RestExceptionMessage )
      {
        RestExceptionMessage rem = (RestExceptionMessage)result.getResult();
        throw new HandlerAlertException( "Unable to get group sets from the platform. " + rem.getStatus() + " " + rem.getMessage(), message.getId() );
      }
      else
        throw new HandlerAlertException( "Unable to get group sets from the platform. Unknown error.", message.getId() );
    }

    GetCourseGroupsV2Results resultsSets = (GetCourseGroupsV2Results)result.getResult();
    logger.log(Level.INFO, "Found {0}", resultsSets.getResults().size());


    result = bp.getV2CourseGroups( pgaState.getCourseId() );
    if ( result.getResult() == null )
      throw new HandlerAlertException( "Technical problem fetching group sets.", message.getId() );
    if ( !result.isSuccessful() )
    {
      if ( result.getResult() instanceof ServiceStatus )
      {
        ServiceStatus ss = (ServiceStatus)result.getResult();
        throw new HandlerAlertException( "Unable to get group sets from the platform. " + ss.getStatus() + " " + ss.getMessage(), message.getId() );
      }
      else if ( result.getResult() instanceof RestExceptionMessage )
      {
        RestExceptionMessage rem = (RestExceptionMessage)result.getResult();
        throw new HandlerAlertException( "Unable to get group sets from the platform. " + rem.getStatus() + " " + rem.getMessage(), message.getId() );
      }
      else
        throw new HandlerAlertException( "Unable to get group sets from the platform. Unknown error.", message.getId() );
    }

    GetCourseGroupsV2Results resultsGroups = (GetCourseGroupsV2Results)result.getResult();
    logger.log(Level.INFO, "Found {0}", resultsGroups.getResults().size());
    for ( GroupV2 g : resultsGroups.getResults() )
    {
      logger.log(Level.FINE, "Group Result: {0} {1} {2}", new Object[ ]{g.getId(), g.getName(), g.getGroupSetId()});
    }    
    
    BlackboardGroupSets bbgs = new BlackboardGroupSets();
    for ( GroupV2 set : resultsSets.getResults() )
    {
      String id = set.getExternalId();
      if ( id == null )
        throw new HandlerAlertException( "No external ID in group set results. (Perhaps because user agent lacks permissions.)", message.getId() );
      logger.log(Level.FINE, "Result: {0} {1} {2}", new Object[ ]{set.getId(), set.getName(), set.getGroupSetId()});
      BlackboardGroupSet bbset = new BlackboardGroupSet( set.getId(), set.getUuid(), set.getName(), new ArrayList<>() );
      bbgs.add( bbset );
      for ( GroupV2 g : resultsGroups.getResults() )
      {
        if ( g.isInGroupSet() && g.getGroupSetId().equals( set.getId() ) )
          bbset.addGroup( new BlackboardGroup( g.getId(), g.getUuid(), g.getName() ) );
      }    
    }
    
    sendToolMessage( 
            session,
            new ToolMessage( message.getId(), PgaServerMessageName.BlackboardGroupSets, bbgs ) );
  }  

  /**
   * The client wants to record user data.
   * 
   * @param session The session this endpoint belongs to.
   * @param message The incoming message from the client end.
   * @param id ID of BB group set.
   * @throws IOException Indicates failure to process. 
   * @throws HandlerAlertException  Thrown when the request is not allowed or doesn't make sense.
   */
  @EndpointMessageHandler()
  public void handleImportBlackboardGroupSet( Session session, ToolMessage message, Id id )
          throws IOException, HandlerAlertException
  {
    if ( !pgaState.isAllowedToManage() )
      throw new HandlerAlertException( "Only managers of a resource are allowed to import data from blackboard.", message.getId() );
    PeerGroupResource resource = store.getResource( pgaState.getResourceKey(), true );

    if ( resource.getStage() != Stage.SETUP )
      throw new HandlerAlertException( "You can only import sub-groups in setup phase.", message.getId() );
    
    logger.log(Level.FINE, "Importing group set with ID {0}", id.getId());

    // Build this object using data from BB API. It represents a plan of work
    // that will need to be done to implement the import.
    BlackboardGroupSetImportPlan plan = new BlackboardGroupSetImportPlan();
    

    BlackboardBackchannel bp = (BlackboardBackchannel)getBackchannel( bbbckey );
    JsonResult result = bp.getV2CourseGroupSetGroups( pgaState.getCourseId(), id.getId() );
    if ( result.getResult() == null )
      throw new HandlerAlertException( "Technical problem fetching group sets.", message.getId() );
    if ( !result.isSuccessful() )
    {
      if ( result.getResult() instanceof ServiceStatus )
      {
        ServiceStatus ss = (ServiceStatus)result.getResult();
        throw new HandlerAlertException( "Unable to get groups from the platform. " + ss.getStatus() + " " + ss.getMessage(), message.getId() );
      }
      else if ( result.getResult() instanceof RestExceptionMessage )
      {
        RestExceptionMessage rem = (RestExceptionMessage)result.getResult();
        throw new HandlerAlertException( "Unable to get groups from the platform. " + rem.getStatus() + " " + rem.getMessage(), message.getId() );
      }
      else
        throw new HandlerAlertException( "Unable to get groups from the platform. Unknown error.", message.getId() );
    }

    GetCourseGroupsV2Results resultsGroups = (GetCourseGroupsV2Results)result.getResult();
    logger.log(Level.INFO, "Found {0}", resultsGroups.getResults().size());
    for ( GroupV2 g : resultsGroups.getResults() )
    {
      logger.log(Level.FINE, "Group Result: {0} {1} {2}", new Object[ ]{g.getId(), g.getName(), g.getGroupSetId()});
      plan.addGroup( g.getId(), g.getName() );
      JsonResult resultUsers = bp.getV2CourseGroupUsers( pgaState.getCourseId(), g.getId() );
      if ( resultUsers.getResult() == null )
        throw new HandlerAlertException( "Technical problem fetching group members.", message.getId() );
      if ( !resultUsers.isSuccessful() )
      {
        if ( resultUsers.getResult() instanceof ServiceStatus )
        {
          ServiceStatus ss = (ServiceStatus)resultUsers.getResult();
          throw new HandlerAlertException( "Unable to get group members from the platform. " + ss.getStatus() + " " + ss.getMessage(), message.getId() );
        }
        else if ( resultUsers.getResult() instanceof RestExceptionMessage )
        {
          RestExceptionMessage rem = (RestExceptionMessage)resultUsers.getResult();
          throw new HandlerAlertException( "Unable to get group members from the platform. " + rem.getStatus() + " " + rem.getMessage(), message.getId() );
        }
        else
          throw new HandlerAlertException( "Unable to get group members from the platform. Unknown error.", message.getId() );
      }
      GetCourseGroupUsersV2Results users = (GetCourseGroupUsersV2Results)resultUsers.getResult();
      logger.log(Level.INFO, "Found {0}", users.getResults().size());
      for ( GroupUserV2 u : users.getResults() )
      {
        logger.log(Level.INFO, "User ID {0}", u.getUserId() );
        JsonResult resultU = bp.getV1Users( u.getUserId() );
        if ( resultU.getResult() == null )
          throw new HandlerAlertException( "Technical problem attempting to find user contact details.", message.getId() );
        logger.info( resultU.getResult().getClass().toString() );
        if ( !resultU.isSuccessful() )
        {
          if ( resultU.getResult() instanceof ServiceStatus )
          {
            ServiceStatus ss = (ServiceStatus)resultU.getResult();
            logger.severe( "Unable to get user info from the platform. " + ss.getStatus() + " " + ss.getMessage() );
            throw new HandlerAlertException( "Unable to get user info from the platform. " + ss.getStatus() + " " + ss.getMessage(), message.getId() );
          }
          else
            throw new HandlerAlertException( "Unable to get user info from the platform. Unknown error.", message.getId() );
        }

        UserV1 user = (UserV1)resultU.getResult();
        logger.fine( user.getUuid() );
        logger.fine( user.getName().getGiven() + " " + user.getName().getFamily() );
        plan.addUser( user.getUuid(), user.getName().getGiven() + " " + user.getName().getFamily() );
      }  
    }
    
    // What's the plan?
    StringBuilder sb = new StringBuilder();
    sb.append( "\n\n" );
    for ( BlackboardGroupSetImportPlan.Group g : plan.getGroups() )
    {
      sb.append( g.getExternalId() );
      sb.append( "\n" );
      sb.append( g.getName() );
      sb.append( "\n" );
      for ( BlackboardGroupSetImportPlan.User u : g.getUsers() )
      {
        sb.append( "    " );
        sb.append( u.getId() );
        sb.append( "    " );
        sb.append( u.getName() );
        sb.append( "\n" );
      }
    }
    sb.append( "\n" );

    logger.fine( sb.toString() );
    
    boolean changes = false;
    // Ensure no duplicate entries.
    HashSet<String> affectedGids = new HashSet<>();
    for ( BlackboardGroupSetImportPlan.Group g : plan.getGroups() )
    {
      Group resgroup = resource.getGroupByExternalId( g.getExternalId() );
      if ( resgroup == null )
      {
        changes = true;
        resgroup = resource.addGroup( g.getName(), g.getExternalId() );
      }
      PgaAddMembership pam = g.getPgaAddMembership( resgroup.getId() );
      if ( !pam.isEmpty() )
      {
        changes = true;
        affectedGids.addAll( resource.addMemberships( pam ) );
      }
    }
    
    if ( changes )
    {
      try
      {
        store.updateResource( resource );
        //PgaChangeGroup p = new PgaChangeGroup( g.getId(), g.getTitle() );
        ToolMessage tm = new ToolMessage( message.getId(), PgaServerMessageName.Resource, resource );
        sendToolMessageToResourceUsers( tm );
        
        for ( String gid : affectedGids )
        {
          // No data for 'unattached' group
          if ( gid == null)
            continue;
          logger.log( Level.INFO, "Sending group user data for group gid [{0}]", gid );
          PeerGroupDataKey key = new PeerGroupDataKey( resource.getKey(), gid );
          PeerGroupData data = store.getData( key, true );
          sendToolMessage( 
                  new AllowedToSeeGroupData( gid, resource ),
                  new ToolMessage( message.getId(), PgaServerMessageName.Data, data ) );
        }
      }
      catch ( IOException e )
      {
        logger.log(  Level.SEVERE, "Unable to store changes.", e );
        throw new HandlerAlertException( "Import failed.", message.getId() );
      }
    }        
  }  
  
  /**
   * The user wants a string containing the resource's complete data set for
   * export.
   * 
   * @param session The session this endpoint belongs to.
   * @param message The incoming message from the client end.
   * @throws IOException Indicates failure to process. 
   * @throws HandlerAlertException Thrown when the request is not allowed or doesn't make sense. 
   */
  @EndpointMessageHandler()
  public void handleGetExport( Session session, ToolMessage message )
          throws IOException, HandlerAlertException
  {
    if ( !pgaState.isAllowedToManage() )
      throw new HandlerAlertException( "Only managers of a resource are allowed to export all data.", message.getId() );
    PeerGroupResource resource = store.getResource( pgaState.getResourceKey(), true );
    if ( resource.getStage() != Stage.RESULTS )
      throw new HandlerAlertException( "You can only export data when the results are frozen. Try again at that stage.", message.getId() );
    
    PeerGroupForm form = store.getForm( resource.getFormId() );
    HashMap<String,String>    memberscore = new HashMap<>();
    HashMap<String,String>  memberendorse = new HashMap<>();

    StringBuilder sb = new StringBuilder();
    sb.append( "Group\tName\tScore\tEndorsed\tGroupCount\tGroupTotal\n" );
    
    for ( String gid : resource.groupIdsInOrder )
    {
      Group g = resource.getGroupById( gid );
      PeerGroupDataKey dk = new PeerGroupDataKey( resource.getKey(), gid );
      PeerGroupData data = store.getData( dk, false );
      int groupcount=0;
      int grouptotal=0;
      boolean groupcomplete=true;
      for ( Member m : g.getMembers() )
      {
        groupcount++;
        ParticipantData pdata=null;
        if ( data != null )
          pdata = data.getParticipantData().get( m.getLtiId() );
        if ( pdata == null )
        {
          memberscore.put( m.getLtiId(), "\"None\"" );
          memberendorse.put( m.getLtiId(), "\"No\"" );
          groupcomplete=false;
          continue;
        }
        
        int total=0;
        boolean complete=true;
        for ( Field field : form.getFields().values() )
        {
          ParticipantDatum datum = pdata.getParticipantData().get( field.getId() );
          if ( datum != null && datum.isValid() )
            total += Integer.parseInt( datum.getValue() );
          else
            complete = false;
        }
        if ( complete )
        {
          memberscore.put( m.getLtiId(), Integer.toString( total ) );
          grouptotal += total;
        }
        else
        {
          memberscore.put( m.getLtiId(), "\"Incomplete\"" );
          groupcomplete = false;
        }
        

        String endorse;
        if ( pdata.getEndorsedDate() != null )
          endorse = "\"Yes\"";
        else if ( pdata.getManagerEndorsedDate() != null )
          endorse = "\"Override\"";
        else
          endorse = "\"No\"";
        memberendorse.put( m.getLtiId(), endorse );        
      }
      
      for ( Member m : g.getMembers() )
      {
        String score = memberscore.get( m.getLtiId() );
        if ( score == null ) score = "";
        String endorse = memberendorse.get( m.getLtiId() );
        if ( endorse == null ) endorse = "";
        sb.append( "\"" );
        sb.append( g.getTitle() );
        sb.append( "\"\t\"" );
        sb.append( m.getName() );
        sb.append( "\"\t" );
        sb.append( score );
        sb.append( "\t" );
        sb.append( endorse );
        sb.append( "\t" );
        sb.append( groupcount );
        sb.append( "\t" );
        if ( groupcomplete )
          sb.append( grouptotal );
        else
          sb.append( "\"incomplete\"" );
        sb.append( "\n" );        
      }
    }
        
    sendToolMessage( session, new ToolMessage( message.getId(), PgaServerMessageName.Export, sb.toString() ) );
  }
  
  @EndpointMessageHandler()
  public void handleLineItemsRequest( Session session, ToolMessage message )
          throws IOException, HandlerAlertException
  {
    logger.log(  Level.INFO, "Attempting to get line items for this resource..." );
    if ( !pgaState.isAllowedToManage() )
      throw new HandlerAlertException( "Only managers of a resource are allowed to fetch assessment line items.", message.getId() );
    if ( pgaState.getAssessmentAndGradesServiceLineItemsUrl() == null )
      throw new HandlerAlertException( "The platform that launched this tool did not provide an API web address for an assessment and grades service.", message.getId() );

    // Get a backchannel (which might be new or reused and which knows how
    // to authenticate/authorize itself.
    LtiAgsBackchannel backchannel = (LtiAgsBackchannel)getToolCoordinator().getBackchannel(this, ltiagsbackchannelkey, getState() );

    // Call the backchannel and wait for result.
    JsonResult jresult = backchannel.getLineItems();
    logger.log( Level.INFO, jresult.getRawValue() );
    if ( jresult.getResult() == null )
      throw new HandlerAlertException( "Unable to get line item information from the platform.", message.getId() );

    if ( !jresult.isSuccessful() )
    {
      if ( jresult.getResult() instanceof ServiceStatus )
      {
        ServiceStatus ss = (ServiceStatus)jresult.getResult();
        throw new HandlerAlertException( "Unable to get line item data from the platform. " + ss.getStatus() + " " + ss.getMessage(), message.getId() );
      }
      else
        throw new HandlerAlertException( "Unable to get line item data from the platform. Unknown error.", message.getId() );
    }
    
    LineItems lineItems = (LineItems)jresult.getResult();
    sendToolMessage( session, new ToolMessage( message.getId(), PgaServerMessageName.AssessmentLineItems, lineItems ) );
  }  
  
  @EndpointMessageHandler()
  public void handleLineItemsResults( Session session, ToolMessage message )
          throws IOException, HandlerAlertException
  {
    logger.log(  Level.INFO, "Attempting to export scores to platform..." );
    if ( !pgaState.isAllowedToManage() )
      throw new HandlerAlertException( "Only managers of a resource are allowed to export assessment results.", message.getId() );
    if ( pgaState.getAssessmentAndGradesServiceLineItemsUrl() == null )
      throw new HandlerAlertException( "The platform that launched this tool did not provide an API web address for an assessment and grades service.", message.getId() );
    PeerGroupResource resource = store.getResource( pgaState.getResourceKey(), true );
    if ( resource.getStage() != Stage.RESULTS )
      throw new HandlerAlertException( "You can only export data when the results are frozen. Try again at that stage.", message.getId() );

    PeerGroupForm form = store.getForm( resource.getFormId() );
    HashMap<String,String>    memberscore = new HashMap<>();
    int reportedprogress = 1;    
    int progress;

    ToolMessage tmprog = new ToolMessage( 
            message.getId(), 
            PgaServerMessageName.AssessmentScoreExportProgress, 
            new PgaScoreProgress( 1 ) );
    sendToolMessage( session, tmprog );

    // Get a backchannel (which might be new or reused and which knows how
    // to authenticate/authorize itself.
    LtiAgsBackchannel backchannel = (LtiAgsBackchannel)getToolCoordinator().getBackchannel(this, ltiagsbackchannelkey, getState() );
    
    // Define the new lineitem we want...
    LineItem[] lineItems = new LineItem[3];
    LineItem[] actualLineItems = new LineItem[3];
    String[] liNames = {
      " - Score",
      " - Group Size",
      " - Group Total"};
    BigDecimal[] liMax = 
      { 
        BigDecimal.valueOf( 120 ), 
        BigDecimal.valueOf( 10 ), 
        BigDecimal.valueOf( 1200 )
      };
    for ( int i=0; i<3; i++ )
    {
      lineItems[i]= new LineItem(
                      null,
                      liMax[i],
                      resource.getTitle() + liNames[i],
                      pgaState.getResourceKey().getResourceId(),
                      false,
                      null,
                      null );
        
      // Call the backchannel and wait for result.
      JsonResult jresult = backchannel.postLineItem( lineItems[i] );
      logger.log( Level.INFO, jresult.getRawValue() );
      if ( jresult.getResult() == null )
        throw new HandlerAlertException( "Unable to create line item in the platform.", message.getId() );

      if ( !jresult.isSuccessful() )
      {
        if ( jresult.getResult() instanceof ServiceStatus )
        {
          ServiceStatus ss = (ServiceStatus)jresult.getResult();
          throw new HandlerAlertException( "Unable to create line item in the platform. " + ss.getStatus() + " " + ss.getMessage(), message.getId() );
        }
        else
          throw new HandlerAlertException( "Unable to create line item in the platform. Unknown error.", message.getId() );
      }

      actualLineItems[i] = (LineItem)jresult.getResult();
      logger.log(Level.INFO, "Success, column item ID = {0}", actualLineItems[i].getId());
    }
    logger.log( Level.INFO, "All three columns created." );
        
    for ( int i=0; i < resource.groupIdsInOrder.size(); i++ )
    {
      progress = Math.round( 100.0f * (float)i / (float)resource.groupIdsInOrder.size() );
      if ( progress > reportedprogress )
      {
        tmprog = new ToolMessage( 
                message.getId(), 
                PgaServerMessageName.AssessmentScoreExportProgress, 
                new PgaScoreProgress( progress ) );
        sendToolMessage( session, tmprog );
        reportedprogress = progress;
      }
      
      String gid = resource.groupIdsInOrder.get( i );      
      Group g = resource.getGroupById( gid );
      PeerGroupDataKey dk = new PeerGroupDataKey( resource.getKey(), gid );
      PeerGroupData data = store.getData( dk, false );
      int groupcount=0;
      int grouptotal=0;
      boolean groupcomplete=true;
      for ( Member m : g.getMembers() )
      {
        groupcount++;
        ParticipantData pdata=null;
        if ( data != null )
          pdata = data.getParticipantData().get( m.getLtiId() );
        if ( pdata == null )
        {
          memberscore.put( m.getLtiId(), "\"None\"" );
          groupcomplete=false;
          continue;
        }
        
        int total=0;
        boolean complete=true;
        for ( Field field : form.getFields().values() )
        {
          ParticipantDatum datum = pdata.getParticipantData().get( field.getId() );
          if ( datum != null && datum.isValid() )
            total += Integer.parseInt( datum.getValue() );
          else
            complete = false;
        }
        if ( complete )
        {
          memberscore.put( m.getLtiId(), Integer.toString( total ) );
          grouptotal += total;
        }
        else
        {
          memberscore.put( m.getLtiId(), "\"Incomplete\"" );
          groupcomplete = false;
        }
      }

      // Only if all scores for all group members are valid
      // send each member's scores to the platform.
      if ( groupcomplete )
      {
        for ( Member m : g.getMembers() )
        {
          String score = memberscore.get( m.getLtiId() );
          if ( score == null ) score = "";

          Instant ts = Instant.now();
          Score[] ltiscores = new Score[3];
          ltiscores[0] = new Score( 
                  m.getLtiId(),
                  new BigDecimal( score, MathContext.UNLIMITED ),
                  liMax[0],
                  null,
                  ts,
                  "Completed",
                  "FullyGraded",
                  null
          );
          ltiscores[1] = new Score( 
                  m.getLtiId(),
                  new BigDecimal( groupcount, MathContext.UNLIMITED ),
                  liMax[1],
                  null,
                  ts,
                  "Completed",
                  "FullyGraded",
                  null
          );
          ltiscores[2] = new Score( 
                  m.getLtiId(),
                  new BigDecimal( grouptotal, MathContext.UNLIMITED ),
                  liMax[2],
                  null,
                  ts,
                  "Completed",
                  "FullyGraded",
                  null
          );

          for ( int j=0; j<3; j++ )
          {
            // Call the backchannel and wait for result.
            JsonResult jresult = backchannel.postScores( actualLineItems[j], ltiscores[j] );
            logger.log( Level.INFO, jresult.getRawValue() );
            if ( jresult.getResult() == null )
              throw new HandlerAlertException( "Unable to post score information to the platform.", message.getId() );
            if ( !jresult.isSuccessful() )
              throw new HandlerAlertException( "Unable to post score information to the platform. " + jresult.getErrorMessage(), message.getId() );                
          }
        }
      }
    }
    // Ensure client page knows we have completed without an exception.
    tmprog = new ToolMessage( 
            message.getId(), 
            PgaServerMessageName.AssessmentScoreExportProgress, 
            new PgaScoreProgress( 100 ) );
    sendToolMessage( session, tmprog );    
  }
  
  
  @EndpointMessageHandler()
  public void handleConfigurationRequest( Session session, ToolMessage message )
          throws IOException, HandlerAlertException
  {
    if ( !pgaState.isAllowedToConfigure() )
      throw new HandlerAlertException( "Recieved request for configuration from user who is not allowed to configure the tool.", message.getId() );
    
    logger.info( "Fetching config for platform " + platformName );
    Configuration config = tool.getPlatformConfig( platformName );
    ToolMessage tmf = new ToolMessage( message.getId(), PgaServerMessageName.Configuration, new PgaConfigurationMessage( config ) );
    sendToolMessage( session, tmf );
  }
  
  @EndpointMessageHandler()
  public void handleConfigure( Session session, ToolMessage message, PgaConfigurationMessage configMessage )
          throws IOException, HandlerAlertException
  {
    if ( !pgaState.isAllowedToConfigure() )
      throw new HandlerAlertException( "Recieved request to save new configuration from user who is not allowed to configure the tool.", message.getId() );
            
    Configuration config = configMessage.getConfiguration();
    if ( config == null )
      throw new HandlerAlertException( "Null configuration was received.", message.getId() );
    
    try
    {  
      tool.savePlatformConfig( platformName, config);
    }
    catch ( Exception e )
    {
      logger.log( Level.SEVERE, "Unable to save configuration for platform " + platformName, e );
      throw new HandlerAlertException( "Unable to save configuration.", message.getId() );
    }
    
    ToolMessage tmf = new ToolMessage( message.getId(), PgaServerMessageName.ConfigurationSuccess, "Saved" );
    sendToolMessage( session, tmf );
    
    // To do - send message to all users now accessing tool from the same platform
    // for now just for confirmation to current user.
    ToolMessage tmc = new ToolMessage( message.getId(), PgaServerMessageName.Configuration, new PgaConfigurationMessage( config ) );
    sendToolMessage( session, tmc );
  }

  
  /**
   * This gets called when a handler throws a HandlerAlertException and decides
   * how to alert the user.
   * 
   * @param session The web socket session.
   * @param haex The exception that was thrown.
   * @throws IOException If the attempt to alert the user fails.
   */
  @Override
  public void processHandlerAlert( Session session, HandlerAlertException haex ) throws IOException
  {
    sendToolMessage( session, new ToolMessage( haex.getMessageId(), PgaServerMessageName.Alert, haex.getMessage() ) );    
  }
}
