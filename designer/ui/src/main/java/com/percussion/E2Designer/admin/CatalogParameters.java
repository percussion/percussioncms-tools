/*[ CatalogParameters.java ]***************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer.admin;  

import com.percussion.E2Designer.CatalogerMetaData;

/**
 * Helper class to hold cataloging parameters for member cataloging. This class
 * encapsulates security provider, security provider instance, type of cataloged 
 * members(user, group, both), lookup filter lookupText and  display filter   
 * displayText. These are parameters that were used to perform member cataloging
 * in Modify member list dialog.
 */
public class CatalogParameters
{
   /**
    * Initializes all variables.
    * @param catalogerMeta The cataloger that cataloging was done for
    * can not be, <code>null</code>.
    * @param type a type of the cataloged members (user, group, both)
    * can not be <code>null</code>.
    * @param lookupText a lookup filter used for cataloging
    * can not <code>null</code>.
    * @param displayText a display filter used for displaying cataloged
    * members can not <code>null</code>.
    */
   public CatalogParameters(CatalogerMetaData catalogerMeta,
      String type, String lookupText, String displayText)
   {
      if(catalogerMeta == null )
         throw new IllegalArgumentException("Security Provider can not be null");

      if(type == null)
         throw new IllegalArgumentException ("Member type can not be null");
      if(lookupText == null)
         lookupText = "%";
      if(displayText == null)
         displayText = "%";

      m_catalogerMeta = catalogerMeta;
      m_objectType = type;
      m_lookupText  = lookupText;
      m_displayText = displayText;
   }

   /**
    * Gets the cataloger meta data cataloging parameter.
    * @return the meta data for which the cataloging is done.
    * never <code>null</code>, can not be empty.
    */
   public CatalogerMetaData getCatalogProvider()
   {
      return m_catalogerMeta;
   }


   /**
    * Gets the member type.
    * @return cataloged members type for which cataloging is done,
    * never <code>null</code>.
    */
   public String getCatalogObjectType()
   {
      return m_objectType;
   }

   /**
    * Gets the lookup filter text.
    * @return a lookup filter text for which the cataloging is done.
    * never <code>null</code>.
    */
   public String getLookupText()
   {
      return m_lookupText;
   }

   /**
    * Gets the display filter text.
    * @return a display filter for which cataloging is done,
    * never <code>null</code>.
    */
   public String getDisplayText()
   {
      return m_displayText;
   }

   /**
    * Current cataloger used for cataloging members,
    * gets initialized in constructor.
    */
   private CatalogerMetaData m_catalogerMeta = null;

   /**
    * A string that represents member type used for cataloging.
    * Currently we support three types: 'Users', 'Groups' and 'Both', gets 
    * initialized in constructor
    */
   private String m_objectType = null;

   /**
    * String varible to hold current filter used for cataloging, is set in
    * constructor
    */
   private String m_lookupText = null;

   /**
    * Varible to hold current filter used for displaying cataloged
    * members, is set in constructor.
    */
   private String m_displayText = null;

}
