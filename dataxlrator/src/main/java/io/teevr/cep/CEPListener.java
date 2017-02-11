/*
 * Copyright (c) 2015-16,  Teevr Data Inc. All Rights Reserved 
 */

package io.teevr.cep;

import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.MqttException;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import io.teevr.config.DataModel;
import io.teevr.mqtt.MqttClientAdapter;

public class CEPListener implements UpdateListener {
	
	MqttClientAdapter remoteBroker=null;
    String eventsDB;
    String eventType;
    Logger logger=null;
    DataModel eventDataModelObj=null;  // To be used for transferring publish events 
    String location="";
    String uaid="";
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
	CEPListener(String eventName, MqttClientAdapter remoteBroker, String location,String UAID)
	{
		logger = Logger.getLogger("DataXlrator-Edge");
		this.location=location;
		uaid=UAID;
		this.eventType=eventName;
		this.remoteBroker=remoteBroker;
		if(remoteBroker!=null)
		{
			String EventDatamodel=remoteBroker.getEventDataModel();
			if(!EventDatamodel.isEmpty())
			{
				eventDataModelObj=gson.fromJson(EventDatamodel, DataModel.class);
			}
		}
	}
	
	public String getEventJSON(int nType)
	{
		int msg[]={nType};
		JsonObject JsonObj = new JsonObject();
	    String jsonOutput="";
	    for(int i=0;i<msg.length;i++)
		{
	    	
				JsonObj.addProperty(eventDataModelObj.getModel().get(i).getName(),new Double(msg[i]).longValue());
			
		}
	    jsonOutput=gson.toJson(JsonObj);
	    logger.debug(jsonOutput);
	    return jsonOutput;

	}

	public void update(EventBean[] newData, EventBean[] oldData) {
		// TODO Auto-generated method stub
		int nType=0;
		
        ///EventBean event = newData[0];
//    	int curIndex = (Integer) event.get("index");
 /*      if (curIndex >= (eventIndex+updateFreq)) {*/
        //	System.out.println("Event " + eventType + " received: " + newData[0].getUnderlying());
        	
        	if(eventType.equals("moving"))
        	{
        		nType=CEP.MOVING;
        		
        		 float accel = Float.parseFloat(newData[0].get("accel").toString());
				
				 if (accel < 2.0)
				 {
					 nType= CEP.FALLING;
	//				 System.out.println("Accel: Falling :   " + accel + ":" + nType);
				 }
        	}
        	else
        		if(eventType.equals("stationary"))
        		{
        			nType=CEP.STATIONARY;
        		}
 /*       		else
      			if(eventType.equals("falling"))
        			{
        				float accel = Float.parseFloat(newData[0].get("accel").toString());
        				
        				 if (accel < 2.0)
        				 {
        					 nType= CEP.FALLING;
        					 System.out.println("Accel: Falling :   " + accel + ":" + nType);
        				 }
        			} */
        	logger.debug("Motion Event: "  + nType);
        	
        	if(remoteBroker!=null)
        	{
        		//publish the event
            	String topic= new String("teevr/sensors/event/"+uaid + "/"+location);
    			String EventJson= getEventJSON(nType);	
       			try {
					remoteBroker.publish(topic,0,EventJson.getBytes());
				} catch (MqttException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} //publish performance numbers to be consumed by UI
				logger.debug("Location: " + location + "Motion Event: "  + nType);

           		/*try {
     				remoteBroker.publish(topic,1,EventJson.getBytes()); //publish performance numbers to be consumed by UI
           			//remoteBroker.publish(topic,EventJson.getBytes()); //publish performance numbers to be consumed by UI
    				logger.debug("Location: " + location + "Motion Event: "  + nType);
    			} catch (MqttException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}*/

        	}
        //       	eventIndex = curIndex;
        //}

		
	}
}
