/**[ PSContentTreePanel ]***************************************************************
 *
 * COPYRIGHT (c) 2002 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader.ui;

import com.percussion.loader.IPSProgressListener;
import com.percussion.loader.IPSStatusListener;
import com.percussion.loader.PSProgressEvent;
import com.percussion.loader.PSStatusEvent;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.Timer;


/**
 * A panel with one main component. Displays updates from a
 * background task that is performing a task that sends progress
 * and status events. This panel displays those updates.
 * It uses timer to trigger updating the progress bar, but the timer is not
 * used to update the status.
 */
public class PSProgressPanel extends JPanel implements IPSProgressListener,
   IPSStatusListener, ActionListener

{
   /**
    * Default constructor with no model hook up
    *
    * @param strType the task name of the background worker.
    *    Never <code>null</code> or empty and must be
    *    either <code>SCAN</code> or <code>UPLOAD</code>.
    */
   public PSProgressPanel(String strType)
   {
      super();

      if (ms_res == null)
      {
         ms_res = ResourceBundle.getBundle(
            getClass().getName() + "Resources",
            Locale.getDefault());
      }

      // Init the panel
      initPanel(strType);
   }

   /**
    * Initialize the panel.
    *
    * @param type the type of progress to show, must be one of <code>SCAN</code>
    *    ,<code>UPLOAD</code> or <code>HIDE</code>.
    * @param site the site which is scanned or uploaded, not <code>null</code>
    *    or empty.
    */
   private void initPanel(String type)
   {
      setLayout(new BorderLayout());
      setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

      if (type == null)
         throw new IllegalArgumentException("type cannot be null");

      if (!type.equals(SCAN) && !type.equals(UPLOAD) &&
         !type.equals(HIDE))
         throw new IllegalArgumentException(
            "type must be one of 'scan' or 'upload'");

      m_type = type;
      add(createInfoPanel());
      
      m_timer = new Timer(500, this); // 0.5 second delay
      m_timer.setRepeats(false);
   }
   
   /**
    * Override super method, restart the timer if visible is <code>true</code>.
    * 
    * @see {@link javax.swing.JComponent#setVisible(boolean)}
    */
   public void setVisible(boolean visible)
   {
      if (visible)
         m_timer.restart();
      else
         m_timer.stop();

      super.setVisible(visible);
   }

   // implements IPSProgressListener
   public void progressChanged(PSProgressEvent event)
   {
      if (event == null)
         throw new IllegalArgumentException(
            "event must not be null");

      synchronized (this)
      {
         m_count = event.getCounter();
         m_percent  = event.getPercent();
         m_resource = event.getResourceId();
         m_hasNewProgressData = true;
         
         if (! m_timer.isRunning())
            m_timer.restart(); // restart will purge previous queue if any
      }
   }

   // implements IPSStatusListener
   public void statusChanged(PSStatusEvent event)
   {
      final PSStatusEvent fevent = event;

      Runnable r = new Runnable()
      {
         public void run()
         {
            if (fevent.getStatus() == PSStatusEvent.STATUS_COMPLETED
               || fevent.getStatus() == PSStatusEvent.STATUS_ABORTED)
            {
               setVisible(false);
            }
         }
      };

      SwingUtilities.invokeLater(r);
   }
   
   /**
    * Implementing the interface of ActionListener to update progress 
    * information. This is triggered by the timer.
    * 
    * @param e event send by the timer.
    */
   public void actionPerformed(ActionEvent e)
   {
      synchronized (this)
      {
         if (m_hasNewProgressData)
         {
            if (m_percent == -1 && m_count >= 0)
            {
               m_countLabel.setText(MessageFormat.format(
                  getStringResource("count"),
                  new Object[] { Integer.toString(m_count)}));
            }
            if (m_count == -1 && m_percent >= 0)
            {
               m_countLabel.setText(MessageFormat.format(
                  getStringResource("count"),
                  new Object[] { Integer.toString(m_percent)}));
            }

            /* nice to display resource, but it's not work correctly, 
             * deal with it later.
             * if (m_resource != null)
             * {
             *   m_resourceLabel.setText(MessageFormat.format(
             *      getStringResource("resource"),
             *      new Object[] { m_resource }));
             * }
             */

            if (m_progress != null)
            {
               if (m_percent >= 0)
                  m_progress.setValue(m_percent);
            }
            m_hasNewProgressData = false;
         } 
      } // synchronized()
   }

   /**
    * Creates the info panel, which contains all status information.
    *
    * @return the info panel, never <code>null</code>.
    */
   private JPanel createInfoPanel()
   {
      JPanel panel = new JPanel();
      panel.setLayout(new BorderLayout());

      m_countLabel = new JLabel(MessageFormat.format(
         getStringResource("count"), new Object[]
         { Integer.toString(m_count) }));

      panel.add(m_countLabel, BorderLayout.NORTH);

   /**   m_resourceLabel = new JLabel(MessageFormat.format(
         getStringResource("resource"), new Object[] { m_resource }));
      panel.add(m_resourceLabel, BorderLayout.CENTER);*/

      if (m_type.equals(UPLOAD))
      {
         m_progress = new JProgressBar();
         panel.add(m_progress, BorderLayout.SOUTH);
      }
      return panel;
   }

   /**
    * Creates a new box panel.
    *
    * @param axis the box orientation.
    * @return the box panel, never <code>null</code>.
    */
   private JPanel createBoxPanel(int axis)
   {
      JPanel panel = new JPanel();
      BoxLayout box = new BoxLayout(panel, axis);
      panel.setLayout(box);

      return panel;
   }

   /**
    * Get the aqddressed string resource.
    *
    * @param resource the resource name, assumed not <code>null</code>.
    * @return the resource string, never <code>null</code> or empty.
    */
   private String getStringResource(String resource)
   {
      return ms_res.getString(m_type + "." + resource);
   }

   /**
    * The type for using this dialog to show the scanning progress.
    */
   public static final String SCAN = "scan";

   /**
    * The type for using this dialog to show the uploading progress.
    */
   public static final String UPLOAD = "upload";

   /**
    * The type for using this dialog to hide itself.
    */
   public static final String HIDE = "hide";


   /**
    * The type for which this dialog was initialized, initialized in
    * constructor, never <code>null</code> or changed after that.
    */
   private String m_type = null;

   /**
    * The number of scanned pages. Updated on each
    * <code>PSProgressEvent</code>.
    */
   private int m_count = 0;

   /**
    * The percentage of  uploaded items. Updated on each
    * <code>PSProgressEvent</code>.
    */
   private int m_percent = 0;

   /**
    * The count label, initialized in constructor, updated on each
    * <code>PSProgressEvent</code>.
    */
   private JLabel m_countLabel = null;

   /**
    * The current page scanned or item uploaded. Updated on each
    * <code>PSProgressEvent</code>.
    */
   private String m_resource = "";

   /**
    * The resource label, initialized in constructor, updated on each
    * <code>PSProgressEvent</code>.
    */
   private JLabel m_resourceLabel = null;

   /**
    * The progress bar shown for uploadding processes only. Initialized in
    * constructor, updated on each <code>PSProgressEvent</code>.
    */
   private JProgressBar m_progress = null;

   /**
    * Resource bundle for this class. Initialised in the constructor.
    * It's not modified after that. May be <code>null</code> if it could not
    * load the resource properties file.
    */
   private volatile static ResourceBundle ms_res = null;
   
   /**
    * Determines whether received new progress data, so that we need to 
    * post the data to the progress bar. <code>true</code> if received new data.
    */
   private boolean m_hasNewProgressData = false;

   /**
    * The timer for updating the progress bar. Initialized by the constructor,
    * never <code>null</code> after that.
    */   
   private Timer m_timer;
}