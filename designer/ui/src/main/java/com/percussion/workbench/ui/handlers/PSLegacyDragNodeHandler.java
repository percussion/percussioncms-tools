/******************************************************************************
 *
 * [ PSLegacyDragNodeHandler.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.handlers;

import com.percussion.client.PSObjectType;
import com.percussion.workbench.ui.PSUiReference;
import com.percussion.workbench.ui.legacy.PSLegacyDnDHelper;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.Transfer;

import java.util.Collection;
import java.util.Properties;

/**
 * Supports drag operation to legacy Swing UI with mediation of
 * {@link PSLegacyDragNodeHandler}.
 *
 * @author Andriy Palamarchuk
 */
public class PSLegacyDragNodeHandler extends PSDeclarativeNodeHandler
{

   public PSLegacyDragNodeHandler(Properties props, String iconPath, PSObjectType[] allowedTypes)
   {
      super(props, iconPath, allowedTypes);
   }

   //see base class method for details
   @Override
   public Transfer[] getSourceTransfers(Collection<PSUiReference> nodes)
   {
      //Make sure getDragHandler is consistent w/ the types here
      Transfer[] baseTransfers = super.getSourceTransfers(nodes);
      Transfer[] transfers = new Transfer[baseTransfers.length
            + getCustomTransfers().length];
      System.arraycopy(baseTransfers, 0, transfers, 0, baseTransfers.length);
      System.arraycopy(getCustomTransfers(), 0, transfers, baseTransfers.length,
            getCustomTransfers().length);
      return transfers;
   }

   //see base class method for details
   @Override
   synchronized public DragSourceListener getDragHandler()
   {
      return new NodeDragHandler()
      {
         @Override
         public void dragStart(DragSourceEvent event)
         {
            super.dragStart(event);
            getDnDHelper().dragStarted();
         }

         //see base class method for details
         @Override
         public void dragFinished(DragSourceEvent event)
         {
            super.dragFinished(event);
            getDnDHelper().dragFinished();
         }

         //see base class method for details
         @Override
         public void dragSetData(DragSourceEvent event)
         {
            Transfer customTransfer = null;
            for (Transfer t : getCustomTransfers())
            {
               if (t.isSupportedType(event.dataType))
               {
                  customTransfer = t;
               }
            }
            if (customTransfer != null)
            {
               //this is one we added
               event.data = getData(customTransfer, getSelection());
            }
            else
            {
               super.dragSetData(event);
            }
         }
      };
   }
   /**
    * Builds an object for the supplied type that can be used for clipboard/dnd
    * operations.
    * 
    * @param type Assumed not <code>null</code>.
    * 
    * @param nodes Assumed not <code>null</code> or empty.
    * 
    * @return The generated object. Never <code>null</code>.
    */
   private Object getData(Transfer type, Collection<PSUiReference> nodes)
   {
      assert(type != null);
      assert(nodes != null && !nodes.isEmpty());

      final Object result = getDnDHelper().getDataForDrag(type, nodes);
      if (result == null)
      {
         throw new IllegalStateException("Unknown transfer type: "
               + type.getClass().getName());
      }
      return result;      
   }

   /**
    * Transfers custom to this handler.
    */
   protected Transfer[] getCustomTransfers()
   {
      return getDnDHelper().getTransfers();
   }

   /**
    * Convenience method to access {@link PSLegacyDnDHelper}. 
    */
   private PSLegacyDnDHelper getDnDHelper()
   {
      return PSLegacyDnDHelper.getInstance();
   }
}
