/******************************************************************************
 *
 * [ PSHierarchyManager.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.models.impl;

import com.percussion.client.*;
import com.percussion.client.models.IPSHierarchyManager;
import com.percussion.client.models.impl.PSCmsModel.IProxyWrapper;
import com.percussion.client.proxies.IPSHierarchyModelProxy;
import com.percussion.client.proxies.IPSHierarchyModelProxy.NodeId;
import com.percussion.client.proxies.impl.PSHierarchyNodeRefHiddenParent;
import com.percussion.utils.guid.IPSGuid;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * Implements the interface by using the connection layer to perform its work.
 * All non-object specific work is done here. Any object-specific work is
 * handled in the lower layers.
 * 
 * @version 6.0
 * @author Paul Howard
 */
public class PSHierarchyManager implements IPSHierarchyManager
{
   /**
    * Create a manager to process a tree.
    * 
    * @param parent The model that is associated with this manger. All nodes are
    * stored in this model.
    * 
    * @param treeName The tree fragment with which this manager is associated.
    * It is used to catalog children when a <code>null</code> parent is
    * supplied. Never <code>null</code> or empty. It must match one of the
    * tree names returned by
    * {@link com.percussion.client.models.IPSCmsModel#getHierarchyTreeNames()}.
    * 
    * @throws PSModelException If the proxy for this type is not found for any
    * reason.
    */
   public PSHierarchyManager(PSCmsModel parent, String treeName)
      throws PSModelException
   {
      if ( null == parent)
      {
         throw new IllegalArgumentException("parent cannot be null");  
      }

      if (StringUtils.isBlank(treeName))
      {
         throw new IllegalArgumentException("treeName cannot be null or empty");  
      }
      
      try
      {
         m_model = parent;
         // check that a proxy exists
         IPSHierarchyModelProxy proxy = getProxy();
         Collection<String> treeNames = proxy.getRoots();
         boolean found = false;
         for (String name : treeNames)
         {
            if (name.equalsIgnoreCase(treeName)) {
               found = true;
               break;
            }
         }
         if (!found)
         {
            throw new IllegalArgumentException(
                  "unknown tree for this hierarchy manager: " + treeName);
         }
         m_treeName = treeName;
      }
      catch (RuntimeException e)
      {
         Throwable cause = e.getCause();
         if (cause instanceof PSModelException)
            throw (PSModelException) cause;
         throw e;
      }
   }

   // see interface
   public IPSHierarchyNodeRef[] cloneChildren(
         IPSHierarchyNodeRef targetParent, IPSHierarchyNodeRef[] sources,
         String[] names)
      throws PSMultiOperationException, PSModelException
   {
      if (null == sources)
      {
         throw new IllegalArgumentException("sources cannot be null");  
      }
      if (names != null && names.length != sources.length)
      {
         throw new IllegalArgumentException(
               "names must have same length as sources if not null");  
      }
         
      
      for (IPSHierarchyNodeRef source : sources)
      {
         if (null == source)
            throw new IllegalArgumentException("no nulls in sources");
         m_model.checkObjectType(source.getObjectType(), true);
      }
      
      Set<String> usedNames = new HashSet<>();
      try
      {
         Collection<IPSHierarchyNodeRef> currentChildren = 
            getChildren(targetParent);
         for (IPSHierarchyNodeRef ref : currentChildren)
         {
            usedNames.add(ref.getName().toLowerCase());
         }
      }
      catch (PSModelException e1)
      {
         //ignore, just let the proxy level deal with it
      }
      
      boolean error = false;
      List<IPSHierarchyNodeRef> availableRefs = 
         new ArrayList<IPSHierarchyNodeRef>();
      /* The refs are stored in results[0], the data are stored in results[1].
       * Each ref can either be an exception or a valid ref, each data can 
       * be null (for exceptions) or a valid data object.
       */
      Object[][] results = new Object[2][sources.length];
      final int REFS = 0, DATA = 1;
      int i = 0;
      for (IPSHierarchyNodeRef ref : sources)
      {
         String name = ref.getName();
         if (names != null && names[i] != null)
            name = names[i];
         if (usedNames.contains(name.toLowerCase()))
         {
            results[REFS][i] = new PSDuplicateNameException(name,
                  ref.getObjectType());
            error = true;
         }
         else
            availableRefs.add(ref);
         usedNames.add(name.toLowerCase());
         i++;
      }

      Object[] createdData = new Object[availableRefs.size()]; 
      if (availableRefs.isEmpty())
      {
         throw new PSMultiOperationException(results[REFS], names);
      }
      
      try
      {
         IPSHierarchyNodeRef[] createdRefs = getProxy().createChildrenFrom(
               createId(targetParent),
               availableRefs.toArray(new IPSHierarchyNodeRef[availableRefs
                     .size()]), names, createdData);
         mergeResults(results[REFS], results[DATA], createdRefs, createdData);
      }
      catch (PSMultiOperationException e)
      {
         error = true;
         mergeResults(results[REFS], results[DATA], e.getResults(), createdData);
      }
      
      fixupRefs(results[REFS]);

      try
      {
         saveObjects(results[REFS], results[DATA], false);
      }
      catch (PSMultiOperationException e)
      {
         error = true;
         for (int j=0; j < e.getResults().length; j++)
         {
            if (e.getResults()[j] instanceof Throwable)
            {
               results[REFS][j] = e.getResults()[j];
               results[DATA][j] = null;
            }
         }
      }

      Collection<IPSHierarchyNodeRef> validRefs = 
         new ArrayList<IPSHierarchyNodeRef>();
      Collection<Object> validData = new ArrayList<Object>();
      for (int j=0; j < results[REFS].length; j++)
      {
         if (results[REFS][j] instanceof IPSHierarchyNodeRef)
         {
            validRefs.add((IPSHierarchyNodeRef) results[REFS][j]);
            validData.add(results[DATA][j]);
         }
      }
      
      IPSHierarchyNodeRef[] validRefsArray = 
         validRefs.toArray(new IPSHierarchyNodeRef[validRefs.size()]);
      m_model.objectsCloned(validRefsArray, Arrays.asList(results[DATA]));

      if (error)
         throw new PSMultiOperationException(results[REFS], names);
      
      assert(validRefs.size() == results[REFS].length);
      return validRefsArray;
   }

   // see interface
   public IPSHierarchyNodeRef[] createChildren(
         IPSHierarchyNodeRef targetParent, PSObjectType type,
         List<String> names) 
      throws PSMultiOperationException, PSModelException
   {
      m_model.checkObjectType(type, true);
      if (null == names || names.size() == 0)
         return new IPSHierarchyNodeRef[0];
      
      Set<String> usedNames = new HashSet<String>();
      try
      {
         Collection<IPSHierarchyNodeRef> currentChildren = 
            getChildren(targetParent);
         for (IPSHierarchyNodeRef ref : currentChildren)
         {
            usedNames.add(ref.getName().toLowerCase());
         }
      }
      catch (PSModelException e1)
      {
         //ignore, just let the proxy level deal with it
      }
      
      boolean error = false;
      List<String> desiredNames = new ArrayList<String>();
      /* The refs are stored in results[0], the data are stored in results[1].
       * Each ref can either be an exception or a valid ref, each data can 
       * be null (for exceptions) or a valid data object.
       */
      Object[][] results = new Object[2][names.size()];
      final int REFS = 0, DATA = 1;
      int i = 0;
      for (String name : names)
      {
         if (usedNames.contains(name.toLowerCase()))
         {
            results[REFS][i] = new PSDuplicateNameException(name, type);
            error = true;
         }
         else
            desiredNames.add(name);
         usedNames.add(name.toLowerCase());
         i++;
      }

      Object[] createdData = new Object[desiredNames.size()]; 
      try
      {
         IPSHierarchyNodeRef[] createdRefs = getProxy().createChildren(
               createId(targetParent), type, desiredNames, createdData);
         mergeResults(results[REFS], results[DATA], createdRefs, createdData);
      }
      catch (PSMultiOperationException e)
      {
         error = true;
         mergeResults(results[REFS], results[DATA], e.getResults(), createdData);
      }
      
      fixupRefs(results[REFS]);

      try
      {
         saveObjects(results[REFS], results[DATA], true);
      }
      catch (PSMultiOperationException e)
      {
         error = true;
         for (int j=0; j < e.getResults().length; j++)
         {
            if (e.getResults()[j] instanceof Throwable)
            {
               results[REFS][j] = e.getResults()[j];
               results[DATA][j] = null;
            }
         }
      }

      Collection<IPSHierarchyNodeRef> validRefs = 
         new ArrayList<IPSHierarchyNodeRef>();
      Collection<Object> validData = new ArrayList<Object>();
      for (int j=0; j < results[REFS].length; j++)
      {
         if (results[REFS][j] instanceof IPSHierarchyNodeRef)
         {
            validRefs.add((IPSHierarchyNodeRef) results[REFS][j]);
            validData.add(results[DATA][j]);
         }
      }
      
      IPSHierarchyNodeRef[] validRefsArray = 
         validRefs.toArray(new IPSHierarchyNodeRef[validRefs.size()]);
      m_model.objectsCreated(validRefsArray, Arrays.asList(results[DATA]));

      if (error)
         throw new PSMultiOperationException(results[REFS], names.toArray());
      
      assert(validRefs.size() == results[REFS].length);
      return validRefsArray;
   }

   /**
    * Merges the data from the created arrays into the result arrays. The 
    * merging is done by processing the result arrays and for each entry, if
    * it is <code>null</code>, a value is copied from the next entry of the 
    * created array, otherwise it is skipped.
    * 
    * @param resultRefs Assumed not <code>null</code>.
    * @param resultData Assumed not <code>null</code>.
    * @param createdRefs Assumed not <code>null</code>.
    * @param createdData  Assumed not <code>null</code>.
    */
   private void mergeResults(Object[] resultRefs, Object[] resultData,
         Object[] createdRefs, Object[] createdData)
   {
      if (createdRefs.length == 0)
         return;
      for (int i=0, j=0; i < resultData.length; i++)
      {
         if (resultRefs[i] == null)
         {
            resultRefs[i] = createdRefs[j];
            resultData[i] = createdData[j];
            j++;
         }
      }
   }

   /**
    * Walks the list of results and saves valid objects based on the supplied
    * flag.
    * 
    * @param refs Assumed not <code>null</code>. Entries are either
    * {@link IPSHierarchyNodeRef} or exception.
    * 
    * @param data A corresponding data object for each ref.
    * 
    * @param foldersOnly If <code>true</code>, only containers are saved,
    * otherwise, all objects are saved.
    * 
    * @throws PSMultiOperationException If any problems communicating with the
    * server. The results will contain exceptions (failure) or valid refs (not
    * saved or saved successfully.)
    */
   private void saveObjects(Object[] refs, Object[] data, boolean foldersOnly)
      throws PSMultiOperationException, PSModelException
   {
      try
      {
         Collection<IPSReference> folderRefs = new ArrayList<IPSReference>(); 
         Collection<Object> folderData = new ArrayList<Object>(); 
         int i=0;
         for (Object o : refs)
         {
            if (((o instanceof IPSHierarchyNodeRef)
                  && ((IPSHierarchyNodeRef) o).isContainer()) 
                  || (!foldersOnly && !(o instanceof Throwable)))
            {
               folderRefs.add((IPSReference) o);
               folderData.add(data[i]);
            }
            i++;
         }
         if (folderRefs.isEmpty())
            return;
         //go directly to proxy, don't use our model layer
         m_model.getProxy().save(
               folderRefs.toArray(new IPSReference[folderRefs.size()]),
               folderData.toArray(), true);
      }
      catch (PSMultiOperationException e)
      {
         Object[] caughtResults = e.getResults();
         Object[] errorResults = new Object[refs.length];
         for (int i = 0, j=0; i < refs.length; i++)
         {
            if ((refs[i] instanceof IPSHierarchyNodeRef)
                  && ((IPSHierarchyNodeRef) refs[i]).isContainer())
            {
               if (caughtResults[j] != null)
                  errorResults[i] = caughtResults[j];
               else
                  caughtResults[i] = refs[i];
               j++;
            }
            else
               errorResults[i] = refs[i];
         }
         throw new PSMultiOperationException(errorResults, refs);
      }
   }

   /**
    * Builds an appropriate id depending on whether the supplied ref is
    * <code>null</code>.
    * 
    * @param ref May be <code>null</code>.
    * 
    * @return An id that contains either the tree name for this manager (if the
    * target parent is <code>null</code>) or the target parent. Never
    * <code>null</code>.
    */
   private NodeId createId(IPSHierarchyNodeRef ref)
   {
      return ref == null ? new NodeId(m_treeName) : new NodeId(ref); 
   }

   // see interface
   public void removeChildren(List<IPSHierarchyNodeRef> children)
      throws PSMultiOperationException, PSModelException
   {
      if (children == null || children.size() == 0)
         return;
      
      try
      {
         List<IPSHierarchyNodeRef> delete = new ArrayList<IPSHierarchyNodeRef>();
         for (int i = 0; i < children.size(); i++)
         {
            IPSHierarchyNodeRef hRef = children.get(i);
            if (hRef.isPersisted())
               delete.add(hRef);
         }
         if (!delete.isEmpty())
         {
            IPSHierarchyNodeRef[] childRefs = delete
               .toArray(new IPSHierarchyNodeRef[delete.size()]);
            getProxy().removeChildren(childRefs);
         }
         m_model.objectsDeleted(children
            .toArray(new IPSHierarchyNodeRef[children.size()]));
      }
      catch (PSMultiOperationException e)
      {
         Object[] resultRefs = e.getResults();
         assert( children.size() == resultRefs.length);
         IPSReference[] goodRefs = new IPSReference[resultRefs.length];
         int i=0;
         for (Object o : resultRefs)
         {
            if (o instanceof IPSReference)
               goodRefs[i] = (IPSReference) o;
            i++;
         }
         m_model.objectsDeleted(goodRefs);
         throw e;
      }
   }

   // see interface
   public void moveChildren(List<IPSHierarchyNodeRef> sourceChildren, 
         IPSHierarchyNodeRef targetParent) 
      throws PSMultiOperationException, PSModelException
   {
      if (sourceChildren == null || sourceChildren.size() == 0)
         return;
      
      assureAllHaveSameParent(sourceChildren);
      assureNotMovedIntoItself(sourceChildren, targetParent);
      //fixme assuming all children have same parent
      IPSHierarchyNodeRef sourceParent = sourceChildren.get(0).getParent();
      if ((sourceParent == null && targetParent == null)
            || (sourceParent != null && sourceParent.equals(targetParent)))
      {
         return;
      }
      Set<String> usedNames = new HashSet<String>();
      try
      {
         Collection<IPSHierarchyNodeRef> currentChildren = 
            getChildren(targetParent);
         for (IPSHierarchyNodeRef ref : currentChildren)
         {
            usedNames.add(ref.getName().toLowerCase());
         }
      }
      catch (PSModelException e1)
      {
         //ignore, just let the proxy level deal with it
      }

      Map<IPSGuid, Object> resultMap = new HashMap<IPSGuid, Object>(
         sourceChildren.size());
      List<IPSHierarchyNodeRef> goodList =new ArrayList<IPSHierarchyNodeRef>();
      for (IPSHierarchyNodeRef ref : sourceChildren)
      {
         if(usedNames.contains(ref.getName().toLowerCase()))
         {
            resultMap.put(ref.getId(), new PSDuplicateNameException(ref
               .getName(), ref.getObjectType()));
         }
         else
         {
            resultMap.put(ref.getId(), null);
            goodList.add(ref);
         }
      }
      if (goodList.size() > 0)
      {
         try
         {
            getProxy().moveChildren(
               goodList.toArray(new IPSHierarchyNodeRef[goodList.size()]),
               createId(targetParent));
            m_model
               .objectsMoved(sourceParent, goodList
                  .toArray(new IPSHierarchyNodeRef[goodList.size()]),
                  targetParent);
         }
         catch (PSMultiOperationException e)
         {
            Object[] resultRefs = e.getResults();
            List<IPSHierarchyNodeRef> goodRefs = new ArrayList<IPSHierarchyNodeRef>();
            for (int i = 0; i < resultRefs.length; i++)
            {
               Object o = resultRefs[i];
               if (o == null)
               {
                  goodRefs.add(sourceChildren.get(i));
               }
               resultMap.put(goodList.get(i).getId(), o);
            }
            if (goodRefs.size() > 0)
            {
               m_model.objectsMoved(sourceParent, goodRefs
                  .toArray(new IPSHierarchyNodeRef[0]), targetParent);
            }
         }
      }
      Object[] result = new Object[sourceChildren.size()];
      boolean error = false;
      for (int i = 0; i < result.length; i++)
      {
         IPSHierarchyNodeRef ref = sourceChildren.get(i);
         Object res = resultMap.get(ref.getId());
         if(res !=null)
            error = true;
         result[i] = res;
      }
      if(error)
      {
         throw new PSMultiOperationException(result);
      }
   }

   /**
    * Makes sure all the nodes have the same parent.
    * @throws PSModelException if the provided nodes do not have the
    * same parent.
    */
   private void assureAllHaveSameParent(List<IPSHierarchyNodeRef> nodes)
         throws PSModelException
   {
      if (nodes.size() > 0)
      {
         IPSHierarchyNodeRef currentParent = null;
         
         if (nodes.get(0) instanceof PSHierarchyNodeRefHiddenParent)
            currentParent = ((PSHierarchyNodeRefHiddenParent) nodes.get(0))
               .getHiddenParent();
         else
            currentParent = nodes.get(0).getParent();
         for (final IPSHierarchyNodeRef ref : nodes)
         {
            IPSHierarchyNodeRef parent = null;
            if(ref instanceof PSHierarchyNodeRefHiddenParent)
               parent = ((PSHierarchyNodeRefHiddenParent)ref).getHiddenParent();
            else
               parent = ref.getParent();
            if (parent != currentParent)
            {
               final Object[] errorArgs = new Object[2];
               errorArgs[0] = currentParent.getName();
               errorArgs[1] = parent.getName();
               
               throw new PSModelException(PSErrorCodes.NOT_SAME_PARENT,
                     errorArgs);
            }
         }
      }
   }

   /**
    * Makes sure targetParent is not under any of source nodes.
    * @throws IllegalArgumentException if any of source nodes is or is above
    * provided parent in the nodes hierarchy.  
    */
   private void assureNotMovedIntoItself(List<IPSHierarchyNodeRef> sourceChildren,
         IPSHierarchyNodeRef targetParent)
         throws PSModelException
   {
      final Set<IPSHierarchyNodeRef> parents = new HashSet<IPSHierarchyNodeRef>();
      {
         IPSHierarchyNodeRef ref = targetParent;
         while (ref != null)
         {
            parents.add(ref);
            ref = ref.getParent();
         }
      }
      for (final IPSHierarchyNodeRef child : sourceChildren)
      {
         if (parents.contains(child))
         {
            final Object[] errorArgs = new Object[2];
            errorArgs[0] = child.getName();
            errorArgs[1] = targetParent.getName();
            
            throw new PSModelException(PSErrorCodes.CANT_MOVE_NODE_UNDER_ITSELF,
                  errorArgs);
         }
      }
   }

   //see interface
   public IPSHierarchyNodeRef getReference(IPSGuid id) throws PSModelException
   {
      return doGetReference(null, id);
   }
   
   /**
    * Implements {@link #getReference(IPSGuid)} using recursion. Uses breadth-
    * first searching at each level to minimize cataloging.
    * 
    * @param parent May be <code>null</code>.
    * 
    * @param id Assumed not <code>null</code>.
    * 
    * @return The reference that matches the supplied id. May be
    * <code>null</code>.
    * 
    * @throws PSModelException If any problems communicating with the server.
    */
   public IPSHierarchyNodeRef doGetReference(IPSHierarchyNodeRef parent,
         IPSGuid id) throws PSModelException
   {
      IPSHierarchyNodeRef result = null;
      Collection<IPSHierarchyNodeRef> children = getChildren(parent);
      Collection<IPSHierarchyNodeRef> folders = 
         new ArrayList<IPSHierarchyNodeRef>();
      for (IPSHierarchyNodeRef node : children)
      {
         if (node.getId() != null && node.getId().equals(id))
            return node;
         if (node.isContainer())
         {
            folders.add(node);
         }
      }
      
      for (IPSHierarchyNodeRef folder : folders)
      {
         result = doGetReference(folder, id);
         if (result != null)
            return result;
      }
      
      return null;
   }

   // see interface
   public Collection<IPSHierarchyNodeRef> getChildren(
         IPSHierarchyNodeRef parent)
      throws PSModelException
   {
      return getChildren(parent, false);
   }

   // see interface
   public Collection<IPSHierarchyNodeRef> getChildren(
         IPSHierarchyNodeRef parent, boolean forceRefresh)
      throws PSModelException
   {
      if (parent != null && !parent.isContainer())
         return new ArrayList<IPSHierarchyNodeRef>();
      
      if (forceRefresh)
         m_model.flush(parent);
      
      Collection<IPSReference> refs = m_model.doCatalog(parent, null,
         new IProxyWrapper()
         {
            public Collection<IPSReference> catalog(IPSReference parentRef)
               throws PSModelException
            {
               return getRefsFromModel(parentRef);
            }
            
            /**
             * Returns the name supplied in the outer class' ctor.
             */
            public String getTreeName()
            {
               return m_treeName;
            }
         });
      Collection<IPSHierarchyNodeRef> results = 
         new ArrayList<IPSHierarchyNodeRef>();
      for (IPSReference ref : refs)
      {
         results.add((IPSHierarchyNodeRef) ref);
      }
      return results;
   }

   /**
    * Calls the proxy to get the children and sets this mgr onto the returned
    * refs. 
    * 
    * @param parent May be <code>null</code>, in which case the root children
    * of the tree supplied in the ctor are returned.
    * 
    * @return Never <code>null</code>, may be empty.
    * 
    * @throws PSModelException If any problems communicating with the server.
    */
   private Collection<IPSReference> getRefsFromModel(IPSReference parent)
      throws PSModelException
   {
      final IPSHierarchyNodeRef[] refs;
      if (ms_logger.isDebugEnabled())
         ms_logger.debug("Calling proxy to get children for " + parent);
      refs = getProxy().getChildren(createId((IPSHierarchyNodeRef) parent));
      fixupRefs(refs);
      return new ArrayList<IPSReference>(Arrays.asList(refs));
   }
   
   // see interface
   public IPSHierarchyNodeRef getParent(IPSHierarchyNodeRef child)
   {
      if ( null == child)
      {
         throw new IllegalArgumentException("child cannot be null");  
      }
      return child.getParent();
   }

   // see interface
   public String getPath(IPSHierarchyNodeRef node)
   {
      if ( null == node)
      {
         throw new IllegalArgumentException("node cannot be null");  
      }
      return node.getPath();
   }

   // see interface
   public IPSHierarchyNodeRef getByPath(String path)
      throws PSModelException
   {
      StringTokenizer toker = new StringTokenizer(path, "/");
      IPSHierarchyNodeRef child = null;
      IPSHierarchyNodeRef parent = null;
      while (toker.hasMoreTokens() && child == null)
      {
         child = findChild(parent, toker.nextToken());
         if (toker.hasMoreTokens())
         {
            parent = child;
            child = null;
         }
      }
      
      return child;
   }

   /**
    * {@inheritDoc}
    * Returns the name supplied in the ctor.
    */
   public String getTreeName()
   {
      return m_treeName;
   }

   private IPSHierarchyNodeRef findChild(IPSHierarchyNodeRef parent, 
         String name)
      throws PSModelException
   {
      IPSHierarchyNodeRef result = null;
      Collection<IPSHierarchyNodeRef> children = getChildren(parent);
      for (IPSHierarchyNodeRef ref : children)
      {
         if (ref.getName().equalsIgnoreCase(name))
            result = ref;
      }
      return result;
   }

   /**
    * Sets this manager as the manager for all nodes in the supplied array.
    * 
    * @param refs Assumed not <code>null</code>. Sets the this as the manager
    * on every entry that is an <code>PSHierarchyNodeRef</code>.
    */
   private void fixupRefs(Object[] refs)
   {
      for (Object o : refs)
      {
         if (!(o instanceof IPSHierarchyNodeRef))
            continue;
         
         IPSHierarchyNodeRef ref = (IPSHierarchyNodeRef) o;

         if (ref.getManager() != null)
            assert(ref.getManager() == this);
         else
            ref.setManager(this);
      }      
   }

   /**
    * Gets the proxy for this manager from the core factory.
    * 
    * @return Never <code>null</code>.
    */
   private IPSHierarchyModelProxy getProxy()
   {
      try
      {
         return PSCoreFactory.getInstance().getHierarchyModelProxy(
               m_model.getObjectTypes().iterator().next().getPrimaryType());
      }
      catch (PSModelException e)
      {
         //this should be very rare
         throw new RuntimeException(e);
      }
   }

   /**
    * The model that owns this manager. All caching and such is done using 
    * this model. Never <code>null</code>.
    */
   private final PSCmsModel m_model;

   /**
    * The name of the tree fragment to which this manager is associated. Never
    * empty after ctor. Used to get the root nodes when a catalog is requested
    * with a <code>null</code> parent.
    */
   private final String m_treeName;
   
   /**
    * The logging target for all instances of this class. Never
    * <code>null</code>.
    */
   private static Logger ms_logger = LogManager.getLogger(PSHierarchyManager.class);
}
