/*[ AdminNameValue.java ]******************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer.admin;

import java.util.List;


/**
 * This class represents a property and its values looked up for an object of  
 * type 'role' or 'subject'. 
 * This class is constructed after we check if there is a valid URL for 
 * the property. 
 *  
 */
public class AdminNameValue
{
   /**
    * Constructor of AdminNameValue. 
    * @param name name of the property, can not be <code>null</code> nor 
    * empty. 
    * @param limitToList limit to list information, can not be 
    * <code>null</code>
    * @param values List of the values for this property, may be 
    * <code>null</code>, may be empty.
    */
   public AdminNameValue(String name,  boolean limitToList, List values)
   {
      if(name == null || name.length() < 1)
         throw new IllegalArgumentException("name can not be null or empty");
      m_name = name;
      m_limitToList = limitToList;
      m_values = values;
   }
   
   /**
    * Method to get the name of the property 
    * @return name of the property.
    */
   public String getName()
   {
      return m_name;
   }
   
   /**
    * Method to get the limit to list information. 
    * @return limit to list information, can not be <code>null</code>.
    *  <code>true</code> if user can not enter additional value. 
    *  <code>false</code> if user can enter additonal value. 
    * 
    */
   public boolean getLimitToList()
   {
      return m_limitToList;
   }
   
   /**
    * Method to get List of the values of current property. 
    * @return list or <code>List</code> of values that belongs to the property, 
    * can not be <code>null</code>, can be empty. 
    */
   public List getValues()
   {
      return m_values;  
   }
   
   /**
    * The variable that hold the name of the property, can not be 
    * <code>null</code>. It's initialized by the constructor, never modify after
    * initiliazation. 
    */
   private String m_name;
   
   /**
    * Boolean variables that tells Rhythmyx server administrator if
    * users can enter aditional values, can not be <code>null</code>
    * initialized by the constructor, never modify after initiliazation. 
    */
   private boolean m_limitToList;
   
   /**
    * List of values that belongs to the property, can not be 
    * <code>null</code> but can be empty. initialized by the constructor, 
    * never modify after initiliazation.  
    * 
    */
   private List m_values;
    
}
