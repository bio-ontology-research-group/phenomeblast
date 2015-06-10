import org.apache.commons.net.smtp.SimpleSMTPHeader
import org.apache.commons.net.smtp.SMTPClient
import org.apache.commons.net.smtp.SMTPReply

class SendEmailAuth {
  static boolean sendMessage( \
    String host, \
    int port, \
    String from, \
    String pw, \
    List recipients, \
    String subject, \
    String message)
  {
    assert host != null
    assert host.trim() != ''
    assert port != null
    assert port > 0
    assert port < 65535
    assert from != null
    assert pw != null
    assert recipients != null
    assert recipients.size() >= 1
    assert subject != null
    assert subject.trim() != ''
    assert message != null
    assert message.trim() != ''

    int timeout = 2000
    int soTimeout = 10000

    def client = new SMTPClient()
    client.defaultTimeout = timeout
    println "Set default timeout to ${client.defaultTimeout}."

    println "Connecting to ${host} ..."
    try {
      client.connect(host, port)
    } catch (Throwable t) {
      println "Error connecting to ${host}: ${t.localizedMessage}"
      return
    }
    println "Connected to ${host}."
    println client.replyString
    int replyCode = client.replyCode
    if (!SMTPReply.isPositiveCompletion(replyCode)) {
      println "Server negative response with code ${replyCode}!"
      disconnect(client)
      return
    }

    client.soTimeout = soTimeout

    replyCode = client.sendCommand("EHLO", "localhost")
    println client.replyString

    String authStr = '~' + from + '~' + pw
    byte[] authBytes = authStr.bytes
    authBytes[0] = 0
    authBytes[from.size() + 1] = 0

    replyCode = client.sendCommand("AUTH", "PLAIN " + authBytes.encodeBase64())
    println client.replyString

    if (!client.setSender(from)) {
      println "Error setting sender!"
    }
    println client.replyString

    recipients.each {
      println "Adding recipient ${it} ..."
      if (!client.addRecipient(it)) {
        println "Error adding recipient!"
      }
      println client.replyString
    }

    def header = new SimpleSMTPHeader(from, recipients[0], subject)
    if (recipients.size() > 1) {
      recipients[1..-1].each {
        println "Adding CC recipient ${it} ..."
        header.addCC(it)
      }
    }
    println "Headers ...\n${header}"

    if (!client.sendShortMessageData(header.toString() + message)) {
      println "Error sending message!"
    }
    println client.replyString

    disconnect(client)
  }

  static void disconnect(SMTPClient client) {
    if ((client != null) && (client.connected)) {
      println "Disconnecting client ..."
      try {
        client.disconnect()
        println "Client disconnected."
      } catch (Throwable t) {
        println "Error disconnecting client: ${t.localizedMessage}!"
      }
    }
  }
}