/******************************************************************************
 *
 * [ PSFilteredSlushBucketControl.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.controls;

import com.percussion.E2Designer.PSDlgUtil;
import com.percussion.client.IPSReference;
import com.percussion.client.PSModelException;
import com.percussion.workbench.ui.PSMessages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.percussion.workbench.ui.IPSUiConstants.COMMON_BORDER_OFFSET;
import static com.percussion.workbench.ui.IPSUiConstants.LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET;
import static com.percussion.workbench.ui.IPSUiConstants.LABEL_HSPACE_OFFSET;
import static com.percussion.workbench.ui.IPSUiConstants.ONE_THIRD;

/**
 * A base class for a control presenting a filtered {@link PSSlushBucket}.
 *
 * @author Andriy Palamarchuk
 */
public abstract class PSFilteredSlushBucketControl extends Composite
{
   public PSFilteredSlushBucketControl(Composite parent, int style)
   {
      super(parent, style);
      setLayout(new FormLayout());
      final Label filterLabel = createFilterLabel();
      m_selectionControl = createSelectionControl(filterLabel);
      m_filterText = createFilterText(filterLabel);
      
      m_filterText.addModifyListener(new ModifyListener()
         {
            public void modifyText(ModifyEvent e)
            {               
               m_selectionControl.filterAvailableList(
                  m_filterText.getText());   
            }         
         });

   }
   
   /**
    * Initializes list of originally selected values.
    */
   private void initializeOriginallySelectedItems(final Object designObject)
   {
      try
      {
         m_originallySelectedItems = createOriginallySelectedItems(designObject);
      }
      catch (PSModelException e)
      {
         PSDlgUtil.showError(e);
         m_originallySelectedItems = new ArrayList<IPSReference>();
      }
      Collections.sort(m_originallySelectedItems, new IPSReference.LabelKeyComparator());
   }

   /**
    * Initializes {@link #m_availableItems} with data to use for the available
    * list.
    */
   private void initializeAvailableItems(final Object designObject)
   {
      try
      {
         m_availableItems = createAvailableItems(designObject);
      }
      catch (PSModelException e)
      {
         PSDlgUtil.showError(e);
         m_availableItems = new ArrayList<IPSReference>();
      }
      Collections.sort(m_availableItems, new IPSReference.LabelKeyComparator());
   }

   /**
    * Text of the label for selected items. Should be provided by subclass.
    */
   public abstract String getSelectedLabelText();

   /**
    * Text of the label for available items. Should be provided by subclass.
    * Called during {@link #loadControlValues(Object)}.
    */
   protected abstract String getAvailableLabelText();
   
   /**
    * Items to populate "available" list with after removing selected items
    * from {@link #createOriginallySelectedItems(Object)}.
    * Called during {@link #loadControlValues(Object)}.
    */
   protected abstract List<IPSReference> createAvailableItems(final Object designObject)
         throws PSModelException;
   
   /**
    * Items to initially populate "selected" list with.
    */
   protected abstract List<IPSReference> createOriginallySelectedItems(final Object designObject) throws PSModelException;

   /**
    * Creates filter text control.
    */
   private Text createFilterText(final Label filterLabel)
   {
      final Text text = new Text(this, SWT.BORDER);
      {
         final FormData formData = new FormData();      
         formData.left = new FormAttachment(filterLabel, LABEL_HSPACE_OFFSET, SWT.DEFAULT);
         formData.right = new FormAttachment(ONE_THIRD, 0);
         formData.top = new FormAttachment(filterLabel,
               -LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.TOP);
         text.setLayoutData(formData);
      }
      return text;
   }
   
   /**
    * Creates filter label.
    */
   private Label createFilterLabel()
   {
      final Label filterLabel = new Label(this, SWT.NONE);
      filterLabel.setText(
            getMessage("common.label.filter") + ':'); //$NON-NLS-1$
      {
         final FormData formData = new FormData();      
         formData.left = new FormAttachment(0);
         formData.bottom = new FormAttachment(100, -COMMON_BORDER_OFFSET);
         filterLabel.setLayoutData(formData);
      }
      return filterLabel;
   }

   /**
    * Creates the control which allows user to make selections.
    */
   private PSSlushBucket createSelectionControl(final Label filterLabel)
   {
      final PSSlushBucket selectionControl = new PSSlushBucket(this, SWT.NONE, 
            getAvailableLabelText() + ':',
            getSelectedLabelText() + ':',
            new PSReferenceLabelProvider());
      {
         final FormData formData = new FormData();
         formData.left = new FormAttachment(0, 0);
         formData.right = new FormAttachment(100, 0);
         formData.top = new FormAttachment(0, 0);
         formData.bottom = 
            new FormAttachment(filterLabel,
                  -(LABEL_HSPACE_OFFSET
                        +  LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET),
                  SWT.TOP);
         selectionControl.setLayoutData(formData);
      }
      return selectionControl;
   }

   /**
    * Should be called to initialize UI with data from the provided object.
    */
   public void loadControlValues(final Object designObject)
   {
      initializeAvailableItems(designObject);
      initializeOriginallySelectedItems(designObject);
      m_selectionControl.setValues(getAvailableItemsToShow(), m_originallySelectedItems);
   }

   /**
    * Available items after substraction of {@link #m_originallySelectedItems}.
    * @return
    */
   private List<IPSReference> getAvailableItemsToShow()
   {
      final List<IPSReference> items = new ArrayList<IPSReference>();
      for (final IPSReference reference : m_availableItems)
      {
         if (!m_originallySelectedItems.contains(reference))
         {
            items.add(reference);
         }
      }
      return items;
   }

   /**
    * Convenience method to get message by specified key.
    */
   protected static String getMessage(final String key)
   {
      return PSMessages.getString(key);
   }

   /**
    * Selected items.
    */
   protected Set<IPSReference> getSelections()
   {
      final Set<IPSReference> selections = new HashSet<IPSReference>();
      for (final Object reference : m_selectionControl.getSelections())
      {
         selections.add((IPSReference) reference);
      }
      return selections;
   }

   /**
    * Control showing selections.
    */
   public PSSlushBucket getSelectionControl()
   {
      return m_selectionControl;
   }

   /**
    * Component to select items.
    */
   public PSSlushBucket m_selectionControl;

   /**
    * Filter UI control.
    */
   private Text m_filterText;
   
   /**
    * Data to populate available items list.
    */
   protected List<IPSReference> m_availableItems;

   /**
    * Originally selected items.
    */
   private List<IPSReference> m_originallySelectedItems;
}
