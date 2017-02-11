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

package io.teevr.mqtt;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

import io.teevr.aws.UsageMeter;
import io.teevr.coap.TCoapClient;
import io.teevr.config.Advanced.Edge2Cloud;
import io.teevr.config.Advanced.LicenseCheckMethod;
import io.teevr.config.Configuration;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
//import org.bouncycastle.jce.provider.*;
//import org.bouncycastle.openssl.*;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;

import com.google.protobuf.InvalidProtocolBufferException;


public class MqttClientAdapter implements MqttCallback, Runnable {
	private MqttAsyncClient 	mqttAsyncClient;
	private MqttConnectOptions 	mqttconOpt;
	Map <String, MQTTMessageHandler> MessageHandlers = new ConcurrentHashMap <String, MQTTMessageHandler>();
	 ConcurrentLinkedQueue<byte[]> MQTTPublishMessageQ = new ConcurrentLinkedQueue<byte[]>();
	 ConcurrentLinkedQueue<String> MQTTPublishTopicQ =new ConcurrentLinkedQueue<String>();
	 public static enum MQTTConfigtoUse
	    {
		    EDGE, CLOUD, PERFMONITOR,MGMT
		}
	String protocol = "tcp://";
	Configuration config;
	boolean bIsRemoteEndpoint=false;
	boolean bUseRemoteBrokerConfig=false; // Use Remote broker address and configuration from config file
	boolean bIsParentClientAdaptr=false;
	MQTTConfigtoUse useConfig=MQTTConfigtoUse.EDGE;
	String connectStatusTopic="teevr/cloud";
	String OfflinePayload= "offline";
	String OnlinePayload="online";
	boolean bIsCloudOffline=true;
	String getConnectStatusTopic="teevr/getstatus";
	String StatusPayload= "connect";
	String ClientDeviceID="";
	//int MAX_EDGE_THREADS= 50;  //
	//int MAX_CLOUD_THREADS= 10000;  //
	//ExecutorService executorService;  // Thread pool
//	String coapHost;
	//int coapPort=5683;

	private String CA_Cert="";
	private String Client_Cert="";
	private String Client_Key="";
	private boolean bPublisher=false;
	String dataSource="";
	 //Logger
	 Logger logger=null;
	 UsageMeter usageMtr=null;
	/*
	 WSServer serverWS =null;
	 WSClient clientWS=null;
*/
	String clientID="";
	
	List<String> MQTTSubTopics=null;
	MQTTExternalMessageHandler mqttExternalHandler= null;
	Object publishQLock=null;
	//Object processMsgQLock=null;
   	
    TCoapClient coapClient=null;
    boolean bEnabledDataComparison=false;  //  If datacomparison is enabled then datagenerator will send with message ids
    private boolean bDisableCloudConnectionHandhsake=true;  // Disable this for sending data without checking for cloud online status
	private boolean bisExternalClient=true; // Set this as true by default for it to be set manually by internal clients(edge and cloud dataxlrator)
	public MqttClientAdapter(Configuration config, boolean bUseRemoteBrokerConfig, boolean bIsRemoteEndpoint, boolean isParent) {
		this(config,bUseRemoteBrokerConfig?MQTTConfigtoUse.CLOUD:MQTTConfigtoUse.EDGE,bIsRemoteEndpoint,isParent);
		
	}
	
	public MqttClientAdapter(Configuration config, MQTTConfigtoUse BrokerConfigtoUse, boolean bIsRemoteEndpoint, boolean isParent) {
		this.config=config;
		this.useConfig=BrokerConfigtoUse;
		this.bIsRemoteEndpoint=bIsRemoteEndpoint;
		bIsParentClientAdaptr=isParent;
		if(isParent && bIsRemoteEndpoint && BrokerConfigtoUse==MQTTConfigtoUse.CLOUD)
		{
			if(config.getAdvanced().getLicenseCheckMethod()== LicenseCheckMethod.AMI)
				usageMtr= new UsageMeter();
		}
		//remotebroker=null;
		//dbAdapter=null;
		//perfMonitorbroker=null;
	   //	if(bIsRemoteEndpoint)
		//	executorService = Executors.newFixedThreadPool(MAX_CLOUD_THREADS);
		//else
			//executorService = Executors.newFixedThreadPool(MAX_EDGE_THREADS);
		
		if(bIsRemoteEndpoint)
			 logger = Logger.getLogger("DataXlrator-Cloud");
		 else
			 logger = Logger.getLogger("DataXlrator-Edge");
		if(BrokerConfigtoUse==MQTTConfigtoUse.MGMT)
			logger=Logger.getLogger("Device-Management-Edge");
		
		if(bIsRemoteEndpoint)
		{
			bIsCloudOffline=false;
		}
		else
		{
			bIsCloudOffline=true;
		}
	
	}

	public void setClientID(String ClientID )
	{
		this.clientID=ClientID;
	}
	
	public void setDataSource(String ds )
	{
		this.dataSource=ds;
	}
	
	public void setMessageHandler(MQTTExternalMessageHandler handler)
	{
		this.mqttExternalHandler=handler;
	}
	
	public void setasPublisher(boolean bPublisher)
	{
		this.bPublisher=bPublisher;
	}
	
	public void setasExternalClient(boolean bIsExternalClient)  // This is for clients used outside of edge and cloud dataxlrator
	{
		this.bisExternalClient=bIsExternalClient;   // We need see if it is datasimulator or device management client so that publish is not blocked as handshake is not required in these cases.
		if(bDisableCloudConnectionHandhsake)
			bisExternalClient=true;  // Force this to be external
	}
	
	public void init()
	{
		/** Start MQTT setup **/
		String tmpDir = System.getProperty("java.io.tmpdir");
		
		String hostName="teevr";
		 Process proc = null;
		 bEnabledDataComparison=config.getAdvanced().getEnableDataComparison();
		try {
			proc = Runtime.getRuntime().exec("hostname");
			InputStream stream = proc.getInputStream();
	        Scanner s = new Scanner(stream).useDelimiter("\\A");
	        hostName= s.hasNext() ? s.next() : "teevr";
	        hostName= StringUtils.chomp(hostName);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			hostName="teevr";
		}
         
   	if(clientID.isEmpty())
			clientID= hostName+ "-" + (bIsRemoteEndpoint?"remote":"local");
		MqttDefaultFilePersistence dataStore = new MqttDefaultFilePersistence(tmpDir);
		
    	String brokerUrl="";
    	String password="";
    	String username="";
    	if(useConfig==MQTTConfigtoUse.CLOUD)
    	{
    		//bUseRemoteBrokerConfig is true and bIsRemoteEndpoint  is false change the clinet id
    		if (config.getMQTT().getCloudMQTT().getEnableSSL())
    			protocol = "ssl://"; 
    		else
    			protocol = "tcp://";

    		if(!config.getMQTT().getCloudMQTT().getClientID().isEmpty())
    		{
     		   clientID=config.getMQTT().getCloudMQTT().getClientID();
     		   clientID= hostName+"-"+clientID;
    		}
    		if(bIsRemoteEndpoint)
    			clientID+="-cloud";
    		else
    			clientID+="-edge";
    		
    		if(bPublisher)
    			clientID+="-pub";
    		
    		if(!dataSource.isEmpty())
    			clientID+="-"+ dataSource;
    		
    		//clientID+="-"+ location;
    		brokerUrl=  new String (protocol + config.getMQTT().getCloudMQTT().getBrokerAddress() + ":" + config.getMQTT().getCloudMQTT().getPort());
    		username= config.getMQTT().getCloudMQTT().getUsername();
    		password= config.getMQTT().getCloudMQTT().getPassword();
    		CA_Cert= config.getMQTT().getCloudMQTT().getCACert(); // "/home/ssmaurya/awscerts/root-CA.crt";
    		Client_Cert=config.getMQTT().getCloudMQTT().getClientCert(); //"/home/ssmaurya/awscerts/6d1fd59188-certificate.pem.crt";
    		Client_Key=config.getMQTT().getCloudMQTT().getClientKey(); //"/home/ssmaurya/awscerts/6d1fd59188-private.pem.key";
 	
    		
    		if(bIsRemoteEndpoint && !bPublisher)
    			MQTTSubTopics=config.getMQTT().getCloudMQTT().getSubscribeTopics();
    	}
    	else if(useConfig==MQTTConfigtoUse.EDGE)
    	{
    		if (config.getMQTT().getEdgeMQTT().getEnableSSL())
    			protocol = "ssl://"; 
    		else
    			protocol = "tcp://";
    		
    		
    		
    		if(!config.getMQTT().getEdgeMQTT().getClientID().isEmpty())
    		{
      		   clientID=config.getMQTT().getEdgeMQTT().getClientID();
      		   clientID= hostName+"-"+clientID;
      		   ClientDeviceID=config.getMQTT().getEdgeMQTT().getClientID();
    		}
    		else
    		{
    			ClientDeviceID=hostName;
    		}
    		if(bPublisher)
    			clientID+="-pub";
    		
    		if(!dataSource.isEmpty())
    			clientID+="-"+ dataSource;

    		brokerUrl=  new String (protocol + config.getMQTT().getEdgeMQTT().getBrokerAddress() + ":" + config.getMQTT().getEdgeMQTT().getPort());
    		username= config.getMQTT().getEdgeMQTT().getUsername();
    		password= config.getMQTT().getEdgeMQTT().getPassword();
    		CA_Cert= config.getMQTT().getEdgeMQTT().getCACert(); //"/home/ssmaurya/Devsetup/MQTTCerts/ca.crt";
    		Client_Cert=config.getMQTT().getEdgeMQTT().getClientCert(); //"/home/ssmaurya/Devsetup/MQTTCerts/client.crt";
    		Client_Key=config.getMQTT().getEdgeMQTT().getClientKey(); //"/home/ssmaurya/Devsetup/MQTTCerts/client.key";
    		if(!bIsRemoteEndpoint && !bPublisher)
    			MQTTSubTopics=config.getMQTT().getEdgeMQTT().getSubscribeTopics();

    	}	
    	else if(useConfig==MQTTConfigtoUse.PERFMONITOR)
    	{
    		
    		if (config.getAdvanced().getUseCloudMQTTForPerfMonitor()) // Cloud MQTt parameters to be used
    		{
        		if (config.getMQTT().getCloudMQTT().getEnableSSL())
        			protocol = "ssl://"; 
        		else
        			protocol = "tcp://";

        		brokerUrl=  new String (protocol + config.getMQTT().getCloudMQTT().getBrokerAddress() + ":" + config.getMQTT().getCloudMQTT().getPort());
        		username= config.getMQTT().getCloudMQTT().getUsername();
        		password= config.getMQTT().getCloudMQTT().getPassword();
        		CA_Cert= config.getMQTT().getCloudMQTT().getCACert(); // "/home/ssmaurya/awscerts/root-CA.crt";
        		Client_Cert=config.getMQTT().getCloudMQTT().getClientCert(); //"/home/ssmaurya/awscerts/6d1fd59188-certificate.pem.crt";
        		Client_Key=config.getMQTT().getCloudMQTT().getClientKey(); //"/home/ssmaurya/awscerts/6d1fd59188-private.pem.key";
        		if(!config.getMQTT().getCloudMQTT().getClientID().isEmpty())
        		{
        		       clientID=config.getMQTT().getCloudMQTT().getClientID();
        		       clientID= hostName+"-"+clientID;
        		}
        		
        		if(bIsRemoteEndpoint)
        			clientID+="-cloud";
        		else
        			clientID+="-edge";
        		
        		clientID+="-perfmonitor";
        		if(!dataSource.isEmpty())
        			clientID+="-"+ dataSource;
        		
    			

    		}
    		else
    		{
	    		if (config.getMQTT().getMonitorMQTT().getEnableSSL())
	    			protocol = "ssl://"; 
	    		else
	    			protocol = "tcp://";
	    		brokerUrl=  new String (protocol + config.getMQTT().getMonitorMQTT().getBrokerAddress() + ":" + config.getMQTT().getMonitorMQTT().getPort());
	    		username= config.getMQTT().getMonitorMQTT().getUsername();
	    		password= config.getMQTT().getMonitorMQTT().getPassword();
	    		CA_Cert= config.getMQTT().getMonitorMQTT().getCACert(); //"/home/ssmaurya/Devsetup/MQTTCerts/ca.crt";
	    		Client_Cert=config.getMQTT().getMonitorMQTT().getClientCert(); //"/home/ssmaurya/Devsetup/MQTTCerts/client.crt";
	    		Client_Key=config.getMQTT().getMonitorMQTT().getClientKey(); //"/home/ssmaurya/Devsetup/MQTTCerts/client.key";
        		if(!config.getMQTT().getMonitorMQTT().getClientID().isEmpty())
        		{
	     		       clientID=config.getMQTT().getMonitorMQTT().getClientID();		
        		       clientID= hostName+"-"+clientID;

        		}
        		if(bIsRemoteEndpoint)
	     			clientID+="-cloud";
	     		else
	     			clientID+="-edge";
	     		
	     		clientID+="-perfmonitor";
	     		if(!dataSource.isEmpty())
	     			clientID+="-"+ dataSource;

    		}
    		
    	}	
    	else
    		if(useConfig==MQTTConfigtoUse.MGMT)
    		{
    			
    			if(!bPublisher)
        			MQTTSubTopics=config.getMQTT().getMgmtMQTT().getSubscribeTopics();
    			
    			// Check if we have to use MonitorMQTT configuration for Mgmt interface
    			if(config.getAdvanced().getUseMonitorMQTTForRemoteMgmt())
    			{
	    			
	        		if (config.getAdvanced().getUseCloudMQTTForPerfMonitor()) // Cloud MQTt parameters to be used
	        		{
	            		if (config.getMQTT().getCloudMQTT().getEnableSSL())
	            			protocol = "ssl://"; 
	            		else
	            			protocol = "tcp://";
	
	            		brokerUrl=  new String (protocol + config.getMQTT().getCloudMQTT().getBrokerAddress() + ":" + config.getMQTT().getCloudMQTT().getPort());
	            		username= config.getMQTT().getCloudMQTT().getUsername();
	            		password= config.getMQTT().getCloudMQTT().getPassword();
	            		CA_Cert= config.getMQTT().getCloudMQTT().getCACert(); // "/home/ssmaurya/awscerts/root-CA.crt";
	            		Client_Cert=config.getMQTT().getCloudMQTT().getClientCert(); //"/home/ssmaurya/awscerts/6d1fd59188-certificate.pem.crt";
	            		Client_Key=config.getMQTT().getCloudMQTT().getClientKey(); //"/home/ssmaurya/awscerts/6d1fd59188-private.pem.key";
	        		}
	        		else
	        		{
	    	    		if (config.getMQTT().getMonitorMQTT().getEnableSSL())
	    	    			protocol = "ssl://"; 
	    	    		else
	    	    			protocol = "tcp://";
	    	    		brokerUrl=  new String (protocol + config.getMQTT().getMonitorMQTT().getBrokerAddress() + ":" + config.getMQTT().getMonitorMQTT().getPort());
	    	    		username= config.getMQTT().getMonitorMQTT().getUsername();
	    	    		password= config.getMQTT().getMonitorMQTT().getPassword();
	    	    		CA_Cert= config.getMQTT().getMonitorMQTT().getCACert(); //"/home/ssmaurya/Devsetup/MQTTCerts/ca.crt";
	    	    		Client_Cert=config.getMQTT().getMonitorMQTT().getClientCert(); //"/home/ssmaurya/Devsetup/MQTTCerts/client.crt";
	    	    		Client_Key=config.getMQTT().getMonitorMQTT().getClientKey(); //"/home/ssmaurya/Devsetup/MQTTCerts/client.key";
	        		}
    			}
    			else
    			{
    	    		if (config.getMQTT().getMgmtMQTT().getEnableSSL())
    	    			protocol = "ssl://"; 
    	    		else
    	    			protocol = "tcp://";
    	    		brokerUrl=  new String (protocol + config.getMQTT().getMgmtMQTT().getBrokerAddress() + ":" + config.getMQTT().getMgmtMQTT().getPort());
    	    		username= config.getMQTT().getMgmtMQTT().getUsername();
    	    		password= config.getMQTT().getMgmtMQTT().getPassword();
    	    		CA_Cert= config.getMQTT().getMgmtMQTT().getCACert(); //"/home/ssmaurya/Devsetup/MQTTCerts/ca.crt";
    	    		Client_Cert=config.getMQTT().getMgmtMQTT().getClientCert(); //"/home/ssmaurya/Devsetup/MQTTCerts/client.crt";
    	    		Client_Key=config.getMQTT().getMgmtMQTT().getClientKey(); //"/home/ssmaurya/Devsetup/MQTTCerts/client.key";
            		
    			}
    			
    		}
    	
    	/*if(dbAdapter !=null)
    	{
	    	//dbName=dbAdapter.getDataDBName();
	    //	perfDbName=dbAdapter.getPerfDBName();
	    	//eventsDbName=dbAdapter.getEventsDBName();
    	}*/
    	
    	
    	//WebSocketImpl.DEBUG = true;
    	
		 // Create Websockets for publishing decompressed data from cloud
		/*if (bIsRemoteEndpoint && config.getWebsockets().getEnableWebsockets())
		{
			try {
				clientWS= new WSClient( new URI("ws://" + config.getWebsockets().getServerAddress() + ":" +config.getWebsockets().getPort()));
				if(clientWS!=null)
					clientWS.connect();
				logger.info("Connected to: " + clientWS.getURI() );
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}*/
    	publishQLock= new String(clientID);
    	try 
		{
			// Construct the connection options object that contains connection parameters
    		// such as cleanSession and LWT
	   		mqttconOpt = new MqttConnectOptions();
	   		mqttconOpt.setCleanSession(true);
	   		mqttconOpt.setConnectionTimeout(600);
	   		
	   		/*if(!bIsRemoteEndpoint)
	   			mqttconOpt.setKeepAliveInterval(600);
	   		else*/
	   			mqttconOpt.setKeepAliveInterval(600);  // try this to see if it addresses EB issue
	   		
            //If edge and remote are on same machine, cleansession clears previous connection and hence remote will be
	   		//in disconnected state so make it false so that if clientID and server Address is same, the previsous session is reused
	   		//mqttconOpt.setCleanSession(false);
	    	if(password!= null ) 
			{
	    		mqttconOpt.setPassword(password.toCharArray());
			}
			if(username != null) {
				mqttconOpt.setUserName(username);
			}

			if(protocol.contains("ssl"))
				try {
					mqttconOpt.setSocketFactory(getSocketFactory(CA_Cert, Client_Cert, Client_Key, ""));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			//System.out.println("ClienttID: " + clientID);

			if(bIsRemoteEndpoint)
			{
				// Set will to notify edge clients of cloud disconnection
				mqttconOpt.setWill(connectStatusTopic, OfflinePayload.getBytes(), 1, false);
			}
    		// Construct a non-blocking MQTT client instance
			mqttAsyncClient = new MqttAsyncClient(brokerUrl,clientID, dataStore);
			// Set this wrapper as the callback handler
			mqttAsyncClient.setCallback(this);
		//	IMqttToken conToken = mqttAsyncClient.connect(mqttconOpt,null, null);
		//	conToken.waitForCompletion();
			
			initiliazeConnect();
			
			// start publish thread
			// This thread is not required for perf monitor clients
			if(bPublisher || bIsParentClientAdaptr)
			{
				if(!bIsParentClientAdaptr)
					new Thread(this,clientID+"-Pub").start();
				else
				{
					new Thread(this,clientID+"-Pub-Parent").start();
				}
				logger.info("Publish Thread started for : " + clientID);
			}

			/*ScheduledExecutorService service = Executors
                    .newSingleThreadScheduledExecutor();
	          service.scheduleAtFixedRate(this, 0, 1, TimeUnit.MILLISECONDS);
	          */
		} catch (MqttException e) {
			e.printStackTrace();
			System.exit(1);
		}

	}
	
    public  void publish(String topicName, byte[] payload)  {
    	
    	synchronized (publishQLock) {
        	MQTTPublishTopicQ.add(topicName);
        	MQTTPublishMessageQ.add(payload);
		}
    	//if(clientWS!=null && clientWS.isOpen())
    	//{
    	//	clientWS.send(topicName);  // Send data to websockets
    	//	clientWS.send(payload);
    	//}
    }
    
    public boolean isQueueEmpty()
    {
    	return MQTTPublishTopicQ.isEmpty() && MQTTPublishMessageQ.isEmpty();
    }
	/**
     * Publish / send a message to an MQTT server
     * @param topicName the name of the topic to publish to
     * @param qos the quality of service to delivery the message at (0,1,2)
     * @param payload the set of bytes to send to the MQTT server
     * @throws MqttException
     */
	
    public  boolean publish(String topicName, int qos, byte[] payload) throws MqttException {
	
    	boolean ret=false;
    	
    	if((mqttAsyncClient.isConnected() && (!bIsCloudOffline || bisExternalClient)) || (mqttAsyncClient.isConnected() && (topicName.equals(getConnectStatusTopic) || topicName.equals(connectStatusTopic)))) // Check that client is Connected and cloud is online
        	{
	    		// Construct the message to send
    			MqttMessage message;
    			if(payload!=null)
    				message = new MqttMessage(payload);
    			else
    				message = new MqttMessage();  // Shiv provide mechanism to publish without payload. Can be used notify no change in data
	        	message.setQos(qos);
	        	IMqttDeliveryToken pubToken = mqttAsyncClient.publish(topicName, message, null, null);
	        	pubToken.waitForCompletion();  //We can assume that once the data is given to paho, it will get delivered with respective qos
	        //	System.out.println(bIsCloudOffline+" : " +  topicName);
	        	ret=true;
        	}
    	else
    	{
    		// try reconnect
    		initiliazeConnect();
    	}
    		
        	
    	return ret;
        	//System.out.println("Published Message Payload Size: " + payload.length);
     }

    /**
     * Subscribe to a topic on an MQTT server
     * Once subscribed this method waits for the messages to arrive from the server
     * that match the subscription. It continues listening for messages until the enter key is
     * pressed.
     * @param topicName to subscribe to (can be wild carded)
     * @param qos the maximum quality of service to receive messages at for this subscription
     * @throws MqttException
     */
    public void subscribe(String[] topics, int qos[]) throws MqttException {

    	if(mqttAsyncClient.isConnected())
	    	{	
    		IMqttToken subToken = mqttAsyncClient.subscribe(topics, qos, null, null);
	    	subToken.waitForCompletion();
	    	}
	    	
    }

    /**
     * Subscribe to a topic on an MQTT server
     * Once subscribed this method waits for the messages to arrive from the server
     * that match the subscription. It continues listening for messages until the enter key is
     * pressed.
     * @param topicName to subscribe to (can be wild carded)
     * @param qos the maximum quality of service to receive messages at for this subscription
     * @throws MqttException
     */
    public void subscribe(String topic, int qos) throws MqttException {

       	if(mqttAsyncClient.isConnected())
    	{	
		IMqttToken subToken = mqttAsyncClient.subscribe(topic, qos, null, null);
    	subToken.waitForCompletion();
    	}
    	
    }

	
	public void connectionLost(Throwable cause) {
		// TODO Auto-generated method stub
		//cause.printStackTrace();
		logger.warn("Connection lost with broker : " + cause.getMessage());
		initiliazeConnect();
	}

	private synchronized void 
	initiliazeConnect() {
		// TODO Auto-generated method stub
		IMqttToken conToken=null;
		try {
			if(!mqttAsyncClient.isConnected())
				conToken = mqttAsyncClient.connect(mqttconOpt,null, null);
		} catch (MqttSecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			if(conToken!=null)
			{
				conToken.waitForCompletion();
				logger.info("Connected to MQTT broker: " + mqttAsyncClient.getServerURI() + " with ClientID: " + mqttAsyncClient.getClientId() );


				// subscribe to topics
				if(MQTTSubTopics!=null && !MQTTSubTopics.isEmpty())
				{
					subscribe(MQTTSubTopics);
					if(bIsRemoteEndpoint)
						logger.info("Subscribed to sensor topics on Cloud");
					else
						logger.info("Subscribed to sensor topics on Edge");
				}
				
				if(bIsRemoteEndpoint)
				{
					// Publish to all edge about cloud being online
					publish(connectStatusTopic,OnlinePayload.getBytes());
					//bIsCloudOffline=false;// So that cloud could always publish the outgoing messages 
				}
				else
				{
					// get connect status from cloud
					// Publish to cloud to get connect status
					publish(getConnectStatusTopic,StatusPayload.getBytes());
					
				}


			}
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
     
	}

	private void subscribe(List<String>MQTTTopics) {
		// TODO Auto-generated method stub
		int nSize= MQTTTopics.size();
		String topics[]= new String[nSize];
		int qos[]= new int[nSize];
		
		for(int i=0;i<nSize;i++)
		{
			topics[i]= MQTTTopics.get(i).toString();
			qos[i]=1;
		}
		
		try {
			subscribe(topics, qos);
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void processMessageWithDataSource(String Datasource, byte[] message)  // This is input from COAP
	{
		String topic;
		if(bIsRemoteEndpoint)
		{
			topic="teevr/msg/"+Datasource;
		}
		else
		{
			topic="teevr/sensors/combo/"+Datasource;
		}

		processMessage(topic,message);
	}
	public void processMessage(String topic, byte[] message)
	{

		String uriArray[];  //=topic.split("/");
		String location=""; // uriArray[3];
		// Let's create handler for each location for now. To be optimized for thread pool executor later
		uriArray=topic.split("/");
		if(bIsRemoteEndpoint)   // Cloud topic is teevr/msg/location
		{
			
			if(topic.contains(getConnectStatusTopic))  // Connect status topic does not have location
			{
				System.out.println(topic + " : " + new String(message));
				publish(connectStatusTopic,OnlinePayload.getBytes());
				
				// Not the cleanest way to notify but it still works
/*				Iterator<String> it = MessageHandlers.keySet().iterator();
				while(it.hasNext()){
					String key = it.next();
					MQTTMessageHandler handler= MessageHandlers.get(key);
					handler.remotebroker.publish(connectStatusTopic, OnlinePayload.getBytes());
//					handler.perfMonitorbroker.setCloudStatus(bIsCloudOffline);
				}
*/
				// Publish to all edge about cloud being online
/*				try {
					publish(connectStatusTopic,0, OnlinePayload.getBytes());
				} catch (MqttException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	*/			
//				System.out.println(connectStatusTopic + " :  Published." );
				
				return;
			}
			else
			{
				// We need to include device identifier along with datasource name to uniquely identify a datasource as 
				// multiple devices could send to same cloud dataxlrator with same datasource name
				// Since cloud topic is teevr/msg/#, we should consider everything after teevr/msg as device+ds
				
				if(uriArray.length>3) // We can assume device id is present
					location= uriArray[2]+"/"+uriArray[3];
				else
					location= uriArray[2];
				
				
			}
		}
		else  // edge topic is teevr/sensors/combo/location
		{
			if(topic.contains(connectStatusTopic))  // Connect status topic does not have location
			{
				//
				String payload= new String(message);
				boolean bCloudConnected=false;
				//System.out.println(payload);
				if(payload.equals(OfflinePayload))
				{
					bCloudConnected=true;
					
				}
				else
					if(payload.equals(OnlinePayload))	
					{
						bCloudConnected=false;
					}

				setCloudStatus(bCloudConnected);
				// Not the cleanest way to notify but it still works
				Iterator<String> it = MessageHandlers.keySet().iterator();
				while(it.hasNext()){
					String key = it.next();
					MQTTMessageHandler handler= MessageHandlers.get(key);
					handler.remotebroker.setCloudStatus(bCloudConnected);
					handler.perfMonitorbroker.setCloudStatus(bCloudConnected);
				}

				return;
			}
			else
			{
				if(config.getAdvanced().getUseDeviceIDinTopic())
				{
					location= ClientDeviceID +"/"+uriArray[3];
				}
				else
				{
					location= uriArray[3];
				}
			}
		}
		
		MQTTMessageHandler handler= MessageHandlers.get(location);
		//logger.info("Handler Size: " + MessageHandlers.size());
		if(handler==null)
		{
			//logger.info("Handler is null");
			handler= new MQTTMessageHandler(config, bIsRemoteEndpoint,location,usageMtr);
			// create a new client adapter for this location
			MqttClientAdapter publisher= getPublisher(bIsRemoteEndpoint,location); 
			publisher.setasExternalClient(false);
			MqttClientAdapter perfMonitor= getPerfMonitor(bIsRemoteEndpoint,location);
		    perfMonitor.setasExternalClient(false);
			handler.init(publisher,perfMonitor);
			//initialize perfdatamodel
			handler.InitPerfDataModel();
			handler.InitADDataModel();
			new Thread(handler,location+"-handler").start();
			MessageHandlers.put(location, handler);
			logger.info("Initialized Handler for " + location +" using topic " + topic);
		}
		
		   handler.AddToDataMessageQ(message);
	/*	if(!bIsRemoteEndpoint)  // Maintain messages in Q for Edge
		{
			//if(!bEnabledDataComparison)
			//{
				// add message id to topic before pushing
			//	handler.lSimulatedMsgId++;
			//	topic=topic+"/"+handler.lSimulatedMsgId;
			//}
			
			 handler.AddToDataMessageQ(message);
		}
		else
		{
			
			// Extract the Message Arr
			MessageFormatterArray.Builder msgArr=MessageFormatterArray.newBuilder();
		
			try {
				msgArr.mergeFrom(message);
			} catch (InvalidProtocolBufferException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			handler.AddToDataMessageQ(msgArr.getSequenceNum(), msgArr);
		}
		*/
	}
	
	public String getEventDataModel()
	{
		return new MQTTMessageHandler().getEventDataModel();
	}
	
	public void setCloudStatus(boolean bIsOffline)
	{
		bIsCloudOffline=bIsOffline;
	}
	
	public void messageArrived(String topic, MqttMessage message)
			throws Exception {
		
		
      //   logger.info("1...");
		
		if (mqttExternalHandler!=null)
		{
			mqttExternalHandler.messageArrived(topic, message);
			return;
		}
		processMessage(topic,message.getPayload());
		message=null;

	}



	private MqttClientAdapter getPerfMonitor(boolean bIsRemoteEndpoint,
			String location) {
		// TODO Auto-generated method stub
		MqttClientAdapter publisher=null;
		if(config.getAdvanced().getEnablePerfMonitor())
		{
			publisher=new MqttClientAdapter(config, MQTTConfigtoUse.PERFMONITOR,bIsRemoteEndpoint,false);
			//publisher.setClientID("teevr-"+location);
			publisher.setDataSource(location);
			//publisher.setasPublisher(true);  // We need this to assign a different client ID
			publisher.init();
		}
		return publisher;
	}

	private MqttClientAdapter getPublisher(boolean bIsRemoteEndpoint, String location) {
		// TODO Auto-generated method stub
		MqttClientAdapter publisher=new MqttClientAdapter(config, MQTTConfigtoUse.CLOUD,bIsRemoteEndpoint,false);
		//publisher.setClientID("teevr-"+location);
		publisher.setDataSource(location);
		publisher.setasPublisher(true);  // We need this to assign a different client ID
		publisher.init(); 

		if(!bIsRemoteEndpoint)
		{
	/*		if(config.getAdvanced().getEdge2Cloud()==Edge2Cloud.SNAPI)
			{
				publisher.snapiClient= new Client(config.getMQTT().getCloudMQTT().getBrokerAddress(),config.getMQTT().getCloudMQTT().getSnapiPort());
				publisher.snapiClient.connect(location);
			}
			else*/
				if(config.getAdvanced().getEdge2Cloud()==Edge2Cloud.COAP)
				{
					publisher.coapClient= new TCoapClient(config,true); // Use cloud coap configuration
					publisher.coapClient.connect();
				}
		}	
		
		return publisher;
	}


	public void deliveryComplete(IMqttDeliveryToken token) {
		// TODO Auto-generated method stub
		
	}

	public PEMParser getPEMParser(String fileName)
	{
		InputStream input = getClass().getResourceAsStream("/certs/" + fileName);
		if (input == null) {
			Path dsResource = Paths.get("./certs/" + fileName);
			try {
				input = Files.newInputStream(dsResource);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
        Reader fRd = new BufferedReader(new InputStreamReader(input));
        return new PEMParser(fRd);
	}
	
	public SSLSocketFactory getSocketFactory (final String caCrtFile, final String crtFile, final String keyFile, 
            final String password) throws Exception
			{
		
			Security.addProvider(new BouncyCastleProvider());
			JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
			JcaX509CertificateConverter certconver = new JcaX509CertificateConverter().setProvider("BC");
			
			// load CA certificate
			logger.info("Processing CA Certifcate: " +  caCrtFile);
			//PEMParser reader = new PEMParser(new InputStreamReader(new ByteArrayInputStream(Files.readAllBytes(Paths.get(caCrtFile)))));
			PEMParser reader=getPEMParser(caCrtFile);
			X509Certificate caCert;  // = (X509Certificate)reader.readObject();
			caCert= certconver.getCertificate((X509CertificateHolder)reader.readObject());
			reader.close();
			
			// load client certificate
			logger.info("Processing Client Certifcate: " + crtFile);
			reader =getPEMParser(crtFile);
			X509Certificate cert; // = (X509Certificate)reader.readObject();
			cert= certconver.getCertificate((X509CertificateHolder)reader.readObject());
			reader.close();
			
			// load client private key
			logger.info("Processing Client Key: " + keyFile);
			reader =getPEMParser(keyFile);
			Object object = reader.readObject();
			PEMDecryptorProvider decProv = new JcePEMDecryptorProviderBuilder().build(password.toCharArray());
  	        //JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
			KeyPair key;
		     if (object instanceof PEMEncryptedKeyPair) {
		        //System.out.println("Encrypted key - we will use provided password");
		        key = converter.getKeyPair(((PEMEncryptedKeyPair) object).decryptKeyPair(decProv));
		    } else if (object instanceof PEMKeyPair) {
		        //System.out.println("Unencrypted key - no password needed");
		        key = converter.getKeyPair((PEMKeyPair) object);
		    }
		    else
		    {
		    	logger.error("OOps!!! Unencrypted key - Something wron with the key!!!!");
		    	key=null;
		    }

		     
		     
			reader.close();
			
			// CA certificate is used to authenticate server
			KeyStore caKs = KeyStore.getInstance(KeyStore.getDefaultType());
			caKs.load(null, null);
			caKs.setCertificateEntry("ca-certificate", caCert);
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			tmf.init(caKs);
			
			// client key and certificates are sent to server so it can authenticate us
			KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
			ks.load(null, null);
			ks.setCertificateEntry("certificate", cert);
			ks.setKeyEntry("private-key", key.getPrivate(), password.toCharArray(), new java.security.cert.Certificate[]{cert});
			
			KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(ks, password.toCharArray());
			
			// finally, create SSL socket factory
			SSLContext context = SSLContext.getInstance("TLSv1.2");
			context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
					return context.getSocketFactory();
			}

	

	@Override
	public void run() {
		// TODO Auto-generated method stub
		long start=0;
		
		while(true)
		{
			if(!MQTTPublishTopicQ.isEmpty() && !MQTTPublishMessageQ.isEmpty())
			{
				try {
					start=System.nanoTime();
					if (publish(MQTTPublishTopicQ.peek(),1, MQTTPublishMessageQ.peek()))
					{
						// Send successful remove the messge from Q
						//logger.info(MQTTPublishTopicQ.peek());
		/*				try {
							Thread.sleep(1);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}*/
						synchronized (publishQLock) {
							MQTTPublishTopicQ.poll();
							MQTTPublishMessageQ.poll();
						}
				    	//if(clientWS!=null && clientWS.isOpen())
				    	//{
				    	//	clientWS.send(topicName);  // Send data to websockets
				    	//	clientWS.send(payl
					}
					//else
					//	break;  // break if publish failed and retry again after sometime to avoid increasing CPU usage
					
					if(logger.isTraceEnabled())
						logger.trace("Publish time(us): " + (System.nanoTime()-start)/1000);
					
				} catch (MqttException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
				//Try giving time for context switching for other threads
				try {
					Thread.sleep(5);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//logger.info("ClientAdapter Run");
			}
			else
			{
			
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
}
