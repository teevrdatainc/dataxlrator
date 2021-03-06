
package io.teevr.config;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Influxdb Config
 * <p>
 * 
 * 
 */
@Generated("org.jsonschema2pojo")
public class Influxdb {

    /**
     * Server Address
     * <p>
     * 
     * 
     */
    @SerializedName("ServerAddress")
    @Expose
    private String ServerAddress;
    /**
     * HTTP Port Number
     * <p>
     * 
     * 
     */
    @SerializedName("HTTPPort")
    @Expose
    private Integer HTTPPort;
    /**
     * User Name
     * <p>
     * 
     * 
     */
    @SerializedName("username")
    @Expose
    private String username;
    /**
     * Password
     * <p>
     * 
     * 
     */
    @SerializedName("password")
    @Expose
    private String password;
    /**
     * Performance Database
     * <p>
     * 
     * 
     */
    @SerializedName("PerfDBName")
    @Expose
    private String PerfDBName;
    /**
     * Events Database
     * <p>
     * 
     * 
     */
    @SerializedName("EventsDBName")
    @Expose
    private String EventsDBName;
    /**
     * Sensor Data Database
     * <p>
     * 
     * 
     */
    @SerializedName("DataDBName")
    @Expose
    private String DataDBName;

    /**
     * Server Address
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
     * Server Address
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
     * HTTP Port Number
     * <p>
     * 
     * 
     * @return
     *     The HTTPPort
     */
    public Integer getHTTPPort() {
        return HTTPPort;
    }

    /**
     * HTTP Port Number
     * <p>
     * 
     * 
     * @param HTTPPort
     *     The HTTPPort
     */
    public void setHTTPPort(Integer HTTPPort) {
        this.HTTPPort = HTTPPort;
    }

    /**
     * User Name
     * <p>
     * 
     * 
     * @return
     *     The username
     */
    public String getUsername() {
        return username;
    }

    /**
     * User Name
     * <p>
     * 
     * 
     * @param username
     *     The username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Password
     * <p>
     * 
     * 
     * @return
     *     The password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Password
     * <p>
     * 
     * 
     * @param password
     *     The password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Performance Database
     * <p>
     * 
     * 
     * @return
     *     The PerfDBName
     */
    public String getPerfDBName() {
        return PerfDBName;
    }

    /**
     * Performance Database
     * <p>
     * 
     * 
     * @param PerfDBName
     *     The PerfDBName
     */
    public void setPerfDBName(String PerfDBName) {
        this.PerfDBName = PerfDBName;
    }

    /**
     * Events Database
     * <p>
     * 
     * 
     * @return
     *     The EventsDBName
     */
    public String getEventsDBName() {
        return EventsDBName;
    }

    /**
     * Events Database
     * <p>
     * 
     * 
     * @param EventsDBName
     *     The EventsDBName
     */
    public void setEventsDBName(String EventsDBName) {
        this.EventsDBName = EventsDBName;
    }

    /**
     * Sensor Data Database
     * <p>
     * 
     * 
     * @return
     *     The DataDBName
     */
    public String getDataDBName() {
        return DataDBName;
    }

    /**
     * Sensor Data Database
     * <p>
     * 
     * 
     * @param DataDBName
     *     The DataDBName
     */
    public void setDataDBName(String DataDBName) {
        this.DataDBName = DataDBName;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(ServerAddress).append(HTTPPort).append(username).append(password).append(PerfDBName).append(EventsDBName).append(DataDBName).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Influxdb) == false) {
            return false;
        }
        Influxdb rhs = ((Influxdb) other);
        return new EqualsBuilder().append(ServerAddress, rhs.ServerAddress).append(HTTPPort, rhs.HTTPPort).append(username, rhs.username).append(password, rhs.password).append(PerfDBName, rhs.PerfDBName).append(EventsDBName, rhs.EventsDBName).append(DataDBName, rhs.DataDBName).isEquals();
    }

}
