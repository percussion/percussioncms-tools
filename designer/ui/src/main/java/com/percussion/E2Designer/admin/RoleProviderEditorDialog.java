/******************************************************************************
 *
 * [ RoleProviderEditorDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer.admin;

import com.percussion.design.objectstore.PSDirectorySet;
import com.percussion.design.objectstore.PSReference;
import com.percussion.design.objectstore.PSRoleProvider;
import com.percussion.validation.StringConstraint;
import com.percussion.validation.ValidationConstraint;
import com.percussion.UTComponents.UTFixedCharTextField;
import com.percussion.guitools.PSPropertyPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

/**
 * An editor dialog used to create new or edit existing directory service 
 * authentications.
 */
public class RoleProviderEditorDialog extends DirectoryServiceDialog
{
   /**
    * Java serial versino id
    */
   private static final long serialVersionUID = 1L;

   /**
    * Convenience constructor that calls {@link #RoleProviderEditorDialog(Frame,
    * DirectoryServiceData, PSRoleProvider)} with a default role provider.
    */
   public RoleProviderEditorDialog(Frame parent, DirectoryServiceData data)
   {
      this(parent, data, new PSRoleProvider(
         RoleProvidersPanel.getDefaultRoleProviderName(), 
            PSRoleProvider.TYPE_DIRECTORY, "dummy"));
   }
   
   /**
    * Create an authentication editor for the supplied authentication.
    * 
    * @param parent the parent frame, may be <code>null</code>.
    * @param data the current directory service data, not <code>null</code>.
    * @param roleProvider the role provider for which to create the editor,
    *    not <code>null</code>.
    */
   public RoleProviderEditorDialog(Frame parent, DirectoryServiceData data, 
      PSRoleProvider roleProvider)
   {
      super(parent, data);
      
      setTitle(getResources().getString("dlg.title"));
         
      initDialog();
      initData(roleProvider);
   }
   
   /**
    * Initializes the dialog from the supplied data.
    * 
    * @param roleProvider the role provider from which to initialize the
    *    dialog, not <code>null</code>.
    */
   private void initData(PSRoleProvider roleProvider)
   {
      if (roleProvider == null)
         throw new IllegalArgumentException("roleProvider cannot be null");
         
      m_currentName = roleProvider.getName();
         
      m_name.setText(roleProvider.getName());
      
      m_directorySet.addItem("");
      Iterator sets = m_data.getDirectorySets().iterator();
      while (sets.hasNext())
      {
         PSDirectorySet set = (PSDirectorySet) sets.next();
         m_directorySet.addItem(set.getName());
      }
      m_directorySet.addItem(
         getResources().getString("ctrl.directoryset.new"));

      PSReference reference = roleProvider.getDirectoryRef();
      if (reference != null)
         m_directorySet.setSelectedItem(reference.getName());
         
      m_directorySet.addItemListener(new ItemListener()
      {
         public void itemStateChanged(ItemEvent event)
         {
            if (event.getStateChange() == ItemEvent.SELECTED)
            {
               String selectedItem = (String) event.getItem();
               if (selectedItem.equals(getResources().getString(
                  "ctrl.directoryset.new")))
               {
                  m_directorySet.setSelectedIndex(0);
                  addDirectorySet();
               }
            }
         }
      });
      
      if (roleProvider.isDelimited())
      {
         m_delimiterCheck.setSelected(true);
         m_delimiter.setText(roleProvider.getDelimiter());
         m_delimiter.setEnabled(true);
      }
      else
      {
         m_delimiterCheck.setSelected(false);
         m_delimiter.setText("");
         m_delimiter.setEnabled(false);
      }
   }
   
   /**
    * Add a new directory set though the directory set editor dialog.
    * 
    * @return the newly added directory set or <code>null</code> if none was 
    *    added.
    */
   private PSDirectorySet addDirectorySet()
   {
      DirectorySetEditorDialog editor = new DirectorySetEditorDialog(
         (Frame) getParent(), m_data);
      editor.setVisible(true);

      if (editor.isOk())
      {
         PSDirectorySet directorySet = editor.getDirectorySet();
         m_newData.addDirectorySet(directorySet);
         m_newData.addAll(editor.getNewData());

         int index = m_directorySet.getItemCount()-1;
         m_directorySet.insertItemAt(directorySet.getName(), index);
         m_directorySet.setSelectedIndex(index);
         
         return directorySet;
      }
      
      return null;
   }
   
   /**
    * @return a role provider object built from all editor controls,
    *    never <code>null</code>.
    */
   public PSRoleProvider getRoleProvider()
   {
      String name = m_name.getText();
      String directorySet = (String) m_directorySet.getSelectedItem();
         
      PSRoleProvider provider = new PSRoleProvider(name, 
         PSRoleProvider.TYPE_DIRECTORY, directorySet);
      
      String delimiterText = m_delimiter.getText().trim();
      
      if (m_delimiterCheck.isSelected() && delimiterText.length() > 0)
      {
         provider.setDelimiter(delimiterText);
      }
      
      return provider;
   }
   
   /**
    * Overrides super class to validate the name uniqueness.
    */
   public void onOk()
   {
      initValidationFramework();
      
      if (!activateValidation())
         return;
         
      String name = m_name.getText();
      if (!m_currentName.equals(name) && 
         m_data.getRoleProviderNames().contains(name))
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
      JPanel cmdPanel = new JPanel(new BorderLayout());
      cmdPanel.add(createCommandPanel(SwingConstants.HORIZONTAL, true),
                   BorderLayout.EAST);
      panel.add(cmdPanel, BorderLayout.SOUTH);
      
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

      m_directorySet.setToolTipText(
         getResources().getString("ctrl.directoryset.tip"));
      panel.addPropertyRow(getResources().getString("ctrl.directoryset"),
         new JComponent[] { m_directorySet }, m_directorySet,
         getResources().getString("ctrl.directoryset.mn").charAt(0), 
         getResources().getString("ctrl.directoryset.tip"));
      
      JPanel delpanel = new JPanel();
      delpanel.setLayout(new BoxLayout(delpanel, BoxLayout.X_AXIS));
      delpanel.add(m_delimiterCheck);
      delpanel.add(m_delimiter);
      
      m_delimiter.setToolTipText(getResources().getString(
         "ctrl.delimiter.tip"));
      m_delimiter.setColumns(1);
      m_delimiter.setMargin(new Insets(0, 5, 0, 4));
      m_delimiter.setMaximumSize(m_delimiter.getPreferredSize());
      
      m_delimiterCheck.setToolTipText(getResources()
            .getString("ctrl.delimiter.check.tip"));
      m_delimiterCheck.setText(getResources()
            .getString("ctrl.delimiter.check"));
      panel.addPropertyRow(getResources().getString("ctrl.delimiter"),
            new JComponent[] {delpanel});
      
      m_delimiterCheck.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e)
         {
            JCheckBox box = (JCheckBox) e.getSource();
            m_delimiter.setEnabled(box.isSelected());            
         }
      });

      return panel;
   }
   
   /**
    * Initialize the validation framework for this dialog.
    */
   @SuppressWarnings("unchecked")
   private void initValidationFramework()
   {
      List comps = new ArrayList();
      List validations = new ArrayList();
      StringConstraint nonEmpty = new StringConstraint();

      // name: cannot be empty
      comps.add(m_name);
      validations.add(nonEmpty);
         
      comps.add(m_directorySet);
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
    * The role provider name, it's value cannot be empty and must be unique 
    * across all other role providers in this server.
    */
   private JTextField m_name = new JTextField();
   
   /**
    * The directory set, it's value cannot be <code>null</code> or empty.
    */
   private JComboBox m_directorySet = new JComboBox();
   
   /**
    * If this is checked then the delimiter is in use. 
    */
   private JCheckBox m_delimiterCheck = new JCheckBox();
   
   /**
    * The delimiter for role separation in the role attribute
    */
   JTextField m_delimiter = new UTFixedCharTextField(1);
}
