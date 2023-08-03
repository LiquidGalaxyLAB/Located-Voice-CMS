package com.gsoc.vedantsingh.locatedvoicecms;

import java.util.List;

public class WikipediaResponse {
    private List<Page> pages;

    public List<Page> getPages() {
        return pages;
    }

    public void setPages(List<Page> pages) {
        this.pages = pages;
    }

    public static class Page {
        private int id;
        private String key;
        private String title;
        private String excerpt;
        private String matchedTitle;
        private String description;
        private Thumbnail thumbnail;

        // Add getters and setters for each field

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getExcerpt() {
            return excerpt;
        }

        public void setExcerpt(String excerpt) {
            this.excerpt = excerpt;
        }

        public String getMatchedTitle() {
            return matchedTitle;
        }

        public void setMatchedTitle(String matchedTitle) {
            this.matchedTitle = matchedTitle;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Thumbnail getThumbnail() {
            return thumbnail;
        }

        public void setThumbnail(Thumbnail thumbnail) {
            this.thumbnail = thumbnail;
        }
    }

    public static class Thumbnail {
        private String mimetype;
        private int width;
        private int height;
        private String duration;
        private String url;

        // Add getters and setters for each field

        public String getMimetype() {
            return mimetype;
        }

        public void setMimetype(String mimetype) {
            this.mimetype = mimetype;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public String getDuration() {
            return duration;
        }

        public void setDuration(String duration) {
            this.duration = duration;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}

