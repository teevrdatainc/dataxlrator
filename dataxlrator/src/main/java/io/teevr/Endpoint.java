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


package io.teevr;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.teevr.coap.TCoapServer;
import io.teevr.config.Configuration;
import io.teevr.mgmt.DMClient;
import io.teevr.mqtt.MqttClientAdapter;
import io.teevr.mqtt.MqttClientAdapter.MQTTConfigtoUse;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import org.apache.commons.math3.analysis.function.HarmonicOscillator;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.optimization.*;
import org.apache.commons.math3.fitting.HarmonicCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

public class Endpoint  extends ApplicationFrame{
	
	static int MAXN=64;
	// ---------------------------------------------------------------
	// Display Chart
	// ---------------------------------------------------------------
	 public Endpoint(int [] yArr2, int depth) {

	    	
	    super("Data plot");
	    final XYSeries series = new XYSeries("Data");
	    for (int i=0; i<depth; i++) {
	    	series.add(i, yArr2[i]);
	    }
	    final XYSeriesCollection data = new XYSeriesCollection(series);
	    final JFreeChart chart = ChartFactory.createXYLineChart(
	        "XY Series Demo",
	        "X", 
	        "Y", 
	        data,
	        PlotOrientation.VERTICAL,
	        true,
	        true,
	        false
	    );

	    final ChartPanel chartPanel = new ChartPanel(chart);
	    chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
	    setContentPane(chartPanel);
	}
	
       	 public Endpoint() {

	    	
	    super("Data plot");
	}
	private MqttClientAdapter edgeMqtt; /* Edge*/
	private MqttClientAdapter cloudMqtt; /*Cloud*/
	private DMClient mgmtClient; /*Remote Mgmt*/
	/*We have multiple modes of working between edge and cloud and edge and sensors
	 * 
	 * 0 - means MQTT
	 * 1- means COAP
	 * 
	 * 
	 * */
	public Logger logger;
 	private static final int MQTT =0;
	private static final int COAP=1;
	private int SensorsToEdge = MQTT;  /* We support only MQTT for demo and can be extended later*/	
	private int EdgeToCloud = MQTT;   
	
	// Use Configuration object
	Configuration config=null;

	/*Enable this flag for buidling jar to be run on Remote server/EC2 machine
	 * When this flag is enabled, the program will use  MQTT and influxdb on EC2
	 * When disabled, this program will work as a gateway and will collect sensor info from local MQTT and 
	 * publish to EC2 
	 * */
	boolean bIsRemoteEndpoint=true;

	long nTSOffset=0;  /* Offset for synchronizing server and edge timestamps*/
    long nReqTS=0;

	private static boolean bExit=false;
	

  
	public static void main(String[] args) {
		String configFileName=new String("config.json");
		 boolean bRemote=true;
	 /*provide argument while executing as  remote=true  for executing on remote server */

		 for(int i=0; i<args.length;i++)
		 {
			 String tmp[]= args[i].split("=");
			 if(tmp.length==2)
			 {
				 if("remote".equalsIgnoreCase(tmp[0]))
				 {
					 if("true".equalsIgnoreCase(tmp[1]))
							 bRemote=true;	 
					 else
						 bRemote=false;
				 }
				 else
					 if("configfile".equalsIgnoreCase(tmp[0]))
					 {
						 configFileName=tmp[1];
					 }
				 }
		 }
		 
		 
		 /* AWS SDK Usage*/
/*		 
		 // Get the Product Code
		 String instanceId = EC2MetadataUtils.getInstanceId();
		 String prodcutId="dt69cvfw78400hwks6914ls4q";
		 AmazonEC2Client EC2Client= new AmazonEC2Client();
		 ConfirmProductInstanceRequest prdReq=new ConfirmProductInstanceRequest(prodcutId,instanceId);
		// prdReq.setProductCode();
		 ConfirmProductInstanceResult res=EC2Client.confirmProductInstance(prdReq);
		 if(res!=null)
		 {
			 System.out.println(" Oner Id of : " + res.getOwnerId());
		 }
		 else
			 System.out.println(" No owner attached to prodcut code");
		 
		 DescribeInstanceAttributeRequest attrReq=new DescribeInstanceAttributeRequest(instanceId,"productCodes");
		 DescribeInstanceAttributeResult resAttr=EC2Client.describeInstanceAttribute(attrReq);
		 if(resAttr!=null)
		 {
			 List<ProductCode> prdCodes= resAttr.getInstanceAttribute().getProductCodes();
			 System.out.println(" Product Codes Size: " + prdCodes.size() + " value: " + prdCodes.get(0).getProductCodeId());
		 }
		 else
		 {
			 System.out.println(" Product Code Request Failed");
		 }
		 
		 //Call the metering service
		 AWSMarketplaceMeteringClient meteringClient= new AWSMarketplaceMeteringClient();
		 MeterUsageRequest req=new MeterUsageRequest();
		 req.setProductCode("dt69cvfw78400hwks6914ls4q");
		 req.setTimestamp(new Date());
		 req.setUsageDimension("AggReadingsM");
		 req.setUsageQuantity(1);
		 req.setDryRun(true);
		 MeterUsageResult meteringRes=meteringClient.meterUsage(req);
		 if(meteringRes!=null)
		     System.out.println("Metering Record ID: " + meteringRes.getMeteringRecordId());
	*/	 
		 // Text code for SQLLite usage. No installation required and just the jar file is good enough
		   /*
		    *  Connection c = null;
		    Statement stmt = null;
		    try {
		      Class.forName("org.sqlite.JDBC");
		      c = DriverManager.getConnection("jdbc:sqlite:test.db");
		      stmt = c.createStatement();
		      String sql = "CREATE TABLE COMPANY " +
		                   "(ID INT PRIMARY KEY     NOT NULL," +
		                   " NAME           TEXT    NOT NULL, " + 
		                   " AGE            INT     NOT NULL, " + 
		                   " ADDRESS        CHAR(50), " + 
		                   " SALARY         REAL)"; 
		      stmt.executeUpdate(sql);
		      stmt.close();
		      c.close();
		    } catch ( Exception e ) {
		      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
		      System.exit(0);
		    }
		    System.out.println("Opened database successfully");
         */
		 
		 Endpoint endpt=new Endpoint();
		 // We'll override the parameters if there is run.sh file in classpath of the jar. This is to enable execution of a jar package easily without passing any 
		 // parameters either on edge or cloud.
		 String cmd=endpt.getFileString("onejar.sh");
		 if(cmd!=null && !cmd.isEmpty())  // Processing run.sh from within jar
		 {
	         // We are looking for two parameters here: Configfile and remote  java -jar *.jar configfile=edge.json remote=false
	         String cmdArr[]=cmd.trim().split("\\s+");
	         // Process config
	         String configcmdArr[]=cmdArr[3].split("=");
	         if("configfile".equalsIgnoreCase(configcmdArr[0].trim()))
	         {
	        	 configFileName=configcmdArr[1].trim();
	         }
	         
	         String remotecmdArr[]=cmdArr[4].split("=");
	         
	         if("remote".equalsIgnoreCase(remotecmdArr[0].trim()))
	         {
			     if("true".equalsIgnoreCase(remotecmdArr[1].trim()))
							 bRemote=true;	 
					 else
						 if("false".equalsIgnoreCase((remotecmdArr[1]).trim()))
							 bRemote=false;	 
	         }
		 }
		 
		 Configuration config = endpt.getConfig(configFileName);
		 endpt.init(config,bRemote);
		    
		 while(!bExit)
		 {
			 
			 try {
				Thread.sleep(1000); /* publish sync packet every 10 seconds*/
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		 }
		//coapserver.stop();
		System.exit(0);
	}
	
	
	public void init (Configuration config, boolean bRemoteEndpoint) {

		/* init the connector*/
		 Properties props = new Properties();
		 try {
			props.load(getClass().getResourceAsStream("/log4j.properties"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		 PropertyConfigurator.configure(props);
		 
	
		 this.config=config;
		 
		
		 this.bIsRemoteEndpoint=bRemoteEndpoint;
		 if(bRemoteEndpoint)
			 logger = Logger.getLogger("DataXlrator-Cloud");
		 else
			 logger = Logger.getLogger("DataXlrator-Edge");
		 setLogLevel();
		 logger.info("Version: " + getClass().getPackage().getImplementationVersion());
		 if(bIsRemoteEndpoint)
		 {
			 cloudMqtt= new MqttClientAdapter(this.config, MQTTConfigtoUse.CLOUD,bIsRemoteEndpoint,true);
			 cloudMqtt.setasExternalClient(false);

		 }
		 else
		 {
			 edgeMqtt= new MqttClientAdapter(this.config,MQTTConfigtoUse.EDGE,bIsRemoteEndpoint,true);
			 edgeMqtt.setasExternalClient(false);
		 }
		 /*if(!bIsRemoteEndpoint)
			 mgmtClient= new DMClient(this.config,getClass().getPackage().getImplementationVersion(),bIsRemoteEndpoint);
		 else*/
			 mgmtClient=null;
		 
		 init(); 
	}	
	
	
	public  Configuration getConfig(String configFileName) {
		JsonElement inputJsonElement=null;
		JsonElement defaultConfigElement;
		JsonElement MergedConfigElement;

		//logger.info(configFileName);
		String inputJson=getFileString(configFileName);
		if(inputJson.isEmpty())
		{
			// use the default config. This is primarily used by datagenerator if there is no matching json config file to the datasource name
			inputJson=getFileString("ds.json");
		}
		//System.out.println(inputJson);
	//	logger.info(inputJson);
		String defaultConfigJson=getFileString("defaultconfig.json");
		JsonParser parser= new JsonParser();
		inputJsonElement=parser.parse(inputJson.trim());
		defaultConfigElement=parser.parse(defaultConfigJson.trim());
		MergedConfigElement=processObject(inputJsonElement,defaultConfigElement);
		Gson gson= new Gson();
		//System.out.println(gson.toJson(MergedConfigElement));
	    return gson.fromJson(MergedConfigElement, Configuration.class);
		
	}
	  public   JsonElement processObject(JsonElement inputconfig, JsonElement defaultconfig)
	   {
		  
		  if(inputconfig!=null)
		  {
			  Set<Entry<String, JsonElement>> inputconfigiterator = ((JsonObject) inputconfig).entrySet();
			//  Set<Entry<String, JsonElement>> defaultconfigiterator = ((JsonObject) defaultconfig).entrySet();
		      if (inputconfigiterator != null) 
		      {
		            for (Entry<String, JsonElement> en : inputconfigiterator) 
		            {
		            	if(en.getValue().isJsonObject())
		            	{
		            		// iterate to get the corresponding  object
		            		JsonElement ret= getObject(en.getKey(),defaultconfig);
		            		ret=processObject(en.getValue(),ret);
		            	}
		            	else
		            	{
		            		defaultconfig=processElement(en.getKey(),en.getValue(),defaultconfig);
		            	}
		            }
		      }
		  }
		   return defaultconfig;
	   }
	  public  JsonElement getObject(String key,JsonElement jsonObj)
	  {
		  JsonElement ret=null;
		  Set<Entry<String, JsonElement>> iterator = ((JsonObject) jsonObj).entrySet();
		  if (iterator != null) 
	      {
	            for (Entry<String, JsonElement> en : iterator) 
	            {
	            	if(en.getKey().equals(key))
	            	{
	            		//we have got the element
	            	//	System.out.println(en.getValue());
	            		return en.getValue();
	            	}
	            }
	      }
		  
		  return ret;
	  }
	  
	  public   JsonElement processElement(String key,JsonElement value, JsonElement defaultConfig)
	  {
		  JsonElement ret=null;
		  Set<Entry<String, JsonElement>> defaultConfigIterator = ((JsonObject) defaultConfig).entrySet();
	      if (defaultConfigIterator != null) 
	      {
	            for (Entry<String, JsonElement> en : defaultConfigIterator) 
	            {
	            	if(en.getValue().isJsonObject())
	            	{
	            		System.out.println("Not expected....");
	            	}
	            	else
	            	{
	            		if(en.getKey().equals(key))
	            		{
	            		  en.setValue(value);
	            		  return defaultConfig;
	            		}
	            	}
	            }
	      }
		
		  return ret;
	  }

	private  String getFileString(String configFileName) {
		// TODO Auto-generated method stub
		 BufferedReader reader=null;
		 String line;
		 StringBuilder configString= new StringBuilder();
		 InputStream input= getClass().getResourceAsStream("/"+configFileName);
		 if(input==null)
		 {
			Path dsResource = Paths.get("./"+configFileName);

			try {
				reader = Files.newBufferedReader(dsResource, StandardCharsets.UTF_8);
			} catch (IOException e) {
				// If we are here means, we did not get the config file Return empty string so that defauly config "ds.json" can be used.
				return "";
			}
		 }
		 else
		 {
			 try {
				reader = new BufferedReader(new InputStreamReader(input, "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		 }


		 try {
			line=reader.readLine();
			 while(line!=null)
			 {
				 configString.append(line);
				 line=reader.readLine();
			 }

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 

		return configString.toString();
	}
	public void setLogLevel()
	{
		//logger
		Level loglevel= Level.INFO;
		if(this.config.getAdvanced().getLogLevel() !=null)
		{
			switch(this.config.getAdvanced().getLogLevel())
			{
			case DEBUG:
				loglevel= Level.DEBUG;
				break;
			case ERROR:
				loglevel= Level.ERROR;
				break;
			case FATAL:
				loglevel= Level.FATAL;
				break;
			case INFO:
				loglevel= Level.INFO;
				break;
			case TRACE:
				loglevel= Level.TRACE;
				break;
			case WARN:
				loglevel= Level.WARN;
				break;
			default:
				loglevel= Level.INFO;
				break;
			}
		}
		logger.setLevel(loglevel);
	}
 	
/**
 * Create a influxDB connection before all tests start.
 * 
 * @throws InterruptedException
 * @throws IOException
 */
	
public void init() {

	
	if(mgmtClient!=null)
		mgmtClient.init(); // Initialize mgmtClient
	
	/* Initialize MQTT clients
	 * 
	 */
	//perfAndEventsPublisher.init();
	if(bIsRemoteEndpoint) /* Initialize Influx db on cloud server*/
	{
		//dbAdapter.init();
		
		if (EdgeToCloud == MQTT)  /* Edge to cloud is MQTT, hence we need access to cloud brokers*/
		{
			cloudMqtt.init();
			new TCoapServer("teevr", cloudMqtt, config,bIsRemoteEndpoint).start();
			//new Server(cloudMqtt,config.getMQTT().getCloudMQTT().getSnapiPort()).start();

		}
	}
	else  /*  edge endpoint*/
	{
		if (EdgeToCloud == MQTT)  /* Edge to cloud is MQTT, hence we need access to local and remote brokers*/
		{
			edgeMqtt.init();
			new TCoapServer("teevr", edgeMqtt, config,bIsRemoteEndpoint).start();
			//new Server(edgeMqtt,config.getMQTT().getEdgeMQTT().getSnapiPort()).start();;
		}
		else
			edgeMqtt.init(); /* Edge to Cloud is COAP, hence only local broker access is needed*/
		
	}
	
}

//---------------------------------------------------------------
	// Utility to predict next value based on previous N samples
	//    Using linear regression
	// ---------------------------------------------------------------
	public static int predictNextValueLinear(int yArr[], int N) {
		
     // Try Math function        
     SimpleRegression regression = new SimpleRegression();
     int t = 0;
     for (int n=(MAXN-N); n<MAXN; n++) {
         regression.addData(t, yArr[n]);
         t++;
     }
     
     // Estimate regression model based on data
     double beta0 = regression.getIntercept();
     double beta1 = regression.getSlope();
     
     double preY  = beta1*N + beta0;
     // System.out.println("y   = " + beta1 + " * x + " + beta0);

     return (int) preY;
	}

}
