package model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

/**
 * Created by alex on 23.04.15.
 */
public enum PreComputedQueries {


    NUMBER_MOVIES_PER_YEAR("Number of movies per year (tv and video movies included)", Holder.queries.get(0)),
    TOP_TEN_COUNTRIES_MOST_PRODUCTION_COMPANIES("Top ten countries with most production companies", Holder.queries.get(1)),
    MIN_MAX_AVERAGE_CAREER_DURATION("Min, max and average career duration", Holder.queries.get(2)),
    MIN_MAX_AVERAGE_NUMBER_OF_ACTORS_IN_PRODUCTION("Min, max and average number of actors in a production.", Holder.queries.get(3)),
    MIN_MAX_AVERAGE_HEIGHT_FEMALE_PERSONS("Min, max and average height of female persons", Holder.queries.get(4)),
    PAIRS_PERSONS_MOVIES_BOTH_DIRECTED_AND_ACTED_MOVIE("Pairs of persons and movies where the person has both directed the movie and acted in the movie (does not include tv and video movies",
            Holder.queries.get(5)),
    THREE_MOST_POPULAR_CHARACTER_NAMES("Three most popular character names", Holder.queries.get(6));


    private static class Holder {
        private static List<String> queries;
        static {
            try {
                queries = Stream.of(Files.lines(Paths.get("sql/asked_requests.sql")).collect(joining(" ")).split(";"))
                        .flatMap(Stream::of)
                        .collect(toList());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

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
