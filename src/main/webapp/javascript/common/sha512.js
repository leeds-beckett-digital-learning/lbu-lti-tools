/* 
 * Copyright 2025 maber01.
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


// ref in https://nvlpubs.nist.gov/nistpubs/FIPS/NIST.FIPS.180-4.pdf	

const sha512 = (function () {


class Digester
{
	chunkBitLength      = 1024;
	chunkByteLength     =  128;
	chunkWordLength     =   16;
	minSuffixByteLength =   17; // 1 byte for end marker and 16 (128 bits) for bitlength
	
	// constants [§4.2.3]
	K = 
	[
		0x428a2f98d728ae22n, 0x7137449123ef65cdn, 0xb5c0fbcfec4d3b2fn, 0xe9b5dba58189dbbcn,
		0x3956c25bf348b538n, 0x59f111f1b605d019n, 0x923f82a4af194f9bn, 0xab1c5ed5da6d8118n,
		0xd807aa98a3030242n, 0x12835b0145706fben, 0x243185be4ee4b28cn, 0x550c7dc3d5ffb4e2n,
		0x72be5d74f27b896fn, 0x80deb1fe3b1696b1n, 0x9bdc06a725c71235n, 0xc19bf174cf692694n,
		0xe49b69c19ef14ad2n, 0xefbe4786384f25e3n, 0x0fc19dc68b8cd5b5n, 0x240ca1cc77ac9c65n,
		0x2de92c6f592b0275n, 0x4a7484aa6ea6e483n, 0x5cb0a9dcbd41fbd4n, 0x76f988da831153b5n,
		0x983e5152ee66dfabn, 0xa831c66d2db43210n, 0xb00327c898fb213fn, 0xbf597fc7beef0ee4n,
		0xc6e00bf33da88fc2n, 0xd5a79147930aa725n, 0x06ca6351e003826fn, 0x142929670a0e6e70n,
		0x27b70a8546d22ffcn, 0x2e1b21385c26c926n, 0x4d2c6dfc5ac42aedn, 0x53380d139d95b3dfn,
		0x650a73548baf63den, 0x766a0abb3c77b2a8n, 0x81c2c92e47edaee6n, 0x92722c851482353bn,
		0xa2bfe8a14cf10364n, 0xa81a664bbc423001n, 0xc24b8b70d0f89791n, 0xc76c51a30654be30n,
		0xd192e819d6ef5218n, 0xd69906245565a910n, 0xf40e35855771202an, 0x106aa07032bbd1b8n,
		0x19a4c116b8d2d0c8n, 0x1e376c085141ab53n, 0x2748774cdf8eeb99n, 0x34b0bcb5e19b48a8n,
		0x391c0cb3c5c95a63n, 0x4ed8aa4ae3418acbn, 0x5b9cca4f7763e373n, 0x682e6ff3d6b2b8a3n,
		0x748f82ee5defb2fcn, 0x78a5636f43172f60n, 0x84c87814a1f0ab72n, 0x8cc702081a6439ecn,
		0x90befffa23631e28n, 0xa4506cebde82bde9n, 0xbef9a3f7b2c67915n, 0xc67178f2e372532bn,
		0xca273eceea26619cn, 0xd186b8c721c0c207n, 0xeada7dd6cde0eb1en, 0xf57d4f7fee6ed178n,
		0x06f067aa72176fban, 0x0a637dc5a2c898a6n, 0x113f9804bef90daen, 0x1b710b35131c471bn,
		0x28db77f523047d84n, 0x32caab7b40c72493n, 0x3c9ebe0a15c9bebcn, 0x431d67c49c100d4cn,
		0x4cc5d4becb3e42b6n, 0x597f299cfc657e2an, 0x5fcb6fab3ad6faecn, 0x6c44198c4a475817n
	];

	// initial hash value [§5.3.5]
	initH =
	[
		0x6a09e667f3bcc908n,
		0xbb67ae8584caa73bn,
		0x3c6ef372fe94f82bn,
		0xa54ff53a5f1d36f1n,
		0x510e527fade682d1n,
		0x9b05688c2b3e6c1fn,
		0x1f83d9abfb41bd6bn,
		0x5be0cd19137e2179n
	];

	constructor()
	{
		this.H = new Array( this.initH.length );
		for ( var i=0; i<this.H.length; i++ )
			this.H[i] = this.initH[i];
		this.complete = false;
		this.remnant = null;
		this.bitLength = 0n;  // BigInt
		// A chunk in the form of an array of BigInt
		this.M = new Array();
    // For constructing a chunk from the tail of one input
    // and the head of the next chunk. Otherwise data view
    // is mapped directly onto the input.
    this.borderBuffer        = new ArrayBuffer( this.chunkByteLength );
    this.borderBufferContent = 0;
	}

    /**
     * Rotates right (circular right shift) value x by n positions [§3.2.4].
     * @private
     */
    static ROTR(n, x) {  return (x >> n) | (x << (64n - n));  }

    /**
     * Logical functions [§4.1.2].
     * @private
     */
    static ?0(x) { return this.ROTR(28n, x) ^ this.ROTR(34n, x) ^ this.ROTR(39n, x); }
    static ?1(x) { return this.ROTR(14n, x) ^ this.ROTR(18n, x) ^ this.ROTR(41n, x); }
    static ?0(x) { return this.ROTR( 1n, x) ^ this.ROTR( 8n, x) ^ (x>>7n);           }
    static ?1(x) { return this.ROTR(19n, x) ^ this.ROTR(61n, x) ^ (x>>6n);           }
    static  Ch(x, y, z) { return (x & y) ^ (~x & z);           } // 'choice'
    static Maj(x, y, z) { return (x & y) ^ ( x & z) ^ (y & z); } // 'majority'



	// Must be multiple of chunkByteLength bytes long unless last chunk.
	update( arrayBuffer )
	{
		if ( this.complete )
			throw new Error( "Update to hash attempted after last update indicated." );
				
		var start;
		for ( start = 0; start < arrayBuffer.byteLength; start += this.chunkByteLength )
		{
			const len = Math.min( arrayBuffer.byteLength - start, this.chunkByteLength );
			// Always make this last if size less than full chunk
			this.updateNextByteArrayChunk( new DataView( arrayBuffer, start, len ), len !== this.chunkByteLength );
		}
	}

	close()
	{
		if ( this.complete ) return;
		const buffer = new ArrayBuffer(0);
		const data = new DataView( buffer );
		this.updateNextByteArrayChunk( data, true );
	}

	updateNextByteArrayChunk( data, last )
	{
		if ( data.byteLength > this.chunkByteLength )
			throw new Error( "Attempt to update digest with chunk bigger than 128 bytes." );
		if ( this.complete )
			return;
		this.complete = last;
		this.bitLength += BigInt( data.byteLength )*8n;

		if ( !last )
		{
			this.convertChunk( data );
			return;
		}
		
		// Last chunk may be too long to fit suffix so get ready
		// for using an additional chunk.
		// Suffix could straddle two chunks.
		const isExtraNeeded = ( data.byteLength + this.minSuffixByteLength ) > this.chunkByteLength;

		const dupChunkBuffer  = new ArrayBuffer( this.chunkByteLength );
		const dupData = new DataView( dupChunkBuffer );
		for ( var i=0; i<this.chunkByteLength; i++ )
			dupData.setUint8( i, (i<data.byteLength )?data.getUint8( i ):0 );
		// dupData is now duplicate of data so we can meddle with it.
		
		// Does the end of data bit fit here? Only need one byte
		if ( data.byteLength < this.chunkByteLength )
			dupData.setUint8( data.byteLength, 0x80 );		
			
		// Does the bit length go in this chunk?
		if ( !isExtraNeeded )
			dupData.setBigUint64( this.chunkByteLength - 8, this.bitLength );
		
		this.convertChunk( dupData );

		if ( !isExtraNeeded )
			return;
		
		const extraChunkBuffer  = new ArrayBuffer( this.chunkByteLength );
		const extraData = new DataView( extraChunkBuffer );
		// If data chunk was full the end of stream bit
		// goes at start of this extra chunk
		if ( data.byteLength === this.chunkByteLength )
			extraData.setUint8( 0, 0x80 );		
		// Bit length goes here
		extraData.setBigUint64( this.chunkByteLength - 8, this.bitLength );
		this.convertChunk( extraData );
	}

	toHex( n )
	{
		var str = n.toString( 16 );
		while ( str.length < 16 )
			str = "0" + str;	
		return str;
	}

	convertChunk( data )
	{
		var M = new Array( this.chunkWordLength );
		var str;
//		console.log( "Chunk ----------------------------" );
		for ( var i=0; i<this.chunkWordLength; i++ )
		{
			M[i] = data.getBigUint64( i*8 );
//			console.log( "    " + this.toHex( M[i] ) );
		}
		this.processNextChunk( M );
	}
	
	processNextChunk( M )
	{
		if ( M.length !==  this.chunkWordLength )
			throw new Error( "Chunk is wrong length for hash iteration." );

	    var H = this.H;
		const W = new Array( 80 );

		// 1 - prepare message schedule 'W'
		for (let t=0;  t<80; t++)
			if ( t < 16 )
				W[t] = M[t];
			else
			{
//				console.log( t );
//				console.log( "          " + this.toHex( Digester.?1(W[t-2]) )  );
//				console.log( "          " + this.toHex( W[t-7] )               );
//				console.log( "          " + this.toHex( Digester.?0(W[t-15]) ) );
//				console.log( "          " + this.toHex( W[t-16] )              );
				W[t] = (Digester.?1(W[t-2]) + W[t-7] + Digester.?0(W[t-15]) + W[t-16]) & 0xffffffffffffffffn;
//				console.log( "    " + this.toHex( W[t]) );
			}

//		console.log( "W------------------" );
//		for ( let t=0; t<80; t++ )
//			console.log( "    " + this.toHex( W[t]) );
		
		// 2 - initialise working variables a, b, c, d, e, f, g, h with previous hash value
		let a = H[0], b = H[1], c = H[2], d = H[3], e = H[4], f = H[5], g = H[6], h = H[7];

		// 3 - main loop
		for (let t=0; t<80; t++) {
			const T1 = h + Digester.?1(e) + Digester.Ch(e, f, g) + this.K[t] + W[t];
			const T2 =     Digester.?0(a) + Digester.Maj(a, b, c);
			h = g;
			g = f;
			f = e;
			e = (d + T1) & 0xffffffffffffffffn;
			d = c;
			c = b;
			b = a;
			a = (T1 + T2) & 0xffffffffffffffffn;
		}

		// 4 - compute the new intermediate hash value
		H[0] = (H[0]+a) & 0xffffffffffffffffn;
		H[1] = (H[1]+b) & 0xffffffffffffffffn;
		H[2] = (H[2]+c) & 0xffffffffffffffffn;
		H[3] = (H[3]+d) & 0xffffffffffffffffn;
		H[4] = (H[4]+e) & 0xffffffffffffffffn;
		H[5] = (H[5]+f) & 0xffffffffffffffffn;
		H[6] = (H[6]+g) & 0xffffffffffffffffn;
		H[7] = (H[7]+h) & 0xffffffffffffffffn;
//		console.log( "H------------------" );
//		for ( let i=0; i<8; i++ )
//			console.log( "    " + this.toHex( H[i]) );
	}

	getHashHex()
	{
	    var H = this.H;
		var hash = "";
		for ( var i=0; i<8; i++ )
			hash = hash + this.toHex( (H[i] ) );
		return hash;
	}
}		

})();


export default sha512;
