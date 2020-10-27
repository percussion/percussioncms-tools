/******************************************************************************
*
* [ PSUrlParamTableComposite.java ]
*
* COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
* All rights reserved. This material contains unpublished, copyrighted
* work including confidential and proprietary information of Percussion.
*
*****************************************************************************/
/**
 * 
 */
package com.percussion.workbench.ui.editors.common;

import com.percussion.utils.types.PSPair;
import com.percussion.workbench.ui.controls.IPSNewRowObjectProvider;
import com.percussion.workbench.ui.controls.PSAbstractTableLabelProvider;
import com.percussion.workbench.ui.controls.PSComboBoxCellEditor;
import com.percussion.workbench.ui.controls.PSSortableTable;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableItem;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Implements a common Url parameter table that accepts and parses
 * a string url for the query name/value pairs and also returns
 * the pairs as a query string. See {@link #setValue(String)}
 * and {@link #getValue()}.
 */
public class PSUrlParamTableComposite extends PSSortableTable
{

   /**
    * @param parent
    */
   public PSUrlParamTableComposite(Composite parent, List<String> contextParams)
   {
      super(parent, getLabelProvider(), getObjectProvider(),
         SWT.NONE,
         PSSortableTable.INSERT_ALLOWED |
         PSSortableTable.DELETE_ALLOWED);
      super.setCellModifier(new CellModifier(this));
      CellEditor cEditor = new TextCellEditor(this.getTable());
      super.addColumn("PSUrlParamTableComposite.col.name.label",
         PSSortableTable.NONE, //$NON-NLS-1$
         new ColumnWeightData(5, 40), cEditor, SWT.LEFT);
      
      if(contextParams == null || contextParams.isEmpty())
      {
         cEditor = new TextCellEditor(this.getTable());
      }
      else
      {
         String[] params = new String[contextParams.size()]; 
         contextParams.toArray(params); 
         cEditor = new PSComboBoxCellEditor(this.getTable(), params);
      }
      
      super.addColumn("PSUrlParamTableComposite.col.value.label",
         PSSortableTable.NONE, //$NON-NLS-1$
         new ColumnWeightData(5, 40), cEditor, SWT.LEFT);
   }   
   
   /** 
    * Overriden to throw an <code>UnsupportedOperationException</code>
    */
   @Override
   @SuppressWarnings("unused")
   public void addColumn( String name, int options, ColumnLayoutData layoutData,
      CellEditor cellEditor, int style)
   {
      throw new UnsupportedOperationException();
   }

   /** 
    * Overriden to throw an <code>UnsupportedOperationException</code>
    */
   @Override
   @SuppressWarnings("unused")
   public void setCellModifier(ICellModifier modifier)
   {
      throw new UnsupportedOperationException();
   }
   
   /** 
    * Overriden to throw an <code>UnsupportedOperationException</code>
    */
   @Override
   @SuppressWarnings("unused")
   public void setValues(List values)
   {
      throw new UnsupportedOperationException();
   }
         
   /**
    * Sets the param row values by parsing the passed in url
    * string for the query section if it exists.
    * @param url the url to be parsed, can be <code>null</code>
    * or empty.
    */
   public void setValue(String url)
   {
      super.setValues(getParameterPairs(StringUtils.defaultString(url)));
   }
   
   /**
    * @return the params formatted as a query string
    */
   @SuppressWarnings("unchecked") //$NON-NLS-1$
   public String getValue()
   {
      return getQueryFromPairs(
         super.getValues());
   }
   
   /**
    * @return iterator of parameter name/value pairs. Never
    * <code>null</code>, may be empty.
    */
   @SuppressWarnings("unchecked")
   public Iterator<PSPair<String, String>> getParamPairs()
   {
      return (Iterator<PSPair<String, String>>)super.getValues().iterator();
   }

   /**
    * Creates the row new object provider
    * @return object provider never <code>null</code>.
    */
   private static IPSNewRowObjectProvider getObjectProvider()
   {
      IPSNewRowObjectProvider objectProvider = new IPSNewRowObjectProvider()
      {

         /* 
          * @see com.percussion.workbench.ui.controls.IPSNewRowObjectProvider#
          * newInstance()
          */
         public Object newInstance()
         {
            return new PSPair<String, String>();
         }

         /* 
          * @see com.percussion.workbench.ui.controls.IPSNewRowObjectProvider#
          * isEmpty(java.lang.Object)
          */
         @SuppressWarnings("unchecked") //$NON-NLS-1$
         public boolean isEmpty(Object obj)
         {
            PSPair<String, String> pair = (PSPair<String, String>) obj;
            return StringUtils.isBlank(pair.getFirst()) 
               || StringUtils.isBlank(pair.getSecond());
         }
         
      };
      return objectProvider;
   }
   
   /**
    * Creates the table label provider
    * @return the label provider, never <code>null</code>.
    */
   private static ITableLabelProvider getLabelProvider()
   {
      ITableLabelProvider labelProvider = new PSAbstractTableLabelProvider()
      {
         /* 
          * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(
          * java.lang.Object, int)
          */
         @SuppressWarnings("unchecked") //$NON-NLS-1$
         public String getColumnText(Object element, int columnIndex)
         {
            PSPair<String, String> pair = (PSPair<String, String>)element;
            String value = 
               columnIndex == 0 ? pair.getFirst() : pair.getSecond();
            return StringUtils.defaultString(value);
         }
         
      };
      return labelProvider;
   }
   
   /**
    * Utility method to turn a query string into a list of <code>PSPair</code>
    * objects. Also URL decodes the value.
    * @param url the url string, may be <code>null</code> or empty.
    * @return list of <code>PSPair&lt;String, String&gt;</code> objects.
    * Never <code>null</code>, may be empty.
    */
   @SuppressWarnings("unchecked") //$NON-NLS-1$
   private List<PSPair<String, String>> getParameterPairs(String url)
   {
      List<PSPair<String, String>> results = 
         new ArrayList<PSPair<String, String>>();
      if(url != null && url.indexOf('?') != -1)
      {
         try
         {
            int pos = url.indexOf('?');
            if(pos < url.length() - 1)
            {
               String query = url.substring(pos + 1).trim();
               StringTokenizer st_Amps = new StringTokenizer(query, "&"); //$NON-NLS-1$
               while(st_Amps.hasMoreTokens())
               {
                  String temp = st_Amps.nextToken();
                  StringTokenizer st_Equals = new StringTokenizer(temp, "="); //$NON-NLS-1$
                  if(st_Equals.countTokens() == 2)
                  {
                     String name = st_Equals.nextToken();
                     String value = URLDecoder.decode(st_Equals.nextToken(), "UTF-8"); //$NON-NLS-1$
                     results.add(new PSPair(name, value));
                  }
                     
               }
            }
         }
         catch (UnsupportedEncodingException e)
         {
            e.printStackTrace();
         }
      }
      return results;
   }
   
   /**
    * Utility method to turn a list of <code>PSPair&lt;String, String&gt;</code> objects
    * into a URL query string.
    * @param pairs name/value parameter pairs, cannot be <code>null</code>
    * @return the query string, never <code>null</code>, may be empty.
    */
   private String getQueryFromPairs(List<PSPair<String, String>> pairs)
   {
      if(pairs == null)
         throw new IllegalArgumentException("pairs cannot be null."); //$NON-NLS-1$
      StringBuilder sb = new StringBuilder();
      int len = pairs.size();
      try
      {
         for(int i = 0; i < len; i++)
         {
            PSPair<String, String> pair = pairs.get(i);
            sb.append(pair.getFirst());
            sb.append("="); //$NON-NLS-1$
            sb.append(URLEncoder.encode(pair.getSecond(), "UTF-8")); //$NON-NLS-1$
            if(i < len - 1)
               sb.append("&"); //$NON-NLS-1$
         }
      }
      catch (UnsupportedEncodingException e)
      {
         e.printStackTrace();
      }
      return sb.toString();
   }
   
   /**
    * Cell modifer for the url parameters table
    */
   class CellModifier implements ICellModifier
   {

      protected CellModifier(PSUrlParamTableComposite comp)
      {
         mi_tableComp = comp;
      }
      
      @SuppressWarnings("unused")
      public boolean canModify(Object element, String property)
      {
         // Always allow
         return true;
      }

      @SuppressWarnings({"synthetic-access","unchecked"}) //$NON-NLS-1$ //$NON-NLS-2$
      public Object getValue(Object element, String property)
      {
         int col = mi_tableComp.getColumnIndex(property);
         PSPair<String, String> pair = (PSPair<String, String>)element;
         switch(col)
         {
            case 0:
               return StringUtils.defaultString(pair.getFirst());
            case 1:
               return StringUtils.defaultString(pair.getSecond());
         }
         return ""; //$NON-NLS-1$
      }

      @SuppressWarnings({"unchecked","synthetic-access"}) //$NON-NLS-1$ //$NON-NLS-2$
      public void modify(Object element, String property, Object value)
      {
         int col = mi_tableComp.getColumnIndex(property);
         TableItem item = (TableItem)element;
         PSPair<String, String> pair = (PSPair<String, String>)item.getData();
         switch(col)
         {
            case 0:
               pair.setFirst(StringUtils.defaultString((String)value));
               break;
            case 1:
               pair.setSecond(StringUtils.defaultString((String)value));
               break;
         }
         mi_tableComp.refreshTable();
         
      }
      
      private PSUrlParamTableComposite mi_tableComp;
      
   }
}
