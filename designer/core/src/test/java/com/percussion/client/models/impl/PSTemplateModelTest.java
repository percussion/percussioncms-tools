/******************************************************************************
 *
 * [ PSTemplateModelTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.models.impl;

import com.percussion.client.IPSReference;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.PSObjectTypes.TemplateSubTypes;
import com.percussion.client.impl.PSReference;
import com.percussion.client.models.IPSContentTypeModel;
import com.percussion.client.objectstore.PSUiAssemblyTemplate;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import org.jmock.cglib.Mock;
import org.jmock.cglib.MockObjectTestCase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.EMPTY_MAP;

public class PSTemplateModelTest extends MockObjectTestCase
{
   public void testBasics()
   {
      new PSTemplateModel("name", "description", PSObjectTypes.TEMPLATE);
   }
   
   public void testGetContentTypes() throws PSModelException
   {
      final PSUiAssemblyTemplate template = new PSUiAssemblyTemplate();
      template.setName("template1");
      template.setGUID(new PSGuid(0, PSTypeEnum.TEMPLATE, 3));
      final IPSReference templateRef = new PSReference(template.getName(),
            null, null, getTemplateObjectType(),
            template.getGUID());

      final Mock mockContentTypeModel = new Mock(IPSContentTypeModel.class);
      final PSTemplateModel model =
            new PSTemplateModel("name", "description", PSObjectTypes.TEMPLATE)
      {
         @Override
         protected IPSContentTypeModel getContentTypeModel()
         {
            return (IPSContentTypeModel) mockContentTypeModel.proxy();
         }

         @Override
         @SuppressWarnings("unused")
         public Object load(IPSReference ref, boolean lock, boolean overrideLock)
         {
            assertSame(ref, templateRef);
            return template;
         }
      };

      // no associations
      {
         final boolean force = true;
         mockContentTypeModel.expects(once()).method("getTemplateAssociations")
               .with(NULL, eq(force), eq(false))
               .will(returnValue(EMPTY_MAP));
         assertTrue(model.getContentTypes(templateRef, force).isEmpty());
      }
      
      final IPSReference templateRef2 = new PSReference("template2", null,
            null, getTemplateObjectType(),
            new PSGuid(0, PSTypeEnum.TEMPLATE, 4));

      final PSReference contentTypeRef1 = new PSReference("Content Type1",
            null, null, getContentTypeObjectType(),
            new PSGuid(0, PSTypeEnum.NODEDEF, 1));
      contentTypeRef1.setPersisted();
      final IPSReference contentTypeRef2 = new PSReference("Content Type2",
            null, null, getContentTypeObjectType(), new PSGuid(0,
                  PSTypeEnum.NODEDEF, 2));
      final IPSReference contentTypeRef3 = new PSReference("Content Type3",
            null, null, getContentTypeObjectType(), new PSGuid(0,
                  PSTypeEnum.NODEDEF, 3));

      // associations exist
      {
         final Map<IPSReference, Collection<IPSReference>> associations =
               new HashMap<IPSReference, Collection<IPSReference>>();
         // contains template
         associations.put(contentTypeRef1, Collections.singleton(templateRef));
         // contains other template 
         associations.put(contentTypeRef2, Collections.singleton(templateRef2));
         // contains both - this and other template
         {
            final Collection<IPSReference> refs = new ArrayList<IPSReference>();
            refs.add(templateRef2);
            refs.add(templateRef);
            associations.put(contentTypeRef3, refs);
         }

         final boolean force = false;
         mockContentTypeModel.expects(once()).method("getTemplateAssociations")
               .with(NULL, eq(force), eq(false))
               .will(returnValue(associations));
         final Set<IPSReference> contentTypes =
               model.getContentTypes(templateRef, force);
         assertEquals(2, contentTypes.size());
         assertTrue(contentTypes.contains(contentTypeRef1));
         assertTrue(contentTypes.contains(contentTypeRef3));
      }
      
      // template has new content types specified
      {
         template.setNewContentTypes(
               Collections.singleton((IPSReference) contentTypeRef1));
         final Set<IPSReference> contentTypes =
               model.getContentTypes(templateRef, true);
         assertEquals(1, contentTypes.size());
         assertTrue(contentTypes.contains(contentTypeRef1));
         assertFalse(contentTypes.contains(contentTypeRef3));
      }
      
      // exception is thrown while loading template
      {
         final Exception exception = new Exception("error");
         try
         {
            new PSTemplateModel("name", "description", PSObjectTypes.TEMPLATE)
            {
               @Override
               protected IPSContentTypeModel getContentTypeModel()
               {
                  return (IPSContentTypeModel) mockContentTypeModel.proxy();
               }

               @Override
               @SuppressWarnings("unused")
               public Object load(IPSReference ref, boolean lock,
                     boolean overrideLock) throws Exception
               {
                  throw exception;
               }
            }.getContentTypes(templateRef, false);
         }
         catch (PSModelException e)
         {
            assertSame(e.getCause(), exception); 
         }
      }

      mockContentTypeModel.verify();
   }

   /**
    * Convenience methot to get content type object type.
    */
   private PSObjectType getContentTypeObjectType()
   {
      return new PSObjectType(PSObjectTypes.CONTENT_TYPE);
   }

   /**
    * Convenience methot to get template object type.
    */
   private PSObjectType getTemplateObjectType()
   {
      return new PSObjectType(PSObjectTypes.TEMPLATE, TemplateSubTypes.SHARED);
   }
}
