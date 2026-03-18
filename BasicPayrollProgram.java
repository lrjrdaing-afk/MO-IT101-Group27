/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.mycompany.basicpayrollprogram;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.Duration;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

/**
 *Basic Payroll Program
 * This program reads employee and attendance CSV files,
 * calculates gross and net salary per payroll cutoff,
 * and prints payroll details for each month.
 * @author Jazper
 */
public class BasicPayrollProgram {

    public static void main(String[] args) {
        // File paths for employee and attendance CSV
        String empFile = "resources/MotorPH_Employee Data - Employee Details.csv";
        String attFile = "resources/MotorPH_Employee Data - Attendance Record.csv";

        Scanner sc = new Scanner(System.in);

        // Ask user to input employee number
        System.out.print("Enter Employee #: ");
        String inputEmpNo = sc.nextLine();

        // Variables to store employee info
        String empNo = "";
        String firstName = "";
        String lastName = "";
        String birthday = "";
        double hourlyRate = 0;
        boolean found = false;

        
        // 1. Read Employee Details CSV
      
        try (BufferedReader br = new BufferedReader(new FileReader(empFile))) {

            br.readLine(); // Skip header
            String line;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue; // Skip empty lines

                // Split CSV but ignore commas inside quotes
                String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

                // Check if employee number matches input
                if (data[0].equals(inputEmpNo)) {
                    empNo = data[0];
                    lastName = data[1];
                    firstName = data[2];
                    birthday = data[3];
                    hourlyRate = Double.parseDouble(data[18]);
                    found = true;
                    break; // Stop after finding employee
                }
            }

        } catch (Exception e) {
            System.out.println("Error reading employee file: " + e);
            return;
        }

        // If employee not found, exit program
        if (!found) {
            System.out.println("Employee does not exist.");
            return;
        }

        
        // 2. Display Employee Info
      
        System.out.println("\n===================================");
        System.out.println("Employee # : " + empNo);
        System.out.println("Employee Name : " + lastName + ", " + firstName);
        System.out.println("Birthday : " + birthday);
        System.out.println("===================================");

        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("H:mm");

        
        // 3. Process Attendance & Payroll
        // Loop through months June to December 2024
        
        for (int month = 6; month <= 12; month++) {
            double firstHalfHours = 0;
            double secondHalfHours = 0;
            int daysInMonth = YearMonth.of(2024, month).lengthOfMonth();

            // Read Attendance CSV
            try (BufferedReader br = new BufferedReader(new FileReader(attFile))) {

                br.readLine(); // Skip header
                String line;

                while ((line = br.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;

                    String[] data = line.split(",");

                    if (!data[0].equals(empNo)) continue; // Skip other employees

                    // Parse attendance date MM/DD/YYYY
                    String[] dateParts = data[3].split("/");
                    int recordMonth = Integer.parseInt(dateParts[0]);
                    int day = Integer.parseInt(dateParts[1]);
                    int year = Integer.parseInt(dateParts[2]);

                    // Only process matching year and month
                    if (year != 2024 || recordMonth != month) continue;

                    // Parse login and logout times
                    LocalTime login = LocalTime.parse(data[4].trim(), timeFormat);
                    LocalTime logout = LocalTime.parse(data[5].trim(), timeFormat);

                    // Compute hours worked for the day
                    double hoursWorked = computeHours(login, logout);

                    // Add hours to first or second half of month
                    if (day <= 15) {
                        firstHalfHours += hoursWorked;
                    } else {
                        secondHalfHours += hoursWorked;
                    }
                }

            } catch (Exception e) {
                System.out.println("Error reading attendance file for month " + month);
                e.printStackTrace();
                continue;
            }

            // Convert month number to name
            String monthName = getMonthName(month);

       
            // 4. Calculate First Half Payroll
            
            double firstHalfGross = firstHalfHours * hourlyRate;
            double firstHalfNet = firstHalfGross; // No deductions for first half display
            System.out.println("\nCutoff Date: " + monthName + " 1 to 15");
            System.out.println("Total Hours Worked : " + firstHalfHours);
            System.out.println("Gross Salary: " + firstHalfGross);
            System.out.println("Net Salary: " + firstHalfNet);

            
            // 5. Calculate Second Half Payroll with deductions
            
            double secondHalfGross = secondHalfHours * hourlyRate;
            double totalGrossIncome = firstHalfGross + secondHalfGross;

            // Compute contributions and tax
            double sss = computeSss(totalGrossIncome);
            double philHealth = computePhilHealth(totalGrossIncome);
            double pagIbig = computePagIbig(totalGrossIncome);
            double totalContribution = sss + philHealth + pagIbig;
            double tax = computeTax(totalGrossIncome - totalContribution);
            double secondHalfNet = secondHalfGross - (totalContribution + tax);

            System.out.println("\nCutoff Date: " + monthName + " 16 to " + daysInMonth);
            System.out.println("Total Hours Worked : " + secondHalfHours);
            System.out.println("Gross Salary: " + secondHalfGross);
            System.out.println("Net Salary: " + secondHalfNet);
            System.out.println("Deductions: ");
            System.out.println("    SSS: " + sss);
            System.out.println("    PhilHealth: " + philHealth);
            System.out.println("    Pag-IBIG: " + pagIbig);
            System.out.println("    Tax: " + tax);
        }
    }

    
    // Function to compute total hours worked in a day

    static double computeHours(LocalTime login, LocalTime logout) {
        LocalTime graceTime = LocalTime.of(8, 10); // Max hours if before grace
        LocalTime cutoffTime = LocalTime.of(17, 0); // Latest logout time

        if (logout.isAfter(cutoffTime)) logout = cutoffTime;

        long minutesWorked = Duration.between(login, logout).toMinutes();

        // Deduct 1 hour lunch if more than 1 hour worked
        if (minutesWorked > 60) {
            minutesWorked -= 60;
        } else {
            minutesWorked = 0;
        }

        double hours = minutesWorked / 60.0;

        // If login before or at grace time, assign full 8 hours
        if (!login.isAfter(graceTime)) return 8.0;

        // Cap daily hours to 8
        return Math.min(hours, 8.0);
    }

    
    // Function to compute SSS deduction
   
    static double computeSss(double grossIncome) {
        double sssDeduction = 0.0;
        if (grossIncome < 3250) sssDeduction = 135;
        else if (grossIncome >= 24750) sssDeduction = 1125;
        else {
            int minSalary = 3250, maxSalary = 3750;
            double contribution = 157.50;
            while (contribution < 1125) {
                if (grossIncome >= minSalary && grossIncome < maxSalary) return contribution;
                minSalary += 500;
                maxSalary += 500;
                contribution += 22.50;
            }
            sssDeduction = contribution;
        }
        return sssDeduction;
    }

  
    // Function to compute PhilHealth deduction
    
    static double computePhilHealth(double grossIncome) {
        if (grossIncome <= 10000) return 300 / 2.0;
        else if (grossIncome < 60000) return (grossIncome * 0.03) / 2.0;
        else return 1800 / 2.0;
    }

    
    // Function to compute Pag-IBIG deduction using ternary
    
    static double computePagIbig(double grossIncome) {
        double contribution = (grossIncome >= 1500) ? grossIncome * 0.02
                            : (grossIncome >= 1000) ? grossIncome * 0.01 : 0.0;
        return (contribution >= 100) ? 100 : contribution;
    }

    
    // Function to compute tax deduction
   
    static double computeTax(double taxableIncome) {
        if (taxableIncome <= 20832) return 0;
        else if (taxableIncome < 33333) return (taxableIncome - 20833) * 0.2;
        else if (taxableIncome < 66667) return ((taxableIncome - 33333) * 0.25) + 2500;
        else if (taxableIncome < 166667) return ((taxableIncome - 66667) * 0.3) + 10833;
        else if (taxableIncome < 666667) return ((taxableIncome - 166667) * 0.32) + 40833.33;
        else return ((taxableIncome - 666667) * 0.35) + 200833.33;
    }

    
    // Function to convert month number to month name
    
    static String getMonthName(int month) {
        return switch (month) {
            case 6 -> "June";
            case 7 -> "July";
            case 8 -> "August";
            case 9 -> "September";
            case 10 -> "October";
            case 11 -> "November";
            case 12 -> "December";
            default -> "Month " + month;
        };
    }
}
        // Return hours worked, capped at 8
        return Math.min(hours, 8.0);
    }
}
