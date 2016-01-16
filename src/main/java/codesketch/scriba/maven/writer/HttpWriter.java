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

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import codesketch.scriba.maven.model.Credential;

/**
 * @author quirino.brizi
 */
public class HttpWriter implements Writer {

    private Log logger;
    private Credential credential;
    private URL targetUrl;
    private URL authenticateUrl;

    public HttpWriter(Log logger, Credential credential, URL targetUrl, URL authenticateUrl) {
        this.logger = logger;
        this.credential = credential;
        this.targetUrl = targetUrl;
        this.authenticateUrl = authenticateUrl;
    }

    /*
     * (non-Javadoc)
     * 
     * @see codesketch.scriba.maven.writer.Writer#write(java.lang.String)
     */
    @Override
    public void write(String data) throws MojoFailureException {

        try {
            HttpResponse<JsonNode> response = Unirest.post(this.authenticateUrl.toExternalForm())
                            .body(this.credential.toJson()).asJson();
            String accessToken = (String) response.getBody().getObject().get("accessToken");
            this.logger.debug(format("token: %s", accessToken));
            this.logger.info(format("authentication performed, sending %s to the server", data));
            HttpResponse<JsonNode> putDocumentResponse = Unirest.put(targetUrl.toExternalForm())
                            .header("Authorization", format("ApiKey %s", accessToken)).body(data)
                            .asJson();
            this.logger.info(putDocumentResponse.getBody().toString());
        } catch (UnirestException e) {
            throw new MojoFailureException(
                            format("can't send results to remote host [%s]", targetUrl), e);
        } finally {
            this.shutdownSilently();
        }
    }

    private void shutdownSilently() {
        // NOOP
        // try {
        // Unirest.shutdown();
        // } catch (IOException e) {
        // this.logger.warn("unable shutdown unirest!");
        // }
    }
}
