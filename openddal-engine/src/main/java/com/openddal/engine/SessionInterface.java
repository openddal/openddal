/*
 * Copyright 2014-2016 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the “License”);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an “AS IS” BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.openddal.engine;

import com.openddal.command.CommandInterface;
import com.openddal.message.Trace;
import com.openddal.value.Value;

import java.io.Closeable;
import java.sql.SQLException;

/**
 * A local or remote session. A session represents a database connection.
 */
public interface SessionInterface extends Closeable {
    /**
     * Parse a command and prepare it for execution.
     *
     * @param sql       the SQL statement
     * @param fetchSize the number of rows to fetch in one step
     * @return the prepared command
     */
    CommandInterface prepareCommand(String sql, int fetchSize);

    /**
     * Roll back pending transactions and close the session.
     */
    @Override
    void close();

    /**
     * Get the trace object
     *
     * @return the trace object
     */
    Trace getTrace();

    /**
     * Check if close was called.
     *
     * @return if the session has been closed
     */
    boolean isClosed();

    /**
     * Check whether this session has a pending transaction.
     *
     * @return true if it has
     */
    boolean hasPendingTransaction();

    /**
     * Cancel the current or next command (called when closing a connection).
     */
    void cancel();

    /**
     * Check if the database changed and therefore reconnecting is required.
     *
     * @param write if the next operation may be writing
     * @return true if reconnecting is required
     */
    boolean isReconnectNeeded(boolean write);

    /**
     * Close the connection and open a new connection.
     *
     * @param write if the next operation may be writing
     * @return the new connection
     */
    SessionInterface reconnect(boolean write);

    /**
     * Add a temporary LOB, which is closed when the session commits.
     *
     * @param v the value
     */
    void addTemporaryLob(Value v);
    
    /**
     * Check if this session is in auto-commit mode.
     *
     * @return true if the session is in auto-commit mode
     */
    boolean getAutoCommit();

    /**
     * Set the auto-commit mode. This call doesn't commit the current
     * transaction.
     *
     * @param autoCommit the new value
     */
    void setAutoCommit(boolean autoCommit);
    
    /**
     * Puts this connection in read-only mode as a hint to the driver to enable
     * database optimizations. 
     * This method cannot be called during a transaction.
     * @param readOnly true enables read-only mode false disables it
     */
    void setReadOnly(boolean readOnly);

    /**
     * Retrieves whether this <code>Connection</code>
     * object is in read-only mode.
     * @return true if this session
     *         is read-only; false otherwise 
     */
    boolean isReadOnly() throws SQLException;

}