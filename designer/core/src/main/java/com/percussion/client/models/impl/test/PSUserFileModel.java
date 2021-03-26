/******************************************************************************
 *
 * [ PSUserFileModel.java ]
 * 
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.models.impl.test;

import com.percussion.client.IPSHierarchyNodeRef;
import com.percussion.client.IPSPrimaryObjectType;
import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSModelException;
import com.percussion.client.PSMultiOperationException;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.models.IPSHierarchyManager;
import com.percussion.client.models.IPSUserFileModel;
import com.percussion.client.models.impl.PSCmsModel;
import com.percussion.client.proxies.IPSHierarchyModelProxy;
import com.percussion.client.proxies.impl.test.PSUserFileHierarchyModelProxy;
import com.percussion.services.ui.data.PSHierarchyNode;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Straight implementation of the interface that uses the test proxies to do 
 * its work.
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
   public Collection<PSHierarchyNode> getDescendentPlaceholders(
         String treeName)
         throws PSModelException
   {
      try
      {
         PSUserFileHierarchyModelProxy proxy = (PSUserFileHierarchyModelProxy) 
               PSCoreFactory.getInstance()
               .getHierarchyModelProxy(PSObjectTypes.USER_FILE);
         Collection<IPSHierarchyNodeRef> resultRefs = 
            new ArrayList<IPSHierarchyNodeRef>();
         proxy.getDescendentPlaceholders(new IPSHierarchyModelProxy.NodeId(
               treeName), resultRefs);
         
         /* normally, this is handled in the upper layers, but since we are 
          * bypassing them
          */
         IPSHierarchyManager mgr = getHierarchyManager(treeName); 
         for (IPSHierarchyNodeRef ref : resultRefs)
         {
            ref.setManager(mgr);
         }      
         
         Object[] placeholders = load(resultRefs
               .toArray(new IPSReference[resultRefs.size()]), false, false);
         
         Collection<PSHierarchyNode> results = new ArrayList<PSHierarchyNode>();
         for (Object o : placeholders)
         {
            results.add((PSHierarchyNode) o);
         }
         return results;
      }
      catch (PSMultiOperationException e)
      {
         throw new PSModelException(e);
      }
   }
}
