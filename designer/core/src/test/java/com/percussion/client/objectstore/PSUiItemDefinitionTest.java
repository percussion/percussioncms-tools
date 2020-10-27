/******************************************************************************
*
* [ PSUiItemDefinitionTest.java ]
*
* COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
* All rights reserved. This material contains unpublished, copyrighted
* work including confidential and proprietary information of Percussion.
*
*****************************************************************************/
package com.percussion.client.objectstore;

import com.percussion.client.IPSReference;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.PSObjectTypes.TemplateSubTypes;
import com.percussion.client.PSObjectTypes.XmlApplicationSubTypes;
import com.percussion.client.impl.PSReference;
import com.percussion.cms.objectstore.PSContentType;
import com.percussion.design.objectstore.PSContentEditor;
import junit.framework.TestCase;

import java.util.Collections;
import java.util.Set;

public class PSUiItemDefinitionTest extends TestCase
{
   public void testGetSetNewContentTypes() throws PSModelException
   {
      final PSContentType contentType1 =
            new PSContentType(1, "ContentType1", null, null, "../appname/resource",
                  false, -1);
      final PSUiItemDefinition def = new PSUiItemDefinition("app1", contentType1,
            new PSContentEditor("ct", 0, 0));

      // new content types are not specified
      assertNull(def.getNewTemplates());
      assertFalse(def.areNewTemplatesSpecified());

      def.setNewTemplates(Collections.<IPSReference> emptySet());
      assertFalse(def.areNewTemplatesSpecified());
      
      // not persisted reference to a template
      try
      {
         final PSObjectType templateObjectType =
            new PSObjectType(PSObjectTypes.TEMPLATE, TemplateSubTypes.SHARED);
         def.setNewTemplates(Collections
               .<IPSReference>singleton(new PSReference("name", null, null,
                     templateObjectType, null)));
         fail();
      }
      catch (IllegalArgumentException success)
      {
         assertTrue(success.getMessage().contains(def.getName()));
      }

      // new content types are specified
      def.setNewTemplates(
            Collections.<IPSReference>singleton(getTemplateRef()));
      assertTrue(def.areNewTemplatesSpecified());

      // wrong reference type
      try
      {
         final PSObjectType objectType =
               new PSObjectType(PSObjectTypes.XML_APPLICATION,
                     XmlApplicationSubTypes.USER);
         final PSReference ref = new PSReference("name", null, null, objectType,
               null);
         ref.setPersisted();
         def.setNewTemplates(Collections.<IPSReference> singleton(ref));
         fail();
      }
      catch (IllegalArgumentException success)
      {
         assertTrue(success.getMessage().contains(
               PSObjectTypes.XML_APPLICATION.toString()));
      }

      // correct reference type
      final Set<IPSReference> refs = Collections
            .<IPSReference> singleton(getTemplateRef());
      def.setNewTemplates(refs);
      assertEquals(refs, def.getNewTemplates());
      assertTrue(def.areNewTemplatesSpecified());

      // new content types are not specified
      def.setNewTemplates(null);
      assertNull(def.getNewTemplates());
      assertFalse(def.areNewTemplatesSpecified());
   }

   /**
    * A dummy persisted temlate ref for testing.
    */
   private PSReference getTemplateRef() throws PSModelException
   {
      final PSObjectType templateObjectType =
         new PSObjectType(PSObjectTypes.TEMPLATE, TemplateSubTypes.SHARED);
      final PSReference templateRef = new PSReference("name", null, null,
            templateObjectType, null);
      templateRef.setPersisted();
      return templateRef;
   }
}
