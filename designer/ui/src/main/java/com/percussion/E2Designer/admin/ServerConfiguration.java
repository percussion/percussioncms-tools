/*[ ServerConfiguration.java ]*************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer.admin;

import com.percussion.conn.PSServerException;
import com.percussion.design.objectstore.PSAcl;
import com.percussion.design.objectstore.PSJavaPluginConfig;
import com.percussion.design.objectstore.PSLockedException;
import com.percussion.design.objectstore.PSLogger;
import com.percussion.design.objectstore.PSObjectStore;
import com.percussion.design.objectstore.PSServerConfiguration;
import com.percussion.error.PSIllegalArgumentException;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.util.PSCollection;

/**
 * ServerConfiguration wraps up all functionality to get / set the servers
 * configuration such as logging configuration.
 */
////////////////////////////////////////////////////////////////////////////////
public class ServerConfiguration
{
   /**
    * The Constuctor creates a new PSObjectStore and retrieves the servers
   * configuration. If it is currently locked, it will be opened read/only.
    *
    * @param   connection      a valid (loged in) server connection
    *
    * @param bEdit If <code>true</code>, the configuration file will tried to
    * be opened for editing, otherwise it will be opened read only.
    *
    * @param bOverrideLock If bEdit is true, and the config is currently locked
    * by the current user in a different session and this value is <code>true
    * </code>, the config will be opened for editing, releasing the lock owned
    * by this user in the other session.
    */
  //////////////////////////////////////////////////////////////////////////////
   public ServerConfiguration(ServerConnection connection, boolean bEdit,
         boolean bOverrideLock, PSObjectStore objectStore )
                      throws PSAuthorizationException,
                      PSAuthenticationFailedException,
                               PSIllegalArgumentException,
                               PSServerException,
                      PSLockedException
   {
       synchronized (this) {
           m_connection = connection;
           m_objectStore = objectStore;
           // locks server configuration

           if (bEdit)
               m_serverConfiguration = m_objectStore.getServerConfiguration(true, bOverrideLock);
           else {
               m_serverConfiguration = m_objectStore.getServerConfiguration();
               m_bReadOnly = true;
           }
       }
   }


//
// PUBLIC METHODS
//

   /**
    * This can be called after creating this object to find out if it was
    * created read/write or read/only.
    *
    * @return <true> if the config is opened read/only, <false> if it was opened
    * read/write
   **/
   public boolean isReadOnly()
   {
      return m_bReadOnly;
   }

   /**
    * Sets whether the server configuration is read only or not. If a server
    * configuration has been set using <code>setServerConfiguration()</code>
    * method, then this method can be used to set whether the configuration is
    * read only or not. This method only sets a <code>boolean</code> variable
    * which is returned by <code>isReadOnly()</code> method.
    *
    * @param readOnly the <code>boolean</code> to be returned by a call to
    * <code>isReadOnly()</code> method
    */
   public void setReadOnly(boolean readOnly)
   {
      m_bReadOnly = readOnly;
   }

  /**
   * @return The server configuration object.
   */
  public synchronized PSServerConfiguration getServerConfiguration()
  {
   return m_serverConfiguration;
  }

  /**
   * Returns a ServerConnection object for additional connections to an E2
   * server.
   *
  */
  public ServerConnection getServerConnection()
  {
   return m_connection;
  }

  /**
   * Return a PSCollection of PSSecurityProviderInstance objects, containing
   * all the existing security provider data.
   */
  public PSCollection getSecurityConfiguration()
  {
    return m_serverConfiguration.getSecurityProviderInstances();
  }

  /**
    * Set servers security provider configuration to the new settings passed in.
    *
    * @param   securityConfiguration      the applet
    */
  //////////////////////////////////////////////////////////////////////////////
  public void setSecurityConfiguration(PSCollection securityConfiguration)
  {
   try
    {
     m_serverConfiguration.setSecurityProviderInstances(securityConfiguration);
    }
    catch (PSIllegalArgumentException e)
    {
      e.printStackTrace();
    }
  }


  /**
   * Returns the JavaPluginConfig associated with this server configuration.
   */
   public PSJavaPluginConfig getJavaPluginConfig()
   {
      return m_serverConfiguration.getJavaPluginConfig();
   }

   /**
    * Sets server JavaPluginConfig to the new settings passed in.
    *
    * @param pluginConfig The config, never <code>null</code>.
    */
   public void setJavaPluginConfig(PSJavaPluginConfig pluginConfig)
   {
      if (pluginConfig==null)
         throw new IllegalArgumentException("pluginConfigad  may not be null");

      m_serverConfiguration.setJavaPluginConfig(pluginConfig);
   }

  /**
   * Return the server ACL associated with this server configuration.
   */
   public PSAcl getServerAcl()
   {
      return m_serverConfiguration.getAcl();
   }

   /**
    * Set server ACL to the new settings passed in.
    *
    * @param   acl The Access control list for the server
    */
   public void setServerAcl(PSAcl acl)
  {
    try
    {
     m_serverConfiguration.setAcl(acl);
    }
    catch (PSIllegalArgumentException e)
   {
      e.printStackTrace();
    }
  }


   /**
    * Return a PSLogger object, containing the servers logging configuration.
    *
    */
  //////////////////////////////////////////////////////////////////////////////
  public PSLogger getLogConfiguration()
  {
    return m_serverConfiguration.getLogger();
  }

   /**
    * Set servers logging configuration to the new settings passed in.
    *
    * @param   logConfiguration      the applet
    */
  //////////////////////////////////////////////////////////////////////////////
  public void setLogConfiguration(PSLogger logConfiguration)
  {
   m_serverConfiguration.setLogger(logConfiguration);
  }

  public boolean isUserSessionEnabled()
  {
    return m_serverConfiguration.isUserSessionEnabled();
  }

  public void setUserSessionEnabled(boolean b)
  {
    m_serverConfiguration.setUserSessionEnabled(b);
  }

   /**
    * Get the user session timeout.
    *
    * @return the user session timeout in seconds
    */
   public int getUserSessionTimeout()
   {
      return m_serverConfiguration.getUserSessionTimeout();
   }

   /**
    * Set the user session timeout.
    *
    * @param timeout the new user session timeout in seconds.
    */
   public void setUserSessionTimeout(int timeout)
   {
      m_serverConfiguration.setUserSessionTimeout(timeout);
   }
   
   /**
    * Get the user session warning.
    *
    * @return the user session warning in seconds
    */
   public int getUserSessionWarning()
   {
      return m_serverConfiguration.getUserSessionWarning();
   }
   
   /**
    * Set the user session timeout.
    *
    * @param timeout the new user session timeout in seconds.
    */
   public void setUserSessionWarning(int warning)
   {
      m_serverConfiguration.setUserSessionWarning(warning);
   }

   /**
    * Get the maximum allowed open user sessions.
    *
    * @return the maximum allowed number of open user sessions.
    */
   public int getMaxOpenUserSessions()
   {
      return m_serverConfiguration.getMaxOpenUserSessions();
   }

   /**
    * Set the maximum allowed open user sesions.
    *
    * @param max the maximum number of open user sessions allowed, must be
    *    greater than MINIMAL_REQUIRED_OPEN_SESSIONS. If an invalid number
    *    is provided, the default will be used.
    */
   public void setMaxOpenUserSessions(int max)
   {
      m_serverConfiguration.setMaxOpenUserSessions(max);
   }

   public int getRunningLogDays()
   {
      return m_serverConfiguration.getRunningLogDays();
   }

   public void setRunningLogDays(int days)
   {
      m_serverConfiguration.setRunningLogDays(days);
   }

  /**
   * @return boolean Gets the server setting for java exit sandbox option.
   * <CODE>true</CODE> means sandbox is in use.
   */
  public boolean getUseSandboxSecurity()
  {
    return m_serverConfiguration.getUseSandboxSecurity();
  }

  /**
   * @param b Sets the server setting for java exit sandbox option.
   * <CODE>true</CODE> means sandbox is in use.
   */
  public void setUseSandboxSecurity( boolean b )
  {
    m_serverConfiguration.setUseSandboxSecurity( b );
  }


  /**
   * See {@link PSServerConfiguration#allowDetailedAuthenticationMessages()
   * allowDetailedAuthenticationMessages} for details.
   */
  public boolean allowDetailedAuthenticationMessages()
  {
      return m_serverConfiguration.allowDetailedAuthenticationMessages();
  }

  /**
   * See {@link PSServerConfiguration
   * #setAllowDetailedAuthenticationMessages(boolean)
   * setAllowDetailedAuthenticationMessages} for details.
   */
  public void setAllowDetailedAuthenticationMessages( boolean allow )
  {
      m_serverConfiguration.setAllowDetailedAuthenticationMessages( allow );
  }

   /**
    * Sets the server configuration.
    *
    * @param config the server configuration, may not be <code>null</code>
    *
    * @throws IllegalArgumentException if config is <code>null</code>
    */
   public void setServerConfiguration( PSServerConfiguration config)
   {
      if(config == null)
         throw new IllegalArgumentException("config can not be null");

      m_serverConfiguration = config;
   }


  //////////////////////////////////////////////////////////////////////////////
   /**
    * the active server connection
    */
  private ServerConnection m_connection = null;
   /**
    * the object store
    */
  private PSObjectStore m_objectStore = null;
   /**
    * the server configuration settings
    */
  private PSServerConfiguration m_serverConfiguration = null;

   /**
    * If the configuration was opened read only, this flag will be <code>true</code>
   **/
   private boolean m_bReadOnly = false;
}
