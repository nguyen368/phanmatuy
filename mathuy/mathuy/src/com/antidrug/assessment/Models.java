package com.antidrug.assessment;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 1. User
class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private String username, password, fullName; private int age;
    public User(String u, String p, int a, String f) { username=u; password=p; age=a; fullName=f; }
    public String getUsername() { return username; }
    public boolean checkPassword(String p) { return this.password.equals(p); }
    public int getAge() { return age; }
    public String getFullName() { return fullName; }
    public void setFullName(String f) { this.fullName = f; }
    public void setAge(int a) { this.age = a; }
    public void setPassword(String p) { this.password = p; }
}

// 2. Counselor
class Counselor implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id, name, degree, specialization, schedule, bio;
    public Counselor(String id, String name, String degree, String specialization, String schedule, String bio) {
        this.id = id; this.name = name; this.degree = degree; this.specialization = specialization; this.schedule = schedule; this.bio = bio;
    }
    public String getName() { return name; }
    public String getDegree() { return degree; }
    public String getSpecialization() { return specialization; }
    public String getSchedule() { return schedule; }
    public String getBio() { return bio; }
    @Override public String toString() { return name + " (" + specialization + ")"; }
}

// 3. EduProgram
class EduProgram implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id, title, description, date;
    public EduProgram(String id, String title, String description, String date) {
        this.id=id; this.title=title; this.description=description; this.date=date;
    }
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getDate() { return date; }
    @Override public String toString() { return title + " (" + date + ")"; }
}

// 4. Course
class Course implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id, title, targetAudience, type;
    public Course(String id, String title, String targetAudience, String type) {
        this.id = id; this.title = title; this.targetAudience = targetAudience; this.type = type;
    }
    public String getTitle() { return title; }
    public String getTargetAudience() { return targetAudience; }
    public Object[] toRowData() { return new Object[]{id, title, targetAudience, type}; }
}

// 5. BlogPost
class BlogPost implements Serializable {
    private static final long serialVersionUID = 1L;
    private String title, author, content;
    public BlogPost(String t, String a, String c) { title=t; author=a; content=c; }
    public String getContent() { return content; }
    public String getAuthor() { return author; }
    @Override public String toString() { return title; }
}

// 6. DATABASE STORE
class DatabaseStore {
    private static DatabaseStore instance;
    private static final String FILE_NAME = "antidrug_data.dat";

    private List<User> users;
    private List<String> logs;
    private List<BlogPost> blogs;
    private List<Course> courses;
    private List<EduProgram> eduPrograms;
    private List<Counselor> counselors;
    private Map<String, Integer> programProgress;

    private DatabaseStore() {
        if (!loadData()) {
            System.out.println("-> Khởi tạo dữ liệu mẫu...");
            users = new ArrayList<>(); logs = new ArrayList<>(); blogs = new ArrayList<>();
            courses = new ArrayList<>(); eduPrograms = new ArrayList<>(); counselors = new ArrayList<>();
            programProgress = new HashMap<>();
            initData(); saveData();
        }
    }

    public static DatabaseStore getInstance() { if (instance == null) instance = new DatabaseStore(); return instance; }

    private void initData() {
        users.add(new User("admin", "123", 30, "Quản Trị Viên"));
        users.add(new User("Nguyen", "123", 16, "Nguyễn Vũ Đình Nguyên (Học sinh)"));
        users.add(new User("tam", "123", 45, "Trần Thị Tâm (Phụ huynh)"));

        blogs.add(new BlogPost("Cách nhận biết sớm", "Dr. Minh", "<html><h2>Dấu hiệu</h2><p>Mất ngủ, mắt đỏ.</p></html>"));
        blogs.add(new BlogPost("Kỹ năng từ chối", "CG Tâm lý", "<html><h2>Nói KHÔNG</h2><p>Dứt khoát từ chối.</p></html>"));
        blogs.add(new BlogPost("Câu chuyện cai nghiện", "Admin", "<html><h2>Hành trình</h2><p>Đừng bỏ cuộc.</p></html>"));

        courses.add(new Course("KH01", "Nhận thức chung", "Tất cả", "Kiến thức"));
        courses.add(new Course("KH02", "Kỹ năng từ chối", "Học sinh", "Kỹ năng mềm"));
        courses.add(new Course("KH03", "Phát hiện sớm", "Phụ huynh", "Kỹ năng"));

        eduPrograms.add(new EduProgram("PR01", "Chiến dịch không khói thuốc", "Tuyên truyền THPT.", "20/12/2025"));
        eduPrograms.add(new EduProgram("PR02", "Workshop Cha mẹ", "Hướng dẫn phụ huynh.", "25/12/2025"));

        counselors.add(new Counselor("CS01", "TS. BS Minh", "Tiến sĩ Y khoa", "Cai nghiện", "T2, T4 (Sáng)", "20 năm kinh nghiệm."));
        counselors.add(new Counselor("CS02", "ThS. Lan", "Thạc sĩ Tâm lý", "Tâm lý vị thành niên", "T3, T5 (Chiều)", "Chuyên gia học đường."));
        counselors.add(new Counselor("CS03", "LS. Hùng", "Luật sư", "Pháp lý", "T7 (Cả ngày)", "Hỗ trợ pháp lý."));
    }

    public void saveData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(users); oos.writeObject(logs); oos.writeObject(programProgress);
            oos.writeObject(blogs); oos.writeObject(courses); oos.writeObject(eduPrograms); oos.writeObject(counselors);
        } catch (IOException e) { e.printStackTrace(); }
    }

    @SuppressWarnings("unchecked")
    private boolean loadData() {
        File file = new File(FILE_NAME);
        if (!file.exists()) return false;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            users = (List<User>) ois.readObject(); logs = (List<String>) ois.readObject();
            programProgress = (Map<String, Integer>) ois.readObject(); blogs = (List<BlogPost>) ois.readObject();
            courses = (List<Course>) ois.readObject(); eduPrograms = (List<EduProgram>) ois.readObject();
            counselors = (List<Counselor>) ois.readObject(); return true;
        } catch (Exception e) { return false; }
    }

    public User login(String u, String p) { for(User user:users) if(user.getUsername().equals(u) && user.checkPassword(p)) return user; return null; }
    public void updateUser(User u) { saveData(); }
    public List<BlogPost> getBlogs() { return blogs; }
    public List<Course> getCourses() { return courses; }
    public List<EduProgram> getEduPrograms() { return eduPrograms; }
    public List<Counselor> getCounselors() { return counselors; }
    public List<String> getLogs() { return logs; }
    public void addLog(String log) { logs.add(log); saveData(); }
    public void registerCourse(String u, String c) { addLog("User " + u + " đăng ký khóa: " + c); }
    public int getProgramStage(String u, String pid) { return programProgress.getOrDefault(u+"_"+pid, 0); }
    public void updateProgramStage(String u, String pid, int stage) { programProgress.put(u+"_"+pid, stage); addLog("User "+u+" cập nhật tiến độ Program "+pid+": "+stage); }
    public int countCoursesByUser(String u) { int c=0; for(String l:logs) if(l.contains("User "+u) && l.contains("đăng ký khóa")) c++; return c; }
    public int countBookingByUser(String u) { int c=0; for(String l:logs) if(l.contains("BOOKING") && l.contains(u)) c++; return c; }
}