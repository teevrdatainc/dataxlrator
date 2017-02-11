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

package io.teevr.coap;

import io.teevr.mqtt.MqttClientAdapter;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.server.resources.CoapExchange;

public class TCoapServerResource extends CoapResource {

	MqttClientAdapter mqttAdapter;
	Object processMsgLock=null;
	public TCoapServerResource(String name,MqttClientAdapter mqttAdapter) {
		super(name);
		this.mqttAdapter=mqttAdapter;
		processMsgLock=new String(name);
	}
	
    @Override
    public void handlePUT(CoapExchange exchange) {

    	String 	datasourceName=exchange.getRequestOptions().getUriQuery().get(0); // We'll use first query string only as topic
      //  synchronized (processMsgLock) 
        {  
        	// access to process message needs to be synchronized
        	//System.out.println("Received payload size: " + exchange.getRequestPayload().length);
        	mqttAdapter.processMessageWithDataSource(datasourceName,exchange.getRequestPayload());
        }
     
         // respond to the request indicating update to the current resource as we are not creating new server resource
    	exchange.respond(ResponseCode.CHANGED);
    }

}
