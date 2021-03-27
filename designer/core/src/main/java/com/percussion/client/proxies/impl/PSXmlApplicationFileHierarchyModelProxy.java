/******************************************************************************
 *
 * [ PSXmlApplicationFileHierarchyModelProxy.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.proxies.impl;

import com.percussion.client.IPSHierarchyNodeRef;
import com.percussion.client.IPSPrimaryObjectType;
import com.percussion.client.PSDuplicateNameException;
import com.percussion.client.PSErrorCodes;
import com.percussion.client.PSModelException;
import com.percussion.client.PSMultiOperationException;
import com.percussion.client.PSMultiOperationStrategy;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.impl.PSHierarchyNodeRef;
import com.percussion.client.models.PSLockException;
import com.percussion.client.proxies.IPSCmsModelProxy;
import com.percussion.conn.PSServerException;
import com.percussion.design.objectstore.*;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.error.PSException;
import com.percussion.error.PSIllegalStateException;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.services.system.data.PSMimeContentAdapter;
import org.apache.commons.collections.list.TransformedList;
import org.apache.commons.lang.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * Provides hierarchy management services for the object type
 * {@link PSObjectTypes#XML_APPLICATION_FILE}. Uses base class implementation
 * whenever possible and does type specific work here.
 * 
 * @see com.percussion.client.proxies.impl.PSHierarchyModelProxy
 * 
 * @version 6.0
 * @created 03-Sep-2005 4:39:27 PM
 */
public class PSXmlApplicationFileHierarchyModelProxy extends
   PSHierarchyModelProxy
{
   /**
    * Ctor. Invokes base class version with
    * {@link PSObjectTypes#XML_APPLICATION_FILE}.
    */
   public PSXmlApplicationFileHierarchyModelProxy()
   {
      super(PSObjectTypes.XML_APPLICATION_FILE);
      m_modelProxy = createModelProxy();
   }

   /**
    * Ctor. Invokes base class version.
    */
   public PSXmlApplicationFileHierarchyModelProxy(
      IPSPrimaryObjectType primaryType)
   {
      super(primaryType);
      m_modelProxy = createModelProxy(primaryType);
   }

   /**
    * Model proxy corresponding to this hierarchy proxy.
    */
   protected IPSCmsModelProxy createModelProxy()
   {
      return new PSResourceFileModelProxy();
   }

   /**
    * Model proxy corresponding to this hierarchy proxy.
    */
   protected IPSCmsModelProxy createModelProxy(IPSPrimaryObjectType primaryType)
   {
      return new PSXmlApplicationFileModelProxy(primaryType);
   }

   //see interface
   @Override
   public IPSHierarchyNodeRef[] createChildrenFrom(NodeId targetParent,
         IPSHierarchyNodeRef[] children, String[] names, Object[] results)
      throws PSMultiOperationException, PSModelException
   {
      PSObjectType type = null;
      List<String> actualNames = new ArrayList<>();
      for (int i=0; i < children.length; i++)
      {
         if (type == null)
            type = children[i].getObjectType();
         else if (type.equals(children[i].getObjectType()))
         {
            throw new IllegalArgumentException(
                  "all children must have same object type");
         }
         if (names == null || StringUtils.isBlank(names[i]))
            actualNames.add(children[i].getName());
         else
            actualNames.add(names[i]);
      }
      
      IPSHierarchyNodeRef[] resultRefs = createChildren(targetParent, type,
            actualNames, results);

      Object[] origData = m_modelProxy.load(children, false, false);
      for (int i = 0; i < resultRefs.length; i++)
      {
         if (resultRefs[i].isContainer())
            continue;
         PSMimeContentAdapter srcData = (PSMimeContentAdapter) origData[i];

         PSMimeContentAdapter tgtData = ((PSMimeContentAdapter) results[i]);
         
         tgtData.setContent(srcData.getContent());
         tgtData.setCharacterEncoding(srcData.getCharacterEncoding());
         tgtData.setMimeType(srcData.getMimeType());
      }
      return resultRefs;
   }

   // see base interface
   @SuppressWarnings("unchecked")
   public IPSHierarchyNodeRef[] createChildren(final NodeId targetParent,
      final PSObjectType type, List<String> names, final Object[] results)
      throws PSMultiOperationException
   {
      if (null == targetParent)
      {
         throw new IllegalArgumentException("targetParent cannot be null");
      }

      if (targetParent.isNameBased())
      {
         throw new UnsupportedOperationException(
            "This manager does not support moving nodes to the root.");
      }

      // assert targetParent.getNodeRef().getObjectType().equals(
      // OBJECT_TYPE_FOLDER);
      assert names.size() == results.length;

      final PSMultiOperationStrategy transformer = new PSMultiOperationStrategy(
         new PSCreateChildrenOperation(
            (PSXmlApplicationFileHierarchyRef) targetParent.getNodeRef(), results,
            type, getObjectStore()));
      final List resultRefs = TransformedList.decorate(new ArrayList(),
         transformer);
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

   // see interface
   @SuppressWarnings("unused") //exception
   public Collection<String> getRoots() throws PSModelException
   {
      return Collections.singleton(ROOT_NAME);
   }

   // see base class method for details
   public IPSHierarchyNodeRef[] getChildren(final NodeId parentId)
      throws PSModelException
   {
      if (null == parentId)
      {
         throw new IllegalArgumentException("parentId cannot be null");
      }

      if (parentId.isNameBased())
      {
         if (!ROOT_NAME.equals(parentId.getTreeName()))
         {
            // should never happen
            throw new PSModelException(PSErrorCodes.TREE_NAME_NOT_FOUND,
               new Object[]
               {
                  parentId.getTreeName()
               });
         }

         try
         {
            Properties info = new Properties();
            info.put("name", "");
            info.put("description", "");
            info.put("isEnabled", "");
            info.put("isEmpty", "");
            info.put("appType", "");
            Enumeration e = getObjectStore().getApplicationSummaries(info, true);
            List<String> appNames = new ArrayList<String>();
            while (e.hasMoreElements())
            {
               Properties pr = (Properties) e.nextElement();
               String name = pr.getProperty("name");
               appNames.add(name);
            }
            IPSHierarchyNodeRef[] refs = new IPSHierarchyNodeRef[appNames
               .size()];
            for (int i = 0; i < appNames.size(); i++)
            {
               String name = appNames.get(i);
               final PSApplication application = getObjectStore().getApplication(
                  name, false, false);
               refs[i] = new PSXmlApplicationFileHierarchyRootRef(application);
            }
            return refs;
         }
         catch (PSException e)
         {
            throw new PSModelException(e);
         }
      }

      IPSHierarchyNodeRef parent = parentId.getNodeRef();
      validateParent(parent);
      final PSXmlApplicationFileHierarchyRef parentRef = (PSXmlApplicationFileHierarchyRef) parent;
      final PSApplicationFile parentApplicationFile = parent.getParent() == null ? null
         : new PSApplicationFile(new File(parentRef.getFilePath()), parent
            .isContainer());
      final Collection<PSApplicationFile> files;
      try
      {
         files = getObjectStore().getApplicationFiles(parentRef.getApplication(),
            parentApplicationFile);
      }
      catch (PSServerException e)
      {
         throw new PSModelException(e);
      }
      catch (PSAuthorizationException e)
      {
         throw new PSModelException(e);
      }
      catch (PSAuthenticationFailedException e)
      {
         throw new PSModelException(e);
      }

      final List<IPSHierarchyNodeRef> children = new ArrayList<IPSHierarchyNodeRef>();
      for (final PSApplicationFile file : files)
      {
         final PSHierarchyNodeRef ref = new PSXmlApplicationFileHierarchyRef(parent,
            file.getFileName().getName(), file.isFolder());
         ref.setPersisted();
         ref.setReadOnly(false);
         children.add(ref);
      }
      return children.toArray(new IPSHierarchyNodeRef[0]);
   }

   /**
    * Validates parent node.
    */
   protected void validateParent(final IPSHierarchyNodeRef parent)
   {
      assert parent != null;
      final Enum primaryType = parent.getObjectType().getPrimaryType();
      if (!SUPPORTED_OBJECT_TYPES.contains(primaryType))
      {
         throw new IllegalArgumentException("Unexpected parent node type: "
            + primaryType);
      }
      Enum secType = parent.getObjectType().getSecondaryType();
      if (!secType.equals(PSObjectTypes.FileSubTypes.FOLDER)
         && !secType.equals(PSObjectTypes.ContentEditorControlSubTypes.SYSTEM)
         && !secType.equals(PSObjectTypes.ContentEditorControlSubTypes.USER))
      {
         throw new IllegalArgumentException(
            "Unexpected parent node secondary type. "
               + "Expected folder but got "
               + parent.getObjectType().getSecondaryType());
      }
   }

   // see base class method for details
   @Override
   @SuppressWarnings("unchecked")
   public void moveChildren(IPSHierarchyNodeRef[] sourceChildren,
      NodeId targetParent) throws PSMultiOperationException
   {
      if (targetParent.isNameBased())
      {
         throw new UnsupportedOperationException(
            "This manager does not support moving nodes to the root.");
      }

      validateParent(targetParent.getNodeRef());
      final PSXmlApplicationFileHierarchyRef targetParentRef = (PSXmlApplicationFileHierarchyRef) targetParent
         .getNodeRef();
      final PSMultiOperationStrategy transformer = new PSMultiOperationStrategy(
         new PSMoveChildrenOperation(targetParentRef, getObjectStore()));
      final List results = TransformedList.decorate(new ArrayList(),
         transformer);
      results.addAll(Arrays.asList(sourceChildren));
      transformer.validateResult(results.toArray());
   }

   // see base class method for details
   @Override
   public void removeChildren(IPSHierarchyNodeRef[] children)
      throws PSMultiOperationException, PSModelException
   {
      m_modelProxy.delete(children);
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
       * @param parent node for which children to check duplicated names
       */
      protected PSCheckDuplicatesStrategyBase(
         final PSXmlApplicationFileHierarchyRef parent)
      {
         super();
         m_parent = parent;
         try
         {
            initializeExistingNames();
         }
         catch (PSModelException e)
         {
            m_initException = e;
         }
      }

      private void initializeExistingNames() throws PSModelException
      {
         m_existingNames.clear();
         for (final IPSHierarchyNodeRef ref : getChildren(new NodeId(m_parent)))
         {
            addNameAndCheckIfExising(ref.getName());
         }
      }

      /**
       * Performs validation for the specified name. Should be called from
       * {@link PSMultiOperationExceptionStrategy#doTransform(Object)}
       * implementation.
       * 
       * @throws PSModelException if {@link #m_initException} is not null.
       * @throws PSDuplicateNameException if duplicated name is found.
       */
      protected void validateChildName(final String name,
         final PSObjectType objectType) throws PSModelException,
         PSDuplicateNameException
      {
         if (m_initException != null)
         {
            throw m_initException;
         }
         if (addNameAndCheckIfExising(name))
         {
            throw new PSDuplicateNameException(name, objectType);
         }
      }

      /**
       * Adds the name to set of existing names.
       * 
       * @param name
       * @return <code>true</code> if the name already existed in the set.
       */
      private boolean addNameAndCheckIfExising(final String name)
      {
         return !m_existingNames.add(name.toLowerCase());
      }

      /**
       * Parent to create children for.
       */
      protected final PSXmlApplicationFileHierarchyRef m_parent;

      /**
       * Existing names set. Is used to find duplicated names during insertion.
       */
      protected final Set<String> m_existingNames = new HashSet<String>();

      /**
       * Exception during initialization.
       */
      private PSModelException m_initException;
   }

   /**
    * Operation to move hierarchy files from one location to another.
    */
   private final class PSMoveChildrenOperation extends
      PSCheckDuplicatesStrategyBase
   {
      private PSMoveChildrenOperation(
         final PSXmlApplicationFileHierarchyRef targetParentRef,
         final PSObjectStore objectStore)
      {
         super(targetParentRef);
         m_objectStore = objectStore;
      }

      @Override
      protected Object doTransform(Object obj) throws PSServerException,
              PSAuthorizationException, PSAuthenticationFailedException,
              PSNotLockedException, PSValidationException, PSModelException,
              PSDuplicateNameException, PSSystemValidationException {
         m_ref = (PSXmlApplicationFileHierarchyRef) obj;
         validateChildName(m_ref.getName(), m_ref.getObjectType());
         if (m_ref.isLocked())
         {
            throw new PSModelException(
                  PSErrorCodes.OPERATION_NOT_ALLOWED_WHILE_LOCKED,
                  new Object[] { "move", m_ref.getName() });
         }

         final PSApplicationFile fromFile = createAppFile(m_ref.getFilePath());
         final PSApplicationFile toFile = createAppFile(m_parent.getFilePath()
            + "/" + m_ref.getName());
         m_objectStore.moveApplicationFile(m_ref.getApplication(), fromFile,
            m_parent.getApplication(), toFile, true);
         m_ref.setParent(m_parent);
         return null;
      }

      /**
       * Creates {@link PSApplicationFile} from string path.
       */
      private PSApplicationFile createAppFile(final String path)
      {
         return new PSApplicationFile(new File(path), isFolder());
      }

      /**
       * Whether type of current reference is folder.
       */
      private boolean isFolder()
      {
         return m_ref.getObjectType().getSecondaryType().equals(
            PSObjectTypes.FileSubTypes.FOLDER);
      }

      // see base class
      @SuppressWarnings("unused")
      @Override
      protected PSLockException constructLockException(PSNotLockedException e)
      {
         return new PSLockException("moveChildren", m_ref.getObjectType()
            .toString(), m_ref.getName());
      }

      /**
       * Currently processed reference.
       */
      private PSXmlApplicationFileHierarchyRef m_ref;

      /**
       * Object store to handle low-level operations.
       */
      private final PSObjectStore m_objectStore;
   }

   private final class PSCreateChildrenOperation extends
      PSCheckDuplicatesStrategyBase
   {
      private PSCreateChildrenOperation(
         final PSXmlApplicationFileHierarchyRef parent, final Object[] results,
         final PSObjectType type, final PSObjectStore objectStore)
      {
         super(parent);
         m_results = results;
         m_type = type;
         m_objectStore = objectStore;
      }

      @Override
      @SuppressWarnings("unused") //exception
      protected Object doTransform(Object obj) throws PSServerException,
         PSAuthorizationException, PSAuthenticationFailedException,
         PSNotLockedException, PSValidationException, PSIllegalStateException,
         PSModelException, PSDuplicateNameException
      {
         final String name = (String) obj;
         validateChildName(name, m_type);
         m_nameIdx++;
         m_ref = new PSXmlApplicationFileHierarchyRef(m_parent, name, m_type,
               isFolder());
         if (isFolder())
         {
            m_results[m_nameIdx] = null;
            m_results[m_nameIdx] = null;
         }
         else
         {
            final PSMimeContentAdapter content = new PSMimeContentAdapter();
            content.setContent(new ByteArrayInputStream(new byte[0]));
            content.setContentLength(0L);
            content.setName(name);
            m_results[m_nameIdx] = content;
         }
         return m_ref;
      }

      /**
       * Whether provided type is folder type.
       */
      private boolean isFolder()
      {
         return m_type.getSecondaryType().equals(
            PSObjectTypes.FileSubTypes.FOLDER);
      }

      /**
       * Constructs PSLockException for this operation.
       */
      @SuppressWarnings("unused") //param
      @Override
      protected PSLockException constructLockException(PSNotLockedException e)
      {
         return new PSLockException("createChildren", m_ref.getObjectType()
            .toString(), m_ref.getName());
      }

      /**
       * Currently processed reference.
       */
      private PSXmlApplicationFileHierarchyRef m_ref;

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

      /**
       * Object store to handle low-level operations.
       */
      @SuppressWarnings("unused")
      private final PSObjectStore m_objectStore;
   }

   /**
    * Object types this hierarchy handles.
    */
   private static final Set<PSObjectTypes> SUPPORTED_OBJECT_TYPES = new HashSet<PSObjectTypes>();

   static
   {
      SUPPORTED_OBJECT_TYPES.add(PSObjectTypes.XML_APPLICATION_FILE);
      SUPPORTED_OBJECT_TYPES.add(PSObjectTypes.RESOURCE_FILE);
      SUPPORTED_OBJECT_TYPES.add(PSObjectTypes.CONTENT_EDITOR_CONTROLS);
   }

   /**
    * Corresponding model proxy.
    */
   IPSCmsModelProxy m_modelProxy;

   /**
    * The hardcoded single tree name.
    */
   static private final String ROOT_NAME = "XML_FILE_TREE";
}
