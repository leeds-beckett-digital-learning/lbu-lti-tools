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
import deeplinking from "../generated/deeplinking.js";

let dynamicData = dynamicPageData;

let toolsocket;
let options;
let selectedti;

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

  finder.linkcreatedialogCloseButton.addEventListener( 
          'click', () => arialib.closeDialog(finder.linkcreatedialogCloseButton) );

  finder.linkcreatedialogConfirmButton.addEventListener( 
          'click', () => sendMakeLink() );
    
  console.log( dynamicData.webSocketUri );
  
  let handler =
  {
    open()
    {
      toolsocket.sendMessage( new deeplinking.GetOptionsMessage() );
    },
    
    handleAlert( message )
    {
      alert( message.payload );
    },

    handleOptions( message )
    {
      options = message.payload;
      console.log( 'Options rxed' );
      updateAvailableTools();
    },

    handleJwt( message )
    {
      createOrLink( message.payload );
    }

  };
  
  toolsocket = new deeplinking.ToolSocket( dynamicData.webSocketUri, handler  );    
}


function updateAvailableTools()
{
  var singHtml = "";
  var multHtml = "";
  var button;
  
  for ( var i=0; i<options.toolInformations.length; i++ )
  {
    var ti = options.toolInformations[i];
    console.log( 'Tool title ' + ti.title );  
    if ( ti.instantiationType === "SINGLETON" )
    {
      singHtml += "<tr><td><button id=\"toolbutton_" + ti.id + "\">Link</button></td><td>" + ti.title + "</td></tr>\n";
    }
    if ( ti.instantiationType === "MULTITON" )
    {
      if ( ti.instantiateOnDeepLinking )
        multHtml += "<tr><td><button id=\"toolbutton_" + ti.id + "\">Create</button></td><td>" + ti.title + "</td></tr>\n";
    }
  }
  
  if ( singHtml.length === 0 )
  {
    finder.optionssiteempty.style.display = 'block';
    finder.optionssite.style.display = 'none';
  }
  else
  {
    finder.optionssitetablebody.innerHTML = singHtml;
    finder.optionssiteempty.style.display = 'none';
    finder.optionssite.style.display = 'block';
  }
  
  if ( multHtml.length === 0 )
  {
    finder.optionsnewresourceempty.style.display = 'block';
    finder.optionsnewresource.style.display = 'none';
  }
  else
  {
    finder.optionsnewresourcebody.innerHTML = multHtml;
    finder.optionsnewresourceempty.style.display = 'none';
    finder.optionsnewresource.style.display = 'block';    
  }
  
  for ( var i=0; i<options.toolInformations.length; i++ )
  {
    // ti declared const here so different for each iteration
    // to make sure each event listener sees separate ti
    const ti = options.toolInformations[i];
    console.log( 'Tool title ' + ti.title );  
    button = finder[ "toolbutton_" + ti.id ];
    if ( button )
      button.addEventListener( 'click', () => openToolDialog( button, ti ) );
  }
  
}


function sendMakeLink()
{
  let title = finder.linkcreatedialogTitle.value;
  let desc  = finder.linkcreatedialogDescription.value;
  toolsocket.sendMessage( new deeplinking.MakeLinkMessage( selectedti.id, selectedti.type, title, desc, null ) )  ;
}

function updateAlerts()
{
  arialib.updateAlerts();  
}
  
function addAlert( text )
{
  arialib.addAlert( text );
}

function openToolDialog( openerElement, ti )
{
  selectedti = ti;
  arialib.openDialog( 'linkcreatedialog', openerElement );
  finder.linkcreatedialogTool.innerHTML    = ti.title;
  finder.linkcreatedialogID.innerHTML      = ti.id;
  finder.linkcreatedialogType.innerHTML    = ti.type;
  finder.linkcreatedialogTitle.value       = ti.title;
  finder.linkcreatedialogDescription.value = "";
  finder.linkcreatedialogForm.action       = dynamicData.deepLinkReturnUrl;
}

function createOrLink( jwt )
{
  finder.linkcreatedialogJwt.value = jwt;
  finder.linkcreatedialogForm.submit();
}
  
  
function openDebugDialog( openerElement )
{
  let pre = finder[ "debugtext" ];
  pre.innerHTML = "testing...";  
  arialib.openDialog( 'debugdialog', openerElement );
}

window.addEventListener( "load", function(){ init(); } );
