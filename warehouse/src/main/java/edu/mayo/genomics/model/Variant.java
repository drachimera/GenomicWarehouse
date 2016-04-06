package edu.mayo.genomics.model;

import hbase.VCFParserConfig;
import htsjdk.samtools.util.Locatable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by m102417 on 2/2/16.
 * This is a genomic variant object for putting into HBase
 */
public class Variant extends SimpleInterval {

    private String id = ".";
    private String ref = "N";
    private List<String> alts = null;
    private Double qual = null;
    private String filter = null;
    //Sample - FormatData pairs (format data is the actual data for the sample in the VCF)
    private HashMap<String,FormatData> format = null;

    public Variant(String contig, int start, int end) {
        super(contig, start, end);
    }

    public Variant(Locatable locatable) {
        super(locatable);
    }

    /**
     *
     * @param VCFLine - this takes a vcf line as input and constructs the variant object from it
     */
    public Variant(String VCFLine, List<String> samples) throws ParseException {
        String[] tokens = VCFLine.split("\t");
        if(tokens.length > 9){
            //#CHROM  POS     ID      REF     ALT     QUAL    FILTER  INFO    FORMAT    SAMPLE
            contig = tokens[0].trim();             //CHROM
            start = new Integer(tokens[1].trim()); //POS
            id = tokens[2].trim();                 //ID
            ref = tokens[3].trim();                //REF
            alts = Arrays.asList(tokens[4].trim().split(","));    //ALT
            alts.sort(String::compareToIgnoreCase);
            end = start + ref.length() - 1;
            String qualtmp = tokens[5].trim();     //QUAL
            if(!qualtmp.equalsIgnoreCase(".")){
                qual = new Double(qualtmp);
            }
            filter = tokens[6];                    //FILTER
            //INFO

            int end = 0;

            //add samples
            for(int i = 0; i<tokens.length; i++){

            }

        }else {
            throw new ParseException("Malformed VCF on line (Number of columns incorrect: " + tokens.length + ") " + VCFLine, 0);
        }

    }

    /**
     *
     * @return - this is the unique hash key of the variant.  All variant types are mapped to a string
     * that is unique for that variant.
     */
    public String hash(){
        StringBuilder sb = new StringBuilder();
        sb.append(contig);
        sb.append("_");
        sb.append(new Integer(start).toString());//pos
        sb.append("_");
        sb.append(ref);
        sb.append("_");
        int n = 1;
        for(String alt : alts){
            sb.append(alt);
            if(n < alts.size()) {
                sb.append("_");
            }
            n++;
        }
        return sb.toString();

    }

    /**
     *
     * @return - a pretty string version of the variant object
     */
    public String pretty(){
        return this.toString();
    }


    public HashMap<String, FormatData> getFormat() {
        return format;
    }


    public String encodeList(List<String> l, String delimiter){
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for(String next : l){
            sb.append(next);
            if(i < l.size() -1){
                sb.append(",");
            }
        }
        return sb.toString();
    }

    /**
     * convert the data in this variant object into a put object for inserting into hbase.
     * @return
     * @param config - gives us a description of the table name and column familes so we can formalize the put correctly
     */
    public Put toPut(VCFParserConfig config){
        Put p = new Put(Bytes.toBytes(hash()));

        //base columns
        byte[] basecf = Bytes.toBytes(VCFParserConfig.BASE_FIELDS);
        p.addColumn(basecf,Bytes.toBytes("CHROM"),Bytes.toBytes(this.contig));
        p.addColumn(basecf,Bytes.toBytes("POS"),Bytes.toBytes(this.getStart()));
        p.addColumn(basecf,Bytes.toBytes("REF"),Bytes.toBytes(this.ref));
        p.addColumn(basecf,Bytes.toBytes("ALT"),Bytes.toBytes(encodeList(this.alts,",")));



//        HashMap<String,String> meta = sample.getMetadata();
//        for(String key : meta.keySet()){
//            p.addColumn(Bytes.toBytes(METADATA_CF), Bytes.toBytes(key), Bytes.toBytes(meta.get(key)));
//        }
        return p;
    }
}
