package com.shopeeclone.shopee_api.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // private LocalDateTime orderDate;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    // private List<OrderItem> orderItems;

    @Column(name = "details_json", columnDefinition = "TEXT")
    private String detailsJson;

    @Lob
    @Column(name = "encrypted_details", columnDefinition = "bytea", nullable = false)
    private byte[] encryptedDetails;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // public LocalDateTime getOrderDate() {
    //     return orderDate;
    // }

    // public void setOrderDate(LocalDateTime orderDate) {
    //     this.orderDate = orderDate;
    // }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    // public List<OrderItem> getOrderItems() {
    //     return orderItems;
    // }

    // public void setOrderItems(List<OrderItem> orderItems) {
    //     this.orderItems = orderItems;
    // }

    public String getDetailsJson() {
        return detailsJson;
    }

    public void setDetailsJson(String detailsJson) {
        this.detailsJson = detailsJson;
    }

    public byte[] getEncryptedDetails() {
        return encryptedDetails;
    }

    public void setEncryptedDetails(byte[] encryptedDetails) {
        this.encryptedDetails = encryptedDetails;
    }
}
