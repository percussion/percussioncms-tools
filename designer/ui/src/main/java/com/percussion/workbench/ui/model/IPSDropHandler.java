/******************************************************************************
 *
 * [ IPSDropHandler.java ]
 * 
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.model;

import com.percussion.workbench.ui.PSUiReference;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.TransferData;

/**
 * This interface is provided to allow the base framework behavior to be
 * overridden. This handler would be used by classes that implement
 * {@link com.percussion.workbench.ui.model.IPSDeclarativeNodeHandler} ( usually
 * by extending
 * {@link com.percussion.workbench.ui.handlers.PSDeclarativeNodeHandler}.) The
 * derived class would create their instance and return it via the
 * {@link com.percussion.workbench.ui.model.IPSDeclarativeNodeHandler#getDropHandler()}.
 * <p>
 * The framework provides a starting point via the inner class
 * <code>NodeDropHandler</code>.
 * 
 * @author paulhoward
 */
public interface IPSDropHandler
{
   /**
    * If this node supports the supplied op, then it is returned, otherwise, a
    * default op is returned. For example, if this node was a variant and the
    * source was a slot, if <code>DND.DROP_MOVE</code> was supplied,
    * <code>DND.DROP_COPY</code> would be returned.
    * 
    * @param desiredOp One of the {@link org.eclipse.swt.dnd.DND} constants of
    * the form <code>DROP_xxx</code> (e.g.
    * {@link org.eclipse.swt.dnd.DND#DROP_COPY}.
    * 
    * @return Either the supplied operation, or the default one if the supplied
    * op is not supported. 
    */
   public int getValidDndOperation(int desiredOp);
   
   /**
    * After the framework has determined that the specified drop could be 
    * performed, this method is called to allow additional checking (e.g. the
    * data in the object may need to be checked.)
    * <p>
    * See the
    * {@link ViewerDropAdapter#validateDrop(Object, int, TransferData) class}
    * for parameter descriptions.
    */
   public boolean validateDrop(PSUiReference target, int operation,
         TransferData transferType);
   
   /**
    * Performs whatever actions are necessary to fulfill the actions as
    * specified in <code>op</code> using the supplied data.
    * 
    * @param target The object that is receiving the drop, never
    * <code>null</code>.
    * 
    * @param op One of the <code>DND.DROP_xxx</code> values (e.g.
    * {@link org.eclipse.swt.dnd.DND#DROP_COPY}.)
    * 
    * @param data The objects that were originally dragged. Never
    * <code>null</code>.
    */
   public boolean performDrop(PSUiReference target, int op, Object data);
}
