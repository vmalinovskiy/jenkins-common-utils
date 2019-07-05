@Grab(group='org.apache.httpcomponents', module='httpclient', version='4.5.8')
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import static org.apache.http.HttpStatus.SC_OK
import groovy.json.JsonOutput

def sendSlackNotification(script, webhookUrl, text) {
    CloseableHttpClient client = HttpClients.createDefault()
    try {
        HttpPost httpPost = new HttpPost(webhookUrl)
        httpPost.setEntity(new StringEntity(JsonOutput.toJson(["text": "${text}"])))
        CloseableHttpResponse response = client.execute(httpPost)
        if (response.getStatusLine().getStatusCode() != SC_OK) {
            String error = toString(response.getEntity(), "UTF-8")
            script.echo "\nERROR. Sending message to ez-release slack channel failed\n    HTTP Status: ${response.getStatusLine().getStatusCode()} \n    Message: ${error}"
        }
    } finally {
        client.close()
    }
}
