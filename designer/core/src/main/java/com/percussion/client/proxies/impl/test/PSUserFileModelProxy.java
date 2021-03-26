/******************************************************************************
 *
 * [ PSUserFileModelProxy.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.proxies.impl.test;

import com.percussion.client.IPSHierarchyNodeRef;
import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSCoreUtils;
import com.percussion.client.PSErrorCodes;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypeFactory;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.PSObjectTypes.XmlApplicationSubTypes;
import com.percussion.client.impl.PSHierarchyNodeRef;
import com.percussion.client.impl.PSReference;
import com.percussion.client.proxies.IPSHierarchyModelProxy;
import com.percussion.client.proxies.PSProxyException;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.ui.data.PSHierarchyNode;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.Collection;

/**
 * Provides CRUD and cataloging services for the object type
 * {@link PSObjectTypes#USER_FILE}. Uses base class implementation whenever
 * possible and does type specific work here.
 * 
 * @see com.percussion.client.proxies.impl.PSCmsModelProxy
 * 
 * @version 6.0
 * @created 03-Sep-2005 4:39:27 PM
 */
public class PSUserFileModelProxy extends PSTestModelProxy
{
   /**
    * Ctor. Invokes base class version with the object type
    * {@link PSObjectTypes#USER_FILE} and for main type and supplied sub typefor
    * the object.
    */
   public PSUserFileModelProxy() throws PSProxyException {
      super(PSObjectTypes.USER_FILE);

      try
      {
         loadFromRepository();
      }
      catch (Exception e)
      {
         throw new PSProxyException(PSErrorCodes.RAW, new Object[]
         {"Could not install the proxy"}, e);

      }
   }

   /**
    * Loads the existing objects from the repository.
    * 
    * Initial Structure is:
    * <pre>
    * slots 
    *    Slot Folder 1_0 
    *    Slot Folder 2_0 
    *    Slot Folder 3_0 
    *    Slot Folder 4_0 
    *       Slot Folder 4_1 
    *          Slot Folder 4_2 
    *             Slot Placeholder 004
    *          Slot Placeholder 002 
    *       Slot Placeholder 001 
    *       Slot Placeholder 003
    * 
    * contentTypes 
    *    contentType folder 1_0
    * 
    * templates
    * 
    * XML Applications
    *    App Folder 1_0 
    *    App Folder 2_0 
    *       XML_APPLICATION_USER Folder 2_1 
    *          XML_APPLICATION_USER Folder 2_2
    *             web_resources
    *          rx_resources 
    *       Docs 
    *    System
    *       sys_resources
    *    rxs_Brief_ce 
    *
    * Extensions - Pre Processors
    *    cx
    *       sys_addAllowableWorkflowsForContentType
    *       sys_ConvertCustomSearchOperator
    *    
    * Extensions - Result Processors
    *    cx
    *       sys_addAllowableWorkflowsForContentType
    * </pre>
    * @throws PSProxyTestException
    * @throws PSModelException
    */
   private void loadFromRepository() throws PSProxyTestException,
         PSModelException
   {
      m_repositoryMap.clear();

      if (!ms_repository.exists())
      {
         // Create test objects as a starting point

         IPSHierarchyModelProxy proxy = PSCoreFactory.getInstance()
               .getHierarchyModelProxy(PSObjectTypes.USER_FILE);

         Collection<String> roots = proxy.getRoots();

         for (String root : roots)
         {
            if (root.equals("slots"))
            {
               final PSObjectType type = new PSObjectType(PSObjectTypes.SLOT);
               PSHierarchyNodeRef rootNode =
                     createNew(null, root, USERFILE_FOLDER);

               PSHierarchyNodeRef parentNode = rootNode;
               //top 4 folders
               for (int inc = 1; inc < 5; ++inc)
               {
                  parentNode = createNew(rootNode, PSTypeEnum.SLOT.toString()
                        + " Folder " + inc + "_0", USERFILE_FOLDER);
               }

               createTestItem(parentNode, "Slot PlaceHolder 001",
                     type, 1);

               PSHierarchyNodeRef parentNode2 = createTestItem("4_1", parentNode,
                     "Slot PlaceHolder 002", type, 2);

               createTestItem(parentNode, "Slot PlaceHolder 003", type, 3);

               createTestItem("4_2", parentNode2, "Slot PlaceHolder 004",
                     type, 4);
            }
            else if (root.equals("contentTypes"))
            {
               createRoot(root);
            }
            else if (root.equals("xmlApplications"))
            {
               final PSObjectType type =
                     new PSObjectType(PSObjectTypes.XML_APPLICATION, XmlApplicationSubTypes.USER);
               final PSHierarchyNodeRef rootNode =
                     createNew(null, root, USERFILE_FOLDER);

               createNew(rootNode, "App Folder 1_0", USERFILE_FOLDER);
               final PSHierarchyNodeRef parentNode =
                     createNew(rootNode, "App Folder 2_0", USERFILE_FOLDER);

               final int docsAppId = 387;
               final int rxResourcesAppId = 8;
               final int webResourcesAppId = 145;

               createTestItem(parentNode, "App PlaceHolder 001", type, docsAppId);

               final PSHierarchyNodeRef parentNode2 = createTestItem("2_1",
                     parentNode, "App PlaceHolder 002", type, rxResourcesAppId);

               createTestItem("2_2", parentNode2, "App PlaceHolder 003", type, webResourcesAppId);
            }
            else if (root.equals("extensionsIPSRequestPreProcessor"))
            {
               final PSObjectType type = PSObjectTypeFactory
                     .getType(PSObjectTypes.EXTENSION);
               final PSHierarchyNodeRef rootNode = createNew(null, root,
                     USERFILE_FOLDER);

               final PSHierarchyNodeRef cxNode =
                     createNew(rootNode, "cx", USERFILE_FOLDER);
               createTestItem(cxNode,
                     "Java_global_percussion_cx_sys_addAllowableWorkflowsForContentType",
                     type, -1);
               createTestItem(cxNode,
                     "Java_global_percussion_cx_sys_ConvertCustomSearchOperator",
                     type, -1);
            }
            else if (root.equals("extensionsIPSResultDocumentProcessor"))
            {
               final PSObjectType type = PSObjectTypeFactory.getType(PSObjectTypes.EXTENSION);
               final PSHierarchyNodeRef rootNode = createNew(null, root, USERFILE_FOLDER);

               final PSHierarchyNodeRef cxNode =
                     createNew(rootNode, "cx", USERFILE_FOLDER);
               createTestItem(cxNode,
                     "Java_global_percussion_cx_sys_addAllowableWorkflowsForContentType",
                     type, -1);
            }
            else
            {
               createNew(null, root, USERFILE_FOLDER);
            }
         }
         save();
      }
      else
      {
         m_repositoryMap = (PSUserFileMap) PSProxyTestUtil
               .loadRepository(ms_repository);
         m_repositoryMap.finish();
      }
   }

   /**
    * Helper method that creates a root folder and the first child folder off
    * the root.
    * 
    * <name>s <name> Folder 1_0 *
    * 
    * @param name of type (Slot, Template ...) assumed not empty and
    *           <code>null</code>.
    * 
    * @return empty child folder never null
    * 
    * 
    * @throws PSModelException
    */
   private PSHierarchyNodeRef createRoot(String name) throws PSModelException
   {
      PSHierarchyNodeRef folder = createNew(null, name, USERFILE_FOLDER);

      folder = createNew(folder, name + " Folder 1_0", USERFILE_FOLDER);

      return folder;
   }

   /**
    * Helper method to create a new {@link PSHierarchyNode} place holder and
    * associate an test item.
    * 
    * @param parentNode - Folder to put it in, assumed not <code>null</code>
    * @param name - item's name
    * @param objectType - Type of Object (Slot ...)
    * @param item - id of the test item. Set it to -1 to indicate that object does
    * not have an id.
    * @throws PSModelException
    */
   private void createTestItem(PSHierarchyNodeRef parentNode, String name,
         PSObjectType objectType, int item)
         throws PSModelException
   {
      PSHierarchyNodeRef node = createNew(parentNode, name, USERFILE_PLACEHOLDER);

      PSHierarchyNode data = (PSHierarchyNode) m_repositoryMap.get(node);
      
      final PSTypeEnum type;
      final int host;
      if (objectType.getPrimaryType().equals(PSObjectTypes.SLOT))
      {
         type = PSTypeEnum.SLOT;
         host = 1;
      }
      else if (objectType.getPrimaryType().equals(PSObjectTypes.XML_APPLICATION))
      {
         type = PSTypeEnum.LEGACY_CHILD;
         host = 0;
      }
      else if (objectType.getPrimaryType().equals(PSObjectTypes.EXTENSION))
      {
         // extensions don't have IDs
         host = -1;
         type = null;
      }
      else
      {
         throw new AssertionError("Implement");
      }

      data.addProperty("objectType", objectType.toSerial());
      if (item >= 0)
      {
         assert host >= 0;
         final PSGuid guid = new PSGuid(host, type, 0);
         guid.setUUID(item);
         data.addProperty("guid", guid.toString());
      }
      else
      {
         data.addProperty("name", name);
      }
   }

   /**
    * Helper method to create a new folder and {@link PSHierarchyNode} place
    * holder and associate a test item. See
    * {@link #createTestItem(PSHierarchyNodeRef, String, PSObjectType, int)  4
    * parameter version} of this method.
    * 
    * @param folderId - unique part of folder name
    * 
    * @return the folder created
    * 
    * @throws PSModelException
    */
   private PSHierarchyNodeRef createTestItem(String folderId,
         PSHierarchyNodeRef parentNode, String name, PSObjectType objectType,
         int item) throws PSModelException
   {
      PSHierarchyNodeRef folderNode = createNew(parentNode,
            objectType.toString().replace(':', '_') + " Folder " + folderId,
            USERFILE_FOLDER);

      createTestItem(folderNode, name, objectType, item);

      return folderNode;
   }

   /*
    * 
    * @see com.percussion.client.proxies.impl.PSCmsModelProxy#
    *      catalog(com.percussion.client.IPSReference)
    */
   @SuppressWarnings("unused") // exception
   @Override
   public Collection<IPSReference> catalog() throws PSModelException
   {
      throw new UnsupportedOperationException(
            "catalog is not supported for hierarchal objects");
   }

   /**
    * Creates a new object and a handle for that object. The handle is returned
    * directly and the data object is returned in the result array. 
    * 
    * @param parent May be <code>null</code> for root node.
    * 
    * @param name Never <code>null</code> or empty.
    * 
    * @param objectType The type of node to create.
    * 
    * @param result The generated data object will be returned in this node.
    * 
    * @return Never <code>null</code>.
    * 
    * @throws PSModelException If any problems creating/persisting the object or
    * its ref.
    */
   PSHierarchyNodeRef createNew(IPSHierarchyNodeRef parent, String name,
         PSObjectType objectType, PSHierarchyNode[] result) 
      throws PSModelException
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name cannot be null or empty");
      if (null == objectType)
         throw new IllegalArgumentException("type cannot be null");
      if (null == result)
         throw new IllegalArgumentException("result array cannot be null");
      PSHierarchyNodeRef userFileRef = doCreate(parent, name, objectType,
            result);
      m_lockHelper.getLock(userFileRef);
      return userFileRef;
   }

   /**
    * See {@link #createNew(IPSHierarchyNodeRef, String, PSObjectType, 
    * PSHierarchyNode[])} for description of params. This method just creates
    * the data object and the ref. No bookkeeping is done.
    */
   private PSHierarchyNodeRef doCreate(IPSHierarchyNodeRef parent, String name,
         PSObjectType objectType, PSHierarchyNode[] result)
      throws PSModelException
   {
      final String nodeName = name; 
      PSHierarchyNodeRef userFileRef = null;

      userFileRef = new PSHierarchyNodeRef(parent, nodeName, objectType,
            PSCoreUtils.dummyGuid(PSTypeEnum.HIERARCHY_NODE), objectType
                  .equals(USERFILE_FOLDER));

      PSHierarchyNode userFile = new PSHierarchyNode(nodeName, userFileRef
            .getId(),
            objectType.equals(USERFILE_FOLDER) ? PSHierarchyNode.NodeType.FOLDER
                  : PSHierarchyNode.NodeType.PLACEHOLDER);
      userFile.setVersion(1);

      userFile.setParentId(parent == null ? null : parent.getId());
      result[0] = userFile;
      return userFileRef;      
   }
   
   /**
    * Helper method to create a new {@link PSHierarchyNode} object. The object
    * is added to the repository, but it is not locked. 
    * 
    * @param parent ref of the folder that contains this item and may be
    *           <code>null</code>
    * @param name UserFile, assumed not <code>null</code>.
    * @param objectType assumed to be a Folder or PlaceHolder
    * @return new UserFile object, never <code>null</code>.
    * @throws PSModelException
    */
   private PSHierarchyNodeRef createNew(IPSHierarchyNodeRef parent, String name,
         PSObjectType objectType) 
      throws PSModelException

   {
      PSHierarchyNode[] result = new PSHierarchyNode[1];
      PSHierarchyNodeRef userFileRef =
            doCreate(parent, name, objectType, result);

      m_repositoryMap.put(userFileRef, result[0]);

      return userFileRef;
   }

   // See interface
   @SuppressWarnings("unused") //exception
   @Override
   public void rename(IPSReference ref, String name, Object data)
         throws PSModelException
   {
      // we may not know about the data object if it hasn't been persisted
      PSHierarchyNode userFile = (PSHierarchyNode) m_repositoryMap.get(ref);
      if (userFile != null)
      {
         userFile.setName(name);
         try
         {
            save();
         }
         catch (Exception e)
         {
            throw new RuntimeException("Error saving to repository file "
                  + ms_repository, e);
         }
      }
      renameLocal(ref, name, data);
      
   }
   
   /* 
    * @see com.percussion.client.proxies.impl.PSCmsModelProxy#renameLocal(
    * com.percussion.client.IPSReference, java.lang.String, java.lang.Object)
    */
   @Override
   public void renameLocal(IPSReference ref, String name, Object data)
   {
      ((PSReference) ref).setName(name);
      if (data != null)
      {
         ((PSHierarchyNode) data).setName(name);
      }
   }

   /*
    * @see com.percussion.client.proxies.test.impl.PSTestModelProxy#
    *      objectToReference(java.lang.Object)
    */
   @SuppressWarnings("unused")
   @Override
   protected IPSReference objectToReference(Object obj)
   {
         throw new UnsupportedOperationException(
               "Need parent to create a reference");
   }
   
   /*
    * @see com.percussion.client.proxies.test.impl.PSTestModelProxy#
    *      getRepositoryFile()
    */
   @Override
   protected File getRepositoryFile()
   {
      return ms_repository;
   }

   /*
    * @see com.percussion.client.proxies.test.impl.PSTestModelProxy#
    *      getRepositoryMap()
    */
   @Override
   protected IPSRepositoryMap getRepositoryMap()
   {
      return m_repositoryMap;
   }

   /**
    * Gets the {@link PSUserFileHierarchyModelProxy }.
    * 
    * @return Never <code>null</code>
    */
   PSUserFileHierarchyModelProxy getProxy()
   {
      try
      {
         return (PSUserFileHierarchyModelProxy) PSCoreFactory.getInstance()
               .getHierarchyModelProxy(PSObjectTypes.USER_FILE);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   /**
    * Causes the map to be written to disk.
    * 
    * @throws PSProxyTestException If the save fails (e.g. disk write error.)
    */
   void save()
      throws PSProxyTestException
   {
      saveRepository(m_repositoryMap, ms_repository);
   }
   
   boolean isLocked(IPSHierarchyNodeRef ref)
   {
      return m_lockHelper.hasLock(ref);
   }
   
   /**
    * Name of the repository file.
    */
   public static final String REPOSITORY_XML = "userfile_repository.xml";

   /**
    * Userfile repository file name this test proxy uses. The file will be
    * created in the root directory for the workbench if one does not exist. It
    * will use the existing one if one exists.
    */
   static private File ms_repository = new File(REPOSITORY_XML);

   /**
    * Map of all slots from the repository. Filled during initialization of the
    * proxy.
    */
   private PSUserFileMap m_repositoryMap = new PSUserFileMap();

   /*
    * These are declared in the real object, this is to save typing
    */
   private static final PSObjectType USERFILE_FOLDER = 
      com.percussion.client.proxies.impl.PSUserFileModelProxy.USERFILE_FOLDER;

   private static final PSObjectType USERFILE_PLACEHOLDER = 
      com.percussion.client.proxies.impl.PSUserFileModelProxy.USERFILE_PLACEHOLDER;
}
