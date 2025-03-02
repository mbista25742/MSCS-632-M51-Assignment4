package cumberland.assignment4.Scheduler;

import cumberland.assignment4.constants.CompanyConstants;
import cumberland.assignment4.database.CompanyDatabase;
import cumberland.assignment4.entity.DailySchedule;
import cumberland.assignment4.entity.Employee;

import java.util.*;

public class EmployeeScheduler {

    public static void assignShifts() {
        CompanyDatabase.weeklyScheduleList.clear(); // Clear previous schedule
        //reset total hours worked and total days worked
        // Loop through all employees in the employee preference database
        for (Employee e : CompanyDatabase.employeePreferenceDB) {
            // Reset total hours worked and total days worked to 0
            e.setTotalHoursWorked((byte) 0);
            e.setTotalDaysWorked((byte) 0);
        }

        for (int i = 0; i < 7; i++) {
            DailySchedule newDailySchedule = new DailySchedule();

            // Process morning (0), day (1), and evening (2) shifts
            for (int shiftIndex = 0; shiftIndex < 3; shiftIndex++) {
                assignShiftForDay(newDailySchedule, i, shiftIndex);
            }

            // Ensure that each shift is rechecked and adjusted as necessary
            recheckShiftsAndAssignEmployees(newDailySchedule, i);

            // Add finalized daily schedule to database
            CompanyDatabase.weeklyScheduleList.add(newDailySchedule);
        }
    }

    private static void assignShiftForDay(DailySchedule dailySchedule, int day, int shiftIndex) {
        // Get employees who meet CompanyConstants.MAX_SHIFT_EMPLOYEE-day work condition for this shift
        ArrayList<Employee> shiftCandidates = getEmployeesByDayAndShift(day, shiftIndex + 1);

        // Filter employees who have not worked any shift that day
        shiftCandidates = ensureNoMoreThanOneShift(shiftCandidates, day);

        System.out.println("Candidates for shift " + shiftIndex+1 + " on day " + day + ": " + shiftCandidates);

        Map<String, Employee> tempMap = new HashMap<>();
        Set<String> assignedEmployeeIds = new HashSet<>();  // Track assigned employees to prevent duplicates

        // Assign employees to the shift (only if the shift is not full)
        for (Employee e : shiftCandidates) {
            if (!assignedEmployeeIds.contains(e.getEmployeeId()) && tempMap.size() < CompanyConstants.MAX_SHIFT_EMPLOYEE) {
                tempMap.put(e.getEmployeeId(), e);
                assignedEmployeeIds.add(e.getEmployeeId());  // Mark as assigned
                updateEmployeeWorkDetails(e);
            }
        }

        // Ensure at least 2 employees in the shift (and do not exceed MAX_SHIFT_EMPLOYEE)
        int maxAttempts = 10; // Limit the number of attempts to prevent infinite loop
        while (tempMap.size() < 2 && maxAttempts > 0) {
            Employee extraEmployee = findAvailableEmployee(day, shiftIndex +1);
            if (extraEmployee != null && !assignedEmployeeIds.contains(extraEmployee.getEmployeeId()) && tempMap.size() < CompanyConstants.MAX_SHIFT_EMPLOYEE) {
                tempMap.put(extraEmployee.getEmployeeId(), extraEmployee);
                assignedEmployeeIds.add(extraEmployee.getEmployeeId());  // Mark as assigned
                updateEmployeeWorkDetails(extraEmployee);
            } else {
                System.out.println("No available employee left for day " + day + " and shift " + shiftIndex);
                break; // No available employee left, break the loop
            }
            maxAttempts--; // Decrement attempts to avoid infinite loop
        }

        if (maxAttempts == 0) {
            System.out.println("Reached maximum attempts to find an employee for day " + day + " shift " + shiftIndex);
        }

        // Set shift in the daily schedule
        dailySchedule.shifts.set(shiftIndex, tempMap);

        // Handle conflicts for this shift
       // resolveConflicts(dailySchedule, day);
    }




    /**
         * Get employees available for a specific shift on a given day.
         * First preference is prioritized, but second preference is considered if necessary.
         */
    private static ArrayList<Employee> getEmployeesByDayAndShift(int day, int shift) {
        ArrayList<Employee> primaryCandidates = new ArrayList<>();
        ArrayList<Employee> secondaryCandidates = new ArrayList<>();
        ArrayList<Employee> noPreferenceCandidates = new ArrayList<>();


        for (Employee e : CompanyDatabase.employeePreferenceDB) {
            byte[][] empPref = e.getEmployeePreference();

            if (e.getTotalDaysWorked() < CompanyConstants.MAX_DAYS_WORK) {
                if (empPref[day][0] == shift) {
                    primaryCandidates.add(e);  // 1st preference match
                } else if (empPref[day][1] == shift) {
                    secondaryCandidates.add(e); // 2nd preference match
                }
                else{
                    noPreferenceCandidates.add(e);
                }
            }
        }

        // If primary candidates are too few, consider second preference employees
        if (primaryCandidates.size() < CompanyConstants.MIN_SHIFT_EMPLOYEE) {
            primaryCandidates.addAll(secondaryCandidates);
        }
        if(primaryCandidates.size() > CompanyConstants.MIN_SHIFT_EMPLOYEE){
            primaryCandidates.addAll(noPreferenceCandidates);
        }

        return primaryCandidates;
    }

    /**
     * Ensures a minimum of 2 employees per shift. If fewer than 2, assign random available employees.
     */
    private static void ensureMinimumEmployees(ArrayList<Employee> shiftEmployees) {
        Random random = new Random();
        while (shiftEmployees.size() < 2) {
            Employee randomEmployee = getRandomAvailableEmployee();
            if (randomEmployee != null) {
                randomEmployee.setTotalDaysWorked((byte) (randomEmployee.getTotalDaysWorked() + 1));
                shiftEmployees.add(randomEmployee);
            } else {
                break; // No available employees left
            }
        }
    }

    /**
     * Selects a random employee who has worked less than CompanyConstants.MAX_SHIFT_EMPLOYEE days.
     */
    private static Employee getRandomAvailableEmployee() {
        List<Employee> availableEmployees = new ArrayList<>();
        for (Employee e : CompanyDatabase.employeePreferenceDB) {
            if (e.getTotalDaysWorked() < CompanyConstants.MAX_DAYS_WORK) {
                availableEmployees.add(e);
            }
        }
        if (availableEmployees.isEmpty()) return null;
        return availableEmployees.get(new Random().nextInt(availableEmployees.size()));
    }

    /**
     * Ensures employees are not double-booked in multiple shifts on the same day.
     */
    private static void resolveConflicts(ArrayList<Employee> morningShift, ArrayList<Employee> dayShift, ArrayList<Employee> eveningShift) {
        HashSet<Employee> assignedEmployees = new HashSet<>();
        List<ArrayList<Employee>> shifts = Arrays.asList(morningShift, dayShift, eveningShift);

        for (ArrayList<Employee> shift : shifts) {
            Iterator<Employee> iterator = shift.iterator();
            while (iterator.hasNext()) {
                Employee employee = iterator.next();
                if (!assignedEmployees.add(employee)) {
                    iterator.remove(); // Remove duplicate assignment
                }
            }
        }
    }



    public static ArrayList<Employee> ensureNoMoreThanOneShift(ArrayList<Employee> empList, int day) {
        // Get the weekly schedule
        ArrayList<DailySchedule> weeklyScheduleList = CompanyDatabase.weeklyScheduleList;

        // If the schedule is empty or the day index doesn't exist yet, return the full employee list
        if (weeklyScheduleList.isEmpty() || day >= weeklyScheduleList.size()) {
            return empList;
        }

        DailySchedule dailyS = weeklyScheduleList.get(day);
        ArrayList<Employee> result = new ArrayList<>();

        // Get all shifts for the given day (ensure they are not null)
        Map<String, Employee> morningShiftList = dailyS.shifts.get(0) != null ? dailyS.shifts.get(0) : new HashMap<>();
        Map<String, Employee> dayShiftList = dailyS.shifts.get(1) != null ? dailyS.shifts.get(1) : new HashMap<>();
        Map<String, Employee> eveningShiftList = dailyS.shifts.get(2) != null ? dailyS.shifts.get(2) : new HashMap<>();

        for (Employee emp : empList) {
            String empId = emp.getEmployeeId();

            // Check if the employee exists in ANY shift
            boolean isAssignedToAnyShift =
                    morningShiftList.containsKey(empId) ||
                            dayShiftList.containsKey(empId) ||
                            eveningShiftList.containsKey(empId);

            // If the employee is NOT in any shift, add them to the result
            if (!isAssignedToAnyShift) {
                result.add(emp);
            }
        }
        return result;
    }




    public static boolean checkConflict(Employee e, int day, int shift){

        //conflict conditions
        //shift is full
       Map<String, Employee> tempResult =  CompanyDatabase.weeklyScheduleList.get(day).shifts.get(shift);
       if(tempResult.size() >= CompanyConstants.MAX_SHIFT_EMPLOYEE){
           return true;
       }

        return false;
    }


    private static void resolveConflicts(DailySchedule dailySchedule, int day) {
        // Iterate through each shift (morning, day, evening) and ensure the shift has no more than MAX_SHIFT_EMPLOYEE
        for (int shiftIndex = 0; shiftIndex < 3; shiftIndex++) {
            Map<String, Employee> currentShift = dailySchedule.shifts.get(shiftIndex);

            // If a shift exceeds the limit, start moving excess employees
            while (currentShift.size() > CompanyConstants.MAX_SHIFT_EMPLOYEE) {
                List<Employee> excessEmployees = new ArrayList<>(currentShift.values());
                Collections.shuffle(excessEmployees); // Shuffle to distribute randomly

                Employee excessEmployee = excessEmployees.get(excessEmployees.size() - 1); // Take the last employee
                currentShift.remove(excessEmployee.getEmployeeId()); // Remove excess employee from the current shift

                // Try assigning excess employees to other shifts (if available)
                if (dailySchedule.shifts.get(1).size() < CompanyConstants.MAX_SHIFT_EMPLOYEE) {
                    dailySchedule.shifts.get(1).put(excessEmployee.getEmployeeId(), excessEmployee);
                } else if (dailySchedule.shifts.get(2).size() < CompanyConstants.MAX_SHIFT_EMPLOYEE) {
                    dailySchedule.shifts.get(2).put(excessEmployee.getEmployeeId(), excessEmployee);
                } else if (day < 6 && CompanyDatabase.weeklyScheduleList.size() > day + 1) {
                    // If all shifts are full, move to the next day
                    DailySchedule nextDay = CompanyDatabase.weeklyScheduleList.get(day + 1);
                    if (nextDay.shifts.get(0).size() < CompanyConstants.MAX_SHIFT_EMPLOYEE) {
                        nextDay.shifts.get(0).put(excessEmployee.getEmployeeId(), excessEmployee);
                    }
                }
            }
        }
    }




    private static Employee findAvailableEmployee(int day, int shift) {
        // First, look for employees with first preference for the shift on the given day
        for (Employee emp : CompanyDatabase.employeePreferenceDB) {
            int workDays = emp.getTotalDaysWorked();

            // Check if the employee has worked less than the allowed maximum days
            if (workDays < CompanyConstants.MAX_DAYS_WORK) {

                // Check if the employee has not been assigned to any shift on the current day
                boolean isAssignedToAnyShift = isEmployeeAssignedToShiftOnDay(emp, day);

                if (!isAssignedToAnyShift && emp.getEmployeePreference()[day][0] == shift) {
                    return emp;
                }
            }
        }

        // If no employee found, look at second preferences
        for (Employee emp : CompanyDatabase.employeePreferenceDB) {
            int workDays = emp.getTotalDaysWorked();

            if (workDays < CompanyConstants.MAX_DAYS_WORK) {
                boolean isAssignedToAnyShift = isEmployeeAssignedToShiftOnDay(emp, day);

                if (!isAssignedToAnyShift && emp.getEmployeePreference()[day][1] == shift) {
                    return emp;
                }
            }
        }

        // If no employee found, look at no preferences
        for (Employee emp : CompanyDatabase.employeePreferenceDB) {
            int workDays = emp.getTotalDaysWorked();

            if (workDays < CompanyConstants.MAX_DAYS_WORK) {
                boolean isAssignedToAnyShift = isEmployeeAssignedToShiftOnDay(emp, day);

                if (!isAssignedToAnyShift && (emp.getEmployeePreference()[day][1] == 0) && emp.getEmployeePreference()[day][0]==0) {
                    return emp;
                }
            }
        }

        // If no employee found for the day, try looking for available employees on other days
        Employee employeeFromOtherDay = findEmployeeFromOtherDay(day, shift);
        if (employeeFromOtherDay != null) {
            return employeeFromOtherDay;
        }

        return null; // No available employee found
    }


    private static Employee findEmployeeFromOtherDay(int day, int shift) {
        // Iterate through the weekly schedule to find an employee who is not assigned to the current shift
        for (int i = 0; i < 7; i++) {
            if (i == day) continue; // Skip the current day

            if(i > CompanyDatabase.weeklyScheduleList.size()){
                continue;
            }
            DailySchedule dailySchedule = CompanyDatabase.weeklyScheduleList.get(i);

            // Search for employees who are assigned to the shift on another day but have fewer than the max allowed work days
            for (Map<String, Employee> shiftMap : dailySchedule.shifts) {
                for (Employee emp : shiftMap.values()) {
                    int workDays = emp.getTotalDaysWorked();

                    if (workDays < CompanyConstants.MAX_DAYS_WORK && emp.getEmployeePreference()[i][0] == shift) {
                        return emp; // Found an employee who can be moved
                    }
                }
            }
        }
        return null; // No employee found from other days
    }


//    private static int getTotalDaysWorked(Employee emp) {
//        int count = 0;getTotalDaysWorked
//        for (DailySchedule daily : CompanyDatabase.weeklyScheduleList) {
//            for (Map<String, Employee> shift : daily.shifts) {
//                if (shift.containsKey(emp.getEmployeeId())) {
//                    count++;
//                    break; // Only count once per day
//                }
//            }
//        }
//        return count;
//    }

    private static void updateEmployeeWorkDetails(Employee emp) {
        emp.setTotalDaysWorked((byte) (emp.getTotalDaysWorked() + 1));
    }

    private static boolean isEmployeeAssignedToShiftOnDay(Employee emp, int day) {
        // Get the weekly schedule
        ArrayList<DailySchedule> weeklyScheduleList = CompanyDatabase.weeklyScheduleList;

        // If the schedule is empty or the day index doesn't exist yet, return false (not assigned)
        if (weeklyScheduleList.isEmpty() || day >= weeklyScheduleList.size()) {
            return false;
        }

        DailySchedule dailySchedule = weeklyScheduleList.get(day);

        // Check if the employee is assigned to any shift on the given day
        for (Map<String, Employee> shift : dailySchedule.shifts) {
            if (shift.containsKey(emp.getEmployeeId())) {
                return true; // Employee is assigned to a shift
            }
        }

        return false; // Employee is not assigned to any shift
    }


    private static void recheckShiftsAndAssignEmployees(DailySchedule dailySchedule, int day) {
        // Iterate through each shift and check if they can hold the max shift employees
        for (int shiftIndex = 0; shiftIndex < 3; shiftIndex++) {
            Map<String, Employee> currentShift = dailySchedule.shifts.get(shiftIndex);

            // If the shift is not full, try adding more employees
            while (currentShift.size() < CompanyConstants.MAX_SHIFT_EMPLOYEE) {
                Employee extraEmployee = findAvailableEmployeeForShift(day, shiftIndex);

                if (extraEmployee != null) {
                    currentShift.put(extraEmployee.getEmployeeId(), extraEmployee);
                    updateEmployeeWorkDetails(extraEmployee);
                } else {
                    System.out.println("No more employees available for shift " + shiftIndex + " on day " + day);
                    break;
                }
            }
        }
    }

    private static Employee findAvailableEmployeeForShift(int day, int shiftIndex) {
        // Iterate through the employee list and find those who meet the criteria for the given shift
        for (Employee emp : CompanyDatabase.employeePreferenceDB) {
            int workDays = emp.getTotalDaysWorked();

            // Check if the employee has worked less than the allowed maximum days and not assigned to any shift today
            if (workDays < CompanyConstants.MAX_DAYS_WORK && !isEmployeeAssignedToShiftOnDay(emp, day)) {
                // Check if they match the required shift
                if (emp.getEmployeePreference()[day][0] == shiftIndex + 1 || emp.getEmployeePreference()[day][1] == shiftIndex + 1) {
                    return emp;
                }
            }
        }
        return null; // No available employee found
    }

}
