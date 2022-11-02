/*[ NotifierPropertyDialog.java ]**********************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;

import com.percussion.EditableListBox.EditableListBox;
import com.percussion.EditableListBox.IDataExchange;
import com.percussion.design.objectstore.PSRecipient;
import com.percussion.util.PSCollection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;


public class NotifierPropertyDialog extends PSEditorDialog
{
//
// CONSTRUCTORS
//

  public NotifierPropertyDialog()
  {
    super();
  
    initDialog();
  }

  public NotifierPropertyDialog(Window parent)
  {
    super(parent);

    initDialog();
  }

//
// PUBLIC METHODS
//

/** Implementation of PSEditorDialog.
*/
  public boolean onEdit( UIFigure figure, final Object data )
  {

    if ( null != figure.getData() && figure.getData() instanceof OSNotifier)
    {
      m_notifier = (OSNotifier)figure.getData();
    
      // setting mailserver name and from name to dialog
      if (m_notifier.getServer().equals("<Unspecified>"))
        m_mailServerField.setText("");
      else
        m_mailServerField.setText(m_notifier.getServer());
      m_fromField.setText(m_notifier.getFrom());
      
      // checking providertype and selecting it in the combobox
      if (OSNotifier.MP_TYPE_SMTP == m_notifier.getProviderType())
        m_smtpBox.setSelectedItem("SMTP");

      // setting up the Recipients (if exists) to the 'ListBox.
      PSCollection collection = m_notifier.getRecipients();
      if (null != collection)
      {
        try
        {
          for (int i = 0; i < collection.size(); i++)
          {
            OSRecipient recipient = new OSRecipient((PSRecipient)collection.get(i));
            m_notifyList.addRowValue(recipient);
          }
        }
        catch (Exception e)
        {
          e.printStackTrace();
        }
      }
    
      center();
      setVisible(true);
    }
    return true;
  }


/** The onOk action in UTStandardCommandPanel is carried out here.  This is made
  * for possible KeyListener button extensions.
*/
  public void onOk()
  {
    if (activateValidation())
    {
      try
      {
        m_notifyList.getCellEditor().stopCellEditing();

        int type = 0;
        if (((String)m_smtpBox.getSelectedItem()).equals("SMTP"))
          type = OSNotifier.MP_TYPE_SMTP;

        m_notifier.setProviderType(type);
        m_notifier.setFrom(m_fromField.getText());
        m_notifier.setServer(m_mailServerField.getText());

        if (1 == m_selectedList.size())
        {
          saveRecipientSingle();
        }

        PSCollection collection = new PSCollection("com.percussion.design.objectstore.PSRecipient");
        for (int i = 0; i < m_notifyList.getItemCount(); i++)
        {
          // making sure the OSRecipient name is not an empty-string;
          // otherwise, ignore
          if (!((OSRecipient)m_notifyList.getRowValue(i)).getName().trim().equals(""))
            collection.add(m_notifyList.getRowValue(i));
        }
        if (0 >= collection.size())
          m_notifier.setRecipients(null);
        else
          m_notifier.setRecipients(collection);
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }

      dispose();
    }
  }  


/** Method delegate for method valueChanged() of m_notifyList&apos;s
  * ListSelectionListener implementation.
*/
  public void onValueChanged(ListSelectionEvent e)
  {
    if ((e.getValueIsAdjusting()) || (-1 == e.getFirstIndex()))
      return;
        
    if (1 == m_selectedList.size())
      saveRecipientSingle();
    else if (1 < m_selectedList.size())
      saveRecipientMultiple();
          
    int min = 0;
    int max = m_notifyList.getItemCount();  
        
    for (int i = min; i < max; i++)
    {
      if (((javax.swing.DefaultListSelectionModel)e.getSource()).isSelectedIndex(i))
        m_selectedList.add(m_notifyList.getRowValue(i));
    }

    if (1 == m_selectedList.size())
    {
      setRecipientDataIntoDialog((OSRecipient)m_selectedList.elementAt(0));
      updateDialogSingle();
    }
    else if (1 < m_selectedList.size())
    {
      updateDialogMultiple();
    }
  }  
  
//
// PRIVATE METHODS
//

/** Saving data into a single recipient object.
*/
  private void saveRecipientSingle()
  {
    setDialogDataIntoRecipient((OSRecipient)m_selectedList.elementAt(0));

    m_selectedList.clear();
  }


/** Saving data into a multiple recipient objects.
*/
  private void saveRecipientMultiple()
  {
    
    
    m_selectedList.clear();
  }
  

/** Updates the dialog with new recipient data.
*/
  private void updateDialogSingle()
  {
    // spinTextField related checkboxes
    if (ICON == m_app_authBox.getIcon())
    {
      m_app_authBox.setIcon(null);
      m_app_authBox.setPressedIcon(null);
    }                                    
    if (m_app_authBox.isSelected())
      m_app_authField.setEnabled(true);
    else
      m_app_authField.setEnabled(false);

      
    if (ICON == m_app_timeBox.getIcon())
    {
      m_app_timeBox.setIcon(null);
      m_app_timeBox.setPressedIcon(null);
    }
    if (m_app_timeBox.isSelected())
      m_app_timeField.setEnabled(true);
    else
      m_app_timeField.setEnabled(false);


    if (ICON == m_app_queueBox.getIcon())
    {
      m_app_queueBox.setIcon(null);
      m_app_queueBox.setPressedIcon(null);
    }
    if (m_app_queueBox.isSelected())
      m_app_queueField.setEnabled(true);
    else
      m_app_queueField.setEnabled(false);


    if (ICON == m_be_authBox.getIcon())
    {
      m_be_authBox.setIcon(null);
      m_be_authBox.setPressedIcon(null);
    }  
    if (m_be_authBox.isSelected())
      m_be_authField.setEnabled(true);
    else
      m_be_authField.setEnabled(false);


    if (ICON == m_be_queueBox.getIcon())
    {
      m_be_queueBox.setIcon(null);
      m_be_queueBox.setPressedIcon(null);
    }
    if (m_be_queueBox.isSelected())
      m_be_queueField.setEnabled(true);
    else
      m_be_queueField.setEnabled(false);

    // non-spinTextField related checkboxes
    if (ICON == m_notify_disableBox.getIcon())
    {
      m_notify_disableBox.setIcon(null);
      m_notify_disableBox.setPressedIcon(null);
    }
    if (ICON == m_app_designBox.getIcon())
    {
      m_app_designBox.setIcon(null);
      m_app_designBox.setPressedIcon(null);
    }
    if (ICON == m_app_validBox.getIcon())
    {
      m_app_validBox.setIcon(null);
      m_app_validBox.setPressedIcon(null);
    }
    if (ICON == m_app_xmlBox.getIcon())
    {
      m_app_xmlBox.setIcon(null);
      m_app_xmlBox.setPressedIcon(null);
    }
    if (ICON == m_app_htmlBox.getIcon())
    {
      m_app_htmlBox.setIcon(null);
      m_app_htmlBox.setPressedIcon(null);
    }
    if (ICON == m_be_downBox.getIcon())
    {
      m_be_downBox.setIcon(null);
      m_be_downBox.setPressedIcon(null);
    }
    if (ICON == m_be_dataBox.getIcon())
    {
      m_be_dataBox.setIcon(null);
      m_be_dataBox.setPressedIcon(null);
    }
    if (ICON == m_be_queryBox.getIcon())
    {
      m_be_queryBox.setIcon(null);
      m_be_queryBox.setPressedIcon(null);
    }
    if (ICON == m_be_updateBox.getIcon())
    {
      m_be_updateBox.setIcon(null);
      m_be_updateBox.setPressedIcon(null);
    }
    
    // threshold radio buttons and their spinTextFields
    if (BUTTON == m_thresh_exceedButton.getIcon() || 
        BUTTON == m_thresh_persistButton.getIcon())
    {
      m_thresh_exceedButton.setIcon(null);
      m_thresh_exceedButton.setPressedIcon(null);
      m_thresh_persistButton.setIcon(null);
      m_thresh_persistButton.setPressedIcon(null);
    }          
    if (m_thresh_exceedButton.isSelected())
    {
      m_thresh_persistField.setEnabled(false);
      m_thresh_exceedField.setEnabled(true);
    }
    else
    {
      m_thresh_exceedField.setEnabled(false);
      m_thresh_persistField.setEnabled(true);
    }
  }


  private void updateDialogMultiple()
  {
    boolean test = false;
    // loop each component to check for differences
    // checkboxes first
    test = ((OSRecipient)m_selectedList.elementAt(0)).isSendEnabled();
    for (int i = 1; i < m_selectedList.size(); i++)
    {
      if (test != ((OSRecipient)m_selectedList.elementAt(i)).isSendEnabled())
      {
        m_notify_disableBox.setIcon(ICON);
        m_notify_disableBox.setPressedIcon(ICON_PRESSED); 
        break;
      }  
    }
    test = ((OSRecipient)m_selectedList.elementAt(0)).isAppAuthorizationFailureEnabled();
    for (int i = 1; i < m_selectedList.size(); i++)
    {
      if (test != ((OSRecipient)m_selectedList.elementAt(i)).isAppAuthorizationFailureEnabled())
      {
        m_app_authBox.setIcon(ICON);
        m_app_authBox.setPressedIcon(ICON_PRESSED);
        m_app_authField.setEnabled(false);
        break;
      }  
    }
    test = ((OSRecipient)m_selectedList.elementAt(0)).isAppDesignErrorEnabled();  
    for (int i = 1; i < m_selectedList.size(); i++)
    {
      if (test != ((OSRecipient)m_selectedList.elementAt(i)).isAppDesignErrorEnabled())
      {
        m_app_designBox.setIcon(ICON);
        m_app_designBox.setPressedIcon(ICON_PRESSED);
        break;
      }  
    }  
    test = ((OSRecipient)m_selectedList.elementAt(0)).isAppValidationErrorEnabled();
    for (int i = 1; i < m_selectedList.size(); i++)
    {
      if (test != ((OSRecipient)m_selectedList.elementAt(i)).isAppValidationErrorEnabled())
      {
        m_app_validBox.setIcon(ICON);
        m_app_validBox.setPressedIcon(ICON_PRESSED);
        break;
      }  
    }  
    test = ((OSRecipient)m_selectedList.elementAt(0)).isAppXmlErrorEnabled();
    for (int i = 1; i < m_selectedList.size(); i++)
    {
      if (test != ((OSRecipient)m_selectedList.elementAt(i)).isAppXmlErrorEnabled())
      {
        m_app_xmlBox.setIcon(ICON);
        m_app_xmlBox.setPressedIcon(ICON_PRESSED);
        break;
      }  
    }
    test = ((OSRecipient)m_selectedList.elementAt(0)).isAppHtmlErrorEnabled();
    for (int i = 1; i < m_selectedList.size(); i++)
    {
      if (test != ((OSRecipient)m_selectedList.elementAt(i)).isAppHtmlErrorEnabled())
      {
        m_app_htmlBox.setIcon(ICON);
        m_app_htmlBox.setPressedIcon(ICON_PRESSED);
        break;
      }  
    }  
    test = ((OSRecipient)m_selectedList.elementAt(0)).isAppRequestQueueLargeEnabled();
    for (int i = 1; i < m_selectedList.size(); i++)
    {
      if (test != ((OSRecipient)m_selectedList.elementAt(i)).isAppRequestQueueLargeEnabled())
      {
        m_app_queueBox.setIcon(ICON);
        m_app_queueBox.setPressedIcon(ICON_PRESSED);
        m_app_queueField.setEnabled(false);
        break;
      }  
    }
    test = ((OSRecipient)m_selectedList.elementAt(0)).isAppResponseTimeEnabled();  
    for (int i = 1; i < m_selectedList.size(); i++)
    {
      if (test != ((OSRecipient)m_selectedList.elementAt(i)).isAppResponseTimeEnabled())
      {
        m_app_timeBox.setIcon(ICON);
        m_app_timeBox.setPressedIcon(ICON_PRESSED);
        m_app_timeField.setEnabled(false);
        break;
      }  
    }  
    test = ((OSRecipient)m_selectedList.elementAt(0)).isBackEndAuthorizationFailureEnabled();      
    for (int i = 1; i < m_selectedList.size(); i++)
    {
      if (test != ((OSRecipient)m_selectedList.elementAt(i)).isBackEndAuthorizationFailureEnabled())
      {
        m_be_authBox.setIcon(ICON);
        m_be_authBox.setPressedIcon(ICON_PRESSED);
        m_be_authField.setEnabled(false);
        break;
      }  
    }
    test = ((OSRecipient)m_selectedList.elementAt(0)).isBackEndDataConversionErrorEnabled();
    for (int i = 1; i < m_selectedList.size(); i++)
    {
      if (test != ((OSRecipient)m_selectedList.elementAt(i)).isBackEndDataConversionErrorEnabled())
      {
        m_be_dataBox.setIcon(ICON);
        m_be_dataBox.setPressedIcon(ICON_PRESSED);
        break;
      }  
    }
    test = ((OSRecipient)m_selectedList.elementAt(0)).isBackEndQueryFailureEnabled();  
    for (int i = 1; i < m_selectedList.size(); i++)
    {
      if (test != ((OSRecipient)m_selectedList.elementAt(i)).isBackEndQueryFailureEnabled())
      {
        m_be_queryBox.setIcon(ICON);
        m_be_queryBox.setPressedIcon(ICON_PRESSED);
        break;
      }  
    }
    test = ((OSRecipient)m_selectedList.elementAt(0)).isBackEndRequestQueueLargeEnabled();  
    for (int i = 1; i < m_selectedList.size(); i++)
    {
      if (test != ((OSRecipient)m_selectedList.elementAt(i)).isBackEndRequestQueueLargeEnabled())
      {
        m_be_queueBox.setIcon(ICON);
        m_be_queueBox.setPressedIcon(ICON_PRESSED);
        m_be_queueField.setEnabled(false);
        break;
      }  
    }
    test = ((OSRecipient)m_selectedList.elementAt(0)).isBackEndServerDownFailureEnabled();  
    for (int i = 1; i < m_selectedList.size(); i++)
    {
      if (test != ((OSRecipient)m_selectedList.elementAt(i)).isBackEndServerDownFailureEnabled())
      {
        m_be_downBox.setIcon(ICON);
        m_be_downBox.setPressedIcon(ICON_PRESSED);
        break;
      }  
    }
    test = ((OSRecipient)m_selectedList.elementAt(0)).isBackEndUpdateFailureEnabled();  
    for (int i = 1; i < m_selectedList.size(); i++)
    {
      if (test != ((OSRecipient)m_selectedList.elementAt(i)).isBackEndUpdateFailureEnabled())
      {
        m_be_updateBox.setIcon(ICON);
        m_be_updateBox.setPressedIcon(ICON_PRESSED);
        break;
      }  
    }

    // radioButtons
    test = ((OSRecipient)m_selectedList.elementAt(0)).isErrorThresholdByCount();
    for (int i = 1; i < m_selectedList.size(); i++)
    {
      if (test != ((OSRecipient)m_selectedList.elementAt(i)).isErrorThresholdByCount())
      {
        m_thresh_exceedButton.setIcon(BUTTON);
        m_thresh_exceedButton.setPressedIcon(BUTTON_PRESSED);
        m_thresh_persistButton.setIcon(BUTTON);
        m_thresh_persistButton.setPressedIcon(BUTTON_PRESSED);
        m_thresh_exceedField.setEnabled(false);
        m_thresh_persistField.setEnabled(false);
        break;
      }  
    }
      
    // spinTextFields
    int value = ((OSRecipient)m_selectedList.elementAt(0)).getAppAuthorizationFailureCount();
    for (int i = 0; i < m_selectedList.size(); i++)
    {
      if (value != ((OSRecipient)m_selectedList.elementAt(i)).getAppAuthorizationFailureCount())
      {
        m_app_authField.clear();
        break;
      }
    }
    
    value = ((OSRecipient)m_selectedList.elementAt(0)).getAppRequestQueueMax();
    for (int i = 0; i < m_selectedList.size(); i++)
    {
      if (value != ((OSRecipient)m_selectedList.elementAt(i)).getAppRequestQueueMax())
      {
        m_app_queueField.clear();
        break;
      }
    }

    value = ((OSRecipient)m_selectedList.elementAt(0)).getAppResponseTimeMax();
    for (int i = 0; i < m_selectedList.size(); i++)
    {
      if (value != ((OSRecipient)m_selectedList.elementAt(i)).getAppResponseTimeMax())
      {
        m_app_timeField.clear();
        break;
      }
    }

    value = ((OSRecipient)m_selectedList.elementAt(0)).getBackEndAuthorizationFailureCount();
    for (int i = 0; i < m_selectedList.size(); i++)
    {
      if (value != ((OSRecipient)m_selectedList.elementAt(i)).getBackEndAuthorizationFailureCount())
      {
        m_be_authField.clear();
        break;
      }
    }

    value = ((OSRecipient)m_selectedList.elementAt(0)).getBackEndRequestQueueMax();
    for (int i = 0; i < m_selectedList.size(); i++)
    {
      if (value != ((OSRecipient)m_selectedList.elementAt(i)).getBackEndRequestQueueMax())
      {
        m_be_queueField.clear();
        break;
      }
    }

    value = ((OSRecipient)m_selectedList.elementAt(0)).getErrorThresholdCount();
    for (int i = 0; i < m_selectedList.size(); i++)
    {
      if (value != ((OSRecipient)m_selectedList.elementAt(i)).getErrorThresholdCount())
      {
        m_thresh_exceedField.clear();
        break;
      }
    }

    value = ((OSRecipient)m_selectedList.elementAt(0)).getErrorThresholdInterval();
    for (int i = 0; i < m_selectedList.size(); i++)
    {
      if (value != ((OSRecipient)m_selectedList.elementAt(i)).getErrorThresholdInterval())
      {
        m_thresh_persistField.clear();
        break;
      }
    }
  }
  

/** @param data The OSRecipient object to set all the components of this dialog
  * to mirror.
*/
  private void setRecipientDataIntoDialog(OSRecipient data)
  {
    // checkboxes
    m_app_authBox.setSelected(data.isAppAuthorizationFailureEnabled());
    m_app_designBox.setSelected(data.isAppDesignErrorEnabled());
    m_app_validBox.setSelected(data.isAppValidationErrorEnabled());
    m_app_xmlBox.setSelected(data.isAppXmlErrorEnabled());
    m_app_htmlBox.setSelected(data.isAppHtmlErrorEnabled());
    m_app_timeBox.setSelected(data.isAppResponseTimeEnabled());
    m_app_queueBox.setSelected(data.isAppRequestQueueLargeEnabled());
    
    m_be_authBox.setSelected(data.isBackEndAuthorizationFailureEnabled());
    m_be_downBox.setSelected(data.isBackEndServerDownFailureEnabled());
    m_be_dataBox.setSelected(data.isBackEndDataConversionErrorEnabled());
    m_be_queryBox.setSelected(data.isBackEndQueryFailureEnabled());
    m_be_updateBox.setSelected(data.isBackEndUpdateFailureEnabled());
    m_be_queueBox.setSelected(data.isBackEndRequestQueueLargeEnabled());
    m_notify_disableBox.setSelected(data.isSendEnabled());

    // spin text fields
    if (0 != data.getAppAuthorizationFailureCount())
      m_app_authField.setData(data.getAppAuthorizationFailureCount());
    if (0 != data.getAppResponseTimeMax())
      m_app_timeField.setData(data.getAppResponseTimeMax());
    if (0 != data.getAppRequestQueueMax())
      m_app_queueField.setData(data.getAppRequestQueueMax());
    if (0 != data.getBackEndAuthorizationFailureCount())
      m_be_authField.setData(data.getBackEndAuthorizationFailureCount());
    if (0 != data.getBackEndRequestQueueMax())
      m_be_queueField.setData(data.getBackEndRequestQueueMax());
    if (0 != data.getErrorThresholdCount())
      m_thresh_exceedField.setData(data.getErrorThresholdCount());
    if (0 != data.getErrorThresholdInterval())
      m_thresh_persistField.setData(data.getErrorThresholdInterval());

    // radio buttons
    m_thresh_exceedButton.setSelected(data.isErrorThresholdByCount());
    m_thresh_persistButton.setSelected(data.isErrorThresholdByInterval());
  }

/** @param data The OSRecipient object that will receive all the component 
  * settings of this dialog.
*/
  private void setDialogDataIntoRecipient(OSRecipient data)
  {
    // checkboxes
    data.setAppAuthorizationFailureEnabled(m_app_authBox.isSelected());
    data.setAppDesignErrorEnabled(m_app_designBox.isSelected());
    data.setAppValidationErrorEnabled(m_app_validBox.isSelected());
    data.setAppXmlErrorEnabled(m_app_xmlBox.isSelected());
    data.setAppHtmlErrorEnabled(m_app_htmlBox.isSelected());
    data.setAppResponseTimeEnabled(m_app_timeBox.isSelected());
    data.setAppRequestQueueLargeEnabled(m_app_queueBox.isSelected());
    
    data.setBackEndAuthorizationFailureEnabled(m_be_authBox.isSelected());
    data.setBackEndServerDownFailureEnabled(m_be_downBox.isSelected());
    data.setBackEndDataConversionErrorEnabled(m_be_dataBox.isSelected());
    data.setBackEndQueryFailureEnabled(m_be_queryBox.isSelected());
    data.setBackEndUpdateFailureEnabled(m_be_updateBox.isSelected());
    data.setBackEndRequestQueueLargeEnabled(m_be_queueBox.isSelected());
    data.setSendEnabled(!m_notify_disableBox.isSelected());

    // spin text fields
    data.setAppAuthorizationFailureCount(m_app_authField.getData().intValue());
    data.setAppResponseTimeMax(m_app_timeField.getData().intValue());
    data.setAppRequestQueueMax(m_app_queueField.getData().intValue());
    data.setBackEndAuthorizationFailureCount(m_be_authField.getData().intValue());
    data.setBackEndRequestQueueMax(m_be_queueField.getData().intValue());
    data.setErrorThresholdCount(m_thresh_exceedField.getData().intValue());
    data.setErrorThresholdInterval(m_thresh_persistField.getData().intValue());

    // radio buttons
    data.setErrorThresholdByCount(m_thresh_exceedButton.isSelected());
    data.setErrorThresholdByInterval(m_thresh_persistButton.isSelected());
  }

  
/** Creates the panel labeled &quot;Application events or errors&quot;.
*/
  private JPanel createAppEventPanel()
  {
    // new ImageIcon(getClass().getResource(getResources().getString("dupeimage")))
    m_app_authBox = new JCheckBox(getResources().getString("authfailed")); 
    m_app_authBox.setBorder(null);
    m_app_authBox.addActionListener(new ActionListener()
    {
      // switches Authorization failure Field on/off
      public void actionPerformed(ActionEvent e)
      {
        if (1 < m_selectedList.size())
        {
          m_app_authBox.setIcon(null);
          m_app_authBox.setPressedIcon(null);

          for (int i = 0; i < m_selectedList.size(); i++)
          {
            ((OSRecipient)m_selectedList.elementAt(i)).
            setAppAuthorizationFailureEnabled(m_app_authBox.isSelected());
          }
        }
        
        if (m_app_authBox.isSelected())
        {
            m_app_failureLabel.setEnabled(true);
            m_app_ipLabel.setEnabled(true);
            m_app_authField.setEnabled(true);
        }
        else
        {
            m_app_failureLabel.setEnabled(false);
            m_app_ipLabel.setEnabled(false);
            m_app_authField.setEnabled(false);
        }
      }
    });
    
    JPanel authBoxPanel = new JPanel();
    authBoxPanel.add(m_app_authBox);
    
    m_app_designBox = new JCheckBox(getResources().getString("designerror"));
    m_app_designBox.setBorder(null);
    m_app_designBox.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        if (1 == m_selectedList.size())
          return;
          
        m_app_designBox.setIcon(null);
        m_app_designBox.setPressedIcon(null);

        for (int i = 0; i < m_selectedList.size(); i++)
        {
          ((OSRecipient)m_selectedList.elementAt(i)).
          setAppDesignErrorEnabled(m_app_designBox.isSelected());
        }
      }
    });
    m_app_validBox = new JCheckBox(getResources().getString("validerror"));
    m_app_validBox.setBorder(null);
    m_app_validBox.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        if (1 == m_selectedList.size())
          return;
          
        m_app_validBox.setIcon(null);
        m_app_validBox.setPressedIcon(null);

        for (int i = 0; i < m_selectedList.size(); i++)
        {
          ((OSRecipient)m_selectedList.elementAt(i)).
          setAppValidationErrorEnabled(m_app_validBox.isSelected());
        }
      }
    });
    m_app_xmlBox = new JCheckBox(getResources().getString("xmlerror"));
    m_app_xmlBox.setBorder(null);
    m_app_xmlBox.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        if (1 == m_selectedList.size())
          return;
          
        m_app_xmlBox.setIcon(null);
        m_app_xmlBox.setPressedIcon(null);

        for (int i = 0; i < m_selectedList.size(); i++)
        {
          ((OSRecipient)m_selectedList.elementAt(i)).
          setAppXmlErrorEnabled(m_app_xmlBox.isSelected());
        }
      }
    });
    m_app_htmlBox = new JCheckBox(getResources().getString("htmlerror"));
    m_app_htmlBox.setBorder(null);
    m_app_htmlBox.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        if (1 == m_selectedList.size())
          return;
          
        m_app_htmlBox.setIcon(null);
        m_app_htmlBox.setPressedIcon(null);

        for (int i = 0; i < m_selectedList.size(); i++)
        {
          ((OSRecipient)m_selectedList.elementAt(i)).
          setAppHtmlErrorEnabled(m_app_htmlBox.isSelected());
        }
      }
    });
    m_app_timeBox = new JCheckBox(getResources().getString("poortime"));
    m_app_timeBox.setBorder(null);
    m_app_timeBox.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        if (1 < m_selectedList.size())
        {
          m_app_timeBox.setIcon(null);
          m_app_timeBox.setPressedIcon(null);

          for (int i = 0; i < m_selectedList.size(); i++)
          {
            ((OSRecipient)m_selectedList.elementAt(i)).
            setAppResponseTimeEnabled(m_app_timeBox.isSelected());
          }
        }

        if (m_app_timeBox.isSelected())
        {
          m_app_timeField.setEnabled(true);
          m_app_mSecLabel.setEnabled(true);
        }
        else
        {
          m_app_timeField.setEnabled(false);
          m_app_mSecLabel.setEnabled(false);
        }
      }
    });
    
    m_app_queueBox = new JCheckBox(getResources().getString("largeuser"));
    m_app_queueBox.setBorder(null);
    m_app_queueBox.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        if (1 < m_selectedList.size())
        {
          m_app_queueBox.setIcon(null);
          m_app_queueBox.setPressedIcon(null);

          for (int i = 0; i < m_selectedList.size(); i++)
          {
            ((OSRecipient)m_selectedList.elementAt(i)).
            setAppRequestQueueLargeEnabled(m_app_queueBox.isSelected());
          }
        }

        if (m_app_queueBox.isSelected())
        {
          m_app_queueField.setEnabled(true);
          m_app_userLabel.setEnabled(true);
        }
        else
        {
          m_app_queueField.setEnabled(false);
          m_app_userLabel.setEnabled(false);
        }
      }
    });
    JPanel queueBoxPanel = new JPanel();
    queueBoxPanel.add(m_app_queueBox);

    m_app_authField = new UTSpinTextField(getResources().getString(SEND),
                                          new Integer(5),
                                          new Integer(1),
                                          new Integer(Integer.MAX_VALUE));
    m_app_authField.addChangeListener(new ChangeListener()
    {
      public void stateChanged(ChangeEvent e)
      {
        if (1 < m_selectedList.size())
        {
          int value = m_app_authField.getDefault();
          Integer valueObj = m_app_authField.getData();
          if (null != valueObj)
            value = valueObj.intValue();
          
          for (int i = 0; i < m_selectedList.size(); i++)
          {
            ((OSRecipient)m_selectedList.elementAt(i)).setAppAuthorizationFailureCount(value);
          }
        }
      }
    });

    m_app_timeField = new UTSpinTextField(getResources().getString(MAX),
                                          new Integer(15000),
                                          new Integer(1),
                                          new Integer(Integer.MAX_VALUE));
    m_app_timeField.addChangeListener(new ChangeListener()
    {
      public void stateChanged(ChangeEvent e)
      {
        if (1 < m_selectedList.size())
        {
          int value = m_app_timeField.getDefault();
          Integer valueObj = m_app_timeField.getData();
          if (null != valueObj)
            value = valueObj.intValue();
            
          for (int i = 0; i < m_selectedList.size(); i++)
          {
            ((OSRecipient)m_selectedList.elementAt(i)).setAppResponseTimeMax(value);
          }
        }
      }
    });

    m_app_queueField = new UTSpinTextField(getResources().getString(MAX),
                                          new Integer(50),
                                          new Integer(1),
                                          new Integer(Integer.MAX_VALUE));
    m_app_queueField.addChangeListener(new ChangeListener()
    {
      public void stateChanged(ChangeEvent e)
      {
        if (1 < m_selectedList.size())
        {
          int value = m_app_queueField.getDefault();
          Integer valueObj = m_app_queueField.getData();
          if (null != valueObj)
            value = valueObj.intValue();
            
          for (int i = 0; i < m_selectedList.size(); i++)
          {
            ((OSRecipient)m_selectedList.elementAt(i)).setAppRequestQueueMax(value);
          }
        }
      }
    });

    m_app_failureLabel = new JLabel(getResources().getString(FAILURE), SwingConstants.LEFT);
    m_app_failureLabel.setPreferredSize(SMALL_LABEL_SIZE);
    m_app_failureLabel.setMaximumSize(m_app_failureLabel.getPreferredSize());
    m_app_failureLabel.setMinimumSize(m_app_failureLabel.getPreferredSize());
    m_app_failureLabel.setBorder(null);
    
    m_app_ipLabel = new JLabel(getResources().getString(IP), SwingConstants.LEFT);
    m_app_ipLabel.setPreferredSize(SMALL_LABEL_SIZE);
    m_app_ipLabel.setMaximumSize(m_app_ipLabel.getPreferredSize());
    m_app_ipLabel.setMinimumSize(m_app_ipLabel.getPreferredSize());
    m_app_ipLabel.setBorder(null);

    m_app_mSecLabel = new JLabel(getResources().getString("millisec"));
    m_app_userLabel = new JLabel(getResources().getString("users"));

    // setting initial states
    m_app_failureLabel.setEnabled(false);
    m_app_ipLabel.setEnabled(false);
    m_app_authField.setEnabled(false);
    m_app_timeField.setEnabled(false);
    m_app_mSecLabel.setEnabled(false);
    m_app_queueField.setEnabled(false);
    m_app_userLabel.setEnabled(false);

    // setting up spinTextField panels
    JPanel ipPanel = new JPanel();
    ipPanel.setLayout(new BoxLayout(ipPanel, BoxLayout.Y_AXIS));
    ipPanel.add(m_app_failureLabel);
    ipPanel.add(m_app_ipLabel);
    ipPanel.setSize(new Dimension(90, 40));
    
    JPanel authPanel = new JPanel();
    authPanel.setLayout(new BoxLayout(authPanel, BoxLayout.X_AXIS));
    authPanel.add(m_app_authField);
    authPanel.add(ipPanel);

    JPanel timePanel = new JPanel();
    timePanel.setLayout(new BoxLayout(timePanel, BoxLayout.X_AXIS));
    timePanel.add(m_app_timeField);
    timePanel.add(m_app_mSecLabel);

    JPanel queuePanel = new JPanel();
    queuePanel.setLayout(new BoxLayout(queuePanel, BoxLayout.X_AXIS));
    queuePanel.add(m_app_queueField);
    queuePanel.add(m_app_userLabel);

    JPanel centerPanel = new JPanel();
    centerPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
    centerPanel.setPreferredSize(new Dimension(150, 115));
    centerPanel.setMaximumSize(centerPanel.getPreferredSize());
    centerPanel.setMinimumSize(centerPanel.getPreferredSize());
    centerPanel.setBorder(null);
    centerPanel.add(m_app_designBox);
    centerPanel.add(m_app_validBox);
    centerPanel.add(m_app_xmlBox);
    centerPanel.add(m_app_htmlBox);
    centerPanel.add(m_app_timeBox);

    // setting up sub-panels
    JPanel appEventPanel = new JPanel();
    appEventPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
    ((FlowLayout)appEventPanel.getLayout()).setVgap(0);
    appEventPanel.add(authBoxPanel);
    appEventPanel.add(authPanel);
    appEventPanel.add(centerPanel);
    appEventPanel.add(timePanel);
    appEventPanel.add(queueBoxPanel);
    appEventPanel.add(queuePanel);
    appEventPanel.setPreferredSize(new Dimension(250, 315));
    appEventPanel.setMaximumSize(appEventPanel.getPreferredSize());
    appEventPanel.setMinimumSize(appEventPanel.getPreferredSize());
    appEventPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED),
                                             getResources().getString("apptitle")));
    
    return appEventPanel;
  }

/** Creates the panel labeled &quot;Back-end events or errors&quot;.
*/
  private JPanel createBEEventPanel()
  {
    // removed for later version
    /*  
    m_be_BEstuffBox = new PSComboBox();
    m_be_BEstuffBox.setPreferredSize(TEXTFIELD_SIZE);
    m_be_BEstuffBox.setMaximumSize(m_be_BEstuffBox.getPreferredSize());
    m_be_BEstuffBox.setMinimumSize(m_be_BEstuffBox.getPreferredSize());
    
    JLabel backendLabel = new JLabel(getResources().getString("backend"));
    backendLabel.setBorder(null);
    */
    
    // Creating the checkboxes    
    m_be_authBox = new JCheckBox(getResources().getString("authfailed"));
    m_be_authBox.setBorder(null);
    m_be_authBox.addActionListener(new ActionListener()
    {
      // switches Authorization failure Field on/off
      public void actionPerformed(ActionEvent e)
      {
        if (1 < m_selectedList.size())
        {
          m_be_authBox.setIcon(null);
          m_be_authBox.setPressedIcon(null);

          for (int i = 0; i < m_selectedList.size(); i++)
          {
            ((OSRecipient)m_selectedList.elementAt(i)).
            setBackEndAuthorizationFailureEnabled(m_be_authBox.isSelected());
          }
        }

        if (m_be_authBox.isSelected())
        {
          m_be_failureLabel.setEnabled(true);
          m_be_ipLabel.setEnabled(true);
          m_be_authField.setEnabled(true);
        }
        else
        {
          m_be_failureLabel.setEnabled(false);
          m_be_ipLabel.setEnabled(false);
          m_be_authField.setEnabled(false);
        }  
      }
    });

    JPanel authBoxPanel = new JPanel();
    authBoxPanel.add(m_be_authBox);
    
    m_be_downBox = new JCheckBox(getResources().getString("serverdown"));
    m_be_downBox.setBorder(null);
    m_be_downBox.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        if (1 == m_selectedList.size())
          return;
          
        m_be_downBox.setIcon(null);
        m_be_downBox.setPressedIcon(null);

        for (int i = 0; i < m_selectedList.size(); i++)
        {
          ((OSRecipient)m_selectedList.elementAt(i)).
          setBackEndServerDownFailureEnabled(m_be_downBox.isSelected());
        }
      }
    });
    m_be_dataBox = new JCheckBox(getResources().getString("dataerror"));
    m_be_dataBox.setBorder(null);
    m_be_dataBox.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        if (1 == m_selectedList.size())
          return;

        m_be_dataBox.setIcon(null);
        m_be_dataBox.setPressedIcon(null);

        for (int i = 0; i < m_selectedList.size(); i++)
        {
          ((OSRecipient)m_selectedList.elementAt(i)).
          setBackEndDataConversionErrorEnabled(m_be_dataBox.isSelected());
        }
      }
    });
    m_be_queryBox = new JCheckBox(getResources().getString("queryerror"));
    m_be_queryBox.setBorder(null);
    m_be_queryBox.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        if (1 == m_selectedList.size())
          return;

        m_be_queryBox.setIcon(null);
        m_be_queryBox.setPressedIcon(null);

        for (int i = 0; i < m_selectedList.size(); i++)
        {
          ((OSRecipient)m_selectedList.elementAt(i)).
          setBackEndQueryFailureEnabled(m_be_queryBox.isSelected());
        }
      }
    });
    m_be_updateBox = new JCheckBox(getResources().getString("updateerror"));
    m_be_updateBox.setBorder(null);
    m_be_updateBox.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        if (1 == m_selectedList.size())
          return;
          
        m_be_updateBox.setIcon(null);
        m_be_updateBox.setPressedIcon(null);

        for (int i = 0; i < m_selectedList.size(); i++)
        {
          ((OSRecipient)m_selectedList.elementAt(i)).
          setBackEndUpdateFailureEnabled(m_be_updateBox.isSelected());
        }
      }
    });
    m_be_queueBox = new JCheckBox(getResources().getString("queueerror"));
    m_be_queueBox.setBorder(null);
    m_be_queueBox.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        if (1 < m_selectedList.size())
        {
          m_be_queueBox.setIcon(null);
          m_be_queueBox.setPressedIcon(null);

          for (int i = 0; i < m_selectedList.size(); i++)
          {
            ((OSRecipient)m_selectedList.elementAt(i)).
            setBackEndRequestQueueLargeEnabled(m_be_queueBox.isSelected());
          }
        }

        if (m_be_queueBox.isSelected())
        {
          m_be_queueField.setEnabled(true);
          m_be_requestLabel.setEnabled(true);
        }
        else
        {
          m_be_queueField.setEnabled(false);
          m_be_requestLabel.setEnabled(false);
        }
      }
    });    

    m_be_authField = new UTSpinTextField(getResources().getString(SEND),
                                     new Integer(5),
                                     new Integer(1),
                                     new Integer(Integer.MAX_VALUE));
    m_be_authField.addChangeListener(new ChangeListener()
    {
      public void stateChanged(ChangeEvent e)
      {
        if (1 < m_selectedList.size())
        {
          int value = m_be_authField.getDefault();
          Integer valueObj = m_be_authField.getData();
          if (null != valueObj)
            value = valueObj.intValue();
            
          for (int i = 0; i < m_selectedList.size(); i++)
          {
            ((OSRecipient)m_selectedList.elementAt(i)).setBackEndAuthorizationFailureCount(value);
          }
        }
      }
    });

    m_be_queueField = new UTSpinTextField(getResources().getString(MAX),
                                      new Integer(50),
                                      new Integer(1),
                                      new Integer(Integer.MAX_VALUE));
    m_be_queueField.addChangeListener(new ChangeListener()
    {
      public void stateChanged(ChangeEvent e)
      {
        if (1 < m_selectedList.size())
        {
          int value = m_be_queueField.getDefault();
          Integer valueObj = m_be_queueField.getData();
          if (null != valueObj)
            value = valueObj.intValue();
            
          for (int i = 0; i < m_selectedList.size(); i++)
          {
            ((OSRecipient)m_selectedList.elementAt(i)).setBackEndRequestQueueMax(value);
          }
        }
      }
    });


    m_be_failureLabel = new JLabel(getResources().getString(FAILURE), SwingConstants.LEFT);
    m_be_failureLabel.setPreferredSize(SMALL_LABEL_SIZE);
    m_be_failureLabel.setMaximumSize(m_be_failureLabel.getPreferredSize());
    m_be_failureLabel.setMinimumSize(m_be_failureLabel.getPreferredSize());
    m_be_failureLabel.setBorder(null);
    
    m_be_ipLabel = new JLabel(getResources().getString(IP), SwingConstants.LEFT);
    m_be_ipLabel.setPreferredSize(SMALL_LABEL_SIZE);
    m_be_ipLabel.setMaximumSize(m_be_ipLabel.getPreferredSize());
    m_be_ipLabel.setMinimumSize(m_be_ipLabel.getPreferredSize());
    m_be_ipLabel.setBorder(null);
    
    m_be_requestLabel = new JLabel(getResources().getString("request"));

    // setting initial states
    m_be_failureLabel.setEnabled(false);
    m_be_ipLabel.setEnabled(false);
    m_be_authField.setEnabled(false);
    m_be_queueField.setEnabled(false);
    m_be_requestLabel.setEnabled(false);

    // setting up panels
    /*
    JPanel backendPanel = new JPanel();
    backendPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
    ((FlowLayout)backendPanel.getLayout()).setVgap(0);
    backendPanel.add(backendLabel);
    backendPanel.add(m_be_BEstuffBox);
    backendPanel.setPreferredSize(new Dimension(190, 45));
    backendPanel.setMaximumSize(backendPanel.getPreferredSize());
    backendPanel.setMinimumSize(backendPanel.getPreferredSize());
    */
    
    JPanel ipPanel = new JPanel();
    ipPanel.setLayout(new BoxLayout(ipPanel, BoxLayout.Y_AXIS));
    ipPanel.add(m_be_failureLabel);
    ipPanel.add(m_be_ipLabel);
    ipPanel.setSize(new Dimension(90, 40));
    
    JPanel authPanel = new JPanel();
    authPanel.setLayout(new BoxLayout(authPanel, BoxLayout.X_AXIS));
    authPanel.add(m_be_authField);
    authPanel.add(ipPanel);

    JPanel queuePanel = new JPanel();
    queuePanel.setLayout(new BoxLayout(queuePanel, BoxLayout.X_AXIS));
    queuePanel.add(m_be_queueField);
    queuePanel.add(m_be_requestLabel);

    JPanel centerPanel = new JPanel();
    centerPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
    centerPanel.setPreferredSize(new Dimension(160, 115));
    centerPanel.setMaximumSize(centerPanel.getPreferredSize());
    centerPanel.setMinimumSize(centerPanel.getPreferredSize());
    centerPanel.setBorder(null);
    centerPanel.add(m_be_downBox);
    centerPanel.add(m_be_dataBox);
    centerPanel.add(m_be_queryBox);
    centerPanel.add(m_be_updateBox);
    centerPanel.add(m_be_queueBox);
    
    JPanel checkboxPanel = new JPanel();
    checkboxPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
    ((FlowLayout)checkboxPanel.getLayout()).setVgap(0);
    //checkboxPanel.add(backendPanel);
    checkboxPanel.add(authBoxPanel);
    checkboxPanel.add(authPanel);
    checkboxPanel.add(centerPanel);
    checkboxPanel.add(queuePanel);
    checkboxPanel.setPreferredSize(new Dimension(250, 315));
    checkboxPanel.setMaximumSize(checkboxPanel.getPreferredSize());
    checkboxPanel.setMinimumSize(checkboxPanel.getPreferredSize());
    checkboxPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED),
                                             getResources().getString("backendtitle")));
    
    return checkboxPanel;
  }


/** Creates the panel labeled &quot;Notification threshold&quot;.
*/
  private JPanel createThresholdPanel()
  {
    // initializing panel members
    m_thresh_exceedButton = new JRadioButton(getResources().getString("errorexceed"), true);
    m_thresh_exceedButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {        
        if (1 < m_selectedList.size())
        {
          m_thresh_exceedButton.setIcon(null);
          m_thresh_exceedButton.setPressedIcon(null);
          m_thresh_persistButton.setIcon(null);
          m_thresh_persistButton.setPressedIcon(null);

          for (int i = 0; i < m_selectedList.size(); i++)
          {
            ((OSRecipient)m_selectedList.elementAt(i)).
            setErrorThresholdByCount(m_thresh_exceedButton.isSelected());
            ((OSRecipient)m_selectedList.elementAt(i)).
            setErrorThresholdByInterval(!m_thresh_exceedButton.isSelected());
          }
        }

        m_thresh_persistField.setEnabled(false);
        m_thresh_minLabel.setEnabled(false);

        m_thresh_exceedField.setEnabled(true);
      }
    }); 
    m_thresh_persistButton = new JRadioButton(getResources().getString("errorpersist"), false);
    m_thresh_persistButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        if (1 < m_selectedList.size())
        {
          m_thresh_persistButton.setIcon(null);
          m_thresh_persistButton.setPressedIcon(null);
          m_thresh_exceedButton.setIcon(null);
          m_thresh_exceedButton.setPressedIcon(null);

          for (int i = 0; i < m_selectedList.size(); i++)
          {
            ((OSRecipient)m_selectedList.elementAt(i)).
            setErrorThresholdByInterval(m_thresh_persistButton.isSelected());
            ((OSRecipient)m_selectedList.elementAt(i)).
            setErrorThresholdByCount(!m_thresh_persistButton.isSelected());
          }
        }

        m_thresh_exceedField.setEnabled(false);

        m_thresh_persistField.setEnabled(true);
        m_thresh_minLabel.setEnabled(true);
      }
    });
    
    m_thresh_group = new ButtonGroup();
    m_thresh_group.add(m_thresh_exceedButton);
    m_thresh_group.add(m_thresh_persistButton);
    

    m_thresh_exceedField = new UTSpinTextField( "",
                                                new Integer(50),
                                                new Integer(1),
                                                new Integer(100) );
    m_thresh_exceedField.addChangeListener(new ChangeListener()
    {
      public void stateChanged(ChangeEvent e)
      {
        if (1 < m_selectedList.size())
        {
          int value = m_thresh_exceedField.getDefault();
          Integer valueObj = m_thresh_exceedField.getData();
          if (null != valueObj)
            value = valueObj.intValue();
            
          for (int i = 0; i < m_selectedList.size(); i++)
          {
            ((OSRecipient)m_selectedList.elementAt(i)).setErrorThresholdCount(value);
          }
        }
      }
    });
                                                
    m_thresh_persistField = new UTSpinTextField( "",
                                                new Integer(50),
                                                new Integer(1),
                                                new Integer(100) );
    m_thresh_persistField.addChangeListener(new ChangeListener()
    {
      public void stateChanged(ChangeEvent e)
      {
        if (1 < m_selectedList.size())
        {
          int value = m_thresh_persistField.getDefault();
          Integer valueObj = m_thresh_persistField.getData();
          if (null != valueObj)
            value = valueObj.intValue();
            
          for (int i = 0; i < m_selectedList.size(); i++)
          {
            ((OSRecipient)m_selectedList.elementAt(i)).setErrorThresholdInterval(value);
          }
        }
      }
    });

    m_thresh_minLabel = new JLabel(getResources().getString("minutes"));

    // setting initial states
    m_thresh_persistField.setEnabled(false);
    m_thresh_minLabel.setEnabled(false);                                            

    JPanel minPanel = new JPanel();
    minPanel.setLayout(new BoxLayout(minPanel, BoxLayout.X_AXIS));
    minPanel.add(m_thresh_persistField);
    minPanel.add(m_thresh_minLabel);

    JPanel thresholdPanel = new JPanel();
    thresholdPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
    ((FlowLayout)thresholdPanel.getLayout()).setVgap(0);
    thresholdPanel.add(m_thresh_exceedButton);
    thresholdPanel.add(m_thresh_exceedField);
    thresholdPanel.add(m_thresh_persistButton);
    thresholdPanel.add(minPanel);
    thresholdPanel.setPreferredSize(new Dimension(200, 155));
    thresholdPanel.setMaximumSize(thresholdPanel.getPreferredSize());
    thresholdPanel.setMinimumSize(thresholdPanel.getPreferredSize());
    thresholdPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED),
                                              getResources().getString("threshold")));

    return thresholdPanel;
  }


/** Creates the panel containing the mailing list and disable notification
  * checkbox.
*/
  private JPanel createNotifyListPanel()
  {
    m_notify_disableBox = new JCheckBox(getResources().getString("disable"));
    m_notify_disableBox.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        if (1 == m_selectedList.size())
          return;
          
        m_notify_disableBox.setIcon(null);
        m_notify_disableBox.setPressedIcon(null);

        for (int i = 0; i < m_selectedList.size(); i++)
        {
          ((OSRecipient)m_selectedList.elementAt(i)).
          setSendEnabled(m_notify_disableBox.isSelected());
        }
      }
    });

    JPanel boxPanel = new JPanel();
    boxPanel.setLayout(new BoxLayout(boxPanel, BoxLayout.X_AXIS));
    boxPanel.add(m_notify_disableBox);
    boxPanel.add(Box.createHorizontalGlue());

    m_notifyList = new EditableListBox();
    m_notifyList.setTitle(getResources().getString("tolist"));
    m_notifyList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    m_notifyList.getRightButton().addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        m_notifyList.deleteRows();
      }
    });
    m_notifyList.addListSelectionListener(new ListSelectionListener()
    {
      public void valueChanged(ListSelectionEvent e)
      {
        NotifierPropertyDialog.this.onValueChanged(e);
      }
    });
    m_notifyList.setDataExchange(new IDataExchange()
    {
      public Object createNewInstance()
      {
        OSRecipient newRecipient = null;
        try
        {
          newRecipient = new OSRecipient();
          // must use space here as a filler. This method cannot take a null or
          // an empty string.  Will let EditableListBox's editor strip out the
          // white space for us.
          newRecipient.setName(" ");
          // default values
          newRecipient.setAppAuthorizationFailureCount(5);
          newRecipient.setAppResponseTimeMax(15000);
          newRecipient.setAppRequestQueueMax(50);
          newRecipient.setBackEndAuthorizationFailureCount(5);
          newRecipient.setBackEndRequestQueueMax(50);
          newRecipient.setErrorThresholdCount(50);
          newRecipient.setErrorThresholdInterval(50);
        }
        catch (Exception e)
        {
          e.printStackTrace();
        }
        return newRecipient;
      }
    });
    
    // setting up panel
    JPanel notifyPanel = new JPanel();
    notifyPanel.setLayout(new BoxLayout(notifyPanel, BoxLayout.Y_AXIS));
    notifyPanel.add(m_notifyList);
    notifyPanel.add(boxPanel);
    notifyPanel.setBorder(new EmptyBorder(6,10,6,6));
    
    return notifyPanel;
  }


/** Creates the panel on the top of the dialog containing the mailserver name,
  * from TextField, and the command buttons panel.
*/  
  private JPanel createTopPanel()
  {
    //TODOph: Use the mail provider catalog
      String[] array = { "SMTP" };
    m_smtpBox = new PSComboBox(array);
    m_smtpBox.setPreferredSize(new Dimension(80, 22));
    m_smtpBox.setMaximumSize(m_smtpBox.getPreferredSize());
    m_smtpBox.setMinimumSize(m_smtpBox.getPreferredSize());
    m_smtpBox.setSelectedIndex(0); // default setting

    // making Mail Server label and textField
    m_mailServerField = new UTFixedTextField("", TEXTFIELD_SIZE);
    m_mailServerLabel = new JLabel(getResources().getString("mailserver"), SwingConstants.RIGHT);
    m_mailServerLabel.setPreferredSize(LABEL_SIZE);
  
    // making From label and textField
    m_fromField = new UTFixedTextField("", TEXTFIELD_SIZE);
    m_fromLabel = new JLabel(getResources().getString("from"), SwingConstants.RIGHT);
    m_fromLabel.setPreferredSize(LABEL_SIZE);
    
    JPanel mailPanel = new JPanel();
    mailPanel.setLayout(new BoxLayout(mailPanel, BoxLayout.X_AXIS));
    mailPanel.add(m_mailServerLabel);
    mailPanel.add(Box.createHorizontalStrut(4));
    mailPanel.add(m_mailServerField);
    mailPanel.add(Box.createHorizontalStrut(6));
    mailPanel.add(m_smtpBox);
    mailPanel.add(Box.createHorizontalGlue());

    JPanel fromPanel = new JPanel();
    fromPanel.setLayout(new BoxLayout(fromPanel, BoxLayout.X_AXIS));
    fromPanel.add(m_fromLabel);
    fromPanel.add(Box.createHorizontalStrut(4));
    fromPanel.add(m_fromField);
    fromPanel.add(Box.createHorizontalGlue());

    // putting mailPanel and fromPanel together
    JPanel leftPanel = new JPanel();
    leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
    leftPanel.add(mailPanel);
    leftPanel.add(Box.createVerticalStrut(4));
    leftPanel.add(fromPanel);

    // putting the whole top panel together    
    JPanel topPanel = new JPanel();
    topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
    topPanel.add(leftPanel);
    topPanel.add(Box.createHorizontalStrut(11));
    topPanel.add(createCommandPanel());
    topPanel.setBorder(new EmptyBorder(6,0,0,0));

    return topPanel;
  }


/** Creates the panel holding the command buttons.
*/  
  private JPanel createCommandPanel()
  {
    m_commandPanel = new UTStandardCommandPanel(this, "", SwingConstants.VERTICAL)
    {
      public void onOk()
      {
        NotifierPropertyDialog.this.onOk();
      }
    };

    return m_commandPanel;
  }


/** Basic initialization for all the sub-panels within this dialog.
*/
  private void initDialog()
  {
    JPanel middlePanel = new JPanel();
    middlePanel.setLayout(new BoxLayout(middlePanel, BoxLayout.X_AXIS));
    middlePanel.add(createNotifyListPanel());
    middlePanel.add(createThresholdPanel());

    JPanel bottomPanel = new JPanel();
    bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
    bottomPanel.add(createAppEventPanel());
    bottomPanel.add(createBEEventPanel());

    getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
    getContentPane().add(createTopPanel());
    getContentPane().add(middlePanel);
    getContentPane().add(bottomPanel);

    getRootPane().setDefaultButton(m_commandPanel.getOkButton());

    setSize(508, 600);

    // adding validation components
      v_array[0] = new StringConstraint(" ~!@#$%^&*()+`=[]{}|;':,<>/?");
    c_array[0] = m_mailServerField;

    setValidationFramework(c_array, v_array);
  }
  

//
// MEMBER VARIABLES
//

  // items for appEventPanel
  private JCheckBox m_app_authBox, m_app_designBox, m_app_validBox,
                    m_app_xmlBox, m_app_htmlBox, m_app_timeBox,
                    m_app_queueBox;
  private UTSpinTextField m_app_authField, m_app_timeField, m_app_queueField;
  private JLabel m_app_failureLabel, m_app_ipLabel, m_app_mSecLabel, 
                 m_app_userLabel;

  // items for beEventPanel
  // private PSComboBox m_be_BEstuffBox;
  private JCheckBox m_be_authBox, m_be_downBox, m_be_dataBox, m_be_queryBox,
                    m_be_updateBox, m_be_queueBox;
  private UTSpinTextField m_be_authField, m_be_queueField;
  private JLabel m_be_failureLabel, m_be_ipLabel, m_be_requestLabel;

  // items for thresholdPanel
  private JRadioButton m_thresh_exceedButton, m_thresh_persistButton;
  private UTSpinTextField m_thresh_exceedField, m_thresh_persistField;
  private ButtonGroup m_thresh_group;
  private JLabel m_thresh_minLabel;

  // items for notifyListPanel
  private EditableListBox m_notifyList;
  private JCheckBox m_notify_disableBox;

  // items for topPanel
  private UTFixedTextField m_mailServerField, m_fromField;
  private JLabel m_mailServerLabel, m_fromLabel;
  private PSComboBox m_smtpBox;
  private UTStandardCommandPanel m_commandPanel;

  // validation framework stuff
  private ValidationConstraint[] v_array = new ValidationConstraint[1];
  private Component[] c_array = new Component[1];

  // misc items
  private OSNotifier m_notifier;
  private Vector<Object> m_selectedList = new Vector<Object>(10);

  private static final Dimension TEXTFIELD_SIZE = new Dimension(180, 22);
  private static final Dimension LABEL_SIZE = new Dimension(80, 22);
  private static final Dimension SMALL_LABEL_SIZE = new Dimension(90, 12);
  private static final String MAX = "max";
  private static final String IP = "ip";
  private static final String FAILURE = "failure";
  private static final String SEND = "send";
  private static final ImageIcon ICON = ResourceHelper.getIcon(E2Designer.getResources(), "DupeIcon");
  private static final ImageIcon ICON_PRESSED = ResourceHelper.getIcon(E2Designer.getResources(), "DupePressedIcon");
  private static final ImageIcon BUTTON = ResourceHelper.getIcon(E2Designer.getResources(), "DupeButton");
  private static final ImageIcon BUTTON_PRESSED = ResourceHelper.getIcon(E2Designer.getResources(), "DupePressedButton");
  
}

