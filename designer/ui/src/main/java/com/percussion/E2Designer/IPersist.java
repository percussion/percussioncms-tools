/*[ IPersist.java ]************************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;

import com.percussion.design.objectstore.PSApplication;

import java.util.Properties;

/**
 * This interface is used for saving and loading applications. When an application
 * is loaded, an application 'walker' will go through all the app objects, create
 * a figure for each one, get its data object (which must implement IPersistance),
 * and call the load method.
 * </p>
 * The figures are arranged in a sort of hierarchy. The application is the
 * highest level, with all figures in the app window getting an application
 * as the store. The next level would be the figures in the dataset, which
 * will receive a dataset as the store; and so-on.
 * </p>
 * When an app is saved, the data objects in all figures visible to each frame
 * will have their save method called with their corresponding store.
 * </p>
 * Each object should save its application specific data to the store and its
 * GUI specific data as key-value pairs to the config.
 */
public interface IPersist
{
   /**
    * The object implementing this interface should load its application
    * properties from the supplied store and its GUI properties from the
    * supplied config. For the GUI properties to be restored, the object must
    * be owned by a UIFigure, using the IGuiLink interface.
    *
    * @param app the application that contains the provided store and config.
    *
    * @param store the object that contains the application properties for this
    * object. The type of the store must be correct for the implementing object.
    *
    * @param config a set of key-value pairs. Each object should look here for
    * gui specific data that was previously saved. If no entries are found,
    * reasonable defaults should be used.
    *
    * @returns <code>true</code> if configuration info was found, otherwise
    * <code>false</code> is returned. If false is returned, the caller may
    * want to override the default positioning of the added objects.
    *
    * @see IGuiLink
    */
   public boolean load(PSApplication app, Object store, Properties config);

   /**
    * The object implementing this interface should save its properties to
    * the supplied store and config. GUI properties will only be written if the
    * object is currently owned by a UIFigure through the IGuiLink interface.
    *
    * @param app the application that contains the provided store and config.
    *
    * @param store the object that contains the application properties for this
    * object. The type of the store must be correct for the implementing object.
    *
    * @param config a set of key-value pairs. Each object should store GUI specific
    * info here. Keys must be unique across the application,
    * use util.getUniqueId() to obtain a key, then save this key with the
    * server object as its id.
    *
    * @see IGuiLink
    */
   public void save(PSApplication app, Object store, Properties config);

   /** 
    * The implementing class may perform any cleanup operations before or 
    * after saving the application object. For example, one may want to 
    * delete the files residing on the server because of the changes made to 
    * the application. his method is called on each object that is deleted 
    * from the figure frame window while the application is being saved. 
    * 
    * @param app the application we are saving to, must not be 
    * <code>null</code>.
    */
   public void cleanup(OSApplication app);
}

