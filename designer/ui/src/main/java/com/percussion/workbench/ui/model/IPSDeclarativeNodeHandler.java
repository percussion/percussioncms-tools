/******************************************************************************
 *
 * [ IPSDeclarativeNodeHandler.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.model;

import com.percussion.client.IPSReference;
import com.percussion.client.PSObjectType;
import com.percussion.workbench.ui.PSUiReference;
import com.percussion.workbench.ui.model.IPSDeclarativeNodeHandler.HandlerOptions.HandlerOptionSet;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkbenchSite;

import java.util.Collection;
import java.util.Map;

/**
 * This interface is used by the framework for processing various operations
 * that may be object dependent. Implementations can derive from
 * {@link com.percussion.workbench.ui.handlers.PSDeclarativeNodeHandler}, which
 * provides some default implementation.
 * <p>
 * Implementations must have a ctor with the following signature:
 * 
 * <pre>
 *      name(Properties props, String iconPath, PSObjectType[] allowedTypes)
 * </pre>
 * 
 * Where <code>props</code> contains all catalog properties from ancestor
 * nodes in the def.
 * <p>
 * When a handler is assigned to a node, that node will call the
 * <code>setOwningNode</code> method passing itself as the input parameter.
 * Some handlers may need this.
 * 
 * <p>
 * Implementation must also implement
 * {@link java.lang.Object#equals(java.lang.Object)} and
 * {@link java.lang.Object#hashCode()}.
 * 
 * @author paulhoward
 */
public interface IPSDeclarativeNodeHandler extends IAdapterFactory
{
   /**
    * Options for passing to the
    * {@link IPSDeclarativeNodeHandler#configure(HandlerOptionSet)} method. For
    * example, to disable the security menu,
    * configure(SECURABLE.removeOptionFlag(HandlerOptions.getAllOptions).
    * 
    * @author paulhoward
    */
   public enum HandlerOptions
   {
      /**
       * @see IPSDeclarativeNodeHandler#supportsCopy(PSUiReference)
       */
      COPYABLE,

      /**
       * @see IPSDeclarativeNodeHandler#supportsDelete(PSUiReference)
       */
      DELETABLE,

      /**
       * @see IPSDeclarativeNodeHandler#supportsPaste(PSUiReference)
       */
      PASTEABLE,

      /**
       * @see IPSDeclarativeNodeHandler#supportsRename(PSUiReference)
       */
      RENAMABLE,

      /**
       * @see IPSDeclarativeNodeHandler#supportsSecurity(PSUiReference)
       */
      SECURABLE;
      
      /**
       * Combines all options into a single value. 
       * 
       * @return Never <code>null</code>.
       */
      public static HandlerOptionSet getAllOptions()
      {
         HandlerOptionSet set = COPYABLE.new HandlerOptionSet(0);
         for (HandlerOptions o : HandlerOptions.values())
         {
            set.addOptionFlag(o);
         }
         return set;
      }
      
      /**
       * Used to manage groups of options.
       *
       * @author paulhoward
       */
      public class HandlerOptionSet
      {
         /**
          * Standard ctor.
          * 
          * @param start Starting point of the option set.
          */
         public HandlerOptionSet(int start)
         {
            m_options = start;
         }
         
         /**
          * Copy ctor - makes a deep copy.
          * 
          * @param copy Never <code>null</code>.
          */
         public HandlerOptionSet(HandlerOptionSet copy)
         {
            if (null == copy)
            {
               throw new IllegalArgumentException("copy cannot be null");  
            }
            m_options = copy.m_options;
         }
         
         /**
          * Checks if the bit specified by <code>option</code> is set in this
          * object.
          * 
          * @param o The flag to check.
          * 
          * @return <code>true</code> if the bit defined by
          * <code>option</code> is enabled in this instance,
          * <code>false</code> otherwise.
          */
         public boolean isOptionSet(HandlerOptions o)
         {
            return (m_options & o.getFlagValue()) != 0;
         }
         /**
          * Add the option to those in this set.
          * 
          * @param o The option to add. Never <code>null</code>.
          * 
          * @return This instance.
          */
         public HandlerOptionSet addOptionFlag(HandlerOptions o)
         {
            if (null == o)
            {
               throw new IllegalArgumentException("option cannot be null");  
            }
            m_options |= o.getFlagValue();
            return this;
         }
         
         /**
          * Remove the option from those in this set.
          * 
          * @param o The option to remove. Never <code>null</code>.
          * 
          * @return This instance.
          */
         public HandlerOptionSet removeOptionFlag(HandlerOptions o)
         {
            if (null == o)
            {
               throw new IllegalArgumentException("option cannot be null");  
            }
            m_options &= ~(o.getFlagValue());
            return this;
         }

         /**
          * The current options.
          */
         private int m_options;
      }
      
      /**
       * Returns a integer w/ 1 bit set that is unique among all options.
       * 
       * @return Exactly 1 bit set.
       */
      private int getFlagValue()
      {
         return 1<<ordinal();
      }
   }
   
   /**
    * Can be used to disable various operations as indicated by the
    * <code>supportsXXX</code> methods. By default, all options are enabled.
    * 
    * @param options A combination of the <code>HandlerOptions</code> flag
    * values. Undefined bits are ignored. Never <code>null</code>.
    */
   public void configure(HandlerOptionSet options);
   
   /**
    * Returns all options as set by the {@link #configure(HandlerOptionSet)}
    * method. By default, all options are enabled.
    * 
    * @return The current settings as a copy. Never <code>null</code>.
    */
   public HandlerOptionSet getConfiguration();
   
   /**
    * Indicates whether this handler will throw an
    * {@link UnsupportedOperationException} when {@link #handleCopy(Collection)}
    * is called.
    * @param node The node to check, never <code>null</code>.
    * 
    * @return <code>true</code> if the action is implemented,
    * <code>false</code> if an exception would be thrown.
    */
   public boolean supportsCopy(PSUiReference node);
   
   /**
    * Indicates whether this handler will throw an
    * {@link UnsupportedOperationException} when
    * {@link #handlePaste(PSUiReference, Map)} is called. Equivalent to calling
    * {@link #getAcceptedTransfers()}.length &gt; 0.
    * @param node The node to check, never <code>null</code>.
    * 
    * @return <code>true</code> if the action is implemented,
    * <code>false</code> if an exception would be thrown.
    */
   public boolean supportsPaste(PSUiReference node);
   
   /**
    * Indicates whether this handler will throw an
    * {@link UnsupportedOperationException} when
    * {@link #handleDelete(Collection)} is called.
    * <p>
    * If <code>node.isReference()</code> returns <code>true</code>, then
    * the request is delegated to the closest parent that is a design object
    * whose <code>isReference()</code> returns <code>false</code>, otherwise,
    * the default implementation returns <code>true</code>.
    * 
    * @param node The node of interest. Whether it is required depends on the
    * implementation. The default implementation requires it.
    * 
    * @return <code>true</code> if the action is implemented,
    * <code>false</code> if an exception would be thrown.
    */
   public boolean supportsDelete(PSUiReference node);
   
   /**
    * Indicates whether this handler will throw an
    * {@link UnsupportedOperationException} when
    * {@link #handleOpen(IWorkbenchSite, PSUiReference)} is called.
    * 
    * @param node The node of interest. Whether it is required depends on the
    * implementation. The default implementation does not require it.
    * 
    * @return <code>true</code> if the action is implemented,
    * <code>false</code> if an exception would be thrown.
    */
   public boolean supportsOpen(PSUiReference node);

   /**
    * Indicates whether objects managed by this handler support being renamed.
    * 
    * @param node The node that is being processed. Never <code>null</code>.
    * 
    * @return <code>true</code> if the rename method of the model can be used
    * to successfully rename the object, <code>false</code> if an
    * <code>UnsupportedOperationException</code> would be thrown.
    */
   public boolean supportsRename(PSUiReference node);

   /**
    * Indicates whether objects managed by this handler support ACLs.
    * 
    * @param node The node that is being processed. Never <code>null</code>.
    * 
    * @return <code>true</code> if the object can be controlled by ACLs,
    * <code>false</code> otherwise.
    */
   public boolean supportsSecurity(PSUiReference node);
   
   /**
    * Every node should have some form of 16x16 image to display with the
    * name in the tree. This method returns the image. Images should be cached
    * as much as possible.
    * 
    * @param node This is provided so the handler can return different images
    * for different nodes if needed. Never <code>null</code>.
    * 
    * @return If there is an image associated with this node, <code>null</code>
    * if there isn't one.
    */
   public Image getLabelImage(PSUiReference node);
   
   /**
    * Each node can accept 0 or more types of objects for pasting or dropping.
    * See {@link com.percussion.workbench.ui.dnd.PSTransferFactory} and
    * {@link com.percussion.workbench.ui.dnd.PSReferenceTransfer} for more
    * details.
    * 
    * @return 0 or more transfer objects that describe what types of design
    * objects this node will accept. Never <code>null</code>, may be empty if
    * this node does not accept any pastes.
    */
   public Transfer[] getAcceptedTransfers();
   
   /**
    * Compares the transfer type of the supplied node against those that this
    * handler allows.
    * 
    * @param ref Never <code>null</code>.
    * 
    * @return <code>true</code> if this handler allows children of this type,
    * false otherwise.
    */
   public boolean isAcceptedType(IPSReference ref);
   
   /**
    * This method must be called instead of going directly to the
    * {@link com.percussion.workbench.ui.dnd.PSTransferFactory} so additional
    * information that may be needed for the supplied type can be 
    * sent in the request to the factory.
    * 
    * @param type Never <code>null</code>.
    * 
    * @return May be <code>null</code> if this handler does not know how to
    * deal with the supplied object.
    */
   public Transfer getTransfer(PSObjectType type);
   
   /**
    * Each node has one or more types when it is copied. This method returns the
    * types that the supplied nodes support.
    * 
    * @param nodes Used to determine the types. If <code>null</code> or empty,
    * an empty array is returned.
    * 
    * @return Never <code>null</code>, may be empty if copying is not
    * supported.
    */
   public Transfer[] getSourceTransfers(Collection<PSUiReference> nodes);
   
   /**
    * If a node can be copied/moved, this method should be implemented. Handlers
    * returned by this method must implement the <code>equals</code> method
    * because the framework determines if a multi-object selection supports
    * being dragged based on whether all the nodes in the selection have the
    * same handler.
    * <p>
    * If any problems occur within the methods in the drag handler, the
    * implementation should show an appropriate message to the user.
    * 
    * @return Non-<code>null</code> if supported, otherwise <code>null</code>.
    */
   public DragSourceListener getDragHandler();
   
   /**
    * The drop handler performs the work needed when an object is dropped onto a
    * node. The default implementation provides a handler that can copy or move
    * design objects. Generally, if pasting is supported, this method should
    * return a valid handler.
    * <p>
    * If any problems occur within the methods in the drop handler, the
    * implementation should show an appropriate message to the user.
    * 
    * @return Non-<code>null</code> if this node will accept some type of
    * dropped object. The accepted types can be determined by the
    * {@link #getAcceptedTransfers()} method.
    */
   public IPSDropHandler getDropHandler();
   
   /**
    * This method will be called when an object is added to a node. If any
    * properties on the object must be set to certain values in order for the
    * reference to be a child of the node, this method should make such changes.
    * It may also make other changes outside of the object, if necessary.
    *
    * @param parent The node instance that is going to receive the reference
    * as a child. Never <code>null</code>. This is a node that contains an
    * instance of this handler as a member.  
    * 
    * @param ref The object that is being added to the supplied parent. Never
    * <code>null</code>.
    */
   public void configureForNode(PSUiReference parent, IPSReference ref);
   
   /**
    * If this node supports dynamic addition of children, then this method
    * will properly deal with the paste. A paste may cause creation of copies
    * of the supplied design objects, or it could cause links to be created
    * to those objects. The implementation makes that determination.
    * <p>
    * If any problems occur, the implementation should show an appropriate
    * message to the user.
    * 
    * @param parent If not <code>null</code>, the pasted objects will be added
    * as children to this node. Otherwise, they will be added to the 'home' 
    * node for the supplied object type.
    * 
    * @param cbData The framework will always provide a non-<code>null</code>
    * value.
    */
   public void handlePaste(PSUiReference parent,
         Map<Transfer, Object> cbData);
   
   /**
    * If this node supports copy, this method will take the supplied data and
    * build a map containing the necessary <code>Transfer</code> types and
    * associated data. This will be expected back by the
    * {@link #handlePaste(PSUiReference, Map)} method.
    * <p>
    * If any problems occur, the implementation should show an appropriate
    * message to the user.
    * 
    * @param nodes Never <code>null</code> or empty.
    * 
    * @return A valid map containing the data if the supplied nodes validate.
    * Otherwise, <code>null</code> is returned. If <code>null</code> is
    * returned, an appropriate message may have been logged.
    */
   public Map<Transfer, Object> handleCopy(Collection<PSUiReference> nodes);
   
   /**
    * If this node supports dynamic removal of children, then this method will
    * handle the processing. This may be permanently removing an object from
    * persistent storage or removing a link to the object.
    * <p>
    * If any problems occur, the implementation should show an appropriate
    * message to the user.
    * 
    * @param nodes The framework will always provide a non-<code>null</code>
    * value.
    */
   public void handleDelete(Collection<PSUiReference> nodes);
   
   /**
    * Method is called instead of standard opening behavior.
    * Should be used only in special cases.
    * Prefered way to handle opening nodes is through editor framework.
    * @param site the site providing context information for this action.
    * Never <code>null</code>.
    * @param ref node reference to be open. 
    */
   public void handleOpen(IWorkbenchSite site, PSUiReference ref);

   /**
    * This method will be called when the handler is assigned to or removed from
    * a {@link PSUiReference node} object.
    * 
    * @param owner May be <code>null</code> when the association is being
    * broken.
    */
   public void setOwningNode(PSUiReference owner);
}
