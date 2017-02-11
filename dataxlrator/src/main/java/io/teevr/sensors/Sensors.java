package io.teevr.sensors;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import com.google.gson.annotations.Expose;

public class Sensors {
	@Expose
	List<MotionSensor> motionsensors; 
	@Expose
	List<PositionSensor> positionsensors; 
	@Expose
	List<EnvironmentSensor> environmentsensors;
	
	public Sensors(){  
		motionsensors=  new ArrayList<MotionSensor>();
		positionsensors=  new ArrayList<PositionSensor>();
		environmentsensors=  new ArrayList<EnvironmentSensor>();
    }
	public List<MotionSensor> getMotionSensors() {
        return motionsensors;
    }
	
	public boolean addMotionSensor(MotionSensor motionsensor) {
        return motionsensors.add(motionsensor);
    }
    public void setMotionSensors(List<MotionSensor> motionsensors) {
        this.motionsensors = motionsensors;
    }
    
    public List<PositionSensor> getPositionSensors() {
        return positionsensors;
    }
    
    public boolean addPositionSensor(PositionSensor positionsensor) {
        return positionsensors.add(positionsensor);
    }
    public void setPositionSensors(List<PositionSensor> positionsensors) {
        this.positionsensors = positionsensors;
    }
    
    public List<EnvironmentSensor> getEnvironmnentSensors() {
        return environmentsensors;
    }
    public boolean addEnvironmentSensor(EnvironmentSensor environmentsensor) {
        return environmentsensors.add(environmentsensor);
    }
    public void setEnvironmentSensors(List<EnvironmentSensor> environmentsensors) {
        this.environmentsensors = environmentsensors;
    }
    
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        if(!(motionsensors==null))
        	sb.append( Arrays.toString(motionsensors.toArray())+ "\n");
        if(!(positionsensors==null))
        	sb.append( Arrays.toString(positionsensors.toArray())+ "\n");
        if(!(environmentsensors==null))
        	sb.append( Arrays.toString(environmentsensors.toArray())+ "\n");
        return sb.toString();
    }


}
