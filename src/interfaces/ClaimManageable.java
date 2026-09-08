package interfaces;

import exceptions.InvalidStatusTransitionException;
import model.Claim;
import model.ClaimStatus;
import java.util.ArrayList;

/**
 * Interface defining contract for claim management operations.
 *
 * @author Nguyen Khanh Nguyen - s4197203
 */
public interface ClaimManageable {
    String addClaim(Claim claim);
    String updateClaimStatus(String claimId, String newStatusLabel, String processedBy) throws InvalidStatusTransitionException;
    String addDocumentToClaim(String claimId, String documentName);
    void deleteClaim(String claimId);
    Claim getClaimById(String claimId);
    ArrayList<Claim> getClaims();
    ArrayList<Claim> getClaimsByStatus(ClaimStatus status);
}
