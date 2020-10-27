/******************************************************************************
 *
 * [ PSResourceFileHierarchyModelProxy.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.proxies.impl.test;

import com.percussion.client.IPSHierarchyNodeRef;
import com.percussion.client.PSModelException;
import com.percussion.client.proxies.PSUninitializedConnectionException;
import com.percussion.client.proxies.impl.PSXmlApplicationFileHierarchyRootRef;
import com.percussion.design.objectstore.PSApplication;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 */
public class PSResourceFileHierarchyModelProxy extends
   com.percussion.client.proxies.impl.PSResourceFileHierarchyModelProxy
{

   /**
    * Just calls parent constructor.
    * 
    * @throws PSUninitializedConnectionException
    */
   public PSResourceFileHierarchyModelProxy()
      throws PSUninitializedConnectionException
   {
      super();
   }

   /**
    * Override to exclude the applications that are NOT empty or part of the
    * exclude application list.
    * 
    * @see com.percussion.client.proxies.impl.PSXmlApplicationFileHierarchyModelProxy#getChildren(com.percussion.client.proxies.IPSHierarchyModelProxy.NodeId)
    */
   @Override
   public IPSHierarchyNodeRef[] getChildren(NodeId parentId)
      throws PSModelException
   {
      IPSHierarchyNodeRef[] refs = super.getChildren(parentId);
      List<IPSHierarchyNodeRef> res = new ArrayList<IPSHierarchyNodeRef>();
      for (IPSHierarchyNodeRef ref : refs)
      {
         // Hide if the application is from the exclude list and also if it is
         // empty.
         if (ref instanceof PSXmlApplicationFileHierarchyRootRef)
         {
            PSXmlApplicationFileHierarchyRootRef nRef = (PSXmlApplicationFileHierarchyRootRef) ref;
            PSApplication app = nRef.getApplication();
            if (app.isEmpty()
               && !com.percussion.client.proxies.impl.PSResourceFileHierarchyModelProxy.EXCLUDE_APP_SET
                  .contains(app.getName()))
            {
               res.add(ref);
            }
         }
         else
            res.add(ref);
      }
      return res.toArray(new IPSHierarchyNodeRef[0]);
   }
}
