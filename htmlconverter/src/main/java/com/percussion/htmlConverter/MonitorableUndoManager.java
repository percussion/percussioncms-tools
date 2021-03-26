/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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
