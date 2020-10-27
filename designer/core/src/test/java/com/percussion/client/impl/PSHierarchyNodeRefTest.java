/******************************************************************************
 *
 * [ PSHierarchyNodeRefTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.impl;

import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypes;
import junit.framework.TestCase;

/**
 * Unit tests for {@link com.percussion.client.impl.PSHierarchyNodeRef}.
 *
 * @author paulhoward
 */
public class PSHierarchyNodeRefTest extends TestCase
{
   public void testName()
      throws Exception
   {
      PSHierarchyNodeRef testRef = new PSHierarchyNodeRef(null, "root", 
            new PSObjectType(PSObjectTypes.RESOURCE_FILE, 
                  PSObjectTypes.FileSubTypes.FOLDER), null, true);
      //verify spaces allowed
      String name = "foo bar";
      testRef.setName(name);
      assertTrue(testRef.getName().equals(name));
      
      try
      {
         testRef.setName("bar\\");
         fail("contract violated");
      }
      catch (IllegalArgumentException success)
      {}
   }
   
   /**
    * Performs an identity check and a check against multiple instances that are
    * the same and different.
    * @throws Exception
    */
   public void testReferencesSameObject()
      throws Exception
   {
      PSHierarchyNodeRef testRef1 = new PSHierarchyNodeRef(null, "root", 
            new PSObjectType(PSObjectTypes.RESOURCE_FILE, 
                  PSObjectTypes.FileSubTypes.FOLDER), null, true);
      PSHierarchyNodeRef testRef11 = new PSHierarchyNodeRef(testRef1, "folder1", 
            new PSObjectType(PSObjectTypes.RESOURCE_FILE, 
                  PSObjectTypes.FileSubTypes.FOLDER), null, true);
      PSHierarchyNodeRef testRef11a = new PSHierarchyNodeRef(testRef1, "folder1", 
            new PSObjectType(PSObjectTypes.RESOURCE_FILE, 
                  PSObjectTypes.FileSubTypes.FOLDER), null, true);
      PSHierarchyNodeRef testRef2 = new PSHierarchyNodeRef(null, "root", 
            new PSObjectType(PSObjectTypes.RESOURCE_FILE, 
                  PSObjectTypes.FileSubTypes.FOLDER), null, true);
      PSHierarchyNodeRef testRef21 = new PSHierarchyNodeRef(testRef2, "folder1", 
            new PSObjectType(PSObjectTypes.RESOURCE_FILE, 
                  PSObjectTypes.FileSubTypes.FOLDER), null, true);
      PSHierarchyNodeRef testRef3 = new PSHierarchyNodeRef(null, "root2", 
            new PSObjectType(PSObjectTypes.RESOURCE_FILE, 
                  PSObjectTypes.FileSubTypes.FOLDER), null, true);
      PSHierarchyNodeRef testRef31 = new PSHierarchyNodeRef(testRef3, "folder1", 
            new PSObjectType(PSObjectTypes.RESOURCE_FILE, 
                  PSObjectTypes.FileSubTypes.FOLDER), null, true);
      assertTrue(testRef1.referencesSameObject(testRef1));
      assertTrue(testRef1.referencesSameObject(testRef2));
      assertTrue(testRef11.referencesSameObject(testRef11a));
      assertTrue(testRef11.referencesSameObject(testRef21));
      assertFalse(testRef1.referencesSameObject(testRef3));
      assertFalse(testRef11.referencesSameObject(testRef31));
   }
   
   /**
    * Builds a simple tree and checks the path for each node.
    * 
    * @throws Exception
    */
   public void testGetPath()
      throws Exception
   {
      PSHierarchyNodeRef ref0 = new PSHierarchyNodeRef(null, "root", 
            new PSObjectType(PSObjectTypes.RESOURCE_FILE, 
                  PSObjectTypes.FileSubTypes.FOLDER), null, true);
      PSHierarchyNodeRef ref11 = new PSHierarchyNodeRef(ref0, "folder11", 
            new PSObjectType(PSObjectTypes.RESOURCE_FILE, 
                  PSObjectTypes.FileSubTypes.FOLDER), null, true);
      PSHierarchyNodeRef ref12 = new PSHierarchyNodeRef(ref0, "folder 12", 
            new PSObjectType(PSObjectTypes.RESOURCE_FILE, 
                  PSObjectTypes.FileSubTypes.FOLDER), null, true);
      PSHierarchyNodeRef ref13 = new PSHierarchyNodeRef(ref0, "folder13", 
            new PSObjectType(PSObjectTypes.RESOURCE_FILE, 
                  PSObjectTypes.FileSubTypes.FOLDER), null, true);
      PSHierarchyNodeRef ref21 = new PSHierarchyNodeRef(ref12, "file21", 
            new PSObjectType(PSObjectTypes.RESOURCE_FILE, 
                  PSObjectTypes.FileSubTypes.FILE), null, false);
      PSHierarchyNodeRef ref22 = new PSHierarchyNodeRef(ref12, "file 22", 
            new PSObjectType(PSObjectTypes.RESOURCE_FILE, 
                  PSObjectTypes.FileSubTypes.FILE), null, false);
      PSHierarchyNodeRef ref23 = new PSHierarchyNodeRef(ref12, "folder23", 
            new PSObjectType(PSObjectTypes.RESOURCE_FILE, 
                  PSObjectTypes.FileSubTypes.FOLDER), null, true);
      
      assertTrue(ref0.getPath().equals("/root"));
      assertTrue(ref11.getPath().equals("/root/folder11"));
      assertTrue(ref13.getPath().equals("/root/folder13"));
      assertTrue(ref21.getPath().equals("/root/folder 12/file21"));
      assertTrue(ref22.getPath().equals("/root/folder 12/file 22"));
      assertTrue(ref23.getPath().equals("/root/folder 12/folder23"));
   }
   
   public void testEqualsAndHash()
      throws Exception
   {
      //identical
      PSHierarchyNodeRef ref1 = new PSHierarchyNodeRef(null, "root", 
            new PSObjectType(PSObjectTypes.RESOURCE_FILE, 
                  PSObjectTypes.FileSubTypes.FOLDER), null, true);
      PSHierarchyNodeRef ref2 = new PSHierarchyNodeRef(null, "root", 
            new PSObjectType(PSObjectTypes.RESOURCE_FILE, 
                  PSObjectTypes.FileSubTypes.FOLDER), null, true);
      assertTrue(ref1.equals(ref2));
      assertTrue(ref1.hashCode() == ref2.hashCode());
      
      //different container type
      ref1 = new PSHierarchyNodeRef(null, "root", 
            new PSObjectType(PSObjectTypes.RESOURCE_FILE, 
                  PSObjectTypes.FileSubTypes.FOLDER), null, true);
      ref2 = new PSHierarchyNodeRef(null, "root", 
            new PSObjectType(PSObjectTypes.RESOURCE_FILE, 
                  PSObjectTypes.FileSubTypes.FILE), null, false);
      assertFalse(ref1.equals(ref2));
      
      //different names
      PSHierarchyNodeRef rootChild1 = new PSHierarchyNodeRef(ref1,
            "child", new PSObjectType(PSObjectTypes.RESOURCE_FILE,
                  PSObjectTypes.FileSubTypes.FILE), null, false);
      PSHierarchyNodeRef rootChild2 = new PSHierarchyNodeRef(ref1,
            "child2", new PSObjectType(PSObjectTypes.RESOURCE_FILE,
                  PSObjectTypes.FileSubTypes.FILE), null, false);
      assertFalse(rootChild1.equals(rootChild2));
      
      //different types
      ref1 = new PSHierarchyNodeRef(null, "root", 
            new PSObjectType(PSObjectTypes.RESOURCE_FILE, 
                  PSObjectTypes.FileSubTypes.FOLDER), null, true);
      ref2 = new PSHierarchyNodeRef(null, "root", 
            new PSObjectType(PSObjectTypes.RESOURCE_FILE, 
                  PSObjectTypes.FileSubTypes.FILE), null, true);
      assertFalse(ref1.equals(ref2));
   }
}
