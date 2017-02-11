/*
 * Copyright (c) 2015-16,  Teevr Data Inc. All Rights Reserved 
 */

package io.teevr.cep;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;
import io.teevr.cep.AcceleroGyro;
import io.teevr.mqtt.MqttClientAdapter;


public class CEP {
	

	static int STATIONARY=0;
	static int MOVING=1;
	static int FALLING=2;
	Configuration cepConfig;
	EPServiceProvider cepSrvcPvdr;
	EPRuntime cepRT;
	EPAdministrator cepAdm;
    int curIndex;
    int updateFreq;
    int eventIndex;
    String type;
    boolean bQuiet;
    String location;  // Use location to create separate CEP engine per location
    String uaid;
    MqttClientAdapter remoteBroker=null;
    public CEP(String location, String UAID) {
		updateFreq = 120;
		curIndex = 0;
		eventIndex = 0;
		cepConfig=new Configuration(); 
		this.bQuiet=true;
		this.location=location;
		this.uaid=UAID;
	}

	public CEP(String type,boolean bQuiet) {
		updateFreq = 120;
		curIndex = 0;
		eventIndex = 0;
		this.type=type;
		cepConfig=new Configuration(); 
		this.bQuiet=bQuiet;
	}


	public void init(MqttClientAdapter remotebroker)
	{
		this.remoteBroker= remotebroker;
	    init();
	}
	
	public void init()
	{
		String CEPProvidername=location +"TeevrCEPEngine";
		cepConfig.addEventType("SensorAccleroGyro",AcceleroGyro.class.getName());
		cepSrvcPvdr = EPServiceProviderManager.getProvider(CEPProvidername,cepConfig);
		cepRT = cepSrvcPvdr.getEPRuntime();
		cepAdm = cepSrvcPvdr.getEPAdministrator();
		
		// Event type Info	
        EPStatement cepMonitor = cepAdm.createEPL("select * from SensorAccleroGyro");
        
      // 	cepMonitor.addListener(new CEPListener("Info",influxdbConn,eventsDB));

  //      EPStatement cepMove = cepAdm.createEPL("select * from SensorAccleroGyro().win:length(10) having (x != 0) AND (y != 0) AND (z != 0)");
    //    cepMove.addListener(new CEPListener("moving",influxdbConn,eventsDB));

        EPStatement cepNmove = cepAdm.createEPL(
        		"select * from SensorAccleroGyro().win:length(20) having "
				+ "((max(x) <= 1) AND (min(x) >= -1))"
				+ " AND ((max(y) <= 1) AND (min(y) >= -1))"
				+ " AND ((max(z) <= 1) AND (min(z) >= -1))"
				);
    /*    EPStatement cepNmove = cepAdm.createEPL(
        		"select * from SensorAccleroGyro().win:length(120) having "
				+ "(x in [-1,1])"
				+ " AND (y in [-1,1])"
				+ " AND (z in [-1,1])"
				); */
        //cepNmove.addListener(new CEPListener("stationary",dbAdapter));
        
        cepNmove.addListener(new CEPListener("stationary",remoteBroker,location,uaid));
        // Event type Fall	
        //Shiv if there is a fall one of the accelerometer coordinates only will change and other two will be close to zero
        EPStatement cepFall = cepAdm.createEPL(
       		"select avg(ax*ax+ay*ay+az*az) as accel from SensorAccleroGyro().win:length(5) having (x != 0) AND (y != 0) AND (z != 0)");
 //       EPStatement cepFall = cepAdm.createEPL(
   //     		"select * from SensorAccleroGyro().win:length(5) having (x != 0) AND (y != 0) AND (z != 0) AND (ax in [-0.1,0.1]) AND (ay in [-0.1,0.1])");
        
       // cepFall.addListener(new CEPListener("moving",dbAdapter));
        cepFall.addListener(new CEPListener("moving",remoteBroker,location,uaid));
	}
	
	public EPRuntime getCEPRT()
	{
		return cepRT;
	}
	


}
