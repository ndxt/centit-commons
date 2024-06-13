package com.centit.support.file;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.centit.support.algorithm.CollectionsOpt;
import com.centit.support.algorithm.StringBaseOpt;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

public abstract class CsvFileIO {

    public static List<Map<String, Object>> readDataFromInputStream(InputStream  inputStream,
                                                  boolean firstRowAsHeader, List<String> columnNames,
                                                  String charsetType) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream,
            Charset.forName(charsetType)), 8192)) {
            // firstRowAsHeader 如果是true则以第一行为key生成一个map，如果第一行不够长，后面的key自动为 column+i
            // 如果firstRowAsHeader为false，则必须要指定每一列的key，不够长同样自动为column+i
            CSVParser csvParser = CSVFormat.EXCEL.parse(reader);
            List<CSVRecord> recordList = csvParser.getRecords();
            if(recordList==null || recordList.isEmpty()){
                return list;
            }
            int dataPos = 0;
            List<String> headers;
            if (firstRowAsHeader) {
                dataPos = 1;
                headers = CollectionsOpt.mergeTwoList(columnNames, recordList.get(0).toList());
            } else {
                headers = columnNames;
            }
            // csvFormat.withHeader(CollectionsOpt.listToArray(headers));
            int headLen = headers==null? 0 : headers.size();
            for (int k = dataPos; k < recordList.size(); k++) {
                CSVRecord record = recordList.get(k);
                int splitResultLength = record.size();
                Map<String, Object> map = new HashMap<>(splitResultLength);
                for (int i = 0; i < splitResultLength; i++) {
                    String columnName = i < headLen ? headers.get(i) : "column" + i;
                    map.put(columnName, record.get(i));
                }
                list.add(map);
            }
            csvParser.close();
            return list;
        }
    }

    public static void saveData2OutputStream(List<Map<String, Object>> listData, OutputStream outs,
                                          boolean firstRowAsHeader, List<String> columnNames,
                                          String charsetType) throws IOException {

        if (listData==null || listData.isEmpty()) {
            return;
        }
        if (columnNames == null || columnNames.isEmpty()) {
            Set<String> headers = new HashSet<>(20);
            for (Map<String, Object> row : listData) {
                headers.addAll(row.keySet());
            }
            columnNames = CollectionsOpt.cloneList(headers);
        }

        if (columnNames == null || columnNames.isEmpty()) {
            return;
        }

        try (
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outs, Charset.forName(charsetType)))) {
            CSVPrinter csvPrinter = CSVFormat.EXCEL.print(writer);
            if(firstRowAsHeader){
                csvPrinter.printRecord(columnNames);
            }

            String[] values = new String[columnNames.size()];
            for (Map<String, Object> row : listData) {
                for (int i = 0; i < columnNames.size(); i++) {
                    values[i] = StringBaseOpt.castObjectToString(row.get(columnNames.get(i)), "");
                }
                csvPrinter.printRecord(values);
            }
            csvPrinter.flush();
            csvPrinter.close();
        }
    }

    public static void saveJSON2OutputStream(JSONArray listData, OutputStream outs,
                                             boolean firstRowAsHeader, List<String> columnNames,
                                             String charsetType) throws IOException {
        if (listData==null || listData.isEmpty()) {
            return;
        }
        if (columnNames == null || columnNames.isEmpty()) {
            Set<String> headers = new HashSet<>(20);
            for (Object row : listData) {
                if(row instanceof JSONObject) {
                    headers.addAll(((JSONObject)row).keySet());
                }
            }
            columnNames = CollectionsOpt.cloneList(headers);
        }
        if (columnNames == null || columnNames.isEmpty()) {
            return;
        }

        try (
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outs, Charset.forName(charsetType)))) {
            CSVFormat csvFormat = CSVFormat.EXCEL;
            if(firstRowAsHeader){
                csvFormat.withHeader(CollectionsOpt.listToArray(columnNames));
            }
            CSVPrinter csvPrinter = csvFormat.print(writer);
            String[] values = new String[columnNames.size()];
            for (Object row : listData) {
                if(row instanceof JSONObject) {
                    JSONObject rowJson = (JSONObject) row;
                    for (int i = 0; i < columnNames.size(); i++) {
                        values[i] = StringBaseOpt.castObjectToString(rowJson.get(columnNames.get(i)), "");
                    }
                    csvPrinter.printRecord(values);
                }
            }
            csvPrinter.flush();
            csvPrinter.close();
        }
    }

}
