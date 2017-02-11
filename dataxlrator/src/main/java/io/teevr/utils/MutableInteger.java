/*
 * Copyright (c) 2015-16,  Teevr Data Inc. All Rights Reserved 
 */

package io.teevr.utils;

public class MutableInteger  implements Comparable {
	 
	  private int val;
	 
	  public MutableInteger(int val) {
	    this.val = val;
	  }
	 
	  public int get() {
	    return val;
	  }
	 
	  public void set(int val) {
	    this.val = val;
	  }

	@Override
	public int compareTo(Object o) {
		// TODO Auto-generated method stub
		if(val<((MutableInteger)o).get())
			return -1;
		if(val>((MutableInteger)o).get())
			return 1;
		return 0;
	}
	
	public boolean equals(Object obj){
	    
	    if (obj instanceof MutableInteger) {
	    	MutableInteger comparatorObj = (MutableInteger) obj;
	        return (comparatorObj.get()==this.get());
	    } else {
	        return false;
	    }
	 
	}
	   
	public int hashCode()
		  {
			  return Integer.hashCode(val);
		   }
	
	}
