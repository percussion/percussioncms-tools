/*[ SecurityProviderSummaryPanel.java ]****************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer.admin;

import com.percussion.E2Designer.CatalogDatasources;
import com.percussion.E2Designer.FeatureSet;
import com.percussion.E2Designer.SecurityProviderMetaData;
import com.percussion.E2Designer.UTReadOnlyTableCellEditor;
import com.percussion.design.objectstore.PSSecurityProviderInstance;
import com.percussion.security.PSBackEndConnection;
import com.percussion.security.PSJndiProvider;
import com.percussion.security.PSOdbcProvider;
import com.percussion.security.PSSecurityProvider;
import com.percussion.util.PSCollection;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import java.util.ResourceBundle;

/** Provides a summary of security providers in the E2 admin client.  It has
  * a table with 3 columns, Name, Type, and ServerName.
*/
@SuppressWarnings(value={"unchecked"})
public class SecurityProviderSummaryPanel extends JPanel implements ITabDataHelper
{
   private static final long serialVersionUID = 1L;

   public SecurityProviderSummaryPanel(ServerConfiguration config)
   {
      super();
   
      m_config = config;
      this.setBorder(new EmptyBorder(5, 5, 5, 5));
      init();
   }
   
   public boolean saveTabData()
   {
      if (!m_bModified)
         return false;
   
      System.out.println("saving SecurityProviderSummaryPanel...");
      m_config.setSecurityConfiguration(m_data);
   
      m_bModified = false;
      return true;
   }
   
   public boolean validateTabData()
   {
      return true;
   }

   /** Uses the vector data at index to return a row of 3 columns (name, type, and
    * server name).
    *
    * @param inst The PSSecurityProviderInstance object in PSCollection m_data
    * where the data is to be converted to a row for the table to display.
    *
    * @return The row of data as an array of Objects[3].
    */
   public Object[] convertDataToRow(PSSecurityProviderInstance inst)
   {
      Object[] rowData = new Object[3];

      rowData[0] = inst.getName();

      // temporary implementation... should use cataloger to catalog for
      // <PROVIDER>
      //   <FULLNAME>
      int type = inst.getType();
      rowData[1] = SecurityProviderMetaData.getInstance().getDisplayNameForId( type );

      // to be implemented when cataloger is ready...
      Properties props = inst.getProperties();
      if(props != null)
      {
         String value = "";
         switch ( type )
         {
            /* We have to use hard coded values here because the provider doesn't
               give us any method that returns a summary. */
            case PSSecurityProvider.SP_TYPE_ODBC:
               value = props.getProperty( PSOdbcProvider.PROPS_SERVER_NAME );
               break;
            case PSSecurityProvider.SP_TYPE_BETABLE:
               value = props.getProperty(
                  PSBackEndConnection.PROPS_DATASOURCE_NAME);
               if (StringUtils.isBlank(value))
                  value = CatalogDatasources.REPOSITORY_LABEL;
               break;
            case PSSecurityProvider.SP_TYPE_DIRCONN:
               value = props.getProperty( PSJndiProvider.PROPS_PROVIDER_URL );
               break;
         }
         rowData[2] = value;

         //todo: add Properties for others as required.
         // presently for Web and HostAddress there are no properties
      }

    return rowData;
  }

   /** Initial population of the table display of SecurityProvider data stored
    * in the Vector.  This is called once by the init() method.  This method also
    * uses populateRow(int) often to dissect the Vector data into table row
    * format.
    *
    * @return The 2-dimen array used for the construction of the
    * display table.
    *
    * @see #convertDataToRow(PSSecurityProviderInstance)
    */
   private Object[][] initializeTableData()
   {
      m_data = m_config.getSecurityConfiguration();
   
      Object[][] storage = new Object[m_data.size()][3];
      Object[] rowStore = new Object[3];
   
      for (int i = 0; i < m_data.size(); i++)
      {
         rowStore = convertDataToRow((PSSecurityProviderInstance) m_data.get(i));
   
         storage[i][0] = rowStore[0];
         storage[i][1] = rowStore[1];
         storage[i][2] = rowStore[2];
      }
   
      return storage;
   }

   /** Constructors defaults to this initialization method for all this
    * panel&apos;s doing.
    */
   private void init()
   {
      String[] tableHeaders =
      {
         sm_res.getString("providername"),
         sm_res.getString("providertype"),
         sm_res.getString("sourcename")
      };
      m_dataModel = new DefaultTableModel(initializeTableData(), tableHeaders);
   
      m_table = new JTable(m_dataModel);
      m_table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      m_table.setIntercellSpacing(new Dimension(0, 0));
      m_table.setRowSelectionAllowed(true);
      m_table.setColumnSelectionAllowed(false);
      m_table.setShowVerticalLines(false);
      m_table.setShowGrid(false);
      m_table.setRequestFocusEnabled(false);
      m_table.getTableHeader().setReorderingAllowed(false);
   
      UTReadOnlyTableCellEditor editor = new UTReadOnlyTableCellEditor();
   
      for (int i = 0; i < 3; i++)
         m_table.getColumnModel().getColumn(i).setCellEditor(editor);
   
      // adding table row-editing capability
      m_table.addMouseListener(new MouseAdapter()
      {
         // need this for double clicking on row to edit
         public void mouseClicked(MouseEvent e)
         {
            if (0 > e.getY() || m_table.getPreferredSize().height < e.getY())
               return; // ignore
   
            if (2 == e.getClickCount())
            {
               int iRow = e.getY() / m_table.getRowHeight();
   
               m_table.getSelectionModel().setSelectionInterval(iRow, iRow);
               onEdit();
            }
         }
      });
   
      JScrollPane scrollPane = new JScrollPane(m_table);
      if (m_table.getParent() != null)
         m_table.getParent().setBackground(Color.white);
   
      JPanel basePane = new JPanel(new BorderLayout());
      basePane.add(scrollPane, BorderLayout.CENTER);
      basePane.setBackground(Color.white);
   
      setLayout(new BorderLayout());
      add(basePane, BorderLayout.CENTER);
      add(createButtonPanel(), BorderLayout.SOUTH);
   }

   /** Creates the three buttons, edit, delete, and new, on the bottom of this
   * panel.
   *
   * @return JPanel The panel that contains 3 buttons.
   */
   private JPanel createButtonPanel()
   {
      m_deleteButton = new JButton(sm_res.getString("delete"));
      m_deleteButton.setPreferredSize(sm_BUTTON_SIZE);
      m_deleteButton.setMnemonic((sm_res.getString( "delete.mn" )).charAt(0));
      m_deleteButton.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            onDelete();
         }
      });
   
      m_editButton = new JButton(sm_res.getString("edit"));
      m_editButton.setPreferredSize(sm_BUTTON_SIZE);
      m_editButton.setMnemonic((sm_res.getString( "edit.mn" )).charAt(0));
      m_editButton.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            onEdit();
         }
      });
   
      m_newButton = new JButton(sm_res.getString("new"));
      m_newButton.setPreferredSize(sm_BUTTON_SIZE);
      m_newButton.setMnemonic((sm_res.getString( "new.mn" )).charAt(0));
      m_newButton.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            onNew();
         }
      });
   
      JPanel buttonPanel = new JPanel();
      buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
      buttonPanel.setBorder(new EmptyBorder(6, 0, 0, 0));
      buttonPanel.add(Box.createHorizontalGlue());
      buttonPanel.add(m_editButton);
      buttonPanel.add(Box.createHorizontalStrut(6));
      buttonPanel.add(m_deleteButton);
      buttonPanel.add(Box.createHorizontalStrut(6));
      buttonPanel.add(m_newButton);
   
      return buttonPanel;
   }

   /**
    * Performs the action for the delete button.
    */
   private void onDelete()
   {
      int iMin = m_table.getSelectionModel().getMinSelectionIndex();
      int iMax = m_table.getSelectionModel().getMaxSelectionIndex();
      if(iMin >= 0 && iMax >= 0)
      {
         int iRowsToRemove = iMax-iMin+1;
         if(iRowsToRemove < 1)
            return;

         for(int i=0; i<iRowsToRemove; i++)
         {
            String strName = (String)m_table.getValueAt(iMin, 0);
            removeEntryFromCollection(strName);
            ((DefaultTableModel)m_table.getModel()).removeRow(iMin);
         }
         m_bModified = true;
      }
   }

   /**
    * Performs the action for the edit button.
    */
   private void onEdit()
   {
      int iMin = m_table.getSelectionModel().getMinSelectionIndex();
      int iMax = m_table.getSelectionModel().getMaxSelectionIndex();
      if(iMin < 0 || iMax < 0 || iMin != iMax)
         return;

      String strName = (String)m_table.getValueAt(iMin, 0);
      int indexInCollection = 0;
      for(int i=0; i<m_data.size(); i++)
      {
         PSSecurityProviderInstance inst = (PSSecurityProviderInstance)m_data.get(i);
         String strInstName = inst.getName();
         if(strName.equals(strInstName))
         {
            m_curInst = inst;
            indexInCollection = i;
            break;
         }
      }

      ArrayList providerNames = new ArrayList(10);
      int size = m_data.size();
      for ( int i = 0; i < size; ++i )
         providerNames.add( ((PSSecurityProviderInstance) m_data.get(i)).getName());


      int iType = m_curInst.getType();
      ISecurityProviderEditor editor = 
         SecurityProviderEditorFactory.getSecurityProviderEditor(iType,
            AppletMainDialog.getMainframe(), m_config.getServerConfiguration());

      if ( providerNames.size() > 0 )
         editor.setInstanceNames( providerNames );
      editor.setInstance( m_curInst );
      editor.setVisible( true );
      if ( editor.isInstanceModified())
      {
         replaceRowData(iMin, m_curInst);
         m_data.set(indexInCollection, m_curInst);
         m_bModified = true;
      }
   }

   /**
    * Performs the action for the new button.
    */
   private void onNew()
   {
      ArrayList providerNames = new ArrayList(10);
      int size = m_data.size();
      for ( int i = 0; i < size; ++i )
         providerNames.add( ((PSSecurityProviderInstance) m_data.get(i)).getName());

      //Make a shallow copy of group providers to prevent the removal of any
      //group providers.
      PSCollection groupProviders = null;
      Collection  copyGroupProviders = null;

      if( FeatureSet.getFeatureSet().isFeatureSupported(
         AppletMainDialog.GROUP_PROVIDERS_FEATURE) )
      {
         groupProviders =
            m_config.getServerConfiguration().getGroupProviderInstances();
         copyGroupProviders = (Collection) groupProviders.clone();
      }
      else //create an empty collection and pass on to dialog.
         copyGroupProviders = new ArrayList();

      NewSecurityProviderTypeDialog d = new NewSecurityProviderTypeDialog(
         AppletMainDialog.getMainframe(), providerNames, copyGroupProviders,
         m_config.getServerConfiguration());
      d.setVisible(true);
      if(d != null && d.isInstanceCreated())
      {
         m_curInst= d.getProviderInstance();
         ((DefaultTableModel)m_dataModel).addRow(convertDataToRow(m_curInst));
         m_data.add(m_curInst);
         m_bModified = true;
      }

      d.dispose();
   }

   private void replaceRowData(int index, PSSecurityProviderInstance inst)
   {
      ((DefaultTableModel)m_dataModel).insertRow(index, convertDataToRow(inst));
      ((DefaultTableModel)m_dataModel).removeRow(index+1);
      m_table.repaint();
      m_table.getSelectionModel().setSelectionInterval(index, index);
   }

   /**
    * Removes the Security Provider Instance with the passed in name from the collection
    * m_data.
    *
    * @param strName The name of the PSSecurityProviderInstance object to be removed from the
    * collection of PSSecurityProviderInstance objects (m_data).
    */
   private void removeEntryFromCollection(String strName)
   {
      if(m_data == null)
       return;

      for(int i=0; i<m_data.size(); i++)
      {
         PSSecurityProviderInstance inst = (PSSecurityProviderInstance)m_data.get(i);
         String strInstName = inst.getName();
         if(strName.equals(strInstName))
         {
            m_data.remove(m_data.get(i));
            break;
         }
      }

   }

   /**
    * The server configuration, initialized while constructed, never 
    * <code>null</code> after that.
    */
   private ServerConfiguration m_config = null;
   
   private JButton m_editButton, m_deleteButton, m_newButton;
   private JTable m_table;
   private TableModel m_dataModel;
   
   private PSCollection m_data;
   
   private PSSecurityProviderInstance m_curInst = null;
   
   private boolean m_bModified = false;
   
   private static ResourceBundle sm_res = PSServerAdminApplet.getResources();
   
   private static final Dimension sm_BUTTON_SIZE = new Dimension(80, 22);
}

