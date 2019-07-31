package main.groovy.lib

import com.callfire.watson.common.util.SlackClient
import main.groovy.lib.TestPipelineScript
import spock.lang.Specification

import java.util.concurrent.CompletableFuture

import static com.callfire.watson.common.util.SlackClient.MessageColor.GREEN
import static com.callfire.watson.common.util.SlackClient.MessageColor.RED

class SlackNotifierTest extends Specification {

    SlackNotifier slackNotifier = new SlackNotifier(new TestPipelineScript())

    def channelName = "testChannel"
    def text = "testText"

    CompletableFuture<Boolean> completableFuture = Mock()

    def setup(){
        GroovyMock(SlackClient, global: true)
        completableFuture.join() >> true
    }

    def "testSendSlackNotification"() {
        when:
        slackNotifier.sendSlackNotification(channelName, text, false)

        then:
        1 * SlackClient.send(channelName, text, GREEN) >> completableFuture
    }

    def "testSendSlackNotificationForError"() {
        when:
        slackNotifier.sendSlackNotification(channelName, text, true)

        then:
        1 * SlackClient.send(channelName, text, RED) >> completableFuture
    }
}
