/******************************************************************************
 *
 * [ PSNameLabelDesc.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.controls;

import com.percussion.client.models.IPSCmsModel;
import com.percussion.workbench.ui.IPSUiConstants;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater;
import com.percussion.workbench.ui.editors.form.PSEditorBase;
import com.percussion.workbench.ui.editors.wizards.PSWizardBase;
import com.percussion.workbench.ui.editors.wizards.PSWizardPageBase;
import com.percussion.workbench.ui.util.PSControlInfo;
import com.percussion.workbench.ui.validators.IPSControlValueValidator;
import com.percussion.workbench.ui.validators.PSControlValidatorFactory;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * A composite that comprises of a label and text field for name, label and
 * description.
 * <p>
 * When used in a form layout, it should be set to take up the entire width
 * of its parent. Then set the label numerator accordingly to match the appropriate 
 * control placement.
 * </p>
 * <p>
 * <pre>
 * <b>Example as used in an editor:</b>
 *     <code>
 *     m_commonComp = new PSNameLabelDesc(comp, SWT.NONE, 
 *       PSMessages.getString("PSSlotEditorComposite.label.slotname"), 
 *       IPSUiConstants.EDITOR_LABEL_NUMERATOR,  
 *       PSNameLabelDesc.SHOW_ALL 
 *          | PSNameLabelDesc.NAME_READ_ONLY);
 *     final FormData formData = new FormData();
 *     formData.left = new FormAttachment(0, 0); // Attach to left side
 *     formData.right = new FormAttachment(100, 0); // Attach to right side
 *     formData.top = new FormAttachment(0, 20);
 *     m_commonComp.setLayoutData(formData_14);
 *     </code>
 * </pre>
 * </p> 
 *
 */
public class PSNameLabelDesc extends Composite 
   implements IPSUiConstants
{
   
   /**
    * Convenience ctor calls
    * PSNameLabelDesc(Composite, int, String, int, int, 60)
    */
   public PSNameLabelDesc(
      Composite parent, int style, 
      String namePrefix, int labelNum, int options)
   {
      this(parent, style, namePrefix, labelNum, options, 60);
   }
   
   /**
    * Convenience ctor calls
    * PSNameLabelDesc(
    *   Composite, int, String, int, int, 60, IPSDesignerObjectUpdater)
    */
   public PSNameLabelDesc(
      Composite parent, int style, 
      String namePrefix, int labelNum, int options, 
      IPSDesignerObjectUpdater editor)
   {
      this(parent, style, namePrefix, labelNum, options, 60, editor);
   }
   
   /**
    * Ctor
    * @param parent the parent <code>Composite</code>, cannot be 
    * <code>null</code>.
    * @param style the style options for this composite.
    * @param namePrefix the name prefix that will be concatenated to the 
    * beginning of the name label. Cannot be <code>null</code>, can be
    * empty.
    * @param labelNum the numerator to be used as the right form attachment
    * setting for all labels. Ignored if <code>LAYOUT_STACKED</code> or
    * <code>LAYOUT_SIDE</code>.
    * @param options option bits that tell the composite how and what to display.
    *        SHOW_ALL, SHOW_NAME, SHOW_LABEL, SHOW_MNEMONIC, NAME_READ_ONLY,
    *        LAYOUT_SIDE, LAYOUT_STACKED
    * @param descHeight the height of the description box
    * @param editor the editor or wizard page, if not <code>null</code> then
    * this composite will register its controls with the passed in editor or
    * wizard page.       
    */
   public PSNameLabelDesc(
      Composite parent, int style, 
      String namePrefix, int labelNum, int options,
      int descHeight, IPSDesignerObjectUpdater editor)
   {
      this(parent, style, namePrefix, labelNum, options, descHeight);
      if(editor != null)
         registerControls(editor);
   } 

  /**
   * Ctor
   * @param parent the parent <code>Composite</code>, cannot be 
   * <code>null</code>.
   * @param style the style options for this composite.
   * @param namePrefix the name prefix that will be concatenated to the 
   * beginning of the name label. Cannot be <code>null</code>, can be
   * empty.
   * @param labelNum the numerator to be used as the right form attachment
   * setting for all labels. Ignored if <code>LAYOUT_STACKED</code> or
   * <code>LAYOUT_SIDE</code>.
   * @param options option bits that tell the composite how and what to display.
   *        SHOW_ALL, SHOW_NAME, SHOW_LABEL, SHOW_MNEMONIC, NAME_READ_ONLY,
   *        LAYOUT_SIDE, LAYOUT_STACKED
   * @param descHeight the height of the description box       
   */
   public PSNameLabelDesc(
      Composite parent, int style, 
      String namePrefix, int labelNum, int options, int descHeight)
   {
      super(parent, style);
      setLayout(new FormLayout());
      Control lastControl = null;
            
      boolean nameReadOnly = (options & NAME_READ_ONLY) != 0;
      boolean showAll = (options & SHOW_ALL) != 0;
      boolean showName = (options & SHOW_NAME) != 0;
      boolean showLabel = (options & SHOW_LABEL) != 0;
      boolean showDesc = (options & SHOW_DESC) != 0;
      boolean showMnem = (options & SHOW_MNEMONIC) != 0;
      boolean stacked = (options & LAYOUT_STACKED) != 0;
      boolean sideLayout = (options & LAYOUT_SIDE) != 0;
      boolean prefixLabel = (options & LABEL_USES_NAME_PREFIX) != 0;
      
      int labelAlign = SWT.RIGHT;
      int labelTopOffset1 = LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET;
      int labelTopOffset2 = LABEL_VSPACE_OFFSET;
      
      if(stacked)
      {
         labelNum = 100;
         labelAlign = SWT.LEFT;
         labelTopOffset1 = 0;
         labelTopOffset2 = 5;
      }
      
      Label separator = null;
      // Need to create this early so we can attach to it 
      if(showAll || showName)
      {
         m_nameLabel = new Label(this, SWT.WRAP);
         m_nameLabel.setAlignment(labelAlign);
         Object[] args = new Object[]{namePrefix};
         m_nameLabel.setText(PSMessages.getString(
            "PSNameLabelDesc.label.name", args));  //$NON-NLS-1$
         if(nameReadOnly)
         {
            m_nameText = new Label(this, SWT.WRAP);
            m_nameText.setFont(JFaceResources.getBannerFont());
            m_nameLabel.setFont(JFaceResources.getBannerFont());
            // Remove mnemonic marker from name label
            m_nameLabel.setText(
               StringUtils.replace(m_nameLabel.getText(), "&", ""));            
         }
         else
         {
            m_nameText = new Text(this, SWT.BORDER);
            ((Text)m_nameText).setTextLimit(50);
            
         }
      }
      if(showAll || showLabel)
      {
         m_labelLabel = new Label(this, SWT.WRAP);
         m_labelText = new Text(this, SWT.BORDER);
         m_labelText.addFocusListener(new FocusAdapter()
         {
            @Override
            public void focusLost(@SuppressWarnings("unused") FocusEvent e)
            {
               if (StringUtils.isBlank(m_labelText.getText()))
               {
                  String label = "";
                  Control nameCtrl = getNameText();
                  if(nameCtrl instanceof Text)
                     label = ((Text)nameCtrl).getText();
                  else if (nameCtrl instanceof Label)
                     label = ((Label)nameCtrl).getText();
                  m_labelText.setText(label);
               }
            }
         });
         m_labelText.setTextLimit(50);         
      }
      if(showAll || showDesc)
      {
         m_descriptionLabel = new Label(this, SWT.WRAP); 
         m_descriptionText = new Text(
            this, SWT.V_SCROLL | SWT.MULTI | SWT.BORDER | SWT.WRAP);
         m_descriptionText.setTextLimit(255);
      }
      if(showAll || showName)
      {        
         final FormData formData_13 = new FormData();         
         if(!(nameReadOnly && stacked) && ! sideLayout)
            formData_13.right = new FormAttachment(labelNum, 0);         
         formData_13.top = new FormAttachment(0, 0);
         formData_13.left = new FormAttachment(0, 0);
         m_nameLabel.setLayoutData(formData_13);
         
      
         if(nameReadOnly)
         {            
            if(sideLayout)
            {
               separator = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
               final FormData formDataSep = new FormData();
               formDataSep.top = new FormAttachment(m_nameText, 5, SWT.BOTTOM);
               formDataSep.right = new FormAttachment(100, 0);
               formDataSep.left = new FormAttachment(0, 0);
               separator.setLayoutData(formDataSep);
               
            }
         }         
         lastControl = stacked ? m_nameText : m_nameLabel;
         
         
         final FormData formData_15 = new FormData();
         if(sideLayout)
         {
            formData_15.right = new FormAttachment(100, 0);
         }
         else
         {
            formData_15.right = new FormAttachment(100, 0);
         }
         if((stacked || sideLayout) && !nameReadOnly)
         {
            formData_15.top = 
               new FormAttachment(m_nameLabel, 0, SWT.BOTTOM);
            formData_15.left = 
               new FormAttachment(m_nameLabel, 0, SWT.LEFT);
         }         
         else
         {
            formData_15.top = 
               new FormAttachment(m_nameLabel, 0, SWT.TOP);
            formData_15.left = 
               new FormAttachment(m_nameLabel, LABEL_HSPACE_OFFSET, SWT.RIGHT);
         }         
         m_nameText.setLayoutData(formData_15);

         // Add modify listener to handle echoing the name into the
         // label field
         if((showAll || showLabel) && !nameReadOnly)
         {
            ((Text)m_nameText).addModifyListener(new ModifyListener()
               {
               
               @SuppressWarnings("synthetic-access")
               public void modifyText(ModifyEvent e)
               {
                  String labelText = m_labelText.getText();
                  String newText = ((Text)e.getSource()).getText();
                  if(m_lastNameValue.equals(labelText))
                  {
                     m_labelText.setText(newText);
                  }
                  m_lastNameValue = newText;
                  
               }
               
               });
         }
      }
      
      if((showAll ||showLabel) && showMnem && !stacked && !sideLayout)
      {
         m_mnemonicText = new Text(this, SWT.BORDER);
         m_mnemonicText.setTextLimit(1);
         
         final FormData formData_21 = new FormData();      
         if(lastControl == null)
         {
            formData_21.top = 
               new FormAttachment(0, 
                  LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET);
         }
         else
         {
            formData_21.top = 
               new FormAttachment(lastControl, 
                  LABEL_VSPACE_OFFSET - LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET,
                  SWT.BOTTOM);
         }
         
         formData_21.right = new FormAttachment(100, 0);
         formData_21.width = 10;
         m_mnemonicText.setLayoutData(formData_21);
         
         m_mnemonicLabel = new Label(this, SWT.WRAP);
         m_mnemonicLabel.setAlignment(labelAlign);
         final FormData formData20 = new FormData();
         formData20.right = 
            new FormAttachment(m_mnemonicText, -LABEL_HSPACE_OFFSET, SWT.LEFT);
         formData20.top = 
               new FormAttachment(m_mnemonicText, 
                  LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.TOP);
         
         m_mnemonicLabel.setLayoutData(formData20);
         m_mnemonicLabel.setText(
            PSMessages.getString(MNEMONIC_TEXT_KEY)); 
      }      
      
      if(showAll || showLabel)
      {                 
         m_labelLabel.setAlignment(labelAlign);
         final FormData formData = new FormData();      
         if(!sideLayout)
            formData.right = new FormAttachment(labelNum, 0);
         formData.left = new FormAttachment(0, 0);
         if(sideLayout && (showAll || showName))
         {
            formData.top = 
               new FormAttachment(separator, 5, SWT.BOTTOM);
         }
         else if(lastControl == null)
         {
            formData.top = 
               new FormAttachment(0, labelTopOffset1);
         }
         else
         {
            int pad = (lastControl instanceof Label) ? LABEL_VSPACE_PAD : 0;
            formData.top = 
               new FormAttachment(lastControl, 
                  labelTopOffset2 + pad, SWT.BOTTOM);
         }
         m_labelLabel.setLayoutData(formData);
         String label = PSMessages.getString(LABEL_TEXT_KEY); 
         if(prefixLabel)
         {
            label = namePrefix + " " + label.toLowerCase();
         }
         m_labelLabel.setText(label);
      
         
         lastControl = stacked ? m_labelText : m_labelLabel;
         
         final FormData formData_1 = new FormData();      
         if(sideLayout)
         {
            formData_1.top = new FormAttachment(m_labelLabel, 0, SWT.BOTTOM);
            formData_1.left = new FormAttachment(0, 0);
            formData_1.right = new FormAttachment(50, -20);
         }
         else if(stacked)
         {
            formData_1.top = 
               new FormAttachment(
                  m_labelLabel, 0,
                  SWT.BOTTOM);
            formData_1.left = 
               new FormAttachment(m_labelLabel, 0, SWT.LEFT);  
         }
         else
         {
            formData_1.top = 
               new FormAttachment(
                  m_labelLabel, -LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET,
                  SWT.TOP);
            formData_1.left = 
               new FormAttachment(m_labelLabel, LABEL_HSPACE_OFFSET, SWT.RIGHT);
         }
         if(showMnem && !stacked && !sideLayout)
         {
            formData_1.right = new FormAttachment(m_mnemonicLabel, -10,
               SWT.LEFT);
         }
         else if (!sideLayout)
         {
            formData_1.right = new FormAttachment(100, 0);
         }
         m_labelText.setLayoutData(formData_1);
      }   
      if(showAll || showDesc)
      {                 
         m_descriptionLabel.setAlignment(labelAlign);
         final FormData formData_2 = new FormData();         
         
         if(!sideLayout)
         {
            formData_2.right = new FormAttachment(labelNum, 0);
            formData_2.left = new FormAttachment(0, 0);
         }
         if(sideLayout)
         {
            if(showAll || showName)
            {
               formData_2.top = new FormAttachment(separator, 5, SWT.BOTTOM);
            }
            else if(!showName && showLabel)
            {
               formData_2.top = new FormAttachment(m_labelLabel, 0, SWT.TOP);
            }
            else
            {
               formData_2.top = new FormAttachment(0, 0);
            }
            formData_2.left = new FormAttachment(m_descriptionText, 0, SWT.LEFT);
         }
         else if(lastControl == null)
         {
            formData_2.top = 
               new FormAttachment(0, labelTopOffset1);
         }
         else
         {
            int pad = (lastControl instanceof Label) ? LABEL_VSPACE_PAD : 0;
            formData_2.top = 
               new FormAttachment(lastControl, labelTopOffset2 + pad, SWT.BOTTOM);
         }
         m_descriptionLabel.setLayoutData(formData_2);
         m_descriptionLabel.setText(PSMessages.getString(DESC_TEXT_KEY));       
         
         final FormData formData_3 = new FormData();     
         formData_3.height = descHeight;
         formData_3.right = new FormAttachment(100, 0);
         if(sideLayout)
         {
            formData_3.top = new FormAttachment(m_descriptionLabel, 0, SWT.BOTTOM);
            if(!showAll && !showLabel)
            {
               formData_3.left = new FormAttachment(0, 0);
            }
            else
            {
               formData_3.left = new FormAttachment(50, 0);
            }
         }
         else if(stacked)
         {
            formData_3.top = 
               new FormAttachment(
                  m_descriptionLabel, 
                  0,
                  SWT.BOTTOM);
            formData_3.left = 
               new FormAttachment(m_descriptionLabel, 0, SWT.LEFT);
         }
         else
         {
            formData_3.top = 
               new FormAttachment(
                  m_descriptionLabel, 
                  -LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET,
                  SWT.TOP);
            formData_3.left = 
               new FormAttachment(m_descriptionLabel, LABEL_HSPACE_OFFSET,
                  SWT.RIGHT);
         }
         m_descriptionText.setLayoutData(formData_3);
         
         
      }
      setTabOrder(nameReadOnly);
      //
   }
   
   /**
    * Sets the appropriate tab order
    * @param readonly
    */
   private void setTabOrder(boolean readonly)
   {
      List<Control> tabList = new ArrayList<Control>();
      if(!readonly && m_nameText != null)
         tabList.add(m_nameText);
      if(m_labelText != null)
         tabList.add(m_labelText);
      if(m_mnemonicText != null)
         tabList.add(m_mnemonicText);
      if(m_descriptionText != null)
         tabList.add(m_descriptionText);
      setTabList(tabList.toArray(new Control[]{}));
   }   
     
   /* 
    * @see org.eclipse.swt.widgets.Composite#setFocus()
    */
   @Override
   public boolean setFocus()
   {
      if(m_nameText != null && (m_nameText instanceof Text))
         return m_nameText.setFocus();        
      if(m_labelText != null)
         return m_labelText.setFocus();
      if(m_mnemonicText != null)
         return m_mnemonicText.setFocus();
      if(m_descriptionText != null)
         return m_descriptionText.setFocus();
      return false;
   }

   /**
    * @return Returns the descriptionText. May return
    * <code>null</code> if description is not set to show.
    */
   public Text getDescriptionText()
   {
      return m_descriptionText;
   }

   /**
    * @return Returns the labelText. May return
    * <code>null</code> if label is not set to show.
    */
   public Text getLabelText()
   {
      return m_labelText;
   }

   /**
    * @return Returns the nameText.  This will be a <code>Text</code>
    * control if in editable mode or a <code>Label</code> control if
    * in read-only mode. May return <code>null</code> if name is not set 
    * to show.
    */
   public Control getNameText()
   {
      return m_nameText;
   }   

   /**
    * @return Returns the nameLabel. May return <code>null</code> if name
    *         label is not set to show.
    */
   public Control getNameLabel()
   {
      return m_nameLabel;
   }

   /**
    * @return Returns the mnemonicText. May return <code>null</code> if
    *         mnemonic is not set to show.
    */
   public Control getMnemonicText()
   {
      return m_mnemonicText;
   }   

   /**
    * @return Returns the descriptionLabel. May return
    * <code>null</code> if description is not set to show.
    */
   public String getDescriptionLabelValue()
   {
      if(m_descriptionLabel == null)
         return null;
      return m_descriptionLabel.getText();
   }

   /**
    * @return Returns the labelLabel. May return
    * <code>null</code> if label is not set to show.
    */
   public String getLabelLabelValue()
   {
      if(m_labelLabel == null)
         return null;
      return m_labelLabel.getText();
   }

   /**
    * @return Returns the nameLabel. May return
    * <code>null</code> if name is not set to show.
    */
   public String getNameLabelValue()
   {
      if(m_nameLabel == null)
         return null;
      return m_nameLabel.getText();
   }

   @Override
   public void dispose()
   {
      super.dispose();
   }

   @Override
   protected void checkSubclass()
   {
   }   
  
   /**
    * Registers the visible controls with the passed in editor or wizard
    * page.
    * @param editor assumed not <code>null</code>
    */
   private void registerControls(IPSDesignerObjectUpdater editor)
   {      
      boolean isWizard = (editor instanceof PSWizardPageBase);
      if(isWizard)
      {
         PSWizardPageBase parent = (PSWizardPageBase)editor;
         if(m_nameText != null)
         {
            IPSCmsModel model = ((PSWizardBase)parent.getWizard()).getModel();
            parent.registerControl(
               NAME_TEXT_KEY,
               m_nameText,
               getNameTextValidators(model),
               PSControlInfo.TYPE_NAME);
         }
         if(m_labelText != null)
         {
            parent.registerControl(
               LABEL_TEXT_KEY,
               m_labelText,
               new IPSControlValueValidator[]{
                  getValidatorFactory().getRequiredValidator()
               });
         }
         if(m_mnemonicText != null)
         {
            parent.registerControl(
               MNEMONIC_TEXT_KEY,
               m_mnemonicText,
               null);
         }
         if(m_descriptionText != null)
         {
            parent.registerControl(
               DESC_TEXT_KEY,
               m_descriptionText,
               null);
         }
      }
      else
      {
         PSEditorBase parent = (PSEditorBase)editor;
        
         if(m_labelText != null)
         {
            parent.registerControl(
               LABEL_TEXT_KEY,
               m_labelText,
               new IPSControlValueValidator[]{
                  getValidatorFactory().getRequiredValidator()
               });
         }
         if(m_mnemonicText != null)
         {
            parent.registerControl(
               MNEMONIC_TEXT_KEY,
               m_mnemonicText,
               null);
         }
         if(m_descriptionText != null)
         {
            parent.registerControl(
               DESC_TEXT_KEY,
               m_descriptionText,
               null);
         }
      }
      
   }

   /**
    * Returns array of validators for the name field.
    * @param model the model passed in by the editor or wizard page this
    * control is used in.
    * Not <code>null</code>.
    * @return an array of validators. Can be <code>null</code> or empty if no
    * validations are needed for the name field.
    */
   protected IPSControlValueValidator[] getNameTextValidators(IPSCmsModel model)
   {
      if (model == null)
      {
         throw new IllegalArgumentException("Model should not be null");
      }
      return new IPSControlValueValidator[]{
         getValidatorFactory().getRequiredValidator(),
         getValidatorFactory().getDuplicateNameValidator(model),
         getValidatorFactory().getNoWhitespaceValidator(),
         getValidatorFactory().getIdValidator()};
   }

   /**
    * Current validator factory. 
    * @return never <code>null</code>.
    */
   protected PSControlValidatorFactory getValidatorFactory()
   {
      return PSControlValidatorFactory.getInstance();
   }
   
   private Label m_descriptionLabel;
   private Label m_labelLabel;
   private Label m_nameLabel;
   private Label m_mnemonicLabel;
   private Text m_descriptionText;
   private Text m_labelText;
   private Control m_nameText;
   private Text m_mnemonicText;
   private String m_lastNameValue = "";
   
   private static final int LABEL_VSPACE_PAD = 5;
   
   /**
    * Option to display field/labels for all of the fields except 
    * the mnemonic
    */
   public static final int SHOW_ALL = 1 << 1;
   
   /**
    * Option to display field/labels for the name field
    */
   public static final int SHOW_NAME = 1 << 2;
   
   /**
    * Option to display field/labels for the label field
    */
   public static final int SHOW_LABEL = 1 << 3;
   
   /**
    * Option to display field/labels for the description field
    */    
   public static final int SHOW_DESC = 1 << 4;
   
   /**
    * Option to display the name value field as read only.
    */
   public static final int NAME_READ_ONLY = 1 << 5;
   
   /**
    * Option to display field/labels for the mnemonic field
    * Will not display if <code>LAYOUT_STACKED</code> option
    * is set.
    */
   public static final int SHOW_MNEMONIC = 1 << 6;
   
   /**
    * Option to display a stacked layout, meaning the
    * Labels are on top of the fields instead of to the left.
    */
   public static final int LAYOUT_STACKED = 1 << 7;
   
   /**
    * Option to layout out the controls where the name and label are on 
    * the left side and the description is on the right. The
    * labels are stacked on top of the controls.
    */
   public static final int LAYOUT_SIDE = 1 << 8;
   
   /**
    * Option that makes the label's label use the name prefix passed
    * in.
    * <p>
    * <pre>
    * Example:
    * 
    *    If the name prefix is "Foo" then the label will be "Foo label:".
    *    
    *    If this option is not set then the label will just be "Label:"
    * </pre>
    * </p>
    */
   public static final int LABEL_USES_NAME_PREFIX = 1 << 9;
   
   /**
    * The key used to retrieve the label text for the label field
    * from the psmessages.properties file.
    */
   public static final String NAME_TEXT_KEY = "PSNameLabelDesc.label.name";
   
   /**
    * The key used to retrieve the label text for the label field
    * from the psmessages.properties file.
    */
   public static final String LABEL_TEXT_KEY = "PSNameLabelDesc.label.label";
   
   /**
    * The key used to retrieve the label text for the description field
    * from the psmessages.properties file.
    */
   public static final String DESC_TEXT_KEY = "PSNameLabelDesc.label.description";
   
   /**
    * The key used to retrieve the label text for the mnemonic field
    * from the psmessages.properties file.
    */
   public static final String MNEMONIC_TEXT_KEY = "PSNameLabelDesc.label.mnemonic";
   
  

}
