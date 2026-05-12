package mingeso.first.travelAgencyBackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportResponseDTO {

    private Long totalRevenue;
    private long totalBookings;
    private String topPackageName;
    private long activeUsers;

}
