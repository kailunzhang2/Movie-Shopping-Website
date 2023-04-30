use moviedb;
drop procedure IF EXISTS add_movie;
DELIMITER //
CREATE PROCEDURE add_movie(IN mTitle VARCHAR(100),
                           IN mDirector VARCHAR(100),
                           IN mYear INTEGER,
                           IN genre VARCHAR(32),
                           IN  star VARCHAR(100),
                           out return_msg VARCHAR(100)
)
BEGIN

IF ((SELECT count(*) FROM movies WHERE title = mTitle AND year = mYear AND director = mDirector) > 0) THEN
SET return_msg = 'Movie Exist';
ELSE
        SET @newMovieId = concat("tt1010100",(select max(substring(id, 10)) from movies) + 1);
        INSERT INTO movies(id, title, year, director) VALUES (@newMovieId, mTitle, mYear, mDirector);

        IF ((SELECT count(*) FROM genres g WHERE g.name = genre) = 0) THEN
                            SET @gId = (SELECT MAX(id) FROM genres) + 1;
        INSERT INTO genres(id, name) VALUES (@gId, genre);
        END IF;

        INSERT INTO genres_in_movies(genreId, movieId) VALUES ((SELECT id FROM genres g WHERE g.name = genre), @newMovieId);

        IF ((SELECT count(*) FROM stars s WHERE s.name = star) = 0) THEN
                            SET @sId = concat("nm",(select max(substring(id, 3)) from stars) + 1);
        INSERT INTO stars(id, name, birthYear) VALUES (@sId, star, null);
        END IF;

        INSERT INTO stars_in_movies(starId, movieId) VALUES ((SELECT id FROM stars s WHERE s.name = star LIMIT 1), @newMovieId);


        SET @mId = (SELECT id FROM movies WHERE title = mTitle AND year = mYear AND director = mDirector LIMIT 1);
                    SET @sId = (SELECT id FROM stars s WHERE s.name = star LIMIT 1);
                    SET @genre = (SELECT id FROM genres g WHERE g.name = genre LIMIT 1);

                SET  return_msg = concat('mId: ',@mId, ', sId: ', @sId, ' genreId: ', @genre);
END IF;

END //
DELIMITER ;
