/******************************************************************************
 *
 * [ PSUserFileModel.java ]
 * 
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.models.impl;

import com.percussion.client.IPSPrimaryObjectType;
import com.percussion.client.PSModelException;
import com.percussion.client.models.IPSUserFileModel;
import com.percussion.client.proxies.impl.PSUserFileHierarchyModelProxy;
import com.percussion.services.ui.data.PSHierarchyNode;

import java.util.Collection;

/**
 * Straight implementation of the interface.
 * 
 * @author paulhoward
 */
public class PSUserFileModel extends PSCmsModel implements IPSUserFileModel
{
   /**
    * Ctor needed for base class. See
    * {@link PSCmsModel#PSCmsModel(String, String, IPSPrimaryObjectType) base ctor}
    * for details.
    */
   public PSUserFileModel(String name, String description,
      IPSPrimaryObjectType supportedType)
   {
      super(name, description, supportedType);
   }

   // see interface
   public Collection<PSHierarchyNode> getDescendentPlaceholders(String treeName)
      throws PSModelException
   {
      PSUserFileHierarchyModelProxy proxy;
      proxy = new PSUserFileHierarchyModelProxy();
      return proxy.getDescendents("/" + treeName + "/*");
   }
}
