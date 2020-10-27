/******************************************************************************
 *
 * [ RoleMemberPropertyDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.E2Designer;

import com.percussion.EditableListBox.EditableListBox;
import com.percussion.conn.PSServerException;
import com.percussion.design.catalog.PSCataloger;
import com.percussion.design.objectstore.PSServerConfiguration;
import com.percussion.security.IPSSecurityProviderMetaData;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Vector;

/**
 * Class for adding a new member to a role or modifying properties of an existing member.
 * Properties can only be modified for application role members.
 *
 */
public class RoleMemberPropertyDialog extends PSDialog
{

   private static final long serialVersionUID = 1L;
   
   //create static resource bundle object
    static ResourceBundle ms_res = null;
    static
    {
      try
      {
        ms_res = ResourceBundle.getBundle( "com.percussion.E2Designer.RoleMemberPropertyDialogResources", Locale.getDefault( ) );
      }catch(MissingResourceException mre)
      {
        System.out.println( mre );
      }
    }

   /**
      * Constructor that takes the parent dialog, the name of the role for which the properties are to be
      * edited. A role with this name must be present in the collection of Application roles.
      * @version 1.0 1999/6/18
      *
      * @param dialog - the parent dialog
      * @param data - the RoleMemberData object stored in the editable list box cell that is to be edited.
      * If adding a new member set this to null.
      * @param conn - the designer connection
      * @param serverConfig - the server config
      * 
      * @see RoleMemberData
      *
      */
   public RoleMemberPropertyDialog(   JDialog dialog,
                                                      RoleMemberData data,
                                                      PSServerConfiguration serverConfig )
   {
      super(dialog);
      if(dialog != null)
         setLocationRelativeTo(dialog);
      else
         center();

      if(data == null)
      {
         m_bEditing = false;
      }
      else
      {
         m_bEditing = true;
      }

      m_data = data;

      initDialog();
   }

   /**
    * Appends a number that corresponds to the active tab, 1 based. This dialog
    * is used by different tabs under different contexts.
   **/
   protected String subclassHelpId( String helpId )
   {
      /* Note: There is a dependency here on the tab order in the parent dlg. A
         better way to do this might be to get a tab based on an internal name
         e.g. getAclTabIndex() and getRoleTabIndex().
         The other dependency is that our parent is always the AppSecDialog. */
      int tabIndex = ((AppSecDialog) getOwner()).getVisibleTabIndex();
      int tabId = 0;
      // if the tab index is "ACL"
      if ( 0 == tabIndex )
      {
         if ( isNewMemberBoxNull())
            tabId = 2;   // editing an ACL entry
         else
            tabId = 1;   // adding an ACL entry
      }
      // if the tab index is "Role"
      else if ( 1 == tabIndex )
      {
         if (((RoleMemberPropertyDialog)this).isNewMemberBoxNull())
            tabId = 4;   // editing a Role entry
         else
            tabId = 3;   // adding a role entry
      }
      return helpId + tabId;
   }


   /**
    *   Initializes the dialog. Sets the size and title, creates the controls, initializes the listeners
    * and centers the dialog.
    *
    */
   private void initDialog()
   {
      setSize(425,360);
      if(!m_bEditing)
         this.setTitle(getResources().getString("titleAddRoleMember"));

      createControls();
      initControls();
      initListeners();

   }

   /**
    *   Internal for creating the controls.
    *
    */
   private void createControls()
   {
      getContentPane().setLayout(null);

      m_panelRadio = new JPanel();
      m_panelRadio.setBorder(new TitledBorder(
                                    new EtchedBorder(),
                                    getResources().getString("memberType")));
      m_panelRadio.setLayout(null);
      m_panelRadio.setBounds(10,10,135,85);
      getContentPane().add(m_panelRadio);

      String labelStr = USER;
      char mn         = getResources().getString(USER+".mn").charAt(0); 
      m_radioUser     = new JRadioButton(labelStr);
      m_radioUser.setMnemonic(mn);
      m_radioUser.setDisplayedMnemonicIndex(labelStr.indexOf(mn));

      m_radioUser.setActionCommand(USER);
      m_radioUser.setBounds(30,20,100,18);
      m_panelRadio.add(m_radioUser);

      labelStr     = GROUP;
      mn           = getResources().getString(GROUP+".mn").charAt(0); 
      m_radioGroup = new JRadioButton(labelStr);
      m_radioGroup.setMnemonic(mn);
      m_radioGroup.setDisplayedMnemonicIndex(labelStr.indexOf(mn));
      
      m_radioGroup.setActionCommand(GROUP);
      m_radioGroup.setBounds(30,40,100,18);
      m_panelRadio.add(m_radioGroup);

      labelStr    = ROLE;
      mn          = getResources().getString(ROLE+".mn").charAt(0); 
      m_radioRole = new JRadioButton(labelStr);
      m_radioRole.setMnemonic(mn);
      m_radioRole.setDisplayedMnemonicIndex(labelStr.indexOf(mn));
      
      m_radioRole.setActionCommand(ROLE);
      m_radioRole.setBounds(30,60,100,18);
      m_radioRole.setVisible(m_bIncludeRoles);
      m_panelRadio.add(m_radioRole);

      m_radioFilter = new JRadioButton();
      m_radioFilter.setText(FILTER);
      m_radioFilter.setActionCommand(FILTER);
      m_radioFilter.setBounds(30,80,100,18);
      m_radioFilter.setVisible(false);
      m_panelRadio.add(m_radioFilter);

      m_panelCommand = new JPanel();
      m_panelCommand.setLayout(new FlowLayout(FlowLayout.CENTER,5,5));
      m_panelCommand.setBounds(305,5,113,90);
      getContentPane().add(m_panelCommand);

      m_panelMemberProps = new JPanel();
      m_panelMemberProps.setBorder(
                           new TitledBorder(
                                 new EtchedBorder(),
                                 getResources().getString("memberProperties")));
      m_panelMemberProps.setLayout(null);
      m_panelMemberProps.setBounds(10,100,400,225);
      getContentPane().add(m_panelMemberProps);

      m_panelSecurity = new JPanel();
      m_panelSecurity.setBounds(10,20,385,55);
      m_panelSecurity.setLayout(new BoxLayout(m_panelSecurity, 
         BoxLayout.X_AXIS));
      m_panelSecurity.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
      m_panelMemberProps.add(m_panelSecurity);

      labelStr = getResources().getString("provInst");
      mn = getResources().getString("provInst.mn").charAt(0); 
      m_labelProvInst =  new JLabel(labelStr);
      m_labelProvInst.setDisplayedMnemonic(mn);
      m_labelProvInst.setDisplayedMnemonicIndex(
                        labelStr.lastIndexOf((""+mn).toLowerCase().charAt(0)));
      m_labelProvInst.setHorizontalAlignment(SwingConstants.RIGHT);
      m_panelSecurity.add(m_labelProvInst);
      m_panelSecurity.add(Box.createHorizontalStrut(5));

      m_comboBoxProviderInstance = new UTFixedHeightComboBox();
      m_comboBoxProviderInstance.setEditable(false);
      m_panelSecurity.add(m_comboBoxProviderInstance);

      m_labelProvInst.setLabelFor(m_comboBoxProviderInstance);

      m_panelMemberTypes = new JPanel();
      m_panelMemberTypes.setLayout(null);
      m_panelMemberTypes.setBounds(10,70,385,150);
      m_panelMemberProps.add(m_panelMemberTypes);

      labelStr = getResources().getString("selectMember");
      mn = getResources().getString("selectMember.mn").charAt(0); 
      m_labelSelectMember =  new JLabel(labelStr);
      m_labelSelectMember.setDisplayedMnemonic(mn);
      m_labelSelectMember.setDisplayedMnemonicIndex(labelStr.indexOf(mn));

      m_labelSelectMember.setBounds(5,10,120,20);
      m_panelMemberTypes.add(m_labelSelectMember);

      m_scrollPaneMembers = new JScrollPane();
      m_scrollPaneMembers.setBounds(5,30,150,95);
      m_panelMemberTypes.add(m_scrollPaneMembers);

      m_listMembers = new JList();
      m_labelSelectMember.setLabelFor(m_listMembers);
      m_scrollPaneMembers.getViewport().add(m_listMembers);

      if(!m_bEditing)
      {
         m_panelEditableListBox = new JPanel();
         m_panelEditableListBox.setLayout(new BorderLayout());
         m_panelEditableListBox.setBounds(215,12,160,112);
         m_listAddedMembers = new EditableListBox();
         m_listAddedMembers.setTitle(getResources().getString("newMembers"));
         m_listAddedMembers.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      m_listAddedMembers.getRightButton().addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          m_listAddedMembers.deleteRows();
        }
      });

         m_panelEditableListBox.add(m_listAddedMembers);

         m_buttonAdd = new JButton(); 
         m_buttonAdd.setText(getResources().getString("add"));
         m_buttonAdd.setMnemonic(getResources().getString("add.mn").charAt(0));
         Insets m = new Insets(2,2,2,2);
         m_buttonAdd.setMargin(m);
         m_buttonAdd.setBounds(160,70,50,24);

         m_panelMemberTypes.add(m_panelEditableListBox);
         m_panelMemberTypes.add(m_buttonAdd);

      }

      m_buttonLookup = new JButton();
      m_buttonLookup.setBounds(40,126,80,22);
      m_buttonLookup.setText(getResources().getString("lookup"));
      m_buttonLookup.setMnemonic(
                              getResources().getString("lookup.mn").charAt(0));
      m_panelMemberTypes.add(m_buttonLookup);

      m_panelFilter = new JPanel();
      m_panelFilter.setLayout(null);
      m_panelFilter.setBounds(10,90,385,115);

      m_scrollPaneFilter = new JScrollPane();
      m_scrollPaneFilter.setBounds(5,30,375,75);
      m_panelFilter.add(m_scrollPaneFilter);

      m_textAreaFilter = new JTextArea();
      m_textAreaFilter.setLineWrap(true);
      m_textAreaFilter.setWrapStyleWord(true);
      m_scrollPaneFilter.getViewport().add(m_textAreaFilter);

      m_labelFilterString = new JLabel();
      m_labelFilterString.setText(getResources().getString("filterString"));
      m_labelFilterString.setBounds(5,5,150,20);
      m_panelFilter.add(m_labelFilterString);

      m_panelMemberProps.add(m_panelFilter);

      createCommandPanel();

   }

   /**
    *   Creates the command panel with OK, Cancel and Help buttons.
    *
    */
   private void createCommandPanel()
   {
      m_commandPanel = new UTStandardCommandPanel(this, "", SwingConstants.VERTICAL)
      {
        public void onOk()
        {
            RoleMemberPropertyDialog.this.onOk();
        }

      };

      getRootPane().setDefaultButton(m_commandPanel.getOkButton());
      m_panelCommand.add(m_commandPanel);

   }

   /**
    *   Internal for initializing the controls.
    *
    */
   private void initControls()
   {
      // group the radio buttons
      ButtonGroup group = new ButtonGroup();
      group.add(m_radioUser);
      group.add(m_radioGroup);
      group.add(m_radioRole);
      group.add(m_radioFilter);

      if(m_bEditing)   // editing an existing member
      {
//         m_buttonLookup.setEnabled(false);
         m_listMembers.getSelectionModel().setSelectionMode(
            ListSelectionModel.SINGLE_SELECTION);
         updateProviderInstanceCombo();

         if(m_data.isUser() || m_data.isGroup() || m_data.isRole())
         {
            if(m_data.isUser())
               m_radioUser.setSelected(true);
            else if(m_data.isGroup())
               m_radioGroup.setSelected(true);
            else
               m_radioRole.setSelected(true);

            m_panelFilter.setVisible(false);
            repaint();
            m_panelMemberTypes.setVisible(true);
            repaint();
            // add the member to the list
            Object [] oMembers = {m_data.getName()};
            m_listMembers.setListData(oMembers);
            m_listMembers.getSelectionModel().setSelectionInterval(0,0);
         }
         else //is filter
         {
            m_textAreaFilter.setText(m_data.getName());
            m_radioFilter.setSelected(true);
            m_panelMemberTypes.setVisible(false);
            repaint();
            m_panelFilter.setVisible(true);
            repaint();
         }

      }
      else   // adding new member
      {
         m_radioUser.setSelected(true);
         m_panelFilter.setVisible(false);
         repaint();
         m_panelMemberTypes.setVisible(true);
         repaint();
         updateProviderInstanceCombo();
         updateAddButtonState();
      }

   }

   /**
    *   Internal for initializing the listeners.
    *
    */
   private void initListeners()
   {
      ButtonListener bl = new ButtonListener();
      m_buttonLookup.addActionListener(bl);
      if(!m_bEditing)
         m_buttonAdd.addActionListener(bl);

      RadioListener rl = new RadioListener();
      m_radioUser.addActionListener(rl);
      m_radioGroup.addActionListener(rl);
      m_radioRole.addActionListener(rl);
      m_radioFilter.addActionListener(rl);

      ItemChangeListener icl = new ItemChangeListener();
      m_comboBoxProviderInstance.addItemListener(icl);

      ListItemListener l = new ListItemListener();
      m_listMembers.getSelectionModel().addListSelectionListener(l);

   }

   /** Inner class to implement ActionListener interface for radio button selections.
    */
   class RadioListener implements ActionListener
   {
      public void actionPerformed( ActionEvent e )
      {
         JRadioButton button = (JRadioButton)e.getSource();
         boolean bRoles = false;
         if(button == m_radioUser)
            onUserRadioSelected();
         else if(button == m_radioGroup)
            onGroupRadioSelected();
         else if(button == m_radioFilter)
            onFilterRadioSelected();
         else if(button == m_radioRole)
         {
            onRoleRadioSelected();
            bRoles = true;
         }
         updateLookupButtonState();
      }
   }


   /**
    * Inner class implements ListSelectionListener interface.
    */
   class ListItemListener implements ListSelectionListener
   {
      public void valueChanged( ListSelectionEvent e )
      {
          onListSelectionChanged();
      }
   }


   /**
    * Inner class to implement ActionListener interface for handling button events.
    */
   class ButtonListener implements ActionListener
   {
      public void actionPerformed( ActionEvent e )
      {
         JButton button = (JButton)e.getSource( );
         if(button == m_buttonLookup)
            onLookup();
         else if (button == m_buttonAdd)
            onAdd();
      }
   }

   /**
    * Inner class to implement ItemListener interface for handling the combo box
    * selection changes.
    */
   class ItemChangeListener implements ItemListener
   {
      public void itemStateChanged(ItemEvent e)
      {
         if (e.getSource() == m_comboBoxProviderInstance)
         {
            if (e.getStateChange() == ItemEvent.SELECTED)
               onProviderInstanceSelectionChange();
         }
      }
   }

   /**
    *   Handler for List selection change. Enables the add button if an item is
    * selected in the list.
    *
    */
   private void onListSelectionChanged()
   {
      updateAddButtonState();
   }



   /**
    *   Handler for Lookup button clicked. Performs a catalog and fills in the list.
    *
    */
   private void onLookup()
   {
      //catalog using PSObjectCatalogHandler and fill in the MemberList
      //for the current security provider type and instance.

/*      Vector v = new Vector();
      v.add("User1");
      v.add("user2");
      v.add("user3");*/

      Vector v = getMembers();
      m_listMembers.setListData(v);
      updateAddButtonState();
   }

   /**
    *   Handler for Add button clicked. Copies the selected names from the list to the
    * New members list.
    *
    */
   private void onAdd()
   {
      int [] iaSels = m_listMembers.getSelectedIndices();
      if(iaSels != null && iaSels.length > 0)
      {
         for(int i=0; i<iaSels.length; i++)
         {
            String strMember = (String)m_listMembers.getModel().getElementAt(iaSels[i]);
            boolean bAlreadyAdded = false;
            int addedCount = m_listAddedMembers.getItemCount();
            for(int j=0; j<addedCount; j++)
            {
               String memName = ((String)m_listAddedMembers.getRowValue(j));
               if(memName != null && !memName.equals(""))
               {
                  if(memName.equals(strMember))
                  {
                     bAlreadyAdded = true;
                     break;
                  }
               }
            }
            if(!bAlreadyAdded)
               m_listAddedMembers.addRowValue(strMember);
         }
      }
   }

   /**
    *   Filter panel is set invisible. Has no effect for V1.
    *
    */
   private void onUserRadioSelected()
   {
      if (!m_comboBoxProviderInstance.isEnabled())
         m_comboBoxProviderInstance.setEnabled(true);

      m_panelFilter.setVisible(false);
      repaint();
      m_panelMemberTypes.setVisible(true);
      repaint();
      if ( !m_bEditing )
         m_listMembers.setListData(m_userList);

      //Update Provider Instance as combo as the values are dependent
      //on which radio button was selected.
      updateProviderInstanceCombo();
   }

   /**
    *   Filter panel is set invisible. Has no effect for V1.
    *
    */
   private void onGroupRadioSelected()
   {
      if (!m_comboBoxProviderInstance.isEnabled())
         m_comboBoxProviderInstance.setEnabled(true);


      m_panelFilter.setVisible(false);
      repaint();
      m_panelMemberTypes.setVisible(true);
      repaint();
      if ( !m_bEditing )
         m_listMembers.setListData(m_groupList);

      CatalogerMetaData meta =
         (CatalogerMetaData)m_comboBoxProviderInstance.getSelectedItem();

      if( !getSupportedObjectTypes(meta).contains(
         IPSSecurityProviderMetaData.OBJECT_TYPE_GROUP))
      {
         updateProviderInstanceCombo();
         m_comboBoxProviderInstance.setSelectedItem(
            CatalogerMetaData.createMetaDataForAny());
      }
   }

   private void onRoleRadioSelected()
   {
      m_comboBoxProviderInstance.setEnabled(false);
      
      m_panelFilter.setVisible(false);
      repaint();
      m_panelMemberTypes.setVisible(true);
      repaint();

      //add roles to add list
      Vector v = m_vRoles;
      if ( !m_bEditing )
         m_listMembers.setListData(v);
      updateAddButtonState();

      //Disable left button to add new member to role in editable list box
      m_listAddedMembers.getLeftButton().setEnabled(false);
   }

   /**
    *   Filter panel is set visible. Filter radio is unavailable for V1.
    *
    */
   private void onFilterRadioSelected()
   {
      m_panelMemberTypes.setVisible(false);
      repaint();
      m_panelFilter.setVisible(true);
      repaint();
   }

   /**
    *   Handler for Security provider instance selection change.
    *
    */
   private void onProviderInstanceSelectionChange()
   {
      updateLookupButtonState();
      invalidateObjectCache();
   }

   /**
    * Sets the cached user and group lists to empty lists and clears the Select
    * member list box.
   **/
   private void invalidateObjectCache()
   {
      if ( !m_bEditing )
      {
         // clear the cataloged objects so they are consistent w/ the currently chosen provider
         m_userList = new Vector(0);
         m_groupList = new Vector(0);
         // clear the list itself
         m_listMembers.setListData(m_groupList);
      }
   }

   /**
    *   gets the members for the currently selected Security Provider Instance
    *
    */
   private Vector getMembers()
   {
      try
      {
         //initialize the cataloger only if needed
         if(m_psCataloger ==null)
            m_psCataloger = new PSCataloger(E2Designer.getDesignerConnection());
      }
      catch(IllegalArgumentException e)
      {
         e.printStackTrace();
      }

      //System.out.println("getting members...");

      Vector v = new Vector();
      // we can't catalog roles right now
      if (m_radioRole.isSelected())
         return v;

      CatalogerMetaData meta = 
         (CatalogerMetaData)m_comboBoxProviderInstance.getSelectedItem();
      if(meta.isAnyCataloger())
      {
         throw new RuntimeException("Must select an actual instance not " + 
            meta.getDisplayName()); 
      }                                                                          

      Properties  catalogProps  = new Properties( );
      String objectType = null;
      boolean bUser = false;
      if (m_radioUser.isSelected())
      {
         objectType = "user";
         bUser = true;
      }
      else if (m_radioGroup.isSelected())
         objectType = "group";

      try
      {
         Document xmlDoc   = null;

         catalogProps.put("RequestCategory", "security");
         catalogProps.put("RequestType", "Object");
         catalogProps.put("CatalogerName", meta.getName());
         catalogProps.put("CatalogerType", meta.getType());
         catalogProps.put("ObjectType", objectType);

         xmlDoc = m_psCataloger.catalog( catalogProps );
         PSXmlTreeWalker tree = new PSXmlTreeWalker(xmlDoc);

         while (tree.getNextElement("Object", true, true) != null)
         {
            String objectName = tree.getElementData("name", false);
            if(objectName != null)
               v.add(objectName);
         }
         if ( bUser )
            m_userList = v;
         else
            m_groupList = v;
      }
      // take advantage of the SqlCatalogHandler
      catch ( IOException ioe )
      {
         SqlCataloger.handleException( ioe );
      }
      catch ( PSAuthorizationException ae )
      {
         SqlCataloger.handleException( ae );
      }
      catch ( PSAuthenticationFailedException ae )
      {
         SqlCataloger.handleException( ae );
      }
      catch ( PSServerException se )
      {
         SqlCataloger.handleException( se );
      }
      catch ( IllegalArgumentException iae )
      {
         JOptionPane.showMessageDialog( E2Designer.getApp().getMainFrame(),
               "Required properties for cataloging have changed.",
               "Security Cataloger Exception",
               JOptionPane.ERROR_MESSAGE);
      }
      //System.out.println("Object types count ="+v.size());
      return v;
   }




   /**
    * Updates the Security provider instance combo box based on the Security provider Type selection.
    *
    */
   private void updateProviderInstanceCombo()
   {
      if(m_comboBoxProviderInstance.getItemCount() > 0)
         m_comboBoxProviderInstance.removeAllItems();

      CatalogerMetaData anyMeta = CatalogerMetaData.createMetaDataForAny();
      
      
      m_comboBoxProviderInstance.addItem( anyMeta );

      for (CatalogerMetaData meta : CatalogCatalogers.getCatalog(null))
      {
         if(m_radioGroup.isSelected())
         {
            if(getSupportedObjectTypes(meta).contains(
               IPSSecurityProviderMetaData.OBJECT_TYPE_GROUP))
            {
               m_comboBoxProviderInstance.addItem(meta);
            }
         }
         else
            m_comboBoxProviderInstance.addItem(meta);

      }
      updateLookupButtonState();
   }

   /**
    * Catalogs server to get the list of supported object types for the supplied
    * cataloger if they are not yet cached. Logs the error to console if an
    * exception happens in the catalog process.
    *
    * @param meta the cataloger metadata to get supported object types, assumed
    * not to be <code>null</code>.
    *
    * @return the list of supported object types for the cataloger, never <code>
    * null</code>, may be empty.
    */
   private List getSupportedObjectTypes(CatalogerMetaData meta)
   {
      if(m_instanceObjectTypesMap.get(meta) != null)
         return (List)m_instanceObjectTypesMap.get(meta);

      try
      {
         //initialize the cataloger only if needed
         if(m_psCataloger == null)
            m_psCataloger = new PSCataloger(E2Designer.getDesignerConnection());
      }
      catch(IllegalArgumentException e)
      {
         //should not come here
      }

      List list = new ArrayList();
      Properties  catalogProps = new Properties( );

      try
      {
         Document xmlDoc = null;
         catalogProps.put("RequestCategory", "security");
         catalogProps.put("RequestType", "ObjectTypes");
         catalogProps.put("CatalogerName", meta.getName());
         catalogProps.put("CatalogerType", meta.getType());

         xmlDoc = m_psCataloger.catalog( catalogProps );
         PSXmlTreeWalker tree = new PSXmlTreeWalker(xmlDoc);

         Element objectTypeElement;

         while (
            (objectTypeElement = tree.getNextElement("ObjectType", true, true))
            != null )
         {
            String Type = objectTypeElement.getAttribute("type");

            if(Type != null)
               list.add(Type);
         }
      }
      catch(Exception e)
      {
         System.out.println(e.getLocalizedMessage());
         e.printStackTrace();
      }

      m_instanceObjectTypesMap.put(meta, list);

      return list;
   }

   /**
    * Sets the enabled state of the lookup button based on the current state of
    * other controls.
   **/
   private void updateLookupButtonState()
   {
      if(m_comboBoxProviderInstance.getItemCount() > 0)
      {
         CatalogerMetaData meta = 
            (CatalogerMetaData)m_comboBoxProviderInstance.getSelectedItem();
         if ( m_bEditing
            || meta.isAnyCataloger()
            || m_radioRole.isSelected())
         {
            m_buttonLookup.setEnabled(false);
         }
         else
            m_buttonLookup.setEnabled(true);
      }
   }

   /**
    * Updates the add button state.
    *
    */
   private void updateAddButtonState()
   {
      if(!m_bEditing)
      {
         Object [] oaValues = m_listMembers.getSelectedValues();
         if(oaValues == null || oaValues.length <= 0)
            m_buttonAdd.setEnabled(false);
         else
            m_buttonAdd.setEnabled(true);
      }
   }

   /**
    * validates dialog data.
    *
    */
   private boolean validateDialogData()
   {
      if(m_radioFilter.isSelected())
      {
         String strFilter = m_textAreaFilter.getText().trim();
         if(strFilter == null || strFilter.equals(""))
         {
            JOptionPane.showMessageDialog(null,
                                                      getResources().getString("enterFilter"),
                                                      getResources().getString("error"),
                                                      JOptionPane.ERROR_MESSAGE);
            return false;
         }
      }
      if(m_bEditing && !m_radioFilter.isSelected())
      {
         int iSel = m_listMembers.getMinSelectionIndex();
         int count = m_listMembers.getModel().getSize();
         if(count > 1 && iSel < 0) // put up error only if more than one item in member list
         {
            JOptionPane.showMessageDialog(null,
                                                      getResources().getString("mustSelectMember"),
                                                      getResources().getString("error"),
                                                      JOptionPane.ERROR_MESSAGE);
            return false;
         }

      }
      if(!m_bEditing && !m_radioFilter.isSelected())
      {
         int count = m_listAddedMembers.getItemCount();
//         System.out.println("Validating data ... new member count = "+count);
         if(count == 0)
         {
            JOptionPane.showMessageDialog(null,
                                                       getResources().getString("mustAddMember"),
                                           getResources().getString("error"),
                                           JOptionPane.ERROR_MESSAGE);
            return false;

         }
         else if(count > 0)
         {
            String strName = (String)m_listAddedMembers.getRowValue(0);
            if(strName == null || (strName.trim()).equals(""))
            {
               JOptionPane.showMessageDialog(null,
                                                       getResources().getString("mustAddMember"),
                                           getResources().getString("error"),
                                           JOptionPane.ERROR_MESSAGE);
               return false;
            }
         }
      }
      return true;
   }

   /**
    * saves the dialog data on OK click.
    *
    */
   private boolean saveData()
   {
      if(!m_bEditing)
         m_data = new RoleMemberData();

      if(!m_radioFilter.isSelected())
      {
         if(!m_bEditing)
            m_data.setName((String)m_listAddedMembers.getRowValue(0));
         else
            m_data.setName((String)m_listMembers.getSelectedValue());
      }
      else
      {
         m_data.setName(m_textAreaFilter.getText());
      }

      if(m_radioUser.isSelected())
         m_data.setMemberType(USER);
      else if(m_radioGroup.isSelected())
         m_data.setMemberType(GROUP);
      else if(m_radioRole.isSelected())
         m_data.setMemberType(ROLE);
      else if(m_radioFilter.isSelected())
         m_data.setMemberType(FILTER);

      return true;
   }


  /** @returns boolean <CODE>true</CODE> = the New Member EditableListBox is
    * visible.
  */
   public boolean isNewMemberBoxNull()
  {
      return null == m_panelEditableListBox;
  }


   /**
    * Returns true if OK button was clicked.
    *
    */
   public boolean isModified()
   {
      return m_bModified;
   }


   /**
    * Returns the member m_data that has the dialog data. The name of the
    * data is set to first member in the New member list.
    *
    */
   public RoleMemberData getData()
   {
      return m_data;
   }

   /**
    * Returns the vector of member names in the new member list.
    *
    */
   public Vector getMemberNames()
   {
      Vector v = new Vector();
      if(!m_bEditing)
      {
         int count = m_listAddedMembers.getItemCount();
         for(int i=0; i<count; i++)
         {
            String strName = ((String)m_listAddedMembers.getRowValue(i)).trim();
            if(!strName.equals(""))
               v.add(strName);
         }
      }
      else
      {
         v.add((String)m_listMembers.getSelectedValue());
      }
      return v;
   }

   private void closeDialog()
   {
      setVisible(false);
   }

   public void includeRoles(Vector vRoles)
   {
      m_bIncludeRoles = true;
      m_vRoles = vRoles;
      m_radioRole.setVisible(true);
      doLayout();
   }

/** Handles ok button action. Overrides PSDialog onOk() method implementation.
*/
   public void onOk()
   {
      if ( null != m_listAddedMembers )
         m_listAddedMembers.getCellEditor().stopCellEditing();

      boolean bValid = validateDialogData();
      if(!bValid)
         return;
      boolean bSaved = saveData();

      if(!bSaved)
         return;
      m_bModified = true;
      closeDialog();
   }

   //{{DECLARE_CONTROLS
   JPanel m_panelRadio;
   JRadioButton m_radioUser;
   JRadioButton m_radioRole;
   JRadioButton m_radioGroup;
   JRadioButton m_radioFilter;
   JPanel m_panelCommand;
   JPanel m_panelMemberProps;
   JPanel m_panelSecurity;
   JLabel m_labelProvInst;
   JLabel m_labelSelectMember;
   JComboBox m_comboBoxProviderInstance;
   JScrollPane m_scrollPaneMembers;
   JScrollPane m_scrollPaneFilter;
   JList m_listMembers;
   JLabel m_labelMemberType;
   JButton m_buttonLookup;
   JPanel m_panelMemberTypes;
   JPanel m_panelFilter;
   JTextArea   m_textAreaFilter;
   JLabel   m_labelFilterString;
   JButton m_buttonAdd;
   JPanel m_panelEditableListBox;
   //}}

   private EditableListBox m_listAddedMembers = null;
   private UTStandardCommandPanel m_commandPanel = null;
   private RoleMemberData m_data = null;
   private boolean m_bEditing = false;
   private boolean m_bModified = false;
   private PSCataloger m_psCataloger = null;

   private boolean m_bIncludeRoles = false;
   // store cataloged users/groups and passed in role list
   private Vector m_vRoles = new Vector();
   private Vector m_userList = new Vector(0);
   private Vector m_groupList = new Vector(0);


   /**
    * Caches the supported object types for each security provider instance with
    * instance name (<code>String</code>) as key and <code>List</code> of
    * supported object types (<code>String</code>) for the instance as value.
    */
   private Map m_instanceObjectTypesMap = new HashMap();

   public static   final String USER = ms_res.getString("user");
   public static   final String GROUP = ms_res.getString("group");
   public static   final String FILTER = ms_res.getString("filter");
   public static   final String ROLE = ms_res.getString("role");
}
