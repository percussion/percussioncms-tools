/*[ PSErrorHandlingEditorPanel.java ]******************************************
 * COPYRIGHT (c) 2002 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 ******************************************************************************/
package com.percussion.loader.ui;

import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.guitools.ErrorDialogs;
import com.percussion.loader.PSLoaderException;
import com.percussion.loader.objectstore.PSEmailDef;
import com.percussion.loader.objectstore.PSErrorHandlingDef;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.awt.Dimension;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.Border;

import org.w3c.dom.Element;

/**
 * Panel for configuring loader descriptor's error handling. It consists of
 * checkboxes to take specific actions in the event of error and whether or not
 * to attach logs. Two single column editable tables holding the list of 'To'
 * and 'CC' email addresses.
 */
public class PSErrorHandlingEditorPanel extends PSConfigPanel
{
   /**
    * Creates the error handling panel.
    */
   public PSErrorHandlingEditorPanel()
   {
      init();
   }

   /**
    * Initializes the panel.
    */
   private void init()
   {
      if (null == ms_res)
         ms_res = ResourceBundle.getBundle(
               getClass().getName() + "Resources", Locale.getDefault() );

      Border b1 = BorderFactory.createEmptyBorder(10, 10, 10, 10 );
      Border b3 = BorderFactory.createEtchedBorder();
      Border b2 = BorderFactory.createCompoundBorder(b3, b1);
      Border b = BorderFactory.createTitledBorder(b2,
            PSContentLoaderResources.getResourceString(ms_res, "border.email"));
      setBorder(b1);
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

      JPanel mainPane = new JPanel();
      mainPane.setLayout(new BoxLayout(mainPane, BoxLayout.Y_AXIS));

      JPanel chkbxPane = new JPanel();
      chkbxPane.setLayout(new BoxLayout(chkbxPane, BoxLayout.X_AXIS));

      m_stop = new JCheckBox(
         PSContentLoaderResources.getResourceString(ms_res,
         "checkbox.stopOnError"));
      m_email = new JCheckBox(
         PSContentLoaderResources.getResourceString(ms_res,
         "checkbox.emailOnError"));
      m_success = new JCheckBox(
         PSContentLoaderResources.getResourceString(ms_res,
         "checkbox.emailOnSuccess"));

      JPanel topLeft = new JPanel();
      topLeft.setLayout(new BoxLayout(topLeft, BoxLayout.Y_AXIS));
      topLeft.add(m_stop);
      topLeft.add(m_email);
      topLeft.add(m_success);

      chkbxPane.add(topLeft);
      chkbxPane.add(Box.createHorizontalGlue());
      mainPane.add(chkbxPane);

      JPanel emailPane = new JPanel();
      emailPane.setLayout(new BoxLayout(emailPane, BoxLayout.Y_AXIS));
      emailPane.setBorder(b);
      emailPane.setPreferredSize(new Dimension(100, 200));
      m_toModel = new PSSingleColumnModel();
      m_toTable = new JTable(m_toModel);
      emailPane.add(getPanel(PSContentLoaderResources.getResourceString(
         ms_res, "table.to"), m_toTable));
      emailPane.add(Box.createRigidArea(new Dimension(0,20)));
      m_ccModel = new PSSingleColumnModel();
      m_ccTable = new JTable(m_ccModel);
      emailPane.add(getPanel(PSContentLoaderResources.getResourceString(
         ms_res, "table.cc"), m_ccTable));
      mainPane.add(emailPane);

      JPanel chkLogPane = new JPanel();
      chkLogPane.setLayout(new BoxLayout(chkLogPane, BoxLayout.X_AXIS));

      m_attachLog = new JCheckBox(
         PSContentLoaderResources.getResourceString(ms_res,
         "checkbox.attachLogs"));
      m_attachLog.setBorder(b1);
      chkLogPane.add(m_attachLog);
      chkLogPane.add(Box.createHorizontalGlue());

      emailPane.add(chkLogPane);
      mainPane.add(Box.createVerticalGlue());
      add(mainPane);
   }

   /**
    * Creates panel comprising a single column editable table and a label
    * specifying the intent of the table.
    *
    * @param ccto specifies the label in the panel, may not be <code>null</code>

    * @return never <code>null</code> or modified after that.
    */
   private JPanel getPanel(String ccto, JTable table)
   {
      JScrollPane jsp = new JScrollPane(table);

      JPanel pane = new JPanel();
      pane.setLayout(new BoxLayout(pane, BoxLayout.X_AXIS));
      JLabel lb = new JLabel(ccto);
      lb.setAlignmentY(JPanel.TOP_ALIGNMENT);
      jsp.setAlignmentY(JPanel.TOP_ALIGNMENT);
      pane.add(Box.createRigidArea(new Dimension(30, 0)));
      pane.add(lb);
      pane.add(Box.createRigidArea(new Dimension(10, 0)));
      pane.add(jsp);
      return pane;
   }

   /**
    * Tell the editor to stop editing and accept any partially edited value
    * as the value of the editor.
    *
    * @param table, table in which to stop editing, assumed to be not <code>null
    * </code>
    */
   private void stopExtTableEditing(JTable table)
   {
      if (table.isEditing())
      {
         table.getCellEditor(table.getEditingRow(),
         table.getEditingColumn()).stopCellEditing();
      }
   }

   /**
    * Implements the {@link IPSConfigPanel} interface.
    */
   public void load(Element configXml)
   {
      try
      {
         m_errDef = new PSErrorHandlingDef(configXml);
         m_emailDef = m_errDef.getEmail();
         m_toModel.setData(m_emailDef.getRecipients());
         m_ccModel.setData(m_emailDef.getCCRecipients());
         m_attachLog.setSelected(m_emailDef.getAttachLogs());
         m_stop.setSelected(m_errDef.getStopOnError());
         m_email.setSelected(m_errDef.getEmailOnError());
         m_success.setSelected(m_errDef.getEmailOnSuccess());

      }
      catch(PSLoaderException e)
      {
         ErrorDialogs.showErrorDialog(this,
            PSContentLoaderResources.getResourceString(
            ms_res, e.getMessage()),
            PSContentLoaderResources.getResourceString(
            ms_res, "err.title.loaderexception"),
            JOptionPane.ERROR_MESSAGE);
         return;
      }
      catch(PSUnknownNodeTypeException f)
      {
         ErrorDialogs.showErrorDialog(this,
            PSContentLoaderResources.getResourceString(
            ms_res, f.getMessage()),
            PSContentLoaderResources.getResourceString(
            ms_res, "error.title.unknownnode"),
            JOptionPane.ERROR_MESSAGE);
         return;
      }
   }

   /**
    * Implements the {@link IPSConfigPanel} interface.
    */
   public Element save()
   {
      try
      {
         if(!validateContent())
            return null;
         stopExtTableEditing(m_toTable);
         stopExtTableEditing(m_ccTable);
         copyListEntries(m_ccModel.getData(), m_emailDef.getCCRecipients());
         copyListEntries(m_toModel.getData(), m_emailDef.getRecipients());
         m_errDef.setEmailOnError(m_email.isSelected());
         m_errDef.setStopOnError(m_stop.isSelected());
         m_errDef.setEmailOnSuccess(m_success.isSelected());
         m_emailDef.setAttachLog(m_attachLog.isSelected());
      }
      catch(PSLoaderException e)
      {
         ErrorDialogs.showErrorDialog(this,
            PSContentLoaderResources.getResourceString(ms_res, e.getMessage()),
            PSContentLoaderResources.getResourceString(ms_res,
            "err.title.loaderexception"), JOptionPane.ERROR_MESSAGE);
      }
      return m_errDef.toXml(PSXmlDocumentBuilder.createXmlDocument());
   }

   /**
    * Implements the {@link IPSConfigPanel} interface.
    */
   public boolean validateContent()
   {
      return true;
   }

   /**
    * Copies all non <code>null</code> and non empty Strings from the supplied
    * source list into the target list. The target is cleared before teh copy 
    * procedure.
    * 
    * @param src the source list to be copied, not <code>null</code> may be 
    *    empty.
    * @param target the target list, not <code>null</code> may be empty.
    * @throws IllegalArgumentException if any supplied parameter is 
    *    <code>null</code>.
    */
   public static void copyListEntries(List src, List target)
   {
      if (src == null || target == null)
         throw new IllegalArgumentException("parameters cannot be null");
         
      target.clear();
      int len = src.size();
      for(int z = 0; z < len; z++)
      {
         String temp = (String)src.get(z);
         if (temp != null && temp.length() != 0)
            target.add(temp);
      }
   }
   
   /**
    * Table for for email recipients in 'TO' table. Initialized in {@link #init(
    * )}, never <code>null</code> or modified after that.
    */
   private JTable m_toTable;

   /**
    * Table for for email recipients in 'CC' table. Initialized in {@link #init(
    * )}, never <code>null</code> or modified after that.
    */
   private JTable m_ccTable;

   /**
    * If checked the currently running process stops after the first error
    * occurred. Initialized in {@link #init()}, never <code>null</code> or
    * modified after that.
    */
   private JCheckBox m_stop;

   /**
    * Sends an error email if checked based on the following settings.
    * Initialized in {@link #init()}, never <code>null</code> or modified after
    * that.
    */
   private JCheckBox m_email;

   /**
    * Sends a success email if checked. Initialized in {@link #init()}, never
    * <code>null</code> or modified after that.
    */
   private JCheckBox m_success;

   /**
    * Attaches log when sending email if checked. Initialized in {@link #init()}
    * , never <code>null</code> or modified after that.
    */
   private JCheckBox m_attachLog;

   /**
    * {@link PSSingleColumnModel}, table model for email recipients in 'TO'
    * table. Initialized in {@link #init()}, never <code>null</code> or modified
    * after that.
    */
   private PSSingleColumnModel m_toModel;

   /**
    * {@link PSSingleColumnModel}, table model for email recipients in 'CC'
    * table. Initialized in {@link #init()}, never <code>null</code> or modified
    * after that.
    */
   private PSSingleColumnModel m_ccModel;

   /**
    * {@link PSEmailDef}, Initialized in {@link #init()}, never <code>null
    * </code> or modified after that.
    */
   private PSEmailDef m_emailDef;

   /**
    * {@link PSErrorHandlingDef}, Initialized in {@link #init()}, never
    * <code>null</code> or modified after that.
    */
   private PSErrorHandlingDef m_errDef;

   /**
    * Resource bundle for this class. Initialised in the constructor.
    * It's not modified after that. Never <code>null</code>.
    */
   private static ResourceBundle ms_res;
}