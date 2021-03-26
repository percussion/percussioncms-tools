/******************************************************************************
 *
 * [ PSConnectionInfo.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client;

import com.percussion.i18n.PSI18nUtils;
import com.percussion.utils.security.deprecated.PSLegacyEncrypter;
import org.apache.commons.lang.StringUtils;


/**
 * A simple bean class for connection information. The password field has two
 * set and get methods. One pair is to set and get cleart text passwords. API
 * users use only these methods. The other pair is mainly used to set and get
 * encrypted versions of the password and meant for serializing in encrypted
 * form.
 * 
 * @author RammohanVangapalli
 * 
 */
public class PSConnectionInfo
{
   /**
    * Default ctor.
    */
   public PSConnectionInfo()
   {
   }

   /**
    * Ctor taking all the required information to create a user connection.
    * 
    * @param name Unique name for the connection, must not be <code>null</code>
    * or empty.
    * @param server Name or IP address of the server, must not be
    * <code>null</code> or empty.
    * @param port Server port number for the connection. -1 or 0 for a default
    * port of 80.
    * @param userid UserId for the connection. Must not be <code>null</code>
    * or empty.
    * @param password Password, may be <code>null</code> or empty assumes
    * clear text,i.e. not encrypted.
    * @param useSsl Flag to indicate to use SSL connection or not.
    * @param locale the locale for this connection, may be <code>null</code>
    * or empty, in which case locale will be set to
    * {@link com.percussion.i18n.PSI18nUtils#DEFAULT_LANG} Locale  will be 
    * specified by a string such as "en-gb" for english, GreatBritain
    */
   public PSConnectionInfo(String name, String server, int port, String userid,
      String password, boolean useSsl, String locale)
   {
      setName(name);
      setServer(server);
      setPort(port);
      setUserid(userid);
      setClearTextPassword(password);
      setUseSsl(useSsl);
      if ( StringUtils.isNotBlank(locale))
         setLocale(locale);
   }

   /**
    * Get name of the conection.
    * 
    * @return connection name, never <code>null</code> or empty.
    */
   public String getName()
   {
      return m_name;
   }

   /**
    * Set the name of connection.
    * 
    * @param name Name of the connection to set, must not be <code>null</code>
    * or empty.
    */
   public void setName(String name)
   {
      if (name == null || name.length() == 0)
      {
         throw new IllegalArgumentException("name must not be null or empty"); //$NON-NLS-1$
      }
      m_name = name;
   }

   /**
    * Get the encrypted password used for the connection. Meant to be used by
    * serializer.
    * 
    * @return encrypted password for the connection, may be empty but never
    * <code>null</code>.
    */
   public String getPassword()
   {
      String pwd;
      if (m_password == null)
         pwd = StringUtils.EMPTY;
      else
         pwd = m_password;
      
      return PSLegacyEncrypter.getInstance().encrypt(pwd, ENC_KEY);
   }

   /**
    * Get the clear text password used for the connection.
    * 
    * @return password for the connection, may be empty but never
    * <code>null</code>.
    */
   public String getClearTextPassword()
   {
      return m_password;
   }

   /**
    * Set decrypted password for the connection. Meant to be used by serializer.
    * If decryption fails will set it to empty.
    * 
    * @param pwd Password to set, assumes encrypted, may be <code>null</code>
    * or empty. If <code>null</code> it is set to empty string.
    */
   public void setPassword(String pwd)
   {
      pwd = (pwd == null) ? StringUtils.EMPTY : pwd;
      try
      {
         pwd = PSLegacyEncrypter.getInstance().decrypt(pwd, ENC_KEY);
      }
      catch (Exception e)
      {
         pwd = StringUtils.EMPTY;
      }
      m_password = pwd;
   }

   /**
    * Set clear text password for the connection.
    * 
    * @param pwd Password to set, assumes clear text, may be <code>null</code>
    * or empty. If <code>null</code> it is set to empty string.
    */
   public void setClearTextPassword(String pwd)
   {
      m_password = (pwd == null) ? StringUtils.EMPTY : pwd;
   }

   /**
    * Get the server name or IP address.
    * 
    * @return name or IP address of the server, never <code>null</code> or
    * emty.
    */
   public String getServer()
   {
      return m_server;
   }

   /**
    * Set the name of the server for the connection.
    * 
    * @param server Name or IP address of the server for the connection. Must
    * not be <code>null</code> or empty.
    */
   public void setServer(String server)
   {
      if (server == null || server.length() == 0)
      {
         throw new IllegalArgumentException("server must not be null or empty"); //$NON-NLS-1$
      }
      m_server = server;
   }

   /**
    * Get port for the connection.
    * 
    * @return the port of the connection.
    */
   public int getPort()
   {
      return m_port;
   }

   /**
    * Set the port for the connection.
    * 
    * @param port Port number to set.
    */
   public void setPort(int port)
   {
      m_port = port;
   }

   /**
    * The socket timeout to use when connecting, in seconds.
    * 
    * @return The current timeout. Never < 1.
    */
   public int getTimeout()
   {
      return m_timeout;
   }

   /**
    * See {@link #getTimeout()}.
    * 
    * @param seconds How long to wait before cancelling the connection. If a
    * value <= 0 is supplied, the default value of {@link #DEFAULT_TIMEOUT} is
    * used.
    */
   public void setTimeout(int seconds)
   {
      m_timeout = seconds <= 0 ? DEFAULT_TIMEOUT : seconds;
   }

   /**
    * Get the user id associated with connection
    * 
    * @return userid for the connection, never <code>null</code> empty.
    */
   public String getUserid()
   {
      return m_userid;
   }

   /**
    * Set userid for the connection.
    * 
    * @param userid User id for the connection, must not be <code>null</code>
    * or empty.
    */
   public void setUserid(String userid)
   {
      m_userid = userid == null ? StringUtils.EMPTY : userid;
   }

   /**
    * Is the connection using SSL?
    * 
    * @return <code>true</code> if using SSL <code>false</code> otherwise.
    */
   public boolean isUseSsl()
   {
      return m_useSsl;
   }

   /**
    * Helper method to get the protocol. It is computed based on
    * {@link #isUseSsl()} result;
    * 
    * @return "https" if uses SSL "http" otherwise.
    */
   public String getProtocol()
   {
      if (isUseSsl())
         return "https";
      return "http";
   }

   
   /**
    * Get locale of the conection.
    * 
    * @return locale, never <code>null</code> or empty.
    */
   public String getLocale()
   {
      return m_locale;
   }

   /**
    * Set the locale of connection.
    * 
    * @param locale locale of the connection never <code>null</code> or empty
    */
   public void setLocale(String locale)
   {
      if (locale == null || locale.length() == 0)
      {
         throw new IllegalArgumentException("locale must not be null or empty");
      }
      m_locale = locale;
   }
   /**
    * Set flag to use SSL.
    * 
    * @param useSsl <code>true</code> to use SSL to connect and
    * <code>false</code> otherwise.
    */
   public void setUseSsl(boolean useSsl)
   {
      m_useSsl = useSsl;
   }

//   //see base class method for details
//   @Override
//   public boolean equals(Object o)
//   {
//      if (o == null)
//         return false;
//      if (!(o instanceof PSConnectionInfo))
//         return false;
//      if (o == this)
//         return true;
//      PSConnectionInfo rhs = (PSConnectionInfo) o;
//      EqualsBuilder builder = new EqualsBuilder();
//      builder.append(m_name, rhs.m_name)
//         .append(m_password, rhs.m_password)
//         .append(m_port, rhs.m_port)
//         .append(m_server, rhs.m_server)
//         .append(m_userid, rhs.m_userid)
//         .append(m_useSsl, rhs.m_useSsl);
//      return builder.isEquals();
//   }
//
//   //see base class method for details
//   @Override
//   public int hashCode()
//   {
//      HashCodeBuilder builder = new HashCodeBuilder();
//      builder.append(m_name)
//         .append(m_password)
//         .append(m_port)
//         .append(m_server)
//         .append(m_userid)
//         .append(m_useSsl);
//      return builder.toHashCode();
//   }

   /**
    * Name of the connection, never <code>null</code> or empty.
    */
   private String m_name;

   /**
    * Name of the server, never <code>null</code> or empty.
    */
   private String m_server;

   /**
    * Connection port.
    */
   private int m_port;

   /**
    * @see #getUserid()
    * @see #setUserid(String)
    */
   private String m_userid = StringUtils.EMPTY;

   /**
    * @see #getPassword()
    * @see #setPassword(String)
    * @see #getClearTextPassword()
    * @see #setClearTextPassword(String)
    */
   private String m_password = StringUtils.EMPTY;

   /**
    * Is connection using SSL?
    */
   private boolean m_useSsl;

   /**
    * Defaults to {@link #DEFAULT_TIMEOUT}.
    * 
    * @see #getTimeout()
    */
   private int m_timeout = DEFAULT_TIMEOUT;
   
   /**
    * the locale that is used by this connection, by default
    * {@link com.percussion.i18n.PSI18nUtils#DEFAULT_LANG}
    */
   private String m_locale = PSI18nUtils.DEFAULT_LANG;

   /**
    * Key to encrypt and decrypt the password. Do not change unless it is
    * required. It will invalidate the persisted passwords.
    */
   @Deprecated
   private static final String ENC_KEY = "MaSaLa-MiTsUbIsHi-RaDiO-louisiana";

   /**
    * @see #getTimeout()
    */
   public static final int DEFAULT_TIMEOUT = 60;
}
