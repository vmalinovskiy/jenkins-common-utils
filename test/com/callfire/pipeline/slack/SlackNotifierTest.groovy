package com.callfire.pipeline.slack

import com.callfire.pipeline.TestPipelineScript
import com.callfire.watson.common.util.SlackClient
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

    def "testSendSlackNotificationWithName"() {
        when:
        slackNotifier.sendSlackNotification(channelName, text, false, "test")

        then:
        1 * SlackClient.sendFrom(channelName, text, GREEN, "test") >> completableFuture
    }

    def "testSendSlackNotificationWithNameForError"() {
        when:
        slackNotifier.sendSlackNotification(channelName, text, true, "test")

        then:
        1 * SlackClient.sendFrom(channelName, text, RED, "test") >> completableFuture
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
