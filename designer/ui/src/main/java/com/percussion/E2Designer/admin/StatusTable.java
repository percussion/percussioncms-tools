/*[ StatusTable.java ]*********************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer.admin;

import com.percussion.E2Designer.UTTableModel;
import com.percussion.xml.PSXmlTreeWalker;

import java.util.ResourceBundle;
import java.util.Vector;

/**
 * The table model used for the server admin status panel.
 */
////////////////////////////////////////////////////////////////////////////////
public class StatusTable extends UTTableModel
{
   /**
   * Construct the table
   *
    */
  //////////////////////////////////////////////////////////////////////////////
   public StatusTable()
   {
    super();
    createTable(m_headers);
   }

   /**
   * Initialize application table from server
   *
   * @param ...
   */
  //////////////////////////////////////////////////////////////////////////////
  public void initTable(ServerConsole console)
  {
     try
    {
         // list all active applications and related information
         org.w3c.dom.Document doc = console.execute("show applications");
         PSXmlTreeWalker   walker   = new PSXmlTreeWalker(doc);
         if (walker.getNextElement("Applications", true, true) != null)
      {
            org.w3c.dom.Node current = walker.getCurrent();
        int index = 0;
        org.w3c.dom.Element element = null;
            while ((element = walker.getNextElement("Application", true, true)) != null)
         {
          boolean bEnabled = false;
          if (null != element.getAttribute("enabled"))
            bEnabled = element.getAttribute("enabled").equals("yes");

          boolean bActive = false;
          if (null != element.getAttribute("active"))
            bActive = element.getAttribute("active").equals("yes");
            
           String name = walker.getElementData("name", false);
            
          String status = bActive ? m_res.getString("active") : m_res.getString("inactive");

             Vector vTableRow = new Vector();
          vTableRow.addElement(name);
          vTableRow.addElement(m_res.getString("typeApplication"));
          vTableRow.addElement(status);

          appendRow(vTableRow);
        }

        walker.setCurrent(current);
      }
    }
    catch (Exception e)
    {
       e.printStackTrace();
    }
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
        m_headers.addElement(m_res.getString("tableName"));
        m_headers.addElement(m_res.getString("tableType"));
        m_headers.addElement(m_res.getString("tableStatus"));
    }
  }
   /**
   * Resources
   */
  private static ResourceBundle m_res = PSServerAdminApplet.getResources();
}
