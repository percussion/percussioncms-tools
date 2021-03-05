/*[ PSAbstractExtractorConfigTabPanel.java ]***********************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader.ui;

import com.percussion.design.objectstore.PSEntry;
import com.percussion.loader.PSLoaderException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JTable;

/**
 * Abstract class that provides default implementations of most methods in the
 * <code>IPSExtractorConfigTabPanel</code> interface as well as other common methods
 * for extractor config tab panel sub-classes. To create a concrete
 * <code>IPSExtractorConfigTabPanel</code> as a subclass of
 * <code>PSAbstractExtractorConfigTabPanel</code> you need only provide implementations
 * for the following two methods:
 *<p>
 *   &nbsp;&nbsp;&nbsp;<code>public void load(PSExtractorConfigContext config)
 *     throws PSLoaderException</code><br>
 *   &nbsp;&nbsp;&nbsp;<code>public void save(PSExtractorConfigContext config)
 *     throws PSLoaderException</code>
 *</p>
 */
public abstract class PSAbstractExtractorConfigTabPanel extends JPanel
   implements IPSExtractorConfigTabPanel
{

   // implements IPSExtractorConfigTabPanel interface method
   public void addChangeListener(IPSExtractorConfigChangeListener listener)
   {
      if(null == listener)
         throw new IllegalArgumentException("Listener cannot be null.");
      if(!m_listeners.contains(listener))
         m_listeners.add(listener);
   }

   // implements IPSExtractorConfigTabPanel interface method
   public void removeChangeListener(IPSExtractorConfigChangeListener listener)
   {
      if(null == listener)
         throw new IllegalArgumentException("Listener cannot be null.");
      if(m_listeners.contains(listener))
         m_listeners.remove(listener);
   }

   /**
    * Fires extractor configuration change event for all registered
    * listeners.
    *
    * @param type the field type that triggered this change event
    */
   protected void fireConfigChange(int type)
   {
      PSExtractorConfigChangeEvent event =
         new PSExtractorConfigChangeEvent(type, this);
      Iterator it = m_listeners.iterator();
      while(it.hasNext())
      {
         IPSExtractorConfigChangeListener listener =
            (IPSExtractorConfigChangeListener)it.next();
         listener.configChanged(event);
      }
   }

   /**
    * Tell the editor to stop editing and accept any partially edited value
    * as the value of the editor.
    *
    * @param table the table to stop editing on. May be <code>null</code>.
    */
   protected void stopTableEditing(JTable table)
   {
      if(null == table)
         return;

      if(table.isEditing())
      {
         table.getCellEditor(table.getEditingRow(),
            table.getEditingColumn()).stopCellEditing();
      }
   }

   /**
    * Returns the configuration context's current content type, if
    * not it tries to return the first content type in it's available
    * content type list. If that fails then it returns <code>null</code>.
    * @param config the configuration context for this extractor. May
    * not be <code>null</code>.
    * @return the current content type. May be <code>null</code>.
    * @throws PSLoaderException if a remote exception occurs
    */
   protected PSEntry getContentType(PSExtractorConfigContext config)
      throws PSLoaderException
   {
      if(null == config)
         throw new IllegalArgumentException(
            "Configuration context cannot be null.");

      PSEntry contenttype = config.getContentType();
      if(contenttype == null)
      {
         Iterator it = config.getContentTypes();
         if(it.hasNext())
            contenttype = (PSEntry)it.next();
      }
      return contenttype;
   }

   // implements IPSExtractorConfigTabPanel interface method always
   // returns true.
   public boolean validateContent()
   {
      return true;
   }

   // implements IPSExtractorConfigTabPanel interface method
   public void reset(PSExtractorConfigContext config)
      throws PSLoaderException
   {
      if(null == config)
         throw new IllegalArgumentException(
            "Extractor Configuration context cannot be null");
      load(config);
   }

   // implements IPSExtractorConfigTabPanel interface method
   public String getName()
   {
      return null;
   }

   /**
    * List of change listeners registered to this panel.
    * Never <code>null</code>, may be empty.
    */
   private List m_listeners = new ArrayList();

}