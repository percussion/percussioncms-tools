/*[ IDependencyManager.java ]**************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;

import com.percussion.design.objectstore.PSDependency;
import com.percussion.design.objectstore.PSDisplayMapping;

/**
 * Collects the methods used to manage content editor control dependencies
 * references for <code>OSControlRef</code> objects.
 */
public interface IDependencyManager
{
   /**
    * Gets a reference to the active instance of the provided dependency.  
    * This method should be called by newly contructed controls of this content
    * editor.  (Controls that are being loaded from an application should use
    * <code>loadDependencyReference</code>.)
    * <p>
    * All requests for a given single-occurrence dependency will return the
    * same instance -- as all controls in a content editor with this dependency
    * should share a single reference.
    * <p>
    * All requests for a multiple-occurrence dependency will return a clone
    * of the template -- as each control with this dependency should have its
    * own reference.
    * 
    * @param template a description of the dependency and its default values,
    * not <code>null</code>.
    * @return the dependency object that should be used to represent the
    * template, never <code>null</code>.
    * 
    * @throws IllegalArgumentException if <code>template</code> is <code>null
    * </code>
    */
   public PSDependency getDependencyReference(PSDependency template);
   
   /**
    * Gets a reference to the active instance of the provided dependency,
    * reconstructing the dependency by extracting its dependent object from the
    * application.  This method should be called by controls being created 
    * during the loading of an application.  (Newly constructed controls should
    * use <code>getDependencyReference</code>.)
    * <p>
    * If a valid id (greater than zero) is provided, it is used to locate the
    * dependent object, but the method is robust in the case of missing or
    * wrong ids by searching by dependency name as a fallback.
    * <p>
    * The instance returned is located by the following algorithm:
    * <ol>
    * <li>If valid id, check to see if a dependency with the that id has
    * already been reconstructed.  If so, return it.
    * <li>If valid id, search the content editor resource for a dependent 
    * object with that id. If found, reconstruct the dependency and return it.
    * <li>If the dependency is single occurrence, see if we've already
    * reconstructed one with the same dependent object.  If so, return it.
    * <li>Search the content editor resource for a dependent object with the
    * same name and type as the dependency requires.   If found, reconstruct 
    * the dependency and return it.
    * <li>Provide a warning (as we should have found something by now), and
    * return a clone of the template.
    * </ol>
    * 
    * <p>
    * When found, a dependent object is removed from the content editor 
    * resource, because only one object should hold its reference -- the 
    * <code>PSDependency</code>.
    * 
    * @param context the environment in which the control containing the
    * dependency is being loaded.  Used to un-resolve macro values.  May not
    * be <code>null</code>.
    * @param dependencyId the E2Designer id of the dependency and its dependent
    * object, if known.  Used to locate and restore the dependent object from
    * the application.  Use <code>0</code> if id is not known.
    * @param template the dependency to restore (usually obtained from the
    * control's metadata); may not be <code>null</code>.
    * 
    * @return a reference to a dependency that has been reconstructed by 
    * extracting its dependent object from the content editor resource, or
    * a clone of the template if no dependent object could be found; never
    * <code>null</code>.
    * 
    * @throws IllegalArgumentException if <code>context</code> or <code>
    * template</code> is <code>null</code>.
    * @throws IllegalStateException if template contains a dependent object 
    * whose class is unsupported. 
    */ 
   public PSDependency loadDependencyReference(PSDisplayMapping context,
                                               int dependencyId,
                                               PSDependency template);
   
}
