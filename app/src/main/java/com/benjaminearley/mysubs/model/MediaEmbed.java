package com.benjaminearley.mysubs.model;


public class MediaEmbed {

    private String content;
    private int width;
    private boolean scrolling;
    private int height;

    /**
     * @return The content
     */
    public String getContent() {
        return content;
    }

    /**
     * @param content The content
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * @return The width
     */
    public int getWidth() {
        return width;
    }

    /**
     * @param width The width
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * @return The scrolling
     */
    public boolean isScrolling() {
        return scrolling;
    }

    /**
     * @param scrolling The scrolling
     */
    public void setScrolling(boolean scrolling) {
        this.scrolling = scrolling;
    }

    /**
     * @return The height
     */
    public int getHeight() {
        return height;
    }

    /**
     * @param height The height
     */
    public void setHeight(int height) {
        this.height = height;
    }

}
