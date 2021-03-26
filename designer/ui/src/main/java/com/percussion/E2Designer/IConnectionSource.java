/*[ IConnectionSource.java ]***************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;

import com.percussion.conn.PSDesignerConnection;

/**
 * A simple interface that can be passed to classes that need an Rx designer
 * connection for cataloging or some other work. With this interface, the
 * connection, which changes very infrequently, can be passed in once when
 * the recipient class is being initialized, but can still support
 * auto-reconnect because the recipient will get the connection each time
 * it needs it.
 * <p>
 * The implementor should automatically reconnect if the server cycles during
 * the lifetime of the program. If a reconnect fails, the designer should be
 * queried about how to proceed.
 */
public interface IConnectionSource
{
   /**
    * Obtain a connection to an Rx server. Typically, this method will be
    * called the first time w/ the flag set to <code>false</code>. If any
    * exceptions occur when trying to use the connection, this method should
    * be called again with the flag set to <code>true</code>. When <code>true
    * </code>, the implementor will verify that the connection is valid and
    * if it isn&apos;t, will attempt to reconnect. If a connection can't be
    * re-established, null will be returned. If null is returned, it won't be
    * useful to call again because the implementor should ask the user what
    * to do when a reconnect fails.
    *
    * @param verifyConnection A flag that asks the implementor to check that
    * the connection is still valid before returning it.
    *
    * @return A connection to an Rx server. If the supplied flag is <code>false
    * </code>, a non-null connection is returned, but it may be invalid. If
    * the flag is <code>true</code>, the returned connection will be valid
    * at the point it was returned. However, it could become invalid between
    * the time it was checked and it was used. If a valid connection can't be
    * obtained, an exception should be thrown.
    *
    * @throws IllegalStateException If supplied flag is <code>true</code> and
    * a valid connection cannot be established with the server. The error
    * message should indicate the  problem (which may be that the user
    * cancelled the reconnect).
    */
   public PSDesignerConnection getDesignerConnection( boolean verifyConnection);
}
