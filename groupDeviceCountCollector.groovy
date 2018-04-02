import com.santaba.agent.groovyapi.expect.Expect;
import com.santaba.agent.groovyapi.snmp.Snmp;
import com.santaba.agent.groovyapi.http.*;
import com.santaba.agent.groovyapi.jmx.*;
import org.xbill.DNS.*;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Hex;
import groovy.json.JsonSlurper;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.ContentType;

def accessId = hostProps.get("lmaccess.id");
def accessKey = hostProps.get("lmaccess.key");
def account = hostProps.get("lmaccount");

// Obtain all the necessary parameters for the coming HTTP requests
def wildvalue = instanceProps.get("wildvalue");
def wildalias = instanceProps.get("wildalias");
def deviceId = hostProps.get("system.deviceId");
def deviceLimit = instanceProps.get("auto.device_limit");
def deviceDataSourceId = instanceProps.get("auto.device_datasource_id");

// GET a list of devices in ##WILDVALUE## group
def resourcePath = "/device/groups/" + wildvalue + "/devices";
def queryParameters = "?size=1000";
def data = "";

responseDict = LMGET(accessId, accessKey, account, resourcePath, queryParameters, data);
responseBody = responseDict.body;
responseCode = responseDict.code;

output = new JsonSlurper().parseText(responseBody);

// total corresponds to the number of devices returned in the items array of data in the GET request
deviceCount = output.data.total;
// Output for device_count datapoint
println("device_count=" + deviceCount);


////////////////////////
// ALERT MODIFICATION //
////////////////////////

// First, we use our previously obtained deviceId and deviceDataSourceId to obtain the instanceId of our current instance
resourcePath = "/device/devices/" + deviceId + "/devicedatasources/" + deviceDataSourceId + "/instances"
queryParameters = "?filter=displayName:" + wildalias;
data = ""

responseDict = LMGET(accessId, accessKey, account, resourcePath, queryParameters, data);
responseBody = responseDict.body;
responseCode = responseDict.code;

output = new JsonSlurper().parseText(responseBody);

instanceId = output.data.items[0].id;

// Now we have:
// deviceId || deviceDataSourceId || instanceId 
//
// Let's GET:
// alertSettingId = Instance level alert threshhold

resourcePath = "/device/devices/" + deviceId + "/devicedatasources/" + deviceDataSourceId + "/instances/" + instanceId + "/alertsettings";
queryParameters = "";
data = "";

responseDict = LMGET(accessId, accessKey, account, resourcePath, queryParameters, data);
responseBody = responseDict.body;
responseCode = responseDict.code;

output = new JsonSlurper().parseText(responseBody);

alertSettingId = output.data.items[0].id;

// Now we have:
// deviceId || deviceDataSourceId || instanceId || alertSettingId 
//
// Let's POST:
// alertExpr = Payload modifying alert threshold
//
// IF device_limit == 0, POST an empty alertExpr to clear thresholds

if (deviceLimit == 0) {
	resourcePath = "/device/devices/" + deviceId + "/devicedatasources/" + deviceDataSourceId + "/instances/" + instanceId + "/alertsettings/" + alertSettingId;
	queryParameters = "";
	// This payload currently triggers a WARNING alert if the device_limit threshold is exceeded
	data = '{"alertExpr":"> ' + deviceLimit + '"}';

	responseDict = LMPUT(accessId, accessKey, account, resourcePath, queryParameters, data);
	responseBody = responseDict.body;
	responseCode = responseDict.code;

	output = new JsonSlurper().parseText(responseBody);
}
else {
	resourcePath = "/device/devices/" + deviceId + "/devicedatasources/" + deviceDataSourceId + "/instances/" + instanceId + "/alertsettings/" + alertSettingId;
	queryParameters = "";
	// This payload currently triggers a WARNING alert if the device_limit threshold is exceeded
	data = '{"alertExpr":""}';

	responseDict = LMPUT(accessId, accessKey, account, resourcePath, queryParameters, data);
	responseBody = responseDict.body;
	responseCode = responseDict.code;

	output = new JsonSlurper().parseText(responseBody);
}




return 0;

///////////////////////
//  Helper Functions //
///////////////////////

def LMPUT(_accessId, _accessKey, _account, _resourcePath, _queryParameters, _data) {

	// Initialize dictionary to hold response code and response body
	responseDict = [:];

	// Construcst URL to POST to from specified input
	url = 'https://' + _account + '.logicmonitor.com' + '/santaba/rest' + _resourcePath + _queryParameters;

	StringEntity params = new StringEntity(_data,ContentType.APPLICATION_JSON);

	// Get current time
	epoch = System.currentTimeMillis();

	// Calculate signature
	requestVars = 'PUT' + epoch + _data + _resourcePath;

	hmac = Mac.getInstance('HmacSHA256');
	secret = new SecretKeySpec(_accessKey.getBytes(), 'HmacSHA256');
	hmac.init(secret);
	hmac_signed = Hex.encodeHexString(hmac.doFinal(requestVars.getBytes()));
	signature = hmac_signed.bytes.encodeBase64();

	// HTTP Get
	CloseableHttpClient httpclient = HttpClients.createDefault();
	http_request = new HttpPut(url);
	http_request.addHeader("Authorization" , "LMv1 " + _accessId + ":" + signature + ":" + epoch);
	http_request.setHeader("Accept", "application/json");
	http_request.setHeader("Content-type", "application/json");
	http_request.setEntity(params);
	response = httpclient.execute(http_request);
	responseBody = EntityUtils.toString(response.getEntity());
	code = response.getStatusLine().getStatusCode();

	responseDict['code'] = code;
	responseDict['body'] = responseBody
	
	return responseDict;
}

def LMGET(_accessId, _accessKey, _account, _resourcePath, _queryParameters, _data) {
	// DATA SHOULD BE EMPTY
	// Initialize dictionary to hold response code and response body
	responseDict = [:];

	// Construcst URL to POST to from specified input
	url = 'https://' + _account + '.logicmonitor.com' + '/santaba/rest' + _resourcePath + _queryParameters;

	// Get current time
	epoch = System.currentTimeMillis();

	// Calculate signature
	requestVars = 'GET' + epoch + _data + _resourcePath;

	hmac = Mac.getInstance('HmacSHA256');
	secret = new SecretKeySpec(_accessKey.getBytes(), 'HmacSHA256');
	hmac.init(secret);
	hmac_signed = Hex.encodeHexString(hmac.doFinal(requestVars.getBytes()));
	signature = hmac_signed.bytes.encodeBase64();

	// HTTP Get
	CloseableHttpClient httpclient = HttpClients.createDefault();
	http_request = new HttpGet(url);
	http_request.addHeader("Authorization" , "LMv1 " + _accessId + ":" + signature + ":" + epoch);
	response = httpclient.execute(http_request);
	responseBody = EntityUtils.toString(response.getEntity());
	code = response.getStatusLine().getStatusCode();

	responseDict['code'] = code;
	responseDict['body'] = responseBody
	
	return responseDict;
}

def LMPOST(_accessId, _accessKey, _account, _resourcePath, _queryParameters, _data) {

	// Initialize dictionary to hold response code and response body
	responseDict = [:];

	// Construcst URL to POST to from specified input
	url = 'https://' + _account + '.logicmonitor.com' + '/santaba/rest' + _resourcePath + _queryParameters;

	StringEntity params = new StringEntity(_data,ContentType.APPLICATION_JSON);

	// Get current time
	epoch = System.currentTimeMillis();

	// Calculate signature
	requestVars = 'POST' + epoch + _data + _resourcePath;

	hmac = Mac.getInstance('HmacSHA256');
	secret = new SecretKeySpec(_accessKey.getBytes(), 'HmacSHA256');
	hmac.init(secret);
	hmac_signed = Hex.encodeHexString(hmac.doFinal(requestVars.getBytes()));
	signature = hmac_signed.bytes.encodeBase64();

	// HTTP Get
	CloseableHttpClient httpclient = HttpClients.createDefault();
	http_request = new HttpPost(url);
	http_request.addHeader("Authorization" , "LMv1 " + _accessId + ":" + signature + ":" + epoch);
	http_request.setHeader("Accept", "application/json");
	http_request.setHeader("Content-type", "application/json");
	http_request.setEntity(params);
	response = httpclient.execute(http_request);
	responseBody = EntityUtils.toString(response.getEntity());
	code = response.getStatusLine().getStatusCode();

	responseDict['code'] = code;
	responseDict['body'] = responseBody
	
	return responseDict;
}