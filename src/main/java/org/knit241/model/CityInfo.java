package org.knit241.model;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CityInfo {
    private String city;
    private String country;
    private double latitude;
    private double longitude;
    private String timezone;
    private String localTime;
    private String utcTime;
    private String timeDescription;
}
