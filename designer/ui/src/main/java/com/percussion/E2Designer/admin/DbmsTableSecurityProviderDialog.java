/******************************************************************************
 *
 * [ DbmsTableSecurityProviderDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer.admin;

import com.percussion.E2Designer.IConnectionSource;
import com.percussion.E2Designer.ListMemberConstraint;
import com.percussion.E2Designer.PSDialog;
import com.percussion.E2Designer.StringConstraint;
import com.percussion.E2Designer.StringLengthConstraint;
import com.percussion.E2Designer.UTStandardCommandPanel;
import com.percussion.E2Designer.ValidationConstraint;
import com.percussion.EditableListBox.EditableListBox;
import com.percussion.conn.PSDesignerConnection;
import com.percussion.design.catalog.PSCataloger;
import com.percussion.design.catalog.exit.PSExtensionCatalogHandler;
import com.percussion.design.objectstore.PSObjectStore;
import com.percussion.design.objectstore.PSSecurityProviderInstance;
import com.percussion.design.objectstore.PSServerConfiguration;
import com.percussion.error.PSExceptionUtils;
import com.percussion.error.PSIllegalArgumentException;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.PSExtensionRef;
import com.percussion.security.PSBackEndConnection;
import com.percussion.security.PSSecurityProvider;
import com.percussion.util.PSCollection;
import com.percussion.guitools.PSPropertyPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Vector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Creates a dialog for editing or creating a back-end table
 * security provider instance. The dialog is resizable and fully
 * internationalized.
 */
@SuppressWarnings(value={"unchecked"})
public class DbmsTableSecurityProviderDialog extends PSDialog
   implements ISecurityProviderEditor
{
   private static final long serialVersionUID = 1L;
   private static final Logger log = LogManager.getLogger(PSServerAdminApplet.SERVER_ADMIN_LOG_CATEGORY);

   /**
    * The standard ctor for the dialog. Creates the dialog centered on the
    * screen and ready to create a new instance. Or the designer may call
    * {@link #setInstance(PSSecurityProviderInstance) setInstance} if she
    * wants to edit an existing provider. After optionally setting the
    * instance, call setVisible to activate the dialog.
    *
    * @param parent The parent of this dialog. Should not be <code>null</code>.
    * @param connSrc An object to obtain a working connection to an Rx server
    *    for cataloging. If <code>null</code>, cataloging is disabled.
    * @param config the server configuration, not <code>null</code>.
    */
   public DbmsTableSecurityProviderDialog(JFrame parent, 
      IConnectionSource connSrc, PSServerConfiguration config)
   {
      super(parent, getString("DbmsTableSPEditorTitle"));
      
      if (config == null)
         throw new IllegalArgumentException("config cannot be null");
         
      m_config = config;
      m_connectionSource = connSrc;
      m_conn = new ConnectionPropsPanel(null, connSrc, ms_res); 

      initUI();
      initCataloging();
      reset();
   }


   /**
    * Handler for the 'Add all' button that adds all user attributes defined
    * in a table. Every entry in the combo box that is not already in the
    * list will be added (unless it is defining a uid/pw column name).
    */
   public void onAddAll()
   {
      // get all possible attributes
      Vector v = m_conn.catalogColumns( false );
      // check if the cataloging was successful
      if (((String) v.get(0)).length() == 0 )
         return;

      if ( v != m_attribsCatalog )
         m_attribsCatalog = v;

      // get all of the current attributes
      TableModel attribs = m_userAttribs.getListModel();
      int rows = attribs.getRowCount();
      HashMap attribMap = new HashMap();
      for ( int i = 0; i < rows; ++i )
      {
         String pair = (String) attribs.getValueAt( i, 0 );
         if ( null != pair && pair.trim().length() > 0 )
         {
            int pos = pair.lastIndexOf(':');
            attribMap.put( pos > 0 ? pair.substring(0, pos) : pair, null );
         }
      }

      // walk thru all columns and add the missing ones
      String uidCol = (String) m_userIdCol.getSelectedItem();
      String pwCol = (String) m_passwordCol.getSelectedItem();
      for (Object o : m_attribsCatalog) {
         String attrib = (String) o;
         if (!attribMap.containsKey(attrib)
                 && !attrib.equals(uidCol)
                 && !attrib.equals(pwCol)) {
            m_userAttribs.addRowValue(attrib);
         }
      }
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
    * <p>Does not dispose the dialog. This is not done so it can be re-used
    * w/o re-allocating all of the peer components. If the designer wants to
    * free up resources, see {@link ISecurityProviderEditor#dispose() dispose}.
    */
   @Override
   public void onOk()
   {
      initValidationFramework();
      if ( !activateValidation())
         return;

      // additional validation
      String providerName = m_providerName.getText();

      // build a properties object to set on the provider instance
      Properties props = new Properties();

      // add the std props
      props.setProperty( PSBackEndConnection.PROPS_DATASOURCE_NAME,
         m_conn.getDatasourceChoice());
      props.setProperty( PSBackEndConnection.PROPS_TABLE_NAME,
         m_conn.getTableChoice());
      
      // set uid/pw cols in table, both are required
      String uidcol = (String) m_userIdCol.getSelectedItem();
      props.setProperty( PSBackEndConnection.PROPS_UID_COLUMN,
         uidcol);
      props.setProperty( PSBackEndConnection.PROPS_PW_COLUMN,
         (String) m_passwordCol.getSelectedItem());

      // set the filter, if one is specified
      Object filterRef = m_filterRef.getSelectedItem();
      if ( filterRef instanceof PSExtensionRef )
      {
         props.setProperty( PSBackEndConnection.PROPS_PW_FILTER,
            filterRef.toString());
      }

      TableModel attribs = m_userAttribs.getListModel();
      int rows = attribs.getRowCount();
      for (int i=0; i<rows; ++i)
      {
         String pair = (String) attribs.getValueAt(i, 0);
         if (null != pair && pair.trim().length () > 0)
         {
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
            if (!providerName.equals(m_provider.getName()))
               m_provider.setName(providerName);
         }
         else
         {
            m_provider = new PSSecurityProviderInstance(providerName,
               PSSecurityProvider.SP_TYPE_BETABLE);
         }

         m_provider.setProperties(props);
      }
      catch (PSIllegalArgumentException e)
      {
         /* This will only happen if a required property is missing, or the
            max length of the provider name changes. In either case, this is
            a design flaw, so we'll throw an illegal arg exception */
         throw new IllegalArgumentException( e.getLocalizedMessage());
      }

      m_isModified = true;
      setVisible(false);
   }

   /**
    * Handler for the Cancel button.
    * <p>Performs any cleanup necessary when exiting the dialog. Does not dispose
    * the dialog. This is not done so it can be re-used w/o re-allocating all
    * of the peer components. If the designer wants to free up resources, see
    * {@link ISecurityProviderEditor#dispose() dispose}.
    */
   @Override
   public void onCancel()
   {
      m_isModified = false;
      setVisible( false );
   }


/* ################ ISecurityProviderEditor interface ################## */

   /**
    * This method can be called after the editor returns to determine if the
    * end user cancelled the editing session or changed the supplied instance,
    * if editing.
    *
    * @return <code>true</code> if the caller had set an instance and the end
    * user modified it or a new instance was created, <code>false</code>
    * otherwise.
    */
   public boolean isInstanceModified()
   {
      return m_isModified;
   }

   /**
    * The editor can function in edit or create mode. This method should be
    * called before the editor is shown the first time to set it into edit mode.
    * When in edit mode, the properties of the supplied instance will be
    * displayed to the user for editing.  Supports editing SP_TYPE_BETABLE
    * providers.
    *
    * @param providerInst The provider to edit. If <code>null</code>, a new
    * instance is created, any existing instance is lost. If you want to keep
    * the existing instance, retrieve it before calling this method. This
    * allows the dialog to be used multiple times w/o creating a new one. If
    * called while the dialog is visible, the call is ignored. If a valid
    * instance is passed in, it is directly modified only if the user
    * successfully processes the dialog.
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

      if (null == providerInst)
      {
         reset();
         return true;
      }
      else if (providerInst.getType() != PSSecurityProvider.SP_TYPE_BETABLE)
         throw new IllegalArgumentException("Incorrect provider type");

      m_provider = providerInst;
      m_providerName.setText(providerInst.getName());

      /* make copy of properties so we can delete the ones we know about and
         end up with a list of those we don't know about */
      Properties props = new Properties();
      copyProps(props, providerInst.getProperties());
      
      String key = PSBackEndConnection.PROPS_DATASOURCE_NAME;
      String datasource = props.getProperty( key );
      props.remove(key);

      key = PSBackEndConnection.PROPS_TABLE_NAME;
      String table = props.getProperty( key );
      props.remove(key);

      m_conn.setDatasourceChoice(datasource);
      m_conn.setTableChoice(table);

      // choose the selected one
      key = PSBackEndConnection.PROPS_UID_COLUMN;
      m_userIdCol.setSelectedItem( props.getProperty( key ));
      props.remove(key);

      key = PSBackEndConnection.PROPS_PW_COLUMN;
      m_passwordCol.setSelectedItem( props.getProperty( key ));
      props.remove(key);

      key = PSBackEndConnection.PROPS_PW_FILTER;
      String filter = props.getProperty( key );
      if ( null != filter && filter.trim().length() > 0 )
      {
         try
         {
            PSExtensionRef ref = new PSExtensionRef( filter );
            m_filterRef.setSelectedItem( ref );
         }
         catch ( IllegalArgumentException e )
         {
            // invalid filter name, tell user and continue processing.
            JOptionPane.showMessageDialog( AppletMainDialog.getMainframe(),
               MessageFormat.format(
                  getString( "InvalidProperty" ), new String [] {filter} ),
               getString( "InvalidPropertyTitle" ),
               JOptionPane.ERROR_MESSAGE );
         }
      }
      props.remove(key);

      // add attributes
      Enumeration e = props.propertyNames();
      while (e.hasMoreElements())
      {
         key = (String) e.nextElement();
         String value = props.getProperty(key);
         m_userAttribs.addRowValue(value + ALIAS_SEP + key);
      }
      
      // If there is an instance set, remove its name from the disallowed list
      if (null != m_existingProviders)
         m_existingProviders.remove(m_provider.getName());

      m_validationInited = false;
      return true;
   }

   /**
    * After editing has completed, the caller can use this method to obtain the
    * newly created or modified provider. If a provider was previously successfully
    * set with the <code>setInstance</code> method, that instance is returned.
    *
    * @return If in edit mode, the edited instance is returned, otherwise a
    * new instance is returned. If the user cancels, <code>null</code> is returned.
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
    * duplicate provider names. If <code>null</code>, the current list is cleared.
    *
    * @throws IllegalArgumentException If any entry in names is <code>null
    * </code> or is not a String object.
    */
   public void setInstanceNames( Collection names )
   {
      if ( null == names || names.isEmpty() )
      {
         m_existingProviders = null;
         return;
      }

      for (Object o : names) {
         if (!(o instanceof String))
            throw new IllegalArgumentException(
                    "Invalid entry in instance name list");
      }
      m_existingProviders = names;

      // If there is an instance set, remove its name from the disallowed list
      if ( null != m_provider )
         m_existingProviders.remove( m_provider.getName());

      // we need to force the validation framework to be rebuilt
      m_validationInited = false;
   }

   /**
    * Creates all the pieces and puts them together. There are 4 pieces used in
    * the dlg;
    * <ul>
    *   <li>Provider name</li>
    *   <li>Connection properties</li>
    *   <li>The command panel (ok/cancel/help)</li>
    *   <li>The authentication properties</li>
    *   <li>The user attributes</li>
    * </ul>
    * These 4 pieces are put together using box layouts so they will resize
    * properly as the dlg is resized.
    */
   private void initUI()
   {
      JPanel contentPane = new JPanel();
      setContentPane(contentPane);
      contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
      contentPane.setLayout(new BorderLayout(10, 5));
      contentPane.add(m_tabbedPane, BorderLayout.CENTER);

      UTStandardCommandPanel cmdPanel = new UTStandardCommandPanel(this, "",
         SwingConstants.HORIZONTAL)
      {
         public void onOk()
         {
            DbmsTableSecurityProviderDialog.this.onOk();
         }

         @Override
         public void onCancel()
         {
            DbmsTableSecurityProviderDialog.this.onCancel();
         }
      };
      getRootPane().setDefaultButton(cmdPanel.getOkButton());
      JPanel commandPanel = new JPanel(new BorderLayout());
      commandPanel.add(cmdPanel, BorderLayout.EAST);
      contentPane.add(commandPanel, BorderLayout.SOUTH);
      
      m_backendPanel = createBackendPanel();
      m_attributesPanel = createAttributesPanel();
      
      m_tabbedPane.add(getString("dbms.tab.properties"),
         createPropertiesPanel());
      setMnemonicForTabIndex(m_tabbedPane, ms_res, "dbms.tab.properties", 0);
      m_tabbedPane.add(getString("dbms.tab.connection"), m_conn);
      setMnemonicForTabIndex(m_tabbedPane, ms_res, "dbms.tab.connection", 1);
      m_tabbedPane.add(getString("dbms.tab.authentication"),
         createAuthenticationPanel());
      setMnemonicForTabIndex(m_tabbedPane, ms_res, "dbms.tab.authentication", 2);
      m_tabbedPane.add(getString("dbms.tab.attributes"), m_attributesPanel);
      setMnemonicForTabIndex(m_tabbedPane, ms_res, "dbms.tab.attributes", 3);
      setResizable(true);
      pack();
      center();
   }
   
   /**
    * @return the new created properties panel, never <code>null</code>.
    */
   private JPanel createPropertiesPanel()
   {
      PSPropertyPanel panel = new PSPropertyPanel();
      panel.setBorder(new EmptyBorder(10, 10, 10, 10));

      panel.addPropertyRow(getString("ProviderNameLabel"),
         new JComponent[] { m_providerName }, m_providerName,
         getString("ProviderNameLabel.mn").charAt(0), "");

      return panel;
   }
   
   /**
    * @return the new created authentication properties panel, never 
    *    <code>null</code>.
    */
   private JPanel createAuthenticationPanel()
   {
      PSPropertyPanel panel = new PSPropertyPanel();
      panel.setBorder(new EmptyBorder(10, 10, 10, 10));

      m_userIdCol.setEditable(true);
      panel.addPropertyRow(getString("UserIdColumnLabel"),
         new JComponent[] { m_userIdCol }, m_userIdCol,
         getString("UserIdColumnLabel.mn").charAt(0), "");
         
      m_passwordCol.setEditable(true);
      panel.addPropertyRow(getString("PasswordColumnLabel"),
         new JComponent[] { m_passwordCol }, m_passwordCol,
         getString("PasswordColumnLabel.mn").charAt(0), "");
         
      panel.addPropertyRow(getString("FilterNameLabel"),
         new JComponent[] { m_filterRef }, m_filterRef,
         getString("FilterNameLabel.mn").charAt(0), "");

      return panel;
   }
   

   /**
    * Create the attributes panel with either the attributes panel or the 
    * directory panel enabled.
    * 
    * @return the newly created attributes panel, never <code>null</code>.
    */
   private JPanel createAttributesPanel()
   {
      JPanel panel = new JPanel(new BorderLayout());

      panel.add(m_backendPanel, "Center");

      return panel;
   }
   
   /**
    * Create the backend attributes panel with an editable list box and an 
    * add all button.
    * 
    * @return the newly created backend attributes panel, never
    *    <code>null</code>.
    */
   private JPanel createBackendPanel()
   {
      JPanel panel = new JPanel(new BorderLayout());
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
      panel.setBorder(new EmptyBorder(10, 10, 10, 10));

      m_userAttribs = new EditableListBox(getString("UserAttribsTitle"), this, 
         null, null, EditableListBox.COMBOBOX, EditableListBox.INSERTBUTTON);
      m_userAttribs.setSelectionMode(
         ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
      m_userAttribs.setPreferredSize(new Dimension(500, 200));
      m_userAttribs.getRightButton().addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent evt)
         {
            DbmsTableSecurityProviderDialog.this.m_userAttribs.deleteRows();
         }
      });

      // add listener to catalog the drop down list
      m_userAttribs.getLeftButton().addFocusListener(new FocusAdapter()
      {
         @Override
         public void focusGained(FocusEvent e)
         {
            DbmsTableSecurityProviderDialog.this.fillAttribsDropList();
         }
      });

      JButton addAll = new JButton("Add All");
      addAll.setMnemonic('l');
      addAll.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent evt)
         {
            DbmsTableSecurityProviderDialog.this.onAddAll();
         }
      });
      
      JPanel buttonPanel = new JPanel(new BorderLayout());
      buttonPanel.add(addAll, BorderLayout.CENTER);

      panel.add(m_userAttribs);
      panel.add(Box.createVerticalStrut(10));
      panel.add(Box.createVerticalGlue());
      panel.add(buttonPanel);
      
      return panel;
   }

   /**
    * Sets data for all controls to their default values (typically empty
    * for text fields and a pre-defined value for combo boxes). To change
    * default values for combo boxes, see the DEFAULT_... indexes.
    * <p>This method can be called as many times as desired to restore the
    * dialog to the state it was in when it was first created w/ no instances.
    * <p>Note: This call will not clear the instance name list.
    */
   private void reset()
   {
      m_provider = null;
      m_isModified = false;
      m_providerName.setText(null);

      m_conn.reset();

      m_userIdCol.setSelectedItem("");
      m_passwordCol.setSelectedItem("");
      m_filterRef.setSelectedItem("");

      for (int i = m_userAttribs.getItemCount()-1; i >= 0; --i)
         m_userAttribs.removeItemAt(i);
   }

   /**
    * Adds listeners to all controls that need them so that cataloging will be
    * performed when the user tries to access the drop-down list.
    */
   private void initCataloging()
   {
      m_userIdCol.getEditor().getEditorComponent().addFocusListener(
         new FocusAdapter()
      {
         @Override
         public void focusGained( FocusEvent e )
         {
            m_userIdCatalog =
               DbmsTableSecurityProviderDialog.this.updateColumnList(
               m_userIdCol, m_userIdCatalog );
         }
      });

      m_passwordCol.getEditor().getEditorComponent().addFocusListener(
         new FocusAdapter()
      {
         @Override
         public void focusGained( FocusEvent e )
         {
            m_pwCatalog = DbmsTableSecurityProviderDialog.this.updateColumnList(
               m_passwordCol, m_pwCatalog );
         }
      });

      final JComboBox editor = (JComboBox) m_userAttribs.getCellEditorComponent();
      editor.getEditor().getEditorComponent().addFocusListener(
         new FA(editor));

      // catalog the password filters, only cataloged entries can be used.
      try
      {
         // This is a non-editable list, so we need an empty item
         m_filterRef.addItem( "" );
         if ( null == m_connectionSource )
            m_filterRef.addItem( getString( "CatalogingDisabled" ));
         else
         {
            IPSExtensionDef [] filters = PSExtensionCatalogHandler.getCatalog(
               new PSCataloger( m_connectionSource.getDesignerConnection(false)),
               "%", null, "com.percussion.security.IPSPasswordFilter" );
            if ( filters.length > 0 )
            {
               for (IPSExtensionDef filter : filters) m_filterRef.addItem(filter.getRef());
            }
            else
               m_filterRef.addItem( getString( "NoFiltersFound" ));
         }
      }
      catch (Exception e)
      {
         m_filterRef.addItem("");
         m_filterRef.addItem(e.getLocalizedMessage());
         m_filterRef.setSelectedItem("");
      }
   }

   /**
    * This is due to a bug in the compiler. I would like this to be an
    * anonymous class, but when I make it so, the compiler complains that
    * the class has already been defined.
    */
   private class FA extends FocusAdapter
   {
      public FA(JComboBox b) { m_a = b; }
      public void actionPerformed(ActionEvent evt)
      {
         DbmsTableSecurityProviderDialog.this.updateColumnList(m_a,
            m_attribsCatalog);
      }
      private JComboBox m_a;
   }


   /**
    * Checks if the cataloged columns in the uid/pw column editors have
    * changed. If they have, the drop list for these controls are updated
    * with the new list. If no changes are detected, the method returns.
    */
   private Vector updateColumnList(JComboBox control, Vector cache)
   {
      Vector v = m_conn.catalogColumns( false );
      if ( v != cache )
      {
         // we need this check for a bug in 1.2
         if ( control.getItemCount() > 0 )
            control.removeAllItems();
         for (Object o : v) {
            String col = (String) o;
            control.addItem(col);
         }
      }
      return v;
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

      // set up the validation framework
      ArrayList comps = new ArrayList();
      ArrayList constraints = new ArrayList();
      

      comps.add(m_providerName);
      constraints.add(new StringConstraint(null,
            "Provider Name on Provider Properties tab: "));
      comps.add(m_providerName);
      constraints.add(
         new StringLengthConstraint(PSSecurityProviderInstance.MAX_NAME_LEN));
      if (null != m_existingProviders)
      {
         comps.add(m_providerName);
         constraints.add(new ListMemberConstraint(m_existingProviders,
               "Provider name already exists: {0}"));
      }

      comps.add(m_userIdCol);
      constraints.add(new StringConstraint(null,
            "User Id on Authentication tab: "));
      comps.add(m_passwordCol);
      constraints.add(new StringConstraint(null,
            "Password on Authentication tab: "));

      // do an 'assert'
      if (comps.size() != constraints.size())
         throw new IllegalArgumentException( "validation array size mismatch" );

      Component[] c = new Component[comps.size()];
      comps.toArray(c);
      ValidationConstraint[] v = new ValidationConstraint[constraints.size()];
      constraints.toArray(v);
      setValidationFramework(c, v);

      m_validationInited = true;
   }

   /**
    * Copies all of the properties in source to target. Any properties already
    * in target are not modified.
    *
    * @param target The destination for the copy.
    *
    * @param source The properties object to obtain the properties from.
    *
    * @throws IllegalArgumentException if target or source is <code>null</code>
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
    * Finds a string in this dialog&apos;s resource bundle based on the supplied
    * key and returns it. If it can't be found, an exception is thrown.
    *
    * @param key The key of the string to return. Must not be null or empty.
    *
    * @throws IllegalArgumentException if key is <code>null</code> or empty.
    *
    * @throws MissingResourceException if key can't be found in the resource
    * bundle
    */
   private static String getString( String key )
   {
      if ( null == key || key.trim().length() == 0 )
         throw new IllegalArgumentException( "key can't be null or empty" );

      return ms_res.getString( key );
   }

   /**
    * Fills the drop list for the combo box in the cell editor of the user
    * attributes list. All columns that can be cataloged, minus any columns
    * already in the list, minus any column specified as the userid/pw column
    * will be added to the list.
    */
   private void fillAttribsDropList()
   {
      // make a list of not alloweds (if its already on list or is a uid/pw col
      HashMap notAllowed = new HashMap();
      String col = (String) m_userIdCol.getSelectedItem();
      if ( null != col && col.trim().length() > 0 )
         notAllowed.put( col, null );

      col = (String) m_passwordCol.getSelectedItem();
      if ( null != col && col.trim().length() > 0 )
         notAllowed.put( col, null );

      TableModel attribs = m_userAttribs.getListModel();
      int rows = attribs.getRowCount();
      for ( int i = 0; i < rows; ++i )
      {
         String pair = (String) attribs.getValueAt( i, 0 );
         if ( null != pair && pair.trim().length() > 0 )
         {
            int pos = pair.lastIndexOf(ALIAS_SEP);
            if ( pos > 0 )
               notAllowed.put( pair.substring(0, pos), null );
            else
               notAllowed.put( pair, null );
         }
      }

      JComboBox dropList = (JComboBox) m_userAttribs.getCellEditorComponent();
      dropList.removeAllItems();
      Vector v = m_conn.catalogColumns( false );
      Iterator iter = v.iterator();
      while ( iter.hasNext())
      {
         col = iter.next().toString();
         if ( !notAllowed.containsKey( col ))
            dropList.addItem( col );
      }
   }

   /**
    * Used for testing the dialog. If an instance called 'foo' is found, it
    * will be edited, otherwise it comes up in create mode.
    */
   public static void main(String[] args)
   {
      try
      {
         String strLnFClass = UIManager.getCrossPlatformLookAndFeelClassName();
         LookAndFeel lnf = (LookAndFeel) Class.forName(strLnFClass).newInstance();
         UIManager.setLookAndFeel( lnf );
         IConnectionSource cs = new IConnectionSource()
         {
            public PSDesignerConnection getDesignerConnection( boolean f )
            {
               try
               {
                  if ( null == ms_conn )
                  {
                     Properties propsLogin = new Properties();
                     propsLogin.setProperty( PSDesignerConnection.PROPERTY_HOST, "xena" );
                     propsLogin.setProperty( PSDesignerConnection.PROPERTY_LOGIN_ID, "foo" );
                     propsLogin.setProperty( PSDesignerConnection.PROPERTY_LOGIN_PW, "foo" );
                     propsLogin.setProperty( PSDesignerConnection.PROPERTY_PORT, "9992" );

                     ms_conn = new PSDesignerConnection(propsLogin);
                  }
               }
               catch ( Exception e )
               {
                  log.error(PSExceptionUtils.getMessageForLog(e));
               }

               return ms_conn;
            }
            private PSDesignerConnection ms_conn;
         };

         // create an instance to edit
         PSObjectStore os = new PSObjectStore( cs.getDesignerConnection(false));
         PSServerConfiguration config = os.getServerConfiguration(true);
         DbmsTableSecurityProviderDialog dlg =
            new DbmsTableSecurityProviderDialog( null, cs, config);
         PSCollection providers = config.getSecurityProviderInstances();
         int size = providers.size();
         boolean editing = false;
         ArrayList existingProviders = new ArrayList( 5 );
         if ( size > 0 )
         {
            for ( int i = 0; i < size; ++i )
            {
               PSSecurityProviderInstance inst = (PSSecurityProviderInstance) providers.get(i);
               log.info( "Found instance: {}" , inst.getName());
               existingProviders.add( inst.getName());
               if ( inst.getName().equals( "foo" ) && inst.getType() == PSSecurityProvider.SP_TYPE_BETABLE )
               {
                  dlg.setInstance(inst);
                  editing = true;
               }
            }
         }
         dlg.setInstanceNames( existingProviders );
         dlg.setVisible(true);
         if ( dlg.isInstanceModified())
         {
            if ( !editing )
               providers.add( dlg.getInstance());
            log.info( "Provider ct = {}" , providers.size());
            config.setSecurityProviderInstances( providers );
            os.saveServerConfiguration(config, true );
            log.info( "Updated server" );
         }
         os.releaseServerConfigurationLock( config );
      }
      catch ( Exception e )
      {
         log.error(PSExceptionUtils.getMessageForLog(e));
      }
   }

   /** Source for all strings used by this dialog. Never <code>null</code. */
   private static ResourceBundle ms_res = PSServerAdminApplet.getResources();

   /**
    * The character (as a string) used to seperate the property name from its
    * alias name.
    */
   private static final String ALIAS_SEP = "=";
   
   /**
    * The server configuration, initialized during construction, never 
    * <code>null</code> or changed after that.
    */
   private PSServerConfiguration m_config = null;

   /**
    * The tabbed panel to hold the various TAB's for this editor. Initialized
    * through {@link #initUI()}, never changed after that.
    */
   private JTabbedPane m_tabbedPane = new JTabbedPane();

   /**
    * The combobox that allows the user to enter the table column name to be
    * used for the UserID when authenticating.
    */
   private JComboBox m_userIdCol = new JComboBox();

   /**
    * The combobox that allows the user to enter the table column name to be
    * used for the password when authenticating.
    */
   private JComboBox m_passwordCol = new JComboBox();

   /**
    * The columns used in the drop-down lists for the m_userIdCol combo box.
    * We keep the reference around so we can check
    * new catalogs and only update the list if the catalog contents has
    * changed. Do not modify this, we don't own it.
    *
    * @todo: In the future, we can be smarter and only show cols are are
    * unique keys
    */
   private Vector m_userIdCatalog = null;
   /**
    * The columns used in the drop-down lists for the m_passwordCol combo box.
    * We keep the reference around so we can check
    * new catalogs and only update the list if the catalog contents has
    * changed. Do not modify this, we don't own it.
    */
   private Vector m_pwCatalog = null;
   /**
    * The columns used in the drop-down lists for the m_userAttribs editing
    * combo box. We keep the reference around so we can check
    * new catalogs and only update the list if the catalog contents has
    * changed. Do not modify this, we don't own it.
    */
   private Vector m_attribsCatalog = null;

   /**
    * The control that contains a reference to the extension that is to be
    * used as the password filter. By default, if no filters were found during
    * cataloging, it contains text that says as much, otherwise, it contains
    * the empty string.
    */
   private JComboBox m_filterRef = new JComboBox();

   /**
    * The user specified name of this provider. It can't match any of the
    * names in m_existingProviders and its length is limited.
    */
   private JTextField m_providerName = new JTextField();

   /**
    * The control to add user defined attributes. Entries are added in the
    * format: <column name>:<alias>, where alias is optional.
    */
   private EditableListBox m_userAttribs;

   /**
    * If editing, this is the initial instance that is being edited, otherwise
    * it is <code>null</code>. After Ok is pressed, a new instance is created
    * and placed here. It can be obtained via {@link #getInstance() getInstance}.
    */
   private PSSecurityProviderInstance m_provider = null;

   /**
    * The panel that contains all of the connection property controls.
    */
   private ConnectionPropsPanel m_conn = null;

   /**
    * <code>null</code> unless set by the designer via {@link
    * #setInstanceNames(Collection) setInstanceNames}. If not <code>null</code>,
    * contains at least 1 valid entry. Used for validation of the provider
    * name. The provider name cannot match any name in this list.
    */
   private Collection m_existingProviders = null;

   /**
    * Indicates whether the user has modified the provider or created a new
    * one. Currently, it gets set to <code>true</code> only if the <code>onOk
    * </code> method is successfull.
    *
    * @todo Add listeners for all editors and set the flag as soon as the
    * user changes an entry rather than doing it in onOk.
    */
   private boolean m_isModified = false;

   /**
    * A flag that indicates whether the validation framework needs to be
    * created before activating validation. We use the flag so we will only
    * build the framework if the user presses Ok.
    */
   private boolean m_validationInited = false;

   /**
    * A source for a PSDesignerConnection, used for cataloging. If <code>null
    * </code>, cataloging is disabled.
    */
   private IConnectionSource m_connectionSource = null;

   
   /**
    * The attributes panel, initialized through {@link #initUI()}
    */
   private JPanel m_attributesPanel = null;

   /**
    * The backend attributes panel, initialized through {@link #initUI()}, never
    * changed after that.
    */
   private JPanel m_backendPanel = null;
}

