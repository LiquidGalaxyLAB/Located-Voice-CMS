package com.gsoc.vedantsingh.locatedvoicecms;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class WikipediaCoordinatesResponse {

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

        @SerializedName("coordinates")
        private Coordinates[] coordinates;

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

        public Coordinates[] getCoordinates() {
            return coordinates;
        }

        public void setCoordinates(Coordinates[] coordinates) {
            this.coordinates = coordinates;
        }
    }

    public static class Coordinates {

        @SerializedName("lat")
        private double latitude;

        @SerializedName("lon")
        private double longitude;

        @SerializedName("primary")
        private String primary;

        @SerializedName("globe")
        private String globe;

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

        public String getPrimary() {
            return primary;
        }

        public void setPrimary(String primary) {
            this.primary = primary;
        }

        public String getGlobe() {
            return globe;
        }

        public void setGlobe(String globe) {
            this.globe = globe;
        }
    }
}
