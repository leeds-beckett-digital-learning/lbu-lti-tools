
import wasmBase64 from '../wasm/sha512.js';

const sha512lib = (function ()
{
  // Base64 to binary string
  const wasmBinaryString = atob( wasmBase64 );
  // Binary string to Uint8Array
  const wasmByteArray = new Uint8Array( wasmBinaryString.length );
  for ( var i=0; i<wasmBinaryString.length; i++ )
      wasmByteArray[i] = wasmBinaryString.charCodeAt( i );
  // Convert to WebAssembly module
  const wasmModule = new WebAssembly.Module( wasmByteArray );
  // Instantiate with above import definitions and fetch exports
  const wasm = new WebAssembly.Instance( wasmModule ).exports;
  // a byte array corresponding to WebAssembly memory object
  // shared by all instances of Sha512 class
  var _memory = new Uint8Array(wasm.memory.buffer);

  // Parameters to wasm.sha512 function
  // wasm.sha512( 
  //              index in memory to state, 
  //              index in memory to input data,
  //              number of bytes of input data
  //              0 for process input data, 1 for remnant input from previous update
  //            )
  // So, input data has to be pushed into memory before calling the function.

  // head and freeList help keep track of the block of memory
  // shared between Javascript and the Wasm module
  // 
  // head is byte index to the end of an array of
  // 216 byte states. If a new active state is required
  // by another instance of Sha512 class, head is incremeted
  // by 216.  It doesn't matter that input data goes just above
  // this area because that is only pushed in immediately
  // before calling wasm.Sha512 and is no longer needed
  // when the function returns.
  let head = 0;
  
  // freeList contains indices into state bytes that were
  // in use by instances but aren't any more.
  const freeList = [];

  
  const BLOCKSIZE    = 128;  // Size of a single block processed by Sha-512 algorithm
  const SHA512_BYTES =  64;  // Size of working and final hash
  
  const INPUT_OFFSET =  80;  // Offset into state of the block of input that needs to be stored after each update
  const STATEBYTES   = 216;  // Total number of bytes in state

  const MAXINPUTBYTES = 1024;

  
  // Utility function will grow the wasm memory if necessary
  // Note it is not possible to shrink it again.
  var reallocateMemory = function (size)
  {
    // How many standard wasm blocks need to be added?
    const blocks = Math.max(0, Math.ceil(Math.abs(size - _memory.length) / 65536));
    console.log( "Grow memory by " + blocks + " blocks." );
    wasm.memory.grow( blocks );
    console.log( "Memory size is now " + BigInt( wasm.memory.buffer.byteLength ).toString(16) );
    // get a new byte array view onto the bigger arraybuffer
    _memory = new Uint8Array(wasm.memory.buffer);
  };

  // Utility function to round up number to nearest
  // multiple of base number.
  // only works for base that is power of 2
  function roundUp (n, base)
  {
    return (n + base - 1) & -base;
  }


  // The library object that will be exported
  let lib = new Object();

  lib.Sha512 = class
  {
    // To instantiate a digester
    constructor()
    {
      // If no state blocks available
      // make another one and record it as free
      if (!freeList.length)
      {
        freeList.push(head);
        head += STATEBYTES;
      }
      this.finalized = false;
      this.digestLength = SHA512_BYTES;
      // Record where this instance's state is stored
      this.pointer = freeList.pop();
      console.log( "pointer = ", this.pointer );
      // pos counts the input bytes left over after call to
      // update because they didn't fit into a block.
      this.pos = 0;
      // Not enough memory for state?
      if (this.pointer + this.STATEBYTES > _memory.length)
        reallocateMemory(this.pointer + STATEBYTES);
      // Initialise state by zeroing every byte
      _memory.fill(0, this.pointer, this.pointer + STATEBYTES);
      this.resultBuf=null;
    }  

    // Put more data through the algorithm
    update( input )
    {
      console.assert(this.finalized === false, 'Hash instance finalized');
      // state blocks are 216 bytes long so head will be
      // on 64 bit word boundary if wasm memory was allocated on such a boundary
      console.assert(head % 8 === 0, 'input should be aligned for int64');
      console.assert(input instanceof Uint8Array, 'input must be Uint8Array or Buffer');
      // Is wasm's memory big enough for the input? If not make it bigger
      // It will never be made smaller
      if (head + input.length > _memory.length)
        reallocateMemory(head + input.length);
      
      // Not sure why it is necessary to fill all of this space with zeros 
      _memory.fill(0, head, head + roundUp( input.length, BLOCKSIZE) - BLOCKSIZE);
      // A little bit of the state area may contain input bytes from the previous
      // update. Don't disturb them - fill that area.
      _memory.set(input.subarray(0, BLOCKSIZE - this.pos), this.pointer + INPUT_OFFSET + this.pos);
      // Put the rest of the data above the state blocks
      // last little bit might be moved into the state area for next update.
      _memory.set(input.subarray(BLOCKSIZE - this.pos /* default size - rest of input */), head);
      // What will the next remnant size be?
      this.pos = (this.pos + input.length) & 0x7f; // 0x7f is BLOCKSIZE - 1
      // Ask wasm to process the input. Params:
      // 1) offset to state data
      // 2) offset to input data
      // 3) length of input in bytes
      // 4) 0 indicates that this is not the end of processing
      wasm.sha512(this.pointer, head, input.length, 0);
      return this;
    }

    // Put the last dribble of data through the algorithm and get
    // the hash value;
    digest()
    {
      if ( this.resultBuf )
        return this.resultBuf;
      
      console.assert(this.finalized === false, 'Hash instance finalized')
      this.finalized = true;
      // Release the state data so another instance can use it
      freeList.push(this.pointer);
      // Part of state stores an incomplete block from the last iteration.
      // Part of that block must zeroed out before the whole block is
      // processed. It's done here with Javascript. The other 'last block'
      // prep is done in the wasm code.
      const paddingStart = this.pointer + INPUT_OFFSET + this.pos;
      _memory.fill(0, paddingStart, this.pointer + INPUT_OFFSET + BLOCKSIZE);
      // Finalise the digest using remnant input and the standard trailing
      // bytes.
      // 1) offset to state data
      // 2) offset to input (why? there isn't any!
      // 3) input length == 0
      // 4) 1 means finalise the hash
      wasm.sha512(this.pointer, head, 0, 1);
      // Fetch the digest from the wasm memory into its own buffer
      // which is stored in this javascript object so it is available if
      // this function called again later.
      this.resultBuf = _memory.subarray(this.pointer, this.pointer + this.digestLength);
      return this.resultBuf;
    }
  };

  return lib;
})();

export default sha512lib;


