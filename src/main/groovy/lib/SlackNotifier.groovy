package main.groovy.lib

import com.callfire.watson.common.util.SlackClient

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
     */
    def sendSlackNotification(channelName, text, isError) {
        if (!SlackClient.send(channelName, text, isError ? SlackClient.MessageColor.RED : SlackClient.MessageColor.GREEN).join()) {
            script.echo("\nERROR. Sending message to ${channelName} slack channel failed")
        }
    }
}
