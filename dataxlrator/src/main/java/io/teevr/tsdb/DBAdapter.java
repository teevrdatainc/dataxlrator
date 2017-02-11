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

import io.teevr.config.Advanced.Clouddb;
import io.teevr.config.Configuration;

public class DBAdapter implements Database{
	
	Configuration config;
	String dbName="";
	Clouddb dbToUse=Clouddb.NONE;
	Database db=null;
	String ds; // Datasource name equivalent of location
	String seriesName="";
	String dataMapperCSVString="";
	public DBAdapter(String DatabaseName,Configuration config,String ds)
	{
		this.config=config;
		dbToUse=config.getAdvanced().getClouddb();
		dbName=DatabaseName;
		this.ds=ds;
		seriesName=ds;
	}
	
	public DBAdapter(String DatabaseName,Configuration config,String ds, String SeriesName)
	{
		this.config=config;
		dbToUse=config.getAdvanced().getClouddb();
		dbName=DatabaseName;
		this.ds=ds;
		this.seriesName=SeriesName;
	}
	public Database init()
	{
		System.out.println("Initializing Database  " + dbToUse + " DataSource: "+ds);
		// Initialize the database to be used
		db=null;
		
		if(dbToUse==Clouddb.NONE)
		{
			db=null;
		}
		else
			if (dbToUse==Clouddb.INFLUXDB)
			{
				db= new InfluxDatabase(dbName,config,ds,seriesName);
				db=db.init();
			}

		return this; // mostly a dummy return

	}

	@Override
	public void write(String point) {
		// TODO Auto-generated method stub
		if(db!=null)
			db.write(point);
		
	}

	@Override
	public void setDataMapper(String csvString) {
		// TODO Auto-generated method stub
		//dataMapperCSVString=csvString;
		if(db!=null)
			db.setDataMapper(csvString);
	}

}
