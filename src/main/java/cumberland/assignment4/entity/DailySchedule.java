package cumberland.assignment4.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class DailySchedule {

    // first index will be the shift
    // second index will be all the employees in that shift
    public ArrayList<Map<String, Employee>> shifts;

    public DailySchedule() {
        // Initialize with empty HashMaps instead of null
        shifts = new ArrayList<>(Arrays.asList(new HashMap<>(), new HashMap<>(), new HashMap<>()));
    }


}
