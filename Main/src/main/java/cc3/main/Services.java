package cc3.main;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class Services {
    private final Scanner SC;
    private final Repository REPO;
    
    private Services(ServicesBuilder builder){
        this.SC = builder.sc;
        this.REPO = builder.repo;
    }
    
    public Passenger registerPassenger() {
    String name = "", pass = "", contact = "", emailAddress = "";

    while (true) {
        System.out.println("\n==========================");
        System.out.println("#  FILL-UP REGISTRATION  #");
        System.out.println("==========================");

        System.out.print("Create Username        : ");
        name = SC.nextLine();

        if (!name.matches("[a-zA-Z\\s.]+")) {
            System.out.println("\n*INVALID INPUT!* Letters only.");
            continue;
        }

        System.out.print("Create Password        : ");
            if (pass.isEmpty()) {
                pass = SC.nextLine();
            } else {
                System.out.println(pass);
            }

        System.out.print("Contact Number         : ");
        contact = SC.nextLine();

        if (!contact.matches("\\d{11}")) {
            System.out.println("\n*INVALID INPUT!* Must be 11 digits.");
            continue;
        }

        System.out.print("Email Address          : ");
        emailAddress = SC.nextLine();

        if (!emailAddress.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            System.out.println("\n*INVALID INPUT!* Invalid email.");
            continue;
        }

        String error = REPO.passengerExists(name, pass, contact, emailAddress);
        if (error != null) {
            System.out.println("\n*INVALID INPUT!* " + error);
            continue;
        }

        System.out.println("\nWould you like to save this?");
        System.out.println("[1] Save ");
        System.out.println("[0] Cancel ");
        int choice = numberAuthenticator(0,1);
        
            if (choice == 1) {

                Passenger p = new Passenger.PassengerBuilder()
                        .setFullname(name)
                        .setPassword(pass)
                        .setContactNumber(contact)
                        .setEmailAddress(emailAddress)
                        .build();

                REPO.savePassenger(p);
                return p;

            } else {
                System.out.println("\n*REGISTRATION CANCELLED!*");
                return null;
            }
         
    }
}
    
    public void setupPayment(Passenger p) {
        
        while (true) {
        
        System.out.println("\n=======================");
        System.out.println("#    PAYMENT SETUP    #");
        System.out.println("=======================");
        System.out.println("[1] Set Up GCash");
        System.out.println("[2] Set Up Card");
        System.out.println("[0] Back");
        
        int choice = numberAuthenticator(0,2);
        
        switch (choice) {
            case 1 -> setupGCash(p);
            case 2 -> setupCard(p);
            case 0 -> { return; }
            default -> System.out.println("\n*INVALID CHOICE!*");
        }
    }
}
    
    public Passenger loginPassenger() {
        System.out.println("\n=== LOGIN ===");

        System.out.print("Username: ");
        String name = SC.nextLine();

        System.out.print("Password: ");
        String password = SC.nextLine();

        Passenger p = REPO.login(name, password);

        if (p != null) {
            System.out.println("\n*LOGIN SUCCESSFUL!*");
        }else {
            System.out.println("\n*INVALID INPUT!* No existing account.");
        }

        return p;
    }
    
   public void cancelReservation(Passenger p) {

    while (true) {

        List<String[]> reservations = REPO.getReservations(p.getFullname());

        if (reservations == null || reservations.isEmpty()) {
            System.out.println("\nNo reservations to cancel. Kindly reserve first.");
            return;
        }

        System.out.println("\n==============================");
        System.out.println("#  CANCEL RESERVATION MENU  #");
        System.out.println("==============================");

        for (int i = 0; i < reservations.size(); i++) {
            System.out.println("[" + (i + 1) + "] " + reservations.get(i)[0]);
        }

        System.out.println("[0] Back");

        int choice = numberAuthenticator(0, reservations.size());

        if (choice == 0) {
            System.out.println("\nExiting cancellation menu...");
            return;
        }

        String[] selected = reservations.get(choice - 1);

        String seatLabel = "Unknown";
        try {
            int seatNumber = Integer.parseInt(selected[8]);
            seatLabel = getSeatLabel(seatNumber);
        } catch (Exception ignored) { }

        String paymentType = null;
        if (selected.length > 10) {
            paymentType = selected[10];
        }
        if (paymentType == null || paymentType.isBlank()) {
            paymentType = "Unknown";
        }

        System.out.println("\n=======================================================");
        System.out.println("Reservation Details");
        System.out.println("Ticket : " + selected[0]);
        System.out.println("Seat : " + seatLabel);
        System.out.println("Route : " + selected[4] + " -> " + selected[5]);
        System.out.println("Date : " + selected[7]);
        System.out.println("Time : " + selected[6]);
        System.out.println("Payment Type : " + paymentType);
        System.out.println("=======================================================");

        System.out.println("\nWould you like to confirm cancellation?");
        System.out.println("[1] Confirm");
        System.out.println("[0] Cancel");

        int confirm = numberAuthenticator(0, 1);

        if (confirm != 1) {
            System.out.println("\nCancellation aborted. Returning to menu...\n");
            continue;
        }

        // Parse payment amount
        double paymentAmount = 0.0;
        try {
            paymentAmount = Double.parseDouble(selected[9]);
        } catch (NumberFormatException e) {
            System.out.println("ERROR: Invalid payment amount. Cannot process refund.");
            return;
        }

            if (selected.length > 10) {
                paymentType = selected[10];
            }
            
        if (paymentType == null || paymentType.isBlank()) {
            System.out.println("ERROR: Payment type missing in reservation record.");
            return;
        }

        boolean refunded = false;

        switch (paymentType.toLowerCase()) {
            case "gcash":
                if (REPO.hasGCash(p.getFullname())) {
                    double currentBalance = REPO.getGCashBalance(p.getFullname());
                    REPO.updateGCashBalance(p.getFullname(), currentBalance + paymentAmount);
                    refunded = true;
                } else {
                    System.out.println("ERROR: No GCash account found for refund.");
                }
                break;

            case "card":
                if (REPO.hasCard(p.getFullname())) {
                    double currentCredit = REPO.getCardCredit(p.getFullname());
                    REPO.updateCardCredit(p.getFullname(), currentCredit + paymentAmount);
                    refunded = true;
                } else {
                    System.out.println("ERROR: No Card account found for refund.");
                }
                break;

            default:
                System.out.println("ERROR: Unknown payment type: " + paymentType);
        }

        if (refunded) {
            REPO.deleteReservation(selected[0]);
            System.out.println("\n*RESERVATION CANCELLED SUCCESSFULLY!*");
            System.out.printf("Refund of P%.2f credited to %s account.\n", paymentAmount, paymentType);
        } else {