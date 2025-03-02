import random

# Constants
DAYS = ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
SHIFTS = ["Morning", "Afternoon", "Evening"]
MIN_EMPLOYEES_PER_SHIFT = 2
MAX_DAYS_WORK = 5

class Employee:
    """
    Represents an employee with a name, shift preferences, assigned shifts, and tracking of days worked.
    """
    def __init__(self, name, preferences):
        self.name = name
        self.preferences = preferences  # Dictionary {day: [preferred shifts]}
        self.assigned_shifts = {}  # Stores assigned shifts {day: shift}
        self.days_worked = 0  # Tracks how many days an employee has worked

    def can_work(self, day):
        """Check if the employee is eligible to work on a given day."""
        return self.days_worked < MAX_DAYS_WORK and day not in self.assigned_shifts

    def assign_shift(self, day, shift):
        """Assign a shift to the employee and update the days worked."""
        self.assigned_shifts[day] = shift
        self.days_worked += 1

    def __str__(self):
        return f"{self.name} (Days Worked: {self.days_worked})"

class ScheduleManager:
    """
    Manages shift assignments for employees, ensuring constraints and handling conflicts.
    """
    def __init__(self, employees):
        self.employees = employees
        self.weekly_schedule = {day: {shift: [] for shift in SHIFTS} for day in DAYS}

    def assign_shifts(self):
        """Assign shifts to employees based on their preferences and constraints."""
        for day in DAYS:
            assigned_employees = set()  # Track employees assigned for this day
            for shift in SHIFTS:
                for emp in self.employees:
                    if emp.can_work(day) and emp.name not in assigned_employees:
                        if shift in emp.preferences.get(day, []) and len(self.weekly_schedule[day][shift]) < MIN_EMPLOYEES_PER_SHIFT:
                            emp.assign_shift(day, shift)
                            self.weekly_schedule[day][shift].append(emp)
                            assigned_employees.add(emp.name)  # Ensure no employee is assigned twice in a day

    def fill_understaffed_shifts(self):
        """Ensure every shift has at least 2 employees."""
        for day in DAYS:
            for shift in SHIFTS:
                while len(self.weekly_schedule[day][shift]) < MIN_EMPLOYEES_PER_SHIFT:
                    available_emps = [e for e in self.employees if e.can_work(day) and e.name not in [emp.name for emp in self.weekly_schedule[day][shift]]]
                    if available_emps:
                        chosen_emp = random.choice(available_emps)
                        chosen_emp.assign_shift(day, shift)
                        self.weekly_schedule[day][shift].append(chosen_emp)
                    else:
                        break  # No more employees available

    def resolve_conflicts(self):
        """Ensure no duplicate shift assignments for a given day."""
        for day in DAYS:
            assigned_employees = {}
            for shift in SHIFTS:
                for emp in self.weekly_schedule[day][shift]:
                    if emp.name in assigned_employees:
                        # Conflict: Employee is already assigned another shift on the same day
                        self.weekly_schedule[day][shift].remove(emp)
                        del emp.assigned_shifts[day]  # Remove the conflicting shift
                    else:
                        assigned_employees[emp.name] = shift  # Mark as assigned

    def print_schedule(self):
        """Output the final weekly schedule in a readable format."""
        print("\nFinal Employee Weekly Schedule:\n")
        for day in DAYS:
            print(f"{day}:")
            for shift in SHIFTS:
                assigned_emps = ", ".join(emp.name for emp in self.weekly_schedule[day][shift])
                print(f"  {shift}: {assigned_emps if assigned_emps else 'No employees assigned'}")
            print("-" * 50)

# Dummy employees with varied shift preferences
employees = [
    Employee("Alice", {"Monday": ["Morning"], "Tuesday": ["Morning"], "Wednesday": ["Evening"], "Thursday": ["Morning"], "Friday": ["Afternoon"], "Saturday": ["Morning"], "Sunday": ["Afternoon"]}),
    Employee("Bob", {"Monday": ["Afternoon"], "Tuesday": ["Evening"], "Wednesday": ["Morning"], "Thursday": ["Evening"], "Friday": ["Morning"], "Saturday": ["Afternoon"], "Sunday": ["Morning"]}),
    Employee("Charlie", {"Monday": ["Morning"], "Tuesday": ["Afternoon"], "Wednesday": ["Evening"], "Thursday": ["Morning"], "Friday": ["Afternoon"], "Saturday": ["Evening"], "Sunday": ["Morning"]}),
    Employee("David", {"Monday": ["Evening"], "Tuesday": ["Morning"], "Wednesday": ["Afternoon"], "Thursday": ["Evening"], "Friday": ["Morning"], "Saturday": ["Afternoon"], "Sunday": ["Evening"]}),
    Employee("Eve", {"Monday": ["Afternoon"], "Tuesday": ["Evening"], "Wednesday": ["Morning"], "Thursday": ["Afternoon"], "Friday": ["Evening"], "Saturday": ["Morning"], "Sunday": ["Afternoon"]}),
    Employee("Frank", {"Monday": ["Morning"], "Tuesday": ["Morning"], "Wednesday": ["Evening"], "Thursday": ["Morning"], "Friday": ["Afternoon"], "Saturday": ["Morning"], "Sunday": ["Afternoon"]}),
    Employee("Grace", {"Monday": ["Afternoon"], "Tuesday": ["Afternoon"], "Wednesday": ["Morning"], "Thursday": ["Evening"], "Friday": ["Morning"], "Saturday": ["Afternoon"], "Sunday": ["Morning"]}),
    Employee("Hank", {"Monday": ["Evening"], "Tuesday": ["Morning"], "Wednesday": ["Afternoon"], "Thursday": ["Morning"], "Friday": ["Afternoon"], "Saturday": ["Evening"], "Sunday": ["Morning"]}),
    Employee("Ivy", {"Monday": ["Morning"], "Tuesday": ["Evening"], "Wednesday": ["Morning"], "Thursday": ["Afternoon"], "Friday": ["Evening"], "Saturday": ["Morning"], "Sunday": ["Afternoon"]}),
    Employee("Jack", {"Monday": ["Afternoon"], "Tuesday": ["Evening"], "Wednesday": ["Morning"], "Thursday": ["Afternoon"], "Friday": ["Evening"], "Saturday": ["Morning"], "Sunday": ["Afternoon"]})
]

# Run the scheduling system
schedule_manager = ScheduleManager(employees)
schedule_manager.assign_shifts()
schedule_manager.fill_understaffed_shifts()
schedule_manager.resolve_conflicts()
schedule_manager.print_schedule()
