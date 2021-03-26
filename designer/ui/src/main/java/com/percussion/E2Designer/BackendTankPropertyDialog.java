/* *****************************************************************************
 *
 * [ BackendTankPropertyDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *******************************************************************************/

package com.percussion.E2Designer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Vector;

/**
 * The DatatankInfoDialog provides read only information about the datatank.
 */
public class BackendTankPropertyDialog extends PSEditorDialog
{
   /**
    * Construct the default datatank info dialog.
    * 
    */
   public BackendTankPropertyDialog()
   {
      super();
      initDialog();
   }

   /**
    * Construct the datatank info dialog with provided information.
    * 
    * @param frame the parent frame.
    */
   public BackendTankPropertyDialog(JFrame frame)
   {
      super(frame);
      initDialog();
   }

   /**
    * Construct the datatank info dialog with provided information.
    * 
    * @param dialog the parent dialog.
    */
   public BackendTankPropertyDialog(JDialog dialog)
   {
      super(dialog);
      initDialog();
   }

   /**
    * Create read only panels for all display fields.
    * 
    * @param field the field to create th epanel for
    * @param resId the fields resource ID
    */

   private JPanel createFieldPanel(JComponent field, String resId)
   {
      if (field instanceof JTextField)
         ((JTextField)field).setText("");
      
      // TO DO: disable when tests finished and grey out!
      field.setEnabled(true);
      // field.setBackground(Color.lightGray);

      JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
      String lblStr = getResources().getString(resId);
      char mn = getResources().getString(resId + ".mn").charAt(0);
      JLabel lbl = new JLabel(lblStr);
      lbl.setLabelFor(field);
      lbl.setDisplayedMnemonic(mn);
      lbl.setDisplayedMnemonicIndex(lblStr.indexOf(mn));
      panel.add(lbl);
      panel.add(field);

      return panel;
   }

   /**
    * Create the dialogs view/edit panel.
    * 
    * @return JPanel a grid panel containing the dialogs view/edit panel.
    */
   private JPanel createViewPanel()
   {
      Box box = new Box(BoxLayout.Y_AXIS);
      box.add(Box.createVerticalGlue());
      box.add(createFieldPanel(m_alias, "alias"));
      box.add(createFieldPanel(m_datasource, "datasource"));
      box.add(createFieldPanel(m_table, "table"));

      JPanel panel = new JPanel(new BorderLayout());
      panel.add(box, BorderLayout.WEST);

      return panel;
   }

   /**
    * Initialize the datatank info dialog with GUI elements and data
    */
   private void initDialog()
   {
      // initialize the standard command panel, implement the onOk action and
      // make the OK button default
      m_commandPanel = new UTStandardCommandPanel(this, "",
         SwingConstants.HORIZONTAL)
      {
         public void onOk()
         {
            BackendTankPropertyDialog.this.onOk();
         }
      };
      getRootPane().setDefaultButton(m_commandPanel.getOkButton());

      JPanel cmdPanel = new JPanel(new BorderLayout());
      cmdPanel.add(m_commandPanel, BorderLayout.EAST);

      JPanel panel = new JPanel(new BorderLayout());
      panel.setBorder(new EmptyBorder(5, 5, 5, 5));
      panel.add(createViewPanel(), "Center");
      panel.add(cmdPanel, "South");

      getContentPane().setLayout(new BorderLayout());
      getContentPane().add(panel);

      // initialize validation constraints
      m_validatedComponents[0] = m_datasource;
      m_validationConstraints[0] = new StringConstraint();
      m_validatedComponents[1] = m_table;
      m_validationConstraints[1] = new StringConstraint();
      setValidationFramework(m_validatedComponents, m_validationConstraints);
      pack();
      setResizable(true);
   }

  
   /**
    * Overriding PSDialog.onOk() method implementation.
    */
   public void onOk()
   {
      String defaultText = E2Designer.getResources().getString("Dummy");
      String newAlias = m_alias.getText();

      if (newAlias.equals("") || newAlias.equals(defaultText))
      {
         JOptionPane.showMessageDialog(null, getResources().getString(
            "enterAnAlias"));
         m_alias.requestFocus();
         return;
      }

      boolean bFound = false;

      // BackendTankPropertyDialog dlg = getDialog();
      Vector existingTables = this.getTables();
      // System.out.println("Existing: " + existingTables.toString());
      if (existingTables != null)
      {
         for (int iTable = 0; iTable < existingTables.size(); ++iTable)
         {
            if (newAlias.equals(existingTables.get(iTable).toString()))
            {
               bFound = true;
               break;
            }
         }
      }

      if (bFound)
      {
         JOptionPane.showMessageDialog(null, getResources().getString(
            "UniqueTableMsg"), getResources().getString("UniqueTableTitle"),
            JOptionPane.ERROR_MESSAGE);
         return;
      }
      else
      {
         if (m_tank.isReadOnly())
         {
            try
            {
               m_tank.setAlias(newAlias);
            }
            catch (Exception e)
            {
               e.printStackTrace();
            }
         }
         else
         {
            if (activateValidation())
            {
               try
               {
                  m_tank.setAlias(m_alias.getText());
                  m_tank.setDataSource(m_datasource.getSelectedDatasource());
                  m_tank.setTable(m_table.getText());

                  m_bModified = true;

               }
               catch (Exception e)
               {
                  e.printStackTrace();
               }
            }
            else
               // failed validation
               return;
         }

         dispose();
      }
   }
  


   // implementation of IEditor
  public boolean onEdit(UIFigure figure, final Object data)
   {
      if (figure == null)
      {
         // System.out.println("...figure not valid");
         return false;
      }

      if (figure.getData() instanceof OSBackendTable)
      {
         m_tank = (OSBackendTable) figure.getData();

         if (m_tank.isReadOnly())
         {
            m_datasource.setEnabled(false);
            m_table.setEnabled(false);
         }

         m_datasource.setSelectedDatasource(m_tank.getDataSource());

         if (m_tank.getTable() != null)
            m_table.setText(m_tank.getTable());

         if (m_tank.getAlias() != null)
            m_alias.setText(m_tank.getAlias());

         this.center();
         this.setVisible(true);
      }
      else
         throw new IllegalArgumentException(getResources().getString(
            "datatypeError"));

      return m_bModified;
   }

   public OSBackendTable getData()
   {
      return m_tank;
   }

   public Vector getTables()
   {
      return m_existingTables;
   }

   public void setTables(Vector tables)
   {
      m_existingTables = tables;
   }
  
   /**
    * the backend tank
    */
   private OSBackendTable m_tank = null;

   /**
    * the table name
    */
   private UTFixedTextField m_table = new UTFixedTextField("");

   /**
    * The datasource combo-box
    */
   private DatasourceComboBox m_datasource = new DatasourceComboBox();

   /**
    * the alias name
    */
   private UTFixedTextField m_alias = new UTFixedTextField("");

   /**
    * the standard command panel
    */
   private UTStandardCommandPanel m_commandPanel = null;

   /**
    * the dialog size
    */
   private final static Dimension DIALOG_SIZE = new Dimension(400, 240);

   private boolean m_bModified = false;

   /*
    * the validation framework variables
    */
   // ////////////////////////////////////////////////////////////////////////////
   private static final int NUM_COMPONENTS_VALIDATED = 2;

   private final Component m_validatedComponents[] = new Component[NUM_COMPONENTS_VALIDATED];

   private final ValidationConstraint m_validationConstraints[] = new ValidationConstraint[NUM_COMPONENTS_VALIDATED];

   /**
    * Used to make sure this table has a unique name
    */
   private Vector m_existingTables = new Vector();
}



 
