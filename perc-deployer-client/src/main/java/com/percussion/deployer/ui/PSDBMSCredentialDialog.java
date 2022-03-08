/******************************************************************************
 *
 * [ PSDBMSCredentialDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.deployer.ui;

import com.percussion.UTComponents.PSPasswordField;
import com.percussion.UTComponents.UTFixedHeightTextField;
import com.percussion.deployer.objectstore.PSDbmsInfo;
import com.percussion.guitools.PSDialog;
import com.percussion.guitools.PSPropertyPanel;
import com.percussion.validation.StringConstraint;
import com.percussion.validation.ValidationConstraint;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * The dialog for adding or editing DBMS credentials.
 */
public class PSDBMSCredentialDialog extends  PSDialog
{
   /**
    * Construct this class with all required parameters.
    *
    * @param parent the parent window of this dialog, may be <code>null</code>.
    *
    * @param dbmsInfo  may be code>null</code> if a new dbms entry is being
    * added, but if an entry is being edited it cannot be code>null</code>.
    *
    * @param drivers the list of database drivers as <code>String</code>s to
    * choose from, may not be <code>null</code> or empty.
    *
    * @throws IllegalArgumentException if any of the required parameters
    * are invalid.
    */
   public PSDBMSCredentialDialog(Dialog parent, PSDbmsInfo dbmsInfo,
      List drivers)
   {
      super(parent);

      if (drivers == null || drivers.isEmpty())
         throw new IllegalArgumentException(
            "drivers may not be null or empty.");

      m_drivers = drivers;
      m_dbmsInfo = dbmsInfo;
      initDialog();
   }

   /**
    * Initializes the dialog framework.
    */
   private void initDialog()
   {
      setTitle(getResourceString("title"));
      JPanel mainpanel = new JPanel();

      mainpanel.setLayout(new BoxLayout(mainpanel, BoxLayout.Y_AXIS));
      mainpanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

      PSPropertyPanel bodyPanel = new PSPropertyPanel();

      m_drvCombo = new JComboBox(m_drivers.toArray());
      JComponent[] drv = {m_drvCombo};
      bodyPanel.addPropertyRow(getResourceString("driver"), drv);

      m_serverField = new UTFixedHeightTextField();
      JComponent[] server = {m_serverField};
      bodyPanel.addPropertyRow(getResourceString("server"), server);

      m_dbField = new UTFixedHeightTextField();
      JComponent[] db = {m_dbField};
      bodyPanel.addPropertyRow(getResourceString("db"), db);

      m_schemaField = new UTFixedHeightTextField();
      JComponent[] schema = {m_schemaField};
      bodyPanel.addPropertyRow(getResourceString("schema"), schema);

      m_userField = new UTFixedHeightTextField();
      JComponent[] usr = {m_userField};
      bodyPanel.addPropertyRow(getResourceString("usr"), usr);

      m_pwField = new PSPasswordField();
      JComponent[] pw = {m_pwField};
      bodyPanel.addPropertyRow(getResourceString("pw"), pw);
      bodyPanel.setAlignmentX(CENTER_ALIGNMENT);
      JPanel cmd = createCommandPanel(SwingConstants.HORIZONTAL, true);

      mainpanel.add(bodyPanel);
      JPanel cmdPanel = new JPanel();
      cmdPanel.add(Box.createHorizontalGlue());
      cmdPanel.add(createCommandPanel(SwingConstants.HORIZONTAL, true));
      cmdPanel.add(Box.createHorizontalGlue());
      mainpanel.add(cmdPanel);
      getContentPane().add(mainpanel);
      init();
      center();
      setResizable(true);
      pack();
   }

   /**
    * Updates the dialog with existing dbms information.
    */
   private void init()
   {
      if (m_dbmsInfo != null)
      {
         m_serverField.setText(m_dbmsInfo.getServer());
         m_dbField.setText(m_dbmsInfo.getDatabase());
         m_schemaField.setText(m_dbmsInfo.getOrigin());
         m_userField.setText(m_dbmsInfo.getUserId());
         m_pwField.setText(m_dbmsInfo.getPassword(false));
      }
   }

  /**
   * Creates the validation framework and sets it in the parent dialog. Sets
   * the following validations.
   * <ol>
   * <li>server field is not empty </li>
   * </ol>
   */
   private void initValidationFramework()
   {
      List components = new ArrayList();
      List constraints = new ArrayList();
      components.add(m_serverField);
      constraints.add(new StringConstraint());
      setValidationFramework(
        (Component[])components.toArray(new Component[components.size()]),
        (ValidationConstraint[])constraints.toArray(
         new ValidationConstraint[constraints.size()]));
   }

   /**
    * Validates data - Checks if password has been supplied  without the
    * user name and prompts an error dialog, driver name is supplied,
    * by default the first item in the 'Driver' combo box is selected,
    * server name is supplied. Updates existing <code>m_dbmsInfo</code> if
    * editing or else a new one is created here.
    * Calls super's <code>onOk()</code> to dispose off the dialog.
    */
   public void onOk()
   {
      char[] pwd = m_pwField.getPassword();
      String usrId = m_userField.getText();
      if (pwd != null && pwd.length != 0)
      {
         if (usrId == null || usrId.length() == 0)
         {
            JOptionPane.showMessageDialog(this, getResourceString("usrErrMsg"),
            getResourceString("errTitle"), JOptionPane.ERROR_MESSAGE);
            return;
         }
      }

      //validates server field.
      initValidationFramework();
      if(!activateValidation())
         return;

      String driver = (String)m_drvCombo.getSelectedItem();

      String pwdStr = (pwd == null ? null : String.valueOf(pwd));
      if (m_dbmsInfo == null)
      {

         m_dbmsInfo = new PSDbmsInfo(driver, m_serverField.getText(),
            m_dbField.getText(), m_schemaField.getText(), usrId,
            pwdStr, false);
      }
      else
      {
         m_dbmsInfo.setDatabase(m_dbField.getText());
         m_dbmsInfo.setDriver(driver);
         m_dbmsInfo.setOrigin(m_schemaField.getText());
         m_dbmsInfo.setServer(m_serverField.getText());
         m_dbmsInfo.setUserNamePwd(usrId, pwdStr, false);
      }
      super.onOk();
   }

   /**
    * Gets a new or edited Dbms credential.
    * If a <code>null</code> <code>PSDbmsInfo</code> object is supplied to the
    * ctor then a new one is created or else the supplied one is edited.
    * @return dbms credential, never <code>null</code>.
    */
   public PSDbmsInfo getDbmsInfo()
   {
      return m_dbmsInfo;
   }

   /**
    * List of db drivers, which are <code>String</code> objects, initialized in
    * the constructor and never <code>null</code> or modified after that.
    */
   private List m_drivers;

   /**
    * Encapsulates database connection information, server name and
    * user credential. If a new entry is being created
    * <code>m_dbmsInfo</code> is initalised to <code>null</code> in ctor and a
    * new <code>m_dbmsInfo</code> is created in <code>onOk()</code>, if
    * <code>m_dbmsInfo</code> is not <code>null</code> it's modified with user
    * supplied values in <code>onOk()</code>.
    */
   private PSDbmsInfo m_dbmsInfo;

   /**
    * Field holding server name. Initialised in <code>initDialog()</code>,
    * never <code>null</code> after that.
    */
   private UTFixedHeightTextField m_serverField;

   /**
    * Field holding database name. Initialised in <code>initDialog()</code>,
    * never <code>null</code> after that.
    */
   private UTFixedHeightTextField m_dbField;

   /**
    * Field holding schema name. Initialised in <code>initDialog()</code>,
    * never <code>null</code> after that.
    */
   private UTFixedHeightTextField m_schemaField;

   /**
    * Field holding user name. Initialised in <code>initDialog()</code>,
    * never <code>null</code> after that.
    */
   private UTFixedHeightTextField m_userField;

   /**
    * Field holding password. Initialised in <code>initDialog()</code>,
    * never <code>null</code> after that.
    */
   private PSPasswordField m_pwField;

   /**
    * Holds list of driver names. Initialised in <code>initDialog()</code>,
    * never <code>null</code> or empty after that. Not modified.
    */
   private JComboBox m_drvCombo;
}
