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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
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
import uk.ac.leedsbeckett.lti.services.data.ServiceStatus;
import uk.ac.leedsbeckett.lti.services.nrps.LtiNamesRoleServiceClaim;
import uk.ac.leedsbeckett.lti.services.nrps.data.NrpsMembershipContainer;
import uk.ac.leedsbeckett.lti.services.nrps.data.NrpsMember;
import uk.ac.leedsbeckett.ltitools.peergroupassessment.formdata.PeerGroupForm.Field;
import uk.ac.leedsbeckett.ltitools.peergroupassessment.inputdata.ParticipantData;
import uk.ac.leedsbeckett.ltitools.peergroupassessment.inputdata.ParticipantDatum;
import uk.ac.leedsbeckett.ltitoolset.websocket.ToolEndpoint;
import uk.ac.leedsbeckett.ltitoolset.websocket.ToolMessage;
import uk.ac.leedsbeckett.ltitoolset.websocket.ToolMessageDecoder;
import uk.ac.leedsbeckett.ltitoolset.websocket.ToolMessageEncoder;
import uk.ac.leedsbeckett.ltitoolset.websocket.annotations.EndpointMessageHandler;
import uk.ac.leedsbeckett.ltitools.peergroupassessment.messagedata.Id;
import uk.ac.leedsbeckett.ltitools.peergroupassessment.messagedata.PgaDataList;
import uk.ac.leedsbeckett.ltitools.peergroupassessment.messagedata.PgaEndorseData;
import uk.ac.leedsbeckett.ltitools.peergroupassessment.predicate.AllowedToSeeGroupData;
import uk.ac.leedsbeckett.ltitools.peergroupassessment.resourcedata.PeerGroupResource.Group;
import uk.ac.leedsbeckett.ltitools.peergroupassessment.resourcedata.PeerGroupResource.Member;
import uk.ac.leedsbeckett.ltitools.peergroupassessment.resourcedata.Stage;
import uk.ac.leedsbeckett.ltitoolset.backchannel.JsonResult;
import uk.ac.leedsbeckett.ltitoolset.backchannel.LtiBackchannel;
import uk.ac.leedsbeckett.ltitoolset.backchannel.LtiBackchannelKey;
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
public class PgaEndpoint extends ToolEndpoint
{
  static final Logger logger = Logger.getLogger(PgaEndpoint.class.getName() );
  
  PeerGroupAssessmentTool tool;
  PgaToolLaunchState pgaState;
  StoreCluster store;

  LtiBackchannelKey ltibackchannelkey;
  
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
    
    pgaState = (PgaToolLaunchState)getState().getToolLaunchState();
    tool = (PeerGroupAssessmentTool)getToolCoordinator().getTool( getState().getToolKey() );
    store = tool.getPeerGroupAssessmentStore();

    logger.log( Level.INFO, "URL for NRPS {0}", pgaState.getNamesRoleServiceUrl() );
    
    // A set of scopes that we intend to use in our LTI backchannel
    LtiServiceScopeSet set = new LtiServiceScopeSet();
    // Just this one
    set.addScope( LtiNamesRoleServiceClaim.SCOPE );
    // The key specifies the backchannel in a way we can share
    // with other endpoints.
    ltibackchannelkey = new LtiBackchannelKey( 
            getPlatformHost(), 
            pgaState.getNamesRoleServiceUrl(),
            set );
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

    if ( !pgaResource.getStage().equals( Stage.JOIN ) )
      throw new HandlerAlertException( "Can only change group membership during the 'joining' stage.", message.getId() );
    
    if ( !pgaState.isAllowedToAccess() )
      throw new HandlerAlertException( "As a non-participant you cannot change group membership.", message.getId() );

    if ( !pgaState.isAllowedToManage() && !m.isOnlySelf( pgaState.getPersonId() ) )
      throw new HandlerAlertException( "You can only change your own group membership.", message.getId() );
    
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

    if ( resource.getStage() != Stage.JOIN )
      throw new HandlerAlertException( "You can only import participants in setup phase. Try again at that stage.", message.getId() );

    if ( pgaState.getNamesRoleServiceUrl() == null )
      throw new HandlerAlertException( "The platform that launched this tool did not provide an API web address for a names/role service.", message.getId() );

    // Get a backchannel (which might be new or reused and which knows how
    // to authenticate/authorize itself.
    LtiBackchannel backchannel = (LtiBackchannel)getToolCoordinator().getBackchannel( this, ltibackchannelkey, getState() );

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
