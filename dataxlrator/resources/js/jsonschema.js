var schema;
schema =
{
"$schema": "http://json-schema.org/draft-04/schema#",
"description": "Configuration file for Teevr Data Platform",
"definitions": {

"MQTTConfig": {
"type": "object",
"properties": {
"BrokerAddress": {
"type": "string"
},
"Port": {
"type": "integer"
},
"Username": {
"type": "string"
},
"Password": {
"type": "string"
},
"EnableSSL": {
"type": "boolean"
},
"ClientID": {
"type": "string"
},
"CACert": {
"type": "string"
},
"ClientCert": {
"type": "string"
},
"ClientKey": {
"type": "string"
},
"SubscribeTopics": {
"type":"array",
"item":
{
"type":"string"
},
"minItems": 1
},
"PublishTopics": {
"type":"array",
"item":
{
"type":"string"
},
"minItems": 1

}
}
},

"PostgresConfig": {
"type": "object",
"properties": {
"ServerAddress": {
"type": "string"
},
"Port": {
"type": "integer"
},
"username": {
"type": "string"
},
"password": {
"type": "string"
},
"PerfDBName": {
"type": "string"
},
"EventsDBName": {
"type": "string"
},
"DataDBName": {
"type": "string"
}
}
},


"InfluxdbConfig": {
"type": "object",
"properties": {
"ServerAddress": {
"type": "string"
},
"HTTPPort": {
"type": "integer"
},
"username": {
"type": "string"
},
"password": {
"type": "string"
},
"PerfDBName": {
"type": "string"
},
"EventsDBName": {
"type": "string"
},
"DataDBName": {
"type": "string"
}



}
}

},

"type": "object",
"properties":
{
"MQTT":
{
"type": "object",
"properties":
{
"CloudMQTT": {"$ref": "#/definitions/MQTTConfig"},

"EdgeMQTT": {"$ref": "#/definitions/MQTTConfig"}
}
},


"coap":
{
"type": "object",
"properties": {
"CoAPServerAddress": {
"type": "string"
},
"CoAPPort": {
"type": "integer"
},
"CoAPTopic": {
"type": "string"
},
"CoAPTopics":{
"type":"array",
"item":
{
"type":"string"
},
"minItems": 1,
"uniqueItems": true
},
"CoAPlatencytopic":{
"type":"string"
},
"CoAPthroughputtopic":{
"type":"string"
}
}

},
"advanced":
{ "type": "object",
"properties":{
"EnableDataCollector":{
"type": "boolean"},
"DataFile":{
"type": "string"
},
"enableJsonData":
{
"type": "boolean"
},
"enableZippedJsonData":
{
"type": "boolean"
},
"enableProtoData":
{
"type": "boolean"
},
"enableBenchmarking":
{
"type": "boolean"
},
"enableWindowSize":
{
"type": "boolean"
},
"EnableDBWrite":{
"type": "boolean"},

"Edgedb":{
"enum": ["Postgres", "Mongodb","Influxdb"]
},
"Clouddb":{
"enum": ["Postgres", "Mongodb","Influxdb"]
}

}
},
"CloudDB":
{
"type": "object",
"properties":
{
"influxdb": {"$ref": "#/definitions/InfluxdbConfig"},

"postgres": {"$ref": "#/definitions/PostgresConfig"}
}
},

"EdgeDB":
{
"type": "object",
"properties":
{
"influxdb": {"$ref": "#/definitions/InfluxdbConfig"},

"postgres": {"$ref": "#/definitions/PostgresConfig"}
}
},
"compression":
{
"type": "object",
"properties":{
"SampleSize":{
"type": "integer"
},
"RegressionSampleSize":{
"type": "integer"
},
"EnableLinearRegression":{
"type": "boolean"
},
"EnablePolyRegression":{
"type": "boolean"
}
}
}


},
"required": ["MQTT", "coap", "clouddb","edgedb","advanced","compression"]

}
