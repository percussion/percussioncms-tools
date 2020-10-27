/******************************************************************************
 *
 * [ PSWorkbenchFolder.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.views.hierarchy;

import com.percussion.client.IPSHierarchyNodeRef;
import com.percussion.services.ui.data.PSHierarchyNode;
import com.percussion.workbench.ui.IPSCatalog;

import java.util.List;

/**
 * @version 1.0
 * @created 03-Sep-2005 4:44:06 PM
 */
public class PSWorkbenchFolder
{

   public PSWorkbenchFolder()
   {
   }

   /**
    * @param name
    * @param catalog
    * @param persistable
    */
   public PSWorkbenchFolder(String name, IPSCatalog catalog, boolean persistable)
   {
   }

   /**
    * @param source
    */
   public PSWorkbenchFolder(PSHierarchyNode source)
   {
   }

   /**
    * A flag to indicate if this is a system folder. System folders cannot be
    * modified by the implementer unless the com.percussion.developer Java
    * property has been set to <code>true</code>. And then, only if they are
    * persistable, as indicated by the {@link #isPersistable()} method.
    */
   public boolean isSystem()
   {
      return false;
   }

   /**
    * Some folders are loaded from persistent storage on the server and thus can
    * potentially be modified and others are created dynamically based on the
    * declarative hierarchy definition. The latter are never persisted. This
    * flag is set for the former and clear for the latter case.
    */
   public boolean isPersistable()
   {
      return false;
   }

   /**
    * Can the implementer add children to this node. This is guaranteed to be
    * <code>false</code> if {@link #isPersistable()} is <code>false</code>.
    */
   public boolean allowsFolderChildren()
   {
      return false;
   }

   public List<IPSHierarchyNodeRef> getChildren()
   {
      return null;
   }

}
