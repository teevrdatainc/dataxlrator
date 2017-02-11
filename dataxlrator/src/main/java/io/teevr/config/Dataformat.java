
package io.teevr.config;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Data Format file for Teevr Data Platform
 * 
 */
@Generated("org.jsonschema2pojo")
public class Dataformat {

    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("sensorsData")
    @Expose
    private SensorsData sensorsData;

    /**
     * 
     * (Required)
     * 
     * @return
     *     The sensorsData
     */
    public SensorsData getSensorsData() {
        return sensorsData;
    }

    /**
     * 
     * (Required)
     * 
     * @param sensorsData
     *     The sensorsData
     */
    public void setSensorsData(SensorsData sensorsData) {
        this.sensorsData = sensorsData;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(sensorsData).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Dataformat) == false) {
            return false;
        }
        Dataformat rhs = ((Dataformat) other);
        return new EqualsBuilder().append(sensorsData, rhs.sensorsData).isEquals();
    }

}
