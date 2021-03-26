/*******************************************************************************
 *
 * [ PSDesignObjectHierarchyTest.java ]
 * 
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.workbench.ui.model;

import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.models.IPSCmsModel;
import com.percussion.workbench.ui.PSModelTracker;
import com.percussion.workbench.ui.PSUiReference;
import com.percussion.workbench.ui.model.IPSHierarchyChangeListener.HierarchyChangeType;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * These tests depend on the model test proxies to function correctly.
 *
 * @author paulhoward
 */
public class PSDesignObjectHierarchyTest extends TestCase
{
   /**
    * Restore to initial state for each test.
    */
   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      PSCoreFactory.getInstance().clearModelProxyCache();
      PSDesignObjectHierarchy.resetInstance();
   }


   /**
    * Tests both getChildren methods by doing some simple cataloging.
    */
   public void testGetChildren()
   {
      try
      {
         PSDesignObjectHierarchy model = PSDesignObjectHierarchy.getInstance();
         
         try
         {
            model.getChildren((String) null);
            fail("Contract violated");
         }
         catch (IllegalArgumentException success)
         {}
         
         try
         {
            model.getChildren((PSUiReference) null);
            fail("Contract violated");
         }
         catch (IllegalArgumentException success)
         {}

         String defName = "assembly";
         //name case shouldn't matter
         Collection<PSUiReference> children1 = model.getChildren(defName);
         Collection<PSUiReference> children2 = model.getChildren(defName
               .toUpperCase());
         assertTrue(children1.size() > 0);
         assertTrue(children1.size() == children2.size());
         
         PSUiReference slotNode = null;
         //don't cache nodes
         for (PSUiReference node1 : children1)
         {
            boolean found = false;
            if (node1.getDisplayLabel().equalsIgnoreCase("slots"))
               slotNode = node1;
            for (PSUiReference node2 : children2)
            {
               if (node1.equals(node2))
                  found = true;
               if (node1 == node2)
                  fail("Shouldn't allow caching");
            }
            if (!found)
               fail("Matching node not found for same parent catalog");
         }
         
         assertTrue("Test config changed, update test.", slotNode != null);
         
         List<PSUiReference> slotChildren = model.getChildren(slotNode);
         assertTrue(slotChildren.size() > 0);
      }
      catch (PSModelException e)
      {
         e.printStackTrace();
         fail("Configuration problem?");
      }
   }
      
   /**
    * This tests both the convenience method that gets a single node and the
    * getNodes method.
    */
   public void testGetNodes()
   {
      try
      {
         PSDesignObjectHierarchy model = PSDesignObjectHierarchy.getInstance();
         
         PSUiReference node = model.getNode("/slots/slot0");
         assertTrue(node != null);
         
         //should come from cache
         PSUiReference node2 = model.getNode("/slots/slot0");
         assertTrue(node == node2);
         
         //clear cache
         PSDesignObjectHierarchy.resetInstance();
         model = PSDesignObjectHierarchy.getInstance();
         
         //add tree name
         String basePath = "/slots/slot folder 4_0/slot1";
         String path = "//assembly" + basePath;
         PSUiReference node3 = model.getNode(path);
         assertTrue(node3 != null);
         
         // case insensitive
         node = model.getNode(path.toUpperCase());
         assertTrue(node != null);

         //repeat same node
         node = model.getNode(basePath);
         assertTrue(node == node3);
         
         //check for inline node
         node = model.getNode("/slots/inline/slot5");
         assertTrue(node != null);
         
         String name0 = "slot2";
         String name1 = "SLots";
         String name2 = "slot0"; 
         String[] paths = 
         {
            "/slots/slot folder 4_0/slot folder 4_1/" + name0,
            "/" + name1,
            "//assembly/slots/" + name2
         };
         List<PSUiReference> results = model.getNodes(paths);
         assertTrue(results.get(0).getDisplayLabel().equalsIgnoreCase(name0));
         assertTrue(results.get(1).getDisplayLabel().equalsIgnoreCase(name1));
         assertTrue(results.get(2).getDisplayLabel().equalsIgnoreCase(name2));
      }
      catch (PSModelException e)
      {
         e.printStackTrace();
         fail("Configuration problem?");
      }
   }

   /**
    * Used to test the notification functionality.
    */
   private class testListener implements IPSHierarchyChangeListener
   {
      /**
       * When the notification is received, the type is placed in the supplied
       * array at the specified index within the array.
       * 
       * @param notify Assumed not <code>null</code> and length >= index+1;
       * 
       * @param index Where to store the result when the
       * {@link #changeOccurred(HierarchyChangeType, PSUiReference[], 
       * PSUiReference[])} method is called.
       */
      public testListener(HierarchyChangeType[] notify, int index)
      {
         m_notify = notify;
         m_index = index;
      }
      
      @SuppressWarnings("unused")
      public void changeOccurred(HierarchyChangeType type,
            PSUiReference[] nodes, PSUiReference[] sourceParents)
      {
         m_notify[m_index] = type;
      }
      
      final int m_index;
      final HierarchyChangeType[] m_notify;
   }
   
   /**
    * This test adds some listeners, then causes an action that should send a 
    * notification, validating that the listener got the correct type and then
    * removes a listener and verifies it doesn't get notified any more.
    */
   public void testAddListeners()
   {
      try
      {
         PSDesignObjectHierarchy model = PSDesignObjectHierarchy.getInstance();
   
         /* need to load some nodes before the model will notify anyone */
         model.getNode("/slots/slot0");
         
         //use to communicate w/ listeners
         final HierarchyChangeType[] notify = new HierarchyChangeType[4];

         //validate contract
         try
         {
            model.addListener(new testListener(notify, 0), "foo");
            fail("Contract violation");
         }
         catch (IllegalArgumentException success)
         {}

         final String createName = "test";
         String treeName = "assembly";
         IPSHierarchyChangeListener listener = new testListener(notify, 0); 
         model.addListener(listener, treeName);
         
         //add one to make sure both get called
         model.addListener(new testListener(notify, 1), treeName.toUpperCase());

         //add one to make sure generic registration gets called
         model.addListener(new testListener(notify, 2), null);
         
         //add one to make sure it doesn't get called
         model.addListener(new testListener(notify, 3), "uielements");
      
         IPSCmsModel coreModel = 
            PSCoreFactory.getInstance().getModel(PSObjectTypes.SLOT);
         IPSReference testSlotRef = PSModelTracker.getInstance().create(
               new PSObjectType(PSObjectTypes.SLOT), createName, null);
         PSModelTracker.getInstance().propertyChanged(testSlotRef, null);
         assertTrue(notify[0] == HierarchyChangeType.NODE_MODIFIED);
         assertTrue(notify[1] == HierarchyChangeType.NODE_MODIFIED);
         assertTrue(notify[2] == HierarchyChangeType.NODE_MODIFIED);
         assertTrue(notify[3] == null);
         notify[0] = notify[1] = notify[2] = null;
         
         coreModel.rename(testSlotRef, "test2");
         assertTrue(notify[0] == HierarchyChangeType.NODE_MODIFIED);
         assertTrue(notify[1] == HierarchyChangeType.NODE_MODIFIED);
         assertTrue(notify[2] == HierarchyChangeType.NODE_MODIFIED);
         assertTrue(notify[3] == null);
         notify[0] = notify[1] = notify[2] = null;
         
         model.removeListener(listener, treeName.toUpperCase());
         coreModel.rename(testSlotRef, "test3");
         assertTrue(notify[0] == null);
         assertTrue(notify[1] == HierarchyChangeType.NODE_MODIFIED);
         assertTrue(notify[2] == HierarchyChangeType.NODE_MODIFIED);
         assertTrue(notify[3] == null);
         notify[0] = notify[1] = notify[2] = null;
         
         PSModelTracker.getInstance().save(testSlotRef, true, true);
         coreModel.delete(testSlotRef);
         assertTrue(notify[1] == HierarchyChangeType.NODE_DELETED);
         assertTrue(notify[2] == HierarchyChangeType.NODE_DELETED);
         assertTrue(notify[3] == null);
         
      }
      catch (Exception e)
      {
         e.printStackTrace();
         fail("Configuration problem?");
      }
   }
   
   /**
    * Tests the {@link PSDesignObjectHierarchy#move(PSUiReference, List)}
    * method. Test the following scenarios:
    * <ol>
    *    <li>Move a folder to another folder</li>
    *    <li>Move a slot to another folder (placeholder->placehoder)</li>
    *    <li>Move a slot to another folder (non-placeholder->placehoder)</li>
    *    <li>Move a slot to another folder (placeholder->non-placehoder)</li>
    *    <li>Move a combination to USER_FILE root</li>
    *    <li>Move a combination from USER_FILE root</li>
    * <ol>
    *
    * We have the following folder structure in a clean test environment.
    * 
    * Slots 
    *    Inline
    *       SLOT5
    *    Navigation
    *       SLOT6
    *       SLOT7
    *    Slot Folder 1_0 
    *    Slot Folder 2_0 
    *    Slot Folder 3_0 
    *    Slot Folder 4_0 
    *       Slot Folder 4_1 
    *          Slot Folder 4_2 
    *             SLOT4 (Slot Placeholder 004)
    *          SLOT2 (Slot Placeholder 002) 
    *       SLOT1 (Slot Placeholder 001)
    *       SLOT3 (Slot Placeholder 003)
    *    SLOT0
    */
   public void testMove()
      throws Exception
   {
      PSDesignObjectHierarchy model = PSDesignObjectHierarchy.getInstance();
      
      String[][] testPaths = 
      {
            //{sourceParentPath, targetParentPath, baseName}

            //move 'Slot Folder 1_0' -> 'Slot Folder 2_0'
            {"/Slots/", "/Slots/Slot folder 2_0/", "Slot Folder 1_0"},

            //move SLOT0 -> 'Slot Folder 2_0'
            {"/slots/", "/slots/slot folder 2_0/", "slot0"},

            //move SLOT4 -> Slots
            {"/Slots/Slot folder 4_0/slot folder 4_1/slot folder 4_2/", "/Slots/", "SLOT4"},
            
            //move SLOT1 -> 'Slot Folder 3_0'
            {"/slots/slot folder 4_0/", "/slots/slot folder 3_0/", "slot1"},
      };
      
      for (int i = 0; i < testPaths.length; i++)
      {
         try
         {
            System.out.println("processing " + i);
            String sourceParentPath = testPaths[i][0];
            String targetParentPath = testPaths[i][1];
            String baseName = testPaths[i][2];
            PSUiReference sourceParent = model.getNode(sourceParentPath);
            PSUiReference targetParent = model.getNode(targetParentPath);
            int sourceChildCount = sourceParent.getChildren().size();
            int targetChildCount = targetParent.getChildren().size();

            List<PSUiReference> children = new ArrayList<PSUiReference>();
            assertNotNull(model.getNode(sourceParentPath + baseName));
            children.add(model.getNode(sourceParentPath + baseName));
            model.move(targetParent, children);

            String msg = "failed on index " + i;
            assertTrue(msg, sourceParent.getChildren().size() == sourceChildCount-1);
            assertTrue(msg, targetParent.getChildren().size() == targetChildCount+1);
            assertTrue(msg, model.getNode(targetParentPath + baseName) != null);
            assertTrue(msg, model.getNode(sourceParentPath + baseName) == null);
            System.out.println("index " + i + " successfully moved");
         }
         finally
         {
            //restore the moved node
            System.out.println("Restoring " + i);
            String sourceParentPath = testPaths[i][1];
            String targetParentPath = testPaths[i][0];
            String baseName = testPaths[i][2];
            PSUiReference targetParent = model.getNode(targetParentPath);

            List<PSUiReference> children = new ArrayList<PSUiReference>();
            children.add(model.getNode(sourceParentPath + baseName));
            model.move(targetParent, children);
         }
      }

      //test multi-object move
      testPaths = new String[][] 
      {
            //{sourceParentPath, targetParentPath, unused}

            //move all children of 'Slot Folder 4_1' -> Slots
            {"/slots/slot folder 4_0/Slot Folder 4_1", "/Slots/", ""},

            //move SLOT1, 'Slot Folder 3_0' -> 'Slot Folder 4_1' 
            {"/slots/slot folder 4_0/", "/slots/slot folder 3_0/", "slot1"},
      };
      
      for (int i = 0; i < testPaths.length; i++)
      {
         List<PSUiReference> children = null;
         try
         {
            String sourceParentPath = testPaths[i][0];
            PSUiReference sourceParent = model.getNode(sourceParentPath);
            PSUiReference targetParent = model.getNode(testPaths[i][1]);
            int sourceChildCount = sourceParent.getChildren().size();
            assert(sourceChildCount > 0);
            
            int targetChildCount = targetParent.getChildren().size();
            children = model.getChildren(sourceParent);
            model.move(targetParent, children);
            assertTrue(sourceParent.getChildren().size() == 0);
            String msg = "failed on index " + i;
            assertTrue(msg, targetParent.getChildren().size() == targetChildCount
                  + children.size());
            for (PSUiReference node : children)
            {
               String path = testPaths[i][1] + node.getDisplayLabel();
               assertNotNull(msg, model.getNode(path));
               path = testPaths[i][0] + node.getDisplayLabel();
               assertNull(msg, model.getNode(path));
            }
         }
         finally
         {
            if (children != null)
            {
               //restore nodes
               System.out.println("Restoring " + i);
               for (PSUiReference node : children)
               {
                  PSUiReference targetParent = model.getNode(testPaths[i][0]);
                  String srcPath = testPaths[i][1] + node.getDisplayLabel();
                  PSUiReference child = model.getNode(srcPath);
                  model.move(targetParent, Collections.singletonList(child));
               }
            }
         }
      }
   }
}
