/******************************************************************************
 *
 * [ PSLogSummaryDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.deployer.ui;

import com.percussion.UTComponents.UTFixedButton;
import com.percussion.deployer.catalog.PSCatalogResultSet;
import com.percussion.deployer.objectstore.PSArchiveInfo;
import com.percussion.deployer.objectstore.PSArchiveSummary;
import com.percussion.deployer.objectstore.PSDependencyTreeContext;
import com.percussion.deployer.objectstore.PSDeployableElement;
import com.percussion.deployer.objectstore.PSLogDetail;
import com.percussion.deployer.objectstore.PSLogSummary;
import com.percussion.deployer.objectstore.PSTransactionLogSummary;
import com.percussion.deployer.objectstore.PSValidationResults;
import com.percussion.guitools.ErrorDialogs;
import com.percussion.guitools.PSDialog;
import com.percussion.utils.collections.PSIteratorUtils;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Iterator;

/**
 * Dialog showing the summary of the selected package deployment log.
 */
public class PSLogSummaryDialog  extends PSDialog
{
   /**
    * Constructs this dialog with required parameters.
    *
    * @param parent the parent window of this dialog, may be <code>null</code>.
    * @param logSummary  the log summary for the selected package,  may not be
    * <code>null</code>.
    * @param types the list of element types, may not be <code>null</code> or
    * empty.
    *
    * @throws IllegalArgumentException if any of the parameters are invalid.
    */
   public PSLogSummaryDialog(Frame parent, PSLogSummary logSummary,
      PSCatalogResultSet types)
   {
      super(parent);
      validateAndInitializeDialog(logSummary, types);
   }

   /**
    * Constructs this dialog with required parameters.
    *
    * @param parent the parent window of this dialog, may be <code>null</code>.
    * @param logSummary  the log summary for the selected package,  may not be
    * <code>null</code>.
    * @param types the list of element types, may not be <code>null</code> or
    * empty.
    *
    * @throws IllegalArgumentException if any of the parameters are invalid.
    */
   public PSLogSummaryDialog(Dialog  parent, PSLogSummary logSummary,
      PSCatalogResultSet types)
   {
      super(parent);
      validateAndInitializeDialog(logSummary, types);
   }

   /**
    * Initializes the dialog with supplied parameters.
    *
    * @param logSummary  the log summary for the selected package,  may not be
    * <code>null</code>.
    * @param types the list of element types, may not be <code>null</code> or
    * empty.
    *
    * @throws IllegalArgumentException if any of the parameters are invalid.
    */
   private void validateAndInitializeDialog(PSLogSummary logSummary,
      PSCatalogResultSet types)
   {
      if (logSummary == null)
         throw new IllegalArgumentException(
         "Log summary for the package may not be null");

      if( types == null || !types.getResults().hasNext() )
      {
         throw new IllegalArgumentException(
            "types may not be null or empty.");
      }
      m_logSum = logSummary;
      m_types = types;
      initDialog();
   }

   /**
    * Private helper method
    * @param resId the tab's label
    * @param tabIx the tab's index
    */
   private void setMnemonicsForTabbedPane(String resId, int tabIx)
   {
      String label = getResourceString(resId);
      char mn = getResourceString(resId + ".mn").charAt(0);
       
      m_tabbedPanel.setMnemonicAt(tabIx, (int)mn);      
      m_tabbedPanel.setDisplayedMnemonicIndexAt(tabIx, label.indexOf(mn));
   }
   
   
   /**
    * Initializes  the dialog framework.
    */
   private void initDialog()
   {
      String deployableElemName = m_logSum.getPackage().getDisplayName();
      String title = getResourceString("title");
      setTitle(MessageFormat.format(title, new Object[]{deployableElemName}));

      JPanel mainPane = new JPanel();
      mainPane.setLayout(new BorderLayout());
      //create tabbed pane
      m_tabbedPanel = new JTabbedPane();
      m_tabbedPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
      int onTab = 0;
      
      //add 'Main' tab
      m_tabbedPanel.addTab(getResourceString("main"), createMainPanel());      
      setMnemonicsForTabbedPane("main", onTab++);
      //add 'Transforms' tab
      PSLogDetail logDet = m_logSum.getLogDetail();
      Iterator idItr = PSIteratorUtils.emptyIterator();
      if(logDet.getIdMap() != null)
         idItr = logDet.getIdMap().getMappings();
      Iterator dbmsItr = PSIteratorUtils.emptyIterator();
      if(logDet.getDbmsMap() != null)
         dbmsItr = logDet.getDbmsMap().getMappings();
      if (idItr.hasNext() || dbmsItr.hasNext())
      {
         JTabbedPane idDbmsPane = new JTabbedPane();
         idDbmsPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
         if (dbmsItr.hasNext())
         {
            PSDBMSMapsPanel dbmsPane =
               new PSDBMSMapsPanel(dbmsItr, PSDBMSMapsPanel.EDIT_TYPE_NONE);
            idDbmsPane.add(getResourceString("dbmsTab"), dbmsPane);
            dbmsPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
         }

         if (idItr.hasNext())
         {
            PSIDMapsPanel idPane =
               new PSIDMapsPanel(idItr, false, false, null, m_types, false);
            idDbmsPane.add(getResourceString("idTab"), idPane);
            idPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
         }
         m_tabbedPanel.add(getResourceString("transform"), idDbmsPane);
         setMnemonicsForTabbedPane("transform", onTab++);
      }
      //add 'Informed Warnings' tab, only if present
      PSValidationResults results = logDet.getValidationResults();
      if (results.getResults().hasNext())
      {
         PSValidationResultsModel model =
               new PSValidationResultsModel(results, false);
         JTable warningTable = new JTable(model);
         JScrollPane scrollPane = new JScrollPane(warningTable);
         scrollPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
         scrollPane.setPreferredSize(new Dimension(250, 250));
         m_tabbedPanel.addTab(getResourceString("infowarning"), scrollPane);
         setMnemonicsForTabbedPane("infowarning", onTab++);
      }
      //add 'Transaction Log' tab
      PSTransactionLogSummary tranxLogSum = logDet.getTransactionLog();
      if (tranxLogSum.getTransactions().hasNext())
      {

         PSTransLogModel model =  new PSTransLogModel(tranxLogSum);
         JTable tranLog = new JTable(model);
         JScrollPane scrollPane = new JScrollPane(tranLog);
         scrollPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
         scrollPane.setPreferredSize(new Dimension(250, 250));
         m_tabbedPanel.addTab(getResourceString("tranLog"), scrollPane);
         setMnemonicsForTabbedPane("tranLog", onTab++);
      }

      //add export close buttons
      JPanel btnPane = new JPanel();
      btnPane.setBorder(BorderFactory.createEmptyBorder(20,0,10,20));
      btnPane.setLayout(new BoxLayout(btnPane, BoxLayout.X_AXIS));
      UTFixedButton export = new UTFixedButton(getResourceString("exp"));
      export.setMnemonic(getResourceString("exp.mn").charAt(0));
      UTFixedButton close = new UTFixedButton(getResourceString("close"));
      close.setMnemonic(getResourceString("close.mn").charAt(0));
      UTFixedButton help = new UTFixedButton(getResourceString("help"));
      help.setMnemonic(getResourceString("help.mn").charAt(0));
      btnPane.add(Box.createHorizontalGlue());
      btnPane.add(export);
      btnPane.add(Box.createRigidArea(new Dimension(5,0)));
      btnPane.add(close);
      btnPane.add(Box.createRigidArea(new Dimension(5,0)));
      btnPane.add(help);
      btnPane.add(Box.createRigidArea(new Dimension(5,0)));

      help.addActionListener(new ActionListener(){
         public void actionPerformed(ActionEvent e)
         {
            onHelp();
         }
      });

      close.addActionListener(new ActionListener(){
         public void actionPerformed(ActionEvent e)
         {
            onCancel();
         }
      });
      export.addActionListener(new ActionListener(){
         public void actionPerformed(ActionEvent e)
         {
            onExport();
         }
      });
      mainPane.add(m_tabbedPanel, BorderLayout.CENTER);
      mainPane.add(btnPane,  BorderLayout.SOUTH);
      getContentPane().add(mainPane);

      pack();
      center();
      setResizable(true);
   }
   
   /**
    * Displays a file browser dialog to export/save the package log information
    * to an xml file for which the pop-up menu is shown to a text file. Displays
    * error dialog with appropriate message if exceptions happen in the process.
    */
   private void onExport()
   {
      FileOutputStream out = null;
      try
      {
         PSLogDetail logDetail = m_logSum.getLogDetail();

         File selectedFile = PSDeploymentClient.showFileDialog(
            this, 
            new File(m_logSum.getPackage().getDisplayName() + ".xml"),
            "xml", "XML Files (*.xml)", JFileChooser.SAVE_DIALOG);
            
         if(selectedFile != null)
         {            
            out = new FileOutputStream(selectedFile);
            Document doc = PSXmlDocumentBuilder.createXmlDocument();
            PSXmlDocumentBuilder.write(logDetail.toXml(doc), out);
         }
      }
      catch(IOException e)
      {
         ErrorDialogs.showErrorMessage(this, e.getLocalizedMessage(),
            PSDeploymentClient.getResources().getString("errorTitle"));
      }
      finally
      {
         try
         {
            if(out != null)
               out.close();
         } catch(IOException ie){}
      }
   }

   /**
    * Creates the panel for 'Main' tab in <code>m_tabbedPanel</code>.
    * @return  never <code>null</code>.
    */
   private JPanel createMainPanel()
   {
      PSArchiveSummary arcSum = m_logSum.getArchiveSummary();
      PSArchiveInfo arcInfo = arcSum.getArchiveInfo();
      // labels
      JLabel arcTypeLbl = new JLabel();
      arcTypeLbl.setText(getResourceString("pkgType"));
      arcTypeLbl.setAlignmentX(JLabel.LEFT_ALIGNMENT);

      JLabel arcNameLbl = new JLabel();
      arcNameLbl.setText(getResourceString("archive"));
      arcNameLbl.setAlignmentX(JLabel.LEFT_ALIGNMENT);

      JLabel arcInstDtLbl = new JLabel();
      arcInstDtLbl.setText(getResourceString("installDate"));
      arcInstDtLbl.setAlignmentX(JLabel.LEFT_ALIGNMENT);

      JLabel arcSrcSvrLbl = new JLabel();
      arcSrcSvrLbl.setText(getResourceString("src"));
      arcSrcSvrLbl.setAlignmentX(JLabel.LEFT_ALIGNMENT);

      JLabel arcVrsBldLbl = new JLabel();
      arcVrsBldLbl.setText(getResourceString("vrBld"));
      arcVrsBldLbl.setAlignmentX(JLabel.LEFT_ALIGNMENT);

      JPanel labelPane = new JPanel();
      Dimension dim = new Dimension(0, 20);
      labelPane.setLayout(new BoxLayout(labelPane, BoxLayout.Y_AXIS));
      labelPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
      labelPane.add(arcTypeLbl);
      labelPane.add(Box.createRigidArea(dim));
      labelPane.add(arcNameLbl);
      labelPane.add(Box.createRigidArea(dim));
      labelPane.add(arcInstDtLbl);
      labelPane.add(Box.createRigidArea(dim));
      labelPane.add(arcSrcSvrLbl);
      labelPane.add(Box.createRigidArea(dim));
      labelPane.add(arcVrsBldLbl);

      //values
      JLabel arcNameValLbl = new JLabel();
      arcNameValLbl.setText(arcInfo.getArchiveRef());
      arcVrsBldLbl.setAlignmentX(JLabel.LEFT_ALIGNMENT);

      JLabel arcTypeValLbl = new JLabel();
      arcTypeValLbl.setText(m_logSum.getPackage().getObjectTypeName());
      arcTypeValLbl.setAlignmentX(JLabel.LEFT_ALIGNMENT);

      Date date = arcSum.getInstallDate();
      DateFormat dfInsdate = DateFormat.getDateTimeInstance();
      
      JLabel instDateValLbl = new JLabel();
      instDateValLbl.setText(dfInsdate.format(date));
      instDateValLbl.setAlignmentX(JLabel.LEFT_ALIGNMENT);

      JLabel arcSrcSvrValLbl = new JLabel();
      arcSrcSvrValLbl.setText(arcInfo.getServerName());
      arcSrcSvrValLbl.setAlignmentX(JLabel.LEFT_ALIGNMENT);

      JLabel arcVrsBldValLbl = new JLabel();
      String vb = arcInfo.getServerVersion() + "/" +
         arcInfo.getServerBuildNumber();
      arcVrsBldValLbl.setText(vb);
      arcVrsBldValLbl.setAlignmentX(JLabel.LEFT_ALIGNMENT);

      JPanel valPane = new JPanel();
      valPane.setLayout(new BoxLayout(valPane, BoxLayout.Y_AXIS));
      valPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
      valPane.add(arcTypeValLbl);
      valPane.add(Box.createRigidArea(dim));
      valPane.add(arcNameValLbl);
      valPane.add(Box.createRigidArea(dim));
      valPane.add(instDateValLbl);
      valPane.add(Box.createRigidArea(dim));
      valPane.add(arcSrcSvrValLbl);
      valPane.add(Box.createRigidArea(dim));
      valPane.add(arcVrsBldValLbl);

      JPanel nvPane = new JPanel();
      nvPane.setLayout(new BoxLayout(nvPane, BoxLayout.X_AXIS));
      nvPane.add(labelPane);
      nvPane.add(valPane);

      PSDeployableElement de = m_logSum.getPackage();
      PSDependencyTree depTree = new PSDependencyTree(de, false, 
         new PSDependencyTreeContext());

      JPanel treePanel = new JPanel();
      treePanel.setLayout(new BoxLayout(treePanel, BoxLayout.Y_AXIS));
      treePanel.setBorder(BorderFactory.createCompoundBorder(
         BorderFactory.createTitledBorder(getResourceString("pkgDep")),
         BorderFactory.createEmptyBorder(10,10,10,10)));

      JScrollPane treeScrollPane = new JScrollPane();
      treePanel.add(treeScrollPane);
      treeScrollPane.setViewportView(depTree);

      JPanel mainPane = new JPanel();
      mainPane.setLayout(new BoxLayout(mainPane, BoxLayout.X_AXIS));
      mainPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
      mainPane.add(nvPane);
      mainPane.add(treePanel);
      return mainPane;
   }

   /**
    * Gets the right help page based on the tab selection.
    *
    * @param helpId the default help id (name of the class), may not be <code>
    * null</code> or empty.
    *
    * @return help id corresponding to the tab selected. Never <code>null</code>
    * or empty.
    *
    * @throws IllegalArgumentException if help id is not valid.
    */
   protected String subclassHelpId( String helpId )
   {
      if(helpId == null || helpId.trim().length() == 0)
         throw new IllegalArgumentException("helpId may not be null or empty.");

      int k = m_tabbedPanel.getSelectedIndex();

      String tabTitle = m_tabbedPanel.getTitleAt(k);
      if( tabTitle.equals(getResourceString("main")) )
         return helpId + "_main";
      else if( tabTitle.equals(getResourceString("transform")) )
         return helpId + "_transforms";
      else if( tabTitle.equals(getResourceString("infowarning")) )
         return helpId + "_warnings";
      else
         return helpId + "_transactionlogs";
   }

   /**
    * The list of element types, initialised in the ctor, may not be <code>null
    * </code> or modified or empty.
    */
   private PSCatalogResultSet m_types;

   /**
    * Encapsulates summary of selected package deployment log. Initialised in
    * the ctor, never <code>null</code> or modified.
    */
   private PSLogSummary m_logSum;

   /**
    * Tabbed panel.Initialised in <code>initDialog()</code>. Never
    * <code>null</code> after that. Contains 'Main', 'Transforms',
    * 'Informed Warnings' and 'Transaction Log' tabs. Some tabs may not be
    * present if there is no data available data to display.
    */
   private JTabbedPane m_tabbedPanel;
}
