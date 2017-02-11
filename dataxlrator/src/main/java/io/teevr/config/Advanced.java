
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
 * Advanced
 * <p>
 * 
 * 
 */
@Generated("org.jsonschema2pojo")
public class Advanced {

    /**
     * Enable Data Collector for Simulation
     * <p>
     * 
     * 
     */
    @SerializedName("EnableDataCollector")
    @Expose
    private Boolean EnableDataCollector;
    /**
     * Datafile to Store Data for Simulation
     * <p>
     * 
     * 
     */
    @SerializedName("DataFile")
    @Expose
    private String DataFile;
    /**
     * Enable JSON  Data Format between Edge and Cloud
     * <p>
     * 
     * 
     */
    @SerializedName("enableJsonData")
    @Expose
    private Boolean enableJsonData;
    /**
     * Enable Zipped JSON Data between Edge and Cloud
     * <p>
     * 
     * 
     */
    @SerializedName("enableZippedJsonData")
    @Expose
    private Boolean enableZippedJsonData;
    /**
     * Enable DataXlator Format between Edge and Cloud
     * <p>
     * 
     * 
     */
    @SerializedName("enableProtoData")
    @Expose
    private Boolean enableProtoData;
    /**
     * Enable Benchmarking Tests
     * <p>
     * 
     * 
     */
    @SerializedName("enableBenchmarking")
    @Expose
    private Boolean enableBenchmarking;
    /**
     * Enable Complex Event Processing
     * <p>
     * 
     * 
     */
    @SerializedName("enableCEP")
    @Expose
    private Boolean enableCEP;
    /**
     * Enable Performance Monitoring
     * <p>
     * 
     * 
     */
    @SerializedName("enablePerfMonitor")
    @Expose
    private Boolean enablePerfMonitor;
    /**
     * Use CloudMQTT or MonitorMQTT for publishing Perf and Events Stats 
     * <p>
     * 
     * 
     */
    @SerializedName("useCloudMQTTForPerfMonitor")
    @Expose
    private Boolean useCloudMQTTForPerfMonitor = true;
    /**
     * Use MonitorMQTT or MgmtMQTT for Remote Mgmt 
     * <p>
     * 
     * 
     */
    @SerializedName("useMonitorMQTTForRemoteMgmt")
    @Expose
    private Boolean useMonitorMQTTForRemoteMgmt = true;
    /**
     * Enable Data Comparison in Datagenerator, used only in Datagenerator
     * <p>
     * 
     * 
     */
    @SerializedName("enableDataComparison")
    @Expose
    private Boolean enableDataComparison;
    /**
     * Enable Window Size
     * <p>
     * 
     * 
     */
    @SerializedName("enableWindowSize")
    @Expose
    private Boolean enableWindowSize;
    /**
     * Enable write to Database for Received Data on Cloud
     * <p>
     * 
     * 
     */
    @SerializedName("EnableDBWrite")
    @Expose
    private Boolean EnableDBWrite;
    /**
     * Database to be used on Edge
     * <p>
     * 
     * 
     */
    @SerializedName("Edgedb")
    @Expose
    private Advanced.Edgedb Edgedb;
    /**
     * Database to be used on Cloud
     * <p>
     * 
     * 
     */
    @SerializedName("Clouddb")
    @Expose
    private Advanced.Clouddb Clouddb;
    /**
     * Enable Data Model for UI
     * <p>
     * 
     * 
     */
    @SerializedName("EnableDataModel")
    @Expose
    private Boolean EnableDataModel;
    /**
     * Dump decompressed values on cloud
     * <p>
     * 
     * 
     */
    @SerializedName("DumpDecompressedOutput")
    @Expose
    private Boolean DumpDecompressedOutput;
    /**
     * Edge Input Data Format. Auto will auto detect the format
     * <p>
     * 
     * 
     */
    @SerializedName("Input")
    @Expose
    private Advanced.Input Input;
    /**
     * Cloud Output Data Format. AUTO will use input format as output Format
     * <p>
     * 
     * 
     */
    @SerializedName("Output")
    @Expose
    private Advanced.Output Output;
    /**
     * Performance Monitor Time Unit. None means no performance monitoring
     * <p>
     * 
     * 
     */
    @SerializedName("PerfMonitorTimeUnit")
    @Expose
    private Advanced.PerfMonitorTimeUnit PerfMonitorTimeUnit;
    /**
     * Performance Monitor Reporting time period with unit as mentioned in PerfMonitorTimeUnit. This parameter is ignored if PerfMonitorTimeUnit is set to NONE
     * <p>
     * 
     * 
     */
    @SerializedName("PerfMonitorTimePeriod")
    @Expose
    private Integer PerfMonitorTimePeriod;
    /**
     * License check methodology. TDI is Teevr's License check, AMI for using AMI product code
     * <p>
     * 
     * 
     */
    @SerializedName("LicenseCheckMethod")
    @Expose
    private Advanced.LicenseCheckMethod LicenseCheckMethod;
    /**
     * If enabled, device id will be added to published topics along with datasource name to identify the publisher of message.This can be disabled if the customers topic contains deviceid 
     * <p>
     * 
     * 
     */
    @SerializedName("UseDeviceIDinTopic")
    @Expose
    private Boolean UseDeviceIDinTopic;
    /**
     * Source of Data to Edge. Used by datagenerator
     * <p>
     * 
     * 
     */
    @SerializedName("InputSource")
    @Expose
    private Advanced.InputSource InputSource;
    /**
     * Transport protocol between edge and cloud for data. Used by edge. IOTHUB is for Azure IOTHub
     * <p>
     * 
     * 
     */
    @SerializedName("Edge2Cloud")
    @Expose
    private Advanced.Edge2Cloud Edge2Cloud;
    /**
     * Unique Authentication Identifier for Customer workspace
     * <p>
     * 
     * 
     */
    @SerializedName("UAID")
    @Expose
    private String UAID;
    /**
     * Log Level
     * <p>
     * 
     * 
     */
    @SerializedName("LogLevel")
    @Expose
    private Advanced.LogLevel LogLevel;

    /**
     * Enable Data Collector for Simulation
     * <p>
     * 
     * 
     * @return
     *     The EnableDataCollector
     */
    public Boolean getEnableDataCollector() {
        return EnableDataCollector;
    }

    /**
     * Enable Data Collector for Simulation
     * <p>
     * 
     * 
     * @param EnableDataCollector
     *     The EnableDataCollector
     */
    public void setEnableDataCollector(Boolean EnableDataCollector) {
        this.EnableDataCollector = EnableDataCollector;
    }

    /**
     * Datafile to Store Data for Simulation
     * <p>
     * 
     * 
     * @return
     *     The DataFile
     */
    public String getDataFile() {
        return DataFile;
    }

    /**
     * Datafile to Store Data for Simulation
     * <p>
     * 
     * 
     * @param DataFile
     *     The DataFile
     */
    public void setDataFile(String DataFile) {
        this.DataFile = DataFile;
    }

    /**
     * Enable JSON  Data Format between Edge and Cloud
     * <p>
     * 
     * 
     * @return
     *     The enableJsonData
     */
    public Boolean getEnableJsonData() {
        return enableJsonData;
    }

    /**
     * Enable JSON  Data Format between Edge and Cloud
     * <p>
     * 
     * 
     * @param enableJsonData
     *     The enableJsonData
     */
    public void setEnableJsonData(Boolean enableJsonData) {
        this.enableJsonData = enableJsonData;
    }

    /**
     * Enable Zipped JSON Data between Edge and Cloud
     * <p>
     * 
     * 
     * @return
     *     The enableZippedJsonData
     */
    public Boolean getEnableZippedJsonData() {
        return enableZippedJsonData;
    }

    /**
     * Enable Zipped JSON Data between Edge and Cloud
     * <p>
     * 
     * 
     * @param enableZippedJsonData
     *     The enableZippedJsonData
     */
    public void setEnableZippedJsonData(Boolean enableZippedJsonData) {
        this.enableZippedJsonData = enableZippedJsonData;
    }

    /**
     * Enable DataXlator Format between Edge and Cloud
     * <p>
     * 
     * 
     * @return
     *     The enableProtoData
     */
    public Boolean getEnableProtoData() {
        return enableProtoData;
    }

    /**
     * Enable DataXlator Format between Edge and Cloud
     * <p>
     * 
     * 
     * @param enableProtoData
     *     The enableProtoData
     */
    public void setEnableProtoData(Boolean enableProtoData) {
        this.enableProtoData = enableProtoData;
    }

    /**
     * Enable Benchmarking Tests
     * <p>
     * 
     * 
     * @return
     *     The enableBenchmarking
     */
    public Boolean getEnableBenchmarking() {
        return enableBenchmarking;
    }

    /**
     * Enable Benchmarking Tests
     * <p>
     * 
     * 
     * @param enableBenchmarking
     *     The enableBenchmarking
     */
    public void setEnableBenchmarking(Boolean enableBenchmarking) {
        this.enableBenchmarking = enableBenchmarking;
    }

    /**
     * Enable Complex Event Processing
     * <p>
     * 
     * 
     * @return
     *     The enableCEP
     */
    public Boolean getEnableCEP() {
        return enableCEP;
    }

    /**
     * Enable Complex Event Processing
     * <p>
     * 
     * 
     * @param enableCEP
     *     The enableCEP
     */
    public void setEnableCEP(Boolean enableCEP) {
        this.enableCEP = enableCEP;
    }

    /**
     * Enable Performance Monitoring
     * <p>
     * 
     * 
     * @return
     *     The enablePerfMonitor
     */
    public Boolean getEnablePerfMonitor() {
        return enablePerfMonitor;
    }

    /**
     * Enable Performance Monitoring
     * <p>
     * 
     * 
     * @param enablePerfMonitor
     *     The enablePerfMonitor
     */
    public void setEnablePerfMonitor(Boolean enablePerfMonitor) {
        this.enablePerfMonitor = enablePerfMonitor;
    }

    /**
     * Use CloudMQTT or MonitorMQTT for publishing Perf and Events Stats 
     * <p>
     * 
     * 
     * @return
     *     The useCloudMQTTForPerfMonitor
     */
    public Boolean getUseCloudMQTTForPerfMonitor() {
        return useCloudMQTTForPerfMonitor;
    }

    /**
     * Use CloudMQTT or MonitorMQTT for publishing Perf and Events Stats 
     * <p>
     * 
     * 
     * @param useCloudMQTTForPerfMonitor
     *     The useCloudMQTTForPerfMonitor
     */
    public void setUseCloudMQTTForPerfMonitor(Boolean useCloudMQTTForPerfMonitor) {
        this.useCloudMQTTForPerfMonitor = useCloudMQTTForPerfMonitor;
    }

    /**
     * Use MonitorMQTT or MgmtMQTT for Remote Mgmt 
     * <p>
     * 
     * 
     * @return
     *     The useMonitorMQTTForRemoteMgmt
     */
    public Boolean getUseMonitorMQTTForRemoteMgmt() {
        return useMonitorMQTTForRemoteMgmt;
    }

    /**
     * Use MonitorMQTT or MgmtMQTT for Remote Mgmt 
     * <p>
     * 
     * 
     * @param useMonitorMQTTForRemoteMgmt
     *     The useMonitorMQTTForRemoteMgmt
     */
    public void setUseMonitorMQTTForRemoteMgmt(Boolean useMonitorMQTTForRemoteMgmt) {
        this.useMonitorMQTTForRemoteMgmt = useMonitorMQTTForRemoteMgmt;
    }

    /**
     * Enable Data Comparison in Datagenerator, used only in Datagenerator
     * <p>
     * 
     * 
     * @return
     *     The enableDataComparison
     */
    public Boolean getEnableDataComparison() {
        return enableDataComparison;
    }

    /**
     * Enable Data Comparison in Datagenerator, used only in Datagenerator
     * <p>
     * 
     * 
     * @param enableDataComparison
     *     The enableDataComparison
     */
    public void setEnableDataComparison(Boolean enableDataComparison) {
        this.enableDataComparison = enableDataComparison;
    }

    /**
     * Enable Window Size
     * <p>
     * 
     * 
     * @return
     *     The enableWindowSize
     */
    public Boolean getEnableWindowSize() {
        return enableWindowSize;
    }

    /**
     * Enable Window Size
     * <p>
     * 
     * 
     * @param enableWindowSize
     *     The enableWindowSize
     */
    public void setEnableWindowSize(Boolean enableWindowSize) {
        this.enableWindowSize = enableWindowSize;
    }

    /**
     * Enable write to Database for Received Data on Cloud
     * <p>
     * 
     * 
     * @return
     *     The EnableDBWrite
     */
    public Boolean getEnableDBWrite() {
        return EnableDBWrite;
    }

    /**
     * Enable write to Database for Received Data on Cloud
     * <p>
     * 
     * 
     * @param EnableDBWrite
     *     The EnableDBWrite
     */
    public void setEnableDBWrite(Boolean EnableDBWrite) {
        this.EnableDBWrite = EnableDBWrite;
    }

    /**
     * Database to be used on Edge
     * <p>
     * 
     * 
     * @return
     *     The Edgedb
     */
    public Advanced.Edgedb getEdgedb() {
        return Edgedb;
    }

    /**
     * Database to be used on Edge
     * <p>
     * 
     * 
     * @param Edgedb
     *     The Edgedb
     */
    public void setEdgedb(Advanced.Edgedb Edgedb) {
        this.Edgedb = Edgedb;
    }

    /**
     * Database to be used on Cloud
     * <p>
     * 
     * 
     * @return
     *     The Clouddb
     */
    public Advanced.Clouddb getClouddb() {
        return Clouddb;
    }

    /**
     * Database to be used on Cloud
     * <p>
     * 
     * 
     * @param Clouddb
     *     The Clouddb
     */
    public void setClouddb(Advanced.Clouddb Clouddb) {
        this.Clouddb = Clouddb;
    }

    /**
     * Enable Data Model for UI
     * <p>
     * 
     * 
     * @return
     *     The EnableDataModel
     */
    public Boolean getEnableDataModel() {
        return EnableDataModel;
    }

    /**
     * Enable Data Model for UI
     * <p>
     * 
     * 
     * @param EnableDataModel
     *     The EnableDataModel
     */
    public void setEnableDataModel(Boolean EnableDataModel) {
        this.EnableDataModel = EnableDataModel;
    }

    /**
     * Dump decompressed values on cloud
     * <p>
     * 
     * 
     * @return
     *     The DumpDecompressedOutput
     */
    public Boolean getDumpDecompressedOutput() {
        return DumpDecompressedOutput;
    }

    /**
     * Dump decompressed values on cloud
     * <p>
     * 
     * 
     * @param DumpDecompressedOutput
     *     The DumpDecompressedOutput
     */
    public void setDumpDecompressedOutput(Boolean DumpDecompressedOutput) {
        this.DumpDecompressedOutput = DumpDecompressedOutput;
    }

    /**
     * Edge Input Data Format. Auto will auto detect the format
     * <p>
     * 
     * 
     * @return
     *     The Input
     */
    public Advanced.Input getInput() {
        return Input;
    }

    /**
     * Edge Input Data Format. Auto will auto detect the format
     * <p>
     * 
     * 
     * @param Input
     *     The Input
     */
    public void setInput(Advanced.Input Input) {
        this.Input = Input;
    }

    /**
     * Cloud Output Data Format. AUTO will use input format as output Format
     * <p>
     * 
     * 
     * @return
     *     The Output
     */
    public Advanced.Output getOutput() {
        return Output;
    }

    /**
     * Cloud Output Data Format. AUTO will use input format as output Format
     * <p>
     * 
     * 
     * @param Output
     *     The Output
     */
    public void setOutput(Advanced.Output Output) {
        this.Output = Output;
    }

    /**
     * Performance Monitor Time Unit. None means no performance monitoring
     * <p>
     * 
     * 
     * @return
     *     The PerfMonitorTimeUnit
     */
    public Advanced.PerfMonitorTimeUnit getPerfMonitorTimeUnit() {
        return PerfMonitorTimeUnit;
    }

    /**
     * Performance Monitor Time Unit. None means no performance monitoring
     * <p>
     * 
     * 
     * @param PerfMonitorTimeUnit
     *     The PerfMonitorTimeUnit
     */
    public void setPerfMonitorTimeUnit(Advanced.PerfMonitorTimeUnit PerfMonitorTimeUnit) {
        this.PerfMonitorTimeUnit = PerfMonitorTimeUnit;
    }

    /**
     * Performance Monitor Reporting time period with unit as mentioned in PerfMonitorTimeUnit. This parameter is ignored if PerfMonitorTimeUnit is set to NONE
     * <p>
     * 
     * 
     * @return
     *     The PerfMonitorTimePeriod
     */
    public Integer getPerfMonitorTimePeriod() {
        return PerfMonitorTimePeriod;
    }

    /**
     * Performance Monitor Reporting time period with unit as mentioned in PerfMonitorTimeUnit. This parameter is ignored if PerfMonitorTimeUnit is set to NONE
     * <p>
     * 
     * 
     * @param PerfMonitorTimePeriod
     *     The PerfMonitorTimePeriod
     */
    public void setPerfMonitorTimePeriod(Integer PerfMonitorTimePeriod) {
        this.PerfMonitorTimePeriod = PerfMonitorTimePeriod;
    }

    /**
     * License check methodology. TDI is Teevr's License check, AMI for using AMI product code
     * <p>
     * 
     * 
     * @return
     *     The LicenseCheckMethod
     */
    public Advanced.LicenseCheckMethod getLicenseCheckMethod() {
        return LicenseCheckMethod;
    }

    /**
     * License check methodology. TDI is Teevr's License check, AMI for using AMI product code
     * <p>
     * 
     * 
     * @param LicenseCheckMethod
     *     The LicenseCheckMethod
     */
    public void setLicenseCheckMethod(Advanced.LicenseCheckMethod LicenseCheckMethod) {
        this.LicenseCheckMethod = LicenseCheckMethod;
    }

    /**
     * If enabled, device id will be added to published topics along with datasource name to identify the publisher of message.This can be disabled if the customers topic contains deviceid 
     * <p>
     * 
     * 
     * @return
     *     The UseDeviceIDinTopic
     */
    public Boolean getUseDeviceIDinTopic() {
        return UseDeviceIDinTopic;
    }

    /**
     * If enabled, device id will be added to published topics along with datasource name to identify the publisher of message.This can be disabled if the customers topic contains deviceid 
     * <p>
     * 
     * 
     * @param UseDeviceIDinTopic
     *     The UseDeviceIDinTopic
     */
    public void setUseDeviceIDinTopic(Boolean UseDeviceIDinTopic) {
        this.UseDeviceIDinTopic = UseDeviceIDinTopic;
    }

    /**
     * Source of Data to Edge. Used by datagenerator
     * <p>
     * 
     * 
     * @return
     *     The InputSource
     */
    public Advanced.InputSource getInputSource() {
        return InputSource;
    }

    /**
     * Source of Data to Edge. Used by datagenerator
     * <p>
     * 
     * 
     * @param InputSource
     *     The InputSource
     */
    public void setInputSource(Advanced.InputSource InputSource) {
        this.InputSource = InputSource;
    }

    /**
     * Transport protocol between edge and cloud for data. Used by edge. IOTHUB is for Azure IOTHub
     * <p>
     * 
     * 
     * @return
     *     The Edge2Cloud
     */
    public Advanced.Edge2Cloud getEdge2Cloud() {
        return Edge2Cloud;
    }

    /**
     * Transport protocol between edge and cloud for data. Used by edge. IOTHUB is for Azure IOTHub
     * <p>
     * 
     * 
     * @param Edge2Cloud
     *     The Edge2Cloud
     */
    public void setEdge2Cloud(Advanced.Edge2Cloud Edge2Cloud) {
        this.Edge2Cloud = Edge2Cloud;
    }

    /**
     * Unique Authentication Identifier for Customer workspace
     * <p>
     * 
     * 
     * @return
     *     The UAID
     */
    public String getUAID() {
        return UAID;
    }

    /**
     * Unique Authentication Identifier for Customer workspace
     * <p>
     * 
     * 
     * @param UAID
     *     The UAID
     */
    public void setUAID(String UAID) {
        this.UAID = UAID;
    }

    /**
     * Log Level
     * <p>
     * 
     * 
     * @return
     *     The LogLevel
     */
    public Advanced.LogLevel getLogLevel() {
        return LogLevel;
    }

    /**
     * Log Level
     * <p>
     * 
     * 
     * @param LogLevel
     *     The LogLevel
     */
    public void setLogLevel(Advanced.LogLevel LogLevel) {
        this.LogLevel = LogLevel;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(EnableDataCollector).append(DataFile).append(enableJsonData).append(enableZippedJsonData).append(enableProtoData).append(enableBenchmarking).append(enableCEP).append(enablePerfMonitor).append(useCloudMQTTForPerfMonitor).append(useMonitorMQTTForRemoteMgmt).append(enableDataComparison).append(enableWindowSize).append(EnableDBWrite).append(Edgedb).append(Clouddb).append(EnableDataModel).append(DumpDecompressedOutput).append(Input).append(Output).append(PerfMonitorTimeUnit).append(PerfMonitorTimePeriod).append(LicenseCheckMethod).append(UseDeviceIDinTopic).append(InputSource).append(Edge2Cloud).append(UAID).append(LogLevel).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Advanced) == false) {
            return false;
        }
        Advanced rhs = ((Advanced) other);
        return new EqualsBuilder().append(EnableDataCollector, rhs.EnableDataCollector).append(DataFile, rhs.DataFile).append(enableJsonData, rhs.enableJsonData).append(enableZippedJsonData, rhs.enableZippedJsonData).append(enableProtoData, rhs.enableProtoData).append(enableBenchmarking, rhs.enableBenchmarking).append(enableCEP, rhs.enableCEP).append(enablePerfMonitor, rhs.enablePerfMonitor).append(useCloudMQTTForPerfMonitor, rhs.useCloudMQTTForPerfMonitor).append(useMonitorMQTTForRemoteMgmt, rhs.useMonitorMQTTForRemoteMgmt).append(enableDataComparison, rhs.enableDataComparison).append(enableWindowSize, rhs.enableWindowSize).append(EnableDBWrite, rhs.EnableDBWrite).append(Edgedb, rhs.Edgedb).append(Clouddb, rhs.Clouddb).append(EnableDataModel, rhs.EnableDataModel).append(DumpDecompressedOutput, rhs.DumpDecompressedOutput).append(Input, rhs.Input).append(Output, rhs.Output).append(PerfMonitorTimeUnit, rhs.PerfMonitorTimeUnit).append(PerfMonitorTimePeriod, rhs.PerfMonitorTimePeriod).append(LicenseCheckMethod, rhs.LicenseCheckMethod).append(UseDeviceIDinTopic, rhs.UseDeviceIDinTopic).append(InputSource, rhs.InputSource).append(Edge2Cloud, rhs.Edge2Cloud).append(UAID, rhs.UAID).append(LogLevel, rhs.LogLevel).isEquals();
    }

    @Generated("org.jsonschema2pojo")
    public static enum Clouddb {

        @SerializedName("NONE")
        NONE("NONE"),
        @SerializedName("POSTGRES")
        POSTGRES("POSTGRES"),
        @SerializedName("MONGODB")
        MONGODB("MONGODB"),
        @SerializedName("INFLUXDB")
        INFLUXDB("INFLUXDB");
        private final String value;
        private static Map<String, Advanced.Clouddb> constants = new HashMap<String, Advanced.Clouddb>();

        static {
            for (Advanced.Clouddb c: values()) {
                constants.put(c.value, c);
            }
        }

        private Clouddb(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        public static Advanced.Clouddb fromValue(String value) {
            Advanced.Clouddb constant = constants.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

    @Generated("org.jsonschema2pojo")
    public static enum Edge2Cloud {

        @SerializedName("MQTT")
        MQTT("MQTT"),
        @SerializedName("COAP")
        COAP("COAP"),
        @SerializedName("SNAPI")
        SNAPI("SNAPI"),
        @SerializedName("IOTHUB")
        IOTHUB("IOTHUB");
        private final String value;
        private static Map<String, Advanced.Edge2Cloud> constants = new HashMap<String, Advanced.Edge2Cloud>();

        static {
            for (Advanced.Edge2Cloud c: values()) {
                constants.put(c.value, c);
            }
        }

        private Edge2Cloud(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        public static Advanced.Edge2Cloud fromValue(String value) {
            Advanced.Edge2Cloud constant = constants.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

    @Generated("org.jsonschema2pojo")
    public static enum Edgedb {

        @SerializedName("NONE")
        NONE("NONE"),
        @SerializedName("POSTGRES")
        POSTGRES("POSTGRES"),
        @SerializedName("MONGODB")
        MONGODB("MONGODB"),
        @SerializedName("INFLUXDB")
        INFLUXDB("INFLUXDB");
        private final String value;
        private static Map<String, Advanced.Edgedb> constants = new HashMap<String, Advanced.Edgedb>();

        static {
            for (Advanced.Edgedb c: values()) {
                constants.put(c.value, c);
            }
        }

        private Edgedb(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        public static Advanced.Edgedb fromValue(String value) {
            Advanced.Edgedb constant = constants.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

    @Generated("org.jsonschema2pojo")
    public static enum Input {

        @SerializedName("AUTO")
        AUTO("AUTO"),
        @SerializedName("JSON")
        JSON("JSON"),
        @SerializedName("CSV")
        CSV("CSV");
        private final String value;
        private static Map<String, Advanced.Input> constants = new HashMap<String, Advanced.Input>();

        static {
            for (Advanced.Input c: values()) {
                constants.put(c.value, c);
            }
        }

        private Input(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        public static Advanced.Input fromValue(String value) {
            Advanced.Input constant = constants.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

    @Generated("org.jsonschema2pojo")
    public static enum InputSource {

        @SerializedName("MQTT")
        MQTT("MQTT"),
        @SerializedName("COAP")
        COAP("COAP"),
        @SerializedName("SNAPI")
        SNAPI("SNAPI");
        private final String value;
        private static Map<String, Advanced.InputSource> constants = new HashMap<String, Advanced.InputSource>();

        static {
            for (Advanced.InputSource c: values()) {
                constants.put(c.value, c);
            }
        }

        private InputSource(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        public static Advanced.InputSource fromValue(String value) {
            Advanced.InputSource constant = constants.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

    @Generated("org.jsonschema2pojo")
    public static enum LicenseCheckMethod {

        @SerializedName("TDI")
        TDI("TDI"),
        @SerializedName("AMI")
        AMI("AMI");
        private final String value;
        private static Map<String, Advanced.LicenseCheckMethod> constants = new HashMap<String, Advanced.LicenseCheckMethod>();

        static {
            for (Advanced.LicenseCheckMethod c: values()) {
                constants.put(c.value, c);
            }
        }

        private LicenseCheckMethod(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        public static Advanced.LicenseCheckMethod fromValue(String value) {
            Advanced.LicenseCheckMethod constant = constants.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

    @Generated("org.jsonschema2pojo")
    public static enum LogLevel {

        @SerializedName("INFO")
        INFO("INFO"),
        @SerializedName("FATAL")
        FATAL("FATAL"),
        @SerializedName("ERROR")
        ERROR("ERROR"),
        @SerializedName("WARN")
        WARN("WARN"),
        @SerializedName("DEBUG")
        DEBUG("DEBUG"),
        @SerializedName("TRACE")
        TRACE("TRACE");
        private final String value;
        private static Map<String, Advanced.LogLevel> constants = new HashMap<String, Advanced.LogLevel>();

        static {
            for (Advanced.LogLevel c: values()) {
                constants.put(c.value, c);
            }
        }

        private LogLevel(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        public static Advanced.LogLevel fromValue(String value) {
            Advanced.LogLevel constant = constants.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

    @Generated("org.jsonschema2pojo")
    public static enum Output {

        @SerializedName("AUTO")
        AUTO("AUTO"),
        @SerializedName("JSON")
        JSON("JSON"),
        @SerializedName("CSV")
        CSV("CSV");
        private final String value;
        private static Map<String, Advanced.Output> constants = new HashMap<String, Advanced.Output>();

        static {
            for (Advanced.Output c: values()) {
                constants.put(c.value, c);
            }
        }

        private Output(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        public static Advanced.Output fromValue(String value) {
            Advanced.Output constant = constants.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

    @Generated("org.jsonschema2pojo")
    public static enum PerfMonitorTimeUnit {

        @SerializedName("NONE")
        NONE("NONE"),
        @SerializedName("MS")
        MS("MS"),
        @SerializedName("SEC")
        SEC("SEC"),
        @SerializedName("MIN")
        MIN("MIN"),
        @SerializedName("HR")
        HR("HR");
        private final String value;
        private static Map<String, Advanced.PerfMonitorTimeUnit> constants = new HashMap<String, Advanced.PerfMonitorTimeUnit>();

        static {
            for (Advanced.PerfMonitorTimeUnit c: values()) {
                constants.put(c.value, c);
            }
        }

        private PerfMonitorTimeUnit(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        public static Advanced.PerfMonitorTimeUnit fromValue(String value) {
            Advanced.PerfMonitorTimeUnit constant = constants.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
