package model;

import java.time.LocalDateTime;

/**
 * Represents a physical or digital insurance coverage card issued to a customer.
 * Each card is linked to a card holder and a policy owner.
 * Membership tier is now derived from the customer's total approved claim spending.
 *
 * @author Nguyen Khanh Nguyen - s4197203
 */
public class InsuranceCard {
    private String cardNumber;
    private String cardHolderId;
    private String policyOwnerId;
    private LocalDateTime expirationDate;

    public InsuranceCard(String cardNumber, String cardHolderId, String policyOwnerId, LocalDateTime expirationDate) {
        this.cardNumber = cardNumber;
        this.cardHolderId = cardHolderId;
        this.policyOwnerId = policyOwnerId;
        this.expirationDate = expirationDate;
    }

    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }

    public String getCardHolderId() { return cardHolderId; }
    public void setCardHolderId(String cardHolderId) { this.cardHolderId = cardHolderId; }

    public String getPolicyOwnerId() { return policyOwnerId; }
    public void setPolicyOwnerId(String policyOwnerId) { this.policyOwnerId = policyOwnerId; }

    public LocalDateTime getExpirationDate() { return expirationDate; }
    public void setExpirationDate(LocalDateTime expirationDate) { this.expirationDate = expirationDate; }

    @Override
    public String toString() {
        return "InsuranceCard{" +
                "cardNumber='" + cardNumber + '\'' +
                ", cardHolderId='" + cardHolderId + '\'' +
                ", policyOwnerId='" + policyOwnerId + '\'' +
                ", expirationDate=" + expirationDate +
                '}';
    }
}
