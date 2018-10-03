#-----Only works in JDK8-----#


SRC_PATH="src/OWON_VDS_v1.0.24/"

OPTIONS="-subpackages . -private"



javadoc $OPTIONS -sourcepath $SRC_PATH -classpath lib -d docs com.owon

