/*[ PSStatusView.java ]********************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader.ui;

import com.percussion.loader.IPSStatusListener;
import com.percussion.loader.PSStatusEvent;

import java.awt.FlowLayout;
import java.awt.Font;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

/**
 * The status view panel displays the current status of a migration process.
 */
public class PSStatusView extends JPanel implements IPSStatusListener
{
   /**
    * Constructs a new status panel.
    */
   public PSStatusView()
   {
      initPanel();
   }

   /**
    * Accessor to read the state.
    * 
    * @return int the state.
    */
   public int getState()
   {
      return m_state;
   }
   
   /**
    * Accessor to read the action.
    * 
    * @return int the action.
    */
   public int getAction()
   {
      return m_action;
   }

   /**
    * Accessor to set single step mode
    * 
    * @param b boolean. <code>true</code> to single step, otherwise
    *    <code>false</code>.
    */
   public void setSingleStep(boolean b)
   {
      m_bSingleStep = b;
   }

   /**
    * Accessor to read if we're single stepping
    */
   public boolean isSingleStep()
   {
      return m_bSingleStep;
   }
   
   /**
    * Returns whether or not there is a next state.
    *
    * @return <code>true</code> if there is a next state, <code>false</code>
    *    otherwise.
    */
   public boolean hasNext()
   {
      return m_state < UPLOAD;
   }

   /**
    * Returns whether or not there is a previous state.
    *
    * @return <code>true</code> if there is a previous state, <code>false</code>
    *    otherwise.
    */
   public boolean hasPrevious()
   {
      return m_state > SCAN;
   }

   /**
    * Resets the status panel.
    */
   public void reset()
   {
      m_state = RESET;
      Logger logger = Logger.getLogger(getClass());      

      removeAll();

      createStatusPanel();
      createProgressPanel();

      validateTree();
   }

   /**
    * Accessor to set the state and performs an update for
    * this panel
    * 
    * @param nState the new state. Must be one of <code>SCAN</code>, 
    *    <code>UPLOAD</code> or <code>RESET</code>. Does nothing if state is
    *    unknown.
    */
   public void setState(int nState)
   {
      if (nState == SCAN || nState == UPLOAD ||
         nState == RESET)
      {                  
         m_state = nState;
         update();            
         validateTree();
      }
   }
   
   /**
    * Accessor to set the action and performs an update for
    * this panel
    * 
    * @param nAction teh new action, must be one of <code>ACTION_NONE</code>,
    *    <code>ACTION_START</code> or <code>ACTION_COMPLETE</code>. Does
    *    nothing if the action is unknown.
    */
   public void setAction(int nAction)
   {
      if (nAction == ACTION_NONE || nAction == ACTION_START ||
         nAction == ACTION_COMPLETE)
      {         
         m_action = nAction;
         update();            
         validateTree();
      }
   }

   /**
    * Update the status panel for the supplied state.
    *
    * @param event a PSStatusEvent to check. Never <code>null</code>
    */
   private void update(PSStatusEvent event)
   {
      if (event == null)
         throw new IllegalArgumentException(
            "event must not be null");
                  
      int nState = event.getStatus();
      int nProcess = event.getProcessId();
            
      if (nState == PSStatusEvent.STATUS_ABORTED)
      {
         reset();
         return;
      }               

      if (nState == PSStatusEvent.STATUS_COMPLETED &&
         event.getProcessId() == PSStatusEvent.PROCESS_SCANNING 
         && m_state == SCAN)
      {        
         setAction(ACTION_COMPLETE);
      }
         // We're not getting a PROCESS_LOADING_CONTENTS Completed Event ??
      else if (nState == PSStatusEvent.STATUS_COMPLETED &&
         event.getProcessId() == PSStatusEvent.PROCESS_LOADING_CONTENTS            
         && m_state == UPLOAD)
      {         
         setAction(ACTION_COMPLETE);
      }
      else if (nState == PSStatusEvent.STATUS_ABORTED)
      {         
         setAction(ACTION_NONE);
      }
   }

   /**
    * Increments the state if we are able.
    */
   public void moveNext()
   {
      if (hasNext())
      {
         int nState = getState();
         setState(++nState);
      }      
   }
   
   /**
    * Update the status panel for the supplied state.
    *    
    */
   private void update()
   {
      removeAll();

      createStatusPanel();
      createProgressPanel();      
   }

   /**
    * Initializes the status panel.
    */
   private void initPanel()
   {
      m_res = ResourceBundle.getBundle(
         getClass().getName() + "Resources", Locale.getDefault());

      m_pendingIcon = new ImageIcon(getClass().getResource(
         PSContentLoaderResources.getResources().getString("gif_empty24")));

      m_currentIcon = new ImageIcon(getClass().getResource(
         PSContentLoaderResources.getResources().getString("gif_current")));

      m_doneIcon = new ImageIcon(getClass().getResource(
         PSContentLoaderResources.getResources().getString("gif_done")));

      setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
      // Main panel x axis
      setLayout(new BoxLayout(this, BoxLayout.X_AXIS));      
         
      createStatusPanel();
      createProgressPanel();            
   }

   /**
    * Init the progress panel
    */
   private void createProgressPanel()
   {                  
      switch (m_state)
      {
         case SCAN:            
            m_progressPanel = new PSProgressPanel(PSProgressPanel.SCAN);  
            
            if (m_action == ACTION_COMPLETE)
               m_progressPanel.setVisible(false);
            else 
               m_progressPanel.setVisible(true);
            break;
         
         case UPLOAD:
            m_progressPanel = new PSProgressPanel(PSProgressPanel.UPLOAD);
            if (m_action == ACTION_COMPLETE)
               m_progressPanel.setVisible(false);
            else 
               m_progressPanel.setVisible(true);
            break;
         
         default:
            m_progressPanel = new PSProgressPanel(PSProgressPanel.HIDE);
            m_progressPanel.setVisible(false);
            break;
      }
      
      add(m_progressPanel);
   }

   /**
    * Init the status panel
    */
   private void createStatusPanel()
   {
      // Each sub panel y axis
      JPanel p = new JPanel();
      BoxLayout box = new BoxLayout(p, BoxLayout.Y_AXIS);
      p.setLayout(box);
            
      switch (m_state)
      {
         case SCAN:
            if (m_action == ACTION_START)
            {
               p.add(createCurrentPanel(m_res.getString("status.scan")));
               p.add(createPendingPanel(m_res.getString("status.upload")));
            }
            else if (m_action == ACTION_COMPLETE)
            {
               p.add(createDonePanel(m_res.getString("status.scan")));
               p.add(createPendingPanel(m_res.getString("status.upload")));
            }
            break;
         case UPLOAD:
            if (m_action == ACTION_START)
            {
               p.add(createPendingPanel(m_res.getString("status.scan")));
               p.add(createCurrentPanel(m_res.getString("status.upload")));     
            }
            else if (m_action == ACTION_COMPLETE)
            {
               p.add(createPendingPanel(m_res.getString("status.scan")));
               p.add(createDonePanel(m_res.getString("status.upload")));     
            }
            break;
         default:            
            p.add(createPendingPanel(m_res.getString("status.scan")));
            p.add(createPendingPanel(m_res.getString("status.upload")));      
            break;
      }
            
      add(p);      
   }
   
   /**
    * Creates a pending status panel for the supplied resource.
    *
    * @param resource the status resource string, assumed not <code>null</code>.
    * @return the panel, never <code>null</code>.
    */
   private JPanel createPendingPanel(String resource)
   {
      JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
      panel.add(new JLabel(m_pendingIcon));

      JLabel status = new JLabel(resource);
      panel.add(status);

      return panel;
   }
   
   /**
    * Creates a current status panel for the supplied resource.
    *
    * @param resource the status resource string, assumed not <code>null</code>.
    * @return the panel, never <code>null</code>.
    */
   private JPanel createCurrentPanel(String resource)
   {
      JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
      panel.add(new JLabel(m_currentIcon));

      JLabel status = new JLabel(resource + "...");
      Font font = status.getFont();
      status.setFont(new Font(font.getName(), Font.BOLD, font.getSize()));
      panel.add(status);

      return panel;
   }

   /**
    * Creates a done status panel for the supplied resource.
    *
    * @param resource the status resource string, assume not <code>null</code>.
    * @return the panel, never <code>null</code>.
    */
   private JPanel createDonePanel(String resource)
   {
      JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
      panel.add(new JLabel(m_doneIcon));

      JLabel status = new JLabel(resource);
      panel.add(status);

      return panel;
   }

   // implements IPSStatusListener
   public void statusChanged(PSStatusEvent event)
   {     
      final PSStatusEvent fevent = event;

      Runnable r = new Runnable()
      {
         public void run()
         {
            update(fevent);
         }
      };   
      
      SwingUtilities.invokeLater(r);                        
   }

   /**
    * Accessor to get the progress panel.
    */
   public PSProgressPanel getProgressPanel()
   {
      return m_progressPanel;
   }
   
   /**
    * The initial state, also the reset state.
    */
   public static final int RESET = 0;

   /**
    * The scanning state.
    */
   public static final int SCAN = 1;

   /**
    * The reviewing content state.
    */
  // public static final int REVIEW_CONTENT = 2;

   /**
    * The uploading state.
    */
   public static final int UPLOAD = 2;

   /**
    * The reviewing log state.
    */
   //public static final int REVIEW_LOG = 4;

   // behavior public defines
   public static final int ACTION_START = 101;
   public static final int ACTION_COMPLETE = 102;
   public static final int ACTION_NONE = 103;

   /**
    * The icon shown for pending states. Initialized in constructor, never
    * <code>null</code> or changed after that.
    */
   private ImageIcon m_pendingIcon = null;

   /**
    * The icon shown for current states. Initialized in constructor, never
    * <code>null</code> or changed after that.
    */
   private ImageIcon m_currentIcon = null;

   /**
    * The icon shown for done states. Initialized in constructor, never
    * <code>null</code> or changed after that.
    */
   private ImageIcon m_doneIcon = null;

   /**
    * The current state shown. Initialized in constructor, updated on each call
    * to <code>reset()</code>, <code>next()</code> or <code>previous()</code>.
    */
   private int m_state = RESET;

   /**
    * The current action shown. Initialized in constructor, updated on each call
    * to <code>reset()</code>, <code>next()</code> or <code>previous()</code>.
    */
   private int m_action = ACTION_NONE;

   /**
    * The resource bundle of this panel. Initialized in constructor, never
    * <code>null</code> or changed after that.
    */
   private ResourceBundle m_res = null;

   /**
    * PSProgressPanel, initialized in ctor, Never <code>null</code>. Is 
    * visually hidden based on state of processing.
    */
   private PSProgressPanel m_progressPanel = null;

   /**
    * Determines the how we step throgh our process. 
    * <code>true</code> if we are single stepping, otherwise 
    * <code>false</code>
    */
   private boolean m_bSingleStep = true;
}
