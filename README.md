# RealScope
OWON USB Oscilloscope Software Project, Because having a USB scope should be like having a RealScope.

JavaDocs: https://abirfaisal.github.io/RealScope/

USB access uses [Java Does USB](https://github.com/manuelbl/JavaDoesUSB), which is pure Java, so no native libraries or libusb are needed. It runs on Intel and Apple Silicon Macs, Windows and Linux.

Requires JDK 25. Run with:
> java --enable-native-access=ALL-UNNAMED -cp ... com.owon.vds.tiny.Main

The IntelliJ run configuration `RealScope` already sets this flag.

On Windows the scope needs the WinUSB driver instead of the libusb-win32 driver that OWON's installer sets up. Switch it once with [Zadig](https://zadig.akeo.ie/).

On Linux the user needs access to the device, e.g. with a udev rule for vendor `5345`, product `1234`.