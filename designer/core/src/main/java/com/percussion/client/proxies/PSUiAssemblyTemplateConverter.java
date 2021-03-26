/******************************************************************************
 *
 * [ PSUiAssemblyTemplateConverter.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.client.proxies;

import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreUtils;
import com.percussion.client.PSObjectTypeFactory;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.objectstore.PSUiAssemblyTemplate;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.assembly.data.PSTemplateSlot;
import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.assembly.data.PSAssemblyTemplate;
import com.percussion.webservices.common.Reference;
import com.percussion.webservices.transformation.converter.PSAssemblyTemplateConverter;
import org.apache.commons.beanutils.BeanUtilsBean;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Just overrides the base class version not load the slots from the server
 * instead, create dummy ones with valid slotids but fake data for the rest of
 * the objects.
 */
public class PSUiAssemblyTemplateConverter extends PSAssemblyTemplateConverter
{
   /**
    * @param beanUtils
    */
   public PSUiAssemblyTemplateConverter(BeanUtilsBean beanUtils)
   {
      super(beanUtils);
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.webservices.transformation.converter.PSAssemblyTemplateConverter#convert(java.lang.Class,
    * java.lang.Object)
    */
   @Override
   public Object convert(Class type, Object value)
   {
      Object result = super.convert(type,value);
      if (isClientToServer(value))
      {
         PSAssemblyTemplate orig = (PSAssemblyTemplate) value;
         PSUiAssemblyTemplate dest = (PSUiAssemblyTemplate) result;

         // convert slots
         Reference[] origSlots = orig.getSlots();
         Set<IPSGuid> slotIds = new HashSet<IPSGuid>();
         for (Reference origSlot : origSlots)
            slotIds.add(new PSDesignGuid(origSlot.getId()));
         if (!slotIds.isEmpty())
            dest.setSlotGuids(slotIds);

         // transformsiteids
         Reference[] origSites = orig.getSites();
         if (origSites.length > 0)
         {
            Set<IPSReference> siteIds = new HashSet<IPSReference>(
               origSites.length);
            for (Reference origSite : origSites)
            {
               siteIds.add(PSCoreUtils.createReference(origSite.getName(),
                  origSite.getName(), origSite.getName(), PSObjectTypeFactory
                     .getType(PSObjectTypes.SITE), new PSDesignGuid(origSite
                     .getId())));
            }
            dest.setSites(siteIds);
         }
      }
      else
      {
         PSUiAssemblyTemplate orig = (PSUiAssemblyTemplate) value;
         PSAssemblyTemplate dest = (PSAssemblyTemplate) result;

         // convert slots
         Set<IPSGuid> origSlots = orig.getSlotGuids();
         Reference[] destSlots = new Reference[origSlots.size()];
         dest.setSlots(destSlots);
         int index = 0;
         for (IPSGuid origSlot : origSlots)
         {
            long lId = new PSDesignGuid(origSlot).getValue();
            Reference destSlot = new Reference(lId, "dummy" + lId);

            destSlots[index++] = destSlot;
         }

         // transformsiteids
         Set<IPSReference> origSites = orig.getSites();
         Reference[] refs = new Reference[origSites.size()];
         Iterator<IPSReference> siter = origSites.iterator();
         for (int i = 0; i < refs.length; i++)
         {
            IPSReference sref = siter.next();
            refs[i] = new Reference(sref.getId().longValue(),
                  sref.getName());
         }
         dest.setSites(refs);
      }
      return result;
   }

   /**
    * Creates a new slot, assigning it the supplied id. This slot is for use as
    * a placeholder in the base class. Only the name and guid are assigned.
    * 
    * @param slotIds Assumed not <code>null</code>.
    * 
    * @return Never <code>null</code>.
    */
   @Override
   protected List<IPSTemplateSlot> loadSlots(List<IPSGuid> slotIds)
   {
      List<IPSTemplateSlot> result = new ArrayList<IPSTemplateSlot>();
      for (IPSGuid id : slotIds)
      {
         IPSTemplateSlot slot = new PSTemplateSlot();
         slot.setName(id.toString());
         slot.setGUID(id);
         result.add(slot);
      }
      return result;
   }
}
