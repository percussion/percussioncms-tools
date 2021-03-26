/******************************************************************************
 *
 * [ PSUiCommunity.java ]
 * 
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.client.objectstore;

import com.percussion.client.IPSReference;
import com.percussion.services.security.data.PSCommunity;

import java.util.HashSet;
import java.util.Set;

/**
 * Extends {@link PSCommunity} with the object visiblity for this community.
 * Currently used when new copies of existing commuities are created to hold 
 * all visible objects of the original until the copies are saved.
 */
public class PSUiCommunity extends PSCommunity
{
   /**
    * Get the set of references to objects that are visible by this community.
    * 
    * @return the list of visible object references by this community, never
    *    <code>null</code>, may be empty. Any change to the returned object
    *    will not affect this object.
    */
   public Set<IPSReference> getVisibleRefs()
   {
      return new HashSet<IPSReference>(m_visibleRefs);
   }
   
   /**
    * Set the references to all objects visible by this community.
    * 
    * @param refs the references to all visible objects by this community,
    *    may be <code>null</code> or empty, overrides the current visiblity
    *    references. Changes to the supplied set will not affect this object.
    */
   public void setVisibleRefs(Set<IPSReference> refs)
   {
      if (refs == null)
         m_visibleRefs = new HashSet<IPSReference>();
      else
         m_visibleRefs = new HashSet<IPSReference>(refs);
   }
   
   /**
    * A set of references to objects that are visible by this community,
    * never <code>null</code>, may be empty.
    */
   private Set<IPSReference> m_visibleRefs = new HashSet<IPSReference>();
}

