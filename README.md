# RealScope
OWON VDS1022 Software Modification Project, Because having a USB scope should be like having a RealScope.


Working.zip contains a tested and functional version. Just use that for now.
I am still trying to figure out exactly what direction to go with the software.



JavaDocs: https://abirfaisal.github.io/RealScope/


**Windows:** If the unmodified software works this should as well.
Obviously you need the drivers so grab them from the stock Owon application.



**MacOS:**

This software is built and tested on MacOS. The master branch should
always build with no errors on JDK11.

Install drivers with Homebrew on MacOS:
> brew install libusb-compat libusb

If you haven't added JDK to your $PATH then you need to install it as well.
>brew install java


You need to compile with this option on JDK11 (not needed on JDK8):
> --add-exports=java.base/sun.nio.ch=ALL-UNNAMED