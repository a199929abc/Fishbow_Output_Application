package pojo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class Bowl {
    public String getNum() {
        return Num;
    }

    public void setNum(String num) {
        Num = num;
    }

    public double getStandardCost() {
        return StandardCost;
    }

    public void setStandardCost(double standardCost) {
        StandardCost = standardCost;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public JSONArray getTag() {
        return Tag;
    }

    public void setTag(JSONArray tag) {
        Tag = tag;
    }

    public String getQuantity() {
        return Quantity;
    }

    public void setQuantity(String quantity) {
        Quantity = quantity;
    }

    public String getABCCode() {
        return ABCCode;
    }

    public void setABCCode(String ABCCode) {
        this.ABCCode = ABCCode;
    }

    public String getDetails() {
        return Details;
    }

    public void setDetails(String details) {
        Details = details;
    }

    public int getHeight() {
        return Height;
    }

    public void setHeight(int height) {
        Height = height;
    }

    public double getWidth() {
        return Width;
    }

    public void setWidth(double width) {
        Width = width;
    }

    public int getPartID() {
        return PartID;
    }

    public void setPartID(int partID) {
        PartID = partID;
    }

    public double getLen() {
        return Len;
    }

    public void setLen(double len) {
        Len = len;
    }

    public String getSerialnumber() {
        return serialnumber;
    }

    public void setSerialnumber(String serialnumber) {
        this.serialnumber = serialnumber;
    }

    public int getArrayFlag() {
        return arrayFlag;
    }

    public void setArrayFlag(int arrayFlag) {
        this.arrayFlag = arrayFlag;
    }

    public JSONObject getSingleTag() {
        return SingleTag;
    }

    public void setSingleTag(JSONObject singleTag) {
        SingleTag = singleTag;
    }

    private JSONObject SingleTag;
    private int arrayFlag;
    private String Num;
    private double StandardCost;
    private String Description;
    private String Quantity;
    private String ABCCode;
    private String Details;
    private int Height;
    private double Width;
    private int PartID;
    private double Len;
    private String serialnumber;
    private JSONArray Tag;





}
