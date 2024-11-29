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
    <link rel="stylesheet" href="../style/fonts.css">    
    <link rel="stylesheet" href="../style/dialog.css"/>
    <link rel="stylesheet" href="../style/buttons.css"/>
   <style>
      body { font-family: 'Nobile', sans-serif; padding: 1em 1em 1em 1em; background-color: white; }
      p { margin: 0em 0em 0em 0em; }
      .section { margin: 2em 0em 2em 0em; }
      .block { max-width: 50em; }
      .stage {
        padding-bottom: 1em;
      }
      .stage-active {
        display: block;
        margin: 0px;
        padding: 0px;
      }
      .stage-inactive {
        display: none;
        margin: 0px;
        padding: 0px;
      }
      .stage-text {
        border: black 3px solid;
        border-radius: 0.5em;
        margin: 0px;
        padding: 0.25em;
      }
      .stage-img {
        vertical-align: bottom;
      }
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

      th { 
        vertical-align: top;
        padding: 0.25em 0.5em 0.25em 0.5em;
      }
      
      th.vertical {
        writing-mode: vertical-lr;
        transform: rotate( 180deg );
      }      
      
      th.plain {
        font-weight: normal;
        text-align: left;
      }
      
      td {
        vertical-align: top;
        padding: 0.25em 0.5em 0.25em 0.5em;
      }
      
      td.vertical {
        writing-mode: vertical-lr;
        transform: rotate( 180deg );
      }      

      table#grouptable {
        border-spacing: 0px;
      }
      
      table#grouptable>tbody:nth-child(odd) {
        background-color: mintcream;
      }
      
      table#grouptable>tbody:nth-child(even) {
        background-color: rgb(240,245,255);        
      }
      
      table#grouptable td {
        padding-top: 0px;
        padding-bottom: 0px;
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
      .grouptitle {
        font-size: 110%;
        font-weight: bold;
      }
      .groupstatus {
        margin-top: 1em;
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
    <script lang="JavaScript">
      
const dynamicPageData = ${support.dynamicPageDataAsJSON};
        
    </script>
    <script type="module" src="../javascript/@BUILDTIMESTAMP@/peergroupassessment/index.js"></script>
  </head>
  <body>
    <div id="dialogdiv" class="dialogs">
      
      <c:if test="${support.allowedToConfigure}">
      <div role="dialog" id="configdialog" aria-labelledby="configdialogLabel" aria-modal="true" class="hidden">
        <h3 id="configdialogLabel">Configure Peer Group Assessment Tool</h3>
        <div>
          <table>
            <tr><th></th><th>Permissions</th></tr>
            <tr><th>Module/Community Instructor/Leader Can Deep Link</th>  <td><input type="checkbox" id="config_membershipInstructorDeepLinkPermitted"/></td></tr>
          </table>          
        </div>
        <div class="dialog_form_actions">
          <button id="configdialogSaveButton" value="Close">Save</button>
          <button id="configdialogCancelButton" value="Close">Cancel</button>
        </div>
      </div>
      </c:if>

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
          <input id="editgrouppropsId" type="hidden"/>
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

      <div role="dialog" id="deletegroup" aria-labelledby="deletegroupLabel" aria-modal="true" class="hidden">
        <h3 id="deletegroupLabel">Delete Group</h3>
        <h4 id="deletegroupTitleTitle"></h4>
        <div class="dialog_form">
          Are you sure that you want to delete this group?
          <input id="deletegroupId"    type="hidden"/>
          <input id="deletegroupTitle" type="hidden""/>
        </div>
        <div class="dialog_form_actions">    
          <button id="deletegroupDeleteButtonBottom" value="Delete">Delete</button>
          <button id="deletegroupCloseButtonBottom" value="Close">Close</button>
        </div>
      </div>

      <div role="dialog" id="dataentry"  aria-labelledby="dataentryLabel" aria-modal="true" class="hidden">
        <h3 id="dataentryLabel">Data Entry</h3>
        <div class="dialog_form_actions">    
          <button id="dataentryCloseButtonTop" value="Close">Close</button>
        </div>

        
        <div class="dialog_form">
          <table>
            <tbody id="dataentrytablebody">
            </tbody>
          </table>
        </div>

        <h3>Scores</h3>
        <div><p>Scores for all criteria must be whole numbers between 0 and 20
            inclusive.</p></div>
        
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
        </div>

        <h3>Advice</h3>
        <div><p>Any member of the group can enter scores against their own
          or other members' names. One person can enter all the marks or you
          can share this task so that two or more group members enter marks
          simultaneously.  To ensure you all agree about the scores you could
          physically meet and later enter scores or you could use a group
          communication tool to discuss scores as you enter them. If you meet on-line,
          each team member can use the communication application to talk and 
          simultaneously use a web browser to view or enter the scores. When
          choosing a communication tool please take into account the needs
          and preferences of all group members.</p></div>

        <p><button id="dataentryCloseButtonBottom" value="Close">Close</button></p>
        
        <h3>Notifications</h3>
        <ul class="alertList"></ul>
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

      <div role="dialog" id="bbgroupsetsdialog" aria-labelledby="bbgroupsetsdialoglabel" aria-modal="true" class="hidden">
        <h3 id="bbgroupsetsdialoglabel" class="dialog_label">Blackboard Group Sets</h3>
        <div class="dialog_form">
          <div class="dialog_form_item">
            <label>
              <span class="label_text">Group Set to Import:</span>
              <select id="bbgroupsetsdialogselect">
                <option>Wait for group sets to load...</option>
              </select>
            </label>
          </div>
        </div>
        <div id="bbgroupsetsdialogdetail">
        </div>
        <div class="dialog_form_actions">    
          <button id="bbgroupsetsdialogSaveButtonBottom" value="Save">Import</button>
          <button id="bbgroupsetsdialogCloseButtonBottom" value="Close">Close</button>
        </div>
      </div>

      <div role="dialog" id="exportplatformdialog" aria-labelledby="exportplatformdialogLabel" aria-modal="true" class="hidden">
        <h3 id="exportlineitemsdialogLabel">Export to Platform</h3>
        <div><p>When you export to the platform line items are created in your
          platform's assessment table which will contain the current snapshot
          of results. If you export again at a later date more line items are
          created with a more recent snapshot of results. (As distinct from
          overwriting scores in the first set of line items.) This is intended
          to provide better data security if you export by mistake. <p>Note that 
          if a single input for a single student is invalid then the entire 
          group will be skipped in the export process.</p> <p>You are advised to 
          review the line items in your platform immediately after exporting
          in case you need to configure visibility of the data by students.</p></div>

        <div>
          <p>Which numbers do you want to export to the platform?</p>
          <input id="exportlineitems_opt_0" type="checkbox"/>
          <label for="exportlineitems_opt_0">Individual Score</label><br/>
          <input id="exportlineitems_opt_1" type="checkbox"/>
          <label for="exportlineitems_opt_1">Highest in Group</label><br/>
          <input id="exportlineitems_opt_2" type="checkbox"/>
          <label for="exportlineitems_opt_2">Score as % of Highest</label><br/>
          <input id="exportlineitems_opt_3" type="checkbox"/>
          <label for="exportlineitems_opt_3">Group Size</label><br/>
          <input id="exportlineitems_opt_4" type="checkbox"/>
          <label for="exportlineitems_opt_4">Group Scores Total</label><br/>
          <input id="exportlineitems_opt_5" type="checkbox"/>
          <label for="exportlineitems_opt_5">Group Mean Score</label><br/>
          <input id="exportlineitems_suffix" cols="20" value=""/>
          <label for="exportlineitems_suffix">Suffix to use for line items.</label><br/>
        </div>
        
        <div><p>Progress: <span id='exportlineitemsprogress'>0</span>%</p></div>
        <div class="dialog_form_actions">
          <button id="exportplatformdialogExportButton" value="Close">Export</button>
          <button id="exportplatformdialogCloseButton" value="Close">Close</button>
        </div>
      </div>
            
      <div role="dialog" id="debugdialog" aria-labelledby="debugdialogLabel" aria-modal="true" class="hidden">
        <h3 id="debugdialogLabel">Debug Information</h3>
        <div class="dialog_form_actions"><button id="debugdialogCloseButtonTop" value="Close">Close</button></div>
        <pre id="debugtext"></pre>
        <div class="dialog_form_actions"><button id="debugdialogCloseButtonBottom" value="Close">Close</button></div>
      </div>
      
      
    </div>


    <div id="basePage">
      
    <h1>Peer Group Assessment Tool</h1>
    <div class="section">
    <div class="block">
      <p class="important">${support.importantMessage}</p>
      <h2 id="mainTitle"></h2>
      <p id="mainDescription"></p>
      <div class="stage">
        <p id="stage1" class="stage-inactive"><img class="stage-img" src="../style/stage1before.png" alt="Stages"                /><span id="stage1text" class="stage-text">Setting Up</span><img             alt="Three more stages" class="stage-img" src="../style/stage1after.png"/></p>
        <p id="stage2" class="stage-inactive"><img class="stage-img" src="../style/stage2before.png" alt="One complete stage"    /><span id="stage2text" class="stage-text">Members Joining Groups</span><img alt="Two more stages"   class="stage-img" src="../style/stage2after.png"/></p>
        <p id="stage3" class="stage-inactive"><img class="stage-img" src="../style/stage3before.png" alt="Two complete stages"   /><span id="stage3text" class="stage-text">Members Entering Data</span><img  alt="One more stage"    class="stage-img" src="../style/stage3after.png"/></p>
        <p id="stage4" class="stage-inactive"><img class="stage-img" src="../style/stage4before.png" alt="Three complete stages" /><span id="stage4text" class="stage-text">Results Frozen</span><img         alt="No more stages"    class="stage-img" src="../style/stage4after.png"/></p>
      </div>
    </div>
      <c:if test="${support.allowedToManage}">
        <p><button id="editpropertiesButton">Edit Properties</button> 
        <c:if test="${support.blackboardLearnRestAvailable}">
          <button id="importBlackboardButton">Import Blackboard Sub-groups</button> 
        </c:if>
        <button id="importButton">Import Class</button></p>
        <p style="margin-top: 0.5em;"><button id="exportButton">Export to Spreadsheet</button>
          <c:if test="${support.allowedToExportToPlatform}">
            <button id="exportPlatformButton">Export to Platform</button>
          </c:if>
        </p>
        <p style="display: none;"><em>These and other buttons on the page are only shown to managers of this resource.</em></p>
      </c:if>
    </div>
    
    <c:choose>
      <c:when test="${support.allowedToManage || support.allowedToParticipate}">
        <div class="section">
        <table id="grouptable">
          <tbody id="grouptablefooter">
            <c:if test="${support.allowedToManage}">        
              <tr><td></td><td style="padding: 1em 1em 1em 1em;"><button id="addgroupButton">Add Group</button></td></tr>
            </c:if>
              <tr id="groupUnattachedRow">
                <td colspan="2"><p class="grouptitle">Unattached Participants</strong></p>
              </tr>
              <tr>
                <td></td>
                <td id="unattachedParticipants"></td>
              </tr>
          </tbody>
        </table>
        </div>
                    
      </c:when>
      <c:otherwise>
        <p>Your role set by system that launched this tool means that you
        can neither participate or manage this resource.</p>
      </c:otherwise>
    </c:choose>
    
    <h4 style="display: none;">Notifications</h4>
    <div class="block">
      <ul id="toplevelalert" class="alertList"></ul>
      <p style="display: none;">Notifications will appear above this paragraph. These are mainly intended
        for users who use browsers that are adapted for accessibility. This is 
        because different elements of this page can change in response to the
        actions of other users and that could cause problems for sight impaired
        users. To help those users, whenever a change occurs to the content of this
        page an alert is added in a way that adapted browsers can notice
        and report to you. You can choose to navigate to the relevant part of the
        page and 'read' the changed content.
      </p>
    </div>
    
    <c:if test="${support.allowedToConfigure}">
      <div style="margin-top: 10em;">
        <hr>
        <p>Your role allows you to configure this tool.</p>
        <p><button id="configureButton">Configure</button></p>          
      </div>
    </c:if>
    
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
