package com.example.droolsTest.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDate;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Customer {

    private Long id;
    private String name;
    private String email;
    private Integer age;
    private String category;
    private Double totalPurchases;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate registrationDate;

    private String country;
    private String city;
    private Boolean isPremium;
    private List<String> interests;
    private Double creditScore;
    private String riskLevel;
    private Boolean isActive;
    private Integer loyaltyPoints;

    // Campos calculados por las reglas
    private String discount;
    private String recommendation;
    private String alert;
    private Boolean eligibleForPromotion;
    private String nextAction;

    // Constructores
    public Customer() {
        this.isActive = true;
        this.isPremium = false;
        this.totalPurchases = 0.0;
        this.loyaltyPoints = 0;
        this.registrationDate = LocalDate.now();
        this.eligibleForPromotion = false;
    }

    public Customer(String name, String email, Integer age) {
        this();
        this.name = name;
        this.email = email;
        this.age = age;
    }

    // Métodos de conveniencia para las reglas
    public boolean isYoungCustomer() {
        return age != null && age < 30;
    }

    public boolean isVipCustomer() {
        return totalPurchases != null && totalPurchases > 10000;
    }

    public boolean isNewCustomer() {
        return registrationDate != null &&
                registrationDate.isAfter(LocalDate.now().minusMonths(3));
    }

    public int getDaysSinceRegistration() {
        if (registrationDate == null) return 0;
        return (int) java.time.temporal.ChronoUnit.DAYS.between(registrationDate, LocalDate.now());
    }

    public boolean isHighValueCustomer() {
        return totalPurchases != null && totalPurchases > 5000;
    }

    public boolean isLoyalCustomer() {
        return loyaltyPoints != null && loyaltyPoints > 1000;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Double getTotalPurchases() {
        return totalPurchases;
    }

    public void setTotalPurchases(Double totalPurchases) {
        this.totalPurchases = totalPurchases;
    }

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Boolean getIsPremium() {
        return isPremium;
    }

    public void setIsPremium(Boolean isPremium) {
        this.isPremium = isPremium;
    }

    public List<String> getInterests() {
        return interests;
    }

    public void setInterests(List<String> interests) {
        this.interests = interests;
    }

    public Double getCreditScore() {
        return creditScore;
    }

    public void setCreditScore(Double creditScore) {
        this.creditScore = creditScore;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Integer getLoyaltyPoints() {
        return loyaltyPoints;
    }

    public void setLoyaltyPoints(Integer loyaltyPoints) {
        this.loyaltyPoints = loyaltyPoints;
    }

    public String getDiscount() {
        return discount;
    }

    public void setDiscount(String discount) {
        this.discount = discount;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }

    public String getAlert() {
        return alert;
    }

    public void setAlert(String alert) {
        this.alert = alert;
    }

    public Boolean getEligibleForPromotion() {
        return eligibleForPromotion;
    }

    public void setEligibleForPromotion(Boolean eligibleForPromotion) {
        this.eligibleForPromotion = eligibleForPromotion;
    }

    public String getNextAction() {
        return nextAction;
    }

    public void setNextAction(String nextAction) {
        this.nextAction = nextAction;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", age=" + age +
                ", category='" + category + '\'' +
                ", totalPurchases=" + totalPurchases +
                ", isPremium=" + isPremium +
                ", discount='" + discount + '\'' +
                ", recommendation='" + recommendation + '\'' +
                ", eligibleForPromotion=" + eligibleForPromotion +
                '}';
    }
}