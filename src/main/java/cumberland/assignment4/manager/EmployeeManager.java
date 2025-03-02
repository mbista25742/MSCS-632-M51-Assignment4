package cumberland.assignment4.manager;

import cumberland.assignment4.database.CompanyDatabase;
import cumberland.assignment4.entity.Employee;
import cumberland.assignment4.utils.EmployeeUtils;

import java.util.ArrayList;
import java.util.Random;

public class EmployeeManager {

//    private ArrayList<Employee> employeeList = new ArrayList<>();

    {
        //load dummy data
        System.out.println("Loading dummy data");
        loadDummyData();

    }

    // Adds a new employee with their schedule
    public static void addEmployee(String name, byte[][] schedule) {
        String id = EmployeeUtils.generateUniqueID(name);
        Employee e = new Employee();
        e.setEmployeeId(id);
        e.setEmployeeName(name);
        e.setEmployeePreference(schedule);

        CompanyDatabase.employeePreferenceDB.add(e);
        System.out.println("New employee added "+ " size now " + CompanyDatabase.employeePreferenceDB.size());

    }

    // Get the list of all employees
    public ArrayList<Employee> getEmployeeList() {
        return CompanyDatabase.employeePreferenceDB;
    }


    private static void loadDummyData() {
        String[] names = {
                "Alice Johnson", "Bob Smith", "Charlie Brown", "David White", "Emma Davis",
                "Frank Miller", "Grace Wilson", "Henry Moore", "Isabella Taylor", "Jack Anderson",
                "Katie Thomas", "Liam Harris", "Mia Martin", "Noah Thompson", "Olivia Scott",
                "Paul Martinez", "Quinn Lee", "Ryan Walker", "Sophia Hall", "Tom King"
        };

        Random random = new Random();

        for (String name : names) {
            byte[][] schedule = new byte[7][2];

            // Populate random preferences
            for (int i = 0; i < 7; i++) {
                schedule[i][0] = (byte) random.nextInt(4); // First preference
                schedule[i][1] = (byte) random.nextInt(4); // Second preference
            }

            addEmployee(name, schedule); // Add to database
        }

        System.out.println("Dummy employees loaded successfully!");
    }
}
