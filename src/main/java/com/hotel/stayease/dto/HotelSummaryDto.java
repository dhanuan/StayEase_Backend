package com.hotel.stayease.dto;

public class HotelSummaryDto {
    private String name;
    private String city;
    private int stars;
    private Long managerId;

    public HotelSummaryDto() {}

    public HotelSummaryDto(String name, String city, int stars, Long managerId) {
        this.name = name;
        this.city = city;
        this.stars = stars;
        this.managerId = managerId;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public int getStars() { return stars; }
    public void setStars(int stars) { this.stars = stars; }
    public Long getManagerId() { return managerId; }
    public void setManagerId(Long managerId) { this.managerId = managerId; }
}

