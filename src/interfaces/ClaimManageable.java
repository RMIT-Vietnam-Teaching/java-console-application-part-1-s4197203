package interfaces;

import model.Claim;
import model.ClaimStatus;
import java.util.ArrayList;

public interface ClaimManageable {
    String addClaim(Claim claim);
    String updateClaimStatus(String claimId, String newStatusLabel);
    String addDocumentToClaim(String claimId, String documentName);
    void deleteClaim(String claimId);
    Claim getClaimById(String claimId);
    ArrayList<Claim> getClaims();
    ArrayList<Claim> getClaimsByStatus(ClaimStatus status);
}
