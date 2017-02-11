
package io.teevr.config;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@Generated("org.jsonschema2pojo")
public class CloudCOAP {

    /**
     * COAP Server Address
     * <p>
     * 
     * 
     */
    @SerializedName("ServerAddress")
    @Expose
    private String ServerAddress = "";
    /**
     * COAP Server Port
     * <p>
     * 
     * 
     */
    @SerializedName("Port")
    @Expose
    private Integer Port = 5683;
    /**
     * Use COAP over tcp
     * <p>
     * 
     * 
     */
    @SerializedName("UseTCP")
    @Expose
    private Boolean UseTCP = true;
    /**
     * Enable TLS
     * <p>
     * 
     * 
     */
    @SerializedName("EnableTLS")
    @Expose
    private Boolean EnableTLS = false;
    /**
     * Trust Store Password
     * <p>
     * 
     * 
     */
    @SerializedName("TrustStorePassword")
    @Expose
    private String TrustStorePassword = "";
    /**
     * Trust Store
     * <p>
     * 
     * 
     */
    @SerializedName("TrustStore")
    @Expose
    private String TrustStore = "";
    /**
     * Key Store Password
     * <p>
     * 
     * 
     */
    @SerializedName("KeyStorePassword")
    @Expose
    private String KeyStorePassword = "";
    /**
     * Key Store
     * <p>
     * 
     * 
     */
    @SerializedName("KeyStore")
    @Expose
    private String KeyStore = "";

    /**
     * COAP Server Address
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
     * COAP Server Address
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
     * COAP Server Port
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
     * COAP Server Port
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
     * Use COAP over tcp
     * <p>
     * 
     * 
     * @return
     *     The UseTCP
     */
    public Boolean getUseTCP() {
        return UseTCP;
    }

    /**
     * Use COAP over tcp
     * <p>
     * 
     * 
     * @param UseTCP
     *     The UseTCP
     */
    public void setUseTCP(Boolean UseTCP) {
        this.UseTCP = UseTCP;
    }

    /**
     * Enable TLS
     * <p>
     * 
     * 
     * @return
     *     The EnableTLS
     */
    public Boolean getEnableTLS() {
        return EnableTLS;
    }

    /**
     * Enable TLS
     * <p>
     * 
     * 
     * @param EnableTLS
     *     The EnableTLS
     */
    public void setEnableTLS(Boolean EnableTLS) {
        this.EnableTLS = EnableTLS;
    }

    /**
     * Trust Store Password
     * <p>
     * 
     * 
     * @return
     *     The TrustStorePassword
     */
    public String getTrustStorePassword() {
        return TrustStorePassword;
    }

    /**
     * Trust Store Password
     * <p>
     * 
     * 
     * @param TrustStorePassword
     *     The TrustStorePassword
     */
    public void setTrustStorePassword(String TrustStorePassword) {
        this.TrustStorePassword = TrustStorePassword;
    }

    /**
     * Trust Store
     * <p>
     * 
     * 
     * @return
     *     The TrustStore
     */
    public String getTrustStore() {
        return TrustStore;
    }

    /**
     * Trust Store
     * <p>
     * 
     * 
     * @param TrustStore
     *     The TrustStore
     */
    public void setTrustStore(String TrustStore) {
        this.TrustStore = TrustStore;
    }

    /**
     * Key Store Password
     * <p>
     * 
     * 
     * @return
     *     The KeyStorePassword
     */
    public String getKeyStorePassword() {
        return KeyStorePassword;
    }

    /**
     * Key Store Password
     * <p>
     * 
     * 
     * @param KeyStorePassword
     *     The KeyStorePassword
     */
    public void setKeyStorePassword(String KeyStorePassword) {
        this.KeyStorePassword = KeyStorePassword;
    }

    /**
     * Key Store
     * <p>
     * 
     * 
     * @return
     *     The KeyStore
     */
    public String getKeyStore() {
        return KeyStore;
    }

    /**
     * Key Store
     * <p>
     * 
     * 
     * @param KeyStore
     *     The KeyStore
     */
    public void setKeyStore(String KeyStore) {
        this.KeyStore = KeyStore;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(ServerAddress).append(Port).append(UseTCP).append(EnableTLS).append(TrustStorePassword).append(TrustStore).append(KeyStorePassword).append(KeyStore).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof CloudCOAP) == false) {
            return false;
        }
        CloudCOAP rhs = ((CloudCOAP) other);
        return new EqualsBuilder().append(ServerAddress, rhs.ServerAddress).append(Port, rhs.Port).append(UseTCP, rhs.UseTCP).append(EnableTLS, rhs.EnableTLS).append(TrustStorePassword, rhs.TrustStorePassword).append(TrustStore, rhs.TrustStore).append(KeyStorePassword, rhs.KeyStorePassword).append(KeyStore, rhs.KeyStore).isEquals();
    }

}
