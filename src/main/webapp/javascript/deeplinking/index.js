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

let dynamicData = dynamicPageData;

let toolsocket;

let loading;

let dataentryopening=true;
        
let form;
let data;
let selectedgroupid;
let formuptodate = false;

let bbgroupsetdata;

let unattachedcheckboxes = new Array();
let oldstage = "unknown";

function init()
{
  console.log( "init" );
  console.log( finder.toplevelalert );

  console.log( finder.toplevelalert.ariaLive );
  finder.toplevelalert.ariaLive = 'polite';
  console.log( "Set ariamixin property" );
  console.log( finder.toplevelalert.ariaLive );
  
  //finder.toplevelalert.setAttribute( 'aria-live', 'polite' );  
  //console.log( finder.toplevelalert.getAttribute( 'aria-live' ) );

  arialib.setDialogAlertClass( 'alertList' );
  arialib.setBaseAlertElement( finder.toplevelalert );
  setInterval( updateAlerts, 1000 );  
  
  if ( dynamicData.options.length === 0 )
  {
    finder.tooloptions.innerHTML = 
            "<h4>No Resources Available</h4><p>Sorry, you don't have access " +
            "rights that will allow you to create any resources.</p>";
    return;
  }
  
  var html = "";
  for ( var i=0; i < dynamicData.options.length; i++ )
  {
    console.log( dynamicData.options[i].title );
    console.log( dynamicData.options[i].id    );
    console.log( dynamicData.options[i].type  );
    html += "<h4>";
    html += dynamicData.options[i].title;
    html += "</h4>\n<p><form method=\"post\" action=\"" + dynamicData.deepLinkReturnUrl + "\">\n";
    html += "<input type=\"hidden\" name=\"JWT\" value=\"" + dynamicData.options[i].jwt + "\"/>\n";
    html += "<input type=\"submit\" value=\"Choose\"/>\n";
    html += "</form></p>\n";
  }
//  html += "<p><form method=\"post\" action=\"" + dynamicData.deepLinkReturnUrl + "\">\n";
//  html += "<input type=\"hidden\" name=\"JWT\" value=\"" + dynamicData.codedMessageCancel + "\"/>\n";
//  html += "<input type=\"submit\" value=\"None of the Above\"/>\n";
//  html += "</form></p>\n";
  finder.tooloptions.innerHTML = html;
}

function updateAlerts()
{
  arialib.updateAlerts();  
}
  
function addAlert( text )
{
  arialib.addAlert( text );
}

function openDebugDialog( openerElement )
{
  let pre = finder[ "debugtext" ];
  pre.innerHTML = "testing...";  
  arialib.openDialog( 'debugdialog', openerElement );
}

window.addEventListener( "load", function(){ init(); } );
