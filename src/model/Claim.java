package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a formal insurance claim submitted for a medical event.
 * Each claim is linked to an insured person, an insurance card, and tracks
 * associated documents, financial amount, and processing status.
 *
 * @author Nguyen Khanh Nguyen - s4197203
 */
public class Claim {
    private String id;
    private LocalDateTime claimDate;
    private String insuredPersonId;
    private String cardNumber;
    private LocalDateTime examDate;
    private List<String> documents;
    private double claimAmount;
    private ClaimStatus status;
    private String processedBy;

    /**
     * Constructs a Claim using the ClaimStatus enum.
     */
    public Claim(String id, LocalDateTime claimDate, String insuredPersonId, String cardNumber,
                 LocalDateTime examDate, double claimAmount, ClaimStatus status) {
        this.id = id;
        this.claimDate = claimDate;
        this.insuredPersonId = insuredPersonId;
        this.cardNumber = cardNumber;
        this.examDate = examDate;
        this.documents = new ArrayList<>();
        this.claimAmount = claimAmount;
        this.status = status;
    }

    /**
     * Constructs a Claim using a string status label.
     */
    public Claim(String id, LocalDateTime claimDate, String insuredPersonId, String cardNumber,
                 LocalDateTime examDate, double claimAmount, String statusLabel) {
        this(id, claimDate, insuredPersonId, cardNumber, examDate, claimAmount,
                ClaimStatus.fromLabel(statusLabel));
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDateTime getClaimDate() {
        return claimDate;
    }

    public void setClaimDate(LocalDateTime claimDate) {
        this.claimDate = claimDate;
    }

    public String getInsuredPersonId() {
        return insuredPersonId;
    }

    public void setInsuredPersonId(String insuredPersonId) {
        this.insuredPersonId = insuredPersonId;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public LocalDateTime getExamDate() {
        return examDate;
    }

    public void setExamDate(LocalDateTime examDate) {
        this.examDate = examDate;
    }

    public List<String> getDocuments() {
        return documents;
    }

    public void setDocuments(List<String> documents) {
        this.documents = documents;
    }

    public double getClaimAmount() {
        return claimAmount;
    }

    public void setClaimAmount(double claimAmount) {
        this.claimAmount = claimAmount;
    }

    public ClaimStatus getStatus() {
        return status;
    }

    public String getStatusLabel() {
        return status.getLabel();
    }

    public void setStatus(ClaimStatus status) {
        this.status = status;
    }

    public String getProcessedBy() { return processedBy; }
    public void setProcessedBy(String processedBy) { this.processedBy = processedBy; }

    public void setStatusFromLabel(String label) {
        this.status = ClaimStatus.fromLabel(label);
    }

    public boolean canProgress() {
        return status != ClaimStatus.DONE;
    }

    public ClaimStatus getNextStatus() {
        if (status == ClaimStatus.NEW) return ClaimStatus.PROCESSING;
        if (status == ClaimStatus.PROCESSING) return ClaimStatus.DONE;
        return null;
    }

    @Override
    public String toString() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return "Claim{" +
                "id='" + id + '\'' +
                ", claimDate=" + claimDate.format(fmt) +
                ", insuredPersonId='" + insuredPersonId + '\'' +
                ", cardNumber='" + cardNumber + '\'' +
                ", examDate=" + examDate.format(fmt) +
                ", documents=" + documents.size() + " file(s)" +
                ", claimAmount=$" + String.format("%.2f", claimAmount) +
                ", status=" + status.getLabel() +
                '}';
    }
}
