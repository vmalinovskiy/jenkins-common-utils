//this is here to be sure we have all required groovy dependecies downloaded during calling lib from jenkins pipeline
@Grab(group='com.callfire.watson.common', module='watson-common-util', version='3.9.386')
@Grab(group='javax.ws.rs', module='javax.ws.rs-api', version='2.1.1')
import com.callfire.watson.common.util.SlackClient;

import com.callfire.pipeline.slack.SlackNotifier

def sendSlackNotification(script, channelName, text, isError, senderName = null) {
    new SlackNotifier(script).sendSlackNotification(channelName, text, isError, senderName)
}
