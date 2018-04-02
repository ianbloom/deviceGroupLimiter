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

def accessId = "dSpe6j9eTQXs3Iph7jCU";
def accessKey = "dcm!p2d2w79V=5f}+[354xL=g{k442Y6h5qV}C_6";
def account = "ianbloom";

def deviceId = 1;
def deviceLimit = 600;

wildalias = 'USA';

resourcePath = "/device/devices/" + deviceId + "/devicedatasources";
queryParameters = "?filter=dataSourceName:Group_Device_Count";
data = "";

// Get the ID of the Group Device Count DataSource
responseDict = LMGET(accessId, accessKey, account, resourcePath, queryParameters, data);
responseBody = responseDict.body;
responseCode = responseDict.code;

output = new JsonSlurper().parseText(responseBody);

// Why in the world there is a device datasource ID i will never know
deviceDataSourceId = output.data.items[0].id;

resourcePath = "/device/devices/" + deviceId + "/devicedatasources/" + deviceDataSourceId + "/instances"
queryParameters = "?filter=displayName:" + wildalias;
data = ""

responseDict = LMGET(accessId, accessKey, account, resourcePath, queryParameters, data);
responseBody = responseDict.body;
responseCode = responseDict.code;

output = new JsonSlurper().parseText(responseBody);

instanceId = output.data.items[0].id;

// deviceId || deviceDataSourceId || instanceId NOW WE GET INSTANCE LEVEL THRESHHOLD ID

resourcePath = "/device/devices/" + deviceId + "/devicedatasources/" + deviceDataSourceId + "/instances/" + instanceId + "/alertsettings";
queryParameters = "";
data = "";

responseDict = LMGET(accessId, accessKey, account, resourcePath, queryParameters, data);
responseBody = responseDict.body;
responseCode = responseDict.code;

output = new JsonSlurper().parseText(responseBody);

alertSettingId = output.data.items[0].id;

// deviceId || deviceDataSourceId || instanceId || alertSettingId, must POST alertExpr

resourcePath = "/device/devices/" + deviceId + "/devicedatasources/" + deviceDataSourceId + "/instances/" + instanceId + "/alertsettings/" + alertSettingId;
queryParameters = "";
data = '{"alertExpr":"> ' + deviceLimit + '"}';
println(data);


responseDict = LMPUT(accessId, accessKey, account, resourcePath, queryParameters, data);
responseBody = responseDict.body;
responseCode = responseDict.code;

output = new JsonSlurper().parseText(responseBody);


print('body . ' + responseBody);


return 0;


//println('body. ' + responseBody);
//println('code. ' + responseCode);


/////////////////////////////////////
// Santa's Little Helper Functions //
/////////////////////////////////////

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

return 0;