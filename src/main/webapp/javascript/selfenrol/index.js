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
import selfenrol from "../generated/selfenrol.js";

let dynamicData = dynamicPageData;

let toolsocket;

let loading;

let dataentryopening=true;

let coursespecvalidator;
let orgspecvalidator;

var currentSearch = "";

function init()
{
  console.log( "init" );
  console.log( finder.toplevelalert );
  console.log( finder.toplevelalert.ariaLive );
  finder.toplevelalert.ariaLive = 'polite';
  console.log( "Set ariamixin property" );
  console.log( finder.toplevelalert.ariaLive );
  
  arialib.setDialogAlertClass( 'alertList' );
  arialib.setBaseAlertElement( finder.toplevelalert );
  setInterval( updateAlerts, 1000 );
    
  console.log( dynamicData.webSocketUri );

  coursespecvalidator = new RegExp( dynamicData.courseSearchValidation );
  orgspecvalidator    = new RegExp( dynamicData.orgSearchValidation    );

  finder.searchdialogEnrolButton.addEventListener( 'click', () => enrolOnCourse() );
  finder.searchdialogCloseButton.addEventListener( 'click', () => arialib.closeDialog(finder.searchdialog)    );
  finder.searchCourseButton.addEventListener( 'click', () => searchForCourses() );
  finder.searchOrgButton.addEventListener( 'click', () => searchForOrgs() );
  finder.searchTrainingButton.addEventListener( 'click', () => searchForTraining() );
  finder.reason.addEventListener( 'change', () => changeReason() );
  
  let handler =
  {
    open()
    {
    },
    
    handleAlert( message )
    {
      console.log( "Rxed alert " + message.payload );
      alert( message.payload );
    },
    
    handleCourseInfoList( message )
    {
      console.log( "Rxed course info list " + message.payload );
      let content = "";
      if ( message.payload.length === 0 )
      {
        finder.searchresults.innerHTML = "<option value=\"\">Nothing found.</option>\n";
        return;
      }
      for ( const d of message.payload )
        content += "<option value=\"" + d.externalId + "\">" + d.externalId + " ~ " + d.name + "</option>\n";
      finder.searchresults.innerHTML = content;
    },
    
    handleEnrolSuccess( message )
    {
      alert( "Enrolment succeeded." );
      arialib.closeDialog(finder.searchdialog);
    }
  };
  
  toolsocket = new selfenrol.ToolSocket( dynamicData.webSocketUri, handler  );  
}

function updateAlerts()
{
  arialib.updateAlerts();  
}
  
function addAlert( text )
{  arialib.addAlert( text );
}

function clearSearch()
{
  finder.whoBlock.style.display = ( currentSearch === "training" ) ? "none" : "block";
  finder.searchresults.innerHTML = "<option value=\"\">Waiting for results...</option>";
  finder.authDiv.style.display = "none";
  finder.reason.value = "none";
  finder.authName.value = "";
  finder.authEmail.value = "";
}

function changeReason()
{
  let r = finder.reason.value;
  if ( "directorpermit" !== r && "leaderpermit" !== r )
  {
    finder.authDiv.style.display = "none";
    return;
  }
  var s = ( "directorpermit" === r )?"course director":"module leader";
  finder.authNameTitle.innerHTML = s;
  finder.authEmailTitle.innerHTML = s;
  finder.authDiv.style.display = "block";
}


function searchForCourses()
{
  currentSearch = "courses";
  var spec = finder.courseid.value;
  if ( !coursespecvalidator.test( spec ) )
  {
    alert( "The input was not valid. Please read the notes on the page about how to search." );
    return;
  }
  toolsocket.sendMessage( new selfenrol.SearchMessage( "course", spec ) );
  clearSearch();
  arialib.openDialog( 'searchdialog', finder.searchCourseButton );
}

function searchForOrgs()
{
  currentSearch = "orgs";
  var spec = finder.orgid.value;
  if ( !orgspecvalidator.test( spec ) )
  {
    alert( "The input was not valid. Please read the notes on the page about how to search." );
    return;
  }
  toolsocket.sendMessage( new selfenrol.SearchMessage( "organization", spec ) );  
  clearSearch();
  arialib.openDialog( 'searchdialog', finder.searchOrgButton );
}

function searchForTraining()
{
  currentSearch = "training";
  toolsocket.sendMessage( new selfenrol.SearchMessage( "training", "" ) );  
  clearSearch();
  arialib.openDialog( 'searchdialog', finder.searchTrainingButton );
}

function enrolOnCourse()
{
  var id = finder.searchresults.value;
  if ( id === "" )
  {
    alert( "No course is selected." );
    return;
  }
  var reason = "";
  var name = "";
  var email = "";

  if ( currentSearch !== "training" )
  {
    var reason = finder.reason.value;
    if ( reason === "" || reason === "none" )
    {
      alert( "You haven't specified who authorised you to self enrol." );
      return;
    }  
    if ( reason === "directorpermit" || reason === "leaderpermit" )
    {
      name = finder.authName.value;
      email = finder.authEmail.value;
      if ( name === null || name.trim().length === 0 )
      {
        alert( "You need to provide the name of the person who authorised you to self-enrol." );
        return;
      }
      if ( email === null || email.trim().length === 0 )
      {
        alert( "You need to provide the EMail address of the person who authorised you to self-enrol." );
        return;
      }
      var split = email.trim().split( "@" );
      if ( split.length !== 2 )
      {
        alert( "The email address doesn't look valid." );
        return;
      }
      if ( split[1].toLowerCase() !== "leedsbeckett.ac.uk" )
      {
        alert( "The email isn't a Leeds Beckett address." );
        return;
      }
    }
  }  
  toolsocket.sendMessage( new selfenrol.EnrolRequestMessage( id, reason, name, email ) );  
}

window.addEventListener( "load", function(){ init(); } );
document.addEventListener( "DOMContentLoaded", function()
  { 
    console.log( "DOMContentLoaded event arrived." ); 
    console.log( finder.toplevelalert );
  } );
