/******************************************************************************
 *
 * [ PSCheckBoxNode.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.packager.ui;

import com.percussion.packager.ui.data.PSElementNode;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.ArrayList;
import java.util.List;

class PSCheckBoxNode {
   PSElementNode elem;

  public PSCheckBoxNode(PSElementNode elem) {
    this.elem = elem;
  }

  public boolean isSelected() {
    return elem.isSelected();
  }

  public void setSelected(boolean newValue) {
    elem.setSelected(newValue);
    fireSelectionChanged();
  }

  public String getText() {
    return elem.getName();
  }

//  public void setText(String newValue) {
//    text = newValue;
//  }

  public String toString() {
    return getClass().getName() + "[" + elem.getName() + "/" + elem.isSelected() + "]";
  }
  
  public void addChangeListener(ChangeListener listener)
  {
     if(listener == null)
        throw new IllegalArgumentException("listener cannot be null.");
     if(!m_listeners.contains(listener))
        m_listeners.add(listener);
  }
  
  private void fireSelectionChanged()
  {
     for(ChangeListener listener : m_listeners)
     {
        ChangeEvent event = new ChangeEvent(this);
        listener.stateChanged(event);
     }
  }
  
  public void removeChangeListener(ChangeListener listener)
  {
     if(listener == null)
        throw new IllegalArgumentException("listener cannot be null.");
     if(m_listeners.contains(listener))
        m_listeners.remove(listener);
  }
  
  
  
  private List<ChangeListener> m_listeners = new ArrayList<ChangeListener>();
}