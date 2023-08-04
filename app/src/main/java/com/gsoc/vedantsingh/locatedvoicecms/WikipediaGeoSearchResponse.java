package com.gsoc.vedantsingh.locatedvoicecms;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class WikipediaGeoSearchResponse {

    @SerializedName("batchcomplete")
    private String batchComplete;

    @SerializedName("query")
    private QueryResult queryResult;

    public String getBatchComplete() {
        return batchComplete;
    }

    public void setBatchComplete(String batchComplete) {
        this.batchComplete = batchComplete;
    }

    public QueryResult getQueryResult() {
        return queryResult;
    }

    public void setQueryResult(QueryResult queryResult) {
        this.queryResult = queryResult;
    }

    public static class QueryResult {

        @SerializedName("pages")
        private Map<String, WikiPage> wikiPages;

        public Map<String, WikiPage> getWikiPages() {
            return wikiPages;
        }

        public void setWikiPages(Map<String, WikiPage> wikiPages) {
            this.wikiPages = wikiPages;
        }
    }

    public static class WikiPage {

        @SerializedName("pageid")
        private long pageId;

        @SerializedName("ns")
        private int ns;

        @SerializedName("title")
        private String title;

        @SerializedName("index")
        private int index;

        @SerializedName("coordinates")
        private Coordinates[] coordinates;

        @SerializedName("thumbnail")
        private Thumbnail thumbnail;

        @SerializedName("pageimage")
        private String pageImage;

        @SerializedName("description")
        private String description;

        @SerializedName("descriptionsource")
        private String descriptionSource;

        public long getPageId() {
            return pageId;
        }

        public void setPageId(long pageId) {
            this.pageId = pageId;
        }

        public int getNs() {
            return ns;
        }

        public void setNs(int ns) {
            this.ns = ns;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public Coordinates[] getCoordinates() {
            return coordinates;
        }

        public void setCoordinates(Coordinates[] coordinates) {
            this.coordinates = coordinates;
        }

        public Thumbnail getThumbnail() {
            return thumbnail;
        }

        public void setThumbnail(Thumbnail thumbnail) {
            this.thumbnail = thumbnail;
        }

        public String getPageImage() {
            return pageImage;
        }

        public void setPageImage(String pageImage) {
            this.pageImage = pageImage;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getDescriptionSource() {
            return descriptionSource;
        }

        public void setDescriptionSource(String descriptionSource) {
            this.descriptionSource = descriptionSource;
        }
    }

    public static class Coordinates {

        @SerializedName("lat")
        private double latitude;

        @SerializedName("lon")
        private double longitude;

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }
    }

    public static class Thumbnail {

        @SerializedName("source")
        private String source;

        @SerializedName("width")
        private int width;

        @SerializedName("height")
        private int height;

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
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
    }
}

