import main.groovy.lib.SlackNotifier

def sendSlackNotification(script, channelName, text, isError) {
    new SlackNotifier(script).sendSlackNotification(channelName, text, isError)
}
