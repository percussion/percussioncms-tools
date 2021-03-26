/* *****************************************************************************
 *
 * [ OptionsPropertyDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *******************************************************************************/
package com.percussion.E2Designer;


import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.nio.charset.StandardCharsets;

/** The options dialog that is accessed through the menu bar -&gt; Tools -&gt;
 * Options.
 */
public final class OptionsPropertyDialog extends PSDialog {
   //
   // CONSTRUCTORS
   //
   
   public OptionsPropertyDialog(javax.swing.JFrame f) {
      super(f);
      m_parent=f;
      m_strPsxTag = Util.getXSplitDynamicPrefix();
      initDialog();
      center();
   }
   
   //
   // PRIVATE METHODS
   //
   public void onCancel() {
      if( m_parent != null ) {
         if( m_parent instanceof UIMainFrame ) {
            UIMainFrame t=(UIMainFrame)m_parent;
            t.deleteOptionsDialog();
         }
      }
      dispose();
   }
   
   /** Performs the ok action.
    */
   public void onOk() {
      int option = 0;
      if (m_bgGroup.getSelection() == m_openNewButton.getModel())
         option = STARTUP_NEW;
      if (m_bgGroup.getSelection() == m_openLastButton.getModel())
         option = STARTUP_LAST;
      if (m_bgGroup.getSelection() == m_openNothingButton.getModel())
         option = STARTUP_NONE;
      
      
      UserConfig.getConfig().setBoolean(E2Designer.getResources().getString("JOIN_OPTION"),
      m_autoJoins.isSelected());
      
      UserConfig.getConfig().setBoolean(E2Designer.getResources().getString("ENABLEARROWS"),
      m_enableArrows.isSelected());
      
      UserConfig.getConfig().setBoolean(E2Designer.getResources().getString("SHOWTIDYMSG"),
      m_tidy.isSelected());
      
      UserConfig.getConfig().setInteger(STARTUP_OPTION, option);
      //TODO: Add ability to ask if they want to replace
      //    UserConfig.getConfig().setBoolean(ASK_OPTION, m_askRemove.isSelected());
      
      UserConfig.getConfig().setValue(PSX_TAG, m_tfDynDataPrefix.getText());
      UserConfig.getConfig().setValue("xslOutputEncoding",
         (String) m_xslOutputEncoding.getSelectedItem());
      UserConfig.getConfig().flush();
      dispose();
      if( m_parent != null ) {
         if( m_parent instanceof UIMainFrame ) {
            UIMainFrame t=(UIMainFrame)m_parent;
            t.deleteOptionsDialog();
            Frame frames[]=t.getFrames();
            if( frames != null ) {
               for(int count=0;count<frames.length;count++) {
                  frames[count].repaint();
               }
               
            }
            
         }
      }
   }
   
   
   /** Initializes the components within the dialog.
    */
   private void initDialog()
   {
      m_commandPanel = new UTStandardCommandPanel(this, "",
         javax.swing.SwingConstants.HORIZONTAL) 
      {
         public void onOk() 
         {
            OptionsPropertyDialog.this.onOk();
         }
         public void onCancel() 
         {
            OptionsPropertyDialog.this.onCancel();
         }
      };
      JPanel cmdPanel = new JPanel(new BorderLayout());
      cmdPanel.add(m_commandPanel, BorderLayout.EAST);

      m_commandPanel.setBorder(new EmptyBorder(10, 5, 5, 5));
      
      Box b1 = Box.createHorizontalBox();
      b1.add(createRadioPanel());
      b1.add(Box.createHorizontalStrut(5));

      
      Box b2 = Box.createVerticalBox();
      b2.add(b1);
      b2.add(Box.createVerticalStrut(5));
      b2.add(createXSplitSettingsPanel());
      b2.add(Box.createVerticalStrut(5));
      b2.add(createAutoJoins());
      b2.add(Box.createVerticalStrut(5));
      b2.add(createArrows());
      
      // set the default button
      getRootPane().setDefaultButton(m_commandPanel.getOkButton());
      getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
      
      JPanel mainPanel = new JPanel(new BorderLayout());      
      mainPanel.add(b2, BorderLayout.NORTH);
      mainPanel.add(cmdPanel, BorderLayout.SOUTH);
      getContentPane().add(mainPanel);
      setResizable(true);
      pack();
   }
   
   private JPanel createArrows() {
      JPanel panel = new JPanel(new BorderLayout());
      
      panel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED),
      E2Designer.getResources().getString("Display")));

      m_enableArrows = new 
         JCheckBox(E2Designer.getResources().getString("EnableArrows"));
      m_enableArrows.setMnemonic(getResources().getString("enableArrows.mn").charAt(0));
      boolean bSelected=
            UserConfig.getConfig().getBoolean(
                     E2Designer.getResources().getString("ENABLEARROWS"),true);
      m_enableArrows.setSelected(bSelected);
      panel.add(m_enableArrows, BorderLayout.WEST);
      
      return(panel);
      
   }
   
   private JPanel createAutoJoins() 
   {
      m_autoJoins = new JCheckBox(
         E2Designer.getResources().getString("enablejoins"));
      m_autoJoins.setMnemonic(getResources().getString("enableJoins.mn").charAt(0));
      boolean bSelected = true;
      
      bSelected = UserConfig.getConfig().getBoolean(
         E2Designer.getResources().getString("JOIN_OPTION"), true);
      m_autoJoins.setSelected(bSelected);
      JPanel panel = new JPanel(new BorderLayout());
      panel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED),
         E2Designer.getResources().getString("databaseObjects")));
      panel.add(m_autoJoins, BorderLayout.WEST);

      return panel;
   }
   
   /** Creates the Startup options panel of this dialog.
    */
   private JPanel createRadioPanel() 
   {
      m_openNewButton = new JRadioButton(getResources().getString("new"));
      
      m_openLastButton = new JRadioButton(getResources().getString("last"));
      m_openNothingButton = new JRadioButton(getResources().getString("nothing"));
      
      m_openNewButton.setMnemonic(
                              getResources().getString("new.mn").charAt(0));
      m_openLastButton.setMnemonic(
                              getResources().getString("last.mn").charAt(0));
      m_openNothingButton.setMnemonic(
                              getResources().getString("nothing.mn").charAt(0));
      m_bgGroup = new ButtonGroup();
      m_bgGroup.add(m_openNewButton);
      m_bgGroup.add(m_openLastButton);
      m_bgGroup.add(m_openNothingButton);
      
      // get user config and set startup options.
      int option = UserConfig.getConfig().getInteger(STARTUP_OPTION, STARTUP_NEW);
      if (STARTUP_NEW == option)
         m_openNewButton.setSelected(true);
      else if (STARTUP_LAST == option)
         m_openLastButton.setSelected(true);
      else if (STARTUP_NONE == option)
         m_openNothingButton.setSelected(true);
      
      
      JPanel panel = new JPanel();
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
      panel.add(m_openNewButton);
      panel.add(m_openLastButton);
      panel.add(m_openNothingButton);
      // panel.add(m_autoJoins);
      panel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED),
      getResources().getString("startuptitle")));
      
      m_askRemove = new JCheckBox(getResources().getString("askremove"));
      boolean bAsk = UserConfig.getConfig().getBoolean(ASK_OPTION, false);
      m_askRemove.setSelected(bAsk);
      
      JPanel askPanel = new JPanel();
      askPanel.setLayout(new BoxLayout(askPanel, BoxLayout.Y_AXIS));
      askPanel.add(m_askRemove);
      askPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED),
      getResources().getString("askremovetitle")));
      
      JPanel mainPanel = new JPanel();
      mainPanel.setLayout(new BorderLayout());
      mainPanel.add(panel, "West");
      
      return mainPanel;
   }
   
   /** Creates the Startup options panel of this dialog. <B>All options default
    * settings (booleans) must be a static constant from Util.</B>
    */
   private JPanel createXSplitSettingsPanel() 
   {
      JPanel labelGrid = new JPanel(new GridLayout(5,1));
      
      String labelStr = getResources().getString("dynamicDataPrefix");
      char mn         = getResources().getString("dynamicDataPrefix.mn").charAt(0);
      JLabel dynLabel =   new JLabel(labelStr, SwingConstants.RIGHT);
      dynLabel.setDisplayedMnemonic(mn);
      dynLabel.setDisplayedMnemonicIndex(labelStr.indexOf(mn));
      dynLabel.setVerticalAlignment(SwingConstants.BOTTOM);
      
      
      labelStr = getResources().getString("xslOutputEncoding");
      mn       = getResources().getString("xslOutputEncoding.mn").charAt(0);
      JLabel encLabel =   new JLabel(labelStr, SwingConstants.RIGHT);
      encLabel.setDisplayedMnemonic(mn);
      encLabel.setDisplayedMnemonicIndex(labelStr.indexOf(mn));
      encLabel.setVerticalAlignment(SwingConstants.CENTER);
  
      // Arrange the Left Grid panel with labels
      labelGrid.add(dynLabel);
      labelGrid.add(Box.createVerticalStrut(5));
      labelGrid.add(encLabel);
      labelGrid.add(Box.createVerticalStrut(5));
      
      labelStr = E2Designer.getResources().getString("ShowTidyWarnings");
      mn       = getResources().getString("showTidyWarnings.mn").charAt(0);
      m_tidy = new JCheckBox(labelStr);
         m_tidy.setSelected(UserConfig.getConfig().getBoolean(
            E2Designer.getResources().getString("SHOWTIDYMSG"), false));
      m_tidy.setMnemonic(mn);
      labelGrid.add(m_tidy);
     
     JPanel textGrid = new JPanel(new GridLayout(5,1));
     
     m_tfDynDataPrefix = new UTFixedTextField(m_strPsxTag);

     String xslEncoding = UserConfig.getConfig().getValue("xslOutputEncoding");
     if (xslEncoding == null || xslEncoding.trim().length() == 0)
        xslEncoding = StandardCharsets.UTF_8.toString();
     m_xslOutputEncoding = new UTFixedComboBox(
             UserConfig.XSL_OUTPUT_ENCODINGS);
     m_xslOutputEncoding.setSelectedItem(xslEncoding);
     textGrid.add(m_tfDynDataPrefix);
     textGrid.add(Box.createVerticalStrut(5));
     textGrid.add(m_xslOutputEncoding);
     textGrid.add(Box.createVerticalGlue());
     

     dynLabel.setLabelFor(m_tfDynDataPrefix);
     encLabel.setLabelFor(m_xslOutputEncoding);
     
     
     JPanel panel = new JPanel();
     panel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED),
        getResources().getString("xSplitSettings")));
     panel.add(labelGrid, BorderLayout.WEST);
     panel.add(textGrid, BorderLayout.CENTER);
     panel.setMinimumSize(new Dimension(350,150));
     return panel;
   }
   
   
   
   //
   // MEMBER VARIABLES
   //
   private JCheckBox m_autoJoins=null;
   private JRadioButton m_openNewButton, m_openLastButton, m_openNothingButton;
   private ButtonGroup m_bgGroup;
   private UTStandardCommandPanel m_commandPanel;
   private JCheckBox m_askRemove = null;
   private UTFixedTextField m_tfDynDataPrefix = null;
   /**
    * The text field to display / edit the output encoding for the stylesheets
    * created while splitting. Initialized during dialog init, never
    * <code>null</code> after that.
    */
   private UTFixedComboBox m_xslOutputEncoding = null;
   private JCheckBox m_enableArrows=null;
   private JCheckBox m_tidy= null;
   
   private String m_strPsxTag = "";
   public static final String PSX_TAG = "DynamicDataPrefix";
   
   // startup options
   public static final int STARTUP_NEW = 0;
   public static final int STARTUP_LAST = 1;
   public static final int STARTUP_NONE = 2;
   
   public static final String STARTUP_OPTION = "StartupOption";
   public static final String ASK_OPTION = "AskRemoveDragDropOption";
   private javax.swing.JFrame m_parent;
}
