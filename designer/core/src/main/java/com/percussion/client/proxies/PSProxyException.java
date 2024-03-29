/*******************************************************************************
 *
 * [ PSProxyException.java ]
 * 
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.client.proxies;

import com.percussion.client.error.IPSErrorCode;
import com.percussion.client.error.PSClientException;

import java.text.MessageFormat;

/**
 * A generic exception thrown a proxy. This generally wraps an exception that
 * was thrown by server while performing a remote operation.
 */
public class PSProxyException extends PSClientException
{
   /**
    * Convenience ctor that calls
    * {@link #PSProxyException(IPSErrorCode, Object[], Throwable) this(msgKey,
    * <code>null</code>, <code>null</code>)}.
    */
   public PSProxyException(IPSErrorCode code)
   {
      this(code, null, null);
   }

   /**
    * Convenience ctor that calls
    * {@link #PSProxyException(IPSErrorCode, Object[], Throwable) this(msgKey, 
    * new Object[] &#125; arg &#125;, <code>null</code>)}.
    */
   public PSProxyException(IPSErrorCode code, Object arg)
   {
      this(code, new Object[] { arg }, null);
   }

   /**
    * Convenience ctor that calls
    * {@link #PSProxyException(IPSErrorCode, Object[], Throwable) this(
    * <code>null</code>, <code>null</code>, <code>cause</code>)}.
    */
   public PSProxyException(Throwable cause)
   {
      super(cause);
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
    */
   public PSProxyException(IPSErrorCode code, Object[] args, Throwable cause)
   {
      super(code, args, cause);
   }

   /**
    * Autogenerated serialization id
    */
   private static final long serialVersionUID = -7193128181586256851L;
}
