SELECT production_year, COUNT(*) FROM PRODUCTION P WHERE P.kind LIKE '%movie%' GROUP BY P.production_year ORDER BY P.production_year;

SELECT country_code, COUNT(*) AS country_count FROM COMPANY C WHERE C.country_code is NOT NULL GROUP BY C.country_code ORDER BY country_count DESC OFFSET 0 ROWS FETCH NEXT 10 ROWS ONLY;

SELECT MIN(height), MAX(height), AVG(height) FROM PERSON P WHERE P.height IS NOT NULL AND P.gender LIKE '%f%' ORDER BY P.height DESC;

ALTER TABLE PRODUCTION_CAST ALTER COLUMN [Column] INTEGER NOT NULL;
ALTER TABLE PRODUCTION_CAST ADD PRIMARY KEY (id);
