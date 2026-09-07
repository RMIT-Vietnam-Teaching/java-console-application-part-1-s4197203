package model;

import java.time.LocalDateTime;

/**
 * Represents a physical or digital insurance coverage card issued to a customer.
 * Each card is linked to a card holder and a policy owner, and tracks an expiration date.
 *
 * @author Nguyen Khanh Nguyen - s4197203
 */
public class InsuranceCard {
    private String cardNumber;
    private String cardHolderId;
    private String policyOwnerId;
    private LocalDateTime expirationDate;
    private MembershipTier membershipTier;

    public InsuranceCard(String cardNumber, String cardHolderId, String policyOwnerId, LocalDateTime expirationDate) {
        this(cardNumber, cardHolderId, policyOwnerId, expirationDate, MembershipTier.BASIC);
    }

    public InsuranceCard(String cardNumber, String cardHolderId, String policyOwnerId, LocalDateTime expirationDate, MembershipTier membershipTier) {
        this.cardNumber = cardNumber;
        this.cardHolderId = cardHolderId;
        this.policyOwnerId = policyOwnerId;
        this.expirationDate = expirationDate;
        this.membershipTier = membershipTier;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCardHolderId() {
        return cardHolderId;
    }

    public void setCardHolderId(String cardHolderId) {
        this.cardHolderId = cardHolderId;
    }

    public String getPolicyOwnerId() {
        return policyOwnerId;
    }

    public void setPolicyOwnerId(String policyOwnerId) {
        this.policyOwnerId = policyOwnerId;
    }

    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDateTime expirationDate) {
        this.expirationDate = expirationDate;
    }

    public MembershipTier getMembershipTier() {
        return membershipTier;
    }

    public void setMembershipTier(MembershipTier membershipTier) {
        this.membershipTier = membershipTier;
    }

    @Override
    public String toString() {
        return "InsuranceCard{" +
                "cardNumber='" + cardNumber + '\'' +
                ", cardHolderId='" + cardHolderId + '\'' +
                ", policyOwnerId='" + policyOwnerId + '\'' +
                ", expirationDate=" + expirationDate +
                ", membershipTier=" + membershipTier.getLabel() +
                '}';
    }
}
