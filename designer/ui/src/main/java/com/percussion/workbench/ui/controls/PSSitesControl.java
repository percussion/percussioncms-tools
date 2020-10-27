/******************************************************************************
 *
 * [ PSSitesControl.java ]
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
import com.percussion.client.models.IPSCmsModel;
import com.percussion.client.objectstore.PSUiAssemblyTemplate;
import org.eclipse.swt.widgets.Composite;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Control allowing to select sites from list of existing. Requres a template
 * reference, not the actual template to be passed around as design object.
 * 
 * @author Andriy Palamarchuk
 */
public class PSSitesControl extends PSFilteredSlushBucketControl
{
   public PSSitesControl(Composite parent, int style)
   {
      super(parent, style);
   }

   /**
    * Initial {@link #m_siteModel} value.
    */
   private IPSCmsModel initializeSiteModel()
   {
      try
      {
         return PSCoreFactory.getInstance().getModel(PSObjectTypes.SITE);
      }
      catch (PSModelException e)
      {
         PSDlgUtil.showError(e);
         return null;
      }
   }

   /**
    * Updates template with the data specified by user.
    * 
    * @param template the template object.
    */
   public void updateTemplate(final PSUiAssemblyTemplate template)
   {
      template.setSites(getSelections());
   }

   @Override
   public void loadControlValues(Object designObject)
   {
      assert designObject instanceof PSUiAssemblyTemplate;
      m_siteModel = initializeSiteModel();
      super.loadControlValues(designObject);
   }

   @Override
   protected List<IPSReference> createOriginallySelectedItems(
      final Object designObject)
   {
      List<IPSReference> rval = new ArrayList<IPSReference>();
      rval.addAll(((PSUiAssemblyTemplate) designObject).getSites());
      return rval;
   }

   @SuppressWarnings("unchecked")
   @Override
   protected List<IPSReference> createAvailableItems(final Object designObject)
      throws PSModelException
   {
      assert designObject instanceof PSUiAssemblyTemplate;

      Collection<IPSReference> coll = m_siteModel.catalog();
      Set<IPSReference> existing = ((PSUiAssemblyTemplate) designObject)
         .getSites();
      List<IPSReference> result = new ArrayList<IPSReference>();
      for (IPSReference ref1 : coll)
      {
         boolean exists = false;
         for (IPSReference ref2 : existing)
         {
            if (ref2.getId().longValue() == ref1.getId().longValue())
            {
               exists = true;
               break;
            }
         }
         if (!exists)
            result.add(ref1);
      }
      return result;
   }

   @Override
   public String getSelectedLabelText()
   {
      return getMessage("PSSitesControl.label.selectedSites");
   }

   @Override
   protected String getAvailableLabelText()
   {
      return getMessage("PSSitesControl.label.availableSites");
   }

   /**
    * Model providing available sites.
    */
   IPSCmsModel m_siteModel;
}
