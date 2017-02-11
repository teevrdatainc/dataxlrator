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

import java.text.ParseException;
import java.util.TimerTask;

import com.verhas.licensor.ExtendedLicense;

class LicenseCheckTimer extends TimerTask {
	
	ExtendedLicense license;
	UsageMeter usgMeter;
	boolean bIsLicenseValid=true;
	LicenseCheckTimer(ExtendedLicense license)
	{
		this.license=license;
		usgMeter=null;
		
	}
	LicenseCheckTimer(UsageMeter usgMtr)
	{
		license=null;
        this.usgMeter=usgMtr;		
	}

	@Override
	public void run() {
		
		try {
			if(license!=null)
			{
				bIsLicenseValid = !(license.isExpired());
			}
			else
				if(usgMeter!=null)
				{
					bIsLicenseValid=usgMeter.IsValidAMI();	
				}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	boolean isLicenseValid()
	{
		return bIsLicenseValid;
	}
}
