package it.unimol.understandability.ui.panels.config;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Simone Scalabrino.
 */
public class Test {
    private JButton button1;
    private JPanel panel1;
    private JTextField textField1;

    Test() {
        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {

            }
        });


        button1.addActionListener(actionEvent -> System.out.println(actionEvent.getWhen()));
    }
}
