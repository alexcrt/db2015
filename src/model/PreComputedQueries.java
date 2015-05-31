package model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.partitioningBy;
import static java.util.stream.Collectors.toList;

/**
 * Created by alex on 23.04.15.
 */
public enum PreComputedQueries {


    //D2 queries
    NUMBER_MOVIES_PER_YEAR("Number of movies per year (tv and video movies included)", Holder.queriesD2.get(0)),
    TOP_TEN_COUNTRIES_MOST_PRODUCTION_COMPANIES("Top ten countries with most production companies", Holder.queriesD2.get(1)),
    MIN_MAX_AVERAGE_CAREER_DURATION("Min, max and average career duration", Holder.queriesD2.get(2)),
    MIN_MAX_AVERAGE_NUMBER_OF_ACTORS_IN_PRODUCTION("Min, max and average number of actors in a production.", Holder.queriesD2.get(3)),
    MIN_MAX_AVERAGE_HEIGHT_FEMALE_PERSONS("Min, max and average height of female persons", Holder.queriesD2.get(4)),
    PAIRS_PERSONS_MOVIES_BOTH_DIRECTED_AND_ACTED_MOVIE("Pairs of persons and movies where the person has both directed the movie and acted in the movie (does not include tv and video movies",
            Holder.queriesD2.get(5)),
    THREE_MOST_POPULAR_CHARACTER_NAMES("Three most popular character names", Holder.queriesD2.get(6));


    /*
    //D3 queries
    QUERY_A("Actors and actresses (and report the productions) who played in a production where they\n"+
                    "were 55 or more year older than the youngest actor/actress playing", Holder.queriesD3.get(false).get(0)),
    QUERY_B("Most productive year of actor: ", Holder.queriesD3),
    QUERY_C(),
    QUERY_D(),
    QUERY_E(),
    QUERY_F(),
    QUERY_G(),
    QUERY_H(),
    QUERY_I(),
    QUERY_J(),
    QUERY_K(),
    QUERY_L(),
    QUERY_M(),
    QUERY_N();*/

    private static class Holder {
        private static List<String> queriesD2;
        private static Map<Boolean, List<String>> queriesD3;
        static {
            try {
                queriesD2 = Stream.of(Files.lines(Paths.get("sql/asked_requests.sql")).collect(joining("\n")).split(";"))
                        .flatMap(Stream::of)
                        .collect(toList());

                queriesD3 =
                        Stream.of(Files.lines(Paths.get("sql/requests_deliverable_3.sql")).collect(joining("\n")).split(";"))
                                .flatMap(Stream::of)
                                .collect(partitioningBy(s -> s.contains("--Dynamic")));

                queriesD3.get(false).replaceAll(s -> Stream.of(s.split("\n")).filter(u -> !u.startsWith("--")).collect(joining("\n")));
                queriesD3.get(true).replaceAll(s -> Stream.of(s.split("\n")).filter(u -> !u.startsWith("--")).collect(joining("\n")));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String description;
    private String query;
    private boolean isDynamic;

    PreComputedQueries(String description, String query) {
        this.description = description;
        this.query = query;
        this.isDynamic = false;
    }

    PreComputedQueries(String description, String query, boolean isDynamic) {
        this(description, query);
        this.isDynamic = isDynamic;
    }

    public boolean isDynamic() {
        return this.isDynamic;
    }

    public String getQuery() {
        return this.query;
    }

    @Override
    public String toString() {
        return this.description;
    }
}
