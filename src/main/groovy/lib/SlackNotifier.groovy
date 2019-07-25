package lib

import groovy.json.JsonOutput
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients

import static org.apache.http.HttpStatus.SC_OK
import static org.apache.http.util.EntityUtils.toString

class SlackNotifier implements Serializable {

    def script

    /**
     * Instantiate slack notifier
     *
     * @param script Link to script object (like pipeline) to get/put data from/to
     */
    SlackNotifier(script) {
        this.script = script
    }

    /**
     * Send slack message to some channel specified by webhook url attached to that channel
     *
     * @param webhookUrl Slack channel webhook url
     * @param text Text of message to send
     */
    def sendSlackNotification(webhookUrl, text) {
        CloseableHttpClient httpClient = HttpClients.createDefault()
        try {
            HttpPost httpPost = new HttpPost(webhookUrl)
            httpPost.setEntity(new StringEntity(JsonOutput.toJson(["text": "${text}"])))
            def response = httpClient.execute(httpPost)
            if (response.getStatusLine().getStatusCode() != SC_OK) {
                String error = toString(response.getEntity(), "UTF-8")
                script.echo("\nERROR. Sending message to groovy-release slack channel failed\n    HTTP Status: ${response.getStatusLine().getStatusCode()} \n    Message: ${error}")
            }
        } finally {
            httpClient.close()
        }
    }
}
