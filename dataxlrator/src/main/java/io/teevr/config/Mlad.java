
package io.teevr.config;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Machine Learning Anomaly Detection
 * <p>
 * 
 * 
 */
@Generated("org.jsonschema2pojo")
public class Mlad {

    /**
     * Sample Size to be used for anomaly detection algorithm
     * <p>
     * 
     * 
     */
    @SerializedName("SampleSize")
    @Expose
    private Integer SampleSize;
    /**
     * Lower Threshold for event/anomaly detection 
     * <p>
     * 
     * 
     */
    @SerializedName("LowThreshold")
    @Expose
    private Integer LowThreshold;
    /**
     * Higher Threshold for event/anomaly detection
     * <p>
     * 
     * 
     */
    @SerializedName("HighThreshold")
    @Expose
    private Integer HighThreshold;
    /**
     * Flag to enable analytics data dump to console. To be used for debugging purposes only
     * <p>
     * 
     * 
     */
    @SerializedName("CollectData")
    @Expose
    private Boolean CollectData = false;

    /**
     * Sample Size to be used for anomaly detection algorithm
     * <p>
     * 
     * 
     * @return
     *     The SampleSize
     */
    public Integer getSampleSize() {
        return SampleSize;
    }

    /**
     * Sample Size to be used for anomaly detection algorithm
     * <p>
     * 
     * 
     * @param SampleSize
     *     The SampleSize
     */
    public void setSampleSize(Integer SampleSize) {
        this.SampleSize = SampleSize;
    }

    /**
     * Lower Threshold for event/anomaly detection 
     * <p>
     * 
     * 
     * @return
     *     The LowThreshold
     */
    public Integer getLowThreshold() {
        return LowThreshold;
    }

    /**
     * Lower Threshold for event/anomaly detection 
     * <p>
     * 
     * 
     * @param LowThreshold
     *     The LowThreshold
     */
    public void setLowThreshold(Integer LowThreshold) {
        this.LowThreshold = LowThreshold;
    }

    /**
     * Higher Threshold for event/anomaly detection
     * <p>
     * 
     * 
     * @return
     *     The HighThreshold
     */
    public Integer getHighThreshold() {
        return HighThreshold;
    }

    /**
     * Higher Threshold for event/anomaly detection
     * <p>
     * 
     * 
     * @param HighThreshold
     *     The HighThreshold
     */
    public void setHighThreshold(Integer HighThreshold) {
        this.HighThreshold = HighThreshold;
    }

    /**
     * Flag to enable analytics data dump to console. To be used for debugging purposes only
     * <p>
     * 
     * 
     * @return
     *     The CollectData
     */
    public Boolean getCollectData() {
        return CollectData;
    }

    /**
     * Flag to enable analytics data dump to console. To be used for debugging purposes only
     * <p>
     * 
     * 
     * @param CollectData
     *     The CollectData
     */
    public void setCollectData(Boolean CollectData) {
        this.CollectData = CollectData;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(SampleSize).append(LowThreshold).append(HighThreshold).append(CollectData).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Mlad) == false) {
            return false;
        }
        Mlad rhs = ((Mlad) other);
        return new EqualsBuilder().append(SampleSize, rhs.SampleSize).append(LowThreshold, rhs.LowThreshold).append(HighThreshold, rhs.HighThreshold).append(CollectData, rhs.CollectData).isEquals();
    }

}
