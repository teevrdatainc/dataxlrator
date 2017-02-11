
package io.teevr.config;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * MQTT
 * <p>
 * 
 * 
 */
@Generated("org.jsonschema2pojo")
public class MQTT {

    @SerializedName("CloudMQTT")
    @Expose
    private io.teevr.config.CloudMQTT CloudMQTT;
    @SerializedName("EdgeMQTT")
    @Expose
    private io.teevr.config.CloudMQTT EdgeMQTT;
    @SerializedName("MonitorMQTT")
    @Expose
    private io.teevr.config.CloudMQTT MonitorMQTT;
    @SerializedName("MgmtMQTT")
    @Expose
    private io.teevr.config.CloudMQTT MgmtMQTT;

    /**
     * 
     * @return
     *     The CloudMQTT
     */
    public io.teevr.config.CloudMQTT getCloudMQTT() {
        return CloudMQTT;
    }

    /**
     * 
     * @param CloudMQTT
     *     The CloudMQTT
     */
    public void setCloudMQTT(io.teevr.config.CloudMQTT CloudMQTT) {
        this.CloudMQTT = CloudMQTT;
    }

    /**
     * 
     * @return
     *     The EdgeMQTT
     */
    public io.teevr.config.CloudMQTT getEdgeMQTT() {
        return EdgeMQTT;
    }

    /**
     * 
     * @param EdgeMQTT
     *     The EdgeMQTT
     */
    public void setEdgeMQTT(io.teevr.config.CloudMQTT EdgeMQTT) {
        this.EdgeMQTT = EdgeMQTT;
    }

    /**
     * 
     * @return
     *     The MonitorMQTT
     */
    public io.teevr.config.CloudMQTT getMonitorMQTT() {
        return MonitorMQTT;
    }

    /**
     * 
     * @param MonitorMQTT
     *     The MonitorMQTT
     */
    public void setMonitorMQTT(io.teevr.config.CloudMQTT MonitorMQTT) {
        this.MonitorMQTT = MonitorMQTT;
    }

    /**
     * 
     * @return
     *     The MgmtMQTT
     */
    public io.teevr.config.CloudMQTT getMgmtMQTT() {
        return MgmtMQTT;
    }

    /**
     * 
     * @param MgmtMQTT
     *     The MgmtMQTT
     */
    public void setMgmtMQTT(io.teevr.config.CloudMQTT MgmtMQTT) {
        this.MgmtMQTT = MgmtMQTT;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(CloudMQTT).append(EdgeMQTT).append(MonitorMQTT).append(MgmtMQTT).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof MQTT) == false) {
            return false;
        }
        MQTT rhs = ((MQTT) other);
        return new EqualsBuilder().append(CloudMQTT, rhs.CloudMQTT).append(EdgeMQTT, rhs.EdgeMQTT).append(MonitorMQTT, rhs.MonitorMQTT).append(MgmtMQTT, rhs.MgmtMQTT).isEquals();
    }

}
