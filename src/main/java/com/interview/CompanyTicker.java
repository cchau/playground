package com.interview;

public class CompanyTicker {

    private String companyName;
    private int price;

    public CompanyTicker(String companyName, int price) {
        this.companyName = companyName;
        this.price = price;
    }

    public String getCompanyName() {
        return companyName;
    }

    public int getPrice() {
        return price;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public CompanyTicker clone() {
        return new CompanyTicker(this.companyName, this.price);
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof CompanyTicker))
            return false;
        CompanyTicker companyTicker = (CompanyTicker) obj;
        return companyTicker.getCompanyName().equals(companyName) && companyTicker.getPrice() == getPrice();
    }

    @Override
    public String toString() {
        return new StringBuilder(companyName).append(" ").append(price).toString();
    }
}
