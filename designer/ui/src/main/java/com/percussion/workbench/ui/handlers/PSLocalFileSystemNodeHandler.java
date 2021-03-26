/******************************************************************************
 *
 * [ PSLocalFileSystemNodeHandler.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.handlers;

import com.percussion.client.PSObjectType;
import com.percussion.workbench.ui.PSUiReference;

import java.io.File;
import java.util.Properties;

/**
 * Node handler for the local filesystem view.
 *
 * @author Andriy Palamarchuk
 */
public class PSLocalFileSystemNodeHandler extends PSFileNodeHandler2
{
   /**
    * Creates new node handler.
    * The constructor signature is required by the framework. 
    */
   public PSLocalFileSystemNodeHandler(Properties props, String iconPath,
         PSObjectType[] allowedTypes)
   {
      super(props, iconPath, allowedTypes);
   }

   /**
    * @inheritDoc
    * 
    * No failure is expected, so it logs any exception thrown, wraps
    * checked exception in runtime exception and rethrows the exception. 
    */
   @Override
   protected File getFileFromRef(final PSUiReference ref)
   {
      final PSUiReference parentRef = ref.getParentNode();
      return parentRef == null ? new File(ref.getName())
            : new File(getFileFromRef(parentRef), ref.getName());  
   }
}
