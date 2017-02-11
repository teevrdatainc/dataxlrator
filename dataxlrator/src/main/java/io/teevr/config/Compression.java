
package io.teevr.config;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Compression
 * <p>
 * 
 * 
 */
@Generated("org.jsonschema2pojo")
public class Compression {

    /**
     * Sample Size to be used for Symbol Generation
     * <p>
     * 
     * 
     */
    @SerializedName("SampleSize")
    @Expose
    private Integer SampleSize;
    /**
     * Bits size at lower end
     * <p>
     * 
     * 
     */
    @SerializedName("LowBitsSize")
    @Expose
    private Integer LowBitsSize;
    /**
     * Bits size at higher end
     * <p>
     * 
     * 
     */
    @SerializedName("HighBitsSize")
    @Expose
    private Integer HighBitsSize;
    /**
     * Maximum number of decimal points supported
     * <p>
     * 
     * 
     */
    @SerializedName("MaxPrecision")
    @Expose
    private Integer MaxPrecision;
    /**
     * Use Prefix Encoding in compression
     * <p>
     * 
     * 
     */
    @SerializedName("UsePfxEncoding")
    @Expose
    private Boolean UsePfxEncoding;
    /**
     * Enable Batch mode for compression. Instead of compressing and sending every sample, consolidate batch numsamples for compression
     * <p>
     * 
     * 
     */
    @SerializedName("EnableBatch")
    @Expose
    private Boolean EnableBatch;
    /**
     * Number of samples to be consolidated before compressing and sending it across.
     * <p>
     * 
     * 
     */
    @SerializedName("BatchSize")
    @Expose
    private Integer BatchSize;
    /**
     * Sample Size to be used for Linear and Polynomial Regression 
     * <p>
     * 
     * 
     */
    @SerializedName("RegressionSampleSize")
    @Expose
    private Integer RegressionSampleSize;
    /**
     * Enable Linear Regression
     * <p>
     * 
     * 
     */
    @SerializedName("EnableLinearRegression")
    @Expose
    private Boolean EnableLinearRegression;
    /**
     * Enable Polynomial Regression
     * <p>
     * 
     * 
     */
    @SerializedName("EnablePolyRegression")
    @Expose
    private Boolean EnablePolyRegression;

    /**
     * Sample Size to be used for Symbol Generation
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
     * Sample Size to be used for Symbol Generation
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
     * Bits size at lower end
     * <p>
     * 
     * 
     * @return
     *     The LowBitsSize
     */
    public Integer getLowBitsSize() {
        return LowBitsSize;
    }

    /**
     * Bits size at lower end
     * <p>
     * 
     * 
     * @param LowBitsSize
     *     The LowBitsSize
     */
    public void setLowBitsSize(Integer LowBitsSize) {
        this.LowBitsSize = LowBitsSize;
    }

    /**
     * Bits size at higher end
     * <p>
     * 
     * 
     * @return
     *     The HighBitsSize
     */
    public Integer getHighBitsSize() {
        return HighBitsSize;
    }

    /**
     * Bits size at higher end
     * <p>
     * 
     * 
     * @param HighBitsSize
     *     The HighBitsSize
     */
    public void setHighBitsSize(Integer HighBitsSize) {
        this.HighBitsSize = HighBitsSize;
    }

    /**
     * Maximum number of decimal points supported
     * <p>
     * 
     * 
     * @return
     *     The MaxPrecision
     */
    public Integer getMaxPrecision() {
        return MaxPrecision;
    }

    /**
     * Maximum number of decimal points supported
     * <p>
     * 
     * 
     * @param MaxPrecision
     *     The MaxPrecision
     */
    public void setMaxPrecision(Integer MaxPrecision) {
        this.MaxPrecision = MaxPrecision;
    }

    /**
     * Use Prefix Encoding in compression
     * <p>
     * 
     * 
     * @return
     *     The UsePfxEncoding
     */
    public Boolean getUsePfxEncoding() {
        return UsePfxEncoding;
    }

    /**
     * Use Prefix Encoding in compression
     * <p>
     * 
     * 
     * @param UsePfxEncoding
     *     The UsePfxEncoding
     */
    public void setUsePfxEncoding(Boolean UsePfxEncoding) {
        this.UsePfxEncoding = UsePfxEncoding;
    }

    /**
     * Enable Batch mode for compression. Instead of compressing and sending every sample, consolidate batch numsamples for compression
     * <p>
     * 
     * 
     * @return
     *     The EnableBatch
     */
    public Boolean getEnableBatch() {
        return EnableBatch;
    }

    /**
     * Enable Batch mode for compression. Instead of compressing and sending every sample, consolidate batch numsamples for compression
     * <p>
     * 
     * 
     * @param EnableBatch
     *     The EnableBatch
     */
    public void setEnableBatch(Boolean EnableBatch) {
        this.EnableBatch = EnableBatch;
    }

    /**
     * Number of samples to be consolidated before compressing and sending it across.
     * <p>
     * 
     * 
     * @return
     *     The BatchSize
     */
    public Integer getBatchSize() {
        return BatchSize;
    }

    /**
     * Number of samples to be consolidated before compressing and sending it across.
     * <p>
     * 
     * 
     * @param BatchSize
     *     The BatchSize
     */
    public void setBatchSize(Integer BatchSize) {
        this.BatchSize = BatchSize;
    }

    /**
     * Sample Size to be used for Linear and Polynomial Regression 
     * <p>
     * 
     * 
     * @return
     *     The RegressionSampleSize
     */
    public Integer getRegressionSampleSize() {
        return RegressionSampleSize;
    }

    /**
     * Sample Size to be used for Linear and Polynomial Regression 
     * <p>
     * 
     * 
     * @param RegressionSampleSize
     *     The RegressionSampleSize
     */
    public void setRegressionSampleSize(Integer RegressionSampleSize) {
        this.RegressionSampleSize = RegressionSampleSize;
    }

    /**
     * Enable Linear Regression
     * <p>
     * 
     * 
     * @return
     *     The EnableLinearRegression
     */
    public Boolean getEnableLinearRegression() {
        return EnableLinearRegression;
    }

    /**
     * Enable Linear Regression
     * <p>
     * 
     * 
     * @param EnableLinearRegression
     *     The EnableLinearRegression
     */
    public void setEnableLinearRegression(Boolean EnableLinearRegression) {
        this.EnableLinearRegression = EnableLinearRegression;
    }

    /**
     * Enable Polynomial Regression
     * <p>
     * 
     * 
     * @return
     *     The EnablePolyRegression
     */
    public Boolean getEnablePolyRegression() {
        return EnablePolyRegression;
    }

    /**
     * Enable Polynomial Regression
     * <p>
     * 
     * 
     * @param EnablePolyRegression
     *     The EnablePolyRegression
     */
    public void setEnablePolyRegression(Boolean EnablePolyRegression) {
        this.EnablePolyRegression = EnablePolyRegression;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(SampleSize).append(LowBitsSize).append(HighBitsSize).append(MaxPrecision).append(UsePfxEncoding).append(EnableBatch).append(BatchSize).append(RegressionSampleSize).append(EnableLinearRegression).append(EnablePolyRegression).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Compression) == false) {
            return false;
        }
        Compression rhs = ((Compression) other);
        return new EqualsBuilder().append(SampleSize, rhs.SampleSize).append(LowBitsSize, rhs.LowBitsSize).append(HighBitsSize, rhs.HighBitsSize).append(MaxPrecision, rhs.MaxPrecision).append(UsePfxEncoding, rhs.UsePfxEncoding).append(EnableBatch, rhs.EnableBatch).append(BatchSize, rhs.BatchSize).append(RegressionSampleSize, rhs.RegressionSampleSize).append(EnableLinearRegression, rhs.EnableLinearRegression).append(EnablePolyRegression, rhs.EnablePolyRegression).isEquals();
    }

}
