/*[ XMLBrowserDialog.java ]****************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.TreePath;
import java.awt.*;

// This dialog is used to change the types of files that show up in the file tab

public class XMLBrowserDialog extends PSDialog
{
  /** Constructor which takes the JFrame object.
   *
   */
    public XMLBrowserDialog( JFrame frame, OSPageDatatank data)
   {
       super( frame );
      setTitle(getResources().getString("title"));
      
      
       m_commandPanel = new UTStandardCommandPanel(this, "", SwingConstants.VERTICAL)
      {
         public void onOk()
         {
            XMLBrowserDialog.this.onOk();
         }
      };
      
            
      XMLBrowser browser = new XMLBrowser(data);
      m_List = browser.getTree();

      getRootPane().setDefaultButton(m_commandPanel.getOkButton());

      JPanel panel = new JPanel(new BorderLayout());
      panel.setBorder(new EmptyBorder(5, 5, 5, 5));
      JPanel listpanel = new JPanel(new BorderLayout());
      listpanel.setBorder(new EmptyBorder(5,5,5,5));
      JScrollPane sPane = new JScrollPane(m_List);
      listpanel.add(sPane, "Center");
      panel.add(listpanel, "Center");
      panel.add(m_commandPanel, "East");

        getContentPane().setLayout(new BorderLayout());
      getContentPane().add(panel);
      this.setSize(DIALOG_SIZE);
        center();
      setVisible(true);
   }
   
   public boolean wasOKPressed()
   {
      return(m_bOk);
   }


/** Handles ok button action. Overrides PSDialog onOk() method implementation.
*/
  public void onOk()
  {
    m_bOk = true;
            
    dispose();
  }
   
   public String getData()
   {
      if(m_List != null)
      {
         TreePath selpath = m_List.getSelectionPath();
          if (selpath != null)
          {
            Object[] path = selpath.getPath();
            if(path != null && path.length > 0)
            {
               String strRet = new String(path[0].toString());   
               for(int iPath = 1; iPath < path.length; ++iPath)
               {
                  strRet += "/";
                  strRet += path[iPath].toString();
               }
               return(strRet);
            }
         }
      }

      return(new String(""));
   }
   
  private UTStandardCommandPanel m_commandPanel = null;
  private MapBrowserTree m_List = null;
  private boolean m_bOk = false;
  private final static Dimension DIALOG_SIZE = new Dimension(300, 200);

}
