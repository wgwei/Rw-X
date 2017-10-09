package com.company;

//import sun.jvm.hotspot.debugger.cdbg.Sym;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static double sum(double numbers[]){
        double result = 0.0;
        for (double num : numbers)
            result += num;
        return result;
    }

    public static double roundOffat2(double v){
        return Math.round(v*100.0)/100.0;
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
        private String glazingCVS = "res/glazing_info.txt";
        private String ventCVS = "res/vent_info.txt";
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

        public double [][] readPerformance(String fileName) throws Exception {
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

    public static class GeneralInputs{
        double  V, S, T, IANLwin, IANLvent, roomConditioni, roomCondition2;
        int n;
        private double[] sourceSpec;
        public GeneralInputs(double V, double S, double T, int n, double [] sourceSpec, double IANLwin, double IANLvent) {
            this.V = V;
            this.S = S;
            this.T = T;
            this.n = n;
            this.sourceSpec = sourceSpec;
            this.IANLwin = IANLwin;
            this.IANLvent = IANLvent;
            this.roomConditioni = 10* Math.log10(T) + 10* Math.log10(S/V) + 11;
            this.roomCondition2 = 10* Math.log10(T) + 10* Math.log10(n/V) + 21;
        }
    }
    public static class FindRwX extends GeneralInputs {
        double L2refspec [] = {12,12,12,12,12};
        double specVariation [] ={6, 5, 6, 11, 11};
        double cNormalised [] = {-21,-14,-8,-5,-4};
        double ctrNormalised [] = {-14,-10,-7,-4,-6};
        private int NumOfSpec = 2000;
        double [][] L2spcsVariation = gen_random_spec_variations(NumOfSpec);
        double  V, S, T, IANLwin, IANLvent, roomConditioni, roomCondition2;
        int n;
        private double[] sourceSpec;

        public FindRwX(double V, double S, double T, int n, double [] sourceSpec, double IANLwin, double IANLvent){
            super(V, S, T, n, sourceSpec, IANLwin, IANLvent);
            this.V = V;
            this.S = S;
            this.T = T;
            this.n = n;
            this.sourceSpec = sourceSpec;
            this.IANLwin = IANLwin;
            this.IANLvent = IANLvent;
            this.roomConditioni = super.roomConditioni;
            this.roomCondition2 = super.roomConditioni;
        }

        protected int random_int_between(double a, double b){
            return (int) Math.floor(Math.random() * (b-a) + a);
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
            double[] RwC = r_x(sourceMinusC, roomConditioni, L2specsWin);

            // return {Cmax, C5, C10, C25, C75, C50perRange};
            double[] RwCwinS = get_statistical_value(RwC);
            return  RwCwinS;
        }

        public double [] get_RwPlusCtr_samples() {
            double[][] L2specsWin = gen_internal_spec(L2spcsVariation, IANLwin);
            double [] sourceMinusCtr = source_minus_x(sourceSpec, ctrNormalised);
            double[] RwCtr = r_x(sourceMinusCtr, roomConditioni, L2specsWin);

            // return {Cmax, C5, C10, C25, C75, C50perRange};
            double[] RwCtrWinS = get_statistical_value(RwCtr);
            return RwCtrWinS;
        }

        public double [] get_DnewPlusC_samples() {
            double[][] L2specsVent = gen_internal_spec(L2spcsVariation, IANLvent);
            double [] sourceMinusC = source_minus_x(sourceSpec,cNormalised);
            double[] DnewCvent = r_x(sourceMinusC, roomCondition2, L2specsVent);

            // return {Cmax, C5, C10, C25, C75, C50perRange};
            double[] DnewCventS = get_statistical_value(DnewCvent);
            return DnewCventS;
        }

        public double [] get_DnewPlusCtr_samples() {
            double[][] L2specsVent = gen_internal_spec(L2spcsVariation, IANLvent);
            double [] sourceMinusCtr = source_minus_x(sourceSpec,ctrNormalised);
            double[] DnewCtrVent = r_x(sourceMinusCtr, roomCondition2, L2specsVent);

            // return {Cmax, C5, C10, C25, C75, C50perRange};
            double[] DnewCtrVentS = get_statistical_value(DnewCtrVent);
            return DnewCtrVentS;
        }
    }

    public static class ShowOutputs extends GeneralInputs{
        double [][] levelViaWin = new double [200][7]; // [0]: id; [1]: total; [2-5] octave
        double [][] levelViaVent = new double [200][7];

        readFilebyScanner facade = new readFilebyScanner();
        String glassDescription [] = facade.readDescription(facade.glazingFile);
        double Ris[][] = facade.readPerformance(facade.glazingFile);
        String ventDescription [] = facade.readDescription(facade.ventFile);
        double Dneis [][] = facade.readPerformance(facade.ventFile);

        public ShowOutputs(double V, double S, double T, int n, double[] sourceSpec, double IANLwin, double IANLvent) throws Exception {

            super(V, S, T, n, sourceSpec, IANLwin, IANLvent);

            for(int i=0; i<200; i++){
                for (int j=0; j<7; j++){
                    levelViaWin[i][j] = 9999;
                    levelViaVent[i][j] = 9999;
                }
            }

            levelViaWin = find_suitable_glass();
            levelViaVent = find_suitable_vent();
        }

        public void testPassing(){
            System.out.println("\n\n from here");
            System.out.println(V);
            for (double s: super.sourceSpec) System.out.println(s);
        }


        public double[][] find_suitable_glass() throws Exception {
            int dataLen = glassDescription.length;
            int cnt = 0;
            for (int i=0; i<dataLen; i++){
                double[] fe = new double[5]; // 5 octave between 125 Hz and 2k Hz
                for (int j=4; j<9; j++){
                    fe[j-4] = super.sourceSpec[j-4] - Ris[i][j-1] + super.roomConditioni;
                }
                double IANL = logsum(fe);
                if(IANL<IANLwin){
                    levelViaWin[cnt][0] = i;
                    levelViaWin[cnt][1] = IANL;
                    for (int m=0; m<5; m++){
                        levelViaWin[cnt][m+2] = fe[m];
                    }
                    cnt++;
                }
            }
            return levelViaWin;
        }

        public double[][] find_suitable_vent() throws Exception {
            int dataLen = ventDescription.length;
            int cnt = 0;
            for (int i=0; i<dataLen; i++){
                double[] fe = new double[5]; // 5 octave between 125 Hz and 2k Hz
                for (int j=4; j<9; j++){
                    fe[j-4] = super.sourceSpec[j-4] - Dneis[i][j-1] + super.roomCondition2;
                }
                double IANL = logsum(fe);
                if(IANL<IANLwin){
                    levelViaVent[cnt][0] = i;
                    levelViaVent[cnt][1] = IANL;
                    for (int m=0; m<5; m++){
                        levelViaVent[cnt][m+2] = fe[m];
                    }
                    cnt++;
                }
            }
            return levelViaVent;
        }

        public int valid_ID(double [][] glassOrVent){
            int cnt = 0;
            for (int i=0; i<200; i++){
                if (glassOrVent[i][0]<9998) cnt++;
            }
            return cnt-1;
        }
    }

    public static String convert_prediction_to_String(double V, double S, double T, int n, double[] sourceSpec, double IANLwin, double IANLvent) throws Exception{
        String RwPlusCString,RwPlusCtrString,  DnewPlusCString, DnewPlusCtrString;

        FindRwX test1 = new FindRwX(V, S, T, n, sourceSpec, IANLwin, IANLvent);

        //{Cmax, C5, C10, C25, C75, C50perRange};
        double [] RwPlusC = test1.get_RwPlusC_samples();
        double [] RwPlusCtr = test1.get_RwPlusCtr_samples();
        double [] DnewPlusC = test1.get_DnewPlusC_samples();
        double [] DnewPlusCtr = test1.get_DnewPlusCtr_samples();
        String headinfo = "Para\t\t\tMax\t\t\t5%\t\t10%\t\t25%\t\t75%\t\t50% range\n";

        RwPlusCString="Glazing Rw+C\t";
        for (int c=0; c<5; c++){
            RwPlusCString += Double.toString((int) RwPlusC[c])+"\t\t";
        }
        RwPlusCString += Double.toString(roundOffat2(RwPlusC[5])) + "\n";

        RwPlusCtrString="Glazing Rw+Ctr\t";
        for (int c=0; c<5; c++){
            RwPlusCtrString += Double.toString((int) RwPlusCtr[c])+"\t\t";
        }
        RwPlusCtrString += Double.toString(roundOffat2(RwPlusCtr[5])) + "\n";

        DnewPlusCString="Vent Dnew+C\t\t";
        for (int c=0; c<5; c++){
            DnewPlusCString += Double.toString((int) DnewPlusC[c])+"\t\t";
        }
        DnewPlusCString += Double.toString(roundOffat2(DnewPlusC[5])) + "\n";

        DnewPlusCtrString="Vent Dnew+Ctr\t";
        for (int c=0; c<5; c++){
            DnewPlusCtrString += Double.toString((int) DnewPlusCtr[c])+"\t\t";
        }
        DnewPlusCtrString += Double.toString(roundOffat2(DnewPlusCtr[5])) + "\n";


        String rv = headinfo + RwPlusCString + RwPlusCtrString + DnewPlusCString + DnewPlusCtrString;
        return rv;
    }

    public static String convert_checking_to_String(double V, double S, double T, int n, double[] sourceSpec, double IANLwin, double IANLvent) throws Exception{
        String suitableGlass,suitableVents;

        ShowOutputs so = new ShowOutputs(V, S, T, n, sourceSpec, IANLwin, IANLvent);
        double [][] glass = so.find_suitable_glass();
        double [][] vent = so.find_suitable_vent();
        int cntGlass = so.valid_ID(glass);
        int cntVent = so.valid_ID(vent);

        suitableGlass = "\n\nGlass\t\t\t\t\t\t\t\tIANL\t125Hz\t250Hz\t500Hz\t1kHz\t2kHz\n";
        for (int p=0; p<cntGlass; p++){
            suitableGlass += so.glassDescription[(int) glass[p][0]] + "\t\t";
            for (int q=1; q<7; q++){
                suitableGlass += Double.toString(Math.round(glass[p][q])) + "\t";
            }
            suitableGlass += "\n";
        }

        suitableVents = "\n\nVent\t\t\t\t\t\t\t\tIANL\t125Hz\t250Hz\t500Hz\t1kHz\t2kHz\n";
        for (int p=0; p<cntVent; p++){
            suitableVents += so.ventDescription[(int) vent[p][0]] + "\t\t";
            for (int q=1; q<7; q++){
                suitableVents += Double.toString(Math.round(vent[p][q])) + "\t";
            }
            suitableVents += "\n";
        }


        String rv = suitableGlass + suitableVents;
        return rv;
    }

    public static void main(String[] args) throws Exception {
        // write your code here
        double V = 60;
        double S = 3;
        double T = 0.5;
        int n = 1;
        double sourceSpec[] = {50, 55, 65, 60, 50};
        double IANLwin = 50;
        double IANLvent = 50;

        String outputs = convert_prediction_to_String(V, S, T, n, sourceSpec, IANLwin, IANLvent);
        System.out.println(outputs);
//        String checking = convert_checking_to_String(V, S, T, n, sourceSpec, IANLwin, IANLvent);
//        System.out.println(checking);

//        ShowOutputs so = new ShowOutputs(V, S, T, n, sourceSpec, IANLwin, IANLvent);
//        so.testPassing();
//        double [][] glass = so.find_suitable_glass();
//        double [][] vent = so.find_suitable_vent();
//        int cntGlass = so.valid_ID(glass);
//        int cntVent = so.valid_ID(vent);
//        for (int p=0; p<cntGlass; p++){
//            System.out.println(so.glassDescription[(int) glass[p][0]]);
//            for (double v: glass[p]) System.out.println(v);
//        }
    }
}