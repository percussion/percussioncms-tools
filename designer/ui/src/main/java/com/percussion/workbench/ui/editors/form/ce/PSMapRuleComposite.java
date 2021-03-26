/******************************************************************************
*
* [ PSMapRuleComposite.java ]
*
* COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
* All rights reserved. This material contains unpublished, copyrighted
* work including confidential and proprietary information of Percussion.
*
*****************************************************************************/
package com.percussion.workbench.ui.editors.form.ce;

import com.percussion.workbench.ui.IPSUiConstants;
import com.percussion.workbench.ui.controls.IPSNewRowObjectProvider;
import com.percussion.workbench.ui.controls.PSAbstractTableLabelProvider;
import com.percussion.workbench.ui.controls.PSSortableTable;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableItem;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PSMapRuleComposite extends Composite implements IPSUiConstants
{
   public PSMapRuleComposite(Composite parent, int style, boolean isInput) 
   {
      super(parent, style);
      m_isInput = isInput;
      setLayout(new FormLayout());
      createControls();
   }
   
   /**
    *  Convenient method to create controls for this composite
    */
   private void createControls()
   {
      Composite mainComp = new Composite(this,SWT.NONE);
      mainComp.setLayout(new FormLayout());
      FormData fd = new FormData();
      fd.left = new FormAttachment(0, COMMON_BORDER_OFFSET);
      fd.top = new FormAttachment(0, 0);
      fd.right = new FormAttachment(100, 0);
      fd.bottom = new FormAttachment(100, 0);
      mainComp.setLayoutData(fd);
      // Create the label provider for this table
      ITableLabelProvider labelProvider = new PSAbstractTableLabelProvider()
      {
         @SuppressWarnings("synthetic-access")
         public String getColumnText(Object element, int columnIndex)
         {
            MapRow rowData = (MapRow) element;
            switch (columnIndex)
            {
               case 0 :
                  return StringUtils.defaultString(rowData.key);
               case 1 :
                  return StringUtils.defaultString(rowData.value);
            }
            return ""; // should never get here
         }
      };

      // Create the new row object provider for this table
      IPSNewRowObjectProvider newRowProvider = new IPSNewRowObjectProvider()
      {
         @SuppressWarnings("synthetic-access")
         public Object newInstance()
         {
            MapRow newRow = new MapRow();
            return newRow;
         }

         public boolean isEmpty(Object obj)
         {
            if (!(obj instanceof MapRow))
               throw new IllegalArgumentException(
                     "The passed in object must be an instance of CondRow.");
            MapRow cond = (MapRow) obj;
            return cond.isEmpty();
         }
      };

      m_mapTable = new PSSortableTable(mainComp, labelProvider, newRowProvider,
            SWT.NONE, PSSortableTable.DELETE_ALLOWED
                  | PSSortableTable.INSERT_ALLOWED);
      FormData fd1 = new FormData();
      fd1.left = new FormAttachment(0, 0);
      fd1.top = new FormAttachment(0, 0);
      fd1.right = new FormAttachment(100, 0);
      fd1.bottom = new FormAttachment(100, 0);
      m_mapTable.setLayoutData(fd1);
      
      
      final CellEditor keyCellEditor = new TextCellEditor(m_mapTable.getTable());
      String col1Name = m_isInput?INPUT_VALUE:STORED_VALUE;
      m_mapTable.addColumn(col1Name, PSSortableTable.NONE,
            new ColumnWeightData(1,50, true), keyCellEditor, SWT.LEFT);      
      

      final CellEditor valueCellEditor = new TextCellEditor(m_mapTable.getTable());
      String col2Name = m_isInput?STORED_VALUE:OUTPUT_VALUE;
      m_mapTable.addColumn(col2Name, PSSortableTable.NONE,
            new ColumnWeightData(1,50, true), valueCellEditor, SWT.LEFT);      
      m_mapTable.setCellModifier(new CellModifier(m_mapTable));
   }
 
   /**
    * Checks whether the keys or values or both have duplicates.
    * 
    * @param kenum Checks based on the value of kenum.
    * @return <code>true</code> if the map data has duplicates.
    * otherwise <code>false</code>.
    */
   private boolean hasDuplicates(KeysEnum kenum)
   {
      List<MapRow> rows = m_mapTable.getValues();
      Set<String> strs = new HashSet<String>();
      boolean checkKeys = kenum == KeysEnum.KEYS || kenum==KeysEnum.BOTH?true:false;
      boolean checkValues = kenum == KeysEnum.VALUES || kenum==KeysEnum.BOTH?true:false;
      if(checkKeys)
      {
         for (MapRow row : rows)
            strs.add(StringUtils.defaultString(row.key));
         if (strs.size() != rows.size())
            return true;
      }
      strs.clear();
      if(checkValues)
      {
         for (MapRow row : rows)
            strs.add(StringUtils.defaultString(row.value));
         if (strs.size() != rows.size())
            return true;
      }
      return false;
   }

   /**
    * Validates the data enetered in the map table.
    * 
    * @return <code>null</code>, if the map data is valid, otherwise
    * appropriate error string.
    */
   public String validateMapData()
   {
      List<MapRow> rows = m_mapTable.getValues();
      if (rows.isEmpty())
         return EMPTY_MAP_DATA;
      if (hasDuplicates(KeysEnum.KEYS))
         return DUPLICATE_MAP_DATA;
      return null;
   }
   /**
    * Gets the map data represented by this composite.
    * 
    * @return String The String representing the map data.
    * @throws UnsupportedEncodingException 
    */
   public String getMapData() throws UnsupportedEncodingException
   {
      String mapString = "";
      List<MapRow> rows = m_mapTable.getValues();
      for(MapRow row:rows)
      {
         mapString += URLEncoder.encode(row.key, DEFAULT_ENCODING) + "="
               + URLEncoder.encode(row.value, DEFAULT_ENCODING) + "&";
      }
      if (mapString.endsWith("&"))
         mapString = mapString.substring(0, mapString.length() - 1);
      return mapString;
   }
   
   /**
    * Gets the map data represented by this composite.
    * @param Map of key value pair
    * @throws UnsupportedEncodingException 
    */
   public void setMapData(String mapData) throws UnsupportedEncodingException
   {
      List<MapRow> data = new ArrayList<MapRow>();
      if (!StringUtils.isEmpty(mapData))
      {
         String pairs[] = mapData.split("&");
         for (int i = 0; i < pairs.length; i++)
         {
            String parts[] = pairs[i].split("=");
            String key = URLDecoder.decode(parts[0], DEFAULT_ENCODING);
            String value = "";
            if (parts.length > 1)
               value = URLDecoder.decode(StringUtils.defaultString(parts[1]),
                     DEFAULT_ENCODING);
            if (!(StringUtils.isEmpty(key) && StringUtils.isEmpty(value)))
               data.add(new MapRow(key, value));
         }
      }
      m_mapTable.setValues(data);
      m_mapTable.refreshTable();
   }
   /**
    * Cell modifier for the choice list table
    */
   class CellModifier implements ICellModifier
   {

      CellModifier(PSSortableTable comp)
      {
         mi_tableComp = comp;
      }
      /* 
       * @see org.eclipse.jface.viewers.ICellModifier#canModify(
       * java.lang.Object, java.lang.String)
       */
      public boolean canModify(Object element, String property)
      {
         return true;
      }

      /* 
       * @see org.eclipse.jface.viewers.ICellModifier#getValue(
       * java.lang.Object, java.lang.String)
       */
      @SuppressWarnings("synthetic-access") //$NON-NLS-1$
      public Object getValue(Object element, String property)
      {
         int col = m_mapTable.getColumnIndex(property);
         MapRow mapRow = (MapRow)element;
         switch(col)
         {
            case 0:
               return StringUtils.defaultString(mapRow.key);
            case 1:
               return StringUtils.defaultString(mapRow.value);
         }
         return "";
      }

      /* 
       * @see org.eclipse.jface.viewers.ICellModifier#modify(
       * java.lang.Object, java.lang.String, java.lang.Object)
       */
      @SuppressWarnings("synthetic-access") //$NON-NLS-1$
      public void modify(Object element, String property, Object value)
      {
         if(value == null)
            return;
         int col = m_mapTable.getColumnIndex(property);
         TableItem item = (TableItem)element;
         MapRow mapRow = (MapRow)item.getData();
         switch(col)
         {
            case 0:
               mapRow.key = (String)value;
               break;
            case 1:               
               mapRow.value = (String)value;                  
               break;
         }
         mi_tableComp.refreshTable();
      }
      private PSSortableTable mi_tableComp;
   }
   
   
   private class MapRow
   {
      MapRow()
      {
         
      }
      MapRow(String key, String value)
      {
         this.key = key;
         this.value = value;
      }
      public boolean isEmpty()
      {
         if (StringUtils.isEmpty(key) && StringUtils.isEmpty(value))
            return true;
         return false;
      }
      public String key;
      public String value;
   }

   /*
    *  (non-Javadoc)
    * @see org.eclipse.swt.widgets.Widget#dispose()
    */
   public void dispose()
   {
      super.dispose();
   }

   /*
    *  (non-Javadoc)
    * @see org.eclipse.swt.widgets.Widget#checkSubclass()
    */
   protected void checkSubclass()
   {
   }

   /**
    * Temporary enum to hold keys, values and both entries, used for
    * duplicate validation
    */
   private enum KeysEnum
   {
      KEYS,
      VALUES,
      BOTH;
   }

   /**
    * Constant for duplicate map data error message
    */
   private static final String DUPLICATE_MAP_DATA = 
      "Duplicate keys are not allowed in the map.";
   
   /**
    * Constant for empty map data error message
    */
   private static final String EMPTY_MAP_DATA =
      "Empty map is not allowed for this rule.";
   
   /**
    * String constant for column title stored vale
    */
   private static final String STORED_VALUE = "Stored Value";

   /**
    * String constant for column title input vale
    */
   private static final String INPUT_VALUE = "Input Value";

   /**
    * String constant for column title output vale
    */
   private static final String OUTPUT_VALUE = "Output Value";
   
   /**
    * String constant for default encoding
    */
   private static final String DEFAULT_ENCODING = "UTF8";

   private PSSortableTable m_mapTable;

   private boolean m_isInput;



}
