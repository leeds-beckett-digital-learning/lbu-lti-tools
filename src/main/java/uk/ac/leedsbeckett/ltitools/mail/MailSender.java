/*
 * Copyright 2023 maber01.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.leedsbeckett.ltitools.mail;

import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 *
 * @author maber01
 */
public class MailSender
{
  static final Logger logger = Logger.getLogger( MailSender.class.getName() );
  
  String smtpHost;
  String sender;

  public MailSender( String smtpHost, String sender ) throws UnsupportedEncodingException
  {
    this.smtpHost = smtpHost;
    this.sender = sender;
  }
  
  public void processOneEmail( String to, String cc, String subject, String body, String mimeType, boolean ccself )
  {
    logger.log(Level.INFO, "Sending Email {0} {1} {2} {3}", new Object[ ]{sender, to, cc, subject});
    
    int cccount = 0;
    int ccn=0;
    if ( ccself ) cccount++;
    if ( cc != null ) cccount++;
    
    InternetAddress[] recipients = new InternetAddress[1];
    InternetAddress[] ccs = new InternetAddress[cccount];
    InternetAddress from;
    try
    {
      from          = new InternetAddress( sender );
      recipients[0] = new InternetAddress( to );
      if ( ccself )
        ccs[ccn++]      = from;
      if ( cc != null )
        ccs[ccn++]      = new InternetAddress( cc );
      sendHtmlEmail( subject, from, new InternetAddress[0], recipients, ccs, body, mimeType );
    }
    catch (MessagingException ex)
    {
      logger.log( Level.SEVERE, "Error sending email.", ex );
    }
  }
  
  
  public void sendHtmlEmail(
          String subject, 
          InternetAddress from, 
          InternetAddress[] reply, 
          InternetAddress[] recipients, 
          InternetAddress[] courtesycopies, 
          String message,
          String mimeType ) throws MessagingException
  {
    MimeMessage email = getBbEmail();
    MimeMultipart multipart = new MimeMultipart();
    BodyPart messageBodyPart = new MimeBodyPart();

    email.setSubject(subject);
    if ( reply != null && reply.length > 0 )
      email.setReplyTo( reply );
    email.setFrom( from );
    messageBodyPart.setContent(message, mimeType);
    multipart.addBodyPart(messageBodyPart);
    email.setRecipients( javax.mail.Message.RecipientType.TO, recipients );
    if ( courtesycopies != null && courtesycopies.length > 0 )
      email.setRecipients( javax.mail.Message.RecipientType.CC, courtesycopies );
    email.setContent(multipart);
    Transport.send(email);
  }  

  
  MimeMessage getBbEmail()
  {
    Properties mailprops = new Properties();
    mailprops.setProperty( "mail.smtp.host", smtpHost );
    Session mailSession = Session.getDefaultInstance(mailprops);
    
    return new MimeMessage(mailSession);
  }  
  
}
