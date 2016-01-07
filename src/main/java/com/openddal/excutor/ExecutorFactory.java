/*
 * Copyright 2014-2015 the original author or authors
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
package com.openddal.excutor;

import com.openddal.command.CommandInterface;
import com.openddal.command.Prepared;
import com.openddal.command.ddl.*;
import com.openddal.command.dml.*;
import com.openddal.excutor.ddl.*;
import com.openddal.excutor.dml.*;
import com.openddal.message.DbException;

/**
 * @author <a href="mailto:jorgie.mail@gmail.com">jorgie li</a>
 */
public class ExecutorFactory implements PreparedExecutorFactory {

    @Override
    public PreparedExecutor newExecutor(Prepared prepared) {
        return create(prepared);
    }

    private PreparedExecutor create(Prepared prepared) {
        int type = prepared.getType();
        switch (type) {
            //dml
            case CommandInterface.CREATE_TABLE:
                return new CreateTableExecutor((CreateTable) prepared);
            case CommandInterface.DROP_TABLE:
                return new DropTableExecutor((DropTable) prepared);
            case CommandInterface.ALTER_TABLE_ALTER_COLUMN_NOT_NULL:
            case CommandInterface.ALTER_TABLE_ALTER_COLUMN_NULL:
            case CommandInterface.ALTER_TABLE_ALTER_COLUMN_DEFAULT:
            case CommandInterface.ALTER_TABLE_ALTER_COLUMN_CHANGE_TYPE:
            case CommandInterface.ALTER_TABLE_ADD_COLUMN:
            case CommandInterface.ALTER_TABLE_DROP_COLUMN:
            case CommandInterface.ALTER_TABLE_ALTER_COLUMN_SELECTIVITY:
                return new AlterTableAlterColumnExecutor((AlterTableAlterColumn) prepared);
            case CommandInterface.ALTER_TABLE_ADD_CONSTRAINT_REFERENTIAL:
            case CommandInterface.ALTER_TABLE_ADD_CONSTRAINT_PRIMARY_KEY:
            case CommandInterface.ALTER_TABLE_ADD_CONSTRAINT_UNIQUE:
            case CommandInterface.ALTER_TABLE_ADD_CONSTRAINT_CHECK:
                return new AlterTableAddConstraintExecutor((AlterTableAddConstraint) prepared);
            case CommandInterface.TRUNCATE_TABLE:
                return new TruncateTableExecutor((TruncateTable) prepared);
            //ddl
            case CommandInterface.INSERT:
                return new CreateTableExecutor((CreateTable) prepared);
            case CommandInterface.DELETE:
                return new DropTableExecutor((DropTable) prepared);
            case CommandInterface.UPDATE:
                return new CreateTableExecutor((CreateTable) prepared);
            case CommandInterface.REPLACE:
                return new DropTableExecutor((DropTable) prepared);
            case CommandInterface.MERGE:
                return new CreateTableExecutor((CreateTable) prepared);
            case CommandInterface.SELECT: {
                if (prepared instanceof SelectUnion) {
                    return new SelectUnionExecutor((SelectUnion) prepared);
                } else {
                    return new SelectExecutor((Select) prepared);
                }
            }
            case CommandInterface.CALL:
                return new CallExecutor((Call) prepared);
            case CommandInterface.SET:
                return new SetExecutor((Set) prepared);
            case CommandInterface.SET_AUTOCOMMIT_TRUE:
            case CommandInterface.SET_AUTOCOMMIT_FALSE:
            case CommandInterface.BEGIN:
            case CommandInterface.COMMIT:
            case CommandInterface.ROLLBACK:
            case CommandInterface.CHECKPOINT:
            case CommandInterface.SAVEPOINT:
            case CommandInterface.ROLLBACK_TO_SAVEPOINT:
            case CommandInterface.COMMIT_TRANSACTION:
            case CommandInterface.ROLLBACK_TRANSACTION:
            case CommandInterface.TRANSACTION_ISOLATION:
            case CommandInterface.TRANSACTION_READONLY_FALSE:
            case CommandInterface.TRANSACTION_READONLY_TRUE:
                return new TransactionExecutor((TransactionCommand) prepared);
            default:
                throw DbException.getUnsupportedException("statemets type=" + type);
        }
    }

}