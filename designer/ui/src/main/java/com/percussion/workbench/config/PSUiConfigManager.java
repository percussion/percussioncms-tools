/******************************************************************************
 *
 * [ PSUiConfigManager.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.config;

import com.percussion.client.PSConnectionInfo;
import com.percussion.i18n.PSI18nUtils;
import com.percussion.workbench.connections.PSUserConnection;
import com.percussion.workbench.connections.PSUserConnectionSet;
import com.percussion.workbench.ui.PSWorkbenchPlugin;
import com.percussion.workbench.ui.util.PSResourceLoader;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.serialization.PSObjectSerializer;
import com.percussion.xml.serialization.PSObjectSerializerException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;

/**
 * Manages UI specific settings. Can load, delete and persist sections' settings
 * for various scopes. TODO do we need the scope here at all???
 * 
 * @version 6.0
 * @created 09-Sep-2005 2:49:09 PM
 */
public class PSUiConfigManager
{
   /**
    * Private ctor to prevent instantiation outside of this class.
    */
   private PSUiConfigManager()
   {
      AbstractUIPlugin plugin = PSWorkbenchPlugin.getDefault();
      if (plugin == null)
      {
         ms_logger.warn("Lookslike the workbench is not running and "
            + "hence it will create a private preference store file.");
         ms_ps = new PreferenceStore("workbench.preferences"); //$NON-NLS-1$
      }
      else
         ms_ps = plugin.getPreferenceStore();
   }

   /**
    * Returns the singleton instance of this class.
    * 
    * @return only instance of this class, never <code>null</code>.
    */
   static public PSUiConfigManager getInstance()
   {
      if (ms_this == null)
         ms_this = new PSUiConfigManager();
      return ms_this;
   }

   /**
    * Get the list of all persisted connections. Shortcut for {@link
    * #getSectionConfig(String) } with section key paramof {@link
    * PSUserConnectionSet#PREFERENCE_KEY} and result cast to {@link
    * PSUserConnectionSet}.
    * 
    * @return user connection list, never <code>null</code> may be empty.
    */
   public PSUserConnectionSet getUserConnections()
   {
      String cfgStr = ms_ps.getString(PSUserConnectionSet.PREFERENCE_KEY);
      PSUserConnectionSet set = null;
      if (cfgStr == null || cfgStr.length() == 0)
      {
         set = new PSUserConnectionSet();
         int port = -1;
         String server = null;
         String user = null;
         try
         {
            port = PSResourceLoader.getLastPortOpened();
            server = PSResourceLoader.getLastServerOpened();
            user = PSResourceLoader.getLastUserName();
         }
         catch (Exception e)
         {
            ms_logger.warn(e);
         }
         
         if (port == -1 || server == null || user == null)
         {
            ms_logger.warn("Error loading previous connection properties.  "
                  + "Using defaults.");
            
            port = 9992;
            server = "localhost";
            user = "admin1";
         }
         
         PSUserConnection conn = new PSUserConnection("Connection", //$NON-NLS-1$
            server, port, user, "demo", //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
            false, PSI18nUtils.DEFAULT_LANG);
         set.addConnection(conn);
         set.setDefault(conn.getName());
         PSUiConfigManager.getInstance().saveSectionConfig(set);
         return set;
      }
      try
      {
         Document doc = PSXmlDocumentBuilder.createXmlDocument(
            new StringReader(cfgStr), false);
         Object obj = PSObjectSerializer.getInstance().fromXml(
            doc.getDocumentElement());
         // If it is not a right instance - fatal
         if (!(obj instanceof PSUserConnectionSet))
         {
            throw new RuntimeException(
               "Deserialized connections object expected type "
                  + "is PSUserConnectionSet but found:"
                  + obj.getClass().getName());
         }
         set = (PSUserConnectionSet) obj;
      }
      catch (Exception e)
      {
         ms_logger.warn(e);
      }

      if (set == null)
      {
         ms_logger.warn("Could not deserialize the connections from the "
            + "preference store. Creating sample ones.");
         ms_ps.setValue(PSUserConnectionSet.PREFERENCE_KEY, "");
         return getUserConnections();
      }
      return set;
   }

   /**
    * Save a user connection. 
    * 
    * @param conn the user connection information that has failed in the prior
    * attempt due to invalid locale information. Never <code>null</code>
    */
   public void saveUserConnection(PSConnectionInfo conn)
   {
      if (conn == null)
      {
         throw new IllegalArgumentException("connection may not be null");
      }
      String cfgStr = ms_ps.getString(PSUserConnectionSet.PREFERENCE_KEY);
      PSUserConnectionSet set = null;
      // this must not happen, but guard against modifying a non-existant config
      if (cfgStr == null || cfgStr.length() == 0)
      {
         ms_logger.warn("Attempting to modify a user connection locale, user" +
                " connection does not exist in preferences");
         return;
      }
      // valid list of locales were found, walk thru and update the locale info
      // for that particular connection
      try
      {
         Document doc = PSXmlDocumentBuilder.createXmlDocument(
            new StringReader(cfgStr), false);
         Object obj = PSObjectSerializer.getInstance().fromXml(
            doc.getDocumentElement());
         // If it is not a right instance - fatal
         if (!(obj instanceof PSUserConnectionSet))
         {
            throw new RuntimeException(
               "Deserialized connections object expected type "
                  + "is PSUserConnectionSet but found:"
                  + obj.getClass().getName());
         }
         set = (PSUserConnectionSet) obj;
         Iterator<PSUserConnection> cIt = set.getConnections();
         boolean isModified = false;
         while(cIt.hasNext())
         {
            PSUserConnection c = cIt.next();
            if (c.getName().equals(conn.getName()))
            {
               c.setLocale(conn.getLocale());
               isModified = true;
               break;
            }
         }
         if (isModified == true)
            PSUiConfigManager.getInstance().saveSectionConfig(set);
      }
      catch (Exception e)
      {
         ms_logger.warn(e);
      }
   }

   /**
    * Get the configuration for the given section name. Uses eclipse preference
    * framework to get the persisted XML string for th given section key and
    * deserialzes to the object.
    * 
    * @return configuration for the section, may be <code>null</code> if one
    * never persisted.
    * @param sectionKey key of the configuration section, must not be
    * <code>null</code> or empty.
    */
   public PSSectionConfig getSectionConfig(String sectionKey)
   {
      if (sectionKey == null || sectionKey.length() == 0)
      {
         throw new IllegalArgumentException(
            "sectionKey must not be null or empty"); //$NON-NLS-1$
      }
      String cfgStr = ms_ps.getString(sectionKey);
      if (cfgStr == null || cfgStr.length() == 0)
         return null;
      PSSectionConfig cfg = null;
      try
      {
         cfg = (PSSectionConfig) PSObjectSerializer.getInstance()
            .fromXmlString(cfgStr);
      }
      catch (PSObjectSerializerException e)
      {
         throw new RuntimeException(e);
      }
      return cfg;
   }

   /**
    * Save the configuration for the section. This use eclipse preference
    * persistence mechanism. The section is converted an XML string and saved
    * against the key returned by the section's {PSSectionConfig#getKey()} for
    * scope returned by {PSSectionConfig#getScope()}. It assumes the section
    * config object follows standard bean specs so that the deserialized object
    * is indeed the object that was seralized.
    * <p>
    * Changes are immediately persisted to the permanent store.
    * 
    * @param config Section configuration , must not be <code>null</code> or
    * empty.
    */
   public void saveSectionConfig(PSSectionConfig config)
   {
      if (config == null)
      {
         throw new IllegalArgumentException("config must not be null"); //$NON-NLS-1$
      }
      String cfgStr = null;
      try
      {
         cfgStr = PSObjectSerializer.getInstance().toXmlString(config);
      }
      catch (IOException e)
      {
         // XXX Auto-generated catch block
         e.printStackTrace();
      }
      catch (SAXException e)
      {
         // XXX Auto-generated catch block
         e.printStackTrace();
      }
      catch (IntrospectionException e)
      {
         // XXX Auto-generated catch block
         e.printStackTrace();
      }
      if (cfgStr != null && cfgStr.length() > 0)
      {
         ms_ps.setValue(config.getKey(), cfgStr);
      }
      persist();
   }

   /**
    * Remove the specified section configuration from persisted storage if it
    * was persisted ever.
    * <p>
    * Changes are immediately persisted to the permanent store.
    * 
    * @param config Configuration object , must not be <code>null</code>.
    */
   public void removeSectionConfig(PSSectionConfig config)
   {
      if (config == null)
      {
         throw new IllegalArgumentException("config must not be null"); //$NON-NLS-1$
      }
      ms_ps.setValue(config.getKey(), StringUtils.EMPTY);
      persist();
   }

   /**
    * Calls the plugin to save the preference store.
    */
   private void persist()
   {
      PSWorkbenchPlugin.getDefault().savePluginPreferences();
   }
   
   /**
    * Enumerated constants for prefernce scopes. The supported scopes are:
    * <ol>
    * <li>Configuration</li>
    * <li>Instance</li>
    * <li>Default</li>
    * <li>Project</li>
    * </ol>
    * Read eclipse documentation for more details.
    */
   /**
    * Enumerated constants for the eclipse rpeference scopes.
    */
   static public enum SCOPE
   {
      /**
       * Configuration
       */
      Configuration,
      /**
       * Instance Scope
       */
      Instance,
      /**
       * Default scope
       */
      Default,
      /**
       * Project scope
       */
      Project
   }

   /**
    * Reference to only instance of this class.
    */
   private static PSUiConfigManager ms_this = null;

   /**
    * Reference to the ecplise preference store. Initialized in the ctor and
    * never <code>null</code> after that.
    */
   private static IPreferenceStore ms_ps = null;

   /**
    * Logger to log error or warnings.
    */
   private static Logger ms_logger = Logger.getLogger(PSUiConfigManager.class);
}
