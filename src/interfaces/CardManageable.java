package interfaces;

import model.InsuranceCard;
import java.util.ArrayList;

public interface CardManageable {
    String addCard(InsuranceCard card);
    String updateCard(String cardNumber, String newHolderId, String newOwnerId, java.time.LocalDateTime newExpDate);
    String deleteCard(String cardNumber);
    InsuranceCard getCardByNumber(String cardNumber);
    ArrayList<InsuranceCard> getCards();
    ArrayList<InsuranceCard> getCardsByCustomerId(String customerId);
}
