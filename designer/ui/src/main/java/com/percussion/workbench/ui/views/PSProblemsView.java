/******************************************************************************
*
* [ PSProblemsView.java ]
*
* COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
* All rights reserved. This material contains unpublished, copyrighted
* work including confidential and proprietary information of Percussion.
*
*****************************************************************************/
package com.percussion.workbench.ui.views;

import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSWorkbenchPlugin;
import com.percussion.workbench.ui.controls.IPSNewRowObjectProvider;
import com.percussion.workbench.ui.controls.PSAbstractTableLabelProvider;
import com.percussion.workbench.ui.controls.PSSortableTable;
import com.percussion.workbench.ui.editors.form.PSEditorBase;
import com.percussion.workbench.ui.layouts.IPSTableLayoutListener;
import com.percussion.workbench.ui.layouts.PSTableLayoutEvent;
import com.percussion.workbench.ui.util.PSProblemSet;
import com.percussion.workbench.ui.util.PSProblemSet.Problem;
import com.percussion.workbench.ui.util.PSUiUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.part.ViewPart;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A view that will display field level problems for the
 * currently active editor.
 */
public class PSProblemsView extends ViewPart
{

   /**
    * The view id as specified in the plugin.xml.
    */
   public static final String ID = PSProblemsView.class.getName();

   /* 
    * @see org.eclipse.ui.IWorkbenchPart#createPartControl(
    * org.eclipse.swt.widgets.Composite)
    */
   @Override
   public void createPartControl(Composite parent)
   {
      Composite comp = new Composite(parent, SWT.NONE);
      comp.setLayout(new FormLayout());
      
      m_table = createTable(comp);
      FormData formdata_3 = new FormData();
      formdata_3.top = new FormAttachment(0, 0);
      formdata_3.bottom = new FormAttachment(100, 0);
      formdata_3.right = new FormAttachment(100, 0);
      formdata_3.left = new FormAttachment(0, 0);
      m_table.setLayoutData(formdata_3);
      
      List rows = new ArrayList();
      addEmptyRows(rows);
      m_table.setValues(rows);
      forceValidationOnActiveEditor();
      
   }

   /* 
    * @see org.eclipse.ui.IWorkbenchPart#setFocus()
    */
   @Override
   public void setFocus()
   {
     m_table.setFocus();     
   }
   
   /**
    * Displays problems from the passed in problem set.
    * @param problems the problem set, may be <code>null</code>,
    * or empty.
    */
   @SuppressWarnings("unchecked")
   public void displayProblems(PSProblemSet problems)
   {
      List list = new ArrayList();
      if(problems != null)
      {
         Iterator it = problems.getProblems();
         while(it.hasNext())
         {
            list.add(it.next());
         }
      }
      addEmptyRows(list);
      m_table.setValues(list);
   }
   
   /**
    * Adds empty rows to end of passed in list
    * @param list assumed not <code>null</code>.
    */
   @SuppressWarnings("unchecked")
   private void addEmptyRows(List list)
   {
      for(int i = 0; i < MIN_ROWS; i++)
         list.add(new EmptyRow());
   }
   
   /**
    * Forces validation on active editor if there is one.
    */
   private void forceValidationOnActiveEditor()
   {
      IWorkbench workbench = PSWorkbenchPlugin.getDefault().getWorkbench();
      IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
      if(window == null)
         return;
      IWorkbenchPage page = window.getActivePage();
      if(page == null)
         return;
      IEditorPart part = page.getActiveEditor();
      if(part != null && part instanceof PSEditorBase)
      {
         PSEditorBase editor = (PSEditorBase)part;
         editor.runValidation(true, true);         
      }
   }
   
   /**
    * Creates the views table
    * @param parent the parent composite, cannot be <code>null</code>.
    * @return the newly created table, never <code.null</code>.
    */
   private PSSortableTable createTable(Composite parent)
   {
      IPSNewRowObjectProvider dummyObjectProvider = 
         new IPSNewRowObjectProvider()
         {

            public Object newInstance()
            {
               return null;
            }

            public boolean isEmpty(@SuppressWarnings("unused") Object obj)
            {
               if(obj instanceof EmptyRow)
                  return true;
               return false;
            }
         
         };
         
         ITableLabelProvider labelProvider= new PSAbstractTableLabelProvider(){

            public String getColumnText(Object element, int columnIndex)
            {
               if(element instanceof EmptyRow)
                  return "";
               Problem problem = (Problem)element;
               switch(columnIndex)
               {
                  case 1:
                     return StringUtils.defaultString(problem.getFieldname());
                  case 2:
                     return StringUtils.defaultString(problem.getPagename());
                  case 3:   
                     return StringUtils.defaultString(problem.getDescription());                  
                  default:
                     return ""; //$NON-NLS-1$
               }
            }

            /* 
             * @see com.percussion.workbench.ui.controls.PSAbstractTableLabelProvider#
             * getColumnImage(java.lang.Object, int)
             */
            @Override
            public Image getColumnImage(Object element, int columnIndex)
            {
               if(element instanceof EmptyRow)
                  return null;
               Problem problem = (Problem)element;
               if(problem == null)
                  return null;
               if(columnIndex == 0)
               {
                  int type = problem.getType();
                  switch(type)
                  {
                     case PSProblemSet.TYPE_ERROR:
                        return JFaceResources.getImage(
                           PSPROBLEMSVIEW_IMG_MESSAGE_ERROR);
                     case PSProblemSet.TYPE_WARN:
                        return JFaceResources.getImage(
                           PSPROBLEMSVIEW_IMG_MESSAGE_WARNING);
                     case PSProblemSet.TYPE_INFO:
                        return JFaceResources.getImage(
                           PSPROBLEMSVIEW_IMG_MESSAGE_INFO);
                     default:
                        return JFaceResources.getImage(
                           PSPROBLEMSVIEW_IMG_MESSAGE_ERROR);
                  }
               }
               return null;
            }  
            
            
            };
            
         PSSortableTable table = new PSSortableTable(parent,
            labelProvider, dummyObjectProvider,
            SWT.SINGLE | SWT.FULL_SELECTION,
            PSSortableTable.SURPRESS_MANUAL_SORT |
            PSSortableTable.SURPRESS_TABLE_CURSOR);
         final FormData formData = new FormData();
         formData.top = new FormAttachment(0, 5);
         formData.left = new FormAttachment(0, 225);
         table.setLayoutData(formData);
         
         table.addColumn(" ", PSSortableTable.NONE, //$NON-NLS-1$
            new ColumnWeightData(1, 25, true), null, SWT.LEFT);
         table.addColumn("Field name",
            PSSortableTable.IS_SORTABLE, //$NON-NLS-1$
            new ColumnWeightData(10, 80, true), null, SWT.LEFT);
         table.addColumn("Tab",
            PSSortableTable.IS_SORTABLE, //$NON-NLS-1$
            new ColumnWeightData(10, 80, true), null, SWT.LEFT);
         table.addColumn("Description",
            PSSortableTable.IS_SORTABLE, //$NON-NLS-1$
            new ColumnWeightData(30, 200, true), null, SWT.LEFT);
         IDialogSettings settings = 
            PSUiUtils.getDialogSettings(this.getClass());
         IDialogSettings tableWidths = settings.getSection(
            TABLE_WIDTH_SETTINGS);
         
         if(tableWidths != null)
         {
            float[] ratios = new float[table.getColumnCount()];
            boolean error = false;
            for(int i = 0; i < ratios.length; i++)
            {
               try
               {
                  ratios[i] = tableWidths.getFloat(String.valueOf(i));
                  
               }
               catch (NumberFormatException e)
               {
                  PSWorkbenchPlugin.handleException(null,
                     null,
                     PSMessages.getString(
                        "PSObjectSorterView.error.missingColWidthValue"), //$NON-NLS-1$
                     e);
                  break;
               }
               
            }
            if(!error)
            {
               table.setColumnWidthRatios(ratios);
            }
         }
         
         // Add table layout listener so we know when the column ratios have 
         // been changed and we can then save the tables state info
         table.addTableLayoutListener(new IPSTableLayoutListener()
            {

               /* 
                * @see com.percussion.workbench.ui.layouts.IPSTableLayoutListener#
                * columnsResized(com.percussion.workbench.ui.layouts.PSTableLayoutEvent)
                */
               @SuppressWarnings("synthetic-access") //$NON-NLS-1$
               public void columnsResized( 
                  @SuppressWarnings("unused") PSTableLayoutEvent event)
               {
                  saveViewState();                  
               }
            
            });
         table.addFocusListener(new FocusAdapter()
            {

               /* 
                * @see org.eclipse.swt.events.FocusAdapter#focusGained(
                * org.eclipse.swt.events.FocusEvent)
                */
               @Override
               public void focusGained(@SuppressWarnings("unused") FocusEvent e)
               {
                  forceValidationOnActiveEditor();
               }
               
            });
       
         return table;
   }
   
   /**
    * Persist the state of this view using the <code>PSDialogSettings</code>
    * object. Saves the tables width ratios.
    */
   private void saveViewState()
   {
      IDialogSettings settings = 
         PSUiUtils.getDialogSettings(this.getClass());
      float[] ratios = m_table.getColumnWidthRatios();
      if(ratios != null)
      {
         IDialogSettings tableWidths = settings.getSection(
            TABLE_WIDTH_SETTINGS);
         if(tableWidths == null)
            tableWidths = settings.addNewSection(TABLE_WIDTH_SETTINGS);
         for(int i = 0; i < ratios.length; i++)
         {
            tableWidths.put(String.valueOf(i), ratios[i]);
         }
      }
      
   }
   
   /**
    * A dummy class that represents an empty row in the table
    */
   class EmptyRow{}
   
   
   
   private PSSortableTable m_table;
   private static final String TABLE_WIDTH_SETTINGS = "tableWidths"; //$NON-NLS-1$
   
   /**
    * Minimum number of rows that should be shown at all times
    */
   private int MIN_ROWS = 20;
   
   /**
    * Image registry key for info message image (value
    * <code>"pseditor_base_message_info_image"</code>).
    */
   public static final String PSPROBLEMSVIEW_IMG_MESSAGE_INFO = 
      "pseditor_base_message_info_image"; //$NON-NLS-1$

   /**
    * Image registry key for info message image (value
    * <code>"pseditor_base_message_warning_image"</code>).
    */
   public static final String PSPROBLEMSVIEW_IMG_MESSAGE_WARNING = 
      "pseditor_base_message_warning_image"; //$NON-NLS-1$

   /**
    * Image registry key for info message image (value
    * <code>"pseditor_base_message_error_image"</code>). 
    */
   public static final String PSPROBLEMSVIEW_IMG_MESSAGE_ERROR = 
      "pseditor_base_message_error_image"; //$NON-NLS-1$
   
   static 
   {
      ImageRegistry reg = JFaceResources.getImageRegistry();
      reg.put(PSPROBLEMSVIEW_IMG_MESSAGE_INFO, ImageDescriptor.createFromFile(
         Dialog.class, "images/message_info.gif")); //$NON-NLS-1$
      reg.put(PSPROBLEMSVIEW_IMG_MESSAGE_WARNING, ImageDescriptor.createFromFile(
         Dialog.class, "images/message_warning.gif")); //$NON-NLS-1$
      reg.put(PSPROBLEMSVIEW_IMG_MESSAGE_ERROR, ImageDescriptor.createFromFile(
         Dialog.class, "images/message_error.gif")); //$NON-NLS-1$
  }

}
