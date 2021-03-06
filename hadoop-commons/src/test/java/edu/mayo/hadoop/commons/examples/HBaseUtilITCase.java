package edu.mayo.hadoop.commons.examples;

import edu.mayo.hadoop.commons.hbase.AutoConfigure;
import edu.mayo.hadoop.commons.hbase.HBaseConnector;
import edu.mayo.hadoop.commons.hbase.HBaseUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by m102417 on 3/10/16.
 *
 * Checks that the utility functions in HBaseUtil are working properly
 *
 */
public class HBaseUtilITCase {

    String tableName = "table";
    String[] columnFamily = {"cf1", "cf2"};


    Configuration configuration;
    HBaseConnector hconnect;
    HBaseUtil hutil;

    @Before
    public void setup() throws Exception {
        configuration = AutoConfigure.getConfiguration();
        hconnect = new HBaseConnector(configuration);
        hutil = new HBaseUtil(hconnect.getConnection());
    }

    @After
    public void teardown() throws IOException {
        hconnect.close();
    }

    @Test
    public void testAddTable() throws Exception {

        //if there was anything in the schema, get rid of it so the tests pass
        hutil.dropAll();

        //get the tables in hbase, it should be zero
        //System.err.println("***************************************");
        //System.err.println(schema.getTables().toString());
        //System.err.println("***************************************");
        assertEquals(0, hutil.getTables().size());

        //create a table, 'users'
        // and put a column family 'info' into the table
        String[] columnFamiles = {"foo","bar"};
        hutil.createTable("users", columnFamiles);

        //get the tables in hbase, it should be now have one
        assertEquals(1, hutil.getTables().size());

        hutil.dropAll();
    }


    //load some data in for testing...
    public void load() throws Exception {

        //put data in using our restricted json spec...
        hutil.putJSON(tableName, "key1", "{\"cf1\": {\"field1\":\"value1\", \"field2\":123, \"field3\":1.23, }, \"cf2\": {\"field4\":\"value1\"} }");
        //put data in using a more direct method
        hutil.put(tableName,"key2", columnFamily[0], "field2", "value2");

    }

    @Test
    public void testCRUD() throws Exception {
        hutil.dropAll();
        hutil.createTable(tableName,columnFamily);


        //CRUD - Create, Retrieve, Update, Delete

        //Create
        load();

        //Retrieve
        byte[] b = hutil.get(tableName, "key2",columnFamily[0],"field2");
        assertEquals("value2",Bytes.toString(b));

        //Update (just use put)
        hutil.put(tableName, "key2", columnFamily[0], "field2", "X");
        hutil.put(tableName, "key1", columnFamily[0], "field3", "Y");
        b = hutil.get(tableName, "key2",columnFamily[0],"field2");
        assertEquals("X",Bytes.toString(b));
        List<String> pretty = hutil.format(hutil.first(tableName, 1000));
        for(String line : pretty){
            System.err.println(line);
        }
        assertTrue(pretty.contains("        Key : field2, Value : X, NumericalValue : X"));
        assertTrue(pretty.contains("        Key : field3, Value : Y, NumericalValue : Y"));

        //Delete
        hutil.deleteRow(tableName,"key2");
        b = hutil.get(tableName, "key2",columnFamily[0],"field2");
        assertTrue(b == null);
        //todo: perhaps we want to build more fine grain delete control when we have the use cases...
        //e.g. http://www.tutorialspoint.com/hbase/hbase_delete_data.htm


        hutil.dropAll();

    }

    @Test
    public void testScan() throws Exception {
        hutil.dropAll();
        hutil.createTable(tableName,columnFamily);
        load();

        ArrayList<String> expected = new ArrayList<String>();//note, we don't care about whitespace!
        expected.add("key1");
        expected.add("cf1");
        expected.add("Key : field1, Value : value1");
        expected.add("NumericalValue : 123");
        expected.add("NumericalValue : 1.23");
        expected.add("cf2");
        expected.add("Key : field4, Value : value1");
        expected.add("key2");
        expected.add("cf1");
        expected.add("Key : field2, Value : value2,");

        Result[] results = hutil.first(tableName, 1000);
        List<String> pretty = hutil.format(results);
        int i = 0;
        for(String line : pretty){
            System.out.println(line);
            assertTrue(line.contains(expected.get(i)));
            i++;
        }

        //todo: build test cases to start scanning at different places in the hbase table structure!

        hutil.dropAll();
    }
}
