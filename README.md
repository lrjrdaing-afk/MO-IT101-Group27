CP1 – MS2 Source Code
Basic Payroll Program
Program Overview

The Basic Payroll Program is a Java application that reads employee and attendance data from CSV files to compute payroll. It calculates the total hours worked per payroll cutoff period, determines the gross salary based on the employee’s hourly rate, and deducts government contributions such as SSS, PhilHealth, Pag-IBIG, and Withholding Tax to generate the employee’s net salary.

Team Details

Jazper Adriel Lim – Main developer who implemented the payroll logic, attendance processing, and salary computation.

John Russel Daing – Responsible for program testing, verifying payroll outputs, and documenting the system.

Program Details

The system reads employee information and attendance records from CSV files. The user enters an employee number, and the program retrieves the employee’s details and attendance logs.

The program calculates the total hours worked while applying rules such as the 8:10 AM grace period, 5:00 PM logout limit, lunch break deduction, and maximum of 8 work hours per day.

Working hours are grouped into two payroll cutoff periods: 1–15 and 16–end of the month. The program then computes the gross salary, subtracts government deductions (SSS, PhilHealth, Pag-IBIG, and Withholding Tax), and displays the payroll summary including hours worked, gross salary, deductions, and net salary.

