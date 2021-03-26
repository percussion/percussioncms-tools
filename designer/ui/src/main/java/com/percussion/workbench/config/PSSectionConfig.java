/******************************************************************************
 *
 * [ PSSectionConfig.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.config;

import com.percussion.workbench.config.PSUiConfigManager.SCOPE;

/**
 * This abstract class enables a section configuration to be saved to the
 * system. The classes extending this must follow standard bean conventions
 * since section configuration is meant to be persisted transparently. Following
 * bean conventions allows serializing and deserializing error free. Objects of
 * this class are manageable by
 * {@link com.percussion.workbench.config.PSUiConfigManager config manager}
 * class.
 * 
 * @version 6.0
 * @created 09-Sep-2005 2:49:09 PM
 */
public abstract class PSSectionConfig
{
   /**
    * Default ctor. Does nothing. Keeps the serializer happy. Subclass must
    * override this for the same reason.
    */
   protected PSSectionConfig()
   {

   }

   /**
    * Ctor that takes the unique configuration key.
    * 
    * @param key key for the section configuration, must not be
    * <code>null</code> or empty.
    */
   public PSSectionConfig(String key)
   {
      setKey(key);
   }

   /**
    * Ctor that takes the unique configuration key.
    * 
    * @param key key for the section configuration, must not be
    * <code>null</code> or empty.
    * @param scope Scope to set. One of the enumerated constants of
    * {@link PSUiConfigManager.SCOPE}
    */
   public PSSectionConfig(String key, SCOPE scope)
   {
      setKey(key);
      setScope(scope);
   }

   /**
    * Get the key for the section configuration. The implementation must make
    * sure that this ke is unique across all config sections.
    * 
    * @return section key, never <code>null</code> or empty.
    */
   public String getKey()
   {
      return m_key;
   }

   /**
    * Set method for the unique configuration key.
    * 
    * @param key key for the section configuration, must not be
    * <code>null</code> or empty.
    */
   public void setKey(String key)
   {
      if (key == null || key.length() == 0)
      {
         throw new IllegalArgumentException("key must not be null or empty"); //$NON-NLS-1$
      }
      m_key = key; 
   }

   /**
    * Get the scope of the section config.
    * 
    * @return one of the enumerated constants for SCOPE. Never <code>null</code>
    */
   public SCOPE getScope()
   {
      return m_scope;
   }

   /**
    * Set the scope for the section configuration.
    * 
    * @param scope Scope to set. One of the enumerated constants of
    * {@link PSUiConfigManager.SCOPE}
    */
   public void setScope(SCOPE scope)
   {
      m_scope = scope;
   }

   /**
    * Key to identify the object, initialized in the ctor and never
    * <code>null</code> after that.
    */
   private String m_key = null;

   /**
    * scope of the section config to be used for persistence. Default is
    * 'Default'.
    */
   private SCOPE m_scope = PSUiConfigManager.SCOPE.Default;
}
