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

class  MessageBuilder {
	
	List<Message> Messages = new ArrayList<Message>();
    int seqNum=0;

	void setSequenceNum( int seqNum)
	{
		this.seqNum=seqNum;
	}
	void AddMessage(Message msg)
	{
		Messages.add(msg);
			
	}
	int getMessageCount()
	{
		return Messages.size();
	}
	int getSequenceNum()
	{
		return seqNum;
	}

	void clear()
	{
		Messages.clear();
	}


	byte[] toByteArray( )
	{
		int totalsize=0;
		byte [] payload;
		int index=0;
		for(int j=0;j<Messages.size();j++)
		{
			totalsize+=Messages.get(j).getMessagesize();
		}
		payload= new byte[1+totalsize];  // to include the seq id
		payload[index]=(byte) seqNum;
		index++;
		
		
		for(int j=0;j<Messages.size();j++)
		{
			byte[] msgArr=Messages.get(j).toByteArray();
			for(int i=0;i<msgArr.length;i++)
			{
				payload[index]=msgArr[i];
		//		System.out.println(payload[index]) ;
				index++;
			}
		}
		//System.out.println("Done");
		return payload;
	}
}
