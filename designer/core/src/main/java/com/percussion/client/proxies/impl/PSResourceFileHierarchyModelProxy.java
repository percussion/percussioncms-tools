/******************************************************************************
 *
 * [ PSResourceFileHierarchyModelProxy.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.proxies.impl;

import com.percussion.client.IPSHierarchyNodeRef;
import com.percussion.client.IPSPrimaryObjectType;
import com.percussion.client.PSErrorCodes;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypeFactory;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.impl.PSHierarchyNodeRef;
import com.percussion.client.proxies.IPSCmsModelProxy;
import com.percussion.conn.PSServerException;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSApplicationFile;
import com.percussion.error.PSException;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * Provides hierarchy management services for the object type
 * {@link PSObjectTypes#RESOURCE_FILE}. Uses base class implementation whenever
 * possible and does type specific work here.
 * 
 * @see com.percussion.client.proxies.impl.PSHierarchyModelProxy
 * @author Andriy Palamarchuk
 * @version 6.0
 */
public class PSResourceFileHierarchyModelProxy extends
   PSXmlApplicationFileHierarchyModelProxy
{
   /**
    * Ctor. Invokes base class version with the object type
    * {@link PSObjectTypes#RESOURCE_FILE}.
    */
   public PSResourceFileHierarchyModelProxy()
   {
      super(PSObjectTypes.RESOURCE_FILE);
      m_modelProxy = createModelProxy();
   }

   /**
    * Ctor. Invokes base class version with the supplied primary object type.
    * 
    * @param primaryType
    */
   public PSResourceFileHierarchyModelProxy(IPSPrimaryObjectType primaryType)
   {
      super(primaryType);
      m_modelProxy = createModelProxy(primaryType);
   }

   /**
    * Model proxy corresponding to this hierarchy proxy.
    */
   @Override
   protected IPSCmsModelProxy createModelProxy()
   {
      return new PSResourceFileModelProxy();
   }

   /**
    * Model proxy corresponding to this hierarchy proxy.
    */
   @Override
   protected IPSCmsModelProxy createModelProxy(IPSPrimaryObjectType primaryType)
   {
      return new PSResourceFileModelProxy(primaryType);
   }

   /* (non-Javadoc)
    * @see com.percussion.client.proxies.impl.PSXmlApplicationFileHierarchyModelProxy#getRoots()
    */
   @Override
   public Collection<String> getRoots()
   {
      return Collections.singleton(ROOT_NAME);
   }
   
   // see base class method for details
   @Override
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
               boolean empty = pr.getProperty("isEmpty").equals("true");
               if (!EXCLUDE_APP_SET.contains(name) && empty)
                  appNames.add(name);
            }
            IPSHierarchyNodeRef[] refs = new IPSHierarchyNodeRef[appNames
               .size()];
            for (int i = 0; i < appNames.size(); i++)
            {
               String name = appNames.get(i);
               final PSApplication application = getObjectStore().getApplication(
                  name, false, false);
               refs[i] = new PSXmlApplicationFileHierarchyRootRef(
                     application, PSObjectTypeFactory.getType(
                           PSObjectTypes.RESOURCE_FILE,
                           PSObjectTypes.FileSubTypes.FOLDER), true);
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
      PSObjectType fileType = PSObjectTypeFactory.getType(
            PSObjectTypes.RESOURCE_FILE,
            PSObjectTypes.FileSubTypes.FILE);
      PSObjectType folderType = PSObjectTypeFactory.getType(
            PSObjectTypes.RESOURCE_FILE,
            PSObjectTypes.FileSubTypes.FOLDER);
      for (final PSApplicationFile file : files)
      {
         final boolean folder = file.isFolder();
         final PSHierarchyNodeRef ref = new PSXmlApplicationFileHierarchyRef(parent,
            file.getFileName().getName(), folder ? folderType : fileType, folder);
         ref.setPersisted();
         ref.setReadOnly(false);
         children.add(ref);
      }
      return children.toArray(new IPSHierarchyNodeRef[0]);
   }

   /**
    * Validates parent node.
    */
   @Override
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

   /**
    * Object types this hierarchy handles.
    */
   private static final Set<PSObjectTypes> SUPPORTED_OBJECT_TYPES = new HashSet<PSObjectTypes>();

   static
   {
      SUPPORTED_OBJECT_TYPES.add(PSObjectTypes.RESOURCE_FILE);
      SUPPORTED_OBJECT_TYPES.add(PSObjectTypes.CONTENT_EDITOR_CONTROLS);
   }

   static public Set<String> EXCLUDE_APP_SET = new HashSet<String>();
   static
   {
      EXCLUDE_APP_SET.add("Docs");
      EXCLUDE_APP_SET.add("DTD");
      EXCLUDE_APP_SET.add("rxs_GlobalTemplates");
      EXCLUDE_APP_SET.add("rx_reports");
      EXCLUDE_APP_SET.add("sys_logs");
      EXCLUDE_APP_SET.add("cm");
      
   }

   /**
    * The hardcoded single tree name.
    */
   static private final String ROOT_NAME = "RESOURCE_FILE_TREE";
}
