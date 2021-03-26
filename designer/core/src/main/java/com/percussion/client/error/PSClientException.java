/******************************************************************************
 *
 * [ PSClientException.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.error;

import com.percussion.client.PSErrorCodes;
import org.apache.commons.lang.StringUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * The purpose of this class is to centralize all error messages. All extensions
 * should extend this class. An enumeration that implements the
 * {@link com.percussion.client.error.IPSErrorCode} interface is used as the
 * source of the error codes. The messages for a set of codes can be registered
 * with this class by calling the {@link #registerMessageBundle}. See that
 * method for futher details.
 * 
 * @version 6.0
 * @author paulhoward
 */
public class PSClientException extends Exception
{
   /**
    * Provide a set of messages for a set of error codes. The
    * com.percussion.client classes are automatically registered for the error
    * code range 30000-30399, inclusive. The messages are stored in the
    * com.percussion.client.impl.ErrorMessages.properties file.
    * 
    * @param classNameSuffix When called, this value is checked against the
    * calling class' name using <code>String.startsWith</code>. If it
    * matches, then the supplied bundle is used. Never <code>null</code> or
    * empty.
    * 
    * @param minErrorCode The minimum error code for this bundle, inclusive. If
    * the supplied range conflicts with an already registered range, an
    * exception is thrown. &gt;= 0 && &lt; <code>maxErrorCode</code>
    * 
    * @param maxErrorCode The largest number for error codes in this bundle,
    * inclusive. &gt; <code>minErrorCode</code>
    * 
    * @param bundle The bundle containing the error messages. Never
    * <code>null</code> or empty.
    * 
    * @throws PSClientException If the error code range overlaps with any
    * previously registered bundles.
    * 
    * @throws MissingResourceException If the bundleName bundle cannot be found.
    */
   public static void registerMessageBundle(String classNameSuffix,
         int minErrorCode, int maxErrorCode, ResourceBundle bundle)
      throws PSClientException, MissingResourceException
   {
      if (StringUtils.isBlank(classNameSuffix))
      {
         throw new IllegalArgumentException(
               "classNameSuffix cannot be null or empty");
      }
      if (bundle == null)
      {
         throw new IllegalArgumentException("bundle cannot be null");
      }
      if (minErrorCode < 0 || minErrorCode >= maxErrorCode)
      {
         throw new IllegalArgumentException(
               "The supplied error codes must meet the following criteria: "
                     + "minErrorCode >= 0, minErrorCode < maxErrorCode.");
      }

      for (ErrorBundleInfo info : ms_bundleInfo)
      {
         if ((info.m_minErrorCode >= minErrorCode && info.m_minErrorCode < maxErrorCode)
               || (info.m_maxErrorCode >= minErrorCode && info.m_maxErrorCode < maxErrorCode))
         {
            Object[] args = { minErrorCode, maxErrorCode };
            throw new PSClientException(
                  PSErrorCodes.OVERLAPPING_ERROR_CODE_RANGE, args);
         }
      }

      PSClientException e = new PSClientException(PSErrorCodes.RAW);
      ErrorBundleInfo info = e.new ErrorBundleInfo();
      info.m_bundle = bundle;
      info.m_classNameSuffix = classNameSuffix;
      info.m_minErrorCode = minErrorCode;
      info.m_maxErrorCode = maxErrorCode;
      ms_bundleInfo.add(info);
   }

   /**
    * Convenience ctor that calls
    * {@link #PSClientException(IPSErrorCode, Object[], Throwable) this(msgKey,
    * <code>null</code>, <code>null</code>)}.
    */
   public PSClientException(IPSErrorCode code)
   {
      this(code, null, null);
   }

   /**
    * Convenience ctor that calls
    * {@link #PSClientException(IPSErrorCode, Object[], Throwable) this(msgKey, 
    * new Object[] &#125; arg &#125;, <code>null</code>)}.
    */
   public PSClientException(IPSErrorCode code, Object arg)
   {
      this(code, new Object[] { arg }, null);
   }

   /**
    * Convenience ctor that calls
    * {@link #PSClientException(IPSErrorCode, Object[], Throwable) this(
    * <code>null</code>, <code>null</code>, <code>cause</code>)}.
    */
   public PSClientException(Throwable cause)
   {
      super(cause);
      if (cause instanceof PSClientException)
      {
         PSClientException e = (PSClientException) cause;
         m_errorCode = e.m_errorCode;
         m_message = e.m_message;
      }
   }

   /**
    * Takes the info in the supplied exception and uses it to configure this one
    * so it represents the same exception.
    */
   public PSClientException(PSClientException cause)
   {
      m_errorCode = cause.m_errorCode;
      m_message = cause.m_message;
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
   public PSClientException(IPSErrorCode code, Object[] args, Throwable cause)
   {
      super(cause);

      if (null != code)
      {
         initDetails(code, args);
      }
      else
      {
         m_message = getClass().getName();
      }
   }

   /**
    * @return The message localized to the default Locale in effect when the
    * exception was created. Never <code>null</code>.
    */
   @Override
   public String getLocalizedMessage()
   {
      if (m_message == null)
         throw new IllegalStateException("Derived class didn't initialize.");

      return m_errorCode == null ? m_message + ": " + super.getMessage()
            : m_message;
   }

   /**
    * @return Same as {@link #getLocalizedMessage()}.
    */
   @Override
   public String getMessage()
   {
      return getLocalizedMessage();
   }

   /**
    * Returns the numeric code that identifies this exception.
    * 
    * @return If a code is available, a non-negative value, otherwise, -1;
    */
   public int getErrorCode()
   {
      return m_errorCode == null ? -1 : m_errorCode.getCodeAsInt();
   }

   /**
    * For use by derived classes. If this ctor is used, the
    * {@link #initDetails(IPSErrorCode, Object[])} method must be called.
    */
   protected PSClientException()
   {}

   /**
    * Used by derived classes to properly initialize this object when the needed
    * info is not available to use the parameterized ctors. The error code and
    * generated message are stored locally and be accessed via other methods.
    * 
    * @param code Never <code>null</code>.
    * 
    * @param args Appropriate for the message whose key is the supplied code.
    * May be <code>null</code>.
    */
   protected void initDetails(IPSErrorCode code, Object[] args)
   {
      if (null == code)
      {
         throw new IllegalArgumentException("code must not be null");
      }

      m_errorCode = code;

      String message = null;
      try
      {
         StackTraceElement[] trace = Thread.currentThread().getStackTrace();
         int i = 0;
         // the purpose here is to walk down the stack until we get to the
         // class that called us
         while (!trace[i].getClassName().equals(getClass().getName()))
            i++;
         while (trace[i].getClassName().equals(getClass().getName()))
            i++;

         ResourceBundle bundle = null;
         for (ErrorBundleInfo info : ms_bundleInfo)
         {
            if (trace[i].getClassName().startsWith(info.m_classNameSuffix))
            {
               bundle = info.m_bundle;
               break;
            }
         }
         if (bundle != null)
         {
            String pattern = bundle.getString(code.toString());
            if (args == null || args.length == 0)
               message = pattern;
            else
               message = MessageFormat.format(pattern, args);
         }
         else
         {
         }
      }
      catch (MissingResourceException ignore)
      {
      }

      if (message == null)
      {
         StringBuffer buf = new StringBuffer();
         buf.append("Error code: ");
         buf.append(code.getCodeAsString());
         if (args != null)
         {
            buf.append(" - Args: ");
            for (Object o : args)
            {
               buf.append(o.toString());
               buf.append(", ");
            }
         }
         message = buf.toString();
      }
      m_message = message;
   }

   /**
    * Stores the code that describes this exception. See {@link #m_message} for
    * details of its invariance. May be <code>null</code> to indicate no code
    * is available.
    */
   private IPSErrorCode m_errorCode = null;

   /**
    * Stores the processed message created in the ctor. Never <code>null</code>
    * or empty or changed after construction if a parameterized ctor is used.
    * May be <code>null</code> until the <code>initDetails</code> method is
    * called after the no param ctor is used.
    */
   private String m_message;

   /**
    * For serialization support.
    */
   private static final long serialVersionUID = 8245940251184084258L;

   /**
    * Stores all registered information for error message handling.
    */
   private static List<ErrorBundleInfo> ms_bundleInfo = new ArrayList<ErrorBundleInfo>();
   {
      // auto registration
      ErrorBundleInfo info = new ErrorBundleInfo();
      ResourceBundle bundle = ResourceBundle.getBundle(
            "com.percussion.client.impl.ErrorMessages", Locale.getDefault());
      info.m_bundle = bundle;
      info.m_classNameSuffix = "com.percussion.client";
      info.m_minErrorCode = 30000;
      info.m_maxErrorCode = 30399;
      ms_bundleInfo.add(info);
   }

   /**
    * Structure to store the info that describes a set of error messages and
    * their associated codes and text bundle.
    * 
    * @author paulhoward
    */
   private class ErrorBundleInfo
   {
      // See <code>registerMessageBundle</code> method for details of all
      // members.
      public int m_minErrorCode;

      public int m_maxErrorCode;

      String m_classNameSuffix;

      ResourceBundle m_bundle;
   }
}
