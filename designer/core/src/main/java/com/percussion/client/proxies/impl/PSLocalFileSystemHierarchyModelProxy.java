/******************************************************************************
 *
 * [ PSLocalFileSystemHierarchyModelProxy.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.proxies.impl;

import com.percussion.client.IPSHierarchyNodeRef;
import com.percussion.client.IPSReference;
import com.percussion.client.PSDuplicateNameException;
import com.percussion.client.PSErrorCodes;
import com.percussion.client.PSModelException;
import com.percussion.client.PSMultiOperationException;
import com.percussion.client.PSMultiOperationStrategy;
import com.percussion.client.PSObjectType;
import com.percussion.client.models.PSLockException;
import com.percussion.client.proxies.IPSCmsModelProxy;
import com.percussion.client.proxies.IPSHierarchyModelProxy;
import com.percussion.error.PSNotLockedException;
import com.percussion.services.system.data.PSMimeContentAdapter;
import org.apache.commons.collections.list.TransformedList;
import org.apache.commons.io.FileUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.percussion.client.proxies.impl.PSLocalFileSystemHierarchyNodeRef.OBJECT_TYPE_FOLDER;

/**
 * Provides access to the local file system.
 *
 * @author Andriy Palamarchuk
 */
public class PSLocalFileSystemHierarchyModelProxy implements
      IPSHierarchyModelProxy
{
   //see base
   @SuppressWarnings("unchecked")
   public IPSHierarchyNodeRef[] createChildren(NodeId targetParent,
         PSObjectType type, List<String> names, Object[] results)
      throws PSMultiOperationException
   {
      validateParent(targetParent);
      
      final PSLocalFileSystemHierarchyNodeRef parentRef =
            (PSLocalFileSystemHierarchyNodeRef) targetParent.getNodeRef();
      final File parentFile = parentRef.getFile();
      final PSMultiOperationStrategy transformer = new PSMultiOperationStrategy(
            new PSCreateChildrenOperation(parentFile, results, type));
      final List resultRefs =
            TransformedList.decorate(new ArrayList(),transformer);
     resultRefs.addAll(names);
     return castToRefs(transformer.validateResult(resultRefs.toArray()));
   }

   /**
    * Casts provided refs to {@link IPSHierarchyNodeRef}.
    */
   private IPSHierarchyNodeRef[] castToRefs(final Object[] refsAsObjects)
   {
      final IPSHierarchyNodeRef[] refsAsRefs = new IPSHierarchyNodeRef[refsAsObjects.length];
      for (int i = 0; i < refsAsObjects.length; i++)
      {
         refsAsRefs[i] = (IPSHierarchyNodeRef) refsAsObjects[i];
      }
      return refsAsRefs;
   }

   /**
    * Validates parent reference. Uses only reference-based parent definition.
    */
   private void validateParent(NodeId targetParent)
   {
      if (targetParent.isNameBased())
      {
         throw new IllegalArgumentException("Handles only references");
      }

      final PSLocalFileSystemHierarchyNodeRef parentRef =
         (PSLocalFileSystemHierarchyNodeRef) targetParent.getNodeRef();
      if (!parentRef.isContainer())
      {
         throw new IllegalArgumentException(
               "Expected a directory but got file: " + parentRef.getFile());
      }
   }

   // see base
   @SuppressWarnings("unchecked")
   public IPSHierarchyNodeRef[] createChildrenFrom(NodeId targetParent,
         IPSHierarchyNodeRef[] children, 
         @SuppressWarnings("unused") String[] names, 
         Object[] results)
         throws PSMultiOperationException
   {
      validateParent(targetParent);
      
      final PSLocalFileSystemHierarchyNodeRef parentRef =
            (PSLocalFileSystemHierarchyNodeRef) targetParent.getNodeRef();
      final File parentFile = parentRef.getFile();
      final PSMultiOperationStrategy transformer = new PSMultiOperationStrategy(
            new PSCreateChildrenFromOperation(parentFile, results));
      final List resultRefs =
            TransformedList.decorate(new ArrayList(),transformer);
     resultRefs.addAll(Arrays.asList(children));
     return castToRefs(transformer.validateResult(resultRefs.toArray()));
   }

   /**
    * This implementation just calls
    * {@link PSLocalFileSystemModelProxy#delete(IPSReference[])} which
    * handles hierarchy just file. 
    */
   public void removeChildren(IPSHierarchyNodeRef[] children)
      throws PSMultiOperationException, PSModelException
   {
      m_modelProxy.delete(children);
   }

   // see base
   @SuppressWarnings("unchecked")
   public void moveChildren(IPSHierarchyNodeRef[] sourceChildren,
         NodeId targetParent) throws PSMultiOperationException
   {
      validateParent(targetParent);
      
      final PSLocalFileSystemHierarchyNodeRef parentRef =
            (PSLocalFileSystemHierarchyNodeRef) targetParent.getNodeRef();
      final File parentFile = parentRef.getFile();

      final PSMultiOperationStrategy transformer =
         new PSMultiOperationStrategy(new PSMoveOperation(parentFile));
      final List results =
            TransformedList.decorate(new ArrayList(),transformer);
      results.addAll(Arrays.asList(sourceChildren));
      transformer.validateResult(results.toArray());
   }

   // see base class
   public IPSHierarchyNodeRef[] getChildren(NodeId parent)
      throws PSModelException
   {
      if (null == parent)
      {
         throw new IllegalArgumentException("parent cannot be null");  
      }
      
      if (parent.isNameBased())
      {
         if (!ROOT_NAME.equals(parent.getTreeName()))
         {
            throw new PSModelException(PSErrorCodes.TREE_NAME_NOT_FOUND,
                  new Object[] { parent.getTreeName() });
         }

         final List<IPSHierarchyNodeRef> children = 
            new ArrayList<IPSHierarchyNodeRef>();
         for (final File root : File.listRoots())
         {
            children.add(new PSLocalFileSystemHierarchyNodeRef(root, true));
         }
         return children.toArray(new IPSHierarchyNodeRef[0]);
      }

      return parent.getNodeRef().getChildren().toArray(
            new IPSHierarchyNodeRef[0]);
   }

   /**
    * Returns constant single-value list because this proxy presents one tree.
    * 
    * @see com.percussion.client.proxies.IPSHierarchyModelProxy#getRoots()
    */
   public Collection<String> getRoots()
   {
      return Collections.singleton(ROOT_NAME);
   }

   /**
    * Base class for strategies checking for duplicated names
    */
   private abstract class PSCheckDuplicatesStrategyBase extends
      PSMultiOperationExceptionStrategy
   {
      /**
       * Creates new strategy.
       * 
       * @param parentFile node for which children to check duplicated names
       */
      protected PSCheckDuplicatesStrategyBase(final File parentFile)
      {
         super();
         m_parentFile = parentFile;
      }
      
      /**
       * Adds the name to set of existing names.
       * 
       * @param name
       * @return <code>true</code> if the name already exists.
       */
      protected boolean addNameAndCheckIfExising(final String name)
      {
         if (new File(m_parentFile, name).exists())
         {
            return true;
         }
         return !m_existingNames.add(name.toLowerCase());
      }

      /**
       * Parent directory.
       */
      protected final File m_parentFile;

      /**
       * Existing names set. Is used to find duplicated names during insertion.
       */
      protected final Set<String> m_existingNames = new HashSet<String>();

      /**
       * Currently processed reference.
       */
      protected PSLocalFileSystemHierarchyNodeRef m_ref;
   }
   
   /**
    * Moves files to the targed directory.
    */
   private final class PSMoveOperation
         extends PSCheckDuplicatesStrategyBase
   {
      private PSMoveOperation(final File parentFile)
      {
         super(parentFile);
      }

      @Override
      protected Object doTransform(Object obj)
            throws PSDuplicateNameException, PSModelException
      {
         m_ref = (PSLocalFileSystemHierarchyNodeRef) obj;
         final String name = m_ref.getFile().getName();
         if (addNameAndCheckIfExising(name))
         {
            throw new PSDuplicateNameException(name, OBJECT_TYPE_FOLDER);
         }
         
         final File toFile = new File(m_parentFile, name);
         final boolean moved = m_ref.getFile().renameTo(toFile);
         if (!moved)
         {
            final Object[] params = new Object[] {
                  m_ref.getFile().getAbsolutePath(), toFile.getAbsolutePath()};
            throw new PSModelException(PSErrorCodes.MOVE_FAILED, params);
         }
         m_ref.setFile(toFile);
         return null;
      }

      /**
       * Constructs PSLockException for this operation.
       */
      @SuppressWarnings("unused")
      @Override
      protected PSLockException constructLockException(
            @SuppressWarnings("unused") PSNotLockedException e)
      {
         return new PSLockException("createChildren", m_ref.getObjectType()
               .toString(), m_ref.getName());
      }
   }
   
   /**
    * Creates children operation.
    */
   private final class PSCreateChildrenOperation
         extends PSCheckDuplicatesStrategyBase
   {
      private PSCreateChildrenOperation(
            final File parentFile,
            final Object[] results, final PSObjectType type)
      {
         super(parentFile);
         m_results = results;
         m_type = type;
      }

      @Override
      protected Object doTransform(Object obj) throws PSDuplicateNameException
      {
         m_nameIdx++;
         final String name = (String) obj;
         if (addNameAndCheckIfExising(name))
         {
            throw new PSDuplicateNameException(name, m_type);
         }
         final File file = new File(m_parentFile, name);
         m_ref = new PSLocalFileSystemHierarchyNodeRef(file, isDirectory(),
               false);
         if (isDirectory())
         {
            file.mkdirs();
         }
         else
         {
            final PSMimeContentAdapter content = new PSMimeContentAdapter();
            content.setName(file.getAbsolutePath());
            content.setContent(new ByteArrayInputStream(new byte[0]));
            m_results[m_nameIdx] = content;
         }
         return m_ref;
      }

      /**
       * Constructs PSLockException for this operation.
       */
      @Override
      protected PSLockException constructLockException(
            @SuppressWarnings("unused") PSNotLockedException e)
      {
         return new PSLockException("createChildren", m_ref.getObjectType()
               .toString(), m_ref.getName());
      }

      /**
       * Returns true if directories are requested to be created.
       */
      private boolean isDirectory()
      {
         return m_type.equals(OBJECT_TYPE_FOLDER);
      }

      /**
       * Currently processing name index.
       */
      private int m_nameIdx = -1;

      /**
       * Created children.
       */
      private final Object[] m_results;

      /**
       * Children object type.
       */
      private final PSObjectType m_type;
   }

   /**
    * Creates children operation.
    */
   private final class PSCreateChildrenFromOperation
         extends PSCheckDuplicatesStrategyBase
   {
      private PSCreateChildrenFromOperation(
            final File parentFile,
            final Object[] results)
      {
         super(parentFile);
         m_results = results;
      }

      @Override
      protected Object doTransform(Object obj) throws Exception
      {
         m_idx++;
         final PSLocalFileSystemHierarchyNodeRef ref =
               (PSLocalFileSystemHierarchyNodeRef) obj;
         final String name = ref.getFile().getName();
         if (addNameAndCheckIfExising(name))
         {
            throw new PSDuplicateNameException(name, ref.getObjectType());
         }
         final File toFile = new File(m_parentFile, name);
         m_ref = new PSLocalFileSystemHierarchyNodeRef(toFile, ref.isContainer(),
               false);
         if (ref.isContainer())
         {
            FileUtils.copyDirectory(ref.getFile(), toFile);
         }
         else
         {
            final PSMimeContentAdapter originalContent;
            try
            {
               originalContent = (PSMimeContentAdapter) m_modelProxy.load(
                     new IPSReference[] {ref}, false, false)[0];
            }
            catch (PSMultiOperationException e)
            {
               throw e;
            }
            final PSMimeContentAdapter content = new PSMimeContentAdapter();
            content.setName(toFile.getAbsolutePath());
            content.setContent(originalContent.getContent());
            m_results[m_idx] = content;
         }
         return m_ref;
      }

      /**
       * Constructs PSLockException for this operation.
       */
      @Override
      protected PSLockException constructLockException(
            @SuppressWarnings("unused") PSNotLockedException e)
      {
         return new PSLockException("createChildrenFrom", m_ref.getObjectType()
               .toString(), m_ref.getName());
      }

      /**
       * Currently processing index.
       */
      private int m_idx = -1;

      /**
       * Created children.
       */
      private final Object[] m_results;
   }

   /**
    * The hardcoded single tree name.
    */
   static final String ROOT_NAME = "LOCAL_FILE_TREE";
   
   /**
    * Corresponding model proxy.
    */
   IPSCmsModelProxy m_modelProxy = new PSLocalFileSystemModelProxy();
}
