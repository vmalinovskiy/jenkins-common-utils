import main.groovy.lib.SlackNotifier

def sendSlackNotification(script, webhookUrl, text) {
    new SlackNotifier(script).sendSlackNotification(webhookUrl, text)
}
