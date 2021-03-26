/*******************************************************************************
 *
 * [ JndiSecurityProviderDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer.admin;

import com.percussion.E2Designer.IConnectionSource;
import com.percussion.E2Designer.ListMemberConstraint;
import com.percussion.E2Designer.PSDialog;
import com.percussion.E2Designer.StringConstraint;
import com.percussion.E2Designer.UTFixedHeightTextField;
import com.percussion.E2Designer.UTStandardCommandPanel;
import com.percussion.E2Designer.ValidationConstraint;
import com.percussion.EditableListBox.EditableListBox;
import com.percussion.design.catalog.PSCataloger;
import com.percussion.design.catalog.exit.PSExtensionCatalogHandler;
import com.percussion.design.objectstore.PSDirectorySet;
import com.percussion.design.objectstore.PSProvider;
import com.percussion.design.objectstore.PSReference;
import com.percussion.design.objectstore.PSSecurityProviderInstance;
import com.percussion.design.objectstore.PSServerConfiguration;
import com.percussion.error.PSIllegalArgumentException;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.PSExtensionRef;
import com.percussion.security.IPSPasswordFilter;
import com.percussion.security.PSDirectoryServerCataloger;
import com.percussion.security.PSJndiProvider;
import com.percussion.security.PSSecurityProvider;
import com.percussion.guitools.PSPropertyPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableModel;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;


/**
 * Implements a dialog for editing or creating a JNDI directory or connection
 * based security provider instance. The dialog is resizable and fully
 * internationalized.
 */
@SuppressWarnings(value={"unchecked"})
public class JndiSecurityProviderDialog extends PSDialog
   implements ISecurityProviderEditor
{
   /**
    * The standard ctor for the dialog. Creates the dialog centered on the
    * screen and ready to create a new instance. Or the designer may call
    * {@link #setInstance(PSSecurityProviderInstance) setInstance} if she
    * wants to edit an existing JNDI provider. After optionally setting the
    * instance, call setVisible to activate the dialog.
    *
    * @param parent The parent of this dialog. Should not be <code>null</code>.
    * @param config the server configuration, not <code>null</code>.
    */
   public JndiSecurityProviderDialog(JFrame parent, 
      PSServerConfiguration config)
   {
      super(parent, getString("JndiTitle"));
      
      if (config == null)
         throw new IllegalArgumentException("config cannot be null");
      m_config = config;

      initUI();
      setSize(450, 200);

      // fill in all drop down lists
      initLists();

      reset();
      setResizable( true );
      center();
   }


   /**
    * Handler for the OK button.
    * <p>Performs all necessary actions when the user presses the OK buttion.
    * First, all input data is validated. If it validates correctly, the
    * existing provider instance is updated (if there is one) or a new
    * instance is created. If validation fails, a message is displayed to
    * the user and control returns to the dialog, with the focus set to the
    * invalid control. On success, the dialog is hidden and control returns
    * to the caller.
    * <p>Does not dispose the dialog. The dialog is not disposed so it can be
    * re-used w/o re-allocating all of the peer components. If the designer
    * wants to free up resources, see {@link ISecurityProviderEditor#dispose()
    * dispose}.
    */
   public void onOk()
   {
      initValidationFramework();

      if (!activateValidation())
         return;

      String providerName = m_providerName.getText();
      Properties props = new Properties();
      
      PSProvider directoryProvider = null;
      if (AppletMainDialog.isDirectoryServicesSupported())
      {     
         String selectedDirectoryProvider;
         if ( m_directoryProvider.getSelectedIndex() < 0 )
            selectedDirectoryProvider = "";
         else
            selectedDirectoryProvider = 
            m_directoryProvider.getSelectedItem().toString();
         if (selectedDirectoryProvider.trim().length() > 0)
         {
            PSReference directorySetReference = new PSReference(
               m_directoryProvider.getSelectedItem().toString(), 
               PSDirectorySet.class.getName());
            directoryProvider = new PSProvider(
               PSDirectoryServerCataloger.class.getName(), 
               PSProvider.TYPE_DIRECTORY, directorySetReference);
         }
      
      }
      else
      {
         // required props
         setProperty(props, PSJndiProvider.PROPS_PROVIDER_CLASS_NAME, 
            m_jndiClassname);
         setProperty(props, PSJndiProvider.PROPS_PROVIDER_URL, m_jndiUrl);
         // optional props
         setProperty(props, PSJndiProvider.PROPS_AUTH_SCHEME, m_scheme);
         setProperty(props, PSJndiProvider.PROPS_AUTH_PRINCIPAL, m_principle);
         setProperty(props, PSJndiProvider.PROPS_AUTH_CRED, m_cred);
         setProperty(props, PSJndiProvider.PROPS_ATTR_PRINCIPAL, 
            m_principleAttrib);
         setProperty(props, PSJndiProvider.PROPS_ATTR_CRED, m_credAttrib);

         // set the filter, if one is specified
         Object filterRef = m_filterRef.getSelectedItem();
         if (filterRef instanceof PSExtensionRef)
         {
            props.setProperty( PSJndiProvider.PROPS_CRED_FILTER,
               filterRef.toString());
         }

         /**
          * Now get the list of attribute/alias pairs and add them to the 
          * properties list.
          */ 
         TableModel attribs = m_userAttribs.getListModel();
         int rows = attribs.getRowCount();
         for (int i=0; i<rows; ++i)
         {
            String pair = (String) attribs.getValueAt(i, 0);
            int pos = pair.lastIndexOf(ALIAS_SEP);
            if (pos > 0)
               props.setProperty(pair.substring(pos+1), pair.substring(0, pos));
            else
               props.setProperty(pair, pair);
         }
      }

      // update if editing, otherwise, create a new instance of the provider
      try
      {
         if (null != m_provider)
         {
            if (!providerName.equalsIgnoreCase(m_provider.getName()))
               m_provider.setName(providerName);
         }
         else
         {
            m_provider = new PSSecurityProviderInstance(providerName, 
               PSSecurityProvider.SP_TYPE_DIRCONN);
         }
         m_provider.setProperties(props);
         if (AppletMainDialog.isDirectoryServicesSupported())
         {
            m_provider.setDirectoryProvider(directoryProvider);
         }
      }
      catch (PSIllegalArgumentException e)
      {
         /* This will only happen if a required property is missing, or the
            max length of the provider name changes. In either case, this is
            a design flaw, so we'll throw an illegal arg exception */
         throw new IllegalArgumentException(e.getLocalizedMessage());
      }
      
      /* todo - track modification in each text field */
      m_modified = true;
      setVisible(false);
   }

   /**
    * Handler for the Cancel button.
    * <p>Performs any cleanup necessary when exiting the dialog. Does not dispose
    * the dialog. This is not done so it can be re-used w/o re-allocating all
    * of the peer components. If the designer wants to free up resources, see
    * {@link ISecurityProviderEditor#dispose() dispose}.
    */
   public void onCancel()
   {
      m_modified = false;
      setVisible( false );
   }

   /**
    * This method can be called after the editor returns to determine if the
    * end user cancelled the editing session or changed the supplied instance,
    * if editing.
    *
    * @return <code>true</code> if the caller had set an instance and the end
    * user modified it or a new instance was created (i.e. the user pressed
    * the OK button), <code>false</code> otherwise.
    */
   public boolean isInstanceModified()
   {
      return m_modified;
   }


   /**
    * The editor can function in edit or create mode. This method should be
    * called before the editor is shown the first time to set it into edit mode.
    * When in edit mode, the properties of the supplied instance will be displayed
    * to the user for editing. Supports editing SP_TYPE_[DIR, DIRCONN] providers.
    *
    * @param providerInst The provider to edit. If <code>null</code>, a new
    * instance is created, any existing instance is lost. If you want to keep
    * the existing instance, retrieve it before calling this method. This
    * allows the dialog to be used multiple times w/o creating a new one. If
    * called while the dialog is visible, the call is ignored. The type of this
    * provider must match the type used when the dialog was created or an
    * exception is thrown.
    *
    * @return <code>true</code> if the provided instance is used, <code>false
    * </code> otherwise.
    *
    * @throws IllegalArgumentException if providerInst is not the type supported
    * by the editor.
    */
   public boolean setInstance(PSSecurityProviderInstance providerInst)
   {
      if (isVisible())
         return false;

      // clear all data from controls
      reset();

      if (null == providerInst)
         return true;

      m_provider = providerInst;
         
      // set the data in all of the controls
      m_providerName.setText(providerInst.getName());

      /**
       * Make copy of properties so we can delete the ones we know about and
       * end up with a list of those we don't know about.
       */
      Properties props = new Properties();
      copyProps(props, providerInst.getProperties());
      
      if (AppletMainDialog.isDirectoryServicesSupported())
      {
         PSProvider directoryProvider = m_provider.getDirectoryProvider();
         if (directoryProvider != null)
            m_directoryProvider.setSelectedItem(
               directoryProvider.getReference().getName());
               
      }
      else
      {
         String key = PSJndiProvider.PROPS_PROVIDER_CLASS_NAME;
         m_jndiClassname.getModel().setSelectedItem(props.getProperty(key));
         props.remove(key);

         key = PSJndiProvider.PROPS_PROVIDER_URL;
         m_jndiUrl.setText(props.getProperty(key));
         props.remove(key);

         key = PSJndiProvider.PROPS_AUTH_SCHEME;
         m_scheme.setSelectedItem(props.getProperty(key));
         props.remove(key);

         key = PSJndiProvider.PROPS_AUTH_PRINCIPAL;
         m_principle.setText(props.getProperty(key));
         props.remove(key);

         key = PSJndiProvider.PROPS_ATTR_PRINCIPAL;
         m_principleAttrib.setText(props.getProperty(key));
         props.remove(key);

         key = PSJndiProvider.PROPS_AUTH_CRED;
         m_cred.setText(props.getProperty(key));
         props.remove(key);

         key = PSJndiProvider.PROPS_ATTR_CRED;
         m_credAttrib.setText(props.getProperty(key));
         props.remove(key);

         key = PSJndiProvider.PROPS_CRED_FILTER;
         String filter = props.getProperty(key);
         if (null != filter && filter.trim().length() > 0)
         {
            try
            {
               PSExtensionRef ref = new PSExtensionRef(filter);
               m_filterRef.setSelectedItem(ref);
            }
            catch (IllegalArgumentException e)
            {
               // invalid filter name, tell user and continue processing.
               JOptionPane.showMessageDialog(AppletMainDialog.getMainframe(),
                  MessageFormat.format(
                     getString("InvalidProperty"), new String [] {filter}),
                  getString("InvalidPropertyTitle"),
                  JOptionPane.ERROR_MESSAGE);
            }
         }
         props.remove(key);

         key = PSJndiProvider.PROPS_AUTH_SCHEME;
         String scheme = props.getProperty(key);
         if (null != scheme)
            m_scheme.setSelectedItem(scheme);
         props.remove(key);

         // add attributes, which are all remaining properties
         Enumeration e = props.propertyNames();
         while (e.hasMoreElements())
         {
            key = (String) e.nextElement();
            String value = props.getProperty(key);
            String rowText;
            // make the row appear as it did when they typed it in
            if (key.equals(value))
               rowText = key;
            else
               rowText = value + ALIAS_SEP + key;
               
            m_userAttribs.addRowValue(rowText);
         }
      }

      // If there is an instance set, remove its name from the disallowed list
      if (null != m_existingProviders)
         m_existingProviders.remove(m_provider.getName());

      m_validationInited = false;
      
      return true;
   }
   
   /**
    * Adds a property to props, using the supplied key and the value read
    * from the supplied component. If the component has a non-<code>null</code>,
    * non-empty value, an entry of the form &apos;key=value&apos; is added to
    * props.
    *
    * @param props The properties object to which the mapping will be added,
    *    if one is added. Can't be <code>null</code>.
    * @param key The name of the key to use if adding a property. Can't be
    *    <code>null</code>.
    * @param comp The control to query for a value. Can't be <code>null</code>.
    */
   private void setProperty(Properties props, String key, JComboBox comp)
   {
      if (null == props)
         throw new IllegalArgumentException("properties can't be null");
      else if (null == key)
         throw new IllegalArgumentException("property key can't be null");
      else if (null == comp)
         throw new IllegalArgumentException("combo box can't be null");

      String value = "";
      Object o = comp.getSelectedItem();
      if (null != o)
      {
         if (o instanceof JTextComponent)
            value = ((JTextComponent) o).getText();
         else
            value = o.toString();
      }
      
      if (value.trim().length() > 0)
         props.setProperty(key, value);
   }

   /**
    * Adds a property to props, using the supplied key and the value read
    * from the supplied component. If the component has a non-<code>null</code>,
    * non-empty value, an entry of the form &apos;key=value&apos; is added to
    * props.
    *
    * @param props The properties object to which the mapping will be added,
    *    if one is added. Can't be <code>null</code>.
    * @param key The name of the key to use if adding a property. Can't be
    *    <code>null</code>.
    * @param comp The control to query for a value. Can't be <code>null</code>.
    */
   private void setProperty(Properties props, String key, JTextComponent comp)
   {
      if (null == props)
         throw new IllegalArgumentException("properties can't be null");
      else if (null == key)
         throw new IllegalArgumentException("property key can't be null");
      else if (null == comp)
         throw new IllegalArgumentException("text control can't be null");

      String value = "";
      if (null != comp.getDocument())
      {
         if (comp instanceof JPasswordField)
         {
            char[] password = ((JPasswordField) comp).getPassword();
            value = new String(password);
         }
         else
            value = comp.getText();
      }
      
      if (value.trim().length() > 0)
         props.setProperty(key, value);
   }

   /**
    * After editing has completed, the caller can use this method to obtain the
    * newly created or modified provider. If a provider was previously 
    * successfully set with the <code>setInstance</code> method, that instance 
    * is returned.
    *
    * @return If in edit mode, the edited instance is returned, otherwise a
    *    new instance is returned. If the user cancels, <code>null</code> is 
    *    returned.
    */
   public PSSecurityProviderInstance getInstance()
   {
      return m_provider;
   }


   /**
    * Sets a list of existing security providers. The editor will not allow the
    * end user to create a new security provider with any name that matches a
    * name on the supplied list, case insensitive. If <code>null</code>, the
    * current list will be discarded. All entries in the list must be
    * non-<code>null</code>, String objects or an exception will be thrown.
    *
    * @param names A list of existing security providers, used to prevent
    * duplicate provider names. If <code>null</code> or empty, the previous
    * list is cleared and all names are allowed.
    *
    * @throws IllegalArgumentException If any entry in names is <code>null
    * </code> or is not a String object or the list is empty.
    */
   public void setInstanceNames( Collection names )
   {
      if ( null == names || names.size() == 0 )
      {
         m_existingProviders = null;
         return;
      }

      Iterator iter = names.iterator();
      while ( iter.hasNext())
      {
         Object o = iter.next();
         if ( null == o || !( o instanceof String ))
            throw new IllegalArgumentException(
               "Invalid entry in instance name list" );
      }
      m_existingProviders = names;

      // If there is an instance set, remove its name from the disallowed list
      if ( null != m_provider )
         m_existingProviders.remove( m_provider.getName());

      // we need to signal that the validation info needs to be re-calculated
      m_validationInited = false;
   }


/* ################### Implementation methods ##################### */

   /**
    * Initializes the dialog by creating tabs for each group of properties
    * (Provider Properties, Authentication Properties, Group Providers, User
    * Attributes). Handlers for the OK and Cancel buttons are added.
    */
   private void initUI()
   {
      JPanel contentPane = new JPanel();
      setContentPane( contentPane );
      contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
      contentPane.setLayout( new BorderLayout(10, 5));
      contentPane.add(m_tabbedPane, BorderLayout.CENTER);

      UTStandardCommandPanel cmdPanel = new UTStandardCommandPanel( this, "",
         SwingConstants.HORIZONTAL )
      {
         public void onOk()
         {
            JndiSecurityProviderDialog.this.onOk();
         }

         public void onCancel()
         {
            JndiSecurityProviderDialog.this.onCancel();
         }
      };
      getRootPane().setDefaultButton(cmdPanel.getOkButton());
      JPanel commandPanel = new JPanel(new BorderLayout());
      commandPanel.add(cmdPanel, BorderLayout.EAST);
      contentPane.add(commandPanel, BorderLayout.SOUTH);

      int tabIx=0;
      m_tabbedPane.add(getString( "ProviderPropsTitle" ),
         createProviderPropsPanel());
      setMnemonicForTabIndex(m_tabbedPane, ms_res, "ProviderPropsTitle", tabIx++);
      if (!AppletMainDialog.isDirectoryServicesSupported())
      {    
         m_tabbedPane.add(getString("AuthenticationPropertiesGroupTitle"), 
            createAuthenticationPanel());
         setMnemonicForTabIndex(m_tabbedPane, ms_res, "AuthenticationPropertiesGroupTitle", tabIx++);
      }

      if (!AppletMainDialog.isDirectoryServicesSupported())
      {
         m_tabbedPane.add(getString("UserAttribsTitle"), createAttribsPanel());
         setMnemonicForTabIndex(m_tabbedPane, ms_res, "UserAttribsTitle", tabIx++);
      }
   }
   
   /**
    * Fills in all list boxes with known values that are present whether
    * editing or creating a new instance. This should only be called once when
    * the dialog is created.
    */
   private void initLists()
   {
      if (AppletMainDialog.isDirectoryServicesSupported())
      {
         Iterator sets = m_config.getDirectorySets();
         while (sets.hasNext())
         {
            PSDirectorySet set = (PSDirectorySet) sets.next();
            m_directoryProvider.addItem(set.getName());
         }
      }
      else
      {
         // fill combo boxes w/ known selections
         for (int i=0; i<JNDI_CLASSNAMES.length; ++i)
            m_jndiClassname.addItem(JNDI_CLASSNAMES[i]);

         m_jndiClassname.addItemListener(new ItemListener()
         {
            /**
             * Disables the 'Group Providers' tab if the jndi class name
             * selected/entered does not support group providers.
             *
             * @param e the action event, assumed not to be <code>null</code> as
             * this is a listener method which must be called only when an
             * action occurred on the 'Jndi Class Name' combo-box.
             */
            public void itemStateChanged(ItemEvent e)
            {
               if(e.getStateChange() == ItemEvent.DESELECTED)
                  return;
               
               int index = m_tabbedPane.indexOfTab(
                     getString( "GroupProvidersTitle" ) );
               if(index != -1)
               {
                  if( m_jndiClassname.getSelectedItem().toString().equals(
                     JNDI_CLASSNAMES[DEFAULT_JNDI_CLASSNAME] ) )
                  {
                     m_tabbedPane.setEnabledAt(index, true);
                  }
                  else
                  {
                     m_tabbedPane.setEnabledAt(index, false);
                  }
               }
            }
         });

         for (int i=0; i<SCHEMES.length; ++i)
            m_scheme.addItem(SCHEMES[i]);
         // these are the only allowed choices
         m_scheme.setEditable(false);

         // catalog the password filters, only cataloged entries can be used.
         m_filterRef.setEditable(false);
         try
         {
            // This is a non-editable list, so we need an empty item
            m_filterRef.addItem("");
            IConnectionSource connection = 
               AppletMainDialog.getServerConnection();
            if (null == connection)
               m_filterRef.addItem( getString("CatalogingDisabled"));
            else
            {
               IPSExtensionDef [] filters = 
                  PSExtensionCatalogHandler.getCatalog(new PSCataloger(
                     connection.getDesignerConnection(false)),
                  "%", null, IPSPasswordFilter.class.getName());
               if (filters.length > 0)
               {
                  for (int i=0; i<filters.length; ++i)
                     m_filterRef.addItem(filters[i].getRef());
               }
               else
                  m_filterRef.addItem(getString("NoFiltersFound"));
            }
         }
         catch (Exception e)
         {
            m_filterRef.addItem("");
            m_filterRef.addItem(e.getLocalizedMessage());
         }
      }
   }

   /**
    * Creates the panel that contains the controls for basic JNDI provider
    * info.
    *
    * @return The completed panel, never <code>null</code>
    */
   private JPanel createProviderPropsPanel()
   {
      PSPropertyPanel panel = new PSPropertyPanel();
      panel.setBorder(new EmptyBorder(10, 10, 10, 10));

      panel.addPropertyRow(getString("ProviderNameLabel"),
         new JComponent[] { m_providerName });

      if (AppletMainDialog.isDirectoryServicesSupported())
      {
         panel.addPropertyRow(getString("directoryset"),
            new JComponent[] { m_directoryProvider });
      }
      else
      {
         m_jndiClassname = new JComboBox();
         panel.addPropertyRow(getString("ClassNameLabel"),
            new JComponent[] { m_jndiClassname });

         m_jndiUrl = new JTextField();
         panel.addPropertyRow(getString("URLLabel"),
            new JComponent[] { m_jndiUrl });
      }

      return panel;
   }
   
   /**
    * Builds the controls to enter authentication properties. Creates 2 box
    * layouts, side by side. The left box contains the labels for the controls,
    * right justified. The right box contains the controls.
    *
    * @return A panel containing all the components, never <code>null</code>
    */
   private JPanel createAuthenticationPanel()
   {
      PSPropertyPanel panel = new PSPropertyPanel();
      panel.setBorder(new EmptyBorder(10, 10, 10, 10));
      
      m_scheme = new JComboBox();
      panel.addPropertyRow(getString("SchemeLabel"),
         new JComponent[] { m_scheme });
      
      m_principle = new JTextField();
      panel.addPropertyRow(getString("PrincipleLabel"),
         new JComponent[] { m_principle });
      
      m_principleAttrib = new JTextField();
      panel.addPropertyRow(getString("PrincipleAttribsLabel"),
         new JComponent[] { m_principleAttrib });
     
      m_cred = new JPasswordField();
      panel.addPropertyRow(getString("CredentialLabel"),
         new JComponent[] { m_cred });
      
      m_credAttrib = new JTextField();
      panel.addPropertyRow(getString("CredentialAttributeLabel"),
         new JComponent[] { m_credAttrib });
      
      m_filterRef = new JComboBox();
      panel.addPropertyRow(getString("FilterNameLabel"),
         new JComponent[] { m_filterRef });

      return panel;
   }



   /**
    * Creates a panel that contains an <code>EditableListBox</code> that enables
    * to add new user attributies or delete entries in the list box.
    *
    * @return The completed panel, never <code>null</code>
    */
   private JPanel createAttribsPanel()
   {
      JPanel panel = new JPanel(new BorderLayout(5, 5));
      panel.setBorder(new EmptyBorder(10, 10, 10, 10));

      m_userAttribs = new EditableListBox(getString("UserAttribsListTitle"),
         this, null, null, EditableListBox.TEXTFIELD, 
         EditableListBox.INSERTBUTTON);
      m_userAttribs.getRightButton().addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent evt)
         {
            JndiSecurityProviderDialog.this.m_userAttribs.deleteRows();
         }
      });
      panel.add(m_userAttribs);
      
      return panel;
   }


   /**
    * Creates the validation framework and sets it in the parent dialog. After
    * setting it, the m_validationInited flag is set to indicate it doesn't
    * need to be done again. If something changes requiring a new framework,
    * just clear this flag and the next time onOk is called, the framework
    * will be recreated.
    * <p>By using the flag, this method can be called multiple times w/o a
    * performance penalty.
    *
    * @throws IllegalArgumentException if the component and constraint arrays
    * don't match. This can only happen if they are both not updated equally
    * (i.e. an implementation flaw).
    */
   private void initValidationFramework()
   {
      if (m_validationInited)
         return;

      List comps = new ArrayList();
      List validations = new ArrayList();
      
      StringConstraint nonEmpty = new StringConstraint();

      comps.add(m_providerName);
      validations.add(nonEmpty);
      
      comps.add(m_directoryProvider);
      validations.add(new ListMemberConstraint(null != m_existingProviders
         ? m_existingProviders : new ArrayList()));

      if (!AppletMainDialog.isDirectoryServicesSupported())
      {
         comps.add(m_jndiClassname);
         validations.add(nonEmpty);

         comps.add(m_jndiUrl);
         validations.add(nonEmpty);
      }

      Component[] components = new Component[comps.size()];
      comps.toArray(components);
      
      ValidationConstraint[] constraints = 
         new ValidationConstraint[validations.size()];
      validations.toArray(constraints);
      
      setValidationFramework(components, constraints);

      m_validationInited = true;
   }

   /**
    * Finds a string in this dialog&apos;s resource bundle based on the supplied
    * key and returns it. If it can't be found, an exception is thrown.
    *
    * @param key The key of the string to return. Must not be <code>null</code>
    *    or empty.
    */
   private static String getString( String key )
   {
      if ( null == key || key.trim().length() == 0 )
         throw new IllegalArgumentException( "key can't be null or empty" );

      return ms_res.getString( key );
   }

   /**
    * Copies all of the properties in source to target. Any properties already
    * in target are not modified.
    *
    * @param target The destination for the copy.
    *
    * @param source The properties object to obtain the properties from.
    *
    * @throws IllegalArgumentException if target or source is null
    */
   private void copyProps( Properties target, Properties source )
   {
      if ( null == target || null == source )
         throw new IllegalArgumentException( "target or source is null" );
      Enumeration e = source.propertyNames();
      while ( e.hasMoreElements())
      {
         String key = (String) e.nextElement();
         target.setProperty( key, source.getProperty( key ));
      }
   }

   /**
    * Sets data for all controls to their default values (typically empty
    * for text fields and a pre-defined value for combo boxes). 
    * <p>This method can be called as many times as desired to restore the
    * dialog to the state it was in when it was first created w/ no instances.
    * <p>Note: This call will not clear the instance name list.
    */
   private void reset()
   {
      // release any provider that is currently set
      m_provider = null;

      // clear/default all controls
      m_providerName.setText(null);
      
      if (!AppletMainDialog.isDirectoryServicesSupported())
      {
         m_jndiClassname.setSelectedIndex(DEFAULT_JNDI_CLASSNAME);
         m_jndiUrl.setText(null);

         m_scheme.setSelectedIndex(DEFAULT_SCHEME);
         m_principle.setText(null);
         m_principleAttrib.setText(null);
         m_cred.setText(null);
         m_credAttrib.setText(null);
         m_filterRef.setSelectedItem("");

         for (int i = m_userAttribs.getItemCount()-1; i >= 0; --i)
            m_userAttribs.removeItemAt(i);
      }

      m_modified = false;
   }

   /** Source for all strings used by this dialog. Never <code>null</code. */
   private static ResourceBundle ms_res = PSServerAdminApplet.getResources();

   /**
    * The character (as a string) used to seperate the property name from its
    * alias name.
    */
   private static final String ALIAS_SEP = "=";

   /**
    * All names in this array will be added to the classname combo box dropdown
    * list, in the order provided.
    */
   private static final String [] JNDI_CLASSNAMES =
   {
      "com.sun.jndi.ldap.LdapCtxFactory",
      "com.sun.jndi.nis.NISCtxFactory"
   };

   /**
    * The index into JNDI_CLASSNAMES that should appear when a new provider is
    * being created. Must be between 0 and JNDI_CLASSNAMES.length-1,
    * inclusive.
    */
   private static final int DEFAULT_JNDI_CLASSNAME = 0;

   /**
    * All names in this array will be added to the scheme combo box dropdown
    * list, in the order provided.
    *
    * @todo Dynamically order.
    */
   private static final String [] SCHEMES =
   {
      getString("SchemeTypeSimple"),
      getString("SchemeTypeNone"),
      getString("SchemeTypeCram_md")
   };

   /**
    * The index into SCHEMES that should appear when a new provider is
    * being created. Must be between 0 and SCHEMES.length-1, inclusive.
    */
   private static final int DEFAULT_SCHEME = 0;

   /**
    * Contains either the provider that is being edited (set via {@link
    * #setInstance(PSSecurityProviderInstance) setInstance}) or a newly created
    * provider (after user presses OK) or <code>null</code> (until either one 
    * of the above actions occurs). The designer has read access via 
    * {@link #getInstance()}.
    */
   private PSSecurityProviderInstance m_provider = null;

   /**
    * The tabbed panel to hold tabs for each group of properties. Initialized to
    * a new object and never modified after that.
    */
   private JTabbedPane m_tabbedPane = new JTabbedPane();

   /** The editing component for the provider name. */
   private JTextComponent m_providerName = new UTFixedHeightTextField();
   
   /**
    * A list with all available directory sets to choose from. The user must
    * select a valid directory set.
    */
   private JComboBox m_directoryProvider = new JComboBox();


   /**
    * The flag that indicates whether the user has initiated any action that
    * changes the dialog from it's initial state.
    *
    * @see #isInstanceModified
    */
   private boolean m_modified = false;
   
   /**
    * The server configuration, initialized while constructed, never 
    * <code>null</code> after that.
    */
   private PSServerConfiguration m_config = null;

   /**
    * If not null, when the user presses OK, the provider name will be validated
    * against all entries in this list. If the new name matches any name
    * in this list, validation will fail. If not null, at least 1, non-empty
    * String is present.
    */
   private Collection m_existingProviders = null;

   /**
    * A flag to indicate whether to create the validation framework when the
    * onOk method is called. If <code>false</code>, the validation framework
    * is created, otherwise the existing one is used.
    */
   private boolean m_validationInited = false;

   /** The editing component for the JNDI driver fully qualified classname. */
   private JComboBox m_jndiClassname = null;

   /** The editing component for the URL to access the JNDI server. */
   private JTextComponent m_jndiUrl = null;

   /** 
    * The editing component for the sheme to use. Only applicable to certain 
    * models.
    */
   private JComboBox m_scheme = null;

   /** The editing component for the 'Principle' name. */
   private JTextComponent m_principle = null;

   /** The editing component for the 'Principle Attribute' name. */
   private JTextComponent m_principleAttrib = null;

   /** The editing component for the 'Credential' name. */
   private JTextComponent m_cred = null;

   /** The editing component for the 'Credential Attribute' name. */
   private JTextComponent m_credAttrib = null;

   /**
    * The control that contains a reference to the extension that is to be
    * used as the password filter. By default, if no filters were found during
    * cataloging, it contains text that says as much, otherwise, it contains
    * the empty string.
    */
   private JComboBox m_filterRef = null;

   /** The editing component for user defined attributes and their alias. */
   private EditableListBox m_userAttribs = null;

}

