/******************************************************************************
 *
 * [ PSModelException.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client;

import com.percussion.client.error.IPSErrorCode;
import com.percussion.client.error.PSClientException;

import java.text.MessageFormat;

/**
 * This exception is thrown for a number of problems. Generally, most exceptions
 * that may be returned from the server, will be wrapped in this exception,
 * except security/authorization exceptions.
 *
 * @author paulhoward
 */
public class PSModelException extends PSClientException
{
   /**
    * Convenience ctor that calls
    * {@link #PSModelException(IPSErrorCode, String, Object)} this(msgKey,
    * <code>null</code>, <code>null</code>).
    */
   public PSModelException(IPSErrorCode code)
   {
      this(code, (String)null, null);
   }
   
   /**
    * Convenience ctor that calls
    * {@link #PSModelException(IPSErrorCode, String, Object)} 
    * this(msgKey, <code>null</code>, detail).
    */
   public PSModelException(IPSErrorCode code, Object detail)
   {
      this(code, (String)null, detail);
   }
   
   /**
    * Convenience ctor that calls
    * {@link #PSModelException(IPSErrorCode, Object[], Throwable, Object)}
    *  this(code, new Object[] &#123; arg &#125;, <code>null</code>,
    *  detail).
    */
   public PSModelException(IPSErrorCode code, String arg, Object detail)
   {
      this(code, new Object[] { arg }, null, detail);
   }

   /**
    * Convenience ctor that calls
    * {@link #PSModelException(IPSErrorCode, Object[], Throwable)} this(code, 
    * new Object[] &#125; arg &#125;, <code>null</code>).
    */
   public PSModelException(IPSErrorCode code, String arg)
   {
      this(code, new Object[] { arg }, null);
   }
   
   /**
    * Convenience ctor that calls
    * {@link #PSModelException(IPSErrorCode, Object[], Throwable)} this(code, 
    * args, <code>null</code>).
    */
   public PSModelException(IPSErrorCode code, Object[] args)
   {
      this(code, args, null);
   }

   /**
    * Convenience ctor that calls
    * {@link #PSModelException(IPSErrorCode, Object[], Throwable, Object)}
    *  this(code, args, <code>null</code>, detail).
    */
   public PSModelException(IPSErrorCode code, Object[] args, Object detail)
   {
      this(code, args, null, detail);
   }
   
   /**
    * Convenience ctor that calls
    * {@link #PSModelException(Throwable, Object) this(
    * cause, <code>null</code>)}.
    */
   public PSModelException(Throwable cause)
   {
      this(cause, null);
   }

   /**
    * Convenience ctor that calls
    * {@link #PSModelException(IPSErrorCode, Object[], Throwable, Object)} this(
    * <code>null</code>, <code>null</code>, cause,
    *  detail).
    */
   public PSModelException(Throwable cause, Object detail)
   {
      this(null, null, cause, detail);
   }

   /**
    * Configures this instance with the values from the supplied instance.
    */
   public PSModelException(PSClientException cause)
   {
      super(cause);
   }
   
   /**
    * Convenience ctor that calls
    * {@link #PSModelException(IPSErrorCode, Object[], Throwable, Object)}
    * as this(code, args, cause, <code>null</code>).
    */
   public PSModelException(IPSErrorCode code, Object[] args, Throwable cause)
   {
      this(code, args, cause, null);
   }

   /**
    * Ctor for parameterized error messages.
    * 
    * @param code The error code, which is used as a key to find the associated
    * text. The key will be used to lookup the optionally patterned message in
    * the ErrorMessages resource bundle located in the impl package. If the
    * bundle or key cannot be found, the key is used as the message. May be
    * <code>null</code> or empty, in which case the class name is used as the
    * message. 
    * 
    * @param args One argument for each replaceable parameter in the pattern
    * associated with <code>code</code>. If provided, must conform to the
    * format expected by the
    * {@link MessageFormat#format(java.lang.String, java.lang.Object[])} class.
    * 
    * @param cause The exception that caused this one to be thrown. May be
    * <code>null</code> if unknown or this is the root exception.
    * 
    * @param detail Must be either an instance of <code>String</code> or
    * <code>IPSReference</code> or <code>null</code>. These objects are
    * used to give further detail about the object that had the corresponding
    * error.
    */
   public PSModelException(IPSErrorCode code, Object[] args, Throwable cause,
      Object detail)
   {
      super(code, args, cause);
      m_detail = detail;
   }
   
   /**
    * Adds a detail for the specified exception
    * 
    * @param obj the detail object. Can be <code>null</code>, but if not
    * then must be an instance of either <code>IPSReference</code> or
    * <code>String</code>.
    */
   public void setDetail(Object obj)
   {
      if(obj != null && !(obj instanceof String || obj instanceof IPSReference))
      {
         throw new IllegalArgumentException("Exception detail object must be an"
            + "instance of String or IPSReference or null.");
      }      
      m_detail = obj;
   }
   
   /**
    * Returns an exception detail object
    * @return Returns the detail object supplied in the ctor or set by calling 
    * the {@link #setDetail(Object)} method.
    * Either a <code>IPSReference</code> or <code>String</code>
    * object, may be <code>null</code>.
    */
   public Object getDetail()
   {
      return m_detail;   
   }
   
   /**
    * The exception details object. Either <code>String</code> or
    * <code>IPSReference</code> or <code>null</code>.
    */
   private Object m_detail;

   /**
    * To support serialization. 
    */
   private static final long serialVersionUID = 131698535618052637L;
   
   
}
