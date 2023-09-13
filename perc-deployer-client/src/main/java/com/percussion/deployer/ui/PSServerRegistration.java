/******************************************************************************
 *
 * [ PSServerRegistration.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.deployer.ui;

import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * The class to hold server registration information.
 */
public class PSServerRegistration 
{   
   /**
   * Constructs this object with supplied parameters.
   * 
   * @param server the name of registered server, may not be <code>null</code> 
   * or empty.
   * @param port The port on which server is listening, must be > 0.
   * @param userName The user id to connect to server, may not be <code>null
   * </code> or empty.
   * @param password The encrypted password to connect to, may be <code>null
   * </code> or empty.
   * @param isSaveCredentials if <code>true</code> the credentials are saved and
   * does not prompt for credentials with further connections.
   * @param useSSL <code>true</code> to use 'https' protocol to connect to the
   * server, <code>false</code> to use default 'http' protocol.
   * 
   * @throws IllegalArgumentException if any param is invalid.
   */
   public PSServerRegistration(String server, int port, String userName, 
      String password, boolean isSaveCredentials, boolean useSSL)
   {
      this(server, port, useSSL);
      
      setCredentials(userName, password);
      setSaveCredential(isSaveCredentials);
   }
   
   /**
    * Constructs this object with supplied parameters. The credentials are not 
    * set with this.
    * 
    * @param server the name of registered server, may not be <code>null</code> 
    * or empty.
    * @param port The port on which server is listening, must be > 0
    * @param useSSL <code>true</code> to use 'https' protocol to connect to the
    * server, <code>false</code> to use default 'http' protocol.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    */
   public PSServerRegistration(String server, int port, boolean useSSL)
   {
      if(server == null || server.trim().length() == 0)
         throw new IllegalArgumentException("server may not be null or empty.");         
         
      m_server = server;
      m_useSSL = useSSL;

      setPort(port);
   }
   
   /**
    * Constructs the object from the xml element. If the element has 'username' 
    * attribute, then it sets the save credentials flag to <code>true</code> to
    * represent that user previously saved the credentials. See {@link 
    * #toXml(Document)} for the format of the xml.
    * 
    * @param source the source element, may not be <code>null</code>
    * 
    * @throws IllegalArgumentException if source is <code>null</code>
    * @throws PSUnknownNodeTypeException if the source element does not 
    * represent the xml element node of this object or if the required 
    * attributes are missing on this element.
    */
   public PSServerRegistration(Element source)
      throws PSUnknownNodeTypeException   
   {   
      fromXml(source);
   }
   
   /**
    * Populates the members of this object from xml representation. Please refer
    * to {@link #toXml(Document) } for the format of the xml.
    * 
    * @param source the source element, may not be <code>null</code>
    * 
    * @throws IllegalArgumentException if source is <code>null</code>
    * @throws PSUnknownNodeTypeException if the source element does not 
    * represent the xml element node of this object or if the required 
    * attributes are missing on this element.
    */
   private void fromXml(Element source)
      throws PSUnknownNodeTypeException   
   {
      if(source == null)
      {
         throw new IllegalArgumentException("source may not be null.");
      }

      if(!source.getTagName().equals(XML_NODE_NAME))
      {
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, XML_NODE_NAME);
      }
      
      String name = source.getAttribute(XML_NAME_ATTR);
      if (name == null || name.trim().length() == 0)
      {
         Object[] args = {XML_NODE_NAME, XML_NAME_ATTR, "null"};
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
      }
      m_server = name;
      
      String port = source.getAttribute(XML_PORT_ATTR);
      if (port == null || port.trim().length() == 0)
      {
         Object[] args = {XML_NODE_NAME, XML_PORT_ATTR, "null"};
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
      }
      else
      {
         try 
         {
            m_port = Integer.parseInt(port);
         }
         catch(NumberFormatException e)
         {
            Object[] args = {XML_NODE_NAME, XML_PORT_ATTR, port};
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
         }
      }
      
      String userName = source.getAttribute(XML_USERNAME_ATTR);
      if(userName != null && userName.trim().length() > 0)
      {
         m_userName = userName;
         m_isSaveCredentials = true;         
         m_password = source.getAttribute(XML_PASSWORD_ATTR);
      }      
      
      // ssl disabled by default if not specified
      m_useSSL = XML_ATTR_TRUE.equals(source.getAttribute(XML_USE_SSL_ATTR));
   }
   
   /**
    * Gets the xml representation of this object. Sets the credential attributes
    * (username and password) on the element only if the save credentials flag
    * is <code>true</code>. Please see {@link #isSaveCredentials()} to know more
    * about this flag. The format of the xml is as follows.
    * <pre><code>
    *    %lt;!ELEMENT PSXServerRegistration(EMPTY) >
    *    %lt;!ATTLIST PSXServerRegistration
    *       name CDATA #REQUIRED
    *       port CDATA #REQUIRED
    *       username CDATA #IMPLIED
    *       password CDATA #IMPLIED
    *       useSSL (yes | no) "no">
    * </code></pre>
    * 
    * @param doc the document used to create the element, may not be <code>null
    * </code>
    * 
    * @return the newly created element, never <code>null</code>
    */
   public Element toXml(Document doc)
   {
      Element el = doc.createElement(XML_NODE_NAME);
      el.setAttribute(XML_NAME_ATTR, m_server);
      el.setAttribute(XML_PORT_ATTR, String.valueOf(m_port));
      if(isSaveCredentials())
      {
         el.setAttribute(XML_USERNAME_ATTR, m_userName);
         el.setAttribute(XML_PASSWORD_ATTR, m_password);         
      }
      el.setAttribute(XML_USE_SSL_ATTR, m_useSSL ? XML_ATTR_TRUE : 
         XML_ATTR_FALSE);
      
      return el;
   }
   
   /**
    * Sets the credentials that need to be used to connect to the server.
    * 
    * @param userName The username to connect to server, may not be <code>null
    * </code> or empty.
    * @param password The encrypted password to connect to, may be <code>null
    * </code> or empty.
    * 
    * @throws IllegalArgumentException if username is <code>null</code> or 
    * empty.
    */
   public void setCredentials(String userName, String password)
   {
      if(userName == null || userName.trim().length() == 0)
         throw new IllegalArgumentException(
            "userName may not be null or empty.");      
      m_userName = userName;
      m_password = password;
   }

   /**
    * Sets the port on which server is listening.
    * 
    * @param port the port, must be > 0
    * 
    * @throws IllegalArgumentException if port is invalid.
    */
   public void setPort(int port)
   {
      if(port <= 0)
         throw new IllegalArgumentException("port must be > 0.");
      m_port = port;      
   }

   /**
    * Sets the save credentials flag.
    * 
    * @param save if <code>true</code> the credentials are saved with the server 
    * registration details and user will not be prompted for credentials while 
    * connecting, otherwise not.
    * 
    * @throws IllegalStateException if the credentials are not set on this 
    * object and supplied flag is <code>true</code>
    */
   public void setSaveCredential(boolean save)
   {
      if(save && getUserName() == null)
         throw new IllegalStateException("Credentials are not set to save");
         
      m_isSaveCredentials = save;
   }

   /**
    * Gets the save credentials flag. Useful to determine whether to prompt for 
    * credentials when user is connecting to a server.
    * 
    * @return <code>true</code> if the credentials are saved, otherwise <code>
    * false</code>
    */
   public boolean isSaveCredentials()
   {
      return m_isSaveCredentials;
   }
   
   /**
    * Gets the SSL setting for this registration.
    * 
    * @return <code>true</code> if the connection to the server should use the
    * "https" protocol, <code>false</code> to use "http".
    */
   public boolean isUseSSL()
   {
      return m_useSSL;
   }
   
   /**
    * Sets the SSL setting for this registration.
    * 
    * @param useSSL <code>true</code> if the connection to the server should 
    * use the "https" protocol, <code>false</code> to use "http".
    */
   public void setUseSSL(boolean useSSL)
   {
      m_useSSL = useSSL;
   }
   
   /**
    * Gets the server name.
    * 
    * @return the server name, never <code>null</code> or empty.
    */
   public String getServer()
   {
      return m_server;
   }

   /**
    * Gets the port on which server is listening to.
    * 
    * @return the port number
    */
   public int getPort()
   {
      return m_port;
   }

   /**
    * Gets the user name saved with registration.
    * 
    * @return the user name, may be <code>null</code> if the credentials are not
    * yet set, never empty.
    */
   public String getUserName()
   {
      return m_userName;
   }

   /**
    * Gets the password.
    * 
    * @return the encrypted password, may be <code>null</code> or empty.
    */
   public String getPassword()
   {
      return m_password;
   }
   
   /**
    * The name of the server, Initialized in the constructor and never <code>
    * null</code>, empty or modified after that.
    */
   private String m_server;
   
   /**
    * The port on which server is listening to. Initialized in the constructor 
    * and modified in <code>setPort(int)</code> method.
    */
   private int m_port;
   
   /**
    * The username to connect to the server. Initialized in the constructor and 
    * modifed in <code>setCredentials</code>. May be <code>null</code> if this 
    * is not set. Never empty if it is not <code>null</code>.
    */
   private String m_userName;

   /**
    * The encrypted password to connect to the server. Initialized in the 
    * constructor and modifed in <code>setCredentials</code>. May be <code>null
    * </code> if this is not set. May be empty.
    */
   private String m_password;
   
   /**
    * Flag to define whether to save the login credentials or not. 
    * Initialized to <code>false</code> and set to <code>true</code> when user 
    * chooses to save the credentials.
    */
   private boolean m_isSaveCredentials = false;
   
   /**
    * <code>true</code> to use 'https' protocol to connect to the
    * server, <code>false</code> to use default 'http' protocol.  Initialized
    * to <code>false</code>, modified during construction.
    */
   private boolean m_useSSL = false;
   
   /**
    * The constants that defines xml element and attribute names.
    */
   public static final String XML_NODE_NAME = "PSXServerRegistration";    
   private static final String XML_NAME_ATTR = "name";    
   private static final String XML_PORT_ATTR = "port";    
   private static final String XML_USERNAME_ATTR = "username";    
   private static final String XML_PASSWORD_ATTR = "password";                
   private static final String XML_USE_SSL_ATTR = "useSSL";
   private static final String XML_ATTR_TRUE = "yes";
   private static final String XML_ATTR_FALSE = "no";
}
