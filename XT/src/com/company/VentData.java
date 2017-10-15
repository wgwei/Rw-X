package com.company;

public class VentData {
    protected String [] ventDesc;
    protected  double [][] ventPerf;

    public VentData() {
        ventDesc = ventDescription();
        ventPerf = ventPerformance();
    }
    public String [] ventDescription(){
        String [] vd = {"Titon-V75+C75",
                "Titon-V75+C50",
                "Titon-V75+SF418",
                "Titon-V25+C25",
                "Titon-V50+SF418",
                "Titon-V50+C25",
                "Greenwood-6000S",
                "Greenwood-4000S",
                "Greenwood-4000L",
                "Greenwood-3000S",
                "Greenwood-1600-DN",
                "Greenwood-2000L",
                "Greenwood-EHA574",
                "Greenwood-MA3051",
                "Simon-Framevent",
                "Simon-Frame-Vent-(FV)",
                "Simon-EMM",
                "Simon-EHA",
                "Simon-EHAS",
                "Simon-TTF-Slimline-(SL)",
                "Simon-EHAS+AEA851"
        };
        return vd;
    }

    public double [][] ventPerformance(){
        double [][] vp = new double [][] {
                {44,42,41,37,37,36.1,46.9,49.2},
                {42,41,40,40.2,37.2,34.4,42.8,50.1},
                {40,39,38,37.8,36.5,34.8,39.5,41.5},
                {36,35,34,37.8,37.6,34.4,32.1,42.4},
                { 38,37,36,34.4,36.8,34.5,35.9,40.8},
                {38,38,37,37.9,37.7,33.4,36.3,47.1},
                { 32,32,31,36.9,33.6,31.6,29.2,33.4},
                {33,32,32,38.4,36.8,33.8,29.7,33.8},
                {34,33,33,38.5,35.7,34.3,31.4,34.4},
                {35,35,34,39.9,37,34.9,32.5,36.2},
                {37,36,36,39.8,38.9,36.5,33.6,38.7},
                {37,36,36,41.4,38.5,37.3,33.9,37.5},
                { 44,43,42,33.9,39.9,39.3,42.3,49.3},
                {55,54,52,46.5,45.9,49,55.5,66.2},
                {31,30,31,35.5,32.8,33,30.6,28.6},
                {33,32,32,39.5,35.9,35.1,30.7,32.1},
                {33,33,33,40.1,36.1,35.3,31.1,32.6},
                {36,35,35,39.6,35.9,35.1,33,37.2},
                {38,38,36,40.1,36.1,33.9,35.3,43.9},
                {39,38,38,42.3,39.6,38,35.8,40.3},
                {42,42,41,40.5,41.8,37.2,43.1,45.3},
        };
        return vp;
    }

    public static void main(String [] args){
        VentData vent = new VentData();
        System.out.println(vent.ventDesc.length);
        System.out.println(vent.ventPerf.length);
    }
}
