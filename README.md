CP1 – MS2 Source Code
Basic Payroll Program
Program Overview

The Basic Payroll Program is a Java application that reads employee and attendance data from CSV files to compute payroll. It calculates the total hours worked per payroll cutoff period, determines the gross salary based on the employee’s hourly rate, and deducts government contributions such as SSS, PhilHealth, Pag-IBIG, and Withholding Tax to generate the employee’s net salary.

Team Details

Jazper Adriel Lim – Primarily responsible for the development and coding of the system, including the implementation of attendance management, computation of employees’ hours worked, and the resources/download file functionality. Also collaborated with the team in debugging and field testing of the program.

John Russel Daing – Assisted in the coding development of the payroll deduction module, including the implementation of statutory deductions such as SSS, PhilHealth, Pag-IBIG, and Tax. Mainly responsible for the preparation of system documentation and contributed to debugging and field testing in collaboration with the team..

Program Details

Features
Employee Information Retrieval

Reads employee data from a CSV file.

Allows the user to enter an employee number.

Displays the employee’s name, ID, and birthday if found.

Shows an error message if the employee does not exist.

Attendance Processing

Reads attendance records from a CSV file.

Filters records based on the selected employee.

Processes attendance from June to December 2024.

Groups work hours into payroll cutoff periods:

1–15

16–end of month

Work Hours Calculation

The program calculates daily working hours based on the following rules:

8:10 AM grace period for login

5:00 PM maximum logout time

1-hour lunch break deduction

Maximum of 8 working hours per day

Payroll Computation

The program calculates the gross salary using:

Gross Salary = Hours Worked × Hourly Rate

Payroll is computed for each cutoff period.

Government Deductions

The program automatically computes the following deductions:

SSS

PhilHealth

Pag-IBIG

Withholding Tax

These deductions are subtracted from the gross salary to determine the net salary.

Payroll Summary Output

The program displays a summary that includes:

Payroll cutoff period

Total hours worked

Gross salary

Government deductions

Net salary



Project Plan link : https://docs.google.com/spreadsheets/d/1HMEVUhI2O6Gfg6cjb8s-gZpOU4HTeq9a/edit?usp=sharing&ouid=111972521313348663798&rtpof=true&sd=true
