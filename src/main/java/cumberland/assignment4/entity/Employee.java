package cumberland.assignment4.entity;

import java.util.Arrays;

public class Employee {

    private String employeeId;
    private String employeeName;
    //this will store employee preference:
    //first number will store first perference
    //second number will store second preference

    byte[][] employeePreference = new byte[7][2];
    private byte totalHoursWorked = 0;
    private byte totalDaysWorked = 0;
    //this will indicate if the employee has already worked /assigned the shift on a given day
    int[] workedOnShiftIndex = new int[7];

    public Employee(){

    }

    public Employee(String employeeName, byte[][] employeePreference) {
        this.employeeName = employeeName;
        this.employeePreference = employeePreference;
    }


    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public byte[][] getEmployeePreference() {
        return employeePreference;
    }

    public void setEmployeePreference(byte[][] employeePreference) {
        this.employeePreference = employeePreference;
    }

    public byte getTotalHoursWorked() {
        return totalHoursWorked;
    }

    public void setTotalHoursWorked(byte totalHoursWorked) {
        this.totalHoursWorked = totalHoursWorked;
    }

    public byte getTotalDaysWorked() {
        return totalDaysWorked;
    }

    public void setTotalDaysWorked(byte totalDaysWorked) {
        this.totalDaysWorked = totalDaysWorked;
    }

    @Override
    public String toString() {
        return "Employee{" +
                "employeeId='" + employeeId + '\'' +
                ", employeeName='" + employeeName + '\'' +
                ", employeePreference=" + Arrays.deepToString(employeePreference) +
                ", totalHoursWorked=" + totalHoursWorked +
                ", totalDaysWorked=" + totalDaysWorked +
                '}';
    }

    public int[] getWorkedOnShiftIndex() {
        return workedOnShiftIndex;
    }

    public void setWorkedOnShiftIndex(int i) {
        this.workedOnShiftIndex[i] = 1;
    }

    public void resetWorkedOnShiftIndex(){
        Arrays.fill(this.workedOnShiftIndex, 0);  // Reset all elements to 0
    }
}
