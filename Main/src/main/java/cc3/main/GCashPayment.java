package cc3.main;
public class GCashPayment extends PaymentFramework {

    private double updatedBalance;
    private final Repository REPO;

    public GCashPayment(double amount, double discount, Reservation reservation, Repository repo) {
        super(amount, discount, reservation);
        this.REPO = repo;
    }

    @Override
    public boolean validatePayment(double finalAmount) {

    String fullname = reservation.getPassenger().getFullname();
    String storedPin = REPO.getGCashPin(fullname);

    if (storedPin == null) {
        System.out.println("No GCash account found.");
        return false;
    }

    java.util.Scanner scanner = new java.util.Scanner(System.in);
    
    int attempts = 3;
    boolean authenticated = false;

    while (attempts > 0) {

        System.out.print("Enter GCash PIN: ");
        String input = scanner.nextLine();

        if (!input.matches("\\d{4}")) {
            System.out.println("*INVALID INPUT! 4-digit PIN only*");
            continue;
        }

        if (input.equals(storedPin)) {
            authenticated = true;
            break;
        } else {
            attempts--;
            System.out.println("Incorrect PIN. Attempts left: " + attempts);
        }
    }

    if (!authenticated) {
        return false;
    }
    
    double discountPercent = (amount == 0) ? 0 : (discount / amount) * 100;
    double currentBalance = REPO.getGCashBalance(fullname);
    System.out.println("\nCurrent Balance: " + currentBalance);
    
    System.out.println("\n===== PAYMENT SUMMARY =====");

    System.out.printf("%-18s : P%.2f%n", "Base Fare", amount);
    System.out.printf("%-18s : P%.2f%n", "VAT (12%)", amount * VATRATE);
    System.out.printf("%-18s : P%.2f%n", 
            String.format("Discount (%.2f%%)", discountPercent), discount);

    System.out.println("----------------------------");

    System.out.printf("%-18s : P%.2f%n", "Final Total", finalAmount);

    double remainingBalance = currentBalance - finalAmount;
    System.out.println("--------------------------");

    System.out.println("\nConfirm payment?");
    System.out.println("[1] Confirm");
    System.out.println("[0] Cancel");

    System.out.print("Enter choice: ");
    String choice = scanner.nextLine();
    
    if (!choice.equals("1")) {
        System.out.println("Payment cancelled.");
        return false;
    }

    if (currentBalance < finalAmount) {
        System.out.println("Insufficient balance.");
        return false;
    }
    
    REPO.updateGCashBalance(fullname, remainingBalance);

    System.out.println("\nGCash Payment Successful!");
    System.out.println("Remaining Balance: " + remainingBalance);
     
    return true;
}

    @Override
    public void displayRemaining() {
        System.out.println("\nRemaining Balance: " + updatedBalance);
    }

    @Override
    public String getPaymentType() {
        return "GCash";
    }
}
