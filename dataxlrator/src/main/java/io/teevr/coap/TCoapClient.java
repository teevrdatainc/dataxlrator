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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.eclipse.californium.elements.Connector;
import org.eclipse.californium.elements.tcp.TcpClientConnector;
import org.eclipse.californium.elements.tcp.TlsClientConnector;
import org.eclipse.californium.scandium.DTLSConnector;
import org.eclipse.californium.scandium.config.DtlsConnectorConfig;
import org.eclipse.californium.scandium.dtls.pskstore.StaticPskStore;

import io.teevr.config.Configuration;


// Coap Client is required to send Data from edge to Cloud
public class TCoapClient implements Runnable{
	
	Configuration config;
	CoapClient client;
	String uri="coap";
	NetworkConfig net = null;
	boolean bConnected=false;
	String host="";
	int nPort=5683;
	boolean bUseTCP=true;
	Object processPutLock=null;
    ConcurrentLinkedQueue<byte[]> COAPPutMessageQ = new ConcurrentLinkedQueue<byte[]>();
    String resourceName="";
    String uriWithQuery="";
    long dbgMsgcounter=0;
   
    /**
     * TLS Implementation variables
     */
    boolean enableTLS = false;
    private String trustStorePassword = "";
    private String trustStoreName = "";
	private String keyStorePassword = "";
	private String keyStoreName = "";
	public TCoapClient(Configuration config, boolean bUseCloudConfig)
	{
		this.config=config;
		if(bUseCloudConfig) // If it is edge end point use cloud 
		{
			bUseTCP=config.getCOAP().getCloudCOAP().getUseTCP();
			enableTLS = config.getCOAP().getCloudCOAP().getEnableTLS();
			if(bUseTCP)
				uri = enableTLS?"coaps+tcp":"coap+tcp";
			else
				uri = enableTLS?"coaps":"coap";
			
			if(enableTLS){
				trustStorePassword = config.getCOAP().getCloudCOAP().getTrustStorePassword();
				trustStoreName = config.getCOAP().getCloudCOAP().getTrustStore();
				keyStorePassword = config.getCOAP().getCloudCOAP().getKeyStorePassword();
				keyStoreName = config.getCOAP().getCloudCOAP().getKeyStore();
//				tlsConnectorCert = config.getCOAP().getCloudCOAP().getTLSConnectorCert();
			}
			uri+="://"+config.getCOAP().getCloudCOAP().getServerAddress()+":"+config.getCOAP().getCloudCOAP().getPort();
			nPort=config.getCOAP().getCloudCOAP().getPort();
			host=config.getCOAP().getCloudCOAP().getServerAddress();
		}
		else // else use edge configuration
		{
			bUseTCP=config.getCOAP().getEdgeCOAP().getUseTCP();
			enableTLS = config.getCOAP().getEdgeCOAP().getEnableTLS();
			if(bUseTCP)
				uri = enableTLS?"coaps+tcp":"coap+tcp";
			else
				uri = enableTLS?"coaps":"coap";

			if(enableTLS){
				trustStorePassword = config.getCOAP().getEdgeCOAP().getTrustStorePassword();
				trustStoreName = config.getCOAP().getEdgeCOAP().getTrustStore();
				keyStorePassword = config.getCOAP().getEdgeCOAP().getKeyStorePassword();
				keyStoreName = config.getCOAP().getEdgeCOAP().getKeyStore();
//				tlsConnectorCert = config.getCOAP().getEdgeCOAP().getTLSConnectorCert();
			}
			uri+="://"+config.getCOAP().getEdgeCOAP().getServerAddress()+":"+config.getCOAP().getEdgeCOAP().getPort();
			nPort=config.getCOAP().getEdgeCOAP().getPort();
			host=config.getCOAP().getEdgeCOAP().getServerAddress();
			
		}
		// Set this dynamically later on
		/*net = NetworkConfig.createStandardWithoutFile()
				.setLong(NetworkConfig.Keys.MAX_MESSAGE_SIZE,64 * 1024)
				.setInt(NetworkConfig.Keys.PROTOCOLxzcxcdcx csefdfefdfdsddsd_STAGE_THREAD_COUNT, 2)
				.setLong(NetworkConfig.Keys.EXCHANGE_LIFETIME, 5*60*1000)
				.setInt(NetworkConfig.Keys.PREFERRED_BLOCK_SIZE, 1024)
				.setLong(NetworkConfig.Keys.BLOCKWISE_STATUS_LIFETIME, 10*60*1000);*/
		}

	public TCoapClient(int nPort)
	{
		this.config=null;
		uri="coap+tcp://127.0.0.1:"+nPort;

		this.nPort=nPort;
		host="127.0.0.1";
		// Set this dynamically later on
/*		net = NetworkConfig.createStandardWithoutFile()
				.setLong(NetworkConfig.Keys.MAX_MESSAGE_SIZE,64 * 1024)
				.setInt(NetworkConfig.Keys.PROTOCOL_STAGE_THREAD_COUNT, 2)
				.setLong(NetworkConfig.Keys.EXCHANGE_LIFETIME, 10000)
				.setInt(NetworkConfig.Keys.PREFERRED_BLOCK_SIZE, 1024)
				.setLong(NetworkConfig.Keys.BLOCKWISE_STATUS_LIFETIME, 10*60*1000);*/
	}	
	public boolean isConnected()
	{
		return bConnected;
	}
	public boolean connect()
	{
		Connector clientConnector=null;
		//Enable Californium debug loggging
		/*
		CaliforniumLogger.initialize();
		CaliforniumLogger.setLevel(java.util.logging.Level.ALL);
		*/
		client=new CoapClient(uri);
		

//		client=new CoapClient();
		if(bUseTCP)
		{
			net = NetworkConfig.createStandardWithoutFile()
					.setLong(NetworkConfig.Keys.MAX_MESSAGE_SIZE,32 * 1024)
					.setInt(NetworkConfig.Keys.PROTOCOL_STAGE_THREAD_COUNT, 2)
					.setLong(NetworkConfig.Keys.EXCHANGE_LIFETIME, 5*60*1000)
					.setInt(NetworkConfig.Keys.PREFERRED_BLOCK_SIZE, 1024)
					.setLong(NetworkConfig.Keys.BLOCKWISE_STATUS_LIFETIME, 10*60*1000);
			
			if(enableTLS)
			{
				clientConnector = new TlsClientConnector(getTLSConnector(), 1, 100, 100);
				client.setEndpoint(new CoapEndpoint(clientConnector,net));
			}
			else
			{
				clientConnector = new TcpClientConnector(5, 5000, 0);  // Idle timeot is 5 mins and connection timeout is 5 secs
				client.setEndpoint(new CoapEndpoint(clientConnector,net));
			}
		}
		else
		{
			net = NetworkConfig.createStandardWithoutFile()
					.setLong(NetworkConfig.Keys.MAX_MESSAGE_SIZE,1024)
					.setInt(NetworkConfig.Keys.PROTOCOL_STAGE_THREAD_COUNT, 2)
					.setLong(NetworkConfig.Keys.EXCHANGE_LIFETIME, 5*60*1000)
					.setInt(NetworkConfig.Keys.PREFERRED_BLOCK_SIZE, 512)
					.setLong(NetworkConfig.Keys.BLOCKWISE_STATUS_LIFETIME, 10*60*1000);
		if(enableTLS)
		{
			client.setEndpoint(new CoapEndpoint(getDTLSConnector(), net));
		}
		else
		{
			client.setEndpoint(new CoapEndpoint(net)); // default to UDP
		}
		}
		client.useCONs(); // Use Confirmable messages
		//client.useEarlyNegotiation(1024);
		client.setTimeout(5*60*1000);  // Wait for 5 minute in worst case if there is a block transfer. This is to support large block messages
	//	if(!bUseTCP&& client.ping(5000))  // Connect successful
		{
			System.out.println("Using COAP Server URI: " + uri);
			bConnected=true;
			return true;
		}
		/*else
		{
			System.out.println("Connection to COAP Server URI: " + uri + " failed!!");
			bConnected=false;
			return false;
		}*/
	}
	
	private boolean send(byte[] payload)
	{
		if(!bConnected)  // try a reconnect every time we are sending data
			connect();
		
		if (bConnected)
		{
			
			
//			System.out.println("Query: " +uriWithQuery + "Payload Size: " + payload.length );
			//System.out.println("Message with ID : " + (dbgMsgcounter+1) + " being sent. Message Q size: " + COAPPutMessageQ.size());
			CoapResponse res=client.put(payload, MediaTypeRegistry.UNDEFINED);
			if(res!=null)
			{
				if(!res.isSuccess())
					System.out.println("COAP Message send failed");
				else
				{
				//	dbgMsgcounter++;
				//	System.out.println("Message with ID : " + dbgMsgcounter + " sent successfully. Message Q size: " + COAPPutMessageQ.size());
					return true;
				}
			}
			else
			{
				System.out.println("COAP Message send failed. Response is NULL.");
			}
			
		}
		return false;

	}
	
	/* Convenience function to check if all the message has been sent before closing the connection in datagenerator*/
	public boolean isQueueEmpty()
	{
		return COAPPutMessageQ.isEmpty();
	}
	public boolean send(String resource, byte[] payload)
	{
		
		if(processPutLock==null)  // Create lock for this datasource
			processPutLock=new String(resource);

		//synchronized(processPutLock)
		{
			COAPPutMessageQ.add(payload);
		}

		//System.out.println("Message addded to Q. Size:  " + COAPPutMessageQ.size());
		if(resourceName.isEmpty())
		{
			resourceName=resource;
			uriWithQuery= uri+"/teevr?"+resourceName;
			client.setURI(uriWithQuery);
			new Thread(this,resourceName+"-COAP-PUT").start();
			System.out.println("COAP PUT Thread started for : " + resourceName);
		}
		else
			if(!resourceName.equals(resource))
			{
				// This should never happen, this is just as precuationary check
				System.out.println(" Resource name mismatch in CoapClient. Expected : " + resourceName + " Received: " + resource);
			}
		

		return true;
	}

	@Override
	public void run() {
		while(true)
		{
			if(!COAPPutMessageQ.isEmpty())
			{
				if(send(COAPPutMessageQ.peek()))
				{
					//synchronized (processPutLock) 
					{
						COAPPutMessageQ.poll();
					}
				}

				//Try giving time for context switching for other threads
				try {
					Thread.sleep(5);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
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
	private DTLSConnector getDTLSConnector() {
		DTLSConnector ret=null;
		try {
			// load key store
			KeyStore keyStore = KeyStore.getInstance("JKS");
			InputStream input = getInputStream(keyStoreName);
			keyStore.load(input, keyStorePassword.toCharArray());
			input.close();

			// load trust store
			KeyStore trustStore = KeyStore.getInstance("JKS");
			InputStream inTrust = getInputStream(trustStoreName);
			trustStore.load(inTrust, trustStorePassword.toCharArray());
			inTrust.close();
						
			// You can load multiple certificates if needed
			Certificate[] trustedCertificates = new Certificate[1];
			trustedCertificates[0] = trustStore.getCertificate("root");
			DtlsConnectorConfig.Builder builder = new DtlsConnectorConfig.Builder(new InetSocketAddress(0));
			builder.setIdentity((PrivateKey)keyStore.getKey("client", keyStorePassword.toCharArray()),
					keyStore.getCertificateChain("client"), true);
			builder.setTrustStore(trustedCertificates);
			ret = new DTLSConnector(builder.build());
		} catch (Exception e) {
			System.err.println("Could not load the keystore");
			e.printStackTrace();
		}
		return ret;
	}

	private SSLContext getTLSConnector() {
		SSLContext clientContext = null;
		try {
			String algorithm = Security.getProperty("ssl.KeyManagerFactory.algorithm");
			KeyStore keyStore = KeyStore.getInstance("JKS");
			InputStream input = getInputStream(keyStoreName);
			keyStore.load(input, keyStorePassword.toCharArray());
            input.close();
			// Set up key manager factory to use our key store
			KeyManagerFactory kmf = KeyManagerFactory.getInstance(algorithm);
			kmf.init(keyStore, keyStorePassword.toCharArray());

			// load trust store
			KeyStore trustStore = KeyStore.getInstance("JKS");
			InputStream inTrust = getInputStream(trustStoreName);
			trustStore.load(inTrust, trustStorePassword.toCharArray());
			inTrust.close();
			// Set up key manager factory to use our key store
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(algorithm);
			tmf.init(trustStore);

			
			clientContext = SSLContext.getInstance("TLS");
			clientContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
		} catch (Exception e) {
			System.err.println("Could not load the TLS");
			e.printStackTrace();
		}
		return clientContext;
	}
	/**
	 * @return
	 */
	private InputStream getInputStream(String fileName) {
		InputStream input = getClass().getResourceAsStream("/certs/" + fileName);
		if (input == null) {
			Path dsResource = Paths.get("./certs/" + fileName);
			try {
				input = Files.newInputStream(dsResource);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return input;
	}
}
