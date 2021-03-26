/*[ FileTypesDialog.java ]*****************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer.browser;

import com.percussion.E2Designer.PSDialog;
import com.percussion.E2Designer.UTStandardCommandPanel;
import com.percussion.EditableListBox.EditableListBox;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


// This dialog is used to change the types of files that show up in the file tab

public class FileTypesDialog extends PSDialog
{
  /** Constructor which takes the JFrame object.
   *
   */
   public FileTypesDialog( JFrame frame, String[] data)
   {
       super( frame );
      setTitle(getResources().getString("title"));
      
      m_commandPanel = new UTStandardCommandPanel(this, "", SwingConstants.VERTICAL)
      {
         public void onOk()
         {
            m_bOk = true;
            m_data = new String[m_List.getListModel().getRowCount()];
            for(int i = 0; i < m_List.getListModel().getRowCount(); ++i)
            {
               Object o =    m_List.getListModel().getValueAt(i,0);
               if(o != null)
                  m_data[i] = o.toString();
            }
            
            dispose();
         }
      };
      
      m_List = new EditableListBox(getResources().getString("filetypes"), null, data);
    m_List.getRightButton().addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        m_List.deleteRows();
      }
    });
    
      getRootPane().setDefaultButton(m_commandPanel.getOkButton());

      JPanel panel = new JPanel(new BorderLayout());
      panel.setBorder(new EmptyBorder(5, 5, 5, 5));
      JPanel listpanel = new JPanel(new BorderLayout());
      listpanel.setBorder(new EmptyBorder(5,5,5,5));
      listpanel.add(m_List, "Center");
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
   
   public String[] getData()
   {
      return(m_data);
   }
   
  private UTStandardCommandPanel m_commandPanel = null;
  private EditableListBox m_List = null;
  private boolean m_bOk = false;
  private final static Dimension DIALOG_SIZE = new Dimension(300, 200);
  private String[] m_data = null;

}
