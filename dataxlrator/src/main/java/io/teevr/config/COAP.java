
package io.teevr.config;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * COAP Client Configuration to be used by edge or external data sources like data generator
 * <p>
 * 
 * 
 */
@Generated("org.jsonschema2pojo")
public class COAP {

    @SerializedName("CloudCOAP")
    @Expose
    private io.teevr.config.CloudCOAP CloudCOAP;
    @SerializedName("EdgeCOAP")
    @Expose
    private io.teevr.config.CloudCOAP EdgeCOAP;

    /**
     * 
     * @return
     *     The CloudCOAP
     */
    public io.teevr.config.CloudCOAP getCloudCOAP() {
        return CloudCOAP;
    }

    /**
     * 
     * @param CloudCOAP
     *     The CloudCOAP
     */
    public void setCloudCOAP(io.teevr.config.CloudCOAP CloudCOAP) {
        this.CloudCOAP = CloudCOAP;
    }

    /**
     * 
     * @return
     *     The EdgeCOAP
     */
    public io.teevr.config.CloudCOAP getEdgeCOAP() {
        return EdgeCOAP;
    }

    /**
     * 
     * @param EdgeCOAP
     *     The EdgeCOAP
     */
    public void setEdgeCOAP(io.teevr.config.CloudCOAP EdgeCOAP) {
        this.EdgeCOAP = EdgeCOAP;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(CloudCOAP).append(EdgeCOAP).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof COAP) == false) {
            return false;
        }
        COAP rhs = ((COAP) other);
        return new EqualsBuilder().append(CloudCOAP, rhs.CloudCOAP).append(EdgeCOAP, rhs.EdgeCOAP).isEquals();
    }

}
