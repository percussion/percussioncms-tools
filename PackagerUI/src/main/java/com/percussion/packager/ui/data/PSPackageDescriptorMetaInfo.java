/******************************************************************************
 *
 * [ PSPackageDescriptorMetaInfo.java ]
 * 
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.packager.ui.data;

import com.percussion.deployer.objectstore.PSDescriptor;
import com.percussion.deployer.objectstore.PSExportDescriptor;
import org.apache.commons.lang.StringUtils;

import java.util.Date;

/**
 * Data class that represents the packager client meta info
 * (General) page.
 * 
 */
public class PSPackageDescriptorMetaInfo
{
   /**
    * Ctor
    * @param name
    */
   public PSPackageDescriptorMetaInfo(String name)
   {
      setName(name);
   }
   
   /**
    * Create the package descriptor meta info from a
    * PSDescriptor object.
    * @param desc
    */
   public PSPackageDescriptorMetaInfo(PSDescriptor desc)
   {
      if(desc == null)
         throw new IllegalArgumentException("descriptor cannot be null.");
      m_name = desc.getName();
      m_id = desc.getId();
      m_description = desc.getDescription();
      m_version = desc.getVersion();
      m_publisherName = desc.getPublisherName();
      m_publisherUrl = desc.getPublisherUrl();
      m_cmsMinVersion = desc.getCmsMinVersion();
      m_cmsMaxVersion = desc.getCmsMaxVersion();
      m_configDefPath = desc.getConfigDefFile();
      m_localConfigPath = desc.getLocalConfigFile();
   }
   
   /**
    * Create a new export descriptor from info contained in this
    * object.
    * @return the descriptor, never <code>null</code>.
    */
   public PSExportDescriptor toExportDescriptor()
   {
      PSExportDescriptor desc = new PSExportDescriptor(m_name);
      if(StringUtils.isNotBlank(m_id))
         desc.setId(m_id);
      if(StringUtils.isNotBlank(m_description))
         desc.setDescription(m_description);
      if(StringUtils.isNotBlank(m_version))
         desc.setVersion(m_version);
      if(StringUtils.isNotBlank(m_publisherName))
         desc.setPublisherName(m_publisherName);
      if(StringUtils.isNotBlank(m_publisherUrl))
         desc.setPublisherUrl(m_publisherUrl);
      if(StringUtils.isNotBlank(m_cmsMinVersion))
         desc.setCmsMinVersion(m_cmsMinVersion);
      if(StringUtils.isNotBlank(m_cmsMaxVersion))
         desc.setCmsMaxVersion(m_cmsMaxVersion);
      if(StringUtils.isNotBlank(m_configDefPath))
         desc.setConfigDefFile(m_configDefPath);
      if(StringUtils.isNotBlank(m_localConfigPath))
         desc.setLocalConfigFile(m_localConfigPath);
      
      return desc;
   }  
   
   
   /* (non-Javadoc)
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj)
   {
      if(obj == null || !(obj instanceof PSPackageDescriptorMetaInfo))
         return false;
      PSPackageDescriptorMetaInfo other = (PSPackageDescriptorMetaInfo)obj;
      return m_name.equals(other.m_name);
   }

   /**
    * @return the name
    */
   public String getName()
   {
      return m_name;
   }

   /**
    * @param name the name to set
    */
   public void setName(String name)
   {      
      m_name = name;
   }

   /**
    * @return the description
    */
   public String getDescription()
   {
      return m_description;
   }

   /**
    * @param description the description to set
    */
   public void setDescription(String description)
   {
      m_description = description;
   }

   /**
    * @return the publisherName
    */
   public String getPublisherName()
   {
      return m_publisherName;
   }

   /**
    * @param publisherName the publisherName to set
    */
   public void setPublisherName(String publisherName)
   {
      m_publisherName = publisherName;
   }

   /**
    * @return the publisherUrl
    */
   public String getPublisherUrl()
   {
      return m_publisherUrl;
   }

   /**
    * @param publisherUrl the publisherUrl to set
    */
   public void setPublisherUrl(String publisherUrl)
   {
      m_publisherUrl = publisherUrl;
   }

   /**
    * @return the version
    */
   public String getVersion()
   {
      return m_version;
   }

   /**
    * @param version the version to set
    */
   public void setVersion(String version)
   {
      m_version = version;
   }

   /**
    * @return the cmsMinVersion
    */
   public String getCmsMinVersion()
   {
      return m_cmsMinVersion;
   }

   /**
    * @param cmsMinVersion the cmsMinVersion to set
    */
   public void setCmsMinVersion(String cmsMinVersion)
   {
      m_cmsMinVersion = cmsMinVersion;
   }

   /**
    * @return the cmsMaxVersion
    */
   public String getCmsMaxVersion()
   {
      return m_cmsMaxVersion;
   }

   /**
    * @param cmsMaxVersion the cmsMaxVersion to set
    */
   public void setCmsMaxVersion(String cmsMaxVersion)
   {
      m_cmsMaxVersion = cmsMaxVersion;
   }
   
   /**
    * @return the lastModified
    */
   public Date getLastModified()
   {
      return m_lastModified;
   }

   /**
    * @param lastModified the lastModified to set
    */
   public void setLastModified(Date lastModified)
   {
      m_lastModified = lastModified;
   }
   
   /**
    * @return the id
    */
   public String getId()
   {
      return m_id;
   }

   /**
    * @param id the id to set
    */
   public void setId(String id)
   {
      m_id = id;
   }
   
   /**
    * @return the configDefPath
    */
   public String getConfigDefPath()
   {
      return m_configDefPath;
   }

   /**
    * @param configDefPath the configDefPath to set
    */
   public void setConfigDefPath(String configDefPath)
   {
      m_configDefPath = configDefPath;
   }

   /**
    * @return the localConfigPath
    */
   public String getLocalConfigPath()
   {
      return m_localConfigPath;
   }

   /**
    * @param localConfigPath the localConfigPath to set
    */
   public void setLocalConfigPath(String localConfigPath)
   {
      m_localConfigPath = localConfigPath;
   }
   
   /**
    * Descriptor id.
    */
   private String m_id;
   
   /**
    * Package name.
    */
   private String m_name;
   
   /**
    * Package Description.
    */
   private String m_description;
   
   /**
    * Package publisher.
    */
   private String m_publisherName;
   
   /**
    * Package publisher url.
    */
   private String m_publisherUrl;
   
   /**
    * Package version.
    */
   private String m_version;
   
   /**
    * Package cms minimum version.
    */
   private String m_cmsMinVersion;
   
   /**
    * Package cms maximum version.
    */
   private String m_cmsMaxVersion;
   
   /**
    * Config def path on local machine.
    */
   private String m_configDefPath;
   
   /**
    * Local config path on local machine.
    */
   private String m_localConfigPath;
   
   /**
    * Last modified date
    */
   private Date m_lastModified;

   

  

   

   
}
