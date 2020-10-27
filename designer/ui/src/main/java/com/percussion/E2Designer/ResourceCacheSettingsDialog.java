/* *****************************************************************************
 *
 * [ ResourceCacheSettingsDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *******************************************************************************/

package com.percussion.E2Designer;

import com.percussion.design.objectstore.PSResourceCacheSettings;
import com.percussion.guitools.ErrorDialogs;
import com.percussion.guitools.PSListBox;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.Vector;


/**
 * Dialog to specify settings for caching based on the dataset handling the
 * request.
 */
public class ResourceCacheSettingsDialog extends PSDialog
{
   /**
    * Construct this dialog.
    * 
    * @param parent The parent dialog, may not be <code>null</code>.
    * @param settings The cache settings this dialog will act upon, may not be 
    * <code>null</code>.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    */
   public ResourceCacheSettingsDialog(Dialog parent, 
      PSResourceCacheSettings settings)
   {
      super(parent);
      
      if (settings == null)
         throw new IllegalArgumentException("settings may not be null");
      
      m_settings = settings;
      initDialog();
   }

   /**
    * Construct this dialog.
    * 
    * @param parent The parent frame, may not be <code>null</code>.
    * @param settings The cache settings this dialog will act upon, may not be 
    * <code>null</code>.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    */
   public ResourceCacheSettingsDialog(Frame parent, 
      PSResourceCacheSettings settings)
   {
      super(parent);
      
      if (settings == null)
         throw new IllegalArgumentException("settings may not be null");
      
      m_settings = settings;
      initDialog();
   }

   /**
    * Handle user click of OK button.  Saves all dialog data to the cache
    * settings supplied during construction and disposes the dialog.  There can
    * be no invalid data entered in the dialog, so there is no validation.
    */
   public void onOk()
   {
      m_settings.setIsCachingEnabled(m_cbEnabled.isSelected());
      m_settings.setAdditionalKeys(m_keyList.iterator());
      m_settings.setDependencies(m_depList.iterator());
      dispose();
   }
   
   /**
    * Initializes the dialog, creating tabs and their contents.
    */   
   private void initDialog()
   {
      ResourceBundle bundle = getResources();
      setTitle(bundle.getString("title"));
      
      // create main panel
      JPanel mainPane = new JPanel();
      mainPane.setLayout(new BorderLayout());
      mainPane.setBorder(BorderFactory.createEmptyBorder( 5, 5, 5, 5 ));

      // create bottomPanel
      JPanel bottomPanel = new JPanel();
      bottomPanel.setLayout(new BorderLayout());

      UTStandardCommandPanel cmdPanel = new UTStandardCommandPanel(this,
         SwingConstants.HORIZONTAL, true)
      {
         public void onOk()
         {
            ResourceCacheSettingsDialog.this.onOk();
         }
      };
         
      bottomPanel.add(cmdPanel, BorderLayout.EAST);

      // create tabbed pane
      JTabbedPane tabbedPane = new JTabbedPane();
      String pStr = bundle.getString("general");
      JPanel genPanel = createGeneralPanel();
      tabbedPane.addTab(pStr, genPanel);
      char mn = bundle.getString("general.mn").charAt(0);
      int mnIx = pStr.indexOf(mn);   
      tabbedPane.setDisplayedMnemonicIndexAt(0, mnIx);
      tabbedPane.setMnemonicAt(0, (int)mn);
         
      JPanel depPanel = createDependenciesPanel();
      pStr = bundle.getString("dependencies"); 
      mn = bundle.getString("dependencies.mn").charAt(0);
      mnIx = pStr.indexOf(mn);   
      tabbedPane.addTab(pStr, depPanel);
      tabbedPane.setDisplayedMnemonicIndexAt(1, mnIx);
      tabbedPane.setMnemonicAt(1, (int)mn);
      
      // add the tabbed pane and bottom panel to the main panel
      mainPane.add(tabbedPane, BorderLayout.CENTER);
      mainPane.add(bottomPanel, BorderLayout.SOUTH);
      getContentPane().add(mainPane);
      pack();
      center();
      setResizable(true);
   }
   
   /**
    * Create the panel that displays the contents of the general tab
    * 
    * @return The panel, never <code>null</code>.
    */
   private JPanel createGeneralPanel()
   {
      ResourceBundle bundle = getResources();
      
      JPanel panel = new JPanel(new BorderLayout());
      panel.setBorder(BorderFactory.createEmptyBorder( 5, 5, 5, 5 ));
      panel.setPreferredSize(new Dimension(475, 300));
      
      // create top panel
      JPanel top = new JPanel();
      top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
      top.setBorder(BorderFactory.createEmptyBorder( 5, 5, 5, 5 ));
      
      // create text area for warning and add to top panel
      JPanel textPanel = new JPanel(new BorderLayout());
      textPanel.setBorder(BorderFactory.createCompoundBorder(
         BorderFactory.createBevelBorder(BevelBorder.LOWERED),
         BorderFactory.createEmptyBorder(5,5,5,5)));
      JTextArea text = new JTextArea();
      text.setLineWrap(true);
      text.setWrapStyleWord(true);
      text.setText(bundle.getString("warning"));
      text.setEditable(false);
      text.setFont(getFont());
      textPanel.add(text, BorderLayout.CENTER);
      textPanel.setBackground(text.getBackground());
      top.add(textPanel);
      top.add(Box.createVerticalStrut(5));
      
      // create the check box panel and add to top panel
      JPanel cbPanel = new JPanel(new BorderLayout());
      m_cbEnabled = new JCheckBox(bundle.getString("enabled"), 
         m_settings.isCachingEnabled());
      m_cbEnabled.setMnemonic(bundle.getString("enabled.mn").charAt(0));
      m_cbEnabled.setAlignmentX(LEFT_ALIGNMENT);
      cbPanel.add(m_cbEnabled, BorderLayout.WEST);
      top.add(cbPanel);
      
      // add the top panel
      panel.add(top, BorderLayout.NORTH);
      
      // create list of data types for value selector
      final Vector types = new Vector();
      types.add(new DTCgiVariable());
      types.add(new DTCookie());
      types.add(new DTUserContext());

      // create list box containing additional keys
      m_keyList = new PSListBox(bundle.getString("addKeys"), 
         m_settings.getAdditionalKeys())
      {
         /**
          * Overrides abstract method in <code>PSListBox</code> to handle 
          * click of Add button.  Displays value selector dialog to user so that 
          * additional keys may be specified.  Does not allow duplicate entries
          * to be added.
          * 
          * @return A <code>PSNamedReplacementValue</code> object if one was
          * specified, or <code>null</code> if not.
          */
         public Object onAdd()
         {
            Object result = null;
            try
            {
               ValueSelectorDialog selector = new ValueSelectorDialog(
                  ResourceCacheSettingsDialog.this, types, null);
               selector.setVisible(true);
               result = selector.getData();
               if (result != null && m_keyList.containsItem(result))
               {
                  handleDupe(result);
                  result = null;
               }
            }
            catch (Exception e)
            {
               e.printStackTrace();
            }
            
            return result;
         }
      };
      
      // add the list box to the panel
      panel.add(m_keyList, BorderLayout.CENTER);
      
      return panel;
   }
   
   /**
    * Create the panel that displays the contents of the dependencies tab.
    * 
    * @return The panel, never <code>null</code>.
    */
   private JPanel createDependenciesPanel()
   {
      ResourceBundle bundle = getResources();
      JPanel panel = new JPanel(new BorderLayout());
      panel.setBorder(BorderFactory.createEmptyBorder(20, 5, 10, 5));
      
      // create list box containing depedencies
      final String title = bundle.getString("selectDeps");
      m_depList = new PSListBox(bundle.getString("dependencies"), 
         m_settings.getDependencies())
      {
         /**
          * Overrides abstract method in <code>PSListBox</code> to handle 
          * click of Add button.  Displays resource selector dialog to user so 
          * that dependencies may be specified.  Does not allow duplicate 
          * entries to be added.
          * 
          * @return A <code>String</code> object in the form 
          * "<appname>/<resourcename>" if one was specified, or 
          * <code>null</code> if not.
          */
         public Object onAdd()
         {
            Object result = null;
            try
            {
               ResourceSelectorDialog dlg = new ResourceSelectorDialog(
                  ResourceCacheSettingsDialog.this, title);
               dlg.setVisible(true);
               String app = dlg.getAppName();
               String resource = dlg.getRequestPageName();
               if (app != null || resource != null)
               {
                  result = app + "/" + resource;
                  if (result != null && m_depList.containsItem(result))
                  {
                     handleDupe(result);
                     result = null;
                  }
               }
            }
            catch (Exception e)
            {
               e.printStackTrace();
            }
            
            return result;
         }
      };
      
      // add the list box to the panel
      panel.add(m_depList, BorderLayout.CENTER);
      
      return panel;
   }

   /**
    * Unit test method.
    * 
    * @param arg No args expected.
    */
   public static void main(String[] arg)
   {
      try
      {
         final JFrame frame = new JFrame("Test Dialog");
         UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
         frame.addWindowListener(new BasicWindowMonitor());
         
         JButton startButton = new JButton("Open Dialog");
         frame.getContentPane().add(startButton);
         startButton.addActionListener(new ActionListener()
         {
            public void actionPerformed(ActionEvent e)
            {
               
               ResourceCacheSettingsDialog dialog = 
                  new ResourceCacheSettingsDialog( frame, 
                     new PSResourceCacheSettings());
               dialog.setVisible(true);
            }
         });
         
         frame.setSize(640, 480);
         frame.setVisible(true);
      }
      catch (Exception ex)
      {
         ex.printStackTrace();
      }

   }
   
   /**
    * Handles attempt to add a duplicate object to a list.  Displays an error
    * to the user, calling the <code>toString()</code> on the supplied object
    * for its string representation in the message.
    * 
    * @param o The object that is already in the list, assumed not 
    * <code>null</code>.
    */
   private void handleDupe(Object o)
   {
      ResourceBundle bundle = getResources();
      String errorTitle = bundle.getString("dupeErrorTitle");
      String errorMsg = bundle.getString("dupeError");
      Object[] args = {o};
      String msg = MessageFormat.format(errorMsg, args);
      ErrorDialogs.showErrorMessage(getOwner(), msg, errorTitle);
   }
   
   /**
    * The cache settings modified by this dialog.  Supplied in the ctor, never
    * <code>null</code> after that.  Modified only by a call to 
    * <code>onOk()</code>.
    */
   private PSResourceCacheSettings m_settings;
   
   /**
    * The checkbox to indicate if caching is enabled or disabled.  If selected
    * caching is enabled.  Initialized based on the settings supplied to the
    * ctor, never <code>null</code> after that.
    */
   private JCheckBox m_cbEnabled;
   
   /**
    * The list box containing the additional keys as 
    * <code>PSNamedReplacementValue</code> objects.  Initialized based on the 
    * settings supplied to the ctor, never <code>null</code> after that.
    */
   private PSListBox m_keyList;
   
   /**
    * The list box containing the dependencies as <code>String</code> objects.
    * Initialized based on the settings supplied to the ctor, never 
    * <code>null</code> after that.
    */
   private PSListBox m_depList;
}
