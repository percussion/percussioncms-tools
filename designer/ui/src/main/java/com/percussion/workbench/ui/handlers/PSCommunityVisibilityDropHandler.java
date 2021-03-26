/******************************************************************************
*
* [ PSCommunityVisibilityDropHandler.java ]
*
* COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
* All rights reserved. This material contains unpublished, copyrighted
* work including confidential and proprietary information of Percussion.
*
*****************************************************************************/
package com.percussion.workbench.ui.handlers;

import com.percussion.client.IPSReference;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSSecurityUtils;

import java.util.Collection;
import java.util.Properties;

/**
 * Drop handler to handle the dropping of objects into the various folders
 * in the community visibility view and making the appropriate modifications
 * to the acls.
 */
public class PSCommunityVisibilityDropHandler extends PSLinkNodeHandler
{

   /**
    * Ctor
    * @param props
    * @param iconPath
    * @param allowedTypes
    */
   public PSCommunityVisibilityDropHandler(Properties props, String iconPath,
      PSObjectType[] allowedTypes)
   {
      super(props, iconPath, allowedTypes);
   }

   /* 
    * @see com.percussion.workbench.ui.handlers.PSLinkNodeHandler#
    * doSaveAssociations(com.percussion.client.IPSReference, java.util.Collection)
    */
   @Override
   protected Collection<IPSReference> doSaveAssociations(IPSReference commRef,
      Collection<IPSReference> associatedRefs) throws Exception
   {
      return PSSecurityUtils.saveCommunityAssociations(commRef, associatedRefs);
   }

   /* 
    * @see com.percussion.workbench.ui.handlers.PSLinkNodeHandler#doDeleteAssociations(
    * com.percussion.client.IPSReference, java.util.Collection)
    */
   @Override
   protected Collection<IPSReference> doDeleteAssociations(IPSReference commRef,
      Collection<IPSReference> associatedRefs) throws Exception
   {
      return PSSecurityUtils.deleteCommunityAssociations(commRef, associatedRefs);
   }
}
