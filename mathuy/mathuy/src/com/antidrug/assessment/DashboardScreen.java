package com.antidrug.assessment;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class DashboardScreen extends JFrame {
    private User currentUser;

    public DashboardScreen(User user) {
        this.currentUser = user;
        setTitle("Hệ Thống Hỗ Trợ Cộng Đồng - User: " + user.getFullName());
        setSize(1280, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Trang Chủ", createHomePanel());
        tabbedPane.addTab("Giáo Dục", createEducationPanel());
        tabbedPane.addTab("Chuyên Gia", createCounselorPanel());
        tabbedPane.addTab("Khóa Học", createTrainingPanel());
        // Tab Khảo sát dùng giao diện Wizard mới
        tabbedPane.addTab("Khảo Sát (Wizard)", createSurveyPanel());
        tabbedPane.addTab("Đặt Lịch", createBookingPanel());
        tabbedPane.addTab("Hồ Sơ", createProfilePanel());
        tabbedPane.addTab("Báo Cáo", createReportPanel());

        add(tabbedPane);
        setVisible(true);
    }

    // --- TAB KHẢO SÁT WIZARD ---
    private JPanel surveyCardPanel;
    private CardLayout cardLayout;
    private JProgressBar progressBar;
    private IAssessment currentAssessment;
    private List<Question> currentQuestions;
    private int currentScore = 0;

    private JPanel createSurveyPanel() {
        JPanel main = new JPanel(new BorderLayout());
        JPanel header = new JPanel(new BorderLayout()); header.setBorder(new EmptyBorder(10,50,10,50));
        JLabel title = new JLabel("ĐÁNH GIÁ NGUY CƠ", SwingConstants.CENTER); title.setFont(new Font("Arial",Font.BOLD,18));
        progressBar = new JProgressBar(0,100); progressBar.setStringPainted(true); progressBar.setVisible(false);
        header.add(title, BorderLayout.NORTH); header.add(Box.createVerticalStrut(10)); header.add(progressBar, BorderLayout.SOUTH);

        cardLayout = new CardLayout();
        surveyCardPanel = new JPanel(cardLayout);
        surveyCardPanel.add(createStartCard(), "START");

        main.add(header, BorderLayout.NORTH); main.add(surveyCardPanel, BorderLayout.CENTER);
        return main;
    }

    private JPanel createStartCard() {
        JPanel p = new JPanel(); p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));
        JLabel l = new JLabel("<html><center>Hệ thống sẽ chọn câu hỏi phù hợp với tuổi của bạn.</center></html>", SwingConstants.CENTER);
        JButton b = new JButton("BẮT ĐẦU");
        b.addActionListener(e -> startSurvey());
        p.add(Box.createVerticalGlue()); p.add(l); p.add(Box.createVerticalStrut(20)); p.add(b); p.add(Box.createVerticalGlue());
        return p;
    }

    private void startSurvey() {
        currentScore=0; surveyCardPanel.removeAll(); surveyCardPanel.add(createStartCard(),"START");
        currentAssessment = AssessmentFactory.createAssessment(currentUser.getAge());
        currentQuestions = currentAssessment.getQuestions();
        for(int i=0; i<currentQuestions.size(); i++) surveyCardPanel.add(createQuestionCard(i),"Q"+i);
        surveyCardPanel.add(createResultCard(),"RESULT");
        progressBar.setVisible(true); progressBar.setMaximum(currentQuestions.size()); progressBar.setValue(0);
        cardLayout.show(surveyCardPanel, "Q0");
    }

    private JPanel createQuestionCard(int index) {
        Question q = currentQuestions.get(index);
        JPanel p = new JPanel(new BorderLayout());
        JLabel l = new JLabel("<html><center>Câu "+(index+1)+":<br>"+q.getText()+"</center></html>", SwingConstants.CENTER);
        l.setFont(new Font("Arial",Font.BOLD,20));
        JPanel bp = new JPanel(); JButton y = new JButton("CÓ"), n = new JButton("KHÔNG");
        y.setPreferredSize(new Dimension(100,50)); n.setPreferredSize(new Dimension(100,50));
        y.addActionListener(e -> nextQ(index, q.getWeight()));
        n.addActionListener(e -> nextQ(index, 0));
        bp.add(y); bp.add(n); p.add(l, BorderLayout.CENTER); p.add(bp, BorderLayout.SOUTH);
        return p;
    }

    private void nextQ(int index, int score) {
        currentScore += score; progressBar.setValue(index+1);
        if(index < currentQuestions.size()-1) cardLayout.show(surveyCardPanel, "Q"+(index+1));
        else showResult();
    }

    private void showResult() {
        SurveyResult r = currentAssessment.evaluate(currentScore);
        surveyCardPanel.add(createFinalResultPanel(r), "FINAL");
        cardLayout.show(surveyCardPanel, "FINAL");
        DatabaseStore.getInstance().addLog(currentUser.getUsername()+" làm khảo sát: "+r.getRiskLevel());
    }

    private JPanel createFinalResultPanel(SurveyResult r) {
        JPanel p = new JPanel(); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel l1 = new JLabel(r.getRiskLevel()); l1.setFont(new Font("Arial",Font.BOLD,30)); l1.setForeground(r.getColorLevel());
        JTextArea t = new JTextArea(r.getAdvice()); t.setLineWrap(true); t.setEditable(false); t.setMaximumSize(new Dimension(600,100));
        JButton b = new JButton("Làm lại"); b.addActionListener(e -> {progressBar.setVisible(false); cardLayout.show(surveyCardPanel,"START");});
        p.add(Box.createVerticalGlue()); p.add(l1); p.add(Box.createVerticalStrut(20)); p.add(t); p.add(Box.createVerticalStrut(20)); p.add(b); p.add(Box.createVerticalGlue());
        return p;
    }

    // Placeholder required for createResultCard to avoid compile error if called directly
    private JPanel createResultCard() { return new JPanel(); }


    // --- TAB PROFILE (CÓ NÚT REFRESH) ---
    private JPanel createProfilePanel() {
        JPanel p = new JPanel(new BorderLayout());
        JPanel info = new JPanel(); info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS)); info.setPreferredSize(new Dimension(300,0));
        JTextField n = new JTextField(currentUser.getFullName()), a = new JTextField(currentUser.getAge()+"");
        JPasswordField ps = new JPasswordField("******");
        JButton u = new JButton("Lưu thay đổi"), r = new JButton("🔄 TẢI LẠI DỮ LIỆU");

        info.add(Box.createVerticalStrut(10)); info.add(r); info.add(Box.createVerticalStrut(20));
        info.add(new JLabel("Tên:")); info.add(n); info.add(new JLabel("Tuổi:")); info.add(a); info.add(new JLabel("Pass:")); info.add(ps); info.add(u);

        DefaultTableModel mBook = new DefaultTableModel(new String[]{"Lịch sử đặt hẹn"},0);
        DefaultTableModel mEdu = new DefaultTableModel(new String[]{"Giáo dục"},0);
        JTabbedPane t = new JTabbedPane(); t.addTab("Đặt hẹn",new JScrollPane(new JTable(mBook))); t.addTab("Giáo dục",new JScrollPane(new JTable(mEdu)));

        Runnable load = () -> {
            mBook.setRowCount(0); mEdu.setRowCount(0);
            List<String> logs = DatabaseStore.getInstance().getLogs();
            for(int i=logs.size()-1; i>=0; i--) {
                String log = logs.get(i);
                if(log.contains(currentUser.getUsername())) {
                    if(log.contains("BOOKING")) mBook.addRow(new Object[]{log});
                    if(log.contains("Program") || log.contains("khóa")) mEdu.addRow(new Object[]{log});
                }
            }
            n.setText(currentUser.getFullName()); a.setText(currentUser.getAge()+"");
        };

        r.addActionListener(e -> { load.run(); JOptionPane.showMessageDialog(this,"Đã tải lại!"); });
        u.addActionListener(e -> {
            try { currentUser.setFullName(n.getText()); currentUser.setAge(Integer.parseInt(a.getText()));
                String pass = new String(ps.getPassword()); if(!pass.equals("******")) currentUser.setPassword(pass);
                DatabaseStore.getInstance().updateUser(currentUser); load.run(); JOptionPane.showMessageDialog(this,"Đã lưu!"); }
            catch(Exception ex) { JOptionPane.showMessageDialog(this,"Lỗi nhập liệu!"); }
        });

        load.run();
        p.add(info, BorderLayout.WEST); p.add(t, BorderLayout.CENTER); return p;
    }

    // --- TAB REPORT ---
    private JPanel createReportPanel() {
        JPanel main = new JPanel(new BorderLayout());
        JPanel stats = new JPanel(); stats.setLayout(new BoxLayout(stats,BoxLayout.Y_AXIS)); stats.setPreferredSize(new Dimension(400,0));
        DatabaseStore db = DatabaseStore.getInstance();
        JProgressBar barC = new JProgressBar(0,5); barC.setValue(db.countCoursesByUser(currentUser.getUsername())); barC.setStringPainted(true);
        JProgressBar barB = new JProgressBar(0,10); barB.setValue(db.countBookingByUser(currentUser.getUsername())); barB.setStringPainted(true);
        JButton btn = new JButton("Xuất Báo Cáo"); JTextArea txt = new JTextArea();
        btn.addActionListener(e -> { ReportGenerator rg = new UserActivityReport(); txt.setText(rg.generateReport(currentUser)); });
        stats.add(new JLabel("Khóa học:")); stats.add(barC); stats.add(new JLabel("Đặt lịch:")); stats.add(barB); stats.add(btn);
        main.add(stats,BorderLayout.WEST); main.add(new JScrollPane(txt),BorderLayout.CENTER); return main;
    }

    // --- CÁC TAB KHÁC (GIỮ NGUYÊN) ---
    private JPanel createHomePanel() { JPanel p = new JPanel(new BorderLayout()); DefaultListModel<BlogPost> m = new DefaultListModel<>(); for(BlogPost b:DatabaseStore.getInstance().getBlogs()) m.addElement(b); JList<BlogPost> l = new JList<>(m); JEditorPane c = new JEditorPane(); c.setContentType("text/html"); l.addListSelectionListener(e->{if(!e.getValueIsAdjusting()&&l.getSelectedValue()!=null)c.setText(l.getSelectedValue().getContent());}); p.add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,new JScrollPane(l),new JScrollPane(c))); return p; }
    private JPanel createEducationPanel() { JPanel m = new JPanel(new BorderLayout()); DefaultListModel<EduProgram> d = new DefaultListModel<>(); for(EduProgram e:DatabaseStore.getInstance().getEduPrograms()) d.addElement(e); JList<EduProgram> l = new JList<>(d); JPanel dt = new JPanel(); dt.setLayout(new BoxLayout(dt,BoxLayout.Y_AXIS)); JTextArea t = new JTextArea(5,30); JButton b1=new JButton("1. KS Trước"), b2=new JButton("2. Tham gia"), b3=new JButton("3. KS Sau"); b1.setEnabled(false); b2.setEnabled(false); b3.setEnabled(false); dt.add(t); dt.add(b1); dt.add(b2); dt.add(b3); l.addListSelectionListener(e->{ EduProgram p=l.getSelectedValue(); if(p!=null){ t.setText(p.toString()); updateEdu(DatabaseStore.getInstance().getProgramStage(currentUser.getUsername(),p.getId()),b1,b2,b3); }}); b1.addActionListener(e->{if(JOptionPane.showInputDialog("KS:")!=null){DatabaseStore.getInstance().updateProgramStage(currentUser.getUsername(),l.getSelectedValue().getId(),1);updateEdu(1,b1,b2,b3);}}); b2.addActionListener(e->{JOptionPane.showMessageDialog(null,"Done!");DatabaseStore.getInstance().updateProgramStage(currentUser.getUsername(),l.getSelectedValue().getId(),2);updateEdu(2,b1,b2,b3);}); b3.addActionListener(e->{if(JOptionPane.showInputDialog("KS:")!=null){DatabaseStore.getInstance().updateProgramStage(currentUser.getUsername(),l.getSelectedValue().getId(),3);updateEdu(3,b1,b2,b3);}}); m.add(new JScrollPane(l),BorderLayout.WEST); m.add(dt,BorderLayout.CENTER); return m; }
    private void updateEdu(int s, JButton b1, JButton b2, JButton b3) { b1.setEnabled(s==0); b2.setEnabled(s==1); b3.setEnabled(s==2); }
    private JPanel createCounselorPanel() { JPanel p = new JPanel(new BorderLayout()); DefaultListModel<Counselor> m = new DefaultListModel<>(); for(Counselor c:DatabaseStore.getInstance().getCounselors()) m.addElement(c); JList<Counselor> l = new JList<>(m); JTextArea i = new JTextArea(); JButton b = new JButton("Đặt lịch"); l.addListSelectionListener(e->{if(l.getSelectedValue()!=null) i.setText(l.getSelectedValue().toString());}); b.addActionListener(e->{if(l.getSelectedValue()!=null) new BookingManager().book(currentUser,l.getSelectedValue().getName());}); p.add(new JScrollPane(l),BorderLayout.WEST); JPanel r=new JPanel(new BorderLayout()); r.add(i); r.add(b,BorderLayout.SOUTH); p.add(r); return p; }
    private JPanel createTrainingPanel() { JPanel p = new JPanel(new BorderLayout()); DefaultTableModel m = new DefaultTableModel(new String[]{"Mã","Tên","Đối tượng","Loại"},0); JTable t = new JTable(m); loadC(m,DatabaseStore.getInstance().getCourses()); JButton b = new JButton("Đăng ký"); b.addActionListener(e->{if(t.getSelectedRow()>=0) DatabaseStore.getInstance().registerCourse(currentUser.getUsername(),(String)m.getValueAt(t.getSelectedRow(),1));}); p.add(new JScrollPane(t),BorderLayout.CENTER); p.add(b,BorderLayout.SOUTH); return p; }
    private void loadC(DefaultTableModel m, java.util.List<Course> c) { m.setRowCount(0); for(Course x:c) m.addRow(x.toRowData()); }
    private JPanel createBookingPanel() { JPanel p=new JPanel(null); JButton b=new JButton("Đặt lịch nhanh"); b.setBounds(50,50,200,30); b.addActionListener(e->new BookingManager().book(currentUser,"Nhanh")); p.add(b); return p; }
}