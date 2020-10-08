/*[ PSLogPanel.java ]***********************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader.ui;

import com.percussion.guitools.PSResources;
import com.percussion.loader.IPSLogListener;
import com.percussion.loader.IPSStatusListener;
import com.percussion.loader.PSContentLoaderApp;
import com.percussion.loader.PSItemContext;
import com.percussion.loader.PSLogDispatcher;
import com.percussion.loader.PSLogMessage;
import com.percussion.loader.PSStatusEvent;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;

/**
 * A log panel with two main components. A tabbed pane in which all tabs show
 * logging information for the different states and a toolbar to navigate
 * between errors.
 */
public class PSLogPanel extends JPanel
   implements ActionListener, IPSLogListener, IPSStatusListener
{
   /**
    * Constructs the log panel for the supplied dispatcher.
    *
    * @param logDispatcher to dispatcher with which to register this to receive
    *    logging events, not <code>null</code>.
    */
   public PSLogPanel(PSLogDispatcher logDispatcher)
   {
      if (logDispatcher == null)
         throw new IllegalArgumentException("logDispatcher cannot be null");

      if (ms_res == null)
         ms_res = ResourceBundle.getBundle(getClass().getName() + "Resources",
            Locale.getDefault());

      setLayout(new BorderLayout());

      m_pane.setTabPlacement(SwingConstants.BOTTOM);

      String tabName =
         PSContentLoaderResources.getResourceString(ms_res, "tab.scan");
      addTab(tabName);
      m_pane.setToolTipTextAt(m_pane.indexOfTab(tabName),
         PSContentLoaderResources.getResourceString(ms_res, "tab.scan.tip"));

      tabName =
         PSContentLoaderResources.getResourceString(ms_res, "tab.upload");
      addTab(tabName);
      m_pane.setToolTipTextAt(m_pane.indexOfTab(tabName),
         PSContentLoaderResources.getResourceString(ms_res, "tab.upload.tip"));

      initToolbar();

      m_pane.setSelectedIndex(m_pane.indexOfTab(
         PSContentLoaderResources.getResourceString(ms_res, "tab.scan")));

      JPanel panel = new JPanel();
      BoxLayout box = new BoxLayout(panel, BoxLayout.X_AXIS);
      panel.setLayout(box);
      panel.add(m_pane);
      panel.add(m_toolbar);

      //JScrollPane scroll = new JScrollPane(panel);
      add(panel, BorderLayout.CENTER);

      logDispatcher.addLogListener(this);
   }

   /**
    * Updates the appender for this panel
    */
   public void updateAppender()
   {
      Logger logger = Logger.getRootLogger();
      PSLogDispatcher appender =
         (PSLogDispatcher) logger.getAppender(PSMainFrame.LOG_DISPATCHER);
      appender.addLogListener(this);
   }

   /**
    * Adds a new Tab with the supplied name.
    *
    * @param name the name for the new tab, assumed not <code>null</code>.
    */
   private void addTab(String name)
   {
      DefaultListModel model = new DefaultListModel();
      m_models.put(model, name);
      model.addListDataListener(new ListDataListener()
      {
         // implement ListDataListener
         public void contentsChanged(ListDataEvent event)
         {
            // no-op
         }

         // implement ListDataListener
         public void intervalAdded(ListDataEvent event)
         {
            DefaultListModel m = (DefaultListModel) event.getSource();
            int index = m_pane.indexOfTab((String) m_models.get(m));

            if (index >= 0)
               m_pane.setEnabledAt(index, !m.isEmpty());
         }

         // implement ListDataListener
         public void intervalRemoved(ListDataEvent event)
         {
            DefaultListModel m = (DefaultListModel) event.getSource();
            int index = m_pane.indexOfTab((String) m_models.get(m));
            if (index >= 0)
               m_pane.setEnabledAt(index, !m.isEmpty());
         }
      });
      JList list = new JList(model);
      list.setSelectionModel(new SingleSelectionModel());
      list.setCellRenderer(new LogRenderer());
      JScrollPane pane = new JScrollPane(list);

      m_pane.addTab(name, pane);
      m_pane.setEnabledAt(m_pane.indexOfTab(name), false);
   }

   /**
    * Initializes the error toolbar. This will contain two buttons to go to the
    * previous of next error in the currently selected Tab.
    */
   private void initToolbar()
   {
      m_toolbar.setOrientation(SwingConstants.VERTICAL);

      PSResources res = PSContentLoaderResources.getResources();

      ImageIcon ic = new ImageIcon(getClass().getResource(
         res.getString("gif_previous")));
      JButton btn = new JButton();
      btn.setActionCommand(PREVIOUS);
      btn.setToolTipText(res.getString("tt_previousError"));
      btn.addActionListener(this);
      btn.setIcon(ic);
      m_toolbar.add(btn);

      ic = new ImageIcon(getClass().getResource(
         res.getString("gif_next")));
      btn = new JButton();
      btn.setToolTipText(res.getString("tt_nextError"));
      btn.setActionCommand(NEXT);
      btn.addActionListener(this);
      btn.setIcon(ic);
      m_toolbar.add(btn);
   }

   // implementation for ActionListener
   public void actionPerformed(ActionEvent e)
   {
      Logger logger = Logger.getLogger(getClass());

      Object o = e.getSource();
      String command = ((JButton) e.getSource()).getActionCommand();

      if (command.equals(PREVIOUS))
      {
         if (m_nCurrErr == 0)
            m_nCurrErr = m_errorData.size();

         m_nCurrErr--;
         highlightError();
      }
      else if (command.equals(NEXT))
      {
         m_nCurrErr++;
         highlightError();
      }
   }

   // implementation for IPSLogListener
   public void logReceived(LoggingEvent event, Appender appender)
   {
      if (event == null)
         throw new IllegalArgumentException("event cannot be null");

      if (appender == null)
         throw new IllegalArgumentException("appender cannot be null");

      if (event.getLevel() == Level.DEBUG
         && !PSContentLoaderApp.getInstance().isDebugOn())
         return;

      final LoggingEvent fevent = event;
      final Appender fappender = appender;

      Runnable r = new Runnable()
      {
         public void run()
         {
            String message = fappender.getLayout().format(fevent);
            Object o = fevent.getMessage();

            JScrollPane scroll = (JScrollPane) m_pane.getSelectedComponent();
            JViewport port = (JViewport) scroll.getComponent(0);
            JList list = (JList) port.getView();
            DefaultListModel model = (DefaultListModel) list.getModel();

            if (o instanceof PSLogMessage)
            {
               PSLogMessage msg = (PSLogMessage) o;
               // Add to the list model
               model.addElement(msg);

               // Add item to hash for fast lookup
               PSItemContext item = msg.getItemContext();

               if (item != null)
               {
                  if (msg.getLevel() == PSLogMessage.LEVEL_ERROR ||
                      msg.getLevel() == PSLogMessage.LEVEL_FATAL)
                  {
                     m_errorData.addElement(msg);
                  }
                  else
                  {
                     m_data.put(item.getResourceId(), msg);
                  }
               }
            }
            else
            {
               model.addElement(message);
            }

            int first = list.getFirstVisibleIndex();
            int last = list.getLastVisibleIndex();
            Rectangle bounds = list.getCellBounds(first+1, last+1);

            if (bounds != null)
               port.scrollRectToVisible(bounds);
         }
      };

      SwingUtilities.invokeLater(r);
   }

   /**
    * Selects in the list model the next error
    */
   private void highlightError()
   {
      // Threshold
      if (m_errorData.size() < 1 || m_nCurrErr < 0)
      {
         m_nCurrErr = -1;
         return;
      }

      JList l = getList();
      PSLogMessage m = (PSLogMessage) m_errorData.elementAt(
         (m_nCurrErr % m_errorData.size()));
      l.setSelectedValue(m, true);
   }

   // implementation for IPSStatusListener
   public void statusChanged(PSStatusEvent event)
   {
      final PSStatusEvent fEvent = event;

      Runnable r = new Runnable()
      {
         public void run()
         {
            Logger logger = Logger.getLogger(getClass());

            switch (fEvent.getProcessId())
            {
               case PSStatusEvent.PROCESS_SCANNING:
                  m_pane.setSelectedIndex(m_pane.indexOfTab(
                     PSContentLoaderResources.getResourceString(
                     ms_res, "tab.scan")));
                  break;

               case PSStatusEvent.PROCESS_LOADING_CONTENTS:
                  m_pane.setSelectedIndex(m_pane.indexOfTab(
                     PSContentLoaderResources.getResourceString(
                     ms_res, "tab.upload")));
                  break;

               case PSStatusEvent.PROCESS_MARKING_TREE:
               case PSStatusEvent.PROCESS_MANAGER:
                  break;

               default:
                  logger.debug(
                     "Received unknown process ID in status changed event.");

            }
            print(fEvent);
         }
      };

      SwingUtilities.invokeLater(r);
   }

   /**
    * Used to log a status event or progress event.
    *
    * @param e PSStatusEvent. Never <code>null</code>.
    */
   private void print(PSStatusEvent e)
   {
      Logger logger = Logger.getLogger(getClass());
      logger.info(e.toDescription());
   }

   /**
    * Used to clear the log and associated data
    */
   public void clearAll()
   {
      m_data.clear();
      m_errorData.removeAllElements();
      m_models.clear();
      m_nCurrErr = -1;

      JList list = getList();
      if (list != null)
      {
         DefaultListModel model = (DefaultListModel) list.getModel();
         model.clear();
      }
   }

   /**
    * Visually 'select' the list entry that encapsulates this
    * <code>item</code>.
    *
    * @param item PSItemContext to select. Never <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>item</code> is
    *    <code>null</code>
    */
   public void highlightNode(PSItemContext item)
   {
      if (item == null)
         throw new IllegalArgumentException(
            "item must not be null");

      JList list = getList();

      // find the PSLogMessage that corresponds to
      // this item
      PSLogMessage msg = (PSLogMessage) m_data.get(item.getResourceId());

      if (msg != null)
      {
         list.setSelectedValue(msg, true);
      }
      else
      {
         clearListSelection();
      }
   }

   /**
    * Convience method
    */
   private JList getList()
   {
      JScrollPane pane = (JScrollPane) m_pane.getSelectedComponent();
      JViewport c = (JViewport) pane.getComponent(0);
      return (JList) c.getView();
   }

   /**
    * Clear the list selection
    */
   public void clearListSelection()
   {
      JList list = getList();
      list.clearSelection();
   }

   /**
    * Create a SINGLE_SELECTION ListSelectionModel that calls a new
    * method, updateSingleSelection(), each time the selection
    * changes.  This can be a little bit more convienent than using the
    * ListModels ListSelectionListener, since ListSelectionListeners
    * are only given the range of indices that the change spans.
    */
   class SingleSelectionModel extends DefaultListSelectionModel
   {
      /**
       * creates the model and sets the mode to single selection.
       */
      public SingleSelectionModel()
      {
         setSelectionMode(SINGLE_SELECTION);
      }

      //overrides this method defined in DefaultListSelectionModel.
      public void setSelectionInterval(int index0, int index1)
      {
         super.setSelectionInterval(index0, index1);
         JList list = getList();
         Object o = list.getSelectedValue();

         // Threshold
         if (o == null)
            return;

         if (o instanceof PSLogMessage)
         {
            PSLogMessage msg = (PSLogMessage) o;
            PSMainFrame.getFrameNoAction().actionPerformed(
               new ActionEvent(msg, PSMainFrame.HIGHLIGHT_TREE,
               PSMainFrame.HIGHLIGHT_TREE_CMD, 0));
         }
         else
         {
            // Clear the selection to avoid confusion
            PSMainFrame.getFrameNoAction().actionPerformed(
               new ActionEvent(o, PSMainFrame.HIGHLIGHT_TREE,
               PSMainFrame.HIGHLIGHT_TREECLEAR_CMD, 0));
         }
      }
   }

  /**
   * List cell renderer for the JList view.
   */
   class LogRenderer extends JTextArea implements ListCellRenderer
   {
      public LogRenderer()
      {
         super();
         m_noFocusBorder = new EmptyBorder(1, 2, 1, 2);
         setLineWrap(true);
         setWrapStyleWord(true);
         setOpaque(true);
         setBorder(m_noFocusBorder);
      }

      // This is the only method defined by ListCellRenderer.
      // We just reconfigure the JLabel each time we're called.
      public Component getListCellRendererComponent(
         JList list,
         Object value,            // value to display
         int index,               // cell index
         boolean isSelected,      // is the cell selected
         boolean cellHasFocus)    // the list and the cell have the focus
      {
         PSLogMessage msg = null;
         String text = "";
         boolean bErr = false;
         boolean bWarn = false;

         if (value instanceof PSLogMessage)
         {
            msg = (PSLogMessage) value;
            text = msg.getMessage();

            if ((msg.getLevel() == PSLogMessage.LEVEL_ERROR) ||
                (msg.getLevel() == PSLogMessage.LEVEL_FATAL) )
            {
               bErr = true;
            }
            if(msg.getLevel() == PSLogMessage.LEVEL_WARN)
               bWarn = true;
         }
         else
         {
            text = value.toString();
         }

         if (isSelected)
         {
            setForeground(list.getSelectionForeground());
            setBackground(list.getSelectionBackground());
         }
         else
         {
            setForeground(list.getForeground());
            setBackground(list.getBackground());
         }

         setFont(list.getFont());

         // Set error foreground color
         // is overriden by selection above
         if (bErr && !isSelected)
         {
            setForeground(Color.red);
         }
         else if(bWarn && !isSelected)
         {
            setBackground(Color.yellow);
         }

         if (cellHasFocus)
         {
            setBorder(UIManager.getBorder("List.focusCellHighlightBorder"));
         }
         else
         {
            setBorder(m_noFocusBorder);
         }

         this.setText(text);

         return this;
      }

      /**
       * Border used by graphic object representing a cell in the list,
       * initialized in ctor. Never <code>null</code>.
       */
      protected Border m_noFocusBorder;
   }

   /**
    * The tabbed pane that will hold all logging tabs. Never <code>null</code>.
    */
   private JTabbedPane m_pane = new JTabbedPane();

   /**
    * Toolbar for this frame. Never <code>null</code>.
    */
   private JToolBar m_toolbar = new JToolBar();

   /**
    * Resource bundle for this class. Initialised in the constructor.
    * It's not modified after that. May be <code>null</code> if it could not
    * load the resource properties file.
    */
   private static ResourceBundle ms_res = null;

   /**
    * A map of <code>javax.swing.DefaultListModel</code> objects as key and
    * the tab name as value. Initialized in {#addTab(String)}. Never
    * <code>null</code>, might be empty.
    */
   private Map m_models = new HashMap();

   /**
    * Transient data that represents the list model. Used for fast lookups
    * on selection. Never <code>null</code>
    */
   private transient Hashtable m_data = new Hashtable();

   /**
    * Transient data that represents the list model. Used for
    * navigating through the error messages. Never <code>null</code>
    */
   private transient Vector m_errorData = new Vector();

   /**
    * The current index of the error message in the log. Defaults to -1
    * for no selection.
    */
   private int m_nCurrErr = -1;

   /**
    * The constant to use for getting the related resources(label, mnemonic,
    * accelerator key, image), the component name and action command of the
    * tool bar item indicating previous button.
    */
   public static final String PREVIOUS = "previousError";

   /**
    * The constant to use for getting the related resources(label, mnemonic,
    * accelerator key, image), the component name and action command of the
    * tool bar item indicating previous button.
    */
   public static final String NEXT = "nextError";
}