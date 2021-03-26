/******************************************************************************
 *
 * [ DirectoryEditorDialog.java ]
 * 
 * COPYRIGHT (c) 1999 - 2004 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer.admin;

import com.percussion.EditableListBox.EditableListBox;
import com.percussion.EditableListBox.EditableListBoxEditor;
import com.percussion.design.objectstore.IPSGroupProviderInstance;
import com.percussion.design.objectstore.PSAuthentication;
import com.percussion.design.objectstore.PSDirectory;
import com.percussion.security.PSSecurityProvider;
import com.percussion.util.PSCollection;
import com.percussion.validation.StringConstraint;
import com.percussion.validation.ValidationConstraint;
import com.percussion.UTComponents.UTFixedButton;
import com.percussion.guitools.PSPropertyPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EventObject;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

/**
 * An editor dialog used to create new or edit existing directory service 
 * directories.
 */
@SuppressWarnings(value={"unchecked"})
public class DirectoryEditorDialog extends DirectoryServiceDialog 
   implements ItemListener
{
   /**
    * Convenience constructor that calls {@link DirectoryEditorDialog(Frame, 
    * DirectoryServiceData, PSDirectory)} with a default directory.
    */
   public DirectoryEditorDialog(Frame parent, DirectoryServiceData data)
   {
      this(parent, data, new PSDirectory(
         DirectoriesPanel.getDefaultDirectoryName(), 
         PSDirectory.CATALOG_SHALLOW, PSDirectory.FACTORY_LDAP, 
         "defaultAuthentication", "ldap://localhost:389", null));
   }
   
   /**
    * Create an directory editor for the supplied directory.
    * 
    * @param parent the parent frame, may be <code>null</code>.
    * @param data the current directory service data, not <code>null</code>.
    * @param directory the directory for which to create the editor,
    *    not <code>null</code>.
    */
   public DirectoryEditorDialog(Frame parent, DirectoryServiceData data, 
      PSDirectory directory)
   {
      super(parent, data);

      setTitle(getResources().getString("dlg.title"));
      
      initDialog();
      initData(directory);
   }
   
   /**
    * Initializes the dialog from the supplied data.
    * 
    * @param directory the directory from which to initialize the
    *    dialog, not <code>null</code>.
    */
   private void initData(PSDirectory directory)
   {
      if (directory == null)
         throw new IllegalArgumentException("directory cannot be null");
         
      m_currentName = directory.getName();
         
      m_name.setText(directory.getName());
      
      String catalog = directory.isDeepCatalogOption() ? 
         DirectoriesPanel.getCatalogDisplayString(PSDirectory.CATALOG_DEEP) :
         DirectoriesPanel.getCatalogDisplayString(PSDirectory.CATALOG_SHALLOW);
      m_catalog.setSelectedItem(catalog);
      
      m_factory.setSelectedItem(directory.getFactory());
      
      m_authentication.setSelectedItem(
         directory.getAuthenticationRef().getName());
         
      m_provider.setText(directory.getProviderUrl());
      
      if (directory.getAttributes() != null)
      {
         Iterator attributes = directory.getAttributes().iterator();
         while (attributes.hasNext())
            m_attributes.addRowValue(attributes.next());
      }
      
      Iterator<String> groupProviderNames = directory.getGroupProviderNames();
      while (groupProviderNames.hasNext())
      {
         Object value = getGroupProviderInstance(
            (String) groupProviderNames.next());
  
         if (value != null)
            m_groupProvidersListBox.addRowValue(value);
      }      
      
      m_debug.setSelected(directory.isDebug());
   }
   
   /**
    * @return a directory object built from all editor controls,
    *    never <code>null</code>.
    */
   public PSDirectory getDirectory()
   {
      String name = m_name.getText();
      String catalog = m_catalog.getSelectedItem().toString();
      String factory = m_factory.getSelectedItem().toString();
      String authentication = m_authentication.getSelectedItem().toString();
      String provider = m_provider.getText();
      
      
      PSCollection attributes = new PSCollection(String.class);
      int rows = m_attributes.getListModel().getRowCount();
      for (int i=0; i<rows; ++i)
         attributes.add((String) m_attributes.getListModel().getValueAt(i, 0));
         
      PSDirectory directory = new PSDirectory(name, catalog, factory, 
         authentication, provider, attributes.isEmpty() ? null : attributes);

      List groupProviderNames = new ArrayList();
      // Get all group providers added to this security provider
      int groupProviderCount = m_groupProvidersListBox.getItemCount();
      for (int i=0; i<groupProviderCount; i++)
      {
         IPSGroupProviderInstance inst = (IPSGroupProviderInstance)
            m_groupProvidersListBox.getRowValue(i);
         groupProviderNames.add(inst.getName());
      }
      directory.setGroupProviderNames(groupProviderNames.iterator());         
      
      
      directory.setDebug(m_debug.isSelected() ? PSDirectory.DEBUG_YES : null);
      
      return directory;
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
         m_data.getDirectoryNames().contains(name))
      {
         JOptionPane.showMessageDialog(null, 
            getResources().getString("error.msg.notunique"), 
            getResources().getString("error.title"), JOptionPane.ERROR_MESSAGE);
            
         return; 
      }
      
      super.onOk();
   }
   
   
   
   @Override
   public void onCancel()
   {
      disposeGroupProviderDialog();
      super.onCancel();
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
      JPanel cmdPanel = new JPanel(new BorderLayout());
      cmdPanel.add(createCommandPanel(SwingConstants.HORIZONTAL, true), 
              BorderLayout.EAST);
      panel.add(cmdPanel, BorderLayout.SOUTH);

      initValidationFramework();
      
      setResizable(true);
      pack();
      center();
   }
   
   /**
    * Create a new property panel.
    * 
    * @return the new created property panel, never <code>null</code>.
    */
   private JPanel createPropertyPanel()
   {
      JPanel panel = new JPanel();
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

      panel.add(createGeneralPropertiesPanel());
      panel.add(Box.createVerticalStrut(10));
      panel.add(Box.createVerticalGlue());
      panel.add(createAttributesPanel());
      panel.add(Box.createVerticalStrut(10));
      panel.add(Box.createVerticalGlue());
      panel.add(createGroupProvidersPanel());
      panel.add(Box.createVerticalStrut(10));
      panel.add(Box.createVerticalGlue());      
      panel.add(createDebugPanel());

      return panel;
   }
   
   /**
    * @return the debug panel, which allows a user do enable or disable debug
    *    output for this directory.
    */
   private JPanel createDebugPanel()
   {
      PSPropertyPanel panel = new PSPropertyPanel();

      m_debug = new JCheckBox();
      m_debug.setToolTipText(getResources().getString("ctrl.debug.tip"));
      panel.addPropertyRow(getResources().getString("ctrl.debug"),
         new JComponent[] { m_debug }, m_debug,
         getResources().getString("ctrl.debug.mn").charAt(0),
         getResources().getString("ctrl.debug.tip"));
         
      return panel;
   }
   
   /**
    * Create a new general properties panel.
    * 
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
         
      String[] catalogs = PSDirectory.CATALOG_ENUM;
      for (int i=0; i<catalogs.length; i++)
         m_catalog.addItem(DirectoriesPanel.getCatalogDisplayString(catalogs[i]));
      m_catalog.setToolTipText(getResources().getString("ctrl.catalog.tip"));
      panel.addPropertyRow(getResources().getString("ctrl.catalog"),
         new JComponent[] { m_catalog }, m_catalog,
         getResources().getString("ctrl.catalog.mn").charAt(0), 
         getResources().getString("ctrl.catalog.tip"));
         
      for (int i=0; i<PSDirectory.FACTORY_ENUM.length; i++)
         m_factory.addItem(PSDirectory.FACTORY_ENUM[i]);
      m_factory.setToolTipText(getResources().getString("ctrl.factory.tip"));
      panel.addPropertyRow(getResources().getString("ctrl.factory"),
         new JComponent[] { m_factory }, m_factory,
         getResources().getString("ctrl.factory.mn").charAt(0), 
         getResources().getString("ctrl.factory.tip"));

      m_authentication.addItem("");
      Iterator authentications = m_data.getAuthentications().iterator();
      while (authentications.hasNext())
         m_authentication.addItem(
            ((PSAuthentication) authentications.next()).getName());
      m_authentication.addItem(getResources().getString(
         "ctrl.authentication.entry.new"));
      m_authentication.setToolTipText(
         getResources().getString("ctrl.authentication.tip"));
      panel.addPropertyRow(getResources().getString("ctrl.authentication"),
         new JComponent[] { m_authentication }, m_authentication,
         getResources().getString("ctrl.authentication.mn").charAt(0), 
         getResources().getString("ctrl.authentication.tip"));
         
      m_authentication.addItemListener(new ItemListener()
      {
         public void itemStateChanged(ItemEvent event)
         {
            if (event.getStateChange() == ItemEvent.SELECTED)
            {
               String selectedItem = (String) event.getItem();
               if (selectedItem.equals(getResources().getString(
                  "ctrl.authentication.entry.new")))
               {
                  m_authentication.setSelectedIndex(0);
                  addAuthentication();
               }
            }
         }
      });
      JPanel providerPanel = createProviderPanel();
      panel.addPropertyRow(getResources().getString("ctrl.provider"),
         new JComponent[] { providerPanel}, providerPanel,
         getResources().getString("ctrl.provider.mn").charAt(0),
         getResources().getString("ctrl.provider.tip"));
         
      return panel;
   }
   
   /**
    * Adds a new authentication through the authentication editor dialog.
    */
   private void addAuthentication()
   {
      AuthenticationEditorDialog editor = 
         new AuthenticationEditorDialog((Frame) getParent(), m_data);
      editor.show();
      
      if (editor.isOk())
      {
         PSAuthentication authentication = editor.getAuthentication();
         m_newData.addAuthentication(authentication);
         
         int index = m_authentication.getItemCount()-1;
         m_authentication.insertItemAt(authentication.getName(), index);
         m_authentication.setSelectedIndex(index);
      }
   }
   
   /**
    * @return the provider URL panel, a text box with an edit button. Never
    *    <code>null</code>.
    */
   private JPanel createProviderPanel()
   {
      JPanel panel = new JPanel();
      panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

      m_provider.setToolTipText(getResources().getString("ctrl.provider.tip"));
      m_providerEditorButton = new UTFixedButton(
         getResources().getString("ctrl.provider.button"), 20, 20);
      m_providerEditorButton.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent event)
         {
            onSelectProvider();
         }
      });

      panel.add(m_provider);
      panel.add(Box.createHorizontalStrut(10));
      panel.add(Box.createHorizontalGlue());
      panel.add(m_providerEditorButton);
      panel.setPreferredSize(new Dimension(400, 
         panel.getPreferredSize().height));
      
      return panel;
   }

   /**
    * Creates a panel that contains an <code>EditableListBox</code> that enables
    * a user to add new attributes or edit and delete exxisting entries in 
    * the list box.
    *
    * @return the attributes panel, never <code>null</code>.
    */
   private JPanel createAttributesPanel()
   {
      JPanel panel = new JPanel(new BorderLayout(5, 5));

      m_attributes = new EditableListBox(
         getResources().getString("ctrl.attributes.title"), this, null, null, 
         EditableListBox.TEXTFIELD, EditableListBox.INSERTBUTTON);
      m_attributes.getRightButton().addActionListener( new ActionListener()
      {
         public void actionPerformed( ActionEvent evt )
         {
            DirectoryEditorDialog.this.m_attributes.deleteRows();
         }
      });
      
      panel.add(m_attributes);
      panel.setBorder(new EmptyBorder(10, 0, 0, 0));
      panel.setPreferredSize(new Dimension(400, 100));
      
      return panel;
   }
   
   /**
    * Creates a panel that contains an <code>EditableListBox</code> to add new
    * group providers from the existing list or to create a new group provider
    * or to delete the provider from the list.
    *
    * @return The completed panel, never <code>null</code>
    */
   private JPanel createGroupProvidersPanel()
   {
      JPanel panel = new JPanel(new BorderLayout(5,5));
      TableCellEditor cellEditor = new EditableListBoxEditor(
         m_groupProvidersEditor)
      {
         /**
          * Makes the cell as non-editable if the cell has a value(group
          * provider) and the event is mouse double-click. This is to make it
          * use the group provider dialog for editing a group provider instead
          * of using the combo-box editor.
          *
          * @param anEvent the event which tries to edit the cell, may be
          * <code>null</code>
          */
         public boolean isCellEditable(EventObject anEvent)
         {
            boolean isEditable = false;

            if (anEvent instanceof MouseEvent)
            {
               MouseEvent event = (MouseEvent)anEvent;
               if (event.getClickCount() == 2)
               {
                  Point point = event.getPoint();
                  int row = m_groupProvidersListBox.getList().rowAtPoint(point);
                  if (row >= 0)
                  {
                     Object value = m_groupProvidersListBox.getRowValue(row);
                     if (!(value instanceof IPSGroupProviderInstance))
                        isEditable = true;
                  }
               }
            }
            else
               isEditable = super.isCellEditable(anEvent);

            return isEditable;
         }
      };

      m_groupProvidersEditor.addItemListener(this);

      m_groupProvidersListBox = new EditableListBox(
         getResources().getString( "GroupProvidersListTitle" ), this, null, null,
         cellEditor, EditableListBox.INSERTBUTTON);
         
      m_groupProvidersListBox.getRightButton().addActionListener(
         new ActionListener()
         {
            //deletes all selected rows in the list box
            public void actionPerformed( ActionEvent evt )
            {
               m_groupProvidersListBox.deleteRows();
            }
         }
      );
      m_groupProvidersListBox.setSelectionMode(
         ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

      m_groupProvidersListBox.getLeftButton().addActionListener(
         new ActionListener()
         {
            /**
             * Updates the cell editor combo-box with the available group
             * providers and an entry to create a new group provider. The group
             * providers already added to the list box won't be available to
             * add again.
             *
             * @param evt the action event caused this call, assumed not be
             * <code>null</code> as this is a listener method which must be
             * called only when an action event occurs.
             */
            public void actionPerformed( ActionEvent evt )
            {
               m_groupProvidersEditor.removeAllItems();
               m_groupProvidersEditor.removeItemListener(
                  DirectoryEditorDialog.this);
               m_groupProvidersEditor.addItem( getResources().getString(
                  "CreateNewGroup") );
               Iterator availableProviders = getAvailableGroupProviders();
               while (availableProviders.hasNext())
                  m_groupProvidersEditor.addItem(availableProviders.next());
                  
               m_groupProvidersEditor.setSelectedItem(null);
               m_groupProvidersEditor.addItemListener(
                  DirectoryEditorDialog.this);
            }
         }
      );

      m_groupProvidersListBox.getList().addMouseListener(
         new MouseAdapter()
         {
            /**
             * Displays group provider dialog to edit the currently selected
             * group provider. Activated when the left-mouse-button is double
             * clicked on a group provider value.
             *
             * @param e the mouse event, assumed not to be <code>null</code> as
             * this is a listener method which must be called only when a mouse
             * clicked event occurs.
             */
            public void mouseClicked(MouseEvent e)
            {
               if (((e.getModifiers() & InputEvent.BUTTON1_MASK) ==
                  InputEvent.BUTTON1_MASK) && e.getClickCount() == 2)
               {
                  Point point = e.getPoint();
                  int row = m_groupProvidersListBox.getList().rowAtPoint(point);
                  if (row >= 0)
                  {
                     Object rowValue = m_groupProvidersListBox.getRowValue(row);
                     if (rowValue instanceof IPSGroupProviderInstance)
                     {
                        displayGroupProviderDialog(rowValue);
                     }
                  }
               }
            }
         }
      );
      
      panel.add(m_groupProvidersListBox, BorderLayout.CENTER);
      panel.setBorder(new EmptyBorder(10, 0, 0, 0));
      panel.setPreferredSize(new Dimension(400, 100));
      
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

      // authentication: cannot be empty
      comps.add(m_authentication);
      validations.add(nonEmpty);

      // provider: cannot be empty
      comps.add(m_provider);
      validations.add(nonEmpty);

      Component[] components = new Component[comps.size()];
      comps.toArray(components);
      
      ValidationConstraint[] constraints = 
         new ValidationConstraint[validations.size()];
      validations.toArray(constraints);
      
      setValidationFramework(components, constraints);
   }
   
   /**
    * Initialize and show the provider URL selector dialog.
    */
   private void onSelectProvider()
   {
      setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      
      DirectoryServiceData data = new DirectoryServiceData();
      data.addAll(m_data);
      data.addAll(m_newData);
      
      ProviderUrlSelectorDialog selector = new ProviderUrlSelectorDialog(
         (Frame) getParent(), m_provider.getText(), 
         (String) m_authentication.getSelectedItem(), data);
      
      setCursor(Cursor.getDefaultCursor());
      selector.show();
      
      if (selector.isOk())
         m_provider.setText(selector.getProviderUrl());
   }
   
   /**
    * Gets the available group providers to set to this security provider
    * instance. Considers only the providers valid for this security provider
    * type and omits the providers already added to this instance.
    *
    * @return the iterator over zero or more IPSGroupProviderInstance objects,
    * never <code>null</code>
    */
   private Iterator getAvailableGroupProviders()
   {
      List allowedGroupProviders = new ArrayList();
      Iterator providers = m_data.getGroupProviders().iterator();
      while (providers.hasNext())
      {
         IPSGroupProviderInstance providerInst =
            (IPSGroupProviderInstance) providers.next();

         allowedGroupProviders.add(providerInst);
      }

      //Get the list of provider names already added to the list.
      List usedProviders = new ArrayList();
      int count = m_groupProvidersListBox.getItemCount();
      for (int i=0; i<count; i++)
      {
         Object obj = m_groupProvidersListBox.getRowValue(i);
         if (obj instanceof IPSGroupProviderInstance)
         {
            IPSGroupProviderInstance providerInst =
               (IPSGroupProviderInstance) obj;
            usedProviders.add(providerInst.getName());
         }
      }

      //Get the providers which are available to add
      List availableGroupProviders = new ArrayList();
      providers = allowedGroupProviders.iterator();
      while (providers.hasNext())
      {
         IPSGroupProviderInstance providerInst =
            (IPSGroupProviderInstance) providers.next();
         if (!usedProviders.contains(providerInst.getName()))
            availableGroupProviders.add(providerInst);
      }

      return availableGroupProviders.iterator();
   }
   
   
   /**
    * Overridden to avoid obfuscation issues.
    */
   protected ResourceBundle getResources()
   {
      return super.getResources();
   }
   
   
   /**
    * Handles the 'Create new...' action in the group providers list editor.
    * Displays the group provider dialog to create a new group provider when
    * 'Create new..' item is clicked in the list editor. Updates currently
    * editing row in the list and the list of group providers with new one and
    * sets the flag <code>m_bGroupProvidersChanged</code> to <code>true</code>
    * upon user clicks OK in the dialog. If the user cancels the dialog, the
    * editing row is removed.
    */
   public void itemStateChanged(ItemEvent e)
   {
      Object obj = e.getItem();
      if (e.getSource() == m_groupProvidersEditor &&
         e.getStateChange() == ItemEvent.SELECTED)
      {
         if (obj instanceof String)
         {
            if( ((String)obj).equals( getResources().getString(
               "CreateNewGroup") ) )
            {
               displayGroupProviderDialog(null);
               if (m_groupProviderDlg.isInstanceModified())
               {
                  // clicked OK
                  int row = m_groupProvidersListBox.getList().getEditingRow();
                  m_groupProvidersListBox.setRowValue(
                     m_groupProviderDlg.getInstance(), row);
                     
                  m_bGroupProvidersChanged = true;
                  m_data.getGroupProviders().add(
                     m_groupProviderDlg.getInstance());
               }
               else
               {
                  //clicked Cancel, so remove the editing cell
                  m_groupProvidersListBox.deleteRows();
               }
            }
         }
      }
   }

   /**
    * Disposes the group provider dialog re-used in this dialog.
    */
   private void disposeGroupProviderDialog()
   {
      if (m_groupProviderDlg != null)
      {
         m_groupProviderDlg.dispose();
         m_groupProviderDlg = null;
      }
   }
   

   /**
    * Gets the group provider with the specified name.
    *
    * @param name the name of the group provider, assumed not to be
    * <code>null</code> or empty.
    *
    * @return the group provider, may be <code>null</code> if the matching group
    * provider is not found.
    *
    * @throws IllegalStateException if the collection of group providers is not
    * yet set.
    */
   private Object getGroupProviderInstance(String name)
   {
      Iterator providers = m_data.getGroupProviders().iterator();
      while (providers.hasNext())
      {
         IPSGroupProviderInstance provInst =
            (IPSGroupProviderInstance) providers.next();
         if (provInst.getName().equals(name))
            return provInst;
      }
      
      return null;
   }
   
   /**
    * Displays group provider dialog either to create or edit a group provider.
    * Caches the dialog instance and re-uses it.
    */
   private void displayGroupProviderDialog(Object value)
   {
      if (m_groupProviderDlg == null)
         m_groupProviderDlg = new JndiGroupProviderDialog((JFrame) getParent());

      Collection existingProviders = getExistingGroupProviderNames();
      m_groupProviderDlg.setProviderData(PSSecurityProvider.SP_TYPE_DIRCONN,
         PSDirectory.FACTORY_LDAP, existingProviders, 
         (IPSGroupProviderInstance) value);
         
      m_groupProviderDlg.setVisible(true);
   }

   /**
    * @return the list of existing group provider names, may be empty, never
    * <code>null</code>
    */
   private Collection getExistingGroupProviderNames()
   {
      List providerNames = new ArrayList();
      Iterator providers = m_data.getGroupProviders().iterator();
      while (providers.hasNext())
      {
         IPSGroupProviderInstance instance =
            (IPSGroupProviderInstance)providers.next();
         providerNames.add( instance.getName() );
      }
      return providerNames;
   }   
   
   /**
    * The directory name at initialization time. Used to validate the
    * name for uniqueness. Never <code>null</code> or changed.
    */
   private String m_currentName = null;
   
   /**
    * The directory name, it's value cannot be empty and must be unique 
    * across all other directories in this server.
    */
   private JTextField m_name = new JTextField();
   
   /**
    * The catalog type selection, initialized 
    * {@link createGeneralPropertiesPanel()}, never empty or changed after that.
    */
   private JComboBox m_catalog = new JComboBox();
   
   /**
    * The factory class selection, initialized in 
    * {@link createGeneralPropertiesPanel()}, never empty or changedd after that.
    */
   private JComboBox m_factory = new JComboBox();

   /**
    * The authentication selection, initialized 
    * {@link createGeneralPropertiesPanel()}, never empty or changed after that.
    */
   private JComboBox m_authentication = new JComboBox();
   
   /**
    * The provider url, it's value cannot be empty.
    */
   private JTextField m_provider = new JTextField();
   
   /**
    * Button to open the provider url ddialog, initialized in 
    * {@link createProviderPanel()}, never <code>null</code> or changed after 
    * that.
    */
   private JButton m_providerEditorButton = null;
   
   /**
    * A check box to allow a user to specify whether or not to output
    * debug messages to the console.
    */
   private JCheckBox m_debug = null;
   
   /**
    * An edditable list box to add, edit, delete or view return attributes.
    * Initialized in {@link createAttributesPanel()}, never <code>null</code>
    * or changed after that.
    */
   private EditableListBox m_attributes = null;
   
   /**
    * The group provider dialog user to create or edit group providers. This is
    * cached to re-use the resources required for dialog. Gets initialized the
    * first time <code>displayGroupProviderDialog()</code> method is called and
    * gets disposed when this dialog becomes inivisible.
    */
   private JndiGroupProviderDialog m_groupProviderDlg;
   

   /**
    * The editing component to add or remove group providers to the list.
    * Initialized in <code>createGroupProvidersPanel()</code> and never
    * <code>null</code> after that.
    */
   private EditableListBox m_groupProvidersListBox = null;

   /**
    * The editor used to add a group provider to the list.
    */
   private JComboBox m_groupProvidersEditor = new JComboBox();

   /**
    * A flag to indicate that the collection of the group providers has been
    * changed or not. Initialized to <code>false</code> and set to <code>true
    * </code> when a new group provider is added.
    */
   private boolean m_bGroupProvidersChanged = false;   
}
