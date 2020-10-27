/******************************************************************************
 *
 * [ PSSysAndSharedFieldTreeComposite.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form.ce;

import com.percussion.cms.objectstore.PSFieldDefinition;
import com.percussion.design.objectstore.PSContentEditorSharedDef;
import com.percussion.design.objectstore.PSContentEditorSystemDef;
import com.percussion.design.objectstore.PSFieldSet;
import com.percussion.design.objectstore.PSSharedFieldGroup;
import com.percussion.design.objectstore.PSValidationException;
import com.percussion.workbench.ui.IPSUiConstants;
import com.percussion.workbench.ui.PSWorkbenchPlugin;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PSSysAndSharedFieldTreeComposite extends Composite
      implements
         IPSUiConstants
{

   public PSSysAndSharedFieldTreeComposite(Composite parent, int style) {
      super(parent, style);
      setLayout(new FormLayout());

      // Initialize the shared and system def objects
      try
      {
         m_sysDef = PSContentEditorDefinition.getSystemDef();
         m_shDef = PSContentEditorDefinition.getSharedDef();
      }
      catch (Exception e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }

      Label treeLbl = new Label(this, SWT.WRAP);
      final FormData formData = new FormData();
      formData.top = new FormAttachment(0, 0);
      formData.left = new FormAttachment(0, 0);
      treeLbl.setText("Shared and system fields");

      m_treeViewer = new TreeViewer(this, SWT.BORDER | SWT.MULTI);
      m_tree = m_treeViewer.getTree();
      final FormData formData1 = new FormData();
      formData1.bottom = new FormAttachment(100, 0);
      formData1.top = new FormAttachment(treeLbl, COMBO_VSPACE_OFFSET,
            SWT.BOTTOM);
      formData1.right = new FormAttachment(0, 185);
      formData1.left = new FormAttachment(treeLbl, 0, SWT.LEFT);
      m_tree.setLayoutData(formData1);
      m_tree.addSelectionListener(new SelectionAdapter()
      {
         @SuppressWarnings("unused") //param e
         public void widgetSelected(SelectionEvent e)
         {
         }
      });

      m_treeViewer.setContentProvider(new FieldTreeContentProvider());
      m_treeViewer.setLabelProvider(new FieldTreeLabelProvider());
      m_treeViewer.setSorter(new ViewerSorter());
      m_treeViewer.setInput("Root");
   }

   /**
    * Returns the list selected field def objects. If the selection consists of
    * fieldset then all the fields under it will be returned.
    * 
    * @return list of PSFieldDefinition objects may be empty but never
    *         <code>null</code>.
    */
   public List<PSFieldDefinition> getSelectedFieldDefs()
   {
      List<PSFieldDefinition> selections = new ArrayList<PSFieldDefinition>();
      TreeItem[] items = m_tree.getSelection();
      for (int i = 0; i < items.length; i++)
      {
         Object obj = items[i].getData();
         if (obj instanceof PSContentEditorSystemDef
               || obj instanceof PSContentEditorSharedDef
               || items[i].getGrayed())
         {
            continue;
         }
         else if (obj instanceof PSSharedFieldGroup)
         {
            TreeItem[] chitems = items[i].getItems();
            for (int j = 0; j < chitems.length; j++)
            {
               Object chObj = chitems[j].getData();
               if (chObj instanceof PSFieldDefinition)
               {
                  selections.add((PSFieldDefinition) chObj);
               }
            }
         }
         else if (obj instanceof PSFieldDefinition)
         {
            selections.add((PSFieldDefinition) obj);
         }

      }
      return selections;
   }

   /**
    * Removes the selections from the tree.
    */
   public void grayOutSelections()
   {
      TreeItem[] items = m_tree.getSelection();
      for (int i = 0; i < items.length; i++)
      {
         Object obj = items[i].getData();
         if (obj instanceof PSContentEditorSystemDef
               || obj instanceof PSContentEditorSharedDef)
         {
            continue;
         }
         else if (obj instanceof PSSharedFieldGroup)
         {
            TreeItem[] chitems = items[i].getItems();
            for (int j = 0; j < chitems.length; j++)
            {
               Object chObj = chitems[j].getData();
               if (chObj instanceof PSFieldDefinition)
               {
                  chitems[j].setGrayed(true);
               }
            }
            items[i].setGrayed(true);
         }
         else if (obj instanceof PSFieldDefinition)
         {
            items[i].setGrayed(true);
         }

      }
   }

   /**
    * Convenient method to return the deep copy of the supplied list of
    * PSFieldDefinition objects.
    * 
    * @param fieldDefList List of PSFieldDefinition objects assumed not null.
    * @return List of deep copied elements of the supplied list. May be empty
    *         never <code>null</code>.
    */
   private List<PSFieldDefinition> createDeepCopy(
         List<PSFieldDefinition> fieldDefList)
   {
      List<PSFieldDefinition> copiedList = new ArrayList<PSFieldDefinition>();
      for (int i = 0; i < fieldDefList.size(); i++)
      {
         copiedList.add(PSFieldDefinition.deepCopy(fieldDefList.get(i)));
      }
      return copiedList;
   }

   public void dispose()
   {
      super.dispose();
   }

   protected void checkSubclass()
   {
   }

   /**
    * Content provider class for the System and Shared field table tree. If
    * there is any error getting the system and shared fields do not throw the
    * error but log it.
    */
   private class FieldTreeContentProvider implements ITreeContentProvider
   {

      @SuppressWarnings("synthetic-access")
      public Object[] getChildren(Object parentElement)
      {
         Object[] obj =
         {};
         if (parentElement instanceof PSContentEditorSystemDef)
         {
            List<PSFieldDefinition> fd = new ArrayList<PSFieldDefinition>();
            try
            {
               fd = PSContentEditorDefinition.getSystemFieldDefinitions();
               obj = createDeepCopy(fd).toArray(new PSFieldDefinition[0]);
            }
            catch (PSValidationException e)
            {
               String msg = "Validation error occured while getting the system field definitions";
               PSWorkbenchPlugin.handleException(m_logContext, m_logTitle, msg,
                     e);
            }
            catch (Exception e)
            {
               String msg = "Exception occured while getting the system field definitions";
               PSWorkbenchPlugin.handleException(m_logContext, m_logTitle, msg,
                     e);
            }
         }
         else if (parentElement instanceof PSContentEditorSharedDef)
         {
            Iterator iter = ((PSContentEditorSharedDef) parentElement)
                  .getFieldGroups();
            List<PSSharedFieldGroup> temp = new ArrayList<PSSharedFieldGroup>();
            while (iter.hasNext())
            {
               PSSharedFieldGroup sg = (PSSharedFieldGroup) iter.next();
               temp.add(sg);
            }
            obj = temp.toArray();
         }
         else if (parentElement instanceof PSSharedFieldGroup)
         {
            List<PSFieldDefinition> fd = new ArrayList<PSFieldDefinition>();
            fd = PSContentEditorDefinition
                  .getSharedFieldDefinitions((PSSharedFieldGroup) parentElement);
            obj = createDeepCopy(fd).toArray(new PSFieldDefinition[0]);
         }
         else if (parentElement instanceof PSFieldSet)
         {
            obj = ((PSFieldSet) parentElement).getAllFields();
         }
         return obj;
      }

      public Object getParent(@SuppressWarnings("unused") Object element)
      {
         return null;
      }

      public boolean hasChildren(Object element)
      {
         Object[] obj = getChildren(element);
         return obj.length > 0 ? true : false;
      }

      @SuppressWarnings("synthetic-access")
      public Object[] getElements(Object inputElement)
      {
         Object[] obj = {};
         if (inputElement instanceof String)
         {
            if (inputElement.equals("Root"))
            {
               Object defs[] =
               {m_sysDef, m_shDef};
               obj = defs;
            }
         }
         else
         {
            obj = getChildren(inputElement);
         }
         return obj;
      }

      public void dispose()
      {
      }

      @SuppressWarnings("unused")   //all params
      public void inputChanged(Viewer viewer, Object oldInput, 
            Object newInput)
      {
      }

   }

   /**
    * Label provider class for the System and Shared field table tree.
    */
   private class FieldTreeLabelProvider extends LabelProvider
   {
      public String getText(Object element)
      {
         String text = "Dummy";
         if (element instanceof PSContentEditorSystemDef)
         {
            text = "System";
         }
         else if (element instanceof PSContentEditorSharedDef)
         {
            text = "Shared";
         }
         else if (element instanceof PSSharedFieldGroup)
         {
            text = ((PSSharedFieldGroup) element).getFieldSet().getName();
         }
         else if (element instanceof PSFieldSet)
         {
            text = ((PSFieldSet) element).getName();
         }
         else if (element instanceof PSFieldDefinition)
         {
            text = ((PSFieldDefinition) element).getField().getSubmitName();
         }
         return text;
      }
   }

   /**
    * Tree control of the system and shared fields
    */
   private Tree m_tree;

   /**
    * Tree viewer of the system and shared fields
    */
   private TreeViewer m_treeViewer;

   /**
    * Content editor system definition object. Initialized in constructor and
    * never <code>null</code> after that.
    */
   private PSContentEditorSystemDef m_sysDef;

   /**
    * Content editor shared definition object. Initialized in constructor and
    * never <code>null</code> after that.
    */
   private PSContentEditorSharedDef m_shDef;

   /**
    * Constant for context for logging the in case of exceptions
    */
   private String m_logContext = "System and Shared Field Tree";

   /**
    * Constant for title for logging the in case of exceptions
    */
   private String m_logTitle = "System and Shared Field Tree Error";

}
