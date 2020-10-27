/*[ PSLogEditorPanel.java ]****************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader.ui;

//import com.percussion.cms.IPSConstants;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.guitools.ErrorDialogs;
import com.percussion.loader.PSLoaderException;
import com.percussion.loader.objectstore.PSLogDef;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.Border;

import org.w3c.dom.Element;

/**
 * Log editor panel specifies how much information will be logged. There are six
 * log levels indicated by a radio button group. The log location specified by
 * two checkboxes, whether log goes to console and/or file. The file settings if
 * log goes to file, two radio buttons specifying if the it's appended or
 * overwritten, a checkbox and a text field indicating if backup and the number
 * of backups respectively.
 */
public class PSLogEditorPanel extends PSConfigPanel
{
   /**
    * Creates the log editor panel.
    */
   public PSLogEditorPanel()
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

      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
      Border b1 = BorderFactory.createEmptyBorder(10, 10, 10, 10 );
      Border b3 = BorderFactory.createEtchedBorder();
      Border b2 = BorderFactory.createCompoundBorder(b3, b1);
      Border b = BorderFactory.createTitledBorder(b2,
         PSContentLoaderResources.getResourceString(ms_res, "border.logLevel"));
      setBorder(b1);
      m_offRadioBtn = new JRadioButton(
         PSContentLoaderResources.getResourceString(ms_res, "off"));
      m_fatalRadioBtn = new JRadioButton(
         PSContentLoaderResources.getResourceString(ms_res, "fatal"));
      m_errorRadioBtn = new JRadioButton(
         PSContentLoaderResources.getResourceString(ms_res, "error"));
      m_warningsRadioBtn = new JRadioButton(
         PSContentLoaderResources.getResourceString(ms_res, "warning"));
      m_infoRadioBtn = new JRadioButton(
         PSContentLoaderResources.getResourceString(ms_res, "info"));
      m_debugRadioBtn = new JRadioButton(
         PSContentLoaderResources.getResourceString(ms_res, "debug"));

      ButtonGroup logGrp = new ButtonGroup();
      logGrp.add(m_offRadioBtn);
      logGrp.add(m_fatalRadioBtn);
      logGrp.add(m_errorRadioBtn);
      logGrp.add(m_warningsRadioBtn);
      logGrp.add(m_infoRadioBtn);
      logGrp.add(m_debugRadioBtn);

      JPanel top = new JPanel();
      top.setLayout(new BoxLayout(top, BoxLayout.X_AXIS));
      top.setBorder(b);

      JPanel topLeft = new JPanel();
      topLeft.setLayout(new BoxLayout(topLeft, BoxLayout.Y_AXIS));
      topLeft.add(m_offRadioBtn);
      topLeft.add(m_fatalRadioBtn);
      topLeft.add(m_errorRadioBtn);
      JPanel topRight = new JPanel();
      topRight.setLayout(new BoxLayout(topRight, BoxLayout.Y_AXIS));
      topRight.add(m_warningsRadioBtn);
      topRight.add(m_infoRadioBtn);
      topRight.add(m_debugRadioBtn);
      top.add(topLeft);
      top.add(Box.createRigidArea(new Dimension(50,0)));
      top.add(Box.createHorizontalGlue());
      top.add(topRight);
      top.add(Box.createHorizontalGlue());
      add(top);

      Border bb1 = BorderFactory.createEmptyBorder(10, 7, 10, 10 );
      Border bb2 = BorderFactory.createCompoundBorder(b3, bb1);
      Border bb = BorderFactory.createTitledBorder(bb2,
            PSContentLoaderResources.getResourceString(ms_res,
            "border.logLocation"));
      JPanel mainBottom = new JPanel();
      mainBottom.setLayout(new BorderLayout());
      mainBottom.setBorder(bb);

      m_bottomPanel = new JPanel();
      m_bottomPanel.setLayout(new BoxLayout(m_bottomPanel, BoxLayout.Y_AXIS));

      JPanel middle = new JPanel();
      middle.setLayout(new BoxLayout(middle, BoxLayout.X_AXIS));
      middle.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));

      JPanel middleLeft = new JPanel();
      middleLeft.setLayout(new BoxLayout(middleLeft, BoxLayout.Y_AXIS));

      JPanel middleRight = new JPanel();
      middleRight.setLayout(new BoxLayout(middleRight, BoxLayout.Y_AXIS));

      m_fileCbx = new JCheckBox(
         PSContentLoaderResources.getResourceString(ms_res, "file"));
      m_fileSettingsPanel = new JPanel();
      m_fileCbx.addItemListener( new ItemListener()
      {
         public void itemStateChanged(ItemEvent e)
         {
            if (e.getStateChange() == ItemEvent.SELECTED)
               m_bottomPanel.add(m_fileSettingsPanel);
            else
               m_bottomPanel.remove(m_fileSettingsPanel);
             m_bottomPanel.revalidate();
         }
      });

      middleLeft.add(m_fileCbx);
      middle.add(middleLeft);
      middle.add(Box.createHorizontalGlue());
      middle.add(middleRight);
      middle.add(Box.createHorizontalGlue());
      m_bottomPanel.add(middle);

      m_appendRadioBtn = new JRadioButton(
         PSContentLoaderResources.getResourceString(ms_res, "append"));
      m_overiteRadioBtn = new JRadioButton(
         PSContentLoaderResources.getResourceString(ms_res, "overite"));

      ButtonGroup fileGrp = new ButtonGroup();
      fileGrp.add(m_appendRadioBtn);
      fileGrp.add(m_overiteRadioBtn);

      //m_upback = new JCheckBox(
      //   PSContentLoaderResources.getResourceString(ms_res, "backup"));

      //m_text = new JTextField();
      //final JLabel lb = new JLabel(
      //   PSContentLoaderResources.getResourceString(ms_res, "maxbackup"));

      m_fileSettingsPanel.setLayout(new BoxLayout(m_fileSettingsPanel,
         BoxLayout.X_AXIS));

      JPanel downLeft = new JPanel();
      downLeft.setLayout(new BoxLayout(downLeft, BoxLayout.Y_AXIS));

      m_downRight = new JPanel();
      m_downRight.setLayout(new BoxLayout(m_downRight, BoxLayout.Y_AXIS));
      m_downExtremeRight = new JPanel();
      m_downExtremeRight.setLayout(new BoxLayout(m_downExtremeRight,
         BoxLayout.Y_AXIS));
      m_downExtremeRight.add(Box.createVerticalGlue());

      downLeft.add(m_appendRadioBtn);
      downLeft.add(Box.createVerticalGlue());
      m_fileSettingsPanel.add(downLeft);
      m_fileSettingsPanel.add(Box.createRigidArea(new Dimension(50, 0)));
      m_fileSettingsPanel.add(Box.createHorizontalGlue());
      m_downRight.add(m_overiteRadioBtn);
      //m_downRight.add(m_upback);
      m_downRight.add(Box.createRigidArea(new Dimension(0, 5)));
      /*
      m_upback.addItemListener( new ItemListener()
      {
         public void itemStateChanged(ItemEvent e)
         {
            if (e.getStateChange() == ItemEvent.SELECTED)
            {
               m_downExtremeRight.add(lb);
               m_downRight.add(m_text);
            }
            else
            {
               m_downExtremeRight.remove(lb);
               m_downRight.remove(m_text);
            }
            m_downExtremeRight.revalidate();
            m_downRight.revalidate();
         }
      });
      */
      m_fileSettingsPanel.add(m_downRight);
      m_fileSettingsPanel.add(Box.createRigidArea(new Dimension(10, 0)));
      m_fileSettingsPanel.add(m_downExtremeRight);
      m_fileSettingsPanel.add(Box.createHorizontalGlue());

      Border bb0 = BorderFactory.createTitledBorder(bb2,
         PSContentLoaderResources.getResourceString(ms_res,
         "border.fileSettings"));
      m_fileSettingsPanel.setBorder(bb0);
      mainBottom.add(m_bottomPanel, BorderLayout.NORTH);
      add(mainBottom);
   }

   /**
    * Implements the {@link IPSConfigPanel} interface.
    */
   public void load(Element configXml)
   {
      try
      {
         m_logDef = new PSLogDef(configXml);
         setLogLevel(m_logDef.getDefaultLogLevel(null));
         setlogLocation();
      }
      catch(PSLoaderException e)
      {
         ErrorDialogs.showErrorDialog(this,
            PSContentLoaderResources.getResourceString(ms_res, e.getMessage()),
            PSContentLoaderResources.getResourceString(ms_res,
            "err.title.loaderexception"), JOptionPane.ERROR_MESSAGE);
         return;
      }
      catch(PSUnknownNodeTypeException f)
      {
         ErrorDialogs.showErrorDialog(this,
            PSContentLoaderResources.getResourceString(ms_res, f.getMessage()),
            PSContentLoaderResources.getResourceString(ms_res,
            "error.title.unknownnode"), JOptionPane.ERROR_MESSAGE);
         return;

      }
   }

   /**
    * Sets log location i.e where the log needs to be directed. If the logging
    * is done to a file, it specifies if the log is going to be appended or over
    * written and if the back up is required and the max backup.
    */
   private void setlogLocation()
   {
      boolean isFile = m_logDef.isFile();
      String appendParam = m_logDef.getFileParamValue(
         PSLogDef.FILE_APPENDER, PSLogDef.FILE_PARAM_APPEND);
      if (isFile)
      {
         m_fileCbx.setSelected(isFile);
         /*
         String backupParam = m_logDef.getFileParamValue(PSLogDef.FILE_APPENDER,
            PSLogDef.FILE_PARAM_BACKUP);
         if (backupParam != null && backupParam.length() != 0)
         {
            if (backupParam.equals(IPSConstants.BOOLEAN_TRUE))
            {
               m_upback.setSelected(true);
               String maxbackupParam = m_logDef.getFileParamValue(
                     PSLogDef.FILE_APPENDER, PSLogDef.FILE_PARAM_MAXBACKUP);
               m_text.setText(maxbackupParam);
            }
            else
               m_upback.setSelected(false);
         }
         */
      }
      if (appendParam != null && appendParam.length() != 0)
      {
         if (appendParam.equals(TRUE))
            m_appendRadioBtn.setSelected(true);
         else
            m_overiteRadioBtn.setSelected(true);
      }
   }

   /**
    * Sets the log level.
    *
    * @param logLevel, specifies the log level defined in {@link PSLogDef},
    * assumed to be not <code>null</code> or empty.
    */
   private void setLogLevel(String logLevel)
   {
      if (logLevel.equals(PSLogDef.DEBUG))
         m_debugRadioBtn.setSelected(true);
      else if (logLevel.equals(PSLogDef.ERROR))
         m_errorRadioBtn.setSelected(true);
      else if ((logLevel.equals(PSLogDef.FATAL)))
         m_fatalRadioBtn.setSelected(true);
      else if ((logLevel.equals(PSLogDef.INFO)))
         m_infoRadioBtn.setSelected(true);
      else if ((logLevel.equals(PSLogDef.WARN)))
         this.m_warningsRadioBtn.setSelected(true);
      else if ((logLevel.equals(PSLogDef.OFF)))
         m_offRadioBtn.setSelected(true);
   }

   /**
    * Gets the log level.
    *
    * @return, specifies the log level defined in {@link PSLogDef}, never <code>
    * null</code> or empty.
    */
   private String getLogLevel()
   {
      if (m_debugRadioBtn.isSelected())
         return PSLogDef.DEBUG;
      else if (m_fatalRadioBtn.isSelected())
         return PSLogDef.FATAL;
      else if (m_errorRadioBtn.isSelected())
       return PSLogDef.ERROR;
      else if (m_infoRadioBtn.isSelected())
         return PSLogDef.INFO;
      else if (m_warningsRadioBtn.isSelected())
         return PSLogDef.WARN;
      else
         return PSLogDef.OFF;
   }

   /**
    * Implements the {@link IPSConfigPanel} interface.
    */
   public Element save()
   {
      try
      {
         if (!validateContent())
            return null;
         m_logDef.setDefaultLogLevel(getLogLevel());

         if (m_fileCbx.isSelected())
         {
            m_logDef.setFile(true);
            if (m_appendRadioBtn.isSelected())
               m_logDef.setAppenderParam(PSLogDef.FILE_APPENDER,
                  PSLogDef.FILE_PARAM_APPEND, TRUE);
            else
               m_logDef.setAppenderParam(PSLogDef.FILE_APPENDER,
                  PSLogDef.FILE_PARAM_APPEND, FALSE);
            /*
            if (m_upback.isSelected())
            {
               m_logDef.setAppenderParam(PSLogDef.FILE_APPENDER,
                  PSLogDef.FILE_PARAM_BACKUP, IPSConstants.BOOLEAN_TRUE);
               String backup = m_text.getText();
               if (backup != null && backup.length() != 0)
                  m_logDef.setAppenderParam(PSLogDef.FILE_APPENDER,
                     PSLogDef.FILE_PARAM_MAXBACKUP, backup);
            }
            else
            {
               m_logDef.setAppenderParam(PSLogDef.FILE_APPENDER,
                  PSLogDef.FILE_PARAM_BACKUP, IPSConstants.BOOLEAN_FALSE);
            }
            */
         }
         else
         {
            m_logDef.setFile(false);
            m_logDef.setAppenderParam(PSLogDef.FILE_APPENDER,
               PSLogDef.FILE_PARAM_APPEND, TRUE);
            m_logDef.setAppenderParam(PSLogDef.FILE_APPENDER,
               PSLogDef.FILE_PARAM_BACKUP, FALSE);
            m_logDef.setAppenderParam(PSLogDef.FILE_APPENDER,
               PSLogDef.FILE_PARAM_MAXBACKUP, "0");
         }
      }
      catch(PSLoaderException e)
      {
         ErrorDialogs.showErrorDialog(this,
           PSContentLoaderResources.getResourceString(ms_res, e.getMessage()),
           PSContentLoaderResources.getResourceString(ms_res,
           "err.title.loaderexception"), JOptionPane.ERROR_MESSAGE);
         return null;
      }
      catch(PSUnknownNodeTypeException f)
      {
         ErrorDialogs.showErrorDialog(this,
            PSContentLoaderResources.getResourceString(ms_res, f.getMessage()),
            PSContentLoaderResources.getResourceString(ms_res,
            "error.title.unknownnode"), JOptionPane.ERROR_MESSAGE);
         return null;
      }
      return m_logDef.toXml(PSXmlDocumentBuilder.createXmlDocument());
   }

   /**
    * Implements the {@link IPSConfigPanel} interface.
    *
   public boolean validateContent()
   {
      String backUpSz = m_text.getText();
      if (m_upback.isSelected())
      {
         if (backUpSz != null && backUpSz.length() != 0)
         {
            try
            {
               int sz = Integer.parseInt(backUpSz);
            }
            catch(NumberFormatException e)
            {
               ErrorDialogs.showErrorDialog(this, e.toString(),
                     PSContentLoaderResources.getResourceString(ms_res,
                     "error.title.numberException"), JOptionPane.ERROR_MESSAGE);
               return false;
            }
         }
      }
      return true;
   }
   */
   /**
    * Represents 'Off' log level in the panel. If <code>true</code> 'Off' is
    * effective else not. Initialized in the {@link #init()}, never <code>null
    * </code> or modified after that.
    */
   private JRadioButton m_offRadioBtn;

   /**
    * Represents 'Fatal' log level in the panel. If <code>true</code> 'Fatal' is
    * effective else not. Initialized in the {@link #init()}, never <code>null
    * </code> or modified after that.
    */
   private JRadioButton m_fatalRadioBtn;

   /**
    * Represents 'Error' log level in the panel. If <code>true</code> 'Error' is
    * effective else not. Initialized in the {@link #init()}, never <code>null
    * </code> or modified after that.
    */
   private JRadioButton m_errorRadioBtn;

   /**
    * Represents 'Warning' log level in the panel. If <code>true</code>
    * 'Warning' is  effective else not. Initialized in the {@link #init()},
    * never <code>null </code> or modified after that.
    */
   private JRadioButton m_warningsRadioBtn;

   /**
    * Represents 'Info' log level in the panel. If <code>true</code> 'Info' is
    * effective else not. Initialized in the {@link #init()}, never <code>null
    * </code> or modified after that.
    */
   private JRadioButton m_infoRadioBtn;

   /**
    * Represents 'Debug' log level in the panel. If <code>true</code> 'Debug' is
    * effective else not. Initialized in the {@link #init()}, never <code>null
    * </code> or modified after that.
    */
   private JRadioButton m_debugRadioBtn;

   /**
    * If <code>m_fileCbx</code> has been selected, then checking this checkbox
    * means the log will be appended to the existing log file. Represents
    * 'Append' checkbox in the panel. Initialized in the {@link #init()}, never
    * <code>null</code> or modified after that.
    */
   private JRadioButton m_appendRadioBtn;

   /**
    * If <code>m_fileCbx</code> has been selected, then checking this checkbox
    * means the log will be over written  to the log file. Represents
    * 'Overwrite' checkbox in the panel. Initialized in the {@link #init()},
    * never <code>null</code> or modified after that.
    */
   private JRadioButton m_overiteRadioBtn;

   /**
    * File settings panel which gets visible when <code>m_fileCbx</code> is
    * selected. Initialized in the {@link #init()},
    * never <code>null</code> or modified after that.
    */
   private JPanel m_fileSettingsPanel;

   /**
    * If selected then the logging is directed to a file . Represented by 'File'
    * checkbox. Initialized in the {@link #init()}, never <code>null</code> or
    * modified after that.
    */
   private JCheckBox m_fileCbx;

   /**
    * Panel holding <code>m_fileSettingsPanel</code>. Initialized in the
    * {@link #init()}, never <code>null</code> or modified after that.
    */
   private JPanel m_bottomPanel;

   /**
    * Panel holding the label 'Maximum backups' for the <code>m_text</code>.
    * Initialized in the {@link #init()}, never <code>null</code> or
    * modified after that.
    */
   private JPanel m_downExtremeRight;

   /**
    * Panel holding the <code>m_text</code> for specifying maximum number of
    * backups. Initialized in the {@link #init()}, never <code>null</code> or
    * modified after that.
    */
   private JPanel m_downRight;

   /**
    * if <code>m_upback</code> is selected, it specifies the the maximum number
    * of backups. Initialized in the {@link #init()}, never <code>null</code> or
    * modified after that.
    */
   //private JTextField m_text;

   /**
    * If <code>m_fileCbx</code> has been selected, then checking this checkbox
    * means log file backups will be created limited by the number specified
    * in <code>m_text</code>. Initialized in the {@link #init()}, never
    * <code>null</code> or modified after that.
    */
   //private JCheckBox m_upback;

   /**
    * Encapsulates log data object. Initialized in {@link #load(Element)}, never
    * <code>null</code> or modified after that.
    */
   private PSLogDef m_logDef;

   /**
    * Resource bundle for this class. Initialised in the constructor.
    * It's not modified after that. May be <code>null</code> if it could not
    * load the resource properties file.
    */
   private static ResourceBundle ms_res;

   // Constant values for true or false
   private final static String TRUE = "true";
   private final static String FALSE = "false";
}