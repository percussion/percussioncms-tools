/******************************************************************************
 *
 * [ CatalogerMetaData.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Encapsulates the meta data of a subject or directory cataloger.
 */
public class CatalogerMetaData implements Comparable
{
   /**
    * Factory method to create meta data to represent the "<Any>" selection.
    * 
    * @return The metadata, never <code>null</code>.
    */
   public static CatalogerMetaData createMetaDataForAny()
   {
      return new CatalogerMetaData(
         SecurityProviderMetaData.NAME_FOR_ANY_PROVIDER, 
         SecurityProviderMetaData.NAME_FOR_ANY_PROVIDER, 
         SecurityProviderMetaData.NAME_FOR_ANY_PROVIDER);
   }
   
   /**
    * Construct the metadata for a cataloger.
    * 
    * @param name The name of the cataloger, never <code>null</code> or empty.
    * @param type The type of the cataloger, never <code>null</code> or empty,
    * together with the name forms a unqique reference.
    * @param fullName The full display name of this cataloger, never
    * <code>null</code> or empty.
    */
   public CatalogerMetaData(String name, String type, String fullName)
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name may not be null or empty");

      if (StringUtils.isBlank(type))
         throw new IllegalArgumentException("type may not be null or empty");

      if (StringUtils.isBlank(fullName))
         throw new IllegalArgumentException(
            "fullName may not be null or empty");

      m_name = name;
      m_type = type;
      m_fullName = fullName;
   }
   
   /**
    * Get the name of the cataloger.
    * 
    * @return The name, never <code>null</code> or empty.
    */
   public String getName()
   {
      return m_name;
   }
   
   /**
    * Get the type of the cataloger.
    * 
    * @return The type, never <code>null</code> or empty.
    */
   public String getType()
   {
      return m_type;
   }
   /**
    * Get the full display name of the cataloger.
    * 
    * @return The display name, never <code>null</code> or empty.
    */
   public String getDisplayName()
   {
      return m_fullName;
   }
   
   // see base class
   @Override
   public String toString()
   {
      return m_fullName;
   }
   
   /**
    * Determine if this meta data matches what is created by the 
    * {@link #createMetaDataForAny()} method.
    *  
    * @return <code>true</code> if it was, <code>false</code> if not.
    */
   public boolean isAnyCataloger()
   {
      return (m_name.equals(SecurityProviderMetaData.NAME_FOR_ANY_PROVIDER)
         && m_type.equals(SecurityProviderMetaData.NAME_FOR_ANY_PROVIDER)
         && m_fullName.equals(SecurityProviderMetaData.NAME_FOR_ANY_PROVIDER));
   }
   
   @Override
   public boolean equals(Object obj)
   {
      if (!(obj instanceof CatalogerMetaData))
      {
         return false;
      }
      if (this == obj)
      {
         return true;
      }
      final CatalogerMetaData data = (CatalogerMetaData) obj;
      return new EqualsBuilder()
            .append(m_fullName, data.m_fullName)
            .append(m_name, data.m_name)
            .append(m_type, data.m_type)
            .isEquals();
   }

   @Override
   public int hashCode()
   {
      return new HashCodeBuilder()
            .append(m_fullName)
            .append(m_name)
            .append(m_type)
            .toHashCode();
   }   
   
   /**
    * Compares based on display name.  See {@link Comparable}
    * for more details.
    */
   public int compareTo(Object o)
   {
      CatalogerMetaData other = (CatalogerMetaData) o;
      return getDisplayName().compareTo(other.getDisplayName());
   }   
   
   /**
    * Name supplied during construction, immutable.
    */
   private String m_name;
   
   /**
    * Type supplied during construction, immutable.
    */
   private String m_type;
   
   /**
    * Full name supplied during construction, immutable.
    */
   private String m_fullName;
}

