package com.shopeeclone.shopee_api.dto;

public class CartItemDto {
    private Long productId;
    private String name;
    private String imageUrl;
    private Double price;
    private Integer qty;

    public CartItemDto(Long productId, String name, String imageUrl, Double price, Integer qty) {
        this.productId = productId;
        this.name = name;
        this.imageUrl = imageUrl;
        this.price = price;
        this.qty = qty;
    }

    public Long getProductId() { return productId; }
    public String getName() { return name; }
    public String getImageUrl() { return imageUrl; }
    public Double getPrice() { return price; }
    public Integer getQty() { return qty; }
}