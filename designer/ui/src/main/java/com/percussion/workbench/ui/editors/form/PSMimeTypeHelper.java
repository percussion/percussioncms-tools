/******************************************************************************
 *
 * [ PSMimeTypeHelper.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
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
 * Manages mime types dropdown.
 * Is created to share mime types management logic between different forms.
 * Before usage must be initialized with {@link #setCombo(Combo)}.
 *
 * @author Andriy Palamarchuk
 */
class PSMimeTypeHelper
{
   /**
    * Initiates the mime types combo from the template data  
    */
   public void loadControlValues(final PSUiAssemblyTemplate template)
   {
      setMimeType(template.getMimeType());
   }

   /**
    * Initializes mime type UI and selects the specified mime type.
    * @param mimeType the mime type to select. If <code>null</code> the first
    * type in the dropdown is selected. If the specified mime type does not
    * exist in the list of known types it is added. 
    */
   public void setMimeType(final String mimeType)
   {
      m_mimeTypes = new ArrayList<String>(MIME_TYPES);
      final int selection;
      if (StringUtils.isBlank(mimeType))
      {
         selection = 0;
         Collections.sort(m_mimeTypes, new PSIgnoreCaseStringComparator());
      }
      else
      {
         if (!m_mimeTypes.contains(mimeType))
         {
            m_mimeTypes.add(mimeType);
         }
         Collections.sort(m_mimeTypes, new PSIgnoreCaseStringComparator());
         selection = m_mimeTypes.indexOf(mimeType);
      }
      m_mimeTypes = Collections.unmodifiableList(m_mimeTypes);
      getCombo().setItems(m_mimeTypes.toArray(new String[0]));
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
    * Updates template with the combo selection.
    */
   public void updateTemplate(PSUiAssemblyTemplate template)
   {
      int idx = getCombo().getSelectionIndex();
      if (idx > -1)
         template.setMimeType(m_mimeTypes.get(idx));
   } 

   /**
    * Loads mime types data.
    */
   private static List<String> loadMimeTypes()
   {
      final List<String> mimeTypes = new ArrayList<String>();
      try
      {
         mimeTypes.addAll(PSResourceLoader.loadMimeTypes());
      }
      catch (IOException e)
      {
         PSDlgUtil.showError(e);
      }
      return Collections.unmodifiableList(mimeTypes);
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
    * Mime type label text. 
    */
   public static final String LABEL =
         PSMessages.getString("PSMimeTypeHelper.label.mimeType"); //$NON-NLS-1$

   /**
    * Ordered list of mime types.
    */
   private static final List<String> MIME_TYPES = loadMimeTypes(); 

   /**
    * Combo to select mime type.
    * @see #getCombo()
    */
   private Combo m_combo;

   /**
    * Actual list of mime types used for {@link #m_combo} dropdown.
    * Initiated in {@link #loadControlValues(PSUiAssemblyTemplate)}. 
    */
   List<String> m_mimeTypes;
}
