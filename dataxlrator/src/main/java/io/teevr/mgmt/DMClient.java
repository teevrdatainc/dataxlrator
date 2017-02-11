package io.teevr.mgmt;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Scanner;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import io.teevr.config.Configuration;
import io.teevr.mqtt.MQTTExternalMessageHandler;
import io.teevr.mqtt.MqttClientAdapter;
import io.teevr.mqtt.MqttClientAdapter.MQTTConfigtoUse;

public class DMClient implements MQTTExternalMessageHandler,Runnable{
	
	Logger logger=null;
	Configuration config;
	String version;
	boolean bIsRemoteEndpoint;
    MqttClientAdapter TxClient=null;
    MqttClientAdapter RxClient=null;
    String ClientID="";
	// subscription topics
	String RestartCmd= "teevr/mgmt/restart";  // Restart DataXlrator
	String ConfigUpdateCmd="teevr/mgmt/configupdate";  // Update Configuration
	String SWUpdateCmd="teevr/mgmt/swupdate";  // Update DataXlrator
	
	// publish topics
	String VersionCmd="teevr/mgmt/version";  // DataXlrator Version
	String StatusCmd="teevr/mgmt/status";  // DataXlrator Running Status
	
	public DMClient(Configuration config, String version, boolean bIsRemoteEndpoint) 
	{
		this.config=config;
		this.version=version;
		this.bIsRemoteEndpoint=bIsRemoteEndpoint;
		if(bIsRemoteEndpoint)
			 logger = Logger.getLogger("DataXlrator-Cloud");
		 else
			 logger = Logger.getLogger("DataXlrator-Edge");
	}

	public void init()
	{
		String hostName="teevr";
		 Process proc = null;
		try {
			proc = Runtime.getRuntime().exec("hostname");
			InputStream stream = proc.getInputStream();
	        Scanner scanner = new Scanner(stream);
			Scanner s = scanner.useDelimiter("\\A");
	        hostName= s.hasNext() ? s.next() : "teevr";
	        hostName= StringUtils.chomp(hostName);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			hostName="teevr";
		}
		String TxclientID= hostName + "-mgmt-pub-"+ Math.round((Math.random()*10));
		String RxclientID= hostName + "-mgmt-recv-" +Math.round((Math.random()*10));
		TxClient= new MqttClientAdapter(config,MqttClientAdapter.MQTTConfigtoUse.MGMT,bIsRemoteEndpoint,true);
		TxClient.setClientID(TxclientID);
		TxClient.setasPublisher(true);
		TxClient.init();

		RxClient= new MqttClientAdapter(config,MqttClientAdapter.MQTTConfigtoUse.MGMT,bIsRemoteEndpoint,true);
		RxClient.setClientID(RxclientID);
		RxClient.setMessageHandler(this);
		RxClient.init();
		
		
		// Report the version and status of dataxlrator
		ClientID=config.getMQTT().getMgmtMQTT().getClientID();
		if(ClientID.isEmpty())
			ClientID=hostName;
		
		TxClient.publish(VersionCmd+"/"+ClientID, version.getBytes());
	}
	

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) {
		// TODO Auto-generated method stub
		
		// get message destination and command and it's payload
		
		String topicArr[]= topic.split("/");
		String command= topicArr[2];
		String destID= topicArr[3];
		if(destID.equals(ClientID))  // This message is destined for the current client
		{
			if(command.equalsIgnoreCase("advanced"))
			{
				// we expect payload too
				String payload= new String(message.getPayload());
				String line = null;
				BufferedReader in;
				File workingDir=null;
				logger.info("Received Command: " + payload);
				Process p;
				try {
					p=Runtime.getRuntime().exec("pwd");
					in = new BufferedReader(
                            new InputStreamReader(p.getInputStream()));
					line=null;
					while ((line = in.readLine()) != null) {
						logger.info("cwd: " + line);
						workingDir= new File(line);
					}	
					p = Runtime.getRuntime().exec(payload,null,workingDir);
					in = new BufferedReader(
                            new InputStreamReader(p.getInputStream()));
					line=null;
					while ((line = in.readLine()) != null) {
						logger.info("advanced: " + line);
					}
					//p.waitFor();
				//	p.destroy();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				 
			}
		}
		
		
	}
}
