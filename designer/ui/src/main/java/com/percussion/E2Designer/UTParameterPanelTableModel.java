/*[ UTParameterPanelTableModel.java ]******************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Vector;

/**
 * The table model used for the parameter panel.
 */
////////////////////////////////////////////////////////////////////////////////
public class UTParameterPanelTableModel extends UTTableModel
{
   /**
   * Construct the table
    */
  //////////////////////////////////////////////////////////////////////////////
   public UTParameterPanelTableModel()
   {
    super();
    createTable(m_headers);
   }

   /**
   * Get table resources.
   *
   */
  //////////////////////////////////////////////////////////////////////////////
  private ResourceBundle m_res = null;
  public ResourceBundle getResources()
  {
      try
    {
      if (m_res == null)
          m_res = ResourceBundle.getBundle(getClass().getName() + "Resources",
                                                           Locale.getDefault());
    }
    catch (MissingResourceException e)
      {
         System.out.println(e);
      }

       return m_res;
    }

  //////////////////////////////////////////////////////////////////////////////
  /**
   * the table headers
   */
  private static Vector m_headers = new Vector();
  {
     if (m_headers.isEmpty())
    {
        // initialize table headers
        m_headers.addElement(getResources().getString("parameter"));
        m_headers.addElement(getResources().getString("value"));
        m_headers.addElement(getResources().getString("description"));
    }
  }

  // the table column indexes
  public static final int PARAMETER = 0;
  public static final int VALUE = 1;
  public static final int DESCRIPTION = 2;
}

