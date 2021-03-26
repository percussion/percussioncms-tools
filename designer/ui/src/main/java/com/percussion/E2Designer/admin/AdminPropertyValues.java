/*[ AdminPropertyValues.java ]*************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer.admin;

/**
 * This class represents the property values of an object of type 'role' or 
 * 'subject' that are loaded from PSX_ADMINLOOKUP table. 
 * This class represents single row data except 'type' and 'category' that is 
 * cataloged from PSX_ADMINLOOKUP table.
 */
public class AdminPropertyValues
{

   /**
    * Constructor for AdminPropertyValues. 
    * @param catalogUrl url property of this object, may be <code>null</code>
    * @param name name of the property can not be <code>null</code> or empty.
    * This can be either a name of attribute/property if the category is 
    * 'SYS_ATTRIBUTE', otherwise it will be value of the property.
    * @param limitToList flag that specifies Administrator whether to allow user 
    * to enter their own value. May be <code>null</code>, if it's null then the 
    * default value is true. If "Y" or "y" then the value of m_limitToList is 
    * <code>true</code>. If "N" or "n" then  the value is <code>false</code>. 
    */
   public AdminPropertyValues(String catalogUrl, 
      String name,String limitToList)
   {
      if(name == null)
         throw new IllegalArgumentException("name can not be null");
              
      m_catalogUrl = catalogUrl;
      if(limitToList != null && limitToList.equalsIgnoreCase("N"))
         m_limitToList = false;
      m_name = name;    
   }
   
   /**
    * Method to get the catalog Url information. 
    * @return catalogUrl, may be <code>null</code>.
    */
   public String getCatalogUrl()
   {
      return m_catalogUrl;
   }
   
   /**
    * Method to get the name or value of the property.
    * @return name of the propery, never <code>null</code>  
    */
   public String getPropertyName()
   {
      return m_name;
   }
   /**
    * Method to get the limit to list information.
    * @return limit to list information, can not be <code>null</code> or 
    * empty.
    */
   public boolean getLimitToList()
   {
      return m_limitToList;
   }
   
   /**
    * The variable that holds the catalog url information. 
    * This url tells the Rhytmyx server administrator where to get the values 
    * from. It can be <code>null</code> if designer does not specify the url.
    * It's always initialized by the contructor can not be modified after that.  
    */
   private String m_catalogUrl;
   
   /**
    * The varible to hold the limit to list information.
    * This variable that tells the Rhythmyx server administrator if the user can 
    * enter their own value or not. User will be able to enter their own value 
    * if the value is "N". User will not be able to enter their own value if the 
    * value is "Y". The default value is true. 
    */
   private boolean m_limitToList=true;

   /**
    * The name of the property, may not be <code>null<code> 
    * This varible initilize by the construtor. Can not be modified after 
    * object creation. 
    */   
   private String m_name; 
    
}
