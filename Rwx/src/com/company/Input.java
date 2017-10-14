package com.company;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static com.company.Main.convert_checking_to_String;
import static com.company.Main.convert_prediction_to_String;

public class Input {
    private JPanel Input;
    private JTextField volumeM3TextField;
    private JTextField winAreaM2TextField;
    private JLabel Volume;
    private JLabel WinArea;
    private JTextField RTSTextField;
    private JPanel RT;
    private JLabel NoOfVent;
    private JTextField noOfVentsTextField;
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
    private JButton showResultsButton;
    private JButton exportButton;
    private JTextPane ShowGlaingResluts;
    private JTabbedPane tabbedPane5;
    private JTabbedPane tabbedPane1;
    private JTabbedPane tabbedPane3;
    private JScrollPane outputPane;
    private JTextArea Results_pane;
    private JTextArea displayArea;


    public Input() {
        showResultsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                    double Vdb, Sdb, RTdb, ndb, src125Str, src250Str, src500Str, src1kStr,src2kStr, Lwin, Lvent;
                    Vdb = Double.parseDouble(volumeM3TextField.getText());
                    Sdb = Double.parseDouble(winAreaM2TextField.getText());
                    RTdb = Double.parseDouble(RTSTextField.getText());
                    ndb = Double.parseDouble(noOfVentsTextField.getText());
                    src125Str = Double.parseDouble(a125TextField.getText());
                    src250Str = Double.parseDouble(a250TextField.getText());
                    src500Str = Double.parseDouble(a500TextField.getText());
                    src1kStr = Double.parseDouble(a1000TextField.getText());
                    src2kStr = Double.parseDouble(a2000TextField.getText());
                    Lwin = Double.parseDouble(noiseViaWindowDBTextField.getText());
                    Lvent = Double.parseDouble(noiseViaVentDBTextField.getText());
                    double [] sourceSpec = {src125Str, src250Str, src500Str, src1kStr, src2kStr};

                    String outputs = convert_prediction_to_String(Vdb, Sdb, RTdb, ndb, sourceSpec, Lwin, Lvent);
                    String checking = convert_checking_to_String(Vdb, Sdb, RTdb, ndb, sourceSpec, Lwin, Lvent);
                    String total = outputs + checking;
                    displayArea.setText(total);
                    displayArea.setText(total);
                }
                catch (Exception ex){
                    displayArea.setText("Not valid inputs!");
                }
            }
        });
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Input");
        frame.setContentPane(new Input().Input);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
}
