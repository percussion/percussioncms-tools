/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.htmlConverter;

/*[ FileSaveDialog ]***********************************************************
 * $Id: FileSaveDialog.java 1.2 1999/09/23 20:41:21Z martingenhart Release $
 *
 * Version Labels  : $Name: Pre_CEEditorUI RX_40_REL 20010618_3_5 20001027_3_0 20000724_2_0 20000522_1_1 20000501_1_1 20000327_1_1 20000111_1_0 991227_1_0 991214_1_0 991213_1_0 991202_1_0 Release_1_0_Intl Release_10_1 Release_10 $
 *
 * Locked By       : $Locker: $
 *
 * Revision History:
 *                   $Log: FileSaveDialog.java $
 *                   Revision 1.2  1999/09/23 20:41:21Z  martingenhart
 *                   make inner classes for our new build system
 *                   Revision 1.1  1999/06/30 23:36:09  RammohanVangapalli
 *                   Initial revision
 *                   Initial revision
 *
 ***************************************************************************/

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;

public class FileSaveDialog extends JDialog
{
  class FileSaveDialog_button1_actionAdapter implements ActionListener{
    FileSaveDialog adaptee;

    FileSaveDialog_button1_actionAdapter(FileSaveDialog adaptee) {
      this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
      adaptee.button1_actionPerformed(e);
    }
  }

  class FileSaveDialog_button2_actionAdapter implements ActionListener{
    FileSaveDialog adaptee;

    FileSaveDialog_button2_actionAdapter(FileSaveDialog adaptee) {
      this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
      adaptee.button2_actionPerformed(e);
    }
  }

  class FileSaveDialog_this_windowAdapter extends WindowAdapter {
    FileSaveDialog adaptee;

    FileSaveDialog_this_windowAdapter(FileSaveDialog adaptee) {
      this.adaptee = adaptee;
    }

    public void windowClosing(WindowEvent e) {
      adaptee.this_windowClosing(e);
    }
  }

  JPanel dialogPanel = new JPanel();
  JPanel mainPanel = new JPanel();
  JButton button1 = new JButton();
  JButton button2 = new JButton();
  Border border1;
  Border border2;
  JPanel jPanel1 = new JPanel();
  JPanel jPanel2 = new JPanel();
  GridLayout gridLayout1 = new GridLayout();
  GridBagLayout gridBagLayout1 = new GridBagLayout();
  GridBagLayout gridBagLayout2 = new GridBagLayout();
  JTextField jTextFileName = new JTextField();
  GridBagLayout gridBagLayout3 = new GridBagLayout();

  public FileSaveDialog(JFrame frame, String title, boolean modal) {
    super(frame, title, modal);
    try {
      jbInit();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    pack();
  }

  public FileSaveDialog(JFrame frame, String title) {
    this(frame, title, false);
  }

  public FileSaveDialog(JFrame frame) {
    this(frame, "", false);
  }

  private void jbInit() throws Exception {
    border1 = BorderFactory.createRaisedBevelBorder();
    border2 = BorderFactory.createEtchedBorder();
    mainPanel.setLayout(gridBagLayout3);
    button1.setText("OK");
    button1.addActionListener(new FileSaveDialog_button1_actionAdapter(this));
    button2.setText("Cancel");
    gridLayout1.setColumns(1);
    gridLayout1.setVgap(2);
    gridLayout1.setRows(2);
    jPanel2.setLayout(gridLayout1);
    jPanel1.setLayout(gridBagLayout1);
    button2.addActionListener(new FileSaveDialog_button2_actionAdapter(this));
    this.addWindowListener(new FileSaveDialog_this_windowAdapter(this));
    dialogPanel.setLayout(gridBagLayout2);
    dialogPanel.add(mainPanel, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 8), 0, 0));
    mainPanel.add(jTextFileName, new GridBagConstraints(0, 1, 1, 2, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 212, 6));
    dialogPanel.add(jPanel2, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.NORTH, GridBagConstraints.NONE, new Insets(12, 0, 12, 8), 0, 0));
    jPanel2.add(button1, null);
    jPanel2.add(button2, null);
    getContentPane().add(dialogPanel);
  }


  // OK
  void button1_actionPerformed(ActionEvent e) {
    dispose();
  }

  // Cancel
  void button2_actionPerformed(ActionEvent e) {
    dispose();
  }

  void this_windowClosing(WindowEvent e) {
    dispose();
  }
}

