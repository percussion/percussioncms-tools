/*[ PSSingleSelectionEditorPanel.java ]***************************************
 * COPYRIGHT (c) 2002 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *****************************************************************************/
package com.percussion.loader.ui;

import com.percussion.UTComponents.UTFixedButton;
import com.percussion.loader.IPSContentLoader;
import com.percussion.loader.IPSContentSelector;
import com.percussion.loader.PSLoaderException;
import com.percussion.loader.PSPluginFactory;
import com.percussion.loader.objectstore.PSContentSelectorDef;
import com.percussion.loader.objectstore.PSLoaderDef;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionListener;

/**
 * Represents single plugin selection editors. The PSSingleSelectionEditorPanel
 * shows a list of available components in a list. The list allows single
 * selection only.
 */
public class PSSingleSelectionEditorPanel extends PSConfigPanel
{
   /**
    * Creates single plugin selection editor.
    */
   public PSSingleSelectionEditorPanel()
   {
      init();
   }

   /**
    * Initializes the panel.
    */
   private void init()
   {
      if (null == ms_res)
        ms_res = ResourceBundle.getBundle(
              getClass().getName() + "Resources", Locale.getDefault());

     JPanel mainPane = new JPanel();
     mainPane.setLayout(new BoxLayout(mainPane, BoxLayout.Y_AXIS));
     setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
     m_list = new JList(new DefaultListModel());
     m_list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
     JScrollPane jsp = new JScrollPane(m_list);
     Border b0 = BorderFactory.createEtchedBorder();
     Border b1 = BorderFactory.createEmptyBorder( 5, 5, 5, 5);
     Border b3 = BorderFactory.createCompoundBorder(b0, b1);
     Border b2 = BorderFactory.createTitledBorder(b3,
        PSContentLoaderResources.getResourceString(ms_res, "border.title"));

     m_applyBtn = new UTFixedButton(
        PSContentLoaderResources.getResourceString(ms_res, "button.apply"));
     m_applyBtn.addActionListener(new ActionListener()
     {
        public void actionPerformed(ActionEvent e)
        {
           Iterator itr = m_actionListenerContainer.iterator();
           while (itr.hasNext())
              ((ActionListener)itr.next()).actionPerformed(e);
        }
     });
     ms_loaderName = PSContentLoaderResources.getResourceString(
        ms_res, "loader");
     ms_selectorName = PSContentLoaderResources.getResourceString(
        ms_res, "selector");
     m_applyBtn.setEnabled(false);
     jsp.setAlignmentX(Component.CENTER_ALIGNMENT);
     m_applyBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
     mainPane.add(jsp);
     mainPane.add(Box.createRigidArea(new Dimension(0, 10)));
     mainPane.add(m_applyBtn);
     mainPane.setBorder(b2);
     add(mainPane);
     setBorder(b1);
   }

   /**
    * Sets the state of <code>m_applyBtn</code> to enabled or disabled based on
    * the parameter supplied.
    *
    * @param isEnable, sets to enable if <code>isEnable</code> is <code>true
    * </code>.
    */
   public void setApplyBtnState(boolean isEnable)
   {
      m_applyBtn.setEnabled(isEnable);
   }

   /**
    * Sets the selectors for this panel.
    *
    * @param itr, iterator of <code>PSContentSelectorDef</code> objects. Never
    * <code>null</code>, may be empty.
    *
    * @throws PSLoaderException if an error is encountered in loading selectors.
    */
   public void setSelectors(Iterator itr) throws PSLoaderException
   {
      PSPluginFactory plgFact = PSPluginFactory.getInstance();
      DefaultListModel listModel = (DefaultListModel)m_list.getModel();
      while (itr.hasNext())
      {
         PSContentSelectorDef defs = (PSContentSelectorDef)itr.next();
         IPSContentSelector contSelector = plgFact.newContentSelector(defs);
         PSConfigPanel confgPane =
               ((IPSUIPlugin)contSelector).getConfigurationUI();
         confgPane.load(defs.toXml(PSXmlDocumentBuilder.createXmlDocument()));
         PSTreeObject userObj = new PSTreeObject(defs.getName(), defs,
               confgPane);
         if (defs.getName().equals(ms_selectorName))
            userObj.setHelpId(PSDescriptorTreePanel.HID_FILESELECTOR);
         else
            userObj.setHelpId(PSDescriptorTreePanel.HID_LISTSELECTOR);
         listModel.addElement(userObj);
      }
      m_applyBtn.setActionCommand(PSDescriptorTreePanel.CONTENT_SELECTOR);
   }

   /**
    * Loads the selector for this panel.
    *
    * @param def,  <code>PSContentSelectorDef</code> objects. Assumed to be not
    * <code>null</code>.
    *
    * @throws PSLoaderException if an error is encountered in loading the
    * selector.
    */
   public PSTreeObject setSelector(PSContentSelectorDef def) throws PSLoaderException
   {
      PSPluginFactory plgFact = PSPluginFactory.getInstance();
      IPSContentSelector contSelector = plgFact.newContentSelector(def);
      PSConfigPanel confgPane =
            ((IPSUIPlugin)contSelector).getConfigurationUI();
      confgPane.load(def.toXml(PSXmlDocumentBuilder.createXmlDocument()));
      PSTreeObject userObj = new PSTreeObject(def.getName(), def,
            confgPane);
      if (def.getName().equals(ms_selectorName))
         userObj.setHelpId(PSDescriptorTreePanel.HID_FILESELECTOR);
      else
         userObj.setHelpId(PSDescriptorTreePanel.HID_LISTSELECTOR);
      return userObj;
   }

   /**
    * Sets the content loader for this panel.
    *
    * @param itr, iterator of <code>PSLoaderDef</code> objects. Never
    * <code>null</code>, may be empty.
    *
    * @throws PSLoaderException if an error is encountered in loading content
    * loaders.
    */
   public void setContentLoaders(Iterator itr) throws PSLoaderException
   {
      PSPluginFactory plgFact = PSPluginFactory.getInstance();
      DefaultListModel listModel = (DefaultListModel)m_list.getModel();

      while (itr.hasNext())
      {
         PSLoaderDef defs = (PSLoaderDef)itr.next();
         IPSContentLoader loader = plgFact.newContentLoader(defs);
         PSConfigPanel confgPane =
               ((IPSUIPlugin)loader).getConfigurationUI();
         confgPane.load(defs.toXml(PSXmlDocumentBuilder.createXmlDocument()));
         PSTreeObject userObj = new PSTreeObject(defs.getName(), defs,
            confgPane);
         //as of now we have only two loaders so it'll be fine or else not.
         if (defs.getName().equals(ms_loaderName))
            userObj.setHelpId(PSDescriptorTreePanel.HID_RHYTHMYXLOADER);
         else
            userObj.setHelpId(PSDescriptorTreePanel.HID_PREVIEWLOADER);
         listModel.addElement(userObj);
      }
      m_applyBtn.setActionCommand(PSDescriptorTreePanel.CONTENT_LOADER);
   }

   /**
    * Loads the content loader for this panel.
    *
    * @param <code>PSLoaderDef</code> objects. Assumed to be not <code>null</
    * code>.
    *
    * @return, user object that'll be attached to the child node of 'Content
    * Loader', never <code>null</code>.
    *
    * @throws PSLoaderException if an error is encountered in loading content
    * loader.
    */
   public PSTreeObject setContentLoader(PSLoaderDef def)
      throws PSLoaderException
   {
      PSPluginFactory plgFact = PSPluginFactory.getInstance();
      IPSContentLoader loader = plgFact.newContentLoader(def);
      PSConfigPanel confgPane =
            ((IPSUIPlugin)loader).getConfigurationUI();
      confgPane.load(def.toXml(PSXmlDocumentBuilder.createXmlDocument()));
      PSTreeObject userObj = new PSTreeObject(def.getName(), def, confgPane);
      if (def.getName().equals(ms_loaderName))
         userObj.setHelpId(PSDescriptorTreePanel.HID_RHYTHMYXLOADER);
      else
         userObj.setHelpId(PSDescriptorTreePanel.HID_PREVIEWLOADER);
      return userObj;
   }

   /**
    * Returns default list model for the list in this panel.
    *
    * @return default list model, never <code>null</code>, may be empty.
    */
   public DefaultListModel getListModel()
   {
      return (DefaultListModel)m_list.getModel();
   }

   /**
    * Remove all listeners for both action and list selection listeners
    */
   public void removeAllListeners()
   {
      m_actionListenerContainer.removeAllElements();
      m_list.removeAll();
   }

   /**
    * Adds the actionlistener for 'Add', 'Remove' and 'Apply' buttons in this
    * panel.
    *
    * @param listener Never <code>null</code>.
    * @throws IllegalArgumentException if the parameters are invalid.
    */
   public void addActionListener(ActionListener listener)
   {
      if (listener == null)
         throw new IllegalArgumentException("action listener cannot be null");
      m_actionListenerContainer.add(listener);
   }

   /**
    * Adds  list selection listener to this panel.
    *
    * @param listener Never <code>null</code>.
    * @throws IllegalArgumentException if the parameters are invalid.
    */
   public void addListSelectionListener(ListSelectionListener listener)
   {
      if (listener == null)
         throw new IllegalArgumentException(
            "list selection listener cannot be null");
      m_list.addListSelectionListener(listener);
   }

   /**
    * Initialized in {@link# init()}, holds the content loader's node name.
    */
   private static String ms_loaderName;

   /**
    * Initialized in {@link# init()}, holds the content selector's node name.
    */
   private static String ms_selectorName;

   /**
    * Displays a list of content selectors. Initialized in the {@link #init()}
    * and never <code>null</code> or modified after that.
    */
   private JList m_list;

   /**
    * Button for applying a selected content selector to the tree. Initialzed in
    * the {@link #init()}, <code>null</code> or modified after that.
    *
    */
   private UTFixedButton m_applyBtn;

   /**
    * Holds a list of {@link java.awt.event.ActionListener} to be notified when
    * 'Apply' button is pressed. Listeners are added in {@link
    * #addActionListener(ActionListener)}.
    */
   private Vector m_actionListenerContainer = new Vector();

   /**
    * Resource bundle for this class. Initialized in the constructor.
    * It's not modified after that. Never <code>null</code>.
    */
   private static ResourceBundle ms_res;
}