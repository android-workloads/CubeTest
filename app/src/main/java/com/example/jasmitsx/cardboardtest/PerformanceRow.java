package com.example.jasmitsx.cardboardtest;

/**
 * Created by jasmitsx on 7/19/2016.
 */
public class PerformanceRow {
    private int id;
    private int rowNumber;
    private double rowFPS;
    private double rowCPU;
    private int rowJanks;
    private double rowAPS;

    public PerformanceRow(){}

    public PerformanceRow(int rowNumber, double rowFPS, double rowCPU, int rowJanks, double rowAPS){
        this.rowNumber = rowNumber;
        this.rowFPS = rowFPS;
        this.rowCPU = rowCPU;
        this.rowJanks = rowJanks;
        this.rowAPS = rowAPS;
    }

    public PerformanceRow(int id, int rowNumber, double rowFPS, double rowCPU, int rowJanks, double rowAPS){
        this.id = id;
        this.rowNumber = rowNumber;
        this.rowFPS = rowFPS;
        this.rowCPU = rowCPU;
        this.rowJanks = rowJanks;
        this.rowAPS = rowAPS;
    }

    //get methods

    public int getId(){ return id; }

    public int getRowNumber(){
        return rowNumber;
    }

    public double getRowFPS(){ return rowFPS; }

    public double getRowCPU(){ return rowCPU; }

    public int getRowJanks(){ return rowJanks; }

    public double getRowAPS(){ return rowAPS; }

    //set methods
    public void setId(int id){ this.id = id; }

    public void setRowNumber(int rowNumber){ this.rowNumber = rowNumber; }

    public void setRowFPS(double rowFPS){ this.rowFPS=rowFPS; }

    public void setRowCPU(double rowCPU){ this.rowCPU=rowCPU; }

    public void setRowJanks(int rowJanks){ this.rowJanks=rowJanks; }

    public void setRowAPS(double rowAPS){ this.rowAPS=rowAPS; }
}
