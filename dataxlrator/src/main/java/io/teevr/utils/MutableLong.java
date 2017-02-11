/*
 * Copyright (c) 2015-16,  Teevr Data Inc. All Rights Reserved 
 */

package io.teevr.utils;

public class MutableLong  implements Comparable{
	 
	  private long val;
	 
	  public MutableLong(long val) {
	    this.val = val;
	  }
	 
	  public long get() {
	    return val;
	  }
	 
	  public void set(long val) {
	    this.val = val;
	  }
@Override
		public int compareTo(Object o) {
			// TODO Auto-generated method stub
			if(val<((MutableLong)o).get())
				return -1;
			if(val>((MutableLong)o).get())
				return 1;
			return 0;
		}

public boolean equals(Object obj){
    
    if (obj instanceof MutableLong) {
    	MutableLong comparatorObj = (MutableLong) obj;
    	boolean ret=(comparatorObj.get()==this.get());
        return ret;
    } else {
        return false;
    }
}
public int hashCode()
{
	  return Long.hashCode(val);
 }


	}
