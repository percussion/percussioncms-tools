/******************************************************************************
 *
 * [ PSConfigNodeHandler.java ]
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
 * This class prevents paste & rename. It adds support for file-type
 * interactions.
 * 
 * @author paulhoward
 */
public class PSConfigNodeHandler extends PSFileNodeHandler2
{
   /**
    * Ctor required by base class. See that method for details.
    */
   public PSConfigNodeHandler(Properties props, String iconPath,
         PSObjectType[] allowedTypes)
   {
      super(props, iconPath, allowedTypes);
   }

   @Override
   protected File getFileFromRef(final PSUiReference ref)
   {
      return new File("c:/temp/" + ref.getName() + ".txt");
   }
}
