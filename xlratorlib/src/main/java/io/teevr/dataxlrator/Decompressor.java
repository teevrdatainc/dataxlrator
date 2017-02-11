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
import io.teevr.dataxlrator.LicenseValidator.LicenseType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonReader;

/**
* <h1>Decompress DataXlrator compressed  binary</h1>
*  Decompressor provides functions for decompressing DataXlrator compressed input.
* <p>
* <p>
*  <b>Example:</b> The sample code below is illustration of DataXlrator's Compressor and Decompressor usage. This code has to be run with continuous data 
*  stream to get benefit of DataXlrator. Each compressor and decompressor should have one to one mapping with same data stream. Compressor/Decompressor object of one data stream 
*  cannot be used with any other data stream.     
* <pre>
*     String csvinput= "123,3,4,5,6,7,8";
*     
*     // Create compressor to compress the above string
*     Compressor compressor= new Compressor();
*     byte compressedOutput[]= compressor.compress(csvinput);
*     
*     // Create decompressor to decompress the binary to String
*      Decompressor decompressor= new Decompressor();
*      String decompressedOutput= decompressor.decompress(compressedOutput);
*      
* </pre>
* <b>Note:</b> Since DataXlrator is streaming based compression, different types of data inputs should have corresponding decompressor.
* e.g. If there are two json streams with different schema's, there should be two compressor/decompressor pairs. Similarly, if there are two csv streams 
* with different numbers of values, there should be two compressor/decompressor pairs. Developers should ensure one to one mapping between compressor/decompressor 
* for different data sources, data source types and data source schemas.
*
* @author  Teevr Data Inc
* @version 1.0
* @since   05-Aug-2016
* @see	Compressor
*/

public class Decompressor {
	
	Core xlratorCore;
	boolean bIsCompressor=false;
	Logger logger=null;
	int expectedSequenceNum=0;
	
	/**
	 * for internal use only
	 * @deprecated
	 * 
	 *
	 */
	/*public static enum DataFormat {
	    AUTO,
		CSV, 
	    JSON
	};*/
	int outputFormat= DataFormat.AUTO;
	// We need this Q for storing messages out of order and processing later. Ideally this should be empty always if messages are received in order
	//Map<Integer,MessageFormatterArray.Builder> DecompressorDataMessageHandlerQ = new ConcurrentHashMap<Integer,MessageFormatterArray.Builder>();
	Map<Integer,MessageParser> DecompressorDataMessageHandlerQ = new ConcurrentHashMap<Integer,MessageParser>();

	Map<String,String> arrValues = new ConcurrentHashMap<String,String>();  // map to store CSV Values for each array
	Map<String,JsonArray> arrModels = new ConcurrentHashMap<String,JsonArray>();  // map to store CSV Values for each array
	Map<String,String> processedArrays = new ConcurrentHashMap<String,String>();  // map to store Processed Arrays. Value is just dummy
	
	/**/
	DataModel dataMapperObj=null;   //This will store datamapper key names and precision
	Gson gson;
	JsonElement jsonModelObj=null;
	JsonElement anomalyDetectJson=null;
	LicenseValidator licenseChecker= null;
	long rxSensorCount=0;
	/**/
	
	 
	public Decompressor()
	{
		this("TDI");  // default to TDI license check
		
	}
	

	public Decompressor(String licType)
	{
		logger= Logger.getLogger(this.getClass());
		gson = new GsonBuilder().setPrettyPrinting().create();
		xlratorCore= new Core();
		licenseChecker=new LicenseValidator(licType);
		licenseChecker.init();
		init();
		
	}
	

	private void init()
	{
		expectedSequenceNum=1;
		xlratorCore.setDeCompressionParams(false); // To be expanded to add more parameters
	}

	   /**
	   * This method is used to set debug log level.
	   * @deprecated 
	   * @param logLevel This is Log Level to be set. Level is import from org.apache.log4j.Level
	   */
	
	public void setLogLevel( Level logLevel)
	{
	
		logger.setLevel(logLevel);
		xlratorCore.setLogLevel(logLevel); 
	}
	
	   /**
	   * This method is used to decompress binary input from DataXlrator compressor. 
	   * @param inmsg  This is the byte array output of compressor 
	   * @return String This returns decompressed message as string. The current version supports output format as JSON and CSV.
	   */
	
	public String decompress(byte[] inmsg)
	{

		// Check for license validity
		if(!licenseChecker.isLicenseValid())
		{
			
			return null;
		}
		String msgOutput="";
		// Extract the Message Arr
	//	MessageFormatterArray.Builder msgArr=MessageFormatterArray.newBuilder();
	
	/*	logger.info(inmsg.length);	
	    String  binaryString="";
	    long payload[] = BitUtils.byteArrayTolongArray( inmsg);
		for(int i=0;i<payload.length;i++)
	    	   binaryString+= Long.toBinaryString(payload[i]);
	       logger.info("Binary: " + binaryString); */	
		MessageParser msgArr= new MessageParser(inmsg);
	/*	try {
			msgArr.mergeFrom(inmsg);
		} catch (InvalidProtocolBufferException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	*/

		
		 if((msgArr.getSequenceNum() != expectedSequenceNum)) //// Message received out of sequence. Put in Q for further processing
		 {
			 
			 // if we are here, it is quite possible that there is a restart from edge
			 // Check if current message Sequence Number is from start
			 boolean bComboMessageFound=false;
			 if(msgArr.getSequenceNum()==1) 
			 {
				 // check if there is potential for Combo Message
				 
				 for(int i=0;i<msgArr.getMessageCount();i++)
					{
						Message msgFormatter=msgArr.getMessage(i);
						if(msgFormatter.getMessgeID()==BitUtils.RCOMBO)
						{
							bComboMessageFound=true;
							break;
						}
					}
			 }
			 
			 if(!bComboMessageFound)
			 {
				 System.out.println("Received: " + msgArr.getSequenceNum() + " Expected: " + expectedSequenceNum);
				 // Process for decompression
				 DecompressorDataMessageHandlerQ.put(new Integer(msgArr.getSequenceNum()), msgArr);
				 // Check if the entry exists for current expected sequence number
				 msgArr=DecompressorDataMessageHandlerQ.remove(new Integer(expectedSequenceNum));
				 if(DecompressorDataMessageHandlerQ.size()>50)
				 {
					 logger.warn("Out of order messages Q is growing. Check for network issue and restart" );
				 }
			 }
			 else
			 {
				// reset  
				 expectedSequenceNum=1;
				 DecompressorDataMessageHandlerQ.clear();
			 }
			 
		 }
		 
		 if(msgArr !=null)
		 {
			 
			 msgOutput= getDecompressedMessage(msgArr, xlratorCore,xlratorCore);
			 expectedSequenceNum++;
			 if(expectedSequenceNum>BitUtils.MAX_SEQ_NUMBER)
				 expectedSequenceNum=1;
		 }

		updateSensorCount(msgOutput); 
		// Convert to appropriate Output Format 
		if ((outputFormat==DataFormat.JSON))
		{
			msgOutput= getOutputAsJSON(msgOutput);
		}

		//System.out.println(msgOutput);
		return msgOutput;
	}
	
	private void updateSensorCount(String msgOutput) {
		
        int pos = 0, end;
        long sensorCount=0;
        rxSensorCount=0;
        while ((end = msgOutput.indexOf(',', pos)) >= 0) {
            pos = end + 1;
            sensorCount++;
        }
        rxSensorCount=sensorCount+1;
	}

    public long getSensorCount()
    {
    	 return rxSensorCount;
    }
	private String getDecompressedMessage(MessageParser msgParser, Core processor, Core parentProcessor)
	{
		String msgOutput=null;
		
		 if(msgParser !=null)
		 {

			 if(msgParser.getMessageCount()==0)
			 {
				 // if message count is zero, then there is no change in values, update history
				 
				 msgOutput=processor.processCompressedMessage(null);
			 }
			 else
			 {
				 // This is the correct message to be processed
				 for(int i=0;i<msgParser.getMessageCount();i++)
					{
						Message msgFormatter=msgParser.getMessage(i);
						//logger.info("Processing: " + msgFormatter.getMessgeID() );
						switch(msgFormatter.getMessgeID())
						{
						case BitUtils.RCOMBO:
							
							if((processor == parentProcessor ) && arrModels.size()!=processedArrays.size())
							{
								// We are here means some array has no change in values and hence it has not been sent by compressor
								Iterator it = arrModels.entrySet().iterator();
							    while (it.hasNext()) {
							        Map.Entry pair = (Map.Entry)it.next();
							        if(processedArrays.get(pair.getKey()) == null)
							        {
							        	// This array has not been processed, meaning there is no change in value
							        	Core currentArrProcessor=parentProcessor.getArrayProcessor((String)pair.getKey());
							        	currentArrProcessor.processCompressedMessage(null);
							        	
							        }
							    }
							}
							msgOutput=processor.processComboMessage(msgFormatter.getPayload());
							if(processor == parentProcessor )
									processedArrays.clear();
						//	lMsgCounter++;
							break;
						case BitUtils.SCALE:
							processor.processScaleChange(msgFormatter.getPayload());
							break;
						case BitUtils.COMPRESSED:
							if((processor == parentProcessor ) && arrModels.size()!=processedArrays.size())
							{
								// We are here means some array has no change in values and hence it has not been sent by compressor
								Iterator it = arrModels.entrySet().iterator();
							    while (it.hasNext()) {
							        Map.Entry pair = (Map.Entry)it.next();
							        if(processedArrays.get(pair.getKey()) == null)
							        {
							        	// This array has not been processed, meaning there is no change in value
							        	Core currentArrProcessor=parentProcessor.getArrayProcessor((String)pair.getKey());
							        	currentArrProcessor.processCompressedMessage(null);
							        	
							        }
							    }
								//processedArrays.clear();
		  
							}
							msgOutput=processor.processCompressedMessage(msgFormatter.getPayload());
							if(processor == parentProcessor )
								processedArrays.clear();
					//		lMsgCounter++;
							break;

						case BitUtils.COMPRESSION_PARAMS:
							
							processCompressionParams(msgFormatter.getPayload());
						//	lMsgCounter++;
							break;

						case BitUtils.DATAMODEL:
							processDataModel(msgFormatter.getPayload());
							break;
						case BitUtils.JSONMODEL:
							processJsonModel(msgFormatter.getPayload());
							break;
						case BitUtils.ARR_MODEL_UPDATE:
							processArrayModel(msgFormatter.getPayload());
							break;
						case BitUtils.STR_PAYLOAD:
							processor.processStringPayload(msgFormatter.getPayload());
							break;
						case BitUtils.ARR_UPDATE:
							//xlratorCore.processArrPayload(msgFormatter.getPayload());
							MessageParser msgArr= new MessageParser(msgFormatter.getPayload());
							String arrName= parentProcessor.getArrayName(msgArr.getSequenceNum());
							Core currentArrProcessor=parentProcessor.getArrayProcessor(arrName);
							processedArrays.put(arrName,arrName);
							
							if(msgArr.getMessageCount()==0)
							{
								//logger.info("Recevied empty array");
								arrValues.put(arrName, ""); // put empty string to indicate zero size array

							}
							else
							{
								if(currentArrProcessor!=null)
								{
									String decompressedArrCSV= getDecompressedMessage(msgArr,currentArrProcessor,parentProcessor);
									
									if(decompressedArrCSV!=null) /* If there are numeric values then put it to Q*/
										arrValues.put(arrName, decompressedArrCSV);
								}
								else
									logger.info("Processor for :" + arrName + " is NULL.");
							}
							//System.out.println("Decompressed Message: " + decompressedArrCSV);
							break;	
						default:
							break;
							
						}
					}
				 
				// if we have iterated through the whole message and msg output is null, this means there is no combo or compressed message
				 // and payload may just have string data only
				 if(msgOutput==null)  
				 {
					 msgOutput=processor.processCompressedMessage(null);
				 }
			 }
		 }
		return msgOutput;
	}
	
	
	private void processArrayModel(byte[] payload) {
		
		// first byte contains array index
		int arrIndex= payload[0];
		String arrName= xlratorCore.getArrayName(arrIndex);
		byte arrString[]= new byte[payload.length-1];
		for(int i=0;i<arrString.length;i++)
		{
			arrString[i]=payload[i+1];
		}
		String jsonStr= new String(arrString);
		//logger.info("Array String:" + jsonStr + " Array name " + arrName + "Index: " + arrIndex);
		JsonReader reader = new JsonReader(new InputStreamReader(new ByteArrayInputStream(jsonStr.getBytes(StandardCharsets.UTF_8))));
		JsonArray jsonArr=new JsonParser().parse(reader).getAsJsonArray();
		
		arrModels.put(arrName, jsonArr);	
		
		xlratorCore.updateArrayDataModel(arrName,jsonArr);
		
		logger.info("Json Array Model Updated for " + arrName);
	}

	private void processCompressionParams(byte[] payload) {
		
		String compressionParams[]= new String(payload).split(",");
		int sampleSize=Integer.parseInt(compressionParams[0]);
		int lowBitSize=Integer.parseInt(compressionParams[1]);   
		int highBitSize=Integer.parseInt(compressionParams[2]);
		int precisionCap=Integer.parseInt(compressionParams[3]);
		boolean bUsePfxEncoding=Boolean.parseBoolean(compressionParams[4]);
		boolean bEnableBatch=Boolean.parseBoolean(compressionParams[5]);
		int batchSize=Integer.parseInt(compressionParams[6]);
		int nRegressionSampleSize=Integer.parseInt(compressionParams[7]);
		boolean bEnableLinearCompression=Boolean.parseBoolean(compressionParams[8]);
		xlratorCore.setCompressionParams(sampleSize,lowBitSize,highBitSize,precisionCap,bUsePfxEncoding,bEnableBatch,batchSize, false);
		xlratorCore.setRegressionParams(nRegressionSampleSize,bEnableLinearCompression);
		
	}



	private void processDataModel(byte[] modelByteArray) {
		logger.info("Processing  Datamodel");
		InitDataModel(getUnZippedString(modelByteArray));
		return;
	}

	private String getUnZippedString(byte[] input)
	{
		 ByteArrayInputStream sourceStream = new ByteArrayInputStream(input);
		 ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		 InputStream decompressor = null;
		   
		        try {
					decompressor = new GZIPInputStream(sourceStream);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		        
		        int reads=-1;
				try {
					reads = decompressor.read();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
		        while(reads != -1)
		        { 
		        	outStream.write(reads); 
		        	try {
						reads = decompressor.read();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 
		        }
		    return outStream.toString();    
	}

	private void InitDataModel(String datamodel)
	{
        if(!datamodel.isEmpty())
        {
        	//System.out.println(datamodel);
			dataMapperObj=gson.fromJson(datamodel, DataModel.class);
			xlratorCore.UpdateDataModelPrecison(false,dataMapperObj,0);
        }
	}
	
	private void processJsonModel(byte[] jsonModelByteArray) {
		logger.info("Processing  JSON Model");
		String JSONString= getUnZippedString(jsonModelByteArray);
		xlratorCore.generateDataMapperfromJSON(JSONString);
		JsonReader reader = new JsonReader(new InputStreamReader(new ByteArrayInputStream(JSONString.getBytes(StandardCharsets.UTF_8))));
		jsonModelObj= new JsonParser().parse(reader);
		if(logger.isDebugEnabled())
			logger.debug(gson.toJson(jsonModelObj));
	     updateJsonModelForArrays(jsonModelObj);
		if(outputFormat== DataFormat.AUTO)
		{
			// Auto detection of Output Data Format.
			outputFormat=DataFormat.JSON;
			logger.info("Auto Detected Output Format as JSON ");
		}
			
		return;  //JSON model processed
		
	}
	private void updateJsonModelForArrays(JsonElement jsonObj) {
		
		if (jsonObj.isJsonObject()) 
		{
					
	        Set<Entry<String, JsonElement>> ens = ((JsonObject) jsonObj).entrySet();
	        if (ens != null) 
	        {
		    	
	            // Iterate JSON Elements with Key values
	            for (Entry<String, JsonElement> en : ens) 
	            {
	            	if(en.getValue().isJsonObject())
	            	{
	            			updateJsonModelForArrays(en.getValue());
	            	}
	            	else
	            		if(en.getValue().isJsonArray())
	            		{
	            			JsonArray arr=(JsonArray) en.getValue();
	            			if(arr.size()>0)
	            			{
	            				
	            				// Create a new Json Array String
	            				
	            				byte arrayString[]= gson.toJson(arr).getBytes();
	            				JsonReader reader = new JsonReader(new InputStreamReader(new ByteArrayInputStream(arrayString)));
	            				arrModels.put(en.getKey(), new JsonParser().parse(reader).getAsJsonArray());	  
	            				logger.info("Json Array Model Added for " + en.getKey());
	            			}
	            			
	            		}
	            }
	            
	        } 
		}
		
	}


	   /**
	   * This method is for internal use only.
	   * @deprecated 
	   */
	
	public void setDataFormat( int input ) 
	{
		outputFormat=input;
		
	}


	   /**
	   * This method is for internal use only.
	   * @deprecated 
	   */
	/* Convenience function that can be used for Database Write*/
	public String getJSON(String inMsg)
	{
		// check if it is already in json format
		if(outputFormat== DataFormat.JSON)
		{
			return inMsg;
		}
		else
		{
			return getOutputAsJSON(inMsg);
		}
				
	}

	   /**
	   * This method is for internal use only.
	   * @deprecated 
	   */
	public String getAnamolyDetectJSON(String jsonFormat)
	{
		
		if(anomalyDetectJson==null)  // To avoid parsing again and again, create the instance once
		{
			JsonReader reader = new JsonReader(new InputStreamReader(new ByteArrayInputStream(jsonFormat.getBytes(StandardCharsets.UTF_8))));
			anomalyDetectJson= new JsonParser().parse(reader);
		}
		
		double[] values= xlratorCore.getAdValues();
		int index=0;
		if (anomalyDetectJson.isJsonObject())
		{
	        Set<Entry<String, JsonElement>> ens = ((JsonObject) anomalyDetectJson).entrySet();
	        if (ens != null) {
	            // Iterate JSON Elements with Key values
	            for (Entry<String, JsonElement> en : ens) {
	            	
	            	if(en.getValue().isJsonObject())
	            	{   
	            		Set<Entry<String, JsonElement>> ensScalarValues = ((JsonObject) en.getValue()).entrySet();
	            		for (Entry<String, JsonElement> enDps : ensScalarValues)
	            		{
	            			if(!(enDps.getValue().isJsonObject()))
	            					enDps.setValue(new JsonPrimitive(new BigDecimal(values[index++])));
	            		}
	            	}
	            }
	            
	        }
		}
	    return gson.toJson(anomalyDetectJson);
	}
	
	private String getOutputAsJSON(String inmsg)
	{
	    String jsonOutput="";
	    int startIndex[]= new int[1];
	    startIndex[0]=0;
	    if(jsonModelObj!=null) // Use the model from input derived JSON
	    {
	    	//logger.info(inmsg);
	    	//create a copy of JSON model so that elements can be added or removed
	    	
	    	jsonOutput= gson.toJson(setJsonValues(jsonModelObj,inmsg,startIndex,null));
	    }
	    else  // We have to generate JSON Output for Input as CSV
	    {
	       // Simulate JSON Output based on input Datamodel or auto generate it once
	    	jsonModelObj=getOutputAsJSONForCSVInput(inmsg);
	    	jsonOutput=gson.toJson(jsonModelObj);
	    }
	   // System.out.println(jsonOutput);
	    if(logger.isTraceEnabled())
	    	logger.trace(jsonOutput);
	    
	    return jsonOutput;

	}
	
	private JsonElement getOutputAsJSONForCSVInput(String inputMsg) {
		
		int endIndex=0;
	    int startIndex=0;
	    String dpStr="";
	    int i=0;
	    JsonObject JsonObj = new JsonObject();

	    while((endIndex !=-1)) 	
	    {
	        // Use substring as it gives better performance over split
	    	//colNo=i+1;
		    
	    	endIndex=inputMsg.indexOf(',', startIndex);
	    	if((endIndex==-1) )
	    	{
	    		dpStr=inputMsg.substring(startIndex);
	    	}		
	    	else
	    	{
	    		dpStr=inputMsg.substring(startIndex,endIndex);
	    		startIndex=endIndex+1;
	    	}

	    	// Check the name to see if it has to be a primitive data or JSON Object
    		String fieldName[]=dataMapperObj.getModel().get(i).getName().split("_");
    		DataType dt=dataMapperObj.getModel().get(i).getDataType();
    		if(fieldName.length>1)
    		{
    			JsonObject nestedObj=JsonObj.getAsJsonObject(fieldName[0]);
    			if(nestedObj==null)
    			{
    				nestedObj = new JsonObject();	
    			}
    			if(dt==DataType.NUMBER)
    				nestedObj.addProperty(fieldName[1], new JsonPrimitive(dpStr).getAsNumber());
    			else
    				if(dt==DataType.BOOLEAN)
        				nestedObj.addProperty(fieldName[1], new JsonPrimitive(dpStr).getAsBoolean());
    				else
    					nestedObj.addProperty(fieldName[1], new JsonPrimitive(dpStr).getAsString());
    			
    			
    			JsonObj.add(fieldName[0], nestedObj);
    		}
    		else   // This is primitive data
    		{
    			if(dt==DataType.NUMBER)
    				JsonObj.addProperty(fieldName[0],new JsonPrimitive(dpStr).getAsNumber());
       			else
    				if(dt==DataType.BOOLEAN)
    					JsonObj.addProperty(fieldName[0], new JsonPrimitive(dpStr).getAsBoolean());
    				else
    					JsonObj.addProperty(fieldName[0], new JsonPrimitive(dpStr).getAsString());

    		}
	    	
	    	i++;
	    }
		return JsonObj;
	}

	private JsonElement setJsonValues(JsonElement jsonElement, String csvValues, int[] startIndex,JsonArray newArr) {

		if (jsonElement.isJsonObject()) {
			
			JsonObject obj=null;
            // System.out.println(csvValues + ":" + startIndex[0]);
	        Set<Entry<String, JsonElement>> ens = ((JsonObject) jsonElement).entrySet();
	        if (ens != null) {
	        	
	        	if(newArr!=null)
		    	{
    				obj= new JsonObject();
		    	}
	            // Iterate JSON Elements with Key values
	            for (Entry<String, JsonElement> en : ens) {
	            	
	            	if(en.getValue().isJsonObject())
	            	{
	            		setJsonValues(en.getValue(),csvValues,startIndex,null);
	            	}
	            	else
	            		if(en.getValue().isJsonArray())
	            		{
	            			JsonArray arr=(JsonArray) en.getValue();
	            			JsonArray arrNew= new JsonArray();
	            			String arrCSVValues=arrValues.get(en.getKey());
	            			int []startcsvIndx=new int[1];
	            			startcsvIndx[0]=0;
	            			//logger.info(arrCSVValues + " arr size : " + arr.size());
	            			
	            			/*for(int i = 0; i < arr.size(); i++)
	            			{
	            				JsonElement obj=setJsonValues(arr.get(i),arrCSVValues,startcsvIndx,arrNew);
	            				
	            			}*/
	            			// We'll populate arrays as per CSV Count
	            			
	            			// Handle single string entry case over here, do-while is better 
	            			if(arrCSVValues!=null && !arrCSVValues.isEmpty() )
	            			{
		            			do
		            			{
		            				// Use first entry as schema indicator only
		            				
		            				setJsonValues(arrModels.get(en.getKey()).get(0),arrCSVValues,startcsvIndx,arrNew);
		            				
		            			} while(startcsvIndx[0] != (arrCSVValues.lastIndexOf(",")+1));
	            			}
	            			else
	            			{
	            				// populating empty array
	            				//logger.info("Got empty array");
	            			}
	            			
	            			en.setValue(arrNew);
	            			
	            			//arr.remove(arr.size()-1);
	            			// there is dummy entry in csv file for arrays due to datamodel, skip the csv value for the same
	    	            	/*int endIndexKey=csvValues.indexOf(',',startIndex[0]);
	    	    			
	    	    			if(endIndexKey!=-1)
	    	    			{
	    	    				startIndex[0]=endIndexKey+1;
	    	    			}*/
	    	    			
	            			
	            		}
		            	else
		            	{
		            		// this has to be JSON Primitive
		            		if(en.getValue().isJsonPrimitive())
		            			
		            		{
		    	            	String value="";
		    	    			int endIndexKey=csvValues.indexOf(',',startIndex[0]);
		    	    			
		    	    			if(endIndexKey==-1)
		    	    			{
		    	    				value=csvValues.substring(startIndex[0]);
		    	    				
		    	    			}
		    	    			else
		    	    			{
		    	    				value=csvValues.substring(startIndex[0],endIndexKey);
		    	    				startIndex[0]=endIndexKey+1;
		    	    			}
		    	    			
		    	    			
		    	    			JsonPrimitive val= (JsonPrimitive) en.getValue();
		    	    		//	logger.info("key:"+ en.getKey() + "Value:" + value);
		    	    			JsonPrimitive newVal=null;

		    	    			if(val.isNumber())
		    	    		    {
		    	    		    	BigDecimal numVal= new BigDecimal(value);
		    	    		    	newVal=new JsonPrimitive(numVal);
//		   	    		    		en.setValue(new JsonPrimitive(numVal));
		    	    		    	
		    	    		    }
		    	    		    else
		    	    		    	if(val.isBoolean())
		    	    		    	{
		    	    		    		Boolean boolVal= Boolean.parseBoolean(value);
		    	    		    		newVal=new JsonPrimitive(boolVal);
	//		    	    		    	en.setValue(new JsonPrimitive(boolVal));
		    	    		    		
		    	    		    	}
		    	    		    	else // String
		    	    		    	{
		    	    		    		//check if it is String data type from the object model
		    	    		    		newVal=new JsonPrimitive(value);
//		    	    		    		en.setValue(new JsonPrimitive(value));
		    	    		    			
		    	    		    	}
		    	    			
		    	    			if(obj!=null)
	    	    		    	{
		    	    				obj.add(en.getKey(), newVal);
	    	    		    	}
		    	    			else
		    	    				en.setValue(newVal);
		    	    			
		    	    			
		    	    			
		    	    		    
		            		}	
		            		
		            		else
		            		 logger.warn("JSON Parser error. Expecting JSON Primitive but found ");
		            	}
	            }
	            
	            if(newArr!=null)  // Add the object to array
	            {
    				newArr.add(obj);
	            }
	            
	        } 
	  
	       
	    }
		else  // it is primitive datatype. We expect to reach this code only for arrays with just values and not key-value pairs
		{
			JsonPrimitive val= (JsonPrimitive) jsonElement;
			JsonPrimitive newVal=null;

			String value="";
			int endIndexKey=csvValues.indexOf(',',startIndex[0]);
			
			if(endIndexKey==-1)
			{
				value=csvValues.substring(startIndex[0]);
				
			}
			else
			{
				value=csvValues.substring(startIndex[0],endIndexKey);
				startIndex[0]=endIndexKey+1;
			}
			

			if(val.isNumber())
		    {
		    	BigDecimal numVal= new BigDecimal(value);
		    	newVal=new JsonPrimitive(numVal);
//		    		en.setValue(new JsonPrimitive(numVal));
		    	
		    }
		    else
		    	if(val.isBoolean())
		    	{
		    		Boolean boolVal= Boolean.parseBoolean(value);
		    		newVal=new JsonPrimitive(boolVal);
//		    	    		    	en.setValue(new JsonPrimitive(boolVal));
		    		
		    	}
		    	else // String
		    	{
		    		//check if it is String data type from the object model
		    		newVal=new JsonPrimitive(value);
//		    		en.setValue(new JsonPrimitive(value));
		    			
		    	}

			if( newArr !=null)
			{
				newArr.add(newVal);
			}
		}
    	

	    
		return  jsonElement;

	}


}
