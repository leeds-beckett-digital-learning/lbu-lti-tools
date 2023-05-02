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
    finder.addgroupButton              .addEventListener( 'click', () => addGroup()                     );
    
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
      updateResource( resource.properties );
      updateGroups();
      if ( dynamicData.allowedToManage )
      {
        updateOverview();
        toolsocket.sendMessage( new peergroupassessment.GetAllDataMessage() );
      }
    },
    
    handleResourceProperties( message )
    {
      let stagechanged = !(message.payload.stage === resource.properties.stage);
      resource.properties = message.payload;
      updateResource( resource.properties );
      if ( stagechanged )
      {
        updateGroups();
        // because buttons need to be enabled disabled
        updateForm();      
        updateFormData();      
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
    
    handleBlackboardGroupSets( message )
    {
      bbgroupsetdata = message.payload;
      updateBlackboardGroupSets();
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

function updateResource( properties )
{
  let stagetext="Unknown Stage";
  switch ( properties.stage )
  {
    case "SETUP":
      stagetext = "Setting Up Stage";
      break;
    case "JOIN":
      stagetext = "Group Members Joining";
      break;
    case "DATAENTRY":
      stagetext = "Group Members Entering Data";
      break;
    case "RESULTS":
      stagetext = "Results Frozen";
      break;
  }
  console.log( "stage       = " + properties.stage       );
  console.log( "title       = " + properties.title       );
  console.log( "description = " + properties.description );

  let message = "";
  if ( finder.mainTitle.innerText !== properties.title )
  {
    if ( finder.mainTitle.innerText )
      addAlert( "Title changed to " + properties.title + "." );
      //message += "Title changed. ";
    finder.mainTitle.innerText        = properties.title;
  }
  if ( finder.mainDescription.innerText !== properties.description )
  {
    if ( finder.mainDescription.innerText )
      addAlert( "Description changed." );
      //message += "Description changed. ";
    finder.mainDescription.innerText  = properties.description;
  }
  if ( finder.mainStage.innerText !== stagetext )
  {
    if ( finder.mainStage.innerText )
      addAlert( "Stage changed to " + stagetext + "." );
    //message += "Stage changed to " + stagetext + ". ";
    finder.mainStage.innerText = stagetext;
  }
  
//  if ( message.length > 0 )
//    addAlert( message );
  
  finder.editpropsTitle.value       = properties.title;
  finder.editpropsDescription.value = properties.description;
  finder.editpropsStage.value = properties.stage;
}

function updateGroups()
{
  let html = "\n";
  finder.grouptablebody.innerHTML = html;
  finder.unattachedParticipants.innerHTML = "";
  for ( const gid of resource.groupIdsInOrder )
  {
    const g = resource.groupsById[gid];
    console.log( g );
    updateGroup( g );
  }
  updateUnattachedGroup();
  addAlert( "The table of groups on this page has been rebuilt." );
}
function updateUnattachedGroup()
{
  finder.unattachedParticipants.innerHTML = "";
  const set = resource.groupOfUnattached.membersbyid;
  let html = "";
  for ( let id in set )
  {
    html += set[id].name;
    html += "<br>";
  }
  finder.unattachedParticipants.innerHTML = html;
}

function isMemberOf( g )
{
  return g.hasOwnProperty( "membersbyid" ) && g.membersbyid.hasOwnProperty( dynamicData.myId );
}

function updateGroup( g )
{
  if ( !g.id )
  {
    updateUnattachedGroup();
    return;
  }
  
  resource.groupsById[g.id] = g;
  let row = finder[ "group-" + g.id ];
  if ( !row )
  {
    row = document.createElement( "tr" );
    row.id = "group-" + g.id;
    finder.grouptablebody.appendChild( row );
  }

  let html = "";
  html += "<td>";
  if ( dynamicData.allowedToManage )
  {
    html +=     "<button id=\"groupDeleteButton" + g.id + "\">Delete</button>\n";
    html +=     "<button id=\"groupEditButton"   + g.id + "\">Edit</button>\n";
  }
  
  if ( dynamicData.allowedToManage || isMemberOf( g ) )
    html += "<td><a id=\"groupViewLink"   + g.id + "\" href=\".\">" + g.title + "</a></td>\n";
  else
    html += "<td>" + g.title + "</td>\n";

  html += "<td>";
  let ingroup = false;
  let first = true;
  for ( const mid in g.membersbyid )
  {
    if ( first )
      first = false;
    else
      html += "<br>\n";
    let m = g.membersbyid[mid];
    if ( m.ltiId === dynamicData.myId )
      ingroup = true;
    html += m.name;
  }
  html += "</td>\n";
  html += "<td>";
  if ( dynamicData.allowedToParticipate && resource.properties.stage === "JOIN" && !isMemberOf(g) )
    html += "<button id=\"groupJoinButton" + g.id + "\" class=\"joinbutton\">Join</button>";
  html += "</td>";

  row.innerHTML = html;
  
  if ( dynamicData.allowedToManage )
  {
    var db = finder[ "groupDeleteButton" + g.id ];
    db.addEventListener( 'click', () => openGroupDeleteDialog( de, g.id ) );
    var de = finder[ "groupEditButton"   + g.id ];
    de.addEventListener( 'click', () => openGroupEditDialog( de, g.id ) );
  }
  if ( dynamicData.allowedToManage || isMemberOf( g ) )
  {
    var link = finder[ "groupViewLink"   + g.id ];
    link.addEventListener( 'click', ( e ) =>     
    {
      e.preventDefault();
      openDataEntryDialog( link, g.id ) ;
    }
            );
  }
  if ( resource.properties.stage === "JOIN" && !isMemberOf(g) && dynamicData.allowedToParticipate )  
    finder[ "groupJoinButton"   + g.id ].addEventListener( 'click', () => addMembership( g.id ) );
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
  for ( let m in group.membersbyid )
  {
    let th = document.createElement( "th" );
    th.className = "dataentry-formcell";
    th.innerHTML = group.membersbyid[m].name;
    finder.dataentryheadrow.append( th );
  }
  
  for ( let i=0; i<form.fieldIds.length; i++ )
  {
    let fieldid = form.fieldIds[i];
    let field = form.fields[fieldid];
    if ( !field ) continue;
    let row = document.createElement( "tr" );
    row.className = "dataentry-formrow";
    row.id = "dataentryrow-" + field.id;
    let td = document.createElement( "td" );
    td.innerText = field.description;
    row.append( td );
    for ( let m in group.membersbyid )
    {
      td = document.createElement( "td" );
      row.append( td );
      let inputid = "dataentrycell_" + fieldid + "_" + groupid + "_" + m;
      let input = document.createElement( "input" );
      input.size = 5;
      input.id = inputid;
      input.disabled = true;
      input.autocomplete = 'off';
      td.append( input );
      addFormInputListener( input, groupid, field, group.membersbyid[m] );      
    }
    finder.dataentrytablebody.append( row );
  }
  
  // Row at bottom with endorsement buttons...
  let endorserow = document.createElement( "tr" );
  endorserow.className = "dataentry-formrow";
  let managerendorserow = document.createElement( "tr" );
  managerendorserow.className = "dataentry-formrow";
  let td, tdm;
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
    td.className = "dataentry-formcell";
    p = document.createElement( "p" );
    p.id = "dataentry-endorsedate-" + m;
    td.append( p );
    endorserow.append( td );
    tdm = document.createElement( "td" );
    tdm.className = "dataentry-formcell";
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


function updateOverview()
{
  console.log( "updateOverview" );
  finder.overviewtablebody.innerHTML = '';
  for ( const gid of resource.groupIdsInOrder )
    updateOverviewGroup( resource.groupsById[gid] );
}

function updateOverviewGroup( g )
{
  console.log( "updateOverview group " + g.title );
  for ( let mid in g.membersbyid )
  {
    let m = g.membersbyid[mid];
    console.log( "updateOverview member " );
    console.log( m );
    let tr  = document.createElement( "tr" );
    let td = [];
    for ( let i=0; i<6; i++ )
    {
      td[i] = document.createElement( "td" );
      tr.append( td[i] );
    }
    td[0].innerText = g.title;
    td[1].innerText = m.name;
    for ( let i=2; i<6; i++ )
      td[i].innerText = '?';
    td[2].id = "overviewScore_"    + mid;
    td[3].id = "overviewEndorsed_" + mid;
    td[4].id = "overviewCount_"    + mid;
    td[5].id = "overviewTotal_"    + mid;
    finder.overviewtablebody.append( tr );
  }
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
  let groupcount=0;
  let grouptotal=0;
  let groupcomplete=true;
  
  for ( const mid in d.participantData )
  {
    groupcount++;
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
    if ( complete )
      grouptotal += total;
    else
      groupcomplete = false;
    let td = finder[ "overviewScore_" + mid ];
    if ( td ) td.innerText = complete ? total : "incomplete";
    
    td = finder[ "overviewEndorsed_" + mid ];
    if ( td )
    {
      if ( memberdata.endorsedDate )
        td.innerText = "Yes";
      else if ( memberdata.managerEndorsedDate )
        td.innerText = "Override";
      else
        td.innerText = "No";
    }
  }
  
  for ( const mid in d.participantData )
  {
    let td = finder[ "overviewCount_" + mid ];
    td.innerText = groupcount;
    td = finder[ "overviewTotal_" + mid ];
    td.innerText = groupcomplete?grouptotal:'incomplete';
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

function getImport( openerelement )
{
  toolsocket.sendMessage( new peergroupassessment.GetImportMessage() );
}

function getBlackboardGroupSets( openerelement )
{
  toolsocket.sendMessage( new peergroupassessment.GetBlackboardGroupSetsMessage() );
  arialib.openDialog( 'bbgroupsetsdialog', openerelement );  
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

window.addEventListener( "load", function(){ init(); } );
document.addEventListener( "DOMContentLoaded", function()
  { 
    console.log( "DOMContentLoaded event arrived." ); 
    console.log( finder.toplevelalert );
  } );
