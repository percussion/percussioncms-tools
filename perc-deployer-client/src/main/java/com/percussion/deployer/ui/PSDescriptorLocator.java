/******************************************************************************
 *
 * [ PSDescriptorLocator.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.deployer.ui;

import com.percussion.deployer.objectstore.PSExportDescriptor;
import com.percussion.error.PSDeployException;

/**
 * Class to encapsulate information regarding the source of a descriptor, and
 * to provide functionality to load it from that source.
 */
public class PSDescriptorLocator
{
   /**
    * Construct a locator to load a saved export descriptor.
    * 
    * @param name The name of the export descriptor, may not be 
    * <code>null</code> or empty.
    */
   public PSDescriptorLocator(String name)
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty");
      
      m_descName = name; 
   }
   
   /**
    * Construct a locator to load a descriptor from an installed archive.
    * 
    * @param archiveLogId The archive log id used to locate the archive from 
    * which the descriptor is to be extracted.
    */
   public PSDescriptorLocator(int archiveLogId)
   {
      m_archiveLogId = archiveLogId;
   }
   
   /**
    * Loads the descriptor from the supplied server using the information
    * provided during construction.
    * 
    * @param server The server from which the descriptor is to be loaded, may
    * not be <code>null</code> and must be connected.
    * @return
    * @throws PSDeployException
    */
   public PSExportDescriptor load(PSDeploymentServer server) 
      throws PSDeployException
   {   
      if (server == null)
         throw new IllegalArgumentException("server may not be null");
      
      if (!server.isConnected())
         throw new IllegalArgumentException("server must be connected");
      
      PSExportDescriptor desc = null;
      if (m_descName != null)
         desc = server.getDeploymentManager().getExportDescriptor(m_descName);
      else
         desc = server.getDeploymentManager().getExportDescriptor(
            m_archiveLogId);

      return desc;
   }
   
   /**
    * Determine if this locator was constructed with an export descriptor name
    * and will load a saved export descriptor.
    * 
    * @return <code>true</code> if it references a saved export descriptor, 
    * <code>false</code> if not.
    */
   public boolean isSavedExportDescriptor()
   {
      return m_descName != null;
   }
   
   /**
    * Determine if this locator was constructed with an archive log id and will 
    * load an export descriptor from an installed archive.
    * 
    * @return <code>true</code> if it references a descriptor from an installed 
    * archive, <code>false</code> otherwise.
    */
   public boolean isInstalledArchiveDescriptor()
   {
      return m_descName == null;
   }
   
   /**
    * The name used to load a saved export descriptor from the server, initially
    * <code>null</code>, may be provided during construction in which case it
    * will not be <code>null</code> or empty.  Immutable after construction.
    */
   private String m_descName = null;
   
   /**
    * The id used to load an export descriptor from an archive installed on the 
    * server, initially <code>-1</code>, may be modified during construction.  
    * Immutable after construction.
    */   
   private int m_archiveLogId = -1;
}
