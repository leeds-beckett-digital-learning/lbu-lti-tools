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

<jsp:useBean id="support" class="uk.ac.leedsbeckett.ltitoolset.deeplinking.DeepLinkingPageSupport" scope="request"/>
<% support.setRequest( request ); %>

<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Create LTI 1.3 Link to a Resource</title>
    <link rel="stylesheet" href="../style/fonts.css">    
    <link rel="stylesheet" href="../style/dialog.css"/>
    <link rel="stylesheet" href="../style/buttons.css"/>
   <style>
      body { font-family: 'Nobile', sans-serif; padding: 1em 1em 1em 1em; }
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
        padding: 0.25em 0.5em 0.25em 0.5em;
      }
      
      td.vertical {
        writing-mode: vertical-lr;
        transform: rotate( 180deg );
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
    <script>
      
const dynamicPageData = ${support.dynamicPageDataAsJSON};
        
    </script>
    <script type="module" src="../javascript/@BUILDTIMESTAMP@/deeplinking/index.js"></script>
  </head>
  <body>
    <div id="dialogdiv" class="dialogs">      
      <div role="dialog" id="linkcreatedialog" aria-labelledby="linkcreatedialogLabel" aria-modal="true" class="hidden">
        <h3 id="linkcreatedialogLabel">Link or Create Resource</h3>
        <table>
          <tr><th>Title</th>   <td id="linkcreatedialogTitle"></td> </tr>
          <tr><th>ID</th>      <td id="linkcreatedialogID"></td>    </tr>
          <tr><th>Type</th>    <td id="linkcreatedialogType"></td>  </tr>
        </table>
        <p>Do you want to proceed?</p>
        <div>
          <form id="linkcreatedialogForm" method="POST">
            <input id="linkcreatedialogJwt" type="hidden" name="JWT"/>
          </form>
        </div>
        <div class="dialog_form_actions"><button id="linkcreatedialogConfirmButton" value="Close">Confirm</button></div>
        <div class="dialog_form_actions"><button id="linkcreatedialogCloseButton" value="Close">Cancel</button></div>
      </div>
      <div role="dialog" id="debugdialog" aria-labelledby="debugdialogLabel" aria-modal="true" class="hidden">
        <h3 id="debugdialogLabel">Debug Information</h3>
        <div class="dialog_form_actions"><button id="debugdialogCloseButtonTop" value="Close">Close</button></div>
        <pre id="debugtext"></pre>
        <div class="dialog_form_actions"><button id="debugdialogCloseButtonBottom" value="Close">Close</button></div>
      </div>
    </div>


    <div id="basePage">
      
    <h1>Deep Linking</h1>
    <div class="section">
    <div class="block">
    </div>
      <h4>Create New Resource</h4>
        <div id="optionsnewresourceempty" class="block" style="display: none; margin-left: 4em;">
          <p>There are no tools in this category.</p>
        </div>
        <div id="optionsnewresource" class="block" style="display: none; margin-left: 4em;">
          <table>
            <thead>
              <tr><th>Select</th><th>Tool</th></tr>
            </thead>
            <tbody id="optionsnewresourcebody">
            </tbody>
          </table>
        </div>
      <h4>Link to Site-Wide Tool</h4>
        <div id="optionssiteempty" class="block" style="display: none; margin-left: 4em;">
          <p>There are no tools in this category.</p>
        </div>
        <div id="optionssite" class="block" style="display: none; margin-left: 4em;">
          <table>
            <thead>
              <tr><th>Select</th><th>Tool</th></tr>
            </thead>
            <tbody id="optionssitetablebody">
            </tbody>
          </table>
        </div>
      <h4>Link to Existing Resource</h4>
      <div id="optionsresource" class="block">
      <p><em>Not yet implemented.</em></p>
      </div>

      <div id="tooloptions" class="block">
      </div>      
    </div>
        
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
    
    </div>
  </body>
</html>
