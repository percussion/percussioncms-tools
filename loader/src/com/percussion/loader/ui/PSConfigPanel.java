/*[ PSConfigPanel.java ]*******************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader.ui;

import com.percussion.loader.PSLoaderException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JPanel;

import org.w3c.dom.Element;

/**
 * The default implementation for the {@link IPSConfigPanel} interface. It does
 * not provide UI to edit the definition. The UI for this definition will be an 
 * empty panel.
 */
public class PSConfigPanel extends JPanel 
   implements IPSConfigPanel
{
   //implements the interface
   public boolean validateContent()
   { 
      return true; 
   }

   //implements the interface
   public Element save()
      throws PSLoaderException
   { 
      return m_def;
   }

   //implements the interface
   public void load(Element source)
      throws PSLoaderException
   {
      m_def = source;
   }
   

   //implements the interface.
   public void reset()
      throws PSLoaderException
   {
   }
   

   //implements the interface.
   public String getName()
   {
      return null;
   }
   
   //implements the interface
   public void addChangeListener(IPSConfigChangeListener listener)
   {
      if (listener == null)
         throw new IllegalArgumentException("listener may not be null");
         
      m_changeListeners.add(listener);
   }

   //implements the interface
   public void removeChangeListener(IPSConfigChangeListener listener)
   {
      if (listener == null)
         throw new IllegalArgumentException("listener may not be null");
         
      m_changeListeners.remove(listener);
   }

   /**
    * Fire the specified config change event.
    * 
    * @param event The change event, it may not be <code>null</code>.
    */
   protected void fireChangeEvent(PSConfigurationChangeEvent event)
   {
      if (event == null)
         throw new IllegalArgumentException("event may not be null");
         
      IPSConfigChangeListener listener = null;
      Iterator listeners = m_changeListeners.iterator();
      while (listeners.hasNext())
      {
         listener = (IPSConfigChangeListener) listeners.next();
         listener.configurationChanged(event);
      }
   }
   
   /**
    * A list of <code>IPSConfigChangeListener</code> objects, never 
    * <code>null</code>, but may be empty.
    */
   private List m_changeListeners = new ArrayList();
   
   /**
    * The loaded definition, initialized by #loader(Element), never 
    * <code>null</code> after that.
    */
   private Element m_def;
}
