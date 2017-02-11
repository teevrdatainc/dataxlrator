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

//import io.teevr.cep.AcceleroGyro;
//import io.teevr.cep.CEP
import io.teevr.aws.UsageMeter;
import io.teevr.cep.AcceleroGyro;
import io.teevr.cep.CEP;
import io.teevr.config.Advanced;
import io.teevr.config.Advanced.Clouddb;
import io.teevr.config.Advanced.Edge2Cloud;
import io.teevr.config.Advanced.Input;
import io.teevr.config.Advanced.Output;
import io.teevr.config.Advanced.PerfMonitorTimeUnit;
import io.teevr.config.Configuration;
import io.teevr.config.DataModel;
import io.teevr.config.Mlad;
import io.teevr.config.Model;
import io.teevr.config.Model.DataType;
import io.teevr.tsdb.DBAdapter;
import io.teevr.dataxlrator.Compressor;
import io.teevr.dataxlrator.DataFormat;
import io.teevr.dataxlrator.Decompressor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
/*import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;
import org.apache.commons.math3.ml.clustering.DoublePoint;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;*/
import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonReader;
import com.google.protobuf.ByteString;
import com.google.protobuf.Message;




public class MQTTMessageHandler implements Runnable{
	
	// Ensure to sync this with sensor data origin
	int PERF_MONITOR_FREQUENCY = 1000;  //  every PERF_MONITOR_FREQUENCY miliseconds  
 	//private DatabaseAdapter dbAdapter;
	MqttClientAdapter remotebroker;
	MqttClientAdapter perfMonitorbroker;
	//private String dbName;
	//private String perfDbName;
	//private String eventsDbName;
	boolean bIsDatamodelProcessed=false;
	int BOOLEAN_PRECISION=-998; // set the precision value to indicate it is a boolean field
	int STRING_PRECISION=-999; // set the precision to indicate String Value
	Configuration config;
	boolean bIsRemoteEndpoint=false;
	int coapPort=5683;
	//String inputcsvMessage="";
	
	int lowBitSize=10;   // lower end of bit size to store data for out of range Huffman
	int highBitSize=26;  // upper end of bit size to store data for out of range Huffman
	int precisionCap=5;   // maximum number of decimal points supported
	boolean bUsePfxEncoding=true;
	boolean bEnableBatch=false;
	int batchsize=1000;
	int bitSzStep= (highBitSize-lowBitSize)/3;
	int numDataPts = 0;  					  // Number of columns in the file
	//long [] dataModelPrec;
	int [] dataModelScale;
	UsageMeter usageMtr=null;
	
    //boolean bEnableDataCollector=false;  // When true collect the mobile sensor data to a file to be used by simulator
    int numRecordsWritten=0;
    BufferedWriter bw;
    int sampleSize=1000;
    boolean bEnableJsonData=false;  //if enabled data sent to cloud will be in JSON format
    boolean bEnableProtoData=false;  //if enabled data sent to cloud will use Google Protocol Buffers 
    boolean bEnableWindowSize=true;  // if Enabled compressions symbols will be created every Samplesize 
   								 //and if disabled, symbol will be created only once at beginning using SampleSize samples  
    boolean bEnableDataModel=false;
    boolean bEnableBenchmarking=false; // If enabled benchmarking will be no compression
    boolean bEnableZippedJSON=false;   // If enabled ,JSON data will be sent as a zip
    boolean bEnableLinearCompression=false;
    boolean bEnablePolyCompression=false;
    boolean bEnableCEP=false;
    boolean bUseDeltaForRLE=true;   //Whether to used Deltas as zero for RLE or pfxsymbols as zeros for RLE
    //ConcurrentLinkedQueue<byte[]> MQTTMessageQ = new ConcurrentLinkedQueue<byte[]>();
    //ConcurrentLinkedQueue<String> MQTTTopicQ =new ConcurrentLinkedQueue<String>();
	 int rleIndexTable[] = null;  // Assuming max datapoint per sample to be 1000 and we use 8 bits to keep count
	 int rleCountTable[] = null;  // Assuming max datapoint per sample to be 1000 and we use 8 bits 
	 int currentRLECount=0; // For keeping track of counts for currently indexed datapoint in RLETable
	 int currentIndexTobeUpdated=0;
	 
    // Start Analytics Variables
    //int nZeroDeltaCount=0;  // keep track of number of sensors exhibiting zero delta between consecutive readings
    int nAggregatedTotalNumBits=0; // Total number of bits of aggregated delta values to be used on cloud
  //  int[]numBits=null;   // Array to store required number of bits for delta between consecutive readings
    int nAnamolyDetectSamples=20;  // Configurable number of samples
    ConcurrentLinkedQueue<Double> AggregatedBitsQ =new ConcurrentLinkedQueue<Double>(); // Aggregated compressed bits queue with fixed size 
    int nDetectLow=30;  // Configurable Lower range for Anomaly detection  
    int nDetectHigh=40;  //Configurable Higher range for Anomaly detection
    int nNumAnamolies=0;
    int nAdEventValue=0;  // Anomaly Detect value
    int nEvent=0; // For tracking same
    int nDpCounter=0; 
    double wma=0;
    double sma=0;
   // List<DoublePoint> DBScanPoints = new ArrayList<DoublePoint>();
    boolean bCollectData=false; // Used to collect numberof bits and wma values to set number of samples and detection thresholds
    boolean bIsCompressor=false;  // flag to track if we are doing compression or decompression
    //End Analytics variables

	    
     // MQTT brokers don't guarantee in order delivery of messages. Our DataXlator relies on order so
    // each topic will also contain message number. Data mesages can be pushed to this Q
   // Map<String,byte[]> DataMessageHandlerQ = new ConcurrentHashMap<String,byte[]>();

    //Map<String,byte[]> DataMessageHandlerQ = new ConcurrentHashMap<String,byte[]>();
    
    ConcurrentLinkedQueue<byte[]> DataMessageHandlerQ =new ConcurrentLinkedQueue<byte[]>(); // 

    //ConcurrentLinkedQueue<byte[]> DecompressorDataMessageHandlerQ =new ConcurrentLinkedQueue<byte[]>();
    
    //Map<Integer,MessageFormatterArray.Builder> DecompressorDataMessageHandlerQ = new ConcurrentHashMap<Integer,MessageFormatterArray.Builder>();
    
    String scaleChangedCSV="";  // Used to indicate to cloud instance of change in upward change in scale format: (index,scale)
    
  	//long[]currentPayloadArray; //= new ArrayList<long[]> (); //We have to compress for each column
  	long[] [] sensorDataDelta; //= new ArrayList<long[]> ();
  	int sensorDataUpdateIndex=0;    // used to track update index 
  	
  	// For String encoding
  	int stringDataUpdateIndex=0;
  	Map<String,Integer> StringEncodeMap = new ConcurrentHashMap<String,Integer>();
  	Map<Integer,String> StringDecodeMap = new ConcurrentHashMap<Integer,String>();
  	
    String prevStringArray[];
    int encodeStringIndex[];  // index into hashmap
    int strEncodeNumbits[]; // number of bits required including headers
    byte encodedString[];  // encoded string output array
    int MAX_NUMBER_STRINGS=128; // Number of strings
    int MAX_STRING_SIZE=255;  // Maximum size of String in bytes
    int numBitsRequiredForString=0; // Update this at the start 
    int numBitsRequiredForIndex=0;
    int newStringAddedIndicator= 0xFFFFFFFF;
    //ArrayList<long[]> currentSensorData;// = new ArrayList<long[]> (); //Current Historical Sensor Data for Window based compression
	//ArrayList<Integer[]> sensorDataDelta = new ArrayList<Integer[]> (); //We have to compress for each column
	//Integer [] baseValue;
    
    // JSON Array Handling Variables
    
    // We'll send notification only when there is increase in Array Size. If Array size decreases, we'll just notify on number of rows in array
    // for creation of output data
     Map<String,Integer> JsonArraySizeMap = new ConcurrentHashMap<String,Integer>();  // This will store the previously processed size of an Array
    
     long prevSensorData[]; 
     //Long prevTimestamp=0L;
     Gson gsonBuilder=null;
     boolean bProcessingCompressedMsg=false;
     boolean bResetPfxCode; // Track when to reset Prefix codes for window based compression
     List<Map <Long,Long>> listOfPrefixCodes = new ArrayList<Map <Long,Long>>(); // Array to hold prefix code symbols
  //   List<Map <MutableLong,MutableLong>> mutablelistOfPrefixCodes = new ArrayList<Map <MutableLong,MutableLong>>(); // Array to hold prefix code symbols
	// List<Map <Integer,Integer>> currentListOfPrefixCodes = new ArrayList<Map <Integer,Integer>>(); // Array
	 
	 int numHuffLevels=3;
	 long[] huffSymbols3={0L,8L,9L,10L,11L,48L,49L,50L,51L,52L,53L,54L,55L}; // Prefix symbols upto three levels
	 long[] huffSymbols2={0L,8L,9L,10L,11L}; // Prefix symbols upto two levels
	 int rleIdentifier3 = 0x0E;
	 int rleIdentifier2 = 0x06;
	// int outofBoundvaluesHeader3= 0x1E;
	 int outofBoundvaluesHeader3= 0x3C;  // We'll two bytes for length indication
	 //int outofBoundvaluesHeader2= 0x0E;
	 int outofBoundvaluesHeader2= 0x1C;   // We'll two bytes for length indication
	 int outofBounderHeader=0;
	 //int outofPfxBoundNumidentifierBits3=5;
	 //int outofPfxBoundNumidentifierBits2=4;
	 int outofPfxBoundNumidentifierBits3=6;
	 int outofPfxBoundNumidentifierBits2=5;
	 int outofPfxBoundNumidentifierBits=0;
	 int rleNumHeaderbits=0; //Number of bits used as RLE identifier
	 int rleNumIdentifierbits3=4;
	 int rleNumIdentifierbits2=3;
	 int rleNumSizeBits=0;  // # of bits We'll have three levels depending on size of data points 0-100 : 4 bits 100-2000: 8 bits >2000: 10 bits
     int rleMaxlength=0;
     int rleTriggerLength=0;
     int rleHeader=0;
     int rleHeader3=0x0E;
     int rleHeader2=0x06;
     int rlemask=-1;
	 int rlesizenumbits=0; 
	 int shiftedrleheader=0; 

	// int HUFF_LEVEL=3;
	// long[]prefixSymbols={0L,8L,9L,10L,11L,48L,49L,50L,51L,52L,53L,54L,55L}; // Prefix symbols upto three levels
	 //try two levels
	 
	 long[]prefixSymbols; //={0,8,9,10,11}; // Prefix symbols upto two levels
	 

	 // Long[]prefixSymbols2={0L,8L,9L,10L,11L,48L,49L,50L,51L,52L,53L,54L,55L}; // Prefix symbols upto three levels
	 int nRegressionSampleSize=10; //Regression parameters
	// List<Integer[]> LinearRegressionHistory= new ArrayList<Integer[]>();
    // For each column index Regression Array size will be of nRegressionSampleSize
	 ArrayList<long[]> RegressionHistory = new ArrayList<long[]> ();
	 long yArray[];
	 String currentLocation="";
	 String uaid="";
	// Gson gson = new Gson();
	 Gson gson = new GsonBuilder().setPrettyPrinting().create();
	 //Datamodel Generation
	 //ArrayList<Model> model=new ArrayList<Model>();

	 long lTotalCompressedMsgSize=0;
	 long lcurrentCompressedSize=0;
	 long currentZippedmsgSize=0;
	 long currentInputmsgSize=0;
	 long lTotalInputMsgSize=0;
	 long lTotalGZippedJSONSize=0;
	 long lTotalGZipTime=0;
	 long lTotalCompressionTime=0;
	 long lMsgCounter=0;
	 long lPrevMsgCounter=-1;  // Use this to monitor change in msg counters before sending statts as it is sent periodically
	 int lMqttMsgId=0;  // MessageIds in MQTT topic from Datagenerator to edge and from edge to cloud
	 boolean bResetMsgID=false;
	 CEP cepEngine;
	 JsonParser parser= new JsonParser();
	 DataModel dataMapperObj=null;   //This is the triggger for publishing datamodel from edge to cloud 
	 DataModel perfDataModelObj=null;  // To be used for transferring performance parameters 
	 String dataMapperString="";
	 String dataMapperCSVString=""; // Comma separated Data mapper field names
	 
	 JsonElement jsonModelObj=null;
	 //Model modelObj=new Model();
	 //Logger
	 Logger logger=null;
	 // Optimizations for sending compressed data
	 int firstskippedSensor=0;  // Identifier for the first skipped sensor id
	 byte [] skippedSensorIDs= null; //Array to store sensor data not transmitted
	 boolean bEnableFilter = false; // If enabled do not send samples with no change
	 //Object CloudQLock = null;
	 Object inQLock=null;
	// long currentSensorData[]= null;  //create one time to avoid heap usage and GC  
	// long payloadArray[]= null; 
	 // Expected topics on Cloud
	String datamodelTopic="teevr/sensors/rdatamodel/";
	String jsonmodelTopic="teevr/sensors/jsonmodel/";
	String cloudExpectedTopics[]={"teevr/sensors/scale/","teevr/sensors/compressed/","teevr/sensors/rcombo/" };
	String edgeExpectedTopics[]={"teevr/sensors/combo/"};
	boolean bEnabledDataComparison=false;  //  If datacomparison is enabled then datagenerator will send with message ids
	int lSimulatedMsgId=0;  // used in MQTTClient adapter
	boolean bTxZippedMsg=false; // Sending as zipped messages seems to require more heap and result into out of memory exceptions
	
	long msgCounter=0;
	DBAdapter dbAdptrRawData=null;
	DBAdapter dbAdptrML=null;  // Adapter for putting ML data like compression bits, wma
	//TDIMessage msgFormatter= new TDIMessage();

	int MAX_JSON_ROW_SIZE=8;
	JsonElement defaultArrayElement=null;
	String AdDataModelString="";
	
/*	public enum DataFormat {
	    AUTO,
		CSV, 
	    JSON
	};
*/
	int InputFormat=DataFormat.AUTO;
	int OutputFormat=DataFormat.AUTO;
	Compressor compressor=null;
	Decompressor decompressor=null;
	boolean bToggle=false;
	int nDataCount=0;
	int datamodelFieldIndex=0;  // Used to get the index into DataModelObject for reading the model during output conversion
	 public MQTTMessageHandler(Configuration config, boolean bIsRemoteEndpoint, String location, UsageMeter usageMrt) {
		this.config=config;
		currentLocation=location;
		this.usageMtr=usageMrt;
		uaid=config.getAdvanced().getUAID();
		if(bIsRemoteEndpoint)
		{
		   dbAdptrRawData=new DBAdapter(uaid,config,location);
		   dbAdptrML=new DBAdapter(uaid,config,location);
		}
		 if(bIsRemoteEndpoint)
			 logger = Logger.getLogger("DataXlrator-Cloud");
		 else
			 logger = Logger.getLogger("DataXlrator-Edge");

		 //CloudQLock = new String("CloudQLock-"+ location);
		 //EdgeQLock = new String("EdgeQLock-"+location);
		 if(bIsRemoteEndpoint)
			 inQLock=new String("CloudInQLock-"+ location);
		 else
			 inQLock=new String("EdgeInQLock-"+ location);
	

		if (config.getCompression()!=null)
		{
			this.sampleSize=config.getCompression().getSampleSize();
			this.lowBitSize=config.getCompression().getLowBitsSize();   
			this.highBitSize=config.getCompression().getHighBitsSize();
			this.precisionCap=config.getCompression().getMaxPrecision();   
			this.bUsePfxEncoding=config.getCompression().getUsePfxEncoding();
			this.bEnableBatch=config.getCompression().getEnableBatch();
			this.batchsize=config.getCompression().getBatchSize();
			this.nRegressionSampleSize= config.getCompression().getRegressionSampleSize();
			this.bEnableLinearCompression=config.getCompression().getEnableLinearRegression();
			this.bEnablePolyCompression=config.getCompression().getEnablePolyRegression();
			yArray= new long[nRegressionSampleSize]; //
		}

		 if(config.getAdvanced().getInput()== Input.JSON)
		 {
			 InputFormat=DataFormat.JSON;
		 }
		 else
			 if(config.getAdvanced().getInput()== Input.AUTO)
			 {
				 InputFormat=DataFormat.AUTO;
			 }
			 else
				 InputFormat=DataFormat.CSV;

		 if(config.getAdvanced().getOutput()== Output.JSON)
		 {
			 OutputFormat=DataFormat.JSON;
		 }
		 else
			 if(config.getAdvanced().getOutput()== Output.AUTO)
			 {
				 OutputFormat=DataFormat.AUTO;
			 }
			 else
				 OutputFormat=DataFormat.CSV;

		if(!bIsRemoteEndpoint)
		{
	     compressor= new Compressor();
	     compressor.init(precisionCap, lowBitSize, highBitSize, sampleSize,bUsePfxEncoding,bEnableBatch,batchsize);
	     compressor.setDataMapper(getDataModel(location));
	     compressor.setDataFormat(InputFormat);
	     decompressor=null;
		}
		else
		{
	     decompressor= new Decompressor(config.getAdvanced().getLicenseCheckMethod().toString());
		 decompressor.setDataFormat(OutputFormat);
	     compressor=null;
		}
	     
		 
		if (config.getMlad()!=null)  // Machine LEarning and Anomaly Detection
		{
			this.nAnamolyDetectSamples=config.getMlad().getSampleSize();
			this.nDetectLow=config.getMlad().getLowThreshold();   
			this.nDetectHigh=config.getMlad().getHighThreshold();
			this.bCollectData=config.getMlad().getCollectData();
		    // Try JSON printing here
		/*	String mlad=gson.toJson(config.getMlad());
			System.out.println("JSON String:" + mlad);
			Mlad obj=gson.fromJson(mlad, Mlad.class);
			obj.setSampleSize(100);
			System.out.println("JSON String:" + gson.toJson(obj));
			*/
			
		}
		
		//sensorDataArray = new long[SampleSize]; //We have to compress for each column
	  	//sensorDataDelta = new int[SampleSize];
	  	sensorDataUpdateIndex=0;
		//currentSensorData = new ArrayList<long[]> (SampleSize); //Current Historical Sensor Data for Window based compression

	   
		bEnableJsonData=config.getAdvanced().getEnableJsonData();
		bEnableProtoData= config.getAdvanced().getEnableProtoData();
		if(bEnableJsonData)
			gsonBuilder=new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();
		bEnableBenchmarking=config.getAdvanced().getEnableBenchmarking();
		bEnableZippedJSON= config.getAdvanced().getEnableZippedJsonData();
		bEnableWindowSize=config.getAdvanced().getEnableWindowSize();
		bEnabledDataComparison=config.getAdvanced().getEnableDataComparison();
		this.bIsRemoteEndpoint=bIsRemoteEndpoint;
        // Set Data input and out formats
		
		this.bEnableCEP=config.getAdvanced().getEnableCEP();
		remotebroker=null;
		perfMonitorbroker=null;
		//dbAdapter=null;
		if(config.getAdvanced().getEnableDataModel()!=null)
			bEnableDataModel=config.getAdvanced().getEnableDataModel();
		//Reading DataModel
		//Reading Data model
		if(bEnableDataModel)
		{
			
				
		}
		
			prevSensorData=null;
		
		if(bEnableCEP)
			cepEngine=new CEP(location,uaid);
		else
			cepEngine=null;
		
		if(config.getAdvanced().getEnableDataCollector())
		{
			try {
				
				File file = new File(config.getAdvanced().getDataFile());

				// if file doesnt exists, then create it
				if (!file.exists()) {
					file.createNewFile();
				}

				FileWriter fw = new FileWriter(file.getAbsoluteFile(),true);
				bw = new BufferedWriter(fw);
				
				//bw.close();
			} catch (IOException e) {
				e.printStackTrace();
				
			}
		}
	}

	public MQTTMessageHandler() {
		// TODO Auto-generated constructor stub
	}

	public void init(MqttClientAdapter remotebroker, MqttClientAdapter perfMonitorbroker) {
		//this.dbAdapter= dbAdapter;
		this.remotebroker=remotebroker;
		this.perfMonitorbroker=perfMonitorbroker;
//		 System.out.println("Initializing CEP.." + dbAdapter + " Db: " + dbAdapter.getEventsDBName());
		//initialize CEP
		//cepEngine.init(dbAdapter);
		if(bEnableCEP && (cepEngine!=null))
			cepEngine.init(perfMonitorbroker);  // Initialize remotebroker as well for publishing the event
		if(dbAdptrRawData!=null)
			dbAdptrRawData.init();
		if(dbAdptrML!=null)
			dbAdptrML.init();
		logger.info("MQTT Handler for Datasource " + currentLocation + " initialized.");
	
		}
	
	public   void AddToDataMessageQ(byte[] message)
	{
		synchronized(inQLock)
		{
			DataMessageHandlerQ.add( message);
		}
	}
	

	/*public   void AddToDataMessageQ(Integer seqNum, MessageFormatterArray.Builder msgArr)
	{
		
		// We should change the value to a bit number and check for rollovers
	//	if(seqNum==1)    // Reset message id tracker if the sequence number is 1. We still need to take care of roll over 
		//	lMqttMsgId=0;
		
		
		synchronized(inQLock)
		{
			DecompressorDataMessageHandlerQ.put(seqNum, msgArr);
		}
		//logger.info(topic);
	}
	*/
	

	public String getPerfJSON(long currentInputmsgSize2, long currentZippedmsgSize2,long lcurrentCompressedSize2, long lTotalJsonMsgSize2, long lTotalGZippedJSONSize2,long lTotalCompressedMsgSize2, long lGzipTime, long lCompressionTime, long MessageCount)
	{
		long msg[]={currentInputmsgSize2,currentZippedmsgSize2,lcurrentCompressedSize2,lTotalJsonMsgSize2,lTotalGZippedJSONSize2,lTotalCompressedMsgSize2,lGzipTime,lCompressionTime,MessageCount};
		JsonObject JsonObj = new JsonObject();
	    String jsonOutput="";
	    for(int i=0;i<msg.length;i++)
		{
	    		if(msg[i]!=0)
				JsonObj.addProperty(perfDataModelObj.getModel().get(i).getName(),new Double(msg[i]).longValue());
			
		}
	    jsonOutput=gson.toJson(JsonObj);
	    if(logger.isTraceEnabled())
	    	logger.trace("Location: " + currentLocation + "  " +  jsonOutput);
	    return jsonOutput;
	}
	
public void InitDataModel(String datamodel)
	{
        if(!datamodel.isEmpty())
        {
        	
        	//System.out.println(datamodel);
			dataMapperObj=gson.fromJson(datamodel, DataModel.class);
			// Update CSV Names string
			for(int i=0; i<dataMapperObj.getModel().size();i++)
			{
				if(i==0)
					dataMapperCSVString=dataMapperObj.getModel().get(i).getName();
				else
					dataMapperCSVString=dataMapperCSVString+","+dataMapperObj.getModel().get(i).getName();
			}
			
			UpdateDataModelPrecison(dataMapperObj);
        }
	}

public void InitPerfDataModel()
{
	String datamodel=getPerfDataModel();
    if((datamodel!=null) && !datamodel.isEmpty())
    {
		perfDataModelObj=gson.fromJson(datamodel, DataModel.class);
		
    }
}

public void InitADDataModel()
{
	String datamodel=getADDataModel();
	if((datamodel!=null) && !datamodel.isEmpty())
		AdDataModelString=getADDataModel();
}
public void UpdateDataModelPrecison(DataModel dataModelObj)
	{
        if(!(dataModelObj==null))
        {
        	int nStringCount=0;
        	numDataPts=dataModelObj.getModel().size();
            listOfPrefixCodes = new ArrayList<Map <Long,Long>>(numDataPts); // Array to hold prefix code symbols 
            listOfPrefixCodes.clear();
            RegressionHistory.clear();
            //currentListOfPrefixCodes = new ArrayList<Map <Integer,Integer>>(numDataPts); // Array
       	    // initialize data and delta storage arrays here
            //currentPayloadArray = new long[numDataPts]; 
    	  	sensorDataDelta = new long[sampleSize][numDataPts];
    	  	sensorDataUpdateIndex=0;

       	    // populate fields as per hufflevel
       	   if(numHuffLevels==3)
       	   {
       		   prefixSymbols= huffSymbols3;
       		   rleNumHeaderbits=rleNumIdentifierbits3;
       		   rleHeader=rleHeader3;
       		   outofPfxBoundNumidentifierBits=outofPfxBoundNumidentifierBits3;
       		   outofBounderHeader=outofBoundvaluesHeader3;
       	   }
       	   else  //assume level2 only
       	   {
       		   prefixSymbols= huffSymbols2;
       		   rleNumHeaderbits=rleNumIdentifierbits2;
       		   rleHeader=rleHeader2;
       		   outofBounderHeader=outofBoundvaluesHeader2;
       		   outofPfxBoundNumidentifierBits=outofPfxBoundNumidentifierBits2;;
       	   }
       		   
       	    if(numDataPts<1000)  // 
       	    	rleNumSizeBits=4; 
       	    else
       	    	//if(numDataPts<=2000)
       	    	//	rleNumBits=8;
       	    	//else
       	    		rleNumSizeBits=10;
       	    
       	    rleMaxlength=(1<<rleNumSizeBits)-1;
       	    
       	    rleTriggerLength=rleNumSizeBits + rleNumHeaderbits;
       	    
       	    rlemask=-1;
       	    rlemask=~(rlemask<<rleNumSizeBits);
       	    rlesizenumbits=rleNumSizeBits+rleNumHeaderbits;
       	    shiftedrleheader=(rleHeader<<rleNumSizeBits);

       	    bitSzStep= (highBitSize-lowBitSize)/3;
       	    
       	 rleIndexTable= new int[numDataPts/4];// Assume 1/4 of numDatapts should be sufficient
       	 rleCountTable= new int[numDataPts/4];
        /*  if(bIsRemoteEndpoint)
          {
        	  currentSensorData= new long[numDataPts];
          }
         */
       	 //Temp change to handle ds1 precision
       	//if(numDataPts==12)  // Mobile phone data
       		//precisionCap=2;
         //payloadArray= new long[numDataPts];
           prevSensorData= new long[numDataPts];
			//logger.info("rleNumBits:" + rleNumBits + "rleMaxlength: " + rleMaxlength + "rleTriggerLength: "+ rleTriggerLength);
       	    //dataModelPrec=new long[dataModelObj.getModel().size()];
			dataModelScale=new int[dataModelObj.getModel().size()];
	
			for(int i=0;i<dataModelObj.getModel().size();i++)
			{
	
				//dataModelPrec[i]= (long) Math.pow(10,dataModelObj.getModel().get(i).getPrecision());
				//if(dataModelPrec[i]==0) // This means scale is negative number, which means it is a long number already and just notation using exponent
					//dataModelPrec[i]=1;
				if(dataModelObj.getModel().get(i).getDataType()==DataType.STRING)
					nStringCount++;
				
				// Sometime Datamodel could be autoderived and in that case the precision may not be correctly set. Ensure it does not go beyond Precision cap
				dataModelScale[i]=dataModelObj.getModel().get(i).getPrecision()>precisionCap?precisionCap:dataModelObj.getModel().get(i).getPrecision();

			}
			if(nStringCount==0)
			{
				prevStringArray=null;
				encodeStringIndex=null;
				strEncodeNumbits=null;
			}
			else
			{
				prevStringArray= new String[nStringCount];
				encodeStringIndex= new int[nStringCount];
				strEncodeNumbits= new int[nStringCount];
			    int tempspread=MAX_STRING_SIZE;
			    numBitsRequiredForString=1; // Update this at the start 
    		    while ((tempspread >>>= 1) != 0) // Major performance issue may exist
    		    {
    		    	numBitsRequiredForString++;
    		    }
    		    
    		    logger.info("Number of bits required for String Storage : " + numBitsRequiredForString + " for Max String size of " + MAX_STRING_SIZE +" bytes.");
    		    
    		    tempspread=MAX_NUMBER_STRINGS-1;
    		    numBitsRequiredForIndex=1;
    		    while ((tempspread >>>= 1) != 0) // Major performance issue may exist
    		    {
    		    	numBitsRequiredForIndex++;
    		    }
    		    
			}
        }
	}


	public String getFileData( String filepath)
	{
		BufferedReader dmreader=null;
	
		 InputStream input= getClass().getResourceAsStream("/"+filepath);
		 if(input==null)
		 {
			Path dsResource = Paths.get("./"+filepath);
	
			try {
				dmreader = Files.newBufferedReader(dsResource, StandardCharsets.UTF_8);
			} catch (IOException e) {
				logger.info(e.getMessage());
			}
		 }
		 else
		 {
			 try {
				 dmreader = new BufferedReader(new InputStreamReader(input, "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				logger.info(e.getMessage());
			}
		 }
	
		  if(dmreader==null)
		  {
			  return null;
		  }
			
	       StringBuilder fileDataString=new StringBuilder();
	       String inputline="";
	       try {
				inputline=dmreader.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	       
	       while(inputline!=null)
	       {
	    	   fileDataString.append(inputline);
	       	try {
					inputline=dmreader.readLine();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	       }
	       
	       return fileDataString.toString();
	       
	}
	
	public String getDataModel( String datasource)
	{
			
		 return getFileData("configs/"+datasource+"m.json");
	}
	
	public String getJsonData( String datasource, int fileIndex)
	{
		
		 return getFileData(datasource +"j/"+ fileIndex + ".txt");
	}	

	public String getPerfDataModel()
	   {
		return getFileData("perf.json");
	   }

	public String getEventDataModel( )
	   {
		return getFileData("event.json");
	   }
	
	public String getADDataModel( )
	   {
		return getFileData("ad.json");
	   }


//	public  void processDecompressorInput(MessageFormatterArray.Builder inmsg)
	public  void processDecompressorInput(byte[] inmsg)
	{
	
		
		String msgOutput="";
		
		//msgOutput=decompressor.decompress(inmsg.build().toByteArray());
		msgOutput=decompressor.decompress(inmsg);
		//logger.info(msgOutput);
		if(config.getAdvanced().getDumpDecompressedOutput())
			System.out.println(msgOutput);
		
		
		// Since Messages are in Order, we do not expect to get null
		if((msgOutput!=null) && msgOutput.isEmpty())
		{
			// 
			logger.info("Out of order Message");
			return;
		}
		else
		{
			if(msgOutput==null)
			{
				logger.error("Invalid License or AMI Product Code.");
				System.exit(-1);
			}
		}
		

		
		lMsgCounter++;
		
		
  		if (lMsgCounter>sampleSize)
  		{
  			currentInputmsgSize=msgOutput.length();
  			lcurrentCompressedSize=inmsg.length;
   		}
		
       	if((lMsgCounter==1) && (perfMonitorbroker !=null))  // Start performance monitor thread
		{
			cloudPerfMonitor();
		}
       	
		if(!msgOutput.isEmpty())
		{
			// Update Metering service
			if(usageMtr!=null)
			{
			  // Update 
				usageMtr.updateUsage(decompressor.getSensorCount());
			}
			routeOutputMsg(msgOutput);
		}
		
	}

	

	private void routeOutputMsg(String msgOutput) {

		
		
       	// Write the data to database
       //	dbAdptrML.write(nAggregatedTotalNumBits+","+wma + "," + nAdEventValue);
       	nAdEventValue=0; // Reset the value for Anomaly detection now
       	
       	
       	// If database is set to None do not call DB writes
       	if(config.getAdvanced().getClouddb()!=Clouddb.NONE)
       	{
	       	if(!AdDataModelString.isEmpty())
	       		dbAdptrML.write(decompressor.getAnamolyDetectJSON(AdDataModelString));
	       	dbAdptrRawData.write(decompressor.getJSON(msgOutput));
       	}
       	// Check if this is JSON output
		// check if it is JSON 

		// publish to Websockets and MQTT
		String topicToPublish="teevr/DataXlrator/"+currentLocation;
		
		if(!config.getMQTT().getCloudMQTT().getPublishTopics().isEmpty())
		{
			topicToPublish=config.getMQTT().getCloudMQTT().getPublishTopics().get(0)+"/"+currentLocation;
			// AWS IOT does not send message in order so we should publish with message IDs to ensure that
			//endpoint assembles in order. Will be primarily be  useful for our internal testing
			// Since we are not sending sequence numbers, we should not publish with sequence numbers too but if datacomparison is enabled, let's send it to ensure
			// the messages compared are in order
			//if(config.getAdvanced().getEnableDataComparison())
			//	topicToPublish=topicToPublish +"/"+lMqttMsgId;
		}

		if(!config.getMQTT().getCloudMQTT().getPublishTopics().isEmpty())
		{
			
			if(!msgOutput.isEmpty())
					remotebroker.publish(topicToPublish,msgOutput.getBytes());
		}
		
	}


	private TimeUnit getTimeUnit(PerfMonitorTimeUnit inUnit)
	{
		
  	  TimeUnit unit=TimeUnit.MILLISECONDS;
  	  switch(inUnit)
  	  {
  	  	case MS:
  	  		unit=TimeUnit.MILLISECONDS;
  	  		break;
  	  	case SEC:
  	  		unit=TimeUnit.SECONDS;
  	  		break;
  	  	case MIN:
  	  	    unit=TimeUnit.MINUTES;
  	  		break;
  	  	case HR:
  	  		unit=TimeUnit.HOURS;
  	  		break;
  		default:
  	  		unit=TimeUnit.MILLISECONDS;
  	  		
  	  }
		return unit; 
	}
	public void cloudPerfMonitor()
	{
		Runnable runnable = new Runnable() {
            public void run() {
              // task to run goes here
    			String PerfJson= "";
    			if(lPrevMsgCounter!=lMsgCounter)
    			{
	    			lPrevMsgCounter=lMsgCounter;	
	    			//PerfJson=getPerfJSON(0,0,0,0,0,0,0,0,lMsgCounter);
	    			// For Bandwidth optimization, we need to calculate current ratio on cloud
	    			PerfJson=getPerfJSON(currentInputmsgSize,0,lcurrentCompressedSize,0,0,0,0,0,lMsgCounter);
		           	String topic= new String("teevr/sensors/perf/"+uaid + "/"+currentLocation + "/cloud");
		           //	remotebroker.publish(topic,PerfJson.getBytes()); 
		          		try {
		          			
		          			if (!perfMonitorbroker.publish(topic,0,PerfJson.getBytes())) //publish performance numbers to be consumed by UI
		          			{
		          				// If publish fails
		          				try {
									Thread.sleep(1000);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
		          			}
		          			PerfJson=null;
		          			topic=null;
		
		   			} catch (MqttException e) {
		   				// TODO Auto-generated catch block
		   				e.printStackTrace();
		   			}
    			}
            }
          };
          
          

          if(config.getAdvanced().getPerfMonitorTimeUnit()!= PerfMonitorTimeUnit.NONE)
          {

	          ScheduledExecutorService service = Executors
	                          .newSingleThreadScheduledExecutor();
	          service.scheduleAtFixedRate(runnable, 0, config.getAdvanced().getPerfMonitorTimePeriod(), getTimeUnit(config.getAdvanced().getPerfMonitorTimeUnit()));
          }
	}
	
	public void edgePerfMonitor()
	{
		Runnable runnable = new Runnable() {
            public void run() {
              // task to run goes here
    			String PerfJson= "";
    			long lcompressionTime=0;
    			long lgZipTime=0;
    			if(lPrevMsgCounter!=lMsgCounter)
    			{	
    				lPrevMsgCounter=lMsgCounter;
    				if(lMsgCounter>sampleSize)
    				{
    					lcompressionTime=lTotalCompressionTime/(lMsgCounter-sampleSize);
    					lgZipTime=lTotalGZipTime/(lMsgCounter-sampleSize);
    				}
    					
		   			//PerfJson=getPerfJSON(currentInputmsgSize,currentZippedmsgSize,lcurrentCompressedSize,lTotalInputMsgSize,lTotalGZippedJSONSize,lTotalCompressedMsgSize,lgZipTime,lcompressionTime,lMsgCounter);
    				// For optimization purposes we calculate current compression ratio on cloud
    				PerfJson=getPerfJSON(0,0,0,lTotalInputMsgSize,0,lTotalCompressedMsgSize,0,lcompressionTime,lMsgCounter);
    				
		           	String topic= new String("teevr/sensors/perf/"+uaid + "/"+currentLocation);
		           //	remotebroker.publish(topic,PerfJson.getBytes()); //publish performance numbers to be consumed by UI
		
		          		try {
		          			
		          			if (!perfMonitorbroker.publish(topic,0,PerfJson.getBytes())) //publish performance numbers to be consumed by UI
		          			{
		          				try {
									Thread.sleep(1000);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
		          			}
		          			PerfJson=null;
		          			topic=null;
		
		   			} catch (MqttException e) {
		   				// TODO Auto-generated catch block
		   				e.printStackTrace();
		   			}
    			}
            }
          };
          
  /*        ScheduledExecutorService service = Executors
                          .newSingleThreadScheduledExecutor();
          service.scheduleAtFixedRate(runnable, 0, PERF_MONITOR_FREQUENCY, TimeUnit.MILLISECONDS);
*/
          if(config.getAdvanced().getPerfMonitorTimeUnit()!= PerfMonitorTimeUnit.NONE)
          {

	          ScheduledExecutorService service = Executors
	                          .newSingleThreadScheduledExecutor();
	          service.scheduleAtFixedRate(runnable, 0, config.getAdvanced().getPerfMonitorTimePeriod(), getTimeUnit(config.getAdvanced().getPerfMonitorTimeUnit()));
          }
	}
	
	
	public   void processCompressorInput(byte[] inmsg)
	{
		String inputmsg= new String(inmsg);
		String incsvMsg=""; // message after extracting into CSV format
		long nEnd=0;
		long nStart=System.nanoTime();
		long JsonExtractionTime=0;
		long JsonExtractionStartTime=0;
		String publishtopic="teevr/msg/"+currentLocation;
		
/*		MessageFormatter msgFormatter; 
		
		MessageFormatterArray.Builder msgArr=MessageFormatterArray.newBuilder();
		
		msgArr=MessageFormatterArray.newBuilder(); // Reset message Array
		msgArr.setSequenceNum(lMqttMsgId);  // set the sequence number
		bIsCompressor=true; */
		//Simulator
		nDataCount++;
		/*if(nDataCount>1000)
		{
			nDataCount=1;
			//logger.info("Not processing..");
			return;
		}
		String file= "event"+StringUtils.leftPad(String.valueOf(nDataCount), 4, "0") +".txt";
	//	logger.info(file);
		inputmsg= getData(file);*/
	//	logger.info(inputmsg);
		
		/*if(!bToggle)
			inputmsg= getDataModel("nested1"); // bypass
		else
			inputmsg= getDataModel("nested2"); // bypass
		
		bToggle=!bToggle;
	*/


		
/*		msgFormatter=processDataMapper(inputmsg);
		if(msgFormatter!=null)
		{
			msgArr.addMsgFormatter(msgFormatter);
			// Process JSON Model now
			msgFormatter=processJsonModel(inputmsg);
			if(msgFormatter!=null)
			{
				msgArr.addMsgFormatter(msgFormatter);
			}
		}
	
		// extract data from JSON
		if(InputFormat==DataFormat.JSON)
		 {
		  incsvMsg= InputAsJSON(inputmsg);
		 }
		else
			incsvMsg=inputmsg;

		byte[] compressedBytes=getCompressedMsg(incsvMsg,msgArr);
*/
		byte[] compressedBytes= compressor.compress(inputmsg);

		
		nEnd=System.nanoTime();
		lMsgCounter++;
		
		//logger.info("Msg ID: " + lMsgCounter + "Input Size: " + inputmsg.length() + "Compressed Size: " + compressedBytes.length);
		// Update Benchmark and performance parameters
  		if (lMsgCounter>sampleSize)
  		{
  			currentInputmsgSize=inputmsg.length();
  			if(compressedBytes!=null)
  				lcurrentCompressedSize=compressedBytes.length;
  			else
  				lcurrentCompressedSize=0;
  			
  			lTotalCompressionTime+=((nEnd-nStart)/1000);
  		}
  
          /*if(bEnableBenchmarking)
          {
	      	    //GZIP size for JSON/CSV
				nStart=System.nanoTime();
				if (lMsgCounter>=sampleSize)
				{
					currentZippedmsgSize=getZippedBytes(inputmsg).length;
					//nEnd=System.nanoTime();
					
					lTotalGZipTime+=((System.nanoTime()-nStart)/1000);
				}
	      }	*/
        
          updatePerformanceParameters(); 
          if(compressedBytes!=null)
        	  routeCompressedOutput(compressedBytes);
		
	}

	private void routeCompressedOutput(byte[] compressedBytes) {
		
		String publishtopic="teevr/msg/"+currentLocation;
		
        if(config.getAdvanced().getEdge2Cloud()==Edge2Cloud.MQTT)   // All messages in one go
       	{
       		//logger.info(" Msg size: " +compressedBytes.length );
       		remotebroker.publish(publishtopic, compressedBytes);
       	}
        else
        	if(config.getAdvanced().getEdge2Cloud()==Edge2Cloud.COAP)   // All messages in one go over COAP
           	{
           		//logger.info(" Msg size: " +compressedBytes.length );
           		remotebroker.coapClient.send(currentLocation,compressedBytes);
           	}
	}

	private void updatePerformanceParameters() {
		
  		if(lMsgCounter<=sampleSize)
  		{
        	  //Exclude initial samples used for symbol creation as it skews the compression ratio
  			lTotalCompressedMsgSize=1;
  			lTotalInputMsgSize=1;
  			lTotalGZippedJSONSize=1;
  	      	lcurrentCompressedSize=1;
  	      	currentInputmsgSize=1;
  	      	currentZippedmsgSize=1;
  	      	lTotalCompressionTime=0;
  	      	lTotalGZipTime=0;
  		}
  		else
  		{
  			lTotalCompressedMsgSize+=lcurrentCompressedSize;
  			lTotalInputMsgSize+=currentInputmsgSize;
  			lTotalGZippedJSONSize+=currentZippedmsgSize;
  		}


		if((lMsgCounter==1) && (perfMonitorbroker !=null))  // Start performance monitor thread
		{
			edgePerfMonitor();
		}

		
	}


	private void generateCEPEvent(String csvMessage) {
		// TODO Auto-generated method stub

	
		// CEP affects overall processing time per message so this should be moved to a background thread for processing
		if(bEnableCEP && (cepEngine !=null)  )  // We should provide the input to CEP for processing here
		{
			
			int Gyroindex=3; 
			if (numDataPts==12)  // force CEP processing for only 12 sensor data input for now
			{
				String dataPoints[]= csvMessage.split(",");
				cepEngine.getCEPRT().sendEvent(new AcceleroGyro(Double.parseDouble(dataPoints[Gyroindex]),Double.parseDouble(dataPoints[Gyroindex+1]),Double.parseDouble(dataPoints[Gyroindex+2]),Double.parseDouble(dataPoints[Gyroindex-3]),Double.parseDouble(dataPoints[Gyroindex-2]),Double.parseDouble(dataPoints[Gyroindex-1])));
			}
		}

	}

	private void writeData(String  csvMessage) {
		// TODO Auto-generated method stub
		try {
			
			bw.write(csvMessage);
			bw.newLine();
			logger.debug("Records number: " + ++numRecordsWritten + "  Data Writen:  " + csvMessage);
		} catch (IOException e) {
			//e.printStackTrace();
			logger.error("IOException At Edge",e);
		}
		
		
	}


	public void run() {
		// TODO Auto-generated method stub
		long start=0;
		String key="";
		byte[] msg= null;
		
		/* if(bIsRemoteEndpoint)
		{

			while (true)
			{
			

				if (!DecompressorDataMessageHandlerQ.isEmpty()) 
				{
					
					if((lMqttMsgId+1)>255) // Rollover
						lMqttMsgId=0;
						
					MessageFormatterArray.Builder msgArr= DecompressorDataMessageHandlerQ.get(new Integer(lMqttMsgId+1));
					if(msgArr!=null)
					{
						synchronized(inQLock)
						{
							DecompressorDataMessageHandlerQ.remove(new Integer(lMqttMsgId+1));
						}
						
						lMqttMsgId++;
						processDecompressorInput(msgArr);

						
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
				//System.out.println("Exited loop..");
			

			}
		}
		else*/
		{
			while(true)
			//synchronized(this)
			{
				//if(!MQTTTopicQ.isEmpty() && !MQTTMessageQ.isEmpty())
				if (!DataMessageHandlerQ.isEmpty()) 
				{
					
					synchronized(inQLock)
		    		{
						lMqttMsgId++;
						msg=DataMessageHandlerQ.remove();
						
		    		}
					
					if(bIsRemoteEndpoint)
					{
						processDecompressorInput(msg);
					}
					else
					{
						processCompressorInput(msg);
					}
				}
				else
				{
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
			
		
		
	}

}
