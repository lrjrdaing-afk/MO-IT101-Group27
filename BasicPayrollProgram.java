/*
 * BasicPayrollProgram.java
 * MotorPH Basic Payroll System - Group 27
 * Authors: Jazper Adriel Lim / John Russel Daing
 */

package com.mycompany.basicpayrollprogram;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

public class BasicPayrollProgram {

    // File paths
    static final String EMPLOYEE_FILE   = "resources/MotorPH_Employee Data - Employee Details.csv";
    static final String ATTENDANCE_FILE = "resources/MotorPH_Employee Data - Attendance Record.csv";

    // Valid login credentials
    static final String EMPLOYEE_USERNAME = "employee";
    static final String PAYROLL_USERNAME  = "payroll_staff";
    static final String COMMON_PASSWORD   = "12345";

    static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("H:mm");

    // -------------------------------------------------------------------------
    // MAIN
    // -------------------------------------------------------------------------

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Check if required CSV files exist before starting
        if (!isFileReadable(EMPLOYEE_FILE) || !isFileReadable(ATTENDANCE_FILE)) {
            System.out.println("One or more required CSV files are missing or unreadable.");
            System.out.println("Please check the resources folder and try again.");
            scanner.close();
            return;
        }

        // Read and store all attendance data once at startup.
        // This prevents re-reading the file repeatedly during payroll processing.
        Map<String, Map<String, Double>> allAttendanceHours = readAttendanceHours();

        // Login system
        System.out.println("===== MotorPH Basic Payroll System =====");
        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Enter password: ");
        String password = scanner.nextLine().trim();

        // Route to the correct menu based on role
        if (username.equals(EMPLOYEE_USERNAME) && password.equals(COMMON_PASSWORD)) {
            showEmployeeMenu(scanner);
        } else if (username.equals(PAYROLL_USERNAME) && password.equals(COMMON_PASSWORD)) {
            showPayrollStaffMenu(scanner, allAttendanceHours);
        } else {
            System.out.println("Incorrect username and/or password.");
        }

        scanner.close();
    }

    // -------------------------------------------------------------------------
    // MENUS
    // -------------------------------------------------------------------------

    // Employee menu - can only view their own basic information
    static void showEmployeeMenu(Scanner scanner) {
        while (true) {
            System.out.println("\n===== Employee Menu =====");
            System.out.println("1. View my employee information");
            System.out.println("2. Exit the program");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine().trim();

            if (choice.equals("1")) {
                System.out.print("Enter your employee number: ");
                String employeeNumber = scanner.nextLine().trim();
                displayEmployeeInfo(employeeNumber);
            } else if (choice.equals("2")) {
                System.out.println("Program terminated.");
                break;
            } else {
                System.out.println("Invalid option. Please enter 1 or 2.");
            }
        }
    }

    // Payroll staff menu - can process payroll for one or all employees
    static void showPayrollStaffMenu(Scanner scanner,
                                     Map<String, Map<String, Double>> allAttendanceHours) {
        while (true) {
            System.out.println("\n===== Payroll Staff Menu =====");
            System.out.println("1. Process Payroll");
            System.out.println("2. Exit the program");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine().trim();

            if (choice.equals("1")) {
                showProcessPayrollMenu(scanner, allAttendanceHours);
            } else if (choice.equals("2")) {
                System.out.println("Program terminated.");
                break;
            } else {
                System.out.println("Invalid option. Please enter 1 or 2.");
            }
        }
    }

    // Sub-menu for selecting single or all employee payroll processing
    static void showProcessPayrollMenu(Scanner scanner,
                                       Map<String, Map<String, Double>> allAttendanceHours) {
        while (true) {
            System.out.println("\n===== Process Payroll =====");
            System.out.println("1. Process one employee");
            System.out.println("2. Process all employees");
            System.out.println("3. Back to previous menu");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine().trim();

            if (choice.equals("1")) {
                System.out.print("Enter employee number: ");
                String employeeNumber = scanner.nextLine().trim();
                processSingleEmployee(employeeNumber, allAttendanceHours);
            } else if (choice.equals("2")) {
                processAllEmployees(allAttendanceHours);
            } else if (choice.equals("3")) {
                break;
            } else {
                System.out.println("Invalid option. Please enter 1, 2, or 3.");
            }
        }
    }

    // -------------------------------------------------------------------------
    // EMPLOYEE & PAYROLL DISPLAY
    // -------------------------------------------------------------------------

    static void displayEmployeeInfo(String employeeNumber) {
        Map<String, String[]> employees = readEmployeeData();

        if (!employees.containsKey(employeeNumber)) {
            System.out.println("Employee number does not exist.");
            return;
        }

        String[] row = employees.get(employeeNumber);
        System.out.println("\nEmployee Number : " + cleanField(row[0]));
        System.out.println("Employee Name   : " + cleanField(row[2]) + " " + cleanField(row[1]));
        System.out.println("Birthday        : " + cleanField(row[3]));
    }

    static void processSingleEmployee(String employeeNumber,
                                      Map<String, Map<String, Double>> allAttendanceHours) {
        Map<String, String[]> employees = readEmployeeData();

        if (!employees.containsKey(employeeNumber)) {
            System.out.println("Employee number does not exist.");
            return;
        }

        printPayrollForEmployee(
            employeeNumber,
            employees.get(employeeNumber),
            allAttendanceHours.get(employeeNumber)
        );
    }

    static void processAllEmployees(Map<String, Map<String, Double>> allAttendanceHours) {
        Map<String, String[]> employees = readEmployeeData();

        for (String employeeNumber : employees.keySet()) {
            printPayrollForEmployee(
                employeeNumber,
                employees.get(employeeNumber),
                allAttendanceHours.get(employeeNumber)
            );
        }
    }

    // Prints full payroll summary for one employee from June to December 2024.
    // Deductions are computed from the combined monthly gross and applied on the second cutoff.
    static void printPayrollForEmployee(String employeeNumber,
                                        String[] employeeRow,
                                        Map<String, Double> employeeHours) {
        String firstName  = cleanField(employeeRow[2]);
        String lastName   = cleanField(employeeRow[1]);
        String birthday   = cleanField(employeeRow[3]);
        double hourlyRate = parseDouble(cleanField(employeeRow[18]));

        System.out.println("\n==============================================================");
        System.out.println("Employee #    : " + employeeNumber);
        System.out.println("Employee Name : " + firstName + " " + lastName);
        System.out.println("Birthday      : " + birthday);
        System.out.println("==============================================================");

        for (int month = 6; month <= 12; month++) {
            String firstCutoffKey  = "2024-" + padMonth(month) + "-1";
            String secondCutoffKey = "2024-" + padMonth(month) + "-2";

            // Retrieve stored hours for each cutoff; default to 0 if no records found
            double firstCutoffHours  = (employeeHours != null && employeeHours.containsKey(firstCutoffKey))
                                        ? employeeHours.get(firstCutoffKey) : 0.0;
            double secondCutoffHours = (employeeHours != null && employeeHours.containsKey(secondCutoffKey))
                                        ? employeeHours.get(secondCutoffKey) : 0.0;

            double firstCutoffGross  = firstCutoffHours  * hourlyRate;
            double secondCutoffGross = secondCutoffHours * hourlyRate;

            // Compute deductions based on combined monthly gross
            double monthlyGross    = firstCutoffGross + secondCutoffGross;
            double sssDeduction    = computeSss(monthlyGross);
            double philHealth      = computePhilHealth(monthlyGross);
            double pagIbig         = computePagIbig(monthlyGross);
            double taxableIncome   = monthlyGross - sssDeduction - philHealth - pagIbig;
            double withholdingTax  = computeTax(taxableIncome);
            double totalDeductions = sssDeduction + philHealth + pagIbig + withholdingTax;

            double firstCutoffNet  = firstCutoffGross;
            double secondCutoffNet = secondCutoffGross - totalDeductions;

            int    lastDay    = YearMonth.of(2024, month).lengthOfMonth();
            String monthLabel = getMonthName(month);

            System.out.println("\nCutoff Date        : " + monthLabel + " 1 to " + monthLabel + " 15");
            System.out.println("Total Hours Worked : " + firstCutoffHours);
            System.out.println("Gross Salary       : " + firstCutoffGross);
            System.out.println("Net Salary         : " + firstCutoffNet);

            System.out.println();

            System.out.println("Cutoff Date        : " + monthLabel + " 16 to " + monthLabel + " " + lastDay);
            System.out.println("Total Hours Worked : " + secondCutoffHours);
            System.out.println("Gross Salary       : " + secondCutoffGross);
            System.out.println("SSS                : " + sssDeduction);
            System.out.println("PhilHealth         : " + philHealth);
            System.out.println("Pag-IBIG           : " + pagIbig);
            System.out.println("Withholding Tax    : " + withholdingTax);
            System.out.println("Total Deductions   : " + totalDeductions);
            System.out.println("Net Salary         : " + secondCutoffNet);
        }
    }

    // -------------------------------------------------------------------------
    // CSV READING
    // -------------------------------------------------------------------------

    static Map<String, String[]> readEmployeeData() {
        Map<String, String[]> employees = new LinkedHashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(EMPLOYEE_FILE))) {
            reader.readLine(); // Skip header row

            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = splitCsvLine(line);
                if (fields.length >= 19) {
                    employees.put(cleanField(fields[0]), fields);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading employee data: " + e.getMessage());
        }

        return employees;
    }

    // Reads the attendance CSV once and stores total hours per employee per cutoff.
    // Structure: Map<employeeNumber, Map<cutoffKey, totalHours>>
    // Cutoff key format: "YYYY-MM-1" (days 1-15) or "YYYY-MM-2" (days 16-end)
    static Map<String, Map<String, Double>> readAttendanceHours() {
        Map<String, Map<String, Double>> allAttendance = new LinkedHashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(ATTENDANCE_FILE))) {
            reader.readLine(); // Skip header row

            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                if (fields.length < 6) continue;

                String employeeNumber = cleanField(fields[0]);
                String dateText       = cleanField(fields[3]);
                String loginText      = cleanField(fields[4]);
                String logoutText     = cleanField(fields[5]);

                if (employeeNumber.isEmpty() || dateText.isEmpty()
                        || loginText.isEmpty() || logoutText.isEmpty()) continue;

                try {
                    LocalDate date       = LocalDate.parse(dateText,   DATE_FORMAT);
                    LocalTime loginTime  = LocalTime.parse(loginText,  TIME_FORMAT);
                    LocalTime logoutTime = LocalTime.parse(logoutText, TIME_FORMAT);

                    // Only process records within June-December 2024
                    int monthValue = date.getMonthValue();
                    if (date.getYear() != 2024 || monthValue < 6 || monthValue > 12) continue;

                    double hoursWorked = computeHoursWorked(loginTime, logoutTime);
                    String cutoffKey   = buildCutoffKey(date);

                    allAttendance.putIfAbsent(employeeNumber, new LinkedHashMap<>());
                    Map<String, Double> employeeCutoffs = allAttendance.get(employeeNumber);

                    // Accumulate hours into the correct cutoff bucket
                    double existingHours = employeeCutoffs.getOrDefault(cutoffKey, 0.0);
                    employeeCutoffs.put(cutoffKey, existingHours + hoursWorked);

                } catch (Exception e) {
                    // Skip rows with unparseable date or time values
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading attendance data: " + e.getMessage());
        }

        return allAttendance;
    }

    // -------------------------------------------------------------------------
    // HOURS COMPUTATION
    // -------------------------------------------------------------------------

    // Rules: grace period up to 8:10 AM counts as 8:00 AM,
    // logout capped at 5:00 PM, 1-hour lunch deducted, max 8 hours per day.
    static double computeHoursWorked(LocalTime actualLogin, LocalTime actualLogout) {
        LocalTime officialStart = LocalTime.of(8, 0);
        LocalTime officialEnd   = LocalTime.of(17, 0);
        LocalTime graceDeadline = LocalTime.of(8, 10);

        LocalTime adjustedLogout = actualLogout.isAfter(officialEnd) ? officialEnd : actualLogout;
        LocalTime adjustedLogin  = !actualLogin.isAfter(graceDeadline) ? officialStart : actualLogin;

        if (!adjustedLogout.isAfter(adjustedLogin)) return 0.0;

        double hoursWorked = Duration.between(adjustedLogin, adjustedLogout).toMinutes() / 60.0;
        hoursWorked -= 1.0; // Deduct 1-hour lunch break

        if (hoursWorked < 0.0) return 0.0;

        return Math.min(hoursWorked, 8.0); // Cap at 8 hours per day
    }

    // Returns "YYYY-MM-1" for days 1-15, "YYYY-MM-2" for days 16 to end of month
    static String buildCutoffKey(LocalDate date) {
        String prefix = date.getYear() + "-" + padMonth(date.getMonthValue());
        return prefix + (date.getDayOfMonth() <= 15 ? "-1" : "-2");
    }

    // -------------------------------------------------------------------------
    // STATUTORY DEDUCTIONS
    // -------------------------------------------------------------------------

    // SSS contribution table based on 2024 SSS contribution schedule
    static double computeSss(double monthlyGross) {
        if (monthlyGross < 3250)  return 135.0;
        if (monthlyGross < 3750)  return 157.5;
        if (monthlyGross < 4250)  return 180.0;
        if (monthlyGross < 4750)  return 202.5;
        if (monthlyGross < 5250)  return 225.0;
        if (monthlyGross < 5750)  return 247.5;
        if (monthlyGross < 6250)  return 270.0;
        if (monthlyGross < 6750)  return 292.5;
        if (monthlyGross < 7250)  return 315.0;
        if (monthlyGross < 7750)  return 337.5;
        if (monthlyGross < 8250)  return 360.0;
        if (monthlyGross < 8750)  return 382.5;
        if (monthlyGross < 9250)  return 405.0;
        if (monthlyGross < 9750)  return 427.5;
        if (monthlyGross < 10250) return 450.0;
        if (monthlyGross < 10750) return 472.5;
        if (monthlyGross < 11250) return 495.0;
        if (monthlyGross < 11750) return 517.5;
        if (monthlyGross < 12250) return 540.0;
        if (monthlyGross < 12750) return 562.5;
        if (monthlyGross < 13250) return 585.0;
        if (monthlyGross < 13750) return 607.5;
        if (monthlyGross < 14250) return 630.0;
        if (monthlyGross < 14750) return 652.5;
        if (monthlyGross < 15250) return 675.0;
        if (monthlyGross < 15750) return 697.5;
        if (monthlyGross < 16250) return 720.0;
        if (monthlyGross < 16750) return 742.5;
        if (monthlyGross < 17250) return 765.0;
        if (monthlyGross < 17750) return 787.5;
        if (monthlyGross < 18250) return 810.0;
        if (monthlyGross < 18750) return 832.5;
        if (monthlyGross < 19250) return 855.0;
        if (monthlyGross < 19750) return 877.5;
        if (monthlyGross < 20250) return 900.0;
        if (monthlyGross < 20750) return 922.5;
        if (monthlyGross < 21250) return 945.0;
        if (monthlyGross < 21750) return 967.5;
        if (monthlyGross < 22250) return 990.0;
        if (monthlyGross < 22750) return 1012.5;
        if (monthlyGross < 23250) return 1035.0;
        if (monthlyGross < 23750) return 1057.5;
        if (monthlyGross < 24250) return 1080.0;
        if (monthlyGross < 24750) return 1102.5;
        return 1125.0;
    }

    // PhilHealth: 3% of salary, employee pays half. Floor: P10,000, ceiling: P60,000.
    static double computePhilHealth(double monthlyGross) {
        double salaryBasis = Math.max(10000.0, Math.min(monthlyGross, 60000.0));
        return (salaryBasis * 0.03) / 2.0;
    }

    // Pag-IBIG: 1% for gross P1,500 and below, 2% above P1,500. Capped at P100.
    static double computePagIbig(double monthlyGross) {
        double contribution = (monthlyGross > 1500) ? monthlyGross * 0.02 : monthlyGross * 0.01;
        return Math.min(contribution, 100.0);
    }

    // Withholding tax based on 2024 BIR monthly tax table
    static double computeTax(double taxableIncome) {
        if (taxableIncome <= 20833)  return 0.0;
        if (taxableIncome < 33333)   return (taxableIncome - 20833)   * 0.20;
        if (taxableIncome < 66667)   return 2500    + (taxableIncome - 33333)  * 0.25;
        if (taxableIncome < 166667)  return 10833   + (taxableIncome - 66667)  * 0.30;
        if (taxableIncome < 666667)  return 40833.33 + (taxableIncome - 166667) * 0.32;
        return                              200833.33 + (taxableIncome - 666667) * 0.35;
    }

    // -------------------------------------------------------------------------
    // UTILITY METHODS
    // -------------------------------------------------------------------------

    static boolean isFileReadable(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    // Splits a CSV line while correctly handling commas inside quoted fields (e.g. "90,000")
    static String[] splitCsvLine(String line) {
        return line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
    }

    static String cleanField(String rawValue) {
        return rawValue.trim().replace("\"", "");
    }

    static double parseDouble(String rawValue) {
        String cleaned = cleanField(rawValue).replace(",", "");
        return cleaned.isEmpty() ? 0.0 : Double.parseDouble(cleaned);
    }

    // Pads single-digit months with a leading zero (e.g. 6 -> "06")
    static String padMonth(int month) {
        return month < 10 ? "0" + month : String.valueOf(month);
    }

    static String getMonthName(int month) {
        switch (month) {
            case 1:  return "January";
            case 2:  return "February";
            case 3:  return "March";
            case 4:  return "April";
            case 5:  return "May";
            case 6:  return "June";
            case 7:  return "July";
            case 8:  return "August";
            case 9:  return "September";
            case 10: return "October";
            case 11: return "November";
            case 12: return "December";
            default: return "";
        }
    }
}
