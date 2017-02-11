package io.teevr.aws;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;


import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstanceAttributeRequest;
import com.amazonaws.services.ec2.model.DescribeInstanceAttributeResult;
import com.amazonaws.services.ec2.model.ProductCode;
import com.amazonaws.services.marketplacemetering.AWSMarketplaceMeteringClient;
import com.amazonaws.services.marketplacemetering.model.MeterUsageRequest;
import com.amazonaws.services.marketplacemetering.model.MeterUsageResult;
import com.amazonaws.util.EC2MetadataUtils;


public class UsageMeter {

	AWSMarketplaceMeteringClient meteringClient;
	Logger logger=null;
	Object usageUpdateLock=null;
	String productCode=null;
	BigInteger totalNumberofSensors=null;
	BigInteger freeTierLimit= new BigInteger("100000");  // 100K Messages are Free
	BigInteger billingUnit= new BigInteger("1000000");  //1M messages is one unit for charging
	long checkInterval=3600;  // Deafult to 3600 seconds
	BigInteger prevbillingTotalCount=null;
	boolean bAWSMeteringServiceReachable=true;
	
	public UsageMeter() {
		
		this(false);  // Start background timer
	}

	public UsageMeter(boolean bLicenseChecker) {
		
		meteringClient= new AWSMarketplaceMeteringClient();
		logger = Logger.getLogger("UsageMeter");
		
		productCode=getProductCode();
		if(!bLicenseChecker)
		{
			usageUpdateLock= new String("tdi-usagemeter");
			totalNumberofSensors= new BigInteger("0"); // Initialize to zero
			prevbillingTotalCount= new BigInteger("0");
			updateAWSMeterUsage();
		}
	}
	private String getProductCode() {
		 String instanceId = EC2MetadataUtils.getInstanceId();
		 AmazonEC2Client EC2Client= new AmazonEC2Client();
		 DescribeInstanceAttributeRequest attrReq=new DescribeInstanceAttributeRequest(instanceId,"productCodes");
		 DescribeInstanceAttributeResult resAttr=EC2Client.describeInstanceAttribute(attrReq);
		 if(resAttr!=null)
		 {
			 List<ProductCode> prdCodes= resAttr.getInstanceAttribute().getProductCodes();
			 if(prdCodes.size()!=1) // IF there is no prodcuct code or more than one prodcut code,something might be wrong.
			 {
				 System.out.println(" Not a Valid AMI. Please contact info@teevr.io");
			 }
			 String productCodeID=prdCodes.get(0).getProductCodeId();
			 return productCodeID;
		 }
		 else
		 {
			 System.out.println(" Product Code Request Failed. Please contact info@teevr.io");
			 
		 }
		 System.exit(-1);
		return null;
	}

	public boolean IsValidAMI()
	{
  	     MeterUsageRequest req=new MeterUsageRequest();
 		 req.setProductCode(productCode);
 		 req.setTimestamp(new Date());
 		 req.setUsageDimension("AggReadingsM");
		 req.setUsageQuantity(0);
 		 //req.setDryRun(true);
		 req.setDryRun(false);
 		 MeterUsageResult meteringRes=meteringClient.meterUsage(req);
 		 if(meteringRes!=null)
 		 {
 			 return true;
 		 }
 		 else
 			 return false; 
	}
	
	
	public boolean updateUsage(long qty)
	{
		
		BigInteger addend= new BigInteger(Long.toString(qty));
		//logger.info("Current: " + addend.toString() + " Total: " + totalNumberofSensors.toString());
    	synchronized (usageUpdateLock) {
    		totalNumberofSensors=totalNumberofSensors.add(addend); // This will have cumulative count
		}
		//logger.info(" Total: " + totalNumberofSensors.toString());

    	return bAWSMeteringServiceReachable;
	}

	public void updateAWSMeterUsage()
	{
		
		logger.info("updateAWSMeterUsage is called");
		Runnable runnable = new Runnable() {
            public void run() {
              // task to run goes here. Optimize usage of Biginteger of assignment operations
              BigInteger currentCount= new BigInteger("0");
          	synchronized (usageUpdateLock) {
          		currentCount=totalNumberofSensors; //new BigInteger(totalNumberofSensors.toString()); // This will have cumulative count
    		}
        	  MeterUsageRequest req=new MeterUsageRequest();
       		 req.setProductCode(productCode);
       		 req.setTimestamp(new Date());
       		 req.setUsageDimension("AggReadingsM");
       		 // This has to be usage quantity for the past one hour
       		logger.info("Total Count: " + currentCount.toString() + " Prev Count: " + prevbillingTotalCount.toString());
       		 if(currentCount.compareTo(freeTierLimit)==-1)
       		 {
       			 req.setUsageQuantity(0);
       			prevbillingTotalCount=currentCount;
       		 }
       		 else // We have publish usage only for the past one hour
       		 {
//       			logger.info("Total Count: " + currentCount.toString() + " Prev Count: " + prevbillingTotalCount.toString());
       			 BigInteger lastOneHourUsage=currentCount.subtract(prevbillingTotalCount);
       			prevbillingTotalCount=currentCount;
       			
       			BigInteger[] usageCount= lastOneHourUsage.divideAndRemainder(billingUnit); 
       			if(usageCount[1].compareTo(BigInteger.ZERO)==1)
       			{
       				usageCount[0]=usageCount[0].add(BigInteger.ONE);
       			}
       			
       			req.setUsageQuantity(usageCount[0].intValue());
       			logger.info("Last one hour usage: " + lastOneHourUsage.toString() + " AWS Metering Usage: " + usageCount[0].intValue());
       		 }
       		// req.setDryRun(true);
       		 req.setDryRun(false);
       		 MeterUsageResult meteringRes=meteringClient.meterUsage(req);
       		 if(meteringRes!=null)
       		 {
       			 logger.info("Metering Record ID: " + meteringRes.getMeteringRecordId());
       			 bAWSMeteringServiceReachable=true;
       		 }
       		 else
       		 {
       			logger.warn("Meter Usage Call Failed.!!! " );
       			 bAWSMeteringServiceReachable=false;
       		 }
 
            }
          };
          
	          ScheduledExecutorService service = Executors
	                          .newSingleThreadScheduledExecutor();
	          service.scheduleAtFixedRate(runnable, 0, checkInterval, TimeUnit.SECONDS);
	}

}
