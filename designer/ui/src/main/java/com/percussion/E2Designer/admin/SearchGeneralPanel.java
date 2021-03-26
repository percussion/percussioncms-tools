/* *****************************************************************************
 *
 * [ SearchGeneralPanel.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *******************************************************************************/

package com.percussion.E2Designer.admin;

import com.percussion.design.objectstore.PSSearchConfig;
import com.percussion.validation.StringConstraint;
import com.percussion.validation.ValidationConstraint;
import com.percussion.validation.ValidationFramework;
import com.percussion.UTComponents.UTFixedHeightTextField;
import com.percussion.guitools.ErrorDialogs;
import com.percussion.guitools.PSPropertyPanel;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * This class implements the UI to edit the PSSearchConfig object (from the
 * PSServerConfiguration) as well as provide some actions for managing the 
 * search engine and indexing. 
 * <p>Data is transferred from the object passed in the ctor to the UI controls
 * and back to the object supplied in the ctor when <code>
 * ITabDataHelper.saveTabData()</code> is called.
 */
public class SearchGeneralPanel extends JPanel implements ITabDataHelper
{
   /**
    * Construct the GUI elements and initialize them with actual data.
    *
    * @param parent The frame within which this panel will be displayed. Used 
    * by dialogs so they don't become hidden from the end-user.
    * 
    * @param config The data object that contains the data we are presenting 
    * in this panel.
    */
   public SearchGeneralPanel(Frame parent, ServerConfiguration config)
   {
      m_parent = parent;
      m_serverConfig = config;

      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
      setBorder(new EmptyBorder(10, 10, 10, 10));

      add(createSetupPanel());
      
      transferData(false);
   }

   /* Save - want to add when we have time. This functionality is available
    * thru the remote console, but this would make it easier to use. See
    * above too.
    */
   /**  
    * Builds the panel that contains the controls for editing search engine
    * connectivity info.
    * 
    * @return A panel as a vertical <code>Box</code> with glue at the top.
    * Never <code>null</code>.
   private Component createControlButtonsPanel()
   {
      Box panel = new Box(BoxLayout.Y_AXIS);
      panel.add(Box.createVerticalGlue());
      // pick a size big enough for both buttons
      int width = 160;
      UTFixedButton button = new UTFixedButton(
            m_res.getString("search.restart"), width, 
            UTFixedButton.STANDARD_BUTTON_SIZE.height);
      button.setAlignmentX(RIGHT_ALIGNMENT);
      button.setAlignmentY(BOTTOM_ALIGNMENT);
      button.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            System.out.println("Restart button pressed");
         }
      });
      panel.add(button);
      //separation between buttons, chosen arbitrarily to look good
      panel.add(Box.createVerticalStrut(5));
      
      button = new UTFixedButton(
            m_res.getString("search.indexmgmt"), width, 
            UTFixedButton.STANDARD_BUTTON_SIZE.height);
      button.setAlignmentX(RIGHT_ALIGNMENT);
      button.setAlignmentY(BOTTOM_ALIGNMENT);
      button.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            System.out.println("Index mgmt button pressed");
         }
      });
      panel.add(button);
      return panel;
   }
    */

   /**
    * Builds the panel that contains the controls for editing lucene extensions
    * provided by a {@link LuceneExtensionPanel}.
    * 
    * @param title The title to be used for the returned panel, assumed not
    * <code>null<code>.
    * @param p The {@link LuceneExtensionPanel} object which will be packaged
    * with an appropriate header, assumed not <code>null<code>.
    * 
    * @return A panel with an appropriately aligned header label and an inner-
    * panel for editing lucene extensions. Never <code>null</code>.
    */
   private Component createExtensionsPanel(String title, LuceneExtensionPanel p)
   {
      Box panel = new Box(BoxLayout.Y_AXIS);
      Box header = new Box(BoxLayout.X_AXIS);
      JLabel label = new JLabel(title);
      label.setAlignmentX(LEFT_ALIGNMENT);
      label.setLabelFor(p);
      header.add(label);
      header.add(Box.createHorizontalGlue());
      panel.add(header);
      panel.add(p);
            
      return panel;
   }
   
   /**
    * Creates a border that simulates a group box in windows.
    * <p>Centralize creation for consistent look.
    * 
    * @param titleKey The key for the name displayed in the box. May be <code>
    * null</code> or empty.
    * 
    * @return The generated border, never <code>null</code>.
    */
   private Border createGroupBorder(String titleKey)
   {
      String title;
      if (null == titleKey || titleKey.trim().length() == 0)
         title = "";
      else
         title = m_res.getString(titleKey);
      return new CompoundBorder(
            new TitledBorder(new EtchedBorder(), title),
            //leave some space between the group box and the controls
            new EmptyBorder(0, 3, 3, 3));
             
   }

   /**
    * Builds the panel that contains the controls for editing search engine
    * setup and configuration info.
    * 
    * @return The panel with all controls properly arranged. Never <code>
    * null</code>.
    */
   private Component createSetupPanel()
   {
      JPanel setupPanel = new JPanel();
      setupPanel.setLayout(new GridLayout(3, 1));
      setupPanel.setBorder(createGroupBorder("search.setup"));
      
      PSPropertyPanel panel = new PSPropertyPanel();      
      panel.addPropertyRow(m_res.getString("search.enabled"), m_enabled);
      m_enabled.addItemListener(new ItemListener() {

         public void itemStateChanged(ItemEvent e)
         {
            if (e.getStateChange() == ItemEvent.DESELECTED)
            {
               String msg = m_res.getString("search.disable.msg");
               msg = ErrorDialogs.cropErrorMessage(msg);
               JOptionPane.showMessageDialog(m_parent, msg, 
                  m_res.getString("search.disable.title"), 
                  JOptionPane.WARNING_MESSAGE);
            }
         }});
      panel.addPropertyRow(m_res.getString("search.indexdir"), m_indexDir);
      panel.addPropertyRow(m_res.getString("search.synonymExpansion"),
            m_synonymExpansion);
            
      m_analyzersPanel = new LuceneExtensionPanel(m_serverConfig.
            getServerConfiguration().getSearchConfig(),
            LuceneExtensionPanel.ANALYZER_INTERFACE_NAME,
            m_res.getString("search.itemlocale"),
            m_res.getString("search.analyzer"));
            
      m_extractorsPanel = new LuceneExtensionPanel(m_serverConfig.
            getServerConfiguration().getSearchConfig(),
            LuceneExtensionPanel.EXTRACTOR_INTERFACE_NAME,
            m_res.getString("search.mimetype"),
            m_res.getString("search.extractor"));
            
      setupPanel.add(panel);
      setupPanel.add(createExtensionsPanel(m_res.getString("search.analyzers"),
            m_analyzersPanel));
      setupPanel.add(createExtensionsPanel(m_res.getString("search.extractors"),
            m_extractorsPanel));
            
      return setupPanel;
   }

   /** 
    * Centralizes the transfer of data between the dialog controls and the
    * object being edited. Assumes that data validation has already been
    * performed. The data is moved to/from the <code>m_searchConfig</code> 
    * object.
    * 
    * @param toObject Supply <code>true</code> if the data should be 
    * transferred from the dialog to the object being edited, <code>false
    * </code> to transfer from the object to the dialog controls.
    * 
    * @return Always <code>false</code> if <code>toObject</code> is <code>
    * false</code>. If <code>toObject</code> is <code>true</code>, <code>
    * false</code> is returned if no changes have been made to the config,
    * otherwise, <code>true</code> is returned.
    */
   @SuppressWarnings("unchecked")
   private boolean transferData(boolean toObject)
   {
      boolean changed = false;
      /* We have to get the config each time because the underlying 
       * PSServerConfiguration can change w/in our m_serverConfig object.
       */
      PSSearchConfig sc = 
            m_serverConfig.getServerConfiguration().getSearchConfig();
      Map scAnalyzers = sc.getAnalyzers();
      Map scConverters = sc.getTextConverters();
      Map pAnalyzers = m_analyzersPanel.getExtensions();
      Map pConverters = m_extractorsPanel.getExtensions();
      
      if (toObject)
      {
         if (sc.isFtsEnabled() != m_enabled.isSelected())
         {
            changed = true;
            sc.setFtsEnabled(m_enabled.isSelected());
         }
         
         String normalizedDir = m_indexDir.getText().trim();
         normalizedDir = normalizedDir.replace('\\', '/');
         //reset the text in case any slashes were normalized
         m_indexDir.setText(normalizedDir);
         
         String synonymExp = m_synonymExpansion.isSelected() ? "yes" : "no";
         
         Map<String, String> propSet = new HashMap<String, String>();
         propSet.put(PSSearchConfig.INDEXROOTDIR_KEY, normalizedDir);
         propSet.put(PSSearchConfig.SYNONYM_EXPANSION, synonymExp);
         
         if (!propSet.equals(sc.getCustomProps()))
         {
            changed = true;
            sc.removeAllCustomProps();
            Iterator<String> props = propSet.keySet().iterator();
            while (props.hasNext())
            {
               String prop = props.next();
               sc.addCustomProp(prop, propSet.get(prop));
            }
         }
       
         if (!scAnalyzers.equals(pAnalyzers))
         {
            changed = true;
            scAnalyzers.clear();
            scAnalyzers.putAll(pAnalyzers);
         }
         
         if (!scConverters.equals(pConverters))
         {
            changed = true;
            scConverters.clear();
            scConverters.putAll(pConverters);
         }
      }
      else
      {
         m_enabled.setSelected(sc.isFtsEnabled());
         
         String configDir = findCustomProp(sc, PSSearchConfig.INDEXROOTDIR_KEY,
               "sys_search/lucene");
         m_indexDir.setText(configDir);
         
         m_synonymExpansion.setSelected(sc.isSynonymExpansionRequired());
         
         pAnalyzers.clear();
         pAnalyzers.putAll(scAnalyzers);
         
         pConverters.clear();
         pConverters.putAll(scConverters);
      }
      return changed;
   }

   // see ITabDataHelper
   public boolean saveTabData()
   {
      //save analyzers, extractors data first
      m_analyzersPanel.saveTabData();
      m_extractorsPanel.saveTabData();
      
      return transferData(true);
   }

   /**
    * Validation rules (only fields that are validated are shown in the table):
    * <table>
    * <th>Field</th><th>Valid values</th>
    * <tr>
    *    <td>configuration directory</td>
    *    <td>Cannot be empty.</td>
    * </tr>
    * <tr>
    *    <td>custom properties</td>
    *    <td>The names cannot be <code>null</code>, contain spaces or appear
    * more than once.</td>
    * </tr>
    * </table>
    */
   public boolean validateTabData()
   {
      Component[] comps = 
      {
         m_indexDir
      };
      ValidationConstraint[] constraints =
      {
         /*chars not allowed in windows filenames, some of these are allowed
          * in unix, but I think we are OK not allowing them
          */ 
         new StringConstraint("*?\"<>|")
      };

      ValidationFramework vf = new ValidationFramework(m_parent, comps, 
            constraints);
            
      boolean valid = true;
      if (!vf.checkValidity())
         valid = false;
      
      return valid;
   }

   /**
    * Used to retrieve a custom property from the search configuration.
    * 
    * @param sc the search config object, assumed not <code>null</code>.
    * @param key the customer property key to look for, assumed not
    * <code>null</code>.
    * @param defaultVal the value to be returned if the key is not found,
    * assumed not <code>null</code>.
    * 
    * @return the search configuration custom property value for the specified
    * key, or the default value if the key was not found.
    */
   private String findCustomProp(PSSearchConfig sc, String key,
         String defaultVal)
   {
      Map customProps = sc.getCustomProps();

      String val = (String) customProps.get(key);
      //set the default if not found
      if (null == val || val.length() == 0)
         val = defaultVal;
      
      return val;
   }
   
   /**
    * 
    * <p>Must be first of member fields w/in file. Other member inits use it.
    */
   private static ResourceBundle m_res = PSServerAdminApplet.getResources();

   /**
    * See {@link #SearchGeneralPanel(Frame, ServerConfiguration) ctor} for a 
    * description. Set in ctor then never changed or modified.
    */
   private Frame m_parent = null;
   
   /**
    * See {@link #SearchGeneralPanel(Frame, ServerConfiguration) ctor} for a 
    * description. Set in ctor then never <code>null</code>. The object is 
    * modified if {@link #saveTabData()} is called.
    */
   private ServerConfiguration m_serverConfig = null;

   /**
    * The editing control for the directory name that contains the 
    * index data for the search engine. Never <code>null</code>.
    * <p>See {@link #validateTabData()} for valid values.
    */
   private UTFixedHeightTextField m_indexDir = new UTFixedHeightTextField();

   /**
    * Panel containing controls used to edit lucene analyzers.  Initialized in
    * {@link #createSetupPanel()}, never <code>null</code> after that.
    */
   private LuceneExtensionPanel m_analyzersPanel = null;
   
   /**
    * Panel containing controls used to edit lucene text converters
    * (extractors).  Initialized in {@link #createSetupPanel()}, never
    * <code>null</code> after that.
    */
   private LuceneExtensionPanel m_extractorsPanel = null;
      
   /**
    * The editing control for the enabled flag. Never <code>null</code>.
    * <p>There is no text on this checkbox. 
    */
   private JCheckBox m_enabled = new JCheckBox();
   
   /**
    * The editing control for the synonym expansion property. Never
    * <code>null</code>.
    * <p>There is no text on this checkbox. 
    */
   private JCheckBox m_synonymExpansion = new JCheckBox();
}
