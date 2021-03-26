/*[ IEditor.java ]*************************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;


/**
 * Defines a simple interface that can be implemented by dialogs or other
 * editing windows that provides polymorphic behavior for the data that will
 * be edited and limited programatic control of the window.
 */
public interface IEditor
{
   /**
    * This method is called when a data object needs to be visually edited. For
    * a modal editor, the passed in object will be modified when the user
    * performs certain actions, such as pressing the OK button.
    *
    * @param figure The screen object that contains the data that this editor
    * knows how to deal with. The actual data object can be obtained from
    * the figure by calling getData() on it.
    *
    * @param data The data object of the frame containing the figure supplied
    * as the first parameter. This object should not be modified.
    *
    * @return If the dialog is modal, returns <code>true</code> if the state of
    * the object was changed by the user, <code>false</code> otherwise. If the editor is not
    * modal, the return value is undefined.
    *
    * @throws IllegalArgumentException if figure or Data is not an instance of the type
    * required by the editor implementing this interface
    */
   public boolean onEdit( UIFigure figure, final Object data );

   /**
    * @return <code>true</code> if this editor is a modal window. A modal window
    * must be dismissed before the user can continue w/ the program.
    */
   public boolean isModal();

   /**
    * Just before an editor is to be closed, this method should be called to
    * find out if the editor is in a closable state. When called, this method
    * will perform any checks necessary, including querying the user, to determine
    * if it is ok to close the window. This is useful when multiple windows must
    * be closed in a transaction-like manner. Call this method on all windows,
    * and only proceed with the actual close if all windows returned non-zero.
    * This method is not the same as <code>isDataChanged</code>. The latter
    * could return <code>false</code> while this method returns <code>false
    * </code>. Basically, this method applies to anything that isn't covered by
    * the data changed method.
    *
    * @return A non-zero cookie that can be passed to the <code>close</code>
    * method to close the window w/o re-checking the closable state (assuming
    * the state hasn't changed since this check). A cookie w/ a value of zero
    * is returned if the window is not currently closable.
   **/
   public int canClose();

   /**
    * Causes the editing window to close itself w/o saving. The default
    * implementation is to call <code>dispose</code> (unless the editor has no
    * native peer, then the default is to do nothing). <code>canClose</code>
    * can be called to determine if this method will succeed. There is a small
    * chance that the state of the object could change between a call to
    * <code>canClose</code> and this method, so be prepared to deal with the
    * exception even if <code>canClose</code> is successful. A class should
    * withstand multiple, successful calls to this method w/o ill effects.
    *
    * @param cookie A value returned by the <code>canClose</code> method. If the
    * object being edited has not changed since this cookie was issued, the
    * window will be closed w/o re-checking the closability state. If the cookie
    * has changed or is 0, <code>canClose</code> will be called and the window
    * will close if it returns <code>true</code>. Otherwise, an exception will
    * be thrown.
    *
    * @param bForce Forces the window to close, even if it is in a not-closable
    * state. This should only be used in extreme situations, as data loss will
    * most likely occur.
    *
    * @return <code>true</code> if the window was successfully closed, <code>
    * false</code> if it couldn't be closed for any reason. If <code>bForce</code>
    * is <code>true</code>, <code>true</code> is always returned.
    */
   public boolean close( int cookie, boolean bForce );

   /**
    * Causes the editor to write the changes to the object that was passed in
    * the <code>onEdit</code> call. This is really only useful for modeless
    * windows.
    *
    * @param context An object that has meaning to the class implementing
    * this interface. It can provide additional information that may be needed
    * to perform the save operation.
    *
    * @return <code>true</code> if all changed values were successfully
    * validated and saved to the data object, <code>false</code> otherwise
    *
    * @throws UnsupportedOperationException if this editor does not support
    * programatic saves. Typically only modeless dialogs will support this
    * feature.
    *
    * @throws IllegalArgumentException if the implementing class expects a
    * context and one isn't provided or the supplied context is not of the
    * expected type.
    */
   public boolean saveData( Object context );


   /**
    * @return <code>true</code> if the user has modified any values since it was
    * opened or last saved, whichever came last. The default is to always
    * return <code>true</code>. It is preferred that more intelligent algorithms
    * be used to determine if an object has changed.
   **/
   public boolean isDataChanged();

     /**
    * Sets title of window.
    */
   public void setTitle(String title);

   /**
    * gets title of window.
    */
   public String getTitle();
}

