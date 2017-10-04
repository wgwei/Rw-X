package com.company;

import java.io.*;
import java.util.Arrays;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws Exception {
        // write your code here
        FindRwX test1 = new FindRwX();
        double sourceSpec[] = {10, 20, 30, 40, 50};
        double x[] = {-21, -14, -8, -5, -4};
        double[] c;
        c = test1.source_minus_x(sourceSpec, x);
        for (int j = 0; j < 5; j++) System.out.println(c[j]);

        double a[][] = new double[10][3];
        System.out.println(a.length);
        System.out.println(a[0].length);

        String fileName = "res/glazing_info.txt";
        readFilebyScanner rf = new readFilebyScanner();
        String descriptions [] = rf.readDescription(rf.glazingFile);
        for (String s:descriptions) System.out.println(s);
        double values [][] = rf.readRis(rf.glazingFile);
        for (int i=0; i<values.length;i++){
            for (int j=0; j<values[0].length; j++){
                System.out.println(values[i][j]);
            }
        }
        String descriptions2 [] = rf.readDescription(rf.ventFile);
        for (String s:descriptions2) System.out.println(s);
    }

    public static double sum(double numbers[]){
        double result = 0.0;
        for (double num : numbers)
            result += num;
        return result;
    }

    public static double getMax(double [] decMax) {
        double max = decMax[0];
        for (double elem : decMax)
            max = Math.max(elem, max);
        return max;
    }
    public static int getNumOfLines(String fileName) throws IOException{
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        int lines = 0;
        while (reader.readLine() != null) lines++;
        reader.close();
        return lines;
    }

    public static class readFilebyScanner{
        private String glazingFile = "res/glazing_info.txt";
        private String ventFile = "res/vent_info.txt";
        private int numOfLines;

        public String [] readDescription(String fileName) throws Exception {
            // reading a text file line by line using Scanner
            String[] description;
            Scanner sc = new Scanner(new File(fileName));
            numOfLines = getNumOfLines(fileName);

            description = new String[numOfLines - 1];
            for (int cnt=0; cnt<numOfLines; cnt++){
                String str = sc.nextLine();
                if (cnt>0){
                    String [] values = str.split("\\s");
                    description[cnt-1] = values[0];
                }
            }
            sc.close();
            return description;
        }

        public double [][] readRis(String fileName) throws Exception {
            // reading a text file line by line using Scanner
            Scanner sc = new Scanner(new File(fileName));
            numOfLines = getNumOfLines(fileName);

            double [][] numbers = new double [numOfLines-1][8]; // Rw,Rw+C, Rw+Ctr, 125, 250, 500, 1k, 2k

            for (int cnt=0; cnt<numOfLines; cnt++){
                String str = sc.nextLine();
                if (cnt>0){
                    String [] values = str.split("\\s");
                    for (int i=1; i<values.length; i++){
                        numbers[cnt - 1][i - 1] = Double.parseDouble(values[i]);
                    }
                }
            }
            sc.close();
            return numbers;
        }
    }

    public static class FindRwX {
        double L2refspec [] = {12,12,12,12,12};
        double specVariation [] ={6, 5, 6, 11, 11};
        double cNormalised [] = {-21,-14,-8,-5,-4};
        double ctrNormalised [] = {-14,-10,-7,-4,-6};
        private double[] sourceSpec;
        private int NumOfSpec = 2000;
        double [][] L2spcsVariation = gen_random_spec_variations(NumOfSpec);

        protected int random_int_between(double a, double b){
            return (int) Math.floor(Math.random() * (b-a) + a);
        }

        public double room_conditioni(double V, double S, double T){
            return 10* Math.log10(T) + 10* Math.log10(S/V) + 11;
        }

        public double room_condition2(double V, double S, double T, int n){
             return 10* Math.log10(T) + 10* Math.log10(n/V) + 21;
        }

        protected double[] source_minus_x(double[] sourceSpec, double[] x){
            double sourceMinusX []  = new double[x.length];
            for (int i=0; i<x.length; i++) sourceMinusX[i] = sourceSpec[i] - x[i];
            return sourceMinusX;
        }

        public double [][] gen_random_spec_variations(int NumOfSpec){
            double L2specsVariation [][] = new double[NumOfSpec][L2refspec.length];
            for (int i=0; i<NumOfSpec; i++){
                for (int f=0; f<L2refspec.length; f++)
                    L2specsVariation[i][f] = random_int_between(L2refspec[f] - (specVariation[f] / 2), L2refspec[f] + (specVariation[f] / 2));
            }
            return L2specsVariation;
        }

        public double[][] gen_internal_spec(double [][] L2spcsVariation, double IANL){
            double L2specs[][] = new double [L2spcsVariation.length][L2spcsVariation[0].length];
            for (int i=0; i<L2spcsVariation.length; i++){
                double totalEngergy = 0;
                for (int j=0; j<L2spcsVariation[i].length; j++){
                    totalEngergy += Math.pow(10,(L2spcsVariation[i][j]/10));
                }
                double totalLevel = 10* Math.log10(totalEngergy);
                for (int m=0; m<L2spcsVariation[0].length; m++) {
                    L2specs[i][m] = L2spcsVariation[i][m] - totalLevel + IANL;
                }
            }
            return L2specs;
        }

        public double [] r_x(double[] sourceMinusX, double roomCondition, double[][] L2specs){
            double RwX[] = new double [L2specs.length];
            for (int i=0; i<L2specs.length; i++){
                double L2EnergyX = 0;
                for (int j=0; j<L2specs[0].length; j++){
                    L2EnergyX += Math.pow(10, (L2specs[i][j] - sourceMinusX[j])/10);
                }
                RwX[i] = roomCondition - 10*Math.log10(L2EnergyX);
            }
            return RwX;
        }

        public double [] get_statistical_value(double [] RwX){
            Arrays.sort(RwX);
            // get statistical values
            double Cmax = getMax(RwX);
            double C5 = RwX[(int) Math.round(0.95*RwX.length)];
            double C10 = RwX[(int) Math.round(0.9*RwX.length)];
            double C25 = RwX[(int) Math.round(0.75*RwX.length)];
            double C75 = RwX[(int) Math.round(0.25*RwX.length)];
            double C50perRange = C25 - C75;
            return new double[]{Cmax, C5, C10, C25, C75, C50perRange};
        }
        public double [] get_RwPlusC_samples(double V, double S, double T, int n, double [] sourceSpec, double IANLwin, double IANLvent) {
            double[][] L2specsWin = gen_internal_spec(L2spcsVariation, IANLwin);
            double[] sourceMinusC = source_minus_x(sourceSpec, cNormalised);
            double roomConditioni = room_conditioni(V, S, T);
            double[] RwC = r_x(sourceMinusC, roomConditioni, L2specsWin);

            // return {Cmax, C5, C10, C25, C75, C50perRange};
            double[] RwCwinS = get_statistical_value(RwC);
            return  RwCwinS;
        }

        public double [] get_RwPlusCtr_samples(double V, double S, double T, int n, double [] sourceSpec, double IANLwin, double IANLvent) {
            double[][] L2specsWin = gen_internal_spec(L2spcsVariation, IANLwin);

            double[] sourceMinusCtr = source_minus_x(sourceSpec, ctrNormalised);
            double roomConditioni = room_conditioni(V, S, T);

            double[] RwCtr = r_x(sourceMinusCtr, roomConditioni, L2specsWin);

            // return {Cmax, C5, C10, C25, C75, C50perRange};
            double[] RwCtrWinS = get_statistical_value(RwCtr);
            return RwCtrWinS;
        }

        public double [] get_DnewPlusC_samples(double V, double S, double T, int n, double [] sourceSpec, double IANLwin, double IANLvent) {
            double[][] L2specsVent = gen_internal_spec(L2spcsVariation, IANLvent);

            double[] sourceMinusC = source_minus_x(sourceSpec, cNormalised);
            double roomCondition2 = room_condition2(V, S, T, n);

            double[] DnewCvent = r_x(sourceMinusC, roomCondition2, L2specsVent);

            // return {Cmax, C5, C10, C25, C75, C50perRange};
            double[] DnewCventS = get_statistical_value(DnewCvent);
            return DnewCventS;
        }

        public double [] get_DnewPlusCtr_samples(double V, double S, double T, int n, double [] sourceSpec, double IANLwin, double IANLvent) {
            double[][] L2specsVent = gen_internal_spec(L2spcsVariation, IANLvent);

            double[] sourceMinusCtr = source_minus_x(sourceSpec, ctrNormalised);
            double roomCondition2 = room_condition2(V, S, T, n);

            double[] DnewCtrVent = r_x(sourceMinusCtr, roomCondition2, L2specsVent);

            // return {Cmax, C5, C10, C25, C75, C50perRange};
            double[] DnewCtrVentS = get_statistical_value(DnewCtrVent);
            return DnewCtrVentS;
        }


        public void get_Rwx_samples(double V, double S, double T, int n, double [] sourceSpec, double IANLwin, double IANLvent) {
            double[][] L2specsWin = gen_internal_spec(L2spcsVariation, IANLwin);
            double[][] L2specsVent = gen_internal_spec(L2spcsVariation, IANLvent);

            double[] sourceMinusC = source_minus_x(sourceSpec, cNormalised);
            double[] sourceMinusCtr = source_minus_x(sourceSpec, ctrNormalised);
            double roomConditioni = room_conditioni(V, S, T);
            double roomCondition2 = room_condition2(V, S, T, n);

            double[] RwC = r_x(sourceMinusC, roomConditioni, L2specsWin);
            double[] RwCtr = r_x(sourceMinusCtr, roomConditioni, L2specsWin);

            double[] DnewCvent = r_x(sourceMinusC, roomCondition2, L2specsVent);
            double[] DnewCtrVent = r_x(sourceMinusCtr, roomCondition2, L2specsVent);

            // sort smallest to largest for glazing spec and get statistical value
            // return {Cmax, C5, C10, C25, C75, C50perRange};
            double[] RwCwinS = get_statistical_value(RwC);
            double[] RwCtrWinS = get_statistical_value(RwCtr);
            double[] DnewCventS = get_statistical_value(DnewCvent);
            double[] DnewCtrVentS = get_statistical_value(DnewCtrVent);

            // recommended glazing parameter
            if (RwCwinS[RwCwinS.length - 1] > RwCtrWinS[RwCtrWinS.length - 1]) {
                String recommendedGlaze = "Rw+Ctr";
            } else {
                String recommendedGlaze = "Rw+C";
            }
            // recommended vent spec
            if (DnewCventS[DnewCventS.length - 1] > DnewCtrVentS[DnewCtrVentS.length - 1]) {
                String recommendVent = "Dne,w+Ctr";
            } else {
                String recommendVent = "Dne,w+C";
            }

//            var titles = ["Parameter", "0%", "10%", "25%", "75%", "25% to 75%", "Recommended para"];
//            var statisticalReturn = [titles,["Glazing Rw+C", Cmax, C10, C25, C75, C50perRange, recommendC],
//                                         ["Glazing Rw+Ctr", Ctrmax, Ctr10, Ctr25, Ctr75, Ctr50perRange, recommendCtr],
//                    ["Vent Dne,w+C", CmaxVent, C10Vent, C25Vent, C75Vent, C50perRangeVent, recommendVentC],
//                    ["Vent Dne,w+Ctr", CtrmaxVent, Ctr10Vent, Ctr25Vent, Ctr75Vent, Ctr50perRangeVent, recommendVentCtr]]
//            ;

//
//            // read glass and vent data from the sheets
//            var ss = SpreadsheetApp.getActiveSpreadsheet();
//            var Loc125Hz = 5;
//            var Loc2kHz = 9;
//
//            // process glass
//            var glassSheet = ss.getSheetByName("glazing data");
//            var glassRange = glassSheet.getDataRange(); // glass data
//            var glassSpec = glassRange.getValues(); // object [][]
//            var glassL2 = [];
//            for (var i = 1; i < glassSpec.length; i++) { //loop row
//                var L2f = [];
//                for (var j = Loc125Hz; j < Loc2kHz; j++) {
//                    if (glassSpec[i][j]) {
//                        L2f.push(sourceSpec[0][j-Loc125Hz] - glassSpec[i][j] + roomConditions[0]); // sourceSpec start from location 0
//                    }
//                }
//                var energy = 0.0;
//                for (var f=0; f<L2f.length; f++){
//                    energy = energy + Math.pow(10, L2f[f]/10);
//                }
//                var L2 = 10*log10(energy);
//                if (L2<=IANLwin){
//                    glassL2.push([glassSpec[i][1], glassSpec[i][3], glassSpec[i][4], L2]);
//                }
//            }
//
//            // process vent
//            var ventSheet = ss.getSheetByName("vent data");
//            var ventRange = ventSheet.getDataRange(); // vent data
//            var ventSpec = ventRange.getValues(); //object [][]
//            var ventL2 = [];
//            for (var m = 1; m < ventSpec.length; m++) { //loop row
//                var L2fVent = [];
//                for (var n = Loc125Hz; n < Loc2kHz; n++) {
//                    if (ventSpec[m][n]) {
//                        L2fVent.push(sourceSpec[0][n-Loc125Hz] - ventSpec[m][n] + roomConditions[1]);
//                    }
//                }
//                var energy = 0.0;
//                for (var f=0; f<L2fVent.length; f++){
//                    energy = energy + Math.pow(10, L2fVent[f]/10);
//                }
//                var L2 = 10*log10(energy);
//                if (L2<=IANLvent){
//                    ventL2.push([ventSpec[m][1], ventSpec[m][3], ventSpec[m][4], L2]);
//                }
//            }
//
//            var titles2 = ["Producer", "Rw + C", "Rw + Ctr", "Internal noise level, dB(A)"];
//            statisticalReturn.push(" ");
//            statisticalReturn.push(titles2);
//            if (glassL2.length<1){
//                statisticalReturn.push("No glass data feasible");
//            }
//            else{
//                for (var p=0; p<glassL2.length; p++){
//                    statisticalReturn.push(glassL2[p]);
//                }
//            }
//
//            var titles3 = ["Producer", "Dne,w + C", "Dne,w + Ctr", "Internal noise level, dB(A)"];
//            statisticalReturn.push(" ");
//            statisticalReturn.push(titles3);
//            if (ventL2.length<1){
//                statisticalReturn.push("No vent data feasible");
//            }
//            else{
//                for (var q=0; q<ventL2.length; q++){
//                    statisticalReturn.push(ventL2[q]);
//                }
//            }
//
//            return statisticalReturn;
        }
    }
}

