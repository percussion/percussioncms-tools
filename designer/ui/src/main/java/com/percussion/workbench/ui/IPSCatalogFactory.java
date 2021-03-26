/******************************************************************************
 *
 * [ IPSCatalogFactory.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui;

/**
 * Part of the framework for lazy retrieval of tree/list nodes. The work
 * required to build the list supplied by the catalog is generally done by the
 * implementing class.
 * <p>
 * Creates a class implementing the {@link IPSCatalog} interface that will
 * return the children of the supplied <code>parent</code>. Actual work 
 * performed should be pushed off into the IPSCatalog implementation as much as
 * possible.
 * <p>
 * Each implementing class must derive from
 * {@link com.percussion.workbench.ui.catalogs.PSCatalogFactoryBase}.
 * 
 * @version 6.0
 * @author Paul Howard
 */
public interface IPSCatalogFactory
{
   /**
    * Performs the work of retrieving the children of the supplied node and
    * creates the appropriate objects for the catalog list.
    * 
    * @param parent May be <code>null</code> to obtain the root elements. Not
    * all factories support cataloging the roots.
    * 
    * @return Never <code>null</code>.
    */
   public IPSCatalog createCatalog(PSUiReference parent);
}
