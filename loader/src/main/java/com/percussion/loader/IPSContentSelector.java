/*[ IPSContentSelector.java ]**************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader;

import com.percussion.loader.selector.PSScanException;

import java.io.IOException;
import java.io.InputStream;

/**
 * See base interface for a more generic description.
 * Plugins implementing this interface fit into the migration/loader model as
 * the 'source' of content. Each plugin is repsonsible for scanning some
 * repository (such as a web server, file system or database) for a set of
 * 'resources' and building a tree view of all the resources found. The module
 * will typically follow links found in a resource's content (inline links). It
 * would also follow any related content links it found.
 * <p>The tree that this module builds should include nodes for each found
 * resource and for all followed links. If a link points to a resource already
 * in the tree, then it should insert a reference node instead of a standard
 * node.
 * <p>The usage of this interface by the manager class would proceed by a call
 * to the configure method to initialize the plugin and then a call to the scan
 * method. Then for some set of items in the tree, the retrieve method will be
 * called. The scan method may be called again before or after the retrieve
 * method has been called.
 *
 * @see IPSContentTree
 * @see IPSContentTreeNode
 */
public interface IPSContentSelector extends IPSPlugin
{
   /**
    * Scans the repository as specified by the implementing plugin's
    * documentation (and possibly configuration) and builds a tree
    * representation of all the found resources. Each node in the tree must
    * contain a locator that can be passed back to this plugin to actually
    * retrieve the content using the {@link #retrieve(IPSContentTreeNode)} 
    * method.
    *
    * @return the new content tree, never <code>null</code>, might be empty.
    *
    * @throws PSScanException for fatal scanning errors only.
    *
    * @throws IllegalStateException if the crawler is <code>null</code>.
    */
   public IPSContentTree scan()
      throws PSScanException;

   /**
    * Creates a stream which contains the source data for the resource
    * described by the supplied descriptor.
    *
    * @param descriptor a node originally placed in the tree
    *    returned by the <code>scan</code> method. If the locator is invalid
    *    (i.e., it is <code>null</code> or was not originally supplied by
    *    this plugin), an exception is thrown. The locator is the resource id
    *    of the <code>PSItemContext</code> object in the current node, see
    *    {@link PSItemContext#getResourceId()}.
    *
    * @return a stream that can be used to get the source data. This stream
    *    is guaranteed to be resettable. This stream is also set on the
    *    descriptor
    *
    * @throws IllegalStateException if the throwOnDelta param is <code>true
    *    </code> and any of the meta data of the specified resource has changed.
    *
    * @throws IOException If any errors occur while reading the data, including
    *    the item not being available at the location where the descriptor
    *    specifies.
    *
    * @throws IllegalArgumentException If descriptor is <code>null</code> or did
    *    not originate from this plugin.
    */
   public InputStream retrieve(IPSContentTreeNode descriptor)
      throws IOException;

   /**
    * Added a job progress listener to the progress listener list.
    *
    * @param listener The to be added listener, may not be <code>null</code>.
    */
   public void addProgressListener(IPSProgressListener listener);

   /**
    * Remove a progress listener from the progress listener list.
    *
    * @param listener The to be removed listener, may not be <code>null</code>.
    */
   public void removeProgressListener(IPSProgressListener listener);

   /**
    * Inform the selector to abort current (scanning) operation.
    */
   public void abort();
}
