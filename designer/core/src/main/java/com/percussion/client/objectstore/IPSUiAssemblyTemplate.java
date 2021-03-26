/******************************************************************************
 *
 * [ IPSUiAssemblyTemplate.java ]
 * 
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.objectstore;

import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.utils.guid.IPSGuid;

import java.util.Set;

/**
 * This interface changes the way slots are obtained and set. It uses ids
 * instead of slot instances. The slot instance methods from the base interface
 * are 'overridden' to throw exceptions. In their place are equivalent methods
 * that use ids.
 * 
 * @author paulhoward
 */
public interface IPSUiAssemblyTemplate extends IPSAssemblyTemplate
{
   /**
    * Assign the slots that are contained/used by this template. If
    * <code>null</code> or empty, all associations are removed. The existing
    * set of slot ids is cleared before assigning the supplied ids.
    * 
    * @param slots The ids are copied from the supplied set. If
    * <code>null</code> or empty, all slot associations are cleared.
    */
   public void setSlotGuids(Set<IPSGuid> slots);
   
   /**
    * Returns identifiers for the slots that are contained by this template.
    * 
    * @return Never <code>null</code>, may be empty. The caller takes ownership
    * of the returned set. Changes to it do not affect this object.
    */
   public Set<IPSGuid> getSlotGuids();
   
   /**
    * Strictly for converter not to be used outside. 
    * @see #getSlotGuids()
    */
   public Set<IPSTemplateSlot> getSlots();
   
   /**
    * Strictly for converter not to be used outside. 
    * @see #setSlotGuids(Set)
    */
   public void setSlots(Set<IPSTemplateSlot> slots);
}
