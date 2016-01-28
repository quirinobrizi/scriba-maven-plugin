/*
 * Copyright [2015] [Quirino Brizi (quirino.brizi@gmail.com)]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 *
 */
package codesketch.scriba.maven.writer;

import static java.lang.String.format;

import java.net.URL;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.json.JSONArray;
import org.json.JSONObject;

import codesketch.scriba.maven.model.Credential;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

/**
 * @author quirino.brizi
 */
public class HttpWriter implements Writer {

	private Log logger;
	private Credential credential;
	private URL targetUrl;
	private URL authenticateUrl;
	private String apiKey;
	private boolean useApiKey = true;

	public HttpWriter(Log logger, String apiKey, URL targetUrl) {
		this(logger, null, targetUrl, null, apiKey, true);
	}

	public HttpWriter(Log logger, Credential credential, URL targetUrl,
			URL authenticateUrl) {
		this(logger, credential, targetUrl, authenticateUrl, null, false);
	}

	public HttpWriter(Log logger, Credential credential, URL targetUrl,
			URL authenticateUrl, String apiKey, boolean useApiKey) {
		this.logger = logger;
		this.credential = credential;
		this.targetUrl = targetUrl;
		this.authenticateUrl = authenticateUrl;
		this.apiKey = apiKey;
		this.useApiKey = useApiKey;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see codesketch.scriba.maven.writer.Writer#write(java.lang.String)
	 */
	@Override
	public void write(String data) throws MojoExecutionException {
		try {
			String accessToken = null;
			if (useApiKey) {
				accessToken = this.apiKey;
			} else {
				accessToken = getAccessToken();
			}
			this.logger.debug(format("token: %s", accessToken));
			this.logger
					.info(format(
							"authentication performed, sending %s to the server",
							data));
			HttpResponse<JsonNode> putDocumentResponse = Unirest
					.put(targetUrl.toExternalForm())
					.header("Authorization", format("ApiKey %s", accessToken))
					.body(data).asJson();
			this.logger.info(putDocumentResponse.getBody().toString());
		} catch (UnirestException e) {
			throw new MojoExecutionException(format(
					"can't send results to remote host [%s]", targetUrl), e);
		} finally {
			this.shutdownSilently();
		}
	}

	private String getAccessToken() throws UnirestException {
		HttpResponse<JsonNode> response = Unirest
				.post(this.authenticateUrl.toExternalForm())
				.body(this.credential.toJson()).asJson();
		return findAndExtractAccessToken(response);
	}

	private String findAndExtractAccessToken(HttpResponse<JsonNode> response) {
		String answer = null;
		if (null != response && null != response.getBody()) {
			JsonNode body = response.getBody();
			if (body.isArray()) {
				logger.info("there are multiple accounts responses get the first one");
				JSONArray array = body.getArray();
				JSONObject object = array.getJSONObject(0);
				answer = findAccessToken(object);
			} else {
				answer = findAccessToken(body.getObject());
			}
		}
		return answer;
	}

	private String findAccessToken(JSONObject object) {
		return object.getString("accessToken");
	}

	private void shutdownSilently() {
		// NOOP
	}
}
