

-- Query b). Quite complex but if multiple years are the most productives, it returns them all.
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
SELECT * 
FROM (SELECT res1.cname, res1.pgenre, RANK() OVER (PARTITION BY res1.pgenre ORDER BY res1.cnt DESC) AS cnt2
        FROM (SELECT DISTINCT C.name AS cname, Prod.genre AS pgenre, COUNT(*) OVER (PARTITION BY C.name, Prod.genre) AS cnt
                FROM PRODUCTION Prod, COMPANY C, PRODUCTION_COMPANY Pc
                WHERE Prod.production_year = 1992 AND Prod.genre IS NOT NULL AND Pc.production_id = Prod.id AND Pc.company_id = C.id) res1) res2
WHERE res2.cnt2 = 1


--Query d)
WITH res1  AS (
  SELECT P.name AS pname, Pc.production_id AS prod_id
  FROM PERSON P, PRODUCTION_CAST Pc
  WHERE P.name LIKE '%Freeman%' AND Pc.person_id = P.id)
SELECT DISTINCT a.pname
FROM res1 a INNER JOIN res1 b 
ON a.prod_id = b.prod_id AND a.pname NOT LIKE b.pname
