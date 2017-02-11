package io.teevr.sensors;
import com.google.gson.annotations.Expose;
public class PositionSensor {
	
	@Expose
	private String sensor= new String("PositionSensorValue");
	
	@Expose
	private float x;
	@Expose
	private float y;
	@Expose
	private float z;
    boolean bLinear= true;
    
 
    public PositionSensor(String sensorName, float sensorVal){  
		this.sensor=sensorName;  
		this.x=sensorVal;
		this.bLinear= true;

    }  

    public PositionSensor(String sensorName, float x ,float y, float z){  
		this.sensor=sensorName;  
		this.x=x;
		this.y=y;
		this.z=z;
		this.bLinear= false;
    }
    
	public String getSensorName() {
        return sensor;
    }
	public float getsensorVal() {
        return x;
    }
	public void setsensorVal(float sensorVal) {
        this.x =sensorVal;
    }

    public float getx() {
        return x;
    }
    public void setx(float x) {
        this.x = x;
    }
    public float gety() {
        return y;
    }
    public void sety(float y) {
        this.y = y;
    }
    public float getz() {
        return z;
    }
    public void setz(float z) {
        this.z =z;
    }
         
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        if(bLinear)
        {
	        sb.append(sensor + "="+getsensorVal()+"\n");
        }
        else
        {
        sb.append(sensor + "\n");
        sb.append("x="+getx()+"\n");
        sb.append("w="+gety()+"\n");
        sb.append("z="+getz()+"\n");
        }
        return sb.toString();
    }


}
