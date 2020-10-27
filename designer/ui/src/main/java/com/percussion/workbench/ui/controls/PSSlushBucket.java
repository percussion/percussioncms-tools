/******************************************************************************
 *
 * [ PSSlushBucket.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.controls;


import com.percussion.workbench.ui.IPSUiConstants;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSWorkbenchPlugin;
import org.apache.commons.lang.StringUtils;
import org.apache.oro.text.GlobCompiler;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Matcher;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * The slush bucket control is also known as the twin box control. It is comprised
 * of two list components with four buttons in between them. This control sorts
 * both lists by alpha ascending. The available (left) list can also be filtered.
 * @see #filterAvailableList(String)
 * <p>
 * <pre>
 *    +----------------------------------------------------------------+
 *    | +--------------------+                  +--------------------+ |
 *    | |                    |                  |                    | |
 *    | |                    | +--------------+ |                    | |
 *    | |                    | |      >       | |                    | |
 *    | |                    | +--------------+ |                    | |
 *    | |                    | +--------------+ |                    | |
 *    | |                    | |      <       | |                    | |
 *    | |                    | +--------------+ |                    | |
 *    | |                    |                  |                    | |
 *    | |                    | +--------------+ |                    | |
 *    | |                    | |      >>      | |                    | |
 *    | |                    | +--------------+ |                    | |
 *    | |                    | +--------------+ |                    | |
 *    | |                    | |      <<      | |                    | |
 *    | |                    | +--------------+ |                    | |
 *    | |                    |                  |                    | |         
 *    | |                    |                  |                    | |
 *    | +--------------------+                  +--------------------+ |
 *    +----------------------------------------------------------------+
 * 
 * </pre>
 * </p>
 *  
 * @author erikserating
 *
 */
public class PSSlushBucket extends Composite 
   implements IPSUiConstants
{
   /**
    * The ctor 
    * @param parent the parent control for this control
    * @param style the style hints for this control
    * @param availLabel the label for the available (left) list, cannot
    * be <code>null</code> or empty. 
    * @param selectLabel the label for the selected (right) list, cannot
    * be <code>null</code> or empty. 
    * @param labelProvider the label provider to display the list entries,
    * cannot be <code>null</code>.
    */
   public PSSlushBucket(Composite parent, int style, String availLabel, 
      String selectLabel,  ILabelProvider labelProvider)
   {
      this(parent, style, availLabel, selectLabel, null, null, labelProvider);
   }
   
   /**
    * The ctor 
    * @param parent the parent control for this control
    * @param style the style hints for this control
    * @param availLabel the label for the available (left) list, cannot
    * be <code>null</code> or empty. 
    * @param selectLabel the label for the selected (right) list, cannot
    * be <code>null</code> or empty. 
    * @param availableItems list of objects representing
    * items that will appear in the available (left) list. May be
    * <code>null</code>or empty.
    * @param selections list of  objects representing
    * items that will appear in the selected (right) list. may be
    * <code>null</code> or empty.
    * @param labelProvider the label provider to display the list entries,
    * cannot be <code>null</code>.
    */
   public PSSlushBucket(Composite parent, int style, String availLabel, 
      String selectLabel, java.util.List availableItems,
      java.util.List selections, ILabelProvider labelProvider)
   {
      super(parent, style);
      if(StringUtils.isBlank(availLabel))
         throw new IllegalArgumentException("availLabel cannot be null or empty."); //$NON-NLS-1$
      if(StringUtils.isBlank(selectLabel))
         throw new IllegalArgumentException("selectLabel cannot be null or empty.");       //$NON-NLS-1$
      if(labelProvider == null)
         throw new IllegalArgumentException("Label provider cannot be null."); //$NON-NLS-1$
      
      setLayout(new FormLayout());
      
      // Available values composite
      final Composite availableComp = new Composite(this, SWT.NONE);
      final FormData formData = new FormData();
      formData.bottom = new FormAttachment(100, 0);
      formData.right = new FormAttachment(45, 0);
      formData.top = new FormAttachment(0, 0);
      formData.left = new FormAttachment(0, 0);
      availableComp.setLayoutData(formData);
      availableComp.setLayout(new FormLayout());

      final Label availableLabel = new Label(availableComp, SWT.NONE);
      final FormData formData_1 = new FormData();      
      formData_1.right = new FormAttachment(100, 0);
      formData_1.top = new FormAttachment(0, 0);
      formData_1.left = new FormAttachment(0, 0);
      availableLabel.setLayoutData(formData_1);
      availableLabel.setText(availLabel);

      m_availableListViewer = 
         new ListViewer(availableComp, SWT.V_SCROLL | SWT.MULTI | SWT.BORDER);
      m_availableListViewer.setContentProvider(new PSDefaultContentProvider());
      m_availableListViewer.setLabelProvider(labelProvider);
      m_availableListViewer.setSorter(new ViewerSorter());     
      m_availableList = m_availableListViewer.getList();
      m_availableList.addSelectionListener(new SelectionAdapter() {
         @SuppressWarnings("synthetic-access") //$NON-NLS-1$
         public void widgetSelected(SelectionEvent e)
         {
            m_selectedList.deselectAll();
            updateButtonStatus();
         }
      });
      m_availableList.addFocusListener(new InternalFocusListener());
      final FormData formData_2 = new FormData();
      formData_2.bottom = new FormAttachment(100, 0);
      formData_2.right = new FormAttachment(100, 0);
      formData_2.top = new FormAttachment(availableLabel, 0, SWT.BOTTOM);
      formData_2.left = new FormAttachment(0, 0);
      m_availableList.setLayoutData(formData_2);
      
      // filler composite
      final Composite fillerComp1 = new Composite(this, SWT.NONE);
      fillerComp1.setLayout(new FormLayout());
      final FormData formData_100 = new FormData();
      formData_100.right = new FormAttachment(55, 0);
      formData_100.top = new FormAttachment(0, 13);
      formData_100.left = new FormAttachment(availableComp, 0, SWT.RIGHT);
      formData_100.bottom = new FormAttachment(100, 0);
      fillerComp1.setLayoutData(formData_100);
     
      //filler composite      
      final Composite fillerComp2 = new Composite(fillerComp1, SWT.NONE);
      final FormData formData_200 = new FormData();
      formData_200.bottom = new FormAttachment(100, 0);
      formData_200.top = new FormAttachment(0, 0);
      formData_200.left = new FormAttachment(0, 0);
      formData_200.right = new FormAttachment(0,0);
      fillerComp2.setLayoutData(formData_200);
      
      // Button composite
      final Composite buttonComp = new Composite(fillerComp1, SWT.NONE);
      final FormData formData_4 = new FormData();
      formData_4.right = new FormAttachment(100, 0);
      formData_4.top = new FormAttachment(fillerComp2, 25, SWT.TOP);
      formData_4.left = new FormAttachment(0, 0);      
      buttonComp.setLayoutData(formData_4);
      buttonComp.setLayout(new FormLayout());
      
      
      
      m_selectButton = PSButtonFactory.createRightButton(buttonComp);
      m_selectButton.addSelectionListener(new SelectionAdapter() {
         public void widgetSelected(SelectionEvent e)
         {
            onSelect();
         }
      });
      final FormData formData_5 = new FormData();
      formData_5.width = BUTTON_WIDTH;
      formData_5.height = BUTTON_HEIGHT;
      formData_5.right = new FormAttachment(100,-BUTTON_HSPACE_OFFSET);
      formData_5.top = new FormAttachment(0, 0);
      formData_5.left = new FormAttachment(0, BUTTON_HSPACE_OFFSET);
      m_selectButton.setLayoutData(formData_5);
      m_selectButton.setToolTipText(PSMessages.getString(
         "PSSlushBucket.select.tooltip")); //$NON-NLS-1$

      m_unselectButton = PSButtonFactory.createLeftButton(buttonComp);
      m_unselectButton.addSelectionListener(new SelectionAdapter() {
         public void widgetSelected(SelectionEvent e)
         {
            onUnSelect();
         }
      });
      final FormData formData_6 = new FormData();     
      formData_6.width = BUTTON_WIDTH;
      formData_6.height = BUTTON_HEIGHT;
      formData_6.left = new FormAttachment(0, BUTTON_HSPACE_OFFSET);
      formData_6.right = new FormAttachment(100,-BUTTON_HSPACE_OFFSET);
      formData_6.top = new FormAttachment(m_selectButton, 2, SWT.BOTTOM);
      new FormAttachment(0, 0);
      m_unselectButton.setLayoutData(formData_6);
      m_unselectButton.setToolTipText(PSMessages.getString(
         "PSSlushBucket.unselect.tooltip")); //$NON-NLS-1$

      m_selectallButton = PSButtonFactory.createDoubleRightButton(buttonComp);
      m_selectallButton.addSelectionListener(new SelectionAdapter() {
         public void widgetSelected(SelectionEvent e)
         {
            onSelectAll();
         }
      });
      final FormData formData_7 = new FormData();
      formData_7.width = BUTTON_WIDTH;
      formData_7.height = BUTTON_HEIGHT;
      formData_7.right = new FormAttachment(100,-BUTTON_HSPACE_OFFSET);
      formData_7.top = new FormAttachment(m_unselectButton, 2, SWT.BOTTOM);
      formData_7.left = new FormAttachment(0, BUTTON_HSPACE_OFFSET);
      m_selectallButton.setLayoutData(formData_7);
      m_selectallButton.setToolTipText(PSMessages.getString(
         "PSSlushBucket.selectall.tooltip")); //$NON-NLS-1$

      m_unselectallButton = PSButtonFactory.createDoubleLeftButton(buttonComp);
      m_unselectallButton.addSelectionListener(new SelectionAdapter() {
         public void widgetSelected(SelectionEvent e)
         {
            onUnSelectAll();
         }
      });
      final FormData formData_8 = new FormData();
      formData_8.width = BUTTON_WIDTH;
      formData_8.height = BUTTON_HEIGHT;
      formData_8.right = new FormAttachment(100,-BUTTON_HSPACE_OFFSET);
      formData_8.top = new FormAttachment(m_selectallButton, 2, SWT.BOTTOM);
      formData_8.left = new FormAttachment(0, BUTTON_HSPACE_OFFSET);
      m_unselectallButton.setLayoutData(formData_8);
      m_unselectallButton.setToolTipText(PSMessages.getString(
         "PSSlushBucket.unselectall.tooltip")); //$NON-NLS-1$
      
      // Selected values composite
      final Composite selectedComp = new Composite(this, SWT.NONE);
      final FormData formData_3 = new FormData();
      formData_3.bottom = new FormAttachment(100, 0);
      formData_3.top = new FormAttachment(0, 0);
      formData_3.right = new FormAttachment(100, 0);
      formData_3.left = new FormAttachment(fillerComp1, 0, SWT.RIGHT);
      selectedComp.setLayoutData(formData_3);
      selectedComp.setLayout(new FormLayout());

      final Label selectedLabel = new Label(selectedComp, SWT.NONE);
      final FormData formData_1_1 = new FormData();
      formData_1_1.top = new FormAttachment(0, 0);
      formData_1_1.right = new FormAttachment(100, 0);
      formData_1_1.left = new FormAttachment(0, 0);
      selectedLabel.setLayoutData(formData_1_1);
      selectedLabel.setText(selectLabel);

      m_selectedListViewer = 
         new ListViewer(selectedComp, SWT.V_SCROLL | SWT.MULTI | SWT.BORDER);
      m_selectedListViewer.setContentProvider(new PSDefaultContentProvider());
      m_selectedListViewer.setLabelProvider(labelProvider);
      m_selectedListViewer.setSorter(new ViewerSorter());
      m_selectedList = m_selectedListViewer.getList();
      m_selectedList.addSelectionListener(new SelectionAdapter() {
         @SuppressWarnings("synthetic-access") //$NON-NLS-1$
         public void widgetSelected(SelectionEvent e)
         {
            m_availableList.deselectAll();
            updateButtonStatus();
         }
      });
      m_selectedList.addFocusListener(new InternalFocusListener());
      final FormData formData_2_1 = new FormData();
      formData_2_1.bottom = new FormAttachment(100, 0);
      formData_2_1.top = new FormAttachment(selectedLabel, 0, SWT.BOTTOM);
      formData_2_1.right = new FormAttachment(100, 0);
      formData_2_1.left = new FormAttachment(0, 0);
      m_selectedList.setLayoutData(formData_2_1);
      
      setValues(availableItems, selections);
      //

   }
  
   
   
   /* 
    * @see org.eclipse.swt.widgets.Composite#computeSize(int, int, boolean)
    */
   @Override
   public Point computeSize(int w, int h, boolean bool)
   {      
      // Suggest that the control have an initial width of 
      // WIDTH_HINT      
      return super.computeSize(WIDTH_HINT, h, bool);
   }

   /**
    * Method called when the select button is hit
    */
   protected void onSelect()
   {
      moveSelection(m_availableListViewer, m_selectedListViewer, false); 
   }
      
   /**
    * Method called when the select all button is hit
    */
   protected void onSelectAll()
   {
      moveSelection(m_availableListViewer, m_selectedListViewer, true);
   }
   
   /**
    * Method called when the unselect button is hit
    */
   protected void onUnSelect()
   {
      moveSelection(m_selectedListViewer, m_availableListViewer, false);
   }
   
   /**
    * Method called when the unselect  all button is hit
    */
   protected void onUnSelectAll()
   {
      moveSelection(m_selectedListViewer, m_availableListViewer, true);
   }   
   
   /**
    * Get all the selected items for this control.
    * @return list of selected objects, never
    * <code>null</code>, may be empty. This list is not sorted.
    */
   @SuppressWarnings("unchecked") //$NON-NLS-1$
   public java.util.List getSelections()
   {
      java.util.List input = (java.util.List)m_selectedListViewer.getInput();
      
      return input != null ? input : m_selectedItems;
   }
   
   /**
    * Set all the selected items for this control.
    * @param selections list of selected objects, may be <code>null</code>.
    */
   @SuppressWarnings("unchecked") //$NON-NLS-1$
   protected void setSelections(java.util.List selections)
   {
      m_selectedListViewer.setInput(selections);
      m_selectedItems = selections != null ? 
            (java.util.List) m_selectedListViewer.getInput() : new ArrayList();
   }
   
   /**
    * Get all the available items for this control.
    * @return list of available objects, never
    * <code>null</code>, may be empty. This list is not sorted.
    */
   @SuppressWarnings("unchecked") //$NON-NLS-1$
   public java.util.List getAvailable()
   {
      java.util.List input = (java.util.List)m_availableListViewer.getInput();
      
      return input != null ? input : m_availableItems;
   }
   
   /**
    * Set all the available items for this control.
    * @param available list of available objects, may be <code>null</code>.
    */
   @SuppressWarnings("unchecked") //$NON-NLS-1$   
   protected void setAvailable(java.util.List available)
   {
      m_availableListViewer.setInput(available);
      m_availableItems = available != null ? 
            (java.util.List) m_availableListViewer.getInput() : new ArrayList();
   }
   
   /**
    * Sets the values for the two list boxes in this control
    * @param available list of objects representing
    * items that will appear in the available (left) list. May be
    * <code>null</code> or empty.
    * @param selections list of objects representing
    * items that will appear in the selected (right) list. May be
    * <code>null</code> or empty.
    */
   @SuppressWarnings("unchecked") //$NON-NLS-1$
   public void setValues(java.util.List available, java.util.List selections)
   {
      java.util.List clonedSelections = new ArrayList();
      java.util.List clonedAvailable = new ArrayList();
      if(available != null)
         clonedAvailable.addAll(available);
      
      if(selections != null)
      {
         clonedSelections.addAll(selections);
         // remove any items that exist in both lists from the available
         // list
         Iterator it = clonedSelections.iterator();
         while(it.hasNext())
         {
            Object obj = it.next();
            if(clonedAvailable.contains(obj))
               clonedAvailable.remove(obj);
         }
      }
      
      setSelections(clonedSelections);
      setAvailable(clonedAvailable);
    
      updateButtonStatus();
   }
   
   /**
    * Filters the available (Left side) list by the pattern passed
    * in. Case insensitive.
    * @param pattern the glob type pattern to filter the list by.
    * The wildcard characters * and ? are allowed as well. If
    * <code>null</code> or empty, then no filtering will occur.
    * 
    */
   public void filterAvailableList(String pattern)
   {
      if(m_lastFilter != null)
         m_availableListViewer.removeFilter(m_lastFilter);
      if(StringUtils.isBlank(pattern) || pattern.trim().equals("*")) //$NON-NLS-1$
      {
         m_lastFilter = null;
      }
      else
      {
         m_lastFilter = new AvailableListFilter(pattern);
         m_availableListViewer.addFilter(m_lastFilter);
      }
      
   }
   
   /**
    * Add a selection listener to be notified when a selection
    * event occurs.
    * @param listener cannot be <code>null</code>.
    */
   public void addSelectionListener(SelectionListener listener)
   {
      if(listener == null)
         throw new IllegalArgumentException("listener cannot be null"); //$NON-NLS-1$
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
         throw new IllegalArgumentException("listener cannot be null"); //$NON-NLS-1$
      if(m_selectionListeners.contains(listener))
         m_selectionListeners.remove(listener);
   }
   
   /**
    * Fires a <code>SelectionEvent</code> for all registered 
    * <code>SelectionListeners</code>.
    */
   private void fireSelectionEvent()
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
   public void dispose()
   {
      super.dispose();
   }

   @Override
   protected void checkSubclass()
   {
      // Disable the check that prevents subclassing of SWT components
   }
   
   /**
    * Utility method to move items from one list viewer to another.
    * @param fromList
    * @param toList
    * @param moveAll
    */
   @SuppressWarnings("unchecked") //$NON-NLS-1$
   private void moveSelection(
      ListViewer fromList, ListViewer toList, boolean moveAll)
   {
      java.util.List from = 
         (java.util.List)fromList.getInput();
      java.util.List to = 
         (java.util.List)toList.getInput();
      if(moveAll && !from.isEmpty())
         fromList.getList().selectAll();
      if(fromList.getList().getSelectionCount() > 0)
      {
         StructuredSelection selection = 
            (StructuredSelection)fromList.getSelection();
         Iterator it = selection.iterator();
         while(it.hasNext())
         {
            Object obj = it.next();           
            from.remove(obj);
            to.add(obj);
         }
         
         fireSelectionEvent();         
         fromList.refresh();
         toList.refresh();
         updateButtonStatus();
      }      
  
   }
   
   /**
    * Updates the enable status of the buttons
    */
   protected void updateButtonStatus()
   {
      m_selectButton.setEnabled(m_availableList.getSelectionCount() > 0);
      m_unselectButton.setEnabled(m_selectedList.getSelectionCount() > 0);
      m_selectallButton.setEnabled(m_availableList.getItemCount() > 0);
      m_unselectallButton.setEnabled(m_selectedList.getItemCount() > 0);      
   }
  
   /**
    * Refreshes the two lists of this control.
    */
   protected void refreshLists()
   {
      m_availableListViewer.refresh();
      m_selectedListViewer.refresh();
   }
   
   /**
    * Used to get the currently selected item from the list of selections.
    * @return selected item as a {@link StructuredSelection} object.
    */
   protected StructuredSelection getSelection()
   {
      return (StructuredSelection) m_selectedListViewer.getSelection();
   }
   
   /**
    * Viewer filter that filters the available list by the glob type
    * pattern passed in. This matching is case insensitive.
    */
   class AvailableListFilter extends ViewerFilter
   {

      /**
       * The ctor
       * @param pattern the pattern, assumed not <code>null</code>.
       */
      AvailableListFilter(final String pattern)
      {
         mi_pattern = pattern;    
      }
      
      /* 
       * @see org.eclipse.jface.viewers.ViewerFilter#select(
       * org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
       */
      @Override
      public boolean select(Viewer viewer, Object parentElement, Object element)
      {
         ILabelProvider provider = 
            (ILabelProvider)((ListViewer)viewer).getLabelProvider();
         String value = provider.getText(element);            
         try
         {
            return isMatch(value.toLowerCase(), mi_pattern.toLowerCase());
         }
         catch(MalformedPatternException e)
         {
            PSWorkbenchPlugin.handleException(null, null, null, e);
         }
         return false;
      }
      
      /**
       * Matches the passed in string against the passed in Glob type
       * expression.
       * @param str the string to match against the GlobType expression
       * @param gExp the globtype expression
       * @return <code>true</code> if a match is found
       * @throws MalformedPatternException upon pattern compilation error
       */
      private boolean isMatch(String str, String gExp)
         throws MalformedPatternException
      {
         PatternCompiler compiler = new GlobCompiler();
         PatternMatcher matcher  = new Perl5Matcher();

         // may throw MalformedPatternException
         Pattern pattern = compiler.compile(gExp + "*"); //$NON-NLS-1$

         return matcher.matches(str, pattern);
      } 
      
      private String mi_pattern;
   }
   
   /**
    * List control displaying available items.
    */
   public List getAvailableList()
   {
      return m_availableList;
   }
   
   /**
    * List control displaying available items.
    */
   public List getSelectedList()
   {
      return m_selectedList;
   }
   
   /**
    * Adds a filter for the available list.
    * @param filter cannot be <code>null</code>.
    */
   public void addAvailableListFilter(ViewerFilter filter)
   {
      if(filter == null)
         throw new IllegalArgumentException("filter cannot be null"); //$NON-NLS-1$
      if(m_availableListViewer != null)
      {
         m_availableListViewer.addFilter(filter);
      }
   }
   
   /**
    * Removes the specified filter from the available list.
    * @param filter cannot be <code>null</code>.
    */
   public void removeAvailableListFilter(ViewerFilter filter)
   {
      if(filter == null)
         throw new IllegalArgumentException("filter cannot be null"); //$NON-NLS-1$
      if(m_availableListViewer != null)
      {
         m_availableListViewer.removeFilter(filter);
      }
   }
   
   class InternalFocusListener implements FocusListener
   {

      @SuppressWarnings("synthetic-access")
      public void focusGained(FocusEvent e)
      {
         fireFocusEvent(true);
         
      }

      @SuppressWarnings("synthetic-access")
      public void focusLost(FocusEvent e)
      {
         fireFocusEvent(false);         
      }
      
   }

   /**
    * Controls
    */
   private List m_selectedList;
   private List m_availableList;
   private ListViewer m_availableListViewer;
   private ListViewer m_selectedListViewer;
   private ViewerFilter m_lastFilter;
   private Button m_selectButton;
   private Button m_unselectButton;
   private Button m_selectallButton;
   private Button m_unselectallButton;
   
   /**
    * List of selection listeners
    */
   private java.util.List<SelectionListener> m_selectionListeners = 
      new ArrayList<SelectionListener>();
   
   /**
    * List of all focus listeners registered to this control.
    */
   private java.util.List<FocusListener> m_focusListeners = 
      new ArrayList<FocusListener>();

   /**
    * List of selected items, never <code>null</code>.  When the contents of
    * {@link #m_selectedListViewer} are modified in
    * {@link #setSelections(java.util.List)}, this list is also modified
    * accordingly.
    */
   private java.util.List m_selectedItems = new ArrayList();
   
   /**
    * List of available items, never <code>null</code>.  When the contents of
    * {@link #m_availableListViewer} are modified in
    * {@link #setAvailable(java.util.List)}, this list is also modified
    * accordingly.
    */
   private java.util.List m_availableItems = new ArrayList();
   
   @SuppressWarnings("unused") //$NON-NLS-1$
   private static final int MIN_HEIGHT = 130;
   
   private static final int WIDTH_HINT = 500;


   


}
