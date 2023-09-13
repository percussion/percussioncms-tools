/******************************************************************************
 *
 * [ PSDeploymentInstallSummaryDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.deployer.ui;

import com.percussion.deployer.objectstore.PSDependencyTreeContext;
import com.percussion.deployer.objectstore.PSDeployableElement;
import com.percussion.deployer.objectstore.PSImportDescriptor;
import com.percussion.deployer.objectstore.PSImportPackage;
import com.percussion.deployer.objectstore.PSValidationResults;
import com.percussion.guitools.ErrorDialogs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * The dialog to show the summary of user selections or informed details
 * of installing packages.
 */
public class PSDeploymentInstallSummaryDialog extends PSDeploymentWizardDialog
{
   /**
    * Constructs this dialog calling {@link 
    * PSDeploymentWizardDialog#PSDeploymentWizardDialog(Frame, 
    * PSDeploymentServer, int, int) super(parent, server, step, sequence)}.
    */
   public PSDeploymentInstallSummaryDialog(Frame parent, 
      PSDeploymentServer deploymentServer, int step, int sequence)
   {
       super(parent, deploymentServer, step, sequence);      
       initDialog();
   }

   /**
    * Initializes  the dialog framework.
    */
   protected void initDialog()
   {
      setTitle(getResourceString("title"));
      JPanel mainPane = new JPanel();
      mainPane.setLayout(new BoxLayout(mainPane,BoxLayout.Y_AXIS ));
      mainPane.setPreferredSize(new Dimension(700, 550));

      JPanel topRightPane = new JPanel();
      topRightPane.setLayout(new BoxLayout(topRightPane, BoxLayout.X_AXIS ));
      topRightPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

      m_packageList = new JList();
      m_packageList.addListSelectionListener(new PackageListListener());
      JScrollPane sp = new JScrollPane(m_packageList);

      m_viewDepBtn = new JButton(getResourceString("viewDep"));
      m_viewDepBtn.setMnemonic(getResourceString("viewDep.mn").charAt(0));
      m_viewDepBtn.setEnabled(false);
      m_viewDepBtn.addActionListener(new DependencyListener());
      m_viewDepBtn.setAlignmentY(Component.TOP_ALIGNMENT);
      sp.setAlignmentY(Component.TOP_ALIGNMENT);
      topRightPane.add(sp);
      topRightPane.add(Box.createRigidArea(new Dimension(25,0)));
      topRightPane.add(m_viewDepBtn);

      JPanel topLeftPane = new JPanel();
      topLeftPane.setLayout(new BoxLayout(topLeftPane, BoxLayout.X_AXIS ));
      topLeftPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
      
      String labelStr = getResourceString("pkgLabel");
      char mn = getResourceString("pkgLabel.mn").charAt(0);
      JLabel lb = new JLabel(labelStr);
      lb.setDisplayedMnemonic(mn);
      lb.setDisplayedMnemonicIndex(labelStr.indexOf(mn));
      
      topLeftPane.add(lb);

      JPanel topPane = new JPanel();
      topPane.setLayout(new BorderLayout());
      topPane.add(topLeftPane,BorderLayout.WEST);
      topPane.add(topRightPane,BorderLayout.CENTER);

      JPanel tablePanel = new JPanel();
      tablePanel.setPreferredSize(new Dimension(550, 400));
      tablePanel.setLayout(new BorderLayout());
      tablePanel.setBorder( BorderFactory.createCompoundBorder(
         BorderFactory.createTitledBorder(getResourceString("waLabel")),
         BorderFactory.createEmptyBorder(0,10,10,10)) );
      m_filterPanel = new PSValidationFilterPanel();
      m_filterPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
      tablePanel.add(m_filterPanel, BorderLayout.NORTH);
      
      m_table = new JTable();
      lb.setLabelFor(m_table);
      JScrollPane tableSp = new JScrollPane(m_table);
      tablePanel.add(tableSp, BorderLayout.CENTER);

      JPanel ancestorPane = new JPanel();
      ancestorPane.setLayout(new BoxLayout(ancestorPane, BoxLayout.X_AXIS));
      ancestorPane.setBorder(BorderFactory.createEmptyBorder(0,10,10,10)) ;

      m_ancestorList = new JList();
      JScrollPane anscesSp = new JScrollPane(m_ancestorList);
      anscesSp.setBorder(BorderFactory.createCompoundBorder(
         BorderFactory.createTitledBorder(getResourceString("ancestorLabel")),
         BorderFactory.createEmptyBorder(0,10,10,10)));

      ancestorPane.add(anscesSp);
      ancestorPane.add(Box.createHorizontalGlue());

      mainPane.setBorder( BorderFactory.createEmptyBorder(0,10,10,10));
      mainPane.add(topPane);
      mainPane.add(tablePanel);
      mainPane.add(ancestorPane);
      mainPane.add(createCommandPanel(false));
      getContentPane().add(mainPane);
      setResizable(true);
      pack();
      center();
   }

   /**
    * Gets all packages to install from import descriptor and updates
    * 'Packages to install' list box with them.
    */
   protected void init()
   {
      PSImportDescriptor impDesc = (PSImportDescriptor)m_descriptor;
      m_importList = impDesc.getImportPackageList();

      // if ancestor validation was skipped, set a message and disable the list
      if (!impDesc.isAncestorValidationEnabled())
      {
         m_ancestorList.setListData(new Object[] {getResourceString(
            "noAncestorValidation")});
         m_ancestorList.setEnabled(false);
      }
      // populate package list in top right pane and create result models.
      PSImportPackage impPackage = null;
      String packageId;
      int packages = m_importList.size();
      List packageNames = new ArrayList();
      m_valResultModels = new PSValidationResultsModel[packages];
      for(int k = 0; k < packages; k++)
      {
         impPackage = (PSImportPackage)m_importList.get(k);
         packageId = impPackage.getPackage().getDisplayIdentifier();
         packageNames.add(packageId);

         PSValidationResults results = impPackage.getValidationResults();
         m_valResultModels[k] = new PSValidationResultsModel(results, false);
         m_filterPanel.addValidationResults(m_valResultModels[k]);
      }
      
      m_packageList.setListData(packageNames.toArray());
      m_packageList.setSelectedIndex(0);
   }

   /**
    * Listener for viewing dependency tree of selected package in 'Packages to
    * install' list.
    */
   private class DependencyListener implements ActionListener
   {
      /**
       * Displays a tree view of dependencies of the package selected to install
       * in 'Package Dependencies' dialog when "View Dependencies" button is
       * clicked.
       *
       * @param evt Set by swing model, Never <code>null</code>.
       */
      public void actionPerformed(ActionEvent evt)
      {
         if (m_packageList.getSelectedIndex() != -1)
         {
            if (m_treeDialog == null)
            {
               m_treeDialog = new JDialog(PSDeploymentInstallSummaryDialog.this);
               m_treeDialog.setTitle(getResourceString("depViewer"));
               m_treeDialog.setModal(true);
               m_treeDialog.setResizable(true);
               Dimension screenSize =
                     Toolkit.getDefaultToolkit().getScreenSize();
               Dimension size = getSize();
               m_treeDialog.setLocation((screenSize.width - size.width )/2,
                                        (screenSize.height - size.height)/2);
               m_treePanel = new JPanel(new BorderLayout());
               m_treePanel.setBorder( BorderFactory.createEmptyBorder(
                     10,10,10,10));
               m_treeScrollPane = new JScrollPane();
               m_treeScrollPane.setPreferredSize(new Dimension(300, 250));
               m_treePanel.add(m_treeScrollPane, BorderLayout.CENTER);
               JButton close = new JButton(getResourceString("close"));
               close.setMnemonic(getResourceString("close.mn").charAt(0));
               close.addActionListener(new ActionListener()
               {
                  public void actionPerformed(ActionEvent e)
                  {
                     m_treeDialog.setVisible(false);
                  }
               });
               JPanel cmdPanel = new JPanel(new BorderLayout());
               cmdPanel.add(close, BorderLayout.EAST);
               cmdPanel.setBorder(new EmptyBorder(8,8,8,8));
               m_treePanel.add(cmdPanel, BorderLayout.SOUTH);
               m_treeDialog.getContentPane().add(m_treePanel);
            }
            m_treeScrollPane.setViewportView(m_depTree);
            m_treeDialog.pack();
            m_treeDialog.setVisible(true);
         }
      }
   }

   /**
    * Listener responding to selection in <code>m_packageList</code>.
    * Based on the package selection, <code>m_table</code> displays validation
    * results and <code>m_ancestorList</code> displays list of
    * absent ancestors for  that package.
    */
   private class PackageListListener implements ListSelectionListener
   {
      /**
       * Responds to selection events in 'Packgaes to install' list.
       *
       * @param e Set by swing model, Never <code>null</code>.
       */
      public void valueChanged(ListSelectionEvent e)
      {
         int seIndex = m_packageList.getSelectedIndex();
         if (seIndex != -1)
         {
            m_viewDepBtn.setEnabled(true);
            PSImportPackage impPackage =
                  (PSImportPackage)m_importList.get(seIndex);
            PSDeployableElement depElem = impPackage.getPackage();
            m_depTree = new PSDependencyTree(depElem, false, 
               new PSDependencyTreeContext());

            // set model on table
            PSValidationResults results = impPackage.getValidationResults();
            PSValidationResultsModel model = m_valResultModels[seIndex];
            m_table.setModel(model);
         }
      }
   }

   /**
    * Confirms with user to continue with the installation and disposes the tree
    * dialog if it is initialized.
    */
   public void onNext()
   {
      int option = JOptionPane.showConfirmDialog(this, 
         ErrorDialogs.cropErrorMessage(
         MessageFormat.format(getResourceString("finishWarning"), 
         new String[] {m_deploymentServer.getServerName()}) ), 
         getResourceString("warning"), JOptionPane.YES_NO_OPTION);

      if(option == JOptionPane.YES_OPTION)
      {      
         if (m_treeDialog != null)
            m_treeDialog.dispose();
                                   
         super.onNext();
      }
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
      if (m_treeDialog != null)
         m_treeDialog.dispose();
      super.onBack();
   }
   
   /**
    * Call's super's <code>onCancel</code> to confirm with user to cancel and 
    * disposes tree dialog if user chooses to cancel the installation and the 
    * tree dialog is initialized.
    */
   public void onCancel()
   {
      super.onCancel();
      
      if(!isShowing())
      {
         if (m_treeDialog != null)
            m_treeDialog.dispose();
      }
   }

   /**
    * Table dispalying validation results for a package selected for
    * installation. Initialised in <code>initDialog()</code> and never
    * <code>null</code> or modified after that.
    */
   private JTable m_table;

   /**
    * Displays packages to be installed. Initialised in <code>initDialog()
    * </code> and never <code>null</code> or modified after that.
    */
   private JList  m_packageList;

   /**
    * Displays a list of absent ancestors for a package to be installed.
    * Initialised in <code>initDialog()</code> and never <code>null</code> after 
    * that.  Disabled if ancestor validation was skipped.  Otherwise "absent
    * ancestors" are set on this list in 
    * {@link PackageListListener#valueChanged(ListSelectionEvent)}.
    */
   private JList m_ancestorList;

   /**
    * Represents the deployable element/package with its dependencies
    * and ancestors in a tree format. Initialised in
    * <code>PackageListListener#valueChanged(ListSelectionEvent)</code>
    * and never <code>null</code> after that.
    */
   private PSDependencyTree m_depTree;

   /**
    * Dialog contianing <code>m_treeScrollPane</code>. Initialised in
    * <code>DependencyListener#actionPerformed(ActionEvent)</code>
    * and never <code>null</code> after that.
    */
   private JDialog m_treeDialog;

   /**
    * Scroll pane containing <code>PSDependencyTree</code>. Initialised in
    * <code>DependencyListener#actionPerformed(ActionEvent)</code>
    * and never <code>null</code> after that.
    */
   private JScrollPane m_treeScrollPane;

   /**
    * List of packages to be imported. Initialised in
    * <code>init()</code> and never <code>null</code> after that.
    */
   private List m_importList;
   
   /**
    * List of validation results models, each corresponding to the same index
    * into {@link #m_importList}, initialized {@link #init()}, never
    * <code>null</code> after that.  Models are lazily instantiated and set
    * when items in the {@link #m_packageList} are selected.
    */
   private PSValidationResultsModel[] m_valResultModels;

   /**
    * Panel dispalying the <code>m_treeScrollPane</code> and a 'Close' JButton.
    * Initialised in (@link DependencyListener#actionPerformed(ActionEvent)),
    * never <code>null</code> after that.
    */
   private JPanel m_treePanel;

   /**
    * Button for viewing dependency tree for packages selected selected from
    * 'Packages to Install' list. Initialised in <code>initDialog()</code>,
    * never <code>null</code> after that.
    */
   private JButton m_viewDepBtn;   
   
   /**
    * Panel containing filter combo.  Created during initialization, each
    * validation table model is added to this panels data as the list panel
    * contents are built.  Never <code>null</code> after that.
    */
   private PSValidationFilterPanel m_filterPanel;
}
