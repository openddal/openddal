/*
 * Copyright 2004-2014 H2 Group. Multiple-Licensed under the MPL 2.0,
 * and the EPL 1.0 (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package com.openddal.test.jdbc;

import com.openddal.test.BaseTestCase;
import org.junit.Test;

import java.sql.*;

/**
 * Tests the server by creating many JDBC objects (result sets and so on).
 */
public class ManyJdbcObjectsTestCase extends BaseTestCase {

    @Test
    public void test() throws SQLException {
        testNestedResultSets();
        testManyConnections();
        testOneConnectionPrepare();
    }

    private void testNestedResultSets() throws SQLException {
        Connection conn = getConnection();
        DatabaseMetaData meta = conn.getMetaData();
        ResultSet rsTables = meta.getColumns(null, null, null, null);
        while (rsTables.next()) {
            meta.getExportedKeys(null, null, null);
            meta.getImportedKeys(null, null, null);
        }
        conn.close();
    }

    private void testManyConnections() throws SQLException {
        // SERVER_CACHED_OBJECTS = 1000: connections = 20 (1250)
        // SERVER_CACHED_OBJECTS = 500: connections = 40
        // SERVER_CACHED_OBJECTS = 50: connections = 120
        int connCount = 400;
        Connection[] conn = new Connection[connCount];
        for (int i = 0; i < connCount; i++) {
            conn[i] = getConnection();
        }
        int len = 500;
        for (int j = 0; j < len; j++) {
            if ((j % 10) == 0) {
                trace("j=" + j);
            }
            for (int i = 0; i < connCount; i++) {
                conn[i].getMetaData().getSchemas().close();
            }
        }
        for (int i = 0; i < connCount; i++) {
            conn[i].close();
        }
    }

    private void testOneConnectionPrepare() throws SQLException {
        Connection conn = getConnection();
        PreparedStatement prep;
        Statement stat;
        int size = 1000;
        for (int i = 0; i < size; i++) {
            conn.getMetaData();
        }
        for (int i = 0; i < size; i++) {
            conn.createStatement();
        }
        stat = conn.createStatement();
        stat.execute("CREATE TABLE TEST(ID INT PRIMARY KEY, NAME VARCHAR)");
        stat.execute("INSERT INTO TEST VALUES(1, 'Hello')");
        for (int i = 0; i < size; i++) {
            stat.executeQuery("SELECT * FROM TEST WHERE 1=0");
        }
        for (int i = 0; i < size; i++) {
            stat.executeQuery("SELECT * FROM TEST");
        }
        for (int i = 0; i < size; i++) {
            conn.prepareStatement("SELECT * FROM TEST");
        }
        prep = conn.prepareStatement("SELECT * FROM TEST WHERE 1=0");
        for (int i = 0; i < size; i++) {
            prep.executeQuery();
        }
        prep = conn.prepareStatement("SELECT * FROM TEST");
        for (int i = 0; i < size; i++) {
            prep.executeQuery();
        }
        conn.close();
    }

}