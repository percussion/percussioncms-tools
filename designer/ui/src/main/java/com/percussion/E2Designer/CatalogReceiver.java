/******************************************************************************
 *
 * [ CatalogReceiver.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.E2Designer;

import java.util.HashMap;
import java.util.Map;

/**
 * The storage object used in all GUI cataloging features. This wraps around a
 * <CODE>HashMap</CODE> to avoid duplicated objects.
 */
public class CatalogReceiver
{
  /** Constants used for cataloging application UDFs. This should be used as
   * the key to retrieving a <CODE>PSCollection</CODE> of application UDFs.
   */
  public static final String CATALOG_UDF_APP = "application";
  /** Constants used for cataloging server UDFs. This should be used as
   * the key to retrieving a <CODE>PSCollection</CODE> of server UDFs.
   */
  public static final String CATALOG_UDF_SERVER = "server";

  /**
   * Simpily adds a new element into our storage. This should be used if you
   * want to use this storage without a key.
   *
   * @param element Object to be stored. <CODE>null</CODE> is okay, will be
   * ignored.
   */
  public void add( Object element )
  {
    m_hmContainer.put( element, null );
  }

  /**
   * This should be used if you want to use this storage without a key.
   *
   * @return A reference to the <CODE>HashMap</CODE> object that stores all the
   * cataloged results.
   */
  public Map<Object, Object> getCatalogData()
  {
    return m_hmContainer;
  }

   /**
    * Instead of working directly with the storage <CODE>HashMap</CODE>
    * instance, you can go through this method instead to retrieve data that you
    * stored with a key.
    *
    * @param key The key for which an object stored within this
    * <CODE>HashMap</CODE>.
    *
    * @return The value stored based on the key passed in.
    */
   public Object get( Object key )
   {
      return m_hmContainer.get( key );
   }


   /**
    * Instead of working directly with the storage <CODE>HashMap</CODE>
    * instance, you can go through this method instead to set data that you
    * want to store with a key.
    *
    * @param key The key for which an object stored within this
    * <CODE>HashMap</CODE>.
    * @param value The object that you are storing based on the key.
    */
   public void put( Object key, Object value )
   {
      m_hmContainer.put( key, value );
   }


  /** The main source of storage; used to avoid duplication. */
  private Map<Object, Object> m_hmContainer = new HashMap<Object, Object>();
}
