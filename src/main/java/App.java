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

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
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

            List<Object> exportData = new ArrayList<Object>();
            exportData.add("Part Title");
            exportData.add("MTC Number");
            exportData.add("DI SN");
            exportData.add("Data");

            List<List<Object>> datalist = new ArrayList<List<Object>>();
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
            // get Description String
            String result = getDescription(response).replaceAll(",","");
            String DIpattern = "(?<=DI:)((\\s\\w.*?\\s)|(\\w.*?\\s)|(\\s\\w.*))";
            Pattern DIr = Pattern.compile(DIpattern);
            Matcher DIm = DIr.matcher(result);
            String SNpattern = "(?<=SN:)((\\s\\w.*?\\s)|(\\w.*?\\s)|(\\s\\w.*))";
            Pattern SNr = Pattern.compile(SNpattern);
            Matcher SNm = SNr.matcher(result);


              List<Object> data=new ArrayList<Object>();



              if(DIm.find()==true){
                    String DIname =result.replace("DI:","");
                    data.add(DIname.replace(DIm.group(),""));
                    data.add(partid);
                    System.out.println(DIm.group());
                    data.add("DI:");
                    data.add(DIm.group());
                }else if(SNm.find()==true){
                  String SNname =result.replace("SN:","");
                  data.add(SNname.replace(SNm.group(),""));
                    System.out.println(SNm.group());
                    data.add(partid);
                    data.add("SN:");
                    data.add(SNm.group());
              }else{
                  data.add(result);
                  data.add(partid);
              }
              datalist.add(data);

                    }



                }
              //end of for
      } catch (Exception e){
          App.infoBox(String.format("ERROR: Please check row number %s then retry",count),"Error");
          e.printStackTrace();
          System.exit(1);

      }

            String path = System.getProperty("user.home");
            File file = UtilsCSV.createCSVFile(exportData, datalist, path, fileName);
            String fileName2 = file.getName();
            System.out.println("File Name is : " + fileName2);



        }catch (IOException e){
            e.printStackTrace();
        }
    }
}

