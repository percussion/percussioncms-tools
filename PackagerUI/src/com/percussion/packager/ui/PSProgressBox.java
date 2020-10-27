/******************************************************************************
 *
 * [ PSProgressBox.java ]
 * 
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.packager.ui;

import net.miginfocom.swing.MigLayout;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * A small indeterminate progress box.
 *
 */
public class PSProgressBox extends JDialog
{
   
   public PSProgressBox(Frame frame, String msg, boolean allowCancel)
   {
      super(frame);
      init(msg, allowCancel);
   }
   
   /**
    * Layout dialog and initialize components.
    * @param msg the message, may be <code>null</code> or
    * empty.
    */
   private void init(String msg, boolean allowCancel)
   {
      setTitle(PSResourceUtils.getCommonResourceString("progressTitle"));
      setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
      JPanel main = new JPanel();
      getContentPane().add(main);
      
      MigLayout mainLayout = new MigLayout(
         "wrap 1, hidemode 3",
         "[grow, fill, 280::]",
         "[grow, fill][grow][grow]");
      main.setLayout(mainLayout);
      
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
      setMessage(msg);
      msgPanel.add(m_messageLabel, "grow");
      
      main.add(msgPanel);
      
      m_noteLabel = new JLabel();
      setNote("");
      main.add(m_noteLabel, "grow");
      
      m_bar = new JProgressBar();
      m_bar.setIndeterminate(true);
      main.add(m_bar, "grow");
      
      JPanel cmdPanel = new JPanel();
      MigLayout cmdLayout = new MigLayout(
         "center",
         "[]",
         "[]"
      );
      cmdPanel.setLayout(cmdLayout);
      m_cancelButton = 
         new JButton(PSResourceUtils.getCommonResourceString("label.cancel"));
      m_cancelButton.setMnemonic((int) PSResourceUtils.getCommonResourceString(
            "label.cancel.m").charAt(0));
      m_cancelButton.addActionListener(new ActionListener()
      {
         public void actionPerformed(@SuppressWarnings("unused")
               ActionEvent event)
         {
            onCancel();            
         }
         
      });
      cmdPanel.add(m_cancelButton);
      cmdPanel.setVisible(allowCancel);
      main.add(cmdPanel);
      pack();
      PSUiUtils.center(this);
      setVisible(true);       
      
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
    * Open the progress box and make visible.
    */
   public void open()
   {
      setVisible(true);
   }
   
   /**
    * Close the progress box.
    */
   public void close()
   {
      m_listeners.clear();
      setVisible(false);
   }
   
   /**
    * Action that happens when the cancel button is clicked.
    */
   private void onCancel()
   {
      for(CancelListener cl : m_listeners)
      {
         cl.cancelled();
      }
   }
   
   /**
    * Adds a Cancel listener to this progress box.
    * @param listener cannot be <code>null</code>.
    */
   public void addCancelListener(CancelListener listener)
   {
      if(listener == null)
         throw new IllegalArgumentException("listener cannot be null.");
      if(!m_listeners.contains(listener))
         m_listeners.add(listener);
   }
   
   /**
    * Removes a Cancel listener to this progress box.
    * @param listener cannot be <code>null</code>.
    */
   public void removeCancelListener(CancelListener listener)
   {
      if(listener == null)
         throw new IllegalArgumentException("listener cannot be null.");
      if(m_listeners.contains(listener))
         m_listeners.remove(listener);
   }
   
   /**
    * Interface for listening for a cancel press.
    */
   public interface CancelListener
   {
      /**
       * Called when cancel button is pressed in progress box.
       */
      public void cancelled();
   }
   
   /**
    * @param args
    */
   public static void main(String[] args)
   {
      new PSProgressBox(null, "Test Message", false);

   }
   
   /**
    * List of cancel listeners.
    */
   private List<CancelListener> m_listeners = 
      new ArrayList<CancelListener>();
   
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
   
   /**
    * Initialized in {@link #init(String)},
    *  never <code>null</code> after that.
    */
   private JButton m_cancelButton;
   
      
   

}
