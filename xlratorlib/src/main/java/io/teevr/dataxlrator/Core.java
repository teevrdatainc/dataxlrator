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

import io.teevr.config.DataModel;
import io.teevr.config.Model;
import io.teevr.config.Model.DataType;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;
import org.apache.commons.math3.ml.clustering.DoublePoint;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.apache.commons.math3.ml.clustering.evaluation.ClusterEvaluator;
import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;



class Core {

	int sampleSize=50;
	int lowBitSize=5;   
	int highBitSize=26;
	int precisionCap=5;   
	int nRegressionSampleSize=10;
	boolean bEnableLinearCompression=false;
	boolean bEnablePolyCompression=false;
    boolean bIsCompressor=true;
    Logger logger=null;
	
    /* */
    int numHuffLevels=3;
	long[] huffSymbols3={0L,8L,9L,10L,11L,48L,49L,50L,51L,52L,53L,54L,55L}; // Prefix symbols upto three levels
	long[] huffSymbols2={0L,8L,9L,10L,11L}; // Prefix symbols upto two levels
	int rleIdentifier3 = 0x0E;
	int rleIdentifier2 = 0x06;
	// int outofBoundvaluesHeader3= 0x1E;
	int outofBoundvaluesHeader3= 0x7C;  // We'll two bytes for length indication
	 //int outofBoundvaluesHeader2= 0x0E;
	int outofBoundvaluesHeader2= 0x3C;   // We'll two bytes for length indication
	int outofBoundvaluesHeaderNoPfx= 0x08;   // We'll use two bytes for length indication
	int packetendHeader3= 0x1E;  // Use this to indicate end of packet
	int packetendHeader2= 0x0E;  // Use this to indicate end of packet
	int packetendHeaderNoPfx= 0x0E;  // Use this to indicate end of packet
	int packetendHeader=0;
	int packetendHeaderBitSize=0;
	int outofBounderHeader=0;
	 //int outofPfxBoundNumidentifierBits3=5;
	 //int outofPfxBoundNumidentifierBits2=4;
	int outofPfxBoundNumidentifierBits3=7;
	int outofPfxBoundNumidentifierBits2=6;
	int outofPfxBoundNumidentifierBitsNoPfx=4;
	int outofPfxBoundNumidentifierBits=0;
	int rleNumHeaderbits=0; //Number of bits used as RLE identifier
	int rleNumIdentifierbits3=4;
	int rleNumIdentifierbits2=3;
	int rleNumIdentifierbitsNoPfx= 3;  // Used for rle header
	int rleNumSizeBits=0;  // # of bits We'll have three levels depending on size of data points 0-100 : 4 bits 100-2000: 8 bits >2000: 10 bits
    int rleMaxlength=0;
    int rleTriggerLength=0;
    int rleHeader=0;
    int rleHeader3=0x0E;
    int rleHeader2=0x06;
    int rleHeaderNoPfx=0x06;
    int rlemask=-1;
	int rlesizenumbits=0; 
	int shiftedrleheader=0; 
    int rleIndexTable[] = null;  // Assuming max datapoint per sample to be 1000 and we use 8 bits to keep count
	int rleCountTable[] = null;  // Assuming max datapoint per sample to be 1000 and we use 8 bits 
	int currentRLECount=0; // For keeping track of counts for currently indexed datapoint in RLETable
	int currentIndexTobeUpdated=0;
	boolean bUseDeltaForRLE=true;   //Whether to used Deltas as zero for RLE or pfxsymbols as zeros for RLE
	int nZeroNumericDeltaCount=0;    // This is to track number of non zero deltas to see check change in count
	int nZeroDiffCount=0;      //  This to track number of difference in strings
	int bitSzStep=0;
	int numTotalDataPts = 0;  					  // total number of data points in the sample including strings
	int numNumericDataPoints=0;               // This will indicate number of numeric DataPoints includes boolean as well
	int nStringDataPoints=0;               // This will indicate number of Strings DataPoints 
	
	long[]prefixSymbols; //={0,8,9,10,11}; // Prefix symbols upto two levels
	boolean bUsePfxEncoding=true;  // This is to test for volatile datasets using just delta encoding
	boolean bEnableBatch=false;
	int batchsize=1000;
	int currentbatchsize=0; // for tracking batchsize
    String scaleChangedCSV="";  // Used to indicate to cloud instance of change in upward change in scale format: (index,scale)
  	long[] [] sensorDataDelta; //= new ArrayList<long[]> ();
  	int sensorDataUpdateIndex=0;    // used to track update index 
  	List<Map <Long,Long>> listOfPrefixCodes = new ArrayList<Map <Long,Long>>(); // Array to hold prefix code symbols
  	long prevSensorData[]; 
  	int [] dataModelScale;
  	ArrayList<BigInteger> HexStringArr = new ArrayList<BigInteger> (); // Array to hold Hex String Numbers. We can assume that delta will fit within long  	
    // For each column index Regression Array size will be of nRegressionSampleSize
	 ArrayList<long[]> RegressionHistory = new ArrayList<long[]> ();
	 long yArray[];

	 /* String Encoding Decoding variables*/
	  	// For String encoding
	  int stringDataUpdateIndex=0;
	  LRUCache<String,Integer> StringEncodeMap = new LRUCache<String,Integer>(BitUtils.MAX_NUMBER_STRINGS);
	  LRUCache<Integer,String> StringDecodeMap = new LRUCache<Integer,String>(BitUtils.MAX_NUMBER_STRINGS);
	  
	  String prevStringArray[];
	  int encodeStringIndex[];  // index into hashmap
	  int strEncodeNumbits[]; // number of bits required including headers
	  byte encodedString[];  // encoded string output array
	  int numBitsRequiredForString=0; // Update this at the start 
	  int numBitsRequiredForIndex=0;
	  int newStringAddedIndicator= 0xFFFFFFFF;
	 
	    /* End String Encoding/decoding variables*/
	 
	 /* Analytics and Anomaly Detection parameters*/
	  boolean bEnableAnomalyDetection=false;
	  boolean bCollectData=true; // Used to collect numberof bits and wma values to set number of samples and detection thresholds

	  int nAggregatedTotalNumBits=0; // Total number of bits of aggregated delta values to be used on cloud
	  int nAnamolyDetectSamples=500;  // Configurable number of samples
	  ConcurrentLinkedQueue<Double> AggregatedBitsQ =new ConcurrentLinkedQueue<Double>(); // Aggregated compressed bits queue with fixed size
	  List<DoublePoint>DBScanPoints= new ArrayList<DoublePoint>();
	  List<DoublePoint>DBScanDeltaPoints= new ArrayList<DoublePoint>();
	  List<Cluster<DoublePoint>> clusters= new ArrayList<Cluster<DoublePoint>>();
	  List<CentroidCluster<DoublePoint>> cclusters= new ArrayList<CentroidCluster<DoublePoint>>();
	  int nDetectLow=30;  // Configurable Lower range for Anomaly detection  
	  int nDetectHigh=40;  //Configurable Higher range for Anomaly detection
	  int nNumAnamolies=0;
	  int nAdEventValue=-1;  // Anomaly Detect value default -1 to indicate learning phase
	  int nEvent=0; // For tracking same
	  int nDpCounter=0; 
	  double wma=0;
	  double sma=0;
	  double wmabase=0;
	  long varianceCount=0;
	  double maxVal=0;
	  double minVal=0;
	  int currentState=0;
	  int transientState=0;
	  int transientStateCount=0;
	  /* End Analytics and Anomaly Detection parameters*/  

	/**/
	  long lMsgCounter=0;

	  /* Decompressor Additional parameter*/
	  boolean bProcessingCompressedMsg=false;
	  
	  /* DEcompressor additional parameters*/

	  /***/
	  
	  JsonParser parser;
	  Gson gson; 
	  DataModel dataMapperObj=null;  // We need Datamapper Object for dynamic resizing of Arrays and will be null for NonJSON input
	  /*  JSON Array Processing */
	//  SimpleDateFormat dateFormatter=null;
	  Map<String,Core> JsonArrayCoreMap = new ConcurrentHashMap<String,Core>();  // This will store scale for array with its name
	  Map<String,Integer> JsonArrayNameIndexMap = new ConcurrentHashMap<String,Integer>();  // This will store index to be used for array during message formatting/retrieval
	  Map<Integer,String> JsonArrayIndexNameMap = new ConcurrentHashMap<Integer,String>();  // This will store index to be used for array during message formatting/retrieval
      int currentArrayNameIndexToUse=0;	  
	  Map<String,Message> JsonArrayCompressedMsg = new ConcurrentHashMap<String,Message>();  // This will store compressed Message for each array
	  
	// For arrays, we allocate arrays for storing strings and numeric data points upfront
	  // We should keep track of actual count in payload so that we do not send more data than is required to be send due to prellocated buffers
	  long currentStringsCount=0;       
	  long currentNumericDataPointsCount=0;
	  int 	nPrevArraySize=0;
	  /**/
	  
	  /* String Date Formats*/
	  String dateFormats[]={
		"yyyy-MM-dd'T'HH:mm:ss.SSSZ",
		"yyyy-MMM-dd HH:mm:ss.SSSSSS",
		"yyyy-MM-dd HH:mm:ss", 
		"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
		"yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
		"yyyy.MM.dd G 'at' HH:mm:ss z",
		"EEE, MMM d, ''yy",
		"h:mm a",
		"hh 'o''clock' a, zzzz",
		"K:mm a, z",
		"yyyyy.MMMMM.dd GGG hh:mm aaa",
		"EEE, d MMM yyyy HH:mm:ss Z",
		"yyMMddHHmmssZ",
		"YYYY-'W'ww-u"
	  };
	 
	  /* bool Formats*/
	  String boolFormats[][]={
			  {"false","true"},
			  {"False","True"},
			  {"FALSE","TRUE"}
			  };
	 int dateFormatIndex=-1; 
	 
	Core()
	{
		logger= Logger.getLogger(this.getClass());
		parser= new JsonParser();
		gson = new GsonBuilder().setPrettyPrinting().create();
		//dateFormatter=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
	}
	
	void setCompressionParams(int sampleSize, int lowBitSize, int highBitSize,
			int precisionCap , boolean bUsePfxEncoding, boolean bEnableBatch, int batchsize, boolean bIsCompressor) {
		
		this.precisionCap=precisionCap;   
		this.lowBitSize=lowBitSize;   
		this.highBitSize=highBitSize;
		this.sampleSize=sampleSize;
		this.bIsCompressor=bIsCompressor;
		this.bUsePfxEncoding=bUsePfxEncoding;
		this.bEnableBatch=bEnableBatch;
		this.batchsize=batchsize;
		
		
	}
	

	void setDeCompressionParams(boolean bIsCompressor) {
		
		this.bIsCompressor=bIsCompressor;
		
	}

	
	 void setRegressionParams(int nRegressionSampleSize,
				boolean bEnableLinearCompression) {
			
			 this.nRegressionSampleSize=nRegressionSampleSize;
			 this.bEnableLinearCompression=bEnableLinearCompression;
			 yArray= new long[nRegressionSampleSize]; 
		}

	 
	 byte[] getCompressedMsg(String incsvMsg, MessageBuilder msgArr)
	 {
		 return getCompressedMsg(incsvMsg,msgArr,false);
	 }
	 
	byte[] getCompressedMsg(String incsvMsg, MessageBuilder msgArr,boolean bOptimize) {
		
		byte [] msgBytes=null;
		byte[] packedpayload;
		Message msg = null;
        boolean bpackCompressedPayload=false;

		if(bEnableBatch)  // Let's consider batch for csv messages or json without arrays for now
		{
			// Create History and return null till we have process batchsize
			
			if(currentbatchsize<batchsize)
			{
				packedpayload=compressMessage(incsvMsg,bOptimize);
				lMsgCounter++;
				// Update scale 
				if(!scaleChangedCSV.isEmpty())
				{
					msgArr.AddMessage(new Message(BitUtils.SCALE,scaleChangedCSV.getBytes()));
					bpackCompressedPayload=true;
				}
				
				// Update String Data  
				if(prevStringArray!=null) //  it means we have to send string info as well
				{
					if(encodedString!=null)
					{
						msgArr.AddMessage(new Message(BitUtils.STR_PAYLOAD,encodedString));
						bpackCompressedPayload=true;
					}
				}
				
				if(packedpayload==null) 	// we are still learning, combine data as is. Optimize later if two consecutive samples are same
				{
						msg= new Message(BitUtils.RCOMBO,incsvMsg.getBytes());
				}
				else  // we have compressed data now. Optimze for same set of samples
				{
					if(bpackCompressedPayload)
						msg= new Message(BitUtils.COMPRESSED,packedpayload);
					else
						msg=null;
				}

				if(msg!=null)
					msgArr.AddMessage(msg);
                
				currentbatchsize++;
			}

			if(currentbatchsize==batchsize)
			{
				currentbatchsize=0;
				msgBytes=msgArr.toByteArray();
				msgArr.clear();
				return msgBytes;
			}
			else
				return null;
		}

		//logger.info("numeric Datapoints: "+ numNumericDataPoints + ":"+incsvMsg + "DataModel: " + Arrays.toString(dataModelScale));
		if(bOptimize && (incsvMsg==null))  // This means it is an empty array
		{
			packedpayload=null;
			msgBytes=msgArr.toByteArray();
			msgArr.clear();
			return msgBytes; // return message just with array ID
		}
		else
			packedpayload=compressMessage(incsvMsg,bOptimize);
		
		
		lMsgCounter++;
		
		// Update scale 
		if(!scaleChangedCSV.isEmpty())
		{
			/*MessageFormatter.Builder msgBuilder=MessageFormatter.newBuilder();
			msgBuilder.setCommandID(Command.SCALE);
			msgBuilder.setDataPayload(ByteString.copyFrom(scaleChangedCSV.getBytes()));
			msgArr.addMsgFormatter(msgBuilder.build());*/
			msgArr.AddMessage(new Message(BitUtils.SCALE,scaleChangedCSV.getBytes()));
		}
		
		// Update String Data  
		if(prevStringArray!=null) //  it means we have to send string info as well
		{
/*			MessageFormatter.Builder msgBuilder=MessageFormatter.newBuilder();
			msgBuilder.setCommandID(Command.STRPAYLOAD);
			msgBuilder.setDataPayload(ByteString.copyFrom(encodedString));
			msgArr.addMsgFormatter(msgBuilder.build());*/
		//	System.out.println("encoded string size: " + msgBuilder.build().toByteArray().length);
			if(encodedString!=null)
				msgArr.AddMessage(new Message(BitUtils.STR_PAYLOAD,encodedString));
		}
		
		
		/*if(bOptimize && (packedpayload==null)) 
		{
			msg=null;  // No change in payload
		}
		else
		{*/
		if(packedpayload==null) 	// we are still learning, send data as is
		{
			//msgBuilder.setCommandID(Command.RCOMBO);
			//msgBuilder.setDataPayload(ByteString.copyFrom(incsvMsg.getBytes()));
			if(nZeroNumericDeltaCount==numNumericDataPoints)  
			{
				msg= null;  // Do not send any payload
//				logger.info("Zero Delta Message");
			}
			else
			{
				msg= new Message(BitUtils.RCOMBO,incsvMsg.getBytes());
				//logger.info("CSV Message: " +incsvMsg);
			}
		}
		else  // we have compressed data now
		{
//			msgBuilder.setCommandID(Command.COMPRESSED);
	//		msgBuilder.setDataPayload(ByteString.copyFrom(packedpayload));
		    // Start Array optimization only after symbol creation
			if(bOptimize && nZeroNumericDeltaCount == currentNumericDataPointsCount)   // Array size remains same as previous one and no change in value. No need for encoding
			{
				msg=null;
			}
			else
			// check for further optimization
				if(nZeroNumericDeltaCount==numNumericDataPoints)  // there is  no change in value and no need to encode
					msg=null;
				else
					msg= new Message(BitUtils.COMPRESSED,packedpayload);
		}
		//}
		
		// Add Combo/Compressed message to message array
		//msgArr.addMsgFormatter(msgBuilder.build());
		if(msg!=null)
			msgArr.AddMessage(msg);
		
		// if it is an Array and there is no Message in MsgArray, return null to indicate no change in array value
		if (bOptimize && msgArr.getMessageCount()==0)
			return null;
		else
		{
			msgBytes=msgArr.toByteArray();
			msgArr.clear();
			return msgBytes;
		}
	}
	
	 private void updateArraySize(int arrSize) {
			
		 // Array size indicates the number of data points and Strings required update it accordingly
         
		 int numRequiredTotalDatapoints=arrSize* dataMapperObj.getModel().size();
		 int nStringCount=0;
		 
		 if(numTotalDataPts == numRequiredTotalDatapoints)  // We are good. No action required
			 return;
		 
//		 if(numRequiredTotalDatapoints<numTotalDataPts)
		 {
			 // We don't need to resize. Just ensure number of points is updated correctly
			 numTotalDataPts=numRequiredTotalDatapoints;
	         for (int j=0; j<arrSize;j++)
	         	{
	         		
	         	
					for(int i=0;i<dataMapperObj.getModel().size();i++)
					{
						if(dataMapperObj.getModel().get(i).getDataType()==DataType.STRING)
							nStringCount++;
					}
				}
	         
	         initForArrayResize(numTotalDataPts,nStringCount);
	         
		 }
		
	}

	private void initForArrayResize( int numDataPoints, int numStringsCount)
		{
			
			numNumericDataPoints= numDataPoints-numStringsCount;
			nStringDataPoints=numStringsCount;
	
			if(numNumericDataPoints < sensorDataDelta[0].length)
			{
				// No resizing needed
				//logger.info("No resizing required " + sensorDataDelta[0].length);
			}
			else   // Adjust the values now
			{
				listOfPrefixCodes = new ArrayList<Map <Long,Long>>(numNumericDataPoints); // Array to hold prefix code symbols
		        sensorDataDelta = new long[sampleSize][numNumericDataPoints];  // Does not store history so size increase does not require copy
		        if(numNumericDataPoints<4)
		        {
			        rleIndexTable= new int[1];// Does not store history so size increase does not require copy
				   	rleCountTable= new int[1]; // Does not store history so size increase does not require copy
		        }
		        else
		        {
			        rleIndexTable= new int[numNumericDataPoints/4];// Does not store history so size increase does not require copy
				   	rleCountTable= new int[numNumericDataPoints/4]; // Does not store history so size increase does not require copy
		        }
		
			   	prevSensorData= new long[numNumericDataPoints];  // This stores history so needs data to be copied over after resize
			}
	   	
			if(numStringsCount<prevStringArray.length)
			{
				// No resizing required
			}
			else   // Resize and adjust values 
			{
				prevStringArray= new String[nStringDataPoints];  // History is maintained so data needs to be copied during resize
				encodeStringIndex= new int[nStringDataPoints];   // no History
				strEncodeNumbits= new int[nStringDataPoints];    // no History
   
			}
		}
 
	void UpdateDataModelPrecison(boolean bIsArray,DataModel dataModelObj, int initialSz)
	{
		
		 

        if(dataModelObj!=null)
        {
        	int nStringCount=0;
        	int nArrayCount=0;
        	dataMapperObj=dataModelObj;
        	int nscaleUpdateIndex=0;
        	
        	if(bIsArray)
        	{
        		numTotalDataPts=initialSz*dataModelObj.getModel().size();
             	dataModelScale=new int[initialSz*dataModelObj.getModel().size()];

        	}
        	else
        	{
        		numTotalDataPts=dataModelObj.getModel().size();
             	dataModelScale=new int[dataModelObj.getModel().size()];
             	initialSz=1;

        	}

         	for (int j=0; j<initialSz;j++)
         	{
         		
         	
				for(int i=0;i<dataModelObj.getModel().size();i++)
				{
					if(dataModelObj.getModel().get(i).getPrecision()==BitUtils.STRING_PRECISION)
						nStringCount++;
					else
						if(dataModelObj.getModel().get(i).getPrecision()==BitUtils.ARRAY_PRECISION)
							nArrayCount++;

					
					// Sometime Datamodel could be autoderived and in that case the precision may not be correctly set to follow cap. 
					//Ensure it does not go beyond Precision cap
					
					dataModelScale[nscaleUpdateIndex]=dataModelObj.getModel().get(i).getPrecision();
					if(dataModelScale[nscaleUpdateIndex]>precisionCap)
						dataModelScale[nscaleUpdateIndex]=precisionCap;
					nscaleUpdateIndex++;
						
				}
			}

			init(numTotalDataPts,nStringCount,nArrayCount,bIsArray);  // Reduce Array element from total number of Datapoints to get right numeric datapoints
			
			if(!bIsArray)
				logger.info("Low Bits: " + lowBitSize + " High Bits: " + highBitSize + " Precision: " + precisionCap + " UsingPrefix: " + bUsePfxEncoding + " EnableBatch: " + bEnableBatch + " BatchSize: " + batchsize + " Level: " + numHuffLevels);
			//	if(bIsArray)
		//	logger.info("Num Total datapoints :" + numTotalDataPts + " String Count: " + nStringCount  + "dtaamode:" + Arrays.toString(dataModelScale));
        }
	}

	void setLogLevel(Level level) {
		logger.setLevel(level);
		
	}
	
	private void init( int numDataPoints, int numStringsCount, int numArraysCount, boolean bIsArray)
	{
		
		numNumericDataPoints= numDataPoints-numStringsCount-numArraysCount;
		nStringDataPoints=numStringsCount;
		
        listOfPrefixCodes = new ArrayList<Map <Long,Long>>(numNumericDataPoints); // Array to hold prefix code symbols 
        listOfPrefixCodes.clear();
        RegressionHistory.clear();
	  	sensorDataDelta = new long[sampleSize][numNumericDataPoints];
	  	sensorDataUpdateIndex=0;

   	    // populate fields as per hufflevel
   	   if(numHuffLevels==3)
   	   {
   		   prefixSymbols= huffSymbols3;
   		   rleNumHeaderbits=rleNumIdentifierbits3;
   		   rleHeader=rleHeader3;
   		   outofPfxBoundNumidentifierBits=outofPfxBoundNumidentifierBits3;
   		   outofBounderHeader=outofBoundvaluesHeader3;
   		   packetendHeader=packetendHeader3;
   		   packetendHeaderBitSize=5;
   	   }
   	   else  //assume level2 only
   	   {
   		   prefixSymbols= huffSymbols2;
   		   rleNumHeaderbits=rleNumIdentifierbits2;
   		   rleHeader=rleHeader2;
   		   outofBounderHeader=outofBoundvaluesHeader2;
   		   outofPfxBoundNumidentifierBits=outofPfxBoundNumidentifierBits2;
   		   packetendHeader=packetendHeader2;
   		   packetendHeaderBitSize=4;
   	   }
   	   
   	   if(!bUsePfxEncoding)
   	   {
   		   rleNumHeaderbits=rleNumIdentifierbitsNoPfx;
   		   rleHeader=rleHeaderNoPfx;
   		   outofBounderHeader=outofBoundvaluesHeaderNoPfx;
   		   outofPfxBoundNumidentifierBits=outofPfxBoundNumidentifierBitsNoPfx;
   		   packetendHeader=packetendHeaderNoPfx;
   		   packetendHeaderBitSize=4;
   		   numHuffLevels=0;

   	   }
   		   
   	    if(numDataPoints<1000)  // 
   	    	rleNumSizeBits=4; 
   	    else
	    		rleNumSizeBits=10;
   	    
   	    rleMaxlength=(1<<rleNumSizeBits)-1;
   	    
   	    rleTriggerLength=rleNumSizeBits + rleNumHeaderbits;
   	    
   	    rlemask=-1;
   	    rlemask=~(rlemask<<rleNumSizeBits);
   	    rlesizenumbits=rleNumSizeBits+rleNumHeaderbits;
   	    shiftedrleheader=(rleHeader<<rleNumSizeBits);

   	    bitSzStep= (highBitSize-lowBitSize)/3;
   	 
   	 if(numNumericDataPoints<4)
   	 {
	   	 rleIndexTable= new int[1];// Assume 1/4 of numDatapts should be sufficient
	   	 rleCountTable= new int[1];
   	 }
   	 else
   	 {
	   	 rleIndexTable= new int[numNumericDataPoints/4];// Assume 1/4 of numDatapts should be sufficient
	   	 rleCountTable= new int[numNumericDataPoints/4];
   	 }
   	 prevSensorData= new long[numNumericDataPoints];
   	 
   	
   	 
   	 // Initialize String Storage vars
   	 
		if(numStringsCount==0)
		{
			prevStringArray=null;
			encodeStringIndex=null;
			strEncodeNumbits=null;
		}
		else
		{
			prevStringArray= new String[nStringDataPoints];
			encodeStringIndex= new int[nStringDataPoints];
			strEncodeNumbits= new int[nStringDataPoints];
		    int tempspread=BitUtils.MAX_STRING_SIZE;
		    numBitsRequiredForString=1; // Update this at the start 
		    while ((tempspread >>>= 1) != 0) // Major performance issue may exist
		    {
		    	numBitsRequiredForString++;
		    }
		    
		  //  if(!bIsArray)
		   // 	logger.info("Number of bits required for String Storage : " + numBitsRequiredForString + " for Max String size of " + MAX_STRING_SIZE +" bytes.");
		    
		    tempspread=BitUtils.MAX_NUMBER_STRINGS-1;
		    numBitsRequiredForIndex=1;
		    while ((tempspread >>>= 1) != 0) // Major performance issue may exist
		    {
		    	numBitsRequiredForIndex++;
		    }
		    
		}
		
		//bProcessingCompressedMsg=false;
   	 
	}

	private byte[]  compressMessage(String cvsMsg, boolean bOptimize)
	{
		/* Simple filtering of data with same values*/
		byte[] packedData=null;
		//long start= System.nanoTime();
		//logger.info("Numreric:" + numNumericDataPoints + " Total: " + numTotalDataPts);
		/****
		 *  When batch mode is enabled, we need to maintain history of deltas till batchsize
		 *  At every samplesize, we also need to create symbols and need to keep on packing based on symbols created every sample size
		 *  
		 */
		long[] payloadDelta=buildHistory(cvsMsg);
        //logger.info("Build History time: " + (System.nanoTime()-start)/1000 + "  MsgCounter: " + lMsgCounter ); 
		if (payloadDelta !=null)
		{
			// We can start encoding the new samples from now on
		//	start=System.nanoTime();
			packedData=PfxEncodeSensorData(payloadDelta,bOptimize);

		}

		if(prevStringArray!=null)
		{
			
			encodedString=null;
//			logger.info("Zero Diff Count: " + nZeroDiffCount + " Num match:" + (nZeroNumericDeltaCount==currentNumericDataPointsCount) + "Total Current String Points: " + currentStringsCount);
//			if(nZeroDiffCount!=(numTotalDataPts-numNumericDataPoints))
			if(bOptimize && nZeroDiffCount==currentStringsCount) // Array size remains same as previous one and no change in values, No need for encoding
			{
				encodedString=null;
				
			}
			else
				if(nZeroDiffCount==nStringDataPoints)   // For non-Arrays, we expect fixed string count and if there is no change then do not encode
				{
					encodedString=null;
				}
			else
				encodedString= PfxEncodeStringData();
			
			//System.out.println(" Prev String Lenght " + prevStringArray.length);
		}
		// since message has been encode, we can create symbols for encoding next incoming samples
		//logger.info("Total Numeric: " + currentNumericDataPointsCount+"total strinfs: " + currentStringsCount );
		
		if(sensorDataUpdateIndex==sampleSize)
		{
			//start=System.nanoTime();
			createSymbols();
			//logger.info("CreateSymbols: " + (System.nanoTime()-start) + "  MsgCounter: " + lMsgCounter ); 

		}	
		
		   if(bCollectData && bEnableAnomalyDetection)
		   {
		    	//System.out.println(nAggregatedTotalNumBits +"," + nAdEventValue  );
			   System.out.println(nAdEventValue  );
		   }
		   
		return packedData; //message not compressed or is not same as previous message so publish message as is
	}
   
	/* return null if history is not built else return delta array for current sensor data*/
	private long[] buildHistory(String csvMsg)
	{
		
		int startIndex=0;
		int endIndex=0;
		String strToLong="";
		scaleChangedCSV="";
		nZeroNumericDeltaCount=0;
		nZeroDiffCount=0; 
		/* These two variables are useful in keeping track of arrays*/
		currentStringsCount=0;       
		currentNumericDataPointsCount=0;

		/* Build historical data based on unique samples */

		int strArrayIndex=0;
		long payloadDelta[]=null;
		int dataIndex=0;
		int hexStringIndex=0;
		
		long currentDataPoint=0;
		
		if(lMsgCounter>1) // For first sample there is no need for Delta
			payloadDelta=sensorDataDelta[sensorDataUpdateIndex];  // We assume delta values will not exceed 32 bits
  	     //Reset RLECounters
		 //rleIndexTable = new int[1250];  // Assuming max datapoint per sample to be 1000 and we use 8 bits to keep count
		// rleCountTable= new int[1250];
		 currentRLECount=0; // For keeping track of counts for currently indexed datapoint in RLETable
		 currentIndexTobeUpdated=0; 
		// nZeroDeltaCount=0; // Reset for use in next iteration
		// nAggregatedTotalNumBits=0;  // Total number of bits of aggregated delta
		// numBits=new int[numDataPts];  // Reset array for storing bit sizes
		
		 long ret[]=null;

		// long x=stringtoLong("-0.167857512832",5);
		// Use index of instead of splitting 
		// logger.info("numTotalDataPts: " + numTotalDataPts);
		 
		 // iterate through csv values
		 int i=0;
		//for(int i=0;i<numTotalDataPts;i++)  // Iterate through 
		 while(endIndex!=-1)
		{
	 	    
			 
			 if(dataModelScale[i]==BitUtils.ARRAY_PRECISION)  //  If it is a string process it for String Arrays and skip populating into numeric delta Array
				{
					//logger.info("Found Array");
				}
			 else
			 { 
					 // Use substring as it gives better performance over split
		
					endIndex=csvMsg.indexOf(',', startIndex);
		
					if(endIndex==-1)
					{
						strToLong=csvMsg.substring(startIndex);
					}
					else
					{
						strToLong=csvMsg.substring(startIndex,endIndex );
						startIndex=endIndex+1;
					}
					
					
					
					
						if(dataModelScale[i]==BitUtils.STRING_PRECISION)  //  If it is a string process it for String Arrays and skip populating into numeric delta Array
						{
							processString(strToLong,strArrayIndex);
//							logger.info("String Being processed: " + strToLong );
							strArrayIndex++;
							currentStringsCount++;
							
						}
						else
						{
							
							
							if(dataModelScale[i]==BitUtils.HEXSTRING_PRECISION)  //  If it is a hexstring, then put it in Hexstring Array and update difference accordingly
							{
								if(HexStringArr.size()>hexStringIndex)
								{
									HexStringArr.set(hexStringIndex, new BigInteger(strToLong,16));
								}
								else
								{
									HexStringArr.add(new BigInteger(strToLong,16));
								}
								
								currentDataPoint=HexStringArr.get(hexStringIndex).longValue();   // Since we are only using it for delta, we will assume for delta purpose this should fit
								hexStringIndex++;
								currentNumericDataPointsCount++;
								
							}
							else
								if(dataModelScale[i]==BitUtils.BOOLEAN_PRECISION)
								{
									if(strToLong.equalsIgnoreCase("true"))
										currentDataPoint=1L;
									else
										currentDataPoint=0L;
									
									currentNumericDataPointsCount++;
								}
								else
									if(dataModelScale[i]==BitUtils.TIMESTAMP_PRECISION)
									{
										
										try {
											
											int dateFromatIndx=dataMapperObj.getModel().get(i).getTSFormat();
											if(dateFromatIndx==1) // We need custom formatter for this format
											{
												DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(dateFormats[dateFromatIndx]);
												LocalDateTime date = LocalDateTime.parse(strToLong, dateFormatter);
												Timestamp t = Timestamp.valueOf(date);
												currentDataPoint=t.getTime()*1000+((t.getNanos()/1000)%1000); // in micros
											}
											else
											{
												SimpleDateFormat dateFormatter=  new SimpleDateFormat(dateFormats[dateFromatIndx]);
												currentDataPoint=dateFormatter.parse(strToLong).getTime();
											}
											//logger.info(" Messgage ID: " + lMsgCounter + " : " + currentDataPoint);
										} catch (ParseException e) {
											logger.warn("Invalid Timestamp. Resetting Timestamp to zero" );
											currentDataPoint=0;
										}
	
										currentNumericDataPointsCount++;
									}
									else
										///if((dataModelScale[i]!=BitUtils.BOOLEAN_PRECISION))  // Process as number
										{
											if(!bIsCompressor)
											{
							
												currentDataPoint= stringtoLong(strToLong,dataModelScale[i]);
											}
											else
											{
							
												currentDataPoint=stringtoLong(dataIndex,i,strToLong,dataModelScale[i]);
							
											}
											currentNumericDataPointsCount++;
										}
				
							if(lMsgCounter>1)
							{
								//build history for Linear Regression and predict delta
								if(bEnableLinearCompression)
								{
									//Integer yArr[]=ssionHistory.get(i); //Get Regression History for current column/sensor
									int nHistoryArraySize=RegressionHistory.get(i).length;
									if(nHistoryArraySize>=nRegressionSampleSize)
									{
										System.arraycopy(RegressionHistory.get(i), nHistoryArraySize-nRegressionSampleSize, yArray, 0, nRegressionSampleSize);
										long delta1 = (currentDataPoint - predictNextValue(yArray));
										//System.out.println("Build History Predicted Value::  " + predictNextValue(yArray));
										long delta2 =   (currentDataPoint - prevSensorData[i]);
										
										payloadDelta[dataIndex] = Math.abs(delta2) > Math.abs(delta1) ? delta1 : delta2;
										if((payloadDelta[dataIndex]==delta1) && (payloadDelta[dataIndex]!=delta2)) 
											logger.debug("Linear Prediction Data..............." + "Predicted Delta::  " + delta1 + "Actual Delta::" + delta2);
									}
									else
									{
										payloadDelta[dataIndex]=(currentDataPoint-prevSensorData[dataIndex]);
										
										if(bUseDeltaForRLE)
											UpdateRLETable(dataIndex,payloadDelta[dataIndex]);
									}
								}
								else
								{
									payloadDelta[dataIndex]=(currentDataPoint-prevSensorData[dataIndex]);
									if (bUseDeltaForRLE)
										UpdateRLETable(dataIndex,payloadDelta[dataIndex]);
								}
								
								if(payloadDelta[dataIndex]==0)
										nZeroNumericDeltaCount++;	
								
							}
							prevSensorData[dataIndex]=currentDataPoint;
							dataIndex++;				
						}
					
					strToLong=null;
					//if(sensorDataArray.size()>0)
					//if(prevSensorData!=null)
			 }
					i++;
				

		}
		
		if(bIsCompressor && logger.isTraceEnabled())
			logger.trace(" Payload to be sent : " + Arrays.toString(prevSensorData));

		// check if last index is a valid RLE entry or not
		if(bUseDeltaForRLE && numNumericDataPoints>0) // ensure this is update only if there are numeric datapoints
			UpdateRLETable(-1,0);
		
		
		
		boolean bRetDeltaPayload= buildHistory(prevSensorData,payloadDelta);
		if(bRetDeltaPayload)	
        {
        	ret=payloadDelta;
        }
        else
        	ret=null;

		return ret; //Shiv
		
		
	}

	private void processString(String strToLong, int strArrayIndex) {

		if(bIsCompressor)
		{
			Integer strindex=StringEncodeMap.get(strToLong);
			if(strindex==null)  // this string needs to be added as well as to be sent as is
			{
				//logger.info("Entry not found for: " + strToLong + " Size is: " + StringEncodeMap.usedEntries() );
				if(stringDataUpdateIndex<BitUtils.MAX_NUMBER_STRINGS)  //// Send the string as is till we reset the String map at symbol creation boundary
					StringEncodeMap.put(strToLong, new Integer(stringDataUpdateIndex++));
				else
				{
					//logger.info("Skipping index usage due to limit: " + stringDataUpdateIndex);
				}
				strEncodeNumbits[strArrayIndex]=3+numBitsRequiredForString+8*strToLong.getBytes().length;  //  3 bits header '111', num bits for string length and the values byte array
				encodeStringIndex[strArrayIndex]=newStringAddedIndicator;
			}
			else
			{
				encodeStringIndex[strArrayIndex]=strindex;
				strEncodeNumbits[strArrayIndex]=2+numBitsRequiredForIndex;  // 2 bits as header '11' and then numbits bits for index values
			}

			if(strToLong.equals(prevStringArray[strArrayIndex]))
			{
				strEncodeNumbits[strArrayIndex]=1;  
				nZeroDiffCount++;
			}
			
			prevStringArray[strArrayIndex]=strToLong;
			//logger.info("STring Array Size: " + prevStringArray[strArrayIndex] + "Total Datapoitns: " + numTotalDataPts);
		}
		
	}

	private boolean buildHistory(long[] payload, long[] delta)
	{
		
		sensorDataUpdateIndex++;  // update the next updation index now
		if(bEnableLinearCompression)
			buildRegressionHistory(payload);
		return true;
	}

	private void buildRegressionHistory(long[] payload) {
		// TODO Optimize this function
		//System.out.println("Inside building Regression History.................");
		if(logger.isDebugEnabled())
			logger.debug("Inside building Regression History.................");
		if(payload.length!= numNumericDataPoints)
		{
			//System.out.println(" This should not happpen............................");
			logger.error(" buildRegressionHistory: This should not happpen............................");
			return;
		}
		int nRegressionSize= RegressionHistory.size();
		for(int i=0;i<numNumericDataPoints;i++)
		{
			long[] yArray;
			long [] tmpArr;
			if(nRegressionSize!=0) 
			{
				yArray = RegressionHistory.get(i);
				if(yArray.length<nRegressionSampleSize)
				{
					tmpArr= new long[yArray.length+1];
					System.arraycopy(yArray, 0, tmpArr, 0, yArray.length);
					
				}
				else  //if yArray already has the required size, we need to remove first and add the current sample to last index
				{
					tmpArr= new long[nRegressionSampleSize];
					System.arraycopy(yArray, 1, tmpArr, 0, yArray.length-1);
					
				}
				yArray=tmpArr;
				yArray[yArray.length-1]=payload[i];
				RegressionHistory.add(i,yArray);
				
			}
			else  //If there are no entries yet in the array, then create and add
			{
				//Create new array and add to Regression History
				yArray= new long[1];
				yArray[0]=payload[i];
				RegressionHistory.add(yArray);
			}
			
			
			RegressionHistory.add(payload);  // This meeans we'll have same object if payload is global. Relook later
		}
		if(logger.isDebugEnabled())
			logger.debug("Exiting Regression History.................");
		//System.out.println("Exiting Regression History.................");
	}

	// Create Symbols
	private void createSymbols()
	{
		
		
		// Ensure we reset prefix codes
		//listOfPrefixCodes.clear();
		long nStart=0;
		boolean bListEmpty= listOfPrefixCodes.isEmpty();
	//	bListEmpty=true;
		//listOfPrefixCodes.clear();
		// --------------------------------------------------------------
	    // Analysis for delta encoding
	    //System.out.println("Creating Symbols.....");
		
		if(stringDataUpdateIndex==BitUtils.MAX_NUMBER_STRINGS)
		{
			// Reset message Q
			//logger.info("Strings cleared now due to max size limit");
			stringDataUpdateIndex=0;
		//	StringEncodeMap.clear();
		//	StringDecodeMap.clear();
			
			
		}
		if(logger.isDebugEnabled())
			logger.debug("Creating Symbols with total size of : " + sensorDataUpdateIndex );
	    // Calculate the frequency of  each value
	    //long start= System.nanoTime();
	    
		if(bListEmpty)
		{
			// Create prefix codes and add to the list
			//Create as many as is the size of sensorDataDelta Array
			for(int i=0;i<sensorDataDelta[0].length;i++)
			{
				listOfPrefixCodes.add(new  HashMap <Long,Long>(prefixSymbols.length));
			}
			bListEmpty=false;
		}

		if(!bUsePfxEncoding)  // Do not do any prefix encoding
		{
			sensorDataUpdateIndex=0;
			return;
		}
		//Encoding for numeric Values
		for (int index = 0; index < numNumericDataPoints; index++)  
		{
			//start= System.nanoTime();
 		   // Map <Long, Long> sensorDataFreq = new HashMap <Long, Long>();
			 Map <Long,Long> prefixCodes=null;
			 // To avoid creation of Hashmap object, let's reuse
			 if(bListEmpty)
			 {
				 prefixCodes= new  HashMap <Long,Long>(prefixSymbols.length); // Map for prefix codes
			 }
			 else
			 {
				 prefixCodes=listOfPrefixCodes.get(index);
				 prefixCodes.clear();
			 }
			 
 		   Long curVal= new Long(0);
  		  
 	 //start=System.nanoTime();
 		Map <Long, int[]> freq = new HashMap <Long, int[]>(sampleSize);
 	
 		for (int i=0; i<sampleSize; i++)
 		   {
 			  curVal= sensorDataDelta[i][index];
 			   	int[] valueWrapper = freq.get(curVal);
 			    
 				if (valueWrapper == null) {
 					{
 						//long[] newEntry=new long[] { 1,curValArr[0]};
 						//listToSort[j++]=newEntry;
 						freq.put(curVal, new int[] {1});
 					}
 				} else {
 					valueWrapper[0]++;
 				}
 		   }
 		  List<Map.Entry<Long, int[]>> mutablesortedlist = new LinkedList<Map.Entry<Long, int[]>> (freq.entrySet());
		    Collections.sort(mutablesortedlist, new Comparator<Object>() {
				public int compare(Object o1, Object o2) {
					int[] count1=((Map.Entry<Long, int[]>) (o1)).getValue();
					int[] count2=((Map.Entry<Long, int[]>) (o2)).getValue();
					 if( count1[0] < count2[0]) 
					 {
		    	            return -1;
		    	            		    	            
					 } else 
						 if ( count1[0] == count2[0]) 
						 {
							 long val1=((Map.Entry<Long, int[]>) (o1)).getKey();
							 long val2=((Map.Entry<Long, int[]>) (o2)).getKey();
		    	         	 return val1 < val2 ? -1 : 1;
		    	         }
						
						return 1;
				}
			});
		    
		    freq.clear();
		    freq=null;

		 int sortedListLength=mutablesortedlist.size();
    	 for(int i=0;i<sortedListLength;i++)
 		  {
    		 if(i<prefixSymbols.length)
    		 {
    			 long deltaValue= mutablesortedlist.get(sortedListLength-i-1).getKey();
    			if (!bIsCompressor) // If it is a remote endpoint, i need to search for value based on symbol value
    			  prefixCodes.put(prefixSymbols[i],deltaValue);
    			else  // For edge we need to get symbol value based on delat as key
    			   prefixCodes.put(deltaValue,prefixSymbols[i]);
    		 }
    		 else 
    			 break;	 
 		  }
    	 mutablesortedlist.clear();
    	 mutablesortedlist=null;

    	 if(bListEmpty) // Add onlt if it was empty 
	    		listOfPrefixCodes.add(prefixCodes);
	    	//logger.info("Create Symbols Time Each iteration:" + (System.nanoTime()-start)/1000 + "iteration count: " + index);
		}
			// Print Prefixcodes to validate the population in map and arrays
		if(logger.isTraceEnabled())
		{
			 for (int i=0; i<listOfPrefixCodes.size();i++)
			 {
				 Map <Long,Long> pfxcode= listOfPrefixCodes.get(i);
				 
				 logger.trace("Prefix code at index: " + i + "  " + pfxcode.toString());
	
			 }
		}
		
		//logger.info("Create Symbols Time:" + (System.nanoTime()-start)/1000);
		
		sensorDataUpdateIndex=0;
	}

	private byte []  PfxEncodeSensorData(long[] pfxencodedelta, boolean bOptimize)
	{
		
	
		//
		
		// --------------------------------------------------------------
		// Calculate Size and pack the data
	    int [] sizeNumBits; 
	    long [] deltaValue;
	    int totalSizeinBits = 0;
	    int ntotalAggregatedBits=0;
	    
	    //Map <Long,Long> prefixCodes = new  HashMap <Long, Long>(); // Map for prefix codes  
	    
	    //for (int i = 0; i < numColumn[0]; i++) {
	    	//sizeNumBits[i] = 0;
	    //}

	    /* Implement packing side by side as bit widths for each is already known
	    * If the delta value is in Map use the key as symbol value 
	    * for value 0 bit width is 1 bits
	    * for values 8-11, bit width is 4 bits
	    * for values 48-55 bit width is 6 bits
	    * rest values bit width will 16+3= 19 bits  16 for value and 111 as prefix
	    */ 
	    
	    //int endRLEIndex=currentIndexTobeUpdated;
	    int RLEloop=0;
	    int index=0;
	    int compressedindexcntr=0;
	    int rleUpdateIndex=0;
	    int rleCount=0;
	    
	    
	    if(bUsePfxEncoding && listOfPrefixCodes.isEmpty())  // we are not yet ready for encoding
	    	return null;
	    
	    sizeNumBits = new int[numNumericDataPoints];
	    deltaValue = new long[numNumericDataPoints];
	    
	    // Numeric DataPoints is always expected to be more than currentNumericDataPointsCount, so encode only for the required number of datapoints
//	    while(index < numNumericDataPoints)
	    while(index < currentNumericDataPointsCount)
	    {
	    	    // check if this index is RLE candidate
    	    if(bUseDeltaForRLE&& (currentIndexTobeUpdated >=0) && index== rleIndexTable[RLEloop])
    	    {
    	    	// Process RLE 
        		int value=rleCountTable[RLEloop]; 
    		    sizeNumBits[compressedindexcntr] = rlesizenumbits; // rleNumIdentifierbits bits prefix header 1110
    		    deltaValue[compressedindexcntr]=(rlemask&value)| shiftedrleheader;
    	    	RLEloop++;
    	    	if (RLEloop >currentIndexTobeUpdated)
    	    		RLEloop=0; // All valid entries have been processed

    	    	// Update Zero delta count
	    		 // nZeroDeltaCount+=value;
	    		  ntotalAggregatedBits+=value; // We assume 1 bit for zero delta value 
    	    	//logger.info("RLE from index: " + index + "count: " + value + " numbits: " + rlesizenumbits);
    	    	index+=value-1;
    	    	

    	    	
    	    }
	    	else 
	    	{   
	    	    //get prefixcodes map
	    	//	logger.info(" Index in pfx encode: " + index + "Num Numeric Datapoints: " + numNumericDataPoints + " Num Total Datapoints: " + numTotalDataPts);
	    		Map <Long,Long> pfxcode=null;
	    		Long pfxSymbol=null;
	    		if(bUsePfxEncoding)
	    		{
		    		pfxcode= listOfPrefixCodes.get(index);
		    		pfxSymbol= pfxcode.get(pfxencodedelta[index]);
	    		}	
	        	if (bUsePfxEncoding && (pfxSymbol != null)) 
	        	{
	        		deltaValue[compressedindexcntr]=pfxSymbol;
	        		if(pfxSymbol==0)
	        		{	
	        			sizeNumBits[compressedindexcntr] =  1;
	        			if(!bUseDeltaForRLE)
	        			{
		        			rleCount++;
		        			if(rleCount==1)
		        			{
		        				rleUpdateIndex=compressedindexcntr;
		        			}
		        			else
		        			// check we are not more than rlemaxlength
		        			if(rleCount==rleMaxlength)
		        			{
		        				// complete rle encoding
		        				sizeNumBits[rleUpdateIndex] = rlesizenumbits; //rleNumSizeBits+rleNumHeaderbits;
		        				compressedindexcntr=rleUpdateIndex;
		        				//int mask=-1;
		    	    		   // mask=~(mask<<rleNumSizeBits);
		    	    		    deltaValue[compressedindexcntr]=(rlemask&rleCount)| shiftedrleheader;
		    	    		    rleCount=0;
		    	    		  //  rleUpdateIndex=0;
		        			}
	        			}
	        		}
	        		else
	        		{
	        			
	        			if(bUseDeltaForRLE && rleCount>=rleTriggerLength)
	        			{
	        				// complete rle encoding
	        				sizeNumBits[rleUpdateIndex] = rlesizenumbits; //rleNumSizeBits+rleNumHeaderbits;
	        				compressedindexcntr=rleUpdateIndex;
	        				//int mask=-1;
	    	    		    //mask=~(mask<<rleNumSizeBits);
	    	    		    deltaValue[compressedindexcntr]=(rlemask&rleCount)| shiftedrleheader; 
	    	    		    compressedindexcntr++;
	    	    		    deltaValue[compressedindexcntr]=pfxSymbol;
	        			}
	        			
    	    		    rleCount=0;
    	    		    //rleUpdateIndex=0;

	        			
	        			if (pfxSymbol>7 && pfxSymbol<12)
	        					sizeNumBits[compressedindexcntr] = 4;
	        			else 
	        				if (pfxSymbol>47 && pfxSymbol<56)
	        					sizeNumBits[compressedindexcntr] = 6;
	        				else 
	        				{
	    	    		    	logger.error("Symbol not in symbol table.. " + pfxSymbol );
	    	    		    	System.exit(-1);   // fix code here to recover instead if exiting
	        				}
	        		

	        		//System.out.println("DeltaValue In Range: " + Integer.toBinaryString(DeltaValue[index]));	
	        		}
	        		
	        		pfxSymbol=null;
	        	}
	    		
	        	else 
	        	{
	    		    long header= 0x07;  // populate the header

	    		    // calculate number of bits required
	        		long tempSpread=Math.abs(pfxencodedelta[index]);
	        		int bits=0;
	        		int numberOfCalculatedbits=2; /* We need one extra bit for signed representation*/
	        		
        			if(bUseDeltaForRLE && rleCount>=rleTriggerLength)
        			{
        				// complete rle encoding
        				//DeltaValue[compressedindexcntr+1]=DeltaValue[compressedindexcntr];
        				sizeNumBits[rleUpdateIndex] = rlesizenumbits; //rleNumSizeBits+rleNumHeaderbits;
        				compressedindexcntr=rleUpdateIndex;
        				//int mask=-1;
    	    		    //mask=~(mask<<rleNumSizeBits);
    	    		    deltaValue[compressedindexcntr]=(rlemask&rleCount)| shiftedrleheader; 
    	    		    compressedindexcntr++;
        			}
        			
	    		    rleCount=0;
	    		    //rleUpdateIndex=0;

	    		    while ((tempSpread >>>= 1) != 0) // Major performance issue may exist
	    		    {
	    		    	numberOfCalculatedbits++;
	    		    }
	    		    
	    		    // Second level of optimization to use only 16 or 32 bits for out of range values
	    		    if(numberOfCalculatedbits<=lowBitSize)
	    		    {
	    		    	bits=lowBitSize;
	    		    	//header= 0x1E;
	    		    	//header= 0x0E;
	    		    	header=outofBounderHeader;
	    		    }
	    		    else if(numberOfCalculatedbits>lowBitSize && numberOfCalculatedbits<=(lowBitSize+bitSzStep))
	    		    {
	    		    	bits=lowBitSize+bitSzStep;
	    		    	//header= 0x1F;
	    		    	//header= 0x0F;
	    		    	header=outofBounderHeader+1;
	    		    }
	    		    else if(numberOfCalculatedbits>(lowBitSize+bitSzStep) && numberOfCalculatedbits<=(lowBitSize+2*bitSzStep))
	    		    {
	    		    	bits=lowBitSize+2*bitSzStep;
	    		    	//header= 0x1F;
	    		    	//header= 0x0F;
	    		    	header=outofBounderHeader+2;
	    		    }
	    		    else if(numberOfCalculatedbits>(lowBitSize+2*bitSzStep) && numberOfCalculatedbits<=highBitSize)
	    		    {
	    		    	//bits=lowBitSize+3*bitSzStep;
	    		    	bits=highBitSize;
	    		    	//header= 0x1F;
	    		    	//header= 0x0F;
	    		    	header=outofBounderHeader+3;
	    		    }
	    		    else
	    		    {
	    		    	//System.out.println("Number of bits exceeded estimated bits..............." + numberOfCalculatedbits + " value: " + delta[index] );
	    		    	logger.error("Number of bits exceeded support " + highBitSize + " bits. Required number of bits is: " + numberOfCalculatedbits + " value: " + pfxencodedelta[index] + " index: "+ index + "Message: " + lMsgCounter);
	    		    	//bits=highBitSize;
	    		    	//header= 0x1F;
	    		    	System.exit(-1);   // fix code here to recover instead if exiting
	    		    }
	    		    //sizeNumBits[index] = bits+3+2; // 3 bits prefx header 111 and 2 bits size 00,01,10,11 for 4,8,12,16 bits
	    		    sizeNumBits[compressedindexcntr] = bits+outofPfxBoundNumidentifierBits; // 3 bits prefix header 111 and 1 bits size 0 for 10 and 1 for 16 bits
	    		    
	    		   // header= 0x1C | ((bits/4)-1);  // populate the header
	    		    
	    		    long mask=-1;
	    		    mask=~(mask<<bits);
	    		    deltaValue[compressedindexcntr]=(mask&pfxencodedelta[index])| (header<<bits); 
	    		    if(!bUsePfxEncoding)
	    		    {
	    		    	if(pfxencodedelta[index]==0)
	    		    	{
	    		    		
//	    		    		logger.info("Delta Zero at Index :" + compressedindexcntr);
	    		    		deltaValue[compressedindexcntr]=0;
	    		    		sizeNumBits[compressedindexcntr]=1;
	    		    	}
	    		    }
	    		  //  logger.info("index: "+ index + "Msg: " + lMsgCounter + "Bits: "+ bits+ "Value: " + pfxencodedelta[index]);
	    		   // System.out.println(" Number of bits: " + bits + " value: " + delta[index]);
	        	}
	        	
	    	    // Tosimplify analytics calculations, we will use bit size as one for delta values as zeros
	    	       if(pfxencodedelta[index]==0)
	    	       {
	    	    	   ntotalAggregatedBits++; // total number of aggregated bits
	        	    //   numBits[index]=1;

	    	       }else
	    	       {
	    	    	   ntotalAggregatedBits+=sizeNumBits[compressedindexcntr]; // total number of aggregated bits
	    	    	  //numBits[index]=sizeNumBits[compressedindexcntr];
	    	       }
	        	
	    }
	        	//totalSizeinBits+=sizeNumBits[compressedindexcntr];

	        	compressedindexcntr++;
	        	index++;   // Increment the count
 		    }
	   
	    
		if(bUseDeltaForRLE && rleCount>=rleTriggerLength)
		{
			// complete rle encoding
			sizeNumBits[rleUpdateIndex] =rlesizenumbits; // rleNumSizeBits+rleNumHeaderbits;
			compressedindexcntr=rleUpdateIndex;
			//int mask=-1;
		   // mask=~(mask<<rleNumSizeBits);
		    deltaValue[compressedindexcntr]=(rlemask&rleCount)| shiftedrleheader; 
		    compressedindexcntr++;
		}
		
		
		// get totalsizeinbits
		totalSizeinBits=5;  // Add 5 bits for end identifier header 0x 1E
		for (int i = 0; i < compressedindexcntr; i++) 
			totalSizeinBits+=sizeNumBits[i];
		
	    // Pack the delta values
	    long[] tmpCompressedBlock = new long[1+(totalSizeinBits >>> 6)];
	    //int[] tmpCompressedBlock = new int[compressedindexcntr];
	    
	    int offset=0;
	    for (int i = 0; i < compressedindexcntr; i++) 
	    {
	    	BitUtils.writeBits(tmpCompressedBlock,deltaValue[i],offset,sizeNumBits[i]);
	    	 offset+=sizeNumBits[i];
	    }
	    
	    //Add the end identifier
	    
	    BitUtils.writeBits(tmpCompressedBlock,packetendHeader,offset,packetendHeaderBitSize);
	    
	    deltaValue=null;
	    sizeNumBits=null;
	    
   /*    String binaryString="";
       for(int i=0;i<tmpCompressedBlock.length;i++)
    	   binaryString+= Long.toBinaryString(tmpCompressedBlock[i]);
       logger.info("Message ID: " + lMsgCounter + "Binary: " + binaryString); 	
*/
	       
	    byte[] ret=BitUtils.longArrayTobyteArray(tmpCompressedBlock,totalSizeinBits);;
	    tmpCompressedBlock=null;

	   detectAnomaly(ntotalAggregatedBits);
	    return ret;
	}	

	private byte[] PfxEncodeStringData() {
		// We have to use prefix encoding now
		
		int totalNumBits=3;  // We need three bits '110' as end of string indicator
	//	for(int i=0; i<strEncodeNumbits.length;i++)
		for(int i=0; i<currentStringsCount;i++)
		{
			totalNumBits+=strEncodeNumbits[i];
		}
		// do the packing
		// Pack the delta values
	    long[] tmpCompressedBlock = new long[1+(totalNumBits >>> 6)];
	    
	    int offset=0;
	    //currentStringsCount is expected to be less than allocated array size. We'll process only the required number for current number of strings
//	    for (int i = 0; i < prevStringArray.length; i++)
	    for (int i = 0; i < currentStringsCount; i++) 
	    {
	    	//
	    	if(strEncodeNumbits[i]==1) // No change in value. Just add 0
	    	{
	    		BitUtils.writeBits(tmpCompressedBlock,0,offset,1);
	    	    offset+=1;
//	    	    System.out.println("No Change");
	    	}
	    	else
	    		if(encodeStringIndex[i]==newStringAddedIndicator) // send string as is
	    		{
	    			// use the value from String Array
	    			byte[] byteArr=prevStringArray[i].getBytes();
	    			//write the header first
	    			long header= 0x06; // bin 110
	    			long value= byteArr.length;
   				    long mask=-1;
	    		    mask=~(mask<<numBitsRequiredForString);
	    		    value=(mask&value)| (header<<(numBitsRequiredForString));
	    		    //System.out.println("Header Binary for :"+ prevStringArray[i] + "= " + Long.toBinaryString(value));
	    		    BitUtils.writeBits(tmpCompressedBlock,value,offset,numBitsRequiredForString+3);
	    		    offset+=numBitsRequiredForString+3;
	    		    for(int j=0;j<byteArr.length;j++)
	    		    {
	    		    	BitUtils.writeBits(tmpCompressedBlock,byteArr[j],offset,8);
	    		    	offset+=8;
	    		    }
	    		    //System.out.println("String Encoded" + prevStringArray[i]);
	    		}
	    		else  // send the index into map
	    		{
	    			long header= 0x02; // bin 10
	    			long value= encodeStringIndex[i];
   				    long mask=-1;
	    		    mask=~(mask<<numBitsRequiredForIndex);
	    		    value=(mask&value)| (header<<numBitsRequiredForIndex); 
	    		    BitUtils.writeBits(tmpCompressedBlock,value,offset,strEncodeNumbits[i]);
	    		    offset+=strEncodeNumbits[i];
	    		    //System.out.println("Index:" + encodeStringIndex[i]);
	    		}
	    		
	    	 
	    }
	    
	    // add the end indicator
	    long endindicatorHeader= 0x07;   // bin 111
	    BitUtils.writeBits(tmpCompressedBlock,endindicatorHeader,offset,3);
/*
		  String binaryString="";
	       for(int i=0;i<tmpCompressedBlock.length;i++)
	    	   binaryString+= Long.toBinaryString(tmpCompressedBlock[i]);
	       logger.info("Binary: " + binaryString);*/
	       
	    byte[] ret=BitUtils.longArrayTobyteArray(tmpCompressedBlock,totalNumBits);

		
		return ret;
	}

private void detectAnomaly(double nAggregatedTotalNumBits) {
	// We need to collect 64 samples for compressed data before analysing for anamoly
	// check for Q size
	//int nNormalizedMovingAvg=0;
	
	if(!bEnableAnomalyDetection)
		return;
	if(AggregatedBitsQ.size()<nAnamolyDetectSamples)
	{
		AggregatedBitsQ.add(nAggregatedTotalNumBits);
		if(AggregatedBitsQ.size()==nAnamolyDetectSamples)
		{
			// Calculate sma, wma
			Iterator<Double> it = AggregatedBitsQ.iterator();
	        int index=1;
	        double currentDp=0;
	        double sumwma=0;
	        double sumsma=0;
	        while(it.hasNext())
	        {
	        	currentDp=it.next();
	        	sumsma+=currentDp;
	        	sumwma+=(currentDp*index)/nAnamolyDetectSamples;
	        	index++;
	        }
	        //System.out.println("Index:" + index);
	        sma=sumsma/nAnamolyDetectSamples; // moving average
	        wma=sumwma/nAnamolyDetectSamples;  // weighted moving average
		}
	}
	else  // sample size if equal to nAnamolyDetectSamples
	{
		// rmeove the oldest entry
		double oldestdp=AggregatedBitsQ.poll();
		AggregatedBitsQ.add(nAggregatedTotalNumBits);
		//update sma
		//update wmav wmas
		
		wma=(wma + ((nAggregatedTotalNumBits-sma)/nAnamolyDetectSamples));
	//	wmav=(wmav-smac + nAggregatedTotalNumBits);
		sma= sma + ((nAggregatedTotalNumBits-oldestdp)/nAnamolyDetectSamples);
        if(wma>nDetectHigh)
        {
        	if(nEvent==0)	
        	{
        //		if(!bCollectData)
        	//		logger.info("Anomaly Detected-M :: " + nNumAnamolies++ + "  wma: "+ wma);
        		nEvent=1;
        		//nAdEventValue=100;
        	}
          
        }
        else	
        	if(wma<nDetectLow)
        	{
        		if(nEvent==1) // If transitioning from moving
        		{
        		
        		//	if(!bCollectData)
        			//	logger.info("Anomaly Detected-S :: " + nNumAnamolies++ + "  wma: "+ wma );
        			nEvent=0;
        			//nAdEventValue=50;
        		}
        		
        	}
//        if(bCollectData)  // Ensure Log level is set to info to avoid seeing mix of messages in logs
 //       	System.out.println(nAggregatedTotalNumBits +"," + (wma-sma) );
	}
	
    double[] point = new double[1];
    point[0]=wma;
   // point[0]=prevSensorData[3];
   // point[1]=prevSensorData[4];
    //point[2]=prevSensorData[5];
    
    
    DBScanPoints.add(new DoublePoint(point));
    
    if(DBScanPoints.size()>10000 && clusters.isEmpty())
    {
      
    	//System.out.println("Creating Clusters..");
      
    boolean bDone=false;
    double eps=.35;
    int minpoints=500; 
    double epsStep=.05;
    int minPtsSteps=100;

		//System.out.println("Clusters being created using  eps: "+ eps + " minpts: "+minpoints  );
		
		EuclideanDistance distMeasure= new EuclideanDistance();
	    DBSCANClusterer<DoublePoint> dbscan = new DBSCANClusterer<DoublePoint>(eps, minpoints,distMeasure);
	    
	    
	    List<Cluster<DoublePoint>> cluster = dbscan.cluster(DBScanPoints);
	    
	    
	    long totalPoints=0;
	    for(Cluster<DoublePoint> c: cluster){
	        //System.out.println("Num Points DBSCAN: " + c.getPoints().size() + " Point at index 0: " + c.getPoints().get(0));
	        // get the average value in each cluster
	        List<DoublePoint> points=c.getPoints();
	           
	        if(points.size()>=500)
	        {
	        	clusters.add(c);
	        	
		        double sum=0;
		        double pts[]= new double[points.size()];
		        for (int x = 0; x < points.size(); x++) {
					sum=sum+points.get(x).getPoint()[0];
					pts[x]=points.get(x).getPoint()[0];
		        }
		        
		        wmabase=sum/c.getPoints().size();
		        minVal=StatUtils.min(pts);
		        maxVal=StatUtils.max(pts);;
		        //System.out.println("Num Points DBSCAN: "+ c.getPoints().size() + "Average : " + sum/c.getPoints().size());
		     //   System.out.println("Num Points DBSCAN: "+ c.getPoints().size() + "Max : " + maxVal + " Min: "+ minVal);
	        }
	        
	        totalPoints+=c.getPoints().size();
	    } 
	    //System.out.println("DB Clusters Created: " + cluster.size() + " Total Points: " + totalPoints );

    
	    //KMeansPlusPlusClusterer<DoublePoint> kmeans= new KMeansPlusPlusClusterer<DoublePoint>(1);  // use only one cluster
	     //cclusters = kmeans.cluster(clusters.get(0).getPoints());
	    
	     /*for(Cluster<DoublePoint> c: ccluster){
	       // System.out.println("Num Points K-means: " + c.getPoints().size() + " Point at index 0: " + c.getPoints().get(0));
	        // get the average value in each cluster
	        List<DoublePoint> points=c.getPoints();
	        double sum=0;
	        for (int i = 0; i < points.size(); i++) {
				sum=sum+points.get(i).getPoint()[0];
	        }
		  System.out.println("Num Points K-means: "+ c.getPoints().size() + "Average : " + sum/c.getPoints().size());		
	    }*/

	    
    
     // We'll try model creation from eps of .05 in steps of .05 to .25 and minpoint from 1000 in steps of 500 to 5000 
   // while(!bDone)  // We have to look for optimal number fo clusters to fit the current model
  //  {
 /*   	for( double i=eps;i<=.35;i+=epsStep)
    	{
    		for(int j=minpoints;j<=1000;j+=minPtsSteps)
    		{
    			System.out.println("Clusters being created using  eps: "+i + " minpts: "+ j );
    			
    			//EuclideanDistance distMeasure= new EuclideanDistance();
			    DBSCANClusterer<DoublePoint> dbscanloop = new DBSCANClusterer<DoublePoint>(i, j);
			    
			    
			    List<Cluster<DoublePoint>> clusterloop = dbscanloop.cluster(DBScanPoints);
			    
			    long totalPointsloop=0;
			    for(Cluster<DoublePoint> c: clusterloop){
			        //System.out.println("Num Points DBSCAN: " + c.getPoints().size() + " Point at index 0: " + c.getPoints().get(0));
			        // get the average value in each cluster
			        List<DoublePoint> points=c.getPoints();
			        
			        double sum=0;
			        for (int x = 0; x < points.size(); x++) {
						sum=sum+points.get(x).getPoint()[0];
			        }
			        System.out.println("Num Points DBSCAN: "+ c.getPoints().size() + "Average : " + sum/c.getPoints().size());	
			        totalPointsloop+=c.getPoints().size();
			    } 
			    System.out.println("DB Clusters Created: " + clusterloop.size() + " Total Points: " + totalPointsloop );
    		}
    	}*/
    //}
   /* KMeansPlusPlusClusterer kmeans= new KMeansPlusPlusClusterer(3);
    cluster = kmeans.cluster(DBScanPoints);

    for(Cluster<DoublePoint> c: cluster){
       // System.out.println("Num Points K-means: " + c.getPoints().size() + " Point at index 0: " + c.getPoints().get(0));
        // get the average value in each cluster
        List<DoublePoint> points=c.getPoints();
        double sum=0;
        for (int i = 0; i < points.size(); i++) {
			sum=sum+points.get(i).getPoint()[0];
        }
	  System.out.println("Num Points K-means: "+ c.getPoints().size() + "Average : " + sum/c.getPoints().size());		
    }
    System.out.println("K-means Clusters Created: " + cluster.size());
    */
    DBScanPoints.clear();
    }
    else
    {
    	if(!clusters.isEmpty())  // cluster is not empty evaluate if the point falls in this cluster
    	{
    		//DoublePoint pts= cclusters.getCenter();
           // if(bCollectData)  // Ensure Log level is set to info to avoid seeing mix of messages in logs
            {
            	    if(wma>=minVal && wma<=maxVal)
            	    {
//            	    	System.out.println(nAggregatedTotalNumBits +",  0"  );
            	    	if(currentState==1)
            	    	{
//            	    		transientState=0;
            	    		transientStateCount++;
            	    		if(transientStateCount>5000)
            	    		{
            	    			currentState=0;
            	    			transientStateCount=0;
            	    		}
            	    	}
            	    	else
            	    		transientStateCount=0;
            	    }
            	    else
            	    {
            	    	if(currentState==0)
            	    	{
//            	    		transientState=0;
            	    		transientStateCount++;
            	    		if(transientStateCount>50)
            	    		{
            	    			currentState=1;
            	    			transientStateCount=0;
            	    		}
            	    	}
            	    	else
            	    		transientStateCount=0;
  //          	    	System.out.println(nAggregatedTotalNumBits +",  1"  );
            	    }
            	    
            	    nAdEventValue=currentState;
                	//System.out.println(nAggregatedTotalNumBits +", " + Math.abs(wma-wmabase) );
            }

    	}
    }

 

}

// Assumes String does not have spaces  
private  long stringtoLong( String s, int scale )
{
    // Check for a sign.
    long num  = 0;
    long sign = -1;
    int len  = s.length();
    int indx=0;
    int decIndx=-1;
    char ch;
    int exponent;
    
    // check for exponent notation. This is the least intrusive change for now.
    ch  = s.charAt(0);
    if ( ch == '-' )
        sign = 1;
    
    while(indx<len)
    {
    	ch=s.charAt(indx);
    	if(ch=='.')
    	{
    		decIndx=indx;
    	}
    	else
    	{
    		if (ch=='e' || ch=='E')
    		{
    			int startIndx=0;
    			StringBuilder expStr= new StringBuilder();
    			exponent=Integer.parseInt(s.substring(indx+1, len));
				if(sign==1)
				{
					expStr.append("-");
					startIndx=1;
				}
    			if(exponent<0)
    			{
    				int zeroCounts=Math.abs(exponent)-decIndx;
    				expStr.append(".");
    				for(int i=0;i<zeroCounts;i++)
    					expStr.append("0");
       				if(sign==1)
    				{
    					expStr.append("0");  // in case on negative numbers decIndx is one more than positive numbers
    				}

    			}
    			
				expStr.append(s.substring(startIndx, decIndx)); // append upto decimal
				expStr.append(s.substring(decIndx+1,indx));
				
				if(exponent>0)
				{
					//
					int zeroCounts=Math.abs(exponent)-(indx-decIndx-1);
	   				for(int i=0;i<zeroCounts;i++)
    					expStr.append("0");
 
				}
				s=expStr.toString();
//				System.out.println(s);
    			break;
    		}
    	}
    	indx++;
    }
    
    len  = s.length();
    ch  = s.charAt(0);
    if ( ch == '-' )
        sign = 1;
    else
        num = '0' - ch;

    // Build the number.
    int i = 1;
    int multiplierScale=0;
    
    if(scale<0)
    	multiplierScale=Math.abs(scale);
    
    int j=0; //track scale
    boolean decFound=false;
    boolean expFound=false;
   
    while ( i < len )
    {
    	ch=s.charAt(i);
    	if(ch=='.')
    	{
    		decFound=true;
    	}
    	else
    		if (ch=='e' || ch=='E')
    		{
    			expFound=true;
    			j=0;
    			break;
    		}
	    	else
	    	{
	    		if(scale>=0 && decFound) j++;
	    		if((scale < 0)  || (scale >=0 && j<=scale))
	    			num = num*10 + '0' - ch;
	    		
	    	}
    	i++;
    }
    

    s=null;
    // we need to adjust to the scale as well. 
    //Handling for exponent for float is not yet handled. This handling is only for long/int 
    //values in exponent form
    while((scale>=0 && j<scale) || (expFound && j<multiplierScale))  
    {
    	num = num*10;
    	j++;
    }
    return sign * num;
} 

// Assumes String does not have spaces  
private  long stringtoLong( int index, int dataModelIndex, String s, int scale )
{
    // Check for a sign.
    long num  = 0;
    long sign = -1;
    int len  = s.length();
    int indx=0;
    int decIndx=-1;
    char ch;
    int exponent;
    
    // check for exponent notation. This is the least intrusive change for now.
    ch  = s.charAt(0);
    if ( ch == '-' )
        sign = 1;
    
    while(indx<len)
    {
    	ch=s.charAt(indx);
    	if(ch=='.')
    	{
    		decIndx=indx;
    	}
    	else
    	{
    		if (ch=='e' || ch=='E')
    		{
    			int startIndx=0;
    			StringBuilder expStr= new StringBuilder();
    			exponent=Integer.parseInt(s.substring(indx+1, len));
				if(sign==1)
				{
					expStr.append("-");
					startIndx=1;
				}
    			if(exponent<0)
    			{
    				int zeroCounts=Math.abs(exponent)-decIndx;
    				expStr.append(".");
    				for(int i=0;i<zeroCounts;i++)
    					expStr.append("0");
       				if(sign==1)
    				{
    					expStr.append("0");  // in case on negative numbers decIndx is one more than positive numbers
    				}

    			}
    			
				expStr.append(s.substring(startIndx, decIndx)); // append upto decimal
				expStr.append(s.substring(decIndx+1,indx));
				
				if(exponent>0)
				{
					//
					int zeroCounts=Math.abs(exponent)-(indx-decIndx-1);
	   				for(int i=0;i<zeroCounts;i++)
    					expStr.append("0");
 
				}
				s=expStr.toString();
//				System.out.println(s);
    			break;
    		}
    	}
    	indx++;
    }
    
    len  = s.length();
    ch  = s.charAt(0);
    if ( ch == '-' )
        sign = 1;
    else
        num = '0' - ch;

    // Build the number.
    int i = 1;
    int multiplierScale=0;
    
    if(scale<0)
    	multiplierScale=Math.abs(scale);
    
    int j=0; //track scale
    boolean decFound=false;
    boolean expFound=false;
   
    while ( i < len )
    {
    	ch=s.charAt(i);
    	if(ch=='.')
    	{
    		decFound=true;
    	}
    	else
    		if (ch=='e' || ch=='E')
    		{
    			expFound=true;
    			j=0;
    			break;
    		}
	    	else
	    	{
	    		if(scale>=0 && decFound) j++;
	    		//if((scale < 0)  || (scale >=0 && j<=scale))
	    		
	    		
	    		if(j<=precisionCap)
	    			num = num*10 + '0' - ch;
	    		else 
	    		{
	    			j--;
	    			break;
	    		}	    	

	    		
	    	}
    	i++;
    }
    

    
    // we need to adjust to the scale as well. 
    //Handling for exponent for float is not yet handled. This handling is only for long/int 
    //values in exponent form
    while((scale>=0 && j<scale) || (expFound && j<multiplierScale))  
    {
    	num = num*10;
    	j++;
    }
	

   // logger.info("str:" + s + "num: "+ num + "scale: " + scale + "j:" + j);
    if(scale>=0 && j<=precisionCap && j>scale )
    {
    	updateScale(index,dataModelIndex,j);
    }
    return sign * num;
} 


private void updateScale(int index, int dataModelIndex, int scale)
{
	long multiplier= (long)Math.pow(10, scale-dataModelScale[dataModelIndex]);
	//Update Previous  value
	if(prevSensorData!=null)
		prevSensorData[index]=prevSensorData[index]*multiplier;
	//Update Scale and Precision
	//dataMapperObj.getModel().get(index).setPrecision(scale);
	//dataModelPrec[index]=(long) Math.pow(10,dataModelObj.getModel().get(index).getPrecision());
	dataModelScale[dataModelIndex]=scale;
	scaleChangedCSV=scaleChangedCSV + index + "," + dataModelIndex +"," +scale + ",";
//	logger.info("Scale Change Notification Msg:" + scaleChangedCSV  + "index: " + index + "Msg: " + lMsgCounter);
}

private void UpdateRLETable(int index, long delta) {
	// TODO Auto-generated method stub
	
	if(index==-1)
	{
		// At the end of processing the sample,the check if last entry is valid, 
		// else decrement the count to track end of valid RLE entries  
		if (rleCountTable[currentIndexTobeUpdated]<=rleTriggerLength)  // addiitonal 4 bits used in prefix identifier
			currentIndexTobeUpdated--;
			
		return;
	}
	if(delta==0)  // IF delta is zero, then this is potential to start tracking and keeping count
	{
		if(currentRLECount==0)
			rleIndexTable[currentIndexTobeUpdated]=index;
		currentRLECount++;
		
		rleCountTable[currentIndexTobeUpdated]=currentRLECount;
		
		if(currentRLECount==rleMaxlength)
		{
			currentIndexTobeUpdated++;
			currentRLECount=0;
		}

	}
	else
	{
		if (currentRLECount>rleTriggerLength)   // used  4 bits as identifier 
		{
				rleCountTable[currentIndexTobeUpdated]= currentRLECount;	
				//logger.info("Message ID: " + lMsgCounter+ " RLE Count from index: " + RLEIndexTable[currentIndexTobeUpdated] + " is:" + currentRLECount);
				currentIndexTobeUpdated++;
		}
		currentRLECount=0;

	}
			
}
// ---------------------------------------------------------------
// Utility to predict next value based on previous N samples
// ---------------------------------------------------------------
private long predictNextValue(long yArr[]) {
	double sumx = 0.0, sumy = 0.0;
	int N= yArr.length;
    for (int n=0; n<N; n++) {
        sumx  += n;
        sumy  += yArr[n];
    }
    double xbar = sumx / N;
    double ybar = sumy / N;

    // second pass: compute summary statistics
    double xxbar = 0.0, xybar = 0.0;
    for (int n = 0; n < N; n++) {
        xxbar += (n - xbar) * (n - xbar);
        xybar += (n - xbar) * (yArr[n] - ybar);
    }
    double beta1 = xybar / xxbar;
    double beta0 = ybar - beta1 * xbar;
    double preY  = beta1*N + beta0;
    
    // print results
    // System.out.println("y   = " + beta1 + " * x + " + beta0);
    return  (long) preY;
}

	void processStringPayload(byte[] byteArrayPayload) {
	
	// process String Payload to populate the values
	  int offset=0;
      int packidentifieroffset=0;
      int bits=0;
      long packidentifier=-1;  // pack identifiers could be 0, 10, 110
      long packedSymbolVal=0;
      long packidentifierNumBits=0;
	  int index=0;   
	  boolean bDone=false;
	  long payloadszinbits= 8*byteArrayPayload.length;
	  long payload[] = BitUtils.byteArrayTolongArray( byteArrayPayload);
	  currentStringsCount=0;
/*		  String binaryString="";
       for(int i=0;i<payload.length;i++)
    	   binaryString+= Long.toBinaryString(payload[i]);
       logger.info("Binary: " + binaryString + " Lenght: " + prevStringArray.length); 	
 */     
//	  while(index<prevStringArray.length)
	  while(!bDone)
	    {
		  
		// get the pack identifier first
			packidentifieroffset=offset;
			packidentifierNumBits=0;
			packidentifier=-1;
			
			while(packidentifierNumBits<3 && packidentifier!=0)
			{
				packidentifier=BitUtils.readBits(payload,packidentifieroffset,1);
				packidentifierNumBits++;
				packidentifieroffset++;
			}
			if(packidentifierNumBits==1)  //Handler for '0' level
			{
				packedSymbolVal=packidentifier;
				offset++;
				currentStringsCount++;
			}
			else
				if(packidentifierNumBits==2)  //Handler for '10' level
				{
					packedSymbolVal=packidentifier;
					// retrive the string from Index
					packedSymbolVal=BitUtils.readBits(payload,offset+2,numBitsRequiredForIndex);
					prevStringArray[index]=StringDecodeMap.get(new Integer((int)packedSymbolVal));
					offset+=numBitsRequiredForIndex+2;
					currentStringsCount++;
					///logger.info(prevStringArray[index]);
				}
				else
					if(packidentifierNumBits==3)  //Handler for '110' level 110 is string as is 111 is end identifier
					{
						packedSymbolVal=packidentifier;
						if(packedSymbolVal==1)
						{
							bDone=true;  // all the bits have been processed
						}
						else
						{
							// Get the string length
							packedSymbolVal=BitUtils.readBits(payload,offset+3,numBitsRequiredForString);
							offset+=numBitsRequiredForString+3;
							// create a new byte array
							byte strVal[]= new byte[(int)packedSymbolVal];
							for(int j=0;j<packedSymbolVal;j++)
							{
								strVal[j]=(byte)BitUtils.readBits(payload,offset,8);;
								offset+=8;
							}
							
							String str= new String(strVal);
							// Add to DecodeQ
							StringDecodeMap.put(new Integer(stringDataUpdateIndex++), str);
							prevStringArray[index]=str;
							currentStringsCount++;
						}
						
						// no change in Previous String Array
						///logger.info(prevStringArray[index]);
					}

		  index++;
	    	
	    }
	
}	  


	void processScaleChange(byte[] scaleChangedByteArray) {
		String scaleIndexValue[]=new String(scaleChangedByteArray).split(",");
		int i=0;
		while(i<scaleIndexValue.length)
		{
			int indexToUpdate=Integer.parseInt(scaleIndexValue[i++]);
			int dataModelIndexToUpdate=Integer.parseInt(scaleIndexValue[i++]);
			int updatedScale= Integer.parseInt(scaleIndexValue[i++]);
			updateScale(indexToUpdate,dataModelIndexToUpdate,updatedScale);
		}
		if(logger.isDebugEnabled())
			logger.debug("Scale CSV: " + new String(scaleChangedByteArray));
		// we have got the changed scale values 
		return;
	}
	
	String processComboMessage(byte[] comboMsgbyteArray) {

		String inmsg="";
		inmsg= new String(comboMsgbyteArray);
		/* if combo message is received after the processing of compressed message, we should restart over*/
		if(bProcessingCompressedMsg)
		{
			bProcessingCompressedMsg=false;
			listOfPrefixCodes.clear();
			sensorDataUpdateIndex=0;
			lMsgCounter=0;
			for (int i=0;i<prevSensorData.length;i++)
			{
				prevSensorData[i]=0;
			}

//			System.out.println("Resetting to Combo");
			RegressionHistory.clear();
		}

		buildHistory(inmsg); //build history
		
		if(sensorDataUpdateIndex==sampleSize)
			createSymbols();

		lMsgCounter++;
		//logger.info("Combo Message:" +inmsg);
		return inmsg;
	}
	
	String processCompressedMessage(byte[] compressedMsgByteArray) {
		
		// Received compressed message
		long payloadDelta[];
		int hexstringUpdateIndex=0;
		if(logger.isTraceEnabled())
			logger.trace("Received payload for processing..........");

		if(compressedMsgByteArray == null)
		{
			payloadDelta= sensorDataDelta[sensorDataUpdateIndex];

			for(int i=0;i<numNumericDataPoints;i++)
				payloadDelta[i]=0;
		}
		else
		{
			bProcessingCompressedMsg=true;
			payloadDelta=decompressPfxMessage(compressedMsgByteArray);
		}
		
	//	logger.info("numeric: " + numNumericDataPoints);
		//for (int i=0;i<numNumericDataPoints;i++)
		for (int i=0;i<currentNumericDataPointsCount;i++)
		{
			prevSensorData[i]= prevSensorData[i] + payloadDelta[i];
			
			if(dataModelScale[i]==BitUtils.HEXSTRING_PRECISION)  // Update Hex value
			{
				// Update the HexString Array
				BigInteger prevHexVal=HexStringArr.get(hexstringUpdateIndex);
				BigInteger currHexVal=prevHexVal.add(BigInteger.valueOf(payloadDelta[i]));
				//logger.info("HexString at " + hexstringUpdateIndex + ":" + currHexVal.toString(16));
				HexStringArr.set(hexstringUpdateIndex, currHexVal);
				hexstringUpdateIndex++;
			}
		}
		
		buildHistory(prevSensorData,payloadDelta);
		
		if(sensorDataUpdateIndex==sampleSize)
			createSymbols();
		
		lMsgCounter++;
	 	return getFormattedCSVString(prevSensorData);
	
	}
	
	private  String getFormattedCSVString(long payload[])
	{
		StringBuilder outString=new StringBuilder();
		String separator="";
		int strUpdateIndex=0;
		int numUpdateIndex=0;
		int hexstringUpdateIndex=0;
		long currentTotalDataPoints= currentNumericDataPointsCount+currentStringsCount;
	 //   logger.info(Arrays.toString(dataModelScale));	
	   // for(int i=0;i<numTotalDataPts;i++)
	    //for(int i=0;i<currentTotalDataPoints;i++)
	    long processedCount=0;   // We just need to process valid datapoints only
	    //logger.info(" Current Numeric: " + currentNumericDataPointsCount + " Current Strins:  " + currentStringsCount);
	    for(int i=0;processedCount<currentTotalDataPoints;i++)
		{
			// check for boolean
			if(dataModelScale[i]==BitUtils.ARRAY_PRECISION)
			{
				// skip processing for arrays
			}
			else
				if(dataModelScale[i]==BitUtils.HEXSTRING_PRECISION)
				{
					outString.append(separator + HexStringArr.get(hexstringUpdateIndex).toString(16));
					hexstringUpdateIndex++;
					processedCount++;
					numUpdateIndex++;
					
				}
				else
					if(dataModelScale[i]==BitUtils.BOOLEAN_PRECISION)
					{
						//boolean val=true;
						int val=1; // for true
						if(payload[numUpdateIndex++]==0)
							val=0; // for false
						String boolArr[]=boolFormats[dataMapperObj.getModel().get(i).getBoolFormat()];
						
						outString.append(separator + boolArr[val]);
						processedCount++;
					}
					else
						if(dataModelScale[i]==BitUtils.TIMESTAMP_PRECISION)	// This is a Timestamp
		    			{
							String converetdTS;
							int dateFormatIndx=dataMapperObj.getModel().get(i).getTSFormat();
							if(dateFormatIndx==1)
							{
								long l=payload[numUpdateIndex++];
								int micros=(int)(l%1000000);
								DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss.SSSSSS");
								Timestamp converted= new Timestamp(l/1000);
								converted.setNanos(micros*1000);
								LocalDateTime ldt2 = converted.toLocalDateTime();
								converetdTS=ldt2.format(dateFormatter);
							}
							else
							{
								SimpleDateFormat dateFormatter=  new SimpleDateFormat(dateFormats[dataMapperObj.getModel().get(i).getTSFormat()]);
								converetdTS= dateFormatter.format(new Date(payload[numUpdateIndex++]));
							}
							//logger.info("Msg ID:" + lMsgCounter + " TS: " + payload[numUpdateIndex-1]);
		    				outString.append(separator + converetdTS);
		    				processedCount++;
		    			}
						else
							if(dataModelScale[i]==BitUtils.STRING_PRECISION)	// This is a string
			    			{
			    				outString.append(separator + prevStringArray[strUpdateIndex++]);
			    				processedCount++;
			    			}
							else
							{
				    			outString.append(separator+getScaledValue(payload[numUpdateIndex++],dataModelScale[i]));
				    			processedCount++;
							}
	    	separator=",";

		}
	    return outString.toString();
       // return StringUtils.join(outString,",");  //Use Apache utility;
	}
	
	String getScaledValue(long value,int scale)
	{
		String scaledVal="";
		int length=0;
		
		scaledVal=String.valueOf(value);
		length=scaledVal.length();
		if(scale>0)
		{
			int sign=0;
			if (scaledVal.charAt(0) == '-' )
			{
		        sign = 1;
		        scaledVal=scaledVal.substring(1);
		        length=scaledVal.length();
			}
			
			if(scale>=length)
			{
				for(int i=0;i<=scale-length;i++)
					scaledVal="0"+scaledVal;
			}

			if(sign==1)
				scaledVal="-"+scaledVal;

			length=scaledVal.length();
			// trim trailing zeros Optimize this later
			int numZeros=0;
			String trimZeros=scaledVal.substring(length-scale);
			char ch=trimZeros.charAt(trimZeros.length()-1);
			
			while(ch=='0')
			{
				numZeros++;
				if(numZeros<scale) //  We can not have zeros more than scale
				{
					ch=trimZeros.charAt(trimZeros.length()-numZeros-1);
				}
				else
					break;
			}
			
		    if(numZeros == trimZeros.length())
		    	scaledVal= scaledVal.substring(0, length-scale);
		    else
		    	scaledVal= scaledVal.substring(0, length-scale)+"."+scaledVal.substring(length-scale,length-numZeros);
		}
		return scaledVal;
	}

	private  long [] decompressPfxMessage(byte[] message)
	{
		
		long delta[]= sensorDataDelta[sensorDataUpdateIndex];
		
		long payload[] = BitUtils.byteArrayTolongArray( message);
        int offset=0;
        int packidentifieroffset=0;
        int bits=0;
        long packidentifier=-1;  // pack identifiers could be 0, 10, 110
        long packedSymbolVal=0;
        int packidentifierNumBits=0;
        int ntotalAggregatedBits=0; // recreate the number of compressed bits so that it is not required to be sent
        long payloadSzinBits= 8* message.length;
	    //get prefixcodes map
		Map <Long,Long> pfxcode=null;
	    //long pfxSymbol;
		boolean bDone=false;
        boolean bIsnegative=false;

        if(logger.isTraceEnabled())
        {
	       String binaryString="";
	       for(int i=0;i<payload.length;i++)
	    	   binaryString+= Long.toBinaryString(payload[i]);
	       logger.trace("Message ID: " + lMsgCounter + "Binary: " + binaryString); 	
        }
        
        currentNumericDataPointsCount=0;
	    int index=0;   
		
	  //  while(index<numNumericDataPoints)
	    //while(offset<payloadSzinBits)
	    while(!bDone)
		{
			
			// get the pack identifier first
			packidentifieroffset=offset;
			packidentifierNumBits=0;
			packidentifier=-1;
			
			while(packidentifierNumBits<6 && packidentifier!=0 && packidentifieroffset<payloadSzinBits)
			{
				packidentifier=BitUtils.readBits(payload,packidentifieroffset,1);
				packidentifierNumBits++;
				packidentifieroffset++;
			}


			if (packidentifier!=0 && (packidentifieroffset<payloadSzinBits))
				packidentifierNumBits=-1; 
			
			
			if((packidentifierNumBits ==packetendHeaderBitSize))  // check if it is end  
				//if(packidentifierNumBits ==4)  //Handler for "11110" level  Not a Huffman but for 8 bits RLE length
				{
					bDone=true;
				}
			else
			{
				if(bUsePfxEncoding)
					pfxcode= listOfPrefixCodes.get(index);
				if(logger.isTraceEnabled())
				{
					if(pfxcode !=null)
						logger.trace("index: " + index + "pfxcode" + pfxcode.toString());
				}
					if(packidentifierNumBits ==1)  //Handler for '0' level
					{
						if(bUsePfxEncoding)
						{
							packedSymbolVal=packidentifier;
							offset++;
							if(pfxcode.get(packedSymbolVal)==null)
								logger.error("  This is not expected remote and edge history out of sync...................." + " Message ID: " + (lMsgCounter) + " Index: " + index + "SymbolValue: " + packedSymbolVal + " ID bits:" + packidentifierNumBits);
							delta[index]= pfxcode.get(packedSymbolVal);
						}
						else
						{
							delta[index]= 0;
							offset++;
						}
						ntotalAggregatedBits++;
						currentNumericDataPointsCount++;	
					}
					else
						if(bUsePfxEncoding && packidentifierNumBits ==2)  // Handler for "1" level
						{
							packedSymbolVal=BitUtils.readBits(payload,offset,4);
							offset=offset+4;
							if(pfxcode.get(packedSymbolVal)==null)
								logger.error("  This is not expected remote and edge history out of sync...................." +  " Message ID: " + (lMsgCounter)+ " Index: " + index + "SymbolValue: " + packedSymbolVal + " ID bits:" + packidentifierNumBits);
							delta[index]= pfxcode.get(packedSymbolVal);
							
							if(delta[index]==0)  // We count as one bit for all deltas on edge
								ntotalAggregatedBits++;
							else
								ntotalAggregatedBits+=4; // 4 bits for value
							currentNumericDataPointsCount++;
		
						}
						else
							if((numHuffLevels==3) && packidentifierNumBits ==3) // Handler for "11" level
							{
								packedSymbolVal=BitUtils.readBits(payload,offset,6);
								offset=offset+6;
								if(pfxcode.get(packedSymbolVal)==null)
									logger.error("  This is not expected remote and edge history out of sync...................." +  " Message ID: " + (lMsgCounter)+ " Index: " + index + "SymbolValue: " + packedSymbolVal+ " ID bits:" + packidentifierNumBits);
								delta[index]= pfxcode.get(packedSymbolVal);
								
								if(delta[index]==0)  // We count as one bit for all deltas on edge
									ntotalAggregatedBits++;
								else
									ntotalAggregatedBits+=6; // 6 bits for value
								
								currentNumericDataPointsCount++;
		
							}
							else
							if(packidentifierNumBits ==rleNumHeaderbits) 
								//if(packidentifierNumBits ==4)  //Handler for "111" level  Not a Huffman but for 8 bits RLE length
								{
									  //processing for RLE
									//offset+=4;
									offset+=rleNumHeaderbits;
									packedSymbolVal=BitUtils.readBits(payload,offset,rleNumSizeBits);   //rleTriggerLength bits for RLE length size
									offset=offset+rleNumSizeBits;
									ntotalAggregatedBits+=packedSymbolVal; // For Rle, we use count as bit size 
									if(logger.isTraceEnabled())
										logger.trace("index: " + index + "RLE Length: " + packedSymbolVal);
									for(int j=0; j<packedSymbolVal; j++)
									{
										
										if(bUseDeltaForRLE)
										{
											delta[index]= 0;
										}
										else
										{
											pfxcode= listOfPrefixCodes.get(index);
											delta[index]= pfxcode.get(0);
										}
										currentNumericDataPointsCount++;
										index++;
										
									}
									index=index-1;
								}
							else
							{
								int payloadSz=0;
								//bits=readBits(payload,offset+4,1);
								bits=(int)BitUtils.readBits(payload,offset+outofPfxBoundNumidentifierBits-2,2);
								
								if(bits<3)
									payloadSz=lowBitSize+bits*bitSzStep;
								else
									payloadSz=highBitSize;
								
								//payloadSz=(bits==0?lowBitSize:highBitSize); //Bit sizes are 16 and 32
								//offset+=5;
								offset+=outofPfxBoundNumidentifierBits;
								packedSymbolVal=BitUtils.readBits(payload,offset,payloadSz);
								
								bIsnegative = (BitUtils.readBits(payload,offset,1)==1);
								offset=offset+payloadSz;
								delta[index]= packedSymbolVal;
								currentNumericDataPointsCount++;
								if(delta[index]==0)
								{
									ntotalAggregatedBits++;
								}
								else
								{
									// if we are here means the value could not be represented within pfx encoding and we are receiving raw difference
									ntotalAggregatedBits+=payloadSz+outofPfxBoundNumidentifierBits; // For Rle, we use count as bit size
		
								}
									
								if(bIsnegative)
									delta[index]= (delta[index] | (0xffffffffffffffffL<<payloadSz));
								//if(lMsgCounter==1409 && index==476)
									
									//logger.info("Value: " + delta[index]);
							}
								
					index++; 
					}
		}
		
	    detectAnomaly(ntotalAggregatedBits);
	    nAggregatedTotalNumBits=ntotalAggregatedBits;
	    
		//If we have linear regression we should correct to actual delta values here
		if(bEnableLinearCompression)
		{
			for (int i=0;i<numNumericDataPoints;i++)
			{
				
				int nHistoryArraySize=RegressionHistory.get(i).length;
				if(nHistoryArraySize>=nRegressionSampleSize)
				{
					System.arraycopy(RegressionHistory.get(i), nHistoryArraySize-nRegressionSampleSize, yArray, 0, nRegressionSampleSize);
					long predictedValue=predictNextValue(yArray);
					long actualpayload=0;
					if(logger.isDebugEnabled())
						logger.debug("Decompressed Fun Predicted Value::  " + predictedValue);
					// Since most of our delta is not from predicted value, Assume actual delta to be from previous sensor instead of predicted
					long deltaPr=  (delta[i]-(predictedValue-prevSensorData[i]));
					if(Math.abs(delta[i])  <  Math.abs(deltaPr)) // Our assumption is correct
					{
						if((delta[i]==0) && (predictedValue==0))  //hackish way
							actualpayload=0;
						else
						   actualpayload=delta[i]+prevSensorData[i];
					}
					else
					{
						if((delta[i]==0) && (predictedValue==0))  //hackish way
							actualpayload=0;
						else
						actualpayload=delta[i]+ predictedValue;
					}
					if(logger.isDebugEnabled())
						logger.debug("Decompressed Fun Predicted Value::  " + predictedValue + " Payload: " + actualpayload);

					delta[i]= (actualpayload-prevSensorData[i]);
				}
			}
		}
		return delta;
	}

	 double[] getAdValues() {
		
		 double [] values= new double[3];
		 values[0]=nAggregatedTotalNumBits;
		 values[1]=wma;
		 values[2]=nAdEventValue;
		// nAdEventValue=0;
		return values;
	}


	 void  setJsonArrayModel(String Keyname, DataModel dataModelObj, int initialArrSz)
	 {
		 /* Create Core object to handle the Array entries*/
		 if(dataModelObj!=null && JsonArrayCoreMap.get(Keyname)==null)
		 {
			  Core arrayCore= new Core();
			  arrayCore.setCompressionParams(sampleSize,lowBitSize,highBitSize,precisionCap,bUsePfxEncoding,bEnableBatch,batchsize,bIsCompressor);
			  arrayCore.setRegressionParams(nRegressionSampleSize,bEnableLinearCompression);
			  arrayCore.UpdateDataModelPrecison(true,dataModelObj,initialArrSz);
			  arrayCore.setLogLevel(logger.getLevel());
			  JsonArrayCoreMap.put(Keyname, arrayCore);
			 // logger.info("Processor added for " + Keyname);
		 }
		 
		  if(JsonArrayNameIndexMap.get(Keyname) == null)
		  {
//			  logger.info("Array name " + Keyname + " Index: " + currentArrayNameIndexToUse);
			  JsonArrayNameIndexMap.put(Keyname, new Integer(currentArrayNameIndexToUse));
			  JsonArrayIndexNameMap.put(new Integer(currentArrayNameIndexToUse), Keyname);
			  currentArrayNameIndexToUse++;
		  }
		  //else
			//  logger.info("Array name " + Keyname + " Index: " + JsonArrayNameIndexMap.get(Keyname));
		  
		  

	 }
	 
	 int getPreviousArraySize()
	 {
		 return nPrevArraySize;
	 }
	 
	 void setPreviousArraySize( int sz)
	 {
		 nPrevArraySize=sz;
	 }
	 
	 boolean isDataModelProcessed( String arrName)  //  Helper function to check presence of datamodel at edge
	 {
		 return JsonArrayCoreMap.get(arrName)==null?false:true;
	 }
	 Message  compressArrayMessage(String Keyname, String incsvMsg ,int arrSize)
	 {
		 
		 Core compressor= JsonArrayCoreMap.get(Keyname);
		 
		 if(compressor ==null) // There is no data model yet for this array
			  return null;
		 
		 MessageBuilder msgArr= new MessageBuilder();
		 msgArr.setSequenceNum(JsonArrayNameIndexMap.get(Keyname));  // seqID/Index of Array
		// logger.info("Compressing.." + incsvMsg + "Array Name: " + Keyname + "Array size: " + arrSize);
		// compressor.updateArraySize(arrSize);
		 boolean bOptimize=false;
		 if((arrSize == compressor.getPreviousArraySize()) || (incsvMsg==null) )
			 bOptimize=true;
			 
		 byte compressedArr[]=compressor.getCompressedMsg(incsvMsg, msgArr,bOptimize);  
		 compressor.setPreviousArraySize(arrSize);
		 
	//	 logger.info("Compressed.." + incsvMsg);
		 if(compressedArr ==null)  // No change in array values
		 {
			 return null;
		 }
		 else
			 return (new Message(BitUtils.ARR_UPDATE,compressedArr));
		 
		 
	 }
	 


	String getArrayName(int index)
	 {
		 return JsonArrayIndexNameMap.get(new Integer(index));
	 }
	 Core  getArrayProcessor(String arrName)
	 {
		 return JsonArrayCoreMap.get(arrName);
		 
	 }
	 

	private Model getMapper(String name, String value)
		{
			Model mapper= new Model();
			
			if(name !=null)  // will be null if we have to send 
			{
				mapper.setName(name);
				mapper.setIsMetaData(false);
			}
			
			int scaleVal=0;
			if(value.isEmpty())
				return null;
			try
			{
				scaleVal=getScale(value);
				if(name!=null)
					mapper.setDataType(DataType.NUMBER);

				mapper.setPrecision(scaleVal);

			}
			catch(NumberFormatException e)
			{
				// check if it is a boolean or string
				if(value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false"))
				{
					mapper.setDataType(DataType.BOOLEAN);
					mapper.setPrecision(BitUtils.BOOLEAN_PRECISION);
					mapper.setBoolFormat(0); // Set to default "true"/"false"
					for(int j=0;j<boolFormats.length;j++)
					{
						String boolArr[]=boolFormats[j];
						if(value.equals(boolArr[0]) || value.equals(boolArr[1]))
						{
							mapper.setBoolFormat(j);
							break;
						}
					}
				}
				else
				{
					// check if this is a timestamp
					//GH timestamp format is "yyyy-MM-dd'T'HH:mm:ss.SSSz"
					int dateFormatIndx=-1;
					for (int i=0;i<dateFormats.length;i++)
					{
						Date ret=parseDate(value,dateFormats[i]);
						if(ret!=null)
						{
							dateFormatIndx=i;
							break;
						}
					}
				
					if(dateFormatIndx!=-1)
					{
						mapper.setDataType(DataType.TIMESTAMP);
						mapper.setTSFormat(dateFormatIndx);
						mapper.setPrecision(BitUtils.TIMESTAMP_PRECISION); // Indicate it is a timestamp
						logger.info(name + " Auto-Detected as Timestamp with format as: " + dateFormats[dateFormatIndx]);

					}
					else
					{
						
						// Do a final check to see if this is a hex string
						try
						{
							BigInteger hexnum= new BigInteger(value,16);
							mapper.setDataType(DataType.HEXSTRING);
							mapper.setPrecision(BitUtils.HEXSTRING_PRECISION); // Indicate it is a hexstring and to be treated separately
							logger.info(name + " Auto-Detected as Hexstring.");

						}catch(NumberFormatException hexnume)
						{
							mapper.setDataType(DataType.STRING);
							mapper.setPrecision(BitUtils.STRING_PRECISION); // Indicate it is a string and to be treated separately
						}

					}
				}
			}
			return mapper;
		}

	private Date parseDate(String value, String dateFormat)
	{
		
		SimpleDateFormat dateFormatter=  new SimpleDateFormat(dateFormat);
		Date ret=null;
		try {
			ret=dateFormatter.parse(value);
		} catch (ParseException e1) {
			ret=null;
		}
		return ret;
	}
		private int getScale(String value) 
		{
			BigDecimal num= new BigDecimal(value);
			int scaleVal=num.scale();
			if(scaleVal>precisionCap)
				scaleVal=precisionCap;
			num=null;
			return scaleVal;
		}
		 DataModel generateDataMapperfromJSON(String inputmsg) {
		    
			 
		    // Check whether jsonElement is JsonObject or not
			ArrayList<Model> DataMapperArray=new ArrayList<Model>();
			JsonElement jsonElement=parser.parse(inputmsg.trim());
		    updateDataMapperArray(DataMapperArray,jsonElement,"");
		  
		    DataModel mapper= new DataModel();
		    mapper.setModel(DataMapperArray);
		    return mapper;
		}
		
		private void updateDataMapperArray(ArrayList<Model> dataMapperArray,
				JsonElement jsonElement, String strKey) {

			if (jsonElement.isJsonObject()) {
				

		        Set<Entry<String, JsonElement>> ens = ((JsonObject) jsonElement).entrySet();
		        if (ens != null) {
		        	
		        	
		            // Iterate JSON Elements with Key values
		            for (Entry<String, JsonElement> en : ens) {

		            	if(en.getValue().isJsonObject())
		            	{
		            		//System.out.println(en.getKey());
		            		if(!strKey.isEmpty())
		            			updateDataMapperArray(dataMapperArray,en.getValue(),strKey +"_"+ en.getKey());
		            		else
		            			updateDataMapperArray(dataMapperArray,en.getValue(),en.getKey());
		            	}
		            	else
		            		if(en.getValue().isJsonArray())
		            		{
		            			JsonArray arr=(JsonArray) en.getValue();

		            			if(arr.size()>0)  // If array size is not zero then only model can be created. else model should be sent later when their is any entry avaliable
		            				processJSONArrayModel(en.getKey(),arr);
		            			else
		            			{
		            				// process null array to store the index of this array for process the model later
		            				processJSONArrayModel(en.getKey(),null);  // Call this to just update the index map
		            			}
		            			/*for(int i = 0; i < arr.size(); i++)
		            			{
		                    		if(!strKey.isEmpty())
		                    			updateDataMapperArray(dataMapperArray,arr.get(i),strKey +"_"+en.getKey()+"."+ i);
		                    		else
		                    			updateDataMapperArray(dataMapperArray,arr.get(i),en.getKey()+"."+ i);

		            			}*/
		            			/* Update Model to indicate it is an Array and processing has to be done separately*/
		            			Model currentModel=new Model();
		            			currentModel.setName(en.getKey());
		            			currentModel.setPrecision(BitUtils.ARRAY_PRECISION);
		            			currentModel.setIsMetaData(false);
				             	if(currentModel!=null)
				             		dataMapperArray.add(currentModel);

		            		}
		            		else
				            	{
					            	Model currentModel=null;
				            		if(strKey.isEmpty())
				            			currentModel=getMapper(en.getKey(),en.getValue().getAsString());
				            		else
				            			currentModel=getMapper(strKey +"_"+en.getKey(),en.getValue().getAsString());
					            	
					             	if(currentModel!=null)
					             		dataMapperArray.add(currentModel);
				            	}
		            }
		            
		        }
		  
		       
		    }
			else  // if this is primitve data type
			{
            	Model currentModel=null;
        		
        		currentModel=getMapper("",jsonElement.getAsString());
        		
             	if(currentModel!=null)
             		dataMapperArray.add(currentModel);
        	}
		    
			
		}
		
		private void processJSONArrayModel(String keyName,JsonArray arr) {

			// Update the ArraySize Hashmap for detection of change in size.
			//JsonArraySizeMap.put(keyName, new Integer(arr.size()));
			// Create Datamodel for the array now, since there will always
			generateDataMapperfromJSONArray(keyName,arr);
		}


		private void generateDataMapperfromJSONArray(String keyName,JsonArray arr) {
			
			
			DataModel mapper=null;
			if(arr!=null)
			{
				ArrayList<Model> JSONArrayMapper=new ArrayList<Model>();
				updateDataMapperArray(JSONArrayMapper,arr.get(0),"");
			  
			    mapper= new DataModel();
			    mapper.setModel(JSONArrayMapper);
			}
			
				
			//setJsonArrayModel(keyName,mapper,arr.size());
		   setJsonArrayModel(keyName,mapper,BitUtils.MAX_JSON_ARRAY_SIZE);  // Allocate memory for Arrays at the begining
		}

		Message updateArrayDataModel(String keyName,JsonArray arr)
		{
			processJSONArrayModel(keyName,arr);
			if(bIsCompressor)
			{
	            byte[] arrString= gson.toJson(arr).getBytes();
	            
	            byte [] arrpayload= new byte[1+arrString.length];
	            
	            //logger.info(arrString);
	            arrpayload[0]=(byte)JsonArrayNameIndexMap.get(keyName).intValue();
	            // populate json string byte array
	            for(int i=0;i<arrString.length;i++)
	            {
	            	arrpayload[i+1]=arrString[i];
	            }
	            
	            return new Message(BitUtils.ARR_MODEL_UPDATE,arrpayload);
			}
			else 
				return null;

		}

		DataModel generateDataMapperfromCSV(String inputmsg) {
		    
			 
		    // Check whether jsonElement is JsonObject or not
			ArrayList<Model> DataMapperArray=new ArrayList<Model>();
			int startIndex=0;
			int endIndex=0;
			int i=0;
			///String CSVElements[]= inputmsg.split(",");
		    //
		    while(endIndex!=-1) 
		    {
		        // Use substring as it gives better performance over split
		    	endIndex=inputmsg.indexOf(',', startIndex);
		    	if(endIndex==-1)
		    	{
		    		Model currentModel=null;
		   			currentModel=getMapper("s"+i,inputmsg.substring(startIndex));
		         	if(currentModel!=null)
		         		DataMapperArray.add(currentModel);
		    	}
		    	else
		    	{
		    		Model currentModel=null;
		   			currentModel=getMapper("s"+i,inputmsg.substring(startIndex,endIndex));
		         	if(currentModel!=null)
		         		DataMapperArray.add(currentModel);
		    		startIndex=endIndex+1;
		    	}
		    	i++;
		    }
		    
		    DataModel mapperObj= new DataModel();
		    mapperObj.setModel(DataMapperArray);
		    
		    return mapperObj;
		}



}
