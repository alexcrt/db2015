SELECT P.production_year, COUNT(*) as number_of_movie
  FROM PRODUCTION P 
  WHERE P.kind LIKE '%movie%' 
  GROUP BY P.production_year 
  ORDER BY P.production_year;

SELECT country_code, COUNT(*) AS country_count 
  FROM COMPANY C 
  WHERE C.country_code is NOT NULL 
  GROUP BY C.country_code 
  ORDER BY country_count DESC OFFSET 0 ROWS FETCH NEXT 10 ROWS ONLY;

SELECT MIN(dur.max_year - dur.min_year), MAX(dur.max_year - dur.min_year), AVG(dur.max_year - dur.min_year) 
  FROM (SELECT MIN(Prod.production_year) AS min_year, MAX(Prod.production_year) AS max_year 
          FROM PRODUCTION Prod, PRODUCTION_CAST Pc 
          WHERE Pc.production_id = Prod.id 
          GROUP BY Pc.person_id) dur;
   
SELECT MIN(R.actor_count), MAX(R.actor_count), AVG(R.actor_count) 
  FROM(SELECT COUNT(*) AS actor_count
          FROM PRODUCTION_CAST Pc
          WHERE Pc.role LIKE '%actor%' OR PC.role LIKE '%actress%'
          GROUP BY Pc.production_id) R;
          


SELECT MIN(height), MAX(height), AVG(height) 
  FROM PERSON P 
  WHERE P.height IS NOT NULL AND P.gender LIKE '%f%' 
  ORDER BY P.height DESC;
  
  

SELECT DISTINCT P.id, P.name, Prod.id, Prod.title
 FROM PERSON P, PRODUCTION Prod,
  PRODUCTION_CAST Pc1 INNER JOIN PRODUCTION_CAST Pc2 ON 
    (Pc1.person_id = Pc2.person_id AND Pc1.production_id = Pc2.production_id AND (Pc1.role LIKE '%actor%' OR Pc1.role LIKE '%actress%') AND Pc2.role LIKE '%director%')
 WHERE Prod.id = Pc1.production_id AND P.id = Pc1.person_id AND Prod.kind LIKE 'movie';



SELECT C.name
FROM CHARACTER_TABLE C
WHERE C.id IN (SELECT Pc.character_id
                  FROM PRODUCTION_CAST Pc
                  WHERE Pc.character_id IS NOT NULL
                  GROUP BY Pc.character_id
                  ORDER BY COUNT(*) DESC OFFSET 0 ROWS FETCH NEXT 3 ROWS ONLY);
  
--ALTER TABLE PRODUCTION_CAST ADD CONSTRAINT fk_character_id FOREIGN KEY (character_id) REFERENCES CHARACTER_TABLE (id) ON DELETE CASCADE;