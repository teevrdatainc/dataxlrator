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

import io.teevr.aws.UsageMeter;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;

import org.apache.log4j.Logger;
import org.bouncycastle.openpgp.PGPException;

import com.verhas.licensor.ExtendedLicense;

class LicenseValidator {
	
	byte [] digest = new byte[] {
			(byte)0x78, 
			(byte)0x02, (byte)0x82, (byte)0xCA, (byte)0xF2, (byte)0x02, (byte)0x04, (byte)0x16, (byte)0x97, 
			(byte)0xFF, (byte)0x10, (byte)0x4A, (byte)0xFA, (byte)0x47, (byte)0x4E, (byte)0x20, (byte)0x86, 
			(byte)0x79, (byte)0xF7, (byte)0x79, (byte)0x23, (byte)0xE3, (byte)0xCC, (byte)0xAD, (byte)0x97, 
			(byte)0xC7, (byte)0xF1, (byte)0x8D, (byte)0xCB, (byte)0xE3, (byte)0x6C, (byte)0xFA, (byte)0xEB, 
			(byte)0x7A, (byte)0x65, (byte)0x90, (byte)0x83, (byte)0x51, (byte)0x54, (byte)0xA2, (byte)0x4E, 
			(byte)0xFC, (byte)0xEC, (byte)0x97, (byte)0x73, (byte)0xE9, (byte)0xCD, (byte)0x52, (byte)0x35, 
			(byte)0x99, (byte)0xD6, (byte)0x05, (byte)0xBE, (byte)0x43, (byte)0xF0, (byte)0x46, (byte)0xF7, 
			(byte)0x70, (byte)0xB0, (byte)0x33, (byte)0x71, (byte)0x63, (byte)0xA0, (byte)0xDA, 
			};
	
	
	/* This should match with the license types in configuration schema of dataxlrator*/
	public enum LicenseType {
	    TDI, AMI
	}
	
	LicenseType licType=LicenseType.TDI; // Default to TDI Licensing 
	String licenseFileName = "teevr-license-key.out";
	String amiProductCodeFileName="teevr-ami-code.out";
	String amiProductcode="";
	long AMICheckTimePeriod=60*60*1000;  // check every hour
	String pubringFileName= "pubring.gpg";
	UsageMeter usgMtr=null;
	long LicenseCheckTimePeriod=12*60*60*1000;   // check every 12 hours
	
	ExtendedLicense license = null;
	Logger logger;
	LicenseCheckTimer licenseValidity;
	
	
	LicenseValidator(String licType)
	{

		license= new ExtendedLicense();
		logger= Logger.getLogger(this.getClass());
		setLicenseType(licType);
		licenseValidity=null;
	}
	
	private void setLicenseType(String licType)
	{
		if(licType.equalsIgnoreCase(LicenseType.AMI.toString()))
		{
			this.licType=LicenseType.AMI;
		}
		else 
		{
			this.licType=LicenseType.TDI;
			
		}
		System.out.println("License Type " + this.licType);
	}
	
	private  String getFileString(String FileName) {
		// TODO Auto-generated method stub
		 BufferedReader reader=null;
		 String line;
		 StringBuilder configString= new StringBuilder();
		 InputStream input= getClass().getResourceAsStream("/"+FileName);
		 if(input==null)
		 {
			Path dsResource = Paths.get("./"+FileName);

			try {
				reader = Files.newBufferedReader(dsResource, StandardCharsets.UTF_8);
			} catch (IOException e) {
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
				 configString.append(System.lineSeparator());
				 line=reader.readLine();
			 }

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 

		return configString.toString();
	}
	
	void init()
	{
		if(licType==LicenseType.TDI)
		{
			try {
				license.loadKeyRingFromResource(pubringFileName, digest);
//				license.setLicenseEncodedFromFile(licenseFileName);
				//System.out.println(getFileString(licenseFileName));
				//license.setLicenseEncodedFromResource(licenseFileName);
		        license.setLicenseEncoded(getFileString(licenseFileName));
			
			
				if (license.isVerified())
				{
					if(license.isExpired())
					{
						logger.fatal("License has expired. Please contact info@teevr.io for renewal of license.");
					}
					else
						if(license.isRevoked())
						{
							logger.fatal("License has been revoked. Please contact info@teevr.io for new license.");
						}
					else
					{
						licenseValidity= new LicenseCheckTimer(license);
						
				        new Timer().scheduleAtFixedRate(licenseValidity, 0, LicenseCheckTimePeriod);
				        logger.info("License check successful");
					}
				}
				else
				{
					logger.fatal("License is not valid. Please contact info@teevr.io for new license.");
				}
			} catch (FileNotFoundException e) {
				
				logger.warn(e.getMessage());
			} catch (IOException e) {
				
				logger.warn(e.getMessage());
				//e.printStackTrace();
			}  catch (ParseException e) {
				logger.warn(e.getMessage());
			} catch (IllegalArgumentException e){
				logger.warn(e.getMessage());
			} 
			catch (PGPException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}  
		}
		else
		{
			if(licType==LicenseType.AMI)
			{
				usgMtr= new UsageMeter(true); // Create Instance for License Check
				licenseValidity= new LicenseCheckTimer(usgMtr);
				
		        new Timer().scheduleAtFixedRate(licenseValidity, 0, AMICheckTimePeriod);

			}
		}
	}
	
	boolean isLicenseValid()
	{
		boolean ret=false;
		if(licType==LicenseType.TDI)
		{
			if(licenseValidity!=null)
			{
				ret= licenseValidity.isLicenseValid();
			}
			if(!ret)
				logger.warn("License is invalid. Please contact info@teevr.io");
		}
		else
		{
			if(licType==LicenseType.AMI)
			{
				// Validate product code
//				ret=amiProductcode.equalsIgnoreCase("abcdef01234");
//				ret=amiProductcode.equalsIgnoreCase("abcdef01234");
				if(licenseValidity!=null)
				{
					ret= licenseValidity.isLicenseValid();
				}

				if(!ret)
					logger.warn("Invalid AMI Product Code. Please contact info@teevr.io");

			}
		}
		return ret;
	}

	private String getAMIProductCode()
	{
		String ret="abcdef01234";
		InputStream input = getClass().getResourceAsStream(amiProductCodeFileName);
		if (input == null) {
			Path dsResource = Paths.get("./" + amiProductCodeFileName);
			try {
				input = Files.newInputStream(dsResource);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		BufferedReader fRd;
		try {
			fRd = new BufferedReader(new InputStreamReader(input, "UTF-8"));
			ret=fRd.readLine();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	
		return ret;
  }
}
