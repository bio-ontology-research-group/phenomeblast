import groovy.sql.Sql
import javax.mail.internet.*
import javax.mail.*
import javax.activation.*


def id = 0 ;
try {
  def sql = Sql.newInstance("jdbc:mysql://localhost:3306/phenomeblast", "root",
			  "", "com.mysql.jdbc.Driver") ;
  try {
  def check = sql.firstRow("SELECT * FROM job WHERE status=1") 
  if (check==null) { // no running job found
    def result = sql.firstRow("SELECT * FROM job WHERE status=0")
    if (result != null) { // no job in queue
      id = result.id
      sql.execute("UPDATE job SET status=1 WHERE id=$id") // running
      
      def infile = result.location
      def outfile = result.res
      def email = result.email
      def link = result.link
      
      def command = """/bin/bash /home/rh497/Public/software/phenomeblast/phenomeblast/phenomeBLAST.sh $infile $outfile"""
      def proc = command.execute()
      proc.waitFor()
      if (proc.exitValue()) {
	sql.execute("UPDATE job SET status=3 WHERE id=$id") // set as problematic
	sendfailuremail(email)
      }
      sendmail(email, link)
      sql.execute("UPDATE job SET status=2 WHERE id=$id") // finished
    }
  }
} catch (Exception E) {
  sql.execute("UPDATE job SET status=3 WHERE id=$id") // set as problematic
  }
} catch (Exception ABC) {} // everything failed

def sendmail(String email, String words) {
  subject = "PhenomeBLAST-web job completion"
  message = "Greetings,\n\nYour PhenomeBLAST job has completed. You can access your results at $words.\n\n"
  message += "Please use our service again.\nLive long and prosper,\nYour PhenomeBLAST."
  toAddress = email
  fromAddress = "rh497@cam.ac.uk"
  host = "mail.gen.cam.ac.uk"
  port = "25"



  Properties mprops = new Properties();
  mprops.setProperty("mail.transport.protocol","smtp");
  mprops.setProperty("mail.host",host);
  mprops.setProperty("mail.smtp.port",port);
  Session lSession = Session.getDefaultInstance(mprops,null);
  MimeMessage msg = new MimeMessage(lSession);


  //tokenize out the recipients in case they came in as a list
  StringTokenizer tok = new StringTokenizer(toAddress,";");
  ArrayList emailTos = new ArrayList();
  while(tok.hasMoreElements()){
    emailTos.add(new InternetAddress(tok.nextElement().toString()));
  }
  InternetAddress[] to = new InternetAddress[emailTos.size()];
  to = (InternetAddress[]) emailTos.toArray(to);
  msg.setRecipients(MimeMessage.RecipientType.TO,to);
  InternetAddress fromAddr = new InternetAddress(fromAddress);
  msg.setFrom(fromAddr);
  msg.setFrom(new InternetAddress(fromAddress));
  msg.setSubject(subject);
  msg.setText(message)

  Transport transporter = lSession.getTransport("smtp");
  transporter.connect();
  transporter.send(msg);
}

def sendfailuremail(String email, String words) {
  subject = "PhenomeBLAST-web error"
  message = "Greetings,\n\nYour PhenomeBLAST job has encountered an error. You may contact rh497@cam.ac.uk for support.\n\n"
  message += "Please use our service again.\nLive long and prosper,\nYour PhenomeBLAST."
  toAddress = email
  fromAddress = "rh497@cam.ac.uk"
  host = "mail.gen.cam.ac.uk"
  port = "25"



  Properties mprops = new Properties();
  mprops.setProperty("mail.transport.protocol","smtp");
  mprops.setProperty("mail.host",host);
  mprops.setProperty("mail.smtp.port",port);
  Session lSession = Session.getDefaultInstance(mprops,null);
  MimeMessage msg = new MimeMessage(lSession);


  //tokenize out the recipients in case they came in as a list
  StringTokenizer tok = new StringTokenizer(toAddress,";");
  ArrayList emailTos = new ArrayList();
  while(tok.hasMoreElements()){
    emailTos.add(new InternetAddress(tok.nextElement().toString()));
  }
  InternetAddress[] to = new InternetAddress[emailTos.size()];
  to = (InternetAddress[]) emailTos.toArray(to);
  msg.setRecipients(MimeMessage.RecipientType.TO,to);
  InternetAddress fromAddr = new InternetAddress(fromAddress);
  msg.setFrom(fromAddr);
  msg.setFrom(new InternetAddress(fromAddress));
  msg.setSubject(subject);
  msg.setText(message)

  Transport transporter = lSession.getTransport("smtp");
  transporter.connect();
  transporter.send(msg);
}
