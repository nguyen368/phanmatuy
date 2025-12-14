package com.antidrug.assessment;
import javax.swing.*; import java.awt.*;
public class LoginScreen extends JFrame {
    private JTextField u; private JPasswordField p;
    public LoginScreen() {
        setTitle("Đăng nhập"); setSize(400,200); setDefaultCloseOperation(3); setLocationRelativeTo(null); setLayout(new GridLayout(4,2));
        add(new JLabel(" User:")); u=new JTextField(); add(u);
        add(new JLabel(" Pass:")); p=new JPasswordField(); add(p);
        JButton b=new JButton("Login"); add(new JLabel(" (Nguyen/123)")); add(b);
        b.addActionListener(e->{ User user=DatabaseStore.getInstance().login(u.getText(),new String(p.getPassword()));
            if(user!=null){new DashboardScreen(user);dispose();}else JOptionPane.showMessageDialog(null,"Sai!"); });
        setVisible(true);
    }
}