/*[ FigureFactoryManager.java ]************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;

import java.text.MessageFormat;
import java.util.Hashtable;

/**
 * A very simple class for creating different figure factories. The name of a 
 * figure factory class is passed in. That class is then loaded and a new instance
 * is returned. The new object is cached for later requests.
 */
public class FigureFactoryManager
{
   // constructors
   /**
    * Returns the single instance of the manager, creating it if necessary.
    * The factory manager can be used to obtain class factories of any type.
    */
   public static FigureFactoryManager getFactoryManager( )
   {
      if (null == ms_theManager)
         ms_theManager = new FigureFactoryManager();
      return ms_theManager;
   }
   // only a single instance can exist, obtained thru getFactoryManager
   private FigureFactoryManager( )
   {
   }

   // properties

   // operations
   /**
    * If strFactoryName is the name of a class derived from FigureFactory,
    * returns the singleton instance of the requested class, creating it
    * if necessary. The exceptions from Class.newInstance are not handled 
    * because the possibility of them occurring is extremely remote. If the
    * ClassNotFoundException is thrown, a debug message is also displayed. This
    * should only occur during development.
    * 
    * @returns a valid FigureFactory
    *
    * @throws IllegalArgumentException if the factory can't be loaded for any
    * reason. The detail message indicates the type of error. 
    */
   public FigureFactory getFactory( String strFactoryName )
   {
      try
      {
         FigureFactory factory = null;
         // see if we have already created one
         if (m_FactoryList.containsKey(strFactoryName))
            factory = (FigureFactory)m_FactoryList.get(strFactoryName);
         else
         {
            factory = (FigureFactory)Class.forName(strFactoryName).newInstance();
            if (!(factory instanceof FigureFactory))
            {
               // design time error
               final String [] astrParams = 
               {
                  strFactoryName
               };
               throw new IllegalArgumentException( MessageFormat.format( 
                     E2Designer.getResources().getString( "FigFactoryDerivedErr" ), 
                     astrParams ));
            }
            m_FactoryList.put(strFactoryName, factory);
         }
         return(factory);
      } 
      catch (ClassNotFoundException e)
      {
         // design time error
         final String [] astrParams = 
         {
            strFactoryName
         };
         throw new IllegalArgumentException( MessageFormat.format( 
               E2Designer.getResources().getString( "FigFactoryDerivedErr" ), 
               astrParams ));
      }
      catch (InstantiationException e)
      {
         // design time error
         final String [] astrParams = 
         {
            strFactoryName
         };
         throw new IllegalArgumentException( MessageFormat.format( 
               E2Designer.getResources().getString( "FigFactoryDerivedErr" ), 
               astrParams ));
      }
      catch (IllegalAccessException e)
      {
         // design time error
         final String [] astrParams = 
         {
            strFactoryName
         };
         throw new IllegalArgumentException( MessageFormat.format( 
               E2Designer.getResources().getString( "FigFactoryDerivedErr" ), 
               astrParams ));
      }
   }
   

   // private storage
   private static FigureFactoryManager ms_theManager = null;
   private Hashtable m_FactoryList = new Hashtable();
}

