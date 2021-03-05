/*[ IPSFieldTransformer.java ]*************************************************
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
 * after the resource has been converted to a 'standard item'. A transformer
 * can be used to modify the content of a field, add a new field or clear a
 * field. A transformer must accept 1 or more parameters that contain the
 * data to be transformed or information needed to perform the transform. These
 * parameters are passed through an array.
 * <p>The output of the transformer is the data for the target field. If the
 * returned object is not a <code>String</code>, <code>toString</code> will
 * be called on it to convert it.
 * <p>The target field name and the source for each parameter is specified in
 * the configuration for the transformer. This allows transformers to be easily
 * reused.
 */
public interface IPSFieldTransformer extends IPSPlugin
{
   /**
    * Transforms the data in the supplied params into the desired format.
    * Field transformers allow the following parameter types:
    * <table>
    *    <tr>
    *       <th>Type</th><th>Description</th>
    *    </tr>
    *    <tr>
    *       <td>java.lang.Object</td><td>Any java object that can be 
    *          constructed from a <code>String</code> and implements the
    *          toString() method.</td>
    *    </tr>
    * </table>
    *
    * @param params an array of transformation parameters, never
    *    <code>null</code>, may be empty. The configuration determines how
    *    many params there are and where their values are obtained from. The
    *    class types of the parameters may vary, as indicated in the table
    *    above.
    *
    * @param info data pertinent to the field being transformed, such as the
    *    <code>PSItemField</code> object. Never <code>null</code>.
    *
    * @throws PSParameterValidationException if the supplied parameters don't
    *    match what the transformer expects. This means the configuration entry
    *    does not match the transformer interface.
    *
    * @throws PSTransformationException for anything that goes wrong during the
    *    transformation.
    */
   public void transform(Object[] params, IPSTransformContext info)
      throws PSParameterValidationException, PSTransformationException;
}
