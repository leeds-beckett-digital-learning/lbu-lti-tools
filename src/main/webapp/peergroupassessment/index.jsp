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

<jsp:useBean id="support" class="uk.ac.leedsbeckett.ltitools.peergroupassessment.PgaPageSupport" scope="request"/>
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
      td {
                   padding: 0.5em 2em 0.5em 2em;
      }
      
      .emptyinput {
        background-color: white;
      }

      .validinput {
        background-color: greenyellow;
      }

      .invalidinput {
        background-color: hotpink;
      }
      
    </style>
    <script lang="JavaScript">
      
const gendata = 
{
  myid:         '${support.personId}',
  myname:       '${support.personName}',
  manager:      ${support.allowedToManage},
  participant:  ${support.allowedToParticipate},
  wsuri:        '${support.websocketUri}'
};
        
    </script>
    <script type="module" src="../javascript/@BUILDTIMESTAMP@/peergroupassessment/index.js" defer></script>
  </head>
  <body>
    <div id="editprops" class="dialog">
      <div class="dialogcontent">
        <h3>Edit Properties</h3>
        <p><button id="editpropsSaveButtonTop" value="Save">Save</button>
          <button id="editpropsCloseButtonTop" value="Close">Close</button></p>
        <h4>Title</h4>
        <p><input id="editpropsTitle" size="30" value="loading..."/></p>
        <h4>Description</h4>
        <p><textarea id="editpropsDescription" cols="40" rows="10">...loading</textarea></p>
        <h4>Stage</h4>
        <p>
          <select id="editpropsStage">
            <option value="SETUP">Set Up</option>
            <option value="JOIN">Join Groups</option>
            <option value="DATAENTRY">Enter Data</option>
            <option value="RESULTS">Results Frozen</option>
          </select>
        </p>
        <p><button id="editpropsSaveButtonBottom" value="Save">Save</button>
          <button id="editpropsCloseButtonBottom" value="Close">Close</button></p>
      </div>
    </div>
    <div id="editgroupProps" class="dialog">
      <div class="dialogcontent">
        <h3>Edit Group Properties</h3>
        <p><button id="editgrouppropsSaveButtonTop" value="Save">Save</button>
          <button id="editgrouppropsCloseButtonTop" value="Close">Close</button></p>
        <h4>ID</h4>
        <p id="editgrouppropsId">Loading...</p>
        <h4>Title</h4>
        <p><input id="editgrouppropsTitle" size="30" value="loading..."/></p>
        <p><button id="editgrouppropsSaveButtonBottom" value="Save">Save</button>
          <button id="editgrouppropsCloseButtonBottom" value="Close">Close</button></p>
      </div>
    </div>

    <div id="dataentry" class="dialog">
      <div class="dialogcontent">
        <h3>Data Entry</h3>
        <p><button id="dataentryCloseButtonTop" value="Close">Close</button></p>
        <table>
          <thead>
            <tr id="dataentryheadrow"><th></th></tr>
          </thead>
          <tbody id="dataentrytablebody">
          </tbody>
        </table>
        <h3>Endorsement</h3>
        <p>When marks are entered and agreed, each participant must indicate 
          their endorsement of the marks using the button below. When the first
          participant endorses the marks, all the marks become read-only.
          An instructor can reset all the endorsements so you can edit marks
          and an instructor can endorse marks on behalf of participants who have
          been absent.</p>
        <p>
          <c:if test="${support.allowedToParticipate}">
            <button id="dataentryEndorseButton">Endorse</button>
          </c:if>
          <c:if test="${support.allowedToManage}">
            <button id="dataentryManagerEndorseButton">Endorse For All</button>
            <button id="dataentryClearEndorsementsButton">Reset Endorsements</button>
          </c:if>
        </p>
        <p><button id="dataentryCloseButtonBottom" value="Close">Close</button></p>
      </div>
    </div>

    <div id="debugdialog" class="dialog">
      <div class="dialogcontent">
        <h3>Debug Information</h3>
        <p><button id="debugdialogCloseButtonTop" value="Close">Close</button></p>
        <pre id="debugtext"></pre>
        <p><button id="debugdialogCloseButtonBottom" value="Close">Close</button></p>
      </div>
    </div>


    <h1>Peer Group Assessment Tool</h1>
    <p class="important">${support.importantMessage}</p>
    <h2 id="mainTitle">...loading</h2>
    <p id="mainDescription">...loading</p>
    <p class="stage"><span class="stage-label">Stage:</span> <span id="mainStage"></span></p>
    
    <!--
    <p>${support.personId} - ${support.personName}</p>
    -->
    
    <c:choose>
      <c:when test="${support.allowedToManage || support.allowedToParticipate}">
        <c:if test="${support.allowedToManage}">
          <p><button id="editpropertiesButton">Edit Properties</button></p>          
        </c:if>
        
        <table id="grouptable">
          <thead><tr><th></th><th>Group</th><th>Members</th><th></th></tr></thead>
          <tbody id="grouptablebody"></tbody>
          <c:if test="${support.allowedToManage}">        
            <tfoot><tr><td><button id="addgroupButton">Add Group</button></td><td></td><td></td><td></td></tr></tfoot>
          </c:if>
        </table>

        <h4>Unplaced Students</h4>
        <div id="unattachedParticipants"></div>
          
        <c:if test="${support.allowedToManage}">
          <h4>Overview of Students</h4>
          <p>A tables of all students and their scores.</p>
          <table id="overviewtable">
            <thead>
              <tr>
                <th>Group</th>
                <th>Name</th>
                <th>Score</th>
                <th>Endorsed</th>
                <th>Group<br />Count</th>
                <th>Group<br />Total</th>
              </tr>
            </thead>
            <tbody id="overviewtablebody"></tbody>
          </table>
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
        <p><button id="debugdialogButton">Debug Dialog</button></p>          
        <div><tt><pre>${support.dump}</pre></tt></div>
      </div>
    </c:if>
    
  </body>
</html>
