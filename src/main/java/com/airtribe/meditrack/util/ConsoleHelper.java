package com.airtribe.meditrack.util;

import com.airtribe.meditrack.entity.Appointment;
import com.airtribe.meditrack.entity.Bill;
import com.airtribe.meditrack.entity.Doctor;
import com.airtribe.meditrack.entity.Patient;

import java.util.List;
import java.util.Scanner;

public final class ConsoleHelper {

    private static final int PAGE_SIZE = 5;
    private static final Scanner scanner = new Scanner(System.in);

    private ConsoleHelper() {}


    public static Doctor selectDoctor(List<Doctor> doctors) {
        if (doctors.isEmpty()) {
            System.out.println("No doctors available.");
            return null;
        }
        System.out.println("\n--- Select Doctor ---");
        return paginate(doctors, item -> item.getDisplayInfo());
    }



    public static Patient selectPatient(List<Patient> patients) {
        if (patients.isEmpty()) {
            System.out.println("No patients available.");
            return null;
        }
        System.out.println("\n--- Select Patient ---");
        return paginate(patients, item -> item.getDisplayInfo());
    }



    public static Appointment selectAppointment(List<Appointment> appointments) {
        if (appointments.isEmpty()) {
            System.out.println("No appointments available.");
            return null;
        }
        System.out.println("\n--- Select Appointment ---");
        return paginate(appointments, item -> item.toString());
    }

    public static Bill selectBill(List<Bill> bills) {
        if (bills.isEmpty()) {
            System.out.println("No bills available.");
            return null;
        }
        System.out.println("\n--- Select Bill ---");
        return paginate(bills, bill -> String.format("Bill[%s] Patient: %s | Appointment: %s | Total: %.2f | Paid: %s",
                bill.getId(), bill.getPatientId(), bill.getAppointmentId(),
                bill.calculateTotal(), bill.isPaid() ? "Yes" : "No"));
    }



    private static <T> T paginate(List<T> items, DisplayFormatter<T> formatter) {
        int totalPages = (int) Math.ceil((double) items.size() / PAGE_SIZE);
        int currentPage = 0;

        while (true) {
            int start = currentPage * PAGE_SIZE;
            int end = Math.min(start + PAGE_SIZE, items.size());

            // Print current page
            for (int i = start; i < end; i++) {
                System.out.println((i - start + 1) + ". " + formatter.format(items.get(i)));
            }

            System.out.println("\n[Page " + (currentPage + 1) + " of " + totalPages + "]");

            // Navigation options
            StringBuilder nav = new StringBuilder();
            if (currentPage > 0) nav.append("P. Previous | ");
            if (currentPage < totalPages - 1) nav.append("N. Next | ");
            nav.append("Enter number to select | 0. Cancel");
            System.out.println(nav);
            System.out.print("Choice: ");

            String input = scanner.nextLine().trim().toLowerCase();

            switch (input) {
                case "n" -> {
                    if (currentPage < totalPages - 1) currentPage++;
                    else System.out.println("Already on last page.");
                }
                case "p" -> {
                    if (currentPage > 0) currentPage--;
                    else System.out.println("Already on first page.");
                }
                case "0" -> { return null; }
                default -> {
                    try {
                        int choice = Integer.parseInt(input);
                        int index = start + choice - 1;
                        if (choice >= 1 && index < end) {
                            return items.get(index);
                        }
                        System.out.println("Invalid selection.");
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid input.");
                    }
                }
            }
        }
    }


    @FunctionalInterface
    private interface DisplayFormatter<T> {
        String format(T item);
    }
}