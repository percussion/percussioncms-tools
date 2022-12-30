/*******************************************************************************
 *
 * [ PSCommunityConverter.java ]
 * 
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.client.proxies;

import com.percussion.error.PSMissingBeanConfigurationException;
import com.percussion.services.security.data.PSBackEndRole;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.transformation.converter.PSCommunityConverter;
import org.apache.commons.beanutils.BeanUtilsBean;

/**
 * Override the {@link PSCommunityConverter} to provide a dummy back end role 
 * without requiring the server.
 */
public class PSUiCommunityConverter extends PSCommunityConverter
{
   /**
    * @param beanUtils
    */
   public PSUiCommunityConverter(BeanUtilsBean beanUtils)
   {
      super(beanUtils);
   }

   /*(non-Javadoc)
    * @see PSCommunityConverter#loadRole(IPSGuid)
    */
   @Override
   protected PSBackEndRole loadRole(IPSGuid roleId)
      throws PSMissingBeanConfigurationException
   {
      PSBackEndRole role = new PSBackEndRole();
      role.setId(roleId.longValue());
      role.setName("dummy" + roleId.longValue());
      
      return role;
   }
}
