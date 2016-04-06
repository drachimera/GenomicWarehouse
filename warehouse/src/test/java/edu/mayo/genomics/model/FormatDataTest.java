package edu.mayo.genomics.model;

import junit.framework.TestCase;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FormatDataTest {

    String sample = "sample1";
    String format = "GT:AD:DP:GD:GL:GQ:OG";
    String sampleData = "0|0:3,0:3:.:-0.00,-0.90,-11.67:13.65:./.";

    @Test
    public void toHashTest(){
        String[] formatKeys = format.split(":");
        String[] sampleValues = sampleData.split(":");
        FormatData fd = new FormatData(sample, format, sampleData);
        HashMap<String,String> map = fd.getFormatData();
        assertEquals(formatKeys.length, map.size());
        int i = 0;
        for(String key : formatKeys){
            //System.out.println(key);
            String value = map.get(key);
            //System.out.println(value);
            assertEquals(sampleValues[i] , value);
            i++;
        }

        String encoded = fd.encodeFormatData(map);
        assertEquals("AD:3,0;GL:-0.00,-0.90,-11.67;OG:./.;GQ:13.65;DP:3;GT:0|0;GD:.", encoded);
        HashMap<String,String> decoded = fd.decodeFromData(encoded);
        i = 0;
        for(String key : formatKeys){
            String value = decoded.get(key);
            assertEquals(sampleValues[i] , value);
            i++;
        }
    }

}