/******************************************************************************
 *
 * [ PSViewPanel.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.deployer.ui;

import com.percussion.error.PSDeployException;
import com.percussion.guitools.PSTableSorter;
import com.percussion.guitools.StatusBar;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * The panel to represent the respective view of the selected node in
 * the servers tree of the browser panel
 */
public class PSViewPanel extends JPanel implements IPSViewDataChangeListener
{
   /**
     * Constructs the view panel. Initializes the handler map for supported
    * views. Supports the following {@link #TYPE_VIEW_REPOSITORY repository},
    * {@link #TYPE_VIEW_SERVER server}, {@link #TYPE_VIEW_DESCRIPTORS
    * descriptors}, {@link #TYPE_VIEW_ARCHIVES archivelogs}, {@link
    * #TYPE_VIEW_PACKAGES packagelogs} and {@link #TYPE_VIEW_NOTHING Nothing}
    * views. Initializes the panel ui.
    */
   public PSViewPanel()
   {
      m_viewHandlerMap.put(new Integer(TYPE_VIEW_REPOSITORY),
         new PSRepositoryViewHandler());
      m_viewHandlerMap.put(new Integer(TYPE_VIEW_SERVER),
         new PSServerViewHandler());
      m_viewHandlerMap.put(new Integer(TYPE_VIEW_DESCRIPTORS),
         new PSDescriptorViewHandler());
      m_viewHandlerMap.put(new Integer(TYPE_VIEW_ARCHIVES),
         new PSArchiveViewHandler());
      m_viewHandlerMap.put(new Integer(TYPE_VIEW_PACKAGES),
         new PSPackageViewHandler());
      init();
   }

   /**
    * Initializes the panel with a status bar on top and a panel to hold the
    * table in the center. Adds listeners to the table to allow sorting and
    * display popup menu.
    */
   private void init()
   {
      setLayout(new BorderLayout());
      m_statusBar = new StatusBar("");
      m_tableSorter = new PSTableSorter(new DefaultTableModel());
      m_table = new JTable(m_tableSorter);
      m_table.setDefaultRenderer(Date.class, new DateRenderer());
      JScrollPane tablePanel = new JScrollPane(m_table);
      tablePanel.setPreferredSize(new Dimension (300, 200));

      add(m_statusBar, BorderLayout.NORTH);
      add(tablePanel, BorderLayout.CENTER);

      m_tableSorter.addMouseListenerToHeaderInTable(m_table);
      
      m_table.getSelectionModel().setSelectionMode(
         ListSelectionModel.SINGLE_SELECTION);

      //Adds a mouse listener to show the pop-up menu if the current view
      //handler has the pop-up menu to show on the table based on the clicked
      //row.
      m_table.addMouseListener( new MouseAdapter()
      {
         public void mouseReleased(MouseEvent event)
         {
            if(event.isPopupTrigger())
            {
               if(m_curViewHandler.hasPopupMenu())
               {
                  int row = m_table.rowAtPoint(event.getPoint());
                  // get the row index that contains that coordinate
                  int rowNumber = m_table.rowAtPoint(event.getPoint());
                  // set the selected row by using the "rowNumber"
                  m_table.getSelectionModel().setSelectionInterval( rowNumber, rowNumber );
                  //Get the actual model row
                  row = m_tableSorter.getModelRow(row);
                  m_curViewHandler.getPopupMenu(row).show(
                     m_table, event.getX(), event.getY());
               }
            }
         }
         // Gets the row which has been double clicked.
         public void mouseClicked(MouseEvent event)
         {
            if( ((event.getModifiers() & InputEvent.BUTTON1_MASK) ==
               InputEvent.BUTTON1_MASK) &&  event.getClickCount() == 2 )
            {
               if(m_curViewHandler.supportsDetailView())
               {
                  int row = m_table.rowAtPoint(event.getPoint());
                  //Get the actual model row
                  if (row != -1)
                  {
                     row = m_tableSorter.getModelRow(row);
                     m_curViewHandler.showDetailView(row);
                  }
               }
            }
         }
      });
      
   }

   /**
    * Gets the view handler for the specified type.
    *
    * @param type the view type, assumed to be one of the TYPE_VIEW_xxx values
    * except <code>TYPE_VIEW_NOTHING</code>
    *
    * @return the view handler, never <code>null</code>
    */
   private IPSViewHandler getHandler(int type)
   {
      return (IPSViewHandler)m_viewHandlerMap.get(new Integer(type));
   }

   /**
    * Updates the current view of the panel as specified by the view type.
    * Should be called whenever the selection changes in the servers tree of the
    * browser panel. Displays error dialog if an exception happens in extracting
    * required data from the supplied object. Adds itself as the listener to
    * data change in the <code>object</code> and removes as listener from the
    * old object this view was representing if they support notifying .
    *
    * @param viewType The type of view that need to be shown in the panel, must
    * be one of the TYPE_VIEW_xxx values
    * @param object The data object to set on the view handler, must be an
    * instance of supported objects of the specified view type.
    *
     * @throws IllegalArgumentException if any param is invalid.
    */
   public void updateView(int viewType, Object object)
   {
      if(viewType != TYPE_VIEW_REPOSITORY &&
         viewType != TYPE_VIEW_SERVER &&
         viewType != TYPE_VIEW_DESCRIPTORS &&
         viewType != TYPE_VIEW_ARCHIVES &&
         viewType != TYPE_VIEW_PACKAGES &&
         viewType != TYPE_VIEW_NOTHING)
         throw new IllegalArgumentException("viewType is not a supported type");

      m_curView = viewType;

      IPSViewHandler oldHandler = m_curViewHandler;

      if(viewType != TYPE_VIEW_NOTHING)
      {
         IPSViewHandler handler = getHandler(viewType);

         if(!handler.supportsObject(object))
            throw new IllegalArgumentException(
               "object is not a supported instance of the specified view type."
               );

         m_curViewHandler = handler;
      }
      else
      {
         m_curViewHandler.removeSelectedData();
         m_curViewHandler = null;
      }

      //remove data change listener on the old object that this view was
      //representing.
      if(oldHandler != null)
      {
         Object oldData = oldHandler.getData();
         if(oldData instanceof IPSDataChangeNotifier)
            ((IPSDataChangeNotifier)oldData).removeDataChangeListener(this);
      }

      //add this as listener to data change if the object supports notifying.
      if(object instanceof IPSDataChangeNotifier)
         ((IPSDataChangeNotifier)object).addDataChangeListener(this);

      updateView(object, true);
   }

   //implements interface method
   public void dataChanged(final Object object)
   {
      if(m_curViewHandler != null)
      {
         if(!m_curViewHandler.supportsObject(object))
            throw new IllegalArgumentException(
               "object is not a supported instance of current view.");
      }
      SwingUtilities.invokeLater(new Runnable()
      {
         public void run()
         {
            updateView(object, false);
         }
      });
   }

   /**
    * Updates the view handler with supplied data object unless the current view
    * represents <code>TYPE_VIEW_NOTHING</code>. In case of <code>
    * TYPE_VIEW_NOTHING</code> sets the empty label and a table model with no
    * columns on the table. Extracts the label and the table model from the
    * current view handler and updates the status bar and table. Displays an
    * error dialog and simply returns in case of an exception extracting
    * required view data by handler.
    *
    * @param object the object assumed to be a supported instance of the current
    * view handler.
    * @param isTableChange supply <code>true</code> to indicate table structure 
    * changed, <code>false</code> to only data changed.
    */
   private void updateView(Object object, boolean isTableChange)
   {
      String label = "";
      TableModel tableModel = new DefaultTableModel();
      if(m_curViewHandler != null)
      {
         try {
            m_curViewHandler.setData(object);
         }
         catch(PSDeployException e)
         {
            PSDeploymentClient.getErrorDialog().showError(e, false, null);
            return;
         }
         label = m_curViewHandler.getViewLabel();         
         
         if(m_curViewHandler.getTableModel() != null) 
            tableModel = m_curViewHandler.getTableModel(); 
      }

      m_statusBar.setMessage(label);
      m_tableSorter.setModel(tableModel, isTableChange);

   }

   /**
    * The table that needs to be shown if the view supports details in tabular
    * format, initialized in <code>init()</code> method and never <code>null
    * </code> or modified after that.
    */
   private JTable m_table;

   /**
    * The table sorter model that is used to sort the columns data represented
    * by this view, initialized in <code>init()</code> method and never <code>
    * null</code> after that. The data model of this is changed as the view
    * changes.
    */
   private PSTableSorter m_tableSorter;

   /**
    * The status bar to display the view label, never <code>null</code> or
    * modified after construction.
    */
   private StatusBar m_statusBar;

   /**
    * The map of view handlers for each supported type. Initialized in the
    * constructor and never <code>null</code> or modified after that.
    */
   private Map m_viewHandlerMap = new HashMap();

   /**
    * The type which represents the current view of the panel, initialized to
    * <code>TYPE_VIEW_NOTHING</code> and gets updated whenver the view is
    * changed by a call to <code>updateView(int, Object)</code>
    */
   private int m_curView = TYPE_VIEW_NOTHING;

   /**
    * The handler that is handling the current view of this panel, initialized
    * to <code>null</code> and gets updated whenever the view is changed by a
    * call to <code>updateView(int, Object)</code>
    */
   private IPSViewHandler m_curViewHandler = null;

   /**
    * The constant to represent the 'repository' view. Displays the repository
    * information (driver, server, database and origin) in the status bar.
    * Supported by {@link PSRepositoryViewHandler}.
    */
   public static final int TYPE_VIEW_REPOSITORY = 0;

   /**
    * The constant to represent the 'server' view. Displays the server
    * information (name, port, version and build) in the status bar.
    * Supported by {@link PSServerViewHandler}.
    */
   public static final int TYPE_VIEW_SERVER = 1;

   /**
    * The constant to represent the 'descriptors' view. Displays the server
    * information (name, port, version and build) in the status bar and displays
    * the export descriptors exist on that server in a table with details.
    * Supported by {@link PSDescriptorViewHandler}.
    */
   public static final int TYPE_VIEW_DESCRIPTORS = 2;

   /**
    * The constant to represent the 'archives' view. Displays the server
    * information (name, port, version and build) in the status bar and displays
    * the installed archive logs exist on that server in a table with details.
    * If an archive is installed multiple times, this view will have respective
    * entries in the table. Supported by {@link PSArchiveViewHandler}.
    */
   public static final int TYPE_VIEW_ARCHIVES = 3;

   /**
    * The constant to represent the 'packages' view. Displays the server
    * information (name, port, version and build) in the status bar and displays
    * the installed package logs exist on that server in a table with details.
    * If a package is installed multiple times, this view will have respective
    * entries in the table. Supported by {@link PSPackageViewHandler}.
    */
   public static final int TYPE_VIEW_PACKAGES = 4;

   /**
    * The constant to represent no view in this panel, should be used when a
    * root node is selected.
    */
   public static final int TYPE_VIEW_NOTHING = 5;

   /**
    * Date renderer that overrides setvalue to provide a datetime format
    */
   static class DateRenderer extends DefaultTableCellRenderer
   {
      // see base class
      public void setValue(Object value)
      {
         if (mi_formatter == null)
         {
            mi_formatter = DateFormat.getDateTimeInstance();
         }
         setText((value == null) ? "" : mi_formatter.format(value));
      }
      
      /**
       * Formatter used to format date time values, <code>null</code> until
       * first call to {@link #setValue(Object)}, never <code>null</code> or
       * modified after that.
       */
      private DateFormat mi_formatter;      
   }   
}
