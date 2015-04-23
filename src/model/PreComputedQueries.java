package model;

/**
 * Created by alex on 23.04.15.
 */
public enum PreComputedQueries {

    MAX_BY("Max by", "Select * FROM PERSON WHERE ROWNUM <= 10"),
    MIN_BY("Min by", "Select * FROM COMPANY WHERE ROWNUM <= 10");

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
