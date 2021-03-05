/* *****************************************************************************
 *
 * [ PSMultiSelectionEditorPanel.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *******************************************************************************/
package com.percussion.loader.ui;

import com.percussion.UTComponents.UTFixedButton;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.guitools.ResourceHelper;
import com.percussion.loader.IPSFieldTransformer;
import com.percussion.loader.IPSItemExtractor;
import com.percussion.loader.IPSItemTransformer;
import com.percussion.loader.IPSLogCodes;
import com.percussion.loader.PSLoaderException;
import com.percussion.loader.PSLogMessage;
import com.percussion.loader.PSPluginFactory;
import com.percussion.loader.objectstore.PSExtractorDef;
import com.percussion.loader.objectstore.PSFieldTransformationDef;
import com.percussion.loader.objectstore.PSLoaderComponent;
import com.percussion.loader.objectstore.PSLoaderNamedComponent;
import com.percussion.loader.objectstore.PSTransformationDef;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;

/**
 * Panel representing multiple plugin selection editor. It shows a list of
 * available and a list of used components in 'Available' and 'Used' lists.
 * Both the lists allow multiple selection. It has 'Add' and 'Remove' buttons.
 * If the 'User' list has focus then 'Add' button is enabled and 'Remove' is
 * disabled and vice versa if 'Available' list has the focus.
 *
 * The 'Used' list has 'Up' and 'Down' buttons to the right for ordering the
 * selected components.
 */
public class PSMultiSelectionEditorPanel extends PSConfigPanel
{
   /**
    * Creates multiple plugin selection editor.
    */
   public PSMultiSelectionEditorPanel()
   {
      init();
   }
   
   /**
    * Initializes this panel.
    *
    */
   private void init()
   {
      if (null == ms_res)
         ms_res = ResourceBundle.getBundle(
               getClass().getName() + "Resources", Locale.getDefault());
      ms_pageExt = PSContentLoaderResources.getResourceString(ms_res,
         "pageExt");
      ms_imgExt = PSContentLoaderResources.getResourceString(ms_res,
         "imgExt");
      ms_fileExt = PSContentLoaderResources.getResourceString(ms_res,
         "fileExt");
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
      JScrollPane jsp = new JScrollPane();
      m_availableList = new JList(new DefaultListModel());
      m_availableList.setSelectionMode(
         ListSelectionModel.SINGLE_INTERVAL_SELECTION);
      m_availableList.addListSelectionListener(new AvailableListListener());

      jsp.setViewportView(m_availableList);
      Border b0 = BorderFactory.createEtchedBorder();
      Border b1 = BorderFactory.createEmptyBorder( 5, 10, 10, 10);
      Border b3 = BorderFactory.createCompoundBorder(b0, b1);
      Border b2 = BorderFactory.createTitledBorder(b3,
         PSContentLoaderResources.getResourceString(ms_res,
         "border.list.available"));
      jsp.setBorder(b2);
      add(jsp);

      JPanel btnPanel = new JPanel();
      btnPanel.setLayout(new BoxLayout(btnPanel, BoxLayout.X_AXIS));

      // setup [add] (from config --> descriptor) button
      ImageIcon downIcon = new ImageIcon(getClass().getResource(
         PSContentLoaderResources.getResources().getString("down")));
      m_addBtn = new UTFixedButton(downIcon);
      m_addBtn.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            onAdd();
            m_actionListener.actionPerformed(e);
         }
      });

      btnPanel.add(m_addBtn);
      add(Box.createRigidArea(new Dimension(0, 10)));
      add(btnPanel);
      JPanel bottomPane = new JPanel();
      bottomPane.setLayout(new BoxLayout(bottomPane, BoxLayout.X_AXIS));
      JToolBar tb = new JToolBar();

      // setup [up] button
      ImageIcon upIcon = new ImageIcon(getClass().getResource(
         PSContentLoaderResources.getResources().getString("up")));
      m_upBtn = new JButton();
      m_upBtn.setActionCommand("up");
      m_upBtn.setToolTipText(ResourceHelper.getToolTipText(
         PSContentLoaderResources.getResources(), "up"));
      m_upBtn.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            onMove(true);
            m_actionListener.actionPerformed(e);
         }
      });
      m_upBtn.setIcon(upIcon);

      // setup [down] button
      m_dwnBtn = new JButton();
      m_dwnBtn.setActionCommand("down");
      m_dwnBtn.setToolTipText(ResourceHelper.getToolTipText(
         PSContentLoaderResources.getResources(), "down"));
      m_dwnBtn.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            onMove(false);
            m_actionListener.actionPerformed(e);
         }
      });
      m_dwnBtn.setIcon(downIcon);
      
      // setup [delete] button
      ImageIcon deleteIcon = new ImageIcon(getClass().getResource(
         PSContentLoaderResources.getResources().getString("delete")));
      m_deleteBtn = new JButton();
      m_deleteBtn.setActionCommand("delete");
      m_deleteBtn.setToolTipText(ResourceHelper.getToolTipText(
         PSContentLoaderResources.getResources(), "delete"));
      m_deleteBtn.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            onRemove();
            m_actionListener.actionPerformed(e);
         }
      });
      m_deleteBtn.setIcon(deleteIcon);
      
      
      tb.add(m_upBtn);
      tb.add(Box.createRigidArea(new Dimension(0, 10)));
      tb.add(m_dwnBtn);
      tb.add(Box.createRigidArea(new Dimension(0, 10)));
      tb.add(m_deleteBtn);
      tb.setOrientation(JToolBar.VERTICAL);

      JScrollPane jspBottom = new JScrollPane();
      m_editedList = new JList(new DefaultListModel());
      m_editedList.setSelectionMode(
            ListSelectionModel.SINGLE_INTERVAL_SELECTION);
      m_editedList.addListSelectionListener(new EditedListListener());

      jspBottom.setViewportView(m_editedList);
      Border bb2 = BorderFactory.createTitledBorder(b3,
         PSContentLoaderResources.getResourceString(ms_res,
         "border.list.used"));

      bottomPane.add(jspBottom);
      bottomPane.add(Box.createRigidArea(new Dimension(10, 0)));
      bottomPane.add(tb);
      bottomPane.setBorder(bb2);
      add(bottomPane);
      setBorder(BorderFactory.createEmptyBorder( 10, 10, 10, 10));
      setButtonsState();
   }

   /**
    * Adds the selected items from the available list to the used list.
    */
   private void onAdd()
   {
      int[] intAr = m_availableList.getSelectedIndices();
      DefaultListModel targetList = (DefaultListModel)m_editedList.getModel();
      DefaultListModel srcList = (DefaultListModel)m_availableList.getModel();
      int len = intAr.length;
      for(int k = 0; k < len; k++)
      {
         PSTreeObject userObject = (PSTreeObject)srcList.get(intAr[k]);
         try
         {
            // clone the source definition, so that it will not be override
            PSLoaderComponent comp = 
               (PSLoaderComponent) userObject.getDataObj().clone();
            if (comp instanceof PSLoaderNamedComponent)
            {
               PSLoaderNamedComponent namedComp = (PSLoaderNamedComponent) comp;
               String name = getUniqueName(targetList, namedComp.getName());
               namedComp.setName(name);
            }
            PSTreeObject addedUserObject = createTreeObject(comp);
            targetList.addElement(addedUserObject);
         }
         catch (PSLoaderException e) // not possible
         {
            e.printStackTrace();
            return;
         }
      }
      setButtonsState();
   }

   /**
    * Creates an user object from the supplied definition.
    * 
    * @param def The definition, assume it is a definition of extractor or
    *    transformer, not <code>null</code>.
    * 
    * @return The created user object, never <code>null</code>.
    * 
    * @throws PSLoaderException if an error occurs.
    * 
    * @throws IllegalArgumentException if the definition is not an instance
    *    of extractor or transformer.
    */   
   private PSTreeObject createTreeObject(PSLoaderComponent def)
      throws PSLoaderException
   {
      if (def instanceof PSExtractorDef)
      {
         return createExtractorTreeObject((PSExtractorDef)def, false);
      }
      else if (def instanceof PSFieldTransformationDef)
      {
         return createFieldTransObject((PSFieldTransformationDef)def);
      }
      else if (def instanceof PSTransformationDef)
      {
         return createItemTransObject((PSTransformationDef)def);
      }
      else
      {
         throw new IllegalArgumentException("Unexpected tree node");
      }
   }
   
   /**
    * Get a (unique) name which is not exist in the supplied list. The unique
    * name will use the supplied name as a based and append additional digit
    * as needed.
    * 
    * @param targetList The list that will contain the user object, assume not
    *    <code>null</code>.
    *  
    * @param baseName The base name for the unique name, assume not
    *    <code>null</code> or empty.
    *    
    * @return The unique name, which may be the <code>baseName</code> if it is
    *    unique already. Never <code>null</code> or empty.
    */
   private String getUniqueName(DefaultListModel targetList, String name)
   {
      boolean done = false;
      while (!done)
      {
         if (! findName(targetList, name))
            done = true;
         else 
            name = makeNewName(name);
      }
      return name;
   }

   /**
    * Makes a new name for the supplied baseName
    * 
    * @param baseName The base name, assume not <code>null</code> or empty.
    * 
    * @return the created name, never <code>null</code> or empty.
    */
   private String makeNewName(String name)
   {
      char lastCh = name.charAt(name.length()-1);
      if (Character.isDigit(lastCh))
      {
         if (lastCh < '9')
         {
            // last-char + 1
            String chString = new String(new char[] {lastCh});
            int i = Integer.parseInt(chString);
            chString = Integer.toString(i+1);
            
            name = name.substring(0, name.length()-1) + chString;
         }
         else // last char is '9', just add one more zero
         {
            name = name + "0";
         }
      }
      else // last char is not digit, just add one more zero
      {
         name = name + "0";
      }
            
      return name;
   }
      
   /**
    * Looks for the specified name in the supplied list.
    * 
    * @param targetList The list to be searched from, assume not
    *    <code>null</code>.
    * 
    * @param name The searched name, assume not <code>null</code> or empty.
    * 
    * @return <code>true</code> if found the name; <code>false</code> otherwise.
    */
   private boolean findName(DefaultListModel targetList, 
      String name)
   {
      Enumeration e = targetList.elements();
      PSTreeObject treeObject;
      while (e.hasMoreElements())
      {
         treeObject = (PSTreeObject) e.nextElement();
         if (treeObject.getName().equalsIgnoreCase(name))
            return true;
      }
      return false;
   }

   /**
    * Removes the selected items from the used list.
    */
   private void onRemove()
   {
      int[] intAr = m_editedList.getSelectedIndices();
      DefaultListModel listModel = (DefaultListModel)m_editedList.getModel();
      int len = intAr.length;
      int shift = 0;
      for(int k = 0; k < len; k++)
      {
         PSTreeObject userObject = (PSTreeObject)listModel.get(intAr[k]);
         listModel.removeElementAt(intAr[k] - shift);
         shift++;
      }
      setButtonsState();
   }

   /**
    * Moves items in the <code>m_editedList</code> list up or down.
    *
    * @boolean up, if <code>true</code> item/s are moved up else down.
    */
   private void onMove(boolean up)
   {
      int[] intAr = m_editedList.getSelectedIndices();
      DefaultListModel listModel = (DefaultListModel)m_editedList.getModel();
      int size = listModel.getSize();
      int len = intAr.length;
      int previousIndex = 0;
      int nextIndex = 0;

      if (up)
      {
         nextIndex = intAr[len - 1];
         previousIndex = intAr[0] - 1;
         Object obj = listModel.remove(previousIndex);
         listModel.add(nextIndex, obj);
      }
      else
      {
         nextIndex = intAr[len - 1] + 1;
         previousIndex = intAr[0];
         Object obj = listModel.remove(nextIndex);
         listModel.add(previousIndex, obj);
         m_editedList.setSelectionInterval(previousIndex + 1, nextIndex);
      }
      setButtonsState();
   }

   /**
    * Sets all the buttons in this panel to enabled or disabled state based on
    * the selection  in either of the list boxes - <code>m_availableList</code>
    * and <code>m_editedList</code>.
    */
   private void setButtonsState()
   {
      if(m_availableList.getModel().getSize() == 0)
      {
         m_addBtn.setEnabled(false);
      }
      else
      {
         if (m_availableList.getSelectedIndices().length == 0)
            m_addBtn.setEnabled(false);
         else
            m_addBtn.setEnabled(true);
      }
      if(m_editedList.getModel().getSize() == 0)
      {
         m_deleteBtn.setEnabled(false);
      }
      else
      {
         if (m_editedList.getSelectedIndices().length == 0)
            m_deleteBtn.setEnabled(false);
         else
            m_deleteBtn.setEnabled(true);
      }

      int size = m_editedList.getModel().getSize();
      if (size == 0 || size == 1)
      {
         m_dwnBtn.setEnabled(false);
         m_upBtn.setEnabled(false);
      }
      else
      {
         m_dwnBtn.setEnabled(true);
         m_upBtn.setEnabled(true);
      }

      int[] selIndcs = m_editedList.getSelectedIndices();
      if (selIndcs.length != 0)
      {
        if (selIndcs[0] == 0)
           m_upBtn.setEnabled(false);
        else
           m_upBtn.setEnabled(true);

        if (selIndcs[selIndcs.length - 1] ==
           m_editedList.getModel().getSize() - 1)

           m_dwnBtn.setEnabled(false);
        else
            m_dwnBtn.setEnabled(true);
      }
      else
      {
         m_dwnBtn.setEnabled(false);
         m_upBtn.setEnabled(false);
      }
   }

   /**
    * Enables <code>m_removeButton</code> if there is a selection in </code>
    * m_editedList</code> or else disables it.
    */
   private class EditedListListener implements ListSelectionListener
   {
      public void valueChanged(ListSelectionEvent e)
      {
         setButtonsState();
      }
   }

   /**
    * Enables <code>m_addButton</code> if there is a selection in </code>
    * m_availableList</code> or else disables it.
    */
   private class AvailableListListener implements ListSelectionListener
   {
      public void valueChanged(ListSelectionEvent e)
      {
           setButtonsState();
      }
   }

   /**
    * Gets the list model for </code>m_editedList</code>. An instanceof
    * {@link java.swing.DefaultListModel}.
    *
    * @return list model, never <code>null</code>, may be empty.
    */
   ListModel getEditedListModel()
   {
      return m_editedList.getModel();
   }

   /**
    * Gets the list model for </code>m_availableList</code>. An instanceof
    * {@link java.swing.DefaultListModel}.
    *
    * @return list model, never <code>null</code>, may be empty.
    */
   ListModel getAvailableListModel()
   {
      return m_availableList.getModel();
   }

   /**
    * Sets the actionlistener for 'Add' and 'Remove' buttons in this panel.
    *
    * @param listener Never <code>null</code>.
    * @throws IllegalArgumentException if the parameters are invalid.
    */
   public void setActionListener(ActionListener listener)
   {
      if (listener == null)
         throw new IllegalArgumentException("action listener cannot be null");
      m_actionListener = listener;
   }

   /**
    * Creates an user object from the supplied extractor definition.
    * 
    * @param def The extractor definition, assume not <code>null</code>.
    * 
    * @param isEdited <code>true</code> if the created user object will be
    *    inserted into the edited (or used) list; otherwise it will be
    *    inserted into the available list.
    * 
    * @return
    * 
    * @throws PSLoaderException
    */
   private PSTreeObject createExtractorTreeObject(PSExtractorDef def, 
      boolean isEdited)
      throws PSLoaderException
   {
      PSPluginFactory plgFact = PSPluginFactory.getInstance();
      IPSItemExtractor extractor = plgFact.newItemExtractor(def);
      PSConfigPanel configPane = null;
      if (extractor instanceof IPSUIPlugin)
      {
         configPane = ((IPSUIPlugin)extractor).getConfigurationUI();
         configPane.load(def.toXml(
            PSXmlDocumentBuilder.createXmlDocument()));
      }
      
      PSTreeObject node = new PSTreeObject(def.getName(), def, configPane);
      
      setHelpIdForExtractor(node);
      
      if ( def.isStaticType())
         setActionCommandForButtons(PSDescriptorTreePanel.STATIC_ITEMS);
      else 
         setActionCommandForButtons(PSDescriptorTreePanel.ITEMS);   
         
      //add children to created PSTreeObject

      if (!def.isStaticType())
      {
         //load target field Names for each content type
         //set field transformations
         Iterator fTransItr = def.getFieldTransformations();
         PSMultiSelectionEditorPanel msEdPane = null;
         PSTreeObject childNode = null;
         if (fTransItr.hasNext())
         {
            msEdPane = new PSMultiSelectionEditorPanel();
            msEdPane.setActionListener(m_actionListener);
            msEdPane.setActionCommandForButtons(
               PSDescriptorTreePanel.FLD_TRANS);
            childNode = new PSTreeObject(PSDescriptorTreePanel.FLD_TRANS,
               msEdPane);
            node.addChildren(childNode);
            loadFieldTransformers(fTransItr, msEdPane, isEdited);
         }
         //set item transformations
         Iterator iTransItr = def.getItemTransformations();
         if (iTransItr.hasNext())
         {
            msEdPane = new PSMultiSelectionEditorPanel();
            msEdPane.setActionCommandForButtons(
               PSDescriptorTreePanel.ITM_TRANS);
            msEdPane.setActionListener(m_actionListener);
            childNode = new PSTreeObject(PSDescriptorTreePanel.ITM_TRANS,
               msEdPane);
            node.addChildren(childNode);
            loadItemTransformers(iTransItr, msEdPane, isEdited);
         }

      }
      
      return node;      
   }
   
   /**
    * Set the help id for the specified extarctor's panel.
    * 
    * @param node The extractor panel, assume not <code>null</code>.
    * 
    * @throws PSLoaderException if an error occurs.
    */
   private void setHelpIdForExtractor(PSTreeObject node)
      throws PSLoaderException
   {
      PSConfigPanel panel = node.getUIObj();
      
      if (panel instanceof PSStaticItemExtractorPanel)
      {
         node.setHelpId(PSDescriptorTreePanel.HID_STATIC_EXTRACTOR);
      }
      else if (panel instanceof PSPageConfigPanel)
      {
         node.setHelpId(PSDescriptorTreePanel.HID_PAGE_EXTRACTOR);
      }
      else if (panel instanceof PSXMLConfigPanel)
      {
         node.setHelpId(PSDescriptorTreePanel.HID_XML_EXTRACTOR);
      }
      else if (panel instanceof PSXSLConfigPanel)
      {
         node.setHelpId(PSDescriptorTreePanel.HID_XSL_EXTRACTOR);
      }
      else if (panel instanceof PSBinaryConfigPanel)
      {
         node.setHelpId(PSDescriptorTreePanel.HID_BINARY_EXTRACTOR);
      }
   }
   
   /**
    * Reset or cleanup all element in the edited (or used) list.
    */
   public void resetEditedList()
   {
      DefaultListModel editedList = (DefaultListModel)m_editedList.getModel();
      editedList.removeAllElements();
   }
   
   /**
    * Sets the static and internal extractors for selection in the right panel
    * of {@link PSContentDescriptorDialog}.
    *
    * @param itr, an iteration over {@link PSExtractorDef}, may be empty but
    * never <code>null</code>.
    *
    * @param edit, if <code>true</code> then a descriptor has been loaded for
    * editing else a new descriptor is to be created.
    *
    * @throws PSLoaderException, thrown if there is an exception associated with
    * loader application.
    */
   public void setExtractors(Iterator itr, boolean edit)
      throws PSLoaderException, PSUnknownNodeTypeException
   {
      PSPluginFactory plgFact = PSPluginFactory.getInstance();
      DefaultListModel availList = (DefaultListModel)m_availableList.getModel();
      DefaultListModel editedList = (DefaultListModel)m_editedList.getModel();
      // reset or cleanup before set the new list
      if (edit)
         resetEditedList();
      else
         availList.removeAllElements();
      PSTreeObject node = null;
      Object obj = null;
      IPSItemExtractor iExt = null;
      while (itr.hasNext())
      {
         obj = itr.next();
         if (obj instanceof PSExtractorDef)
         {
            try
            {
               node = createExtractorTreeObject((PSExtractorDef)obj, edit);
            }
            catch (PSLoaderException e) // log and ignore the BAD extractor
            {
               Logger.getLogger(getClass()).error(e.getLocalizedMessage());
               
               String[] args = {((PSExtractorDef)obj).getName()};
               PSLogMessage   msg = new PSLogMessage(
                  IPSLogCodes.FAILED_LOAD_EXTRACTOR_DEF,
                  args, null, PSLogMessage.LEVEL_ERROR);
               Logger.getLogger(getClass()).error(msg);

               continue;
            }

            if (edit)
               editedList.addElement(node);
            else
               availList.addElement(node);
         }
         else
            throw new IllegalArgumentException("Only extractors are expected");
      }
   }

   /**
    * Set the specified command to all the button on this panel
    * 
    * @param cmd The to be set command, assume it is not <code>null</code>
    *    or empty.
    */
   private void setActionCommandForButtons(String cmd)
   {
      m_deleteBtn.setActionCommand(cmd);
      m_addBtn.setActionCommand(cmd);
      m_upBtn.setActionCommand(cmd);
      m_dwnBtn.setActionCommand(cmd);
   }
   
   /**
    * Sets global item and field transformers.
    *
    * @param defs A list of transformer definitions, an iteration over 
    *    <code>PSTransformationDef</code>, never <code>null</code>, but may be 
    *    empty.
    *
    * @param isItem <code>true</code> if it is for item transformer; otherwise
    *    it is for field transformer.
    *
    * @param isEdited <code>true</code> if the list will be used for edited;
    *    otherwise it will be used for available list.
    *
    * @throws PSLoaderException if an error occurs.
    */
   public void setTransformers(Iterator defs, boolean isItem, boolean isEdited)
      throws PSLoaderException
   {
      if (isItem)
      {
         loadItemTransformers(defs, this, isEdited);
         setActionCommandForButtons(PSDescriptorTreePanel.ITM_TRANS);
      }
      else
      {
         loadFieldTransformers(defs, this, isEdited);
         setActionCommandForButtons(PSDescriptorTreePanel.FLD_TRANS);
      }
   }

   /**
    * Loads item transformers.
    *
    * @param itr an iteration over {@link PSTransformationDef}, never <code>
    * null</code>, may be empty.
    *
    * @param pane {@link PSMultiSelectionEditorPanel} holding the item
    * transformers, assumed to be not <code>null</code>.
    *
    * @param isEdited if <code>true</code> then a descriptor has been loaded for
    * editing else a new descriptor is to be created.
    *
    * @throws PSLoaderException, thrown if there is an exception associated with
    * loader application.
    */
   private void loadItemTransformers(Iterator itr, PSMultiSelectionEditorPanel
      pane, boolean isEdited) throws PSLoaderException
   {
      PSTransformationDef transDef = null;
      PSPluginFactory plgFact = PSPluginFactory.getInstance();
      DefaultListModel dlm = (DefaultListModel)pane.m_availableList.getModel();
      DefaultListModel ulm = (DefaultListModel)pane.m_editedList.getModel();
      PSTreeObject node = null;
      while(itr.hasNext())
      {
         transDef = (PSTransformationDef)itr.next();
         try
         {
            node = createItemTransObject(transDef);
         }
         catch (PSLoaderException e) // log and ignore the BAD transformer
         {
            Logger.getLogger(getClass()).error(e.getLocalizedMessage());
            
            String[] args = {transDef.getName()};
            PSLogMessage   msg = new PSLogMessage(
               IPSLogCodes.FAILED_LOAD_ITEM_TRANSFORMER_DEF,
               args, null, PSLogMessage.LEVEL_ERROR);
            Logger.getLogger(getClass()).error(msg);

            continue;            
         }
         
         if (isEdited)
            ulm.addElement(node);
         else
            dlm.addElement(node);
      }
   }

   /**
    * Creates an user object for the supplied item transformation.
    * 
    * @param transDef The definition of the item transformation, assume not 
    *    <code>null</code>.
    * 
    * @return The created user object, never <code>null</code>.
    * 
    * @throws PSLoaderException if an error occurs.
    */
   private PSTreeObject createItemTransObject(PSTransformationDef transDef)
      throws PSLoaderException
   {
      PSPluginFactory plgFact = PSPluginFactory.getInstance();
      IPSItemTransformer transformer =
            plgFact.newItemTransformer(transDef);
      PSConfigPanel panel = null;
      
      if (transformer instanceof IPSUIPlugin)
      {
         panel =
               ((IPSUIPlugin)transformer).getConfigurationUI();
         panel.load(transDef.toXml(
               PSXmlDocumentBuilder.createXmlDocument()));
      }

      PSTreeObject node = new PSTreeObject(transDef.getName(), transDef, panel);
      
      return node;
   }

   /**
    * Creates user object for the specified field transformation.
    * 
    * @param transDef The definition of the field transformation, assume
    *    not <code>null</code>.
    * 
    * @return The created user object, never <code>null</code>.
    * 
    * @throws PSLoaderException if an error occurs.
    */
   private PSTreeObject createFieldTransObject(
      PSFieldTransformationDef transDef)
      throws PSLoaderException
   {
      PSPluginFactory plgFact = PSPluginFactory.getInstance();
      IPSFieldTransformer transformer =
            plgFact.newFieldTransformer(transDef);
      PSConfigPanel panel = null;
      
      if (transformer instanceof IPSUIPlugin)
      {
         panel =
               ((IPSUIPlugin)transformer).getConfigurationUI();
         panel.load(transDef.toXml(
               PSXmlDocumentBuilder.createXmlDocument()));
      }

      PSTreeObject node = new PSTreeObject(transDef.getName(), transDef, panel);
      
      return node;
   }
   
   /**
    * Loads field transformers.
    *
    * @param itr, an iteration over {@link PSTransformationDef}, never <code>
    * null</code>, may be empty.
    *
    * @param pane, {@link PSMultiSelectionEditorPanel} holding the field
    * transformers, assumed to be not <code>null</code>.
    *
    * @param edit, if <code>true</code> then a descriptor has been loaded for
    * editing else a new descriptor is to be created.
    *
    * @param targetFields list of target fields displayed. Never <code>null
    * </code>, may be empty.
    *
    * @throws PSLoaderException, thrown if there is an exception associated with
    * loader application.
    */
   private void loadFieldTransformers(Iterator itr, PSMultiSelectionEditorPanel
      pane, boolean edit) throws PSLoaderException
   {
      PSFieldTransformationDef transDef = null;
      PSPluginFactory plgFact = PSPluginFactory.getInstance();
      DefaultListModel dlm = (DefaultListModel)pane.m_availableList.getModel();
      DefaultListModel ulm = (DefaultListModel)pane.m_editedList.getModel();
      PSTreeObject node = null;
      while (itr.hasNext())
      {
         transDef = (PSFieldTransformationDef)itr.next();
         try
         {
            node = createFieldTransObject(transDef);
         }
         catch (PSLoaderException e) // log and ignore the BAD transformer
         {
            Logger.getLogger(getClass()).error(e.getLocalizedMessage());
            
            String[] args = {transDef.getName()};
            PSLogMessage   msg = new PSLogMessage(
               IPSLogCodes.FAILED_LOAD_FIELD_TRANSFORMER_DEF,
               args, null, PSLogMessage.LEVEL_ERROR);
            Logger.getLogger(getClass()).error(msg);

            continue;            
         }
         
         if (edit)
            ulm.addElement(node);
         else
            dlm.addElement(node);
      }
   }

   /**
    * Represented by the bottom list in this panel. Initialized in {@link
    * #init()}, never <code>null</code> or modified after that.
    */
   private JList m_editedList;

   /**
    * Represented by the top list in this panel. Initialized in {@link
    * #init()}, never <code>null</code> or modified after that.
    */
   private JList m_availableList;

   /**
    * Initialized in the {@link #addActionListener(ActionListener)}. Never
    * <code>null</code> or modified after that.
    */
   private ActionListener m_actionListener;

   /**
    * Button for moving items up in the <code>m_editedList</code> list.
    * Initialized in the {@link #init()}, never <code>null</code> or modified.
    * It's state is changed from enabled or disabled in {
    * @link #setButtonsState()}.
    */
   private JButton m_upBtn;

   /**
    * Button for moving items down in the <code>m_editedList</code> list.
    * Initialized in the {@link #init()}, never <code>null</code> or modified.
    * It's state is changed from enabled or disabled in {@link
    * #setButtonsState()}.
    */
   private JButton m_dwnBtn;

   /**
    * The button for removing the selected items in the <code>m_editedList</code>.
    * Initialized in the {@link #init()}, never <code>null</code> or modified.
    * It's state is changed from enabled or disabled in {@link
    * #setButtonsState()}.
    */
   private JButton m_deleteBtn;

   /**
    * Button for moving items from <code>m_availableList</code> to <code>
    * m_editedList</code> list. Initialized in the {@link #init()}, never <code>
    * null</code> or modified. It's state is changed from enabled or disabled in
    * {@link #setButtonsState()}.
    */
   UTFixedButton m_addBtn;

   /**
    * Initialized in {@link# init()}, holds the page extractor's node name.
    */
   private static String ms_pageExt;

   /**
    * Initialized in {@link# init()}, holds the image extractor's node name.
    */
   private static String ms_imgExt;

   /**
    * Initialized in {@link# init()}, holds the file extractor's node name.
    */
   private static String ms_fileExt;

   /**
    * Resource bundle for this class. Initialized in the constructor.
    * It's not modified after that. Never <code>null</code>.
    */
   private static ResourceBundle ms_res;
}
