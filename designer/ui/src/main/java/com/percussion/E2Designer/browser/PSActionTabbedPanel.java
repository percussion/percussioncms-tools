/*******************************************************************************
 *
 * [ PSActionTabbedPanel.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer.browser;

import com.percussion.E2Designer.PSDlgUtil;
import com.percussion.cms.objectstore.PSAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * The command panel lets the designer specify the actual command that is
 * executed with this menu.
 */
public class PSActionTabbedPanel extends JPanel implements ItemListener
{
   /**
    * Constructs the panel.
    */
   public PSActionTabbedPanel(List list)
   {
      m_list = list;
      init();
   }

   /**
    * Initializes the panel.
    */
   private void init()
   {
      if (null == ms_res)
         ms_res = ResourceBundle.getBundle(getClass().getName() + "Resources",
            Locale.getDefault());
      m_tabPane = new JTabbedPane();
      //general panel
      m_agpanel = new ActionsGeneralPanel(m_list, this);
      m_agpanel.addItemListener(this);
      m_tabPane.add(ms_res.getString("tabname.general"), m_agpanel);
      setMnemonicForTabIndex("tabname.general", 0);
      //usage panel
      m_usgpanel = new ActionsUsagePanel(m_list);
      m_tabPane.add(ms_res.getString("tabname.usage"), m_usgpanel);
      setMnemonicForTabIndex("tabname.usage", 1);
      //command panel
      m_acpanel = new ActionsCommandPanel(m_list);

      m_tabPane.add(ms_res.getString("tabname.command"), m_acpanel);
      setMnemonicForTabIndex("tabname.command", 2);
      //visibility panel
      m_avpanel = new ActionsVisibilityPanel();
      m_tabPane.add(ms_res.getString("tabname.visibility"), m_avpanel);
      setMnemonicForTabIndex("tabname.visibility", 3);
      
      //properties panel
      m_appanel = new ActionsPropertiesPanel(
         new String[]{"Name", "Value"}, 1);
      m_tabPane.add(ms_res.getString("tabname.properties"), m_appanel);
      setMnemonicForTabIndex("tabname.properties", 4);
      setLayout(new BorderLayout());
      add(m_tabPane, BorderLayout.CENTER);
      setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
      setPreferredSize(new Dimension(350, 400));
   }

   /**
    * Depending on the <code>isLoad</code> parameter this loads all panels
    * from the supplied data objects or saves the data from all panels to the
    * supplied data object.
    *
    * @param data the data object to load from or save to, may be
    *    <code>null</code>.
    * @param isLoad <code>true</code> to load from the supplied data object,
    *    <code>false</code> to save to the supplied data object.
    * @return <code>true</code> if the update was successful, <code>false</code>
    *    otherwise.
    */
   public boolean update(Object data, boolean isLoad)
   {
      PSAction action = null;
      boolean isSuccess = false;
      if (data instanceof PSAction)
      {
         action = (PSAction) data;

         isSuccess = m_agpanel.update(action, isLoad);
         if (!isSuccess)
            return false;

         isSuccess = m_usgpanel.update(action, isLoad);
         if (!isSuccess)
            return false;

         isSuccess = m_acpanel.update(action, isLoad);
         if (!isSuccess)
            return false;

         isSuccess = m_avpanel.update(action, isLoad);
         if (!isSuccess)
            return false;

         isSuccess = m_appanel.update(action, isLoad);
      }

      return isSuccess;
   }

   public boolean validateData()
   {
      return m_agpanel.validateData();
   }

   public boolean validateDataUrl()
   {
      boolean isMenu = m_agpanel.isDynamicMenuSelected();
      if (isMenu)
      {
         if (m_acpanel.getUrl().length() == 0)
         {
            PSDlgUtil.showErrorDialog(ms_res.getString("err.urlmissing.msg"),
                  ms_res.getString("err.title"));
            return false;
         }
      }
      isMenu = m_agpanel.isCascadingMenuSelected();
      if (isMenu)
      {
         if (m_acpanel.getUrl().length() != 0)
         {
            int n = PSDlgUtil.showConfirmDialog(
                  ms_res.getString("err.urlwillberemoved.msg"),
                  ms_res.getString("err.title"),
                  JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (n == JOptionPane.YES_NO_OPTION)
            {
               m_acpanel.setUrl("", null);
            }
         }
      }
      return true;
   }

   public void itemStateChanged(ItemEvent e)
   {
      if (e.getStateChange() == ItemEvent.SELECTED)
      {
         int index = m_tabPane.indexOfTab(ms_res.getString("tabname.command"));
         if (index != -1)
            m_tabPane.remove(index);
      }
      else
      {
         m_tabPane.add(m_acpanel, 2);
         m_tabPane.setTitleAt(2, ms_res.getString("tabname.command"));
      }
   }

   /**
    * Refresh the general panels internal name text field
    * so it can disable itself if necessary.
    * @param data the action object. May be <code>null</code>.
    */
   public void refreshInternalName(Object data)
   {
      m_agpanel.refreshInternalName(data);
   }

   /**
    * Returns the selected index for the tabbed panel
    * @return selected index for the tabbed panel
    */
   public int getSelectedTabIndex()
   {
      return m_tabPane.getSelectedIndex();
   }

   /**
    * A service method to set mnemonics on a tab panel
    * This method can be used if the convention of resourcename.mn is used 
    * for adding mnemonics in the resource bundle
    * @param resId the resource id from the bundle cannot be <code>null</code>
    * @param tabIx is the tab index on which a mnemonic has to be set and it
    * cannot be <code>null</code>
    */
   private void setMnemonicForTabIndex(String resId, int tabIx)
   {
       char mnemonic;
       String tabName = ms_res.getString(resId);
       mnemonic = ms_res.getString(resId+".mn").charAt(0);
       int ix = tabName.indexOf(mnemonic);
       char upperMnemonic = (""+mnemonic).toUpperCase().charAt(0);
       m_tabPane.setMnemonicAt(tabIx, (int)upperMnemonic);
       m_tabPane.setDisplayedMnemonicIndexAt(tabIx, ix);
   }

   
   
   private List m_list;
   private JTabbedPane m_tabPane;
   private ActionsUsagePanel m_usgpanel;
   private ActionsGeneralPanel m_agpanel;
   private ActionsCommandPanel m_acpanel;
   private ActionsVisibilityPanel m_avpanel;
   private ActionsPropertiesPanel m_appanel;

   /**
    * Resource bundle for this class. Initialized in {@link init()}.
    * It's not modified after that. Never <code>null</code>, equivalent to a
    * variable declared final.
    */
   private static ResourceBundle ms_res;

   //test code
   public static void main(String[] arg)
   {
      JFrame f = new JFrame("BoxLayoutDemo");
      Container contentPane = f.getContentPane();
      PSActionTabbedPanel ac = new PSActionTabbedPanel(null);
      contentPane.add(ac, BorderLayout.CENTER);
      f.addWindowListener(new WindowAdapter() {
         public void windowClosing(WindowEvent e) {
            System.exit(0);
         }
      });
      f.pack();
      f.setVisible(true);
   }
   //end
}
