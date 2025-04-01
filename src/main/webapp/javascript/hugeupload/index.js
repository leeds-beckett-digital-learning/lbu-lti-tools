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
import hugeupload from "../generated/hugeupload.js";

let dynamicData = dynamicPageData;

let toolsocket;

let loading;

let dataentryopening=true;

let resource = 
        {
          "resourceKey":{"platformId":"https://my-test.leedsbeckett.ac.uk/","resourceId":"_137_1"},
          "properties":{"title":"","description":"","stage":"SETUP"},
          "groupsById":{},
          "groupOfUnattached":{"id":null,"title":null,"membersbyid":{}},
          "groupIdsByMember":{}
        };
        
let form;
let data;
let selectedgroupid;
let formuptodate = false;

let bbgroupsetdata;

let unattachedcheckboxes = new Array();
let oldstage = "unknown";

let platformconfig = null;

const fileProgress = new Object();

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
      
  console.log( dynamicData.webSocketUri );
  
  if ( dynamicData.allowedToConfigure )
  {
    // These HTML elements won't exist if the user is not allowed to configure
    finder.configureButton.addEventListener(          'click', () => openConfig() );
    finder.configdialogSaveButton.addEventListener(   'click', () => saveConfig() );
    finder.configdialogCancelButton.addEventListener( 'click', () => arialib.closeDialog( finder.configdialog ) );
  }

  finder.blobuploadtestbutton.addEventListener( 'click', () => blobUploadTest() );
  
  let handler =
  {
    open()
    {
      toolsocket.sendMessage( new hugeupload.GetResourceMessage() );
    },
    
    handleAlert( message )
    {
      alert( message.payload );
    },
    
    handleResource( message )
    {
      formuptodate = false;
      resource = message.payload;
      updateResource();
      if ( dynamicData.allowedToManage )
      {
      }
    },
            
    handleConfiguration( message )
    {
      console.log( message );
      platformconfig = message.payload.configuration;
      if ( dynamicData.allowedToConfigure )
      {
        for ( var prop in platformconfig )
        {
          console.log( "Configuration property name: " + prop );
          let inputid = "config_" + prop;
          console.log( inputid );
          let input = finder[inputid];
          console.log( input );
          if ( input )
          {
            if ( input.type === 'checkbox' )
              input.checked = platformconfig[prop];              
            else
              input.value = platformconfig[prop];
          }
        }
        console.log( "End of list" );
      }
      // Now update the validation strings...
      coursespecvalidator = new RegExp( platformconfig.courseSearchValidation );
      orgspecvalidator    = new RegExp( platformconfig.organizationSearchValidation    );
      // And advice strings
      finder.courseadvice.innerHTML   = platformconfig.courseAdvice;
      finder.orgadvice.innerHTML      = platformconfig.organizationAdvice;
      finder.trainingadvice.innerHTML = platformconfig.trainingAdvice;
    },
    
    handleConfigurationSuccess( message )
    {
      alert( "Configuration success: " + message.payload );
      arialib.closeDialog( finder.configdialog );
    }
  
  };
  
  toolsocket = new hugeupload.ToolSocket( dynamicData.webSocketUri, handler  );  
}

function updateAlerts()
{
  arialib.updateAlerts();  
}
  
function addAlert( text )
{
  arialib.addAlert( text );
}

function updateResource()
{
}

async function blobUploadTest()
{
    console.log( "Uploading starting" );
    const fileInput = document.querySelector("input[type=file]");
    if ( fileInput.files.length < 1 )
    {
        alert( "No files selected." );
        return;
    }

    fileProgress.file = null;
    fileProgress.maxChunkSize = 10 * 1000 * 1000; // 10 MB
    fileProgress.chunkSize=0;
    fileProgress.chunkNo=0;
    fileProgress.chunkCount=0;
    fileProgress.chunk = null;
    fileProgress.reader = new FileReader();
    fileProgress.reader.addEventListener( 'load', (e) => sendIncomingChunk( e ) );

    
    for ( var f=0; f<fileInput.files.length; f++ )
    {
        processOneFile( fileInput.files[f] );
    }

    console.log( "Uploading done" );
}


function processOneFile( file )
{    
    console.log( " Name of file: " + file.name );
    console.log( "Last modified: " + file.lastModified );
    console.log( "         Size: " + file.size );
    console.log( "         Type: " + file.type );

    fileProgress.file = file;
    fileProgress.chunkNo=0;
    fileProgress.chunkCount=Math.floor( file.size / fileProgress.maxChunkSize );
    if ( (file.size % fileProgress.maxChunkSize) > 0 )
        fileProgress.chunkCount++;
    console.log( fileProgress );
    startChunk();
}

function startChunk()
{
    const start = fileProgress.chunkNo * fileProgress.maxChunkSize;
    const end   = ( (start + fileProgress.maxChunkSize) > fileProgress.file.size ) ? fileProgress.file.size : start + fileProgress.maxChunkSize;
    fileProgress.chunkSize=end-start;
    
    console.log( "Processing chunk " + fileProgress.chunkNo + " from " + start + " to " + end );
    
    fileProgress.chunk = fileProgress.file.slice( start, end );
    fileProgress.reader.readAsArrayBuffer( fileProgress.chunk );
}

async function sendIncomingChunk( e )
{
    console.log( e );
    console.log( fileProgress );
    const data = fileProgress.reader.result;
    const hash = await crypto.subtle.digest( "SHA-256", data );
    const hashArray = Array.from(new Uint8Array(hash)); // convert buffer to byte array
    const hashHex = hashArray.map((b) => b.toString(16).padStart(2, "0")).join("");
    console.log( "Hash = " + hashHex );
        
    const url = "https://digles-tools-test.leedsbeckett.ac.uk/lbu-lti-tools/blobex";
    const requestOptions = {
          method: 'PUT',
          headers: { 
            'Content-Type': 'application/octet-stream',
            'Content-Length': fileProgress.chunkSize
          },
          body: data
      };
    const response = await fetch( url, requestOptions);
    console.log( "status = " + response.status );
        
    if ( Math.floor( response.status / 100 ) === 2 )
      if ( ++fileProgress.chunkNo < fileProgress.chunkCount )
        setTimeout( startChunk, 10 );
}


function saveEditProps()
{
  toolsocket.sendMessage( new hugeupload.SetResourcePropertiesMessage( 
          finder.editpropsTitle.value, 
          finder.editpropsDescription.value, 
          finder.editpropsStage.value ) );
  arialib.closeDialog( finder.editpropsSaveButtonBottom );
}

function openConfig()
{
  if ( !dynamicData.allowedToConfigure )
  {
    alert( "No permission to configure this tool." );
    return;
  }
  toolsocket.sendMessage( new hugeupload.ConfigurationRequestMessage() );
  arialib.openDialog( 'configdialog', finder.configureButton );
}

function saveConfig()
{
  if ( !dynamicData.allowedToConfigure )
  {
    alert( "No permission to configure this tool." );
    return;
  }
  
  if ( platformconfig === null )
  {
    alert( "Unable to save configuration because none was received." );
    return;
  }

  let updatedconfig = new Object();
  for ( var prop in platformconfig )
  {
    console.log( "Configuration property name: " + prop );
    let inputid = "config_" + prop;
    console.log( inputid );
    let input = finder[inputid];
    console.log( input );
    if ( input )
    {
      if ( input.type === 'checkbox' )
        updatedconfig[prop] = input.checked;
      else
        updatedconfig[prop] = input.value;
      console.log( input.value );
    }
  }
  console.log( updatedconfig );
  
  toolsocket.sendMessage( new hugeupload.ConfigureMessage( updatedconfig ) );
}

function test()
{
  setTimeout(() => {
    addAlert( "Test alert." );
  }, "3000" );
}

window.addEventListener( "load", function(){ init(); } );
document.addEventListener( "DOMContentLoaded", function()
  { 
    console.log( "DOMContentLoaded event arrived." ); 
    console.log( finder.toplevelalert );
  } );
