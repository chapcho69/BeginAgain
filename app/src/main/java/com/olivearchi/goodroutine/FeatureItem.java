package com.olivearchi.goodroutine;

public class FeatureItem {
    private String featureId;
    private int position;
    private String title;
    private String icon;
    private int color;

    public FeatureItem(String featureId, int position, String title, String icon, int color) {
        this.featureId = featureId;
        this.position = position;
        this.title = title;
        this.icon = icon;
        this.color = color;
    }

    public String getFeatureId() { return featureId; }
    public void setFeatureId(String featureId) { this.featureId = featureId; }
    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public int getColor() { return color; }
    public void setColor(int color) { this.color = color; }
}
