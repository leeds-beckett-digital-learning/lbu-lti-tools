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

  console.log( wsuri );
  socket = new WebSocket( wsuri );
  socket.addEventListener( 'open',    (event) => 
  {
    socket.send( messageToString( "getresource" ) );
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
  var html = "<tr><th>Title</th><th>Members</th><th></th></tr>\n";
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
    row.innerHTML = "<tr><td colspan=\"2\"><button onclick=\"addGroup()\">Add Group</button></td></tr>\n";
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

  html = "<td><span>" + g.title + "</span>";
  if ( manager )
    html += " <button onclick=\"openGroupEditDialog( '" + g.id + "' )\">Edit</button>";
  html += "</td><td>";
  var ingroup = false;
  for ( const mid in g.membersbyid )
  {
    console.log( mid );
    m = g.membersbyid[mid];
    console.log( m );
    if ( m.ltiId === myid )
      ingroup = true;
    console.log( `Comparing ${m.ltiId} with ${myid} ingroup = ${ingroup}` );
    html += m.name + "<br>\n";
  }
  html += "</td>\n<td>";
  if ( participant && resource.properties.stage === "JOIN" && !ingroup )
    html += "<button class=\"joinbutton\" onclick=\"addMembership( '" + g.id + "')\">Join</button>";
  html += "</td>\n";

  row.innerHTML = html;
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


