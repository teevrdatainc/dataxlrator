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

class  Message {
	
	int msgID;
	int lengthIndicator=0;
	byte[] payload;
	int msgSize=0;

	Message(int msgID,byte[] payload)
	{
		this.msgID=msgID;
		this.payload=payload;
		if(msgID == BitUtils.COMPRESSED || msgID == BitUtils.RCOMBO)
		{
			lengthIndicator=BitUtils.READALL;
		}
		else
		{
			if(payload.length<= BitUtils.ONE_BYTE_RANGE)
				lengthIndicator=BitUtils.ONE_BYTE;
			else
				if(payload.length<= BitUtils.TWO_BYTE_RANGE)
				{
					lengthIndicator=BitUtils.TWO_BYTE;
				}
				else
					if(payload.length<=BitUtils.THREE_BYTE_RANGE)
					{
						lengthIndicator=BitUtils.THREE_BYTE;
					}
					else
						lengthIndicator=BitUtils.FOUR_BYTE;
		}
		
		msgSize=1+(lengthIndicator>>>4) + payload.length;
		//System.out.println("Message ID :   " + msgID );

	}
	
	int getMessgeID()
	{
		return msgID;
	}
	byte[] getPayload()
	{
		return payload;
	}

	int getMessagesize()  // return number of bytes
	{
		return msgSize; 
	}
	
	byte[] toByteArray()
	{
		byte[] packedMsg= new byte[msgSize];
		
		int length= payload.length;
		int numLengthBytes=lengthIndicator>>>4;
		int offset=0;

		msgID= msgID | lengthIndicator;		
		
		packedMsg[offset++]=(byte) msgID;

		if(numLengthBytes>0)
			BitUtils.intTobyteArray(length,offset,packedMsg,numLengthBytes);
	
		offset+=numLengthBytes;
		for(int i=0;i<length;i++)
		{
			packedMsg[offset+i]=payload[i];
		}
		
		return packedMsg;
	}
}
