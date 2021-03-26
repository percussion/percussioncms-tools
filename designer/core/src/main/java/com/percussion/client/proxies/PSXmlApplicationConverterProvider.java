/******************************************************************************
 *
 * [ PSXmlApplicationConverterProvider.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.proxies;

import com.percussion.design.objectstore.PSApplication;

/**
 * Global storage for the {@link IPSXmlApplicationConverter}.
 * Singleton.
 *
 * @author Andriy Palamarchuk
 */
public class PSXmlApplicationConverterProvider
{
   /**
    * The singleton instance.
    */
   public static PSXmlApplicationConverterProvider getInstance()
   {
      return ms_instance;
   }

   /**
    * The stored converter. Returns value specified by
    * {@link #setConverter(IPSXmlApplicationConverter)}. 
    * If not initialized returns converter which just passes application
    * object through without any changes.
    */
   public IPSXmlApplicationConverter getConverter()
   {
      return m_converter;
   }

   /**
    * Specifies the converter. 
    */
   public void setConverter(IPSXmlApplicationConverter converter)
   {
      assert !(converter instanceof NullConverter);
      assert m_converter instanceof NullConverter
            : "The expectation is that this method will be called only once. " +
            "If not, remove this assertion.";
      m_converter = converter;
   } 

   /**
    * "Null" converter which does nothing. 
    */
   private static class NullConverter implements IPSXmlApplicationConverter
   {
      public PSApplication convert(PSApplication application)
      {
         return application;
      }
   }
   
   /**
    * The singleton instance.
    */
   private static PSXmlApplicationConverterProvider ms_instance =
      new PSXmlApplicationConverterProvider();
   
   private IPSXmlApplicationConverter m_converter = new NullConverter();
}
