/* *****************************************************************************
 *
 * [ PSEmailDef.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *******************************************************************************/
package com.percussion.loader.objectstore;

import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.loader.PSLoaderException;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Represents a PSEmailDef xml object.
 */
public class PSEmailDef extends PSLoaderComponent 
   implements java.io.Serializable
{
   /**
    * Constructs the xml object based on the given parameters.
    * 
    * @param smtpHost
    *           the SMTP host of the mail server, may not be <code>null</code>
    *           or empty.
    * 
    * @param attachLogs
    *           the value determines whether to attach logs when send email.
    *           <code>true</code> if attach logs when send email;
    *           <code>false</code> otherwise.
    * 
    * @param strRecip
    *           the String value of a XML_NODE_RECIPIENT. Never
    *           <code>null</code> or empty.
    * 
    * @param fromAddr
    *           the from mail address, may not be <code>null</code> or empty.
    * 
    * @throws IllegalArgumentException
    *            if any parameters are invalid.
    */
   public PSEmailDef(String smtpHost, boolean attachLogs, String strRecip,
         String fromAddr)
   {
      if (smtpHost == null || smtpHost.trim().length() == 0)
         throw new IllegalArgumentException(
               "smtpHost must not be null or empty");
      if (strRecip == null || strRecip.trim().length() == 0)
         throw new IllegalArgumentException(
               "strRecip must not be null or empty");
      if (fromAddr == null || fromAddr.trim().length() == 0)
         throw new IllegalArgumentException(
               "fromAddr must not be null or empty");
      
      m_attachLogs = attachLogs;
      m_smtpHost = smtpHost;
      addRecipient(strRecip, false);
      m_fromAddr = fromAddr;
   }

   /**
    * Create this object from its XML representation
    * 
    * @param source
    *           The source element. See {@link #toXml(Document)}for the
    *           expected format. May not be <code>null</code>.
    * 
    * @throws IllegalArgumentException
    *            If <code>source</code> is <code>null</code>.
    * @throws PSUnknownNodeTypeException
    *            <code>source</code> is malformed.
    * @throws PSLoaderException
    *            if other error occurs.
    */
   public PSEmailDef(Element source)
      throws PSUnknownNodeTypeException, PSLoaderException
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");

      fromXml(source);
   }

   /**
    * Get the list of recipient objects.
    * 
    * @return A list over <code>String</code> of recipient email addresses.
    *    Never <code>null</code> but may be empty
    */
   public List getRecipients()
   {
      return m_recipients;
   }

   /**
    * Get the list of ccrecipient objects.
    * 
    * @return A list over <code>String</code> of ccrecipient email addresses.
    *    May be <code>null</code>.
    */
   public List getCCRecipients()
   {
      return m_ccRecipients;
   }

   /**
    * Add a recipient.
    *
    * @param strName The recipient to be added, 
    *    may not <code>null</code> or empty.
    * 
    * @param bCC A boolean if <code>true</code>
    *    the param is a cc recipient, otherwise just
    *    a recipient
    *
    * @throws IllegalArgumentException If params are
    *    invalid
    */
   public void addRecipient(String strName, boolean bCC)
   {
      if (strName == null || strName.trim().length() == 0)
         throw new IllegalArgumentException(
            "strName may not be null or empty");

      if (bCC)
      {
         m_ccRecipients.add(strName);
      }
      else 
         m_recipients.add(strName);
   }

   /**
    * Determines whether attaches log when send email
    *
    * @return <code>true</code> if attaches log when send email;
    *    <code>false</code> otherwise.
    */
   public boolean getAttachLogs()
   {
      return m_attachLogs;
   }

   /**
    * Set whether to attach log when send email.
    * 
    * @param attachLogs <code>true</code> if attaches log when send email;
    *    <code>false</code> otherwise.
    */
   public void setAttachLog(boolean attachLogs)
   {
      m_attachLogs = attachLogs;
   }
   
   /**
    * Get the smtp host, the email server.
    *
    * @return The SMTP host name, it may be empty if has been been set yet.
    */
   public String getSmtpHost()
   {
      return m_smtpHost;
   }
  
   /**
    * Get the from email address.
    * 
    * @return The from email address, never <code>null</code> or empty.
    */
   public String getFromAddr()
   {
      return m_fromAddr;
   }
   
   /**
    * Serializes this object's state to its XML representation.  
    * The format is:
    * &lt;!ELEMENT PSXEmailDef (Recipients, CCRecipients?)&gt;
    * &lt;!ATTLIST PSXEmailDef
    * attachlogs CDATA #REQUIRED
    * smtpHost CDATA #REQUIRED
    * fromAddr CDATA #REQUIRED
    * &gt;
    * &lt;!--
    * A list of email recipients.
    * --&gt;
    * &lt;!ELEMENT Recipients (Recipient+)&gt;
    * &lt;!--
    * A list of courtesy email recipients.
    * --&gt;
    * &lt;!ELEMENT CCRecipients (Recipient+)&gt;
    * &lt;!--
    * An email recipient.
    * --&gt;
    * &lt;!ELEMENT Recipient (#PCDATA)&gt;
    * </code></pre>
    *
    * @throws IllegalArgumentException If <code>doc</code> 
    * is <code>null</code>
    *   
    * 
    * See {@link PSLoaderComponent#toXml(Document)} for more info.
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");

      Element root = doc.createElement(XML_NODE_NAME);
      
      // Attributes
      String sAttachLogs = m_attachLogs ? PSLoaderComponent.YES_STRING :
                                          PSLoaderComponent.NO_STRING;
      root.setAttribute(XML_ATTR_ATTACHLOGS, sAttachLogs);
      root.setAttribute(XML_ATTR_SMTPHOST, m_smtpHost);
      root.setAttribute(XML_ATTR_FROMADDR, m_fromAddr);
      
      if (!m_recipients.isEmpty())
      {
         Element el = doc.createElement(XML_NODE_RECIPIENTS);
         
         for (int i=0; i<m_recipients.size(); i++)
         {
            PSXmlDocumentBuilder.addElement(doc, el, XML_NODE_RECIPIENT,
               (String) m_recipients.get(i));
         }
         root.appendChild(el);
      }

      if (!m_ccRecipients.isEmpty())
      {
         Element el = doc.createElement(XML_NODE_CCRECIPIENTS);
      
         for (int i=0; i<m_ccRecipients.size(); i++)
         {
            PSXmlDocumentBuilder.addElement(doc, el, XML_NODE_RECIPIENT,
               (String) m_ccRecipients.get(i));
         }   
         root.appendChild(el);
      }
      return root;
   }

   /**
    * @see PSLoaderComponent#fromXml(Element)
    */ 
   public void fromXml(Element sourceNode)
      throws PSUnknownNodeTypeException, PSLoaderException
   {
      validateElement(sourceNode, XML_NODE_NAME);

      String sAttachLogs = getRequiredAttribute(sourceNode, XML_ATTR_ATTACHLOGS);
      m_attachLogs = sAttachLogs.equals(PSLoaderComponent.YES_STRING);
      m_smtpHost = getRequiredAttribute(sourceNode, XML_ATTR_SMTPHOST);
      m_fromAddr = getRequiredAttribute(sourceNode, XML_ATTR_FROMADDR);
      
      PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);
      
      // Required
      Element listEl = getNextRequiredElement(tree, XML_NODE_RECIPIENTS, 
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      
      Element resetNode = null;

      // Load the component list objects
      while (listEl != null)
      {      
         if (listEl.getNodeName().equals(XML_NODE_RECIPIENTS))
         {
            // One required
            Element recip = getNextRequiredElement(tree, XML_NODE_RECIPIENT,
               PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
         
            // Reset the tree to this parent node below
            resetNode = listEl;

            // load all the recipients
            while (recip != null)
            {
               if (recip.getNodeName().equals(XML_NODE_RECIPIENT))
                  m_recipients.add(tree.getElementData(recip));

               recip = tree.getNextElement(
                  PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
            }
         }
         else if (listEl.getNodeName().equals(XML_NODE_CCRECIPIENTS))
         {
            // One required
            Element recip = getNextRequiredElement(tree, XML_NODE_RECIPIENT,
               PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
               
            // Reset the tree to this parent node below
            resetNode = listEl;

            // load all the recipients
            while (recip != null)
            {
               if (recip.getNodeName().equals(XML_NODE_RECIPIENT))
                  m_ccRecipients.add(tree.getElementData(recip));

               recip = tree.getNextElement(
                  PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
            }
         }
         
         // Keep loading next element
         if (resetNode != null)
            tree.setCurrent(resetNode);
         
         // Not Required - CCRecipients
         listEl = tree.getNextElement(
            PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
      }
   }

   /**
    * PSLoaderComponent#hashCode()
    */
   public int hashCode()
   {
      return m_smtpHost.hashCode()
            + m_recipients.hashCode()
            + m_ccRecipients.hashCode()
            + (m_attachLogs ? 1 : 0)
            + m_fromAddr.hashCode();
   }

   /**
    * @see PSLoaderComponent#equals(Object)
    */
   public boolean equals(Object obj)
   {
      if (obj == null || !(obj instanceof PSEmailDef))
         return false;

      PSEmailDef obj2 = (PSEmailDef) obj;

      return m_smtpHost.equals(obj2.m_smtpHost) 
            && (m_attachLogs == obj2.m_attachLogs)
            && m_fromAddr.equals(obj2.m_fromAddr)
            && m_recipients.equals(obj2.m_recipients)
            && m_ccRecipients.equals(obj2.m_ccRecipients);
   }

   /**
    * Copy construction. 
    * 
    * @param obj2 The to be copied or cloned object, must be an instance of
    *    <code>PSEmailDef</code>.
    */
   public void copyFrom(PSEmailDef obj2)
   {
      if (!(obj2 instanceof PSEmailDef))
         throw new IllegalArgumentException(
            "obj2 must be an instance of PSEmailDef");
         
      m_fromAddr = obj2.m_fromAddr;
      m_attachLogs = obj2.m_attachLogs;
      m_smtpHost = obj2.m_smtpHost;
      m_recipients = obj2.m_recipients;
      m_ccRecipients = obj2.m_ccRecipients;   
   }

   /**
    * The XML node name of this object. 
    */
   final public static String XML_NODE_NAME = "PSXEmailDef";   

    
   /**
    * The value determines whether to attach logs when send email.
    * <code>true</code> if attach logs when send email; <code>false</code>
    * otherwise.
    */
   protected boolean m_attachLogs;

   /**
    * The SMTP Host or the email server, initialized by constrcutor, 
    * never <code>null</code> after that.
    */
   protected String m_smtpHost = "";

   /**
    * The from address, initialized by constructor, never <code>null</code>
    * or empty after that.
    */
   protected String m_fromAddr = "";
      
   /**
    * A list of recipients, initialized in definition, 
    * never <code>null</code>, may be empty.
    */
   protected ArrayList m_recipients = new ArrayList();
   
   /**
    * A list of ccrecipients, initialized in definition, 
    * never <code>null</code>, may be empty.
    */
   protected ArrayList m_ccRecipients = new ArrayList();

   // Public constants for XML attribute default values
   final static public String XML_NODE_RECIPIENTS = "Recipients";
   final static public String XML_NODE_RECIPIENT = "Recipient";
   final static public String XML_NODE_CCRECIPIENTS = "CCRecipients";

   // Private constants for XML attribute and element name
   final static protected String XML_ATTR_ATTACHLOGS = "attachlogs";
   final static protected String XML_ATTR_SMTPHOST = "smtpHost";
   final static protected String XML_ATTR_FROMADDR = "fromAddr";
   
}
