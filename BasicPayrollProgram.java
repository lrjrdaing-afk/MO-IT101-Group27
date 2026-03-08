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
 *
 * @author Jazper
 */
public class BasicPayrollProgram {

    public static void main(String[] args) {
        String empFile = "resources/MotorPH_Employee Data - Employee Details.csv";
        String attFile = "resources/MotorPH_Employee Data - Attendance Record.csv";

        Scanner sc = new Scanner(System.in);

        System.out.print("Enter Employee #: ");
        String inputEmpNo = sc.nextLine();
        String empNo = "";
        String firstName = "";
        String lastName = "";
        String birthday = "";
        double hourlyRate = 0;
        boolean found = false;

        // Read Employee Details CSV
        try (BufferedReader br = new BufferedReader(new FileReader(empFile))) {

            br.readLine(); // Skip Header
            String line;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                // IF WE ONLY USE "," it will break the csv because of this kind of value i.e "90,000" 
                // This regex skips the split if the comma is enclosed by double quote
                String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

                if (data[0].equals(inputEmpNo)) {
                    empNo = data[0];
                    lastName = data[1];
                    firstName = data[2];
                    birthday = data[3];
                    hourlyRate = Double.parseDouble(data[18]);
                    found = true;
                    break;
                }
            }

        } catch (Exception e) {
            System.out.println("Error reading employee file." + e);
            return;
        }

        if (!found) {
            System.out.println("Employee does not exist.");
            return;
        }

        System.out.println("\n===================================");
        System.out.println("Employee # : " + empNo);
        System.out.println("Employee Name : " + lastName + ", " + firstName);
        System.out.println("Birthday : " + birthday);
        System.out.println("===================================");

        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("H:mm");

        // Read Attendance Records CSV
        // Nested loop: month ---> cutoff (1-15, 16-end-of-month)
        for (int month = 6; month <= 12; month++) { // June to December 2024
            double firstHalf = 0;
            double secondHalf = 0;
            int daysInMonth = YearMonth.of(2024, month).lengthOfMonth();

            try (BufferedReader br = new BufferedReader(new FileReader(attFile))) {

                br.readLine(); // Skip Header
                String line;

                while ((line = br.readLine()) != null) {
                    if (line.trim().isEmpty()) {
                        continue;
                    }

                    String[] data = line.split(",");

                    if (!data[0].equals(empNo)) {
                        continue;
                    }

                    String[] dateParts = data[3].split("/");
                    int recordMonth = Integer.parseInt(dateParts[0]);
                    int day = Integer.parseInt(dateParts[1]);
                    int year = Integer.parseInt(dateParts[2]);

                    if (year != 2024 || recordMonth != month) {
                        continue;
                    }

                    LocalTime login = LocalTime.parse(data[4].trim(), timeFormat);
                    LocalTime logout = LocalTime.parse(data[5].trim(), timeFormat);

                    double hours = computeHours(login, logout);

                    if (day <= 15) {
                        firstHalf += hours;
                    } else {
                        secondHalf += hours;
                    }
                }

            } catch (Exception e) {
                System.out.println("Error reading attendance file for month " + month);
                e.printStackTrace();
                continue;
            }

            String monthName = switch (month) {
                case 6 ->
                    "June";
                case 7 ->
                    "July";
                case 8 ->
                    "August";
                case 9 ->
                    "September";
                case 10 ->
                    "October";
                case 11 ->
                    "November";
                case 12 ->
                    "December";
                default ->
                    "Month " + month;
            };

            double firstHalfGross = (firstHalf * hourlyRate);

            System.out.println("\nCutoff Date: " + monthName + " 1 to 15");
            System.out.println("Total Hours Worked : " + firstHalf);
            System.out.println("Gross Salary: " + firstHalfGross);
            System.out.println("Net Salary: " + firstHalfGross);

            double secondHalfGross = secondHalf * hourlyRate;
            double grossIncome = (firstHalfGross + secondHalfGross);
            double sss = computeSss(grossIncome);       // Example SSS rate
            double philHealth = computePhilHealth(grossIncome); // Example PhilHealth rate
            double pagIbig = computePagIbig(grossIncome);   // Example Pag-IBIG rate
            double totalContribution = sss + philHealth + pagIbig;
            double tax = computeTax(grossIncome-totalContribution);       // Example Tax rate
            double deduct = totalContribution + tax;
            double secondHalfNetSalary = secondHalfGross - deduct;
            System.out.println("\nCutoff Date: " + monthName + " 16 to " + daysInMonth);
            System.out.println("Total Hours Worked : " + secondHalf);
            System.out.println("Gross Salary: " + secondHalfGross);
            System.out.println("Net Salary: " + secondHalfNetSalary);
            System.out.println("Deductions: ");
            System.out.println("    SSS: " + sss);
            System.out.println("    PhilHealth: " + philHealth);
            System.out.println("    Pag-IBIG: " + pagIbig);
            System.out.println("    Tax: " + tax);
        }
    }

    static double computeSss(double grossIncome) {
        double sssDeduction = 0.00;
        if (grossIncome < 3250) {
            sssDeduction = 135;
        } else if (grossIncome >= 24750) {
            sssDeduction = 1125.00;
        } else {
            int minSalary = 3250,
                    maxSalary = 3750;
            double contribution = 157.50;
            sssDeduction = contribution;
            while (contribution < 1125) {
                if (grossIncome >= minSalary && grossIncome < maxSalary) {
                    return sssDeduction;
                } else {
                    minSalary += 500;
                    maxSalary += 500;
                    contribution += 22.50;
                }
            }
            sssDeduction = contribution;
            return sssDeduction;
        }

        return sssDeduction;
    }
// compute philhealth
    static double computePhilHealth(double grossIncome) {
        double philHealthDeduction = 0;
        // 0.03 = premium rate
        // 2 = employee share
        if (grossIncome <= 10_000) {
            philHealthDeduction = 300 / 2;
        } else if (grossIncome > 10_000 && grossIncome < 60_000) {
            philHealthDeduction = (grossIncome * 0.03) / 2;
        } else {
            philHealthDeduction = 1_800 / 2;
        }

        return philHealthDeduction;
    }

    static double computeTax(double grossIncome) {
        double taxDeduction = 0;
        if (grossIncome <= 20832) {
            taxDeduction = 0;
        } else if (grossIncome < 33333) {
            taxDeduction = (grossIncome - 20833) * 0.2;
        } else if (grossIncome < 66667) {
            taxDeduction = ((grossIncome - 33333) * 0.25) + 2500;
        } else if (grossIncome < 166667) {
            taxDeduction = ((grossIncome - 66667) * 0.3) + 10833;
        } else if (grossIncome < 666667) {
            taxDeduction = ((grossIncome - 166667) * 0.32) + 40833.33;
        } else {
            taxDeduction = ((grossIncome - 666667) * 0.35) + 200833.33;
        }
        return taxDeduction;
    }
// calculate pagibig

    static double computePagIbig(double grossIncome) {
        double contribution = grossIncome >= 1_500 ? grossIncome * 0.02f
                : (grossIncome >= 1_000) ? grossIncome * 0.01f
                        : 0.00f;

        return (contribution >= 100) ? 100 : contribution;

    }

    // Calculate Hours Worked
    static double computeHours(LocalTime login, LocalTime logout) {

        LocalTime graceTime = LocalTime.of(8, 10);
        LocalTime cutoffTime = LocalTime.of(17, 0);

        // Apply 17:00 cutoff
        if (logout.isAfter(cutoffTime)) {
            logout = cutoffTime;
        }

        long minutesWorked = Duration.between(login, logout).toMinutes();

        // Deduct lunch (if total worked is more than 1 hour)
        if (minutesWorked > 60) {
            minutesWorked -= 60;
        } else {
            minutesWorked = 0;
        }

        double hours = minutesWorked / 60.0;

        // Grace period rule
        if (!login.isAfter(graceTime)) {
            return 8.0;
        }

        // Return hours worked, capped at 8
        return Math.min(hours, 8.0);
    }
}
