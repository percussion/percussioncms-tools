/******************************************************************************
 *
 * [ PSSlotsControl.java ]
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
import com.percussion.utils.guid.IPSGuid;
import org.eclipse.swt.widgets.Composite;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Control allowing to select slots from list of existing.
 *
 * @author Andriy Palamarchuk
 */
public class PSSlotsControl extends PSFilteredSlushBucketControl
{
   public PSSlotsControl(Composite parent, int style)
   {
      super(parent, style);
   }

   /**
    * Initial {@link #m_slotModel} value.
    */
   private IPSCmsModel initializeSlotModel()
   {
      try
      {
         return PSCoreFactory.getInstance().getModel(PSObjectTypes.SLOT);
      }
      catch (PSModelException e)
      {
         PSDlgUtil.showError(e);
         return null;
      }
   }
   
   /**
    * Updates template with the data specified by user.
    */
   public void updateTemplate(final PSUiAssemblyTemplate template)
   {
      template.getSlots().clear();
      final IPSReference[] refs = getSelections().toArray(new IPSReference[0]);

      Set<IPSGuid> slotGuids = new HashSet<IPSGuid>(refs.length);
      for (final IPSReference ref : refs)
         slotGuids.add(ref.getId());

      template.setSlotGuids(slotGuids);
   }

   @Override
   public void loadControlValues(Object designObject)
   {
      m_slotModel = initializeSlotModel();
      super.loadControlValues(designObject);
   }

   @Override
   protected List<IPSReference> createOriginallySelectedItems(final Object designObject)
         throws PSModelException
   {
      final PSUiAssemblyTemplate template = (PSUiAssemblyTemplate) designObject;
      final List<IPSReference> selected = new ArrayList<IPSReference>();
      for (final IPSGuid guid : template.getSlotGuids())
      {
         selected.add(m_slotModel.getReference(guid));
      }
      return selected;
   }

   @Override
   public List<IPSReference> createAvailableItems(final Object designObject)
         throws PSModelException
   {
      return new ArrayList<IPSReference>(m_slotModel.catalog());
   }

   @Override
   public String getSelectedLabelText()
   {
      return getMessage("PSSlotsControl.label.containedSlots");
   }

   @Override
   protected String getAvailableLabelText()
   {
      return getMessage("PSSlotsControl.label.availableSlots");
   }

   /**
    * Model providing available slots.
    */
   IPSCmsModel m_slotModel;
}
