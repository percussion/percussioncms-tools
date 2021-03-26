/*[ PSWebServerSecurityProviderDialog.java ]************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer.admin;

import com.percussion.E2Designer.SecurityProviderMetaData;
import com.percussion.design.objectstore.PSSecurityProviderInstance;
import com.percussion.error.PSIllegalArgumentException;
import com.percussion.security.PSWebServerProvider;
import com.percussion.validation.ListMemberConstraint;
import com.percussion.validation.StringConstraint;
import com.percussion.validation.StringLengthConstraint;
import com.percussion.validation.ValidationConstraint;
import com.percussion.guitools.PSDialog;
import com.percussion.guitools.PSPropertyPanel;
import com.percussion.guitools.UTStandardCommandPanel;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * Dialog to enter the properties for a web server security provider.
 */
public class PSWebServerSecurityProviderDialog extends PSDialog
   implements ISecurityProviderEditor
{
   /**
    * Constructs a new dialog to setup web server security providers.
    * 
    * @param parent the parent frame, may be <code>null</code>.
    * @param type the security provider type, one of <code>SP_TYPE_xxx</code>.
    */
   public PSWebServerSecurityProviderDialog(JFrame parent, int type)
   {
      super(parent, 
         SecurityProviderMetaData.getInstance().getDisplayNameForId(type));
         
      m_type = type;
      initDialog();
   }

   /**
    * Initialize this dialogs ui components.
    */
   private void initDialog()
   {
      getContentPane().setLayout(new BorderLayout());
      
      getContentPane().add(createMainPanel(), BorderLayout.CENTER);
      JPanel cmdPanel = new JPanel(new BorderLayout());
      cmdPanel.add(createCommandPanel(), BorderLayout.EAST);
      getContentPane().add(cmdPanel, BorderLayout.SOUTH);
      loadDefaults();
      
      pack();
      center();
   }
   
   /**
    * Creates the main panel for this dialog, which contains 4 text fields
    * the user must specify.
    * 
    * @return the main panel, never <code>null</code>.
    */
   private JPanel createMainPanel()
   {
      PSPropertyPanel panel = new PSPropertyPanel();
      panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

      m_name.setToolTipText(getResources().getString("textfield.name.tip"));
      panel.addPropertyRow(getResources().getString("textfield.name"),
         new JComponent[] { m_name }, m_name,
         getResources().getString("textfield.name.mn").charAt(0), "");

      m_userHeader.setToolTipText(getResources().getString(
         "textfield.user.headername.tip"));
      panel.addPropertyRow(getResources().getString("textfield.user.headername"),
         new JComponent[] { m_userHeader }, m_userHeader,
         getResources().getString("textfield.user.headername.mn").charAt(0), "");

      m_roleHeader.setToolTipText(getResources().getString(
         "textfield.role.headername.tip"));
      panel.addPropertyRow(getResources().getString("textfield.role.headername"),
         new JComponent[] { m_roleHeader }, m_roleHeader,
         getResources().getString("textfield.role.headername.mn").charAt(0), "");

      m_roleDelimiter.setToolTipText(getResources().getString(
         "textfield.role.delimiter.tip"));
      panel.addPropertyRow(getResources().getString("textfield.role.delimiter"),
         new JComponent[] { m_roleDelimiter }, m_roleDelimiter,
         getResources().getString("textfield.role.delimiter.mn").charAt(0), "");
         
      return panel;
   }

   /**
    * Creates the command panel with 'OK', 'Cancel' and 'Help' buttons in 
    * vertical direction.
    *
    * @return the command panel, never <code>null</code>.
    */
   private JPanel createCommandPanel()
   {
      JPanel panel = new UTStandardCommandPanel(this, 
         SwingConstants.HORIZONTAL, true);
      panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
         
      return panel;
   }
   
   /**
    * Process the action for the Ok button.
    */
   public void onOk()
   {
      initValidationFramework();

      if (!activateValidation())
         return;

      String strProvName = m_name.getText().trim();
      String strUserHeader = m_userHeader.getText().trim();
      String strRoleHeader = m_roleHeader.getText().trim();
      String strRoleDelimiter = m_roleDelimiter.getText().trim();

      try
      {
         if (m_instance != null)
         {
            if (!strProvName.equalsIgnoreCase(m_instance.getName()))
            {
               m_instance.setName(strProvName);
               m_isModified = true;
            }
            
            if (!strUserHeader.equalsIgnoreCase(
               m_instance.getProperties().getProperty(
                  PSWebServerProvider.AUTHENTICATED_USER_HEADER, "")))
            {
               setProperty(m_instance, 
                  PSWebServerProvider.AUTHENTICATED_USER_HEADER, strUserHeader);
               m_isModified = true;
            }
            
            if (!strRoleHeader.equalsIgnoreCase(
               m_instance.getProperties().getProperty(
                  PSWebServerProvider.USER_ROLE_LIST_HEADER, "")))
            {
               setProperty(m_instance, 
                  PSWebServerProvider.USER_ROLE_LIST_HEADER, strRoleHeader);
               m_isModified = true;
            }
            
            if (!strRoleDelimiter.equalsIgnoreCase(
               m_instance.getProperties().getProperty(
                  PSWebServerProvider.ROLE_LIST_DELIMITER, "")))
            {
               setProperty(m_instance, 
                  PSWebServerProvider.ROLE_LIST_DELIMITER, strRoleDelimiter);
               m_isModified = true;
            }
         }
         else
         {
            m_instance = new PSSecurityProviderInstance(strProvName, m_type);
            setProperty(m_instance, 
               PSWebServerProvider.AUTHENTICATED_USER_HEADER, strUserHeader);
            setProperty(m_instance, 
               PSWebServerProvider.USER_ROLE_LIST_HEADER, strRoleHeader);
            setProperty(m_instance, 
               PSWebServerProvider.ROLE_LIST_DELIMITER, strRoleDelimiter);
               
            m_isModified = true;
         }
      }
      catch (PSIllegalArgumentException e)
      {
         // This should not happen because we have validated the name
      }

      setVisible(false);
   }
   
   /**
    * Process the action for the Cancel button.
    */
   public void onCancel()
   {
      m_isModified = false;
      setVisible(false);
   }
      
   /**
    * Creates the validation framework and sets it in the parent dialog. After
    * setting it, the <code>m_validationInited</code> flag is set to indicate 
    * it doesn't need to be done again. If something changes requiring a new 
    * framework, just clear this flag and the next time onOk is called, the 
    * framework will be recreated.
    * <p>
    * By using the flag, this method can be called multiple times w/o a
    * performance penalty.
    *
    * @throws IllegalArgumentException if the component and constraint arrays
    *    don't match. This can only happen if they are both not updated equally
    *    (i.e. an implementation flaw).
    */
   private void initValidationFramework()
   {
      if (m_validationInited)
         return;

      // set up the validation framework
      ArrayList comps = new ArrayList(10);
      ArrayList constraints = new ArrayList(10);
      StringConstraint nonEmpty = new StringConstraint();

      // validate security privider name
      comps.add(m_name);
      constraints.add(nonEmpty);
      comps.add(m_name);
      constraints.add(new StringLengthConstraint(
         PSSecurityProviderInstance.MAX_NAME_LEN));
         
      if (m_existingProviders != null)
      {
         comps.add(m_name);
         constraints.add(new ListMemberConstraint(m_existingProviders));
      }
      
      // validate authenticated user header name
      comps.add(m_userHeader);
      constraints.add(nonEmpty);
      
      // validate role list header name
      comps.add(m_roleHeader);
      constraints.add(nonEmpty);

      // validate role delimiter
      comps.add(m_roleDelimiter);
      constraints.add(nonEmpty);
      
      if (comps.size() != constraints.size())
         throw new IllegalArgumentException("validation array size mismatch");

      Component [] c = new Component[comps.size()];
      comps.toArray( c );
      ValidationConstraint [] v = new ValidationConstraint[constraints.size()];
      constraints.toArray(v);
      setValidationFramework(c, v);

      m_validationInited = true;
   }
   
   /**
    * Get the resources for this dialog.
    * 
    * @return the dialog resources, never <code>null</code>.
    */
   protected ResourceBundle getResources()
   {
      if (ms_res == null)
         ms_res = ResourceBundle.getBundle(
            getClass().getName() + "Resources", Locale.getDefault());

      return ms_res;
   }
   
   // implementation for ISecurityProviderEditor
   public boolean isInstanceModified()
   {
      return m_isModified;
   }

   // implementation for ISecurityProviderEditor
   public boolean setInstance(PSSecurityProviderInstance providerInst)
   {
      if (isVisible())
         return false;

      if (providerInst != null && providerInst.getType() != m_type)
         throw new IllegalArgumentException(
            "provider type not supported by this dialog");

      m_instance = providerInst;
      load(m_instance);

      // if there is an instance set, remove its name from the disallowed list
      if (m_existingProviders != null && m_instance != null)
         m_existingProviders.remove(m_instance.getName());

      // we need to force the validation framework to be rebuilt
      m_validationInited = false;
      
      return true;
   }
   
   // implementation for ISecurityProviderEditor
   public PSSecurityProviderInstance getInstance()
   {
      return m_instance;
   }

   // implementation for ISecurityProviderEditor
   public void setInstanceNames(Collection names)
   {
      if (names == null || names.size() == 0)
      {
         m_existingProviders = null;
         return;
      }

      int size = names.size();
      Iterator iter = names.iterator();
      while (iter.hasNext())
      {
         Object o = iter.next();
         if (!(o instanceof String))
            throw new IllegalArgumentException(
               "Invalid entry in instance name list");
      }
      m_existingProviders = names;

      // if there is an instance set, remove its name from the disallowed list
      if (m_instance != null)
         m_existingProviders.remove(m_instance.getName());

      // we need to force the validation framework to be rebuilt
      m_validationInited = false;
   }

   /**
    * Loads the dialog components for the supplied security provider instance.
    * 
    * @param instance the security provider instance to load from, may be 
    *    <code>null</code> in which case the defaults are loaded.
    */
   private void load(PSSecurityProviderInstance instance)
   {
      if (instance == null)
      {
         loadDefaults();
      }
      else
      {
         m_name.setText(instance.getName());
         Properties props = instance.getProperties();
         if (props != null)
         {
            m_userHeader.setText(props.getProperty(
               PSWebServerProvider.AUTHENTICATED_USER_HEADER, 
               getResources().getString("textfield.user.headername.default")));
            m_roleHeader.setText(props.getProperty(
               PSWebServerProvider.USER_ROLE_LIST_HEADER, 
               getResources().getString("textfield.role.headername.default")));
            m_roleDelimiter.setText(props.getProperty(
               PSWebServerProvider.ROLE_LIST_DELIMITER, 
               getResources().getString("textfield.role.delimiter.default")));
         }
         else
         {
            loadDefaults();
         }
      }
   }
   
   /**
    * Loads all ui components with default values.
    */
   private void loadDefaults()
   {
      m_name.setText("");
      m_userHeader.setText(
         getResources().getString("textfield.user.headername.default"));
      m_roleHeader.setText(
         getResources().getString("textfield.role.headername.default"));
      m_roleDelimiter.setText(
         getResources().getString("textfield.role.delimiter.default"));
   }
   
   /**
    * Set the supplied property in the provided security provider instance.
    * 
    * @param instance the security provider instance in which to set the
    *    property, assumed not <code>null</code>.
    * @param name the property name to set, assumed not <code>null</code>.
    * @param value the property value to set, assumed not <code>null</code>.
    */
   private void setProperty(PSSecurityProviderInstance instance, 
      String name, String value)
   {
      Properties props = instance.getProperties();
      if (props == null)
      {
         props = new Properties();
         try
         {
            instance.setProperties(props);
         }
         catch (PSIllegalArgumentException e)
         {
            // ignore, cannot happen
         }
      }
      
      props.setProperty(name, value);
   }
   
   /**
    * The text field holding the security provider name. Initialized at 
    * construction, never <code>null</code> after that.
    */
   private JTextField m_name = new JTextField();
   
   /**
    * The text field holding the HTTP header name of authenticated users. 
    * Initialized at construction, never <code>null</code> after that. Defaults
    * to <code>Auth_User</code>.
    */
   private JTextField m_userHeader = new JTextField();
   
   /**
    * The text field holding the HTTP header name of the user role list. 
    * Initialized at construction, never <code>null</code> after that. Defaults
    * to <code>RxUserRoles</code>.
    */
   private JTextField m_roleHeader = new JTextField();
   
   /**
    * The text field holding the delimiter used in the user role list. 
    * Initialized at construction, never <code>null</code> after that. Defaults
    * to <code>;</code>.
    */
   private JTextField m_roleDelimiter = new JTextField();
   
   /**
    * The resource bundle for this dialog. Initialized the first time used, 
    * never changed after that.
    */
   private static ResourceBundle ms_res = null;

   /**
    * One of the <code>SP_TYPE_xxx</code> types that specify the type of 
    * the security provider. This is set in the ctor and does not change after
    * that.
    *
    * @see PSSecurityProvider
    */
   private int m_type;

   /**
    * The security provider instance edited with this dialog. Initialized in
    * {#link setInstance(PSSecurityProviderInstance)}, may be <code>null</code>.
    */
   private PSSecurityProviderInstance m_instance = null;
   
   /**
    * A list of existing security providers. Initialized in 
    * {#link setInstanceNames(Collection)}, may be <code>null</code>.
    */
   private Collection m_existingProviders = null;

   /**
    * A flag that indicates whether the validation framework needs to be
    * created before activating validation. We use the flag so we will only
    * build the framework if the user presses Ok.
    */
   private boolean m_validationInited = false;
   
   /**
    * If <code>true</code>, something was changed during this edit session, 
    * <code>false</code> otherwise.
    */
   private boolean m_isModified = false;

   /**
    * The standard button size for all buttons.
    */
   private static final Dimension STANDARD_BUTTON_SIZE = new Dimension(80, 24);
}