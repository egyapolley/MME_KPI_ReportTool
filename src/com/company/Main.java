package com.company;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.*;
import java.util.stream.Stream;


public class Main {

    public static void main(String[] args) {



        Properties properties = new Properties();



        try (
             InputStream inputStream = Files.newInputStream(Paths.get("/mme2/report/config.properties"))
        ) {
            long start = System.currentTimeMillis();
            properties.load(inputStream);
            String url = properties.getProperty("jdbc.url");
            String username = properties.getProperty("jdbc.username");
            String password = properties.getProperty("jdbc.password");

            String input_dir_prop = properties.getProperty("file.input_dir");
            String output_dir_prop = properties.getProperty("file.output_dir");





            try (Stream<Path> input_dir = Files.list(Paths.get(input_dir_prop));
                    Connection connection = DatabaseUtils.getConnection(url, username, password)) {
                if (connection != null) {

                    Stats stats = new Stats();

                    input_dir.forEach(p -> {

                        File file = p.toFile();
                        Path topath = Paths.get(output_dir_prop, file.getName());
                        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
                        saxParserFactory.setNamespaceAware(true);

                        try {
                            SAXParser saxParser = saxParserFactory.newSAXParser();
                            for (int i = 0; i < Counters.getCounters().length; i++) {
                                saxParser.parse(file, new MyHandler(Counters.getCounters()[i], file.getName(), connection,stats));
                            }
                            stats.incrementFileCounter();
                            Files.move(file.toPath(), topath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });

                    stats.produceStats();
                }else {
                    System.out.println("Unable to connect to the Database");

                }


            }


            System.out.println((System.currentTimeMillis() - start) / 1000);

        } catch (Exception e) {
            e.printStackTrace();

        }


    }


}

class MyHandler extends DefaultHandler2 {

    private final String counter;
    private final String fileName;


    private String temp_counter;

    private final Connection connection;
    private final Stats stats;



    public MyHandler(String counter,String fileName,Connection connection,Stats stats) {

        this.counter = counter;
        this.fileName = fileName;
        this.connection = connection;
        this.stats = stats;

    }

    private Attributes measType_attributes;

    private String counterAttributeValue;

    private String dataTime;







    private Attributes r_attributes;


    private boolean inMeasInfo;
    private boolean inMeasValue;
    private boolean inMeasType;
    private boolean inR;


    private final List<Map<String, List<String>>> resultList = new ArrayList<>();

    private final Map<String, List<String>> temp_result_map = new HashMap<>();
    private final List<String> counterValues = new ArrayList<>();

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (localName.equals("measInfo")) {
            this.inMeasInfo = true;

        }

        if (inMeasInfo && localName.equals("granPeriod")){
            this.dataTime = attributes.getValue(1);
        }
        if (localName.equals("measType")) {
            this.inMeasType = true;
        }
        if (inMeasInfo && inMeasType) {
            this.measType_attributes = attributes;
        }
        if (inMeasInfo && localName.equals("measValue")) {
            this.inMeasValue = true;

        }
        if (inMeasInfo && inMeasValue && localName.equals("r")) {
            inR = true;
            this.r_attributes = attributes;

        }


    }


    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (inMeasInfo && inMeasType) {
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = start; i < start + length; i++) {
                stringBuilder.append(ch[i]);

            }
            String textContent = stringBuilder.toString();

            if (textContent.equals(this.counter)) {
                this.temp_counter = this.counter;
                for (int i = 0; i < measType_attributes.getLength(); i++) {
                    this.counterAttributeValue = measType_attributes.getValue(i);
                }
            }
        }

        if (inMeasInfo && inMeasValue && inR && temp_counter != null) {
            for (int i = 0; i < r_attributes.getLength(); i++) {
                if (r_attributes.getValue(i).equals(counterAttributeValue)) {
                    StringBuilder stringBuilder = new StringBuilder();
                    for (int j = start; j < start + length; j++) {
                        stringBuilder.append(ch[j]);

                    }
                    String textContent = stringBuilder.toString();
                    this.counterValues.add(textContent);


                }

            }
        }


    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (localName.equals("measType")) {
            this.inMeasType = false;
        }

        if (inMeasInfo && inMeasValue && localName.equals("r")) {
            inR = false;
            this.r_attributes = null;

        }

        if (inMeasInfo && localName.equals("measValue")) {
            this.inMeasValue = false;

        }

        if (localName.equals("measInfo") && !this.counterValues.isEmpty() && temp_counter != null) {
            this.inMeasInfo = false;
            this.temp_counter = null;


        }


    }

    @Override
    public void endDocument() throws SAXException {
        if (!temp_result_map.containsKey(this.counter)) {
            if (!this.counterValues.isEmpty()) {
                temp_result_map.put(this.counter, this.counterValues);
                resultList.add(temp_result_map);
            }


        }


        if (!resultList.isEmpty()) {
           DataSet dataSet = new DataSet(this.dataTime,this.fileName, resultList);
            String tableName = this.counter.substring(3);
            String oneColumnQuery = "insert into  "+tableName+ " (dateTime, fileName,counterValue_1) values(?,?,?)";
            String twoColumnQuery = "insert into "+tableName+ " (dateTime, fileName,counterValue_1,counterValue_2) values(?,?,?,?)";
            if (counter.equals("VS.memUsage")||counter.equals("VS.maxNEmemUsage")||counter.equals("VS.aveCpuUsage")||counter.equals("VS.peakCpuUsage")){
                insertDB_Ten_Column(connection,dataSet);

            }else if (counter.equals("VS.NbrPageRespInLastSeenTA")||counter.equals("VS.NbrPageRespNotInLastSeenTA")){
                insertDB_One_Column(connection,oneColumnQuery,dataSet);

            }else {
                insertDB_Two_Column(connection,twoColumnQuery,dataSet);

            }

            stats.incrementRecordCounter();

        }


    }

    private void insertDB_One_Column(Connection connection, String sql,DataSet dataSet){

        try(PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1,dataSet.getDataTime());
            preparedStatement.setString(2,dataSet.getFile_name());
            preparedStatement.setInt(3,Integer.parseInt(dataSet.getMapList().get(0).get(counter).get(0)));
            preparedStatement.executeUpdate();


        }catch (Exception e){
            e.printStackTrace();

        }



    }

    private void insertDB_Two_Column(Connection connection, String sql,DataSet dataSet){
        try(PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1,dataSet.getDataTime());
            preparedStatement.setString(2,dataSet.getFile_name());
            preparedStatement.setInt(3,Integer.parseInt(dataSet.getMapList().get(0).get(counter).get(0)));
            preparedStatement.setInt(4,Integer.parseInt(dataSet.getMapList().get(0).get(counter).get(1)));
            preparedStatement.executeUpdate();


        }catch (Exception e){
            e.printStackTrace();

        }





    }

    private void insertDB_Ten_Column(Connection connection,DataSet dataSet){
        prepareSQLStatement(connection,dataSet);

    }

    private void prepareSQLStatement(Connection connection, DataSet dataSet){
        List<String> counterValues = dataSet.getMapList().get(0).get(counter);

        StringBuilder part1 = new StringBuilder("insert into "+this.counter.substring(3)+" (dateTime, fileName,");
        StringBuilder part2 = new StringBuilder(" values(?,?,");

        for (int i = 1; i <= counterValues.size(); i++) {
            if (i == counterValues.size()){
                part1.append("counterValue_").append(i).append(")");
                part2.append("?)");

            }else {
                part1.append("counterValue_").append(i).append(",");
                part2.append("?").append(",");
            }

        }

        String stringQuery = part1.append(part2).toString();
        try (PreparedStatement preparedStatement =connection.prepareStatement(stringQuery)){
            preparedStatement.setString(1,dataSet.getDataTime());
            preparedStatement.setString(2,dataSet.getFile_name());
            int insert_idx =3;
            for (int i = 0; i < counterValues.size(); i++) {
                preparedStatement.setDouble((insert_idx+i),Double.parseDouble(counterValues.get(i)));
            }

            preparedStatement.executeUpdate();


        }catch (Exception e){
            e.printStackTrace();

        }



    }
}
