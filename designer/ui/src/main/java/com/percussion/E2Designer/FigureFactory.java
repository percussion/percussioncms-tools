/*[ FigureFactory.java ]*******************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;

import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Defines an interface for a factory that creates UI objects. This class does
 * the work on a list supplied by the derived class. The vector containing the
 * list of figures should be filled in the constructor of the derived class.
 * The elements in this list must be of type FigureInfo.
 * <p>
 * Each figure in the list has a category and a type (or name). Objects in the
 * same category but different types may be able to connect to the same, single 
 * connector if the connector is set to accept category connections. The name
 * for any object should be unique (case insensitive) within the figure list.
 */
public abstract class FigureFactory implements Serializable
{

   /**
    * Returns a list of strings, one for each different category within the list.
    * A category is a group of figures that have similar (or identical) 
    * functionality, but appear within different contexts in the diagram. They
    * are usually, although not required to be interchangable on a connector
    * (the connector determines whether this is allowed).
    */
   public Enumeration getFigureCategories( )
   {
      Vector<String> CategoryList = new Vector<String>();

      // walk the figure list, and create a new vector containing the categories
      Enumeration e = m_FigureList.elements();
      while (e.hasMoreElements())
      {
         String strCategory = ((FigureInfo)e.nextElement()).getCategory();

         boolean bFound = false;
         Enumeration eCategory = CategoryList.elements();
         while (eCategory.hasMoreElements() && !bFound)
         {
            bFound = 0 == strCategory.compareTo((String) eCategory.nextElement());
         }
         if (!bFound)
            CategoryList.add(strCategory);
      }
      return(CategoryList.elements());
      
   }

   /**
    * Returns number of categories present in this factory.
    */
   public int getCategoryCount( )
   {
      return m_FigureList.size();
   }

   /**
    * Returns a list of all types that have the same category. If there is only
    * one type for a category, the type will have the same name as the category.
    * If there is more than 1 type, there will be 1 type with the same name
    * as the category, which will act as the default. If strCategory is null 
    * or empty, all types are returned.
    */
   public Enumeration getFigureTypes( String strCategory )
   {
      final boolean bAll = StringUtils.isBlank(strCategory);

      Vector<String> TypeList = new Vector<String>();

      Enumeration e = m_FigureList.elements();
      while (e.hasMoreElements())
      {
         FigureInfo fi = (FigureInfo)e.nextElement();
         if (fi.getCategory().equals(strCategory) || bAll)
            TypeList.add(fi.getName());
      }

      return TypeList.elements();
   }

   /**
    * Returns number of types in the specified category, or 0 if the category
    * is not present in this factory. If strCategory is null or empty, a count
    * of all types is returned.
    */
   public int getTypeCount( String strCategory )
   {
      int iCount = 0;
      final boolean bAll = StringUtils.isBlank(strCategory);

      Enumeration e = m_FigureList.elements();
      while (e.hasMoreElements())
      {
         if (((FigureInfo)e.nextElement()).getCategory().equals(strCategory)
            || bAll)
         {
            iCount++;   
         }
            
      }

      return iCount;
   }

   /**
    * Returns the FigureInfo object in the array that matches the supplied
    * type. If the type is not found, null is returned.
    */
   protected FigureInfo getElement(String strType)
   {
      FigureInfo fi = null;
      Enumeration e = m_FigureList.elements();
      boolean bFound = false;
      while (e.hasMoreElements() && !bFound)
      {
         fi = (FigureInfo)e.nextElement();
         if (fi.getName().equals(strType))
            bFound = true;
      }
      return(bFound ? fi : null);
   }

   /**
    * @returns the base name of the derived figure factory class
    */
   public String getName()
   {
      String strFullName = getClass().getName();
      return(strFullName.substring(strFullName.lastIndexOf('.')+1));
   } 

   /**
    * If strType is a valid type for this factory, creates an object of that
    * type and returns it.
    *
    * @param strType name of the type to create, case insensitive. Should be
    * one of the values returned by getFigureTypes.
    *
    * @param figureFrame the frame the figure is in.
    * <code>null</code> if it is not in a frame.
    *
    * @returns the newly created figure
    *
    * @throws IllegalArgumentException if strType is null or empty
    *
    * @throws UnsupportedOperationException if strType is not an available type
    *
    * @throws FigureCreationException if any errors occur while creating the figure.
    * The text of the exception is the detail message of the original exception.
    */
   public abstract UIConnectableFigure createFigure(String strType)
         throws FigureCreationException;

   protected Vector<FigureInfo> m_FigureList = new Vector<FigureInfo>();
}
