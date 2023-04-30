use moviedb;
load data local infile 'movies.txt' INTO TABLE movies fields terminated by ',';
load data local infile 'genres.txt' INTO TABLE genres fields terminated by ',';
load data local infile 'genres_in_movies.txt' INTO TABLE genres_in_movies fields terminated by ',';
load data local infile 'stars.txt' INTO TABLE stars fields terminated by ',';
load data local infile 'stars_in_movies.txt' INTO TABLE stars_in_movies fields terminated by ',';