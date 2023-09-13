/******************************************************************************
 *
 * [ PSTransLogModel.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.deployer.ui;

import com.percussion.deployer.objectstore.PSTransactionLogSummary;
import com.percussion.deployer.objectstore.PSTransactionSummary;
import com.percussion.util.PSEntrySet;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;


/**
 * Table model for a transaction log.
 */
class PSTransLogModel extends AbstractTableModel
{
   /**
    * Table model for displaying transaction log.
    * @param tranxLogSum  transaction log summary for the package, may be
    * <code>null</code>.
    */
   public PSTransLogModel(PSTransactionLogSummary tranxLogSum)
   {
      List sumList = new ArrayList();
      if (tranxLogSum != null)
         sumList.add(new PSEntrySet("", tranxLogSum));
      
      m_showPackage = false;
      loadColumnNames();
      setData(sumList);
   }

   /**
    * Table model for displaying transaction log.
    * 
    * @param logSumList List of <code>Map.entry</code> objects, may not be
    * <code>null</code>, may be empty.  For each entry the key is the package 
    * name as a <code>String</code> and the value is a 
    * {@link PSTransactionLogSummary}.
    */
   public PSTransLogModel(List logSumList)
   {
      if (logSumList == null)
         throw new IllegalArgumentException("logSumList may not be null");
      
      m_showPackage = true;
      loadColumnNames();
      setData(logSumList);
   }

   /**
    * Sets data for this model.
    * 
    * @param txnSumList See {@link #PSTransLogModel(List)} for details, assumed 
    * not <code>null</code>. 
    */
   private void setData(List txnSumList)
   {
      m_list = new ArrayList();
      Iterator sums = txnSumList.iterator();
      while (sums.hasNext())
      {
         Map.Entry entry = (Map.Entry)sums.next();
         String pkg = (String) entry.getKey();
         PSTransactionLogSummary txnLogSum = 
            (PSTransactionLogSummary)entry.getValue();
         Iterator iter = txnLogSum.getTransactions();
         while(iter.hasNext())
            m_list.add(new PSEntrySet(pkg, iter.next()));
      }
   }

   /**
    * Load column names from the resource properties file.
    */
   private void loadColumnNames()
   {
      if(m_colNames.isEmpty())
      {
         if (m_showPackage)
            m_colNames.add(getResourceString("pkg"));
         
         m_colNames.add(getResourceString("dependency"));
         m_colNames.add(getResourceString("elem"));
         m_colNames.add(getResourceString("elemType"));
         m_colNames.add(getResourceString("depAx"));
      }
   }

   /**
    * Gets the number of columns in the table.
    * @return number of columns
    */
   public int getColumnCount()
   {
      return m_colNames.size();
   }

   /**
    * Gets the number of rows in the table.
    * A minimum of <code>MIN_ROWS</code> are returned.
    * @return number of rows.
    */
   public int getRowCount()
   {
      if(m_list.size() < MIN_ROWS)
         return MIN_ROWS;
      else
         return m_list.size();
   }

   /**
    * Gets the name of the column specified by its column number.
    * @param col the column index, must be >= 0 and less than
    * {@link #getColumnCount()} of this model.
    * @return  name of the the column.
    */
   public String getColumnName(int col)
   {
      checkColumn(col);
      return (String)m_colNames.get(col);
   }

   /**
    * Gets the data at the specified row and column number.
    * @param row the row index, must be >= 0 and less than
    * {@link #getRowCount() rowcount} of this model.
    * @param col the column index, must be >= 0 and less than
    * {@link #getColumnCount()} of this model.
    * @return data at <code>row</code> and <code>col</code>, may be
    * <code>null</code>.
    */
   public Object getValueAt(int row, int col)
   {
      checkRow(row);
      checkColumn(col);

      if(row < m_list.size())
      {
         Map.Entry entry = (Map.Entry)m_list.get(row);
         String pkg = (String) entry.getKey();
         PSTransactionSummary logSum = (PSTransactionSummary)entry.getValue();
         
         // bump value if not showing package
         int adjCol = m_showPackage ? col : col + 1;
         switch(adjCol)
         {
            case PACKAGE_COLUMN:
               return pkg;
               
            case ELEMENT_COLUMN:
               return logSum.getElement();

            case ELEM_TYPE_COLUMN:
               return logSum.getType();

            case DEPENDENCY_COLUMN:
               return logSum.getDepDescription();

            case DEP_ACTION_COLUMN:
               int action = logSum.getAction();
               if (action == PSTransactionSummary.ACTION_CREATED)
                  return getResourceString("created");
               else if (action == PSTransactionSummary.ACTION_MODIFIED)
                  return getResourceString("modified");
               else if (action == 
                  PSTransactionSummary.ACTION_SKIPPED_NO_OVERWRITE)
               {
                  return getResourceString("skippedNoOverwite");
               }
               else if (action == 
                  PSTransactionSummary.ACTION_SKIPPED_ALREADY_INSTALLED)
               {
                  return getResourceString("skippedAlreadyInstalled");
               }
               else if (action == PSTransactionSummary.ACTION_FAILED_TO_INSTALL)
               {
                  return getResourceString("failedToInstall");
               }
               else
                  return getResourceString("deleted");
         }
      }
      return null;
   }

   /**
    * Checks that the supplied row exists in this model.
    *
    * @param row the row index to check, must be >= 0 and less than
    * {@link #getRowCount() rowcount} of this model.
    *
    * @throws IllegalArgumentException if row index is invalid.
    */
   private void checkRow(int row)
   {
      if(row < 0 || row >= getRowCount())
         throw new IllegalArgumentException("row must be between 0 and " +
               (getRowCount()-1) + "inclusive");
   }

   /**
    * Checks that the supplied column exists in this model.
    *
    * @param col the column index to check, must be >= 0 and less than
    * {@link #getColumnCount()} of this model.
    *
    * @throws IllegalArgumentException if column index is invalid.
    */
   private void checkColumn(int col)
   {
      if(col < 0 || col >= getColumnCount())
         throw new IllegalArgumentException("col must be between 0 and " +
               (getColumnCount()-1) + "inclusive");
   }

   /**
    * Gets the resource string identified by the specified key.  If the
    * resource cannot be found, the key itself is returned.
    *
    * @param key identifies the resource to be fetched; may not be <code>null
    * </code> or empty.
    *
    * @return String value of the resource identified by <code>key</code>, or
    * <code>key</code> itself, never <code>null</code> or empty.
    */
   protected String getResourceString(String key)
   {
      if(key == null || key.trim().length() == 0)
         throw new IllegalArgumentException("key may not be null or empty");

      String resourceValue = key;
      try
      {
         if (getResources() != null)
            resourceValue = m_res.getString( key );
      } catch (MissingResourceException e)
      {
         // not fatal; warn and continue
         System.err.println( this.getClass() );
         System.err.println( e );
      }
      return resourceValue;
   }

   /**
    * Gets resource file name for this class.
    *
    * @return resource file name, never <code>null</code> or empty.
    **/
   protected String getResourceName()
   {
      return getClass().getName() + "Resources";
   }
   
   /**
    * Retrieve the ResourceBundle for this class.  Loaded lazily on first call,
    * cached after that.
    *
    * @return ResourceBundle, may be <code>null</code>.
    */
   protected ResourceBundle getResources()
   {
      try 
      {
         if (null == m_res)
            m_res = ResourceBundle.getBundle(getResourceName(),
               Locale.getDefault());
      }
      catch(MissingResourceException mre)
      {
         mre.printStackTrace();
      }
      
      return m_res;
   }
   /**
    * List of <code>Map.Entry</code>, key is a package name as a 
    * <code>String</code>, value is a <code>PSTransactionSummary</code> object. 
    * Initialized in {@link #setData(List)}, never <code>null</code> or 
    * modified after that. 
    */
   private List m_list;
   
   /**
    * Flag to indicate if the package column is to be shown.  <code>true</code>
    * to show it, <code>false</code> to hide it.  Set during construction,
    * never modified after that.
    */
   private boolean m_showPackage = false;

   /**
    * Resource bundle lazily loaded by {@link #getResources()}.
    * Never <code>null</code> or modified once loaded. 
    */
   private ResourceBundle m_res = null;
   
   /**
    * Minimum number of rows to be dispalyed.
    */
   public static final int MIN_ROWS = 6;

   /**
    * List holding column names in the same order as they appear in the table.
    * Names are set in <code>loadColumnNames()</code>, assuming that resource
    * bundle was loaded properly, it's never empty. Never <code>null</code>
    * or modified.
    */
   private List m_colNames = new ArrayList();

   /**
    * The index of the package column.
    */
   private static final int PACKAGE_COLUMN = 0;
   
   /**
    * The index of dependency column.
    */
   private static final int DEPENDENCY_COLUMN = 1;

   /**
    * The index of element column.
    */
   private static final int ELEMENT_COLUMN = 2;

   /**
    * The index of element type column.
    */
   private static final int ELEM_TYPE_COLUMN = 3;

   /**
    * The index of deployment action column.
    */
   private static final int DEP_ACTION_COLUMN = 4;
}
