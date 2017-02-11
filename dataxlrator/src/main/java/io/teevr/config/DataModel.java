
package io.teevr.config;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 *  
 * <p>
 * 
 * 
 */
@Generated("org.jsonschema2pojo")
public class DataModel {

    /**
     *  Mappped Data 
     * <p>
     * 
     * (Required)
     * 
     */
    @SerializedName("Model")
    @Expose
    private List<io.teevr.config.Model> Model = new ArrayList<io.teevr.config.Model>();

    /**
     *  Mappped Data 
     * <p>
     * 
     * (Required)
     * 
     * @return
     *     The Model
     */
    public List<io.teevr.config.Model> getModel() {
        return Model;
    }

    /**
     *  Mappped Data 
     * <p>
     * 
     * (Required)
     * 
     * @param Model
     *     The Model
     */
    public void setModel(List<io.teevr.config.Model> Model) {
        this.Model = Model;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(Model).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DataModel) == false) {
            return false;
        }
        DataModel rhs = ((DataModel) other);
        return new EqualsBuilder().append(Model, rhs.Model).isEquals();
    }

}
