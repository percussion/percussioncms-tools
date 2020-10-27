/******************************************************************************
 *
 * [ PSInfoView.java ]
 * 
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.packageinstaller.ui;

import net.miginfocom.swing.MigLayout;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;

/**
 * @author erikserating
 *
 */
public class PSInfoView extends JPanel
{
   public PSInfoView(PSPackageInstallerFrame frame)
   {
      m_parent = frame;
      init();
   }
      
   public void setText(String text)
   {
      m_descTextPane.setText(StringUtils.defaultString(text));
      m_descTextPane.setCaretPosition(0);
   }

      
   private void init()
   {
      MigLayout layout = new MigLayout("fill");
      setLayout(layout);
      this.add(getMainPanel(), "grow");
   }
   
   
   /**
    * Initialize pre-defined styles. Must be called after
    * the document is initialized.
    */
   private void initStyles()
   {
      Style style = null;
      style = m_doc.addStyle(Styles.BOLD.toString(), null);
      StyleConstants.setBold(style, true);
      
      style = m_doc.addStyle(Styles.BOLD_BLUE.toString(), null);
      StyleConstants.setBold(style, true);
      StyleConstants.setForeground(style, Color.blue);
      
      style = m_doc.addStyle(Styles.BOLD_RED.toString(), null);
      StyleConstants.setBold(style, true);
      StyleConstants.setForeground(style, Color.red);
      
      style = m_doc.addStyle(Styles.ITALIC.toString(), null);
      StyleConstants.setItalic(style, true);
      
      style = m_doc.addStyle(Styles.NORMAL.toString(), null);      
     
   }
   
   /*
    * Sets Main Panel
    */
   private JPanel getMainPanel()
   {
      MigLayout layout = new MigLayout(
            "wrap 1, fill",
            "[grow]",
            "[grow]");
      
      JPanel mainPanel = new JPanel();
      mainPanel.setLayout(layout);
      
      m_descTextAreaScroll = new JScrollPane(m_descTextPane, 
      JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, 
      JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
      
      m_descTextPane.setBorder(null);
      m_descTextPane.setEditable(false);
      m_descTextPane.setContentType("text/html");
           
      mainPanel.add(m_descTextAreaScroll,"grow");
      return mainPanel;
   }
   
   /**
    * Enumeration of available pre-defined styles.
    */
   public enum Styles
   {
      BOLD,
      BOLD_RED,
      BOLD_BLUE,
      ITALIC,
      NORMAL      
   }
   
   private StyledDocument m_doc;
   
   /**
    * A reference to the parent frame. Initialized in the
    * ctor. Never <code>null</code> after that.
    */
   private PSPackageInstallerFrame m_parent;
   
   /**
    * Package Description
    */
   private JTextPane m_descTextPane= new JTextPane();
   
   /**
    * Display Message
    */
   private JScrollPane m_descTextAreaScroll;
   
  
   
   
}
