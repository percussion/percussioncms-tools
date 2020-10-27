/******************************************************************************
 *
 * [ IPSCommunityModel.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.client.models;

import com.percussion.client.IPSReference;
import com.percussion.client.PSModelException;

import java.util.Collection;

/**
 * Adds some special cataloging functionality.
 * 
 * @author ram
 */
public interface IPSCommunityModel extends IPSCmsModel
{
   /**
    * Get the roles that are members fo the supplied community reference.
    * 
    * @param commRef community reference whose member roles are sought, may be
    * <code>null</code> in which case roles from all communities are returned.
    * @return set of roles from the supplied or all communities, never
    * <code>null</code> mat be empty.
    * @throws PSModelException
    */
   Collection<IPSReference> getCommunityRoles(IPSReference commRef)
      throws PSModelException;
}
