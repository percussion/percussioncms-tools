/******************************************************************************
 *
 * [ PSDeploymentDependencyValidationsDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.deployer.ui;

import com.percussion.deployer.objectstore.PSImportDescriptor;
import com.percussion.deployer.objectstore.PSImportPackage;
import com.percussion.deployer.objectstore.PSValidationResult;
import com.percussion.deployer.objectstore.PSValidationResults;
import com.percussion.guitools.ErrorDialogs;
import com.percussion.guitools.PSJTableWithTooltips;
import com.percussion.guitools.PSListPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * The dialog to display validation results of each package for installation
 * and to allow user to choose the packages/dependencies to install.
 */
public class PSDeploymentDependencyValidationsDialog  extends
   PSDeploymentWizardDialog
{
   /**
    * Constructs this dialog calling {@link 
    * PSDeploymentWizardDialog#PSDeploymentWizardDialog(Frame, 
    * PSDeploymentServer, int, int) super(parent, server, step, sequence)}.
    */
   public PSDeploymentDependencyValidationsDialog(Frame parent,
      PSDeploymentServer server, int step, int sequence)
   {
      super(parent, server, step, sequence);
      initDialog();
   }

   /**
    * Initializes  the dialog framework.
    */
   protected void initDialog()
   {
      JPanel mainPane = new JPanel();
      mainPane.setLayout(new BorderLayout());
      EmptyBorder emptyBorder = new EmptyBorder(10, 10, 10, 10);
      mainPane.setBorder(emptyBorder);


      //load description
      setTitle(getResourceString("title"));
      int steps = 1;
      try
      {
         steps = Integer.parseInt(getResourceString("descStepCount"));
      }
      catch (NumberFormatException ex)
      {
         //uses the default
      }
      String[] description = new String[steps];
      for (int i = 1; i <= steps; i++)
      {
         description[i-1] = getResourceString("descStep" + i);
      }

      JPanel descPanel = createDescriptionPanel(
         getResourceString("descTitle"), description);
      // create command panel
      JPanel cPanel = createCommandPanel(true);
      // create filter panel
      m_filterPanel = new PSValidationFilterPanel();
      m_filterPanel.setBorder(new EmptyBorder(10,10,0,0));
      m_filterPanel.addChangeListener(new ChangeListener() {

         public void stateChanged(ChangeEvent e)
         {
            // remove any panels that display no results
            onFilterChange();
         }});
      
      //create list panel
      m_listPanel = new PSListPanel(getResourceString("packagesLabel"), 
         PSListPanel.ALIGN_RIGHT, false);
      m_listPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
      m_listPanel.setEmptyListMessage(getResourceString("noMessages"));
      

      // create container for filter and list panels
      JPanel midPanel = new JPanel(new BorderLayout());
      midPanel.add(m_filterPanel, BorderLayout.NORTH);
      midPanel.add(m_listPanel, BorderLayout.CENTER);
            
      mainPane.add(descPanel, BorderLayout.NORTH);
      mainPane.add(midPanel, BorderLayout.CENTER);
      mainPane.add(cPanel, BorderLayout.SOUTH);

      getContentPane().add(mainPane);
      setResizable(true);
      pack();
   }

   /**
    * Handles changes in the filter selection, showing only those panels that
    * have results.
    */
   protected void onFilterChange()
   {
      // walk all panels, check model's row count, hide any that are empty,
      // show any that have rows.  Get both iterators first so the contents
      // don't change as we hide and show panels.
      Iterator shownPanels = m_listPanel.getPanels();
      Iterator hiddenPanels = m_listPanel.getHiddenPanels();
      while (hiddenPanels.hasNext())
      {
         ValidationPanel panel = (ValidationPanel) hiddenPanels.next();
         if (panel.getModel().hasVisibleData())
            m_listPanel.showPanel(panel);
      }
      
      while (shownPanels.hasNext())
      {
         ValidationPanel panel = (ValidationPanel) shownPanels.next();
         if (!panel.getModel().hasVisibleData())
            m_listPanel.hidePanel(panel);
      }
   }

   /**
    * Populates the table model with <code>PSValidationResult</code> objects,
    * each such object represent a row in the table and are contained in <code>
    * PSValidationResults</code> object. Creates the panel with 'Define Actions'
    * label and 'Define Actions' table enclosed in scroll pane, 'Skip this
    * package'checkbox and a label displaying if there have been any validation
    * errors.
    * @param results assumed not <code>null</code> or empty.
    * @param index indicates the order of the package to install.
    * @return the panel, never <code>null</code>.
    */
   private JPanel createActionsPanel(PSValidationResults results,
      int index)
   {
      //main panel cotaining tablePanel and cblPanel
      ValidationPanel mainPane = new ValidationPanel();
      mainPane.setLayout(new BoxLayout(mainPane, BoxLayout.Y_AXIS));
      mainPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

      //panel containing checkbox and label
      JPanel cblPanel = new JPanel();
      cblPanel.setLayout(new BoxLayout(cblPanel, BoxLayout.X_AXIS));
      JCheckBox chk =  new JCheckBox(getResourceString("skipPackage"));
      chk.setMnemonic(getResourceString("skipPackage.mn").charAt(0));
      cblPanel.add(chk);

      Iterator itr = results.getResults();
      //If skipping is allowed then the default is to show the 'Install'
      //checkbox as selected.
      while(itr.hasNext())
      {
         PSValidationResult r = (PSValidationResult)itr.next();
         if (r.allowSkip())
            r.skipInstall(false);
      }

      JPanel tablePanel = new JPanel();
      tablePanel.setLayout(new BorderLayout());

      CheckPackage chkPkg = new CheckPackage();

      //panel containing table in a scroll pane
      chkPkg.setErrWng(true);
      tablePanel.setBorder( BorderFactory.createCompoundBorder(
         BorderFactory.createTitledBorder(getResourceString("actionTitle")),
         BorderFactory.createEmptyBorder(10,10,10,10)) );
      tablePanel.setAlignmentX(LEFT_ALIGNMENT);
      
      //table creation
      PSValidationResultsModel model =
            new PSValidationResultsModel(results, true);
      m_filterPanel.addValidationResults(model);
      mainPane.setModel(model);
      
      PSJTableWithTooltips table = new PSJTableWithTooltips(model);
      table.getColumn(PSValidationResultsModel.ms_actionColumn).
            setCellRenderer(new ActionColumnRenderer());
      DefaultCellEditor editor =
         new DefaultCellEditor(new JCheckBox(getResourceString("install")))
      {
         public Component getTableCellEditorComponent(JTable table,
          Object value, boolean isSelected, int row, int column)
         {
            super.getTableCellEditorComponent(table, value,
              isSelected, row, column);
            return editorComponent;
         }
      };

      table.getColumn(
      PSValidationResultsModel.ms_actionColumn).setCellEditor(editor);

      JScrollPane scrollPane = new JScrollPane(table);
      scrollPane.setPreferredSize(new Dimension (500, 125));
      tablePanel.add(scrollPane);
      
      chkPkg.setIndex(index);
      m_chkPkgObjList.add(chkPkg);

      cblPanel.setAlignmentX(LEFT_ALIGNMENT);
      tablePanel.setAlignmentX(LEFT_ALIGNMENT);

      JLabel label = new JLabel();
      //check for errors to display error message and disable the checkbox
      // with 'Skip this package' label.
      if (results.hasErrors())
      {
         label.setText(getResourceString("packageError"));
         chk.setSelected(true);
         chk.setEnabled(false);
         chkPkg.setSkip(true);
      }
      else
         label.setText("");
      label.setForeground(Color.black);
      cblPanel.add(label);

      //add the item listener at the end, so that it does not get fired when
      //the selections are made to the check-box when errors happened.
      chk.addItemListener(new SkipCheckBoxListener());
            
      mainPane.add(tablePanel);
      mainPane.add(cblPanel);
      return mainPane;
   }

   /**
    * Dialog is updated with import packages.
    */
   protected void init()
   {
      PSImportDescriptor impDesc = (PSImportDescriptor)m_descriptor;
      m_importList = impDesc.getImportPackageList();

      PSImportPackage impPackage = null;
      //indicates package name on the tab
      String packageId;
      int packages = m_importList.size();
      //populate tables within the tabs.
      for(int k = 0; k < packages; k++)
      {
         impPackage = (PSImportPackage)m_importList.get(k);
         packageId = impPackage.getPackage().getDisplayIdentifier();
         PSValidationResults rs = impPackage.getValidationResults();
         if (rs != null && rs.getResults().hasNext())
            m_listPanel.addPanel(packageId, createActionsPanel(rs, k));
      }
      pack();
      center();
      setResizable(true);
   }

   /**
    * Checks the following conditions, displays appropriate messages and acts 
    * upon user actions.
    * <ol>
    * <li>The list of packages to install may not be empty. (Error)</li>
    * <li>Checks installing packages has any warnings. (Confirmation message)
    * </li>
    * <li>Checks any skipped packages are required for the installing packages.
    * (Confirmation message)</li>
    * </ol>
    * If user continues with the installation, updates the import descriptor and
    * hides the dialog by calling super's <code>onNext()</code>.
    */
   public void onNext()
   {
//      /**
//       * Here </code>m_chkPkgObjList</code> is checked if the packages have been
//       * skipped or not. If not skipped then they are checked for warnings, if
//       * there is warning error dialog is displayed before continuing any
//       * further. If all the packages are skipped then an error dialog is
//       * displayed. <code>CheckPackage</code> maintains the status of each
//       * packages displayed in the tabbed pane, as to if they contain warning,
//       * if they have been skipped and their index.
//       * If evrything is fine then all the skipped packages are removed from the
//       * <code>m_importList</code>.
//       */
//      boolean hasPackagesWithWarnings = false;
//      int size = m_chkPkgObjList.size();
//      int skpPkg = 0;
//      for(int k = 0; k < size; k++)
//      {
//         CheckPackage c = (CheckPackage)m_chkPkgObjList.get(k);
//         if(c.m_skip)
//            skpPkg++;
//         else if(!hasPackagesWithWarnings && c.hasErrWng())         
//            hasPackagesWithWarnings = true;
//      }      
//      //If all packages are skipped give an error message that they can not skip
//      //all
//      if (skpPkg == m_importList.size())
//      {
//         ErrorDialogs.showErrorMessage(this,
//            getResourceString("listEmptyErr"),
//            getResourceString("listEmptyTitle"));
//         return;
//      }
//      
//      //Found some packages to install that has warnings, so just give a warning
//      if(hasPackagesWithWarnings)
//      {      
//         String cropMsg = ErrorDialogs.cropErrorMessage(
//            getResourceString("onNextWarning"));
//         int option = JOptionPane.showOptionDialog(this, cropMsg,
//            getResourceString("onNextWarningTitle"),
//            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, 
//            null, null);
//            
//         if (option == JOptionPane.NO_OPTION)
//            return;
//      }
//      
//      //Get all skipped packages that are actually required for other packages.
//      Iterator iter = m_chkPkgObjList.iterator();
//      Set parentPackages = new TreeSet(String.CASE_INSENSITIVE_ORDER);
//      List skippedDepPackages = new ArrayList();
//      while(iter.hasNext())
//      {
//         CheckPackage c = (CheckPackage)iter.next();
//         if (c.skip())
//         {
//            PSDeployableElement element = ((PSImportPackage)m_importList.get(
//               c.getindex())).getPackage();
//               
//            if(PSDeploymentWizardDialog.isDependencyPackage(
//               (PSImportDescriptor)getDescriptor(), 
//               element, parentPackages))
//            {
//               skippedDepPackages.add(element.getDisplayIdentifier());
//            }
//         }
//      }
//      
//      //display warning message if there are skipped packages that are actually
//      //required by other packages for proper installation.
//      if(!skippedDepPackages.isEmpty())
//      {
//         String[] args = new String[] {skippedDepPackages.toString(), 
//            parentPackages.toString()};
//         int option = JOptionPane.showConfirmDialog(this, 
//            ErrorDialogs.cropErrorMessage( MessageFormat.format(
//            getResourceString("reqPacksSkipWarning"), args) ), 
//            getResourceString("onNextWarningTitle"), JOptionPane.YES_NO_OPTION, 
//            JOptionPane.INFORMATION_MESSAGE);
//         
//         if(option == JOptionPane.NO_OPTION)
//            return;
//      }
//
//      skpPkg = m_chkPkgObjList.size();
//      int shiftFactor = 0;
//      for(int z = 0; z < skpPkg; z++)
//      {
//         CheckPackage c = (CheckPackage)m_chkPkgObjList.get(z);
//         if (c.skip())
//         {
//            m_importList.remove(c.getindex() - shiftFactor);
//            shiftFactor++;
//         }
//      }
      super.onNext();
   }

   // see base class
   public Object getDataToSave()
   {
      return null;
   }

   // see base class
   public void onBack()
   {
      setShouldUpdateUserSettings(false);
      super.onBack();
   }
    /**
    * List of <code>PSImportPackage</code> objects. Initialised in <code>init()
    * </code>. Never <code>null</code> or empty. It's being modified in <code>
    * onNext()</code>, packages which have errors or skipped by users.
    */
   private List m_importList;

   /**
    * The list panel initialized in <code>initDialog()</code>. Never
    * <code>null</code> after that.  Panels are added to it in
    * (@link #createActionsPanel(PSValidationResults, int)) based on the number
    * of packages got from <code>m_importList</code>.
    */
   private PSListPanel m_listPanel;

   /**
    * Responds to checkbox events in the panel for each package.
    */
   private class SkipCheckBoxListener implements ItemListener
   {
      /**
       * Updates <code>CheckPackage</code> object based on the selection of
       * checkbox with 'Skip this package' label.
       * If selected then the list above mentioned updates to remove the package
       * or else package is included.
       * @param e the item state change event, assumed not to be <code>null
       * </code> as this method is called by Swing model when the item state 
       * change event occurs.
       */
      public void itemStateChanged(ItemEvent e)
      {
         int index = m_listPanel.getSelectedIndex();
         //no selected tab, should not happen after initialization         
         if(index == -1) 
            return;         
         CheckPackage c = (CheckPackage)m_chkPkgObjList.get(index);
         if (e.getStateChange() == ItemEvent.SELECTED)
         {
            if(shouldSkipPackage(index))
               c.setSkip(true);
            else //make the check-box as not selected
               ((JCheckBox)e.getSource()).setSelected(false);
         }
         else
            c.setSkip(false);
      }
   }
   
   /**
    * Checks whether the package represented by the tabbed pane at the specified
    * index should be skipped or not. A package can be skipped if any other 
    * package getting installed does not depend on this package. Displays a 
    * confirmation warning message to skip or not if some other packages depend 
    * on this package.
    * 
    * @param index the current selected tab index, 
    * 
    * @return <code>true</code> if no other package depends on this package or 
    * if user chooses to skip even though some other packages depend on this.
    */
   private boolean shouldSkipPackage(int index)
   {
      boolean shouldSkip = true;
      
      PSImportPackage pkg = (PSImportPackage)m_importList.get(index);
      Set parentPackages = new TreeSet();
      if(PSDeploymentWizardDialog.isDependencyPackage(
         (PSImportDescriptor)getDescriptor(), pkg.getPackage(), parentPackages))
      {
         String[] args = new String[] {pkg.getPackage().toString(), 
            parentPackages.toString()};            
         int option = JOptionPane.showConfirmDialog(this, 
            ErrorDialogs.cropErrorMessage( MessageFormat.format(
            getResourceString("packageSkipWarning"), args) ), 
            getResourceString("onNextWarningTitle"), JOptionPane.YES_NO_OPTION, 
            JOptionPane.INFORMATION_MESSAGE);
         
         if(option == JOptionPane.NO_OPTION)
            shouldSkip = false;
      }
      
      return shouldSkip;
   }

   /**
    * Implements the interface which defines the method required by any object
    * that would like to be a renderer for cells in a JTable.
    */
   private class ActionColumnRenderer implements TableCellRenderer
   {
      /**
       * Returns JCheckBox as the renderer if <code>value</code> is an instance
       * of Boolean or else a label displaying <code>value</code> as a String.
       * All the input params are called by swing model so they are valid.
       */
      public Component getTableCellRendererComponent(JTable table, Object value,
         boolean isSelected, boolean hasFocus, int row, int column)
      {
         if (value instanceof Boolean) //action column
         {
            JCheckBox chk = new JCheckBox(getResourceString("install"));
            //if skip is false, install is true, set the check-box appropriately.
            chk.setSelected(!((Boolean)value).booleanValue());
            if(isSelected)
            {
               chk.setBackground(UIManager.getColor("Table.selectionBackground"));
               chk.setForeground(UIManager.getColor("Table.selectionForeground"));
            }
            else
            {
               chk.setBackground(UIManager.getColor("Table.textBackground"));
               chk.setForeground(UIManager.getColor("Table.textForeground"));
            }
            return chk;
         }
         else
         {
            String text = "";
            if(value != null)
               text = value.toString();
            return new JLabel(text);
         }
      }
   }

   /**
    * Contains list of <code>CheckPackage</code> objects corresponding to number
    * of tabs in the tabbed pane <code>m_tabbedPanel</code> used to display
    * warning dialog if an attempt is made to save a package containing errors
    * or warnings. Modified in {@link #createActionsPanel(
    * PSValidationResults, int)}, never <code>null</code>, may be empty.
    */
   private List m_chkPkgObjList = new ArrayList();
   
   /**
    * Panel containing filter combo.  Created during initialization, each
    * validation table model is added to this panels data as the list panel
    * contents are built.  Never <code>null</code> after that.
    */
   private PSValidationFilterPanel m_filterPanel;

   /**
    * Whether or not a package is going to be skipped and if it contains errors
    * or warnings is encapsulated here.
    */
   class CheckPackage
   {
      /**
       * Sets the condition for skipping the package.
       * @param skip, If <code>true</code> package will be skipped else not.
       */
      public void setSkip(boolean skip)
      {
         m_skip = skip;
      }

      /**
       * Sets to <code>true</code> if the package has errors or warnings.
       * @param errWrng, If <code>true</code> then the package has errors or
       * warnings.
       */
      public void setErrWng(boolean errWrng)
      {
         m_errWrng = errWrng;
      }

      /**
       * Sets the index of this object. This index must match with the
       * corresponding package in the <code>m_importList</code>, becuase it
       * indicates if that package has been skipped or not and based on that
       * <code>m_importList</code> is updated in <code>onNext()</code>.
       *
       * @param index of this object.
       */
      public void setIndex(int index)
      {
         m_index = index;
      }

      /**
       * Gets the index of this object. See {@link #setIndex(int) set(index)}
       * for index description.
       *
       * @return index of this object
       */
      public int getindex()
      {
         return m_index;
      }

      /**
       * Specifies if the package will be skipped or not.
       * @return If <code>true</code> then the package will be skipped else not.
       */
      public boolean skip()
      {
         return m_skip;
      }

      /**
       * Specifies if the package has error or warnings.
       * @return If <code>true</code> then the package has errors or
       * warnings else not.
       */
      public boolean hasErrWng()
      {
         return m_errWrng;
      }

      /**
       * Specifies if the package will be skipped. If <code>true</code> then
       * the package will be skipped else not.
       */
      private boolean m_skip;

      /**
       * Specifies if the package has error or warnings. If <code>true</code>
       * then the package has errors or warnings else not.
       */
      private boolean m_errWrng;

      /**
       * Specifies the index of this object, it's index must match exactly with
       * the  index of corresponding package to be installed in  <code>
       * m_importList</code>.
       */
      private int m_index;
   }
   
   /**
    * Panel to hold validation results table, provides accessors for the table
    * model
    */
   private class ValidationPanel extends JPanel
   {
      /**
       * Set the model the table on this panel will use.
       * 
       * @param model The model, assumed not <code>null</code>.
       */
      public void setModel(PSValidationResultsModel model)
      {
         m_model = model;
      }
      
      /**
       * Get the model set by {@link #setModel(PSValidationResultsModel)}.
       * 
       * @return The model, may be <code>null</code> if not set.
       */
      public PSValidationResultsModel getModel()
      {
         return m_model;
      }
      
      /**
       * The model, may be <code>null</code>.
       */
      private PSValidationResultsModel m_model = null;
   }
}
