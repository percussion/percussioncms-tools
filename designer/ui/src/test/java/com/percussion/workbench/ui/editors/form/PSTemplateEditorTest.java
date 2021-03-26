/******************************************************************************
 *
 * [ PSTemplateEditorTest.java ]
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
import com.percussion.client.PSObjectTypes;
import com.percussion.client.PSObjectTypes.TemplateSubTypes;
import com.percussion.client.impl.PSReference;
import com.percussion.workbench.ui.PSUiTestBase;

public class PSTemplateEditorTest extends PSUiTestBase
{
   public void testIsValidReference() throws PSModelException
   {
      // template reference
      assertTrue(createEditor().isValidReference(createRef(
            new PSObjectType(PSObjectTypes.TEMPLATE, TemplateSubTypes.SHARED))));

      // other reference
      assertFalse(createEditor().isValidReference(createRef(
            new PSObjectType(PSObjectTypes.COMMUNITY))));
   }
   
   /**
    * Creates an editor.
    */
   private PSTemplateEditor createEditor()
   {
      return new PSTemplateEditor();
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
