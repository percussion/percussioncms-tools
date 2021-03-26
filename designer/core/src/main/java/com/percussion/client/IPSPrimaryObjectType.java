/******************************************************************************
 *
 * [ IPSPrimaryObjectType.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client;

import java.io.Serializable;
import java.util.Set;

/**
 * This interface is meant to be implemented by enumerations that are meant to
 * be part of the {@link com.percussion.client.models.IPSCmsModel} type system.
 * A design object has a primary type and possibly a sub type.
 * 
 * @author paulhoward
 * @version 6.0
 */
public interface IPSPrimaryObjectType extends Serializable
{
   /**
    * Each design object type may be further classified by a sub-type. This 
    * method determines if that is the case. 
    * 
    * @return <code>true</code> if this primary type supports secondary types,
    * <code>false</code> otherwise.
    */
   public boolean hasSubTypes();

   /**
    * If sub types are supported, this method allows validating whether a 
    * certain type is a valid sub type for this primary type.
    * 
    * @param subType Never <code>null</code>.
    * 
    * @return <code>true</code> if this type knows about the supplied 
    * secondary type, <code>false</code> otherwise.
    */
   public boolean isAllowedType(Enum subType);
   
   /**
    * If this type has sub-types, an instance of each one is returned, else
    * an instance of this primary type is returned with a <code>null</code>
    * sub type.
    * 
    * @return Never <code>null</code>, or empty . The primary
    * type of each returned object is this type. The returned set is 
    * unmodifiable.
    */
   public Set<PSObjectType> getTypes();
   
   /**
    * Returns the name of the underlying enumeration.
    * 
    * @return Never <code>null</code> or empty.
    */
   public String name();
   
   /**
    * Indicates whether objects that have this type have data that is stored in
    * a file, or a file-like way. If this method returns <code>true</code>,
    * then the data for this object is a
    * {@link com.percussion.services.system.data.PSMimeContentAdapter 
    * PSMimeContentAdapter}.
    * 
    * @return <code>true</code> if the data for this object type (and any sub-
    * type) is a single text property, <code>false</code> otherwise.
    */
   public boolean isFileType();
   
   /**
    * Indicates whether objects that have this type can have ACL's assigned to
    * them and therefore their models will have implemented the ACL support
    * methods.
    * 
    * @return <code>true</code> if the object type can support acls
    * , <code>false</code> otherwise.
    */
   public boolean supportsAcls();
}
