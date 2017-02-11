
package io.teevr.config;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Websockets
 * <p>
 * 
 * 
 */
@Generated("org.jsonschema2pojo")
public class Websockets {

    /**
     * Enable Websockets for publish data
     * <p>
     * 
     * 
     */
    @SerializedName("EnableWebsockets")
    @Expose
    private Boolean EnableWebsockets = true;
    /**
     * Websockets Server Address
     * <p>
     * 
     * 
     */
    @SerializedName("ServerAddress")
    @Expose
    private String ServerAddress = "127.0.0.1";
    /**
     * Port
     * <p>
     * 
     * 
     */
    @SerializedName("Port")
    @Expose
    private Integer Port = 8887;

    /**
     * Enable Websockets for publish data
     * <p>
     * 
     * 
     * @return
     *     The EnableWebsockets
     */
    public Boolean getEnableWebsockets() {
        return EnableWebsockets;
    }

    /**
     * Enable Websockets for publish data
     * <p>
     * 
     * 
     * @param EnableWebsockets
     *     The EnableWebsockets
     */
    public void setEnableWebsockets(Boolean EnableWebsockets) {
        this.EnableWebsockets = EnableWebsockets;
    }

    /**
     * Websockets Server Address
     * <p>
     * 
     * 
     * @return
     *     The ServerAddress
     */
    public String getServerAddress() {
        return ServerAddress;
    }

    /**
     * Websockets Server Address
     * <p>
     * 
     * 
     * @param ServerAddress
     *     The ServerAddress
     */
    public void setServerAddress(String ServerAddress) {
        this.ServerAddress = ServerAddress;
    }

    /**
     * Port
     * <p>
     * 
     * 
     * @return
     *     The Port
     */
    public Integer getPort() {
        return Port;
    }

    /**
     * Port
     * <p>
     * 
     * 
     * @param Port
     *     The Port
     */
    public void setPort(Integer Port) {
        this.Port = Port;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(EnableWebsockets).append(ServerAddress).append(Port).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Websockets) == false) {
            return false;
        }
        Websockets rhs = ((Websockets) other);
        return new EqualsBuilder().append(EnableWebsockets, rhs.EnableWebsockets).append(ServerAddress, rhs.ServerAddress).append(Port, rhs.Port).isEquals();
    }

}
