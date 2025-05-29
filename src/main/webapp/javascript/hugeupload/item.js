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
import sha512lib from "../common/sha512.js";
import hugeupload from "../generated/hugeupload.js";

let dynamicData = dynamicPageData;

let toolsocket;

let loading;

let resource = null;
        

let platformconfig = null;

const importProgress = new Object();
const uploadProgress = new Object();
const downloadProgress = new Object();

let pendingFileMap = new Object();

function init()
{
  console.log( "init" );

  console.debug( finder.toplevelalert.ariaLive );
  finder.toplevelalert.ariaLive = 'polite';
  console.debug( "Set ariamixin property" );
  console.debug( finder.toplevelalert.ariaLive );
  
  //finder.toplevelalert.setAttribute( 'aria-live', 'polite' );  
  //console.debug( finder.toplevelalert.getAttribute( 'aria-live' ) );

  arialib.setDialogAlertClass( 'alertList' );
  arialib.setBaseAlertElement( finder.toplevelalert );
  setInterval( updateAlerts, 1000 );
      
  console.debug( dynamicData.webSocketUri );
  
  finder.fileselection.addEventListener( 'change', () => importFile() );
  finder.startuploadbutton.addEventListener( 'click', () => alert( 'Not implemented yet' ) );
  finder.stopuploadbutton.addEventListener( 'click', () => alert( 'Not implemented yet' ) );
  finder.startdownloadbutton.addEventListener( 'click', () => startDownload() );
  finder.savebutton.addEventListener( 'click', () => save() );
  
  let handler =
  {
    open()
    {
      toolsocket.sendMessage( new hugeupload.GetResourceMessage() );
    },
    
    handleBinaryTest( message )
    {
      console.log( message.payload );
    },
    
    handleBinaryChunkUploadAck( message )
    {
      processChunkAck( message.payload );
    },
    
    handleBinaryChunkUploadRequest( message )
    {
      processChunkReq( message.payload );
    },
    
    handleBinaryChunkDownload( message )
    {
      processChunkDownload( message.payload );
    },
    
    handleAlert( message )
    {
      alert( message.payload );
    },
    
    handleResource( message )
    {
      resource = message.payload;
      console.log( "receiving resource data" );
      console.log( resource );
      updateResource();
    },
            
    handleConfiguration( message )
    {
      console.log( message );
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
  console.log( "Updating UI to reflect changes to resource." );
}

async function importFile()
{
  const fileInput = document.querySelector("input[type=file]");
  if ( fileInput.files.length < 1 ) { alert( "No files selected."             ); return; }
  if ( fileInput.files.length > 1 ) { alert( "Only one file may be selected." ); return; }
  const digester    = new sha512lib.Sha512();
  const inFile      = fileInput.files[0];
  const opfsRoot    = await navigator.storage.getDirectory();
  const outFileName = "import.bin";
  const fileHandle  = await opfsRoot.getFileHandle( outFileName, { create: true } );
  const writable    = await fileHandle.createWritable();
  const maxSize     = 10*1024*1024;
  var end;
  var previousPercent=0;
  const startTime = performance.now();
  for ( var start = 0; start < inFile.size; start+=maxSize )
  {
    end = start + maxSize;
    if ( end > inFile.size ) end = inFile.size;
    const chunkBlob = await inFile.slice( start, end );
    const chunk = await chunkBlob.bytes();
    const littleHash = await crypto.subtle.digest( "SHA-1", chunk );
    const littleHashStr = new Uint8Array(littleHash).toBase64();
    console.log( "Chunk SHA-1 hash ", littleHashStr );
    await writable.write( chunkBlob );
    digester.update( chunk );
    const percent = Math.floor( 100*(end/inFile.size) );
    if ( percent !== previousPercent )
    {
      console.log( "Completed ", percent );
      previousPercent = percent;
    }
  }
  await writable.close();
  const binhash = digester.digest();
  console.log( binhash.toBase64() );
  const endTime = performance.now();
  console.log( "Time taken = ", (endTime-startTime)/1000, "s" );
}


function blobUploadTest()
{
    console.log( "File chunk mapping starting" );
    const fileInput = document.querySelector("input[type=file]");
    if ( fileInput.files.length < 1 )
    {
        alert( "No files selected." );
        return;
    }
    if ( fileInput.files.length > 1 )
    {
        alert( "Only one file may be selected." );
        return;
    }

    uploadProgress.action = "digest";    
    uploadProgress.file = fileInput.files[0];
    uploadProgress.chunkNo = 0;
    uploadProgress.maxChunkSize = 10 * 1024 * 1024;

    uploadProgress.chunkCount=Math.floor( uploadProgress.file.size / uploadProgress.maxChunkSize );
    // Remainder? One smaller chunk
    if ( (uploadProgress.file.size % uploadProgress.maxChunkSize) > 0 )
        uploadProgress.chunkCount++;
  
    pendingFileMap = {};
    pendingFileMap.name         = uploadProgress.file.name;
    pendingFileMap.lastModified = uploadProgress.file.lastModified;
    pendingFileMap.size         = uploadProgress.file.size;
    pendingFileMap.type         = uploadProgress.file.type;
    pendingFileMap.map          = [];

    console.debug( uploadProgress );
    console.debug( pendingFileMap );
    startChunk();
}

function startChunk()
{
  const start = uploadProgress.chunkNo * uploadProgress.maxChunkSize;
  const end   = ( (start + uploadProgress.maxChunkSize) > uploadProgress.file.size ) ? uploadProgress.file.size : start + uploadProgress.maxChunkSize;
  uploadProgress.chunkSize=end-start;

  pendingFileMap.map[uploadProgress.chunkNo] = {};
  pendingFileMap.map[uploadProgress.chunkNo].start = start;
  pendingFileMap.map[uploadProgress.chunkNo].end   = end;

  console.debug( "Processing chunk " + uploadProgress.chunkNo + " from " + start + " to " + end );

  function handleHash( hash )
  {
    const hashArray = Array.from(new Uint8Array(hash)); // convert buffer to byte array
    const hashHex = hashArray.map((b) => b.toString(16).padStart(2, "0")).join("");
    console.log( "Hash = " + hashHex );        
    pendingFileMap.map[uploadProgress.chunkNo].hash   = hashHex;
    if ( ++uploadProgress.chunkNo < uploadProgress.chunkCount )
      startChunk();
    else
    {
      console.debug( "File mapping complete" );
      console.log( pendingFileMap );
      const message = new hugeupload.FileMapMessage();
      message.payload = pendingFileMap;
      toolsocket.sendMessage( message );
    }
  }

  function chunkLoadProcess( abuffer )
  {
    console.debug( "Buffer slice read into abuffer", abuffer );

    if ( uploadProgress.action === "digest" )
    {
      const data = new Uint8Array( abuffer );
      crypto.subtle.digest( "SHA-256", data ).then( (hash) => handleHash( hash ) );
    }
    if ( uploadProgress.action === "upload" )
    {
      var bcm = new hugeupload.BinaryChunkMessage();
      bcm.payload.id = "made up id";
      bcm.payload.chunkNo = uploadProgress.chunkNo;
      bcm.payload.chunk = data;
      console.debug( "Sending chunk message." );
      toolsocket.sendMessage( bcm );
    }
  }

  uploadProgress.chunk = uploadProgress.file.slice( start, end );
  uploadProgress.chunk.arrayBuffer().then( 
            (abuffer) => chunkLoadProcess( abuffer ),
            (error)   => console.log( "Buffer slice error", error )
          );
}

function processChunkAck( message )
{
  if ( message.chunkNo !== uploadProgress.chunkNo )
  {
    alert( "Received mis-matched chunk no acknowledgement." );
    return;
  }
  
  if ( ++uploadProgress.chunkNo < uploadProgress.chunkCount )
    startChunk();
}

function processChunkReq( message )
{  
  console.debug( message );
  
  // server asked for a chunk to be uploaded
  if ( !uploadProgress.file )
  {
    alert( "No file selected for upload. Resuming upload not yet implemented." );
    return;
  }
  
  function chunkUpload( abuffer )
  {
    const data = new Uint8Array( abuffer );
    console.debug( "Buffer slice read into abuffer", abuffer );
    var bcm = new hugeupload.BinaryChunkMessage();
    bcm.payload.id = "made up id";
    bcm.payload.chunkNo = message.chunkNo;
    bcm.payload.chunk = data;
    console.debug( "Sending chunk message." );
    toolsocket.sendMessage( bcm );
  }
  
  console.log( "Slicing file start = " + message.start + " end = " + message.end );
  const chunk = uploadProgress.file.slice( message.start, message.end );
  chunk.arrayBuffer().then( 
            (abuffer) => chunkUpload( abuffer ),
            (error)   => console.log( "Buffer slice error", error )
          );
}

async function startDownload()
{
  downloadProgress.opfsRoot = await navigator.storage.getDirectory();
  // ToDo - filename must distinguish bewteen platforms/courses/resources
  downloadProgress.fileName = "download.bin";
  downloadProgress.fileHandle = await downloadProgress.opfsRoot.getFileHandle(downloadProgress.fileName, { create: true });
  downloadProgress.writable = await downloadProgress.fileHandle.createWritable();
  
  const chunkReq = new hugeupload.BinaryChunkDownloadRequestMessage();
  chunkReq.payload.chunkNo = 0;
  console.debug( "Sending chunk request message." );
  toolsocket.sendMessage( chunkReq );

  
}

async function processChunkDownload( chunkDown )
{
  const chunkInfo = resource.fileMap.map[chunkDown.chunkNo];
  await downloadProgress.writable.seek( chunkInfo.start );
  await downloadProgress.writable.write( chunkDown.chunk );

  if ( (chunkDown.chunkNo+1) < resource.fileMap.map.length )
  {
    const chunkReq = new hugeupload.BinaryChunkDownloadRequestMessage();
    chunkReq.payload.chunkNo = chunkDown.chunkNo+1;
    console.debug( "Sending chunk request message." );
    toolsocket.sendMessage( chunkReq );    
  }
  else
  {
    downloadProgress.writable.close();
    alert( "Download complete." );
  }
}


async function save()
{
  // https://parzibyte.me/blog/en/2023/10/06/javascript-store-read-files-origin-private-file-system/
  const fileReadHandle = await downloadProgress.opfsRoot.getFileHandle(downloadProgress.fileName, { create: false });
  const file = await fileReadHandle.getFile();
  const objectUrl = URL.createObjectURL(file);
  console.debug( objectUrl );
  const a = document.createElement("a");
  a.href = objectUrl;
  a.download = "thingy.zip";
  a.click();
  URL.revokeObjectURL(objectUrl);  
}
    
    
window.addEventListener( "load", function(){ init(); } );
document.addEventListener( "DOMContentLoaded", function()
  { 
    console.debug( "DOMContentLoaded event arrived." ); 
    console.debug( finder.toplevelalert );
  } );
