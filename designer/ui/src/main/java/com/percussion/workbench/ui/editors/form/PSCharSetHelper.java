/******************************************************************************
 *
 * [ PSCharSetHelper.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form;

import com.percussion.E2Designer.PSDlgUtil;
import com.percussion.client.objectstore.PSUiAssemblyTemplate;
import com.percussion.util.PSIgnoreCaseStringComparator;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.util.PSResourceLoader;
import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.widgets.Combo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages character sets dropdown.
 * Is created to share character sets management logic between different forms.
 * Before usage must be initialized with {@link #setCombo(Combo)}.
 *
 * @author Andriy Palamarchuk
 */
public class PSCharSetHelper
{
   /**
    * Initiates the char sets combo from the template data  
    */
   public void loadControlValues(final PSUiAssemblyTemplate template)
   {
      m_charSets = new ArrayList<String>(CHAR_SETS);
      final int selection;
      if (StringUtils.isBlank(template.getCharset()))
      {
         selection = 0;
         Collections.sort(m_charSets, new PSIgnoreCaseStringComparator());
      }
      else
      {
         if (!m_charSets.contains(template.getCharset()))
         {
            m_charSets.add(template.getCharset());
         }
         Collections.sort(m_charSets, new PSIgnoreCaseStringComparator());
         selection = m_charSets.indexOf(template.getCharset());
      }
      m_charSets = Collections.unmodifiableList(m_charSets);
      getCombo().setItems(m_charSets.toArray(new String[0]));
      getCombo().select(selection);
   }
   
   /**
    * Clears the combo selection and disables the control
    */
   public void clearSelectionAndDisable()
   {
      getCombo().deselectAll();
      getCombo().setEnabled(false);
   }

   /**
    * Loads charset data.
    */
   private static List<String> loadCharSets()
   {
      final List<String> charSets = new ArrayList<String>();
      try
      {
         charSets.addAll(PSResourceLoader.loadCharacterSets());
      }
      catch (IOException e)
      {
         PSDlgUtil.showError(e);
      }
      return Collections.unmodifiableList(charSets);
   }

   /**
    * Updates template with the combo selection.
    */
   public void updateTemplate(PSUiAssemblyTemplate template)
   {
      int idx = getCombo().getSelectionIndex();
      if (idx > -1)
         template.setCharset(m_charSets.get(idx));
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
    * Label text to be used with a charset chooser control. 
    */
   public static final String LABEL =
      PSMessages.getString("PSCharSetHelper.label.characterSet"); //$NON-NLS-1$

   /**
    * Ordered list of charsets.
    */
   static final List<String> CHAR_SETS = loadCharSets();
   
   /**
    * Combo to select character set.
    * @see #getCombo()
    */
   private Combo m_combo;

   /**
    * Actual list of char sets used for {@link #m_combo}.
    * Initiated in {@link #loadControlValues(PSUiAssemblyTemplate)}. 
    */
   List<String> m_charSets;
}
