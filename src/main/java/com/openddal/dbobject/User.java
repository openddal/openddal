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
package com.openddal.dbobject;

import com.openddal.dbobject.schema.Schema;
import com.openddal.dbobject.table.Table;
import com.openddal.engine.Database;
import com.openddal.engine.Session;
import com.openddal.message.DbException;
import com.openddal.message.ErrorCode;
import com.openddal.message.Trace;
import com.openddal.util.New;
import com.openddal.util.StringUtils;

import java.util.ArrayList;

/**
 * Represents a user object.
 */
public class User extends RightOwner {

    private boolean admin;
    private String password;

    public User(Database database, int id, String userName) {
        super(database, id, userName, Trace.USER);
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }


    /**
     * Checks that this user has the given rights for this database object.
     *
     * @param table     the database object
     * @param rightMask the rights required
     * @throws DbException if this user does not have the required rights
     */
    public void checkRight(Table table, int rightMask) {
        if (!hasRight(table, rightMask)) {
            throw DbException.get(ErrorCode.NOT_ENOUGH_RIGHTS_FOR_1, table.getSQL());
        }
    }

    /**
     * See if this user has the given rights for this database object.
     *
     * @param table     the database object, or null for schema-only check
     * @param rightMask the rights required
     * @return true if the user has the rights
     */
    public boolean hasRight(Table table, int rightMask) {
        return true;
    }

    /**
     * Get the CREATE SQL statement for this object.
     *
     * @param password true if the password (actually the salt and hash) should
     *                 be returned
     * @return the SQL statement
     */
    public String getCreateSQL(boolean password) {
        StringBuilder buff = new StringBuilder("CREATE USER IF NOT EXISTS ");
        buff.append(getSQL());
        if (comment != null) {
            buff.append(" COMMENT ").append(StringUtils.quoteStringSQL(comment));
        }
        buff.append(" PASSWORD ''");
        if (admin) {
            buff.append(" ADMIN");
        }
        return buff.toString();
    }

    /**
     * Check the password of this user.
     *
     * @param userPasswordHash the password data (the user password hash)
     * @return true if the user password hash is correct
     */
    boolean validateUserPasswordHash(byte[] userPasswordHash) {
        return true;
    }

    /**
     * Check if this user has admin rights. An exception is thrown if he does
     * not have them.
     *
     * @throws DbException if this user is not an admin
     */
    public void checkAdmin() {
        if (!admin) {
            throw DbException.get(ErrorCode.ADMIN_RIGHTS_REQUIRED);
        }
    }

    /**
     * Check if this user has schema admin rights. An exception is thrown if he
     * does not have them.
     *
     * @throws DbException if this user is not a schema admin
     */
    public void checkSchemaAdmin() {
        if (!hasRight(null, Right.ALTER_ANY_SCHEMA)) {
            throw DbException.get(ErrorCode.ADMIN_RIGHTS_REQUIRED);
        }
    }

    @Override
    public int getType() {
        return USER;
    }

    @Override
    public ArrayList<DbObject> getChildren() {
        ArrayList<DbObject> children = New.arrayList();
        for (Right right : database.getAllRights()) {
            if (right.getGrantee() == this) {
                children.add(right);
            }
        }
        for (Schema schema : database.getAllSchemas()) {
            if (schema.getOwner() == this) {
                children.add(schema);
            }
        }
        return children;
    }

    @Override
    public void removeChildrenAndResources(Session session) {
        for (Right right : database.getAllRights()) {
            if (right.getGrantee() == this) {
                database.removeDatabaseObject(session, right);
            }
        }
        invalidate();
    }

    @Override
    public void checkRename() {

    }

    /**
     * Check that this user does not own any schema. An exception is thrown if
     * he owns one or more schemas.
     *
     * @throws DbException if this user owns a schema
     */
    public void checkOwnsNoSchemas() {
        for (Schema s : database.getAllSchemas()) {
            if (this == s.getOwner()) {
                throw DbException.get(ErrorCode.CANNOT_DROP_2, getName(), s.getName());
            }
        }
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


}