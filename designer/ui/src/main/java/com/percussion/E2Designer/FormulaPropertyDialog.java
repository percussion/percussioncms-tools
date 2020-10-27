/******************************************************************************
 *
 * [ FormulaPropertyDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer;

import com.percussion.design.objectstore.IPSReplacementValue;
import com.percussion.design.objectstore.PSExtensionParamValue;
import com.percussion.design.objectstore.PSTextLiteral;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSExtensionParamDef;
import com.percussion.util.PSCollection;
import com.percussion.UTComponents.UTFixedButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Iterator;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Vector;

/**
 * Editor to select formulas and define its parameters.
 */
public class FormulaPropertyDialog extends PSDialogAPI
{
   /**
    * Construct the dialog.
    */
   public FormulaPropertyDialog()
   {
   }

   /**
    * Construct the dialog.
    */
   public FormulaPropertyDialog(Frame fr, PSUdfSet set, 
         OSExtensionCall udf,
         OSBackendDatatank backendDatatank )
   {
      super( fr );
      if (null == set)
         throw new IllegalArgumentException("PSUdfSet may not be null");
      
      m_udfSet = set;
      m_udf = udf;
      m_backendDatatank = backendDatatank;
      initDialog();
   }

   /**
    * Construct the dialog.
    *
    * @param parent the owner of this dialog; may not be <code>null</code>.
    * @param set contains all the UDF definitions; may not be <code>null</code>.
    * @param udf the current UDF call setting and its parameters; this is the 
    * data set through this dialog.  May be <code>null</code> to use defaults.
    * @param backendDatatank the backend data for the cataloged items; may be
    * <code>null</code>.
    * 
    * @throws IllegalArgumentException if paret or set is <code>null</code>
    */
   public FormulaPropertyDialog(JDialog parent, PSUdfSet set, 
                                OSExtensionCall udf,
                                OSBackendDatatank backendDatatank )
   {
      super( parent );

      if (null == set)
         throw new IllegalArgumentException("PSUdfSet may not be null");
      
      m_udfSet = set;
      m_udf = udf;
      m_backendDatatank = backendDatatank;
      initDialog();
   }


   /**
    * Sets the object that will be edited by this dialog.
    * 
    * @param model object to be edited, not <code>null</code> and must be valid
    * 
    * @throws IllegalArgumentException if <code>model</code> is not a valid 
    * object for this dialog.
    * @see #onEdit(OSExtensionCall)
    */ 
   public void setData(Object model)
   {
      if (!myIsValidModel( model ))
         throw new IllegalArgumentException( "Cannot provide a invalid model" );
      onEdit( (OSExtensionCall) model);
   }


   /**
    * Checks to see if the specified object is editable by this dialog.
    * 
    * @return <code>true</code> if <code>model</code> instanceof <code>
    * OSExtensionCall</code>; <code>false</code> otherwise.
    */ 
   public boolean isValidModel(Object model)
   {
      return myIsValidModel( model );
   }


   /**
    * Checks to see if the specified object is editable by this dialog.  This
    * method is used by <code>setData</code> because it cannot be overriden.
    * (If <code>setData</code> called <code>isValidModel</code>, then if
    * a derived class called <code>super.setData</code>, the super class might
    * use the derived class' <code>isValidModel</code> instead -- which would
    * cause a false result if the derived class had different requirements than
    * the super class.)
    * 
    * @return <code>true</code> if <code>model</code> instanceof <code>
    * OSExtensionCall</code>; <code>false</code> otherwise.
    */ 
   private boolean myIsValidModel(Object model)
   {
      return (model instanceof OSExtensionCall);
   }


   // see interface for description
   public void reset()
   {
      m_udf = null;
      if (m_selector.getItemCount() > 0)
         m_selector.setSelectedIndex(0);
      fillParameters();
   }


   /**
    * @return An <code>OSExtensionCall</code> that reflects the state of the 
    * dialog or <code>null</code> if the dialog was cancelled.
    * @see #getUdfCall
    */ 
   public Object getData()
   {
      return getUdfCall();
   }


   /**
    * Returns the current UDF call.
    *
    * @return the UDF call
    */
   public OSExtensionCall getUdfCall()
   {
        return m_udf;
   }

   /**
    * Checks for if the cancel button has been hit.
    *
    * @return boolean <CODE>true</CODE> if cancel was hit
    */
   public boolean isCancelHit()
   {
      return m_bCancelHit;
   }

   /**
    * Create the dialogs view/edit panel.
    */
   //////////////////////////////////////////////////////////////////////////////
   public void refreshDialog( PSUdfSet set, OSExtensionCall udf,
                              OSBackendDatatank backendDatatank)
         throws IllegalArgumentException
   {
      m_udfSet = set;
      m_udf = udf;
      m_pageTank = null;
      m_backendDatatank = backendDatatank;
      m_selector.removeAllItems();
      initDialog();
   }


   /**
    * Create the dialogs view/edit panel.
    */
   //////////////////////////////////////////////////////////////////////////////
   public void refreshDialog( PSUdfSet set, OSExtensionCall udf,
                                OSPageDatatank pageTank )
         throws IllegalArgumentException
   {
      m_udfSet = set;
      m_udf = udf;
      m_backendDatatank = null;
      m_pageTank = pageTank;
      m_selector.removeAllItems();
      initDialog();
   }


   /**
    * Create the dialogs view/edit panel.
    */
   //////////////////////////////////////////////////////////////////////////////
   public void refreshDialog( PSUdfSet set )
         throws IllegalArgumentException
   {
      m_udfSet = set;
      m_udf = null;
      m_backendDatatank = null;
      m_pageTank = null;
      m_selector.removeAllItems();
      initDialog();
   }


   /**
    *   Add a new OK listener.
    *
    * @ listener the new listener
    */
   //////////////////////////////////////////////////////////////////////////////
   public void addOkListener(ActionListener listener)
   {
        m_okListeners.addElement(listener);
   }

   /**
    *   Remove the provided OK listener.
    *
    * @ listener the listener to be removed
    */
   //////////////////////////////////////////////////////////////////////////////
   public void removeOkListener(ActionListener listener)
   {
        m_okListeners.removeElement(listener);
   }

   /**
    * Inform all OK listeners that the OK button was pressed.
    */
   //////////////////////////////////////////////////////////////////////////////
   protected void fireOk()
   {
      ActionEvent event = new ActionEvent(this, 0, "OK");
      for (int i=0; i<m_okListeners.size(); i++)
          ((ActionListener) m_okListeners.elementAt(i)).actionPerformed(event);
   }

   /**
    *   Add a new Cancel listener.
    *
    * @ listener the new listener
    */
   //////////////////////////////////////////////////////////////////////////////
   public void addCancelListener(ActionListener listener)
   {
        m_cancelListeners.addElement(listener);
   }

   /**
    *   Remove the provided Cancel listener.
    *
    * @ listener the listener to be removed
    */
   //////////////////////////////////////////////////////////////////////////////
   public void removeCancelListener(ActionListener listener)
   {
        m_cancelListeners.removeElement(listener);
   }


   /**
    * A quick method to clear all items in the selector combobox.
    */
   public void clearSelector()
   {
      m_selector.removeAllItems();
   }


   /**
    * Start new edit session.
    *
    * @param call the extension to edit; may not be <code>null</code>.
    * @throws IllegalArgumentException if call is <code>null</code>.
    */
   public void onEdit(OSExtensionCall call)
   {
      if ( null == call )
         throw new IllegalArgumentException( "Parameter call may not be null" );

      m_bCancelHit = false;
      m_udf = call;

      // update the exit list
      m_exits = m_udfSet.getAllUdfs();
      clearSelector();
      updateSelector();

      // find the current exit and set its paramter definitions

      IPSExtensionDef currentExit = m_udf.getExtensionDef();
      final int exitLen = (m_exits == null ? 0 : m_exits.size());
      for (int i=0; i<exitLen; i++)
      {
         IPSExtensionDef exit = (IPSExtensionDef) m_exits.get(i);

         if ( exit.getRef().equals( currentExit.getRef()))
         {
            m_selector.setSelectedIndex(i);
            Iterator paramList = exit.getRuntimeParameterNames();
            Vector<IPSExtensionParamDef> vParams =
               new Vector<IPSExtensionParamDef>();
            while ( paramList.hasNext() )
            {
               String strParamName = (String) paramList.next();
               vParams.add( exit.getRuntimeParameter( strParamName ) );
            }
            m_parameterDefs = vParams.toArray();
            break;
         }
      }
      if(  m_parameterDefs != null &&  m_parameterDefs.length > 0 )
      {
         fillParameters();
      }
      else
      {
         m_parameters.clear();
      }
      if (!m_selector.isEnabled())
      {
         m_selector.setEnabled(true);
         m_createUdf.setEnabled(true);

      }
   }

   /**
    * Start new edit session.
    *
    * @param call   the UDF call to edit
    * @param enable The state that the combo box and its browse button.
    * <CODE>true</CODE> = enabled; <CODE>false</CODE> = disabled.
    */
   //////////////////////////////////////////////////////////////////////////////
   public void onEdit(OSExtensionCall call, boolean enable)
   {
      onEdit(call);
      m_selector.setEnabled(enable);
      m_createUdf.setEnabled(enable);
   }
   
   /**
    * Starts a new edit session for the supplied parameters.
    * 
    * @param call the UDF call to be edited, not <code>null</code>.
    * @param enableSelector <code>true</code> to enable the UDF selector, which
    *    allows the editor to change the currently selected UDF, 
    *    <code>false</code> otherwise.
    * @param showNew <code>true</code> to show the new button, 
    *    <code>false</code> otherwise. If the new button is visible, the
    *    editor is allowed to create a new user UDF.
    */
   public void onEdit(OSExtensionCall call, boolean enableSelector, 
      boolean showNew)
   {
      onEdit(call);
      m_selector.setEnabled(enableSelector);
      m_createUdf.setVisible(showNew);
   }


   /** Overrides PSDialog onOk() method implementation.
    */
   public void onOk()
   {
      try
      {
         if ( !m_parameters.stopEditing())
              return;
         IPSExtensionDef exit = findExit((String) m_selector.getSelectedItem());
         // create a new UDF call
         m_udf = new OSExtensionCall(exit, m_parameters.getParameters());
      }
      catch (Exception e)
      {
         JOptionPane.showMessageDialog(this, 
            Util.cropErrorMessage(e.getMessage()),
            E2Designer.getResources().getString("OpErrorTitle"),
            JOptionPane.ERROR_MESSAGE);

         e.printStackTrace();
      }

      fireOk();
      dispose();
   }


   /** Overrides PSDialog onCancel() method implementation.
    */
   public void onCancel()
   {
      m_udf = null; // clear in case dialog was reused
      m_bCancelHit = true;
      fireCancel();
      super.onCancel();
   }

   /**
    * Inform all Cancel listeners that the Cancel button was pressed.
    */
   //////////////////////////////////////////////////////////////////////////////
   protected void fireCancel()
   {
      ActionEvent event = new ActionEvent(this, 0, "Cancel");
      for (int i=0; i<m_cancelListeners.size(); i++)
          ((ActionListener) m_cancelListeners.elementAt(i)).actionPerformed(event);
   }

   /**
    * Find the exit for the current selection.
    *
    * @param name the name of current selected UDF
    * @return IPSExtensionDef the found exit of null
    */
   //////////////////////////////////////////////////////////////////////////////
   private IPSExtensionDef findExit(String name)
   {
        if (m_exits != null)
      {
           for (int i=0, n=m_exits.size(); i<n; i++)
           {
             IPSExtensionDef exit = (IPSExtensionDef) m_exits.get(i);
            String strParam = exit.getRef().getExtensionName();
            if (strParam.equals(name))
               return exit;
         }
      }

      return null;
   }

   /**
    * Perform action if the UDF selection changes.
    */
   //////////////////////////////////////////////////////////////////////////////
   private void onSelectionChanged()
   {
      IPSExtensionDef exit = findExit((String) m_selector.getSelectedItem());
      if (exit != null)
      {
         Vector<IPSExtensionParamDef> vParams =
               new Vector<IPSExtensionParamDef>();
         Iterator itParams = exit.getRuntimeParameterNames();
         while( itParams.hasNext() )
         {
            String strParamName = (String)itParams.next();
            vParams.add( exit.getRuntimeParameter( strParamName ) );
         }
         m_parameterDefs = vParams.toArray();
         if( m_parameterDefs != null && m_parameterDefs.length > 0 )
         {
             fillParameters();
         }
         else
         {
            m_parameters.clear();
         }
      }
   }
   /**
    * Create the function panel
    */
   //////////////////////////////////////////////////////////////////////////////
   private JPanel createFunctionPanel()
   {
      // update the description field
      m_selector.addItemListener(new ItemListener()
      {
         public void itemStateChanged(ItemEvent e)
         {
            // the selection changed, update parameter list
            if (e.getStateChange() == ItemEvent.SELECTED)
            {
                 onSelectionChanged();
            }
            m_selector.revalidate();
         }
      });

      // start the create new UDF editor
      m_createUdf.addActionListener(new ActionListener()
      {
          public void actionPerformed(ActionEvent event)
         {
            try
            {
               CreateUdfDialog d = new CreateUdfDialog(
                  FormulaPropertyDialog.this, m_udfSet,
                  E2Designer.getApp().getMainFrame());
               d.setVisible(true);

               // update the exits
               PSCollection newUdfs = d.getNewUdfs();
               for ( int i = 0; i < newUdfs.size(); i++ )
                  m_udfSet.addApplicationUdf( (IPSExtensionDef)newUdfs.get(i) );

               m_exits = m_udfSet.getAllUdfs();

               // add udfs to the selector if any
               updateSelector();
            }
            catch ( IllegalStateException e )
            { /* ignore, msg already displayed to user */ }
         }
      });

        JPanel p1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
      p1.add(m_selector);
      p1.add(m_createUdf);
      m_createUdf.setEnabled(false);


      JPanel p2 = new JPanel(new BorderLayout());
      p2.setBorder(new EmptyBorder(10, 10, 10, 10));
      p2.add(new JLabel(getResources().getString("function")),
                        BorderLayout.NORTH);
      p2.add(p1);

      JPanel panel = new JPanel(new BorderLayout());
      panel.add(p2, BorderLayout.CENTER);
      panel.add(m_commandPanel, BorderLayout.EAST);

      return panel;
   }

   /**
    * Create the dialogs view/edit panel.
    */
   //////////////////////////////////////////////////////////////////////////////
   private JPanel createViewPanel()
   {
      JPanel panel = new JPanel(new BorderLayout());
      panel.add(createFunctionPanel(), BorderLayout.NORTH);
      panel.add(m_parameters);

      return panel;
   }

   /**
    * Fill in parameters
    */
   //////////////////////////////////////////////////////////////////////////////
   private void fillParameters()
   {
      // fill in the parameters for the current selection
      m_parameters.clear();
      if (m_parameterDefs != null)
      {
         PSExtensionParamValue[] parameterVals = null;
         if (m_udf != null)
         {
            parameterVals = m_udf.getParamValues();

            ///////////////////////// testing code //////////////////////////
            // making the parameterVals array longer if the parameter definitions are
            // longer then the array
            if (parameterVals.length < m_parameterDefs.length)
            {
               PSExtensionParamValue[] tempVals = parameterVals;
               parameterVals = new PSExtensionParamValue[m_parameterDefs.length];
               for (int num = 0; num < tempVals.length; num++)
                  parameterVals[num] = tempVals[num];
            }
            /////////////////////////////////////////////////////////////////
         }

         for (int a=0; a<m_parameterDefs.length; a++)
            m_parameters.appendParameter("", "", "");

         for (int i=0; i<m_parameterDefs.length; i++)
         {
            IPSExtensionParamDef par = (IPSExtensionParamDef)m_parameterDefs[i];

            IPSReplacementValue value = null;
            if (parameterVals == null)
               value = new PSTextLiteral("");
            else
            {
               if (null == parameterVals[i])
                  value = new PSTextLiteral("");
               else
                  value = parameterVals[i].getValue();
            }

            m_parameters.setParameterAt(par.getName(), value, par.getDescription(), i);
         }

         m_parameters.getTableModel().setReadOnly( m_parameterDefs.length, -1 );
      }
   }

   /**
    * Initialize the dialogs GUI elements with its data.
    */
   //////////////////////////////////////////////////////////////////////////////
   private void initDialog()
   {
      //Set the windows look and feel.
      try
      {
         UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      }
      catch (Exception e)
      {
         //This should not happen as it comes from the swing package
         //If happens we can not set the look and feel but we can let the dialog come up
         e.printStackTrace();
      }

      try
      {
         // create value selector dialog cell editor
         ValueSelectorDialogHelper h = new ValueSelectorDialogHelper(
                                                              m_backendDatatank,
                                                              m_pageTank );
         m_valueDialog = new ValueSelectorDialog( this,
                                                  h.getDataTypes(),
                                                  null);
         m_valueEditor = new ValueSelectorCellEditor( m_valueDialog, null);
         // create parameter panel
         m_parameters = new UTParameterPanel(m_valueEditor, false);

         // get all UDF's to be listed
         m_exits = m_udfSet.getAllUdfs();

         if (m_exits != null)
         {
            // initialize combo box with all available UDF's and select the
            // current UDF from the list
            for (int i=0, n=m_exits.size(); i<n; i++)
            {
               IPSExtensionDef exit = (IPSExtensionDef) m_exits.get(i);
               m_selector.addItem(exit.getRef().getExtensionName());
            }

            if (m_udf == null)
            {
               if ( m_exits.size() != 0 )
               {
                  IPSExtensionDef exit = (IPSExtensionDef) m_exits.get(0);
                  Iterator itParams = exit.getRuntimeParameterNames();
                  Vector<IPSExtensionParamDef> vParams =
                        new Vector<IPSExtensionParamDef>();
                  while( itParams.hasNext() )
                  {
                     String strParamName = (String)itParams.next();
                     vParams.add( exit.getRuntimeParameter( strParamName ) );
                  }
                  m_parameterDefs = vParams.toArray();
                  m_selector.setSelectedIndex(0);
               }
            }
            else
            {
               // find the current exit and set its parameter definitions
               IPSExtensionDef currentExit = m_udf.getExtensionDef();
               for (int i=0; i<m_exits.size(); i++)
               {
                  IPSExtensionDef exit = (IPSExtensionDef) m_exits.get(i);
                  String strCurName = currentExit.getRef().getExtensionName();
                  String strName = exit.getRef().getExtensionName();
                  if (strName.equals(strCurName))
                  {
                     m_selector.setSelectedIndex(i);
                     Iterator itParams = exit.getRuntimeParameterNames();
                     Vector<IPSExtensionParamDef> vParams =
                           new Vector<IPSExtensionParamDef>();
                     while( itParams.hasNext() )
                     {
                        String strParamName = (String)itParams.next();
                        vParams.add( exit.getRuntimeParameter( strParamName ) );
                     }
                     m_parameterDefs = vParams.toArray();
                     break;
                  }
               }

               fillParameters();
            }
         }

         // append empty rows
         while (m_parameters.getRowCount() < 12)
            m_parameters.appendParameter("", "", "");

         // initialize selection
         onSelectionChanged();

         // initialize GUI
         m_commandPanel = new UTStandardCommandPanel(this, "", SwingConstants.VERTICAL)
         {
            // implement onOk action
            public void onOk()
            {
               FormulaPropertyDialog.this.onOk();
            }

            public void onCancel()
            {
               FormulaPropertyDialog.this.onCancel();
            }
         };

         /* this next line causes a Memory Leak for some reason, but not
          * in all dialogs where this same thing is done.  Don't know why.
          * Removing it fixes the leak, and the ok button still ends up as
          * the default, so there is no change for the user
          */
         //getRootPane().setDefaultButton(m_commandPanel.getOkButton());

         JPanel panel = new JPanel(new BorderLayout());
         panel.setBorder(new EmptyBorder(5, 5, 5, 5));
         panel.add(createViewPanel(), BorderLayout.CENTER);

         getContentPane().setLayout(new BorderLayout());
         getContentPane().add(panel, BorderLayout.CENTER);
         pack();
         /* Fix for Memory Leak:  Pack() was leaking a Notify on the window, and
          * this removes it without appearing to cause any problems
          */
         removeNotify();
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   /**
    * updates the selector items
    */
   private void updateSelector()
   {
      if (m_exits != null)
      {
         // if a new Udf exists, add it to the selector list
         for (int i=0, n=m_exits.size(); i<n; i++)
         {
            IPSExtensionDef exit = (IPSExtensionDef) m_exits.get(i);
            String exitName = exit.getRef().getExtensionName();
            m_selector.addItem( exitName );
         }
         m_selector.repaint();
      }
   }

   /**
    * Added for testing reasons only.
    */
   //////////////////////////////////////////////////////////////////////////////
   private ResourceBundle m_res = null;
   protected ResourceBundle getResources()
   {
      try
      {
         if (m_res == null)
             m_res = ResourceBundle.getBundle(getClass().getName() + "Resources",
                                                           Locale.getDefault() );
      }
      catch (MissingResourceException e)
      {
         System.out.println(e);
      }

       return m_res;
    }

   //////////////////////////////////////////////////////////////////////////////
   /**
    * the collection of udfs to be displayed
    */
   private PSCollection m_exits = null;
   /**
    * the standard command panel
    */
   private UTStandardCommandPanel m_commandPanel = null;
   /**
    * the function selector
    */
   private UTFixedComboBox m_selector = new UTFixedComboBox();
   /**
    * the create UDF editor
    */
   private JButton m_createUdf = new UTFixedButton((new ImageIcon(getClass().getResource(E2Designer.getResources().getString("gif_Browser")))), new Dimension(20, 20));
   /**
    * the function parameter panel
    */
   private UTParameterPanel m_parameters = null;
   private ValueSelectorDialog m_valueDialog = null;
   private ValueSelectorCellEditor m_valueEditor = null;
   /**
    * the current UDF call settings
    */
   OSExtensionCall m_udf = null;
   /**
    * the UDF set wrapper containing the UDF definitions
    */
   PSUdfSet m_udfSet = null;
   /**
    * One or neither of the following tanks will be valid at any given time.
    * It should never happen that both are valid.
    */
   OSBackendDatatank m_backendDatatank = null;
   OSPageDatatank m_pageTank = null;

   /**
    * the current UDF parameter definitions
    */
   Object[] m_parameterDefs = null;
   /**
    * the command listeners
    */
   protected transient Vector<ActionListener> m_okListeners =
         new Vector<ActionListener>();
   protected transient Vector<ActionListener> m_cancelListeners =
         new Vector<ActionListener>();
   /**
    * flag to indicate if the cancel button was hit.
    */
   private boolean m_bCancelHit = false;
}

