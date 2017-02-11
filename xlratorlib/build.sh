#! / bin / bash           

rm -rf output/*.*

echo "Building Project Tree"
mvn clean install


cp target/teevr-dataxlrator-lib-*.jar output/

cp output/teevr-dataxlrator-lib-*.jar ../output
cp output/teevr-dataxlrator-lib-*.jar ../dataxlrator/libs/xlratorlib.jar
cp pom.xml ../dataxlrator/libs/
# install library
#mvn install:install-file -Dfile=libs/teevr-dataxlrator-lib-*-obfuscated.jar

echo "PACKAGING COMPLETED...."



