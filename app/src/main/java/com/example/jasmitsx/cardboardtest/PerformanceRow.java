package com.example.jasmitsx.cardboardtest;

/**
 * Created by jasmitsx on 7/19/2016.
 */
public class PerformanceRow {
    private int rowNumber;
    private double rowFPS;
    private double rowCPU;

    public PerformanceRow(){}

    public PerformanceRow(int rowNumber, double rowFPS, double rowCPU){
        this.rowNumber = rowNumber;
        this.rowFPS = rowFPS;
        this.rowCPU = rowCPU;
    }


    public int getRowNumber(){
        return rowNumber;
    }

    public double getRowFPS(){ return rowFPS; }

    public double getRowCPU(){ return rowCPU; }

    public void setRowNumber(int rowNumber){ this.rowNumber = rowNumber; }

    public void setRowFPS(double rowFPS){ this.rowFPS=rowFPS; }

    public void setRowCPU(double rowCPU){ this.rowCPU=rowCPU; }
}
