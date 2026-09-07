package service;

import model.*;

import java.util.ArrayList;

public class ClaimService {
    private ArrayList<Claim> claims;

    public ClaimService() {
        this.claims = new ArrayList<>();
    }

    public void setClaims(ArrayList<Claim> claims) {
        this.claims = claims;
    }

    public ArrayList<Claim> getClaims() {
        return new ArrayList<>(claims);
    }

    public Claim getClaimById(String id) {
        for (Claim c : claims) {
            if (c.getId().equals(id)) return c;
        }
        return null;
    }

    public ArrayList<Claim> getClaimsByStatus(ClaimStatus status) {
        ArrayList<Claim> result = new ArrayList<>();
        for (Claim c : claims) {
            if (c.getStatus() == status) result.add(c);
        }
        return result;
    }
}
