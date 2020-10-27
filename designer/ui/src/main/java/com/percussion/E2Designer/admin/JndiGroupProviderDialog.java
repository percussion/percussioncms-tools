/******************************************************************************
 *
 * [ JndiGroupProviderDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.E2Designer.admin;

import com.percussion.E2Designer.ListMemberConstraint;
import com.percussion.E2Designer.PSDialog;
import com.percussion.E2Designer.StringConstraint;
import com.percussion.E2Designer.UTJTable;
import com.percussion.E2Designer.UTStandardCommandPanel;
import com.percussion.E2Designer.UTTableModel;
import com.percussion.E2Designer.Util;
import com.percussion.E2Designer.ValidationConstraint;
import com.percussion.EditableListBox.EditableListBox;
import com.percussion.design.objectstore.IPSGroupProviderInstance;
import com.percussion.design.objectstore.PSJndiGroupProviderInstance;
import com.percussion.design.objectstore.PSJndiObjectClass;
import com.percussion.security.PSSecurityProvider;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.MissingResourceException;
import java.util.Vector;

/**
 * Implements a dialog for editing or creating a JNDI directory or connection
 * based group provider instance. The dialog is resizable and fully
 * internationalized.
 */
public class JndiGroupProviderDialog extends PSDialog
   implements IGroupProviderEditor
{
   /**
    * The standard ctor for the dialog. Creates the dialog centered on the
    * screen. The designer must call <code>setProviderData</code> before calling
    * <code>setVisible</code> to activate the dialog. Caches the instance of
    * <code>JndiGroupProviderObjectClasses</code> class to populate the default
    * group properties in the dialog to create a new group provider.
    *
    * @param parent The parent of this dialog, may be <code>null</code>
    */
   public JndiGroupProviderDialog(JFrame parent)
   {
      super( parent);
      setTitle( getString("Title") );
      initDialog();

      if( ms_objClassInstance == null)
          ms_objClassInstance = JndiGroupProviderObjectClasses.getInstance();
   }

   /**
    * Initializes the dialog with all controls.
    */
   private void initDialog()
   {
      JPanel panel = new JPanel();
      getContentPane().add(panel);
      panel.setLayout(new BorderLayout(30,5));
      panel.setBorder((new EmptyBorder (10,10,10,10)));

      panel.add(createFieldsPanel(), BorderLayout.CENTER);
      JPanel cmdPanel = new JPanel(new BorderLayout());
      cmdPanel.add(createCommandPanel(), BorderLayout.EAST);
      panel.add(cmdPanel, BorderLayout.SOUTH);

      pack();
      setResizable(true);
      center();
   }

   /**
    * @return a panel that makes the left section of the panel which has
    * controls for editing, never <code>null</code>
    *
    */
   private JPanel createFieldsPanel()
   {
      JPanel namePanel = new JPanel();
      namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.X_AXIS));
      JLabel label = new JLabel( getString("ProviderName"), SwingConstants.RIGHT);
      namePanel.add(label);
      namePanel.add(Box.createHorizontalStrut(5));
      m_providerName = new JTextField(30);
      m_providerName.setMinimumSize(new Dimension(100, 20));
      m_providerName.setPreferredSize(new Dimension(350, 20));
      m_providerName.setMaximumSize(new Dimension(Short.MAX_VALUE,20));
      namePanel.add(m_providerName);

      JPanel propertiesPanel = new JPanel(new BorderLayout(5,5));
      propertiesPanel.add( createTable(), BorderLayout.CENTER );
      propertiesPanel.setBorder( createGroupBorder(
            getString("PropertiesBorder") ) );

      JPanel entriesPanel = new JPanel();
      entriesPanel.setLayout(new BoxLayout(entriesPanel, BoxLayout.Y_AXIS));
      m_entriesListBox = new EditableListBox(
         getString( "DirEntriesListTitle" ),
         this,
         null,
         null,
         EditableListBox.TEXTFIELD,
         EditableListBox.INSERTBUTTON );
      m_entriesListBox.getRightButton().addActionListener( new ActionListener()
      {
         //Deletes all selected rows in Directory Entries List box.
         public void actionPerformed( ActionEvent evt )
         {
            JndiGroupProviderDialog.this.m_entriesListBox.deleteRows();
         }
      });
      m_entriesListBox.setPreferredSize(new Dimension(350,150));
      m_entriesListBox.setMinimumSize(
         new Dimension(Short.MIN_VALUE, Short.MIN_VALUE));
      m_entriesListBox.setMaximumSize(
         new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
      entriesPanel.add( m_entriesListBox);

      JPanel fieldsPanel = new JPanel(new BorderLayout(5,5));
      fieldsPanel.add(namePanel, BorderLayout.NORTH);
      fieldsPanel.add(propertiesPanel, BorderLayout.CENTER);
      fieldsPanel.add(entriesPanel, BorderLayout.SOUTH);

      return fieldsPanel;
   }

   /**
    * @return a scrolling pane with a table to add/edit group properties, never
    * <code>null</code>
    */
   private JScrollPane createTable()
   {
       m_propertiesModel = new UTTableModel();

      Vector columnHeaders = new Vector(3);
      columnHeaders.insertElementAt( getString("colObjectClass"),
        OBJECT_CLASS_COLUMN);
      columnHeaders.insertElementAt( getString("colMemberAttribute"),
         MEMBER_ATTRIBUTE_COLUMN);
      columnHeaders.insertElementAt( getString("colType"),
        TYPE_COLUMN);

      m_propertiesModel.createTable(columnHeaders);
      m_propertiesModel.setMinRows(10);

      m_propertiesTable = new UTJTable(m_propertiesModel);

      m_propertiesTable.getColumn(
         m_propertiesModel.getColumnName(OBJECT_CLASS_COLUMN)).
            setCellEditor(new DefaultCellEditor( new JTextField() ));

      m_propertiesTable.getColumn(
         m_propertiesModel.getColumnName(MEMBER_ATTRIBUTE_COLUMN)).
            setCellEditor(new DefaultCellEditor( new JTextField() ));

      Vector types = new Vector(2);
      types.add(PSJndiObjectClass.MEMBER_ATTR_TYPE_ENUM[
         PSJndiObjectClass.MEMBER_ATTR_STATIC ]);
      types.add(PSJndiObjectClass.MEMBER_ATTR_TYPE_ENUM[
         PSJndiObjectClass.MEMBER_ATTR_DYNAMIC ]);
      DefaultCellEditor typeEditor =
         new DefaultCellEditor(new JComboBox(types));
      typeEditor.setClickCountToStart(2);
      m_propertiesTable.getColumn(m_propertiesModel.getColumnName(TYPE_COLUMN)).
         setCellEditor(typeEditor);

      m_propertiesTable.setSelectionColumn(true);

      m_propertiesTable.getSelectionModel().addListSelectionListener(
         new ListSelectionListener()
         {
            //Adds a new row when the last row is selected
            public void valueChanged (ListSelectionEvent e)
            {
               int row = m_propertiesTable.getSelectedRow();
               if(row == m_propertiesModel.getRowCount()-1)
               {
                  m_propertiesModel.appendRow();
               }
            }
         }
      );

      JScrollPane pane = new JScrollPane (m_propertiesTable);
      pane.setPreferredSize(new Dimension (80, 125));
      pane.setAlignmentX(LEFT_ALIGNMENT);
      return pane;

   }

    /**
    *   Creates the command panel with OK, Cancel and Help buttons.
    *
    * @return The command panel, never <code>null</code>.
    */
   private JPanel createCommandPanel()
   {
      UTStandardCommandPanel commandPanel =
         new UTStandardCommandPanel(this, "", SwingConstants.HORIZONTAL)
      {
        public void onOk()
        {
            JndiGroupProviderDialog.this.onOk();
        }

        public void onCancel()
        {
            JndiGroupProviderDialog.this.onCancel();
        }

      };

      getRootPane().setDefaultButton(commandPanel.getOkButton());

      return commandPanel;
   }

   /**
    * Gets the resource string of the specified key. If the string is not found
    * returns the key itself. Logs to <code>System.err</code> if resource string
    * is not found for the key.
    *
    * @param key the resource key, assumed not to be <code>null</code> and
    * empty.
    *
    * @return the resource string of the key if found, otherwise key itself,
    * never <code>null</code>.
    */
   private String getString( String key )
   {
      if ( null == key || key.trim().length() == 0 )
         throw new IllegalArgumentException( "key can't be null or empty" );

      String resourceValue = key;
      try
      {
         if (getResources() != null)
            resourceValue = getResources().getString( key );
      } catch (MissingResourceException e)
      {
         // not fatal; warn and continue
         System.err.println( this.getClass() );
         System.err.println( e );
      }
      return resourceValue;

   }

    /**
    * Creates the validation framework and sets it in the parent dialog. After
    * setting it, the <code>m_validationInited</code> flag is set to indicate it
    * doesn't need to be done again. If something changes requiring a new
    * framework, just clear this flag and the next time onOk is called, the
    * framework will be recreated.
    * <p>By using the flag, this method can be called multiple times w/o a
    * performance penalty.
    *
    */
   private void initValidationFramework()
   {
      if ( m_validationInited )
         return;

      Component [] c = new Component[]
      {
         m_providerName,
         m_providerName,
      };

      ValidationConstraint nonEmpty = new StringConstraint();
      ValidationConstraint [] v = new ValidationConstraint[]
      {
         nonEmpty,
         new ListMemberConstraint( null != m_existingProviderNames
            ? m_existingProviderNames : new ArrayList()),
      };

      setValidationFramework( c, v );

      m_validationInited = true;
   }

   /**
    * Handler for the ok button.
    * <p>Performs all necessary actions when the user presses the OK buttion.
    * First, all input data is validated. If it validates correctly, the
    * existing group provider instance is updated (if there is one) or a new
    * instance is created. If validation fails, a message is displayed to
    * the user and control returns to the dialog, with the focus set to the
    * invalid control. On success, the dialog is hidden and control returns
    * to the caller.
    * <p>Does not dispose the dialog. The dialog is not disposed so it can be
    * re-used w/o re-allocating all of the peer components. If the designer
    * wants to free up resources, see {@link java.awt.Dialog#dispose()
    * dispose}.
    */
   public void onOk()
   {
      stopTableEditor(m_propertiesTable);
      if (m_entriesListBox.getList().isEditing())
         m_entriesListBox.getCellEditor().stopCellEditing();
      
      initValidationFramework();

      //Validate the provider name if we are creating a new instance
      if(m_providerInst == null)
      {
         if ( !activateValidation())
         {
            m_providerName.requestFocus();
            return;
         }
      }

      //validate that rows filled does not have any empty cells.
      int rows = m_propertiesModel.getRowCount();
      for(int i=0; i<rows; i++)
      {
         if(!m_propertiesModel.isRowEmpty(i) &&
            m_propertiesModel.isAnyCellEmpty(i))
         {
            m_propertiesTable.requestFocus();
            JOptionPane.showMessageDialog(null,
               Util.cropErrorMessage( getString("emptyCells") ),
               getString("error"), JOptionPane.ERROR_MESSAGE);
            return;
         }
      }

      m_bInstanceModified = true;
      m_validationInited = false;

      if(m_providerInst == null)
      {
         m_providerInst = new PSJndiGroupProviderInstance(
            m_providerName.getText(), m_secProviderType);
      }
      m_providerInst.clearObjectClasses();
      m_providerInst.clearGroupNodes();

      //update object classes
      rows = m_propertiesModel.getRowCount();
      for(int i=0; i<rows; i++)
      {
         if(!m_propertiesModel.isRowEmpty(i))
         {
            String objectClass =
               (String)m_propertiesModel.getValueAt(i, OBJECT_CLASS_COLUMN);
            String memberAttr =
               (String)m_propertiesModel.getValueAt(i, MEMBER_ATTRIBUTE_COLUMN);
            String valueType =
               (String)m_propertiesModel.getValueAt(i, TYPE_COLUMN);

            int type = 0;
            for (int j = 0; j < PSJndiObjectClass.MEMBER_ATTR_TYPE_ENUM.length;
               j++)
            {
               if (valueType.equals(PSJndiObjectClass.MEMBER_ATTR_TYPE_ENUM[j]))
               {
                  type = j;
               }
            }
            m_providerInst.addObjectClass(objectClass, memberAttr, type);
         }
      }

      //update directory entries
      rows = m_entriesListBox.getItemCount();
      for(int i=0; i<rows; i++)
      {
         String dirEntry = (String)m_entriesListBox.getRowValue(i);
         if(dirEntry != null && dirEntry.trim().length() != 0)
            m_providerInst.addGroupNode(dirEntry);
      }

      setVisible(false);
   }


   /**
   * Handler for the Cancel button.
   * <p>Performs any cleanup necessary when exiting the dialog. Does not dispose
   * the dialog. This is not done so it can be re-used w/o re-allocating all
   * of the peer components. If the designer wants to free up resources, see
   * {@link java.awt.Dialog#dispose() dispose}.
   */
   public void onCancel()
   {
      m_bInstanceModified = false;
      setVisible(false);
   }

   //Methods from implementation of interface IGroupProviderEditor

   //See interface for description.
   public boolean isInstanceModified()
   {
      return m_bInstanceModified;
   }

   //See interface for description.
   public IPSGroupProviderInstance getInstance()
   {
      return m_providerInst;
   }

   //See interface for description.
   public void setProviderData(int type, String providerClassName,
      Collection groupProviderNames,
         IPSGroupProviderInstance groupProviderInst)
   {
      if ( this.isVisible())
         return;

      if (type != PSSecurityProvider.SP_TYPE_DIRCONN )
         throw new IllegalArgumentException("Incorrect security provider type");

      m_secProviderType = type;
      
      if(providerClassName == null || providerClassName.trim().length() == 0)
         throw new IllegalArgumentException(
            "providerClassName can not be null or empty");

      if(groupProviderNames != null)
      {
         Iterator names =  groupProviderNames.iterator();
         while(names.hasNext())
         {
            if( !(names.next() instanceof String) )
            {
               throw new IllegalArgumentException(
                  "groupProviderNames must be a list of non-null Strings");
            }
         }
      }

      m_existingProviderNames = groupProviderNames;
      m_providerInst = (PSJndiGroupProviderInstance)groupProviderInst;

      // clear all data from controls
      reset();

      Iterator objectClasses = null;
      if(groupProviderInst != null)  //edit instance
      {
         //Name should not be editable
         m_providerName.setText(groupProviderInst.getName());
         m_providerName.setEnabled(false);

         //Get all objectClass properties
         objectClasses = m_providerInst.getObjectClasses();

         //display all directory entries
         Iterator directoryEntries = m_providerInst.getGroupNodes();
         while(directoryEntries.hasNext())
         {
            m_entriesListBox.addRowValue(directoryEntries.next());
         }
      }
      else
      {
         //Get default objectClass properties for the specified jndi group
         //provider class name
         if(ms_objClassInstance != null)
         {
            objectClasses =
               ms_objClassInstance.getObjectClasses(providerClassName);
         }
      }

      //display all objectClass properties
      if(objectClasses != null)
      {
         while(objectClasses.hasNext())
         {
            PSJndiObjectClass objectClass =
               (PSJndiObjectClass)objectClasses.next();

            String objectClassName = objectClass.getObjectClassName();
            String memberAttribute = objectClass.getMemberAttribute();
            String memberAttrType = PSJndiObjectClass.MEMBER_ATTR_TYPE_ENUM[
               objectClass.getAttributeType()];

            Vector data = new Vector(3);
            data.insertElementAt(objectClassName, OBJECT_CLASS_COLUMN);
            data.insertElementAt(memberAttribute, MEMBER_ATTRIBUTE_COLUMN);
            data.insertElementAt(memberAttrType, TYPE_COLUMN);
            m_propertiesModel.appendRow(data);
         }
      }

      //Maintains at least 10 rows in table
      m_propertiesModel.setMinRows(10);
   }

   /**
    * Clears all controls.
    */
   private void reset()
   {
      m_providerName.setEnabled(true);
      m_providerName.setText("");
      m_propertiesModel.clearTableEntries();
      for ( int i = m_entriesListBox.getItemCount()-1; i >= 0; --i )
         m_entriesListBox.removeItemAt(i);
   }

   /**
    * A flag to indicate whether to create the validation framework when the
    * onOk method is called. If <code>false</code>, the validation framework
    * is created, otherwise the existing one is used. Initialized to <code>false
    * </code> and set to <code>true</code> after initializing the validation
    * framework.
    */
   private boolean m_validationInited = false;

   /**
    * Reference to static instance of {@link JndiGroupProviderObjectClasses }
    * so that this instance is not garbage collected until this dialog instance
    * is in memory which reduces the task of reloading the resoure file.
    * Initialized in the constructor and never modified after that.
    */
   private static JndiGroupProviderObjectClasses ms_objClassInstance;

   /**
    * The group provider instance to be created or edited, set in {@link
    * #setProviderData}. Gets initialized with new instance if we are creating a
    * new group provider, otherwise gets modified in <code>onOk()</code> method.
    */
   private PSJndiGroupProviderInstance m_providerInst = null;

   /**
    * The list of existing group provider names, used to test for unique group
    * provider name while creating new group provider. Set in {@link
    * #setProviderData} and never modified after that. If <code>null</code> or
    * empty, assumes there are no existing providers.
    */
   private Collection m_existingProviderNames;

   /**
    * The security provider type of the group provider. Must be one of the
    * <code>PSSecurityProvider.SP_TYPE_xxx</code> types. Initialized in
    * {@link #setProviderData} and never modified after that.
    */
   private int m_secProviderType;

   /**
    * A flag to indicate whether the current instance set on this dialog is
    * modified or not. Set to <code>true</code> in <code>onOk()</code> and set
    * to <code>false</code> in <code>onCancel()</code>. Initialized to
    * <code>false</code>.
    */
   private boolean m_bInstanceModified = false;

   /**
    * The text field to enter or display provider name. Initialized in
    * <code>createFieldsPanel()</code> and never <code>null</code> after that.
    */
   private JTextField m_providerName;

   /**
    * The table to display group properties. Initialized in
    * <code>createTable()</code> and never <code>null</code> after that.
    */
   private UTJTable m_propertiesTable;

   /**
    * The table model for group properties table. Initialized in
    * <code>createTable()</code> and never <code>null</code> after that.
    */
   private UTTableModel m_propertiesModel;

   /**
    * The editable list box to add/delete directory entries. Initialized in
    * <code>createFieldsPanel()</code> and never <code>null</code> after that.
    */
   private EditableListBox m_entriesListBox;

   /**
    * The constant to indicate 'objectClass' column index.
    */
   private static final int OBJECT_CLASS_COLUMN = 0;

   /**
    * The constant to indicate 'Member Attribute' column index.
    */
   private static final int MEMBER_ATTRIBUTE_COLUMN = 1;

   /**
    * The constant to indicate 'Type' column index.
    */
   private static final int TYPE_COLUMN = 2;
}
