/******************************************************************************
 *
 * [ PSContentTypeHelper.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.wizards;

import com.percussion.E2Designer.PSDlgUtil;
import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.models.IPSContentTypeModel;
import com.percussion.client.objectstore.PSUiAssemblyTemplate;
import org.eclipse.swt.widgets.Combo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages content type UI. Assign combo and initialize it with
 * {@link #getContentTypeNames()} before using it. 
 *
 * @author Andriy Palamarchuk
 */
public class PSContentTypeHelper
{
   // for unit tests see PSTemplateTypePageTest

   /**
    * Initializes data from model if data are not loaded yet.
    */
   private void initData()
   {
      if (m_contentTypes != null)
      {
         return;
      }
      try
      {
         m_contentTypes = new ArrayList<IPSReference>(
            m_contentTypeModel.getUseableContentTypes(false));
      }
      catch (PSModelException e)
      {
         throw new RuntimeException(e);
      }

      Collections.sort(m_contentTypes, new IPSReference.NameKeyComparator());
   }

   /**
    * Creates default value for the content type model used on this page.
    */
   private IPSContentTypeModel initializeContentTypeModel()
   {
      try
      {
         return (IPSContentTypeModel)getCoreFactory().getModel(
            PSObjectTypes.CONTENT_TYPE);
      }
      catch (PSModelException e)
      {
         PSDlgUtil.showError(e);
         return null;
      }
   }

   /**
    * Assigns the content type user selected to the template. 
    */
   public void updateTemplate(PSUiAssemblyTemplate template)
   {
      assert m_combo != null;
      final IPSReference contentTypeRef = m_contentTypes.get(m_combo.getSelectionIndex());
      template.setNewContentTypes(
            Collections.<IPSReference>singleton(contentTypeRef));
   }

   /**
    * The singleton core factory instance.
    */
   private PSCoreFactory getCoreFactory()
   {
      return PSCoreFactory.getInstance();
   }

   /**
    * List of content type names. 
    */
   public List<String> getContentTypeNames()
   {
      initData();
      final List<String> names = new ArrayList<String>();
      for (final IPSReference ref : m_contentTypes)
      {
         names.add(ref.getName());
      }
      return names;
   }
   
   /**
    * Provides helper with combo to control.
    * The combo should already be initialized with {@link #getContentTypeNames()}.
    */
   public void setCombo(Combo combo)
   {
      m_combo = combo;
   }

   /**
    * Model used to catalog content types. 
    */
   IPSContentTypeModel m_contentTypeModel = initializeContentTypeModel();
   
   /**
    * List of content type references {@link #m_combo} was initialized
    * with.
    */
   private List<IPSReference> m_contentTypes;
   
   /**
    * Combo to select content types.
    */
   private Combo m_combo;
}
