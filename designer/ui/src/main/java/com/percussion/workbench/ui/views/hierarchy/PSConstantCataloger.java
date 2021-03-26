/******************************************************************************
 *
 * [ PSConstantCataloger.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.views.hierarchy;

import com.percussion.client.IPSReference;
import com.percussion.workbench.ui.IPSCatalog;
import com.percussion.workbench.ui.PSUiReference;

import java.util.List;

/**
 * This is used as the cataloger when the def defines a constant node.
 * 
 * @version 6.0
 * @author paulhoward
 */
public class PSConstantCataloger implements IPSCatalog
{

   public PSConstantCataloger()
   {

   }

   /**
    * 
    * @param name
    */
   public PSConstantCataloger(String name)
   {

   }

   /**
    * 
    * @return Never <code>null</code>, may be empty.
    */
   public List<PSUiReference> getEntries(boolean force)
   {
      return null;
   }
   
   public PSUiReference createEntry(IPSReference ref)
   {
      throw new UnsupportedOperationException(
            "Unsupported for local file system.");
   }
}
