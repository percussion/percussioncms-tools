/*[ PSMail.java ]**************************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader;


import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * This class contains utility method to send emails. 
 * It supports SMTP protocol only.
 */
public class PSMail
{

   /**
    * Sends an email to a SMTP server with the given parameters.
    *
    * @param subject The subject to use, assumed not <code>null</code> or empty.
    * @param messageTest The message, assumed not <code>null</code> or empty.
    */
    
   /**
    * Sends an email to a SMTP server with the given parameters.
    * 
    * @param smtpServer The SMTP server, may not <code>null</code> or empty
    * @param subject The email subject, may not <code>null</code> or empty
    * @param messageText The email message, may not <code>null</code> or empty
    * @param recipients The list of recipient email addresses, may not 
    *    <code>null</code>
    * @param ccRecipients The list of CC recipient email addresses, may not 
    *    <code>null</code>
    * @param fromAddr The from email address, , may not <code>null</code> or 
    *    empty
    */
   public static void send(String smtpServer, String subject,
      String messageText, String[] recipients, String[] ccRecipients, 
      String fromAddr)
   {
      if (smtpServer == null || smtpServer.trim().length() == 0)
         throw new IllegalArgumentException(
            "smtpServer may not be null or empty");
      
      if (subject == null || subject.trim().length() == 0)
         throw new IllegalArgumentException("subject may not be null or empty");

      if (messageText == null || messageText.trim().length() == 0)
         throw new IllegalArgumentException(
            "messageText may not be null or empty");
      
      if (recipients == null)
         throw new IllegalArgumentException("recipients may not be null");

      if (ccRecipients == null)
         throw new IllegalArgumentException("ccRecipients may not be null");

      if (fromAddr == null || fromAddr.trim().length() == 0)
         throw new IllegalArgumentException(
            "fromAddr may not be null or empty");

      try
      {
         // Get system properties
         Properties props = new Properties();

         // Setup mail server
         props.put("mail.smtp.host", smtpServer);

         // Get session
         Session session = Session.getDefaultInstance(props, null);

         InternetAddress[] toAddrs = new InternetAddress[recipients.length];
         for (int i=0; i < toAddrs.length; i++)
            toAddrs[i] = new InternetAddress(recipients[i]);

         InternetAddress[] ccAddrs = new InternetAddress[ccRecipients.length];
         for (int i=0; i < ccAddrs.length; i++)
            ccAddrs[i] = new InternetAddress(ccRecipients[i]);

         // Define message
         MimeMessage message = new MimeMessage(session);
         message.addRecipients(Message.RecipientType.TO, toAddrs);
         message.addRecipients(Message.RecipientType.CC, ccAddrs);
      
         message.setFrom(new InternetAddress(fromAddr));   
         message.setSubject(subject);
         message.setText(messageText);

         // Send message
         Transport.send(message);
      }
      catch (Exception e)
      {
         // TODO: log error
         e.printStackTrace();
      }
   }

}
