package edu.uci.ics.fabflixmobile.ui.movielist;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import edu.uci.ics.fabflixmobile.R;
import edu.uci.ics.fabflixmobile.data.NetworkManager;
import edu.uci.ics.fabflixmobile.data.model.Movie;
import edu.uci.ics.fabflixmobile.databinding.ActivityMovielistBinding;

public class SingleMovieActivity extends AppCompatActivity {
    ListView listView;
    private ArrayList<Movie> movies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movielist);
        Intent intent = this.getIntent();
        ActivityMovielistBinding binding = ActivityMovielistBinding.inflate(getLayoutInflater());

        String movie_name = intent.getStringExtra("movie_name");
        String movie_year = intent.getStringExtra("movie_year");
        String movie_id = intent.getStringExtra("movie_id");
        String director = intent.getStringExtra("director");
        String starsName = intent.getStringExtra("starsName");
        String genres = intent.getStringExtra("genres");
        Movie temp = new Movie(movie_name, movie_year, movie_id, director, starsName, genres);
        movies = new ArrayList<>();
        movies.add(temp);
//        System.out.println(temp.toString());

        SingleMovieViewAdapter adapter = new SingleMovieViewAdapter(this, movies);
        ListView listView = findViewById(R.id.list);
        listView.setAdapter(adapter);
    }



}