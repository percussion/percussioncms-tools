/******************************************************************************
 *
 * [ PSUserConnection.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.connections;

import com.percussion.client.PSConnectionInfo;

/**
 * Implies one connection configured by the user.
 */
public class PSUserConnection extends PSConnectionInfo
{
   /**
    * Default ctor. Invokes the base class version.
    */
   public PSUserConnection()
   {
      super();
   }

   /**
    * Ctor taking all the required information to create a user connection.
    * Simply invokes the base class ctor.
    * 
    * @see PSConnectionInfo#PSConnectionInfo(String, String, int, String,
    * String, boolean, String)
    */
   public PSUserConnection(String name, String server, int port, String userid,
      String password, boolean useSsl, String locale)
   {
      super(name, server, port, userid, password, useSsl, locale);
   }

   /**
    * Is passord for the connection to be saved ?
    * 
    * @return <code>true</code>if password needs to be persisted
    * <code>false</code> otherwise.
    */
   public boolean isSavePassword()
   {
      return m_savePwd;
   }

   /**
    * Whether to save password for the connection?
    * 
    * @param savePwd <code>true</code> to save the password and
    * <code>false</code> otherwise.
    */
   public void setSavePassword(boolean savePwd)
   {
      m_savePwd = savePwd;
   }

   /**
    * String representation of the object which is name of the conection. Useful
    * in rendering in UI components.
    * 
    * @return name of the connection.
    * 
    * @see #getName()
    */
   @Override
   public String toString()
   {
      return getName();
   }

   /**
    * Whether to save password as part of connection settings.
    */
   private boolean m_savePwd = false;
}
