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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import io.teevr.config.DataModel;
import io.teevr.config.Model;
import io.teevr.config.Model.DataType;

/**
* <h1>Compress data using DataXlrator</h1>
*  Compressor provides functions for compressing using DataXlrator.
* <p>
* 
*  <p>
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
* 
* <b>Note:</b> Since DataXlrator is streaming based compression, different types of data inputs should have corresponding decompressor.
* e.g. If there are two json streams with different schema's, there should be two compressor/decompressor pairs. Similarly, if there are two csv streams 
* with different numbers of values, there should be two compressor/decompressor pairs. Developers should ensure one to one mapping between compressor/decompressor 
* for different data sources, data source types and data source schemas.
*
* @author  Teevr Data Inc
* @version 1.0
* @since   05-Aug-2016
* @see	Decompressor
*/


public class Compressor {
	
	
		Core xlratorCore;
		
		int sampleSize=50;
		int lowBitSize=5;   
		int highBitSize=26;
		int precisionCap=5;   
		boolean bUsePfxEncoding=true;
		boolean bEnableBatch=false;
		int batchsize=1000;
		int nRegressionSampleSize=10;
		boolean bEnableLinearCompression=false;

		int nSequenceNumber=0;
		DataModel dataMapperObj=null;   //This will store datamapper key names and precision
		MessageBuilder batchMsgArr=null;
		/**
		 * for internal use only
		 * @deprecated
		 * 
		 *
		 */
		/*public enum DataFormat {
		    AUTO,
			CSV, 
		    JSON
		};*/
		int inputFormat= DataFormat.AUTO;
		String dataMapperString;
		JsonParser parser;
		Gson gson; 
		Logger logger=null;
		LicenseValidator licenseChecker= null;
	    // We'll send notification only when there is increase in Array Size. If Array size decreases, we'll just notify on number of rows in array
	    // for creation of output data
	    Map<String,Integer> JsonArraySizeMap = new ConcurrentHashMap<String,Integer>();  // This will store the previously processed size of an Array
	    Map<String,int[]> JsonArrayScaleMap = new ConcurrentHashMap<String,int[]>();  // This will store scale for array with its name
	     
		public Compressor()
		{
			logger= Logger.getLogger(this.getClass());
			gson = new GsonBuilder().setPrettyPrinting().create();
			parser= new JsonParser();
			dataMapperString="";
			xlratorCore=new Core();
			batchMsgArr= new MessageBuilder();
			/*licenseChecker=new LicenseValidator();
			licenseChecker.init();
			*/
			init();
		}
		
		private void init()  // initialize Compression parameters
		{
			// Create History Object
			nSequenceNumber=1;  //start sequence number from 1
			xlratorCore.setCompressionParams(sampleSize,lowBitSize,highBitSize,precisionCap,bUsePfxEncoding,bEnableBatch,batchsize,true);
			xlratorCore.setRegressionParams(nRegressionSampleSize,bEnableLinearCompression);
		}
		
		/**
		   * This method is for internal use to set max precision
		   * @deprecated 
		   * 
		   */
		public void init(int precisionCap)  // initialize with precision cap
		{
			this.precisionCap=precisionCap;   
			init();
		}

		
		/**
		   * This method is for internal use to set max precision, lo and hi bit sizes for compression
		   * @deprecated 
		   * 
		   */
		public void init( int precisionCap, int lowBitSize, int highBitSize) // initialize precision cap and bitSize caps
		{
			this.precisionCap=precisionCap;   
			this.lowBitSize=lowBitSize;   
			this.highBitSize=highBitSize;
			init();
			
		}

		
		/**
		   * This method is for internal use to set max precision, lo and hi bit sizes for compression,  sample size for history creation
		   * @deprecated 
		   * 
		   */
		
		public void init( int precisionCap, int lowBitSize, int highBitSize,int SampleSize, boolean bUsePfxEncoding, boolean bEnableBatch, int batchsize) // initialize precision cap and bitSize caps, and sample size
		{
			this.precisionCap=precisionCap;   
			this.lowBitSize=lowBitSize;   
			this.highBitSize=highBitSize;
			this.sampleSize=SampleSize;
			this.bUsePfxEncoding=bUsePfxEncoding;
			this.bEnableBatch=bEnableBatch;
			this.batchsize=batchsize;
			init();
		}
		
		
		/**
		   * This method is for internal use to set regression params
		   * @deprecated 
		   * 
		   */
		 public void setRegressionParams(int nRegressionSampleSize,
					boolean bEnableLinearCompression) {
				
				 this.nRegressionSampleSize=nRegressionSampleSize;
				 this.bEnableLinearCompression=bEnableLinearCompression;
				xlratorCore.setRegressionParams(nRegressionSampleSize,bEnableLinearCompression);
			}
		 

		 /**
		   * This method is for internal use.
		   * @deprecated 
		   * 
		   */

		public void setDataMapper( String mapperString ) 
		{
		
			if(mapperString!=null)
				dataMapperString=mapperString;
			
		}
		

		 /**
		   * This method is for internal use.
		   * @deprecated 
		   * 
		   */
		
		public void setDataFormat( int input ) 
		{
			inputFormat=input;
			
		}
		

		 /**
		   * This method is for internal use.
		   * @deprecated 
		   * 
		   */
		
		public void setLogLevel( Level logLevel)
		{
		
			logger.setLevel(logLevel);
			xlratorCore.setLogLevel(logLevel); 
		}
		
		
		   /**
		   * This method is used to compress input sensor data stream using DataXlrator. 
		   * @param inputmsg  This is string input to compressor 
		   * @return byte[] This returns compressed message as byte array. The current version supports input format as JSON and CSV.
		   */
		
		public byte[] compress(String inputmsg)
		{
			
			
			//We will check for license on cloud only
			// Check for license validity	
			/*if(!licenseChecker.isLicenseValid())
			{
				return null;
			}	*/
			String incsvMsg=""; // message after extracting into CSV format
			//long nEnd=0;
			//long nStart=System.nanoTime();
			//System.out.println(inputmsg);
			
			// For Processing batch mode, we will need a global MessageBuilder
			MessageBuilder msgArr=null;
			if(!bEnableBatch)
			   msgArr= new MessageBuilder();
			else
				msgArr=batchMsgArr;
			
			Message msgFormatter; 
			
			/*MessageFormatterArray.Builder msgArr=MessageFormatterArray.newBuilder();
			
			msgArr=MessageFormatterArray.newBuilder(); // Reset message Array
			msgArr.setSequenceNum(nSequenceNumber++);  // set the sequence number*/
			
			if(msgArr.getMessageCount()==0)  // Set sequence number only if there is no message in the Message Array. This is to handle batch mode processing
			{
				msgArr.setSequenceNum(nSequenceNumber++);
				
				// Handle Rollover of sequence number
				if(nSequenceNumber>BitUtils.MAX_SEQ_NUMBER)
					nSequenceNumber=1;
			}			
			msgFormatter=processDataMapper(inputmsg);
			if(msgFormatter!=null)
			{
				
				/* Before sending the data mapper, send compression params to synchronize cloud with coressponding edge data source's params*/
				String compressionParams= sampleSize+","+lowBitSize+","+highBitSize+","+precisionCap +","+ bUsePfxEncoding + "," + bEnableBatch + ","+ batchsize +","+nRegressionSampleSize+","+bEnableLinearCompression;
				msgArr.AddMessage(new Message(BitUtils.COMPRESSION_PARAMS, compressionParams.getBytes()));				

				msgArr.AddMessage(msgFormatter);
				// Process JSON Model now
				if(inputFormat==DataFormat.JSON)
				{
					msgFormatter=processJsonModel(inputmsg);
					if(msgFormatter!=null)
					{
						msgArr.AddMessage(msgFormatter);
					}
				}
			}
			
			// extract data from JSON
			if(inputFormat==DataFormat.JSON)
			 {
			  incsvMsg= InputAsJSON(inputmsg,msgArr); // pas msgarr  for populating compressed array messages
			  if(incsvMsg==null)
			  {
				  // Handle Array Size increase
			  }
			 }
			else
				incsvMsg=inputmsg;

			
			byte[] compressedBytes=xlratorCore.getCompressedMsg(incsvMsg,msgArr);

			//nEnd=System.nanoTime();

		/*	long payload[] = BitUtils.byteArrayTolongArray( compressedBytes);
		    String  binaryString="";
			for(int i=0;i<payload.length;i++)
		    	   binaryString+= Long.toBinaryString(payload[i]);
		       logger.info("Binary: " + binaryString); */	
			return compressedBytes;
		}
		
		
		private Message processDataMapper(String inputmsg) {
			
			if(dataMapperObj==null)
			{

				String dataMapper="";
			   // InitDataModel(dataModelString);
				if(inputFormat==DataFormat.AUTO)
				{
					// check if it is JSON 
					inputFormat=DataFormat.JSON;
					try
					{
						parser.parse(inputmsg.trim());
					}
					catch (JsonParseException  e)
					{
						inputFormat=DataFormat.CSV;
					}
					
					logger.info("Auto Detected Input Format as " + DataFormat.getFormatString(inputFormat));
				}
			
				if(inputFormat==DataFormat.JSON)
				{
					dataMapperObj=xlratorCore.generateDataMapperfromJSON(inputmsg);
				}
				else
					if(inputFormat==DataFormat.CSV)
					{
						
						if(dataMapperString.isEmpty())
						{
							dataMapperObj=xlratorCore.generateDataMapperfromCSV(inputmsg);
						}
						else
						{
							// We need to handle the case for input with dprepeat counts even though datamodel does not correspond to dprepeatcount
							DataModel autogenDataMapperObj=xlratorCore.generateDataMapperfromCSV(inputmsg);
							dataMapperObj=getDataMapper(dataMapperString);
							if(dataMapperObj.getModel().size() == autogenDataMapperObj.getModel().size()) // We will use datamodel string
							{
								dataMapper=dataMapperString;
							}
							else
							{
								dataMapperObj=autogenDataMapperObj;
								dataMapper="";
							}
						}
					}
				
				logger.info("Processing Data Model for " + DataFormat.getFormatString(inputFormat) + " Input.");
				
			    xlratorCore.UpdateDataModelPrecison(false,dataMapperObj,0);
			    
			    if(dataMapper.isEmpty())
			    {
			    	dataMapper=gson.toJson(dataMapperObj);
			    }
			    if(logger.isDebugEnabled())
			    	logger.debug(dataMapper );	
				   

				/*MessageFormatter.Builder msgBuilder= MessageFormatter.newBuilder();
				msgBuilder.setCommandID(Command.DATAMODEL);
				msgBuilder.setDataPayload(ByteString.copyFrom(getZippedBytes(dataMapper)));
				return msgBuilder.build();*/
			    return new Message(BitUtils.DATAMODEL,getZippedBytes(dataMapper));
			}
			else
				return null;
		}




		private DataModel getDataMapper(String dataMapper) {
			
			return gson.fromJson(dataMapper, DataModel.class);
		}
		
		private byte[] getZippedBytes(String buffer)
		{
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			GZIPOutputStream zippedOutput;
				try {
				zippedOutput = new GZIPOutputStream(out);
				zippedOutput.write(buffer.getBytes());
				zippedOutput.close();
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			if(logger.isDebugEnabled())	
				logger.debug("Zipped buffer size: " + out.size());	
			return out.toByteArray();	
		}


		private Message processJsonModel(String inputmsg) {
			
			logger.info("Processing JSON Model for JSON Input.");
		/*	MessageFormatter.Builder msgBuilder= MessageFormatter.newBuilder();
			msgBuilder.setCommandID(Command.JSONMODEL);
			msgBuilder.setDataPayload(ByteString.copyFrom(getZippedBytes(inputmsg)));
			return msgBuilder.build();*/
			return new Message(BitUtils.JSONMODEL,getZippedBytes(inputmsg));
		}
		
		private String InputAsJSON(String msg, MessageBuilder msgArr)
		{
			ArrayList<String>actlpayload= new ArrayList<String>();
			boolean bJsonArrSizeChanged=false;
			JsonReader reader = new JsonReader(new InputStreamReader(new ByteArrayInputStream(msg.getBytes(StandardCharsets.UTF_8))));
			
			JsonElement jsonElement= new JsonParser().parse(reader);
			bJsonArrSizeChanged=!getPayload(jsonElement,actlpayload,msgArr);
			
			if(bJsonArrSizeChanged)  // handle Json Array Size increase
			{
			  logger.fatal("JSON Array size has increased.");
			  return null;	
			}
			else
				return StringUtils.join(actlpayload,",");  //Use Apache utility if Java 8 not available
		    // return String.join(",",actlpayload);  //Use Apache utility if Java 8 not available

		}

		private boolean getPayload(JsonElement jsonElement, ArrayList<String> actlpayload, MessageBuilder msgArr) {

			boolean ret=true;
			
			if (jsonElement.isJsonObject()) {
				

		        Set<Entry<String, JsonElement>> ens = ((JsonObject) jsonElement).entrySet();
		        if (ens != null) {
		        	
		        	
		            // Iterate JSON Elements with Key values
		            for (Entry<String, JsonElement> en : ens) {

		            	if(!ret) //  break if any of the getpayload call fails
		            		break;
		            	
		            	if(en.getValue().isJsonObject())
		            	{
		            		ret=getPayload(en.getValue(),actlpayload,msgArr);
		            	}
		            	else
		            		if(en.getValue().isJsonArray())
		            		{
		            			JsonArray arr=(JsonArray) en.getValue();
		            			
		            			
		            			String csvMsg=null;

		            			if(arr.size()>0)
		            			{
		            				// Check if Core processor exists for this array. It is an indicator that datamodel has been created for this array
		            				if(!xlratorCore.isDataModelProcessed(en.getKey()))
		            						{
		            							// process  the datamodel and also update decompressor for change in array data model
		            							Message updateModelMsg= xlratorCore.updateArrayDataModel(en.getKey(),arr);
		            							if(updateModelMsg!=null)
		        		            				msgArr.AddMessage(updateModelMsg);
		            						    
		            						}
		            				ArrayList<String> arrpayload= new ArrayList<String>();
		            				
			            			for(int i = 0; i < arr.size(); i++)
			            			{
	
					            		ret=getPayload(arr.get(i),arrpayload,msgArr);
						            	
			            			}
			            			csvMsg=StringUtils.join(arrpayload,",");
		            			}
		            			

		            			Message msg= xlratorCore.compressArrayMessage(en.getKey(),csvMsg,arr.size());
		            			if(msg!=null)
		            				msgArr.AddMessage(msg);
		            				
		            			//logger.info(StringUtils.join(arrpayload,","));
		            			
		            			//actlpayload.add("0");  // add dummy value for array element
		            		}
			            	else
			            	{
			            		
			            		actlpayload.add(en.getValue().getAsString());
			            	}
		            }
		        }
		    }
			else  // Consider this as primitve data
				actlpayload.add(jsonElement.getAsString());
			
			return ret;
		}



}
