/******************************************************************************
 *
 * [ PSUserConnectionSet.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.connections;

import com.percussion.workbench.config.PSSectionConfig;
import com.percussion.workbench.config.PSUiConfigManager.SCOPE;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * List of user connections. The first in the list is always the most recent
 * one.
 * 
 * @version 6.0
 * @created 09-Sep-2005 2:49:09 PM
 */
public class PSUserConnectionSet extends PSSectionConfig
{
   /**
    * Ctor. Calls the base class ctor with the hard coded
    * {@link #PREFERENCE_KEY key} for the preference key.
    */
   public PSUserConnectionSet()
   {
      super(PREFERENCE_KEY, DEFAULT_SCOPE);
   }

   /**
    * Get the scope of the section config.
    * 
    * @return one of the enumerated constants for SCOPE. Never <code>null</code>
    */
   @Override
   public SCOPE getScope()
   {
      return null;
   }

   /**
    * Set the scope for the section configuration.
    * 
    * @param scope Scope to set. One of the enumerated constants of
    * {@link com.percussion.workbench.config.PSUiConfigManager.SCOPE}
    */
   @Override
   @SuppressWarnings("unused")
   public void setScope(SCOPE scope)
   {
   }

   /**
    * Adds/replaces a connection to the set.
    * 
    * @param userConnection cnnection to add to the set, must not be
    * <code>null</code>
    */
   public void addConnection(PSUserConnection userConnection)
   {
      if (userConnection == null)
      {
         throw new IllegalArgumentException("userConnection must not be null");
      }
      m_connectionSet.put(userConnection.getName(), userConnection);
   }

   /**
    * Remove the connection for the list.
    * 
    * @param userConnection Connection to remove from the list, must not be
    * <code>null</code>
    */
   public void remove(PSUserConnection userConnection)
   {
      m_connectionSet.remove(userConnection.getName());
   }

   /**
    * Get an iterator of all {@link PSUserConnection connections} in no
    * particular order.
    * 
    * @return iterator of all connections, never <code>null</code>, may be
    * empty.
    */
   public Iterator<PSUserConnection> getConnections()
   {
      return m_connectionSet.values().iterator();
   }

   /**
    * Get name of the default connection.
    * 
    * @return name of the default connection, may be <code>null</code>.
    */
   public String getDefault()
   {
      return m_default;
   }

   /**
    * Set the connection with this name as default connection.
    * 
    * @param conName name of the default connection, no validtaion for the
    * existence of the named connection is performed.
    */
   public void setDefault(String conName)
   {
      m_default = conName;
   }

   /**
    * Get name of the default connection.
    * 
    * @return default connection, can be <code>null</code> if there is none.
    */
   public PSUserConnection getDefaultConnection()
   {
      if (m_default != null && m_default.length() > 0)
         return m_connectionSet.get(m_default);
      return null;
   }
   /**
    * @return return the number of connections in the set, greater than or equal
    * to 0.
    */
   public int size()
   {
      return m_connectionSet.size();
   }

   /**
    * Get the connection with the given name ignoring the case.
    * 
    * @return the named connection, <code>null</code> if one does not exist
    * with that name.
    * @param name name of the conection to retrieve, must not be
    * <code>null</code> or empty.
    */
   public PSUserConnection getConnectionByName(String name)
   {
      if (name == null || name.length() == 0)
      {
         throw new IllegalArgumentException("name must not be null or empty");
      }
      return m_connectionSet.get(name);
   }

   /**
    * List of user connections, never <code>null</code>, may be empty.
    */
   private Map<String, PSUserConnection> m_connectionSet = new HashMap<String, PSUserConnection>();

   /**
    * name of the default connection
    */
   private String m_default;

   /**
    * Preference key for the connection list persistence. This is uniques across
    * the entire system.
    */
   public static final String PREFERENCE_KEY = "rx.preferences.userconnection";

   /**
    * Scope of persistence for the user connections. Default value is
    * {@link com.percussion.workbench.config.PSUiConfigManager.SCOPE#Default}
    * and settable using {@link #setScope(SCOPE)}.
    */
   public static final SCOPE DEFAULT_SCOPE = SCOPE.Default;
}
