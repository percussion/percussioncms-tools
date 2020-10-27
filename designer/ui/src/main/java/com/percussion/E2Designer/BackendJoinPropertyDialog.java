/****************************************************************************
 * $Id: BackendJoinPropertyDialog.java 1.22 2000/06/28 00:09:24Z AlexTeng Release $
 * COPYRIGHT (c) 2000 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted 
 * work including confidential and proprietary information of Percussion.
 *
 * Version Labels  : $Name: Pre_CEEditorUI RX_40_REL 20010618_3_5 20001027_3_0 20000724_2_0 $
 *
 * Locked By       : $Locker: $
 *
 * Revision History:
 * $Log: BackendJoinPropertyDialog.java $
 * Revision 1.22  2000/06/28 00:09:24Z  AlexTeng
 * Changed PSExtensionCall to OSExtensionCall.
 * 
 * Revision 1.21  2000/02/18 00:55:09Z  paulhoward
 * Finished updating to use new extension model classes.
 * 
 * Revision 1.20  2000/02/09 16:59:57Z  candidorodriguez
 * changed to new extensions.
 * 
 * Revision 1.19  1999/09/19 04:52:11Z  AlexTeng
 * Removed all uses of "import....*" to use each individual object file.
 * 
 * Revision 1.18  1999/09/09 16:32:55  AlexTeng
 * Fixed a bug where the Edit formula check box unchecked/checked
 * causes an exception on "ok" button press.
 * 
 * Revision 1.17  1999/09/01 02:26:44  AlexTeng
 * Fixed bug (OSAO-4B3PFM).
 * I made this dialog create a new FormulaPropertyDialog everytime
 * onOk() was called.
 * 
 * Revision 1.16  1999/08/20 23:50:26  martingenhart
 * commit application UDFs to application
 * Revision 1.15  1999/08/12 00:14:12  AlexTeng
 * Updated to use PSUdfSet instead of PSApplication.
 * The call to FormulaPropertyDialog now references "this" to give the
 * correct parent.
 * 
 * Revision 1.14  1999/08/04 15:00:06  AlexTeng
 * Changed the onOk() method call to use the correct "this"
 * reference.
 * 
 * Revision 1.13  1999/07/26 17:55:20  AlexTeng
 * Changed onOk, onCancel, and onHelp to be handled by the PSDialog.
 * All sub-classes of PSDialog will now override the 3 methods for 
 * dialog specific actions.
 * Added key listening capability for ENTER, ESCAPE, and F1 keys.
 * 
 * Revision 1.12  1999/07/15 23:33:29  markdandrea
 * Fixed formula editor for joins.
 *
 * Revision 1.11  1999/05/26 17:20:26  martingenhart
 * added backend datatank for value selector
 * Revision 1.10  1999/05/25 18:27:25  martingenhart
 * need a valid application before starting the dialog
 * Revision 1.9  1999/05/11 17:13:54  paulhoward
 * Removed unused import that was causing compilation failure.
 * 
 * Revision 1.8  1999/05/11 16:55:24  markdandrea
 * Need to pass nothing into the Formula dialog.
 * 
 * Revision 1.7  1999/05/05 23:31:01  markdandrea
 * Added formula support in join.
 * 
 * Revision 1.6  1999/05/04 22:37:13  markdandrea
 * Added formula picture.
 * 
 * Revision 1.5  1999/05/03 22:25:14  markdandrea
 * Revision 1.4  1999/05/03 17:49:49  markdandrea
 * Selector and Mapper now taking catalog columns from all tables.
 * Implemented copy, cut and paste for join editor.
 * 
 * Revision 1.3  1999/04/30 21:55:11  markdandrea
 * Connected Join ui to backend objects.  Enable drag of table
 * from browser to join editor.
 * 
 * Revision 1.2  1999/04/29 22:57:01  markdandrea
 * Connected join editor ui to backend objects.
 * 
 * Revision 1.1  1999/04/28 22:50:54  markdandrea
 * Initial revision
 *
 ****************************************************************************/
package com.percussion.E2Designer;

import com.percussion.util.PSCollection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * This class is used to edit the properties of a table join.
 */
////////////////////////////////////////////////////////////////////////////////
public class BackendJoinPropertyDialog extends PSEditorDialog
{
   /**
   * Construct the default join info dialog.
   *
   */
   //////////////////////////////////////////////////////////////////////////////
   public BackendJoinPropertyDialog(OSBackendDatatank tank)
   {
      super();
      m_tank = tank;
      initDialog();
   }

   /**
   * Construct the join info dialog with provided information.
   *
   * @param frame      the parent frame
   */
   //////////////////////////////////////////////////////////////////////////////
   public BackendJoinPropertyDialog(Frame frame, OSBackendDatatank tank)
   {
      super(frame);
      m_tank = tank;
      initDialog();
   }   

   /**
    * Initialize dialog fields
    */
   private void initDialog()
   {
      // initialize the standard command panel, implement the onOk action and
      // make the OK button default
      m_commandPanel = new UTStandardCommandPanel(this, "", SwingConstants.VERTICAL)
      {
        public void onOk()
        {
           BackendJoinPropertyDialog.this.onOk();
        }
      };
      getRootPane().setDefaultButton(m_commandPanel.getOkButton());

      JPanel panel = new JPanel(new BorderLayout());
      panel.setBorder(new EmptyBorder(5, 5, 5, 5));
      panel.add(createViewPanel(), "West");
      panel.add(m_commandPanel, "East");

        getContentPane().setLayout(new BorderLayout());
      getContentPane().add(panel);
      this.setSize(DIALOG_SIZE);
      pack();
   }
   
   /**
   * Create the dialogs view/edit panel.
   *
   */
   //////////////////////////////////////////////////////////////////////////////
   private JPanel createViewPanel()
   {
      //Box box = new Box(BoxLayout.Y_AXIS);
      //box.add(Box.createVerticalGlue());
      JPanel box = new JPanel(new GridLayout(2,2));
      box.add(createFieldPanel(m_rbInnerJoin, "innerjoin", true));
      box.add(createFieldPanel(m_rbLeftOuterJoin, "leftouterjoin", false));
      box.add(createFieldPanel(m_rbRightOuterJoin, "rightouterjoin", false));
      box.add(createFieldPanel(m_rbFullOuterJoin, "fullouterjoin", false));
      
      JPanel panel = new JPanel(new BorderLayout());
      panel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED),
          getResources().getString("jointype")));
      panel.add(box, BorderLayout.NORTH);

      JPanel box2 = new JPanel(new GridLayout(1,1));
      box2.add(createFieldPanel(m_cbTranslationEnabled, "enabletranslation",
         false));

      JPanel panel2 = new JPanel(new BorderLayout());
      panel2.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED),
         getResources().getString("translation")));
      panel2.add(box2, BorderLayout.NORTH);
      m_editTrans = new JButton(getResources().getString("edittranslation"));
      final BackendJoinPropertyDialog parent = this;
      //handle button for translator dialog
      Box box3 = new Box(BoxLayout.X_AXIS);
      box3.add(m_editTrans);
      m_editTrans.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            m_dlg = new FormulaPropertyDialog( BackendJoinPropertyDialog.this,
                                               m_udfSet,
                                               m_call,
                                               m_tank);
            m_dlg.setLocationRelativeTo(parent);

            m_dlg.setVisible(true);
            m_call = m_dlg.getUdfCall();
         }
      });

      //handler for enabling translation
      m_cbTranslationEnabled.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            UpdateButton();
         }
      });

      panel2.add(box3, BorderLayout.SOUTH);
      
      JPanel panel3 = new JPanel(new BorderLayout());
      panel3.add(panel, BorderLayout.NORTH);
      panel3.add(panel2, BorderLayout.SOUTH);
      panel3.setBorder(new EmptyBorder(5,5,5,10));

      return panel3;
   }
   
   /**
   * Create read only panels for all display fields.
   *
   * @param field      the field to create th epanel for
   * @param data      the initial field data
   * @param resId      the fields resource ID
   */
   //////////////////////////////////////////////////////////////////////////////
   private JPanel createFieldPanel(JRadioButton field, String resId, boolean bSelected)
   {
      field.setText(getResources().getString(resId));
      field.setEnabled(true);
      field.setSelected(bSelected);

      m_ButtonGroup.add(field);

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
      panel.add(field);

      return panel;
   }

   /**
   * Create read only panels for all display fields.
   *
   * @param field      the field to create th epanel for
   * @param data      the initial field data
   * @param resId      the fields resource ID
   */
   //////////////////////////////////////////////////////////////////////////////
   private JPanel createFieldPanel(JCheckBox field, String resId, boolean bSelected)
   {
      field.setText(getResources().getString(resId));
      field.setEnabled(true);
      field.setSelected(bSelected);

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
      panel.add(field);

      return panel;
   }

/** Overriding the ok button action from PSDialog.onOk() method implementation.
*/
  public void onOk()
  {
    if(m_rbInnerJoin.isSelected())
      m_join.setInnerJoin();
    else if(m_rbLeftOuterJoin.isSelected())
      m_join.setLeftOuterJoin();
    else if(m_rbRightOuterJoin.isSelected())
      m_join.setRightOuterJoin();
    else if(m_rbFullOuterJoin.isSelected())
      m_join.setFullOuterJoin();
         
    if(!m_cbTranslationEnabled.isSelected())
      m_join.setTranslator(null);
    else
      m_join.setTranslator(m_call);

    try
    {
      PSCollection exits = new PSCollection("com.percussion.extension.IPSExtensionDef");

      if (null != m_dlg && null != m_dlg.getUdfCall())
        exits.add(m_dlg.getUdfCall().getExtensionDef());
    }
    catch (ClassNotFoundException e)
    {
      e.printStackTrace();
    }

    dispose();
  }
  

   /**
    * @return The extension call that was defined as the translator for the
    * join.
    */
   public OSExtensionCall getCall()
   {
      return(m_call);
   }


   //////////////////////////////////////////////////////////////////////////////
   // implementation of IEditor
   public boolean onEdit(UIFigure figure, final Object data)
   {
      if (figure.getData() instanceof OSBackendJoin)
      {
         m_app = (OSApplication) data;

         m_udfSet = m_app.getUdfSet();
         m_join = (OSBackendJoin) figure.getData();
         m_call = (OSExtensionCall) m_join.getTranslator();
         m_rbInnerJoin.setSelected(false);
         m_rbLeftOuterJoin.setSelected(false);
         m_rbRightOuterJoin.setSelected(false);
         m_rbFullOuterJoin.setSelected(false);

         if(m_join.isInnerJoin())
            m_rbInnerJoin.setSelected(true);
         else if(m_join.isLeftOuterJoin())
            m_rbLeftOuterJoin.setSelected(true);
         else if(m_join.isRightOuterJoin())
            m_rbRightOuterJoin.setSelected(true);
         else if(m_join.isFullOuterJoin())
            m_rbFullOuterJoin.setSelected(true);
         
         boolean bHasTranslation = m_join.getTranslator() == null ? false : true;
         m_cbTranslationEnabled.setSelected(bHasTranslation);
         UpdateButton();
         
         this.center();
         this.setVisible(true);
      }
      else
         throw new IllegalArgumentException(getResources().getString("datatypeError"));

      // this is read only information
      return false;
   }
   
   public boolean isTranslatorEnabled()
   {
      return m_cbTranslationEnabled.isSelected();   
   }
   
   private void UpdateButton()
   {
      m_editTrans.setEnabled(m_cbTranslationEnabled.isSelected());
   }
   
   /**
   * the standard command panel
   */
   private UTStandardCommandPanel m_commandPanel = null;
   /**
   * the dialog size
   */
   private final static Dimension DIALOG_SIZE = new Dimension(400, 240);
   private FormulaPropertyDialog m_dlg = null;
   
   /**
    * Radio buttons for join type
    */
   private JRadioButton m_rbInnerJoin = new JRadioButton();
   private JRadioButton m_rbLeftOuterJoin = new JRadioButton();
   private JRadioButton m_rbRightOuterJoin = new JRadioButton();
   private JRadioButton m_rbFullOuterJoin = new JRadioButton();
   private static ButtonGroup m_ButtonGroup = new ButtonGroup();
   private OSApplication m_app = null;   
   /**
    * Translation controls
    */
   private JCheckBox m_cbTranslationEnabled = new JCheckBox();
   private OSBackendJoin m_join = null;
   private OSBackendDatatank m_tank = null;
   private OSExtensionCall m_call = null;
   private JButton m_editTrans = null;
   private PSUdfSet m_udfSet = null;
}