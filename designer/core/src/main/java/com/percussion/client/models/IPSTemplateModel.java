/******************************************************************************
 *
 * [ IPSTemplateModel.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.models;

import com.percussion.client.IPSReference;
import com.percussion.client.PSModelException;

import java.util.Set;

/**
 * Assembly template model.
 *
 * @author Andriy Palamarchuk
 */
public interface IPSTemplateModel extends IPSCmsModel
{
   /**
    * Returns the content types associated with the template.
    * @param templateRef the template to return content types for.
    * Never <code>null</code>. 
    * @param force whether to force data loading.
    * @return collection of the content types. Is empty if no associated content
    * types are found. 
    */
   public Set<IPSReference> getContentTypes(
         final IPSReference templateRef, final boolean force)
         throws PSModelException;
}
