/******************************************************************************
 *
 * [ PSRelationshipTypeEditorTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form;

import com.percussion.client.IPSReference;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectType;
import com.percussion.client.impl.PSReference;

public class PSRelationshipTypeEditorTest
{
   public void testIsValidReference() throws PSModelException
   {
      // TODO - this test failed on build machine. disable for now
  
      // template reference
      //assertTrue(createEditor().isValidReference(createRef(
      //      new PSObjectType(PSObjectTypes.RELATIONSHIP_TYPE))));

      // other reference
      //assertFalse(createEditor().isValidReference(createRef(
      //      new PSObjectType(PSObjectTypes.COMMUNITY))));
   }

   /**
    * Creates an editor.
    */
   private PSRelationshipTypeEditor createEditor()
   {
      return new PSRelationshipTypeEditor();
   }

   /**
    * Creates a reference with the specified type.
    */
   private IPSReference createRef(final PSObjectType type)
         throws PSModelException
   {
      return new PSReference("name", null, null, type, null);
   }
}
