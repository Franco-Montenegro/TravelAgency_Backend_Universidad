package mingeso.first.travelAgencyBackend.entities;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardDetails {
    private String cardNumber;
    private String expirationDate;
    private String cvv;
}