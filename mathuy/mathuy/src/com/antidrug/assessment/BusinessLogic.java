package com.antidrug.assessment;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.JOptionPane;

// --- 1. LOGIC KHẢO SÁT WIZARD (MỚI) ---
class Question {
    private String text; private int weight;
    public Question(String t, int w) { text=t; weight=w; }
    public String getText() { return text; }
    public int getWeight() { return weight; }
}

class SurveyResult {
    private String riskLevel, advice; private Color color;
    public SurveyResult(String r, String a, Color c) { riskLevel=r; advice=a; color=c; }
    public String getRiskLevel() { return riskLevel; }
    public String getAdvice() { return advice; }
    public Color getColorLevel() { return color; }
}

interface IAssessment {
    List<Question> getQuestions();
    SurveyResult evaluate(int totalScore);
}

class AssistAssessment implements IAssessment {
    public List<Question> getQuestions() {
        List<Question> q = new ArrayList<>();
        q.add(new Question("Trong 3 tháng qua, bạn có thèm muốn sử dụng chất kích thích?", 5));
        q.add(new Question("Việc sử dụng có gây hại sức khỏe/tài chính?", 5));
        q.add(new Question("Bạn đã từng cố cai nghiện nhưng thất bại?", 5));
        q.add(new Question("Bạn có quên làm việc quan trọng vì dùng thuốc?", 5));
        return q;
    }
    public SurveyResult evaluate(int s) {
        if(s>=15) return new SurveyResult("NGUY CƠ CAO", "Cần gặp chuyên gia ngay.", Color.RED);
        if(s>=5) return new SurveyResult("NGUY CƠ TRUNG BÌNH", "Tham gia khóa học kỹ năng.", Color.ORANGE);
        return new SurveyResult("NGUY CƠ THẤP", "Duy trì lối sống tốt.", new Color(0,153,76));
    }
}

class CrafftAssessment implements IAssessment {
    public List<Question> getQuestions() {
        List<Question> q = new ArrayList<>();
        q.add(new Question("(C) Đi xe khi say?", 1));
        q.add(new Question("(R) Dùng để thư giãn?", 1));
        q.add(new Question("(A) Dùng một mình?", 1));
        q.add(new Question("(F) Quên việc?", 1));
        q.add(new Question("(F) Gia đình khuyên can?", 1));
        q.add(new Question("(T) Gặp rắc rối?", 1));
        return q;
    }
    public SurveyResult evaluate(int s) {
        if(s>=2) return new SurveyResult("SÀNG LỌC DƯƠNG TÍNH", "Cần đánh giá chuyên sâu.", Color.RED);
        return new SurveyResult("SÀNG LỌC ÂM TÍNH", "An toàn.", new Color(0,153,76));
    }
}

class AssessmentFactory { public static IAssessment createAssessment(int age) { return (age>=18)?new AssistAssessment():new CrafftAssessment(); } }

// --- 2. TEMPLATE METHOD REPORT (MỚI) ---
abstract class ReportGenerator {
    public final String generateReport(User user) { return generateHeader(user) + generateStats(user) + generateFooter(); }
    protected String generateHeader(User user) { return "=== BÁO CÁO ===\nUser: " + user.getFullName() + "\nNgày: " + java.time.LocalDate.now() + "\n"; }
    protected abstract String generateStats(User user);
    protected String generateFooter() { return "\n----------------\nCảm ơn đã sử dụng hệ thống."; }
}
class UserActivityReport extends ReportGenerator {
    protected String generateStats(User user) {
        DatabaseStore db = DatabaseStore.getInstance();
        return "Khóa học đã đăng ký: " + db.countCoursesByUser(user.getUsername()) + "\nSố lần đặt lịch hẹn: " + db.countBookingByUser(user.getUsername());
    }
}

// --- 3. STRATEGY SEARCH ---
interface ICourseFilterStrategy { List<Course> filter(List<Course> all, String k); }
class AudienceFilterStrategy implements ICourseFilterStrategy {
    public List<Course> filter(List<Course> all, String audience) {
        if (audience.equals("Tất cả")) return all;
        return all.stream().filter(c -> c.getTargetAudience().equalsIgnoreCase(audience) || c.getTargetAudience().equals("Tất cả")).collect(Collectors.toList());
    }
}
class TitleSearchStrategy implements ICourseFilterStrategy {
    public List<Course> filter(List<Course> all, String key) {
        if (key == null || key.isEmpty()) return all;
        return all.stream().filter(c -> c.getTitle().toLowerCase().contains(key.toLowerCase())).collect(Collectors.toList());
    }
}
class CourseSearchContext {
    private ICourseFilterStrategy strategy;
    public void setStrategy(ICourseFilterStrategy s) { this.strategy = s; }
    public List<Course> executeSearch(List<Course> c, String k) { return strategy == null ? c : strategy.filter(c, k); }
}

// --- 4. OBSERVER BOOKING ---
interface IBookingObserver { void onBookingSuccess(User u, String t); }
class ScreenNotifier implements IBookingObserver {
    public void onBookingSuccess(User u, String t) { JOptionPane.showMessageDialog(null, "Đã gửi mail xác nhận cho " + u.getUsername()); }
}
class AdminLogger implements IBookingObserver {
    public void onBookingSuccess(User u, String t) { DatabaseStore.getInstance().addLog("BOOKING: " + u.getUsername() + " - " + t); }
}
class BookingManager {
    private List<IBookingObserver> obs = new ArrayList<>();
    public void attach(IBookingObserver o) { obs.add(o); }
    public void book(User u, String t) { for(IBookingObserver o : obs) o.onBookingSuccess(u, t); }
}