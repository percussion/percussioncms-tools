/******************************************************************************
 *
 * [ PSRelTypeCategoryHelper.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form;

import com.percussion.design.objectstore.PSEntry;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.workbench.ui.PSMessages;
import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.widgets.Combo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages relationship type categories dropdown.
 * Before usage must be initialized with {@link #setCombo(Combo)}.
 *
 * @author Andriy Palamarchuk
 */
public class PSRelTypeCategoryHelper
{
   /**
    * Initiates the categories combo from the relationship type data  
    */
   public void loadControlValues(final PSRelationshipConfig relType)
   {
      final String category = StringUtils.isBlank(relType.getCategory())
            ? PSRelationshipConfig.CATEGORY_ACTIVE_ASSEMBLY
            : relType.getCategory();
      final int selection = CATEGORIES.indexOf(category);
      if (selection < 0)
      {
         throw new IllegalArgumentException(
               "Illegal category name: " + category);
      }

      getCombo().setItems(CATEGORY_NAMES.toArray(new String[0]));
      getCombo().select(selection);
      getCombo().setEnabled(!relType.isSystem());
   }
   
   /**
    * Updates relType with the combo selection.
    */
   public void updateRelType(PSRelationshipConfig relType)
   {
      relType.setCategory(CATEGORIES.get(getCombo().getSelectionIndex()));
      
   } 

   /**
    * Drop down list this class manages.
    * Must be initialized by client code. 
    */
   public Combo getCombo()
   {
      return m_combo;
   }

   /**
    * @see #getCombo()
    */
   public void setCombo(Combo combo)
   {
      m_combo = combo;
   }
   
   /**
    * Category type label text. 
    */
   public static final String LABEL =
         PSMessages.getString("PSRelTypeCategoryHelper.label.category"); //$NON-NLS-1$

   /**
    * Categories.
    */
   static final List<String> CATEGORIES; 

   /**
    * Category names.
    */
   private static final List<String> CATEGORY_NAMES;

   static
   {
      final List<String> categories = new ArrayList<String>(); 
      final List<String> categoryNames = new ArrayList<String>(); 
      for (final PSEntry entry : PSRelationshipConfig.CATEGORY_ENUM)
      {
         categories.add(entry.getValue());
         categoryNames.add(entry.getLabel().getText());
      }
      CATEGORIES = Collections.unmodifiableList(categories);
      CATEGORY_NAMES = Collections.unmodifiableList(categoryNames);
   }

   /**
    * Combo to select category.
    * @see #getCombo()
    */
   private Combo m_combo;
}
