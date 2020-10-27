/**
 * 
 */
package com.percussion.client;

/**
 * Exception thrown when logon to server fails for any reason. All the ctors in
 * this somply invoke the base class version.
 */
public class PSLogonException extends Exception
{
   private static final long serialVersionUID = 1L;

   /**
    * @see Exception#Exception()
    */
   public PSLogonException()
   {
      super();
   }

   /**
    * @see Exception#Exception(java.lang.String)
    */
   public PSLogonException(String message)
   {
      super(message);
   }

   /**
    * @see Exception#Exception(java.lang.String, java.lang.Throwable)
    */
   public PSLogonException(String message, Throwable cause)
   {
      super(message, cause);
   }

   /**
    * @see Exception#Exception(java.lang.Throwable)
    */
   public PSLogonException(Throwable cause)
   {
      super(cause);
   }
}
