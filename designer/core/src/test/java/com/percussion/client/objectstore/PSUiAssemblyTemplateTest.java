/******************************************************************************
 *
 * [ PSUiAssemblyTemplateTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.objectstore;

import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.PSObjectTypes.XmlApplicationSubTypes;
import com.percussion.client.impl.PSReference;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.data.PSTemplateBinding;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.utils.guid.IPSGuid;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.percussion.extension.IPSExtension.LEGACY_ASSEMBLER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class PSUiAssemblyTemplateTest
{
   /**
    * Attempts to set and retrieve some slot ids.
    */
   @Test
   public void testGetSetSlotGuids()
   {
      final PSUiAssemblyTemplate template = new PSUiAssemblyTemplate();
      Set<IPSGuid> empty = template.getSlotGuids();
      assertNotNull(empty);

      IPSGuid test1Id = new PSGuid(1L, PSTypeEnum.SLOT, 1L);
      IPSGuid test2Id = new PSGuid(1L, PSTypeEnum.SLOT, 2L);

      Set<IPSGuid> slotIds = new HashSet<IPSGuid>();
      slotIds.add(test1Id);
      template.setSlotGuids(slotIds);

      Set<IPSGuid> result = template.getSlotGuids();
      assertTrue(result.size() == 1);
      assertTrue(result.iterator().next().equals(test1Id));

      template.setSlotGuids(null);
      result = template.getSlotGuids();
      assertTrue(result.size() == 0);

      slotIds = new HashSet<IPSGuid>();
      slotIds.add(test1Id);
      slotIds.add(test2Id);
      template.setSlotGuids(slotIds);
      assertTrue(template.getSlotGuids().size() == 2);
   }

   @Test
   public void testGetSetNewContentTypes() throws PSModelException
   {
      final PSUiAssemblyTemplate template = new PSUiAssemblyTemplate();
      template.setName("Template1");

      // new content types are not specified
      assertNull(template.getNewContentTypes());
      assertFalse(template.areNewContentTypesSpecified());

      template.setNewContentTypes(Collections.<IPSReference> emptySet());
      assertFalse(template.areNewContentTypesSpecified());

      // not persisted reference to a content type
      try
      {
         template.setNewContentTypes(Collections
               .<IPSReference>singleton(new PSReference("name", null, null,
                     new PSObjectType(PSObjectTypes.CONTENT_TYPE), null)));
         fail();
      }
      catch (IllegalArgumentException success)
      {
         assertTrue(success.getMessage().contains(template.getName()));
      }

      // new content types are specified
      template.setNewContentTypes(
            Collections.<IPSReference>singleton(getContentTypeRef()));
      assertTrue(template.areNewContentTypesSpecified());

      // wrong reference type
      try
      {
         final PSObjectType objectType =
               new PSObjectType(PSObjectTypes.XML_APPLICATION,
                     XmlApplicationSubTypes.USER);
         final PSReference ref =
               new PSReference("name", null, null, objectType, null);
         ref.setPersisted();
         template.setNewContentTypes(Collections.<IPSReference>singleton(ref));
         fail();
      }
      catch (IllegalArgumentException success)
      {
         assertTrue(success.getMessage().contains(
               PSObjectTypes.XML_APPLICATION.toString()));
      }

      // correct reference type
      final Set<IPSReference> refs =
            Collections.<IPSReference>singleton(getContentTypeRef());
      template.setNewContentTypes(refs);
      assertEquals(refs, template.getNewContentTypes());
      assertTrue(template.areNewContentTypesSpecified());

      // new content types are not specified
      template.setNewContentTypes(null);
      assertNull(template.getNewContentTypes());
      assertFalse(template.areNewContentTypesSpecified());
   }

   /**
    * A dummy persisted content type ref for testing.
    */
   private PSReference getContentTypeRef() throws PSModelException
   {
      final PSReference contentTypeRef = new PSReference("name", null, null,
            new PSObjectType(PSObjectTypes.CONTENT_TYPE), null);
      contentTypeRef.setPersisted();
      return contentTypeRef;
   }

   private void setupBindingData(IPSAssemblyTemplate var)
   {
      List<PSTemplateBinding> bindings = new ArrayList<PSTemplateBinding>();
      bindings.add(new PSTemplateBinding(1, "x", "y * z"));
      bindings.add(new PSTemplateBinding(2, "w", "x  / 3"));
      var.setBindings(bindings);
   }

   private void setupTemplateData(IPSAssemblyTemplate var)
         throws PSModelException
   {
      String name = "test_1";

      var.setActiveAssemblyType(IPSAssemblyTemplate.AAType.NonHtml);
      var.setAssembler(LEGACY_ASSEMBLER);
      var.setAssemblyUrl("myassemblyurl");
      var.setDescription("Test variant");
      var.setLocationPrefix("prefix");
      var.setLocationSuffix("suffix");
      var.setName(name);
      var.setLabel(name);
      var.setTemplateType(IPSAssemblyTemplate.TemplateType.Shared);
      var.setOutputFormat(IPSAssemblyTemplate.OutputFormat.Page);
      var.setPublishWhen(IPSAssemblyTemplate.PublishWhen.Always);
      var
            .setGlobalTemplateUsage(IPSAssemblyTemplate.GlobalTemplateUsage.Defined);
      var.setStyleSheetPath("My template");

      setupSlotData(var, 2);
      setupSiteData(var, 3);
      setupContentTypeData(var,4);
   }

   private void setupSlotData(IPSAssemblyTemplate var, int count)
         throws PSModelException
   {
      PSCoreFactory core = PSCoreFactory.getInstance();
      List<IPSReference> slots = (List<IPSReference>) core.getModel(
            PSObjectTypes.SLOT).catalog();
      PSUiAssemblyTemplate template = (PSUiAssemblyTemplate) var;

      for (int i = 0; i < slots.size() && i < count; i++)
      {
         template.addTemplateSlotId(slots.get(i).getId().longValue());
      }
   }

   private void setupSiteData(IPSAssemblyTemplate var, int count)
         throws PSModelException
   {
      PSCoreFactory core = PSCoreFactory.getInstance();
      List<IPSReference> sites = (List<IPSReference>) core.getModel(
            PSObjectTypes.SITE).catalog();
      PSUiAssemblyTemplate template = (PSUiAssemblyTemplate) var;

      for (int i = 0; i < sites.size() && i < count; i++)
      {
         template.addSiteId(sites.get(i).getId().longValue());
      }
   }

   private void setupContentTypeData(IPSAssemblyTemplate var, int count)
         throws PSModelException
   {
      PSCoreFactory core = PSCoreFactory.getInstance();
      List<IPSReference> defs = (List<IPSReference>) core.getModel(
            PSObjectTypes.CONTENT_TYPE).catalog();
      PSUiAssemblyTemplate template = (PSUiAssemblyTemplate) var;

      for (int i = 0; i < defs.size() && i < count; i++)
      {
         template.addContentId(defs.get(i).getId().longValue());
      }
   }

   /**
    * Round trip fully populated assembly templates. Some code borrowed from
    * {@link com.percussion.services.assembly.data.PSAssemblyTemplateTest},
    * specifically for populating templates and bindings.
    * 
    * @throws Exception
    */
   @Ignore("Failing on new CI server")
   @Test
   public void testSerialization() throws Exception
   {
       //TODO: Fix Me
//      PSUiAssemblyTemplate template = new PSUiAssemblyTemplate();
//      setupTemplateData(template);
//      setupBindingData(template);
//      
//      String ser = template.toXML();
//      
//      PSUiAssemblyTemplate restore = new PSUiAssemblyTemplate();
//      restore.fromXML(ser);
//      
//      assertEquals(template, restore);
   }
}
