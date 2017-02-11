#! / bin / bash           

inputconfigschema="resources/json/configuration"
outschemajs="../nodeApp/xlatorUI/configurator/configui/configschema.js"

inputdatamodel="resources/json/DataModel"
outdatamodeljs="../nodeApp/xlatorUI/configurator/configui/datamodelschema.js"


if [ -d "../xlratorlib" ] ; then

cd ../xlratorlib

sh build.sh

cd ../dataxlrator

else

echo "skipping dataxlrator library build"

fi

rm -rf output/*.*

echo "Building Project Tree"
# install library
#mvn install:install-file -Dfile=libs/xlratorlib.jar -DpomFile=libs/pom.xml
mvn clean install
rm $outschemajs
echo "Creating Javascript file for JSON Configuration Schema" 
echo "var schema;" >>$outschemajs
echo "schema =" >>$outschemajs
while read line           
do    
   echo $line >>$outschemajs

done <$inputconfigschema 


rm $outdatamodeljs
echo "Creating Javascript file for JSON Data Model Schema" 
echo "var schema;" >>$outdatamodeljs
echo "schema =" >>$outdatamodeljs
while read line           
do    
   echo $line >>$outdatamodeljs

done <$inputdatamodel 


echo "Packaging jar for AWS"

#jar  uvf target/teevr-dataxlrator-1.0.0-jar-with-dependencies.jar  defaultconfig.json 

#cp  -rf dbgstore/data target/classes 
cp  dbgstore/*.* target/classes
cp -rf certs target/classes
cp -rf ../datagenerator/configs target/classes

cp target/teevr-dataxlrator-*-jar-with-dependencies.jar .
cp target/teevr-dataxlrator-*-jar-with-dependencies.jar output/
rm -rf ../awsscripts/bins/*.jar
cp target/teevr-dataxlrator-*-jar-with-dependencies.jar ../awsscripts/bins/

# cp location*.txt output/
#cp  -rf dbgstore/data output/ 
cp   dbgstore/*.* output/
cp edge.sh output/
cp c-edge.sh output/
cp cloud.sh output/
cp run.sh output/
cp -rf certs output/
cp -rf ../datagenerator/configs output/

# AWS package
zip  -q output/teevr-dataxlrator-aws.zip teevr-dataxlrator-*-jar-with-dependencies.jar Procfile certs/*.* configs/ 
cd dbgstore
zip -q -u -r ../output/teevr-dataxlrator-aws.zip aws*.json 
cd ..

# Debug package Zip

zip -q output/teevr-dataxlrator-debug.zip teevr-dataxlrator-*-jar-with-dependencies.jar edge.sh cloud.sh certs/*.* configs/*.* 
cd dbgstore
zip -q -u -r ../output/teevr-dataxlrator-debug.zip * 
cd ..

cp output/teevr-dataxlrator-debug.zip ../output
cp output/teevr-dataxlrator-aws.zip ../output

rm teevr-dataxlrator-*-jar-with-dependencies.jar

echo "PACKAGING COMPLETED...."



