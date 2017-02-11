package io.teevr.sensors;
import com.google.gson.annotations.Expose;
public class EnvironmentSensor {
	@Expose
	private String sensor= new String("EnvironSensorValue");
	@Expose
	private float sensorVal;
	
	public EnvironmentSensor(String sensorName){  
		this.sensor=sensorName;  
    }  
	
	public EnvironmentSensor(String sensorName,float sensorVal){  
		this.sensor=sensorName;  
		this.sensorVal =sensorVal;
    }
	

	public String getSensorName() {
	        return sensor;
	    }
	
	public float getsensorVal() {
	        return sensorVal;
	    }
	public void setsensorVal(float sensorVal) {
	        this.sensorVal =sensorVal;
	    }
	         
	    @Override
	    public String toString(){
	        StringBuilder sb = new StringBuilder();
	        sb.append(sensor + "="+getsensorVal()+"\n");
	        return sb.toString();
	    }

}
