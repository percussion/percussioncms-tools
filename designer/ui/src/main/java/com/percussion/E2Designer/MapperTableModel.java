/*******************************************************************************
 * $Id: MapperTableModel.java 1.29 2001/11/06 13:45:01Z martingenhart Exp $
 * COPYRIGHT (c) 2000 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted 
 * work including confidential and proprietary information of Percussion. *
 ******************************************************************************/
package com.percussion.E2Designer;

import com.percussion.design.objectstore.IPSBackEndMapping;
import com.percussion.design.objectstore.IPSDocumentMapping;
import com.percussion.design.objectstore.PSBackEndColumn;
import com.percussion.design.objectstore.PSDataMapping;
import com.percussion.design.objectstore.PSXmlField;
import com.percussion.error.PSIllegalArgumentException;
import com.percussion.util.PSCollection;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Vector;

/**
 * The table model used for the mapper editor. The table structure is a vector
 * of vector of mappings.
 */
////////////////////////////////////////////////////////////////////////////////
public class MapperTableModel extends UTTableModel
{
  /**
   * Construct the table
   *
   */
  //////////////////////////////////////////////////////////////////////////////
   public MapperTableModel()
   {
      super();
      createTable(m_headers);
   }


  /**
   * Construct the table
   *
   */
  //////////////////////////////////////////////////////////////////////////////
   public MapperTableModel(OSDataMapper mapper)
   {
      super();
      createTable(m_headers);

      // intialize table
      loadFromMapper(mapper);//, serverUdfs);
   }


   /**
   * Load table from mapper.
   *
   * @param mapper   the mapper data object.
   */
  //////////////////////////////////////////////////////////////////////////////
   public void loadFromMapper(OSDataMapper mapper)
   {
        try
      {
         if (mapper != null)
         {
              for (int i = 0; i < mapper.size(); i++)
              {
               Vector row = new Vector(m_headers.size());

               // create the mappings table
               PSDataMapping mapping = (PSDataMapping) mapper.get(i);

               // add the document mapping
               IPSDocumentMapping docMapping = mapping.getDocumentMapping();

               row.addElement( docMapping );

               IPSBackEndMapping beMapping = mapping.getBackEndMapping();
               // add the backend
               
               row.addElement( beMapping );

               // add conditionals
               PSCollection cond = mapping.getConditionals();
               if (cond != null)
                  row.addElement(cond);
               else
                  row.addElement(new PSCollection("com.percussion.design.objectstore.PSConditional"));

               // add group id
               row.addElement(new Integer(mapping.getGroupId()));
          
               this.appendRow(row);
            }
         }
      }
      catch (ClassNotFoundException e)
      {
          e.printStackTrace();
      }
      catch (IllegalArgumentException e)
      {
          e.printStackTrace();
      }
   }

  /**
   * Save table to mapper.
   *
   * @param mapper   the mapper
   */
  //////////////////////////////////////////////////////////////////////////////
   public void saveToMapper(OSDataMapper mapper)
   {
        try
      {
          // clear existing stuff first
          mapper.clear();
         // set back to initial values
         m_currentGroup = null;
         m_updateGroups = new UpdateGroups();
         m_nextId = 1;

         // create new entries from the table
         for (int i=0; i<getRowCount(); i++)
         {
            // omit empty or incomplete rows
            if (getValueAt(i, XML).toString().equals("") ||
                  getValueAt(i, BACKEND).toString().equals(""))
                 continue;

            PSDataMapping mapping = null;
            String backendString = null;
            if (getValueAt(i, BACKEND) instanceof OSExtensionCall)
            {
                 // create new mapping and add it to the mapper
               IPSDocumentMapping docmapping = null;
               if (getValueAt(i, XML) instanceof IPSDocumentMapping)
                  docmapping = (IPSDocumentMapping) getValueAt(i, XML);
               else if (getValueAt(i, XML) instanceof String)
                  docmapping = new PSXmlField((String) getValueAt(i, XML));

               OSExtensionCall udfCall = (OSExtensionCall) getValueAt(i, BACKEND);

                 // create new mapping and add it to the mapper
               if (udfCall != null && docmapping != null)
                   mapping = new PSDataMapping(docmapping, udfCall);
            }
            else if (getValueAt(i, XML) instanceof OSExtensionCall)
            {
                 // create new mapping and add it to the mapper
               OSExtensionCall udfCall = (OSExtensionCall) getValueAt(i, XML);
               IPSDocumentMapping docmapping = null;
               PSBackEndColumn backendColumn = null;
               if (getValueAt(i, BACKEND) instanceof PSBackEndColumn)
                  backendColumn = (PSBackEndColumn) getValueAt(i, BACKEND);
               else if (getValueAt(i, BACKEND) instanceof IPSDocumentMapping)
               {
                  docmapping = (IPSDocumentMapping) getValueAt(i, BACKEND);
                  backendColumn = createBackendColumn(docmapping);
               }
               else if (getValueAt(i, BACKEND) instanceof String)
               {
                  backendString = (String) getValueAt(i, BACKEND);
                  backendColumn = createBackendColumn(backendString);
               }

                 // create new mapping and add it to the mapper
               if (backendColumn != null && udfCall != null)
                    mapping = new PSDataMapping(udfCall, backendColumn);
            }
            else
            {
               IPSDocumentMapping docmapping = null;
               if (getValueAt(i, XML) instanceof IPSDocumentMapping)
                  docmapping = (IPSDocumentMapping) getValueAt(i, XML);
               else if (getValueAt(i, XML) instanceof String)
                  docmapping = new PSXmlField((String) getValueAt(i, XML));

               IPSBackEndMapping backendColumn = null;
               if (getValueAt(i, BACKEND) instanceof IPSBackEndMapping)
                  backendColumn = (IPSBackEndMapping) getValueAt(i, BACKEND);
               else if (getValueAt(i, BACKEND) instanceof IPSDocumentMapping)
               {
                  docmapping = (IPSDocumentMapping) getValueAt(i, BACKEND);
                  backendColumn = createBackendColumn(docmapping);
               }
               else if (getValueAt(i, BACKEND) instanceof String)
               {
                  backendString = (String) getValueAt(i, BACKEND);
                  backendColumn = createBackendColumn(backendString);
               }

                 // create new mapping and add it to the mapper
               if (backendColumn != null && docmapping != null)
                   mapping = new PSDataMapping(docmapping, backendColumn);
            }

            Vector row = new Vector();
            row.addElement(getValueAt(i, XML));
            row.addElement(getValueAt(i, BACKEND));
            row.addElement(getValueAt(i, CONDITIONALS));
            row.addElement(getValueAt(i, GROUP));
            updateGroup(row);
            Integer group = (Integer) row.get(GROUP);
            if (mapping != null)
            {
               if (getValueAt(i, CONDITIONALS) instanceof PSCollection)
                  mapping.setConditionals((PSCollection) getValueAt(i, CONDITIONALS));
               else
                  mapping.setConditionals(new PSCollection("com.percussion.design.objectstore.PSConditional"));
            
               mapping.setGroupId(group.intValue());
               mapper.add(mapping);
            }
         }
      }
      catch (PSIllegalArgumentException e)
      {
          e.printStackTrace();
      }
      catch (IllegalArgumentException e)
      {
          e.printStackTrace();
      }
      catch (ClassNotFoundException e)
      {
          e.printStackTrace();
      }
   }


  /**
   * Append empty row at table end.
   *
   */
  //////////////////////////////////////////////////////////////////////////////
   public void appendRow()
   {
        try
      {
           for (int i=0; i<getColumnCount(); i++)
           {
             if (i == CONDITIONALS)
                  getColumn(i).addElement(new PSCollection("com.percussion.design.objectstore.PSConditional"));
            else
               getColumn(i).addElement("");
          }

          fireTableDataChanged();
      }
      catch (ClassNotFoundException e)
      {
          e.printStackTrace();
      }
   }


  /**
   * Append the number of empty rows at table end.
   *
   * @param number how many rows to append
   */
  //////////////////////////////////////////////////////////////////////////////
   public void appendRow(int number)
   {
        for (int i=0; i<number; i++)
          appendRow();
   }

  /**
   * Append a row to this table.
   *
   * @param row the row to append
   */
  //////////////////////////////////////////////////////////////////////////////
   public void appendRow(Vector row)
   {
      updateGroup(row);
      super.appendRow(row);
   }

   /**
    * Set the current active backend datatank.
    *
    * @backendTank the new backend tank
    */
  //////////////////////////////////////////////////////////////////////////////
   public void setBackendTank(OSBackendDatatank backendTank)
   {
        m_backendTank = backendTank;
   }

   /**
    * Get current active backend datatank.
    *
    * @return OSBacckendDatatank the backend tank
    */
  //////////////////////////////////////////////////////////////////////////////
   public OSBackendDatatank getBackendTank()
   {
        return m_backendTank;
   }

   /**
    * Set the current active page datatank.
    *
    * @pageTank the new page tank
    */
  //////////////////////////////////////////////////////////////////////////////
   public void setPageTank(OSPageDatatank pageTank)
   {
        m_pageTank = pageTank;
   }


   /**
    * Get current active page datatank.
    *
    * @return OSPageDatatank the page tank
    */
  //////////////////////////////////////////////////////////////////////////////
   public OSPageDatatank getPageTank()
   {
        return m_pageTank;
   }


   /**
    * Calls @link com.percussion.design.objectstore.PSBackEndColumn#findColumnFromDisplay(String, PSCollection)
    * to create a new <code>PSBackEndColumn</code> with the information in 
    * the passed in string. 
    * @param formattedName A string in the format produced by
    * @link #getValueDisplayText()
    * @return <code>null</code> on error or a <code>PSBackEndColumn</code>
    */
   private PSBackEndColumn createBackendColumn(String formattedName)
                                               throws PSIllegalArgumentException
   {
      if (m_backendTank == null)
         return null;
      
      return PSBackEndColumn.findColumnFromDisplay(formattedName, m_backendTank.getTables());
   }


   /**
    * Create a new backend column.
    *
    * @param documentMapping the backend data
    * @return PSBackEndColumn the backend column
    */
 //////////////////////////////////////////////////////////////////////////////
   private PSBackEndColumn createBackendColumn(IPSDocumentMapping documentMapping)
                                               throws PSIllegalArgumentException
   {
       return null;
   }


  /**
   * Helper class which specifies an update group. It contains a list of all
   * columns which belong to the group.
   */
  //////////////////////////////////////////////////////////////////////////////
   private class UpdateGroup extends Vector
   {
    /**
     * Constucts an empty group with the provided id.
     *
     * @param tableName the table name
     * @param id the group id
     */
    ////////////////////////////////////////////////////////////////////////////
      public UpdateGroup(String tableName, int id)
      {
         m_id = id;
         m_tableName = tableName;
      }

    /**
     * Add a new element to this group. This will be done until this group has
     * been terminated. We terminate it ourself if the element to be added is
     * already part of this group.
     *
     * @return int the group id
     */
    ////////////////////////////////////////////////////////////////////////////
      public void addElement(String column)
      {
         for (int i=0, n=size(); i<n; i++)
         {
            String element = (String) this.get(i);
            if (element.equals(column))
            {
               terminate();
               return;
            }
         }

         super.addElement(column);
      }

    /**
     * Is this group terminated.
     *
     * @return boolean the termiated status
     */
    ////////////////////////////////////////////////////////////////////////////
      public boolean isTerminated()
      {
         return m_terminated;
      }

    /*
     * Terminate this group. Once a group is terminated, no more elements can be
     * added.
     *
     * @return int the group id
     */
    ////////////////////////////////////////////////////////////////////////////
      public void terminate()
      {
         m_terminated = true;
      }

    /*
     * Get the group id.
     *
     * @return int the group id
     */
    ////////////////////////////////////////////////////////////////////////////
      public int getId()
      {
         return m_id;
      }

    /*
     * Get the group table name.
     *
     * @return String the table name
     */
    ////////////////////////////////////////////////////////////////////////////
      public String getTableName()
      {
         return m_tableName;
      }

    ////////////////////////////////////////////////////////////////////////////
      private int m_id = 0;
      private String m_tableName = "";
      private boolean m_terminated = false;
   }

   private UpdateGroups m_updateGroups = new UpdateGroups();
  /*
   * Helper class which specified an update group. It contains a list of all
   * attributes which belong to the group.
   */
  //////////////////////////////////////////////////////////////////////////////
   private class UpdateGroups extends Vector
   {
    /*
     * Get the update group for the provided backend. First it checks for an
     * existing table, if not found, a new one will be created.
     *
     * @return UpdateGroup if a group for the provided backend exists, null otherwise
     */
    ////////////////////////////////////////////////////////////////////////////
      public UpdateGroup getGroup(String backend)
      {
         UpdateGroup group = null;

         int index = backend.indexOf(".");
         String tableName = backend.substring(0, index);
         String columnName = backend.substring(index + 1, backend.length());
         for (int i=0, n=this.size(); i<n; i++)
         {
            group = (UpdateGroup) this.elementAt(i);
            if (group.getTableName().equals(tableName))
            {
               if (!group.isTerminated())
                  group.addElement(columnName);
               return group;
            }
         }

         // create new group and append it to the current list
         UpdateGroup newGroup = new UpdateGroup(tableName, m_nextId++);
         this.addElement(newGroup);

         // terminate previous group
         if (group != null)
            group.terminate();

         return newGroup;
      }
   }


   private UpdateGroup m_currentGroup = null;
   private int m_nextId = 1;
  /**
   * Check all entries for groups, set the group ID and validate them. Groups
   * are determined by repeated backend entries. A group for one specific table
   * must have all attributes in any case. Only one group ID is valid for one
   * table.
   * All entries for a paricular group must be in order
   */
  //////////////////////////////////////////////////////////////////////////////
   private void updateGroup(Vector row)
   {
      String backend = null;
      if (row.get(BACKEND) instanceof String)
         backend = (String) row.get(BACKEND);
      else if(row.get(BACKEND) instanceof PSBackEndColumn)
      {
         PSBackEndColumn col = (PSBackEndColumn) row.get(BACKEND);
         backend = col.getValueText();
      }
      else if (row.get(BACKEND) instanceof IPSDocumentMapping)
      {
         // use the current group ID or 0
         if (m_currentGroup != null)
            row.setElementAt(new Integer(m_currentGroup.getId()), GROUP);
         else
            row.setElementAt(new Integer(0), GROUP);

         return;
      }
      else if (row.get(BACKEND) instanceof OSExtensionCall)
      {
         // use the current group ID or 0
         if (m_currentGroup != null)
            row.setElementAt(new Integer(m_currentGroup.getId()), GROUP);
         else
            row.setElementAt(new Integer(0), GROUP);

         return;
      }
      else
      {
         row.setElementAt(new Integer(0), GROUP);
         return;
      }

      String arr[] = PSBackEndColumn.parseValueText(backend);
      String tableName = arr[0];
      String columnName = arr[1];

      if (m_currentGroup == null ||
          !m_currentGroup.getTableName().equals(tableName))
      {
         m_currentGroup = new UpdateGroup(tableName, m_nextId++);
         m_updateGroups.addElement(m_currentGroup);
      }

      m_currentGroup.addElement(columnName);
      if (m_currentGroup.isTerminated())
      {
         m_currentGroup = new UpdateGroup(tableName, m_nextId++);
         m_updateGroups.addElement(m_currentGroup);
         m_currentGroup.addElement(columnName);
      }

      // set the current group id
      row.setElementAt(new Integer(m_currentGroup.getId()), GROUP);
   }


   private ResourceBundle m_res = null;
  /**
   * Get table text resources.
   */
  //////////////////////////////////////////////////////////////////////////////
   public ResourceBundle getResources()
   {
      try
      {
         if (m_res == null)
              m_res = ResourceBundle.getBundle(getClass().getName() + "Resources",
                                                           Locale.getDefault());
      }
      catch (MissingResourceException e)
      {
         System.out.println(e);
      }

       return m_res;
    }


  /** the active backend datatank. */
   private OSBackendDatatank m_backendTank = null;
  /** the active page datatank. */
   private OSPageDatatank m_pageTank = null;
  /** the table headers. */
   private static Vector m_headers = new Vector();
   {
        if (m_headers.isEmpty())
      {
           // initialize table headers
           m_headers.addElement(getResources().getString("xml"));
           m_headers.addElement(getResources().getString("backend"));
           m_headers.addElement(getResources().getString("conditionals"));
           m_headers.addElement(getResources().getString("group"));
      }
   }

   public final static int XML = 0;
   public final static int BACKEND = 1;
   public final static int CONDITIONALS = 2;
   public final static int GROUP = 3;
}

