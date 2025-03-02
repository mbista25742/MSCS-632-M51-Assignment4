package cumberland.assignment4.Scheduler;

import cumberland.assignment4.constants.CompanyConstants;
import cumberland.assignment4.database.CompanyDatabase;
import cumberland.assignment4.entity.DailySchedule;
import cumberland.assignment4.entity.Employee;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class EmployeeScheduler2 {


    public static void assignShifts() {

        System.out.println("Assigning shift with scheduler 2");
        CompanyDatabase.weeklyScheduleList.clear(); // Clear previous schedule
        //reset total hours worked and total days worked
        // Loop through all employees in the employee preference database
        for (Employee e : CompanyDatabase.employeePreferenceDB) {
            // Reset total hours worked and total days worked to 0
            e.setTotalHoursWorked((byte) 0);
            e.setTotalDaysWorked((byte) 0);
            e.resetWorkedOnShiftIndex();
        }

        // Create a copy of the employee list so that we don't modify the original list
        List<Employee> employeeList = new ArrayList<>(CompanyDatabase.employeePreferenceDB);

        // Shuffle the employee list to randomize the order
        Collections.shuffle(employeeList);

        for (int i = 0; i < 7; i++) {


            // Now, pick all elements from the shuffled list
            for (Employee employee : employeeList) {
                // Process each employee (e.g., assign shift, log details, etc.)
                System.out.println("Picking employee: " + employee.getEmployeeId());

                // Example: Use employee in your logic (e.g., assigning shift, updating work details, etc.)
                // updateEmployeeWorkDetails(employee);  // Uncomment if needed for processing

                //check if employee worked 5 days already
                if (employee.getTotalDaysWorked() >= 5) {
                    System.out.println("Employee wored more than 5 days");
                    continue;
                }

                // check if the employee already assigned to the shift on that day
                if (employee.getWorkedOnShiftIndex()[i] == 1) {
                    System.out.println("employee already assigned");
                    continue;
                }

                //now pick  day and preference form each employee
                byte firstShiftPreference = employee.getEmployeePreference()[i][0];
                byte secondShiftPreference = employee.getEmployeePreference()[i][1];

                //check schedule is full or not
                if(CompanyDatabase.weeklyScheduleList.isEmpty()){
                    for (int j = 0; j < 7; j++) {
                        CompanyDatabase.weeklyScheduleList.add(new DailySchedule()); // Add an empty DailySchedule for each day of the week
                    }
                }
                DailySchedule sch = CompanyDatabase.weeklyScheduleList.get(i);

                int shiftPreferenceIndex = firstShiftPreference == 0? 0: firstShiftPreference -1;
                if (sch.shifts.get(shiftPreferenceIndex).size() < 4) {
                    //assign to that shift
                    sch.shifts.get(shiftPreferenceIndex).put(employee.getEmployeeId(), employee);
                    employee.setTotalDaysWorked((byte) (employee.getTotalDaysWorked() + 1));
                    employee.setWorkedOnShiftIndex(i);
                    System.out.println("New schedule " + CompanyDatabase.weeklyScheduleList.toString());
                } else {
                    //if the shift is full
                    //conflict occured assign the employee to next available shift
//                    handleConflictAndAssign(employee, i);
                    conflictResolver(employee, i);
                    System.out.println("Final schedule is " + CompanyDatabase.weeklyScheduleList.toString());
                    //lets handle conflict later
                }

            }
        }

        for(int i = 0; i< 7; i++)
        {
            //now make sure each shift has minimum 2 employees
            for(DailySchedule dailyS : CompanyDatabase.weeklyScheduleList){

                //three shifts morning, day and evening
                for(int m = 0; m< 3; m++){
                    Map<String, Employee> eachShift =    dailyS.shifts.get(m);
                    //
                    int iterationCount = 0;
                    while(eachShift.size() < 2 || iterationCount <5){
                        System.out.println("****** found minimum on " + i + " shift is " + m);
                        //find another employee and assignit
                        Employee e = findAnotherAvailableEmployee(i,m);
                        if(e == null){
                            break;
                        }
                        eachShift.put(e.getEmployeeId(), e);
                        e.setWorkedOnShiftIndex(i);
                        e.setTotalDaysWorked((byte) (e.getTotalDaysWorked() + 1));
                        iterationCount++;
                    }

                }
            }
        }


    }

    public static Employee findAnotherAvailableEmployee(int day, int shift) {
        // Loop through all employees in the database
        for (Employee e : CompanyDatabase.employeePreferenceDB) {
            // Check if the employee has worked less than 5 days
            if (e.getTotalDaysWorked() < 5) {
                // Check if the employee has not worked on the given day and shift
                if (e.getWorkedOnShiftIndex()[day] != 1) {
                    return e; // Return the employee if they meet the criteria
                }
            }
        }

        return null; // If no employee meets the criteria, return null
    }


    public static void handleConflictAndAssign(Employee emp, int day) {

        //

        // Get the employee's first and second shift preferences for the given day
        byte firstShiftPreference = emp.getEmployeePreference()[day][0]; // First preference shift
        byte secondShiftPreference = emp.getEmployeePreference()[day][1]; // Second preference shift

        byte f1 = firstShiftPreference == 0? 0: (byte) (firstShiftPreference - 1);
        byte f2 = secondShiftPreference == 0? 0: (byte) (secondShiftPreference - 1);

        // Check if the first preference shift is available on the current day
        if (isShiftAvailableForAssignment(day, f1)) {
            // If the shift is available, assign the employee to it
            assignShift(day, f1, emp);
            return; // Exit after assigning to first preference
        }

        // If the first preference is full, check the second preference
        if (isShiftAvailableForAssignment(day, f2)) {
            // If the second preference is available, assign the employee to it
            assignShift(day, f2, emp);
            return; // Exit after assigning to second preference
        }

        // If both first and second preferences are full on the current day, look for available shifts on other days
        for (int i = 0; i < 7; i++) {
            if (i == day) continue; // Skip the current day since we are already checking it

            // Check if the first preference shift is available on another day
            if (isShiftAvailableForAssignment(i, f1)) {
                assignShift(i, f1, emp); // Assign to the first preference shift on the other day
                return; // Exit after assigning
            }

            // Check if the second preference shift is available on another day
            if (isShiftAvailableForAssignment(i, f2)) {
                assignShift(i, f2, emp); // Assign to the second preference shift on the other day
                return; // Exit after assigning
            }
        }

        // If no available shift was found for the employee, print a message (you can handle it differently as needed)
        System.out.println("No available shifts for employee " + emp.getEmployeeId());
    }

    // Helper method to check if a shift on a given day is available for assignment
    private static boolean isShiftAvailableForAssignment(int day, byte shiftIndex) {
        // Get the daily schedule for the given day
        DailySchedule dailySchedule = CompanyDatabase.weeklyScheduleList.get(day);

        // Get the shift map for the specified shift
        Map<String, Employee> shift = dailySchedule.shifts.get(shiftIndex);

        // Check if the shift is not full (less than MAX_SHIFT_EMPLOYEE)
        return shift.size() < CompanyConstants.MAX_SHIFT_EMPLOYEE;
    }

    // Method to assign the employee to a specific shift
    private static void assignShift(int day, byte shiftIndex, Employee emp) {
        DailySchedule dailySchedule = CompanyDatabase.weeklyScheduleList.get(day);
        Map<String, Employee> shift = dailySchedule.shifts.get(shiftIndex);

        // Assign the employee to the shift
        shift.put(emp.getEmployeeId(), emp);

        // Update the employee's total days worked
        updateEmployeeWorkDetails(emp, day);
    }

    // Method to update the employee's total days worked
    private static void updateEmployeeWorkDetails(Employee emp, int day) {
        emp.setTotalDaysWorked((byte) (emp.getTotalDaysWorked() + 1));
        emp.setWorkedOnShiftIndex(day);
    }

    public static void conflictResolver(Employee emp, int day){

        DailySchedule dS = CompanyDatabase.weeklyScheduleList.get(day);
        //we already checked that employee is not assigned to any shift in that day
        for(int m = 0; m<3; m++){
            if(dS.shifts.get(m).size() < 4){
                dS.shifts.get(m).put(emp.getEmployeeId(), emp);
                emp.setTotalDaysWorked((byte) (emp.getTotalDaysWorked() + 1));
                emp.setWorkedOnShiftIndex(day);
                break;
            }
        }

        //if not found look for another day

        if(day +1 < 7){
            dS = CompanyDatabase.weeklyScheduleList.get(day+1);
            for(int m = 0; m<3; m++){
                if(dS.shifts.get(m).size() < 4){
                    dS.shifts.get(m).put(emp.getEmployeeId(), emp);
                    emp.setTotalDaysWorked((byte) (emp.getTotalDaysWorked() + 1));
                    emp.setWorkedOnShiftIndex(day+1);
                    break;
                }
            }
        }
    }
}
