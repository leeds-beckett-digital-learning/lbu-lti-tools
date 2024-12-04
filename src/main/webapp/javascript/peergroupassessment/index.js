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

import finder from "../common/domutil.js";
import arialib from "../common/aria.js";
import peergroupassessment from "../generated/peergroupassessment.js";

let dynamicData = dynamicPageData;

let toolsocket;

let loading;

let dataentryopening=true;

let resource = 
        {
          "resourceKey":{"platformId":"https://my-test.leedsbeckett.ac.uk/","resourceId":"_137_1"},
          "properties":{"title":"","description":"","stage":"SETUP"},
          "groupsById":{},
          "groupOfUnattached":{"id":null,"title":null,"membersbyid":{}},
          "groupIdsByMember":{}
        };
        
let form;
let data;
let selectedgroupid;
let formuptodate = false;

let bbgroupsetdata;

let unattachedcheckboxes = new Array();
let oldstage = "unknown";

let platformconfig = null;

function init()
{
  console.log( "init" );
  console.log( finder.toplevelalert );

  console.log( finder.toplevelalert.ariaLive );
  finder.toplevelalert.ariaLive = 'polite';
  console.log( "Set ariamixin property" );
  console.log( finder.toplevelalert.ariaLive );
  
  //finder.toplevelalert.setAttribute( 'aria-live', 'polite' );  
  //console.log( finder.toplevelalert.getAttribute( 'aria-live' ) );

  arialib.setDialogAlertClass( 'alertList' );
  arialib.setBaseAlertElement( finder.toplevelalert );
  setInterval( updateAlerts, 1000 );
      
  if ( dynamicData.allowedToManage )
    finder.editpropsSaveButtonBottom   .addEventListener( 'click', () => saveEditProps()                );
  finder.editpropsCloseButtonBottom    .addEventListener( 'click', () => arialib.closeDialog( finder.editpropsCloseButtonBottom )       );
  
  if ( dynamicData.allowedToManage )
  {
    finder.editgrouppropsSaveButtonBottom .addEventListener( 'click', () => saveEditGroupProps()                );
    finder.deletegroupDeleteButtonBottom  .addEventListener( 'click', () => deleteGroup()                       );
  }
  finder.editgrouppropsCloseButtonBottom  .addEventListener( 'click', () => arialib.closeDialog(finder.editgrouppropsCloseButtonBottom)       );
  finder.deletegroupCloseButtonBottom     .addEventListener( 'click', () => arialib.closeDialog(finder.deletegroupCloseButtonBottom)       );
  
  finder.dataentryCloseButtonTop           .addEventListener( 'click', () => arialib.closeDialog(finder.dataentryCloseButtonTop)       );
  finder.dataentryCloseButtonBottom        .addEventListener( 'click', () => arialib.closeDialog(finder.dataentryCloseButtonBottom)       );
  if ( dynamicData.allowedToParticipate )
    finder.dataentryEndorseButton            .addEventListener( 'click', () => endorseData( false )           );
  if ( dynamicData.allowedToManage )
  {
    finder.dataentryManagerEndorseButton     .addEventListener( 'click', () => endorseData( true )            );
    finder.dataentryClearEndorsementsButton  .addEventListener( 'click', () => clearEndorsements()            );
    finder.exportdialogCloseButtonTop        .addEventListener( 'click', () => arialib.closeDialog(finder.exportdialogCloseButtonTop)    );
    finder.exportdialogCloseButtonBottom     .addEventListener( 'click', () => arialib.closeDialog(finder.exportdialogCloseButtonBottom)    );
    finder.exportButton                      .addEventListener( 'click', () => getExport(finder.exportButton)                      );
    if ( dynamicData.allowedToExportToPlatform )
    {
      finder.exportplatformdialogCloseButton .addEventListener( 'click', () => arialib.closeDialog(finder.exportplatformdialogCloseButton) );
      finder.exportplatformdialogExportButton.addEventListener( 'click', () => exportToPlatform(finder.exportplatformdialogExportButton) );
      finder.exportPlatformButton            .addEventListener( 'click', () => getExportPlatform(finder.exportPlatformButton)              );
    }    
    if ( finder.importButton )
      finder.importButton                      .addEventListener( 'click', () => getImport(finder.importButton)                      );
    if ( finder.importBlackboardButton )
      finder.importBlackboardButton            .addEventListener( 'click', () => getBlackboardGroupSets(finder.importBlackboardButton)  );
  }

  finder.bbgroupsetsdialogselect               .addEventListener( 'change', () => showBlackboardGroupSet() );
  finder.bbgroupsetsdialogSaveButtonBottom     .addEventListener( 'click', () => importBlackboardGroupSet() );
  finder.bbgroupsetsdialogCloseButtonBottom    .addEventListener( 'click', () => arialib.closeDialog(finder.bbgroupsetsdialogCloseButtonBottom)       );
  
  finder.debugdialogCloseButtonTop     .addEventListener( 'click', () => arialib.closeDialog(finder.debugdialogCloseButtonTop)       );
  finder.debugdialogCloseButtonBottom  .addEventListener( 'click', () => arialib.closeDialog(finder.debugdialogCloseButtonBottom)       );


  if ( finder.editpropertiesButton )
    finder.editpropertiesButton.addEventListener( 'click', () => arialib.openDialog( 'editprops', finder.editpropertiesButton ) );
  if ( finder.debugdialogButton )
    finder.debugdialogButton   .addEventListener( 'click', () => openDebugDialog( finder.debugdialogButton )       );
  
  console.log( dynamicData.webSocketUri );
  
  if ( dynamicData.allowedToConfigure )
  {
    // These HTML elements won't exist if the user is not allowed to configure
    finder.configureButton.addEventListener(          'click', () => openConfig() );
    finder.configdialogSaveButton.addEventListener(   'click', () => saveConfig() );
    finder.configdialogCancelButton.addEventListener( 'click', () => arialib.closeDialog( finder.configdialog ) );
  }

  let handler =
  {
    open()
    {
      toolsocket.sendMessage( new peergroupassessment.GetResourceMessage() );
    },
    
    handleAlert( message )
    {
      alert( message.payload );
    },
    
    handleResource( message )
    {
      formuptodate = false;
      resource = message.payload;
      updateResource();
      updateGroups();
      if ( dynamicData.allowedToManage )
      {
        toolsocket.sendMessage( new peergroupassessment.GetAllDataMessage() );
      }
    },
    
    handleResourceProperties( message )
    {
      let stagechanged = !(message.payload.stage === resource.properties.stage);
      resource.properties = message.payload;
      updateResource();
      if ( stagechanged )
      {
        updateGroups();
        // because buttons need to be enabled disabled
        updateForm();      
        updateFormData();
        // group table was cleared so a manager needs status for each group
        if ( dynamicData.allowedToManage )
          toolsocket.sendMessage( new peergroupassessment.GetAllDataMessage() );
      }
    },
    
    handleForm( message )
    {
      formuptodate = false;
      form = message.payload;
      console.log( form );
      updateForm();      
    },
    
    handleData( message )
    {
      if ( message.payload.key.groupId === selectedgroupid )
      {
        data = message.payload;
        console.log( data );
        if ( !formuptodate )
          updateForm();
       updateFormData();
      }      
      if ( dynamicData.allowedToManage )
        updateOverviewDataGroup( message.payload );
    },

    handleDataList( message )
    {
      updateOverviewData( message.payload );
    },
    
    handleExport( message )
    {
      console.log( "handleExport" );
      console.log( message.payload );
      finder.exporttextarea.value = message.payload;
    },
    
    handleAssessmentLineItems( message )
    {
      console.log( "handleAssessmentLineItems" );
      console.log( message.payload );
    },
    
    handleAssessmentScoreExportProgress( message )
    {
      console.log( "handleAssessmentScoreExportProgress" );
      console.log( message.payload );
      finder.exportlineitemsprogress.innerHTML = message.payload.percentage;
      if ( message.payload.percentage === 100 )
        arialib.closeDialog( finder.exportplatformdialog );
    },
    
    handleBlackboardGroupSets( message )
    {
      bbgroupsetdata = message.payload;
      updateBlackboardGroupSets();
      arialib.openDialog( 'bbgroupsetsdialog', finder.importBlackboardButton );  
    },
    
    handleConfiguration( message )
    {
      console.log( message );
      platformconfig = message.payload.configuration;
      if ( dynamicData.allowedToConfigure )
      {
        for ( var prop in platformconfig )
        {
          console.log( "Configuration property name: " + prop );
          let inputid = "config_" + prop;
          console.log( inputid );
          let input = finder[inputid];
          console.log( input );
          if ( input )
          {
            if ( input.type === 'checkbox' )
              input.checked = platformconfig[prop];              
            else
              input.value = platformconfig[prop];
          }
        }
        console.log( "End of list" );
      }
      // Now update the validation strings...
      coursespecvalidator = new RegExp( platformconfig.courseSearchValidation );
      orgspecvalidator    = new RegExp( platformconfig.organizationSearchValidation    );
      // And advice strings
      finder.courseadvice.innerHTML   = platformconfig.courseAdvice;
      finder.orgadvice.innerHTML      = platformconfig.organizationAdvice;
      finder.trainingadvice.innerHTML = platformconfig.trainingAdvice;
    },
    
    handleConfigurationSuccess( message )
    {
      alert( "Configuration success: " + message.payload );
      arialib.closeDialog( finder.configdialog );
    }
  
  };
  
  toolsocket = new peergroupassessment.ToolSocket( dynamicData.webSocketUri, handler  );  
}

function updateAlerts()
{
  arialib.updateAlerts();  
}
  
function addAlert( text )
{
  arialib.addAlert( text );
}

function updateResource()
{
  var properties = resource.properties;
  
  console.log( "stage       = " + properties.stage       );
  console.log( "title       = " + properties.title       );
  console.log( "description = " + properties.description );

  let stagetext;
  let stagechanged = !( properties.stage === oldstage );
  oldstage = properties.stage;
  if ( stagechanged )
  {
    finder.stage1.className = 'stage-inactive';
    finder.stage2.className = 'stage-inactive';
    finder.stage3.className = 'stage-inactive';
    finder.stage4.className = 'stage-inactive';
    switch ( properties.stage )
    {
      case "SETUP":
        finder.stage1.className = 'stage-active';
        stagetext = finder.stage1text.innerText;
        break;
      case "JOIN":
        finder.stage2.className = 'stage-active';
        stagetext = finder.stage2text.innerText;
        break;
      case "DATAENTRY":
        finder.stage3.className = 'stage-active';
        stagetext = finder.stage3text.innerText;
        break;
      case "RESULTS":
        finder.stage4.className = 'stage-active';
        stagetext = finder.stage4text.innerText;
        break;
    }
    addAlert( "Stage changed to " + stagetext + "." );
  }
  
  if ( finder.mainTitle.innerText !== properties.title )
  {
    if ( finder.mainTitle.innerText )
      addAlert( "Title changed to " + properties.title + "." );
    finder.mainTitle.innerText        = properties.title;
  }
  if ( finder.mainDescription.innerText !== properties.description )
  {
    if ( finder.mainDescription.innerText )
      addAlert( "Description changed." );
    finder.mainDescription.innerText  = properties.description;
  }

  finder.editpropsTitle.value       = properties.title;
  finder.editpropsDescription.value = properties.description;
  finder.editpropsStage.value       = properties.stage;
}

function updateGroups()
{
  var headhtml;
  if ( dynamicData.allowedToManage )
    headhtml = "<div id=\"groupadddiv\"><button id=\"addgroupButton\">Add Group</button></div>\n";
  else
    headhtml = "";
  var foothtml     = "<div id=\"groupunattacheddiv\"><h3>Unattached Participants</h3><div id=\"unattachedparticipants\"></div></div>\n";
  finder.groups.innerHTML = headhtml + foothtml;

  if ( finder.addgroupButton )
  {
    finder.groupadddiv.style.display = resource.properties.stage === "SETUP"?"initial":"none";
    finder.addgroupButton.addEventListener( 'click', () => addGroup() );
  }
  
  if ( resource.groupIdsInOrder.length === 0 )
  {
    var d = document.createElement( "div" );
    finder.groups.insertBefore( d, finder.groupunattacheddiv );
    d.innerHTML = "No groups";
  }
  else
  {
    for ( const gid of resource.groupIdsInOrder )
    {
      const g = resource.groupsById[gid];
      console.log( g );
      updateGroup( g );
    }
  }
  updateUnattachedGroup();
  addAlert( "The table of groups on this page has been rebuilt." );
}


function updateUnattachedGroup()
{
  unattachedcheckboxes = new Array();
  finder.unattachedparticipants.innerHTML = "";
  const set = resource.groupOfUnattached.membersbyid;
  
  let html = "<table>\n";
  let empty = true;
  for ( let id in set )
  {
    empty = false;
    html += "<tr><td><span>";
    if ( dynamicData.allowedToManage && ( resource.properties.stage === "SETUP" || resource.properties.stage === "JOIN" ))
    {
      html += "<input type=\"checkbox\" id=\"unattachedMember_" + id + "\" data-id=\"" + id + "\" data-name=\"" + set[id].name + "\"></input>";
      html += "<label for=\"unattachedMember_" + id + "\">" + set[id].name + "</label>";
    }
    else
      html += set[id].name;
    html += "</span></td></tr>\n";
  }
  
  if ( empty )
  {
    finder.groupunattacheddiv.style.display = "none";
  }
  else
  {
    finder.unattachedparticipants.innerHTML = html;
    for ( let id in set )
    {
      var checkbox = finder[ "unattachedMember_" + id ];
      unattachedcheckboxes.push( checkbox );
    }
    finder.groupunattacheddiv.style.display = "block";
  }
}

function isMemberOf( g )
{
  return g.hasOwnProperty( "membersbyid" ) && g.membersbyid.hasOwnProperty( dynamicData.myId );
}

function updateGroup( g )
{
  var buttonstage = ( resource.properties.stage === "SETUP" || resource.properties.stage === "JOIN" );
  
  if ( !g.id )
  {
    updateUnattachedGroup();
    return;
  }

  var membercount=0;
  for ( const mid in g.membersbyid )
    membercount++;
  
  resource.groupsById[g.id] = g;
  let groupdiv = finder[ "group-div-" + g.id ];
  if ( !groupdiv )
  {
    groupdiv = document.createElement( "div" );
    groupdiv.id = "group-div-" + g.id;
    finder.groups.insertBefore( groupdiv, finder.groupunattacheddiv );
  }

  let title = finder[ "group-title-" + g.id ];
  if ( !title )
  {
    title = document.createElement( "h3" );
    title.id = "group-title-" + g.id;
    groupdiv.appendChild( title );
  }

  if ( dynamicData.allowedToManage || isMemberOf( g ) )
    title.innerHTML = "<span id=\"groupViewStatus" + g.id + "\" class=\"groupstatus\"></span><a id=\"groupViewLink"   + g.id + "\" href=\".\">" + g.title + "</a>";
  else
    title.innerHTML = g.title;

  let table = finder[ "group-" + g.id ];
  if ( !table )
  {
    table = document.createElement( "table" );
    table.id = "group-" + g.id;
    table.className = "grouptable";
    groupdiv.appendChild( table );
  }

  var buttonhtml;
  if ( buttonstage )
  {
    buttonhtml = "<td rowspan=\"" + membercount + "\">";
    if ( dynamicData.allowedToManage )
    {
      if ( resource.properties.stage === "SETUP" )
      {
        buttonhtml += "<p style=\"padding-bottom: 0.5em;\"><button id=\"groupDeleteButton" + g.id + "\">Delete</button></p>\n";
        buttonhtml += "<p style=\"padding-bottom: 0.5em;\"><button id=\"groupEditButton"   + g.id + "\">Edit</button></p>\n";
      }
      if ( resource.properties.stage === "SETUP" || resource.properties.stage === "JOIN" )
        buttonhtml += "<p style=\"padding-bottom: 0.5em;\"><button id=\"groupAddToButton_" + g.id + "\" class=\"addtobutton\">Add Selected</button></p>\n";
    }
    if ( dynamicData.allowedToParticipate && resource.properties.stage === "JOIN" && !isMemberOf(g) )
      buttonhtml += "<p style=\"padding-bottom: 0.5em;\"><button id=\"groupJoinButton_" + g.id + "\" class=\"joinbutton\">Join</button></p>\n";
    buttonhtml += "</td>\n";
  }

  var first=true;
  var html="";
  html += "<colgroup>";
  var usedwidth = 0;
  if ( buttonstage )
  {
    html += "<col class=\"groupothercolumn\" style=\"width: 5em;\"/>";
    html += "<col class=\"groupothercolumn\" style=\"width: 5em;\" />";
    usedwidth += 10;
  }
  html += "<col class=\"groupparticipants\"/>";
  if ( dynamicData.allowedToManage )
  {
    html += "<col class=\"groupothercolumn\" style=\"width: 3em;\"/>";
    html += "<col class=\"groupothercolumn\" style=\"width: 5em;\"/>";        
    usedwidth += 8;
  }
  html += "</colgroup>\n";
  html += "<tr>\n";
  if ( buttonstage )
  {
    html += "<th scope=\"col\" class=\"thcollapsed\"><div class=\"nonvisual\">Buttons</div></th>";
    html += "<th scope=\"col\" class=\"thcollapsed\"><div class=\"nonvisual\">Unjoin</div></th>";
  }
  html += "<th scope=\"col\" class=\"thcollapsed\"><div class=\"nonvisual\">Participant</div></th>";
  if ( dynamicData.allowedToManage )
  {
    html += "<th scope=\"col\" class=\"thcollapsed\"><div class=\"nonvisual\">Score</div></th>";
    html += "<th scope=\"col\" class=\"thcollapsed\"><div class=\"nonvisual\">Endorsement</div></th>";      
  }
  html += "</tr>\n";
  if ( membercount === 0 )
    html += "<tr>" + buttonhtml + "<td><em>No members</em></td></tr>\n";
  for ( const mid in g.membersbyid )
  {
    let m = g.membersbyid[mid];
    html += "<tr>";

    if ( first && buttonstage )
    {
      first = false;
      html += buttonhtml;
    }
    if ( buttonstage )
    {
      html += "<td>";
      if (
           ( mid === dynamicData.myId && resource.properties.stage === "JOIN" ) || 
           ( dynamicData.allowedToManage && 
                ( resource.properties.stage === "SETUP" || 
                  resource.properties.stage === "JOIN" ) )
         )
       html += " <button id=\"groupUnjoinButton_" + mid + "\" class=\"unjoinbutton\">Unjoin</button>";
      html += "</td>\n";
    }
    html += "<td>";
    html += m.name;
    html += "</td>";
    if ( dynamicData.allowedToManage )
    {
      html += "<td id=\"overviewScore_"    + mid + "\"></td>";
      html += "<td id=\"overviewEndorsed_" + mid + "\"></td>";
    }
    html += "</tr>\n";
  }
  
  console.log( "Adding group table" );
  console.log( html );
  table.innerHTML = html;
  groupdiv.style = "max-width: " + (usedwidth+20) + "em;";
  
  // Put event handlers on the buttons.
  if ( dynamicData.allowedToManage )
  {
    var db = finder[ "groupDeleteButton" + g.id ];
    if ( db )
      db.addEventListener( 'click', () => openGroupDeleteDialog( db, g.id ) );
    var de = finder[ "groupEditButton"   + g.id ];
    if ( de )
      de.addEventListener( 'click', () => openGroupEditDialog( de, g.id ) );
  }
  if ( dynamicData.allowedToManage || isMemberOf( g ) )
  {
    var link = finder[ "groupViewLink"   + g.id ];
    if ( link )
      link.addEventListener( 'click', ( e ) =>     
                                {
                                  e.preventDefault();
                                  openDataEntryDialog( link, g.id ) ;
                                }
                            );
  }

  if ( resource.properties.stage === "JOIN" && !isMemberOf(g) && dynamicData.allowedToParticipate )
  {
    const but = finder[ "groupJoinButton_"   + g.id ];
    if ( but )
      but.addEventListener( 'click', () => addMembership( g.id ) );
  }

  if ( dynamicData.allowedToManage && ( resource.properties.stage === "SETUP" || resource.properties.stage === "JOIN" ))
  {
    const but = finder[ "groupAddToButton_"   + g.id ];
    if ( but )
      but.addEventListener( 'click', () => addMembersToGroup( g.id ) );
  }
  
  for ( const mid in g.membersbyid )
  {
    let m = g.membersbyid[mid];
    if (
         ( mid === dynamicData.myId && resource.properties.stage === "JOIN" ) || 
         ( dynamicData.allowedToManage && 
              ( resource.properties.stage === "SETUP" || 
                resource.properties.stage === "JOIN" ) )
       )
    {
      const but = finder[ "groupUnjoinButton_" + mid ];
      if ( but )
        but.addEventListener( 'click', () => removeMemberFromGroup( mid, m.name ) );
    }
  }
}

function removeMemberFromGroup( mid, name )
{    
  let pids = [];
  pids[0] = {};
  pids[0].ltiId = mid;
  pids[0].name  = name;
  toolsocket.sendMessage( new peergroupassessment.MembershipMessage( null, pids ) );
}
  
function addMembersToGroup( gid )
{
  let pids = new Array();
  const set = resource.groupOfUnattached.membersbyid;

  for ( let i=0; i < unattachedcheckboxes.length; i++ )
  {
    let checkbox = unattachedcheckboxes[i];
    if ( checkbox.checked )
    {
      let p = {};
      p.ltiId = checkbox.dataset.id;
      p.name  = checkbox.dataset.name;
      pids.push( p );
    }
  }
  if ( pids.length > 0 )
    toolsocket.sendMessage( new peergroupassessment.MembershipMessage( gid, pids ) );  
  else
    alert( "You need to select people in the unattached list if you want to add them to a group." );
}

function clearForm()
{
  
  let formrows = document.getElementsByClassName( "dataentry-formrow" );
  console.log( formrows );
  console.log( formrows.length );
  while ( formrows.length > 0 )
  {
    let formrow = formrows[0];
    console.log( formrow );
    formrow.remove();
  }
  let formcells = document.getElementsByClassName( "dataentry-formcell" );
  while ( formcells.length > 0 )
  {
    let formcell = formcells[0];
    console.log( formcell );
    formcell.remove();
  }
}

function processFormInputEvent( e, gid, f, m )
{
  console.log( 'processFormInputEvent' );
  console.log( e );
  console.log( f );
  console.log( m );
  toolsocket.sendMessage( new peergroupassessment.ChangeDatumMessage( gid, f.id, m.ltiId, e.value ) );
}

function addFormInputListener( e, gid, f, m )
{
  e.addEventListener('input', function () { processFormInputEvent( e, gid, f, m ); } );  
}

function updateForm()
{
  clearForm();
  
  let groupid = selectedgroupid;
  if ( !groupid )
    groupid = resource.groupIdsByMember[dynamicData.myId];
  console.log( "group id = " + groupid );
  if ( !groupid )
    return;
  
  let group = resource.groupsById[groupid];
  let row = document.createElement( "tr" );
  let td;
  let tdm;
  let th = document.createElement( "th" );
  th.className = "dataentry-formcell";
  th.innerHTML = "Criterion";
  th.scope = "col";
  row.append( th );
  
  for ( let m in group.membersbyid )
  {
    th = document.createElement( "th" );
    th.className = "vertical";
    th.innerHTML = group.membersbyid[m].name;
    th.scope = "col";
    row.append( th );
  }
  finder.dataentrytablebody.innerHTML = "";
  finder.dataentrytablebody.append( row );

  for ( let i=0; i<form.fieldIds.length; i++ )
  {
    let fieldid = form.fieldIds[i];
    let field = form.fields[fieldid];
    if ( !field ) continue;
    row = document.createElement( "tr" );
    row.className = "dataentry-formrow";
    row.id = "dataentryrow-" + field.id;
    th = document.createElement( "th" );
    th.scope = "row";
    th.className = "plain";
    th.innerText = field.description;
    row.append( th );
    for ( let m in group.membersbyid )
    {
      td = document.createElement( "td" );
      row.append( td );
      let inputid = "dataentrycell_" + fieldid + "_" + groupid + "_" + m;
      let input = document.createElement( "input" );
      input.size = 3;
      input.id = inputid;
      input.disabled = true;
      input.autocomplete = 'off';
      td.append( input );
      addFormInputListener( input, groupid, field, group.membersbyid[m] );      
    }
    finder.dataentrytablebody.append( row );
    finder.dataentrytablebody.append( document.createTextNode("\n") );
  }
  
  // Row at bottom with endorsement buttons...
  let endorserow = document.createElement( "tr" );
  endorserow.className = "dataentry-formrow";
  let managerendorserow = document.createElement( "tr" );
  managerendorserow.className = "dataentry-formrow";
  td = document.createElement( "td" );
  td.innerHTML = "Participant Endorsement";
  endorserow.append( td );
  tdm = document.createElement( "td" );
  let p = document.createElement( "p" );
  p.innerHTML = "Endorsement Override";
  tdm.append( p );
  p = document.createElement( "p" );
  tdm.append( p );
  managerendorserow.append( tdm );
  
  for ( let m in group.membersbyid )
  {
    td = document.createElement( "td" );
    td.className = "vertical";
    p = document.createElement( "p" );
    p.id = "dataentry-endorsedate-" + m;
    td.append( p );
    endorserow.append( td );
    
    tdm = document.createElement( "td" );
    tdm.className = "vertical";
    p = document.createElement( "p" );
    p.id = "dataentry-managerendorsedate-" + m;
    tdm.append( p );
    managerendorserow.append( tdm );  
  }
  finder.dataentrytablebody.append( endorserow );
  finder.dataentrytablebody.append( managerendorserow );  
  formuptodate = true;
  
  if ( arialib.getCurrentDialog() === 'dataentry' )
    addAlert( "The data entry table on this page has been rebuilt." );
}

function updateFormData()
{  
  let groupid = selectedgroupid;
  if ( !groupid )
    groupid = resource.groupIdsByMember[dynamicData.myId];
  console.log( "group id = " + groupid );
  if ( !groupid )
    return;
  let group = resource.groupsById[groupid];
  
  let inputdisabled = 
          resource.properties.stage !== "DATAENTRY" || 
          !isMemberOf( group ) || 
          !data || 
          data.status !== "NOTENDORSED";
  
  
  for ( let i=0; i<form.fieldIds.length; i++ )
  {
    let fieldid = form.fieldIds[i];
    let field = form.fields[fieldid];
    if ( !field ) continue;
    for ( let m in group.membersbyid )
    {
      let memberdata = undefined;
      let memberdatum = undefined;
      if ( data ) memberdata = data.participantData[m];
      if ( memberdata ) memberdatum = memberdata.participantData[fieldid];
      let inputid = "dataentrycell_" + fieldid + "_" + groupid + "_" + m;
      let input = finder[ inputid ];
      if ( !input )
        continue;      
      input.disabled = inputdisabled;
      if ( memberdatum )
      {
        if ( memberdatum.value === '' )
          input.className = 'emptyinput';
        else if ( memberdatum.valid )
          input.className = 'validinput';
        else
          input.className = 'invalidinput';
        if ( input.value !== memberdatum.value )
        {
          input.value = memberdatum.value;
          if ( arialib.getCurrentDialog() === 'dataentry' && !dataentryopening )
          {
            let name = getParticipantName( m );
            if ( name )
              addAlert( "A score was updated in row " + (i+2) + " for participant " + name + "." );
            else
              addAlert( "A score was updated in row " + (i+2) + "." );
          }
        }
      }
    }
  }
  
  for ( let m in group.membersbyid )
  {
    let p;
    let memberdata = undefined;
    if ( data ) memberdata = data.participantData[m];
    p = finder["dataentry-endorsedate-" + m];
    if ( memberdata && memberdata.endorsedDate )
      p.innerHTML = memberdata.endorsedDate.replace( " ", "<br>" );
    else
      p.innerHTML = 'Not Endorsed';
    p = finder["dataentry-managerendorsedate-" + m];
    if ( memberdata && memberdata.managerEndorsedDate )
      p.innerHTML = memberdata.managerEndorsedDate.replace( " ", "<br>" );
    else
      p.innerHTML = '';
  }
  
  dataentryopening=false;
}

function updateOverviewData( datalist )
{  
  console.log( "updateOverviewData" );
  console.log( datalist );
  for ( const d of datalist )
    updateOverviewDataGroup( d );
}

function updateOverviewDataGroup( d )
{  
  console.log( "updateOverviewDataGroup" );
  console.log( d );
  
  let pstatus = finder[ "groupViewStatus" + d.key.groupId ];
  if ( pstatus )
  {
    if ( d.status === "PARTLYENDORSED" )
      pstatus.innerHTML = "<img src=\"../style/incomplete.png\" style=\"vertical-align: middle;\" alt=\"Some endorsements.\" />";
    else if ( d.status === "FULLYENDORSED" )
      pstatus.innerHTML = "<img src=\"../style/complete.png\"   style=\"vertical-align: middle;\" alt=\"All endorsed.\" />";
    else
      pstatus.innerHTML = "<img src=\"../style/circle.png\"     style=\"vertical-align: middle;\" alt=\"No endorsements.\" />";
  }
  
  for ( const mid in d.participantData )
  {
    console.log( "mid = " + mid );
    let memberdata = d.participantData[mid];
    console.log( "memberdata = " + memberdata );
    console.log( memberdata );
    
    let complete=true;
    let total = 0;
    for ( let i=0; i<form.fieldIds.length; i++ )
    {
      let fieldid = form.fieldIds[i];
      let datum = memberdata.participantData[fieldid];
      console.log( datum );
      if ( datum && datum.valid )
        total += parseInt( datum.value );
      else
        complete = false;
    }
    let td = finder[ "overviewScore_" + mid ];
    if ( td ) td.innerText = complete ? total : "incomplete";    
    td = finder[ "overviewEndorsed_" + mid ];
    if ( td )
    {
      if ( memberdata.endorsedDate )
        td.innerText = "Endorsed";
      else if ( memberdata.managerEndorsedDate )
        td.innerText = "Override";
      else
        td.innerText = "";
    }
  }
}

function updateBlackboardGroupSets()
{
  console.log( "updateBlackboardGroupSets" );
  console.log( bbgroupsetdata );  
      
  finder.bbgroupsetsdialogselect.innerHTML = '';
  finder.bbgroupsetsdialogdetail.innerHTML = '';
  var html;
  if ( !bbgroupsetdata.length )
  {
    html = "<option value=\"-\">No Group Sets in Course</option>\n";
  }
  else
  {
    html = "<option value=\"-\">Select a Set Here</option>\n";
    for ( let i=0; i < bbgroupsetdata.length; i++ )
      html += "<option value=\"" + bbgroupsetdata[i].id + "\">" + bbgroupsetdata[i].name + "</option>";
  }
  finder.bbgroupsetsdialogselect.innerHTML = html;
  console.log( "Done" );
}

function showBlackboardGroupSet()
{
  var selectedid = finder.bbgroupsetsdialogselect.value;
  console.log( "showBlackboardGroupSet" );
  console.log( selectedid );

  finder.bbgroupsetsdialogdetail.innerHTML = "";  
  for ( let i=0; i < bbgroupsetdata.length; i++ )
  {
    console.log( "Checking " + bbgroupsetdata[i].id );
    if ( bbgroupsetdata[i].id === selectedid )
    {
      var html = "<h4>Selected Set: " + bbgroupsetdata[i].name + 
              "</h4>\n<p>Contains " + bbgroupsetdata[i].groups.length + 
              " Groups</p><ul>\n";
      for ( let j=0; j<bbgroupsetdata[i].groups.length; j++ )
        html += "<li>" + bbgroupsetdata[i].groups[j].name + "</li>\n";
      html += "<ul>\n";
      finder.bbgroupsetsdialogdetail.innerHTML = html;
    }
  }  
}

function importBlackboardGroupSet()
{
  var selectedid = finder.bbgroupsetsdialogselect.value;
  console.log( "showBlackboardGroupSet" );
  console.log( selectedid );
  if ( selectedid === null || selectedid === '-' )
  {
    alert( "No group set was selected." );
    return;
  }
  arialib.closeDialog( finder.bbgroupsetsdialogImportButtonBottom );
  toolsocket.sendMessage( new peergroupassessment.ImportBlackboardGroupSetMessage( selectedid ) );  
}


function openGroupEditDialog( openerElement, gid )
{
  let g = resource.groupsById[gid];
  arialib.openDialog( 'editgroupProps', openerElement );
  finder.editgrouppropsId.value = gid;
  finder.editgrouppropsTitle.value = (g)?g.title:"Unknown Group";
}

function openGroupDeleteDialog( openerElement, gid )
{
  let g = resource.groupsById[gid];
  arialib.openDialog( 'deletegroup', openerElement );
  finder.deletegroupId.value = gid;
  finder.deletegroupTitle.value          = (g)?g.title:"Unknown Group";
  finder.deletegroupTitleTitle.innerText = (g)?g.title:"Unknown Group";
}


function openDataEntryDialog( openerElement, gid )
{
  dataentryopening=true;
  selectedgroupid = gid;
  clearForm();
  updateForm();
  toolsocket.sendMessage( new peergroupassessment.GetDataMessage( gid ) );
      
  arialib.openDialog( 'dataentry', openerElement );
}

function getExport( openerelement )
{
  if ( resource.properties.stage !== "RESULTS" )
  {
    alert( "Result export is only available in the final stage when results are frozen." );
    return;
  }
  finder.exporttextarea.innerText = "Waiting for data...";
  toolsocket.sendMessage( new peergroupassessment.GetExportMessage() );
  arialib.openDialog( 'exportdialog', openerelement );  
}

function getExportPlatform( openerelement )
{
  if ( resource.properties.stage !== "RESULTS" )
  {
    alert( "Result export to platform is only available in the final stage when results are frozen." );
    return;
  }
  finder.exportlineitems_suffix.value = finder.mainTitle.innerText;
  arialib.openDialog( 'exportplatformdialog', openerelement );  
}

function exportToPlatform( openerelement )
{
  if ( resource.properties.stage !== "RESULTS" )
  {
    alert( "Result export to platform is only available in the final stage when results are frozen." );
    return;
  }
  // Tell server to export scores to line items on LTI service of platform.
  var suffix = finder.exportlineitems_suffix.value.trim();
  
  var lineItemIncluded = new Array();
  var valid = false;
  for ( var i=0; i<6; i++ )
  {
    var fieldname = "exportlineitems_opt_" + i;
    lineItemIncluded[i] = finder[fieldname].checked;
    if ( lineItemIncluded[i] )
      valid = true;
  }
  if ( valid )
    toolsocket.sendMessage( new peergroupassessment.LineItemsResultsMessage( suffix, lineItemIncluded ) );
  else
    alert( "At least one optional number must be selected to export." );
}

function getImport( openerelement )
{
  toolsocket.sendMessage( new peergroupassessment.GetImportMessage() );
}

function getBlackboardGroupSets( openerelement )
{
  toolsocket.sendMessage( new peergroupassessment.GetBlackboardGroupSetsMessage() );
}

function openDebugDialog( openerElement )
{
  let pre = finder[ "debugtext" ];
  pre.innerHTML = "testing...";  
  arialib.openDialog( 'debugdialog', openerElement );
}

function saveEditProps()
{
  toolsocket.sendMessage( new peergroupassessment.SetResourcePropertiesMessage( 
          finder.editpropsTitle.value, 
          finder.editpropsDescription.value, 
          finder.editpropsStage.value ) );
  arialib.closeDialog( finder.editpropsSaveButtonBottom );
}

function saveEditGroupProps()
{
  toolsocket.sendMessage( new peergroupassessment.SetGroupPropertiesMessage( 
          finder.editgrouppropsId.value, 
          finder.editgrouppropsTitle.value ) );
  arialib.closeDialog( finder.editgrouppropsSaveButtonBottom );
}

function deleteGroup()
{
  toolsocket.sendMessage( new peergroupassessment.DeleteGroupMessage( 
          finder.deletegroupId.value, 
          finder.deletegroupTitle.value ) );
  arialib.closeDialog( finder.deletegroupDeleteButtonBottom );
}

function endorseData( manager )
{
  let groupid = selectedgroupid;
  if ( !groupid )
    groupid = resource.groupIdsByMember[dynamicData.myId];
  toolsocket.sendMessage( new peergroupassessment.EndorseDataMessage( groupid, manager ) );  
}

function clearEndorsements()
{
  let groupid = selectedgroupid;
  if ( !groupid )
    groupid = resource.groupIdsByMember[dynamicData.myId];
  toolsocket.sendMessage( new peergroupassessment.ClearEndorsementsMessage( groupid ) );  
}

function getParticipantName( id )
{
  if ( !id || !resource ) return null;
  let gid = resource.groupIdsByMember[id];
  let group = (gid)?resource.groupsById[gid]:resource.groupOfUnattached;
  let member = group.membersbyid[id];
  if ( !member ) return null;
  return member.name;
}

function addGroup()
{
  toolsocket.sendMessage( new peergroupassessment.AddGroupMessage() );
}

function addMembership( gid )
{
  let pids = [];
  pids[0] = {};
  pids[0].ltiId = dynamicData.myId;
  pids[0].name  = dynamicData.myName;

  toolsocket.sendMessage( new peergroupassessment.MembershipMessage( gid, pids ) );
}

function openConfig()
{
  if ( !dynamicData.allowedToConfigure )
  {
    alert( "No permission to configure this tool." );
    return;
  }
  toolsocket.sendMessage( new peergroupassessment.ConfigurationRequestMessage() );
  arialib.openDialog( 'configdialog', finder.configureButton );
}

function saveConfig()
{
  if ( !dynamicData.allowedToConfigure )
  {
    alert( "No permission to configure this tool." );
    return;
  }
  
  if ( platformconfig === null )
  {
    alert( "Unable to save configuration because none was received." );
    return;
  }

  let updatedconfig = new Object();
  for ( var prop in platformconfig )
  {
    console.log( "Configuration property name: " + prop );
    let inputid = "config_" + prop;
    console.log( inputid );
    let input = finder[inputid];
    console.log( input );
    if ( input )
    {
      if ( input.type === 'checkbox' )
        updatedconfig[prop] = input.checked;
      else
        updatedconfig[prop] = input.value;
      console.log( input.value );
    }
  }
  console.log( updatedconfig );
  
  toolsocket.sendMessage( new peergroupassessment.ConfigureMessage( updatedconfig ) );
}

function test()
{
  setTimeout(() => {
    addAlert( "Test alert." );
  }, "3000" );
}

window.addEventListener( "load", function(){ init(); } );
document.addEventListener( "DOMContentLoaded", function()
  { 
    console.log( "DOMContentLoaded event arrived." ); 
    console.log( finder.toplevelalert );
  } );
