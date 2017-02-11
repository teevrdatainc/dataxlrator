
package io.teevr.config;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@Generated("org.jsonschema2pojo")
public class Combo {

    @SerializedName("timeStamp")
    @Expose
    private Object timeStamp;
    @SerializedName("compressed")
    @Expose
    private Boolean compressed;
    @SerializedName("data")
    @Expose
    private String data;

    /**
     * 
     * @return
     *     The timeStamp
     */
    public Object getTimeStamp() {
        return timeStamp;
    }

    /**
     * 
     * @param timeStamp
     *     The timeStamp
     */
    public void setTimeStamp(Object timeStamp) {
        this.timeStamp = timeStamp;
    }

    /**
     * 
     * @return
     *     The compressed
     */
    public Boolean getCompressed() {
        return compressed;
    }

    /**
     * 
     * @param compressed
     *     The compressed
     */
    public void setCompressed(Boolean compressed) {
        this.compressed = compressed;
    }

    /**
     * 
     * @return
     *     The data
     */
    public String getData() {
        return data;
    }

    /**
     * 
     * @param data
     *     The data
     */
    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(timeStamp).append(compressed).append(data).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Combo) == false) {
            return false;
        }
        Combo rhs = ((Combo) other);
        return new EqualsBuilder().append(timeStamp, rhs.timeStamp).append(compressed, rhs.compressed).append(data, rhs.data).isEquals();
    }

}
