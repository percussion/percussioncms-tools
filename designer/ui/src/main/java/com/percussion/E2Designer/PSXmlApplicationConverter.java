/******************************************************************************
 *
 * [ PSXmlApplicationConverter.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer;

import com.percussion.client.proxies.IPSXmlApplicationConverter;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.error.PSException;

/**
 * Converts {@link PSApplication} to {@link OSApplication}.
 *
 * @author Andriy Palamarchuk
 */
public class PSXmlApplicationConverter implements IPSXmlApplicationConverter
{

   // see base
   public PSApplication convert(final PSApplication application)
         throws PSException
   {
      return application == null || application instanceof OSApplication
            ? application
            : new OSApplication(application.toXml()); 
   }

}
