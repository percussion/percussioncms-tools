/*******************************************************************************
 *
 * [ PSUserConfig.java ]
 * 
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.client;

import com.percussion.conn.PSServerException;
import com.percussion.design.objectstore.PSObjectStore;
import com.percussion.design.objectstore.PSUserConfiguration;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;

import java.util.Vector;

/**
 * Reads all of the user configuration information from the server and stores it
 * for local access. Each PSUserConfig object is for a specific user set when the
 * object is created.
 * <p>
 * User config info is stored as key,value pairs. The user can query keys, add
 * keys and remove specified keys. All pairs can be removed, but this should
 * only be done as a last resort, since this will clear all object's config
 * info.
 * <p>
 * There are a number of convenience methods for getting/setting values of
 * various types.
 * <p>
 * All changes in configuration are kept locally until the flush method is
 * called or the object is garbage collected. At that point in time, the
 * configuration data is written to the server. If the write back is occurring
 * because of a finalize call and the server can't be contacted, any changes in
 * the config data since the last flush will be lost. The object is smart about
 * tracking changes; for example, if you get a value, then set that same value,
 * this will not be considered a change.
 * <p>
 * There is a single user config object that is shared among all objects
 * requiring this service.
 */
public class PSUserConfig
{
   /**
    * Connects to the object store and gets the user configuration. If a user
    * config object already exists, it is released if a new object is
    * successfully created. Walks the XML document and creates a Properties
    * object. Does this in reverse when saving the properties.
    * 
    * @returns the newly created object
    * 
    * @throws PSAuthenticationFailedException
    * @throws PSServerException if strServer cannot be found
    * @throws PSAuthorizationException if the specified user and pw could not be
    * validated with designer access on the server
    */
   static public PSUserConfig createConfig(PSObjectStore Store)
      throws PSServerException, PSAuthorizationException,
      PSAuthenticationFailedException
   {
      ms_Store = Store;
      if (null == ms_Store.getUserConfiguration())
         throw new RuntimeException(
            "User configuration object in the ObjectStore cannot be null");
      ms_theConfig = new PSUserConfig(ms_Store.getUserConfiguration());

      return ms_theConfig;
   }

   /**
    * Returns the current configuration object, or null if one hasn't been
    * created yet.
    * 
    * created thru a call to createConfig()
    */
   static public PSUserConfig getConfig()
   {
      if (null == ms_theConfig)
         throw new RuntimeException(
            "Configuration not initialized. Did you call createConfig() prior to this?");
      return ms_theConfig;
   }

   /**
    * Returns <code>true</code> if there are no properties in the
    * configurator, otherwise <code>false</code> is returned.
    */
   public boolean isEmpty()
   {
      return m_Properties.isEmpty();
   }

   /**
    * Looks for the specified key. If found, tries to convert it and returns the
    * result.
    * 
    * @param strKey name of the key to search for in the configurator. Usually
    * this key/value pair should be created using setBoolean(...).
    * @param bDefaultValue this value is returned if the key is not found
    * 
    * @returns <code>true</code> if the value is 'true' (case insensitive),
    * otherwise <code>false</code> is returned. If no key is found,
    * bDefaultValue is returned.
    * 
    * @see #setValue(String, String, boolean)
    */
   public boolean getBoolean(String strKey, boolean bDefaultValue)
   {
      String strBool = getValue(strKey);
      boolean bVal;

      if (null == strBool)
         bVal = bDefaultValue;
      else
         bVal = Boolean.valueOf(strBool).booleanValue();
      return (bVal);
   }

   /**
    * Sets the specified key to the supplied value, creating it if necessary. If
    * strKey is null or empty, nothing is done and no error occurs.
    * 
    * @see #getValue
    */
   public void setBoolean(String strKey, boolean bValue)
   {
      if (null == strKey || 0 == strKey.length())
         return;
      setValue(strKey, Boolean.toString(bValue));
   }

   /**
    * Adds an entry to the config using the supplied key and value. Use
    * getInteger to retrieve the value.
    * 
    * @param strKey the key to store the value under. If already in the config,
    * it is overwritten.
    * 
    * @param value the value to store with the key
    * 
    * @see #getInteger
    */
   public void setInteger(String strKey, int value)
   {
      setValue(strKey, Integer.toString(value));
   }

   /**
    * Reads a value from the config, converting it to an integer. If a key with
    * the requested name is not found, defaultValue is returned. The value
    * should be called against keys created with setInteger.
    * 
    * @param strKey the key to look up
    * 
    * @param defaultValue is returned if a key by the supplied name is not found
    * 
    * @returns the integer associated with strKey, or defaultValue if the key is
    * not in the config
    * 
    * @see #setInteger
    */
   public int getInteger(String strKey, int defaultValue)
   {
      String strValue = getValue(strKey);
      if (null == strValue)
         return defaultValue;
      else
      {
         Integer val = new Integer(strValue);
         return val.intValue();
      }
   }

   /**
    * Changes the value of an existing key (if strKey is found) or adds a new
    * key,value pair.
    * 
    * @param strKey - a non-null, non-empty string, keys are case sensitive
    * @param iaValues - an array of int values
    * 
    * @throws IllegalArgumentException if strKey is null or empty or iaValues is
    * null or empty
    */
   public void setIntArray(String strKey, int[] iaValues)
   {
      if (null == strKey || null == iaValues || 0 == strKey.length()
         || iaValues.length <= 0)
         throw new IllegalArgumentException();

      String strValue = "" + iaValues[0];
      if (iaValues.length > 1)
      {
         for (int i = 1; i < iaValues.length; i++)
         {
            strValue = strValue + DELIMITER + iaValues[i]; // put a delimiter
                                                            // between each
                                                            // value
         }
      }
      setValue(strKey, strValue);
   }

   /**
    * Returns an array of <code>int</code> associated with the supplied key.
    * If the key cannot be found, null is returned.
    * 
    * @param strKey a non-null, non-empty string; keys are case sensitive
    * 
    * @returns an array of <code>int</code> associated with the supplied key
    * or null if the supplied key can't be found in the configuration data. If
    * strKey is null or empty, null is returned.
    */
   public int[] getIntArray(String strKey)
   {
      if (null == strKey || 0 == strKey.length())
         return (null);

      String[] straIntegers = getStringTokens(strKey);
      if (straIntegers == null || straIntegers.length <= 0)
         return null;

      int[] iaValues = new int[straIntegers.length];

      for (int i = 0; i < iaValues.length; i++)
         iaValues[i] = Integer.parseInt(straIntegers[i]);

      return iaValues;

   }

   /**
    * Returns the value associated with the supplied key. If the key cannot be
    * found, null is returned. It is possible for a key to be associated with
    * the empty string.
    * 
    * @param strKey a non-null, non-empty string; keys are case sensitive
    * 
    * @returns the value associated with the supplied key or null if the
    * supplied key can't be found in the configuration data. If strKey is null
    * or empty, null is returned.
    */
   public String getValue(String strKey)
   {

      if (null == strKey || 0 == strKey.length())
         return (null);

      String value = (String) m_Properties.get(strKey);

      return value;
   }

   /**
    * Changes the value of an existing key (if strKey is found) or adds a new
    * key,value pair.
    * 
    * @param strKey - a non-null, non-empty string, keys are case sensitive
    * @param strValue - a non-null value
    * 
    * @throws IllegalArgumentException if strKey is null or empty or strValue is
    * null
    */
   @SuppressWarnings("unchecked")
   public void setValue(String strKey, String strValue)
   {
      if (null == strKey || null == strValue || 0 == strKey.length())
         throw new IllegalArgumentException();

      String strExistingValue = getValue(strKey);
      if (null == strExistingValue || !strValue.equals(strExistingValue))
      {
         m_bConfigChanged = true;
         String value = new String(strValue);
         m_Properties.put(strKey, value);
      }
   }

   /**
    * Appends to the strValue corresponding to an existing key (if strKey is
    * found) or adds a new key,value pair.
    * 
    * @param strKey - a non-null, non-empty string, keys are case sensitive
    * @param strValue - a non-null value
    * @param append - a boolean value, if true, strValue will be appended to the
    * end of existing strValue with delimiter in between
    * 
    * @throws IllegalArgumentException if strKey is null or empty or strValue is
    * null
    */

   public void setValue(String strKey, String strValue, boolean append)
   {
      if (null == strKey || null == strValue || 0 == strKey.length())
         throw new IllegalArgumentException();

      if (append == false)
      {
         setValue(strKey, strValue);
         return;
      }
      else
      // append == true, need to append
      {
         String strExistingValue = getValue(strKey);
         // check to see if key exists and has an associated string value
         // append to the existing string value with a comma separator
         strValue = PSUserConfigHelper.addEscapeChars(strValue);
         if (strExistingValue != null)
         {
            strValue = strExistingValue + DELIMITER + strValue;
         }
         setValue(strKey, strValue);
      }
   }

   /**
    * Parses the string value associated with the specified key using the
    * delimiter and returns an array of strings (each string is a token)
    * 
    * @param strKey - a non-null, non-empty string, keys are case sensitive
    * 
    * @throws IllegalArgumentException if strKey is null or empty
    */

   public String[] getStringTokens(String strKey)
   {
      if (strKey == null || 0 == strKey.length())
      {
         throw new IllegalArgumentException();
      }
      else
      {
         String[] strArray = null;
         // get the existing Value associated with this key
         String strExistingValue = getValue(strKey);
         if (strExistingValue != null)
         {
            PSUserConfigHelper uch = new PSUserConfigHelper();
            Vector v = uch.getStringTokens(strExistingValue);

            int count = v.size();
            strArray = new String[count];
            for (int i = 0; i < v.size(); i++)
            {
               // System.out.println("............Token = "+(String)v.get(i));
               strArray[i] = (String) v.get(i); // fill the array to be returned
            }
         }
         return strArray;
      }
   }

   /**
    * Returns true if the specified string is found in the delimiter separated
    * list of the string values associated with the specified key
    * 
    * @param strKey - a non-null, non-empty string, keys are case sensitive
    * @param strToken - a non-null, non-empty string, to be searched for in the
    * existing values
    * 
    * @throws IllegalArgumentException if strKey is null or empty or strToken is
    * null
    */
   public boolean isStringTokenPresent(String strKey, String strToken)
   {
      boolean bFound = false;
      if (strKey == null || 0 == strKey.length() || strToken == null)
         throw new IllegalArgumentException();
      else
      {
         String strExistingValue = getValue(strKey);
         if (strExistingValue != null)
         {
            PSUserConfigHelper uch = new PSUserConfigHelper();
            Vector v = uch.getStringTokens(strExistingValue);
            for (int i = 0; i < v.size(); i++)
            {
               String str = (String) v.get(i);
               if (strToken.equals(str))
               {
                  bFound = true;
                  break;
               }
            }
         }
      }
      return bFound;
   }

   /**
    * Removes the first specified string strToken if it is found in the
    * delimiter separated list of the string values associated with the
    * specified key
    * 
    * @param strKey - a non-null, non-empty string, keys are case sensitive
    * @param strToken - a non-null, non-empty string, to be searched for in the
    * existing values
    * 
    * @throws IllegalArgumentException if strKey is null or empty or strToken is
    * null
    */
   public void removeStringToken(String strKey, String strToken)
   {
      if (strKey == null || 0 == strKey.length() || strToken == null)
         throw new IllegalArgumentException();

      if (isStringTokenPresent(strKey, strToken))
      {
         String[] strTokens = getStringTokens(strKey);
         boolean bRemovedOneToken = false;
         // remove the key then re-add the tokens skipping the one to be removed
         deleteEntry(strKey);
         for (int i = 0; i < strTokens.length; i++)
         {
            if (strTokens[i].equals(strToken) && bRemovedOneToken == false)
            {
               bRemovedOneToken = true; // should only get here once
               continue; // skip the addition of this token
            }
            setValue(strKey, strTokens[i], true); // append the token
         }
      }
   }

   /**
    * Removes the entry specified by strKey from the configuration information.
    * If the key is not found, nothing is done. Keys are case sensitive.
    */
   public void deleteEntry(String strKey)
   {
      if (null != getValue(strKey))
      {
         m_bConfigChanged = true;
         m_Properties.remove(strKey);
      }
   }

   /**
    * Removes all key,value pairs from the configuration file. This should only
    * be used as a last resort. The changes don't actually get written to the
    * server until flush() is called.
    */
   public void clearConfiguration()
   {
      if (!isEmpty())
      {
         m_bConfigChanged = true;
         m_Properties.clear();
      }
   }

   /**
    * Writes all the configuration information back to the server, if it has
    * changed. If no changes have been made, no write back is performed.
    * 
    * @throws PSAuthenticationFailedException
    * @throws PSAuthorizationException
    * @throws PSServerException
    * 
    * @returns <code>true</code> if successfully written or not changed,
    * <code>false</code> if changed data could not be written (most likely
    * because the server couldn't be contacted). If <code>false</code> is
    * returned, try again later. There is a remote chance that the user's access
    * was changed to something less than designer, which would also cause a
    * false return. If this happens, the configuration information will be lost.
    */
   public boolean flush() throws PSServerException, PSAuthorizationException,
      PSAuthenticationFailedException
   {
      if (m_bConfigChanged)
      {
         // write back properties
         ms_Store.saveUserConfiguration(m_Properties);
         m_bConfigChanged = false;
      }
      return !m_bConfigChanged;
   }

   /**
    * If the configuration hasn't been saved yet, save it now.
    */
   @Override
   protected void finalize() throws Throwable
   {
      flush();
      super.finalize();
   }

   // private methods
   /**
    * The constructor is private to implement a singleton pattern. Use
    * createConfig() and getConfig().
    * 
    * @param uc user configuration object, assumed not <code>null</code>.
    * 
    */
   private PSUserConfig(PSUserConfiguration uc)
   {
      m_Properties = uc;
   }

   /**
    * Delimiter used for storing multiple string tokens in a string value for a
    * key. Note:Must be a single char and must stay the same between versions.
    */
   public static final String DELIMITER = ";";

   /**
    * The escape char is the character that is inserted in a string just before
    * the chars that are the same as the DELIMITER char. This allows
    * differenciating of the chars within a string and chars that are
    * DELIMITERS.
    */
   public static final char ESCAPE_CHAR = '^';

   // private variables
   /**
    * The singleton instance of this class.
    */
   static private PSUserConfig ms_theConfig = null;

   /**
    * This is the object store that maintains the properties on the server. We
    * need to keep it around to write the configuration later.
    */
   private static PSObjectStore ms_Store = null;

   /**
    * All of the properties from the server are stored in this object.
    */
   private PSUserConfiguration m_Properties = null;

   /**
    * False until the list of properties has changed in some way. If <code>
    * true</code>,
    * the properties will be written back to the server when flush is called or
    * the object is destroyed.
    */
   private boolean m_bConfigChanged = false;
}
