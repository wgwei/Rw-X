package com.company;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws Exception {
        // write your code here
        double V = 60;
        double S = 3;
        double T = 0.5;
        int n = 1;
        double sourceSpec[] = {50, 55, 65, 60, 50};
        double IANLwin = 25;
        double IANLvent = 33;

        double x[] = {-21, -14, -8, -5, -4};
        double[] c;
        FindRwX test1 = new FindRwX(V, S, T, n, sourceSpec, IANLwin, IANLvent);
        c = test1.source_minus_x(sourceSpec, x);

        test1.find_suitable_glass();
        double [] RwPlusC = test1.get_RwPlusC_samples();
        double [] RwPlusCtr = test1.get_RwPlusCtr_samples();
        double [] DnewPlusC = test1.get_DnewPlusC_samples();
        double [] DnewPlusCtr = test1.get_DnewPlusCtr_samples();
        System.out.println("\nprint the statistical values");
        for (double cc : RwPlusC) System.out.println(cc);
        for (double cctr : RwPlusCtr) System.out.println(cctr);
        for (double dd : DnewPlusC) System.out.println(dd);
        for (double ddtr : DnewPlusC) System.out.println(ddtr);
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

    public static double logsum(double[] spec){
        double eng = 0;
        for (double s: spec) eng += Math.pow(10, s / 10);
        return 10*Math.log10(eng);
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
        private int NumOfSpec = 2000;
        double [][] L2spcsVariation = gen_random_spec_variations(NumOfSpec);
        double  V, S, T, IANLwin, IANLvent;
        int n;
        private double[] sourceSpec;

        public FindRwX(double V, double S, double T, int n, double [] sourceSpec, double IANLwin, double IANLvent){
            this.V = V;
            this.S = S;
            this.T = T;
            this.n = n;
            this.sourceSpec = sourceSpec;
            this.IANLwin = IANLwin;
            this.IANLvent = IANLvent;
        }

        protected int random_int_between(double a, double b){
            return (int) Math.floor(Math.random() * (b-a) + a);
        }

        public double room_conditioni(){
            return 10* Math.log10(T) + 10* Math.log10(S/V) + 11;
        }

        public double room_condition2(){
             return 10* Math.log10(T) + 10* Math.log10(n/V) + 21;
        }

        public double[] source_minus_x(double [] sourceSpec, double[] x){
            double [] sourceMinusX  = new double[5];
            for (int i=0; i<5; i++) {
                sourceMinusX[i] = 5;
                sourceMinusX[i] = sourceSpec[i] - x[i];
            }
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
        public double [] get_RwPlusC_samples() {
            double[][] L2specsWin = gen_internal_spec(L2spcsVariation, IANLwin);
            double[] sourceMinusC = source_minus_x(sourceSpec,cNormalised);
            double roomConditioni = room_conditioni();
            double[] RwC = r_x(sourceMinusC, roomConditioni, L2specsWin);

            // return {Cmax, C5, C10, C25, C75, C50perRange};
            double[] RwCwinS = get_statistical_value(RwC);
            return  RwCwinS;
        }

        public double [] get_RwPlusCtr_samples() {
            double[][] L2specsWin = gen_internal_spec(L2spcsVariation, IANLwin);
            double [] sourceMinusCtr = source_minus_x(sourceSpec, ctrNormalised);
            double roomConditioni = room_conditioni();
            double[] RwCtr = r_x(sourceMinusCtr, roomConditioni, L2specsWin);

            // return {Cmax, C5, C10, C25, C75, C50perRange};
            double[] RwCtrWinS = get_statistical_value(RwCtr);
            return RwCtrWinS;
        }

        public double [] get_DnewPlusC_samples() {
            double[][] L2specsVent = gen_internal_spec(L2spcsVariation, IANLvent);
            double [] sourceMinusC = source_minus_x(sourceSpec,cNormalised);
            double roomCondition2 = room_condition2();
            double[] DnewCvent = r_x(sourceMinusC, roomCondition2, L2specsVent);

            // return {Cmax, C5, C10, C25, C75, C50perRange};
            double[] DnewCventS = get_statistical_value(DnewCvent);
            return DnewCventS;
        }

        public double [] get_DnewPlusCtr_samples() {
            double[][] L2specsVent = gen_internal_spec(L2spcsVariation, IANLvent);
            double [] sourceMinusCtr = source_minus_x(sourceSpec,ctrNormalised);
            double roomCondition2 = room_condition2();
            double[] DnewCtrVent = r_x(sourceMinusCtr, roomCondition2, L2specsVent);

            // return {Cmax, C5, C10, C25, C75, C50perRange};
            double[] DnewCtrVentS = get_statistical_value(DnewCtrVent);
            return DnewCtrVentS;
        }


        //public List <String> find_suitable_glass() throws Exception {
        public double[][] find_suitable_glass() throws Exception {
            double [][] levels = new double [500][6];
            double roomConditioni = room_conditioni();
            readFilebyScanner glass = new readFilebyScanner();
            String glassDescription [] = glass.readDescription(glass.glazingFile);
            double Ris[][] = glass.readRis(glass.glazingFile);
            int dataLen = glassDescription.length;
            for (int i=0; i<dataLen; i++){
                double[] fe = new double[5]; // 5 octave between 125 Hz and 2k Hz
                for (int j=3; j<8; j++){
                    fe[j-3] = sourceSpec[j-3] - Ris[i][j] + roomConditioni;
                }
                double IANL = logsum(fe);
                if(IANL<IANLwin){
                    System.out.println(glassDescription[i]);
                }
            }
            return levels;
        }
    }
}

