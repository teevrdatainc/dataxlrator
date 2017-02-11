package io.teevr.sensors;
import com.google.gson.annotations.Expose;
public class MotionSensor {
	 
	@Expose
	private String sensor= new String("MotionSensorValue");
	
	@Expose
    private float x;
	@Expose
	private float y;
	@Expose
	private float z;
    
   public MotionSensor(String sensorName){  
		this.sensor=sensorName;  
    }
    
    public MotionSensor(String sensorName,float x,float y,float z){  
		this.sensor=sensorName;  
		this.x = x;
		this.y = y;
		this.z = z;
    }
    
	public String getSensorName() {
        return sensor;
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
        sb.append(sensor + "\n");
        sb.append("x="+getx()+"\n");
        sb.append("y="+gety()+"\n");
        sb.append("z="+getz()+"\n");
        return sb.toString();
    }

}
