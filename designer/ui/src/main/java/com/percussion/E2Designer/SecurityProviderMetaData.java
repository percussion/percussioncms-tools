/*[ SecurityProviderMetaData.java ]*********************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import com.percussion.conn.PSDesignerConnection;
import com.percussion.conn.PSServerException;
import com.percussion.design.catalog.PSCatalogResultsWalker;
import com.percussion.design.catalog.PSCataloger;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.security.PSSecurityProvider;
import org.w3c.dom.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * A central location for all Security provider information. Provides cataloging
 * functionality and mapping between names and ids.
 * Security providers have a short name, a long name (suitable for display) and
 * a unique identifier. This could be extended to include more information in
 * the future.
 */
public class SecurityProviderMetaData
{
   /**
    * The display name associated with the Security provider type <code>
    * ID_FOR_ANY_PROVIDER</code>. This is GUI only name, it is not recognized
    * by the server.
    */
   public static final String NAME_FOR_ANY_PROVIDER = "<Any>";

   /**
    * The id associated with the Security provider name 
    * <code>NAME_FOR_ANY_PROVIDER</code>.
    */
   public static final int ID_FOR_ANY_PROVIDER = PSSecurityProvider.SP_TYPE_ANY;

   /**
    * Initializes the connection info for the cataloging. This method must be
    * called before any other methods are called. The cataloging is not
    * performed when during this call, so a catalog failure won't be evident
    * until a call is made that requires a catalog.
    * <p>The caller of this method should maintain a the returned reference so
    * that the class is not garbage collected.
    *
    * @param connSrc A valid source to obtain a connection, not 
    *    <code>null</code>.
    * @return the newly created instance which was initialized, 
    *    never <code>null</code>.
    */
   public static SecurityProviderMetaData initialize(IConnectionSource connSrc)
   {
      ms_instance = new SecurityProviderMetaData(connSrc);
      
      return ms_instance;
   }

   /**
    * Obtains the singleton instance of this object.
    *
    * @return A valid object.
    * @throws IllegalStateException If the class hasn't been initialized before
    *    this method is called the first time using <code>initialize</code>.
    */
   public static SecurityProviderMetaData getInstance()
   {
      if (null == ms_instance)
         throw new IllegalStateException("class not initialized yet");
         
      /* Do the init here rather than checking at the beginning of every
         method */
      if (null == ms_instance.m_providerInfo)
         ms_instance.init();
         
      return ms_instance;
   }

   /**
    * Returns a list of all security providers supported by the Rx server, by
    * their type id.
    *
    * @param excludeInternal <code>true</code> to exclude the internal security
    *    provider, <code>false</code> otherwise.
    * @return An array containing all of the supported security provider type
    *    ids. Never <code>null</code>.
    * @see #getSecurityProvidersByName()
    * @see #getSecurityProvidersByDisplayName()
    */
   public int[] getSecurityProvidersById(boolean excludeInternal)
   {
      List ids = new ArrayList();
      for (int i=0; i<m_providerInfo.length; ++i)
      {
         if (excludeInternal && 
            m_providerInfo[i].m_id == PSSecurityProvider.SP_TYPE_RXINTERNAL)
            continue;
            
         ids.add(new Integer(m_providerInfo[i].m_id));
      }
         
      int[] results = new int[ids.size()];
      for (int i=0; i<results.length; i++)
         results[i] = ((Integer) ids.get(i)).intValue();
      
      return results;
   }

   /**
    * A convenience method. Gets list of all providers, by their full name.
    * The order of the names matches the order of ids returned by 
    * <code>getSecurityProviderById</code>.
    *
    * @param excludeInternal <code>true</code> to exclude the internal security
    *    provider, <code>false</code> otherwise.
    * @return An array containing all of the supported security provider type
    *    names, in their long, display ready form. Never <code>null</code>.
    */
   public String[] getSecurityProvidersByDisplayName(boolean excludeInternal)
   {
      List longNames = new ArrayList();
      for (int i=0; i<m_providerInfo.length; ++i)
      {
         if (excludeInternal && 
            m_providerInfo[i].m_id == PSSecurityProvider.SP_TYPE_RXINTERNAL)
            continue;
            
         longNames.add(m_providerInfo[i].m_longName);
      }
         
      String[] results = new String[longNames.size()];
      longNames.toArray(results);
      
      return results;
   }

   /**
    * A convenience method. Gets list of all providers, by their short name.
    * The order of the names matches the order of ids returned by 
    * <code>getSecurityProviderById</code>.
    *
    * @param excludeInternal <code>true</code> to exclude the internal security
    *    provider, <code>false</code> otherwise.
    * @return An array containing all of the supported security provider type
    *    names, in their short form. Never <code>null</code>.
    */
   public String[] getSecurityProvidersByName(boolean excludeInternal)
   {
      List shortNames = new ArrayList();
      for (int i=0; i<m_providerInfo.length; ++i)
      {
         if (excludeInternal && 
            m_providerInfo[i].m_id == PSSecurityProvider.SP_TYPE_RXINTERNAL)
            continue;
               
         shortNames.add(m_providerInfo[i].m_shortName);
      }
         
      String[] results = new String[shortNames.size()];
      shortNames.toArray(results);
      
      return results;
   }

   /**
    * Maps a provider type id to a full display name for the provider
    * identified by that id. If ID_FOR_ANY_PROVIDER is passed in,
    * NAME_FOR_ANY_PROVIDER is returned.
    *
    * @param id One of the types of the form 
    *    <code>PSSecurityProvider.SP_TYPE_xxx</code> Should be one of the 
    *    values returned by <code>getSecurityProvidersById</code>.
    * @return A human readable name for the supplied id, or <code>null</code> 
    *    if the id doesn't match any registered provider.
    */
   public String getDisplayNameForId(int id)
   {
      String name = null;
      if (id == ID_FOR_ANY_PROVIDER)
         name = NAME_FOR_ANY_PROVIDER;
      else
      {
         for (int i=0; i<m_providerInfo.length && null == name; ++i)
            if (m_providerInfo[i].m_id == id)
               name = m_providerInfo[i].m_longName;
      }
      
      return name;
   }

   /**
    * Maps a provider type id to an internal name for the provider
    * identified by that id.
    *
    * @param id One of the types of the form 
    *    <code>PSSecurityProvider.SP_TYPE_xxx</code>. Should be one of the 
    *    values returned by <code>getSecurityProvidersById</code>.
    * @return A human readable name for the supplied id, or <code>null</code> 
    *    if the id doesn't match any registered provider.
    */
   public String getNameForId(int id)
   {
      String name = null;
      for (int i=0; i<m_providerInfo.length && null == name; ++i)
         if (m_providerInfo[i].m_id == id)
            name = m_providerInfo[i].m_shortName;

      return name;
   }

   /**
    * Maps a security provider display name to its identifier of the form
    * <code>PSSecurityProvider.SP_TYPE_xxx</code>. Any name returned by the
    * <code>getSecurityProvidersByDisplayName</code> method, or "ANY" may be 
    * passed in. If "ANY" is passed in, an id that matches all providers is 
    * returned.
    *
    * @param The long form of the security provider name, not
    *    <code>null</code>, may be empty.
    * @return The security provider's unique identifier that matches the
    *    supplied display name. If the name is not found, 0 is returned.
    */
   public int getIdForDisplayName(String name)
   {
      if (name == null)
         throw new IllegalArgumentException("name cannot be null");
         
      int id = 0;
      if (name.equalsIgnoreCase(NAME_FOR_ANY_PROVIDER))
         id = ID_FOR_ANY_PROVIDER;
      else
      {
         for (int i=0; i<m_providerInfo.length && id == 0; ++i)
            if ( m_providerInfo[i].m_longName.equalsIgnoreCase(name))
               id = m_providerInfo[i].m_id;
      }
      
      return id;
   }

   /**
    * A structure grouping info for a security provider.
    */
   private class SecurityProviderInfo
   {
      public int m_id;
      public String m_longName;
      public String m_shortName;
   }

   /**
    * This is a singleton class. It must first be initialized with a call to
    * <code>initialize</code>. Then the instance can be obtained with a call
    * to <code>getInstance</code>.
    * 
    * @param connSrc the connection used to ddo tha cataloging, not
    *    <code>null</code>.
    */
   private SecurityProviderMetaData(IConnectionSource connSrc)
   {
      if (connSrc == null)
         throw new IllegalArgumentException("cconnSrc cannot be null");
         
      m_connectionSrc = connSrc;
   }

   /**
    * Attempts to perform a catalog of the security providers on the Rx server.
    * If successful, <code>m_providerInfo</code> will be filled w/ info about
    * all of the cataloger providers. If it fails, <code>m_providerInfo</code>
    * will be an empty array.
    */
   private void init()
   {
      try
      {
         Properties properties = new Properties();
         properties.put("RequestCategory", "security");
         properties.put("RequestType", "Provider");

         PSDesignerConnection conn = 
            m_connectionSrc.getDesignerConnection(false);
         if (null == conn)
            conn = m_connectionSrc.getDesignerConnection(true);
         if (null == conn)
         {
            m_providerInfo = new SecurityProviderInfo[0];
            System.out.println("Security provider cataloging failed.");
         }

         PSCataloger cataloger   = new PSCataloger(conn);
         Document columns = cataloger.catalog(properties);
         PSCatalogResultsWalker provider = new PSCatalogResultsWalker(columns);
         ArrayList providerList = new ArrayList(5);
         while (provider.nextResultObject("Provider"))
         {
            SecurityProviderInfo info = new SecurityProviderInfo();
            info.m_shortName = provider.getElementData("name", false);
            String type = provider.getElementData("type", false);
            info.m_id = Integer.parseInt(type);
            info.m_longName = provider.getElementData("fullName", false);
            if (null == info.m_longName || info.m_longName.trim().length() == 0)
               info.m_longName = info.m_shortName;
               
            providerList.add(info);
         }
         
         m_providerInfo = new SecurityProviderInfo[providerList.size()];
         providerList.toArray(m_providerInfo);
      }
      catch (IllegalArgumentException e)
      {
         e.printStackTrace();
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
      catch (PSAuthorizationException e)
      {
         e.printStackTrace();
      }
      catch (PSAuthenticationFailedException e)
      {
         e.printStackTrace();
      }
      catch (PSServerException e)
      {
         e.printStackTrace();
      }
   }

   /**
    * Contains an array of structures that contain information about all of
    * the security provider types on the server. Initialized lazily, so is
    * <code>null</code> until the first time the instance is obtained.
    */
   private SecurityProviderInfo[] m_providerInfo = null;

   /**
    * The only instance of this class. <code>null</code> until 
    * <code>initialize</code> is called. Then valid for remainder of life of 
    * this class.
    */
   private static SecurityProviderMetaData ms_instance = null;

   /**
    * A connection to the Rx server is obtained from this guy. Non-null after
    * <code>initialize</code> is called.
    */
   private IConnectionSource m_connectionSrc = null;
}

