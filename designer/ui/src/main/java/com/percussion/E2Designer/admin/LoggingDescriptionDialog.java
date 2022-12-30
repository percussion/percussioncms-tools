/*[ LoggingDescriptionDialog.java ]********************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer.admin;


import com.percussion.E2Designer.PSDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;



 /** 
   * Constructor that takes the dialog title and the table that is to be displayed in the dialog.
   */
public class LoggingDescriptionDialog extends PSDialog
{
   public LoggingDescriptionDialog(String title, JTable table)
   {
      super(AppletMainDialog.getMainframe(), title);
      setSize(750, 550);
      setResizable(true);
      addTable(table);
      pack();
      center();

   }

 /** 
   * adds the table to the dialog.
   */
   private void addTable(JTable table)
   {
      getContentPane().setLayout(new BorderLayout());
      JPanel panel = new JPanel(new BorderLayout());
      panel.setBorder(new EmptyBorder(5,5,5,5));

    JScrollPane scrollPane = new JScrollPane(table);
      panel.add(scrollPane, BorderLayout.CENTER);
      getContentPane().add(panel, BorderLayout.CENTER);
   }




}
