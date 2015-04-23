package model;

/**
 * Created by alex on 23.04.15.
 */
public enum PreComputedQueries {

    MAX_BY("Max by", "Select * FROM *"),
    MIN_BY("Min by", "Select *2 FROM *2");

    private String description;
    private String query;

    private PreComputedQueries(String description, String query) {
        this.description = description;
        this.query = query;
    }

    public String getQuery() {
        return this.query;
    }

    @Override
    public String toString() {
        return this.description;
    }
}
