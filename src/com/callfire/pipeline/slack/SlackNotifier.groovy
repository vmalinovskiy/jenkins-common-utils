package com.callfire.pipeline.slack

import static com.callfire.watson.common.util.SlackClient.send;
import static com.callfire.watson.common.util.SlackClient.sendFrom;
import static com.callfire.watson.common.util.SlackClient.MessageColor.GREEN;
import static com.callfire.watson.common.util.SlackClient.MessageColor.RED;

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
     * Send slack message to some channel
     *
     * @param channelName Slack channel name
     * @param text Text of message to send
     * @param isError Flag if this is error message or not (if error message would be sent in RED colour)
     * @param senderName Message sender name to show in slack (if null - name would be set automatically for watson project from ZK)
     */
    def sendSlackNotification(channelName, text, isError, senderName = null) {
        script.echo("Sending text: ${text} to channel ${channelName}, isError ${isError}, senderName ${senderName}")
        def isSent = senderName != null ? sendFrom(channelName, text, isError ? RED : GREEN, senderName).join() :
                send(channelName, text, isError ? RED : GREEN).join()
        if (!isSent) script.echo("\nERROR. Sending message to ${channelName} slack channel failed")
    }
}
