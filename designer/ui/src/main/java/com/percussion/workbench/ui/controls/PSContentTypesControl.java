/******************************************************************************
 *
 * [ PSContentTypesControl.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.controls;

import com.percussion.E2Designer.PSDlgUtil;
import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.models.IPSContentTypeModel;
import com.percussion.client.objectstore.PSUiAssemblyTemplate;
import org.eclipse.swt.widgets.Composite;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Control to select content types from the list of available content types.
 *
 * @author Andriy Palamarchuk
 */
public class PSContentTypesControl extends PSFilteredSlushBucketControl
{
   public PSContentTypesControl(Composite parent, int style)
   {
      super(parent, style);
   }

   /**
    * Copies data from the UI to the template.
    */
   public void updateTemplate(PSUiAssemblyTemplate template)
   {
      template.setNewContentTypes(new HashSet<IPSReference>(getSelections()));
   }

   @Override
   protected List<IPSReference> createOriginallySelectedItems(
         @SuppressWarnings("unused") Object designObject)
   {
      return new ArrayList<IPSReference>();
   }

   @Override
   public void loadControlValues(Object designObject)
   {
      m_contentTypeModel = initializeContentTypeModel();
      super.loadControlValues(designObject);
   }

   private IPSContentTypeModel initializeContentTypeModel()
   {
      try
      {
         return (IPSContentTypeModel) PSCoreFactory.getInstance().getModel(
               PSObjectTypes.CONTENT_TYPE);
      }
      catch (PSModelException e)
      {
         PSDlgUtil.showError(e);
         return null;
      }
   }

   @Override
   protected List<IPSReference> createAvailableItems(
         @SuppressWarnings("unused") final Object designObject)
      throws PSModelException
   {
      return new ArrayList<IPSReference>(m_contentTypeModel
            .getUseableContentTypes(false));
   }

   @Override
   public String getSelectedLabelText()
   {
      return getMessage("PSContentTypesControl.label.associatedContentTypes");
   }

   @Override
   protected String getAvailableLabelText()
   {
      return getMessage("PSContentTypesControl.label.availableContentTypes");
   }

   /**
    * Model to provide list of available content types.
    */
   IPSContentTypeModel m_contentTypeModel;
}
