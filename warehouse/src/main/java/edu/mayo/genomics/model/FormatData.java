package edu.mayo.genomics.model;

import org.apache.hadoop.hbase.util.Bytes;

import java.util.HashMap;

/**
 * Created by m102417 on 2/4/16.
 */
public class FormatData {
    String sampleID; //the sample identifier for this FormatData
    HashMap<String,String> formatData;  //all of the data for a given sample stored in a map.
    //examples of formatdata:
    //GT : "./1",
    //PL : "0,24,1045",
    //AD_1 : 23,
    //AD_2 : 44,
    //GQ : 40,
    //HQ : "24,40",
    //DP : 20,
    //MIN_DP : 20,
    //gene : "BRCA2",
    //sgV : 1 #sanger validated,
    //GTC : 1


    public FormatData(String sample, String formatCol, String sampleData){
        this.sampleID = sample;
        formatData = toHash(formatCol, sampleData);
    }

    private final String COLON = ":"; //vcf spec uses a colon to seperate values in the format fields
    /**
     *
     * @param formatCol   - column for the format data
     * @param sampleData  - column for the sample (formated as specified by formatCol)
     */
    public HashMap<String,String> toHash(String formatCol, String sampleData){
        HashMap<String,String> map = new HashMap<String,String>();
        String[] keys = formatCol.split(COLON);
        String[] values = sampleData.split(COLON);
        int i=0;
        for(String key : keys){
            map.put(key, values[i]);
            i++;
        }
        return map;
    }

    String DELIMITER1 = ";";
    String DELIMITER2 = ":";
    public String encodeFormatData(HashMap<String,String> map){
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for(String key : formatData.keySet()){
            sb.append(key.trim());
            sb.append(DELIMITER2);
            sb.append(map.get(key).trim());
            if(i < map.size()-1){
                sb.append(DELIMITER1);
            }
            i++;
        }
        return sb.toString();
    }

    public HashMap<String,String> decodeFromData(String encoded){
        HashMap<String,String> map = new HashMap<String,String>();
        String[] pairs = encoded.split(DELIMITER1);
        if( (pairs.length) < 1) return map;
        System.out.println(encoded);
        for(String pair : pairs){
            System.out.println(pair);
            String[] kv = pair.split(DELIMITER2);
            if(kv.length != 2) return map;
            map.put(kv[0],kv[1]);
        }
        return map;
    }

    public String getSampleID() {
        return sampleID;
    }

    public HashMap<String, String> getFormatData() {
        return formatData;
    }
}
