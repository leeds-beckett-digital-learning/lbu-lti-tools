<%-- 
    Document   : index
    Created on : 18 Nov 2021, 08:56:17
    Author     : jon

    The 'home' page for the peergroupassessment tool.
    Same starting point for instructors and students.

--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<jsp:useBean id="support" class="uk.ac.leedsbeckett.ltitools.tool.peergroupassessment.PeerGroupPageSupport" scope="request"/>
<% support.setRequest( request ); %>

<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Peer Group Assessment</title>
    <style>
      body { font-family: sans-serif; padding: 1em 1em 1em 1em; }
      .stage {}
      .stage-label { font-weight: bold }
      .important { background-color: yellow }
      .dialog { 
                   display: none; 
                   position: fixed;
                   background-color: rgb(0,0,0);
                   background-color: rgba(0,0,0,0.4);                   
                   left: 0;
                   top: 0;
                   width: 100%;
                   height: 100%;
                   overflow: auto;
                   z-index: 1000; }
      .dialogcontent {
                   margin: 2em 2em 1em 4em; 
                   padding: 1em 1em 1em 1em;
                   box-shadow: 0.5em 0.5em 0.5em 0.25em black;
                   min-height: 10em;
                   background: white; }
      
    </style>
    <script lang="JavaScript">
      
      var socket;
      var loading;
      var nextid = 100000000;
      var mainTitle;
      var mainDescription;
      var editpropsTitle;
      var editpropsDescription;
      
      function init()
      {
        mainTitle            = document.getElementById( "main-title" );
        mainDescription      = document.getElementById( "main-description" );
        editpropsTitle       = document.getElementById( "editprops-title" );
        editpropsDescription = document.getElementById( "editprops-description" );
        
        var wsuri = "${support.websocketUri}";
        console.log( wsuri );
        socket = new WebSocket( wsuri );
        socket.addEventListener( 'open',    (event) => 
        {
          socket.send( messageToString( "getresource" ) );
        });
        socket.addEventListener( 'message', (event) => 
        {
          console.log( 'Message from server: ', event.data);
          var message = stringToMessage( event.data );
          console.log( message );
          if ( message.valid )
          {
            switch ( message.messageType )
            {
              case "resource":
                updateResource( message.payload );
                break;
            }
          }
        });
      }
      
      function updateResource( resource )
      {
        console.log( "stage       = " + resource.properties.stage       );
        console.log( "title       = " + resource.properties.title       );
        console.log( "description = " + resource.properties.description );
        
        mainTitle.innerHTML        = resource.properties.title;
        editpropsTitle.value       = resource.properties.title;
        mainDescription.innerHTML  = resource.properties.description;
        editpropsDescription.value = resource.properties.description;
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

      function saveEditProps()
      {
        var payload = new Object();
        payload.stage = "SETUP";
        payload.title = editpropsTitle.value;
        payload.description = editpropsDescription.value;
        socket.send( messageToString( 
            "setresourceproperties", 
            "uk.ac.leedsbeckett.ltitools.tool.peergroupassessment.PeerGroupResourceProperties",
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
    </script>
  </head>
  <body>
    <div id="loadingdialog" class="dialog">
      <div class="dialogcontent">
        <h3>Loading...</h3>
        <p><button onclick="closeDialog('editprops');" value="Close">Close</button></p>
      </div>
    </div>
    <div id="editprops" class="dialog">
      <div class="dialogcontent">
        <h3>Edit Properties</h3>
        <p><button onclick="alert('Not implemented yet.');" value="Save">Save</button>
          <button onclick="closeDialog('editprops');" value="Close">Close</button></p>
        <h4>Title</h4>
        <p><input id="editprops-title" size="30" value="loading..."/></p>
        <h4>Description</h4>
        <p><textarea id="editprops-description" cols="40" rows="10">...loading</textarea></p>
        <p><button onclick="saveEditProps();" value="Save">Save</button>
          <button onclick="closeDialog('editprops');" value="Close">Close</button></p>
      </div>
    </div>

    <h1>Peer Group Assessment Tool</h1>
    <p class="important">${support.importantMessage}</p>
    <p>This tool has not been implemented yet.</p>
    <h2 id="main-title">...loading</h2>
    <p id="main-description">...loading</p>
    
    <c:choose>
      <c:when test="${support.allowedToManage || support.allowedToParticipate}">
        <c:if test="${support.allowedToManage}">
          <p class="stage"><span class="stage-label">Stage:</span>
            <c:choose>
              <c:when test="${support.pgaResource.setupStage}">Setting Up</c:when>
              <c:when test="${support.pgaResource.joinStage}">Students Joining Groups</c:when>
              <c:when test="${support.pgaResource.dataEntryStage}">Students Entering Scores</c:when>
              <c:when test="${support.pgaResource.resultsStage}">Results Frozen</c:when>
              <c:otherwise>ERROR - cannot determine stage.</c:otherwise>
            </c:choose>
          </p>
          <p><a onclick="openDialog( 'editprops' )">Edit Properties</a></p>
          
          <p>You can manage this resource.</p>
          <table>
            <tr><th>ID</th><th>Title</th><th>Members</th></tr>
            <c:forEach var="g" items="${support.pgaResource.groups}">
              <tr><td>${g.id}</td><td>${g.title}</td>
                <td><c:forEach var="m" items="${g.members}">${m.name}<br></c:forEach></td>
              </tr>
            </c:forEach>
            <tr><td colspan="2">Button to add a group will go here.</td></tr>
          </table>

          <h4>Unplaced Students</h4>
          <p><c:forEach var="m" items="${support.pgaResource.groupOfUnattached.members}">${m.name}<br></c:forEach></p>
          
        </c:if>
        
        <c:if test="${support.allowedToParticipate}">
          <p>You can use this resource by joining a group and participating in peer group assessment.</p>
        </c:if>
        
      </c:when>
      <c:otherwise>
        <p>Your role set by system that launched this tool means that you
        can neither participate or manage this resource.</p>
      </c:otherwise>
    </c:choose>
    
    <c:if test="${support.debugging}">
      <div style="margin-top: 10em;">
        <hr>
        <div><tt><pre>${support.dump}</pre></tt></div>
      </div>
    </c:if>

    <script>
      init();
    </script>
  </body>
</html>
