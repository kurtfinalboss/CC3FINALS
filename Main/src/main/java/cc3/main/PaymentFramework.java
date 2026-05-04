package cc3.main;
import java.util.Scanner;

public abstract class PaymentFramework {

    protected double amount;
    protected double discount;
    protected final double VATRATE = 0.12;
    protected Reservation reservation;

    public PaymentFramework(double amount, double discount, Reservation reservation) {
        this.amount = amount;
        this.discount = discount;
        this.reservation = reservation;
    }
    
    public abstract boolean validatePayment(double finalAmount);

    public double applyVAT(double amount) {
        return amount + (amount * VATRATE);
    }

    public double applyDiscount(double amount) {
        return amount - discount;
    }

    public double processInvoice() {
        System.out.println("\nProcessing invoice...");

        double total = applyVAT(amount);
        total = applyDiscount(total);

        if (!validatePayment(total)) {
            System.out.println("\nPayment validation failed.");
            return -1;
        }

        finalizeTransaction(total);
        return total;
    }
    public void finalizeTransaction(double finalAmount) {
        
        System.out.println("\n===== TRANSACTION RECEIPT =====");
        System.out.println("Payment Type  : " + getPaymentType());
        System.out.println("Status          : SUCCESS");
        System.out.println("================================");
    }
    public abstract void displayRemaining();

    public abstract String getPaymentType();
}