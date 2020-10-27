/******************************************************************************
 *
 * [ PSObjectType.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client;

import com.percussion.util.PSStringOperation;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Each object has a primary type and possibly a seconday type. This class
 * encapsulates this information for a design object. This object is mainly a
 * container; it does verify that the secondary type is allowed, but it does not
 * enforce any semantic or business logic.
 * <p>
 * The allowed secondary types are determined by the primary type.
 * <p>
 * Instances of this class are immutable and should be obtained using
 * {@link com.percussion.client.PSObjectTypeFactory}.
 * 
 * @author paulhoward
 * @version 6.0
 */
public class PSObjectType implements Serializable
{
   /**
    * To support Serializable interface.
    */
   private static final long serialVersionUID = 1L;

   /**
    * Convenience method that calls
    * {@link #PSObjectType(IPSPrimaryObjectType, Enum) this(primaryType, null)}.
    *///fixme make the ctor pkg access
   public PSObjectType(IPSPrimaryObjectType primaryType)
   {
      this(primaryType, null);
   }
   
   /**
    * Create a type identifier for a particular design object.
    * 
    * @param primaryType The implementing class must be an instance of
    * {@link Enum}. Never <code>null</code>.
    * 
    * @param secondaryType Must be <code>null</code> if this primary type does
    * not support sub types. Otherwise, it cannot be <code>null</code>. This
    * will be checked against the supplied primary type.
    *///fixme make the ctor pkg access
   public PSObjectType(IPSPrimaryObjectType primaryType, Enum secondaryType)
   {
      //validate contract
      if ( null == primaryType)
      {
         throw new IllegalArgumentException("primaryType cannot be null");  
      }
      if (!(primaryType instanceof Enum))
            {
         throw new IllegalArgumentException(
               "invalid primary type, must be an Enum");  
      }      
      if (primaryType.hasSubTypes())
      {
         if (secondaryType == null)
            throw new IllegalArgumentException("secondary type required");  
         if (!primaryType.isAllowedType(secondaryType))
         {
            throw new IllegalArgumentException(
                  "invalid secondary type for supplied primary type");  
         }
      }
      else if (secondaryType != null)
      {
         throw new IllegalArgumentException(
               "primary type does not support secondary types");  
      }
      
      m_primaryType = primaryType;
      m_secondaryType = secondaryType;
   }

   /**
    * This ctor can be used to recreate an instance that has been 'serialized'
    * using the {@link #toSerial()} method.
    * 
    * @param serializedForm A string previously generated by the
    * {@link #toSerial()} method. If the supplied string can't be converted to
    * a valid enum value (possibly because of a code change that occurred after
    * the serialized form was generated,) an exception is thrown.
    * 
    * @throws RuntimeException If the supplied text cannot be converted into
    * a valid enum(s). This should only happen if an incompatible change to 
    * the set of enums was made.
    *///fixme make the ctor pkg access
   public PSObjectType(String serializedForm)
   {
      if (null == serializedForm)
      {
         throw new IllegalArgumentException("serializedForm cannot be null");
      }

      List< ? > tokens = PSStringOperation.getSplittedList(serializedForm, ":");
      if (tokens.size() < 1 || tokens.size() > 4)
      {
         throw new IllegalArgumentException("serializedForm is invalid");
      }

      Enum primary = null;
      Enum secondary = null;
      String tok = tokens.get(0).toString().trim();
      if (tokens.size() == 1)
      {
         //known primary
         primary = PSObjectTypes.valueOf(tok);
      }
      else if (tokens.size() == 2)
      {
         //either unknown primary or known primary/sec
         {
            try
            {
               primary = PSObjectTypes.valueOf(tok);
            }
            catch (IllegalArgumentException ignore)
            {}
            
            if (primary != null)
            {
               secondary = loadSecondaryType((IPSPrimaryObjectType) primary,
                     tokens.get(1).toString().trim());
            }
            else
               primary = loadEnum(tok, tokens.get(1).toString().trim());
         }
      }
      else if (tokens.size() == 3)
      {
         //primary or secondary unknown, the other known
         try
         {
            primary = PSObjectTypes.valueOf(tok);
         }
         catch (IllegalArgumentException ignore)
         {}
         
         if (primary != null)
         {
            secondary = loadEnum(tokens.get(1).toString().trim(), tokens
                  .get(2).toString().trim());
         }
         else
         {
            primary = loadEnum(tok, tokens.get(1).toString().trim());
            secondary = loadSecondaryType((IPSPrimaryObjectType) primary,
                  tokens.get(2).toString().trim());
         }
      }
      else
      {
         //primary and secondary unknown
         primary = loadEnum(tok, tokens.get(1).toString().trim());
         secondary = loadEnum(tokens.get(2).toString().trim(), tokens.get(3)
               .toString().trim());
      }
      
      if (!(primary instanceof IPSPrimaryObjectType))
      {
         throw new RuntimeException(
               "Enum type must implement IPSPrimaryObjectType");
      }
      m_primaryType = (IPSPrimaryObjectType) primary;

      if (secondary != null)
      {
         if (!m_primaryType.isAllowedType(secondary))
         {
            throw new RuntimeException("The primary type (" 
                  + m_primaryType.toString()
                  + ") does not support the secondary type ("
                  + secondary.toString()
                  + ").");
         }
      }
      m_secondaryType = secondary;
   }

   /**
    * Attempts to find a sub type of the supplied primary type with the given
    * name, case-sensitive.
    * 
    * @param pType Assumed not <code>null</code>.
    * 
    * @param secondaryName Assumed not <code>null</code>.
    * 
    * @return Never <code>null</code>. Throws a runtime exception if a match is
    * not found.
    */
   private Enum loadSecondaryType(IPSPrimaryObjectType pType, String secondaryName)
   {
      Set<PSObjectType> types = pType.getTypes();
      for (PSObjectType type : types)
      {
         if (type.getSecondaryType().name().equals(secondaryName))
            return type.getSecondaryType();
      }
      throw new RuntimeException("Could not find matching secondary type '"
            + secondaryName + "' for primary type: " + pType.name());
   }
   
   /**
    * Finds an enumeration based on a class name and the text name of the
    * enum within the class.
    * 
    * @param className The name of the class implementing the enumeration.
    * Assumed not <code>null</code> or empty.
    * @param enumName The text name of the enumeration (<code>getName()</code>).
    * 
    * @return The instance of the requested enumeration. Never 
    * <code>null</code>.
    * 
    * @throws RuntimeException If any problems occur during this process.
    */
   private Enum loadEnum(String className, String enumName)
   {
      try
      {
         Class clazz = Class.forName(className);
         Method m = clazz.getMethod("valueOf", new Class[] {String.class});
         return (Enum) m.invoke(clazz, new Object[] {enumName}); 
      }
      catch (Exception e)
      {
         /*
          * there are so many possible exceptions, just catch all of them. We
          * throw a runtime exception rather than a checked exception because
          * we don't expect this to happen.
          */
         throw new RuntimeException(
               "Exception while loading enum - specified class=" 
               + className + ", enum name=" + enumName, e);
      }
   }
   
   /**
    * See class description for details.
    * 
    * @return The primary type supplied in the ctor. Never <code>null</code>.
    * Guaranteed to implement {@link IPSPrimaryObjectType}.
    */
   public Enum getPrimaryType()
   {
      return (Enum) m_primaryType;
   }

   /**
    * See the class description for details.
    * 
    * @return The secondary type supplied in the ctor. May be <code>null</code>
    * if this primary type does not support sub types.
    */
   public Enum getSecondaryType()
   {
      return m_secondaryType;
   }

   /**
    * Returns a string representation of the types in this instance with
    * sufficient information to recreate it using the string
    * {@link #PSObjectType(String) ctor}.
    * 
    * @return Never <code>null</code> or empty.
    */
   public String toSerial()
   {
      /*
       * We build a string that contains the primary and secondary type class
       * names, along with their enum text names. These are combined as 
       * follows: prim_class_name:prim_name:sec_class_name:sec_name
       * If there is no secondary type, the last 2 segments are not added. 
       * Because each piece is a Java identifier, they do not allow colons.
       */
      List<String> tokens = new ArrayList<String>();
      if (!(m_primaryType instanceof PSObjectTypes))
         tokens.add(m_primaryType.getClass().getName());
      tokens.add(((Enum) m_primaryType).name());
      if (null != m_secondaryType)
      {
         if (!m_secondaryType.getClass().getName().startsWith(
               PSObjectTypes.class.getName()))
         {
            tokens.add(m_secondaryType.getClass().getName());
         }
         tokens.add(m_secondaryType.name());
      }
      return PSStringOperation.append(tokens, ":");
   }
   
   //see base class method for details
   @Override
   public boolean equals(Object obj)
   {
      if (null == obj || !(obj instanceof PSObjectType))
         return false;
      PSObjectType type = (PSObjectType) obj;
      return type.m_primaryType == m_primaryType 
            && type.m_secondaryType == m_secondaryType;
   }

   //see base class method for details
   @Override
   public int hashCode()
   {
      int hash = m_primaryType.hashCode();
      if (null != m_secondaryType)
         hash *= m_secondaryType.hashCode();
      return hash;
   }

   /**
    * @inheritDoc Outputs a string that is the concatenation of the result of
    * calling the <code>name()</code> method of the primary type followed by
    * the secondary type (if there is one), separated by a colon (:). For
    * example - @code{RESOURCE:FILE}
    */
   @Override
   public String toString()
   {
      StringBuffer buf = new StringBuffer(100);
      buf.append(m_primaryType.name());
      if (m_secondaryType != null)
      {
         buf.append(":");
         buf.append(m_secondaryType.name());
      }
      return buf.toString();
   }

   /**
    * Never <code>null</code>.
    */
   private final IPSPrimaryObjectType m_primaryType;

   /**
    * May be
    * <code>null</code> if the primary type has no sub types. Otherwise, it is
    * a valid sub type of the primary type.
    */
   private final Enum m_secondaryType;
}