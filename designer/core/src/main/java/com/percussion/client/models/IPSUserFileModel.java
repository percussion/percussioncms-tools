/******************************************************************************
 *
 * [ IPSUserFileModel.java ]
 * 
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.models;

import com.percussion.client.PSModelException;
import com.percussion.services.ui.data.PSHierarchyNode;

import java.util.Collection;

/**
 * A custom extension is provided for USER_FILE to give extra cataloging 
 * functionality.
 *
 * @author paulhoward
 */
public interface IPSUserFileModel extends IPSCmsModel
{
   /**
    * Gets the data object for all placeholders that exist in the requested 
    * tree.
    *  
    * @param treeName Never <code>null</code> or empty.
    * 
    * @return Never <code>null</code>.
    * 
    * @throws PSModelException If any problems communicating with the server.
    * It may wrap a PSMultiOperationException.
    */
   public Collection<PSHierarchyNode> getDescendentPlaceholders(
         String treeName)
      throws PSModelException;
}
