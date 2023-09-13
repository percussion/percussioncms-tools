/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.percussion.htmlConverter;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

public class MonitorableUndoManager extends UndoManager {

   // List of listeners for events from this object
   protected EventListenerList listenerList = new EventListenerList();

   // A ChangeEvent dedicated to a single MonitorableUndoManager
   protected ChangeEvent changeEvent;
    
   // Super class overrides
   public synchronized void setLimit(int l) {
        super.setLimit(l);
      fireChangeEvent();
    }

   public synchronized void discardAllEdits() {
      super.discardAllEdits();
      fireChangeEvent();
   }

   public synchronized void undo() throws CannotUndoException {
      super.undo();
      fireChangeEvent();
   }
   
   public synchronized void redo() throws CannotRedoException {
      super.redo();
      fireChangeEvent();
   }

   public synchronized boolean addEdit(UndoableEdit anEdit) {
      boolean retval = super.addEdit(anEdit);
      fireChangeEvent();
      return retval;
   }

   // Support for ChangeListeners
   public void addChangeListener(ChangeListener l) {
      listenerList.add(ChangeListener.class, l);
   }

   public void removeChangeListener(ChangeListener l) {
      listenerList.remove(ChangeListener.class, l);
   }

   protected void fireChangeEvent() {
      Object[] listeners = listenerList.getListenerList();
      for (int i = listeners.length - 2; i >= 0; i -= 2) {
         if (listeners[i] == ChangeListener.class) {
            if (changeEvent == null) {
               changeEvent = new ChangeEvent(this);
            }
            ((ChangeListener)listeners[i+1]).stateChanged(changeEvent);
         }
      }
   }
}
