package hbase;

import edu.mayo.genomics.model.Variant;
import edu.mayo.hadoop.commons.hbase.AutoConfigure;
import edu.mayo.hadoop.commons.hbase.HBaseConnector;
import edu.mayo.hadoop.commons.hbase.HBaseUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by m102417 on 4/1/16.
 *
 * verifies that the behavior on a variant object as it is cast to a put gets us back what we expect in an hbase table
 *
 */
public class VariantHBaseITCase {

    List<String> samples = Arrays.asList("sample1", "sample2");
    //two rows from vcf files (the first row would be from sample1, the second row would be from sample2
    String r1 = "22\t16050612\t.\tC\tG\t.\tPASS\tEffect_Impact=MODIFIER;Genotype_Number=1\tGT:AD:DP:GD:GL:GQ:OG\t0|0:.:.:.:.:13.70:./.";
    String r2 = "22\t16050612\t.\tC\tG\t.\tPASS\tBI,BC;EUR_R2=0.691;AFR_R2=0.718;Effect_Impact=MODIFIER\tGT:AD:DP:GD:GL:GQ:OG\t0|0:1,0:1:.:-0.00,-0.30,-4.39:16.65:./.";

    Configuration configuration;
    HBaseConnector hconnect;
    HBaseUtil hutil;
    VCFParserConfig vconfig;

    @Before
    public void setup() throws Exception {
        configuration = AutoConfigure.getConfiguration();
        hconnect = new HBaseConnector(configuration);
        hutil = new HBaseUtil(hconnect.getConnection());
        vconfig = new VCFParserConfig("src/main/resources/VCFParser.properties"); //we need to know what table to put the stuff in, but we don't need the parser

        hutil.createTable(vconfig.getVCFTable(),vconfig.getVCFColumnFamilies());
    }

    @After
    public void teardown() throws IOException {
        hconnect.close();
    }

    @Test
    public void testPut() throws ParseException {
        //here we are going to startup hbase without spark, do the put for each of the rows above and ensure that
        //the output is what we expect
        Variant v1 = new Variant(r1, samples);
        Put p1 = v1.toPut(vconfig);

        try (Table table = hconnect.getConnection().getTable(TableName.valueOf(tableName))) {
            Put put = new Put(Bytes.toBytes(rowKey));
            put.addColumn(Bytes.toBytes(colFamName), Bytes.toBytes(colQualifier), Bytes.toBytes(value));
            table.put(put);
        }

    }

}
