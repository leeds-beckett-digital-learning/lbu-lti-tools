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


var socket;
var loading;
var nextid = 100000000;
var mainTitle;
var mainDescription;
var editpropsTitle;
var editpropsDescription;

var resource = 
        {
          "resourceKey":{"platformId":"https://my-test.leedsbeckett.ac.uk/","resourceId":"_137_1"},
          "properties":{"title":"","description":"","stage":"SETUP"},
          "groupsById":{},
          "groupOfUnattached":{"id":null,"title":null,"membersbyid":{}},
          "groupIdsByMember":{}
        };

var selectedgroupid;

 function init()
{
  mainTitle            = document.getElementById( "main-title" );
  mainDescription      = document.getElementById( "main-description" );
  mainStage            = document.getElementById( "main-stage" );
  editpropsTitle       = document.getElementById( "editprops-title" );
  editpropsDescription = document.getElementById( "editprops-description" );
  editpropsStage       = document.getElementById( "editprops-stage" );
  editgrouppropsId     = document.getElementById( "editgroupprops-id" );
  editgrouppropsTitle  = document.getElementById( "editgroupprops-title" );
  grouptable           = document.getElementById( "grouptable" );
  dataentrytablebody   = document.getElementById( "dataentry-tablebody" );
  dataentryheadrow     = document.getElementById( "dataentry-headrow" );

  console.log( wsuri );
  socket = new WebSocket( wsuri );
  socket.addEventListener( 'open',    (event) => 
  {
    socket.send( messageToString( "getresource" ) );
    socket.send( messageToString( "getform" ) );
  });

  socket.addEventListener( 'close', (event) => 
  {
    if ( event.wasClean )
      alert( `Connection to service was closed with code = ${event.code} reason = ${event.reason}` );
    else
      alert( "Connection to service was closed abruptly." );
  });

  socket.addEventListener( 'error', (event) => 
  {
    alert( `Web Socket error. ${event.message}` );
  });

  socket.addEventListener( 'message', (event) => 
  {
    console.log( 'Message from server: ', event.data);          var message = stringToMessage( event.data );
    console.log( message );
    if ( message.valid )
    {
      switch ( message.messageType )
      {
        case "resource":
          resource = message.payload;
          updateResource( resource.properties );
          updateGroups();
          updateSelf();
          break;
        case "resourceproperties":
          resource.properties = message.payload;
          updateResource( resource.properties );
          break;
        case "group":
          updateGroup( message.payload );
          break;
        case "formanddata":
          form = message.payload.form;
          if ( message.payload.data.key.groupId === selectedgroupid )
            data = message.payload.data;
          console.log( form );
          console.log( data );
          updateForm();
          break;
        case "data":
          if ( message.payload.key.groupId === selectedgroupid )
          {
            data = message.payload;
            console.log( data );
            updateForm();
          }
          break;
      }
    }
  });
}

function updateSelf()
{
  if ( !participant )
    return;
  
  var gid = resource.groupIdsByMember[myid];
  console.log( "updateSelf() " + gid );
  if ( gid === undefined )
    console.log( "Need to add self." );
}

function updateResource( properties )
{
  console.log( "stage       = " + properties.stage       );
  console.log( "title       = " + properties.title       );
  console.log( "description = " + properties.description );

  mainTitle.innerHTML        = properties.title;
  editpropsTitle.value       = properties.title;
  mainDescription.innerHTML  = properties.description;
  editpropsDescription.value = properties.description;
  switch ( properties.stage )
  {
    case "SETUP":
      mainStage.innerHTML = "Setting Up Stage";
      break;
    case "JOIN":
      mainStage.innerHTML = "Group Members Joining";
      break;
    case "DATAENTRY":
      mainStage.innerHTML = "Group Members Entering Data";
      break;
    case "RESULTS":
      mainStage.innerHTML = "Results Frozen";
      break;
  }
  editpropsStage.value = properties.stage;
}

function updateGroups()
{
  var html = "<tr><th colspan=\"4\">Title</th><th colspan=\"3\">Members</th></tr>\n";
  grouptable.innerHTML = html;
  for ( const gid in resource.groupsById )
  {
    const g = resource.groupsById[gid];
    console.log( g );
    updateGroup( g );
  }
  if ( manager )
  {
    row = document.createElement( "tr" );
    row.innerHTML = "<tr><td colspan=\"3\"><button onclick=\"addGroup()\">Add Group</button></td></tr>\n";
    grouptable.appendChild( row );
  }  
}

function updateGroup( g )
{
  if ( !g.id )
  {
    alert( "Trying to update unattached group but not implemented." );
    return;
  }
  resource.groupsById[g.id] = g;
  var row = document.getElementById( "group-" + g.id );
  if ( !row )
  {
    row = document.createElement( "tr" );
    row.id = "group-" + g.id;
    grouptable.appendChild( row );
  }

  html = "";
  html += "<td><button onclick=\"alert( '" + g.id + "' )\">Delete</button></td>\n";
  html += "<td><button onclick=\"openGroupEditDialog( '" + g.id + "' )\">Edit</button></td>\n";
  //if ( participant && resource.properties.stage === "DATAENTRY" && ingroup )
  html += "<td><button onclick=\"openDataEntryDialog( '" + g.id + "' )\">View</button></td>\n";
  html += "<td><span>" + g.title + "</span></td>\n";

  html += "<td>";
  var ingroup = false;
  var first = true;
  for ( const mid in g.membersbyid )
  {
    if ( first )
      first = true;
    else
      html += "<br>\n";
    m = g.membersbyid[mid];
    if ( m.ltiId === myid )
      ingroup = true;
    html += m.name;
  }
  html += "</td>\n";
  //if ( participant && resource.properties.stage === "JOIN" && !ingroup )
  html += "<td><button class=\"joinbutton\" onclick=\"addMembership( '" + g.id + "')\">Join</button></td>";

  row.innerHTML = html;
}

function clearForm()
{
  var formrows = document.getElementsByClassName( "dataentry-formrow" );
  console.log( formrows );
  console.log( formrows.length );
  while ( formrows.length > 0 )
  {
    var formrow = formrows[0];
    console.log( formrow );
    formrow.remove();
  }
  var formcells = document.getElementsByClassName( "dataentry-formcell" );
  while ( formcells.length > 0 )
  {
    var formcell = formcells[0];
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
  var datum = {};
  datum.groupId = gid;
  datum.fieldId = f.id;
  datum.memberId = m.ltiId;
  datum.value = e.value;
  socket.send( messageToString( 
      "changedatum", 
      "uk.ac.leedsbeckett.ltitools.tool.peergroupassessment.PeerGroupChangeDatum",
      datum ) );
}

function addFormInputListener( e, gid, f, m )
{
  e.addEventListener('input', function () { processFormInputEvent( e, gid, f, m ); } );  
}

function updateForm()
{
  clearForm();
  
  var groupid = selectedgroupid;
  if ( !groupid )
    groupid = resource.groupIdsByMember[myid];
  console.log( "group id = " + groupid );
  if ( !groupid )
    return;
  
  var group = resource.groupsById[groupid];
  for ( var m in group.membersbyid )
  {
    var th = document.createElement( "th" );
    th.className = "dataentry-formcell";
    th.innerHTML = group.membersbyid[m].name;
    dataentryheadrow.append( th );
  }
  
  for ( var i=0; i<form.fieldIds.length; i++ )
  {
    var fieldid = form.fieldIds[i];
    var field = form.fields[fieldid];
    if ( !field ) continue;
    var row = document.createElement( "tr" );
    row.className = "dataentry-formrow";
    row.id = "dataentryrow-" + field.id;
    var td = document.createElement( "td" );
    td.innerText = field.description;
    row.append( td );
    for ( var m in group.membersbyid )
    {
      var memberdata = undefined;
      var memberdatum = undefined;
      if ( data ) memberdata = data.participantData[m];
      if ( memberdata ) memberdatum = memberdata.participantData[fieldid];
      td = document.createElement( "td" );
      row.append( td );
      var inputid = "dataentrycell_" + groupid + "_" + m;
      var input = document.createElement( "input" );
      input.size = 5;
      input.id = inputid;
      if ( memberdatum && memberdatum.value )
        input.value = memberdatum.value;
      td.append( input );
      addFormInputListener( input, groupid, field, group.membersbyid[m] );
      
    }
    dataentrytablebody.append( row );
  }
}

function openDialog( id )
{
  var dialog = document.getElementById( id );
  if ( dialog === null )
  {
    alert( "Programmer error - unknown dialog id: " + id );
    return;
  }
  dialog.style.display = "block";
}
function closeDialog( id )
{
  var dialog = document.getElementById( id );
  if ( dialog === null )
  {
    alert( "Programmer error - unknown dialog id: " + id );
    return;
  }
  dialog.style.display = "none";
}

function openGroupEditDialog( gid )
{
  editgrouppropsId.innerHTML = gid;
  var g = resource.groupsById[gid];
  editgrouppropsTitle.value = (g)?g.title:"Unknown Group";
  openDialog( 'editgroupprops' );
}

function openDataEntryDialog( gid )
{
  selectedgroupid = gid;
  socket.send( messageToString( 
      "getformanddata", 
      "java.lang.String",
      selectedgroupid ) );
      
  clearForm();
  openDialog( 'dataentry' );;
}

function saveEditProps()
{
  var payload = new Object();
  payload.stage = editpropsStage.value;
  payload.title = editpropsTitle.value;
  payload.description = editpropsDescription.value;
  socket.send( messageToString( 
      "setresourceproperties", 
      "uk.ac.leedsbeckett.ltitools.tool.peergroupassessment.PeerGroupResourceProperties",
      payload ) );
  closeDialog( "editprops" );
}

function saveEditGroupProps()
{
  var payload = new Object();
  payload.id = editgrouppropsId.innerHTML;
  payload.title = editgrouppropsTitle.value;
  socket.send( messageToString( 
      "setgroupproperties", 
      "uk.ac.leedsbeckett.ltitools.tool.peergroupassessment.PeerGroupChangeGroup",
      payload ) );
  closeDialog( "editgroupprops" );
}

function addGroup()
{
  var payload = new Object();
  socket.send( messageToString( 
      "addgroup", 
      "uk.ac.leedsbeckett.ltitools.tool.websocket.EmptyPayload",
      payload ) );
}

function addMembership( gid )
{
  var payload  = {};
  payload.id   = gid;
  payload.pids = [];
  payload.pids[0] = {};
  payload.pids[0].ltiId = myid;
  payload.pids[0].name  = myname;
  
  socket.send( messageToString( 
      "membership", 
      "uk.ac.leedsbeckett.ltitools.tool.peergroupassessment.PeerGroupAddMembership",
      payload ) );  
}

function messageToString( messageType, payloadType, payload, replyToId  )
{
  var str = "toolmessageversion1.0\n";
  str += "id:" + nextid++ + "\n";
  if ( replyToId )
    str += "replytoid:" + replyToId + "\n";
  if ( messageType )
    str += "messagetype:" + messageType + "\n";
  if ( payloadType && payload )
  {
    str += "payloadtype:" + payloadType + "\npayload:\n" ;
    str += JSON.stringify( payload );
  }
  return str;
}

function stringToMessage( str )
{
  var sig = "toolmessageversion1.0";
  var header, linesplit, name, value;
  var message = new Object();
  var started = false;
  const regex = RegExp('(.*)[\n\r]+', 'gm');

  message.valid = false;
  console.log( message );
  while ( true )
  {
    linesplit = regex.exec( str );
    if ( linesplit )
      header = linesplit[1];
    else
      break;          
    if ( !started )
    {
      started = true;
      if ( sig === header )
        continue;
      else
        return message;
    }
    n = header.indexOf( ":" );
    if ( n > 0 )
    {
      name = header.substring( 0, n );
      value = header.substring( n+1 );
      if ( name === "id" )
        message.id = value;
      else if ( name === "replytoid" )
        message.replyToId = value;
      else if ( name === "messagetype" )
        message.messageType = value;
      else if ( name === "payloadtype" )
        message.payloadType = value;
      else if ( name === "payload" )
      {
        var payload = str.substring( regex.lastIndex );
        message.payload = JSON.parse( payload );
        break;
      }
    }
  }

  if ( message.id && message.messageType )
    message.valid = true;

  return message;
}


