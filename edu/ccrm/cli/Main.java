package edu.ccrm.cli;

import edu.ccrm.domain.*;
import edu.ccrm.io.*;
import edu.ccrm.service.*;

import java.util.Scanner;
import java.util.function.Consumer;

public class Main {
    private static final Scanner sc = new Scanner(System.in);
    private static Services services;

    public static void main(String[] args) {
        services = new Services();
        menuLoop:
        while (true) {
            System.out.println(buildMenu());
            System.out.print("Enter choice: ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1" -> handleAddStudent();
                case "2" -> services.getStudentService().listStudents();
                case "3" -> handleDeactivateStudent();
                case "4" -> handleAddCourse();
                case "5" -> services.getCourseService().listCourses();
                case "6" -> handleDeactivateCourse();
                case "7" -> handleEnrollStudent();
                case "8" -> handleUnenrollStudent();
                case "9" -> services.getEnrollmentService().listEnrollments();
                case "10" -> handleAssignGrade();
                case "11" -> handleShowGpa();
                case "12" -> handlePrintTranscript();
                case "13" -> handleSearchCourses();
                case "14" -> handleImportData();
                case "15" -> handleExportData();
                case "16" -> handleBackupData();
                case "17" -> {
                    System.out.println("Exiting...");
                    break menuLoop;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
        sc.close();
    }

    // --- Private Methods for Handling Menu Choices ---

    private static String promptAndRead(String prompt) {
        System.out.print(prompt);
        return sc.nextLine();
    }

    private static void findAndPerform(String prompt, Consumer<String> action) {
        String id = promptAndRead(prompt);
        action.accept(id);
    }
    
    private static String buildMenu() {
        StringBuilder menu = new StringBuilder("\nCCRM MENU");
        menu.append("\n1. Add Student");
        menu.append("\n2. List Students");
        menu.append("\n3. Deactivate Student");
        menu.append("\n4. Add Course");
        menu.append("\n5. List Courses");
        menu.append("\n6. Deactivate Course");
        menu.append("\n7. Enroll Student in Course");
        menu.append("\n8. Unenroll Student from Course");
        menu.append("\n9. List Enrollments");
        menu.append("\n10. Assign Grade");
        menu.append("\n11. Show GPA");
        menu.append("\n12. Print Transcript");
        menu.append("\n13. Search/Filter Courses");
        menu.append("\n14. Import CSV");
        menu.append("\n15. Export Data");
        menu.append("\n16. Backup Data");
        menu.append("\n17. Exit");
        return menu.toString();
    }

    private static void handleAddStudent() {
        String id = promptAndRead("Enter ID: ");
        String name = promptAndRead("Enter Name: ");
        String email = promptAndRead("Enter Email: ");
        String regNo = promptAndRead("Enter RegNo: ");
        Student s = new Student(id, name, email, regNo);
        services.getStudentService().addStudent(s);
        System.out.println("Student added.");
    }

    private static void handleDeactivateStudent() {
        findAndPerform("Enter Student ID to deactivate: ", sid ->
            services.getStudentService().findById(sid).ifPresentOrElse(
                Student::deactivate,
                () -> System.out.println("Student not found!")
            )
        );
    }

    private static void handleAddCourse() {
        String code = promptAndRead("Enter Course Code: ");
        String title = promptAndRead("Enter Title: ");
        int credits = Integer.parseInt(promptAndRead("Enter Credits: "));
        String instructor = promptAndRead("Enter Instructor: ");
        String dept = promptAndRead("Enter Department: ");
        String sem = promptAndRead("Enter Semester: ");
        Course c = new Course(code, title, credits, instructor, dept, sem);
        services.getCourseService().addCourse(c);
        System.out.println("Course added.");
    }

    private static void handleDeactivateCourse() {
        findAndPerform("Enter Course Code to deactivate: ", code ->
            services.getCourseService().findByCode(code).ifPresentOrElse(
                Course::deactivate,
                () -> System.out.println("Course not found!")
            )
        );
    }

    private static void handleEnrollStudent() {
        String sid = promptAndRead("Enter Student ID: ");
        String code = promptAndRead("Enter Course Code: ");
        services.getStudentService().findById(sid).ifPresentOrElse(
            student -> services.getCourseService().findByCode(code).ifPresentOrElse(
                course -> services.getEnrollmentService().enroll(student, course),
                () -> System.out.println("Course not found!")
            ),
            () -> System.out.println("Student not found!")
        );
    }

    private static void handleUnenrollStudent() {
        String sid = promptAndRead("Enter Student ID: ");
        String code = promptAndRead("Enter Course Code: ");
        services.getStudentService().findById(sid).ifPresentOrElse(
            student -> services.getCourseService().findByCode(code).ifPresentOrElse(
                course -> services.getEnrollmentService().unenroll(student, course),
                () -> System.out.println("Course not found!")
            ),
            () -> System.out.println("Student not found!")
        );
    }

    private static void handleAssignGrade() {
        String sid = promptAndRead("Enter Student ID: ");
        String code = promptAndRead("Enter Course Code: ");
        String g = promptAndRead("Enter Grade (S/A/B/C/D/F): ");
        services.getStudentService().findById(sid).ifPresentOrElse(
            student -> services.getCourseService().findByCode(code).ifPresentOrElse(
                course -> services.getEnrollmentService().assignGrade(student, course, Grade.valueOf(g)),
                () -> System.out.println("Course not found!")
            ),
            () -> System.out.println("Student not found!")
        );
    }

    private static void handleShowGpa() {
        String sid = promptAndRead("Enter Student ID: ");
        services.getStudentService().findById(sid).ifPresentOrElse(
            student -> System.out.println("GPA: " + services.getEnrollmentService().calculateGPA(student)),
            () -> System.out.println("Student not found!")
        );
    }

    private static void handlePrintTranscript() {
        String sid = promptAndRead("Enter Student ID: ");
        services.getStudentService().printTranscript(sid);
    }

    private static void handleSearchCourses() {
        String instr = promptAndRead("Enter instructor name to search: ");
        var filtered = services.getCourseService().searchByInstructor(instr);
        System.out.println("Courses found:");
        filtered.forEach(System.out::println);
    }

    private static void handleImportData() {
        try {
            String studentFile = promptAndRead("Enter Students CSV file path: ");
            services.getImportService().importStudents(studentFile, services.getStudentService());
            String courseFile = promptAndRead("Enter Courses CSV file path: ");
            services.getImportService().importCourses(courseFile, services.getCourseService());
            String enrollFile = promptAndRead("Enter Enrollments CSV file path: ");
            services.getImportService().importEnrollments(enrollFile, services.getStudentService(), services.getCourseService(), services.getEnrollmentService());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void handleExportData() {
        try {
            services.getExportService().exportData(
                "export.txt",
                services.getStudentService().getAllStudents(),
                services.getCourseService().getAllCourses(),
                services.getEnrollmentService().getAllEnrollments()
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void handleBackupData() {
        try {
            services.getBackupService().backup("exports");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class Services {
        private final StudentService studentService = new StudentService();
        private final CourseService courseService = new CourseService();
        private final EnrollmentService enrollmentService = new EnrollmentService();
        private final ExportService exportService = new ExportService();
        private final BackupService backupService = new BackupService();
        private final ImportService importService = new ImportService();

        public StudentService getStudentService() { return studentService; }
        public CourseService getCourseService() { return courseService; }
        public EnrollmentService getEnrollmentService() { return enrollmentService; }
        public ExportService getExportService() { return exportService; }
        public BackupService getBackupService() { return backupService; }
        public ImportService getImportService() { return importService; }
    }
}