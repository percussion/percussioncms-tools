/*[ UIConnectableFigureBEDatatank.java ]***************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/


package com.percussion.E2Designer;

import com.percussion.design.objectstore.PSQueryPipe;



/**
 * This class has been created so that we can set the editor for the backend
 * datatank depending on if the Datatank is connected to a query pipe or not.
 * We override onEdit and set the appropriate editor.
 */
public class UIConnectableFigureBEDatatank extends UIConnectableFigure
{

   // constructors
   /**
    * Creates a new object with the supplied name.
    *
    * @param strName internal name of the new object, must not be null. The user
    * displayable name and image will be obtained from the E2DesignerResources 
    * bundle using this value as the key and the ResourceHelper class. If no 
    * image is found, then the derived class is responsible for handling the
    * paint messages.
    *
    * @param Data the object that contains the data that this UI object is
    * representing. This may be null if there is no associated data.
    *
    * @param iId the unique identifier for this object. It should be unique 
    * within the factory that created it.
    *
    * @throws IllegalArgumentException if strName or Data is null
    * @throws MissingResourceException if strImageResource can't be found/opened
    */
   public UIConnectableFigureBEDatatank( String strName, Object Data,
         int ID, String strFactoryClassName)
   {
      super(strName, Data, "", ID, strFactoryClassName);
   }

   /**
    * Overrides onEdit so that we can set the appropriate editor.
    * Initiates the editor for this object based on the type of pipe
    * the datatank is attached to.
    *
    * @param frameData the data object associated with the frame that this figure
    * is currently in. This data is passed directly to the editing dialog via
    * the IEditor interface.
    *
    * @returns <code>true</code> if the edit changed any properties in the object
    */
   public boolean onEdit( Object frameData )
   {
      if(getEditor() == null)
      {
        UTPipeNavigator navigator = new UTPipeNavigator();
         UIFigure pipeFigure = navigator.getPipe(this);
      
         if (pipeFigure != null && pipeFigure.getData() instanceof PSQueryPipe)
         {
            setEditorName(PipeFigureFactory.QUERY_PIPE_DATATANK_PROPERTY_EDITOR);
            return super.onEdit(frameData);
         }
         else
         {
            setEditorName(PipeFigureFactory.UPDATE_PIPE_DATATANK_PROPERTY_EDITOR);
            return super.onEdit(frameData);
         }
      }
     
      return false;
   }

  /** 
   * Overriden from UIConnectableFigure.
   *
   * @returns <CODE>true</CODE> ALWAYS; this fixes a bug that disables the
   * icon popup menu the very first time it is called.
  */
  public boolean isEditable()
  {
    return true;
  }

}
