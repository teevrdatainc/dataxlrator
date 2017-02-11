/*
 * Copyright (c) 2015-16,  Teevr Data Inc. All Rights Reserved 
 */

package io.teevr.cep;

public class AcceleroGyro {
    double x, y, z,ax,ay,az;  // gyroscope coordinates then accelerometer
 //   float ax,ay,az; //accelrometer coordinates
    int index;

    public AcceleroGyro()
    {
      //To do later	
    }
    
    public AcceleroGyro(double x, double y, double z,double ax, double ay, double az) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.ax=ax;
        this.ay=ay;
        this.az=az;
        index=0;
    }

    public AcceleroGyro(double x, double y, double z,double ax, double ay, double az, int index) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.ax=ax;
        this.ay=ay;
        this.az=az;
        this.index = index;
    }
    public double getX() {return x;}
    public double getY() {return y;}
    public double getZ() {return z;}
    public double getAx() {return ax;}
    public double getAy() {return ay;}
    public double getAz() {return az;}
    public int getIndex() {return index;}

    @Override
    public String toString() {
        return "Gyroscope: " + x + ":" + y + ":" + z +  ax + ":" + ay + ":" + az;
    }
}
