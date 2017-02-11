
package io.teevr.config;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@Generated("org.jsonschema2pojo")
public class CloudDB {

    /**
     * Influxdb Config
     * <p>
     * 
     * 
     */
    @SerializedName("influxdb")
    @Expose
    private Influxdb influxdb;
    /**
     * Postgres Database Configuration
     * <p>
     * 
     * 
     */
    @SerializedName("postgres")
    @Expose
    private Postgres postgres;

    /**
     * Influxdb Config
     * <p>
     * 
     * 
     * @return
     *     The influxdb
     */
    public Influxdb getInfluxdb() {
        return influxdb;
    }

    /**
     * Influxdb Config
     * <p>
     * 
     * 
     * @param influxdb
     *     The influxdb
     */
    public void setInfluxdb(Influxdb influxdb) {
        this.influxdb = influxdb;
    }

    /**
     * Postgres Database Configuration
     * <p>
     * 
     * 
     * @return
     *     The postgres
     */
    public Postgres getPostgres() {
        return postgres;
    }

    /**
     * Postgres Database Configuration
     * <p>
     * 
     * 
     * @param postgres
     *     The postgres
     */
    public void setPostgres(Postgres postgres) {
        this.postgres = postgres;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(influxdb).append(postgres).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof CloudDB) == false) {
            return false;
        }
        CloudDB rhs = ((CloudDB) other);
        return new EqualsBuilder().append(influxdb, rhs.influxdb).append(postgres, rhs.postgres).isEquals();
    }

}
