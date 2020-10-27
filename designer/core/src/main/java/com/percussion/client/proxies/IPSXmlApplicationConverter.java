/******************************************************************************
 *
 * [ IPSXmlApplicationConverter.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.proxies;

import com.percussion.design.objectstore.PSApplication;
import com.percussion.error.PSException;

/**
 * Interface for callback function to control correct retrieved application data.
 * Result of conversion is returned from XML application proxy instead of actual
 * object.
 * 
 * @author Andriy Palamarchuk
 */
public interface IPSXmlApplicationConverter
{
   /**
    * Performs actual conversion.
    */
   public PSApplication convert(PSApplication application) throws PSException;
}
