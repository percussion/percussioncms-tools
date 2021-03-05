/******************************************************************************
 *
 * [ PSRadioAndCheckBoxComposite.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.controls;

import com.percussion.workbench.ui.IPSUiConstants;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * A composite that can display either radio buttons or check boxes in either a
 * vertical or horizontal layout. A nested control can be added to a button if
 * using the vertical layout. Nested controls are not supported for horiontal
 * layout. The control can also have a label above the buttons and a separator.
 * <p>
 * <pre>
 *    <b>
 *    The folloing is an example of how to created radio button composite complete
 *    with a nested radio button composite control:
 *    </b>
 *    <code>
 *    // Create the main radio button composite
 *    PSRadioAndCheckBoxComposite radioComp = 
 *       new PSRadioAndCheckBoxComposite(
 *          composite, "Test", SWT.SEPARATOR | SWT.VERTICAL | SWT.RADIO);
 *    final FormData formData_1 = new FormData();
 *    formData_1.right = new FormAttachment(0, 395);
 *    formData_1.left = new FormAttachment(0, 0);
 *    radioComp.setLayoutData(formData_1);
 *     
 *    // Create another radio button composite that will be nested.
 *    // Note: the parent passed in is the control it will be nested
 *    // in.
 *    PSRadioAndCheckBoxComposite radioCompSub = 
 *       new PSRadioAndCheckBoxComposite(
 *          radioComp, "Test", SWT.SEPARATOR | SWT.VERTICAL | SWT.RADIO);
 *    // Add some simple entries to the nested control
 *    radioCompSub.addEntry("Foo");
 *    radioCompSub.addEntry("Bar");
 *    radioCompSub.addEntry("Dog");
 *    // Call layoutControls() on the nested control
 *    // so all controls get positioned
 *    radioCompSub.layoutControls();
 *    // Now we add some simple entries to the parent radio button control
 *    radioComp.addEntry("Foo");
 *    radioComp.addEntry("Bar");
 *    // Add the nested radio button control
 *    radioComp.addEntry("Subcomponent","", radioCompSub);
 *    // Create some values for a combo control
 *    List<String> values = new ArrayList<String>();
 *    values.add("test1");
 *    values.add("test2");
 *    // Add an entry that will add a new nested combo control
 *    // using the list of values passed in
 *    radioComp.addEntry("Default", null, values, null, true);
 *    // Another simple entry
 *    radioComp.addEntry("Dog");
 *    // Call layoutControls() so all control get positioned
 *    radioComp.layoutControls();
 *    
 *    // Set selections for both main and nested control
 *    radioComp.setSelection(4);
 *    radioCompSub.setSelection(1);
 *    </code>
 * </pre>
 * </p>
 * @author erikserating
 *
 */
public class PSRadioAndCheckBoxes extends Composite
   implements IPSUiConstants
{
   private static final Logger ms_log = Logger.getLogger(PSRadioAndCheckBoxes.class);
  
   /**
    * Create the composite
    * @param parent the parent composite, should not be <code>null</code>.
    * @param topLabel the header label shown at the very top of the component.
    * May be <code>null</code> or empty. In which case no label or
    * separator will be shown.
    * @param options the various display options
    * <p>
    * <pre>
    * Allowed options:
    * 
    *    <table border="1">
    *       <tr><td><b>Option</b></td><td><b>Description<b></td></tr>
    *       <tr><td>SWT.CHECK</td><td>Display check box buttons</td></tr>
    *       <tr><td>SWT.RADIO</td><td>Display radio box buttons (default)</td></tr>
    *       <tr><td>SWT.HORIZONTAL</td><td>Layout horizontally (default)</td></tr>
    *       <tr><td>SWT.VERTICAL</td><td>Layout vertically</td></tr>
    *       <tr><td>SWT.SEPARATOR</td><td>Show separator next to top label</td></tr>
    *       <tr><td>SWT.NONE</td><td>Use only defaults</td></tr>
    *    </table>
    * </pre>
    * </p>
    */
   public PSRadioAndCheckBoxes(
      Composite parent, String topLabel, int options)
   {
      super(parent, SWT.NONE);
      setLayout(new FormLayout());
      m_parent = this;
      
      // Handle options      
      m_horizontal = (options & SWT.VERTICAL) == 0;
      m_hasSeparator = (options & SWT.SEPARATOR) != 0;
      m_isRadio = (options & SWT.CHECK) == 0;
      
      if(m_horizontal)
      {
         m_parent = new Composite(this, SWT.NONE);
      }
          
      if(!StringUtils.isBlank(topLabel))
      {
         m_topLabel = new Label(this, SWT.WRAP);
         m_topLabel.setText(topLabel);
      }      
     
      //
   }
   
   /**
    * Adds a new button entry that will use the displayText as the
    * buttons value as well as its label.
    * @param displayText the buttons label text, cannot be <code>null</code>
    * or empty. 
    */
   public void addEntry(String displayText)
   {
      addEntry(displayText, false);
   }
   
   /**
    * Adds a new button entry that will use the displayText as the
    * buttons value as well as its label.
    * @param displayText the buttons label text, cannot be <code>null</code>
    * or empty.
    * @param separator if <code>true</code> then a horizontal separator will be
    * added to the end of the display text. This only will happen if vertical
    * layout is being used. Horizontal layout will ignore this.
    */
   public void addEntry(String displayText, boolean separator)
   {
      addEntry(displayText, separator, displayText);
   }
   
   /**
    * Adds a new button entry with separate label text and value string
    * @param displayText the buttons label text, cannot be <code>null</code>
    * or empty. 
    * @param value the value string, cannot be <code>null</code>, but
    * can be empty.
    */
   public void addEntry(String displayText, String value)
   {
      addEntry(displayText, false, value);
   }
   
   /**
    * Adds a new button entry with separate label text and value string
    * @param displayText the buttons label text, cannot be <code>null</code>
    * or empty.
    * @param separator if <code>true</code> then a horizontal separator will be
    * added to the end of the display text. This only will happen if vertical
    * layout is being used. Horizontal layout will ignore this.
    * @param value the value string, cannot be <code>null</code>, but
    * can be empty.
    */
   public void addEntry(String displayText, boolean separator, String value)
   {
      if(StringUtils.isBlank(displayText))
         throw new IllegalArgumentException(
            "displayText cannot be null or empty.");
      if(value == null)
         throw new IllegalArgumentException("value cannot be null.");
      m_entries.add(new RadioButtonEntry(displayText, separator, value));
   }
   
   /**
    * Adds a new button entry with a nested control and optional sub text that
    * will be put in a label in front of the control.
    * @param displayText the buttons label text, cannot be <code>null</code>
    * or empty.     
    * @param subText text that is displayed in a label to the left of the
    * nested control. 
    * @param control the nested control. Cannot be <code>null</code>.
    */
   public void addEntry(
      String displayText, String subText, Control control)
   {
      addEntry(displayText, false, subText, control, false);
   }
   
   /**
    * Adds a new button entry with a nested control and optional sub text that
    * will be put in a label in front of the control.
    * @param displayText the buttons label text, cannot be <code>null</code>
    * or empty.     
    * @param subText text that is displayed in a label to the left of the
    * nested control. 
    * @param control the nested control. Cannot be <code>null</code>.
    * @param noDisable if <code>true</code> then the control will not
    * be disabled by the selection of a different button
    */
   public void addEntry(
      String displayText, String subText, Control control, boolean noDisable)
   {
      addEntry(displayText, false, subText, control, noDisable);
   }
   
   /**
    * Adds a new button entry with a nested control and optional sub text that
    * will be put in a label in front of the control.
    * @param displayText the buttons label text, cannot be <code>null</code>
    * or empty.
    * @param separator if <code>true</code> then a horizontal separator will be
    * added to the end of the display text. This only will happen if vertical
    * layout is being used. Horizontal layout will ignore this. 
    * @param subText text that is displayed in a label to the left of the
    * nested control. 
    * @param control the nested control. Cannot be <code>null</code>. 
    */
   public void addEntry(
      String displayText, boolean separator, String subText, Control control)
   {
      addEntry(displayText, separator, subText, control, false);
   }
   
   /**
    * Adds a new button entry with a nested control and optional sub text that
    * will be put in a label in front of the control.
    * @param displayText the buttons label text, cannot be <code>null</code>
    * or empty.
    * @param separator if <code>true</code> then a horizontal separator will be
    * added to the end of the display text. This only will happen if vertical
    * layout is being used. Horizontal layout will ignore this. 
    * @param subText text that is displayed in a label to the left of the
    * nested control. 
    * @param control the nested control. Cannot be <code>null</code>.
    * @param noDisable if <code>true</code> then the control will not
    * be disabled by the selection of a different button
    */
   public void addEntry(
      String displayText, boolean separator, String subText, Control control, 
      boolean noDisable)
   {
      if(m_horizontal)
         throw new UnsupportedOperationException(
            "Nested components are not supported in horizontal layout mode.");
      if(StringUtils.isBlank(displayText))
         throw new IllegalArgumentException(
            "displayText cannot be null or empty.");
      if(control == null)
         throw new IllegalArgumentException("control cannot be null.");
      m_entries.add(new RadioButtonEntry(displayText, separator, subText, control,
         noDisable));
   }
   
   /**
    * Adds a new button entry with a nested <code>Combo</code> control
    * with specified values.
    * @param displayText the buttons label text, cannot be <code>null</code>
    * or empty. 
    * @param subText text that is displayed in a label to the left of the
    * nested control. 
    * @param choices list of strings that will fill the <code>Combo</code> control
    * @param selection the selected string from the choices list
    * @param readOnly indicates that the <code>Combo</code> control
    * should be read-only.
    */
   public void addEntry(
      String displayText, String subText, List<String> choices,
      String selection, boolean readOnly)
   {
      addEntry(displayText, false, subText, choices, selection, readOnly);
   }
   
   /**
    * Adds a new button entry with a nested <code>Combo</code> control
    * with specified values.
    * @param displayText the buttons label text, cannot be <code>null</code>
    * or empty.
    * @param separator if <code>true</code> then a horizontal separator will be
    * added to the end of the display text. This only will happen if vertical
    * layout is being used. Horizontal layout will ignore this.
    * @param subText text that is displayed in a label to the left of the
    * nested control. 
    * @param choices list of strings that will fill the <code>Combo</code> control
    * @param selection the selected string from the choices list
    * @param readOnly indicates that the <code>Combo</code> control
    * should be read-only.
    */
   public void addEntry(
      String displayText, boolean separator, String subText, List<String> choices,
      String selection, boolean readOnly)
   {
      if(m_horizontal)
         throw new UnsupportedOperationException(
            "Nested components are not supported in horizontal layout mode.");
      if(StringUtils.isBlank(displayText))
         throw new IllegalArgumentException(
            "displayText cannot be null or empty.");
      if(choices == null)
         throw new IllegalArgumentException("choices cannot be null.");
      int style = readOnly ? SWT.READ_ONLY : SWT.NONE;
      Combo combo = new Combo(this, style);
      int idx = 0;
      
      
      combo.setItems(choices.toArray(new String[choices.size()]));
      
      for(String item : choices)
      {
         if(selection != null && selection.equals(item))
            combo.select(idx);
         idx++;
      }
      m_entries.add(new RadioButtonEntry(
         displayText, separator, subText, combo, false));
   }
   
   /**
    * Calculates and sets the layout for this control.
    * This must be called after all entries are added
    * via one of the <code>addEntry</code> methods.
    */
   public void layoutControls()
   {
      // Layout top label and separator if needed
      if(m_topLabel != null)
      {
         FormData labelData = new FormData();
         labelData.left = new FormAttachment(0, 0);
         labelData.top = new FormAttachment(0, 0);
         m_topLabel.setLayoutData(labelData);
         
         if(m_hasSeparator)
         {
            Label separator = 
               new Label(this, SWT.HORIZONTAL | SWT.SEPARATOR);
            FormData sepData = new FormData();
            sepData.left = 
               new FormAttachment(m_topLabel, LABEL_HSPACE_OFFSET, SWT.RIGHT);
            sepData.top = new FormAttachment(m_topLabel, 0, SWT.CENTER);
            sepData.right = new FormAttachment(100, 0);
            separator.setLayoutData(sepData);            
         }
      }
      if(m_horizontal)
         horizontalLayout();
      else
         verticalLayout();
      handleButtonSelected();
   }
   
   /**
    * Handles laying the radio buttons out horizontally.
    * Does not support nested controls.
    */
   private void horizontalLayout()
   {      
      FormData formData = new FormData();
      if(m_topLabel == null)
      {
         formData.top = new FormAttachment(0, 0);
         formData.left = new FormAttachment(0, 0);
      }
      else
      {
         formData.top = 
            new FormAttachment(m_topLabel, 3, SWT.BOTTOM);
         formData.left = 
            new FormAttachment(0, 0);
      }
      formData.right = new FormAttachment(100, 0);
      m_parent.setLayoutData(formData);
      
      RowLayout layout = new RowLayout();
      layout.justify = true;
      layout.pack = false;
      layout.type = SWT.HORIZONTAL;
      layout.wrap = false;
      layout.marginTop = 0;
      m_parent.setLayout(layout);
   }
   
   /**
    * Handles laying the radio buttons out vertically.
    * Does not support nested controls.
    */
   private void verticalLayout()
   {
      RadioButtonEntry lastEntry = null;
      for(RadioButtonEntry entry : m_entries)
      {         
         // Set layout placement for radio button
         FormData formData = new FormData();
         if(lastEntry == null)
         {
            if(m_topLabel == null)
            {
               formData.top = new FormAttachment(0, 0);
               formData.left = new FormAttachment(0, 0);
            }
            else
            {
               formData.top = 
                  new FormAttachment(
                     m_topLabel, HORIZONTAL_OFFSET, SWT.BOTTOM);
               formData.left = 
                  new FormAttachment(m_topLabel, TOP_LABEL_OFFSET, SWT.LEFT);
            }
         }
         else
         {
            if(lastEntry.getControl() == null)
            {
               formData.top = 
                  new FormAttachment(
                     lastEntry.getRadioButton(), HORIZONTAL_OFFSET, SWT.BOTTOM);
            }
            else
            {
               formData.top = 
                  new FormAttachment(
                     lastEntry.getControl(), HORIZONTAL_OFFSET, SWT.BOTTOM);
            }
            formData.left = 
               new FormAttachment(
                  lastEntry.getRadioButton(), 0, SWT.LEFT);
         }
         entry.getRadioButton().setLayoutData(formData);
                  
         
         // Handle display text separator if there is one
         if(entry.hasDisplayTextSeparator())
         {
            Label sep = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
            FormData sepFormData = new FormData();
            sepFormData.left = new FormAttachment(
               entry.getRadioButton(), LABEL_HSPACE_OFFSET, SWT.RIGHT);
            sepFormData.right = new FormAttachment(100, 0);
            sepFormData.top = new FormAttachment(
               entry.getRadioButton(), 0, SWT.CENTER);
            sep.setLayoutData(sepFormData);            
         }
         
         // Set placement for nested control if it exists
         if(entry.getControl() != null)
         {
            // first handle sub text if any
            if(!StringUtils.isBlank(entry.getSubText()))
            {
               Label subText = new Label(this, SWT.NONE);
               entry.setSubTextLabel(subText);
               subText.setText(entry.getSubText());
               FormData formData4 = new FormData();
               formData4.top = 
                  new FormAttachment(
                     entry.getRadioButton(), HORIZONTAL_OFFSET,
                     SWT.BOTTOM);
               formData4.left = 
                  new FormAttachment(
                     entry.getRadioButton(), 16 + LABEL_HSPACE_OFFSET,
                     SWT.LEFT);
               subText.setLayoutData(formData4);
               // Now add the nested control
               FormData formData5 = new FormData();
               formData5.top = new FormAttachment(
                  subText, 0, SWT.TOP);
               formData5.left = new FormAttachment(
                  subText, LABEL_HSPACE_OFFSET, SWT.RIGHT);
               formData5.right = new FormAttachment(100, 0);
               entry.getControl().setLayoutData(formData5);
            }
            else
            {
               // Just add the nested control
               FormData formData6 = new FormData();
               formData6.top = 
                  new FormAttachment(
                     entry.getRadioButton(), HORIZONTAL_OFFSET,
                     SWT.BOTTOM);
               formData6.left = 
                  new FormAttachment(
                     entry.getRadioButton(), 16 + LABEL_HSPACE_OFFSET,
                     SWT.LEFT);
               formData6.right = new FormAttachment(100, 0);
               entry.getControl().setLayoutData(formData6);
            }
         }
         lastEntry = entry;
      }
   }
   
   /**
    * @return the index of the selected button, or the first selected 
    * button if these are check boxes. Returns -1 if no
    * selection is found.
    */
   public int getSelectedIndex()
   {
      int[] results = getSelectedIndices();
      if(results == null)
         return -1;
      return results[0];
   }
   
   /**
    * @return an array of indexes which indicate all the selected
    * buttons. If radio buttons, then there will be only one selection.
    * Returns <code>null</code> if there is no selection.
    */
   public int[] getSelectedIndices()
   {
      int index = 0;
      List<Integer> list = new ArrayList<Integer>();
      for (RadioButtonEntry entry : m_entries)
      {
         if(entry.getRadioButton().getSelection())
            list.add(index);
         index++;
      }
      if(list.isEmpty())
         return null;
      int[] results = new int[list.size()];
      for(int i = 0; i < list.size(); i++)
      {
         results[i] = list.get(i);
      }
      return results;
        
   }
   
     
   /**
    * Set the selection for this control.
    * @param index can be a single value, a comma delimited value, or
    * an array of integer primitives. These values will indicate which 
    * button(s) should be selected. A value of just -1 will deselect
    * all.
    */
   public void setSelection(int... index)
   {
      if(m_isRadio && index.length > 1)
         throw new IllegalArgumentException(
            "Cannot have more than one selection for radio buttons.");
      if(index.length == 1 && index[0] < 0)
      {
         // Clear all selections if less than 0
         for(RadioButtonEntry entry : m_entries)
            entry.getRadioButton().setSelection(false);
         return;
         
      }
      int count = 0;      
      for(RadioButtonEntry entry : m_entries)
      {
         Button button = entry.getRadioButton();
         boolean isSelected = ArrayUtils.contains(index, count);
         button.setSelection(isSelected);
         count++;
      }
      
      handleButtonSelected();
   }
   
   /**
    * Enables/disables specified buttons.
    * After a control is disabled through this method it won't be reenabled
    * by default behavior unless it is again enabled by this method.
    * @param enabled whether to enable or disable the buttons.
    * @param indexes the indexes of the buttons to enable/disable.
    */
   public void setEnabledButtons(final boolean enabled, int... indexes)
   {
      int idx = 0;
      for(RadioButtonEntry entry : m_entries)
      {
         if (ArrayUtils.contains(indexes, idx))
         {
            entry.getRadioButton().setEnabled(enabled);
            if (enabled)
            {
               m_forcedDisabledControls.remove(entry.getRadioButton());
               if (entry.getControl() != null)
               {
                  m_forcedDisabledControls.remove(entry.getControl());
               }
               if (entry.getSubTextLabel() != null)
               {
                  m_forcedDisabledControls.remove(entry.getSubTextLabel());
               }
            }
            else
            {
               m_forcedDisabledControls.add(entry.getRadioButton());
               if (entry.getControl() != null)
               {
                  m_forcedDisabledControls.add(entry.getControl());
               }
               if (entry.getSubTextLabel() != null)
               {
                  m_forcedDisabledControls.add(entry.getSubTextLabel());
               }
            }
         }
         idx++;
      }
   }
   
   /**
    * Returns indeces of enabled buttons.
    */
   public int[] getEnabledButtons()
   {
      final List<Integer> enabled = new ArrayList<Integer>();
      {
         int idx = 0;
         for(RadioButtonEntry entry : m_entries)
         {
            if (entry.getRadioButton().getEnabled())
            {
               enabled.add(idx);
            }
            idx++;
         }
      }
      final int[] result = new int[enabled.size()];
      {
         int idx = 0;
         for (final int enabledIdx : enabled)
         {
            result[idx] = enabledIdx;
            idx++;
         }
      }
      return result;
   }
   
   /**
    * Returns the nested control for the specified index if
    * it exists.
    * @param index the index of the radio/check button with the 
    * nested control.
    * @return the nested <code>Control</code> or <code>null</code>.
    */
   public Control getNestedControl(int index)
   {
      if(index < 0 || index >= m_entries.size())
         throw new IndexOutOfBoundsException();
      return m_entries.get(index).getControl();
   }
   
   /**
    * Returns the value for the selected button.
    * @param index the index of the selected button.
    * @return the value  for the button or <code>null</code> if
    * no value, as would be the case if there is a nested control
    * on the button.
    */
   public String getButtonValue(int index)
   {
      if(index < 0 || index >= m_entries.size())
         throw new IndexOutOfBoundsException();
      return m_entries.get(index).getValue();
   }
   
   /**
    * @return the values for all buttons in the order they appear in the control
    * from left to right or top to bottom.
    */
   public String[] getButtonValues()
   {
      int len = m_entries.size();
      String[] values = new String[len];
      for(int i = 0; i < len; i++)
      {
         values[i] = getButtonValue(i);
      }
      return values;      
   }
   
   /**
    * Returns number of buttons in this composite.
    */
   public int getButtonCount()
   {
      return m_entries.size();
   }
   
   /**
    * Returns the entry index by a string value
    * @param val
    * @return -1 if not found
    */
   public int getIndexByValue(String val)
   {
      int result = -1;
      String[] values = getButtonValues();
      for(int i = 0; i < values.length; i ++)
      {
         if(val.equalsIgnoreCase(values[i]))
            return i;
      }
      return result;
   }
   
   /**
    * Returns the entry index that contains the nested control
    * @param control
    * @return -1 if not found
    */
   public int getIndexByNestedControl(Control control)
   {
      int idx = 0;
      for(RadioButtonEntry entry : m_entries)
      {
         if(entry.getControl() != null && entry.getControl() == control)
            return idx;
         idx++;
      }
      return -1;
   }
    
   
   /**
    * Called upon any button selection. Will set the nested control
    * of the selected button as enabled and all other nested controls
    * will be disabled.
    */
   private void handleButtonSelected()
   {     
      
      for (RadioButtonEntry entry : m_entries)
      {
         boolean isSelected = entry.getRadioButton().getSelection();
         if (entry.getControl() != null)
         {
            setControlEnabled(entry.getControl(), isSelected);
         }
         if (entry.getSubTextLabel() != null)
         {
            setControlEnabled(entry.getSubTextLabel(), isSelected);
         }
      }
   }

   @Override
   public void dispose()
   {
      super.dispose();
   }   

   /* 
    * Overriden to set enabled on the child controls
    * @see org.eclipse.swt.widgets.Control#setEnabled(boolean)
    */
   @Override
   public void setEnabled(boolean enabled)
   {
      super.setEnabled(enabled);      
      for (Control control : getChildren())
      {
         setControlEnabled(control, enabled);
      }
      if (enabled)
      {
         handleButtonSelected();
      }
   }
   
   /**
    * Utility method to recursively set enabled on the passed in control
    * and all of its children.
    * @param control assumed not <code>null</code>.
    * @param enable 
    */
   protected void setControlEnabled(Control control, boolean enable)
   {
      // stop enabling controls if it is in the list of forced disabled
      if (m_forcedDisabledControls.contains(control) && enable)
      {
         return;
      }
      if (!(control instanceof PSRadioAndCheckBoxes))
      {
         // nested PSRadioAndCheckBoxComposite take care about the children.
         enableChildren(control, enable);
      }
      control.setEnabled(enable);
   }

   private void enableChildren(Control control, boolean enable)
   {
      Class clazz = control.getClass();
      Method getChildrenMethod = null;
      try
      {
         getChildrenMethod = clazz.getMethod("getChildren",  //$NON-NLS-1$
            new Class[]{});
      }      
      catch (NoSuchMethodException ignore){}
      if(getChildrenMethod != null)
      {
         try
         {
            Control[] children = 
               (Control[])getChildrenMethod.invoke(control, new Object[]{});
            for(Control child : children)
            {
               setControlEnabled(child, enable);
            }
         }
         catch (Exception e)
         {
            ms_log.warn("Failed to walk children", e);
         }        
      }
   }
   
   /**
    * Adds a selection listener to this control. The selection listener
    * will be notified of selection events on this control and any nested
    * control.
    * @param listener cannot be <code>null</code>.
    */
   public void addSelectionListener(SelectionListener listener)
   {
      if(listener == null)
         throw new IllegalArgumentException("listener cannot be null.");
      if(!m_selectionListeners.contains(listener))
         m_selectionListeners.add(listener);
   }
   
   /**
    * Removes the specified selection listener
    * @param listener cannot be <code>null</code>.
    */
   public void removeSelectionListener(SelectionListener listener)
   {
      if(listener == null)
         throw new IllegalArgumentException("listener cannot be null.");
      if(m_selectionListeners.contains(listener))
         m_selectionListeners.remove(listener);
   }
   
   /**
    * Called when a selection event occurs on the control or
    * any of its nested controls.
    */
   public void fireSelectionEvent()
   {
      Event e = new Event();
      e.item = this;
      e.widget = this;     
      SelectionEvent event = new SelectionEvent(e);
      for(SelectionListener listener : m_selectionListeners)
      {
        listener.widgetSelected(event);
      }
   }
   
   /**
    * Add a focus listener to be notified when a focus
    * event occurs.
    * @param listener cannot be <code>null</code>.
    */
   @Override
   public void addFocusListener(FocusListener listener)
   {
      if(listener == null)
         throw new IllegalArgumentException("listener cannot be null"); //$NON-NLS-1$
      if(!m_focusListeners.contains(listener))
         m_focusListeners.add(listener);
   }
   
   /**
    * Removes the specified selection listener
    * @param listener cannot be <code>null</code>.
    */
   @Override
   public void removeFocusListener(FocusListener listener)
   {
      if(listener == null)
         throw new IllegalArgumentException("listener cannot be null"); //$NON-NLS-1$
      if(m_focusListeners.contains(listener))
         m_focusListeners.remove(listener);
   }
   
   /**
    * Fires a <code>FocusEvent</code> for all registered 
    * <code>FocusListeners</code>.
    */
   private void fireFocusEvent(boolean gained)
   {
      Event e = new Event();
      e.item = this;
      e.widget = this;     
      FocusEvent event = new FocusEvent(e);
      for(FocusListener listener : m_focusListeners)
      {
        if(gained)
           listener.focusGained(event);
        else
           listener.focusLost(event);
      }
   }
   
   @Override
   protected void checkSubclass()
   {
      // Disable the check that prevents subclassing of SWT components
   }
   
   /**
    * Top label specified in constructor.
    * Is empty string if no label was specified.
    */
   public String getTopLabel()
   {
      return m_topLabel == null ? "" : m_topLabel.getText();
   }
   
   /**
    * Holds the radio/check button and its display label as well as
    * the nested control, if specified. This class also adds the
    * appropriate listeners to these controls.
    */
   class RadioButtonEntry
   {
      
      RadioButtonEntry(String displayText, boolean separator, String value)
      {
         mi_value = value == null ? "" : value;
         init(displayText, separator);
      }
      
      @SuppressWarnings("synthetic-access") 
      RadioButtonEntry(
         String displayText, boolean separator, String subText, Control control,
         boolean noDisable)
      {
         mi_subText = subText;
         mi_control = control;
         init(displayText, separator);
         
         if(mi_control != null)
         {
            if(noDisable)
               m_forcedDisabledControls.add(mi_control);
            addListeners(mi_control, noDisable);            
         }
         
      }
            
      /**
       * @return the sub text for this entry, may be
       * <code>null</code> or empty.
       * In case label is created from this text the code should save this label
       * using {@link #setSubTextLabel(Label)};
       */
      String getSubText()
      {
         return mi_subText;
      }
      
      String getValue()
      {
         return mi_value;
      }
      
      /**
       *  @return the radio or check button for this entry.
       *  Never <code> null</code>.
       */
      Button getRadioButton()
      {
         return mi_radioButton;
      }
      
      /**
       * @return the nested control for this entry,
       * may be <code>null</code>.
       */
      Control getControl()
      {
         return mi_control;
      }
      
      /**
       * Label for the nested control. <code>null</code> if no label is specifed. 
       */
      Label getSubTextLabel()
      {
         return mi_subTextLabel;
      }
      
      /**
       * Must be set by code creating actual label from {@link #getSubText()}.
       * @param subTextLabel <code>null</code> if no sub text is specified.
       */
      void setSubTextLabel(final Label subTextLabel)
      {
         mi_subTextLabel = subTextLabel;
      }
      
      boolean hasDisplayTextSeparator()
      {
         return mi_displayTextSeparator;
      }
      
      /**
       * Initializes the radio button control and its label
       * @param displayText assumed not <code>null</code> or
       * empty.
       */
      @SuppressWarnings("synthetic-access") 
      void init(String displayText, boolean separator)
      {
         Composite comp = m_parent;
         mi_displayTextSeparator = separator;
                 
         int style = m_isRadio ? SWT.RADIO : SWT.CHECK;
         mi_radioButton = 
            new Button(comp, style);
         mi_radioButton.setText(displayText);
         mi_radioButton.addSelectionListener(new SelectionAdapter()
            {
               /* 
                * @see org.eclipse.swt.events.SelectionAdapter#
                * widgetSelected(org.eclipse.swt.events.SelectionEvent)
                */
               @Override
               public void widgetSelected(
                     @SuppressWarnings("unused") SelectionEvent e)
               {
                  handleButtonSelected();
                  if(m_isRadio)
                  {
                     ++m_eventCount;
                     // Is this the only way to respond to only once?
                     if(m_eventCount%2 == 0)
                     {
                        m_eventCount = 0;
                        fireSelectionEvent();
                     }
                  }
                  else
                  {
                     fireSelectionEvent();
                  }
               }
            
            });
         mi_radioButton.addFocusListener(new InternalFocusListener());
         
      }
      
      /**
       * Adds selection and modify listeners to nested components
       * if they support the listeners. This allows the main component to 
       * be notified when a nested control has a selection change.
       * @param control assumed not <code>null</code>.
       * @param noDisable flag indicating that the passed in control should
       * not be disabled.
       */
      void addListeners(Control control, boolean noDisable)
      {
         // Now we need to add the appropriate listeners to determine if
         // change really occurred on the control in question.
         // We will use reflection to determine if the add listener methods
         // exist for the passed in control. We also invoke using reflection.
         Class clazz = control.getClass();
         Method addModifyListenerMethod = null;
         Method addSelectionListenerMethod = null;
         try
         {
            addModifyListenerMethod = 
               clazz.getMethod("addModifyListener",  //$NON-NLS-1$
               new Class[]{ModifyListener.class});
         }      
         catch (NoSuchMethodException ignore){}
         try
         {
            addSelectionListenerMethod = 
               clazz.getMethod("addSelectionListener",  //$NON-NLS-1$
               new Class[]{SelectionListener.class});
            if((control instanceof Text))
               addSelectionListenerMethod = null;
         }      
         catch (NoSuchMethodException ignore){}
         
         if(addModifyListenerMethod != null)
         {
            ModifyListener mListener = new ModifyListener()
            {
               public void modifyText(@SuppressWarnings("unused") ModifyEvent e)
               {
                  fireSelectionEvent();             
               }            
            };
            try
            {
               addModifyListenerMethod.invoke(
                  control, new Object[]{mListener});
            }
            catch (Exception ex)
            {
               new RuntimeException(ex);
            }
            
         }
         
         if(addSelectionListenerMethod != null)
         {
            SelectionListener sListener = new SelectionAdapter()
            {

               @Override
               public void widgetSelected(
                     @SuppressWarnings("unused") SelectionEvent e)
               {
                 fireSelectionEvent();                           
               }            
            };
            try
            {
               addSelectionListenerMethod.invoke(
                  control, new Object[]{sListener});
            }
            catch (Exception ex)
            {
               new RuntimeException(ex);
            }
         }
         // Add focus listener that selects the radio button that this nested
         // control is under
         if(noDisable)
         {
            addFocusListenerToControl(control);
         }
      }
      
      void addFocusListenerToControl(Control control)
      {
         Control[] children = null;
         if(control instanceof Composite)
         {
            children = ((Composite)control).getChildren();
         }
         else if(control instanceof Group)
         {
            children = ((Group)control).getChildren();
         }
         if(children != null)
         {
            for(Control c : children)
            {
               addFocusListenerToControl(c);
            }
         }
         else
         {
            control.addFocusListener(new FocusAdapter()
               {

                  /* 
                   * @see org.eclipse.swt.events.FocusAdapter#focusGained(
                   * org.eclipse.swt.events.FocusEvent)
                   */
                  @SuppressWarnings("synthetic-access")
                  @Override
                  public void focusGained(
                        @SuppressWarnings("unused") FocusEvent e)
                  {
                     int index = getIndexByNestedControl(mi_control);
                     setSelection(index);
                  }
                  
               });
            control.addFocusListener(new InternalFocusListener());
         }
      }
          
      private boolean mi_displayTextSeparator;
      private String mi_subText;
      private String mi_value;
      private Button mi_radioButton;
      private Control mi_control;
      private Label mi_subTextLabel;
   }
   
   class InternalFocusListener implements FocusListener
   {

      @SuppressWarnings("synthetic-access")
      public void focusGained(@SuppressWarnings("unused") FocusEvent e)
      {
         fireFocusEvent(true);
         
      }

      @SuppressWarnings("synthetic-access")
      public void focusLost(@SuppressWarnings("unused") FocusEvent e)
      {
         fireFocusEvent(false);         
      }
      
   }
   
   private Label m_topLabel;
   private boolean m_hasSeparator;
   private boolean m_horizontal;
   private boolean m_isRadio;
   private Composite m_parent;
   
   private int m_eventCount;
   
   /**
    * List of all radio button entries
    */
   private List<RadioButtonEntry> m_entries = 
      new ArrayList<RadioButtonEntry>();
   
   /**
    * List of all selection listeners registered to this control.
    */
   private List<SelectionListener> m_selectionListeners = 
      new ArrayList<SelectionListener>();
   
   /**
    * List of all focus listeners registered to this control.
    */
   private List<FocusListener> m_focusListeners = 
      new ArrayList<FocusListener>();
   
   /**
    * Controls which are forced to be disabled.
    */
   private List<Control> m_forcedDisabledControls = new ArrayList<Control>();
     
   
   private static final int HORIZONTAL_OFFSET = 5;
   private static final int TOP_LABEL_OFFSET = 20;

}
