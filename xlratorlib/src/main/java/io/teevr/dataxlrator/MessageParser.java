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

import java.util.ArrayList;
import java.util.List;

class MessageParser {
	
	List<Message> Messages = new ArrayList<Message>();
	
    int seqNum=0;
	
    MessageParser( byte[] payload)
	{
		parsePayload(payload); 
	}

	private void  parsePayload(byte[] payload) {
		
		// parse received payload
		byte payloadArr[];
		long payloadLength= payload.length; // length in bits
		int offset=0;
		int lengthIndicator=0;
		int msgID=0;
		int numBytesToRead=0;
		
		// Get the sequence ID
		seqNum=0xff & payload[offset++];  // avoid upcasting to negative number
	
		//System.out.println("Seq Num: " + seqNum );
		
		while(offset<payloadLength)
		{
			msgID=payload[offset++];
			//System.out.println("Msg ID: " + msgID );
		    lengthIndicator=msgID>>>4;
		    //System.out.println("Length: " + lengthIndicator);
			msgID= msgID & BitUtils.MSGID_MASK;
			
			if(lengthIndicator==0)
			{
				// calculate remaining bytes to be read.
				numBytesToRead = payload.length-offset;
				//System.out.println("Lenght Indicator 0 numBytesToread: " + numBytesToRead);
			}
			else
			{
				numBytesToRead=BitUtils.byteArrayToInt(offset,payload,lengthIndicator);
				//System.out.println("numBytesToread: " + numBytesToRead);
				offset+=lengthIndicator;
			}
			//System.out.println("Offset: " + offset);
			payloadArr= new byte[numBytesToRead];
			for(int i=0;i<numBytesToRead;i++)
			{
				payloadArr[i]=payload[offset+i];
			}

			offset+=numBytesToRead;
			
			// Add the message to Q
			Messages.add(new Message(msgID,payloadArr));

		}
		
	}

	int getSequenceNum()
	{
		return seqNum;
	}

	int getMessageCount()
	{
		return Messages.size();
	}
	
	Message getMessage(int index)
	{
		if(index<Messages.size())
		{
			return Messages.get(index);
		}
		else
			return null;
	}

	byte[] toByteArray()
	{
		int totalsize=0;
		int index=0;
		for(int j=0;j<Messages.size();j++)
		{
			totalsize+=Messages.get(j).getMessagesize();
		}
		byte [] payload= new byte[totalsize];
		
		for(int j=0;j<Messages.size();j++)
		{
			byte[] msgArr=Messages.get(j).toByteArray();
			for(int i=0;i<msgArr.length;i++)
			{
				payload[index++]=msgArr[i];
			}
		}
		return payload;
	}
}
