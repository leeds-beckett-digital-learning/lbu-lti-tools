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
let selectedfi;

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
  var html;
  var multHtml = "";
  var courseHtml = "";
  var platformHtml = "";
  var button;
  var buttonid = 1000;
  
  options.toolMap = {};
  for ( var i=0; i<options.toolInformations.length; i++ )
  {
    var ti = options.toolInformations[i];
    options.toolMap[ti.id] = ti;
    ti.facetMap = {};
    console.log( 'Tool title ' + ti.title );
    for ( var j=0; j<ti.facets.length; j++ )
    {
      var facet = ti.facets[j];
      ti.facetMap[facet.id] = facet;      
      html = "<tr><td><button id=\"toolbutton_" + buttonid++ 
              + "\" data-toolid=\"" + ti.id + "\" data-facetid=\"" + facet.id +
                      "\">Link</button></td><td>" + ti.title + "</td></tr>\n";
      if ( facet.instantiationLevel === "PLATFORM_RESOURCE" || facet.instantiationLevel === "TOOL_RESOURCE" )
        multHtml += html;
      else if ( facet.instantiationLevel === "COURSE" )
        courseHtml += html;
      else if ( facet.instantiationLevel === "PLATFORM" )
        platformHtml += html;
    }
  }
  
  if ( platformHtml.length === 0 )
  {
    finder.optionsplatformempty.style.display = 'block';
    finder.optionsplatform.style.display = 'none';
  }
  else
  {
    finder.optionsplatformtablebody.innerHTML = platformHtml;
    finder.optionsplatformempty.style.display = 'none';
    finder.optionsplatform.style.display = 'block';
  }
  
  if ( courseHtml.length === 0 )
  {
    finder.optionscourseempty.style.display = 'block';
    finder.optionscourse.style.display = 'none';
  }
  else
  {
    finder.optionscoursetablebody.innerHTML = courseHtml;
    finder.optionscourseempty.style.display = 'none';
    finder.optionscourse.style.display = 'block';
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
  
  for ( var i=1000; i<buttonid; i++ )
  {
    button = finder[ "toolbutton_" + i ];
    if ( button )
      button.addEventListener( 'click', (event) => openToolDialog( event ) );
  }
  
}


function sendMakeLink()
{
  let title = finder.linkcreatedialogTitle.value;
  let desc  = finder.linkcreatedialogDescription.value;
  toolsocket.sendMessage( new deeplinking.MakeLinkMessage( selectedti.id, selectedfi.id, title, desc, null ) )  ;
}

function updateAlerts()
{
  arialib.updateAlerts();  
}
  
function addAlert( text )
{
  arialib.addAlert( text );
}

function openToolDialog( event )
{
  console.log( event );
  var button = event.target;
  
  const toolid = button.dataset.toolid;
  const facetid = button.dataset.facetid;
  selectedti = options.toolMap[toolid];
  selectedfi = selectedti.facetMap[facetid];
  
  arialib.openDialog( 'linkcreatedialog', button );
  finder.linkcreatedialogTool.innerHTML    = selectedfi.title;
  finder.linkcreatedialogToolID.innerHTML  = toolid;
  finder.linkcreatedialogFacetID.innerHTML = facetid;
  finder.linkcreatedialogTitle.value       = selectedfi.title;
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
