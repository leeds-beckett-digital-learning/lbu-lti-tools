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

import peergroupassessment from "../generated/peergroupassessment.js";

let dyndata = gendata;

let toolsocket;

let loading;


let elements = {};

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

function init()
{
  let list = document.body.getElementsByTagName("*");
  for ( let i=0; i<list.length; i++ )
  {
    let e = list[i];
    if ( e.id )
    {
      console.log( "Found element with ID " + e.id );
      elements[e.id] = e;
    }
  }
  console.log( elements );

  if ( dyndata.manager )
    elements.addgroupButton              .addEventListener( 'click', () => addGroup()                     );
    
  if ( dyndata.manager )
  {
    elements.editpropsSaveButtonTop      .addEventListener( 'click', () => saveEditProps()                );
    elements.editpropsSaveButtonBottom   .addEventListener( 'click', () => saveEditProps()                );
  }
  elements.editpropsCloseButtonTop     .addEventListener( 'click', () => closeDialog('editprops')       );
  elements.editpropsCloseButtonBottom  .addEventListener( 'click', () => closeDialog('editprops')       );
  
  if ( dyndata.manager )
  {
    elements.editgrouppropsSaveButtonTop      .addEventListener( 'click', () => saveEditGroupProps()                );
    elements.editgrouppropsSaveButtonBottom   .addEventListener( 'click', () => saveEditGroupProps()                );
  }
  elements.editgrouppropsCloseButtonTop     .addEventListener( 'click', () => closeDialog('editgroupProps')       );
  elements.editgrouppropsCloseButtonBottom  .addEventListener( 'click', () => closeDialog('editgroupProps')       );
  
  elements.dataentryCloseButtonTop           .addEventListener( 'click', () => closeDialog('dataentry')       );
  elements.dataentryCloseButtonBottom        .addEventListener( 'click', () => closeDialog('dataentry')       );
  if ( dyndata.participant )
    elements.dataentryEndorseButton            .addEventListener( 'click', () => endorseData( false )           );
  if ( dyndata.manager )
  {
    elements.dataentryManagerEndorseButton     .addEventListener( 'click', () => endorseData( true )            );
    elements.dataentryClearEndorsementsButton  .addEventListener( 'click', () => clearEndorsements()            );
    elements.exportdialogCloseButtonTop        .addEventListener( 'click', () => closeDialog('exportdialog')    );
    elements.exportdialogCloseButtonBottom     .addEventListener( 'click', () => closeDialog('exportdialog')    );
    elements.exportButton                      .addEventListener( 'click', () => getExport()                    );
  }
  
  elements.debugdialogCloseButtonTop     .addEventListener( 'click', () => closeDialog('debugdialog')       );
  elements.debugdialogCloseButtonBottom  .addEventListener( 'click', () => closeDialog('debugdialog')       );

  if ( elements.editpropertiesButton )
    elements.editpropertiesButton.addEventListener( 'click', () => openDialog( 'editprops' )       );
  if ( elements.debugdialogButton )
    elements.debugdialogButton   .addEventListener( 'click', () => openDialog( 'debugdialog' )       );
  
  console.log( dyndata.wsuri );
  
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
      if ( dyndata.manager )
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
      if ( dyndata.manager )
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
      elements.exporttextarea.value = message.payload;
    }
  
  };
  
  toolsocket = new peergroupassessment.ToolSocket( dyndata.wsuri, handler  );  
}


function updateResource( properties )
{
  console.log( "stage       = " + properties.stage       );
  console.log( "title       = " + properties.title       );
  console.log( "description = " + properties.description );

  elements.mainTitle.innerHTML        = properties.title;
  elements.editpropsTitle.value       = properties.title;
  elements.mainDescription.innerHTML  = properties.description;
  elements.editpropsDescription.value = properties.description;
  switch ( properties.stage )
  {
    case "SETUP":
      elements.mainStage.innerHTML = "Setting Up Stage";
      break;
    case "JOIN":
      elements.mainStage.innerHTML = "Group Members Joining";
      break;
    case "DATAENTRY":
      elements.mainStage.innerHTML = "Group Members Entering Data";
      break;
    case "RESULTS":
      elements.mainStage.innerHTML = "Results Frozen";
      break;
  }
  elements.editpropsStage.value = properties.stage;
}

function updateGroups()
{
  let html = "\n";
  elements.grouptablebody.innerHTML = html;
  elements.unattachedParticipants.innerHTML = "";
  for ( const gid of resource.groupIdsInOrder )
  {
    const g = resource.groupsById[gid];
    console.log( g );
    updateGroup( g );
  }
  updateUnattachedGroup();
}
function updateUnattachedGroup()
{
  elements.unattachedParticipants.innerHTML = "";
  const set = resource.groupOfUnattached.membersbyid;
  let html = "";
  for ( let id in set )
  {
    html += set[id].name;
    html += "<br>";
  }
  elements.unattachedParticipants.innerHTML = html;
}

function isMemberOf( g )
{
  return g.hasOwnProperty( "membersbyid" ) && g.membersbyid.hasOwnProperty( dyndata.myid );
}

function updateGroup( g )
{
  if ( !g.id )
  {
    updateUnattachedGroup();
    return;
  }
  
  resource.groupsById[g.id] = g;
  let row = document.getElementById( "group-" + g.id );
  if ( !row )
  {
    row = document.createElement( "tr" );
    row.id = "group-" + g.id;
    elements.grouptablebody.appendChild( row );
  }

  let html = "";
  html += "<td>";
  if ( dyndata.manager )
  {
    html +=     "<button id=\"groupDeleteButton" + g.id + "\">Delete</button>\n";
    html +=     "<button id=\"groupEditButton"   + g.id + "\">Edit</button>\n";
  }
  
  if ( dyndata.manager || isMemberOf( g ) )
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
    if ( m.ltiId === dyndata.myid )
      ingroup = true;
    html += m.name;
  }
  html += "</td>\n";
  html += "<td>";
  if ( dyndata.participant && resource.properties.stage === "JOIN" && !isMemberOf(g) )
    html += "<button id=\"groupJoinButton" + g.id + "\" class=\"joinbutton\">Join</button>";
  html += "</td>";

  row.innerHTML = html;
  
  if ( dyndata.manager )
  {
    document.getElementById( "groupDeleteButton" + g.id ).addEventListener( 'click', () => alert( g.id ) );
    document.getElementById( "groupEditButton"   + g.id ).addEventListener( 'click', () => openGroupEditDialog( g.id ) );
  }
  if ( dyndata.manager || isMemberOf( g ) )
  {
    document.getElementById( "groupViewLink"   + g.id ).addEventListener( 'click', ( e ) =>     
    {
      e.preventDefault();
      openDataEntryDialog( g.id ) ;
    }
            );
  }
  if ( resource.properties.stage === "JOIN" && !isMemberOf(g) && dyndata.participant )  
    document.getElementById( "groupJoinButton"   + g.id ).addEventListener( 'click', () => addMembership( g.id ) );
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
    groupid = resource.groupIdsByMember[dyndata.myid];
  console.log( "group id = " + groupid );
  if ( !groupid )
    return;
  
  let group = resource.groupsById[groupid];
  for ( let m in group.membersbyid )
  {
    let th = document.createElement( "th" );
    th.className = "dataentry-formcell";
    th.innerHTML = group.membersbyid[m].name;
    elements.dataentryheadrow.append( th );
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
      td.append( input );
      addFormInputListener( input, groupid, field, group.membersbyid[m] );      
    }
    elements.dataentrytablebody.append( row );
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
  elements.dataentrytablebody.append( endorserow );
  elements.dataentrytablebody.append( managerendorserow );  
  formuptodate = true;
}

function updateFormData()
{  
  let groupid = selectedgroupid;
  if ( !groupid )
    groupid = resource.groupIdsByMember[dyndata.myid];
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
      let input = document.getElementById( inputid );
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
          input.value = memberdatum.value;
      }
    }
  }
  
  for ( let m in group.membersbyid )
  {
    let p;
    let memberdata = undefined;
    if ( data ) memberdata = data.participantData[m];
    p = document.getElementById("dataentry-endorsedate-" + m);
    if ( memberdata && memberdata.endorsedDate )
      p.innerHTML = memberdata.endorsedDate.replace( " ", "<br>" );
    else
      p.innerHTML = 'Not Endorsed';
    p = document.getElementById("dataentry-managerendorsedate-" + m);
    if ( memberdata && memberdata.managerEndorsedDate )
      p.innerHTML = memberdata.managerEndorsedDate.replace( " ", "<br>" );
    else
      p.innerHTML = '';
  }
  
  
}


function updateOverview()
{
  console.log( "updateOverview" );
  elements.overviewtablebody.innerHTML = '';
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
    elements.overviewtablebody.append( tr );
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
    let td = document.getElementById( "overviewScore_" + mid );
    if ( td ) td.innerText = complete ? total : "incomplete";
    
    td = document.getElementById( "overviewEndorsed_" + mid );
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
    let td = document.getElementById( "overviewCount_" + mid );
    td.innerText = groupcount;
    td = document.getElementById( "overviewTotal_" + mid );
    td.innerText = groupcomplete?grouptotal:'incomplete';
  }
}

function openDialog( id )
{
  let dialog = document.getElementById( id );
  if ( dialog === null )
  {
    alert( "Programmer error - unknown dialog id: " + id );
    return;
  }
  dialog.style.display = "block";
}
function closeDialog( id )
{
  let dialog = document.getElementById( id );
  if ( dialog === null )
  {
    alert( "Programmer error - unknown dialog id: " + id );
    return;
  }
  dialog.style.display = "none";
}

function openGroupEditDialog( gid )
{
  elements.editgrouppropsId.innerHTML = gid;
  let g = resource.groupsById[gid];
  elements.editgrouppropsTitle.value = (g)?g.title:"Unknown Group";
  openDialog( 'editgroupProps' );
}

function openDataEntryDialog( gid )
{
  selectedgroupid = gid;
  clearForm();
  updateForm();
  toolsocket.sendMessage( new peergroupassessment.GetDataMessage( gid ) );
      
  openDialog( 'dataentry' );
}

function openDebugDialog()
{
  let pre = document.getElementById( "debugtext" );
  pre.innerHTML = "testing...";  
  openDialog( 'debugdialog' );
}

function saveEditProps()
{
  toolsocket.sendMessage( new peergroupassessment.SetResourcePropertiesMessage( 
          elements.editpropsTitle.value, 
          elements.editpropsDescription.value, 
          elements.editpropsStage.value ) );
  closeDialog( "editprops" );
}

function saveEditGroupProps()
{
  toolsocket.sendMessage( new peergroupassessment.SetGroupPropertiesMessage( 
          elements.editgrouppropsId.innerHTML, 
          elements.editgrouppropsTitle.value ) );
  closeDialog( "editgroupProps" );
}

function endorseData( manager )
{
  let groupid = selectedgroupid;
  if ( !groupid )
    groupid = resource.groupIdsByMember[dyndata.myid];
  toolsocket.sendMessage( new peergroupassessment.EndorseDataMessage( groupid, manager ) );  
}

function clearEndorsements()
{
  let groupid = selectedgroupid;
  if ( !groupid )
    groupid = resource.groupIdsByMember[dyndata.myid];
  toolsocket.sendMessage( new peergroupassessment.ClearEndorsementsMessage( groupid ) );  
}

function addGroup()
{
  toolsocket.sendMessage( new peergroupassessment.AddGroupMessage() );
}

function addMembership( gid )
{
  let pids = [];
  pids[0] = {};
  pids[0].ltiId = dyndata.myid;
  pids[0].name  = dyndata.myname;

  toolsocket.sendMessage( new peergroupassessment.MembershipMessage( gid, pids ) );
}

function getExport()
{
  if ( resource.properties.stage !== "RESULTS" )
  {
    alert( "Result export is only available in the final stage when results are frozen." );
    return;
  }
  elements.exporttextarea.innerText = "Waiting for data...";
  toolsocket.sendMessage( new peergroupassessment.GetExportMessage() );
  openDialog( 'exportdialog' );  
}

init();
