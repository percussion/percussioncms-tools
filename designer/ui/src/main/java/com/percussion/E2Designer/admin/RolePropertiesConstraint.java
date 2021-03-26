/*[ RolePropertiesConstraint.java ]********************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer.admin;

import com.percussion.E2Designer.ValidationConstraint;
import com.percussion.E2Designer.ValidationException;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Validates Role properties such as sys_defaultHomepageURL.
 * Validation for other attributes such as sys_defaultcommunityid,
 * sys_defaultcommunityname etc can be added.
 */
public class RolePropertiesConstraint implements ValidationConstraint
{
   /**
    * implementing definition from interface ValidationConstraint SK: the description should be sentence format. Start with a uppercase letter and end with '.'
    * @return String - error string and an empty string if
    * no error is found.
    */
   public String getErrorText()
   {
      if (m_illegalUrl)
      {
         return sm_res.getString("illegalRelativeUrl");
      }
      if (m_absUrl)
      {
         return sm_res.getString("absUrl");
      }
      if (m_malformedUrl)
      {
         return sm_res.getString("malformedUrl");
      }
      if (m_nullUrl)
      {
         return "URL string cannot be empty.";
      }
      return "";
   }

   /**
    * implementing definition from interface ValidationConstraint.
    * @param jTable - runtime type of this object should be <code>JTable</code>, column count hasa to be 2.
    * if not the method silently exits without performing any validation.
    * @throws ValidationException - if URL is not relative or contains two
    * .. in the relative path.
    */
   public void checkComponent(Object jTable) throws ValidationException
   {
      if (jTable instanceof JTable)
      {
         TableModel tbm = ((JTable)jTable).getModel();
         int rowCount = tbm.getRowCount();
         for (int k = 0; k < rowCount; k++)
         {
            String strName = (String)tbm.getValueAt(k,0);
            if (strName != null)
            {
                if (strName.equalsIgnoreCase(SYS_URL))
                {
                   if (tbm.getColumnCount() != 2)
                   {
                      throw  new ValidationException(
                             "Invalid Column count.It has to be 2");
                   }
                   String url = (String)tbm.getValueAt(k,1);
                   if (url != null)
                   {
                      int firstIndex = url.indexOf("..");
                      int lastIndex = url.lastIndexOf("..");
                      if (firstIndex != -1 && lastIndex != -1 &&
                          firstIndex != lastIndex)
                      {
                       m_illegalUrl = true;
                       throw  new ValidationException(
                         sm_res.getString("illegalRelativeUrl"));
                      }
                      if (url.startsWith("http://"))
                      {
                       m_absUrl = true;
                       throw  new ValidationException(
                         sm_res.getString("absUrl"));
                      }

                      try
                      {
                       URL urlObj = new URL("http",null,url);
                      }
                      catch(MalformedURLException urlex)
                      {
                       m_malformedUrl = true;
                       throw new ValidationException(
                         sm_res.getString("malformedUrl"));
                      }
                      break;
                   }
                   else
                   {
                      m_nullUrl = true;
                      throw new ValidationException("URL must not be empty");
                   }
                }

            }
         }
      }
      else
         return;
   }      // End method checkComponent()

   /**
    * boolean value of true indicates if the
    * PSAddDefaultCommunity.SYS_DEFAULT_HOMEPAGEURL is an
    * absolute URL.
    */
   private boolean m_absUrl = false;

   /**
    * boolean value of true indicates if the
    * PSAddDefaultCommunity.SYS_DEFAULT_HOMEPAGEURL is a
    * relative URL but with two .. in it which is not allowed
    * for security reasons.
    */
   private boolean m_illegalUrl = false;

   private boolean m_nullUrl = false;

   /**
    * boolean value of true indicates if the
    * PSAddDefaultCommunity.SYS_DEFAULT_HOMEPAGEURL is a
    * malformed URL.
    */
   private boolean m_malformedUrl = false;

   private static final String SYS_URL = "sys_defaultHomepageURL";

   /**
    * create static resource bundle object
    */
   static ResourceBundle sm_res = null;
   static
   {
      try
      {
         sm_res = ResourceBundle.getBundle(
            "com.percussion.E2Designer.ValidationResources",
             Locale.getDefault()
         );
       }
       catch(MissingResourceException mre)
       {

       }
   }
}


