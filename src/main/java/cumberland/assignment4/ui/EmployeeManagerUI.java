package cumberland.assignment4.ui;

import cumberland.assignment4.Scheduler.EmployeeScheduler;
import cumberland.assignment4.Scheduler.EmployeeScheduler2;
import cumberland.assignment4.database.CompanyDatabase;
import cumberland.assignment4.entity.DailySchedule;
import cumberland.assignment4.entity.Employee;
import cumberland.assignment4.manager.EmployeeManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class EmployeeManagerUI {

    EmployeeManager manager = new EmployeeManager();

    public void startUI() {
        JFrame frame = new JFrame("Employee Shift Scheduler");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Capture Details", createCapturePanel());
        tabbedPane.addTab("Display Schedule", createSchedulePanel());

        frame.add(tabbedPane);
        frame.setVisible(true);
    }

    private JPanel createCapturePanel() {
        JPanel capturePanel = new JPanel();
        capturePanel.setLayout(new GridLayout(10, 3));

        JLabel nameLabel = new JLabel("Employee Name:");
        capturePanel.add(nameLabel);
        JTextField nameField = new JTextField();
        capturePanel.add(nameField);

        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        JComboBox<String>[][] shiftComboBoxes = new JComboBox[7][2];

        String[] shifts = {"None", "Morning", "Day", "Evening"};

        for (int i = 0; i < 7; i++) {
            capturePanel.add(new JLabel(days[i] + ":"));
            shiftComboBoxes[i][0] = new JComboBox<>(shifts);
            shiftComboBoxes[i][1] = new JComboBox<>(shifts);
            capturePanel.add(shiftComboBoxes[i][0]);
            capturePanel.add(shiftComboBoxes[i][1]);
        }

        JButton captureButton = new JButton("Capture Schedule");
        capturePanel.add(captureButton);

        JTextArea capturedScheduleArea = new JTextArea();
        capturedScheduleArea.setEditable(false);
        capturePanel.add(new JScrollPane(capturedScheduleArea));

        captureButton.addActionListener(e -> {
            String employeeName = nameField.getText();
            if (employeeName.isEmpty()) {
                JOptionPane.showMessageDialog(capturePanel, "Please enter employee's name.");
                return;
            }

            byte[][] scheduleInInteger = new byte[7][2];
            for (int i = 0; i < 7; i++) {
                scheduleInInteger[i][0] = getShiftCode((String) shiftComboBoxes[i][0].getSelectedItem());
                scheduleInInteger[i][1] = getShiftCode((String) shiftComboBoxes[i][1].getSelectedItem());
            }

            EmployeeManager.addEmployee(employeeName, scheduleInInteger);

            StringBuilder scheduleDisplay = new StringBuilder();
            scheduleDisplay.append("Schedule for ").append(employeeName).append(":\n");
            for (int i = 0; i < 7; i++) {
                scheduleDisplay.append(days[i]).append(": ")
                        .append(firstPrefToString(scheduleInInteger[i][0])).append(", ")
                        .append(firstPrefToString(scheduleInInteger[i][1])).append("\n");
            }
            capturedScheduleArea.setText(scheduleDisplay.toString());
        });

        return capturePanel;
    }

    private JPanel createSchedulePanel() {
        JPanel schedulePanel = new JPanel(new BorderLayout());

        String[] columnNames = {"Day", "Morning Shift", "Day Shift", "Evening Shift"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
        JTable scheduleTable = new JTable(tableModel);
        schedulePanel.add(new JScrollPane(scheduleTable), BorderLayout.CENTER);

        JButton loadSchedulesButton = new JButton("Load Schedules");
        schedulePanel.add(loadSchedulesButton, BorderLayout.SOUTH);

        loadSchedulesButton.addActionListener(e -> {
            tableModel.setRowCount(0); // Clear previous table data
            CompanyDatabase.weeklyScheduleList.clear();

//            for (Employee em: CompanyDatabase.employeePreferenceDB){
////                System.out.println(em.toString());
//            }

//            EmployeeScheduler.assignShifts(); // Ensure shifts are updated
            EmployeeScheduler2.assignShifts();

            // Fetch schedule from CompanyDatabase
            List<DailySchedule> weeklySchedule = CompanyDatabase.weeklyScheduleList;
            String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

            for (int i = 0; i < 7; i++) {
                if (i < weeklySchedule.size()) {
                    DailySchedule dailySchedule = weeklySchedule.get(i);

                    tableModel.addRow(new Object[]{
                            days[i],
                            formatEmployeeList(dailySchedule.shifts.get(0)), // Morning Shift
                            formatEmployeeList(dailySchedule.shifts.get(1)), // Day Shift
                            formatEmployeeList(dailySchedule.shifts.get(2))  // Evening Shift
                    });
                } else {
                    // If schedule is missing a day, fill with empty data
                    tableModel.addRow(new Object[]{days[i], "--", "--", "--"});
                }
            }
        });

        return schedulePanel;
    }

    /**
     * Helper method to format a list of employees into a comma-separated string.
     */
    private String formatEmployeeList(Map<String, Employee> employees) {
        if (employees == null || employees.isEmpty()) {
            return "--"; // No employees assigned
        }
        return String.join(", ", employees.values().stream().map(Employee::getEmployeeName).toArray(String[]::new));
    }

    private byte getShiftCode(String shift) {
        return switch (shift) {
            case "Morning" -> 1;
            case "Day" -> 2;
            case "Evening" -> 3;
            default -> 0; // Represents "None" (no shift assigned)
        };
    }

    /**
     * Converts shift code back to shift name.
     * - 1 -> "Morning"
     * - 2 -> "Day"
     * - 3 -> "Evening"
     * - 0 or any unknown value -> "None"
     */
    private String firstPrefToString(byte code) {
        return switch (code) {
            case 1 -> "Morning";
            case 2 -> "Day";
            case 3 -> "Evening";
            default -> "None"; // Represents unassigned shift
        };
    }

}
