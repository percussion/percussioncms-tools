/******************************************************************************
 *
 * [ PSTemplateTypePageTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.wizards;

import com.percussion.client.IPSReference;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectTypeFactory;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.impl.PSReference;
import com.percussion.client.models.IPSCmsModel;
import com.percussion.client.models.IPSContentTypeModel;
import com.percussion.client.objectstore.PSUiAssemblyTemplate;
import com.percussion.services.assembly.IPSAssemblyTemplate.TemplateType;
import com.percussion.services.assembly.data.PSAssemblyTemplate;
import com.percussion.workbench.ui.PSUiTestBase;
import com.percussion.workbench.ui.editors.wizards.PSTemplateTypePage.TypeButton;
import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.jmock.cglib.Mock;

import java.util.ArrayList;
import java.util.List;

import static com.percussion.extension.IPSExtension.DATABASE_ASSEMBLER;
import static com.percussion.extension.IPSExtension.VELOCITY_ASSEMBLER;

public class PSTemplateTypePageTest extends PSUiTestBase
{
   public void testBasics()
   {
      assertTrue(StringUtils.isNotBlank(createPage().getName()));
      assertTrue(StringUtils.isNotBlank(createPage().getTitle()));
   }
   
   public void testContentTypeCombo_Initialization() throws PSModelException
   {
      final PSTemplateTypePage page = createInitializedPage();
      
      page.createControl(m_shell);
      final Combo contentTypeCombo = page.getContentTypeCombo();
      assertEquals(createContentTypeRefs().size(), contentTypeCombo.getItemCount());

      // sorted
      assertEquals(NAME0, contentTypeCombo.getItem(0));
      assertEquals(NAME1, contentTypeCombo.getItem(1));
      assertEquals(NAME2, contentTypeCombo.getItem(2));

      assertEquals(0, contentTypeCombo.getSelectionIndex());
      assertEquals(0, page.m_typeRadio.getEnabledButtons()[0]);
   }

   public void testContentTypeCombo_InitializationNoContentTypes() throws PSModelException
   {
      final PSTemplateTypePage page = createPage();
      m_mockContentTypeModel = new Mock(IPSCmsModel.class);
      page.m_contentTypeHelper.m_contentTypeModel = (IPSContentTypeModel) m_mockContentTypeModel.proxy();
      m_mockContentTypeModel.expects(once()).method("catalog")
            .will(returnValue(new ArrayList<IPSReference>()));

      page.createControl(m_shell);
      final Combo contentTypeCombo = page.getContentTypeCombo();
      assertEquals(0, contentTypeCombo.getItemCount());
      assertFalse(contentTypeCombo.isEnabled());

      assertEquals(-1, contentTypeCombo.getSelectionIndex());
      assertEquals(TypeButton.SHARED.ordinal(), page.m_typeRadio.getEnabledButtons()[0]);
   }

   private List<IPSReference> createContentTypeRefs() throws PSModelException
   {
      final List<IPSReference> contentTypes = new ArrayList<IPSReference>();
      contentTypes.add(createContentTypeRef(NAME2));
      contentTypes.add(createContentTypeRef(NAME0));
      contentTypes.add(createContentTypeRef(NAME1));
      return contentTypes;
   }

   public void testUpdateDesignerObject() throws PSModelException
   {
      final PSTemplateTypePage page = createInitializedPage();

      page.createControl(m_shell);
      assertEquals(TypeButton.SHARED.ordinal(), page.m_typeRadio.getSelectedIndex());

      final PSUiAssemblyTemplate template = new PSUiAssemblyTemplate();
      assertEquals(TemplateType.Shared, template.getTemplateType());
      assertTrue(StringUtils.isBlank(template.getAssemblyUrl()));

      // unknown control
      try
      {
         page.updateDesignerObject(template, new Button(m_shell, SWT.PUSH));
         fail();
      }
      catch (IllegalArgumentException success) {};

      // unknown index
      page.m_typeRadio.addEntry("dummy");
      page.m_typeRadio.setSelection(page.m_typeRadio.getButtonCount() - 1);
      try
      {
         page.updateDesignerObject(template, page.m_typeRadio);
         fail();
      }
      catch (IndexOutOfBoundsException success) {};

      checkRadioButtonForUpdateDesignerObject(page, template,
            TypeButton.SHARED, TemplateType.Shared);
      assertFalse(template.areNewContentTypesSpecified());
      
      checkRadioButtonForUpdateDesignerObject(page, template,
            TypeButton.LOCAL, TemplateType.Local);
      page.getContentTypeCombo().select(1);
      template.setAssemblyUrl("");
      page.updateDesignerObject(template, page.m_typeRadio);
      assertTrue(template.areNewContentTypesSpecified());
      assertEquals(1, template.getNewContentTypes().size());
      assertEquals(NAME1, template.getNewContentTypes().iterator().next().getName());
      assertTrue(StringUtils.isNotBlank(template.getAssemblyUrl()));

      
      checkRadioButtonForUpdateDesignerObject(page, template,
            TypeButton.GLOBAL_FORMAT, TemplateType.Shared);
      assertEquals(VELOCITY_ASSEMBLER, template.getAssembler());

      checkRadioButtonForUpdateDesignerObject(page, template,
            TypeButton.DATABASE_FORMAT, TemplateType.Shared);
      assertEquals(DATABASE_ASSEMBLER, template.getAssembler());
   }
   
   private PSTemplateTypePage createInitializedPage() throws PSModelException
   {
      final PSTemplateTypePage page = createPage();
      m_mockContentTypeModel = new Mock(IPSCmsModel.class);
      page.m_contentTypeHelper.m_contentTypeModel = (IPSContentTypeModel) m_mockContentTypeModel.proxy();
      m_mockContentTypeModel.expects(once()).method("catalog")
            .will(returnValue(createContentTypeRefs()));
      return page;
   }

   /**
    * Checks how radio button state affects the design object. 
    * @param selection button index to check.
    * @param templateType expected template type for the provided template.
    */
   private void checkRadioButtonForUpdateDesignerObject(final PSTemplateTypePage page,
         final PSAssemblyTemplate template, final TypeButton selection,
         final TemplateType templateType)
   {
      page.m_typeRadio.setSelection(selection.ordinal());
      page.updateDesignerObject(template, page.m_typeRadio);
      assertEquals(templateType, template.getTemplateType());
   }

   private PSTemplateTypePage createPage()
   {
      return new PSTemplateTypePage();
   }
   
   protected IPSReference createContentTypeRef(String name) throws PSModelException
   {
      final PSReference ref = new PSReference();
      ref.setName(name);
      ref.setObjectType(PSObjectTypeFactory.getType(PSObjectTypes.CONTENT_TYPE));
      return ref;
   }
   /**
    * Sample names.
    */
   private final String NAME0 = "Name0";
   private final String NAME1 = "Name1";
   private final String NAME2 = "Name2";
   
   /**
    * Content type model providing content type data. 
    */
   Mock m_mockContentTypeModel;
}
