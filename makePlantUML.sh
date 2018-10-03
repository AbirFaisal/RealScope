SRC_PATH="src/OWON_VDS_v1.0.24"

javadoc -sourcepath $SRC_PATH -classpath lib -d docs -docletpath umldoclet-1.1.0.jar -doclet nl.talsmasoftware.umldoclet.UMLDoclet com.owon -verbose