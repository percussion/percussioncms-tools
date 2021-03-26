/******************************************************************************
 *
 * [ PSLocalFileSystemModel.java ]
 * 
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.models.impl;

import com.percussion.client.IPSPrimaryObjectType;
import com.percussion.client.IPSReference;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.models.IPSLocalFileSystemModel;
import com.percussion.client.proxies.impl.PSLocalFileSystemHierarchyNodeRef;

import java.io.File;

/**
 * Model for {@link PSObjectTypes#LOCAL_FILE} objects.
 *
 * @author Andriy Palamarchuk
 */
public class PSLocalFileSystemModel extends PSCmsModel
      implements IPSLocalFileSystemModel
{

   /**
    * The constructor required by the model contract.
    */
   public PSLocalFileSystemModel(String name, String description,
         IPSPrimaryObjectType supportedType)
   {
      super(name, description, supportedType);
   }

   // see base
   public File getFile(IPSReference reference)
   {
      assert reference instanceof PSLocalFileSystemHierarchyNodeRef;
      return ((PSLocalFileSystemHierarchyNodeRef) reference).getFile();
   }
}
