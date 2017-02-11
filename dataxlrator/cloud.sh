#! / bin / bash           

echo $1 

java -jar teevr-dataxlrator-*-jar-with-dependencies.jar configfile=$1 remote=true

