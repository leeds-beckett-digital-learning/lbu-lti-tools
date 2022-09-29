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
      
const myid        = '${support.personId}';
const myname      = '${support.personName}';
const manager     = ${support.allowedToManage};
const participant = ${support.allowedToParticipate};
const wsuri       = '${support.websocketUri}';

${support.messagingScript}
        
    </script>
    <script lang="JavaScript" src="index.js"></script>
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
        <p><button onclick="saveEditProps();" value="Save">Save</button>
          <button onclick="closeDialog('editprops');" value="Close">Close</button></p>
        <h4>Title</h4>
        <p><input id="editprops-title" size="30" value="loading..."/></p>
        <h4>Description</h4>
        <p><textarea id="editprops-description" cols="40" rows="10">...loading</textarea></p>
        <h4>Stage</h4>
        <p>
          <select id="editprops-stage">
            <option value="SETUP">Set Up</option>
            <option value="JOIN">Join Groups</option>
            <option value="DATAENTRY">Enter Data</option>
            <option value="RESULTS">Results Frozen</option>
          </select>
        </p>
        <p><button onclick="saveEditProps();" value="Save">Save</button>
          <button onclick="closeDialog('editprops');" value="Close">Close</button></p>
      </div>
    </div>
    <div id="editgroupprops" class="dialog">
      <div class="dialogcontent">
        <h3>Edit Group Properties</h3>
        <p><button onclick="saveEditGroupProps();" value="Save">Save</button>
          <button onclick="closeDialog('editgroupprops');" value="Close">Close</button></p>
        <h4>ID</h4>
        <p id="editgroupprops-id">Loading...</p>
        <h4>Title</h4>
        <p><input id="editgroupprops-title" size="30" value="loading..."/></p>
        <p><button onclick="saveEditGroupProps();" value="Save">Save</button>
          <button onclick="closeDialog('editgroupprops');" value="Close">Close</button></p>
      </div>
    </div>

    <div id="dataentry" class="dialog">
      <div class="dialogcontent">
        <h3>Data Entry</h3>
        <p><button onclick="closeDialog('dataentry');" value="Close">Close</button></p>
        <table>
          <thead>
            <tr id="dataentry-headrow"><th></th></tr>
          </thead>
          <tbody id="dataentry-tablebody">
          </tbody>
        </table>
        <p><button onclick="closeDialog('dataentry');" value="Close">Close</button></p>
      </div>
    </div>

    <div id="debugdialog" class="dialog">
      <div class="dialogcontent">
        <h3>Debug Information</h3>
        <p><button onclick="closeDialog('debugdialog');" value="Close">Close</button></p>
        <pre id="debugtext"></pre>
        <p><button onclick="closeDialog('debugdialog');" value="Close">Close</button></p>
      </div>
    </div>


    <h1>Peer Group Assessment Tool</h1>
    <p class="important">${support.importantMessage}</p>
    <h2 id="main-title">...loading</h2>
    <p id="main-description">...loading</p>
    <p class="stage"><span class="stage-label">Stage:</span> <span id="main-stage"></span></p>
    
    <p>${support.personId} - ${support.personName}</p>
    
    <c:choose>
      <c:when test="${support.allowedToManage || support.allowedToParticipate}">
        <c:if test="${support.allowedToManage}">
          <p><button onclick="openDialog( 'editprops' )">Edit Properties</button></p>          
          <p>You can manage this resource.</p>
        </c:if>
        
        <c:if test="${support.allowedToParticipate}">
          <p>You can use this resource by joining a group and participating in peer group assessment.</p>
        </c:if>
        
        <table id="grouptable">
        </table>

        <h4>Unplaced Students</h4>
        <p><c:forEach var="m" items="${support.pgaResource.groupOfUnattached.members}">${m.name}<br></c:forEach></p>
          
      </c:when>
      <c:otherwise>
        <p>Your role set by system that launched this tool means that you
        can neither participate or manage this resource.</p>
      </c:otherwise>
    </c:choose>
    
    <p><button onclick="openDebugDialog()">Debug Dialog</button></p>          
    
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
