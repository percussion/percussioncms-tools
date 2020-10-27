/******************************************************************************
 *
 * [ PSPackageDependency.java ]
 * 
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.packager.ui.data;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Data object representing a package dependency.
 *
 */
public class PSPackageDependency
{
   public PSPackageDependency(String name)
   {
      setPackageName(name);
   }
   
   /**
    * @return the packageName
    */
   public String getPackageName()
   {
      return m_packageName;
   }

   /**
    * @param packageName the packageName to set
    */
   public void setPackageName(String packageName)
   {
      if(StringUtils.isBlank(packageName))
         throw new IllegalArgumentException("packageName cannot be null or empty.");
      this.m_packageName = packageName;
   }

   /**
    * @return the isSelected
    */
   public boolean isSelected()
   {
      return m_isSelected;
   }

   /**
    * @param isSelected the isSelected to set
    */
   public void setSelected(boolean isSelected)
   {
      this.m_isSelected = isSelected;
   }

   /**
    * @return the isImpliedDependency
    */
   public boolean isImpliedDependency()
   {
      return m_isImpliedDependency;
   }

   /**
    * @param isImpliedDependency the isImpliedDependency to set
    */
   public void setImpliedDependency(boolean isImpliedDependency)
   {
      this.m_isImpliedDependency = isImpliedDependency;
   }
   
   /**
    * @return the packageVersion
    */
   public String getPackageVersion()
   {
      return m_packageVersion;
   }

   /**
    * @param packageVersion the packageVersion to set
    */
   public void setPackageVersion(String packageVersion)
   {
      this.m_packageVersion = packageVersion;
   }   
   
   
   /* (non-Javadoc)
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj)
   {
      if(obj == null || 
         !(obj instanceof PSPackageDependency))
         return false;
      PSPackageDependency dep = (PSPackageDependency)obj;
      return new EqualsBuilder().
         append(m_packageName, dep.m_packageName).
         append(m_packageVersion, dep.m_packageVersion).
         isEquals();
   }

   /* (non-Javadoc)
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      return new HashCodeBuilder().
         append(m_packageName).
         append(m_packageVersion).
         toHashCode();
   }   
   
   /* (non-Javadoc)
    * @see java.lang.Object#clone()
    */
   @Override
   public Object clone()
   {
      PSPackageDependency clone = new PSPackageDependency(m_packageName);
      clone.m_packageVersion = m_packageVersion;
      clone.m_isImpliedDependency = m_isImpliedDependency;
      clone.m_isSelected = m_isSelected;
      return clone;
   }



   /**
    * Name of the package dependency.
    */
   private String m_packageName;
   
   /**
    * Version of the package dependency.
    */
   private String m_packageVersion;
   
   /**
    * Indicates that this dependency is selected.
    */
   private boolean m_isSelected;
   
   /**
    * Indicates that this dependency is implied by the selection
    * of design objects and will be automatically considered selected.
    */
   private boolean m_isImpliedDependency;

   

   
}
