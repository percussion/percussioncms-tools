/******************************************************************************
 *
 * [ IPSWorkflowModel.java ]
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
public interface IPSWorkflowModel extends IPSCmsModel
{
   /**
    * Get the roles that are members fo the supplied workflow reference.
    * 
    * @param wfRef workflow reference whose member roles are sought, may be
    * <code>null</code> in which case roles from all workflows are returned.
    * @return set of roles from the supplied or all workflow(s), never
    * <code>null</code> mat be empty.
    * @throws PSModelException
    */
   Collection<IPSReference> getWorkflowRoles(IPSReference wfRef)
      throws PSModelException;
}
