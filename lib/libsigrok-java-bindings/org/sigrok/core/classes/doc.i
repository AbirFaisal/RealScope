%typemap(javaclassmodifiers) sigrok::Analog "/** Payload of a datafeed packet with analog data. */
public class"
%javamethodmodifiers sigrok::Analog::data_pointer "/** Pointer to data. */
public"
%javamethodmodifiers sigrok::Analog::get_data_as_float "/** Fills dest pointer with the analog data converted to float. */
public"
%javamethodmodifiers sigrok::Analog::num_samples "/** Number of samples in this packet. */
public"
%javamethodmodifiers sigrok::Analog::channels "/** Channels for which this packet contains data. */
public"
%javamethodmodifiers sigrok::Analog::unitsize "/** Size of a single sample in bytes. */
public"
%javamethodmodifiers sigrok::Analog::is_signed "/** Samples use a signed data type. */
public"
%javamethodmodifiers sigrok::Analog::is_float "/** Samples use float. */
public"
%javamethodmodifiers sigrok::Analog::is_bigendian "/** Samples are stored in big-endian order. */
public"
%javamethodmodifiers sigrok::Analog::digits "/** Number of significant digits after the decimal point if positive, or number of non-significant digits before the decimal point if negative (refers to the value we actually read on the wire). */
public"
%javamethodmodifiers sigrok::Analog::is_digits_decimal "/** TBD. */
public"
%javamethodmodifiers sigrok::Analog::scale "/** TBD. */
public"
%javamethodmodifiers sigrok::Analog::offset "/** TBD. */
public"
%javamethodmodifiers sigrok::Analog::mq "/** Measured quantity of the samples in this packet. */
public"
%javamethodmodifiers sigrok::Analog::unit "/** Unit of the samples in this packet. */
public"
%javamethodmodifiers sigrok::Analog::mq_flags "/** Measurement flags associated with the samples in this packet. */
public"
%javamethodmodifiers sigrok::Analog::get_logic_via_threshold "/** Provides a Logic packet that contains a conversion of the analog data using a simple threshold.
   * @param threshold Threshold to use.
   * @param data_ptr Pointer to num_samples() bytes where the logic samples are stored. When nullptr, memory for logic->data_pointer() will be allocated and must be freed by the caller. */
public"
%javamethodmodifiers sigrok::Analog::get_logic_via_schmitt_trigger "/** Provides a Logic packet that contains a conversion of the analog data using a Schmitt-Trigger.
   * @param lo_thr Low threshold to use (anything below this is low).
   * @param data_ptr Pointer to num_samples() bytes where the logic samples are stored. When nullptr, memory for logic->data_pointer() will be allocated and must be freed by the caller.
   * @param hi_thr High threshold to use (anything above this is high).
   * @param state Points to a byte that contains the current state of the converter. For best results, set to value of logic sample n-1. */
public"
%typemap(javaclassmodifiers) sigrok::Capability "/** Configuration capability. */
public class"
%typemap(javacode) sigrok::Capability %{
  /** Value can be read. */
  public static final Capability GET = new Capability(classesJNI.Capability_GET_get(), false);

  /** Value can be written. */
  public static final Capability SET = new Capability(classesJNI.Capability_SET_get(), false);

  /** Possible values can be enumerated. */
  public static final Capability LIST = new Capability(classesJNI.Capability_LIST_get(), false);

%}
%typemap(javaclassmodifiers) sigrok::Channel "/** A channel on a device. */
public class"
%javamethodmodifiers sigrok::Channel::name "/** Current name of this channel. */
public"
%javamethodmodifiers sigrok::Channel::set_name "/** Set the name of this channel. */
public"
%javamethodmodifiers sigrok::Channel::type "/** Type of this channel. */
public"
%javamethodmodifiers sigrok::Channel::enabled "/** Enabled status of this channel. */
public"
%javamethodmodifiers sigrok::Channel::set_enabled "/** Set the enabled status of this channel.
   * @param value Boolean value to set. */
public"
%javamethodmodifiers sigrok::Channel::index "/** Get the index number of this channel. */
public"
%typemap(javaclassmodifiers) sigrok::ChannelGroup "/** A group of channels on a device, which share some configuration. */
public class"
%javamethodmodifiers sigrok::ChannelGroup::name "/** Name of this channel group. */
public"
%javamethodmodifiers sigrok::ChannelGroup::channels "/** List of the channels in this group. */
public"
%typemap(javaclassmodifiers) sigrok::ChannelType "/** Channel type. */
public class"
%typemap(javacode) sigrok::ChannelType %{
  /** Channel type is logic channel. */
  public static final ChannelType LOGIC = new ChannelType(classesJNI.ChannelType_LOGIC_get(), false);

  /** Channel type is analog channel. */
  public static final ChannelType ANALOG = new ChannelType(classesJNI.ChannelType_ANALOG_get(), false);

%}
%typemap(javaclassmodifiers) sigrok::ConfigKey "/** Configuration key. */
public class"
%javamethodmodifiers sigrok::ConfigKey::data_type "/** Data type used for this configuration key. */
public"
%javamethodmodifiers sigrok::ConfigKey::identifier "/** String identifier for this configuration key, suitable for CLI use. */
public"
%javamethodmodifiers sigrok::ConfigKey::description "/** Description of this configuration key. */
public"
%typemap(javacode) sigrok::ConfigKey %{
  /** The device can act as logic analyzer. */
  public static final ConfigKey LOGIC_ANALYZER = new ConfigKey(classesJNI.ConfigKey_LOGIC_ANALYZER_get(), false);

  /** The device can act as an oscilloscope. */
  public static final ConfigKey OSCILLOSCOPE = new ConfigKey(classesJNI.ConfigKey_OSCILLOSCOPE_get(), false);

  /** The device can act as a multimeter. */
  public static final ConfigKey MULTIMETER = new ConfigKey(classesJNI.ConfigKey_MULTIMETER_get(), false);

  /** The device is a demo device. */
  public static final ConfigKey DEMO_DEV = new ConfigKey(classesJNI.ConfigKey_DEMO_DEV_get(), false);

  /** The device can act as a sound level meter. */
  public static final ConfigKey SOUNDLEVELMETER = new ConfigKey(classesJNI.ConfigKey_SOUNDLEVELMETER_get(), false);

  /** The device can measure temperature. */
  public static final ConfigKey THERMOMETER = new ConfigKey(classesJNI.ConfigKey_THERMOMETER_get(), false);

  /** The device can measure humidity. */
  public static final ConfigKey HYGROMETER = new ConfigKey(classesJNI.ConfigKey_HYGROMETER_get(), false);

  /** The device can measure energy consumption. */
  public static final ConfigKey ENERGYMETER = new ConfigKey(classesJNI.ConfigKey_ENERGYMETER_get(), false);

  /** The device can act as a signal demodulator. */
  public static final ConfigKey DEMODULATOR = new ConfigKey(classesJNI.ConfigKey_DEMODULATOR_get(), false);

  /** The device can act as a programmable power supply. */
  public static final ConfigKey POWER_SUPPLY = new ConfigKey(classesJNI.ConfigKey_POWER_SUPPLY_get(), false);

  /** The device can act as an LCR meter. */
  public static final ConfigKey LCRMETER = new ConfigKey(classesJNI.ConfigKey_LCRMETER_get(), false);

  /** The device can act as an electronic load. */
  public static final ConfigKey ELECTRONIC_LOAD = new ConfigKey(classesJNI.ConfigKey_ELECTRONIC_LOAD_get(), false);

  /** The device can act as a scale. */
  public static final ConfigKey SCALE = new ConfigKey(classesJNI.ConfigKey_SCALE_get(), false);

  /** The device can act as a function generator. */
  public static final ConfigKey SIGNAL_GENERATOR = new ConfigKey(classesJNI.ConfigKey_SIGNAL_GENERATOR_get(), false);

  /** The device can measure power. */
  public static final ConfigKey POWERMETER = new ConfigKey(classesJNI.ConfigKey_POWERMETER_get(), false);

  /** Specification on how to connect to a device. */
  public static final ConfigKey CONN = new ConfigKey(classesJNI.ConfigKey_CONN_get(), false);

  /** Serial communication specification, in the form: */
  public static final ConfigKey SERIALCOMM = new ConfigKey(classesJNI.ConfigKey_SERIALCOMM_get(), false);

  /** Modbus slave address specification. */
  public static final ConfigKey MODBUSADDR = new ConfigKey(classesJNI.ConfigKey_MODBUSADDR_get(), false);

  /** The device supports setting its samplerate, in Hz. */
  public static final ConfigKey SAMPLERATE = new ConfigKey(classesJNI.ConfigKey_SAMPLERATE_get(), false);

  /** The device supports setting a pre/post-trigger capture ratio. */
  public static final ConfigKey CAPTURE_RATIO = new ConfigKey(classesJNI.ConfigKey_CAPTURE_RATIO_get(), false);

  /** The device supports setting a pattern (pattern generator mode). */
  public static final ConfigKey PATTERN_MODE = new ConfigKey(classesJNI.ConfigKey_PATTERN_MODE_get(), false);

  /** The device supports run-length encoding (RLE). */
  public static final ConfigKey RLE = new ConfigKey(classesJNI.ConfigKey_RLE_get(), false);

  /** The device supports setting trigger slope. */
  public static final ConfigKey TRIGGER_SLOPE = new ConfigKey(classesJNI.ConfigKey_TRIGGER_SLOPE_get(), false);

  /** The device supports averaging. */
  public static final ConfigKey AVERAGING = new ConfigKey(classesJNI.ConfigKey_AVERAGING_get(), false);

  /** The device supports setting number of samples to be averaged over. */
  public static final ConfigKey AVG_SAMPLES = new ConfigKey(classesJNI.ConfigKey_AVG_SAMPLES_get(), false);

  /** Trigger source. */
  public static final ConfigKey TRIGGER_SOURCE = new ConfigKey(classesJNI.ConfigKey_TRIGGER_SOURCE_get(), false);

  /** Horizontal trigger position. */
  public static final ConfigKey HORIZ_TRIGGERPOS = new ConfigKey(classesJNI.ConfigKey_HORIZ_TRIGGERPOS_get(), false);

  /** Buffer size. */
  public static final ConfigKey BUFFERSIZE = new ConfigKey(classesJNI.ConfigKey_BUFFERSIZE_get(), false);

  /** Time base. */
  public static final ConfigKey TIMEBASE = new ConfigKey(classesJNI.ConfigKey_TIMEBASE_get(), false);

  /** Filter. */
  public static final ConfigKey FILTER = new ConfigKey(classesJNI.ConfigKey_FILTER_get(), false);

  /** Volts/div. */
  public static final ConfigKey VDIV = new ConfigKey(classesJNI.ConfigKey_VDIV_get(), false);

  /** Coupling. */
  public static final ConfigKey COUPLING = new ConfigKey(classesJNI.ConfigKey_COUPLING_get(), false);

  /** Trigger matches. */
  public static final ConfigKey TRIGGER_MATCH = new ConfigKey(classesJNI.ConfigKey_TRIGGER_MATCH_get(), false);

  /** The device supports setting its sample interval, in ms. */
  public static final ConfigKey SAMPLE_INTERVAL = new ConfigKey(classesJNI.ConfigKey_SAMPLE_INTERVAL_get(), false);

  /** Number of horizontal divisions, as related to SR_CONF_TIMEBASE. */
  public static final ConfigKey NUM_HDIV = new ConfigKey(classesJNI.ConfigKey_NUM_HDIV_get(), false);

  /** Number of vertical divisions, as related to SR_CONF_VDIV. */
  public static final ConfigKey NUM_VDIV = new ConfigKey(classesJNI.ConfigKey_NUM_VDIV_get(), false);

  /** Sound pressure level frequency weighting. */
  public static final ConfigKey SPL_WEIGHT_FREQ = new ConfigKey(classesJNI.ConfigKey_SPL_WEIGHT_FREQ_get(), false);

  /** Sound pressure level time weighting. */
  public static final ConfigKey SPL_WEIGHT_TIME = new ConfigKey(classesJNI.ConfigKey_SPL_WEIGHT_TIME_get(), false);

  /** Sound pressure level measurement range. */
  public static final ConfigKey SPL_MEASUREMENT_RANGE = new ConfigKey(classesJNI.ConfigKey_SPL_MEASUREMENT_RANGE_get(), false);

  /** Max hold mode. */
  public static final ConfigKey HOLD_MAX = new ConfigKey(classesJNI.ConfigKey_HOLD_MAX_get(), false);

  /** Min hold mode. */
  public static final ConfigKey HOLD_MIN = new ConfigKey(classesJNI.ConfigKey_HOLD_MIN_get(), false);

  /** Logic low-high threshold range. */
  public static final ConfigKey VOLTAGE_THRESHOLD = new ConfigKey(classesJNI.ConfigKey_VOLTAGE_THRESHOLD_get(), false);

  /** The device supports using an external clock. */
  public static final ConfigKey EXTERNAL_CLOCK = new ConfigKey(classesJNI.ConfigKey_EXTERNAL_CLOCK_get(), false);

  /** The device supports swapping channels. */
  public static final ConfigKey SWAP = new ConfigKey(classesJNI.ConfigKey_SWAP_get(), false);

  /** Center frequency. */
  public static final ConfigKey CENTER_FREQUENCY = new ConfigKey(classesJNI.ConfigKey_CENTER_FREQUENCY_get(), false);

  /** The device supports setting the number of logic channels. */
  public static final ConfigKey NUM_LOGIC_CHANNELS = new ConfigKey(classesJNI.ConfigKey_NUM_LOGIC_CHANNELS_get(), false);

  /** The device supports setting the number of analog channels. */
  public static final ConfigKey NUM_ANALOG_CHANNELS = new ConfigKey(classesJNI.ConfigKey_NUM_ANALOG_CHANNELS_get(), false);

  /** Current voltage. */
  public static final ConfigKey VOLTAGE = new ConfigKey(classesJNI.ConfigKey_VOLTAGE_get(), false);

  /** Maximum target voltage. */
  public static final ConfigKey VOLTAGE_TARGET = new ConfigKey(classesJNI.ConfigKey_VOLTAGE_TARGET_get(), false);

  /** Current current. */
  public static final ConfigKey CURRENT = new ConfigKey(classesJNI.ConfigKey_CURRENT_get(), false);

  /** Current limit. */
  public static final ConfigKey CURRENT_LIMIT = new ConfigKey(classesJNI.ConfigKey_CURRENT_LIMIT_get(), false);

  /** Enabling/disabling channel. */
  public static final ConfigKey ENABLED = new ConfigKey(classesJNI.ConfigKey_ENABLED_get(), false);

  /** Channel configuration. */
  public static final ConfigKey CHANNEL_CONFIG = new ConfigKey(classesJNI.ConfigKey_CHANNEL_CONFIG_get(), false);

  /** Over-voltage protection (OVP) feature. */
  public static final ConfigKey OVER_VOLTAGE_PROTECTION_ENABLED = new ConfigKey(classesJNI.ConfigKey_OVER_VOLTAGE_PROTECTION_ENABLED_get(), false);

  /** Over-voltage protection (OVP) active. */
  public static final ConfigKey OVER_VOLTAGE_PROTECTION_ACTIVE = new ConfigKey(classesJNI.ConfigKey_OVER_VOLTAGE_PROTECTION_ACTIVE_get(), false);

  /** Over-voltage protection (OVP) threshold. */
  public static final ConfigKey OVER_VOLTAGE_PROTECTION_THRESHOLD = new ConfigKey(classesJNI.ConfigKey_OVER_VOLTAGE_PROTECTION_THRESHOLD_get(), false);

  /** Over-current protection (OCP) feature. */
  public static final ConfigKey OVER_CURRENT_PROTECTION_ENABLED = new ConfigKey(classesJNI.ConfigKey_OVER_CURRENT_PROTECTION_ENABLED_get(), false);

  /** Over-current protection (OCP) active. */
  public static final ConfigKey OVER_CURRENT_PROTECTION_ACTIVE = new ConfigKey(classesJNI.ConfigKey_OVER_CURRENT_PROTECTION_ACTIVE_get(), false);

  /** Over-current protection (OCP) threshold. */
  public static final ConfigKey OVER_CURRENT_PROTECTION_THRESHOLD = new ConfigKey(classesJNI.ConfigKey_OVER_CURRENT_PROTECTION_THRESHOLD_get(), false);

  /** Choice of clock edge for external clock (\"r\" or \"f\"). */
  public static final ConfigKey CLOCK_EDGE = new ConfigKey(classesJNI.ConfigKey_CLOCK_EDGE_get(), false);

  /** Amplitude of a source without strictly-defined MQ. */
  public static final ConfigKey AMPLITUDE = new ConfigKey(classesJNI.ConfigKey_AMPLITUDE_get(), false);

  /** Channel regulation get: \"CV\", \"CC\" or \"UR\", denoting constant voltage, constant current or unregulated. */
  public static final ConfigKey REGULATION = new ConfigKey(classesJNI.ConfigKey_REGULATION_get(), false);

  /** Over-temperature protection (OTP) */
  public static final ConfigKey OVER_TEMPERATURE_PROTECTION = new ConfigKey(classesJNI.ConfigKey_OVER_TEMPERATURE_PROTECTION_get(), false);

  /** Output frequency in Hz. */
  public static final ConfigKey OUTPUT_FREQUENCY = new ConfigKey(classesJNI.ConfigKey_OUTPUT_FREQUENCY_get(), false);

  /** Output frequency target in Hz. */
  public static final ConfigKey OUTPUT_FREQUENCY_TARGET = new ConfigKey(classesJNI.ConfigKey_OUTPUT_FREQUENCY_TARGET_get(), false);

  /** Measured quantity. */
  public static final ConfigKey MEASURED_QUANTITY = new ConfigKey(classesJNI.ConfigKey_MEASURED_QUANTITY_get(), false);

  /** Equivalent circuit model. */
  public static final ConfigKey EQUIV_CIRCUIT_MODEL = new ConfigKey(classesJNI.ConfigKey_EQUIV_CIRCUIT_MODEL_get(), false);

  /** Over-temperature protection (OTP) active. */
  public static final ConfigKey OVER_TEMPERATURE_PROTECTION_ACTIVE = new ConfigKey(classesJNI.ConfigKey_OVER_TEMPERATURE_PROTECTION_ACTIVE_get(), false);

  /** Under-voltage condition. */
  public static final ConfigKey UNDER_VOLTAGE_CONDITION = new ConfigKey(classesJNI.ConfigKey_UNDER_VOLTAGE_CONDITION_get(), false);

  /** Under-voltage condition active. */
  public static final ConfigKey UNDER_VOLTAGE_CONDITION_ACTIVE = new ConfigKey(classesJNI.ConfigKey_UNDER_VOLTAGE_CONDITION_ACTIVE_get(), false);

  /** Trigger level. */
  public static final ConfigKey TRIGGER_LEVEL = new ConfigKey(classesJNI.ConfigKey_TRIGGER_LEVEL_get(), false);

  /** Under-voltage condition threshold. */
  public static final ConfigKey UNDER_VOLTAGE_CONDITION_THRESHOLD = new ConfigKey(classesJNI.ConfigKey_UNDER_VOLTAGE_CONDITION_THRESHOLD_get(), false);

  /** Which external clock source to use if the device supports multiple external clock channels. */
  public static final ConfigKey EXTERNAL_CLOCK_SOURCE = new ConfigKey(classesJNI.ConfigKey_EXTERNAL_CLOCK_SOURCE_get(), false);

  /** Session filename. */
  public static final ConfigKey SESSIONFILE = new ConfigKey(classesJNI.ConfigKey_SESSIONFILE_get(), false);

  /** The device supports specifying a capturefile to inject. */
  public static final ConfigKey CAPTUREFILE = new ConfigKey(classesJNI.ConfigKey_CAPTUREFILE_get(), false);

  /** The device supports specifying the capturefile unit size. */
  public static final ConfigKey CAPTURE_UNITSIZE = new ConfigKey(classesJNI.ConfigKey_CAPTURE_UNITSIZE_get(), false);

  /** Power off the device. */
  public static final ConfigKey POWER_OFF = new ConfigKey(classesJNI.ConfigKey_POWER_OFF_get(), false);

  /** Data source for acquisition. */
  public static final ConfigKey DATA_SOURCE = new ConfigKey(classesJNI.ConfigKey_DATA_SOURCE_get(), false);

  /** The device supports setting a probe factor. */
  public static final ConfigKey PROBE_FACTOR = new ConfigKey(classesJNI.ConfigKey_PROBE_FACTOR_get(), false);

  /** Number of powerline cycles for ADC integration time. */
  public static final ConfigKey ADC_POWERLINE_CYCLES = new ConfigKey(classesJNI.ConfigKey_ADC_POWERLINE_CYCLES_get(), false);

  /** The device supports setting a sample time limit (how long the sample acquisition should run, in ms). */
  public static final ConfigKey LIMIT_MSEC = new ConfigKey(classesJNI.ConfigKey_LIMIT_MSEC_get(), false);

  /** The device supports setting a sample number limit (how many samples should be acquired). */
  public static final ConfigKey LIMIT_SAMPLES = new ConfigKey(classesJNI.ConfigKey_LIMIT_SAMPLES_get(), false);

  /** The device supports setting a frame limit (how many frames should be acquired). */
  public static final ConfigKey LIMIT_FRAMES = new ConfigKey(classesJNI.ConfigKey_LIMIT_FRAMES_get(), false);

  /** The device supports continuous sampling. */
  public static final ConfigKey CONTINUOUS = new ConfigKey(classesJNI.ConfigKey_CONTINUOUS_get(), false);

  /** The device has internal storage, into which data is logged. */
  public static final ConfigKey DATALOG = new ConfigKey(classesJNI.ConfigKey_DATALOG_get(), false);

  /** Device mode for multi-function devices. */
  public static final ConfigKey DEVICE_MODE = new ConfigKey(classesJNI.ConfigKey_DEVICE_MODE_get(), false);

  /** Self test mode. */
  public static final ConfigKey TEST_MODE = new ConfigKey(classesJNI.ConfigKey_TEST_MODE_get(), false);

%}
%typemap(javaclassmodifiers) sigrok::Configurable "/** An object that can be configured. */
public class"
%javamethodmodifiers sigrok::Configurable::config_keys "/** Supported configuration keys. */
public"
%javamethodmodifiers sigrok::Configurable::config_get "/** Read configuration for the given key.
   * @param key ConfigKey to read. */
public"
%javamethodmodifiers sigrok::Configurable::config_set "/** Set configuration for the given key to a specified value.
   * @param value Value to set.
   * @param key ConfigKey to set. */
public"
%javamethodmodifiers sigrok::Configurable::config_list "/** Enumerate available values for the given configuration key.
   * @param key ConfigKey to enumerate values for. */
public"
%javamethodmodifiers sigrok::Configurable::config_capabilities "/** Enumerate configuration capabilities for the given configuration key.
   * @param key ConfigKey to enumerate capabilities for. */
public"
%javamethodmodifiers sigrok::Configurable::config_check "/** Check whether a configuration capability is supported for a given key.
   * @param capability Capability to check for.
   * @param key ConfigKey to check. */
public"
%typemap(javaclassmodifiers) sigrok::Context "/** The global libsigrok context. */
public class"
%javamethodmodifiers sigrok::Context::drivers "/** Available hardware drivers, indexed by name. */
public"
%javamethodmodifiers sigrok::Context::input_formats "/** Available input formats, indexed by name. */
public"
%javamethodmodifiers sigrok::Context::input_format_match "/** Lookup the responsible input module for an input file. */
public"
%javamethodmodifiers sigrok::Context::output_formats "/** Available output formats, indexed by name. */
public"
%javamethodmodifiers sigrok::Context::log_level "/** Current log level. */
public"
%javamethodmodifiers sigrok::Context::set_log_level "/** Set the log level.
   * @param level LogLevel to use. */
public"
%javamethodmodifiers sigrok::Context::set_log_callback "/** Set the log callback.
   * @param callback Callback of the form callback(LogLevel, string). */
public"
%javamethodmodifiers sigrok::Context::set_log_callback_default "/** Set the log callback to the default handler. */
public"
%javamethodmodifiers sigrok::Context::set_resource_reader "/** Install a delegate for reading resource files.
   * @param reader The resource reader delegate, or nullptr to unset. */
public"
%javamethodmodifiers sigrok::Context::create_session "/** Create a new session. */
public"
%javamethodmodifiers sigrok::Context::create_user_device "/** Create a new user device. */
public"
%javamethodmodifiers sigrok::Context::create_header_packet "/** Create a header packet. */
public"
%javamethodmodifiers sigrok::Context::create_meta_packet "/** Create a meta packet. */
public"
%javamethodmodifiers sigrok::Context::create_logic_packet "/** Create a logic packet. */
public"
%javamethodmodifiers sigrok::Context::create_analog_packet "/** Create an analog packet. */
public"
%javamethodmodifiers sigrok::Context::load_session "/** Load a saved session.
   * @param filename File name string. */
public"
%javamethodmodifiers sigrok::Context::create_trigger "/** Create a new trigger.
   * @param name Name string for new trigger. */
public"
%javamethodmodifiers sigrok::Context::open_file "/** Open an input file.
   * @param filename File name string. */
public"
%javamethodmodifiers sigrok::Context::open_stream "/** Open an input stream based on header data.
   * @param header Initial data from stream. */
public"
%typemap(javaclassmodifiers) sigrok::DataType "/** Configuration data type. */
public class"
%typemap(javaclassmodifiers) sigrok::Device "/** A generic device, either hardware or virtual. */
public class"
%javamethodmodifiers sigrok::Device::vendor "/** Vendor name for this device. */
public"
%javamethodmodifiers sigrok::Device::model "/** Model name for this device. */
public"
%javamethodmodifiers sigrok::Device::version "/** Version string for this device. */
public"
%javamethodmodifiers sigrok::Device::serial_number "/** Serial number for this device. */
public"
%javamethodmodifiers sigrok::Device::connection_id "/** Connection ID for this device. */
public"
%javamethodmodifiers sigrok::Device::channels "/** List of the channels available on this device. */
public"
%javamethodmodifiers sigrok::Device::channel_groups "/** Channel groups available on this device, indexed by name. */
public"
%javamethodmodifiers sigrok::Device::open "/** Open device. */
public"
%javamethodmodifiers sigrok::Device::close "/** Close device. */
public"
%typemap(javaclassmodifiers) sigrok::Driver "/** A hardware driver provided by the library. */
public class"
%javamethodmodifiers sigrok::Driver::name "/** Name of this driver. */
public"
%javamethodmodifiers sigrok::Driver::long_name "/** Long name for this driver. */
public"
%javamethodmodifiers sigrok::Driver::scan_options "/** Scan options supported by this driver. */
public"
%javamethodmodifiers sigrok::Driver::scan "/** Scan for devices and return a list of devices found.
   * @param options Mapping of (ConfigKey, value) pairs. */
public"
%typemap(javaclassmodifiers) sigrok::EnumValue "/** Base class for objects which wrap an enumeration value from libsigrok. */
public class"
%javamethodmodifiers sigrok::EnumValue::id "/** The integer constant associated with this value. */
public"
%javamethodmodifiers sigrok::EnumValue::name "/** The name associated with this value. */
public"
%typemap(javaclassmodifiers) sigrok::Error "/** Exception thrown when an error code is returned by any libsigrok call. */
public class"
%typemap(javaclassmodifiers) sigrok::HardwareDevice "/** A real hardware device, connected via a driver. */
public class"
%javamethodmodifiers sigrok::HardwareDevice::driver "/** Driver providing this device. */
public"
%typemap(javaclassmodifiers) sigrok::Header "/** Payload of a datafeed header packet. */
public class"
%typemap(javaclassmodifiers) sigrok::Input "/** An input instance (an input format applied to a file or stream) */
public class"
%javamethodmodifiers sigrok::Input::device "/** Virtual device associated with this input. */
public"
%javamethodmodifiers sigrok::Input::send "/** Send next stream data.
   * @param length Length of data.
   * @param data Next stream data. */
public"
%javamethodmodifiers sigrok::Input::end "/** Signal end of input data. */
public"
%typemap(javaclassmodifiers) sigrok::InputDevice "/** A virtual device associated with an input. */
public class"
%typemap(javaclassmodifiers) sigrok::InputFormat "/** An input format supported by the library. */
public class"
%javamethodmodifiers sigrok::InputFormat::name "/** Name of this input format. */
public"
%javamethodmodifiers sigrok::InputFormat::description "/** Description of this input format. */
public"
%javamethodmodifiers sigrok::InputFormat::extensions "/** A list of preferred file name extensions for this file format. */
public"
%javamethodmodifiers sigrok::InputFormat::options "/** Options supported by this input format. */
public"
%javamethodmodifiers sigrok::InputFormat::create_input "/** Create an input using this input format.
   * @param options Mapping of (option name, value) pairs. */
public"
%typemap(javaclassmodifiers) sigrok::Logic "/** Payload of a datafeed packet with logic data. */
public class"
%typemap(javaclassmodifiers) sigrok::LogLevel "/** Log verbosity level. */
public class"
%typemap(javacode) sigrok::LogLevel %{
  /** Output no messages at all. */
  public static final LogLevel NONE = new LogLevel(classesJNI.LogLevel_NONE_get(), false);

  /** Output error messages. */
  public static final LogLevel ERR = new LogLevel(classesJNI.LogLevel_ERR_get(), false);

  /** Output warnings. */
  public static final LogLevel WARN = new LogLevel(classesJNI.LogLevel_WARN_get(), false);

  /** Output informational messages. */
  public static final LogLevel INFO = new LogLevel(classesJNI.LogLevel_INFO_get(), false);

  /** Output debug messages. */
  public static final LogLevel DBG = new LogLevel(classesJNI.LogLevel_DBG_get(), false);

  /** Output very noisy debug messages. */
  public static final LogLevel SPEW = new LogLevel(classesJNI.LogLevel_SPEW_get(), false);

%}
%typemap(javaclassmodifiers) sigrok::Meta "/** Payload of a datafeed metadata packet. */
public class"
%typemap(javaclassmodifiers) sigrok::Option "/** An option used by an output format. */
public class"
%javamethodmodifiers sigrok::Option::id "/** Short name of this option suitable for command line usage. */
public"
%javamethodmodifiers sigrok::Option::name "/** Short name of this option suitable for GUI usage. */
public"
%javamethodmodifiers sigrok::Option::description "/** Description of this option in a sentence. */
public"
%javamethodmodifiers sigrok::Option::default_value "/** Default value for this option. */
public"
%javamethodmodifiers sigrok::Option::values "/** Possible values for this option, if a limited set. */
public"
%javamethodmodifiers sigrok::Option::parse_string "/** Parse a string argument into the appropriate type for this option. */
public"
%typemap(javaclassmodifiers) sigrok::Output "/** An output instance (an output format applied to a device) */
public class"
%javamethodmodifiers sigrok::Output::receive "/** Update output with data from the given packet.
   * @param packet Packet to handle. */
public"
%typemap(javaclassmodifiers) sigrok::OutputFlag "/** Flag applied to output modules. */
public class"
%typemap(javacode) sigrok::OutputFlag %{
  /** If set, this output module writes the output itself. */
  public static final OutputFlag INTERNAL_IO_HANDLING = new OutputFlag(classesJNI.OutputFlag_INTERNAL_IO_HANDLING_get(), false);

%}
%typemap(javaclassmodifiers) sigrok::OutputFormat "/** An output format supported by the library. */
public class"
%javamethodmodifiers sigrok::OutputFormat::name "/** Name of this output format. */
public"
%javamethodmodifiers sigrok::OutputFormat::description "/** Description of this output format. */
public"
%javamethodmodifiers sigrok::OutputFormat::extensions "/** A list of preferred file name extensions for this file format. */
public"
%javamethodmodifiers sigrok::OutputFormat::options "/** Options supported by this output format. */
public"
%javamethodmodifiers sigrok::OutputFormat::create_output "/** Create an output using this format.
   * @param device Device to output for.
   * @param options Mapping of (option name, value) pairs. */
public"
%javamethodmodifiers sigrok::OutputFormat::create_output "/** Create an output using this format.
   * @param device Device to output for.
   * @param options Mapping of (option name, value) pairs.
   * @param filename Name of destination file. */
public"
%javamethodmodifiers sigrok::OutputFormat::test_flag "/** Checks whether a given flag is set.
   * @param flag Flag to check */
public"
%typemap(javaclassmodifiers) sigrok::Packet "/** A packet on the session datafeed. */
public class"
%javamethodmodifiers sigrok::Packet::type "/** Type of this packet. */
public"
%javamethodmodifiers sigrok::Packet::payload "/** Payload of this packet. */
public"
%typemap(javaclassmodifiers) sigrok::PacketPayload "/** Abstract base class for datafeed packet payloads. */
public class"
%typemap(javaclassmodifiers) sigrok::PacketType "/** Type of datafeed packet. */
public class"
%typemap(javacode) sigrok::PacketType %{
  /** Payload is. */
  public static final PacketType HEADER = new PacketType(classesJNI.PacketType_HEADER_get(), false);

  /** End of stream (no further data). */
  public static final PacketType END = new PacketType(classesJNI.PacketType_END_get(), false);

  /** Payload is struct. */
  public static final PacketType META = new PacketType(classesJNI.PacketType_META_get(), false);

  /** The trigger matched at this point in the data feed. */
  public static final PacketType TRIGGER = new PacketType(classesJNI.PacketType_TRIGGER_get(), false);

  /** Payload is struct. */
  public static final PacketType LOGIC = new PacketType(classesJNI.PacketType_LOGIC_get(), false);

  /** Beginning of frame. */
  public static final PacketType FRAME_BEGIN = new PacketType(classesJNI.PacketType_FRAME_BEGIN_get(), false);

  /** End of frame. */
  public static final PacketType FRAME_END = new PacketType(classesJNI.PacketType_FRAME_END_get(), false);

  /** Payload is struct. */
  public static final PacketType ANALOG = new PacketType(classesJNI.PacketType_ANALOG_get(), false);

%}
%typemap(javaclassmodifiers) sigrok::Quantity "/** Measured quantity. */
public class"
%typemap(javacode) sigrok::Quantity %{
  /** Duty cycle, e.g. */
  public static final Quantity DUTY_CYCLE = new Quantity(classesJNI.Quantity_DUTY_CYCLE_get(), false);

  /** Continuity test. */
  public static final Quantity CONTINUITY = new Quantity(classesJNI.Quantity_CONTINUITY_get(), false);

  /** Electrical power, usually in W, or dBm. */
  public static final Quantity POWER = new Quantity(classesJNI.Quantity_POWER_get(), false);

  /** Gain (a transistor's gain, or hFE, for example). */
  public static final Quantity GAIN = new Quantity(classesJNI.Quantity_GAIN_get(), false);

  /** Logarithmic representation of sound pressure relative to a reference value. */
  public static final Quantity SOUND_PRESSURE_LEVEL = new Quantity(classesJNI.Quantity_SOUND_PRESSURE_LEVEL_get(), false);

  /** Carbon monoxide level. */
  public static final Quantity CARBON_MONOXIDE = new Quantity(classesJNI.Quantity_CARBON_MONOXIDE_get(), false);

  /** Humidity. */
  public static final Quantity RELATIVE_HUMIDITY = new Quantity(classesJNI.Quantity_RELATIVE_HUMIDITY_get(), false);

  /** Time. */
  public static final Quantity TIME = new Quantity(classesJNI.Quantity_TIME_get(), false);

  /** Wind speed. */
  public static final Quantity WIND_SPEED = new Quantity(classesJNI.Quantity_WIND_SPEED_get(), false);

  /** Pressure. */
  public static final Quantity PRESSURE = new Quantity(classesJNI.Quantity_PRESSURE_get(), false);

  /** Parallel inductance (LCR meter model). */
  public static final Quantity PARALLEL_INDUCTANCE = new Quantity(classesJNI.Quantity_PARALLEL_INDUCTANCE_get(), false);

  /** Parallel capacitance (LCR meter model). */
  public static final Quantity PARALLEL_CAPACITANCE = new Quantity(classesJNI.Quantity_PARALLEL_CAPACITANCE_get(), false);

  /** Parallel resistance (LCR meter model). */
  public static final Quantity PARALLEL_RESISTANCE = new Quantity(classesJNI.Quantity_PARALLEL_RESISTANCE_get(), false);

  /** Series inductance (LCR meter model). */
  public static final Quantity SERIES_INDUCTANCE = new Quantity(classesJNI.Quantity_SERIES_INDUCTANCE_get(), false);

  /** Series capacitance (LCR meter model). */
  public static final Quantity SERIES_CAPACITANCE = new Quantity(classesJNI.Quantity_SERIES_CAPACITANCE_get(), false);

  /** Series resistance (LCR meter model). */
  public static final Quantity SERIES_RESISTANCE = new Quantity(classesJNI.Quantity_SERIES_RESISTANCE_get(), false);

  /** Dissipation factor. */
  public static final Quantity DISSIPATION_FACTOR = new Quantity(classesJNI.Quantity_DISSIPATION_FACTOR_get(), false);

  /** Quality factor. */
  public static final Quantity QUALITY_FACTOR = new Quantity(classesJNI.Quantity_QUALITY_FACTOR_get(), false);

  /** Phase angle. */
  public static final Quantity PHASE_ANGLE = new Quantity(classesJNI.Quantity_PHASE_ANGLE_get(), false);

  /** Difference from reference value. */
  public static final Quantity DIFFERENCE = new Quantity(classesJNI.Quantity_DIFFERENCE_get(), false);

  /** Count. */
  public static final Quantity COUNT = new Quantity(classesJNI.Quantity_COUNT_get(), false);

  /** Power factor. */
  public static final Quantity POWER_FACTOR = new Quantity(classesJNI.Quantity_POWER_FACTOR_get(), false);

  /** Apparent power. */
  public static final Quantity APPARENT_POWER = new Quantity(classesJNI.Quantity_APPARENT_POWER_get(), false);

  /** Mass. */
  public static final Quantity MASS = new Quantity(classesJNI.Quantity_MASS_get(), false);

  /** Harmonic ratio. */
  public static final Quantity HARMONIC_RATIO = new Quantity(classesJNI.Quantity_HARMONIC_RATIO_get(), false);

%}
%typemap(javaclassmodifiers) sigrok::QuantityFlag "/** Flag applied to measured quantity. */
public class"
%typemap(javacode) sigrok::QuantityFlag %{
  /** Voltage measurement is alternating current (AC). */
  public static final QuantityFlag AC = new QuantityFlag(classesJNI.QuantityFlag_AC_get(), false);

  /** Voltage measurement is direct current (DC). */
  public static final QuantityFlag DC = new QuantityFlag(classesJNI.QuantityFlag_DC_get(), false);

  /** This is a true RMS measurement. */
  public static final QuantityFlag RMS = new QuantityFlag(classesJNI.QuantityFlag_RMS_get(), false);

  /** Value is voltage drop across a diode, or NAN. */
  public static final QuantityFlag DIODE = new QuantityFlag(classesJNI.QuantityFlag_DIODE_get(), false);

  /** Device is in \"hold\" mode (repeating the last measurement). */
  public static final QuantityFlag HOLD = new QuantityFlag(classesJNI.QuantityFlag_HOLD_get(), false);

  /** Device is in \"max\" mode, only updating upon a new max value. */
  public static final QuantityFlag MAX = new QuantityFlag(classesJNI.QuantityFlag_MAX_get(), false);

  /** Device is in \"min\" mode, only updating upon a new min value. */
  public static final QuantityFlag MIN = new QuantityFlag(classesJNI.QuantityFlag_MIN_get(), false);

  /** Device is in autoranging mode. */
  public static final QuantityFlag AUTORANGE = new QuantityFlag(classesJNI.QuantityFlag_AUTORANGE_get(), false);

  /** Device is in relative mode. */
  public static final QuantityFlag RELATIVE = new QuantityFlag(classesJNI.QuantityFlag_RELATIVE_get(), false);

  /** Sound pressure level is A-weighted in the frequency domain, according to IEC 61672:2003. */
  public static final QuantityFlag SPL_FREQ_WEIGHT_A = new QuantityFlag(classesJNI.QuantityFlag_SPL_FREQ_WEIGHT_A_get(), false);

  /** Sound pressure level is C-weighted in the frequency domain, according to IEC 61672:2003. */
  public static final QuantityFlag SPL_FREQ_WEIGHT_C = new QuantityFlag(classesJNI.QuantityFlag_SPL_FREQ_WEIGHT_C_get(), false);

  /** Sound pressure level is Z-weighted (i.e. */
  public static final QuantityFlag SPL_FREQ_WEIGHT_Z = new QuantityFlag(classesJNI.QuantityFlag_SPL_FREQ_WEIGHT_Z_get(), false);

  /** Sound pressure level is not weighted in the frequency domain, albeit without standards-defined low and high frequency limits. */
  public static final QuantityFlag SPL_FREQ_WEIGHT_FLAT = new QuantityFlag(classesJNI.QuantityFlag_SPL_FREQ_WEIGHT_FLAT_get(), false);

  /** Sound pressure level measurement is S-weighted (1s) in the time domain. */
  public static final QuantityFlag SPL_TIME_WEIGHT_S = new QuantityFlag(classesJNI.QuantityFlag_SPL_TIME_WEIGHT_S_get(), false);

  /** Sound pressure level measurement is F-weighted (125ms) in the time domain. */
  public static final QuantityFlag SPL_TIME_WEIGHT_F = new QuantityFlag(classesJNI.QuantityFlag_SPL_TIME_WEIGHT_F_get(), false);

  /** Sound pressure level is time-averaged (LAT), also known as Equivalent Continuous A-weighted Sound Level (LEQ). */
  public static final QuantityFlag SPL_LAT = new QuantityFlag(classesJNI.QuantityFlag_SPL_LAT_get(), false);

  /** Sound pressure level represented as a percentage of measurements that were over a preset alarm level. */
  public static final QuantityFlag SPL_PCT_OVER_ALARM = new QuantityFlag(classesJNI.QuantityFlag_SPL_PCT_OVER_ALARM_get(), false);

  /** Time is duration (as opposed to epoch, ...). */
  public static final QuantityFlag DURATION = new QuantityFlag(classesJNI.QuantityFlag_DURATION_get(), false);

  /** Device is in \"avg\" mode, averaging upon each new value. */
  public static final QuantityFlag AVG = new QuantityFlag(classesJNI.QuantityFlag_AVG_get(), false);

  /** Reference value shown. */
  public static final QuantityFlag REFERENCE = new QuantityFlag(classesJNI.QuantityFlag_REFERENCE_get(), false);

  /** Unstable value (hasn't settled yet). */
  public static final QuantityFlag UNSTABLE = new QuantityFlag(classesJNI.QuantityFlag_UNSTABLE_get(), false);

  /** Measurement is four wire (e.g. */
  public static final QuantityFlag FOUR_WIRE = new QuantityFlag(classesJNI.QuantityFlag_FOUR_WIRE_get(), false);

%}
%typemap(javaclassmodifiers) sigrok::Rational "/** Number represented by a numerator/denominator integer pair. */
public class"
%javamethodmodifiers sigrok::Rational::numerator "/** Numerator, i.e. */
public"
%javamethodmodifiers sigrok::Rational::denominator "/** Denominator, i.e. */
public"
%javamethodmodifiers sigrok::Rational::value "/** Actual (lossy) value. */
public"
%typemap(javaclassmodifiers) sigrok::ResourceReader "/** Resource reader delegate. */
public class"
%typemap(javaclassmodifiers) sigrok::Session "/** A sigrok session. */
public class"
%javamethodmodifiers sigrok::Session::add_device "/** Add a device to this session.
   * @param device Device to add. */
public"
%javamethodmodifiers sigrok::Session::devices "/** List devices attached to this session. */
public"
%javamethodmodifiers sigrok::Session::remove_devices "/** Remove all devices from this session. */
public"
%javamethodmodifiers sigrok::Session::add_datafeed_callback "/** Add a datafeed callback to this session.
   * @param callback Callback of the form callback(Device, Packet). */
public"
%javamethodmodifiers sigrok::Session::remove_datafeed_callbacks "/** Remove all datafeed callbacks from this session. */
public"
%javamethodmodifiers sigrok::Session::start "/** Start the session. */
public"
%javamethodmodifiers sigrok::Session::run "/** Run the session event loop. */
public"
%javamethodmodifiers sigrok::Session::stop "/** Stop the session. */
public"
%javamethodmodifiers sigrok::Session::is_running "/** Return whether the session is running. */
public"
%javamethodmodifiers sigrok::Session::set_stopped_callback "/** Set callback to be invoked on session stop. */
public"
%javamethodmodifiers sigrok::Session::trigger "/** Get current trigger setting. */
public"
%javamethodmodifiers sigrok::Session::context "/** Get the context. */
public"
%javamethodmodifiers sigrok::Session::set_trigger "/** Set trigger setting.
   * @param trigger Trigger object to use. */
public"
%javamethodmodifiers sigrok::Session::filename "/** Get filename this session was loaded from. */
public"
%typemap(javaclassmodifiers) sigrok::SessionDevice "/** A virtual device associated with a stored session. */
public class"
%typemap(javaclassmodifiers) sigrok::Trigger "/** A trigger configuration. */
public class"
%javamethodmodifiers sigrok::Trigger::name "/** Name of this trigger configuration. */
public"
%javamethodmodifiers sigrok::Trigger::stages "/** List of the stages in this trigger. */
public"
%javamethodmodifiers sigrok::Trigger::add_stage "/** Add a new stage to this trigger. */
public"
%typemap(javaclassmodifiers) sigrok::TriggerMatch "/** A match condition in a trigger configuration. */
public class"
%javamethodmodifiers sigrok::TriggerMatch::channel "/** Channel this condition matches on. */
public"
%javamethodmodifiers sigrok::TriggerMatch::type "/** Type of match. */
public"
%javamethodmodifiers sigrok::TriggerMatch::value "/** Threshold value. */
public"
%typemap(javaclassmodifiers) sigrok::TriggerMatchType "/** Trigger match type. */
public class"
%typemap(javaclassmodifiers) sigrok::TriggerStage "/** A stage in a trigger configuration. */
public class"
%javamethodmodifiers sigrok::TriggerStage::number "/** Index number of this stage. */
public"
%javamethodmodifiers sigrok::TriggerStage::matches "/** List of match conditions on this stage. */
public"
%javamethodmodifiers sigrok::TriggerStage::add_match "/** Add a new match condition to this stage.
   * @param type TriggerMatchType to apply.
   * @param channel Channel to match on. */
public"
%javamethodmodifiers sigrok::TriggerStage::add_match "/** Add a new match condition to this stage.
   * @param type TriggerMatchType to apply.
   * @param value Threshold value.
   * @param channel Channel to match on. */
public"
%typemap(javaclassmodifiers) sigrok::Unit "/** Unit of measurement. */
public class"
%typemap(javacode) sigrok::Unit %{
  /** Volt. */
  public static final Unit VOLT = new Unit(classesJNI.Unit_VOLT_get(), false);

  /** Ampere (current). */
  public static final Unit AMPERE = new Unit(classesJNI.Unit_AMPERE_get(), false);

  /** Ohm (resistance). */
  public static final Unit OHM = new Unit(classesJNI.Unit_OHM_get(), false);

  /** Farad (capacity). */
  public static final Unit FARAD = new Unit(classesJNI.Unit_FARAD_get(), false);

  /** Kelvin (temperature). */
  public static final Unit KELVIN = new Unit(classesJNI.Unit_KELVIN_get(), false);

  /** Degrees Celsius (temperature). */
  public static final Unit CELSIUS = new Unit(classesJNI.Unit_CELSIUS_get(), false);

  /** Degrees Fahrenheit (temperature). */
  public static final Unit FAHRENHEIT = new Unit(classesJNI.Unit_FAHRENHEIT_get(), false);

  /** Hertz (frequency, 1/s, [Hz]). */
  public static final Unit HERTZ = new Unit(classesJNI.Unit_HERTZ_get(), false);

  /** Percent value. */
  public static final Unit PERCENTAGE = new Unit(classesJNI.Unit_PERCENTAGE_get(), false);

  /** Boolean value. */
  public static final Unit BOOLEAN = new Unit(classesJNI.Unit_BOOLEAN_get(), false);

  /** Time in seconds. */
  public static final Unit SECOND = new Unit(classesJNI.Unit_SECOND_get(), false);

  /** Unit of conductance, the inverse of resistance. */
  public static final Unit SIEMENS = new Unit(classesJNI.Unit_SIEMENS_get(), false);

  /** An absolute measurement of power, in decibels, referenced to 1 milliwatt (dBm). */
  public static final Unit DECIBEL_MW = new Unit(classesJNI.Unit_DECIBEL_MW_get(), false);

  /** Voltage in decibel, referenced to 1 volt (dBV). */
  public static final Unit DECIBEL_VOLT = new Unit(classesJNI.Unit_DECIBEL_VOLT_get(), false);

  /** Measurements that intrinsically do not have units attached, such as ratios, gains, etc. */
  public static final Unit UNITLESS = new Unit(classesJNI.Unit_UNITLESS_get(), false);

  /** Sound pressure level, in decibels, relative to 20 micropascals. */
  public static final Unit DECIBEL_SPL = new Unit(classesJNI.Unit_DECIBEL_SPL_get(), false);

  /** Normalized (0 to 1) concentration of a substance or compound with 0 representing a concentration of 0%, and 1 being 100%. */
  public static final Unit CONCENTRATION = new Unit(classesJNI.Unit_CONCENTRATION_get(), false);

  /** Revolutions per minute. */
  public static final Unit REVOLUTIONS_PER_MINUTE = new Unit(classesJNI.Unit_REVOLUTIONS_PER_MINUTE_get(), false);

  /** Apparent power [VA]. */
  public static final Unit VOLT_AMPERE = new Unit(classesJNI.Unit_VOLT_AMPERE_get(), false);

  /** Real power [W]. */
  public static final Unit WATT = new Unit(classesJNI.Unit_WATT_get(), false);

  /** Consumption [Wh]. */
  public static final Unit WATT_HOUR = new Unit(classesJNI.Unit_WATT_HOUR_get(), false);

  /** Wind speed in meters per second. */
  public static final Unit METER_SECOND = new Unit(classesJNI.Unit_METER_SECOND_get(), false);

  /** Pressure in hectopascal. */
  public static final Unit HECTOPASCAL = new Unit(classesJNI.Unit_HECTOPASCAL_get(), false);

  /** Relative humidity assuming air temperature of 293 Kelvin (rF). */
  public static final Unit HUMIDITY_293K = new Unit(classesJNI.Unit_HUMIDITY_293K_get(), false);

  /** Plane angle in 1/360th of a full circle. */
  public static final Unit DEGREE = new Unit(classesJNI.Unit_DEGREE_get(), false);

  /** Henry (inductance). */
  public static final Unit HENRY = new Unit(classesJNI.Unit_HENRY_get(), false);

  /** Mass in gram [g]. */
  public static final Unit GRAM = new Unit(classesJNI.Unit_GRAM_get(), false);

  /** Mass in carat [ct]. */
  public static final Unit CARAT = new Unit(classesJNI.Unit_CARAT_get(), false);

  /** Mass in ounce [oz]. */
  public static final Unit OUNCE = new Unit(classesJNI.Unit_OUNCE_get(), false);

  /** Mass in troy ounce [oz t]. */
  public static final Unit TROY_OUNCE = new Unit(classesJNI.Unit_TROY_OUNCE_get(), false);

  /** Mass in pound [lb]. */
  public static final Unit POUND = new Unit(classesJNI.Unit_POUND_get(), false);

  /** Mass in pennyweight [dwt]. */
  public static final Unit PENNYWEIGHT = new Unit(classesJNI.Unit_PENNYWEIGHT_get(), false);

  /** Mass in grain [gr]. */
  public static final Unit GRAIN = new Unit(classesJNI.Unit_GRAIN_get(), false);

  /** Mass in tael (variants: Hong Kong, Singapore/Malaysia, Taiwan) */
  public static final Unit TAEL = new Unit(classesJNI.Unit_TAEL_get(), false);

  /** Mass in momme. */
  public static final Unit MOMME = new Unit(classesJNI.Unit_MOMME_get(), false);

  /** Mass in tola. */
  public static final Unit TOLA = new Unit(classesJNI.Unit_TOLA_get(), false);

  /** Pieces (number of items). */
  public static final Unit PIECE = new Unit(classesJNI.Unit_PIECE_get(), false);

%}
%typemap(javaclassmodifiers) sigrok::UserDevice "/** A virtual device, created by the user. */
public class"
%javamethodmodifiers sigrok::UserDevice::add_channel "/** Add a new channel to this device. */
public"
