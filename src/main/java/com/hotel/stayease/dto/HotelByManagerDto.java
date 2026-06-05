package com.hotel.stayease.dto;

public class HotelByManagerDto {
    private Long hotelId;
    private String hotelName;
    private String city;

    public HotelByManagerDto() {}

    public HotelByManagerDto(Long hotelId, String hotelName, String city) {
        this.hotelId = hotelId;
        this.hotelName = hotelName;
        this.city = city;
    }

    public Long getHotelId() { return hotelId; }
    public void setHotelId(Long hotelId) { this.hotelId = hotelId; }

    public String getHotelName() { return hotelName; }
    public void setHotelName(String hotelName) { this.hotelName = hotelName; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
}

