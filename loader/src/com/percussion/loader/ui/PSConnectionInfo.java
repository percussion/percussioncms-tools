/*[ PSConnectionInfo ]**********************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader.ui;

/**
 * Encapsulates the server connection info.
 */
public class PSConnectionInfo
{
   /**
    * Protected ctor to allow only inheriting classes or this class to create
    * singleton object.
    */
   protected PSConnectionInfo(){}

   /**
    * Sets the password for the connection.
    *
    * @param pwd, user's password for authentication, may be not be
    *    <code>null</code> or empty.
    */
   public void setPassword(String pwd)
   {
      if (pwd == null || pwd.trim().length() == 0)
         throw new IllegalArgumentException("parameter cannot be empty");
         
      m_password = pwd;
   }

   /**
    * Sets the user name for the connection.
    *
    * @param usr, user's name for authentication, may not be
    * <code>null</code> or empty.
    */
   public void setUser(String usr)
   {
      if (usr == null || usr.trim().length() == 0)
         throw new IllegalArgumentException("parameter cannot be empty");
         
      m_user = usr;
   }

   /**
    * Sets the server name for the connection.
    *
    * @param svr, server to which connection is made, may not be
    * <code>null</code> or empty.
    */
   public void setServer(String svr)
   {
      if (svr == null || svr.trim().length() == 0)
         throw new IllegalArgumentException("parameter cannot be empty");
         
      m_server = svr;
   }

   /**
    * Sets the protocol for the connection.
    *
    * @param prot, protocol for the connection, may be <code>null</code> or
    * empty in which case http is assumed.
    */
   public void setProtocol(String prot)
   {
      //default is http
      if (prot == null || prot.length() == 0)
         return;
      m_protocol = prot;
   }

   /**
    * Sets the port number on which the server is running.
    *
    * @param port, valid port number, may not be <code>null</code> or empty.
    */
   public void setPort(String port)
   {
      if (port == null || port.trim().length() == 0)
         throw new IllegalArgumentException("parameter cannot be empty");
         
      m_port = Integer.parseInt(port);
   }

   /**
    * Get the user's password.
    *
    * @return user password, never </code>null</code> or empty.
    */
   public String getPassword()
   {
      return m_password;
   }

   /**
    * Get the user name.
    *
    * @return user name, never </code>null</code> or empty.
    */
   public String getUser()
   {
      return m_user;
   }

   /**
    * Get the server name.
    *
    * @return server name, never </code>null</code> or empty.
    */
   public String getServer()
   {
      return m_server;
   }

   /**
    * Get the protocol.
    *
    * @return protocol, never </code>null</code> or empty.
    */
   public String getProtocol()
   {
      return m_protocol;
   }

   /**
    * Get the port.
    *
    * @return the port number.
    */
   public int getPort()
   {
      return m_port;
   }

   /**
    * Get connection information. Every time the connection information changes
    * through loader editor the singleton connection object is updated to
    * provide single point access to it for cataloging requests.
    *
    * @return connection info object, never <code>null</code>.
    */
   public static PSConnectionInfo getConnectionInfo()
   {
      if (ms_connection == null)
         ms_connection =  new PSConnectionInfo();
         
      return ms_connection;
   }

   /**
    * {@link com.percussion.loader.ui.PSConnectionInfo}, encapsulates connection
    * information. Singleton object initialized and accessed through
    * {@link #getConnection()}. Never <code>null</code>.
    */
   private static PSConnectionInfo ms_connection = null;

   /**
    * The protocol of the web service server, initialized by constructor,
    * it is either <code>PROTOCOL_HTTP</code> or <code>PROTOCOL_HTTPS</code>
    * and never modified after that.
    */
   private String m_protocol = PROTOCOL_HTTP;

   /**
    * The server name of the web services, initialized by constructor, never
    * <code>null</code> or empty after that.
    */
   private String m_server;

   /**
    * The port of the web services, initialized by constructor.
    */
   private int m_port;

   /**
    * The user name of the Rhythmyx server, initialized by constructor, never
    * <code>null</code> or empty after that.
    */
   private String m_user;

   /**
    * The password of the user, initialized by constructor, <code>null</code> 
    * or empty after that.
    */
   private String m_password;

   /**
    * Constant that represents the standard HTTP protocol.
    */
   private static final String PROTOCOL_HTTP = "http";
   
   /**
    * Constant that represents the secure HTTPS protocol.
    */
   private static final String PROTOCOL_HTTPS = "https";
}