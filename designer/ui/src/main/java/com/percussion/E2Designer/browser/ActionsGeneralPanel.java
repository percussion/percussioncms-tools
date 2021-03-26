/*******************************************************************************
 *
 * [ ActionsGeneralPanel.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer.browser;


import com.percussion.E2Designer.PSDlgUtil;
import com.percussion.cms.objectstore.PSAction;
import com.percussion.cms.objectstore.PSActionProperties;
import com.percussion.cms.objectstore.PSDbComponentCollection;
import com.percussion.UTComponents.UTFixedHeightTextField;
import com.percussion.guitools.PSPropertyPanel;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Vector;

/**
 * General panel
 */
public class ActionsGeneralPanel extends JPanel
{
   /**
    * Constructs the panel.
    */
   public ActionsGeneralPanel(List list, PSActionTabbedPanel tabPanel)
   {
      m_actionDbCollect = (PSDbComponentCollection)list.get(0);
      m_tabbedPanel = tabPanel;
      init();
   }

   /**
    * Initializes the panel
    */
   private void init()
   {
      if (null == ms_res)
         ms_res = ResourceBundle.getBundle(getClass().getName() + "Resources",
            Locale.getDefault());
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
      JPanel mainPropertyPanel = new JPanel();
      mainPropertyPanel.setLayout(new BoxLayout(mainPropertyPanel,
         BoxLayout.Y_AXIS));

      PSPropertyPanel propertyPanel = new PSPropertyPanel();
      propertyPanel.setAlignmentX(PSPropertyPanel.RIGHT_ALIGNMENT);
      mainPropertyPanel.add(propertyPanel);
      
      char mn = ms_res.getString("textfield.label.mn").charAt(0);
      m_labelText = new UTFixedHeightTextField();
      propertyPanel.addPropertyRow(ms_res.getString("textfield.label"),
                       new JComponent[] { m_labelText }, m_labelText, mn, null);      

      mn = ms_res.getString("textfield.name.mn").charAt(0);
      m_nameText = new UTFixedHeightTextField();
      propertyPanel.addPropertyRow(ms_res.getString("textfield.name"),
                       new JComponent[] { m_nameText }, m_nameText, mn, null);
      //Sort Rank label, textfield
      JPanel sortRankPanel = new JPanel();
      sortRankPanel.setLayout(new BoxLayout(sortRankPanel, BoxLayout.X_AXIS));

      m_sortRankText = new UTFixedHeightTextField();
      sortRankPanel.add(m_sortRankText);

      sortRankPanel.add(Box.createRigidArea(new Dimension(160, 0)));
      sortRankPanel.add(Box.createHorizontalGlue());
   
      mn = ms_res.getString("textfield.sortrank.mn").charAt(0);
      propertyPanel.addPropertyRow(ms_res.getString("textfield.sortrank"),
                        new JComponent[] { sortRankPanel }, sortRankPanel, 
                        mn, null);
      
      sortRankPanel.setPreferredSize(new Dimension(0, 10));
      sortRankPanel.setMinimumSize(new Dimension(0, 10));
      sortRankPanel.setMaximumSize(new Dimension(50, 20));

      //Accelerator Key, Mnemonic Key
      JPanel accMnemonPanel = new JPanel();
      accMnemonPanel.setLayout(new BoxLayout(accMnemonPanel, BoxLayout.X_AXIS));

      m_accText = new UTFixedHeightTextField();
      accMnemonPanel.add(m_accText);
      accMnemonPanel.add(Box.createRigidArea(new Dimension(20, 24)));
      m_mnemonText = new UTFixedHeightTextField();
      PSPropertyPanel mnemonicPanel  = new PSPropertyPanel();
      
      mn = ms_res.getString("textfield.mnkey.mn").charAt(0);
      mnemonicPanel.addPropertyRow(ms_res.getString("textfield.mnkey"),
                     new JComponent[] { m_mnemonText }, m_mnemonText, mn, null);
      accMnemonPanel.add(mnemonicPanel);
      JComponent[] accMnemonComp = {accMnemonPanel};
      
      mn = ms_res.getString("textfield.acckey.mn").charAt(0);
      propertyPanel.addPropertyRow(ms_res.getString("textfield.acckey"),
                     accMnemonComp, m_accText, mn, null);

      //Tooltip
      m_tooltipText = new UTFixedHeightTextField();
      mn = ms_res.getString("textfield.tt.mn").charAt(0);
      propertyPanel.addPropertyRow(ms_res.getString("textfield.tt"), 
                   new JComponent[] { m_tooltipText }, m_tooltipText, mn, null);

      //Icon path
      m_iconpathText = new UTFixedHeightTextField();
      mn = ms_res.getString("textfield.iconpath.mn").charAt(0);
      propertyPanel.addPropertyRow(ms_res.getString("textfield.iconpath"),
                   new JComponent[] { m_iconpathText }, m_iconpathText, mn, null);

      //Menu Type Panel
      ButtonGroup btnGrp = new ButtonGroup();
      JPanel menuTypePanel = new JPanel();
      menuTypePanel.setLayout(new BoxLayout(menuTypePanel, BoxLayout.X_AXIS));
      Border b = BorderFactory.createTitledBorder(ms_res.getString(
         "border.menutype"));
      menuTypePanel.setBorder(b);
      m_menu = new JRadioButton(ms_res.getString("radio.menu"));
      m_menu.setMnemonic(ms_res.getString("radio.menu.mn").charAt(0));
      m_cascadingMenu = new JRadioButton(ms_res.getString("radio.cascading"));
      m_cascadingMenu.setMnemonic(ms_res.getString("radio.cascading.mn").charAt(0));
      m_cascadingMenu.addItemListener(new HideMenuOptions());
      m_dynamicMenu = new JRadioButton(ms_res.getString("radio.dynamic"));
      m_dynamicMenu.setMnemonic(ms_res.getString("radio.dynamic.mn").charAt(0));
      m_dynamicMenu.addItemListener(new HideMenuOptions());


      btnGrp.add(m_menu);
      btnGrp.add(m_cascadingMenu);
      btnGrp.add(m_dynamicMenu);
      menuTypePanel.add(m_menu);
      menuTypePanel.add(Box.createRigidArea(new Dimension(40, 0)));
      menuTypePanel.add(Box.createHorizontalGlue());
      menuTypePanel.add(m_cascadingMenu);
      menuTypePanel.add(Box.createRigidArea(new Dimension(40, 0)));
      menuTypePanel.add(Box.createHorizontalGlue());
      menuTypePanel.add(m_dynamicMenu);

      //Menu Options Panel
      JPanel m_menuOptionsPanel = new JPanel();
      m_menuOptionsPanel.setLayout(new BoxLayout(m_menuOptionsPanel,
         BoxLayout.X_AXIS));

      b = BorderFactory.createTitledBorder(ms_res.getString(
         "border.menuoptions"));
      m_menuOptionsPanel.setBorder(b);
      JPanel chkBoxPanel = new JPanel();
      chkBoxPanel.setLayout(new BoxLayout(chkBoxPanel, BoxLayout.Y_AXIS));
      m_launchNewWnd = new JCheckBox(ms_res.getString("chkbox.newwnd"));
      m_launchNewWnd.setMnemonic(
                           ms_res.getString("chkbox.newwnd.mn").charAt(0));
      m_multiSelect = new JCheckBox(ms_res.getString("chkbox.multiselect"));
      m_multiSelect.setMnemonic(
                           ms_res.getString("chkbox.multiselect.mn").charAt(0));
      m_pasteMenu = new JCheckBox(ms_res.getString("chkbox.pastemenu"));
      m_pasteMenu.setMnemonic(
            ms_res.getString("chkbox.pastemenu.mn").charAt(0));

      chkBoxPanel.add(m_launchNewWnd);
      chkBoxPanel.add(m_multiSelect);

      PSPropertyPanel comboPanel = new PSPropertyPanel();

      Vector<String> comboVec = new Vector<String>();
      comboVec.add(ms_res.getString("combo.none"));
      comboVec.add(ms_res.getString("combo.selected"));
      comboVec.add(ms_res.getString("combo.parent"));
      comboVec.add(ms_res.getString("combo.root"));
      m_refreshHint = new JComboBox(comboVec);
      mn = ms_res.getString("combobox.hint.mn").charAt(0);
      comboPanel.addPropertyRow(ms_res.getString("combobox.hint"), 
               new JComponent[] { m_refreshHint }, m_refreshHint, mn, null);

      chkBoxPanel.setAlignmentY(JPanel.TOP_ALIGNMENT);
      comboPanel.setAlignmentY(JPanel.TOP_ALIGNMENT);

      m_menuOptionsPanel.add(chkBoxPanel);
      m_menuOptionsPanel.add(Box.createRigidArea(new Dimension(40, 0)));
      m_menuOptionsPanel.add(Box.createHorizontalGlue());
      m_menuOptionsPanel.add(comboPanel);

      //Description panel
      JPanel descPanel = new JPanel();
      descPanel.setLayout(new BorderLayout());

      b = BorderFactory.createTitledBorder(ms_res.getString("border.desc"));
      descPanel.setBorder(b);

      m_descArea = new JTextArea();
      m_descArea.setLineWrap(true);
      m_descArea.setWrapStyleWord(true);
      m_descArea.setEditable(true);
      JScrollPane areaScrollPane = new JScrollPane(m_descArea);
      areaScrollPane.setPreferredSize(new Dimension(100, 100));
      descPanel.add(areaScrollPane, BorderLayout.CENTER);

      mainPropertyPanel.setAlignmentX(JPanel.CENTER_ALIGNMENT);
      menuTypePanel.setAlignmentX(JPanel.CENTER_ALIGNMENT);
      add(mainPropertyPanel);
      add(menuTypePanel);
      add(m_menuOptionsPanel);
      add(descPanel);
      EmptyBorder emptyBorder = new EmptyBorder(10, 10, 10, 10);
      setBorder(emptyBorder);
   }

   public boolean update(Object data, boolean isLoad)
   {
      PSAction action = null;
      if (data instanceof PSAction)
      {
         action = (PSAction)data;
         m_loadedAction = action;

         PSActionProperties props = m_loadedAction.getProperties();
         if (isLoad)
         {
            m_nameText.setText(m_loadedAction.getName());

            // If persisted then set internal name disabled
           refreshInternalName(action);

            m_labelText.setText(m_loadedAction.getLabel());
            m_descArea.setText(m_loadedAction.getDescription());
            m_sortRankText.setText(
               Integer.toString(m_loadedAction.getSortRank()));

            m_mnemonText.setText(props.getProperty(PSAction.PROP_MNEM_KEY));
            m_accText.setText(props.getProperty(PSAction.PROP_ACCEL_KEY));
            m_tooltipText.setText(props.getProperty(PSAction.PROP_SHORT_DESC));
            m_iconpathText.setText(props.getProperty(PSAction.PROP_SMALL_ICON));

            if (m_loadedAction.isMenuItem())
               m_menu.setSelected(true);
            else
            {
               if (m_loadedAction.getURL().trim().length() == 0)
                  m_cascadingMenu.setSelected(true);
               else
                  m_dynamicMenu.setSelected(true);
            }

            String temp = props.getProperty(PSAction.PROP_LAUNCH_NEW_WND);
            if (!isNullOrEmpty(temp) && temp.equalsIgnoreCase(PSAction.NO))
               m_launchNewWnd.setSelected(false);
            else
               m_launchNewWnd.setSelected(true);

            temp = props.getProperty(PSAction.PROP_MUTLI_SELECT);
            if(!isNullOrEmpty(temp) && temp.equalsIgnoreCase(PSAction.YES))
               m_multiSelect.setSelected(true);
            else
               m_multiSelect.setSelected(false);
            
            temp = props.getProperty(PSAction.PROP_REFRESH_HINT);
            if (isNullOrEmpty(temp))
               m_refreshHint.setSelectedIndex(0);
            else
               m_refreshHint.setSelectedItem(temp);
         }
         else
         {
            if (!validateData())
               return false;

            m_loadedAction.setLabel(m_labelText.getText());
            m_loadedAction.setName(m_nameText.getText());
            m_loadedAction.setDescription(m_descArea.getText());
            m_loadedAction.setSortRank(
               Integer.parseInt(m_sortRankText.getText()));

            props.setProperty(PSAction.PROP_MNEM_KEY, m_mnemonText.getText());
            props.setProperty(PSAction.PROP_ACCEL_KEY, m_accText.getText());
            props.setProperty(PSAction.PROP_SHORT_DESC, m_tooltipText.getText());
            props.setProperty(PSAction.PROP_SMALL_ICON, m_iconpathText.getText());

            if (m_menu.isSelected())
               m_loadedAction.setMenuType(PSAction.TYPE_MENUITEM);
            else if (m_cascadingMenu.isSelected())
               m_loadedAction.setMenuType(PSAction.TYPE_MENU);
            else if (m_dynamicMenu.isSelected())
               m_loadedAction.setMenuType(PSAction.TYPE_MENU);

            if (m_launchNewWnd.isSelected())
               props.setProperty(PSAction.PROP_LAUNCH_NEW_WND, PSAction.YES);
            else
               props.setProperty(PSAction.PROP_LAUNCH_NEW_WND, PSAction.NO);

            if (m_multiSelect.isSelected())
               props.setProperty(PSAction.PROP_MUTLI_SELECT, PSAction.YES);
            else
               props.setProperty(PSAction.PROP_MUTLI_SELECT, PSAction.NO);

            props.setProperty(PSAction.PROP_REFRESH_HINT,
               (String) m_refreshHint.getSelectedItem());
         }
      }

      return true;
   }

   public boolean isDynamicMenuSelected()
   {
      return m_dynamicMenu.isSelected();
   }

   public boolean isCascadingMenuSelected()
   {
      return m_cascadingMenu.isSelected();
   }

   /**
    * Refreshes internal name field so it can disable itself
    * if necessary.
    * @param data the action object. May be <code>null</code>.
    */
   public void refreshInternalName(Object data)
   {
      PSAction action = null;
      if (null != data && data instanceof PSAction)
      {
         action = (PSAction)data;

         // If persisted then set internal name disabled
         m_nameText.setEnabled(!action.isPersisted());

      }

   }

   /**
    * Scans the supplied string for any whitespace characters.
    *
    * @param text May be <code>null</code> or empty.
    *
    * @return <code>true</code> if any character in text is whitespace,
    *    according to Character.isWhitespace(), <code>false</code> otherwise.
    */
   private static boolean containsWhitespace(String text)
   {
      if (text == null)
         return false;

      for (int i=0; i < text.length(); i++)
      {
         if (Character.isWhitespace(text.charAt(i)))
            return true;
      }
      return false;
   }

   /**
    * Is the name not unique in the component collection? This checks the
    * <code>PSDbComponentCollection</code> list for the existance of the
    * specified menuName.
    *
    * @param menuName the string for which to check in the
    * <code>PSDbComponentCollection</code>.  Assumed not <code>null</code> or
    * empty.
    * @return <code>true</code> if it finds the name in the collection,  if it
    * does not find the name in the colleciton it returns <code>false</code>
    * and unique.
    */
   private boolean isNotUnique(String menuName)
   {
      Iterator itr = m_actionDbCollect.iterator();
      PSAction action = null;
      while (itr.hasNext())
      {
         action = (PSAction)itr.next();
         if(menuName.equalsIgnoreCase(action.getName()) &&
            action != m_loadedAction)
            return true;
      }
      return false;
   }

   public String getLabelName()
   {
      validateData();
      return m_labelText.getText();
   }

   private boolean isNullOrEmpty(String str)
   {
      return (str == null || str.trim().length() == 0);
   }

   public boolean validateData()
   {
      String temp = "";
      temp = m_nameText.getText();
      boolean failed = isNullOrEmpty(temp);
      if (failed)
      {
         PSDlgUtil.showErrorDialog(ms_res.getString("err.internalnamemissing.msg"),
               ms_res.getString("err.title"));
         return !failed;
      }

      failed = containsWhitespace(temp);
      if (failed)
      {
         PSDlgUtil.showErrorDialog(ms_res.getString("err.internalnamees.msg"),
               ms_res.getString("err.title"));
         return !failed;
      }

      failed = isNotUnique(temp);
      if (failed)
      {
         PSDlgUtil.showErrorDialog(ms_res.getString("err.internalnamenonunique.msg"), ms_res.getString("err.title"));
         return !failed;
      }

      temp = m_labelText.getText();
      failed = isNullOrEmpty(temp);
      if (failed)
      {
         PSDlgUtil.showErrorDialog(ms_res.getString("err.labelmissing.msg"),
               ms_res.getString("err.title"));
         return !failed;
      }

      temp = m_sortRankText.getText();
      try
      {
         Integer.parseInt(temp);
      }
      catch(NumberFormatException e)
      {
         PSDlgUtil.showErrorDialog(ms_res.getString("err.sortrank.msg"),
               ms_res.getString("err.title"));
         return false;
      }
      return m_tabbedPanel.validateDataUrl();
   }


   public void addItemListener(ItemListener listener)
   {
      m_cascadingMenu.addItemListener(listener);
   }


   public class HideMenuOptions implements ItemListener
   {
      public void itemStateChanged(ItemEvent e)
      {
          boolean isSelectable = (!(e.getStateChange() == ItemEvent.SELECTED));
          m_launchNewWnd.setEnabled(isSelectable);
          m_multiSelect.setEnabled(true); //makes it possible for a user to change this one setting
          m_pasteMenu.setEnabled(isSelectable);
          m_refreshHint.setEnabled(isSelectable);
      }
   }

   private PSActionTabbedPanel m_tabbedPanel;
   private JTextArea m_descArea;
   private PSDbComponentCollection m_actionDbCollect;

   public static final String DYNAMIC_CMD = "dynamic";


   /**
    * The internal name of the current action menu. This must be unique across
    * all defined action menus. Initalized in {@link#init()}, never <code>null
    * </code> or modified after that.
    */
   private UTFixedHeightTextField m_nameText;

   /**
    * The dispaly name for this action menu. This cannot be empty but does not
    * have to be unique. Initalized in {@link#init()}, never <code>null
    * </code> or modified after that.
    */
   private UTFixedHeightTextField m_labelText;

   /**
    * This specifies the sort rank of this menu, the same sort rank is used in
    * all Modes and UI Contexts. If multiple menus have the same sort rank, they
    * are sorted alphabetically. Initalized in {@link#init()}, never <code>null
    * </code> or modified after that.
    */
   private UTFixedHeightTextField m_sortRankText;

   /**
    * The accelerator key for this menu action. Initalized in {@link#init()},
    * never <code>null</code> or modified after that.
    */
   private UTFixedHeightTextField m_accText;

   /**
    * The mnemonic key for this menu action. Initalized in {@link#init()},
    * never <code>null</code> or modified after that.
    */
   private UTFixedHeightTextField m_mnemonText;

   /**
    * A brief menu description shown as tootip information. Initalized in
    * {@link#init()}, never <code>null</code> or modified after that.
    */
   private UTFixedHeightTextField m_tooltipText;

   /**
    * A relative path to the Rhythmyx resource that will be shown as menu icon.
    * Initalized in {@link#init()}, never <code>null</code> or modified after
    * that.
    */
   private UTFixedHeightTextField m_iconpathText;


   private JRadioButton m_menu;

   /**
    * Specifies a placeholder for a cascading menu. The command tab will be
    * hidden since this does not perform a real action. Also all menu options
    * will be disabled. Initalized in {@link#init()}, never <code>null</code>
    * or modified after that.
    */
   private JRadioButton m_cascadingMenu;

   /**
    * Specifies a dynamic menu. The command specified will return alist of
    * actions to be added to the current menu. If cascading is checked, the
    * dynamic menu will be cascaded, otherwise it will expand into the current
    * menu level. Initalized in {@link#init()}, never <code>null</code> or
    * modified after that.
    */
   private JRadioButton m_dynamicMenu;

   /**
    * This option tells a client whether or not to launch new window. If this is
    * selected, Supports Multi Select will be unchecked and disabled. Initalized
    *  in {@link#init()}, never <code>null</code> or modified after that.
    */
   private JCheckBox m_launchNewWnd;

   /**
    * This option tells a client that the attached command supports batch
    * processing. Initalized in {@link#init()}, never <code>null</code> or
    * modified after that.
    */
   private JCheckBox m_multiSelect;

   /**
    * This option tells a client that this menu option can be part of its paste
    * menu.Initalized in {@link#init()}, never <code>null</code> or modified
    * after that.
    */
   private JCheckBox m_pasteMenu;

   /**
    * Refresh hint tells the client what needs to be refreshed after the action
    * is performed. Initalized in {@link#init()}, never <code>null</code> or
    * modified after that.
    */
   private JComboBox m_refreshHint;

   /**
    * The PSAction from which this panel gets its data.  This is
    * instantiated each time {@link #update(Object, boolean)
    * update(Object data, boolean isLoad)}
    * is called.  Once that method is called this is never <code>null</code>.
    */
   private PSAction m_loadedAction = null;

   /**
    * Resource bundle for this class. Initialized in {@link init()}.
    * It's not modified after that. Never <code>null</code>, equivalent to a
    * variable declared final.
    */
   private static ResourceBundle ms_res;
}
