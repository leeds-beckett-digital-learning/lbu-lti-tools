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

<jsp:useBean id="support" class="uk.ac.leedsbeckett.ltitools.selfenrol.SePageSupport" scope="request"/>
<% support.setRequest( request ); %>

<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Self Enrol</title>
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


.alertList {
  padding: 10px;
  border: 2px solid hsl(206deg 74% 54%);
  border-radius: 4px;
  background: hsl(206deg 74% 90%);
}

.alertList:empty {
  display: none;
  padding: 10px;
  border: 2px white;
  border-radius: 4px;
  background: white;
}

      
    </style>
    <link rel="stylesheet" href="../style/dialog.css"/>
    <script lang="JavaScript">
      
const gendata = 
{
  myid:         '${support.personId}',
  myname:       '${support.personName}',
  manager:      ${support.allowedToManage},
  participant:  ${support.allowedToParticipate},
  wsuri:        '${support.websocketUri}',
  debugging:    ${support.debugging}
};
        
    </script>
    <script type="module" src="../javascript/@BUILDTIMESTAMP@/selfenrol/index.js"></script>
  </head>
  <body>
    <div id="dialogdiv" class="dialogs">
      <div role="dialog" id="editprops" aria-labelledby="editpropslabel" aria-modal="true" class="hidden">
        <h3 id="editpropslabel" class="dialog_label">Edit Properties</h3>
        <div class="dialog_form">
          <div class="dialog_form_item">
            <label>
              <span class="label_text">Title:</span>
              <input id="editpropsTitle" size="30"/>
            </label>
          </div>
          <div class="dialog_form_item">
            <label>
              <span class="label_text">Description:</span>
              <textarea id="editpropsDescription" cols="40" rows="10">...loading</textarea>
            </label>
          </div>
          <div class="dialog_form_item">
            <label>
              <span class="label_text">Stage:</span>
              <select id="editpropsStage">
                <option value="SETUP">Set Up</option>
                <option value="JOIN">Join Groups</option>
                <option value="DATAENTRY">Enter Data</option>
                <option value="RESULTS">Results Frozen</option>
              </select>
            </label>
          </div>
        </div>
        <div class="dialog_form_actions">    
          <button id="editpropsSaveButtonBottom" value="Save">Save</button>
          <button id="editpropsCloseButtonBottom" value="Close">Close</button>
        </div>
      </div>
    
      <div role="dialog" id="editgroupProps" aria-labelledby="editgroupPropsLabel" aria-modal="true" class="hidden">
        <h3 id="editgroupPropsLabel">Edit Group Properties</h3>
        <div class="dialog_form">
          <input id="editgrouppropsId" size="30" type="hidden"/>
          <div class="dialog_form_item">
            <label>
              <span class="label_text">Title:</span>
              <input id="editgrouppropsTitle" size="30"/>
            </label>
          </div>
        </div>
        <div class="dialog_form_actions">    
          <button id="editgrouppropsSaveButtonBottom" value="Save">Save</button>
          <button id="editgrouppropsCloseButtonBottom" value="Close">Close</button>
        </div>
      </div>

      <div role="dialog" id="dataentry"  aria-labelledby="dataentryLabel" aria-modal="true" class="hidden">
        <h3 id="dataentryLabel">Data Entry</h3>
        <div class="dialog_form_actions">    
          <button id="dataentryCloseButtonTop" value="Close">Close</button>
        </div>

        <ul class="alertList"></ul>

        <div class="dialog_form">
          <table>
            <thead>
              <tr id="dataentryheadrow"><th>Criterion</th></tr>
            </thead>
            <tbody id="dataentrytablebody">
            </tbody>
          </table>
        </div>
        <h3>Endorsement</h3>
        <p>When marks are entered and agreed, each participant must indicate 
          their endorsement of the marks using the button below. When the first
          participant endorses the marks, all the marks become read-only.
          An instructor can reset all the endorsements so you can edit marks
          and an instructor can endorse marks on behalf of participants who have
          been absent.</p>
        <div class="dialog_form_actions">    
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

      <div role="dialog" id="exportdialog" aria-labelledby="exportdialogLabel" aria-modal="true" class="hidden">
        <h3 id="exportdialogLabel">Export Data</h3>
        <div class="dialog_form_actions"><button id="exportdialogCloseButtonTop" value="Close">Close</button></div>
        <p>The data here is formatted as tab delimited data. It may not look 
          pretty here but is suitable for transfer to most spreadsheet software.
          Select all the text in the text area below and copy to the clipboard.
          Then go to a blank sheet in your preferred spreadsheet software,
          select the top left cell and paste. Your spreadsheet may instantly
          arrange the data into columns or you may need to take further steps
          to indicate how the data should be parsed.</p>
        <p>In a typical web browser you can copy to the clipboard this way:</p>
        <ol>
          <li>Click on the text box.</li>
          <li>Type ctrl-A</li>
          <li>Type ctrl-C</li>
        </ol>
        <div class="dialog_form">
          <textarea id="exporttextarea" cols="70" rows="20" disabled=""></textarea>
        </div>
        <div class="dialog_form_actions"><button id="exportdialogCloseButtonBottom" value="Close">Close</button></div>
      </div>

      <div role="dialog" id="debugdialog" aria-labelledby="debugdialogLabel" aria-modal="true" class="hidden">
        <h3 id="debugdialogLabel">Debug Information</h3>
        <div class="dialog_form_actions"><button id="debugdialogCloseButtonTop" value="Close">Close</button></div>
        <pre id="debugtext"></pre>
        <div class="dialog_form_actions"><button id="debugdialogCloseButtonBottom" value="Close">Close</button></div>
      </div>
      
      
    </div>


    <div id="basePage">
      
    <h1>Self Enrol on Modules and Course Communities</h1>
    <p class="important">${support.importantMessage}</p>

    <ul id="toplevelalert" class="alertList"></ul>
    
    <p>This tool is non-functional as it is under development.</p>
    <c:choose>
      <c:when test="${support.allowedToManage || support.allowedToParticipate}">
        
        <c:if test="${support.allowedToManage}">
          <p>You have management rights where this tool is deployed.</p>          
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
        
    </div>
  </body>
</html>
