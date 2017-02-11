
package io.teevr.config;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * EdgeWare Configuration
 * <p>
 * 
 * 
 */
@Generated("org.jsonschema2pojo")
public class Configuration {

    /**
     * MQTT
     * <p>
     * 
     * (Required)
     * 
     */
    @SerializedName("MQTT")
    @Expose
    private io.teevr.config.MQTT MQTT;
    /**
     * COAP Client Configuration to be used by edge or external data sources like data generator
     * <p>
     * 
     * 
     */
    @SerializedName("COAP")
    @Expose
    private io.teevr.config.COAP COAP;
    @SerializedName("CloudDB")
    @Expose
    private io.teevr.config.CloudDB CloudDB;
    @SerializedName("EdgeDB")
    @Expose
    private io.teevr.config.EdgeDB EdgeDB;
    /**
     * Advanced
     * <p>
     * 
     * (Required)
     * 
     */
    @SerializedName("advanced")
    @Expose
    private Advanced advanced;
    /**
     * Compression
     * <p>
     * 
     * 
     */
    @SerializedName("compression")
    @Expose
    private Compression compression;
    /**
     * Machine Learning Anomaly Detection
     * <p>
     * 
     * 
     */
    @SerializedName("mlad")
    @Expose
    private Mlad mlad;
    /**
     * Websockets
     * <p>
     * 
     * 
     */
    @SerializedName("websockets")
    @Expose
    private Websockets websockets;

    /**
     * MQTT
     * <p>
     * 
     * (Required)
     * 
     * @return
     *     The MQTT
     */
    public io.teevr.config.MQTT getMQTT() {
        return MQTT;
    }

    /**
     * MQTT
     * <p>
     * 
     * (Required)
     * 
     * @param MQTT
     *     The MQTT
     */
    public void setMQTT(io.teevr.config.MQTT MQTT) {
        this.MQTT = MQTT;
    }

    /**
     * COAP Client Configuration to be used by edge or external data sources like data generator
     * <p>
     * 
     * 
     * @return
     *     The COAP
     */
    public io.teevr.config.COAP getCOAP() {
        return COAP;
    }

    /**
     * COAP Client Configuration to be used by edge or external data sources like data generator
     * <p>
     * 
     * 
     * @param COAP
     *     The COAP
     */
    public void setCOAP(io.teevr.config.COAP COAP) {
        this.COAP = COAP;
    }

    /**
     * 
     * @return
     *     The CloudDB
     */
    public io.teevr.config.CloudDB getCloudDB() {
        return CloudDB;
    }

    /**
     * 
     * @param CloudDB
     *     The CloudDB
     */
    public void setCloudDB(io.teevr.config.CloudDB CloudDB) {
        this.CloudDB = CloudDB;
    }

    /**
     * 
     * @return
     *     The EdgeDB
     */
    public io.teevr.config.EdgeDB getEdgeDB() {
        return EdgeDB;
    }

    /**
     * 
     * @param EdgeDB
     *     The EdgeDB
     */
    public void setEdgeDB(io.teevr.config.EdgeDB EdgeDB) {
        this.EdgeDB = EdgeDB;
    }

    /**
     * Advanced
     * <p>
     * 
     * (Required)
     * 
     * @return
     *     The advanced
     */
    public Advanced getAdvanced() {
        return advanced;
    }

    /**
     * Advanced
     * <p>
     * 
     * (Required)
     * 
     * @param advanced
     *     The advanced
     */
    public void setAdvanced(Advanced advanced) {
        this.advanced = advanced;
    }

    /**
     * Compression
     * <p>
     * 
     * 
     * @return
     *     The compression
     */
    public Compression getCompression() {
        return compression;
    }

    /**
     * Compression
     * <p>
     * 
     * 
     * @param compression
     *     The compression
     */
    public void setCompression(Compression compression) {
        this.compression = compression;
    }

    /**
     * Machine Learning Anomaly Detection
     * <p>
     * 
     * 
     * @return
     *     The mlad
     */
    public Mlad getMlad() {
        return mlad;
    }

    /**
     * Machine Learning Anomaly Detection
     * <p>
     * 
     * 
     * @param mlad
     *     The mlad
     */
    public void setMlad(Mlad mlad) {
        this.mlad = mlad;
    }

    /**
     * Websockets
     * <p>
     * 
     * 
     * @return
     *     The websockets
     */
    public Websockets getWebsockets() {
        return websockets;
    }

    /**
     * Websockets
     * <p>
     * 
     * 
     * @param websockets
     *     The websockets
     */
    public void setWebsockets(Websockets websockets) {
        this.websockets = websockets;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(MQTT).append(COAP).append(CloudDB).append(EdgeDB).append(advanced).append(compression).append(mlad).append(websockets).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Configuration) == false) {
            return false;
        }
        Configuration rhs = ((Configuration) other);
        return new EqualsBuilder().append(MQTT, rhs.MQTT).append(COAP, rhs.COAP).append(CloudDB, rhs.CloudDB).append(EdgeDB, rhs.EdgeDB).append(advanced, rhs.advanced).append(compression, rhs.compression).append(mlad, rhs.mlad).append(websockets, rhs.websockets).isEquals();
    }

}
