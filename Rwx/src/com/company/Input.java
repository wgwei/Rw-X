package com.company;

import javax.swing.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.util.Arrays;
import java.util.Scanner;

public class Input {
    private JTabbedPane RoomInfo;
    private JPanel Input;
    private JTextField volumeM3TextField;
    private JTextField winAreaM2TextField;
    private JLabel Volume;
    private JLabel WinArea;
    private JTextField RTSTextField;
    private JPanel RT;
    private JLabel NoOfVent;
    private JTextField noOfVentsTextField;
    private JTabbedPane tabbedPane1;
    private JLabel dBviaWin;
    private JLabel dBviaVent;
    private JTextField noiseViaWindowDBTextField;
    private JTextField noiseViaVentDBTextField;
    private JTabbedPane tabbedPane2;
    private JLabel Hz125;
    private JLabel Hz250;
    private JLabel Hz500;
    private JLabel Hz1k;
    private JLabel Hz2k;
    private JTextField a125TextField;
    private JTextField a250TextField;
    private JTextField a500TextField;
    private JTextField a1000TextField;
    private JTextField a2000TextField;
    private JPanel SourceSpectrum;
    private JTabbedPane tabbedPane3;
    private JButton showResultsButton;
    private JButton exportButton;
    private JTabbedPane tabbedPane4;
    private JPanel ResultsPane;
    private JTextPane ShowGlaingResluts;
    private JTabbedPane tabbedPane5;
    private JTextPane ShowVentResults;

    public Input() {
        ShowGlaingResluts.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
            }

        });
        ShowGlaingResluts.setText("hello \n test ");
        ShowGlaingResluts.setText("\nwww");
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

    public static void main(String[] args) {
        JFrame frame = new JFrame("Input");
        frame.setContentPane(new Input().Input);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
