package model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Types;
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
    THREE_MOST_POPULAR_CHARACTER_NAMES("Three most popular character names", Holder.queriesD2.get(6)),



    //D3 queries
    QUERY_A("Actors and actresses (and report the productions) who played in a production where they\n"+
                    "were 55 or more year older than the youngest actor/actress playing", Holder.queriesD3.get(false).get(0)),
    QUERY_B("Most productive year of actor (dynamic): ", Holder.queriesD3.get(true).get(0), true, Types.NVARCHAR),
    QUERY_C("Company with the highest number of productions in each genre for year (dynamic): ", Holder.queriesD3.get(true).get(1), true, Types.INTEGER),
    QUERY_D("Person who worked with spouses/children/potential relatives on the same production: ", Holder.queriesD3.get(false).get(1)),
    QUERY_E("Average number of actors per production per year", Holder.queriesD3.get(false).get(2)),
    QUERY_F("Average number of episodes per season", Holder.queriesD3.get(false).get(3)),
    QUERY_G("Average number of seasons per series.", Holder.queriesD3.get(false).get(4)),
    QUERY_H("Top ten tv-series (by number of seasons).", Holder.queriesD3.get(false).get(5)),
    QUERY_I("Top ten tv-series (by number of episodes per season)", Holder.queriesD3.get(false).get(6)),
    QUERY_J("Actors, actresses and directors who have movies (including tv movies and video movies) released\n" +
            "after their death", Holder.queriesD3.get(false).get(7)),
    QUERY_K("Companies that released the most movies for each year", Holder.queriesD3.get(false).get(8)),
    QUERY_L("Living people who are opera singers ordered from youngest to oldest", Holder.queriesD3.get(false).get(9)),
    QUERY_M("10 most ambiguous credits (pairs of people and productions) ordered by the degree of ambiguity.", Holder.queriesD3.get(false).get(10)),
    QUERY_N("Most frequent character name that appears in the productions of a\n" +
            "production company (not a distributor) per country", Holder.queriesD3.get(false).get(11));

    private static class Holder {
        private static List<String> queriesD2;
        private static Map<Boolean, List<String>> queriesD3;
        static {
            try {
                queriesD2 = Stream.of(Files.lines(Paths.get("sql/asked_requests.sql")).collect(joining("\n")).split(";"))
                        .flatMap(Stream::of)
                        .map(String::trim)
                        .collect(toList());

                queriesD3 =
                        Stream.of(Files.lines(Paths.get("sql/requests_deliverable_3.sql")).collect(joining("\n")).split(";"))
                                .flatMap(Stream::of)
                                .map(String::trim)
                                .collect(partitioningBy(s -> s.contains("--Dynamic")));

                queriesD3.get(false).replaceAll(s -> Stream.of(s.split("\n")).filter(u -> !u.startsWith("--") || u.isEmpty()).collect(joining("\n")));
                queriesD3.get(true).replaceAll(s -> Stream.of(s.split("\n")).filter(u -> !u.startsWith("--") || u.isEmpty()).collect(joining("\n")));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String description;
    private String query;
    private boolean isDynamic;
    private Integer sqlType;

    PreComputedQueries(String description, String query) {
        this.description = description;
        this.query = query;
        this.isDynamic = false;
        this.sqlType = null;
    }

    PreComputedQueries(String description, String query, boolean isDynamic, int sqlType) {
        this(description, query);
        this.isDynamic = isDynamic;
        this.sqlType = sqlType;
    }

    public int getSqlType() {
        if(!isDynamic) {
            throw new IllegalStateException("Should not query a sql type for a non-dynamic query");
        }
        return this.sqlType;
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
