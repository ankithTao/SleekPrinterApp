package com.seaory.seaorydemo;

import android.graphics.Bitmap;

public class CardViewItem {
    private int id;
    private String name;

    public int getId() {
        return id;
    }
    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getShippingStatus() {
        return shippingStatus;
    }

    public void setShippingStatus(String shippingStatus) {
        this.shippingStatus = shippingStatus;
    }

    public String getPrinterUrl() {
        return printerUrl;
    }

    public void setPrinterUrl(String printerUrl) {
        this.printerUrl = printerUrl;
    }

    private String orderStatus;
    private String shippingStatus;
    private String printerUrl;
    private Bitmap bitmapImage;
    private Boolean isSelected;

    public CardViewItem(int id, String name, String orderStatus, String shippingStatus, String printerUrl, Bitmap bitmapImage, Boolean isSelected) {
        this.id = id;
        this.name = name;
        this.orderStatus = orderStatus;
        this.shippingStatus = shippingStatus;
        this.printerUrl = printerUrl;
        this.bitmapImage = bitmapImage;
        this.isSelected = isSelected;
    }

    public Bitmap getBitmapImage() {
        return bitmapImage;
    }

    public String getText() {
        return name;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
