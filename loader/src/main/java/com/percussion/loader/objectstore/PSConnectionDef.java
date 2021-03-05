/*[ PSConnectionDef.java ]*********************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader.objectstore;

import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.loader.PSLoaderException;
import com.percussion.loader.PSLoaderUtils;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import java.util.Iterator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Encapsulates the connection information to the  Rhthmyx server.
 */
public class PSConnectionDef extends PSLoaderComponent
   implements java.io.Serializable
{
   /**
    * Creates the connection defintion of the server.
    *
    * @param server, name of the server, never <code>null</code> or empty.
    *
    * @param port, port number, never <code>null</code> or empty.
    *
    * @param user, user name, never <code>null</code> or empty.
    *
    * @param pwd, user's password, never <code>null</code> or empty.
    *
    * @param bEnc, if <code>true</code> then the passsword has been encoded else
    * not.
    *
    * @throws IllegalArgumentException if the parameters are invalid.
    * 
    * @throws PSLoaderException from <code>setServerName</code> and 
    *    <code>setPort</code>.
    */
   public PSConnectionDef(String server, String port, String user, String pwd,
       boolean bEnc)
      throws PSLoaderException
   {
      if (server == null || server.length() == 0)
         throw new IllegalArgumentException(
            "server name cannot be null or empty");
      if (port == null || port.length() == 0)
         throw new IllegalArgumentException(
            "port number cannot be null or empty");
      if (user == null || user.length() == 0)
         throw new IllegalArgumentException("user name cannot be null or empty");
      if (pwd == null || pwd.length() == 0)
         throw new IllegalArgumentException("pwd name cannot be null or empty");

      setServerName(server);
      setPort(port);
      m_user = user;
      m_pwd = pwd;
      if (bEnc)
         m_strEnc = YES_STRING;
      else
         m_strEnc = NO_STRING;
   }

   /**
     * Create this object from its XML representation
     *
     * @param source The source element.  See {@link #toXml(Document)} for
     * the expected format.  May not be <code>null</code>.
     *
     * @throws IllegalArgumentException If <code>source</code> is
     * <code>null</code>.
     * @throws PSUnknownNodeTypeException <code>source</code> is malformed.
    */
   public PSConnectionDef(Element source) throws PSUnknownNodeTypeException,
      PSLoaderException
   {
      if (source == null)
        throw new IllegalArgumentException("source may not be null");
     fromXml(source);
     // make sure all required properties exists
     validateProtocol(getServerProtocol());
     getServerName();
      getPort();
   }

   /**
    * Set the server protocol.
    *
    * @param protocol The to be set protocol, it must be either "http" or
    *    "https".
    */
   public void setServerProtocol(String protocol) throws PSLoaderException
   {
      if (protocol == null || protocol.trim().length() == 0)
         throw new IllegalArgumentException("protocol may not be null or empty");

      validateProtocol(protocol);

      PSProperty prop = getProperty(SERVER_PROTOCOL_PROP);
      prop.setValue(protocol);
   }

   /**
    * Validating a given protocol.
    *
    * @param protocol The to be validated protocol, it must be either
    *    "http" or "https". Assume it is not <code>null</code> or empty.
    *
    * @throws IllegalArgumentException if the <code>protocol</code> is not
    *    either "http" or "https".
    */
   private void validateProtocol(String protocol)
   {
      if ( (!protocol.equals("http")) && (!protocol.equals("https")) )
         throw new IllegalArgumentException("protocol must be http or https");
   }

   /**
    * Get the server protocol.
    *
    * @return the server protocol, which is either "http" or "https".
    *    Never <code>null</code> or empty.
    *
    * @throws PSLoaderException if the property of
    *    <code>SERVER_PROTOCOL_PROP</code> does not exist
    */
   public String getServerProtocol() throws PSLoaderException
   {
      PSProperty prop = getProperty(SERVER_PROTOCOL_PROP);
      return prop.getValue();
   }

   /**
    * Get the server name.
    *
    * @return the server name. Never <code>null</code> or empty.
    *
    * @throws PSLoaderException if the property of <code>SERVER_PROP</code>
    *    does not exist
    */
   public String getServerName() throws PSLoaderException
   {
      PSProperty prop = getProperty(SERVER_PROP);
      return prop.getValue();
   }

   /**
    * Set the <code>SERVER_PROP</code> property.
    *
    * @param p a string the server name. Never <code>null</code>
    *    or empty.
    *
    * @throws PSLoaderException if no such property exists
    */
   public void setServerName(String p) throws PSLoaderException
   {
      PSProperty prop = getProperty(SERVER_PROP);
      prop.setValue(p);
   }

   /**
    * Get the <code>PORT_PROP</code> property.
    *
    * @return a String of the port value. Never <code>null</code>
    *    or empty if it exists.
    *
    * @throws PSLoaderException if the property does not exist
    */
   public String getPort() throws PSLoaderException
   {
      PSProperty prop = getProperty(PORT_PROP);
      return prop.getValue();
   }

   /**
    * Set the <code>PORT_PROP</code> property.
    *
    * @param p a string the port value. Never <code>null</code>
    *    or empty.
    *
    * @throws PSLoaderException if no such property exists
    */
   public void setPort(String p) throws PSLoaderException
   {
      PSProperty prop = getProperty(PORT_PROP);
      prop.setValue(p);
   }

   /**
    * Get the <code>PORT_PROP</code> property.
    *
    * @return a int the port value.
    *
    * @throws PSLoaderException if the property does not exist
    * @throws NumberFormatException <code>prop.getValue</code>
    *    is not a valid integer.
    */
   public int getPortInt() throws PSLoaderException
   {
      PSProperty prop = getProperty(PORT_PROP);
      return Integer.parseInt(prop.getValue());
   }

   /**
    * Set the <code>PORT_PROP</code> property.
    *
    * @param p a int the port value.
    *
    * @throws PSLoaderException if no such property exists
    */
   public void setPortInt(int p) throws PSLoaderException
   {
      PSProperty prop = getProperty(PORT_PROP);
      prop.setValue(Integer.toString(p));
   }

   /**
    * Get the server root of the Rhythmyx Server. It may be defined in the
    * <code>SERVERROOT_PROP</code> property.
    * 
    * @return The value of the <code>SERVERROOT_PROP</code> property if
    *    exists; otherwise return <code>DEFAULT_SERVERROOT</code>.
    */
   public String getServerRoot()
   {
      PSProperty property = PSLoaderUtils.getOptionalProperty(SERVERROOT_PROP,
         getProperties());
         
      if (property == null)
         return DEFAULT_SERVERROOT;
      else
         return property.getValue();
   }
   
   /**
    * Get the value of a property.
    *
    * @return The name of the property, may not <code>null</code> or empty.
    *
    * @throws PSLoaderException if cannot find the property
    */
   protected PSProperty getProperty(String name) throws PSLoaderException
   {
      return PSLoaderUtils.getProperty(name, getProperties());
   }


   /**
    * Get the list of PSProperty objects.
    *
    * @return An iterator over one or more <code>PSPropertyDef</code>
    *    objects, never <code>null</code>, but may be empty.
    */
   public Iterator getProperties()
   {
      return m_properties.getComponents();
   }

   /**
    * Get the value of the <code>XML_ATT_ENC</code> attribute
    * of the <code>XML_NODE_PASSWORD</code> element.
    *
    * @return The value of the <code>XML_ATT_ENC</code> attribute,
    *    never <code>null</code> or empty.
    */
   public boolean isPasswordEncoded()
   {
      if (m_strEnc.equalsIgnoreCase(YES_STRING))
         return true;
      return false;
   }

   /**
    * Get the value of the <code>XML_NODE_USER</code> element.
    *
    * @return The value of the <code>XML_NODE_USER</code> element,
    *    never <code>null</code> may be empty.
    */
   public String getUser() throws PSLoaderException
   {
      return m_user;
   }

   /**
    * Set user name.
    *
    * @param user The to be set user name, may not be <code>null</code> or empty
    */
   public void setUser(String user) throws PSLoaderException
   {
      if (user == null || user.trim().length() == 0)
         throw new IllegalArgumentException("user may not be null or empty");

      m_user = user;
   }

   /**
    * Set password of the object.
    *
    * @param pwd The to be set password, may not be <code>null</code> or empty.
    */
   public void setPassword(String pwd) throws PSLoaderException
   {
      if (pwd == null || pwd.trim().length() == 0)
         throw new IllegalArgumentException("pwd may not be null or empty");

      m_pwd = pwd;
   }

   /**
    * Get the value of the <code>XML_NODE_PASSWORD</code> element.
    *
    * @return The value of the <code>XML_NODE_PASSWORD</code> element,
    *    never <code>null</code> may be empty.
    */
   public String getPassword() throws PSLoaderException
   {
      return m_pwd;
   }


   /**
    * Implements {@link PSLoaderComponent} class.
    */
   public void fromXml(Element sourceNode) throws PSUnknownNodeTypeException,
      PSLoaderException
   {
      validateElement(sourceNode, XML_NODE_NAME);

      PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);
      Element listEl = tree.getNextElement(
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);

      // Load the component list objects
      while (listEl != null)
      {
         if (listEl.getNodeName().equals(XML_NODE_PROPERTIES))
         {
            m_properties.fromXml(listEl);
         }
         else if (listEl.getNodeName().equals(XML_NODE_USER))
         {
            m_user = tree.getElementData(listEl);
         }
         else if (listEl.getNodeName().equals(XML_NODE_PASSWORD))
         {
            m_pwd = tree.getElementData(listEl);
            m_strEnc = getRequiredAttribute(listEl, XML_ATTR_ENC);
         }

         // Keep loading next element
         listEl = tree.getNextElement(
            PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
      }
   }

   /**
    * PSLoaderComponent#hashCode()
    */
   public int hashCode()
   {
      return m_strEnc.hashCode()+ m_properties.hashCode()+ m_user.hashCode()
         + m_pwd.hashCode();
   }

   /**
    * @see PSLoaderComponent#equals(Object)
    */
   public boolean equals(Object obj)
   {
      if (obj == null || !(obj instanceof PSConnectionDef))
         return false;
         
      PSConnectionDef obj2 = (PSConnectionDef) obj;
      return m_strEnc.equals(obj2.m_strEnc) &&
         m_properties.equals(obj2.m_properties) && 
         m_user.equals(obj2.m_user) &&
         m_pwd.equals(obj2.m_pwd);
   }

   /**
    * Implements {@link PSLoaderComponent} class.
    */
   public Element toXml(Document doc)
   {

      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");

      Element root = doc.createElement(XML_NODE_NAME);

      /**
       * Write out the component list objects below
       */

      if (!m_properties.isEmpty())
         root.appendChild(m_properties.toXml(doc));

      // Write out the user element
      PSXmlDocumentBuilder.addElement(doc,
         root,
         XML_NODE_USER,
         m_user);

      // Write out the password element
      Element passEl = PSXmlDocumentBuilder.addElement(doc,
         root,
         XML_NODE_PASSWORD,
         m_pwd);
      passEl.setAttribute(XML_ATTR_ENC, m_strEnc);
      return root;
   }


   /**
    * Field holding the user name. Initialized in {@link #init()}, never
    * modified or <code>null</code> that.
    */
   private String m_user;

   /**
    * Field holding the password for the user. Initialized in {@link #init()},
    * never modified or <code>null</code> that.
    */
   private String m_pwd;

   /**
    * The value of the <code>XML_ATTR_ENC</code>
    * attribute, initialized in constructor,
    * Never <code>null</code>, and never empty.
    * Either <code>YES_STRING</code> or <code>
    * NO_STRING</code>. Defaults to <code>YES_STRING</code>
    */
   protected String m_strEnc = YES_STRING;

   /**
    * The XML node name of this object.
    */
   public final static String XML_NODE_NAME = "Connection";

   /**
    * A list of actions, initialized in definition,
    * never <code>null</code>, may be empty.
    */
   protected PSComponentList m_properties = new PSComponentList(
      XML_NODE_PROPERTIES, PSProperty.XML_NODE_NAME, PSProperty.class);

   // Public constants for property names
   public final static String SERVER_PROTOCOL_PROP = "Rhythmyx Server Protocol";
   public final static String SERVER_PROP = "Rhythmyx Server";
   public final static String PORT_PROP = "Port";
   public final static String SERVERROOT_PROP = "ServerRoot";
   
   // Private constants for XML attribute and element name
   private final static String XML_NODE_USER = "User";
   private final static String XML_NODE_PASSWORD = "Password";
   private final static String XML_ATTR_ENC = "encrypted";
   
   private final static String DEFAULT_SERVERROOT = "Rhythmyx";
}