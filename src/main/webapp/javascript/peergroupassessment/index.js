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

let dyndata = gendata;

let toolsocket;

let loading;


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
  arialib.setDialogAlertClass( 'alertList' );
  arialib.setBaseAlertElement( finder.toplevelalert );
  finder.toplevelalert.role = 'alert';
  
  setInterval( alertUpdate, 1000 );
  
  if ( dyndata.manager )
    finder.addgroupButton              .addEventListener( 'click', () => addGroup()                     );
    
  if ( dyndata.manager )
    finder.editpropsSaveButtonBottom   .addEventListener( 'click', () => saveEditProps()                );
  finder.editpropsCloseButtonBottom    .addEventListener( 'click', () => arialib.closeDialog( finder.editpropsCloseButtonBottom )       );
  
  if ( dyndata.manager )
    finder.editgrouppropsSaveButtonBottom .addEventListener( 'click', () => saveEditGroupProps()                );
  finder.editgrouppropsCloseButtonBottom  .addEventListener( 'click', () => arialib.closeDialog(finder.editgrouppropsCloseButtonBottom)       );
  
  finder.dataentryCloseButtonTop           .addEventListener( 'click', () => arialib.closeDialog(finder.dataentryCloseButtonTop)       );
  finder.dataentryCloseButtonBottom        .addEventListener( 'click', () => arialib.closeDialog(finder.dataentryCloseButtonBottom)       );
  if ( dyndata.participant )
    finder.dataentryEndorseButton            .addEventListener( 'click', () => endorseData( false )           );
  if ( dyndata.manager )
  {
    finder.dataentryManagerEndorseButton     .addEventListener( 'click', () => endorseData( true )            );
    finder.dataentryClearEndorsementsButton  .addEventListener( 'click', () => clearEndorsements()            );
    finder.exportdialogCloseButtonTop        .addEventListener( 'click', () => arialib.closeDialog(finder.exportdialogCloseButtonTop)    );
    finder.exportdialogCloseButtonBottom     .addEventListener( 'click', () => arialib.closeDialog(finder.exportdialogCloseButtonBottom)    );
    finder.exportButton                      .addEventListener( 'click', () => getExport(finder.exportButton)                    );
  }
  
  finder.debugdialogCloseButtonTop     .addEventListener( 'click', () => arialib.closeDialog(finder.debugdialogCloseButtonTop)       );
  finder.debugdialogCloseButtonBottom  .addEventListener( 'click', () => arialib.closeDialog(finder.debugdialogCloseButtonBottom)       );

  if ( finder.editpropertiesButton )
    finder.editpropertiesButton.addEventListener( 'click', () => arialib.openDialog( 'editprops', finder.editpropertiesButton ) );
  if ( finder.debugdialogButton )
    finder.debugdialogButton   .addEventListener( 'click', () => openDebugDialog( finder.debugdialogButton )       );
  
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
      finder.exporttextarea.value = message.payload;
    }
  
  };
  
  toolsocket = new peergroupassessment.ToolSocket( dyndata.wsuri, handler  );  
}

function alertUpdate()
{
  let e = document.querySelector( 'ul[role = "alert"]' );
  if ( !e )
    return;

  let now = new Date().getTime();
  let nodelist = e.querySelectorAll( 'li' );
  let a = Array.from( nodelist );
  for ( let i=0; i<a.length; i++ )
  {
    let timestamp = new Number( a[i].dataset.timestamp );
    if ( (now - timestamp)/1000 > 10 )
      a[i].remove();
  }
}
  
function showAlert( text )
{  
  let e = document.querySelector( 'ul[role = "alert"]' );
  if ( !e )
    return;
  let li = document.createElement( 'li' );
  li.dataset.timestamp = new Date().getTime();
  li.innerText = text;
  e.append( li );
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
      showAlert( "Title changed to " + properties.title + "." );
      //message += "Title changed. ";
    finder.mainTitle.innerText        = properties.title;
  }
  if ( finder.mainDescription.innerText !== properties.description )
  {
    if ( finder.mainDescription.innerText )
      showAlert( "Description changed." );
      //message += "Description changed. ";
    finder.mainDescription.innerText  = properties.description;
  }
  if ( finder.mainStage.innerText !== stagetext )
  {
    if ( finder.mainStage.innerText )
      showAlert( "Stage changed to " + stagetext + "." );
    //message += "Stage changed to " + stagetext + ". ";
    finder.mainStage.innerText = stagetext;
  }
  
//  if ( message.length > 0 )
//    showAlert( message );
  
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
  let row = finder[ "group-" + g.id ];
  if ( !row )
  {
    row = document.createElement( "tr" );
    row.id = "group-" + g.id;
    finder.grouptablebody.appendChild( row );
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
    var db = finder[ "groupDeleteButton" + g.id ];
    db.addEventListener( 'click', () => alert( 'Not yet implemented.' ) );
    var de = finder[ "groupEditButton"   + g.id ];
    de.addEventListener( 'click', () => openGroupEditDialog( de, g.id ) );
  }
  if ( dyndata.manager || isMemberOf( g ) )
  {
    var link = finder[ "groupViewLink"   + g.id ];
    link.addEventListener( 'click', ( e ) =>     
    {
      e.preventDefault();
      openDataEntryDialog( link, g.id ) ;
    }
            );
  }
  if ( resource.properties.stage === "JOIN" && !isMemberOf(g) && dyndata.participant )  
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
          input.value = memberdatum.value;
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

function openGroupEditDialog( openerElement, gid )
{
  let g = resource.groupsById[gid];
  arialib.openDialog( 'editgroupProps', openerElement );
  finder.editgrouppropsId.value = gid;
  finder.editgrouppropsTitle.value = (g)?g.title:"Unknown Group";
}

function openDataEntryDialog( openerElement, gid )
{
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


init();
