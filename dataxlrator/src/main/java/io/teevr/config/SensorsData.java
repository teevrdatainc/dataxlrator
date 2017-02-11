
package io.teevr.config;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@Generated("org.jsonschema2pojo")
public class SensorsData {

    @SerializedName("combo")
    @Expose
    private Combo combo;
    @SerializedName("kinoma")
    @Expose
    private Combo kinoma;
    @SerializedName("singular")
    @Expose
    private Combo singular;

    /**
     * 
     * @return
     *     The combo
     */
    public Combo getCombo() {
        return combo;
    }

    /**
     * 
     * @param combo
     *     The combo
     */
    public void setCombo(Combo combo) {
        this.combo = combo;
    }

    /**
     * 
     * @return
     *     The kinoma
     */
    public Combo getKinoma() {
        return kinoma;
    }

    /**
     * 
     * @param kinoma
     *     The kinoma
     */
    public void setKinoma(Combo kinoma) {
        this.kinoma = kinoma;
    }

    /**
     * 
     * @return
     *     The singular
     */
    public Combo getSingular() {
        return singular;
    }

    /**
     * 
     * @param singular
     *     The singular
     */
    public void setSingular(Combo singular) {
        this.singular = singular;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(combo).append(kinoma).append(singular).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SensorsData) == false) {
            return false;
        }
        SensorsData rhs = ((SensorsData) other);
        return new EqualsBuilder().append(combo, rhs.combo).append(kinoma, rhs.kinoma).append(singular, rhs.singular).isEquals();
    }

}
