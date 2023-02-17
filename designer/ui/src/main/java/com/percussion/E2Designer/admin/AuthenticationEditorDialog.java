/******************************************************************************
 *
 * [ AuthenticationEditorDialog.java ]
 * 
 * COPYRIGHT (c) 1999 - 2004 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer.admin;

import com.percussion.E2Designer.IConnectionSource;
import com.percussion.design.catalog.PSCataloger;
import com.percussion.design.catalog.exit.PSExtensionCatalogHandler;
import com.percussion.design.objectstore.PSAuthentication;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.security.IPSPasswordFilter;
import com.percussion.validation.StringConstraint;
import com.percussion.validation.ValidationConstraint;
import com.percussion.guitools.PSPropertyPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * An editor dialog used to create new or edit existing directory service 
 * authentications.
 */
public class AuthenticationEditorDialog extends DirectoryServiceDialog
{
   /**
    * Convenience constructor that calls {@link AuthenticationEditorDialog(
    * Frame, DirectoryServiceData, PSAuthentication)} with a default 
    * authentication.
    */
   public AuthenticationEditorDialog(Frame parent, DirectoryServiceData data)
   {
      this(parent, data, new PSAuthentication(
         AuthenticationsPanel.getDefaultAuthenticationName(), 
         PSAuthentication.SCHEME_NONE, null, null, null, null));
   }
   
   /**
    * Create an authentication editor for the supplied authentication.
    * 
    * @param data the current directory service data, not <code>null</code>.
    * @param authentication the authentication for which to create the editor,
    *    not <code>null</code>.
    */
   public AuthenticationEditorDialog(Frame parent, DirectoryServiceData data, 
      PSAuthentication authentication)
   {
      super(parent, data);
      authentication.setEncryptPwd(false);
      setTitle(getResources().getString("dlg.title"));
      
      initDialog();
      initData(authentication);
   }
   
   /**
    * Initializes the dialog from the supplied data.
    * 
    * @param authentication the authentication from which to initialize the
    *    dialog, not <code>null</code>.
    */
   public void initData(PSAuthentication authentication)
   {
      if (authentication == null)
         throw new IllegalArgumentException("authentication cannot be null");
         authentication.setEncryptPwd(false);
      m_currentName = authentication.getName();
      m_passwordEncrypted = authentication.isPasswordEncrypted();
      m_name.setText(authentication.getName());
      m_schema.setSelectedItem(AuthenticationsPanel.getSchemaDisplayString(
         authentication.getScheme()));
      
      m_userName.setText(authentication.getUser());
      m_appendBaseDn.setSelected(authentication.shouldAppendBaseDn());
      m_userAttribute.setText(authentication.getUserAttr());
      m_password.setText(authentication.getPassword());
      m_passwordFilter.setSelectedItem(authentication.getFilterExtension());
   }
   
   /**
    * Get the authentication object built from all editor controls.
    * 
    * @return an authentication object built from all editor controls,
    *    never <code>null</code>.
    */
   public PSAuthentication getAuthentication()
   {
      String name = m_name.getText();
      String schema = m_schema.getSelectedItem().toString();
      
      String user = m_userName.getText();
      if (user.trim().length() == 0)
         user = null;
         
      String userAttribute = m_userAttribute.getText();
      if (userAttribute.trim().length() == 0)
         userAttribute = null;
         
      String pw = new String(m_password.getPassword());
      if (pw.trim().length() == 0)
         pw = null;
         
      String pwFilter = m_passwordFilter.getSelectedItem().toString();
      if (pwFilter.trim().length() == 0)
         pwFilter = null;
         
      PSAuthentication authentication = new PSAuthentication(name, schema, 
         user, userAttribute, pw, pwFilter);
      authentication.setPasswordEncrypted(m_passwordEncrypted);
      authentication.setEncryptPwd(false);
      authentication.setAppendBaseDn(m_appendBaseDn.isSelected());
      
      return authentication;
   }
   
   /**
    * Overrides super class to validate the name uniqueness.
    */
   public void onOk()
   {
      if (!activateValidation())
         return;
         
      String name = m_name.getText();
      if (!m_currentName.equals(name) && 
         m_data.getAuthenticationNames().contains(name))
      {
         JOptionPane.showMessageDialog(null, 
            getResources().getString("error.msg.notunique"), 
            getResources().getString("error.title"), JOptionPane.ERROR_MESSAGE);
            
         return; 
      }
      
      super.onOk();
   }
   
   /**
    * Initializes the dialogs UI.
    */
   private void initDialog()
   {
      JPanel panel = new JPanel(new BorderLayout(20, 10));
      panel.setBorder((new EmptyBorder (5, 5, 5, 5)));
      getContentPane().add(panel);

      panel.add(createPropertyPanel(), BorderLayout.CENTER);
      JPanel bottomPanel = new JPanel(new BorderLayout());
      bottomPanel.add(createCommandPanel(SwingConstants.HORIZONTAL, true), 
              BorderLayout.EAST);
      panel.add(bottomPanel, BorderLayout.SOUTH);

      initValidationFramework();
      
      setResizable(true);
      pack();
      center();
   }
   
   /**
    * @return the new created property panel, never <code>null</code>.
    */
   private JPanel createPropertyPanel()
   {
      JPanel panel = new JPanel();
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

      panel.add(createGeneralPropertiesPanel());
      panel.add(Box.createVerticalStrut(10));
      panel.add(Box.createVerticalGlue());
      panel.add(createCredentialsPropertyPanel());
      panel.add(Box.createVerticalStrut(10));
      panel.add(Box.createVerticalGlue());
      panel.add(createCredentialAttributesPropertyPanel());

      return panel;
   }
   
   /**
    * @return the new created general property panel, never <code>null</code>.
    */
   private JPanel createGeneralPropertiesPanel()
   {
      PSPropertyPanel panel = new PSPropertyPanel();

      m_name.setToolTipText(getResources().getString("ctrl.name.tip"));
      panel.addPropertyRow(getResources().getString("ctrl.name"),
         new JComponent[] { m_name }, m_name,
         getResources().getString("ctrl.name.mn").charAt(0), 
         getResources().getString("ctrl.name.tip"));
         
      m_schema.setToolTipText(getResources().getString("ctrl.schema.tip"));
      panel.addPropertyRow(getResources().getString("ctrl.schema"),
         new JComponent[] { m_schema }, m_schema,
         getResources().getString("ctrl.schema.mn").charAt(0), 
         getResources().getString("ctrl.schema.tip"));
         
      String[] schemes = PSAuthentication.SCHEME_ENUM;
      for (int i=0; i<schemes.length; i++)
         m_schema.addItem(AuthenticationsPanel.getSchemaDisplayString(schemes[i]));

      return panel;
   }
   
   /**
    * @return the new create credential property panel, never <code>null</code>.
    */
   private JPanel createCredentialsPropertyPanel()
   {
      JPanel panel = new JPanel();
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
      panel.setBorder(new TitledBorder(getResources().getString(
         "border.credentials")));
      
      PSPropertyPanel props = new PSPropertyPanel();
      
      String txt = getResources().getString("ctrl.appendbasedn");
      m_appendBaseDn.setText(txt);
      char mn = getResources().getString("ctrl.appendbasedn.mn").charAt(0);
      m_appendBaseDn.setDisplayedMnemonicIndex(txt.indexOf(mn));
      m_appendBaseDn.setToolTipText(getResources().getString(
         "ctrl.appendbasedn.tip"));
         
      JPanel namePanel = new JPanel();
      namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.X_AXIS));
      namePanel.add(m_userName);
      namePanel.add(Box.createHorizontalStrut(10));
      namePanel.add(Box.createHorizontalGlue());
      namePanel.add(m_appendBaseDn);

      m_userName.setToolTipText(getResources().getString(
         "ctrl.username.tip"));
      props.addPropertyRow(getResources().getString("ctrl.username"),
         new JComponent[] { namePanel }, m_userName,
         getResources().getString("ctrl.username.mn").charAt(0), 
         getResources().getString("ctrl.username.tip"));

      m_password.setToolTipText(getResources().getString(
         "ctrl.password.tip"));
      props.addPropertyRow(getResources().getString("ctrl.password"),
         new JComponent[] { m_password }, m_password,
         getResources().getString("ctrl.password.mn").charAt(0), 
         getResources().getString("ctrl.password.tip"));
      
      panel.add(props);
      return panel;
   }
   
   /**
    * @return the new create credential property panel, never <code>null</code>.
    */
   private JPanel createCredentialAttributesPropertyPanel()
   {
      JPanel panel = new JPanel();
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
      panel.setBorder(new TitledBorder(getResources().getString(
         "border.credentialattributes")));
      
      PSPropertyPanel props = new PSPropertyPanel();

      m_userAttribute.setToolTipText(getResources().getString(
         "ctrl.userattribute.tip"));
      props.addPropertyRow(getResources().getString("ctrl.userattribute"),
         new JComponent[] { m_userAttribute }, m_userAttribute,
         getResources().getString("ctrl.userattribute.mn").charAt(0), 
         getResources().getString("ctrl.userattribute.tip"));

      m_passwordFilter.setToolTipText(getResources().getString(
         "ctrl.passwordfilter.tip"));
      props.addPropertyRow(getResources().getString("ctrl.passwordfilter"),
         new JComponent[] { m_passwordFilter }, m_passwordFilter,
         getResources().getString("ctrl.passwordfilter.mn").charAt(0), 
         getResources().getString("ctrl.passwordfilter.tip"));
      m_passwordFilter.addItem("");
      try
      {
         IConnectionSource connection = AppletMainDialog.getServerConnection();
         if (connection == null)
         {
            m_passwordFilter.addItem(getResources().getString(
               "ctrl.passwordfilter.nocataloging"));
         }
         else
         {
            IPSExtensionDef [] filters = PSExtensionCatalogHandler.getCatalog(
               new PSCataloger(connection.getDesignerConnection(false)),
                  "%", null, IPSPasswordFilter.class.getName());
            if (filters.length > 0)
            {
               for (int i=0; i<filters.length; ++i)
                  m_passwordFilter.addItem(filters[i].getRef().toString());
            }
            else
            {
               m_passwordFilter.addItem(getResources().getString(
                  "ctrl.passwordfilter.nofilters"));
            }
         }
      }
      catch (Exception e)
      {
         m_passwordFilter.addItem(e.getLocalizedMessage());
      }
      
      panel.add(props);
      panel.setPreferredSize(new Dimension(460, 
         panel.getPreferredSize().height));
         
      return panel;
   }
   
   /**
    * Initialize the validation framework for this dialog.
    */
   private void initValidationFramework()
   {
      List comps = new ArrayList();
      List validations = new ArrayList();
      StringConstraint nonEmpty = new StringConstraint();

      // name: cannot be empty
      comps.add(m_name);
      validations.add(nonEmpty);
         
      //  scheme: cannot be empty
      comps.add(m_schema);
      validations.add(nonEmpty);

      Component[] components = new Component[comps.size()];
      comps.toArray(components);
      
      ValidationConstraint[] constraints = 
         new ValidationConstraint[validations.size()];
      validations.toArray(constraints);
      
      setValidationFramework(components, constraints);
   }
   
   /**
    * Overridden to avoid obfuscation issues.
    */
   protected ResourceBundle getResources()
   {
      return super.getResources();
   }
   
   /**
    * The authentication name at initialization time. Used to validate the
    * name for uniqueness. Never <code>null</code> or changed.
    */
   private String m_currentName = null;
   
   /**
    * The authentication name, it's value cannot be empty and must be unique 
    * across all other authentications in this server.
    */
   private JTextField m_name = new JTextField();
   
   /**
    * The authentication scheme, it's value cannot be <code>null</code> or 
    * empty.
    */
   private JComboBox m_schema = new JComboBox();
   
   /**
    * The user name used to catalog a directory, it's value may be empty.
    */
   private JTextField m_userName = new JTextField();
   
   /**
    * A flag to let the user specify whether or not to append the base DN to 
    * the user name used for authentication. Initialized during construction, 
    * never <code>null</code> or changed after that.
    */
   private JCheckBox m_appendBaseDn = new JCheckBox();
   
   /**
    * The attribute name for the user making the catalog requests, it's value
    * may be empty.
    */
   private JTextField m_userAttribute = new JTextField();
   
   /**
    * The password used to catalog a directory, it's value may be empty.
    */
   private JPasswordField m_password = new JPasswordField();

   private boolean m_passwordEncrypted = false;
   
   /**
    * The password filter used to decode the password before sending it to
    * the directory server, it's value may be <code>null</code> or empty.
    */
   private JComboBox m_passwordFilter = new JComboBox();
}
