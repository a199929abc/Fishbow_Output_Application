import javax.swing.*;
import java.awt.event.ActionListener;
import java.io.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.json.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.json.JSONObject;
import org.json.XML;
import org.xml.sax.InputSource;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import pojo.Bowl;

import  java.lang.Class.*;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

public class App extends Application {

    public static String fileName;
    public static String userName;// username
    public static String passWord;// password
    public static final String IA_NAME = "Java Sample"; //API name
    public static final String IA_DESC = "Sample of Java Fishbowl connection";
    public static final int IA_KEY = 54321;//Default key
    public static String ticketKey;
    public static String path;// file path
    private static SAXBuilder builder = new SAXBuilder();
    private static String hostName = "universityofvictoria.myfishbowl.com";
    private static int port = 28192;
    long start = System.currentTimeMillis();
    public static int PRETTY_PRINT_INDENT_FACTOR = 4;



    public static void main(String[] args) {
        // Create new object of this class
      //  long start = System.currentTimeMillis();

        launch();//UI
        Connection connection = new Connection(hostName, port);
        // Create login XML request and send to the server

        String loginRequest = Requests.loginRequest(App.userName,App.passWord);
        String response = connection.sendRequest(loginRequest);
        if (response == null) {
            connection.disconnect();
            System.out.println("response is Null");
            System.exit(1);
        }
        // Parse response
        Document document = null;
        try {
            document = parseXml(response);
        } catch (Exception e) {
            System.out.println("Cannot parse the xml response.");
            connection.disconnect();
            System.exit(1);
        }
        Element root = document.getRootElement();
        String statusCode = root.getChild("FbiMsgsRs").getAttribute("statusCode").getValue();

        if (!statusCode.equals("1000")) {
            System.out.println("The integrated application needs to be approved.");
            connection.disconnect();
            System.exit(0);
        }
        // Login successful, store the key
        ticketKey = root.getChild("Ticket").getChild("Key").getValue();
        Element node = null;
        long start = System.currentTimeMillis();

        write_to_Excel(connection,response);//run write excel code
        App.infoBox("Process Completed ! ","successful");
        long end = System.currentTimeMillis();
        NumberFormat formatter = new DecimalFormat("#0.00000");
        System.out.print("Execution time is " + formatter.format((end - start) / 1000d/60) + " mins");
        connection.disconnect();
        System.exit(1);

    }
    /**
     * @param xml
     * @return
     * @throws IOException
     * @throws JDOMException
     */
    private static Document parseXml(String xml) throws IOException, JDOMException {
        return builder.build(new InputSource(new StringReader(xml)));
    }
    /**
     *
     * @param response
     * @return String description
     */
    private static String getDescription(String response){
        String result = response.substring(response.indexOf("<Description>")+13,response.indexOf("</Description>"));
        return result;
    }
    /**
     * @param primaryStage
     * @throws Exception
     */
    public void start(Stage primaryStage) throws Exception {
     primaryStage.setTitle("Login");//https://blog.csdn.net/guanguoxiang/article/details/45461879
     GridPane grid =new GridPane();
     grid.setAlignment(Pos.CENTER);
     grid.setHgap(10);
     grid.setVgap(10);
     grid.setPadding(new Insets(25,25,25,25));
     Scene sence =new Scene(grid,350,275);


    Text scenetitle = new Text("Login Fishbowl ");
    scenetitle.setFont(Font.font(20));
    grid.add(scenetitle, 0, 0, 2, 1);



    Label username = new Label("Username:");
    grid.add(username, 0, 1);
    username.setFont(Font.font(13));


    TextField userTextField = new TextField();
    grid.add(userTextField, 1, 1);

    Label pw = new Label("Password:");
    pw.setFont(Font.font(13));
    grid.add(pw, 0, 2);

    PasswordField pwBox = new PasswordField();
    grid.add(pwBox, 1, 2);

    Button btn_login = new Button("Sign in");
    HBox hbBtn = new HBox(10);

    hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
    hbBtn.getChildren().add(btn_login);
    grid.add(hbBtn, 1, 4);
    Text actiontarget = new Text();
    actiontarget.setFont(Font.font(13));
    grid.add(actiontarget, 1, 6);

    btn_login.setOnAction(new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            actiontarget.setFill(Color.RED);
            actiontarget.setText("Connecting to server . . . . . . ");
            userName=userTextField.getText();
            passWord=pwBox.getText();
            Connection connection = new Connection(hostName, port);
            // Create login XML request and send to the server
            String loginRequest = Requests.loginRequest(userName,passWord);
            String response = connection.sendRequest(loginRequest);
            // Parse response
            Document document = null;
            try {
                document = parseXml(response);
            } catch (Exception e) {
                System.out.println("Cannot parse the xml response.");
                connection.disconnect();
                System.exit(1);
            }
            Element root = document.getRootElement();
            String statusCode = root.getChild("FbiMsgsRs").getAttribute("statusCode").getValue();

            if (statusCode.equals("1000")) {
                //connection build
                connection.disconnect();
                primaryStage.close();
                Stage secondStage = new Stage();
                gotoFile(secondStage);
            }else{
               App.infoBox("username and password doesn't match with system ", "Login Error");
            }

        }
    });
    primaryStage.setScene(sence);
    primaryStage.show();
    }
    /**
     *
     * @param secondStage
     */
    private void gotoFile(Stage secondStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        System.out.println(System.getProperty("user.home"));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Excel", "*.xlsx")
        );
        Text fileChoose = new Text("Please Choose a File ");
        fileChoose.setFont(Font.font(15));

        Button btnProcess =new Button ("Enter");
        Button btnFile = new Button("Explore");
        btnProcess.setMaxSize(150,150);
        btnFile.setMaxSize(150,150);
        GridPane gridPane = new GridPane();
        gridPane.setMinSize(400, 200);
        gridPane.setPadding(new Insets(10, 10, 10, 10));
        //Setting the vertical and horizontal gaps between the columns
        gridPane.setVgap(5);
        gridPane.setHgap(5);
        gridPane.add(fileChoose,0,0);
        gridPane.add(btnFile,0,1);
        gridPane.add(btnProcess,0,2);

        //Setting the Grid alignment
        gridPane.setAlignment(Pos.TOP_LEFT);
        Scene sceneFile = new Scene(gridPane);
        secondStage.setTitle("Open File ");
        btnFile.setOnAction(new EventHandler<ActionEvent>() {
            int i=2;
            public void handle(ActionEvent actionEvent) {
                i+=1;
                FileChooser fileChooser = new FileChooser();
                fileChooser.setInitialDirectory(new File(System.getProperty("user.home"))
                );
                fileChooser.getExtensionFilters().addAll(
                        new FileChooser.ExtensionFilter("Excel", "*.xlsx")

                );
                File file =fileChooser.showOpenDialog(null);
                path = file.getPath();
                Label labelPath1 = new Label(path);
                labelPath1.setTextFill(Color.BLUE);
                System.out.println("path is "+path);
                gridPane.add(labelPath1,0,i);

            }
        });
        btnProcess.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {

                fileName = path.substring(path.lastIndexOf("\\")+1);
                infoBox(("Processing file "+fileName),"Successful");
                secondStage.close();
            }
        });
        secondStage.setScene(sceneFile);
        secondStage.show();
    }

    public static void infoBox(String infoMessage, String titleBar) {
        JOptionPane.showMessageDialog(null, infoMessage, titleBar, JOptionPane.INFORMATION_MESSAGE);
    }
    /**
     *
     * @param connection
     * @param response
     */
    public static void write_to_Excel(Connection connection,String response){
        try {
            //创建工作簿

            FileInputStream is = new FileInputStream(App.path);
            excelReader reader = new excelReader(is);
            reader.read(is);

            int max_row = reader.maxRow();

            int count=0;
            excelWriter ew = new excelWriter(path, "name");
            //Writing Header to the sheet
            ew.write_header();
            //export data format
            try{
            ArrayList collect=new ArrayList();

            int rowNum=1;
          for (int i = 1; i < max_row; i++) {

              System.out.println("****************************************************************");
              Thread.sleep(500);
              count=i+1;
              collect= reader.processRow(i);
              if (collect.get(0) == null && collect.get(1) == null && collect.get(2) == null) {

                  break;
              }
          if (collect.get(2) != null) {
            BigInteger partid = new BigDecimal(collect.get(2).toString()).toBigInteger();
            // get Part XML format
            String getPart = Requests.get_part(partid);
            System.out.println(partid);
            // connect to API and get response back
            response = connection.sendRequest(getPart);
            Bowl bowl = new Bowl();
            JSONObject xmlJSONObj = XML.toJSONObject(response);
            String jsonPrettyPrintString = xmlJSONObj.toString(PRETTY_PRINT_INDENT_FACTOR);
            //System.out.println(jsonPrettyPrintString);

            bowl.setABCCode(xmlJSONObj.getJSONObject("FbiXml").getJSONObject("FbiMsgsRs").
                    getJSONObject("PartQueryRs").getJSONObject("Part").getString("ABCCode"));

            bowl.setHeight(xmlJSONObj.getJSONObject("FbiXml").getJSONObject("FbiMsgsRs").
                    getJSONObject("PartQueryRs").getJSONObject("Part").getInt("Height"));

            bowl.setStandardCost(xmlJSONObj.getJSONObject("FbiXml").getJSONObject("FbiMsgsRs").
                    getJSONObject("PartQueryRs").getJSONObject("Part").getDouble("StandardCost"));

            bowl.setWidth(xmlJSONObj.getJSONObject("FbiXml").getJSONObject("FbiMsgsRs").
                    getJSONObject("PartQueryRs").getJSONObject("Part").getDouble("Width"));

            bowl.setDetails(xmlJSONObj.getJSONObject("FbiXml").getJSONObject("FbiMsgsRs").
                    getJSONObject("PartQueryRs").getJSONObject("Part").getString("Details"));

            bowl.setPartID(xmlJSONObj.getJSONObject("FbiXml").getJSONObject("FbiMsgsRs").
                    getJSONObject("PartQueryRs").getJSONObject("Part").getInt("PartID"));

            bowl.setLen(xmlJSONObj.getJSONObject("FbiXml").getJSONObject("FbiMsgsRs").
                    getJSONObject("PartQueryRs").getJSONObject("Part").getDouble("Len"));
//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
              try{

                 // System.out.println(xmlJSONObj.getJSONObject("FbiXml").getJSONObject("FbiMsgsRs").getJSONObject("PartQueryRs")
                   //       .getJSONArray("tag").getJSONObject(0).getInt( "Quantity"));

                  bowl.setTag(xmlJSONObj.getJSONObject("FbiXml").getJSONObject("FbiMsgsRs")
                          .getJSONObject("PartQueryRs").getJSONArray("Tag"));
                  //System.out.println("Tag is an array with length : "+ bowl.getTag().length());
                  bowl.setArrayFlag(0);

              }catch (Exception e){
                  bowl.setArrayFlag(1);
                  bowl.setSingleTag(xmlJSONObj.getJSONObject("FbiXml").getJSONObject("FbiMsgsRs")
                          .getJSONObject("PartQueryRs").getJSONObject("Tag"));
                  //System.out.print("Tag is not an array");
              }

           // System.out.println(xmlJSONObj.getJSONObject());
              //System.out.println("Flag : "+bowl.getArrayFlag());
//!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            // get Description String

            String result = getDescription(response).replaceAll(",","");
            String DIpattern = "(?<=DI:)((\\s\\w.*?\\s)|(\\w.*?\\s)|(\\s\\w.*))";
            Pattern DIr = Pattern.compile(DIpattern);
            Matcher DIm = DIr.matcher(result);
            String SNpattern = "(?<=SN:)((\\s\\w.*?\\s)|(\\w.*?\\s)|(\\s\\w.*))";
            Pattern SNr = Pattern.compile(SNpattern);
            Matcher SNm = SNr.matcher(result);

            Boolean SNt = SNm.find();
            Boolean DIt = DIm.find();

            if (DIt != true && i<=154 && i>=1903) {
                continue;
            }

            if(DIt!= true){
                continue;
            }
            System.out.println(result);

            List<Object> data =new ArrayList<Object>();

            ArrayList rowFill = new ArrayList();

            if(bowl.getArrayFlag()==1){

                System.out.println("- - - - Single Location - - - -");

                if( DIt==true && SNt==true){

                    String DImatchGroup = DIm.group();
                    String SNmatchGroup= SNm.group();

                    System.out.println("1111111111111111");

                    String name = result.replace("DI:","");
                    name = name.replace("SN:","");
                    name = name.replace(DImatchGroup,"");
                    name = name .replace(SNmatchGroup,"");
                    rowFill.add(name);
                    rowFill.add(SNmatchGroup);
                    rowFill.add(DImatchGroup);
                    rowFill.add(partid.toString());
                    //rowFill.add(bowl.getABCCode());
                    rowFill.add("$"+bowl.getStandardCost());
                    // rowFill.add(bowl.getLen());
                   //  rowFill.add(bowl.getHeight());
                   // rowFill.add(bowl.getWidth());
                    // rowFill.add(bowl.getPartID());


                    rowFill.add(bowl.getSingleTag().getInt("Quantity"));
                    //location
                    rowFill.add(bowl.getSingleTag().getJSONObject("Location")
                            .getString("LocationGroupName")
                            + ": "
                            +bowl.getSingleTag().getJSONObject("Location")
                            .getString("Name"));
                    //rowFill.add(bowl.getSingleTag().getJSONObject("Location")
                            //.getString("Name"));
                    rowFill.add(bowl.getSingleTag().getJSONObject("Location")
                            .getString("Description"));
                    rowFill.add(bowl.getDetails());
                    rowFill.add(result);

                    ew.write_row(rowFill,rowNum);
                    rowNum++;

                }
                else if(SNt==true && DIt!=true){

                    String SNmatchGroup= SNm.group();

                    System.out.println("222222222222");
                    String name = result.replace("SN:","");
                    name = name.replace(SNmatchGroup,"");
                    rowFill.add(name);
                    rowFill.add(SNmatchGroup);
                    rowFill.add("");
                    rowFill.add(partid.toString());
                    // rowFill.add(bowl.getABCCode());
                    rowFill.add("$"+bowl.getStandardCost());
                    // rowFill.add(bowl.getLen());
                   //  rowFill.add(bowl.getHeight());
                   // rowFill.add(bowl.getWidth());
                    // rowFill.add(bowl.getPartID());

                    rowFill.add(bowl.getSingleTag().getInt("Quantity"));
                    //location
                    rowFill.add(bowl.getSingleTag().getJSONObject("Location")
                            .getString("LocationGroupName")  + ": "
                            +bowl.getSingleTag().getJSONObject("Location")
                            .getString("Name"));
                    //rowFill.add(bowl.getSingleTag().getJSONObject("Location").getString("Name"));
                    rowFill.add(bowl.getSingleTag().getJSONObject("Location")
                    .getString("Description"));
                    rowFill.add(bowl.getDetails());

                    rowFill.add(result);

                    ew.write_row(rowFill,rowNum);
                    rowNum++;
                }
                else if(SNt!=true && DIt==true){

                    String DImatchGroup = DIm.group();


                    System.out.println("33333333333333");
                    String name = result.replace("DI:","");

                    name = name.replace(DImatchGroup,"");
                    System.out.println(DImatchGroup+"fuck you");
                    rowFill.add(name);
                    rowFill.add("");
                    rowFill.add(DImatchGroup);
                    rowFill.add(partid.toString());
                    // rowFill.add(bowl.getABCCode());
                    rowFill.add("$"+bowl.getStandardCost());
                    // rowFill.add(bowl.getLen());
                   //  rowFill.add(bowl.getHeight());
                   // rowFill.add(bowl.getWidth());
                    // rowFill.add(bowl.getPartID());


                    rowFill.add(bowl.getSingleTag().getInt("Quantity"));

                    //location
                    rowFill.add(bowl.getSingleTag().getJSONObject("Location")
                            .getString("LocationGroupName")  + ": "
                            +bowl.getSingleTag().getJSONObject("Location")
                            .getString("Name"));
                   // rowFill.add(bowl.getSingleTag().getJSONObject("Location").getString("Name"));
                    rowFill.add(bowl.getSingleTag().getJSONObject("Location")
                            .getString("Description"));
                    rowFill.add(bowl.getDetails());

                    rowFill.add(result);

                    ew.write_row(rowFill,rowNum);
                    rowNum++;

                }
                else {
                    System.out.println("4444444444444444");
                    rowFill.add(result);
                    rowFill.add("");
                    rowFill.add("");
                    rowFill.add(partid.toString());
                    // rowFill.add(bowl.getABCCode());
                    rowFill.add("$"+bowl.getStandardCost());
                    // // rowFill.add(bowl.getLen());
                   //  rowFill.add(bowl.getHeight());
                   // rowFill.add(bowl.getWidth());
                    // rowFill.add(bowl.getPartID());

                    rowFill.add(bowl.getSingleTag().getInt("Quantity"));

                    //location
                    rowFill.add(bowl.getSingleTag().getJSONObject("Location")
                            .getString("LocationGroupName")  + ": "
                            +bowl.getSingleTag().getJSONObject("Location")
                            .getString("Name"));
                    //rowFill.add(bowl.getSingleTag().getJSONObject("Location").getString("Name"));
                    rowFill.add(bowl.getSingleTag().getJSONObject("Location")
                            .getString("Description"));

                    rowFill.add(bowl.getDetails());

                    rowFill.add(result);
                    ew.write_row(rowFill,rowNum);
                    rowNum++;
                }

            }
            else {
                JSONObject tagTemp = new JSONObject();

                for(int k=0; k< bowl.getTag().length(); k++){
                    tagTemp = bowl.getTag().getJSONObject(k);
                    //System.out.println("TAG quantity : "+tagTemp.getInt("Quantity"));
                    rowFill = new ArrayList();

                    if( DIt == true && SNt == true ){
                        //System.out.println(rowNum+"   ...............................");

                        String DImatchGroup = DIm.group();
                        String SNmatchGroup= SNm.group();

                        System.out.println("5555555555555555");
                        String name = result.replace("DI:","");
                        name = name.replace("SN:","");
                        name = name.replace(DImatchGroup,"");
                        name = name .replace(SNmatchGroup,"");
                        rowFill.add(name);
                        rowFill.add(SNmatchGroup);
                        rowFill.add(DImatchGroup);
                        rowFill.add(partid.toString());
                        // rowFill.add(bowl.getABCCode());
                        rowFill.add("$"+bowl.getStandardCost());
                        // rowFill.add(bowl.getLen());
                        // rowFill.add(bowl.getHeight());
                        // rowFill.add(bowl.getWidth());
                        // rowFill.add(bowl.getPartID());


                        //quantity
                        rowFill.add(tagTemp.getInt("Quantity"));
                        rowFill.add(tagTemp.getJSONObject("Location")
                                .getString("LocationGroupName")  + ": "
                                +tagTemp.getJSONObject("Location").getString("Name"));
                        //rowFill.add(tagTemp.getJSONObject("Location").getString("Name"));
                        rowFill.add(tagTemp.getJSONObject("Location")
                                .getString("Description"));

                        rowFill.add(bowl.getDetails());

                        rowFill.add(result);
                        ew.write_row(rowFill,rowNum);
                        rowNum++;

                    }
                    else if(SNt==true && DIt!=true){

                        String SNmatchGroup= SNm.group();
                        //System.out.println(rowNum+"   ...............................");
                        System.out.println("66666666666666");
                        String name = result.replace("SN:","");
                        name = name .replace(SNmatchGroup,"");

                        rowFill.add(name);
                        rowFill.add(SNmatchGroup);
                        rowFill.add("");
                        rowFill.add(partid.toString());
                        // rowFill.add(bowl.getABCCode());
                        rowFill.add("$"+bowl.getStandardCost());
                        // rowFill.add(bowl.getLen());
                        //  rowFill.add(bowl.getHeight());
                        // rowFill.add(bowl.getWidth());
                        // rowFill.add(bowl.getPartID());

                        //quantity
                        rowFill.add(tagTemp.getInt("Quantity"));
                        rowFill.add(tagTemp.getJSONObject("Location")
                                .getString("LocationGroupName")  + ": "
                                +tagTemp.getJSONObject("Location").getString("Name"));
                        //rowFill.add(tagTemp.getJSONObject("Location").getString("Name"));
                        rowFill.add(tagTemp.getJSONObject("Location")
                                .getString("Description"));

                        rowFill.add(bowl.getDetails());

                        rowFill.add(result);

                        ew.write_row(rowFill,rowNum);
                        rowNum++;

                    }

                    else if(SNt!= true && DIt== true){

                        String DImatchGroup = DIm.group();

                        System.out.println("77777777777777777");
                        String name = result.replace("DI:","");
                        //System.out.println(rowNum+"   ...............................");

                        name = name.replace(DImatchGroup,"");

                        rowFill.add(name);
                        rowFill.add("");
                        rowFill.add(DImatchGroup);
                        rowFill.add(partid.toString());
                        // rowFill.add(bowl.getABCCode());
                        rowFill.add("$"+bowl.getStandardCost());
                        // rowFill.add(bowl.getLen());
                        // rowFill.add(bowl.getHeight());
                        // rowFill.add(bowl.getWidth());
                        // rowFill.add(bowl.getPartID());


                        //quantity
                        rowFill.add(tagTemp.getInt("Quantity"));
                        rowFill.add(tagTemp.getJSONObject("Location")
                                .getString("LocationGroupName")  + ": "
                                +tagTemp.getJSONObject("Location").getString("Name"));
                        //rowFill.add(tagTemp.getJSONObject("Location").getString("Name"));
                        rowFill.add(tagTemp.getJSONObject("Location")
                                .getString("Description"));

                        rowFill.add(bowl.getDetails());

                        rowFill.add(result);
                        ew.write_row(rowFill,rowNum);

                        rowNum++;


                    }
                    else {

                        //System.out.println(rowNum+"   ...............................");
                        System.out.println("888888888888888888");
                        rowFill.add(result);
                        rowFill.add("");
                        rowFill.add("");
                        rowFill.add(partid.toString());
                        // rowFill.add(bowl.getABCCode());
                        rowFill.add("$"+bowl.getStandardCost());
                        // rowFill.add(bowl.getLen());
                       //  rowFill.add(bowl.getHeight());
                       // rowFill.add(bowl.getWidth());
                        // rowFill.add(bowl.getPartID());


                        //quantity
                        rowFill.add(tagTemp.getInt("Quantity"));
                        rowFill.add(tagTemp.getJSONObject("Location")
                                .getString("LocationGroupName")  + ": "
                                +tagTemp.getJSONObject("Location").getString("Name"));
                       // rowFill.add(tagTemp.getJSONObject("Location").getString("Name"));
                        rowFill.add(tagTemp.getJSONObject("Location")
                                .getString("Description"));

                        rowFill.add(bowl.getDetails());


                        rowFill.add(result);

                        ew.write_row(rowFill,rowNum);
                        rowNum++;
                    }


                }
            }
                    }
                }
              //end of for
      } catch (Exception e){
          App.infoBox(String.format("ERROR: Please check row number %s then retry",count),"Error");
          e.printStackTrace();
          System.exit(1);

      }

            String outpath=System.getProperty("user.home");
            String output_path =String.format("%s\\out_%s",outpath,fileName);
            System.out.println(output_path);

            ew.create_excel(output_path);//Write to excel



        }catch (IOException e){
            connection.disconnect();
            e.printStackTrace();
        }
    }

    public void writeRow(String name,String DImatchGroup, String SNmatchGroup, Bowl bowl){

    }
}
















    /** public static void write_to_Excel_withoutTag(Connection connection,String response){
        try {
            //创建工作簿
            FileInputStream is = new FileInputStream(App.path);
            excelReader reader = new excelReader(is);
            reader.read(is);

            int max_row = reader.maxRow();

            int count=0;
            excelWriter ew = new excelWriter(path, "name");
            //Writing Header to the sheet
            ew.write_header();
            //export data format
            try{
                ArrayList collect=new ArrayList();
                for (int i = 1; i < max_row; i++) {
                    System.out.println(i);
                    count=i+1;
                    collect= reader.processRow(i);
                    if (collect.get(0) == null && collect.get(1) == null && collect.get(2) == null) {
                        System.out.println("Process have completed！");
                        break;
                    }
                    if (collect.get(2) != null) {
                        BigInteger partid = new BigDecimal(collect.get(2).toString()).toBigInteger();
                        // get Part XML format
                        String getPart = Requests.get_part(partid);
                        System.out.println(partid);
                        // connect to API and get response back
                        response = connection.sendRequest(getPart);
                        Bowl bowl = new Bowl();
                        JSONObject xmlJSONObj = XML.toJSONObject(response);
                        String jsonPrettyPrintString = xmlJSONObj.toString(PRETTY_PRINT_INDENT_FACTOR);
                        System.out.println(jsonPrettyPrintString);

                        bowl.setABCCode(xmlJSONObj.getJSONObject("FbiXml").getJSONObject("FbiMsgsRs").
                                getJSONObject("PartQueryRs").getJSONObject("Part").getString("ABCCode"));

                        bowl.setHeight(xmlJSONObj.getJSONObject("FbiXml").getJSONObject("FbiMsgsRs").
                                getJSONObject("PartQueryRs").getJSONObject("Part").getInt("Height"));

                        bowl.setStandardCost(xmlJSONObj.getJSONObject("FbiXml").getJSONObject("FbiMsgsRs").
                                getJSONObject("PartQueryRs").getJSONObject("Part").getDouble("StandardCost"));

                        bowl.setWidth(xmlJSONObj.getJSONObject("FbiXml").getJSONObject("FbiMsgsRs").
                                getJSONObject("PartQueryRs").getJSONObject("Part").getDouble("Width"));

                        bowl.setDetails(xmlJSONObj.getJSONObject("FbiXml").getJSONObject("FbiMsgsRs").
                                getJSONObject("PartQueryRs").getJSONObject("Part").getString("Details"));

                        bowl.setPartID(xmlJSONObj.getJSONObject("FbiXml").getJSONObject("FbiMsgsRs").
                                getJSONObject("PartQueryRs").getJSONObject("Part").getInt("PartID"));

                        bowl.setLen(xmlJSONObj.getJSONObject("FbiXml").getJSONObject("FbiMsgsRs").
                                getJSONObject("PartQueryRs").getJSONObject("Part").getDouble("Len"));
//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                        try{


                            // System.out.println(xmlJSONObj.getJSONObject("FbiXml").getJSONObject("FbiMsgsRs").getJSONObject("PartQueryRs")
                            //       .getJSONArray("tag").getJSONObject(0).getInt( "Quantity"));

                            JSONArray array= xmlJSONObj.getJSONObject("FbiXml").getJSONObject("FbiMsgsRs")
                                    .getJSONObject("PartQueryRs").getJSONArray("Tag");
                            System.out.println("Tag is an array with length : "+array.length());
                            for (int j =0; j< array.length(); j++){

                                JSONObject tagTemp = array.getJSONObject(j);
                                System.out.println("TAG quantity : "+tagTemp.getInt("Quantity"));
                            }
                        }catch (Exception e){
                            System.out.print("Tag is not an array");
                        }

                        // System.out.println(xmlJSONObj.getJSONObject());


//!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                        // get Description String

                        String result = getDescription(response).replaceAll(",","");
                        String DIpattern = "(?<=DI:)((\\s\\w.*?\\s)|(\\w.*?\\s)|(\\s\\w.*))";
                        Pattern DIr = Pattern.compile(DIpattern);
                        Matcher DIm = DIr.matcher(result);
                        String SNpattern = "(?<=SN:)((\\s\\w.*?\\s)|(\\w.*?\\s)|(\\s\\w.*))";
                        Pattern SNr = Pattern.compile(SNpattern);
                        Matcher SNm = SNr.matcher(result);


                        List<Object> data=new ArrayList<Object>();

                        ArrayList rowFill = new ArrayList();


                        if(DIm.find()==true&&SNm.find()==true){
                            String name = result.replace("DI:","");
                            name = name.replace("SN:","");
                            name = name.replace(DImatchGroup,"");
                            name = name .replace(SNmatchGroup,"");
                            rowFill.add(name);
                            rowFill.add(SNmatchGroup);
                            rowFill.add(DImatchGroup);
                            rowFill.add(partid.toString());
                            // rowFill.add(bowl.getABCCode());
                            rowFill.add(bowl.getStandardCost()+"$");
                            // rowFill.add(bowl.getLen());
                           //  rowFill.add(bowl.getHeight());
                           // rowFill.add(bowl.getWidth());
                            // rowFill.add(bowl.getPartID());
                            rowFill.add(bowl.getDetails());


                            ew.write_row(rowFill,i);

                        }else if(SNm.find()==true && DIm.find()!=true){
                            String name = result.replace("SN:","");
                            name = name .replace(SNmatchGroup,"");

                            rowFill.add(name);
                            rowFill.add(SNmatchGroup);
                            rowFill.add("");
                            rowFill.add(partid.toString());
                            // rowFill.add(bowl.getABCCode());
                            rowFill.add(bowl.getStandardCost()+"$");
                            // rowFill.add(bowl.getLen());
                           //  rowFill.add(bowl.getHeight());
                           // rowFill.add(bowl.getWidth());
                            // rowFill.add(bowl.getPartID());
                            rowFill.add(bowl.getDetails());


                            ew.write_row(rowFill,i);
                        }else if(SNm.find()!= true && DIm.find()== true){
                            String name = result.replace("DI:","");

                            name = name.replace(DImatchGroup,"");

                            rowFill.add(name);
                            rowFill.add("");
                            rowFill.add(DImatchGroup);
                            rowFill.add(partid.toString());
                            // rowFill.add(bowl.getABCCode());
                            rowFill.add(bowl.getStandardCost()+"$");
                            // rowFill.add(bowl.getLen());
                           //  rowFill.add(bowl.getHeight());
                           // rowFill.add(bowl.getWidth());
                            // rowFill.add(bowl.getPartID());
                            rowFill.add(bowl.getDetails());



                        }
                        else {
                            rowFill.add(result);
                            rowFill.add("");
                            rowFill.add("");
                            rowFill.add(partid.toString());
                            // rowFill.add(bowl.getABCCode());
                            rowFill.add(bowl.getStandardCost()+"$");
                            // rowFill.add(bowl.getLen());
                           //  rowFill.add(bowl.getHeight());
                           // rowFill.add(bowl.getWidth());
                            // rowFill.add(bowl.getPartID());
                            rowFill.add(bowl.getDetails());

                            ew.write_row(rowFill,i);
                        }

                    }



                }
                //end of for
            } catch (Exception e){
                App.infoBox(String.format("ERROR: Please check row number %s then retry",count),"Error");
                e.printStackTrace();
                System.exit(1);

            }

            String outpath=System.getProperty("user.home");
            String output_path =String.format("%s\\out_%s",outpath,fileName);
            System.out.println(output_path);

            ew.create_excel(output_path);//Write to excel



        }catch (IOException e){
            e.printStackTrace();
        }
    }**/



