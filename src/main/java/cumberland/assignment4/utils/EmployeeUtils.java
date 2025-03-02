package cumberland.assignment4.utils;

import cumberland.assignment4.database.CompanyDatabase;
import cumberland.assignment4.entity.Employee;

import java.util.ArrayList;
import java.util.UUID;

public class EmployeeUtils {

    public static String generateUniqueID(String employeeName){

        String id = UUID.randomUUID().toString();

        //first check if the id already exists
        ArrayList<Employee> empList = CompanyDatabase.employeePreferenceDB;

        for(Employee e : empList){
            if(e.getEmployeeId().equals(id)){
                //duplicate id
                //generate new one
                id = generateUniqueID(employeeName);
            }
        }

        return id;
    }
}
