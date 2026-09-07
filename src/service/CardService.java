package service;

import model.*;

import java.util.ArrayList;

public class CardService {
    private ArrayList<InsuranceCard> cards;

    public CardService() {
        this.cards = new ArrayList<>();
    }

    public void setCards(ArrayList<InsuranceCard> cards) {
        this.cards = cards;
    }

    public ArrayList<InsuranceCard> getCards() {
        return new ArrayList<>(cards);
    }

    public ArrayList<InsuranceCard> getCardsByCustomerId(String customerId) {
        ArrayList<InsuranceCard> result = new ArrayList<>();
        for (InsuranceCard card : cards) {
            if (card.getCardHolderId().equals(customerId) || card.getPolicyOwnerId().equals(customerId)) {
                result.add(card);
            }
        }
        return result;
    }

    public InsuranceCard getCardByNumber(String cardNumber) {
        for (InsuranceCard card : cards) {
            if (card.getCardNumber().equals(cardNumber)) return card;
        }
        return null;
    }
}
