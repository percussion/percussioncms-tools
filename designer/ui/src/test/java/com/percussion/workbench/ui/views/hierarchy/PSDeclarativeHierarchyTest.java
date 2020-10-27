/******************************************************************************
 *
 * [ PSDeclarativeHierarchyTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.views.hierarchy;

import com.percussion.client.PSObjectTypeFactory;
import com.percussion.client.PSObjectTypes;
import com.percussion.workbench.ui.PSUiReference;
import com.percussion.workbench.ui.handlers.PSIconNodeHandler;
import com.percussion.workbench.ui.model.IPSDeclarativeNodeHandler;
import junit.framework.TestCase;
import org.eclipse.swt.dnd.Transfer;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

/**
 * This class tests the PSHierarchyDefProcessor and related classes. Several
 * test definitions are used.
 *
 * @author paulhoward
 */
public class PSDeclarativeHierarchyTest extends TestCase
{
   public PSDeclarativeHierarchyTest(String name)
   {
      super(name);
   }

   @Override protected void setUp() throws Exception
   {
      super.setUp();
   }

   /**
    * A tree composed of Node and Catalog elements in many combinations.
    * 
    * @throws Exception
    */
   public void testCatalogNodeTree()
      throws Exception
   {
      Reader reader = new InputStreamReader(getClass().getResourceAsStream(
            "catalogTreeTest_viewHierarchyDef.xml"), "utf-8");
      PSHierarchyDefProcessor proc = new PSHierarchyDefProcessor("test", reader);
      
      List<PSUiReference> cat = proc.getChildren(null);
      
      //<Catalog type="object" name="SLOT"> catalog
      cat = proc.getChildren(cat.get(0));
      assertTrue(cat.size() >= 1);
      assertTrue(cat.get(0).getName().toLowerCase().startsWith("slot"));
      
      //<Catalog type="class" name"classname"> as child of above
      cat = proc.getChildren(cat.get(0));
      assertTrue(cat.size() == 3);
      //check ascending sorting on catalog
      assertTrue(cat.get(0).getName().startsWith("agroup"));
      assertTrue(cat.get(1).getName().startsWith("mgroup"));
      assertTrue(cat.get(2).getName().startsWith("zgroup"));
      
      cat = proc.getChildren(cat.get(0));
      assertTrue(cat.size() == 1);
      assertTrue(cat.get(0).getName().startsWith("subgroup"));
      
      //Test mixture of multiple nodes and multiple catalogs with same parent
      cat = proc.getChildren(null);
      cat = proc.getChildren(cat.get(1));
      assertTrue(cat.size() == 6);
      //first 2 nodes are folders, so they sort highest
      assertTrue(cat.get(0).getName().startsWith("mixed"));
      assertTrue(cat.get(1).getName().startsWith("xmixed"));
      assertTrue(cat.get(2).getName().startsWith("amixed"));
      assertTrue(cat.get(3).getName().startsWith("gmixed"));
      assertTrue(cat.get(4).getName().startsWith("nmixed"));
      assertTrue(cat.get(5).getName().startsWith("zmixed"));
      
      //test alternating Node -> Catalog
      cat = proc.getChildren(null);
      cat = proc.getChildren(cat.get(2));
      assertTrue(cat.size() == 1);
      assertTrue(cat.get(0).getName().startsWith("child1"));
      
      cat = proc.getChildren(cat.get(0));
      assertTrue(cat.size() == 2);
      assertTrue(cat.get(0).getName().startsWith("alt group 1"));
      assertTrue(cat.get(1).getName().startsWith("alt group 1"));

      cat = proc.getChildren(cat.get(0));
      assertTrue(cat.size() == 1);
      assertTrue(cat.get(0).getName().startsWith("catalog child1"));

      cat = proc.getChildren(cat.get(0));
      assertTrue(cat.size() == 1);
      assertTrue(cat.get(0).getName().startsWith("alt group 2"));
      
      // test context parent
      cat = proc.getChildren(null);
      cat = proc.getChildren(cat.get(3));
      cat = proc.getChildren(cat.get(0));
      assertTrue(cat.size() == 1);
      cat = proc.getChildren(cat.get(0));
      assertTrue(cat.size() == 3);
      
   }
   
   /**
    * A tree that contains elements that have hierarchical models.
    * This is the tree structure against which the tests are built, where
    * Resources is the root node.
    * <pre> 
    * Resources
    *   - rx_resources
    *   - sys_resources
    *     - folder1-1
    *       - folder2-1
    *         - folder3-1
    *       - file2-1
    *     - folder1-2
    *       - file2-1
    * </pre>
    * @throws Exception
    */
   public void testHierarchyNodeTree()
      throws Exception
   {
      Reader reader = new InputStreamReader(getClass().getResourceAsStream(
            "hierarchyTreeTest_viewHierarchyDef.xml"), "utf-8");
      PSHierarchyDefProcessor proc = new PSHierarchyDefProcessor("test", reader);
      
      List<PSUiReference> cat = proc.getChildren(null);
      cat = proc.getChildren(cat.get(0));
      //sys_resources and rx_resources
      assertTrue(cat.size() == 2);
      
      cat = proc.getChildren(cat.get(1));
      assertTrue(cat.size() == 2);
      assertTrue(cat.get(0).getName().equals("folder1-1"));
      assertTrue(cat.get(1).getName().equals("folder1-2"));
      
      cat = proc.getChildren(cat.get(0));
      assertTrue(cat.size() == 2);
      assertTrue(cat.get(0).getName().equals("folder2-1"));
      assertTrue(cat.get(1).getName().equals("file2-1"));

      //file shouldn't have any children
      PSUiReference fileRef = cat.get(1);
      List<PSUiReference> cat2 = proc.getChildren(fileRef);
      assertTrue(cat2.size() == 0);
      
      cat = proc.getChildren(cat.get(0));
      assertTrue(cat.size() == 1);
      assertTrue(cat.get(0).getName().equals("folder3-1"));
      
      cat = proc.getChildren(cat.get(0));
      assertTrue(cat.size() == 0);
      
      cat = proc.getChildren(null);
      cat = proc.getChildren(cat.get(0));
      cat = proc.getChildren(cat.get(1));
      cat = proc.getChildren(cat.get(1));
      assertTrue(cat.size() == 1);
      assertTrue(cat.get(0).getName().equals("file2-1"));
      
   }
   
   /**
    * A tree that has nodes that use and override instance trees.
    * 
    * @throws Exception
    */
   public void testInstanceNodeTree()
      throws Exception
   {
      Reader reader = new InputStreamReader(getClass().getResourceAsStream(
            "instanceTreeTest_viewHierarchyDef.xml"), "utf-8");
      PSHierarchyDefProcessor proc = new PSHierarchyDefProcessor("test", reader);
      
      //document order
      List<PSUiReference> cat = proc.getChildren(null);
      assertTrue(cat.size() == 4);
      
      //use of instance tree
      cat = proc.getChildren(cat.get(0));
      assertTrue(cat.size() >= 1);
      
      cat = proc.getChildren(cat.get(0));
      assertTrue(cat.get(0).getName().startsWith("Contained Templates"));
      cat = proc.getChildren(cat.get(0));
      assertTrue(cat.size() == 3);
      assertTrue(cat.get(0).getName().startsWith("exp"));
            
      //override of instance tree w/ attribute
      cat = proc.getChildren(null);
      cat = proc.getChildren(cat.get(1));
      cat = proc.getChildren(cat.get(0));
      assertTrue(cat.size() == 0);
      
      //override of instance tree w/ declared children
      cat = proc.getChildren(null);
      cat = proc.getChildren(cat.get(2));
      cat = proc.getChildren(cat.get(0));
      assertTrue(cat.size() == 2);
      assertTrue(cat.get(0).getName().startsWith("override"));
      
      //instance tree inherits props from node ancestor
      cat = proc.getChildren(null);
      cat = proc.getChildren(cat.get(3));
      assertTrue(cat.size() >= 1);
      cat = proc.getChildren(cat.get(0));
      assertTrue(cat.get(0).getName().startsWith("Contained Templates"));
      cat = proc.getChildren(cat.get(0));
      assertTrue(cat.size() == 3);
      assertTrue(cat.get(0).getName().startsWith("inherited"));
   }
   
   /**
    * A tree composed only of Node elements. Checks various sorting and 
    * cataloging of children.
    * 
    * @throws Exception
    */
   public void testSimpleNodeTree()
      throws Exception
   {
      Reader reader = new InputStreamReader(getClass().getResourceAsStream(
            "simpleTreeTest_viewHierarchyDef.xml"), "utf-8");
      PSHierarchyDefProcessor proc = new PSHierarchyDefProcessor("test", reader);
      
      //document order
      List<PSUiReference> cat = proc.getChildren(null);
      assertTrue(cat.size() == 3);
      assertTrue(cat.get(0).getName().startsWith("zNode"));
      assertTrue(cat.get(1).getName().startsWith("aNode"));
      assertTrue(cat.get(2).getName().startsWith("xNode"));
      
      assertTrue(cat.get(0).getDescription().startsWith("Description "));
      assertTrue(cat.get(1).getProperty("foo").equals("bar"));
            
      //no children in node
      PSUiReference refNoChildren = cat.get(2);
      assertTrue(proc.getChildren(refNoChildren).size() == 0);
      
      PSUiReference ref = cat.get(1);
      cat = proc.getChildren(ref);
      assertTrue(cat.size() == 2);
      
      //descending sort
      ref = cat.get(0);
      cat = proc.getChildren(ref);
      assertTrue(cat.size() == 2);
      assertTrue(cat.get(0).getName().startsWith("zNode"));
      assertTrue(cat.get(1).getName().startsWith("Node"));
      
      ref = cat.get(1);
      cat = proc.getChildren(ref);
      assertTrue(cat.size() == 1);
      
      //ascending sort
      ref = cat.get(0);
      cat = proc.getChildren(ref);
      assertTrue(cat.size() == 4);
      assertTrue(cat.get(0).getName().startsWith("zNode"));
      assertTrue(cat.get(1).getName().startsWith("aNode") 
            || cat.get(1).getName().startsWith("ANode"));
      assertTrue(cat.get(2).getName().startsWith("aNode") 
            || cat.get(2).getName().startsWith("ANode"));
      assertTrue(cat.get(3).getName().startsWith("ZNode"));      
   }
   
   /**
    * Verifies that the declared node handler is loaded and that it inherits
    * allowed types and icon specs properly.
    */
   public void testNodeHandler()
      throws Exception
   {
      Reader reader = new InputStreamReader(getClass().getResourceAsStream(
            "catalogTreeTest_viewHierarchyDef.xml"), "utf-8");
      PSHierarchyDefProcessor proc = new PSHierarchyDefProcessor("test",
            reader);

      //load handler that inherits allowedTypes from ancestor
      List<PSUiReference> cat = proc.getChildren(null);
      PSUiReference node = cat.get(0);
      IPSDeclarativeNodeHandler handler = node.getHandler();
      assertNotNull(handler);
      assertTrue(handler instanceof TestHandler);
      Transfer[] allowedTypes = handler.getAcceptedTransfers();
      assertTrue(allowedTypes.length == 2);
      Transfer templateType = handler.getTransfer(
            PSObjectTypeFactory.getType(PSObjectTypes.TEMPLATE,
                  PSObjectTypes.TemplateSubTypes.SHARED));
      Transfer folderType = handler.getTransfer(
            PSObjectTypeFactory.getType(PSObjectTypes.USER_FILE,
                  PSObjectTypes.UserFileSubTypes.WORKBENCH_FOLDER));
      assertTrue(allowedTypes[0].equals(templateType)
            || allowedTypes[0].equals(folderType));
      assertTrue(allowedTypes[1].equals(templateType)
            || allowedTypes[1].equals(folderType));
      assertFalse(allowedTypes[0].equals(allowedTypes[1]));

      //default handler
      node = cat.get(1);
      handler = node.getHandler();
      assertNotNull(handler);
      assertTrue(handler instanceof PSIconNodeHandler);
      assertTrue(handler.getAcceptedTransfers().length == 0);
      
      //handler declared in InstanceTree picks up allowed types from context parent
      List<PSUiReference> cat2 = proc.getChildren(cat.get(3));
      node = cat2.get(0);
      handler = node.getHandler();
      assertNotNull(handler);
      assertTrue(handler instanceof PSIconNodeHandler);
      allowedTypes = handler.getAcceptedTransfers();
      assertTrue(allowedTypes.length == 1);
      assertTrue(allowedTypes[0].equals(folderType));

      cat2 = proc.getChildren(cat.get(4));
      node = cat2.get(0);
      handler = node.getHandler();
      assertNotNull(handler);
      assertTrue(handler instanceof PSIconNodeHandler);
      allowedTypes = handler.getAcceptedTransfers();
      assertTrue(allowedTypes.length == 1);
      assertTrue(allowedTypes[0].equals(templateType));
      
      //handler overrides inherited types
      cat = proc.getChildren(cat.get(0));
      handler = cat.get(0).getHandler();
      assertNotNull(handler);
      allowedTypes = handler.getAcceptedTransfers();
      assertTrue(allowedTypes.length == 1);
      Transfer slotType = handler.getTransfer(
            PSObjectTypeFactory.getType(PSObjectTypes.SLOT));
      assertTrue(allowedTypes[0].equals(slotType));
   }
}
