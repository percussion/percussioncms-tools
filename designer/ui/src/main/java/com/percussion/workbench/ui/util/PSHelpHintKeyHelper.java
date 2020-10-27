/******************************************************************************
*
* [ PSHelpHintKeyHelper.java ]
*
* COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
* All rights reserved. This material contains unpublished, copyrighted
* work including confidential and proprietary information of Percussion.
*
*****************************************************************************/
package com.percussion.workbench.ui.util;

import com.percussion.workbench.ui.controls.PSSortableTable;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper class used by the editor and wizard pages to create the proper
 * help hint key from passed in mappings, the selected field or if the field
 * is a PSSortableTable then the selected column.
 */
public class PSHelpHintKeyHelper
{

   /**
    * Ctor to create a helper without any mappings
    */
   public PSHelpHintKeyHelper()
   {
      this(null);
   }
   
   /**
    * Ctor to create a helper with mappings provided
    * @param mappings an array of strings that represents name
    * value mapping pairs. This map must have an even number of
    * strings and the key strings cannot be <code>null</code> or
    * empty. 
    */
   public PSHelpHintKeyHelper(String[] mappings)
   {
      if(mappings != null)
      {
         if(mappings.length % 2 != 0)
            throw new IllegalArgumentException(
               "mappings must have an even number of entries.");
         for(int i = 0; i < mappings.length; i += 2)
         {
            if(StringUtils.isBlank(mappings[i]))
               throw new IllegalArgumentException("keys cannot be null or empty.");
            m_mappings.put(mappings[i], mappings[i + 1]);
         }
         
      }
   }
   
   /**
    * Add another mapping to the helper
    * @param key cannot be <code>null</code> or empty.
    * @param value the value can be <code>null</code> or empty.
    */
   public void addMapping(String key, String value)
   {
      if(StringUtils.isBlank(key))
         throw new IllegalArgumentException("key cannot be null or empty.");
      m_mappings.put(key, value);
   }
   
   /**
    * Gets the help hint key based on the existing mappings, and the
    * currently selected field and if a table, which column is selected.
    * @param base the base key provided by super.getHelpHintKey(controlInfo)
    * in the editor or wizard page. Cannot be <code>null</code>.
    * @param info the control info object. Cannot be <code>null</code>.
    * @return the help hint key.
    */
   public String getKey(String base, PSControlInfo info)
   {
      String colKey = null;
      if(info.getControl() instanceof PSSortableTable)
      {
         colKey = getNameKeyByTableColumn((PSSortableTable)info.getControl());
      }
      String nameKey = colKey == null ? info.getDisplayNameKey() : colKey;
      String fieldKey = m_mappings.get(nameKey);
      return base + "." + (fieldKey == null ? nameKey : fieldKey);
   }
   
   /**
    * Gets the name key from the table based on the currently selected
    * column. 
    * @param table assumed not <code>null</code>.
    * @return the column name key or <code>null</code> if no selection.
    */
   private String getNameKeyByTableColumn(PSSortableTable table)
   {
      int col = table.getSelectedColumn();
      if(col != -1)
         return table.getColumnPropertyKey(col);
      return null;
   }
   
   /**
    * All of the workbench to help field mappings for the editor/wizard page
    * that this helper is used in.
    */
   private Map<String, String> m_mappings = new HashMap<String, String>(); 

}
