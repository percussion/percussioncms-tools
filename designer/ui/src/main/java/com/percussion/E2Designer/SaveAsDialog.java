/* *****************************************************************************
 *
 * [ SaveAsDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *******************************************************************************/

package com.percussion.E2Designer;

import com.percussion.conn.PSServerException;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSObjectStore;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.util.PSSortTool;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.Collator;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;


/** Saves Applications on a particular E2 server.  It provides a list of
  * existing files on the selected server, and the user can save over an
  * existing application or specify a new name.
*/

public class SaveAsDialog extends PSDialog
{
  public enum ReturnCommand { CANCEL, OK }
//
// CONSTRUCTORS
//

  public SaveAsDialog(PSApplication app) throws PSAuthenticationFailedException,
                                                PSServerException
  {
    super();

    m_app = app;

    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(createMainPanel(), BorderLayout.CENTER);
    getContentPane().add(createCommandPanel(), BorderLayout.SOUTH);

    getRootPane().setDefaultButton(m_commandPanel.getOkButton());

    setSize(400, 300);
    setResizable(true);
    center();
  }

/** Overriding PSDialog.onOk() implementation.
*/
  public void onOk()
  {
    // if application name is empty, warn user then have them enter a name
    // again
    if (m_appName.getText().equals(""))
    {
      JOptionPane.showMessageDialog(this,
                                    getResources().getString("emptyname"),
                                    getResources().getString("error"),
                                    JOptionPane.OK_OPTION);

      m_appName.requestFocus();
      return;
    }

    if (-1 != m_appName.getText().indexOf("\\"))
    {
      JOptionPane.showMessageDialog(this,
                                    getResources().getString("errorbadname"),
                                    getResources().getString("error"),
                                    JOptionPane.OK_OPTION);

      m_appName.requestFocus();
      return;
    }

    m_returnCommand = ReturnCommand.OK;
    dispose();
  }


//
// PRIVATE METHODS
//

/** Creates the panel to hold the buttons on the right of the dialog.
  *
  * @returns JPanel The panel of all the buttons.
*/
  private JPanel createCommandPanel()
  {
    m_commandPanel = new UTStandardCommandPanel(this, "", 
                                                SwingConstants.HORIZONTAL)
    {
      public void onOk()
      {
        SaveAsDialog.this.onOk();
      }

      public void onCancel()
      {
        SaveAsDialog.this.onCancel();
      }
    };

    m_commandPanel.setBorder(new EmptyBorder(4,4,0,4));

    JPanel cmdPanel = new JPanel(new BorderLayout());
    cmdPanel.add(m_commandPanel, BorderLayout.EAST);
    return cmdPanel;
  }

  /*
   * Get the new application name.
   *
   * @return String the new choosen application name
   */
  //////////////////////////////////////////////////////////////////////////////
  public String getNewName()
  {
    return m_appName.getText();
  }

  /*
   * Returns whether or not we need to validate the application while saving.
   *
   * @return boolean true if validation is needed.
   */
  //////////////////////////////////////////////////////////////////////////////
  public boolean validateApplication()
  {
    return m_validateAppBox.isSelected();
  }

  /*
   * Returns the command, the user selected to leave this dialog.
   *
   * @return UserChoice the command (either OK or CANCEL)
   */
  //////////////////////////////////////////////////////////////////////////////
  public ReturnCommand getCommand()
  {
    return m_returnCommand;
  }

/** Creates the main portion of the SaveAsDialog.  A JList, a JTextField, and
  * a JComboBox are created.
  *
  * @returns JPanel A panel that holds the aforementioned components.
*/
  private JPanel createMainPanel() throws PSServerException,
                                          PSAuthenticationFailedException
  {
    m_appListModel = new DefaultListModel();
    m_appList = new JList(m_appListModel);
    m_appList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    m_appList.addListSelectionListener(new ListSelectionListener()
    {
      public void valueChanged(ListSelectionEvent e)
      {
        // select the cell in the list displays the name of that file.
        m_appName.setText(m_appList.getSelectedValue().toString());
      }
    });
    m_appList.addMouseListener(new MouseAdapter()
    {
      public void mouseClicked(MouseEvent e)
      {
        // clicking on a list cell twice calls the "ok" command (ok button).
        if (2 <= e.getClickCount())
        {
          m_commandPanel.onOk();
        }
      }
    });

    populateList();

    JScrollPane scrollPane = new JScrollPane(m_appList);

    JPanel mainPanel = new JPanel(new BorderLayout());
    mainPanel.setBorder(new EmptyBorder(6,6,6,6));
    mainPanel.add(scrollPane, BorderLayout.CENTER);
    mainPanel.add(createAppNamePanel(), BorderLayout.SOUTH);

    return mainPanel;
  }

/** Creates the panel on the bottom.
  *
  * @returns JPanel The panel that contains a JLabel and a JTextField.
*/
  private JPanel createAppNamePanel()
  {
    m_appName = new JTextField(m_app.getName(), 10);
    m_appName.setPreferredSize(new Dimension(200, 20));
    m_appName.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        getRootPane().getDefaultButton().doClick();
      }
    });
    
    String labelStr = getResources().getString("appname");
    JLabel appName = new JLabel(labelStr, SwingConstants.RIGHT);
    char mn = getResources().getString("appname.mn").charAt(0);
    appName.setDisplayedMnemonicIndex(labelStr.indexOf(mn));
    appName.setDisplayedMnemonic(mn);
    appName.setPreferredSize(new Dimension(60, 20));
    appName.setLabelFor(m_appName);
    
    labelStr = getResources().getString("validate");
    m_validateAppBox = new JCheckBox(labelStr);
    mn = getResources().getString("validate.mn").charAt(0);
    m_validateAppBox.setMnemonic(mn);
    
    JPanel innerBoxPanel = new JPanel(new BorderLayout());
    innerBoxPanel.add(m_validateAppBox, BorderLayout.WEST);

    JPanel appNamePanel = new JPanel(new BorderLayout());
    
    appNamePanel.setLayout(new BoxLayout(appNamePanel, BoxLayout.X_AXIS));
    appNamePanel.add(appName);
    appNamePanel.add(Box.createHorizontalStrut(4));
    appNamePanel.add(m_appName);
    appNamePanel.add(Box.createHorizontalGlue());

    JPanel bottomPanel = new JPanel();
    bottomPanel.setBorder(new EmptyBorder(6,0,0,0));
    bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
    bottomPanel.add(appNamePanel);
    bottomPanel.add(Box.createVerticalStrut(4));
    bottomPanel.add(innerBoxPanel);
    bottomPanel.add(Box.createVerticalGlue());

    return bottomPanel;
  }


/** Goes to the objectstore and retrieves the application summaries, then store
  * them in an Enumeration member variable.
  *
  * @throws PSServerException The server has a problem.
*/
  private void populateList() throws PSAuthenticationFailedException,
                                     PSServerException
  {
    PSObjectStore os = E2Designer.getApp().getMainFrame().getObjectStore();

    Properties props = new Properties();
    props.put(sm_NAME, "");

    m_allAppNames = os.getApplicationSummaries(props);

    Vector<String> v = new Vector<String>();
    for (;m_allAppNames.hasMoreElements();)
    {
      props = (Properties)m_allAppNames.nextElement();
      v.addElement((String)props.get(sm_NAME));
    }

    // sorting all Application names received alphabetically
    // (numbers before letters)
    Object[] array = v.toArray();
    PSSortTool.MergeSort(array, Collator.getInstance());

    for (int i = 0; i < array.length; i++)
      m_appListModel.addElement(array[i]);
  }

//
// MEMBER VARIABLES
//

  private UTStandardCommandPanel m_commandPanel = null;
  private JCheckBox              m_validateAppBox = null;
  private JTextField             m_appName      = null;
  private JList                  m_appList      = null;
  private DefaultListModel       m_appListModel = null;

  private Enumeration            m_allAppNames  = null;

  private PSApplication          m_app          = null;

  private ReturnCommand m_returnCommand = ReturnCommand.CANCEL;

  private static final String sm_NAME = "name";
}

