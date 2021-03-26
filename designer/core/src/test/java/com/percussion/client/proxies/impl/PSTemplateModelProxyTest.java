/******************************************************************************
 *
 * [ PSTemplateModelProxyTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.proxies.impl;

import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSModelException;
import com.percussion.client.PSMultiOperationException;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.PSObjectTypes.TemplateSubTypes;
import com.percussion.client.objectstore.PSUiAssemblyTemplate;
import com.percussion.client.proxies.IPSCmsModelProxy;
import com.percussion.services.assembly.IPSAssemblyTemplate.OutputFormat;
import com.percussion.services.assembly.IPSAssemblyTemplate.TemplateType;
import com.percussion.utils.testing.IntegrationTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.ArrayList;
import java.util.List;

import static com.percussion.extension.IPSExtension.LEGACY_ASSEMBLER;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

/**
 * @author Andriy Palamarchuk
 */
@Category(IntegrationTest.class)
public class PSTemplateModelProxyTest{

   @Test
   public void testCreate() throws PSModelException, PSMultiOperationException
   {
      final List<String> names = new ArrayList<String>();
      names.add("name");

      final IPSCmsModelProxy proxy = PSCoreFactory.getInstance()
            .getCmsModelProxy(PSObjectTypes.TEMPLATE);

      // wrong type
      try
      {
         proxy.create(new PSObjectType(PSObjectTypes.SLOT), names,
            new ArrayList<IPSReference>());
         fail();
      }
      catch (IllegalArgumentException success)
      {
      }

      // OTHER subtype
      try
      {
         proxy.create(new PSObjectType(PSObjectTypes.TEMPLATE,
            TemplateSubTypes.OTHER), names, new ArrayList<IPSReference>());
         fail();
      }
      catch (IllegalArgumentException success) {}
      
      // check correspondence of object subtypes to template types
      assertObjSubTypeCreatesTemplateType(proxy, TemplateSubTypes.LOCAL,
         TemplateType.Local);
      assertObjSubTypeCreatesTemplateType(proxy, TemplateSubTypes.SHARED,
         TemplateType.Shared);
      
      // GLOBAL subtype
      {
         final List<PSUiAssemblyTemplate> results =
               new ArrayList<PSUiAssemblyTemplate>();
         final IPSReference[] refs = proxy.create(
               new PSObjectType(PSObjectTypes.TEMPLATE, TemplateSubTypes.GLOBAL),
               names, results);
         assertEquals(OutputFormat.Global, results.get(0).getOutputFormat());
         assertEquals(TemplateSubTypes.GLOBAL,
               refs[0].getObjectType().getSecondaryType());
         assertFalse(results.get(0).isVariant());
      }

      // VARIANT subtype
      final List<PSUiAssemblyTemplate> results =
            new ArrayList<PSUiAssemblyTemplate>();
      final IPSReference[] refs = proxy.create(
            new PSObjectType(PSObjectTypes.TEMPLATE, TemplateSubTypes.VARIANT),
            names, results);
      assertTrue(results.get(0).isVariant());
      assertEquals(LEGACY_ASSEMBLER, results.get(0).getAssembler());
      assertEquals(TemplateSubTypes.VARIANT,
            refs[0].getObjectType().getSecondaryType());
   }

   /**
    * Asserts that specified object type creates template with the provided
    * template type.
    * 
    * @throws PSMultiOperationException
    */
   private void assertObjSubTypeCreatesTemplateType(
         final IPSCmsModelProxy proxy, final TemplateSubTypes objectSubType,
         final TemplateType templateType)
      throws PSMultiOperationException, PSModelException
   {
      final List<String> names = new ArrayList<String>();
      names.add("name");
      final List results = new ArrayList();
      final IPSReference[] refs = proxy.create(
            new PSObjectType(PSObjectTypes.TEMPLATE, objectSubType),
            names, results);
      assertEquals(templateType,
            ((PSUiAssemblyTemplate) results.get(0)).getTemplateType());
      assertEquals(names.size(), refs.length);
      assertEquals(objectSubType, refs[0].getObjectType().getSecondaryType());
   }
}
