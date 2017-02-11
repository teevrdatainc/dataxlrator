/*
Copyright (c) 2015-17,  Teevr Data Inc. All Rights Reserved 
MIT License
Copyright (c) 2015-17 Teevr Data Inc

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */

package io.teevr.dataxlrator;

class BitUtils {
	
	public static  int HEXSTRING_PRECISION = -995; // set the precision value to indicate it is Hexstring field
	public static  int TIMESTAMP_PRECISION = -996; // set the precision value to indicate it is Timestamp field
	public static  int ARRAY_PRECISION = -997; // set the precision value to indicate it is a array json field
	public static  int BOOLEAN_PRECISION = -998; // set the precision value to indicate it is a boolean field
	public static  int STRING_PRECISION  = -999; // set the precision to indicate String Value
	public static  int MAX_SEQ_NUMBER	 = 255;  // Increase this size if there are chances of getting more out of order messages 
												 // Default is kept to storage in single byte 	

	public static int MAX_JSON_ARRAY_SIZE = 15;   // Maximum size of a JSON Array. This is required to allocate space during startup
	public static  int MAX_NUMBER_STRINGS=127; // Number of strings
	public static  int MAX_STRING_SIZE=255;  // Maximum size of String in bytes

	/* Message IDs for bit packing  Lower nibble */
	public static final int MSGID_MASK= 0x0F;
	public static final int RCOMBO = 0x00;
	public static final int DATAMODEL = 0x01;
	public static final int SCALE = 0x02;
	public static final int COMPRESSED = 0x03;
	public static final int JSONMODEL = 0x04;
	public static final int STR_PAYLOAD = 0x05;
	public static final int ARR_UPDATE = 0x06;
	public static final int ARR_MODEL_UPDATE = 0x07; // used to update array model later if the starting array had zero size
	public static final int COMPRESSION_PARAMS = 0x08;  // use to sync cloud params with edge since in APIs we can not configure cloud
	public static final int COMBO = 0x09;
	
	/* Number of bytes in payload for length indicator*/
	public static int READALL= 0x00;   // Read till the end. To be used by Compressed and Combo messages since those messages will be packed last.
	public static int ONE_BYTE=0x10;  // One byte is payload for length indicator
	public static int TWO_BYTE=0x20;  // two bytes is payload for length indicator
	public static int THREE_BYTE=0x30;  // three byte is payload for length indicator
	public static int FOUR_BYTE=0x40;  // four bytes is payload for length indicator
	
	/* Length Length Range for each byte*/
	public static long ONE_BYTE_RANGE=0xFF;
	public static long TWO_BYTE_RANGE=0xFFFF;
	public static long THREE_BYTE_RANGE=0xFFFFFF;
	public static long FOUR_BYTE_RANGE=0xFFFFFF;
	
	
	/* Msg format
	 * 
	 *   1 byte: Seq ID
	 *   1 byte:  MSG ID + LENGTH INDICATOR 
	 *   0-4 bytes:  Payload length 
	 *   variable bytes: Actual payload
	 *   
	 *   
	 *	     0                   1                   2                   3
	 *       0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
	 *	   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	 *	   |      SEQ      | LENID | MSGID |     PAYLOAD                   |
	 *	   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	 *	  
	 *		SEQ: Packet Sequence number  1 byte
	 *      LENID: Payload Length Indicator
	 *      	   00 - Read ALL remaining bytes post MSGID as payload, Applicable for COMBO and COMPRESSED Messages
	 *      	   01 - Payload has one byte length for length
	 *             02 - Payload has two bytes for length
	 *             03 - Payload has 3 bytes for length
	 *             04 - Payload has 4 bytes for length		
	 *		MSGID: Message Type Indicator
	 *      	   00 - RCOMBO - Combo message for Cloud
	 *      	   01 - DATAMODEL Datamodel packet
	 *             02 - SCALE     Precision Change message 
	 *             03 - COMPRESSED  Compressed payload
	 *             04 - JSONMODEL  JSON DataModel			 
   	 *             05 - STRPAYLOAD String payload 
	 *             06 - ARRUPDATE  Array update message
	 *             07 - ARRADD     Add array. To be used when Array has non-zero size when compared to datamodel
	 *             08 - COMBO     COMBO message(NOT USED)
	 *             			  
 	 *		PAYLOAD: Payload with payload length
 	 *			   0-4 bytes - Length of Payload, with no length payload for COMPRESSED or COMBO data
 	 *			   Variable Length: Payload 			
	 *
	 * 
	 * 
	 * 		ARR_UPDATE:
	 *   
	 *				SEQ ID:  Will use Array Name index
	 *				
	 *
	 *
	 * 		ARR_MODEL_UPDATE:
	 *               1 :  1 byte Array Name index
	 *               Payload: JSON Array as string
	 *   
	 *
	 *	  
	 * */
	
	
	
	public   static void writeBits(long[] out, long val, int outOffset,  int bits) 
	{
	        if (bits == 0)
		            return;
		    int index = outOffset >>> 6;
		    int skip = outOffset & 0x3f;
		    val= (0xffffffffffffffffL & (val << (64 - bits)));
		    out[index] |= (val >>> skip);
		    if (64 - skip < bits) {
		            out[index + 1] |= (val << ( 64- skip));
		    }
	}
	
	
	public  static  long readBits(long[] in,  int inOffset,  int bits) 
	{
	        int index = inOffset >>> 6;
	        int skip = inOffset & 0x3f;
	        long val = in[index] << skip;
	        if (64 - skip < bits) {
	                val |= (in[index + 1] >>> (64 - skip));
	        }
	        val= 0xffffffffffffffffL & (val>>> (64 - bits));
	        return val;
	}

	public  static  byte[] intTobyteArray(int in, int offset, byte[] out) 
	{
    	   out[offset++]= (byte)(0xffff & in>>>24);
    	   
           out[offset++]= (byte)(0xffff & in>>>16);
    	   
    	   out[offset++]=(byte)(0xffff & in>>>8); 
    	   
    	   out[offset++]=(byte)(0xffff & in);
	       return out;
	}


public  static  byte[] intTobyteArray(int in, int offset, byte[] out, int numBytes) 
	{
    	   
	       if(numBytes>3)
	    	   out[offset++]= (byte)(0xffff & in>>>24);
	       
    	   if(numBytes>2)
    		   out[offset++]= (byte)(0xffff & in>>>16);
    	   
    	   if(numBytes>1)
    		   out[offset++]=(byte)(0xffff & in>>>8); 
    	   
    	   out[offset++]=(byte)(0xffff & in);
	       return out;
	}
	
	public  static  int byteArrayToInt(int offset,byte[]in) 
	{
		 int tmpval=0;  
	     int out= 0;
    	 tmpval=0xff & in[offset++];
    	 out= tmpval<<24;
    	  
    	 tmpval=0xff & in[offset++];
    	 out |=tmpval<<16;
    	   
    	   
    	 tmpval=0xff &in[offset++];
    	 out |=tmpval<<8;
    	 
    	 tmpval=0xff &in[offset++];
    	 out|=tmpval; 
	     
    	 return out;
	}

	public  static  int byteArrayToInt(int offset,byte[]in, int numBytes) 
	{
		 int tmpval=0;  
	     int out= 0;
	     
	     if(numBytes>3)
	     {
	    	 tmpval=0xff & in[offset++];
	    	 out= tmpval<<24;
	     }
	     

	      if(numBytes>2)
	      {
	    	 tmpval=0xff & in[offset++];
	    	 out |=tmpval<<16;
	      }

	      if(numBytes>1)
	      {
	
	    	 tmpval=0xff &in[offset++];
	    	 out |=tmpval<<8;
	      }

	      tmpval=0xff &in[offset++];
    	  out|=tmpval; 
	     
    	 return out;
	}

	public  static  byte[] longArrayTobyteArray(long[] in, int nSizebits) 
	{
		   int nSzBytes= 0;
		   if(nSizebits%8==0)
		   {
			   nSzBytes=nSizebits/8;
		   }
		   else
		   {
			   nSzBytes= 1 + nSizebits/8;
		   }
			   
		   //logger.info("Sizein bits: "+nSizebits + "NumBytes: " + nSzBytes );
	       byte[] out= new byte[nSzBytes];
	       int j =0;
	       for(int i=0; j<nSzBytes && i<in.length;i++)
	       {
	    	   if(j<nSzBytes)
	        	   out[j++]= (byte)(0xffff & in[i]>>>56);
	    	   if(j<nSzBytes)
	        	   out[j++]= (byte)(0xffff & in[i]>>>48);
	    	   if(j<nSzBytes)
	        	   out[j++]= (byte)(0xffff & in[i]>>>40);
	    	   if(j<nSzBytes)
	        	   out[j++]= (byte)(0xffff & in[i]>>>32);
	    	   if(j<nSzBytes)
	    	   out[j++]= (byte)(0xffff & in[i]>>>24);
	    	   if(j<nSzBytes)
	           out[j++]= (byte)(0xffff & in[i]>>>16);
	    	   if(j<nSzBytes)
	    	   out[j++]=(byte)(0xffff & in[i]>>>8); 
	    	   if(j<nSzBytes)
	    	   out[j++]=(byte)(0xffff & in[i]);
	       }
	       return out;
	}
	public  static  long[] byteArrayTolongArray(byte[] in) 
	{
		   int szLongArray= 1+ in.length/8;
		   
	       long[] out= new long[szLongArray];
	       int j =0;
	       long tmpval=0;
//	       for(int i=0;i<in.length;i++)
//	    	   System.out.println(" Byte Array index  in " + i + "  "+ in[i]);
	       for(int i=0; i<out.length;i++)
	       {
	    	   if(j< in.length)
	    	   {
	    	   tmpval=0xff & in[j++];
	    	   out[i]= tmpval<<56;
	    	   }
	    	   if(j< in.length)
	    	   {
	    	   tmpval=0xff & in[j++];
	    	   out[i]|= tmpval<<48;
	    	   }
	    	   if(j< in.length)
	    	   {
	    	   tmpval=0xff & in[j++];
	    	   out[i]|= tmpval<<40;
	    	   }
	    	   if(j< in.length)
	    	   {
	    	   tmpval=0xff & in[j++];
	    	   out[i]|= tmpval<<32;
	    	   }
	    	   
	    	   if(j< in.length)
	    	   {
	    	   tmpval=0xff & in[j++];
	    	   out[i]|= tmpval<<24;
	    	   }
	    	   if(j< in.length)
	    	   {
	    	   tmpval=0xff & in[j++];
	    	   out[i]|=tmpval<<16;
	    	   }
	    	   if(j< in.length)
	    	   {
	    	   tmpval=0xff &in[j++];
	    	   out[i]|=tmpval<<8;
	    	   }
	    	   if(j< in.length)
	    	   {
	    	   tmpval=0xff &in[j++];
	    	   out[i]|=tmpval; 
	    	   }
	       }
	       in=null;
	       return out;
	}

}
