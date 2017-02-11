
package io.teevr.config;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * DataField
 * <p>
 * 
 * 
 */
@Generated("org.jsonschema2pojo")
public class Model {

    /**
     * Name
     * <p>
     * 
     * (Required)
     * 
     */
    @SerializedName("Name")
    @Expose
    private String Name;
    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("DataType")
    @Expose
    private Model.DataType DataType;
    @SerializedName("Precision")
    @Expose
    private Integer Precision;
    @SerializedName("TSFormat")
    @Expose
    private Integer TSFormat;
    @SerializedName("BoolFormat")
    @Expose
    private Integer BoolFormat;
    @SerializedName("IsMetaData")
    @Expose
    private Boolean IsMetaData;

    /**
     * Name
     * <p>
     * 
     * (Required)
     * 
     * @return
     *     The Name
     */
    public String getName() {
        return Name;
    }

    /**
     * Name
     * <p>
     * 
     * (Required)
     * 
     * @param Name
     *     The Name
     */
    public void setName(String Name) {
        this.Name = Name;
    }

    /**
     * 
     * (Required)
     * 
     * @return
     *     The DataType
     */
    public Model.DataType getDataType() {
        return DataType;
    }

    /**
     * 
     * (Required)
     * 
     * @param DataType
     *     The DataType
     */
    public void setDataType(Model.DataType DataType) {
        this.DataType = DataType;
    }

    /**
     * 
     * @return
     *     The Precision
     */
    public Integer getPrecision() {
        return Precision;
    }

    /**
     * 
     * @param Precision
     *     The Precision
     */
    public void setPrecision(Integer Precision) {
        this.Precision = Precision;
    }

    /**
     * 
     * @return
     *     The TSFormat
     */
    public Integer getTSFormat() {
        return TSFormat;
    }

    /**
     * 
     * @param TSFormat
     *     The TSFormat
     */
    public void setTSFormat(Integer TSFormat) {
        this.TSFormat = TSFormat;
    }

    /**
     * 
     * @return
     *     The BoolFormat
     */
    public Integer getBoolFormat() {
        return BoolFormat;
    }

    /**
     * 
     * @param BoolFormat
     *     The BoolFormat
     */
    public void setBoolFormat(Integer BoolFormat) {
        this.BoolFormat = BoolFormat;
    }

    /**
     * 
     * @return
     *     The IsMetaData
     */
    public Boolean getIsMetaData() {
        return IsMetaData;
    }

    /**
     * 
     * @param IsMetaData
     *     The IsMetaData
     */
    public void setIsMetaData(Boolean IsMetaData) {
        this.IsMetaData = IsMetaData;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(Name).append(DataType).append(Precision).append(TSFormat).append(BoolFormat).append(IsMetaData).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Model) == false) {
            return false;
        }
        Model rhs = ((Model) other);
        return new EqualsBuilder().append(Name, rhs.Name).append(DataType, rhs.DataType).append(Precision, rhs.Precision).append(TSFormat, rhs.TSFormat).append(BoolFormat, rhs.BoolFormat).append(IsMetaData, rhs.IsMetaData).isEquals();
    }

    @Generated("org.jsonschema2pojo")
    public static enum DataType {

        @SerializedName("Number")
        NUMBER("Number"),
        @SerializedName("String")
        STRING("String"),
        @SerializedName("Boolean")
        BOOLEAN("Boolean"),
        @SerializedName("Timestamp")
        TIMESTAMP("Timestamp"),
        @SerializedName("Hexstring")
        HEXSTRING("Hexstring");
        private final String value;
        private static Map<String, Model.DataType> constants = new HashMap<String, Model.DataType>();

        static {
            for (Model.DataType c: values()) {
                constants.put(c.value, c);
            }
        }

        private DataType(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        public static Model.DataType fromValue(String value) {
            Model.DataType constant = constants.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
