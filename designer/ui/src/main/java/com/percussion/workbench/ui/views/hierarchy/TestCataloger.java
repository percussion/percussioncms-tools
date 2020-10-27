/******************************************************************************
 *
 * [ TestCataloger.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.views.hierarchy;

import com.percussion.client.IPSReference;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypes;
import com.percussion.services.assembly.data.PSTemplateSlot;
import com.percussion.workbench.ui.IPSCatalog;
import com.percussion.workbench.ui.PSUiReference;
import com.percussion.workbench.ui.catalogs.PSCatalogFactoryBase;
import com.percussion.workbench.ui.views.hierarchy.PSHierarchyDefProcessor.InheritedProperties;
import com.percussion.workbench.ui.views.hierarchy.xmlbind.Catalog;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * This class is used to test the 'type="class"' attribute of the Catalog
 * element.
 */
class TestCataloger extends PSCatalogFactoryBase
{

   public TestCataloger(InheritedProperties contextProps,
         PSHierarchyDefProcessor proc, Catalog type)
   {
      super(contextProps, proc, type);
   }

   public IPSCatalog createCatalog(final PSUiReference parent)
   {
      IPSCatalog cat = new IPSCatalog()
      {
         public List<PSUiReference> getEntries(boolean force) 
            throws PSModelException
         {
            try
            {
               List<PSUiReference> results = new ArrayList<PSUiReference>();
               String val = getContextProperty("subgroup");
               if (val.equals("1"))
               {
                  results.add(new PSUiReference(parent,
                        "subgroup 1 - child 1", null, null, getFactory(parent
                              .getObjectType()), false));
                  if (getContextProperty("group").trim().length() < 1)
                     throw new RuntimeException(
                           "Catalog property not inherited.");
               }
               else if (getContextProperty("group").equals("1"))
               {
                  results.add(new PSUiReference(parent, "zgroup 1 - child 1",
                        null, null, getFactory(parent.getObjectType()), false));
                  results.add(new PSUiReference(parent, "agroup 1 - child 2",
                        null, null, getFactory(parent.getObjectType()), false));
                  results.add(new PSUiReference(parent, "mgroup 1 - child 3",
                        null, null, getFactory(parent.getObjectType()), false));
               }
               else if (getContextProperty("expansionType").equals(
                           "variantLinks"))
               {
                  String prefix = getContextProperty("inherited").equals("2") 
                        ? "inherited" : "exp";
                  results
                        .add(new PSUiReference(parent, prefix + " - child 1",
                              null, null, getFactory(parent.getObjectType()),
                              false));
                  results
                        .add(new PSUiReference(parent, prefix + " - child 2",
                              null, null, getFactory(parent.getObjectType()),
                              false));
                  results
                        .add(new PSUiReference(parent, prefix + " - child 3",
                              null, null, getFactory(parent.getObjectType()),
                              false));
               }
               else if (getContextProperty("slotInstanceOverride").equals("1"))
               {
                  results.add(new PSUiReference(parent, "override - child 1",
                        null, null, getFactory(parent.getObjectType()), false));
                  results.add(new PSUiReference(parent, "override - child 2",
                        null, null, getFactory(parent.getObjectType()), false));
               }
               // must come before 'resource=roots'
               else if (getContextProperty("resourceType").equals("folder"))
               {
                  if (!getContextProperty("resource").equals("roots"))
                     throw new RuntimeException("property not inherited");

                  if (parent.getName().equals("folder1-1"))
                  {
                     results.add(new PSUiReference(parent, "folder2-1", null,
                           null, getFactory(new PSObjectType(
                                 PSObjectTypes.RESOURCE_FILE,
                                 PSObjectTypes.FileSubTypes.FOLDER)), true));
                     results.add(new PSUiReference(parent, "file2-1", null,
                           null, getFactory(new PSObjectType(
                                 PSObjectTypes.RESOURCE_FILE,
                                 PSObjectTypes.FileSubTypes.FILE)), false));
                  }
                  else if (parent.getName().equals("folder1-2"))
                  {
                     results.add(new PSUiReference(parent, "file2-1", null,
                           null, getFactory(new PSObjectType(
                                 PSObjectTypes.RESOURCE_FILE,
                                 PSObjectTypes.FileSubTypes.FILE)), false));
                  }
                  else if (parent.getName().equals("folder2-1"))
                  {
                     results.add(new PSUiReference(parent, "folder3-1", null,
                           null, getFactory(parent.getObjectType()), true));
                  }
               }
               else if (getContextProperty("resource").equals("roots"))
               {
                  Properties rootName = new Properties();
                  rootName.setProperty("resource", "rootChildren");

                  
                  rootName.setProperty("rootname", "sys_resources");
                  results
                        .add(new PSUiReference(parent, "sys_resources", null,
                              null, new Factory("sys_resources",
                                    TestCataloger.this), true));

                  rootName.setProperty("rootname", "rx_resources");
                  results.add(new PSUiReference(parent, "rx_resources", null,
                        null, new Factory("rx_resources", TestCataloger.this),
                        true));
               }
               else if (getContextProperty("ctxparent").equals("1"))
               {
                  PSUiReference next = parent;
                  PSUiReference ctxParent = null;
                  while (ctxParent == null && next.getParentNode() != null)
                  {
                     if (next.getData() instanceof PSTemplateSlot)
                        ctxParent = parent;
                     next = next.getParentNode();
                  }
                  if (ctxParent == null)
                     throw new RuntimeException("can't find ctx parent");

                  results.add(new PSUiReference(parent, "ctx - child 1", null,
                        null, getFactory(parent.getObjectType()), false));
                  results.add(new PSUiReference(parent, "ctx - child 2", null,
                        null, getFactory(parent.getObjectType()), false));
                  results.add(new PSUiReference(parent, "ctx - child 3", null,
                        null, getFactory(parent.getObjectType()), false));
               }
               else if (getContextProperty("mixed").length() > 0)
               {
                  if (getContextProperty("mixed").equals("group1"))
                  {
                     results.add(new PSUiReference(parent,
                           "nmixed group 1 - child 1", null, null,
                           getFactory(parent.getObjectType()), false));
                     results.add(new PSUiReference(parent,
                           "zmixed group 1 - child 2", null, null,
                           getFactory(parent.getObjectType()), false));
                  }
                  else if (getContextProperty("mixed").equals("group2"))
                  {
                     results.add(new PSUiReference(parent,
                           "amixed group 2 - child 1", null, null,
                           getFactory(parent.getObjectType()), false));
                     results.add(new PSUiReference(parent,
                           "gmixed group 2 - child 2", null, null,
                           getFactory(parent.getObjectType()), false));
                  }
                  else
                     throw new RuntimeException("unexpected 'mixed' value");

               }
               else if (getContextProperty("alternating").length() > 0)
               {
                  if (getContextProperty("alternating").equals("1"))
                  {
                     results.add(new PSUiReference(parent,
                           "alt group 1 - child 1", null, null,
                           getFactory(parent.getObjectType()), false));
                     results.add(new PSUiReference(parent,
                           "alt group 1 - child 2", null, null,
                           getFactory(parent.getObjectType()), false));
                  }
                  else if (getContextProperty("alternating").equals("2"))
                  {
                     results.add(new PSUiReference(parent,
                           "alt group 2 - child 1", null, null,
                           getFactory(parent.getObjectType()), false));
                     if (getContextProperty("scope").trim().length() < 1
                           || getContextProperty("prop2").trim().length() < 1)
                     {
                        throw new RuntimeException(
                              "Catalog property not inherited.");
                     }
                  }
                  else
                     throw new RuntimeException(
                           "unexpected 'alternating' value");

               }
               return results;
            }
            catch (Exception e)
            {
               throw new RuntimeException(e);
            }
         }

         public PSUiReference createEntry(IPSReference ref)
         {
            throw new UnsupportedOperationException(
                  "Unsupported for local file system.");
         }
      };
      return cat;
   }

   /**
    * This is a special cataloger that handles the children of the RESOURCE_FILE
    * object type 'root' node only.
    *
    * @author paulhoward
    */
   private class Factory extends PSCatalogFactoryBase
   {
      private final String m_treeName;

      public Factory(String treeName, PSCatalogFactoryBase source)
      {
         super(source);
         m_treeName = treeName;
      }
      
      public IPSCatalog createCatalog(final PSUiReference parent)
      {
         IPSCatalog cat = new IPSCatalog()
         {

            public List<PSUiReference> getEntries(boolean force) 
               throws PSModelException
            {
//               try
//               {
                  List<PSUiReference> results = new ArrayList<PSUiReference>();
//                  IPSCmsModel model = PSCoreFactory.getInstance()
//                        .getModel(PSObjectTypes.RESOURCE_FILE);
//                  IPSHierarchyManager mgr = model
//                        .getHierarchyManager(m_treeName);
//                  Collection<IPSHierarchyNodeRef> children = mgr.getChildren(null);
//                  for (IPSHierarchyNodeRef ref : children)
//                  {
//                     results.add(new PSUiReference(parent, m_treeName, null, null,
//                           getFactory(ref.getObjectType())));
//                  }

                  if (m_treeName.equals("sys_resources"))
                  {
                     results.add(new PSUiReference(parent, "folder1-1", null,
                           null, getFactory(new PSObjectType(
                                 PSObjectTypes.RESOURCE_FILE,
                                 PSObjectTypes.FileSubTypes.FOLDER)), true));
                     results.add(new PSUiReference(parent, "folder1-2", null,
                           null, getFactory(new PSObjectType(
                                 PSObjectTypes.RESOURCE_FILE,
                                 PSObjectTypes.FileSubTypes.FOLDER)), true));
                  }
                  return results;
//               }
//               catch (PSModelException e)
//               {
//                  throw new RuntimeException(e);
//               }
            }

            public PSUiReference createEntry(IPSReference ref)
            {
               throw new UnsupportedOperationException(
                     "Unsupported for local file system.");
            }
         };
         return cat;
      }      
   };

}
