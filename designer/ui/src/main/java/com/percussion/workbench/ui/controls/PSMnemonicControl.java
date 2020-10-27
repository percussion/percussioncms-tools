/******************************************************************************
 *
 * [ PSMnemonicControl.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.controls;

import com.percussion.workbench.ui.IPSUiConstants;
import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A combo control for showing the mnemonic for a label.
 */
public class PSMnemonicControl extends Composite implements IPSUiConstants
{

   /**
    * Constructor creates the controls needed.
    * @param parent The parent composite for the mnemonic sontrol.
    * @param style SWT styles.
    */
   public PSMnemonicControl(Composite parent, int style) 
   {
      super(parent, style);
      if (parent == null)
      {
         throw new IllegalArgumentException("parent must not be null");
      }
      setLayout(new FormLayout());

      final Label mnemonicLabel = new Label(this, SWT.NONE);
      final FormData formData = new FormData();
      formData.top = new FormAttachment(0,LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET);
      formData.left = new FormAttachment(0, 0);
      mnemonicLabel.setLayoutData(formData);
      mnemonicLabel.setText("Mnemonic:");

      m_mnemonicCombo = new Combo(this, SWT.READ_ONLY);
      final FormData formData_1 = new FormData();
      formData_1.top = new FormAttachment(mnemonicLabel,-LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET,SWT.TOP);
      formData_1.left = new FormAttachment(mnemonicLabel,LABEL_HSPACE_OFFSET,SWT.RIGHT);
      m_mnemonicCombo.setLayoutData(formData_1);
      //
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
    * Returns the selected mnemonic string.
    * @return String selected mnemonic string.
    */
   public String getMnemonic()
   {
      return m_mnemonicCombo.getText();
   }

   /**
    * Sets the input to the Mnemonic combo with non duplicate characters
    * of the label string. Calls {@link #setInput(String, String, boolean)} 
    * method with false for preservecase to show the uppercase characters.
    * @param labelStr A String whose characters needs to be displayed
    *   in the Mnemonic drop down list.
    * @param selection A String   
    */
   public void setInput(String labelStr, String selection)
   {
      setInput(labelStr,selection,false);
   }
   /**
    * Sets the input to the Mnemonic combo.
    * @param labelStr A String whose characters needs to be displayed
    *   in the Mnemonic drop down list.
    * @param selection A String   
    * @param preserveCase if <code>true</code> case of the letters in the list 
    *   will be be preserved, otherwise converted to uppercase.
    * to 
    */
   public void setInput(String labelStr, String selection, boolean preserveCase)
   {
      String initVal = "";
      if(selection == null || selection.trim().length() < 1)
         initVal = m_mnemonicCombo.getText();
      else
         initVal = selection;
      
      List mnemList = getMnemonicInput(labelStr,preserveCase);
      m_mnemonicCombo.removeAll();
      //Add empty selection
      m_mnemonicCombo.add(StringUtils.EMPTY);
      for(Object str:mnemList.toArray())
      {
         m_mnemonicCombo.add((String)str);
      }
      int index = -1;
      if(m_mnemonicCombo.getItemCount()>0)
         index = m_mnemonicCombo.indexOf(initVal);
      m_mnemonicCombo.select(index);
   }

   /**
    * Uitlity method to convert the supplied String into String 
    * array of characters in it. Returns only not letters.
    * 
    * @param str input String if <code>null</code> or empty returns
    *   empty List.
    * @param preserveCase if <code>true</code> preserves the case
    *   otherwise reurns uppercase letters.
    * @return A String List of letters may be empty but never 
    *   <code>null</code>. 
    */
   private static List<String> getMnemonicInput(String str, boolean preserveCase)
   {
      List<String> charList =  new ArrayList<String>();
      if(str == null || str.trim().length()<1)
         return charList;
      Set<String> charSet = new HashSet<String>();
      char[] chars = str.toCharArray();
      for (char ch : chars)
      {
         if(Character.isLetter(ch))
         {
            if(preserveCase)
            {
               charSet.add(Character.toString(ch));
            }
            else
            {
               charSet.add(Character.toString(Character.toUpperCase(ch)));
            }
         }
            
      }
      charList.addAll(charSet);
      Collections.sort(charList);
      return charList;
   }
   /**
    * Sets the mnemonic with the given character if it is a 
    * valid mnemonic for the supplied label
    */
   public boolean setMnemonic(String label, char ch)
   {
      boolean isValid = isValidMnemonic(label,ch);
      if(isValid)
         m_mnemonicCombo.setText(Character.toString(ch));
      return isValid;
   }
   
   /**
    * 
    */
   public Combo getMnemonicCombo()
   {
      return m_mnemonicCombo;
   }
   /**
    * Is the supplied nnemonic character valid for the supplied label?
    * 
    * @param label the label to test against, if <code>null</code> or empty.
    * returns <code>false</code>.
    * @return <code>true</code> if valid, <code>false</code> otherwise.
    */
   public boolean isValidMnemonic(String label, char mnemChar)
   {
      if(StringUtils.isBlank(label))
      {
         return false;
      }
      return label.toUpperCase().contains(
            Character.toString(mnemChar).toUpperCase());
   }
   /**
    * Clears the mnemonic combo box.
    */
   public void clearMnemonic()
   {
      m_mnemonicCombo.removeAll();
   }
   /**
    * Controls
    */
   private Combo m_mnemonicCombo;
}
