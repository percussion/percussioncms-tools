/******************************************************************************
 *
 * [ PSProgressView.java ]
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

/**
 * @author erikserating
 *
 */
public class PSProgressView extends JPanel
{
   public PSProgressView(PSPackageInstallerFrame frame)
   {
      m_parent = frame;
      init();
   }
   
   private void init()
   {
      MigLayout layout = new MigLayout(
      "fill",
      "[]",
      "100[][400]");
      setLayout(layout);
      add(getMainPanel(), "growx");
   }
   
   /*
    * Sets Main Panel
    */
   private JPanel getMainPanel()
   {
      JPanel mainPanel = new JPanel();
      MigLayout mainLayout = new MigLayout(
            "top, wrap 1, fillx, hidemode 3",
            "[280::]",
            "[][][]");
      mainPanel.setLayout(mainLayout);
      
      JPanel msgPanel = new JPanel();      
      MigLayout msgLayout = new MigLayout(
         "fill",
         "[][grow]",
         "[grow]");
      msgPanel.setLayout(msgLayout);
      
      m_iconLabel = 
         new JLabel(UIManager.getIcon("OptionPane.informationIcon"));
      msgPanel.add(m_iconLabel);
      
      m_messageLabel = new JLabel();
      msgPanel.add(m_messageLabel, "growx");
      
      mainPanel.add(msgPanel);
      
      m_noteLabel = new JLabel();
      setNote("");
      mainPanel.add(m_noteLabel, "growx");
      
      m_bar = new JProgressBar();
      m_bar.setIndeterminate(true);
      mainPanel.add(m_bar, "growx");
      return mainPanel;
   }
   
   /**
    * Set the message for the progress box. 
    * @param msg the message. May be
    * <code>null</code> or empty.
    */
   public void setMessage(String msg)
   {
      String str = "<html>" + StringUtils.defaultString(msg, " ") + "</html>";
      m_messageLabel.setText(str);
   }
   
   /**
    * Set the progress note.  
    * @param note May be
    * <code>null</code> or empty.
    */
   public void setNote(String note)
   {
      String str = "<html>" + 
         StringUtils.defaultString(note, "Please wait...") + "</html>";
      m_noteLabel.setText(str);
   } 
   
   /**
    * A reference to the parent frame. Initialized in the
    * ctor. Never <code>null</code> after that.
    */
   private PSPackageInstallerFrame m_parent;
   
   /**
    *  Initialized in {@link #init(String)},
    *  never <code>null</code> after that.
    */
   private JLabel m_messageLabel;
   
   /**
    * Initialized in {@link #init(String)},
    *  never <code>null</code> after that.
    */
   private JLabel m_noteLabel;
   
   /**
    * Initialized in {@link #init(String)},
    *  never <code>null</code> after that.
    */
   private JLabel m_iconLabel;
   
   /**
    * Initialized in {@link #init(String)},
    *  never <code>null</code> after that.
    */
   private JProgressBar m_bar;
}
