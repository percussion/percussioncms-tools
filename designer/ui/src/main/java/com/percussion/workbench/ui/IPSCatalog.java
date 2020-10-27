/******************************************************************************
 *
 * [ IPSCatalog.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui;

import com.percussion.client.IPSReference;
import com.percussion.client.PSModelException;

import java.util.List;

/**
 * A container for a group of children owned by the same parent. It is used in
 * support of lazy retrieval of tree/list nodes. Generally, the work of building
 * the list was done when the catalog was created.
 * 
 * @version 6.0
 * @author Paul Howard
 */
public interface IPSCatalog
{
   /**
    * A set of references that are considered children of some other reference.
    * 
    * @param force If <code>true</code>, any cached data is invalidated.
    * 
    * @return Never <code>null</code>, may be empty.
    * 
    * @throws PSModelException If any problems while communicating with the
    * server.
    */
   public List<PSUiReference> getEntries(boolean force)
      throws PSModelException;
   
   /**
    * Unlike the {@link #getEntries(boolean)} method, which queries the
    * underlying model for the children of the owning node, this method takes
    * the supplied reference and creates a node equivalent to having cataloged
    * it.
    * <p>
    * Generally, this is used after the {@link #getEntries(boolean)} method has
    * been called at least once and the owner doesn't want to re-catalog
    * everything to get the newly created object.
    * <p>
    * Not all implementations support this method.
    * 
    * @param ref The newly created wrapper node. Never <code>null</code>.
    * 
    * @return Never <code>null</code>.
    * 
    * @throws UnsupportedOperationException If not supported.
    */
   public PSUiReference createEntry(IPSReference ref);   
}
