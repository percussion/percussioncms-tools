/*[ IPSItemTransformer.java ]**************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader;

/**
 * See base interface for a more generic description.
 * Plugins implementing this interface fit into the migration/loader model
 * after the items have been extracted from the original resource and grouped
 * together into a single standard item.
 * <p>The item transformer is used when changes need to be made that can't be
 * accomplished by (@link #IPSFieldTransformer field transformers}.
 *
 * @see IPSFieldTransformer
 */
public interface IPSItemTransformer extends IPSPlugin
{
   /**
    * Possibly modifies the supplied item and returns it. Any aspect of the
    * item may be modified by this transformer, including adding and removing
    * fields.
    * <p>It is possible for the transformer to prevent the item from being
    * submitted to the server. It does this by throwing an exception. The
    * exception text will be saved in the log, so it should be meaningful to
    * the user of the program.
    *
    * @param params an array of values defined by the configuration of this
    *    transformer. If no params were defined, the array will be empty.
    *    Never <code>null</code>. If the number or type of params supplied
    *    doesn't match what is expected, a PSParameterValidationException
    *    should be thrown with a message understandable by the end user of the
    *    tool.
    *
    * @param item the item that needs to be transformed, never <code>null</code>.
    *
    * @return the transformed item, never <code>null</code>. In general, this
    *    should be the item that was passed in, although it is possible to
    *    completely build a new one to be submitted to the server.
    *
    * @throws PSParameterValidationException for any parameters provided that
    *    don't match what is expected by this transformer.
    *
    * @throws PSTransformationException for anything that goes wrong
    *    transforming the supplied item.
    *
    * @throws PSExcludeException if the transformer decides to exclude this
    *    item from beeing uploaded. The exception text should indicate why in
    *    a manner understandable to the end user of this tool.
    */
   public PSItemContext transform(Object[] params, PSItemContext item)
      throws PSParameterValidationException, PSTransformationException,
         PSExcludeException;
}
