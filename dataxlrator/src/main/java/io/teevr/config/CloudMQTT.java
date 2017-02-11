
package io.teevr.config;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@Generated("org.jsonschema2pojo")
public class CloudMQTT {

    /**
     * MQTT Broker Address
     * <p>
     * 
     * 
     */
    @SerializedName("BrokerAddress")
    @Expose
    private String BrokerAddress = "";
    /**
     * Port
     * <p>
     * 
     * 
     */
    @SerializedName("Port")
    @Expose
    private Integer Port = 1883;
    /**
     * SNAPI Port
     * <p>
     * 
     * 
     */
    @SerializedName("SnapiPort")
    @Expose
    private Integer SnapiPort = 6067;
    /**
     * User Name
     * <p>
     * 
     * 
     */
    @SerializedName("Username")
    @Expose
    private String Username = "";
    /**
     * Password
     * <p>
     * 
     * 
     */
    @SerializedName("Password")
    @Expose
    private String Password = "";
    /**
     * Enable SSL
     * <p>
     * 
     * 
     */
    @SerializedName("EnableSSL")
    @Expose
    private Boolean EnableSSL = false;
    /**
     * Client ID
     * <p>
     * 
     * 
     */
    @SerializedName("ClientID")
    @Expose
    private String ClientID = "";
    /**
     * CA Cert
     * <p>
     * 
     * 
     */
    @SerializedName("CACert")
    @Expose
    private String CACert = "";
    /**
     * Client Cert
     * <p>
     * 
     * 
     */
    @SerializedName("ClientCert")
    @Expose
    private String ClientCert = "";
    /**
     * Client Key
     * <p>
     * 
     * 
     */
    @SerializedName("ClientKey")
    @Expose
    private String ClientKey = "";
    /**
     * MQTT Subscription Topics
     * <p>
     * 
     * 
     */
    @SerializedName("SubscribeTopics")
    @Expose
    private List<String> SubscribeTopics = new ArrayList<String>();
    /**
     * MQTT Topic to be Published
     * <p>
     * 
     * 
     */
    @SerializedName("PublishTopics")
    @Expose
    private List<String> PublishTopics = new ArrayList<String>();

    /**
     * MQTT Broker Address
     * <p>
     * 
     * 
     * @return
     *     The BrokerAddress
     */
    public String getBrokerAddress() {
        return BrokerAddress;
    }

    /**
     * MQTT Broker Address
     * <p>
     * 
     * 
     * @param BrokerAddress
     *     The BrokerAddress
     */
    public void setBrokerAddress(String BrokerAddress) {
        this.BrokerAddress = BrokerAddress;
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

    /**
     * SNAPI Port
     * <p>
     * 
     * 
     * @return
     *     The SnapiPort
     */
    public Integer getSnapiPort() {
        return SnapiPort;
    }

    /**
     * SNAPI Port
     * <p>
     * 
     * 
     * @param SnapiPort
     *     The SnapiPort
     */
    public void setSnapiPort(Integer SnapiPort) {
        this.SnapiPort = SnapiPort;
    }

    /**
     * User Name
     * <p>
     * 
     * 
     * @return
     *     The Username
     */
    public String getUsername() {
        return Username;
    }

    /**
     * User Name
     * <p>
     * 
     * 
     * @param Username
     *     The Username
     */
    public void setUsername(String Username) {
        this.Username = Username;
    }

    /**
     * Password
     * <p>
     * 
     * 
     * @return
     *     The Password
     */
    public String getPassword() {
        return Password;
    }

    /**
     * Password
     * <p>
     * 
     * 
     * @param Password
     *     The Password
     */
    public void setPassword(String Password) {
        this.Password = Password;
    }

    /**
     * Enable SSL
     * <p>
     * 
     * 
     * @return
     *     The EnableSSL
     */
    public Boolean getEnableSSL() {
        return EnableSSL;
    }

    /**
     * Enable SSL
     * <p>
     * 
     * 
     * @param EnableSSL
     *     The EnableSSL
     */
    public void setEnableSSL(Boolean EnableSSL) {
        this.EnableSSL = EnableSSL;
    }

    /**
     * Client ID
     * <p>
     * 
     * 
     * @return
     *     The ClientID
     */
    public String getClientID() {
        return ClientID;
    }

    /**
     * Client ID
     * <p>
     * 
     * 
     * @param ClientID
     *     The ClientID
     */
    public void setClientID(String ClientID) {
        this.ClientID = ClientID;
    }

    /**
     * CA Cert
     * <p>
     * 
     * 
     * @return
     *     The CACert
     */
    public String getCACert() {
        return CACert;
    }

    /**
     * CA Cert
     * <p>
     * 
     * 
     * @param CACert
     *     The CACert
     */
    public void setCACert(String CACert) {
        this.CACert = CACert;
    }

    /**
     * Client Cert
     * <p>
     * 
     * 
     * @return
     *     The ClientCert
     */
    public String getClientCert() {
        return ClientCert;
    }

    /**
     * Client Cert
     * <p>
     * 
     * 
     * @param ClientCert
     *     The ClientCert
     */
    public void setClientCert(String ClientCert) {
        this.ClientCert = ClientCert;
    }

    /**
     * Client Key
     * <p>
     * 
     * 
     * @return
     *     The ClientKey
     */
    public String getClientKey() {
        return ClientKey;
    }

    /**
     * Client Key
     * <p>
     * 
     * 
     * @param ClientKey
     *     The ClientKey
     */
    public void setClientKey(String ClientKey) {
        this.ClientKey = ClientKey;
    }

    /**
     * MQTT Subscription Topics
     * <p>
     * 
     * 
     * @return
     *     The SubscribeTopics
     */
    public List<String> getSubscribeTopics() {
        return SubscribeTopics;
    }

    /**
     * MQTT Subscription Topics
     * <p>
     * 
     * 
     * @param SubscribeTopics
     *     The SubscribeTopics
     */
    public void setSubscribeTopics(List<String> SubscribeTopics) {
        this.SubscribeTopics = SubscribeTopics;
    }

    /**
     * MQTT Topic to be Published
     * <p>
     * 
     * 
     * @return
     *     The PublishTopics
     */
    public List<String> getPublishTopics() {
        return PublishTopics;
    }

    /**
     * MQTT Topic to be Published
     * <p>
     * 
     * 
     * @param PublishTopics
     *     The PublishTopics
     */
    public void setPublishTopics(List<String> PublishTopics) {
        this.PublishTopics = PublishTopics;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(BrokerAddress).append(Port).append(SnapiPort).append(Username).append(Password).append(EnableSSL).append(ClientID).append(CACert).append(ClientCert).append(ClientKey).append(SubscribeTopics).append(PublishTopics).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof CloudMQTT) == false) {
            return false;
        }
        CloudMQTT rhs = ((CloudMQTT) other);
        return new EqualsBuilder().append(BrokerAddress, rhs.BrokerAddress).append(Port, rhs.Port).append(SnapiPort, rhs.SnapiPort).append(Username, rhs.Username).append(Password, rhs.Password).append(EnableSSL, rhs.EnableSSL).append(ClientID, rhs.ClientID).append(CACert, rhs.CACert).append(ClientCert, rhs.ClientCert).append(ClientKey, rhs.ClientKey).append(SubscribeTopics, rhs.SubscribeTopics).append(PublishTopics, rhs.PublishTopics).isEquals();
    }

}
