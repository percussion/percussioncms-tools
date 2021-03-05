/******************************************************************************
 *
 * [ PSLoaderRemoteAgent.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.loader;

import com.percussion.cms.objectstore.client.PSRemoteAgent;
import com.percussion.loader.objectstore.PSConnectionDef;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSRemoteRequester;

import java.util.Properties;

/**
 * A special subclass of <code>PSRemoteAgent</code> that has a ctor that
 * specifically used by loader and its <code>PSConnectionDef</code> object
 * to obtain a <code>PSRemoteAgent</code>. The Equals method is also overriden
 * just for use in the loader.
 */
public class PSLoaderRemoteAgent extends PSRemoteAgent
{

   /**
    * Contructs an instance from a given connection definition
    *
    * @param conn The connection definition
    *
    * @throws PSLoaderException if the connection missing required
    *    connection parameters or properties.
    */
   public PSLoaderRemoteAgent(PSConnectionDef conn) throws PSLoaderException
   {
      super();
      if (conn == null)
         throw new IllegalArgumentException("conn may not be null");

      init(
         conn.getServerProtocol(),
         conn.getServerName(),
         conn.getPortInt(),
         conn.getUser(),
         conn.getPassword());

      m_community =
         PSLoaderUtils.getOptionalPropertyValue(
            IPSHtmlParameters.SYS_COMMUNITY,
            conn.getProperties());
      m_locale =
         PSLoaderUtils.getOptionalPropertyValue(
            IPSHtmlParameters.SYS_LANG,
            conn.getProperties());
   }

   /**
    * Validats the supplied parameters and initialize the object from them.
    *
    * @param protocol The protocol of the server (of the web service), it must
    *    be either "http" or "https".
    * @param server The server name of the web service, may not be
    *    <code>null</code> or empty.
    * @param port The port of the web service server.
    * @param user The user name for the Rhythmyx server, may not be
    *    <code>null</code> or empty.
    * @param password The password of the <code>user</code>.
    *
    * throws IllegalArgumentException if one of the parameters is invalid
    */
   private void init(
      String protocol,
      String server,
      int port,
      String user,
      String password)
   {
      if (protocol == null)
         throw new IllegalArgumentException("protocol may not be null");
      if ((!protocol.equals("http"))
         && (!protocol.equals("https")))
         throw new IllegalArgumentException("protocol must be http or https");
      if (server == null || server.trim().length() == 0)
         throw new IllegalArgumentException("server may not be null or empty");
      if (user == null || user.trim().length() == 0)
         throw new IllegalArgumentException("user may not be null or empty");
      if (password == null || password.trim().length() == 0)
         throw new IllegalArgumentException("password may not be null or empty");

      Properties props = new Properties();
      props.put("hostName", server);
      props.put("port", "" + port);
      props.put("loginId", user);
      props.put("loginPw", password);
      if (protocol.equals("https"))
         props.put("useSSL", "true");
      PSRemoteRequester req = new PSRemoteRequester(props);
      super.setRequester(req);

      m_protocol = protocol;
      m_server = server;
      m_port = port;
      m_user = user;
      m_password = password;
   }


   /**
    * Override the {@link Object#equals(Object)} method.
    *
    * @param other The to be compared object, it may be <code>null</code>.
    *
    * @return <code>true</code> if both contains the same connection
    *    information; <code>false</code> otherwise.
    */
   @Override
   public boolean equals(Object otherObj)
   {
      if (!(otherObj instanceof PSLoaderRemoteAgent))
         return false;

      PSLoaderRemoteAgent other = (PSLoaderRemoteAgent)otherObj;

      boolean communityEquals;
      if (m_community != null && other.m_community != null)
         communityEquals = m_community.equals(other.m_community);
      else
         communityEquals = m_community.equals(other.m_community);

      boolean localeEquals;
      if (m_locale != null && other.m_locale != null)
         localeEquals = m_locale.equals(other.m_locale);
      else
         localeEquals = m_locale.equals(other.m_locale);

      return communityEquals
         && localeEquals
         && m_protocol.equals(other.m_protocol)
         && m_server.equals(other.m_server)
         && m_port == other.m_port
         && m_user.equals(other.m_user)
         && m_password.equals(other.m_password);
   }
   
   /**
    * Generates hash code.
    */
   @Override
   public int hashCode()
   {
      return hashCodeOr0(m_community)
            + hashCodeOr0(m_locale)
            + hashCodeOr0(m_protocol)
            + hashCodeOr0(m_server)
            + m_port
            + hashCodeOr0(m_user)
            + hashCodeOr0(m_password);
   }
   
   /**
    * Hash code of the provided object or 0 if the object is <code>null</code>.
    */
   private int hashCodeOr0(Object object)
   {
      return object == null ? 0 : object.hashCode();
   }

   /**
    * The protocol of the server (of the web service). Initialized by the
    * constructor, it is either "http" or "https" after that.
    */
   private String m_protocol;

   /**
    * The server name of the web service. Initialized by the constructor,
    * never <code>null</code> after that.
    */
   private String m_server;

   /**
    * The port of the server. Initialized by the constructor.
    */
   private int m_port;

   /**
    * The user name for the server, Initialized by the constructor,
    * never <code>null</code> after that.
    */
   private String m_user;

   /**
    * The password of the user, Initialized by the constructor,
    * never <code>null</code> after that.
    */
   private String m_password;
}
