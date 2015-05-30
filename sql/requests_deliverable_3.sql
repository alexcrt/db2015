

-- Query b). Quite complex but if multiple years are the most productives, it returns them all.
-- WARNING : The clause of P.name LIKE should be dynamic in app, so user can choose Person name.
SELECT DISTINCT res2.y FROM (
  SELECT res1.y AS y, rank() OVER (ORDER BY res1.year_count DESC) AS ranking
  FROM (SELECT Prod.production_year AS y, 
        COUNT(*) OVER (PARTITION BY Prod.production_year) AS year_count
        FROM PRODUCTION_CAST Pc, PRODUCTION Prod, PERSON P
        WHERE P.name LIKE '%Irisipau' AND Pc.person_id = P.id AND Pc.production_id = Prod.id) res1
) res2
WHERE res2.ranking = 1


--Query c) Again here, if two companies have the same number of production in a year and a genre and they are the most productive this genre for this year
--both companies are returned for that genre.
--WARNING: same as previously but with production_year.
SELECT * 
FROM (SELECT res1.cname, res1.pgenre, RANK() OVER (PARTITION BY res1.pgenre ORDER BY res1.cnt DESC) AS cnt2
      FROM (SELECT DISTINCT C.name AS cname, Prod.genre AS pgenre, COUNT(*) OVER (PARTITION BY C.name, Prod.genre) AS cnt
            FROM PRODUCTION Prod, COMPANY C, PRODUCTION_COMPANY Pc
            WHERE Prod.production_year = 1992 AND Prod.genre IS NOT NULL AND Pc.production_id = Prod.id AND Pc.company_id = C.id) res1) res2
WHERE res2.cnt2 = 1


--Query d). A variant of what was asked where the user enters a name (or part of it)
--and the system gets everyone who has this name and worked with relatives in the same production.
WITH res1  AS (
  SELECT P.name AS pname, Pc.production_id AS prod_id
  FROM PERSON P, PRODUCTION_CAST Pc
  WHERE P.name LIKE 'Freeman%' AND Pc.person_id = P.id)
SELECT DISTINCT a.pname
FROM res1 a INNER JOIN res1 b 
ON a.prod_id = b.prod_id AND a.pname NOT LIKE b.pname


--Query e).
WITH n_prod_by_year AS
(SELECT DISTINCT Prod.production_year AS pyear, COUNT(*) OVER (PARTITION BY Prod.production_year) AS prod_cnt
  FROM PRODUCTION Prod
  WHERE Prod.production_year IS NOT NULL
),
n_actor_by_year AS
(SELECT DISTINCT res1.pyear AS pyear, COUNT(*) OVER (PARTITION BY res1.pyear) AS actor_cnt
  FROM (SELECT DISTINCT Prod.id AS pid, Prod.production_year AS pyear, Pc.person_id
        FROM PRODUCTION Prod, PRODUCTION_CAST Pc
        WHERE Prod.production_year IS NOT NULL AND Pc.role LIKE 'act%' AND Pc.production_id = Prod.id) res1
)
SELECT n_actor_by_year.pyear, (n_actor_by_year.actor_cnt / n_prod_by_year.prod_cnt)
FROM n_actor_by_year, n_prod_by_year
WHERE n_actor_by_year.pyear = n_prod_by_year.pyear
ORDER BY n_actor_by_year.pyear


--Query f).
--Returns series_id, application should get the names from the id by another request.
WITH cnt AS
(SELECT DISTINCT Prod.series_id AS sid, 
  COUNT(DISTINCT Prod.season_number) OVER (PARTITION BY Prod.series_id) AS season_cnt, 
  COUNT(*) OVER (PARTITION BY Prod.series_id) AS episode_cnt
  FROM PRODUCTION Prod
  WHERE Prod.kind LIKE 'episode')
SELECT (sum(cnt.episode_cnt) / sum(cnt.season_cnt))
FROM cnt


--Query g)
--Here we take the max of the season as before because not every season is in the DB, but if there is
--for example if there are season 5,6,9 in the database for a serie, even if its not in the DB there has
--to be a season 7 and a season 8, so we just take the highest season number for each show

--Returns series_id, application should get the names from the id by another request.
WITH cnt AS
(SELECT DISTINCT Prod.series_id AS sid,
  MAX(Prod.season_number) OVER (PARTITION BY Prod.series_id) AS season_cnt
  FROM PRODUCTION Prod
  WHERE Prod.kind LIKE 'episode')
SELECT AVG(cnt.season_cnt)
FROM cnt


--Query h)
--Returns series_id, application should get the names from the id by another request.
SELECT res.sid
FROM (WITH cnt AS
        (SELECT DISTINCT Prod.series_id AS sid, 
          COUNT(DISTINCT Prod.season_number) OVER (PARTITION BY Prod.series_id) AS season_cnt
          FROM PRODUCTION Prod
          WHERE Prod.kind LIKE 'episode')
      SELECT cnt.sid AS sid, rank() OVER (ORDER BY cnt.season_cnt DESC) AS ranking
      FROM cnt) res
ORDER BY res.ranking OFFSET 0 ROWS FETCH NEXT 10 ROWS ONLY


--Query i)
--Same as before concerning the app : Returns series_id, application should get the names from the id by another request.
SELECT res2.sid
FROM (SELECT res.sid, rank() OVER (ORDER BY res.n_episodes_per_seasons DESC) AS ranking
      FROM (SELECT cnt.sid AS sid,(cnt.episode_cnt / cnt.season_cnt) AS n_episodes_per_seasons
            FROM (SELECT DISTINCT Prod.series_id AS sid, 
                  COUNT(DISTINCT Prod.season_number) OVER (PARTITION BY Prod.series_id) AS season_cnt, 
                  COUNT(*) OVER (PARTITION BY Prod.series_id) AS episode_cnt
                  FROM PRODUCTION Prod
                  WHERE Prod.kind LIKE 'episode') cnt
      ) res
) res2
ORDER BY res2.ranking OFFSET 0 ROWS FETCH NEXT 10 ROWS ONLY

--Query k)
-- not tv movies or video movies?
SELECT *
FROM (SELECT CompanyMoviesPerYear.*, RANK() OVER (PARTITION BY prodYear ORDER BY compCount DESC) as rn
      FROM (SELECT prodYear, compId, COUNT(*) AS compCount
            FROM (SELECT DISTINCT Prod.id AS prodId, Prod.production_year AS prodYear, Pcomp.company_id AS compId
                  FROM PRODUCTION Prod, PRODUCTION_COMPANY Pcomp
                  WHERE Prod.production_year IS NOT NULL AND Pcomp.production_id = Prod.id AND Prod.kind LIKE 'movie') res1
            GROUP BY compId, prodYear) CompanyMoviesPerYear
      )
WHERE rn = 1 OR rn =  2 OR rn = 3

--Query m)
--Returns production_id and person_id, application should get the names from the id by another request.
WITH altName AS
( SELECT DISTINCT An.person_id AS peId,
    COUNT(*) OVER (PARTITION BY An.person_id) AS cntName
    FROM ALTERNATIVE_NAME An),
altTitle AS
( SELECT DISTINCT Ati.production_id AS prodId,
    COUNT(*) OVER (PARTITION BY Ati.production_id) AS cntTitle
    FROM ALTERNATIVE_TITLE Ati)
SELECT DISTINCT Pc.production_id, Pc.person_id, (1+altName.cntName)*(1+altTitle.cntTitle) AS degAmbiguity, altName.cntName, altTitle.cntTitle
FROM altName, altTitle, PRODUCTION_CAST Pc
WHERE Pc.production_id = altTitle.prodId AND Pc.person_id = altName.peId
ORDER BY degAmbiguity DESC OFFSET 0 ROWS FETCH NEXT 10 ROWS ONLY


