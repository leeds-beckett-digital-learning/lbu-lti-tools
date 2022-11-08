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

    <div id="exportdialog" class="dialog">
      <div class="dialogcontent">
        <h3>Export Data</h3>
        <p><button id="exportdialogCloseButtonTop" value="Close">Close</button></p>
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
        <textarea id="exporttextarea" cols="70" rows="20" disabled=""></textarea>
        <p><button id="exportdialogCloseButtonBottom" value="Close">Close</button></p>
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
          <p><button id="exportButton">Export</button></p>
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

        <div>
          <h1>Aria Dialog Test</h1>
          
<button id="opendialogbutton" type="button">Add Delivery Address</button>
<div id="dialog_layer" class="dialogs">
  <div role="dialog" id="dialog1" aria-labelledby="dialog1_label" aria-modal="true" class="hidden">
    <h2 id="dialog1_label" class="dialog_label">Add Delivery Address</h2>
    <div class="dialog_form">
      <div class="dialog_form_item">
        <label>
          <span class="label_text">Street:</span>
          <input type="text" class="wide_input">
        </label>
      </div>
      <div class="dialog_form_item">
        <label>
          <span class="label_text">City:</span>
          <input type="text" class="city_input">
        </label>
      </div>
      <div class="dialog_form_item">
        <label>
          <span class="label_text">State:</span>
          <input type="text" class="state_input">
        </label>
      </div>
      <div class="dialog_form_item">
        <label>
          <span class="label_text">Zip:</span>
          <input type="text" class="zip_input">
        </label>
      </div>

      <div class="dialog_form_item">
        <label for="special_instructions">
          <span class="label_text">Special instructions:</span>
        </label>
        <input id="special_instructions" type="text" aria-describedby="special_instructions_desc" class="wide_input">
        <div class="label_info" id="special_instructions_desc">
          For example, gate code or other information to help the driver find you
        </div>
      </div>
    </div>
    <div class="dialog_form_actions">
      <button id="dialog1verifybutton" type="button">Verify Address</button>
      <button id="dialog1addbutton"    type="button">Add</button>
      <button id="dialog1cancelbutton" type="button">Cancel</button>
    </div>
  </div>

  
  <div id="dialog2" role="dialog" aria-labelledby="dialog2_label" aria-describedby="dialog2_desc" aria-modal="true" class="hidden">
    <h2 id="dialog2_label" class="dialog_label">Verification Result</h2>
    <div id="dialog2_desc" class="dialog_desc">
      <p tabindex="-1" id="dialog2_para1">This is just a demonstration. If it were a real application, it would
        provide a message telling whether the entered address is valid.</p>
      <p>
        For demonstration purposes, this dialog has a lot of text. It demonstrates a
        scenario where:
      </p>
      <ul>
        <li>The first interactive element, the help link, is at the bottom of the dialog.</li>
        <li>If focus is placed on the first interactive element when the dialog opens, the
          validation message may not be visible.</li>
        <li>If the validation message is visible and the focus is on the help link, then
          the focus may not be visible.</li>
        <li>
          When the dialog opens, it is important that both:
          <ul>
            <li>The beginning of the text is visible so users do not have to scroll back to
              start reading.</li>
            <li>The keyboard focus always remains visible.</li>
          </ul>
        </li>
      </ul>
      <p>There are several ways to resolve this issue:</p>
      <ul>
        <li>Place an interactive element at the top of the dialog, e.g., a button or link.</li>
        <li>Make a static element focusable, e.g., the dialog title or the first block of
          text.</li>
      </ul>
      <p>
        Please <em>DO NOT </em> make the element with role dialog focusable!
      </p>
      <ul>
        <li>The larger a focusable element is, the more difficult it is to visually
          identify the location of focus, especially for users with a narrow field of view.</li>
        <li>The dialog has a visual border, so creating a clear visual indicator of focus
          when the entire dialog has focus is not very feasible.</li>
        <li>Screen readers read the label and content of focusable elements. The dialog
          contains its label and a lot of content! If a dialog like this one has focus, the
          actual focus is difficult to comprehend.</li>
      </ul>
      <p>
        In this dialog, the first paragraph has <code>tabindex=<q>-1</q></code>. The first
        paragraph is also contained inside the element that provides the dialog description, i.e., the element that is referenced
        by <code>aria-describedby</code>. With some screen readers, this may have one negative
        but relatively insignificant side effect when the dialog opens -- the first paragraph
        may be announced twice. Nonetheless, making the first paragraph focusable and setting
        the initial focus on it is the most broadly accessible option.
      </p>
    </div>
    <div class="dialog_form_actions">
      <a      id="dialog2helplink"    href="#"     >link to help</a>
      <button id="dialog2helpbutton"  type="button">accepting an alternative form</button>
      <button id="dialog2closebutton" type="button">Close</button>
    </div>
  </div>

  
  <div id="dialog3" role="dialog" aria-labelledby="dialog3_label" aria-describedby="dialog3_desc" aria-modal="true" class="hidden">
    <h2 id="dialog3_label" class="dialog_label">Address Added</h2>
    <p id="dialog3_desc" class="dialog_desc">
      The address you provided has been added to your list of delivery addresses. It is ready
      for immediate use. If you wish to remove it, you can do so from
      <a id="dialog3profilelink" href="#">your profile.</a>
    </p>
    <div class="dialog_form_actions">
      <button id="dialog3okbutton" type="button">OK</button>
    </div>
  </div>

  <div id="dialog4" role="dialog" aria-labelledby="dialog4_label" aria-describedby="dialog4_desc" class="hidden" aria-modal="true">
    <h2 id="dialog4_label" class="dialog_label">End of the Road!</h2>
    <p id="dialog4_desc" class="dialog_desc">
      The link or button is present for demonstration purposes only.
    </p>
    <div class="dialog_form_actions">
      <button id="dialog4closebutton" type="button">Close</button>
    </div>
  </div>
</div>
                
        </div>
        
        
  </body>
</html>
