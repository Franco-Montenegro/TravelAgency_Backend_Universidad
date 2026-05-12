package mingeso.first.travelAgencyBackend.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

@Embeddable
@Data
public class CardDetails {
    @Column(name = "card_number", nullable = false)
    private String cardNumber;

    @Column(name = "card_expiration_date", nullable = false)
    private String expirationDate;

    @Column(name = "card_cvv", nullable = false)
    private String cvv;
}