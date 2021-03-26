/* *****************************************************************************
 *
 * [ AdvancedCEPropertiesDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *******************************************************************************/

package com.percussion.E2Designer;

import com.percussion.design.objectstore.PSInputTranslations;
import com.percussion.design.objectstore.PSOutputTranslations;
import com.percussion.design.objectstore.PSValidationRules;
import com.percussion.design.objectstore.PSWorkflow;
import com.percussion.design.objectstore.PSWorkflowInfo;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSRequestPreProcessor;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.util.PSCollection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;

/**
 * This dialog is used to specify the default workflow, to allow or disallow
 * workflow entry for content items created with the editor and set other 
 * in-frequently accessed properties.
 */
public class AdvancedCEPropertiesDialog extends PSDialog
{
  
   /**
    * Constructs the dialog with all its controls.
    * 
    * @param data Container for all data that this dialog can process. Never 
    * <code>null</code>. The contracts identified in the description of the
    * params in this class are validated here.
    */
   public AdvancedCEPropertiesDialog(AdvancedDataTransfer data)
   {  
      if (null == data)
         throw new IllegalArgumentException("data cannot be null");
      m_dataContainer = data;
      
      List workflows = data.m_workflows;
      if (workflows == null || workflows.size() < 1)
         throw new IllegalArgumentException
            ("List of current workflows can not be empty");
      
      for(int i = 0; i < workflows.size(); i++)
         m_currentWF.addElement(workflows.get(i));
      
      Iterator exitWalker = CatalogServerExits.getCatalog(
         E2Designer.getDesignerConnection(),
         CatalogExtensionCatalogHandler.JAVA_EXTENSION_HANDLER_NAME, 
         false).iterator();
      while (exitWalker.hasNext())
      {
         IPSExtensionDef extensionDef = (IPSExtensionDef) exitWalker.next();
         
         if (extensionDef.implementsInterface(
            IPSRequestPreProcessor.class.getName()))
            m_preExitDefinitions.add(extensionDef);
            
         if (extensionDef.implementsInterface(
            IPSResultDocumentProcessor.class.getName()))
            m_postExitDefinitions.add(extensionDef);
      }
         
      if (data.m_inputTranslations == null)
         throw new IllegalArgumentException(
            "input translations cannot be null");
      m_inputTranslations = 
         (PSInputTranslations) data.m_inputTranslations.clone();
         
      if (data.m_outputTranslations == null)
         throw new IllegalArgumentException(
            "output translations cannot be null");
      m_outputTranslations = 
         (PSOutputTranslations) data.m_outputTranslations.clone();
         
      if (data.m_validationRules == null)
         throw new IllegalArgumentException(
            "validation rules cannot be null");
      m_validationRules = 
         (PSValidationRules) data.m_validationRules.clone();
         
      initDialog();
      initData();
   }

   /**
    * Worker method for the OK button action handler.
    * The default workflow and the workflow info is set here.
    */
   public void onOk()
   {
      if (!validateData())
         return;
         
      PSWorkflow defWf = (PSWorkflow) m_defWorkflow.getSelectedItem();
      if (null != defWf)
         m_dataContainer.m_wfId = defWf.getDbId();
      
      Object [] obj = null;
      if (!m_anyWF.isSelected())
      {
         obj = m_workflows.getSelectedValues();

         List workflow = new ArrayList();
         for (int i = 0; i < obj.length; i++)
            workflow.add(obj[i]);

         if (m_exceptWF.isSelected())
            m_dataContainer.m_wfInfo =
               new PSWorkflowInfo(PSWorkflowInfo.TYPE_EXCLUSIONARY, workflow);

         else if (m_onlyWF.isSelected())
            m_dataContainer.m_wfInfo =
               new PSWorkflowInfo(PSWorkflowInfo.TYPE_INCLUSIONARY, workflow);
         
      }
      else
         m_dataContainer.m_wfInfo = null;
         
      m_dataContainer.m_userSearchable = m_userSearchable.isSelected();
      m_dataContainer.m_supportRelatedContent = 
            m_relatedContentCheckBox.isSelected();
            
      m_dataContainer.m_inputTranslations = 
         m_inputTranslationsPanel.getConditionalExits();
      m_dataContainer.m_outputTranslations = 
         m_outputTranslationsPanel.getConditionalExits();
      m_dataContainer.m_validationRules = 
         m_validationRulesPanel.getConditionalExits();
      m_dataContainer.m_maxErrorsToStop = 
         m_validationRulesPanel.getMaxErrorsToStop();            
            
      setVisible(false);
      m_okPressed = true;
   }
   
   /**
    * Validates all data for this dialog.
    * 
    * @return <code>true</code> if everything is valid, <code>false</code>
    *    otherwise.
    */
   private boolean validateData()
   {
      boolean isValid = true;
      
      PSWorkflow defWf = (PSWorkflow) m_defWorkflow.getSelectedItem();
      Object[] obj = null;
      if (!m_anyWF.isSelected())
      {
         obj = m_workflows.getSelectedValues();

         if (!validateWFInfo(obj, defWf))
            isValid = false;
      }
      
      if (!m_inputTranslationsPanel.validateData())
         isValid = false;
         
      if (!m_outputTranslationsPanel.validateData())
         isValid = false;
         
      if (!m_validationRulesPanel.validateData())
         isValid = false;
         
      return isValid;
   }

   /**
    * Overrides PSDialog onCancel() method implementation.
    */
   public void onCancel()
   {
      dispose();
   }

   /**
    * After the caller regains control after launching this dialog, they 
    * can call this method to determine how the dialog was dismissed so they
    * know whether to process the edited data.
    * 
    * @return <code>true</code> if the OK button was pressed, <code>false
    * </code> otherwise.
    */
   public boolean onOkPressed()
   {
      return m_okPressed;
   }
   
   /* (non-Javadoc)
    * @see com.percussion.E2Designer.PSDialog#subclassHelpId(java.lang.String)
    */
   protected String subclassHelpId(String helpId)
   {
      String id =  super.subclassHelpId(helpId);
      int idx = m_tabbedPane.getSelectedIndex();
               
      return id + "." + ms_tabIndexKeys[idx];
      
   }

   /**
    * This class is used to transfer the data from the caller to/from
    * instances of this class. It is a simple container to reduce formal
    * parameter proliferation in the ctor.
    * <p>The ctor will perform all validation specified in the following
    * descriptions.
    */
   static class AdvancedDataTransfer
   {
      /**
       * Each entry is a <code>PSWorkflow</code>. Never <code>null</code>,
       * may be empty. Defaults to empty.
       */
      public List m_workflows = new ArrayList();
      
      /**
       * The id of the default workflow. Defaults to 
       * <code>PSWorkflow.NOT_ASSIGNED</code>.
       */
      public int m_wfId = PSWorkflow.NOT_ASSIGNED;
      
      /**
       * A group of specific workflows into which items may or may not enter, 
       * may be <code>null</code>. Defaults to <code>null</code>.
       */
      public PSWorkflowInfo m_wfInfo = null;
      
      /**
       * A flag indicating whether the content editor items should be 
       * indexed. Defaults to <code>true</code>. 
       */
      public boolean m_userSearchable = true;
      
      /**
       * A flag indicating whether this editor supports related content.
       * Defaults to <code>false</code>.
       */
      public boolean m_supportRelatedContent = true;
      
      /**
       * A collection of input translations ( <code>PSConditionalExit</code>
       * objects ), never <code>null</code>, may be empty.
       */
      public PSCollection m_inputTranslations = null;
      
      /**
       * A collection of output translations ( <code>PSConditionalExit</code>
       * objects ), never <code>null</code>, may be empty.
       */
      public PSCollection m_outputTranslations = null;
      
      /**
       * A collection of validation rules ( <code>PSConditionalExit</code>
       * objects ), never <code>null</code>, may be empty.
       */
      public PSCollection m_validationRules = null;
      
      /**
       * The maximal number of errors that can occur before an item validation
       * is stopped, -1 if undefined, > 0 otherwise.
       */
      public int m_maxErrorsToStop = -1;
   }

   /**
    * Validates selections in the list in respect to the selected radio button.
    * If the radio button that excludes selected workflows is selected and
    * the default workflow is selected as well, the error message pops-up.
    * If the radio button that includes selected workflows is selected and the
    * default workflow is not among selected workflows the error message pops-up.
    * Also, if either of them is selected, but no selection is made, the error
    * message pops-up.
    *
    * @param obj an array of selected objects to be validated.
    */
   private boolean validateWFInfo(Object [] obj, PSWorkflow defaultWf) 
   {
      boolean isValid = true;
      if(obj.length == 0)
      {
         JOptionPane.showMessageDialog(this, ms_res.getString("emptyWFInfo"),
            ms_res.getString("error"), JOptionPane.ERROR_MESSAGE);
         isValid = false;
      }
      else
      {
         boolean found = false;
         for(int i = 0; i < obj.length; i++)
         {
            if(obj [i] instanceof PSWorkflow)
            {
               PSWorkflow workflow = (PSWorkflow)obj[i];
               if(workflow.getName().equals(defaultWf.getName()))
               {
                  found = true;
                  break;
               }
            }
         }
         if(m_onlyWF.isSelected() && (!found))
         {
            JOptionPane.showMessageDialog(this, ms_res.getString("includeWF"),
               ms_res.getString("error"), JOptionPane.ERROR_MESSAGE);
            isValid = false;
         }
         else if (m_exceptWF.isSelected() && found)
         {
            JOptionPane.showMessageDialog(this, ms_res.getString("excludeWF"),
               ms_res.getString("error"), JOptionPane.ERROR_MESSAGE);
            isValid = false;
         }
      }

      return isValid;
   }
    
   /**
    * Initializes controls with data
    */
   private void initData()
   {
      for (int i = 0; i < m_currentWF.size(); i++)
         m_defWorkflow.addItem(m_currentWF.get(i));

      /*if there is a default workflow defined select it in the default workflow
       *combo box
       */
      if (m_dataContainer.m_wfId != PSWorkflow.NOT_ASSIGNED)
      {
         for(int i = 0; i < m_defWorkflow.getItemCount(); i++)
         {
            PSWorkflow temp = (PSWorkflow)m_defWorkflow.getItemAt(i);
            if(temp.getDbId() == m_dataContainer.m_wfId)
            {
               m_defWorkflow.setSelectedItem(temp);
               break;
            }
         }
      }
      
      /*if there is workflow info defined for the content editor
       */
      if (m_dataContainer.m_wfInfo != null)
      {
         String wfType = m_dataContainer.m_wfInfo.getType();
         if (wfType.equals(PSWorkflowInfo.TYPE_EXCLUSIONARY))
            m_exceptWF.setSelected(true);
         else if (wfType.equals(PSWorkflowInfo.TYPE_INCLUSIONARY))
            m_onlyWF.setSelected(true);

         //now get the list of the workflows
         Iterator iter = m_dataContainer.m_wfInfo.getValues();

         ArrayList list = new ArrayList();
         //get the indexes of the workflows to be selected
         while (iter.hasNext())
         {
            int workflowID = ((Integer)iter.next()).intValue();

            for (int i = 0; i < m_workflows.getModel().getSize(); i++)
            {
               PSWorkflow tempWF =
                  (PSWorkflow)m_workflows.getModel().getElementAt(i);
               if (tempWF.getDbId() == workflowID)
               {
                  Integer tempInt = new Integer(i);
                  list.add(tempInt);
               }
            }
         }

         //convert the array list to an array
         int [] indices = new int [list.size()];
         for (int i = 0; i < list.size(); i++)
         {
            int index = ((Integer)list.get(i)).intValue();
            indices [i] = index;
         }
         m_workflows.setSelectedIndices(indices);
      }
      
      m_userSearchable.setSelected(m_dataContainer.m_userSearchable);
      m_relatedContentCheckBox.setSelected(
            m_dataContainer.m_supportRelatedContent);
   }

   /**
    * Inner class to implement ItemListener interface for handling item change
    * events for the radio buttons.  As of now we will handle events regarding
    * 'Allow items to enter any workflow' radio button.
    */
   class ItemChangeListener implements ItemListener
   {
      public void itemStateChanged( ItemEvent e )
      {
         m_workflows.clearSelection();

         if(e.getSource() == m_anyWF )
         {
            if(m_anyWF.isSelected())               
               m_workflows.setEnabled(false);

            else
               m_workflows.setEnabled(true);
         }
         else
            m_workflows.setEnabled(true);
      }
   }
   
   /**
    * Initialzes the dialog framework.
    */
   private void initDialog()
   {
      ms_res = getResources();

      if (ms_res == null)
         throw new IllegalStateException("Can not find " + getResourceName());

      setTitle(ms_res.getString("title"));
      setResizable(true);

      m_tabbedPane = new JTabbedPane(JTabbedPane.TOP);
      m_tabbedPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
      m_tabbedPane.addTab(ms_res.getString("tab.general"), 
         createGeneralPanel());
      setMnemonicForTabbedPane(m_tabbedPane, "tab.general", 0);
      
      m_tabbedPane.addTab(ms_res.getString("tab.workflow"), 
         createWorkflowPanel());
      setMnemonicForTabbedPane(m_tabbedPane, "tab.workflow", 1);  
      
      m_inputTranslationsPanel = new PSConditionalExitPanel(this, 
         m_preExitDefinitions, m_inputTranslations, -1);
      m_tabbedPane.addTab(ms_res.getString("tab.inputtranslation"), 
         m_inputTranslationsPanel);
      setMnemonicForTabbedPane(m_tabbedPane, "tab.inputtranslation", 2); 
      
      m_outputTranslationsPanel = new PSConditionalExitPanel(this, 
         m_postExitDefinitions, m_outputTranslations, -1) ; 
      m_tabbedPane.addTab(ms_res.getString("tab.outputtranslation"), 
         m_outputTranslationsPanel);
      setMnemonicForTabbedPane(m_tabbedPane, "tab.outputtranslation", 3); 
      
      m_validationRulesPanel = new PSConditionalExitPanel(this, 
         m_postExitDefinitions, m_validationRules,
         m_dataContainer.m_maxErrorsToStop);
      m_tabbedPane.addTab(ms_res.getString("tab.validation"), 
         m_validationRulesPanel);
      setMnemonicForTabbedPane(m_tabbedPane, "tab.validation", 4); 
      
      JPanel panel = new JPanel(new BorderLayout());
      panel.add(m_tabbedPane, BorderLayout.CENTER);
      panel.add(createCommandPanel(), BorderLayout.SOUTH);
      
      getContentPane().add(panel);
      center();
      pack();
   }
      
   /**
    * @param resId the resource id from the bundle, prepend status and append mn
    * @return return the integer value of the char ( upper case )
    */
   private void setMnemonicForTabbedPane(JTabbedPane tabPane, String resId, 
                                      int tabIx)
   {
       char mnChar;
       int mnInt;
       String name = ms_res.getString(resId);
       mnChar = ms_res.getString(resId+".mn").charAt(0);
       mnInt = (int)(""+mnChar).toUpperCase().charAt(0);
       tabPane.setMnemonicAt(tabIx, mnInt);
       tabPane.setDisplayedMnemonicIndexAt(tabIx, name.indexOf(mnChar));
   }

   
   /**
    * Creates the panel used for the general tab.
    * 
    * @return the newly created general panel, never <code>null</code>.
    */
   private JPanel createGeneralPanel()
   {
      JPanel panel = new JPanel();
      
      Box all = new Box(BoxLayout.Y_AXIS);
      
      Box check = new Box(BoxLayout.X_AXIS);
      m_userSearchable = new JCheckBox(ms_res.getString(
         "userSearchableLabel"));
      if (FeatureSet.isFTSearchEnabled())
      {
         check.add(m_userSearchable);
         check.add(Box.createHorizontalGlue());
      }
      all.add(check);
      
      m_relatedContentCheckBox = new JCheckBox(ms_res.getString(
         "relatedContentLabel"));
      check = new Box(BoxLayout.X_AXIS);
      check.add(m_relatedContentCheckBox);
      check.add(Box.createHorizontalGlue());
      all.add(check);
      
      panel.add(all);
      return panel;
   }
   
   /**
    * Creates the panel used for the workflow tab.
    * 
    * @return the newly created workflow panel, never <code>null</code>.
    */
   private JPanel createWorkflowPanel()
   {
      JLabel wfLabel = new JLabel(ms_res.getString("defWF"), JLabel.LEFT);
      m_defWorkflow = new PSComboBox();

      JPanel defWF = new JPanel();
      defWF.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
      defWF.setLayout(new BoxLayout(defWF, BoxLayout.X_AXIS));
      defWF.add(wfLabel);
      defWF.add(Box.createHorizontalStrut(3));
      defWF.add(m_defWorkflow);
      defWF.add(Box.createHorizontalStrut(30));

      m_anyWF = new JRadioButton(ms_res.getString("anyWF"), true);
      m_anyWF.addItemListener(new ItemChangeListener());
      m_exceptWF = new JRadioButton(ms_res.getString("exceptWF"));
      m_onlyWF = new JRadioButton(ms_res.getString("onlyWF"));

      //group the buttons
      ButtonGroup group = new ButtonGroup();
      group.add(m_anyWF);
      group.add(m_exceptWF);
      group.add(m_onlyWF);

      JPanel radioPanel = new JPanel();
      radioPanel.setLayout(new BoxLayout(radioPanel, BoxLayout.Y_AXIS));
      radioPanel.setAlignmentY(LEFT_ALIGNMENT);
      radioPanel.add(m_anyWF);
      radioPanel.add(Box.createVerticalStrut(5));
      radioPanel.add(m_exceptWF);
      radioPanel.add(Box.createVerticalStrut(5));
      radioPanel.add(m_onlyWF);
      radioPanel.add(Box.createVerticalStrut(5));

      JPanel radio = new JPanel();
      radio.setLayout(new BoxLayout(radio, BoxLayout.X_AXIS));
      radio.add(radioPanel);

      JLabel wf = new JLabel(ms_res.getString("workflows"), JLabel.LEFT);

      m_workflows = new JList(m_currentWF);
      m_workflows.setEnabled(false);
      JScrollPane pane = new JScrollPane(m_workflows);
      pane.setPreferredSize(new Dimension(120, 80));
      Box box = Box.createHorizontalBox();
      box.add(wf);
      box.add(Box.createHorizontalStrut(3));
      box.add(pane);

      JPanel wfPane = new JPanel();
      wfPane.setLayout(new BoxLayout(wfPane, BoxLayout.X_AXIS));
      wfPane.add(Box.createHorizontalStrut(40));
      wfPane.add(box);
      wfPane.add(Box.createHorizontalStrut(80));

      JPanel groupPanel = new JPanel();
      groupPanel.setLayout(new BoxLayout(groupPanel, BoxLayout.Y_AXIS));
      groupPanel.setBorder(createGroupBorder(""));
      groupPanel.add(radio);
      groupPanel.add(Box.createVerticalStrut(5));
      groupPanel.add(wfPane);

      JPanel panel = new JPanel(new BorderLayout());
      panel.add(defWF, BorderLayout.NORTH);
      panel.add(groupPanel, BorderLayout.CENTER);
      
      return panel;
   }

   /**
    * Creates the command panel with OK, Cancel and Help buttons.
    *
    * @return the command panel, never <code>null</code>.
    */
   private JPanel createCommandPanel()
   {
      UTStandardCommandPanel commandPanel =
         new UTStandardCommandPanel(this, "", SwingConstants.HORIZONTAL)
      {
         public void onOk()
         {
            AdvancedCEPropertiesDialog.this.onOk();
         }

      };
      
      
      getRootPane().setDefaultButton(commandPanel.getOkButton());
      commandPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
      JPanel cmdPanel = new JPanel(new BorderLayout());
      cmdPanel.add(commandPanel, BorderLayout.EAST);
      return cmdPanel;
   }

   /**
    * Radio buttons, get initialized in <code>initDialog</code>
    */
   JRadioButton m_anyWF, m_exceptWF, m_onlyWF;

   /**
    * A combo box that will hold workflows, gets initialized in
    * <code>initDialog</code>.
    */
   PSComboBox m_defWorkflow = null;

    /**A list that will display all current workflows,
     * gets initialized in <code>initDialog</code>, might be empty.
     */
   JList m_workflows = null;

   /**
    * A list of current workflows, never <code>null</code> might be empty.
    */
   Vector m_currentWF = new Vector();

   /**
    * The check box for selecting to support related content or not. 
    * Initialized in <code>initDialog</code>.
    */
   private JCheckBox m_relatedContentCheckBox = null;

   /**
    * The check box for controlling searchability at the content editor level. 
    * Initialized in <code>initDialog</code>.
    */
   private JCheckBox m_userSearchable = null;

   /**
    * The container for all properties editable via this dialog. The data in
    * this object is somewhat unrelated. Set in <code>ctor</code>, then never
    * <code>null</code>.
    */
   private AdvancedDataTransfer m_dataContainer = null;
   
   /**
    * A collection with all pre-exits available from the connected server.
    * Initialized in constructor, never <code>null</code>, may be empty and
    * is never changed after that.
    */
   private Collection m_preExitDefinitions = new ArrayList();
   
   /**
    * A collection with all post-exits available from the connected server.
    * Initialized in constructor, never <code>null</code>, may be empty and
    * is never changed after that.
    */
   private Collection m_postExitDefinitions = new ArrayList();
   
   /**
    * The panel handling input translations, initialized in 
    * {@link initDialog()}, never <code>null</code> or changed after that.
    */
   private PSConditionalExitPanel m_inputTranslationsPanel = null;
   
   /**
    * The panel handling output translations, initialized in 
    * {@link initDialog()}, never <code>null</code> or changed after that.
    */
   private PSConditionalExitPanel m_outputTranslationsPanel = null;
   
   /**
    * The panel handling validation rules, initialized in 
    * {@link initDialog()}, never <code>null</code> or changed after that.
    */
   private PSConditionalExitPanel m_validationRulesPanel = null;
   
   /**
    * All input translations, initialized in constructor, never 
    * <code>null</code>, may be empty after that.
    */
   private PSCollection m_inputTranslations = null;
   
   /**
    * All output translations, initialized in constructor, never 
    * <code>null</code>, may be empty after that.
    */
   private PSCollection m_outputTranslations = null;
   
   /**
    * All validation rules, initialized in constructor, never 
    * <code>null</code>, may be empty after that.
    */
   private PSCollection m_validationRules = null;
   
   /**
    * The tabbed pane used for this dialog. Initialized in
    * {@link #initDialog()}, never <code>null</code> after that.
    */
   private JTabbedPane m_tabbedPane = null;
   
   /**
    * A flag to indicate that the dialog was dismissed using the OK button.
    * Defaults to <code>false</code>. Set to <code>true</code> at the end of 
    * the <code>onOk</code> method.
    */
   private boolean m_okPressed = false;
   
    /**
     * Dialog resource strings, initialized in the <code>initDialog</code>
     */
   private static ResourceBundle ms_res = null;
   
   /**
    * Array of tab index keys that will be used for the help
    * topic mappings. Never <code>null</code> or empty.
    */
   private static String[] ms_tabIndexKeys = new String[]
      {
        "general",
        "workflow",
        "item_input_translation",
        "item_output_translation",
        "item_validation"
      };
}
