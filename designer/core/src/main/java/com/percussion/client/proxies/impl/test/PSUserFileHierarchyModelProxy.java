/******************************************************************************
 *
 * [ PSUserFileHierarchyModelProxy.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.proxies.impl.test;

import com.percussion.client.IPSHierarchyNodeRef;
import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSDuplicateNameException;
import com.percussion.client.PSErrorCodes;
import com.percussion.client.PSModelException;
import com.percussion.client.PSMultiOperationException;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.impl.PSHierarchyNodeRef;
import com.percussion.client.impl.PSReference;
import com.percussion.client.models.PSLockException;
import com.percussion.client.proxies.IPSHierarchyModelProxy;
import com.percussion.services.ui.data.PSHierarchyNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Provides hierarchy management services for the object type
 * {@link PSObjectTypes#USER_FILE}. Uses base class implementation whenever
 * possible and does type specific work here.
 * 
 * @see com.percussion.client.proxies.impl.PSHierarchyModelProxy
 * 
 * @version 6.0
 * @created 03-Sep-2005 4:39:27 PM
 */
public class PSUserFileHierarchyModelProxy implements IPSHierarchyModelProxy
{
   /**
    * The default implemenation throws an unsupported op exception.
    */
   public IPSHierarchyNodeRef[] createChildrenFrom(NodeId targetParent,
         IPSHierarchyNodeRef[] children, String[] names, Object[] results)
      throws PSMultiOperationException
   {
      throw new UnsupportedOperationException("cloning children not supported");
   }

   //see interface
   public IPSHierarchyNodeRef[] createChildren(
         NodeId parentId, PSObjectType type,
         List<String> names, Object[] results) throws PSMultiOperationException
   {
      if (parentId == null)
      {
         throw new IllegalArgumentException("Target Parent is NULL");
      }

      if (names.size() < results.length)
      {
         throw new IllegalArgumentException("Results Array to small");
      }

      if (parentId.getNodeRef() != null && parentId.getNodeRef().isContainer()
            && !parentId.getNodeRef().isPersisted())
      {
         throw new IllegalStateException(
               "Can't add children to a folder until it has been persisted.");
      }
      
      if (names == null || names.size() == 0)
      {
         return (new IPSHierarchyNodeRef[0]);
      }
      
      IPSHierarchyNodeRef targetParent;
      boolean isParentHiddenRoot = parentId.isNameBased(); 
      if (isParentHiddenRoot)
      {
         try
         {
            targetParent = getRootNode(parentId.getTreeName());
            if (targetParent == null)
            {
               throw new IllegalArgumentException("unknown tree name: "
                     + parentId.getTreeName());
            }
         }
         catch (PSModelException e)
         {
            //shouldn't happen
            throw new RuntimeException(e);
         }
      }
      else
      {
         targetParent = parentId.getNodeRef();
      }
      
      IPSHierarchyNodeRef[] refs = new IPSHierarchyNodeRef[names.size()];
      Object[] errorResults = new Object[names.size()];

      PSUserFileModelProxy proxy = getProxy();

      //build list of names to check for dupe names
      Set<String> existingNames = new HashSet<String>();
      try
      {
         Collection<IPSHierarchyNodeRef> tmp = 
            new ArrayList<IPSHierarchyNodeRef>();
         if (isParentHiddenRoot)
         {
            /* this can't go thru the cache or it would make the hidden node
             * visible
             */ 
            tmp.addAll(Arrays.asList(getChildren(parentId)));
         }
         else
         {
            //this call will get unsaved objects from the cache
            tmp.addAll(targetParent.getChildren());
         }
         for (IPSHierarchyNodeRef ref : tmp)
         {
            existingNames.add(ref.getName().toLowerCase());
         }
      }
      catch (PSModelException e1)
      {
         throw new RuntimeException(e1);
      }
      
      boolean failureOccurred = false;
      for (int inc = 0; inc < names.size(); ++inc)
      {
         try
         {
            if (existingNames.contains(names.get(inc).toLowerCase()))
            {
               throw new PSDuplicateNameException(names.get(inc), type);
            }
            existingNames.add(names.get(inc).toLowerCase());
            PSHierarchyNode[] result = new PSHierarchyNode[1];
            results[inc] = refs[inc] = proxy.createNew(targetParent, names
                  .get(inc), type, result);
            
            if (isParentHiddenRoot)
               ((PSHierarchyNodeRef) refs[inc]).setParent(null);
            results[inc] = result[0];
            errorResults[inc] = refs[inc];
         }
         catch (Exception e)
         {
            failureOccurred = true;
            results[inc] = refs[inc] = null;
            errorResults[inc] = e;
         }
      }

      if (failureOccurred)
      {
         throw new PSMultiOperationException(errorResults);
      }

      return (refs);
   }

   /**
    * See
    * {@link com.percussion.client.proxies.IPSHierarchyModelProxy#removeChildren(IPSHierarchyNodeRef[])}
    * for details.
    */
   public void removeChildren(IPSHierarchyNodeRef[] children)
         throws PSMultiOperationException
   {
      if (children == null || children.length == 0)
      {
         return;
      }

      Object[] results = new Object[children.length];
      Collection<IPSHierarchyNodeRef> deleteItems = new ArrayList<IPSHierarchyNodeRef>();

      PSUserFileModelProxy proxy = getProxy();

      boolean hadError = false;
      for (int inc = 0; inc < children.length; ++inc)
      {
         if (children[inc].isContainer())
         {
            try
            {
               Collection<IPSHierarchyNodeRef> grandChildren = children[inc]
                     .getChildren();

               try
               {
                  removeChildren(grandChildren
                        .toArray(new IPSHierarchyNodeRef[grandChildren.size()]));
                  deleteItems.add(children[inc]);
               }
               catch (PSMultiOperationException e)
               {
                  hadError = true;
                  Collection<Object> grandChildResults = new ArrayList<Object>();

                  for (int inc2 = 0; inc2 < e.getResults().length; ++inc2)
                  {
                     if (e.getResults()[inc2] != null)
                     {
                        Object[] exceptions = (Object[]) e.getResults()[inc2];

                        for (int inc3 = 0; inc3 < exceptions.length; ++inc3)
                        {
                           grandChildResults.add(exceptions[inc3]);
                        }
                     }
                  }
                  results[inc] = grandChildResults.toArray();
               }
            }
            catch (PSModelException e)
            {
               throw new RuntimeException(e);
            }
         }
         else
         {
            deleteItems.add(children[inc]);
            results[inc] = null;
         }
      }

      if (deleteItems.size() > 0)
      {
         try
         {
            proxy.delete(deleteItems
                  .toArray(new IPSHierarchyNodeRef[deleteItems.size()]));
         }
         catch (PSMultiOperationException e)
         {
            int inc2 = 0;
            for (int inc = 0; inc < results.length; ++inc)
            {
               if (results[inc] == null)
               {
                  if (e.getResults()[inc2] != null)
                  {
                     Object[] childError = new Object[1];
                     childError[0] = e.getResults()[inc2];
                     results[inc] = childError;
                  }
                  ++inc2;
               }
            }
            hadError = true;
         }
      }

      if (hadError)
      {
         throw new PSMultiOperationException(results);
      }

   }

   /**
    * See
    * {@link com.percussion.client.proxies.IPSHierarchyModelProxy#moveChildren(IPSHierarchyNodeRef[], NodeId)}
    * for details.
    */
   public void moveChildren(IPSHierarchyNodeRef[] sourceChildren,
         NodeId targetParentId) throws PSMultiOperationException,
         PSModelException
   {
      try
      {
         IPSHierarchyNodeRef targetParent;
         boolean isParentHiddenRoot = targetParentId.isNameBased(); 
         if (isParentHiddenRoot)
         {
            try
            {
               targetParent = getRootNode(targetParentId.getTreeName());
               if (targetParent == null)
               {
                  throw new IllegalArgumentException("unknown tree name: "
                        + targetParentId.getTreeName());
               }
            }
            catch (PSModelException e)
            {
               //shouldn't happen
               throw new RuntimeException(e);
            }
         }
         else
         {
            targetParent = targetParentId.getNodeRef();
         }
         
         IPSHierarchyNodeRef sourceParent = null;

         if (sourceChildren == null || sourceChildren.length == 0)
         {
            return;
         }

         if (targetParent == null)
         {
            throw new IllegalArgumentException("targetParentId cannot be resolved to a valid node");
         }

         boolean sameParent = false;

         int inc = 0;
         for (IPSHierarchyNodeRef child : sourceChildren)
         {
            if (child == null)
            {
               throw new IllegalArgumentException("sourceChilden[" + inc
                     + "] is null");
            }

            if (inc == 0)
            {
               sourceParent = child.getParent();

               if (sourceParent == targetParent)
               {
                  sameParent = true;
               }
            }
            else if (child.getParent() != sourceParent)
            {
               Object args[] =
                  {sourceParent.getName(), child.getParent().getName()};
               throw new PSModelException(PSErrorCodes.NOT_SAME_PARENT, args);
            }

            if (!sameParent)
            {
               if (containsNode(child, targetParent))
               {
                  Object args[] =
                  {child.getName(), targetParent.getName()};
                  throw new PSModelException(
                        PSErrorCodes.CANT_MOVE_NODE_UNDER_ITSELF, args);
               }
            }
            ++inc;
         }

         if (sameParent)
         {
            return;
         }

         boolean haveError = false;
         Object[] results = new Object[sourceChildren.length];
         PSUserFileModelProxy flatProxy = getProxy();

         //build list of names to check for dupe names
         Set<String> existingNames = new HashSet<String>();
         Collection<IPSHierarchyNodeRef> tmp = 
            new ArrayList<IPSHierarchyNodeRef>();
         if (isParentHiddenRoot)
         {
            /* this can't go thru the cache or it would make the hidden node
             * visible
             */ 
            tmp.addAll(Arrays.asList(getChildren(targetParentId)));
         }
         else
         {
            //this call will get unsaved objects from the cache
            tmp.addAll(targetParent.getChildren());
         }
         for (IPSHierarchyNodeRef ref : tmp)
         {
            existingNames.add(ref.getName().toLowerCase());
         }
         
         inc = 0;
         for (IPSHierarchyNodeRef child : sourceChildren)
         {
            if (existingNames.contains(child.getName().toLowerCase()))
            {
               haveError = true;
               results[inc] = new PSDuplicateNameException(child.getName(), child
                     .getObjectType());
               continue;
            }
            if (!flatProxy.isLocked(child))
            {
               PSHierarchyNode node = (PSHierarchyNode) (flatProxy.load(
                     new IPSReference[] { child }, true, false))[0];
               node.setParentId(targetParent.getId());
               ((PSHierarchyNodeRef) child).setParent(
                     isParentHiddenRoot ? null : targetParent);
               flatProxy.save(new IPSReference[] { child },
                     new Object[] { node }, true);
               results[inc] = null;
            }
            else
            {
               results[inc] = new PSLockException("move", child
                     .getObjectType().toString(), child.getName());
               haveError = true;
            }
         }

         flatProxy.save();
         
         if (haveError)
         {
            throw new PSMultiOperationException(results);
         }
      }
      catch (PSProxyTestException e)
      {
         throw new PSModelException(e);
      }
   }

   /**
    * Helper method which recursively checks to see if any children contains the
    * node passed in
    */
   boolean containsNode(IPSHierarchyNodeRef node,
         IPSHierarchyNodeRef targetParent)
   {
      try
      {
         Collection<IPSHierarchyNodeRef> children = node.getChildren();

         for (IPSHierarchyNodeRef child : children)
         {
            if (child.getName().equals(targetParent.getName()))
            {
               return (true);
            }
            if (child.isContainer())
            {
               if (containsNode(child, targetParent))
               {
                  return (true);
               }
            }
         }
      }
      catch (PSModelException e)
      {
         throw new RuntimeException(e);
      }

      return (false);
   }

   /**
    * Scans the node and returns all placeholder type nodes that are children,
    * processing recursively.
    * 
    * @param key The node to start from. Never <code>null</code>.
    * 
    * @param results All placeholder nodes are added to this collection. It 
    * should be empty on first call.
    * 
    * @throws PSModelException If any problems.
    */
   public void getDescendentPlaceholders(NodeId key, 
         Collection<IPSHierarchyNodeRef> results)
      throws PSModelException
   {
      if (key == null)
      {
         throw new IllegalArgumentException("key cannot be null");  
      }
      if (null == results)
      {
         throw new IllegalArgumentException("results cannot be null");  
      }
      
      IPSHierarchyNodeRef[] children = getChildren(key);
      for (IPSHierarchyNodeRef ref : children)
      {
         if (ref.isContainer())
            getDescendentPlaceholders(new NodeId(ref), results);
         else
            results.add(ref);
      }
   }
   
   /**
    * See
    * {@link com.percussion.client.proxies.IPSHierarchyModelProxy#getChildren(NodeId)}
    * for details.
    */
   public IPSHierarchyNodeRef[] getChildren(NodeId parent)
         throws PSModelException
   {
      if ( null == parent)
      {
         throw new IllegalArgumentException("parent cannot be null");  
      }
      
      if (parent.isNameBased())
      {
         String treeName = parent.getTreeName();
         IPSHierarchyNodeRef root = getRootNode(treeName);
         if (root == null)
         {
            Collection<String> roots = getRoots();
            StringBuffer treeNames = new StringBuffer();
            for (String name : roots)
            {
               if (treeNames.length() > 0)
                  treeNames.append("; ");
               treeNames.append(name);
            }
            throw new PSModelException(PSErrorCodes.TREE_NAME_NOT_FOUND,
                  new Object[] { treeName, PSObjectTypes.USER_FILE.name(),
                        treeNames.toString() });
         }
         IPSHierarchyNodeRef[] children = doGetChildren(root);
         for (IPSHierarchyNodeRef child : children)
         {
            // the root node is hidden, so we need to clear it
            PSHierarchyNodeRef ref = (PSHierarchyNodeRef) child;
            ref.setParent(null);
         }
         return children;
      }
      
      return doGetChildren(parent.getNodeRef());
   }

   /**
    * Just like {@link #getChildren(NodeId)}, except a different param type and
    * <code>null</code> is allowed.
    * 
    * @param parent May be <code>null</code>.
    * 
    * @return Never <code>null</code>, may be empty.
    * 
    * @throws PSModelException If any problems reading the file used as the
    * repository.
    */
   private IPSHierarchyNodeRef[] doGetChildren(IPSHierarchyNodeRef parent)
      throws PSModelException
   {
      Collection<Object> items = getProxy().getObjects();
      Collection<Object> children = new ArrayList<Object>();
   
      for (Object o : items)
      {
         PSHierarchyNode item = (PSHierarchyNode) o;
         if (parent == null ? item.getParentId() == null
               : (item.getParentId() == null ? parent.getId() == null : item
                     .getParentId().equals(parent.getId())))
         {
            PSObjectType type = item.getType() == PSHierarchyNode.NodeType.FOLDER
                     ? com.percussion.client.proxies.impl.PSUserFileModelProxy.USERFILE_FOLDER
                     : com.percussion.client.proxies.impl.PSUserFileModelProxy.USERFILE_PLACEHOLDER;
            PSReference tmp = new PSHierarchyNodeRef(parent, item.getName(), type,
                  item.getGUID(), type.getSecondaryType().equals(
                        PSObjectTypes.UserFileSubTypes.WORKBENCH_FOLDER));
            tmp.setPersisted();
            children.add(tmp);
         }
      }
      return (children.toArray(new IPSHierarchyNodeRef[children.size()]));
   }

   /**
    * Finds a node by a specific name whose parent is <code>null</code>.
    * 
    * @param name The base name of the node to find. Assumed not
    * <code>null</code> or empty. A case-insensitive compare is done.
    * 
    * @return May be <code>null</code> if a matching node is not found.
    * 
    * @throws PSModelException If any problems reading the repository file.
    */
   public IPSHierarchyNodeRef getRootNode(String name)
      throws PSModelException
   {
      Collection<Object> items = getProxy().getObjects();
      IPSHierarchyNodeRef result = null;
      for (Object o : items)
      {
         PSHierarchyNode item = (PSHierarchyNode) o;
         if (item.getName().equalsIgnoreCase(name) && item.getParentId() == null)
         {
            PSObjectType type = item.getType() == PSHierarchyNode.NodeType.FOLDER
                     ? com.percussion.client.proxies.impl.PSUserFileModelProxy.USERFILE_FOLDER
                     : com.percussion.client.proxies.impl.PSUserFileModelProxy.USERFILE_PLACEHOLDER;
            result = new PSHierarchyNodeRef(null, item.getName(), type,
                  item.getGUID(), type.getSecondaryType().equals(
                        PSObjectTypes.UserFileSubTypes.WORKBENCH_FOLDER));
            result.setManager(PSCoreFactory.getInstance()
                  .getModel(PSObjectTypes.USER_FILE).getHierarchyManager(name));
         }
      }
      return result;
   }

   /**
    * This method will return all of the tree names known to this manager. These
    * can then be used to get the roots of a tree fragment using the
    * {@link #getChildren(NodeId)} method.
    * 
    * @return Never <code>null</code>, may be empty. An empty list does not
    *         necessarily mean there are no roots, just that they can't be
    *         determined at this time.
    * 
    * @throws PSModelException If any problems while communicating with the
    *            server.
    */
   public Collection<String> getRoots() throws PSModelException
   {
      Collection<String> roots = new ArrayList<String>(4);

      roots.add("slots");
      roots.add("contentTypes");
      roots.add("templates");
      roots.add("xmlApplications");
      roots.add("extensionsIPSJexlExpression");
      roots.add("extensionsIPSRequestPreProcessor");
      roots.add("extensionsIPSResultDocumentProcessor");
      roots.add("extensionsIPSAssemblyLocation");
      roots.add("extensionsIPSContentListGenerator");
      roots.add("extensionsIPSEffect");
      roots.add("extensionsIPSItemFilterRule");
      roots.add("extensionsIPSJexlExpression");
      roots.add("extensionsIPSPasswordFilter");
      roots.add("extensionsIPSSearchResultsProcessor");
      roots.add("extensionsIPSSlotContentFinder");
      roots.add("extensionsIPSTemplateExpander");
      roots.add("extensionsIPSUdfProcessor");
      roots.add("extensionsIPSWorkflowAction");
      roots.add("extensionsIPSLuceneAnalyzer");
      roots.add("extensionsIPSLuceneTextConverter");
      roots.add("extensionsJavaScript");

      return roots;
   }

   /**
    * Gets the {@link PSUserFileHierarchyModelProxy }.
    * 
    * @return Never <code>null</code>
    */
   PSUserFileModelProxy getProxy()
   {
      try
      {
         PSUserFileModelProxy proxy = (PSUserFileModelProxy) PSCoreFactory
               .getInstance().getCmsModelProxy(PSObjectTypes.USER_FILE);

         return (proxy);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }
}
