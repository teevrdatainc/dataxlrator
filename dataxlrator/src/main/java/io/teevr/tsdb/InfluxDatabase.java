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

package io.teevr.tsdb;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;
import org.influxdb.dto.Query;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonReader;

import io.teevr.config.Configuration;
import io.teevr.config.Influxdb;

public class InfluxDatabase implements Database{

	Configuration config;
	InfluxDB influxDBInstance;
	String perfdb; 
	String eventsdb;
	String rawdatadb;
	String ds;
	String measurementName;
	
	List<String> measurementNamesArray = new ArrayList<String>(); // Contains measurementnames
	List<Integer> NumFieldsArray = new ArrayList<Integer>(); // Contains number of fields corresponding to measurement names
	List<String> fieldNames = new ArrayList<String>(); // Contains field names for each field in csv string sans measurementname
	JsonElement JsonObj=null;  
	public InfluxDatabase(String DbName, Configuration config, String ds, String measurementName)
	{
		this.config=config;
		eventsdb=config.getCloudDB().getInfluxdb().getEventsDBName();
		perfdb= config.getCloudDB().getInfluxdb().getPerfDBName();
		rawdatadb=DbName;
		this.ds=ds;
		this.measurementName=measurementName;
	}
	
	@Override
	public Database  init() {
		// TODO Auto-generated method stub
	 // Initialize the database
		String uri= "http://"+config.getCloudDB().getInfluxdb().getServerAddress()+":"+config.getCloudDB().getInfluxdb().getHTTPPort();
		System.out.println(" Influxdb Connection URI : " + uri);
		influxDBInstance=InfluxDBFactory.connect(uri, config.getCloudDB().getInfluxdb().getUsername(), config.getCloudDB().getInfluxdb().getPassword());
		if(influxDBInstance!=null)
		{
			influxDBInstance.createDatabase(rawdatadb);
			System.out.println("Connection to Influx succesfull");
			return this;
		}
		else
		{
			System.out.println("Connection to Influx failed ");
			return null;
		}
	}

	@Override
	public void write(String msg) {
		// TODO Auto-generated method stub
		//Dataformat to be written   datasourcename fieldname1=fieldvalue1,fieldname1=fieldvalue1.. timestamp
		
		//ds1 AccelerometerX=-0.0,AccelerometerY=0.14,AccelerometerZ=0.0,GyroscopeX=0.0,GyroscopeY=0.0,GyroscopeZ=0.0,MagnetoX=-40.32,MagnetoY=-11.46,MagnetoZ=-8.76,Proximity=9.0,Light=320.0,Pressure=912.81
		BatchPoints batchPoints = BatchPoints
                .database(rawdatadb)
//                .tag("async", "true")
//                .retentionPolicy("default")
                .consistency(InfluxDB.ConsistencyLevel.ALL)
                .build();
	
		//System.out.println("Influx Write String: " + csvPoints);
		Point.Builder point;
		long currentTS=System.currentTimeMillis();

/*		if(JsonObj==null)  // We'll use this to create the json object. Ensure that each json schema has different dbadapter 
		{
			JsonReader reader = new JsonReader(new InputStreamReader(new ByteArrayInputStream(msg.getBytes(StandardCharsets.UTF_8))));
			JsonObj= new JsonParser().parse(reader);
		}
*/
		JsonReader reader = new JsonReader(new InputStreamReader(new ByteArrayInputStream(msg.getBytes(StandardCharsets.UTF_8))));
		JsonObj= new JsonParser().parse(reader);

		if(JsonObj == null)
		{
			System.out.println("Influx Write String: " + msg);
			return;
		}
	
		// The JsoneElement is bound to be an object. Iterate through the list for values 
		// We don't support nested values for Database write yet
		if (JsonObj.isJsonObject())
		{
	        Set<Entry<String, JsonElement>> ens = ((JsonObject) JsonObj).entrySet();
	        if (ens != null) {
	            // Iterate JSON Elements with Key values
	            for (Entry<String, JsonElement> en : ens) {
	            	
	            	point=Point.measurement(en.getKey()).tag("location", ds).time(currentTS, TimeUnit.MILLISECONDS);
	
	            	if(en.getValue().isJsonObject())
	            	{
	            		Set<Entry<String, JsonElement>> ensScalarValues = ((JsonObject) en.getValue()).entrySet();
	            		for (Entry<String, JsonElement> enDps : ensScalarValues)
	            		{
	            			if((enDps.getValue().isJsonPrimitive()))
	            			{
		    	    			JsonPrimitive val= (JsonPrimitive) enDps.getValue();
		    	    			if(val.isNumber())
		    	    		    {
		    	    				point.addField(enDps.getKey(),val.getAsDouble());
		    	    		    }
		    	    		    else
		    	    		    	if(val.isBoolean())
		    	    		    	{
			    	    				point.addField(enDps.getKey(),val.getAsBoolean());
		    	    		    	}
		    	    		    	else // String
		    	    		    	{
			    	    				point.addField(enDps.getKey(),val.getAsString());
		    	    		    	}
	            			}
	            		}
	            	}
	            	else
	            		if(en.getValue().isJsonArray())
	            		{
	            			// Skip writing Array Values
	            		}
		            	else
		            	{
		            		
//		            		point.addField("value",en.getValue().getAsString());
	    	    			JsonPrimitive val= (JsonPrimitive) en.getValue();
	    	    			if(val.isNumber())
	    	    		    {
	    	    				point.addField("value",val.getAsDouble());

	    	    		    }
	    	    		    else
	    	    		    	if(val.isBoolean())
	    	    		    	{
		    	    				point.addField("value",val.getAsBoolean());
	    	    		    	}
	    	    		    	else // String
	    	    		    	{
		    	    				point.addField("value",val.getAsString());
	    	    		    	}

		            	}
	            	
	            	batchPoints.point(point.build());
	            }
	            
	        }
		}
		//System.out.println("Parsing Time: " + (System.currentTimeMillis()-currentTS));
	//	currentTS=System.currentTimeMillis();
		influxDBInstance.write(batchPoints);
	//	System.out.println("Write Time: " + (System.currentTimeMillis()-currentTS));
	}

	@Override
	public void setDataMapper(String csvString) {
		// TODO Auto-generated method stub
		
		// Create an array of measurement names, csv string array and number of fields
		// each measurement will have corresponding number of fields
		int startIndexKey=0,endIndexKey=0;
		String keys[];
        int index=-1;		
		while(endIndexKey!=-1)
		{
			endIndexKey=csvString.indexOf(',', startIndexKey);
			if(endIndexKey==-1 )
			{
				
				keys=csvString.substring(startIndexKey).split("_");
			}
			else
			{
				keys=csvString.substring(startIndexKey,endIndexKey).split("_");
				startIndexKey=endIndexKey+1;
			}
			
			if(measurementNamesArray.indexOf(keys[0])==-1)
			{
				measurementNamesArray.add(keys[0]);
				if(keys.length>1)
				{
					fieldNames.add(keys[1]);
				
				}
				else
					fieldNames.add("value");
				NumFieldsArray.add(1);
				index++;
			}
			else
			{
				// Measurementname already added. Just add field count and field name
				fieldNames.add(keys[1]);
				NumFieldsArray.set(index, NumFieldsArray.get(index)+1);
				
			}

		}
		
		// print all the values now
//		System.out.println(measurementNamesArray.toString());
//		System.out.println(NumFieldsArray.toString());
//		System.out.println(fieldNames.toString());
	}
	
	

}
