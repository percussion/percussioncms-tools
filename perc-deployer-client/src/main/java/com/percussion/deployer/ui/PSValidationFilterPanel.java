/******************************************************************************
 *
 * [ PSValidationFilterPanel.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.deployer.ui;

import com.percussion.guitools.PSResources;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Panel containing a combo box used to filter validation results.
 */
public class PSValidationFilterPanel extends JPanel
{
   /**
    * Construct the panel.
    *
    */
   public PSValidationFilterPanel()   
   {
      init();
   }

   /**
    * Initializes all ui components on the panel
    */
   private void init()
   {
      // add label and combobox to the panel
      PSResources resources = PSDeploymentClient.getResources();
      setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
      String labelStr = resources.getString("show");
      char mn = resources.getCharacter("mn_show");
      JLabel showLabel = new JLabel(labelStr);
      showLabel.setDisplayedMnemonic(mn);
      showLabel.setDisplayedMnemonicIndex(labelStr.indexOf(mn));
      add(showLabel);
      add(Box.createHorizontalStrut(5));
      
      // create filter combo and populate      
      m_comboBox = new JComboBox();
      showLabel.setLabelFor(m_comboBox);
      
      // first item is always the "all" option
      m_comboBox.addItem(resources.getString("allResults"));
      
      // next set of items is generated from list of possible messages
      Iterator messages = getMessageList().iterator();
      while (messages.hasNext())
      {
         m_comboBox.addItem("\"" + messages.next() + "\" only");
      }
      
      // last item is always the "errors only" options
      m_comboBox.addItem(resources.getString("errorsOnly"));
      
      // add listener
      m_comboBox.addItemListener(new ItemListener() {
         public void itemStateChanged(ItemEvent e)
         {
            if (e.getStateChange() == ItemEvent.SELECTED)
               applyFilters();
         }});
      
      Dimension curSize = m_comboBox.getPreferredSize();
      m_comboBox.setMaximumSize(new Dimension(100, curSize.height));
      
      // add to panel
      add(m_comboBox);
      add(Box.createHorizontalGlue());
   }
   
   /**
    * Applies the currently selected filter to all models that have been added
    * via {@link #addValidationResults(PSValidationResultsModel)}.
    */
   protected void applyFilters()
   {
      // determine filter type
      int index = m_comboBox.getSelectedIndex();
      boolean clearFilter = false;
      boolean errorFilter = false;
      String messageFilter = null;
      if (index <= 0)
      {
         // all results is first item or none
         clearFilter = true;         
      }
      else if (index == m_comboBox.getItemCount() - 1)
      {
         // error results is last item
         errorFilter = true;         
      }
      else
      {
         // get message from index, offset by one since message options start 
         // at index of 1 in the combo box
         messageFilter = (String) ms_messageList.get(index - 1);
      }
      
      // walk list of models and set current filter
      Iterator models = m_modelList.iterator();
      while (models.hasNext())
      {
         PSValidationResultsModel model = 
            (PSValidationResultsModel) models.next();
         
         if (clearFilter)
            model.clearFilters();
         else if (errorFilter)
            model.applyErrorFilter(true);
         else
            model.applyMessageValueFilter(messageFilter);
      }
      
      // notify change listeners
      notifyChangeListeners();
   }

   /**
    * Adds a set of validation results to be filtered whenever the filter
    * combo selection changes.
    * 
    * @param model The results to add, may not be <code>null</code>.
    */
   public void addValidationResults(PSValidationResultsModel model)
   {
      if (model == null)
         throw new IllegalArgumentException("model may not be null");
      
      m_modelList.add(model);
   }

   /**
    * Adds a listener to be informed of any changes to the filter.
    * The change event will have this panel instance as its source.  Any models
    * previously added will have already had the current filter set on them.
    * 
    * @param listener The listener, may not be <code>null</code>.
    */
   public void addChangeListener(ChangeListener listener)
   {
      if (listener == null)
         throw new IllegalArgumentException("listener may not be null");
      
      m_changeListeners.add(listener);
   }

   /**
    * Removes the supplied listener so it is no longer notified of changes. See
    * {@link #addChangeListener(ChangeListener)} for details.
    * 
    * @param listener The listener, may not be <code>null</code>.  If it has not
    * previously been added as a listener, the method simply returns.
    */
   public void removeChangeListener(ChangeListener listener)
   {
      if (listener == null)
         throw new IllegalArgumentException("listener may not be null");
      
      m_changeListeners.remove(listener);
   }
   
   /**
    * Notifies all change listeners. 
    */
   protected void notifyChangeListeners()
   {
      ChangeEvent e = new ChangeEvent(this);
      Iterator listeners = m_changeListeners.iterator();
      while (listeners.hasNext())
         ((ChangeListener)listeners.next()).stateChanged(e);
   }
   
   /**
    * Gets the list of possible validation result messages.  
    * 
    * @return A list of possible values for the message column in the results
    * model, never <code>null</code>, should never be empty.
    */
   private List getMessageList()
   {
      if (ms_messageList == null)
      {
         ResourceBundle bundle = getBundle();
         int count = Integer.parseInt(bundle.getString(
            "validationMsgList.total"));
         ms_messageList = new ArrayList(count);
         for (int i = 0; i < count; i++)
         {
            String key = bundle.getString("validationMsgList." + i);
            String text = bundle.getString(key);
            ms_messageList.add(text);
         }         
      }
      
      return ms_messageList;
   }
   
   
   /**
    * This method is used to get the string resources used for display text.
    *
    * @return the bundle, never <code>null</code>.
    * 
    * @throws MissingResourceException if the bundle cannot be loaded.
    */
   protected static ResourceBundle getBundle()
   {
      if (ms_bundle == null)
      {
         ms_bundle = ResourceBundle.getBundle(
            "com.percussion.deployer.client.PSDeployStringResources");
      }

      return ms_bundle;
   }
   
   /**
    * String bundle used for message formats.  <code>null</code> until loaded
    * by a call to {@link #getBundle()}, never <code>null</code> after that.
    */
   private static ResourceBundle ms_bundle = null;   
   
   /**
    * Combobox containing all filter options, intialized during constuction,
    * never <code>null</code> or modified after that.
    */
   private JComboBox m_comboBox;
   
   /**
    * List of {@link PSValidationResultsModel} objects, intialized during
    * construction, never <code>null</code> after that.  Results are added
    * by calls to {@link #addValidationResults(PSValidationResultsModel)}.
    */
   private List m_modelList = new ArrayList();
   
   
   /**
    * List of {@link ChangeListener} objects added and removed via 
    * {@link #addChangeListener(ChangeListener)} and 
    * {@link #removeChangeListener(ChangeListener)}.  Never <code>null</code>,
    * may be empty.
    */
   private List m_changeListeners = new ArrayList();
   
   /**
    * List of message values that correspond to each message based filter option
    * in the combo box.  Initialized by first call to {@link #getMessageList()},
    * never <code>null</code> or modified after that.
    */
   private static List ms_messageList;
}

