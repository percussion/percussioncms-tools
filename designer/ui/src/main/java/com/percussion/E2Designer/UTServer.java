/*[ UTServer.java ]************************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import com.percussion.conn.PSDesignerConnection;
import com.percussion.design.objectstore.PSObjectStore;

import java.util.Properties;

/**
 * Helper class to test functionality which needs the server.
 */
////////////////////////////////////////////////////////////////////////////////
public class UTServer
{
   /**
   * Construct the object: connect to the server and create an object store and
   * a server configuration.
    */
  //////////////////////////////////////////////////////////////////////////////
   public UTServer()
   {
      Properties propsLogin = new Properties();
      propsLogin.setProperty(PSDesignerConnection.PROPERTY_HOST, "E2");
      propsLogin.setProperty(PSDesignerConnection.PROPERTY_LOGIN_ID, "Paul");
      propsLogin.setProperty(PSDesignerConnection.PROPERTY_LOGIN_PW, "");
      propsLogin.setProperty(PSDesignerConnection.PROPERTY_PORT, "9992");
      try
      {
         m_conn = new PSDesignerConnection(propsLogin);
         m_os = new PSObjectStore(m_conn);
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   /**
   * Get server connection.
    */
  //////////////////////////////////////////////////////////////////////////////
   public PSDesignerConnection getConnection() { return m_conn; }

   /**
   * Get object store.
    */
  //////////////////////////////////////////////////////////////////////////////
   public PSObjectStore getStore() { return m_os; }

  //////////////////////////////////////////////////////////////////////////////
  private PSDesignerConnection m_conn = null;
  private PSObjectStore m_os = null;
}
