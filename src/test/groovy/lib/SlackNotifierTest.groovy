package main.groovy.lib

import org.apache.commons.io.IOUtils
import org.apache.http.HttpStatus
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.entity.BasicHttpEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.message.BasicStatusLine
import spock.lang.Specification

import java.nio.charset.Charset

class SlackNotifierTest extends Specification {

    SlackNotifier slackNotifier = new SlackNotifier(new TestPipelineScript())

    def webhookUrl = "https://hooks.slack.com/services/test"
    def text = "testText"

    BasicStatusLine statusLine = Mock()
    CloseableHttpResponse httpResponse = Mock()
    CloseableHttpClient httpClient = Mock()
    BasicHttpEntity entity = Mock()

    def setup(){
        httpResponse.getStatusLine() >> statusLine
        GroovyMock(HttpClients, global: true)
        HttpClients.createDefault() >> httpClient
    }

    /*def "testSendSlackNotificationError"() {
        given:
        statusLine.getStatusCode() >> HttpStatus.SC_INTERNAL_SERVER_ERROR
        httpResponse.getEntity() >> entity
        entity.getContent() >> IOUtils.toInputStream("error", Charset.defaultCharset())
        when:
        def statCodeResult = statusLine.getStatusCode()
        def statusLineResult = httpResponse.getStatusLine()
        def httpEntityResult = httpResponse.getEntity()

        slackNotifier.sendSlackNotification(webhookUrl, text)
        then:
        statCodeResult == HttpStatus.SC_INTERNAL_SERVER_ERROR
        statusLineResult == statusLine
        httpEntityResult.content != null
        1 * httpClient.execute(_ as HttpUriRequest) >> httpResponse
    }*/

    def "testSendSlackNotificationSuccess"() {
        given:
        statusLine.getStatusCode() >> HttpStatus.SC_OK
        when:
        def statCodeResult = statusLine.getStatusCode()
        def statusLineResult = httpResponse.getStatusLine()

        slackNotifier.sendSlackNotification(webhookUrl, text)
        then:
        statCodeResult == HttpStatus.SC_OK
        statusLineResult == statusLine
        1 * httpClient.execute(_ as HttpUriRequest) >> httpResponse
        0 * httpResponse.getEntity()
    }
}
