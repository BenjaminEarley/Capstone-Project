package com.benjaminearley.mysubs.model;


public class SecureMedia {

    private Oembed oembed;
    private String type;

    /**
     * @return The oembed
     */
    public Oembed getOembed() {
        return oembed;
    }

    /**
     * @param oembed The oembed
     */
    public void setOembed(Oembed oembed) {
        this.oembed = oembed;
    }

    /**
     * @return The type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type The type
     */
    public void setType(String type) {
        this.type = type;
    }

}
