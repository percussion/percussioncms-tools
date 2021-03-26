/******************************************************************************
 *
 * [ PSStackedFieldFormLayoutManager.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.layouts;

import org.apache.commons.collections.map.ListOrderedMap;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A class to help manage a stack of fields that may be
 * shown or hidden. If hidden the next visible control should
 * move up to take the place of the hidden control.
 */
public class PSStackedFieldFormLayoutManager
{
   public PSStackedFieldFormLayoutManager(Composite comp)
   {
      if(comp == null)
         throw new IllegalArgumentException("comp cannot be null.");
      m_comp = comp;
   }
   
   /**
    * Convenience method that calls {@link #addControl(Control, boolean, int, 
    * FormAttachment, FormAttachment, FormAttachment, int, int) addControl(
    * control, show, topOffset, left, right, bottom, -1, -1)}
    */
   public void addControl(Control control, boolean show, int topOffset, 
      FormAttachment left, FormAttachment right, FormAttachment bottom)
   {
      addControl(control, show, topOffset, left, right, bottom, -1, -1);
   }
   
   /**
    * Adds a control to the manager creating the form data from the
    * passed in information and form attachments.
    * @param control the control to add, cannot be <code>null</code>.
    * @param show show flag indicating that the control should be shown
    * initially.
    * @param topOffset the spacing between the top of this control and
    * whatever it is attached to.
    * @param left the left form attachment, if <code>null</code> then
    * it will attempt to attach itself to the left side of the last
    * visible control.
    * @param right the right form attachment, if <code>null</code> then
    * it will attempt to attach itself to the right side of the last
    * visible control.
    * @param bottom the right form attachment, may be <code>null</code>.
    * @param height the form attachement height for this control.
    * Use -1 to take the prefered height of the control.
    * @param width the form attachement width for this control.
    * Use -1 to take the prefered width of the control.
    */
   public void addControl(Control control, boolean show, int topOffset, 
      FormAttachment left, FormAttachment right, FormAttachment bottom,
      int height, int width)
   {
      if(control == null)
         throw new IllegalArgumentException("control cannot be null");
      Control last = m_controls.isEmpty() ? null :
         (Control)m_controls.lastKey();
      FormData data = new FormData();
      data.top = last == null 
         ? new FormAttachment(0, topOffset) 
            : new FormAttachment(last, topOffset, SWT.BOTTOM);
      if(last != null && left == null)
         data.left = new FormAttachment(last, 0, SWT.LEFT);
      else
         data.left = left;
      if(last != null && right == null)
         data.right = new FormAttachment(last, 0, SWT.RIGHT);
      else
         data.right = right;
      data.bottom = bottom;
      data.height = height;
      data.width = width;
      
      m_controls.put(control, data);
      control.setLayoutData(data);
      m_status.put(control, show);
      refreshLayout();
   }
   
   /**
    * Shows or hides the control based on the passed in flag
    * @param control the control to hide/show, cannot be <code>null</code>.
    * @param show flag indicating that the control should be shown.
    */
   public void showControl(Control control, boolean show)
   {
      if(control == null)
         throw new IllegalArgumentException("control cannot be null");
      m_status.put(control, show);
      refreshLayout();
   } 
   
   /**
    * Recalculates the layout data based on each control's visibility
    * status and then refreshes the layout.
    */
   @SuppressWarnings("unchecked")
   private void refreshLayout()
   {
      Set<Control> controls = m_controls.keySet();
      Control lastShowingControl = null;
      for(Control control : controls)
      {
         boolean show = m_status.get(control);
         FormData data = null;
         if(show)
         {
             data = cloneFormData((FormData)m_controls.get(control));
             int topOffset = data.top.offset;
            if(lastShowingControl != null)
               data.top = new FormAttachment(lastShowingControl, topOffset,
                  SWT.BOTTOM);
            else
            {
               data.top = cloneFormData((FormData)m_controls.getValue(0)).top;   
            }
            lastShowingControl = control;
            
         }
         else
         {
            data = new FormData();
            data.top = new FormAttachment(0, -1);
            data.bottom = new FormAttachment(0, -1);
         }
         control.setLayoutData(data);
         m_comp.layout();
         
      }
      
   }
   
   /**
    * Clones a <code>FormData</code>object.
    * @param data assumed not <code>null</code>.
    * @return the cloned form data object.
    */
   private FormData cloneFormData(FormData data)
   {
      FormData clone = new FormData();
      clone.bottom = data.bottom;
      clone.height = data.height;
      clone.left = data.left;
      clone.right = data.right;
      clone.top = data.top;
      clone.width = data.width;
      return clone;
   }
   
   
   
   private Composite m_comp;
   private ListOrderedMap m_controls = new ListOrderedMap();
   private Map<Control, Boolean> m_status = new HashMap<Control, Boolean>();
   
}
