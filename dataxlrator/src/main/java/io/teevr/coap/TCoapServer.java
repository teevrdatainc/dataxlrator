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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.EndpointManager;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.eclipse.californium.elements.Connector;
import org.eclipse.californium.elements.tcp.TcpServerConnector;
import org.eclipse.californium.elements.tcp.TlsServerConnector;
import org.eclipse.californium.scandium.DTLSConnector;
import org.eclipse.californium.scandium.config.DtlsConnectorConfig;
import org.eclipse.californium.scandium.dtls.pskstore.InMemoryPskStore;

import io.teevr.config.Configuration;
import io.teevr.mqtt.MqttClientAdapter;


// Coap Server is required to receive data from external data source to edge and/or edge to cloud
public class TCoapServer extends CoapServer {
	
	int nPort=5683;
	MqttClientAdapter mqttAdapter=null;
	boolean bUseTCP=true;
	NetworkConfig tcpNet= null; 
	NetworkConfig udpNet= null;
	 /**
     * TLS Implementation variables
     */
    boolean enableTLS = false;
    private String trustStorePassword = "";
    private String trustStoreName = "";
	private String keyStorePassword = "";
	private String keyStoreName = "";

	public TCoapServer(String name, MqttClientAdapter adapter, Configuration config, boolean bIsRemoteEndpoint)
	{

		this.mqttAdapter=adapter;	
		if(bIsRemoteEndpoint) // Use Cloud Config
		{
			nPort=config.getCOAP().getCloudCOAP().getPort();
			bUseTCP=config.getCOAP().getCloudCOAP().getUseTCP();
			enableTLS=config.getCOAP().getCloudCOAP().getEnableTLS();
			trustStoreName=config.getCOAP().getCloudCOAP().getTrustStore();
			trustStorePassword=config.getCOAP().getCloudCOAP().getTrustStorePassword();
			keyStoreName=config.getCOAP().getCloudCOAP().getKeyStore();
			keyStorePassword=config.getCOAP().getCloudCOAP().getKeyStorePassword();
		}
		else
		{
			nPort=config.getCOAP().getEdgeCOAP().getPort();
			bUseTCP=config.getCOAP().getEdgeCOAP().getUseTCP();
			enableTLS=config.getCOAP().getEdgeCOAP().getEnableTLS();
			trustStoreName=config.getCOAP().getEdgeCOAP().getTrustStore();
			trustStorePassword=config.getCOAP().getEdgeCOAP().getTrustStorePassword();
			keyStoreName=config.getCOAP().getEdgeCOAP().getKeyStore();
			keyStorePassword=config.getCOAP().getEdgeCOAP().getKeyStorePassword();

		}
		// Enable Californium debug logging
		/*CaliforniumLogger.initialize();
		CaliforniumLogger.setLevel(java.util.logging.Level.ALL);*/
		tcpNet = NetworkConfig.createStandardWithoutFile()
				.setLong(NetworkConfig.Keys.MAX_MESSAGE_SIZE,32 * 1024)
				.setInt(NetworkConfig.Keys.PROTOCOL_STAGE_THREAD_COUNT, 10)
				.setLong(NetworkConfig.Keys.EXCHANGE_LIFETIME, 5*60*1000)
				.setInt(NetworkConfig.Keys.PREFERRED_BLOCK_SIZE, 1024)
				.setLong(NetworkConfig.Keys.BLOCKWISE_STATUS_LIFETIME, 10*60*1000);
		// For udp we need smaller message sizes
		udpNet = NetworkConfig.createStandardWithoutFile()
				.setLong(NetworkConfig.Keys.MAX_MESSAGE_SIZE,1024)
				.setInt(NetworkConfig.Keys.PROTOCOL_STAGE_THREAD_COUNT, 10)
				.setLong(NetworkConfig.Keys.EXCHANGE_LIFETIME, 5*60*1000)
				.setInt(NetworkConfig.Keys.PREFERRED_BLOCK_SIZE, 512)
				.setLong(NetworkConfig.Keys.BLOCKWISE_STATUS_LIFETIME, 10*60*1000);

		
		System.out.println("Adding resource");
		add(new TCoapServerResource(name,adapter));
		addEndpoints();
		System.out.println("Ready to start server");

	}

    /**
     * Add individual endpoints listening on default CoAP port on all IPv4 addresses of all network interfaces.
     */
    private void addEndpoints() {

  
		//Connector serverConnector = new TcpServerConnector(new InetSocketAddress(nPort), 100, 1);
		//addEndpoint(new CoapEndpoint(serverConnector,net));

    	for (InetAddress addr : EndpointManager.getEndpointManager().getNetworkInterfaces()) {
    		// only binds to IPv4 addresses and localhost
			//if (addr instanceof Inet4Address || addr.isLoopbackAddress()) {

				/* Add TCP and UDP endpoints
				 * Server will listen simultaneously on both tcp and udp ip addresses on same port
				 * */ 
    		//////
			if(enableTLS)
			{
				//tlsConnector=""; // Check what is required for tls cert
				Connector serverConnector = new TlsServerConnector(getTLSConnector(), new InetSocketAddress(addr, nPort), 0, 10);
				addEndpoint(new CoapEndpoint(serverConnector,tcpNet));
				
				//Add DTLS Server
				addEndpoint(new CoapEndpoint(getDTLSConnector(new InetSocketAddress(addr, nPort)),udpNet));

			}
			else
			{
				Connector serverConnector = new TcpServerConnector(new InetSocketAddress(addr, nPort), 0, 10);
				addEndpoint(new CoapEndpoint(serverConnector,tcpNet));
				InetSocketAddress bindToAddress = new InetSocketAddress(addr, nPort);
				addEndpoint(new CoapEndpoint(bindToAddress,udpNet));

			}
    			
 
			}
	    }
	
	public DTLSConnector getDTLSConnector(InetSocketAddress addr) {
		DTLSConnector ret=null;
		try {
			// load the key store
			KeyStore keyStore = KeyStore.getInstance("JKS");
			InputStream input = getInputStream(keyStoreName);
			keyStore.load(input, keyStorePassword.toCharArray());
			input.close();

			// load the trust store
			KeyStore trustStore = KeyStore.getInstance("JKS");
			InputStream inTrust = getInputStream(trustStoreName);
			trustStore.load(inTrust, trustStorePassword.toCharArray());
			inTrust.close();

			// You can load multiple certificates if needed
			Certificate[] trustedCertificates = new Certificate[1];
			trustedCertificates[0] = trustStore.getCertificate("root");

			DtlsConnectorConfig.Builder builder = new DtlsConnectorConfig.Builder(addr);
			builder.setIdentity((PrivateKey) keyStore.getKey("server", keyStorePassword.toCharArray()),
					keyStore.getCertificateChain("server"), true);
			builder.setTrustStore(trustedCertificates);
			ret = new DTLSConnector(builder.build());
		} catch (Exception e) {
			System.err.println("Could not load the keystore");
			e.printStackTrace();
		}
		return ret;
	}
	
	private SSLContext getTLSConnector() {
		SSLContext serverContext = null;
		try {

			String algorithm = Security.getProperty("ssl.KeyManagerFactory.algorithm");
			KeyStore keyStore = KeyStore.getInstance("JKS");
			InputStream input = getInputStream(keyStoreName);
			keyStore.load(input, keyStorePassword.toCharArray());
			input.close();

			// Set up key manager factory to use our key store
			KeyManagerFactory kmf = KeyManagerFactory.getInstance(algorithm);
			kmf.init(keyStore, keyStorePassword.toCharArray());

			// Initialize the SSLContext to work with our key managers.
			serverContext = SSLContext.getInstance("TLS");
			serverContext.init(kmf.getKeyManagers(), null, null);

		} catch (Exception e) {
			System.err.println("Could not load the TLS");
			e.printStackTrace();
		}
		return serverContext;
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
