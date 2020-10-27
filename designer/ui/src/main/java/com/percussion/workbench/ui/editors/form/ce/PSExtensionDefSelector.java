/******************************************************************************
*
* [ PSExtensionDefSelector.java ]
*
* COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
* All rights reserved. This material contains unpublished, copyrighted
* work including confidential and proprietary information of Percussion.
*
*****************************************************************************/
package com.percussion.workbench.ui.editors.form.ce;

import com.percussion.design.objectstore.IPSReplacementValue;
import com.percussion.design.objectstore.PSExtensionCall;
import com.percussion.design.objectstore.PSExtensionParamValue;
import com.percussion.design.objectstore.PSTextLiteral;
import com.percussion.extension.PSExtensionDef;
import com.percussion.util.PSStringComparator;
import com.percussion.utils.types.PSPair;
import com.percussion.workbench.ui.IPSUiConstants;
import com.percussion.workbench.ui.controls.PSAbstractLabelProvider;
import com.percussion.workbench.ui.controls.PSDefaultContentProvider;
import com.percussion.workbench.ui.editors.dialog.PSExtensionParamsDialog;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PSExtensionDefSelector extends Composite implements IPSUiConstants
{

   public PSExtensionDefSelector(Composite parent, int style, List<PSExtensionDef> defs) 
   {
      super(parent, style);
      if (defs == null)
      {
         throw new IllegalArgumentException("defs must not be null");
      }
      
      m_exitsList = defs;
      final PSStringComparator stringComp = new PSStringComparator(
         PSStringComparator.SORT_CASE_INSENSITIVE_ASC);
      Collections.sort(m_exitsList, new Comparator<PSExtensionDef>() {

         public int compare(PSExtensionDef ext1, PSExtensionDef ext2)
         {
            return stringComp.compare(ext1.getRef().getExtensionName(), 
               ext2.getRef().getExtensionName());
         }});
      
      setLayout(new FormLayout());
      m_extParamsButton = new Button(this, SWT.NONE);
      final FormData fd2 = new FormData();
      fd2.top = new FormAttachment(0,0);
      fd2.right = new FormAttachment(100,0);
      fd2.height = 21;
      fd2.width = 21;
      m_extParamsButton.setLayoutData(fd2);
      m_extParamsButton.setText("...");
      m_extParamsButton.addSelectionListener(new SelectionAdapter()
            {
               @SuppressWarnings("synthetic-access")
               public void widgetSelected(SelectionEvent event)
               {
                  openExtensionParamDialog();
               }
            });

      m_extensionCV = new ComboViewer(this, SWT.BORDER);
      final FormData fd1 = new FormData();
      fd1.top = new FormAttachment(0, 0);
      fd1.left = new FormAttachment(0,0);
      fd1.right = new FormAttachment(m_extParamsButton, -BUTTON_VSPACE_OFFSET, SWT.LEFT);
      m_extensionCV.getCombo().setLayoutData(fd1);
      m_extensionCV.setLabelProvider(new PSAbstractLabelProvider(){
         public String getText(Object element) {
            PSExtensionDef ext = (PSExtensionDef)element;
            String extName = ext.getRef().getExtensionName();
            extName = extName.split("/")[extName.split("/").length - 1];
            return extName;
         };
      });
      m_extensionCV.setContentProvider(new PSDefaultContentProvider());
      m_extensionCV.setInput(m_exitsList);
      m_extensionCV.getCombo().addSelectionListener(new SelectionAdapter()
      {
         @SuppressWarnings("synthetic-access")
         @Override
         public void widgetSelected(SelectionEvent e)
         {
            IStructuredSelection sel = (IStructuredSelection) m_extensionCV
                  .getSelection();
            PSExtensionDef def = (PSExtensionDef) sel.getFirstElement();
            if (def == null)
               return;
            Iterator titles = def.getRuntimeParameterNames();
            int count = 0;
            for ( ; titles.hasNext(); titles.next())
               count++;
            PSExtensionParamValue values[] = new PSExtensionParamValue[count];
            if ( count > 0 )
            {
               values[0] = new PSExtensionParamValue(new PSTextLiteral(""));
               for ( int i = 1; i < count; ++i )
                  values[i] = values[0];
               
            }
            m_extnsion = new PSExtensionCall(def.getRef(),values);
            if (count>0)
            {
               m_extParamsButton.setEnabled(true);
               openExtensionParamDialog();
            }
            else
            {
               m_extParamsButton.setEnabled(false);
            }
            super.widgetSelected(e);
         }
      });
      //
   }
   
   /**
    * Opens an extension params dialog and if the dialog
    *
    */
   private void openExtensionParamDialog()
   {
      IStructuredSelection sel = (IStructuredSelection) m_extensionCV.getSelection();
      if(sel.isEmpty())
         return;
      PSExtensionDef def = (PSExtensionDef)sel.getFirstElement();
      PSExtensionParamValue[] values = m_extnsion.getParamValues();
      Iterator iter = def.getRuntimeParameterNames();
      Map params = new HashMap();
      int i = 0;
      while (iter.hasNext())
      {
         String name = (String) iter.next();
         IPSReplacementValue value = values[i].getValue();
         params.put(name, value);
         i++;
      }
      PSExtensionParamsDialog dialog = new PSExtensionParamsDialog(getShell(),
            def, params);

      if (dialog.open() == Dialog.OK)
      {
         List<PSPair> retparams = dialog.getParamValues();
         PSExtensionParamValue[] newvalues = new PSExtensionParamValue[retparams
               .size()];

         for (int j = 0; j < retparams.size(); j++)
         {
            IPSReplacementValue repVal = (IPSReplacementValue) ((PSPair) retparams.get(j)).getSecond();
            if (repVal == null)
               repVal = new PSTextLiteral(StringUtils.EMPTY);
            newvalues[j] = new PSExtensionParamValue(repVal);
         }
         m_extnsion.setParamValues(newvalues);
      }
   }
   
   /**
    * Clears the extension comboviewer.
    *
    */
   protected void clearExtensionCall()
   {
      m_extensionCV.getCombo().select(-1);
   }
   
   /**
    * 
    */
   public PSExtensionCall getExtensionCall()
   {
      return m_extnsion;
   }
   
   /**
    * 
    *
    */
   public void setExtensionCall(PSExtensionCall extCall)
   {
      m_extnsion = extCall;
      if(m_extnsion != null)
      {
         //Find the index of the extension and select it.
         for(int i=0; i<m_exitsList.size(); i++)
         {
            PSExtensionDef def = m_exitsList.get(i);
            if(def.getRef().equals(m_extnsion.getExtensionRef()))
            {
               m_extensionCV.getCombo().select(i);
               break;
            }
         }
      }
   }
   
   /*
    *  (non-Javadoc)
    * @see org.eclipse.swt.widgets.Widget#dispose()
    */
   public void dispose()
   {
      super.dispose();
   }

   /*
    *  (non-Javadoc)
    * @see org.eclipse.swt.widgets.Widget#checkSubclass()
    */
   protected void checkSubclass()
   {
   }
   
   //Controls for this composite...
   private ComboViewer m_extensionCV;
   private Button m_extParamsButton;
   //Data object
   private PSExtensionCall m_extnsion;
   //Exitension defs list
   private List<PSExtensionDef> m_exitsList;
}
