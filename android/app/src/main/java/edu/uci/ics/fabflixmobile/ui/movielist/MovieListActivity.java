package edu.uci.ics.fabflixmobile.ui.movielist;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.uci.ics.fabflixmobile.R;
import edu.uci.ics.fabflixmobile.data.NetworkManager;
import edu.uci.ics.fabflixmobile.data.model.Movie;
import edu.uci.ics.fabflixmobile.databinding.ActivityMainBinding;
import edu.uci.ics.fabflixmobile.databinding.ActivityMovielistBinding;
import edu.uci.ics.fabflixmobile.databinding.MovielistRowBinding;
import edu.uci.ics.fabflixmobile.ui.search.mainSearchActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MovieListActivity extends AppCompatActivity {
    private int page, offset, total;
    private String title;
    private ArrayList<Movie> movies;
    ListView listView;


    private final String host = "ec2-18-237-129-173.us-west-2.compute.amazonaws.com";
    private final String port = "8443";
    private final String domain = "cs122b-fall22-project1-api-example";
    private final String baseURL = "https://" + host + ":" + port + "/" + domain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = this.getIntent();
        offset = Integer.parseInt(intent.getStringExtra("offset"));
        page = 0;
        title = intent.getStringExtra("title");
        super.onCreate(savedInstanceState);
        ActivityMovielistBinding binding = ActivityMovielistBinding.inflate(getLayoutInflater());
        setContentView(R.layout.activity_movielist);

        Button prev = findViewById(R.id.prev);
        Button next = findViewById(R.id.next);

        prev.setOnClickListener(view -> {
            Log.d("PREV", "PREV");
            if(page > 1){
                page -= 1;
                paging();
            }
        });

        next.setOnClickListener(view -> {
            Log.d("NEXT", "NEXT");
            if(page < (total/20 + 1)){
                page += 1;
                paging();
            }
        });

        try{
            JSONArray arr = new JSONArray(intent.getStringExtra("results"));
            showMovies(arr);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    public void showMovies(JSONArray arr){
        movies = new ArrayList<>();
        try {
            for(int i = 0; i < arr.length() - 1; i++){
                JSONObject tempMv = arr.getJSONObject(i);
                Movie temp = new Movie(tempMv.getString("movie_name"), tempMv.getString("movie_year"), tempMv.getString("movie_id"), tempMv.getString("director"), tempMv.getString("starsName"), tempMv.getString("genres"));
                movies.add(temp);
                System.out.println(temp.toString());
            }
            JSONObject tempMv = arr.getJSONObject(arr.length()-1);
            total = tempMv.getInt("count");
            System.out.print("total: ");
            System.out.println(total);

            MovieListViewAdapter adapter = new MovieListViewAdapter(this, movies);
            ListView listView = findViewById(R.id.list);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener((parent, view, position, id) -> {
                Movie movie = movies.get(position);
                @SuppressLint("DefaultLocale") String message = String.format("Clicked on position: %d, name: %s, %s", position, movie.getName(), movie.getYear());
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                Intent SingleMoviePage = new Intent(MovieListActivity.this, SingleMovieActivity.class);
                SingleMoviePage.putExtra("movie_name", movie.getName());
                SingleMoviePage.putExtra("movie_year", movie.getYear());
                SingleMoviePage.putExtra("movie_id", movie.getId());
                SingleMoviePage.putExtra("director", movie.getDirector());
                SingleMoviePage.putExtra("starsName", movie.getAllStars());
                SingleMoviePage.putExtra("genres", movie.getAllGenres());
                startActivity(SingleMoviePage);
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void paging() {
        System.out.print("total: ");
        System.out.println(total);
        System.out.print("page: ");
        System.out.println(page);
        movies.clear();
        RequestQueue queue = NetworkManager.sharedManager(this).queue;
        StringRequest loginRequest = new StringRequest(
                Request.Method.POST,
                baseURL + "/api/android-search",
                response -> {
                    Log.d("search.success", response);
                    try {
                        JSONArray resultDataJson = new JSONArray(response);
                        showMovies(resultDataJson);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    Log.d("search.error", error.toString());
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("title", title);
                params.put("offset", String.valueOf((page-1) * 20));
                return params;
            }
        };
        queue.add(loginRequest);
    }
}