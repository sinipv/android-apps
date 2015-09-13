package com.example.app.popularmoviesapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieFragment extends Fragment {
    private final String LOG_TAG = MovieFragment.class.getSimpleName();

    private ImageAdapter imageAdapter;

    public MovieFragment() {
        Log.v(LOG_TAG, "In fragment contrst");
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.v(LOG_TAG, "In onCreateView()");
        View rootView = inflater.inflate(R.layout.fragment_movie, container, false);

        ArrayList<Movie> moviesList = new ArrayList<Movie>();
         imageAdapter = new ImageAdapter(getContext(), R.layout.fragment_movie, moviesList);

        GridView gridView = (GridView) rootView.findViewById(R.id.movieGridView);
        imageAdapter.notifyDataSetChanged();
        gridView.setAdapter(imageAdapter);


        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                Movie movie = imageAdapter.getItem(i);
                intent.putExtra("movie", movie);
                startActivity(intent);
                // Toast.makeText(getActivity(), imageAdapter.getItem(i), Toast.LENGTH_SHORT).show();
            }
        });
        return rootView;
    }

    private void getMovies() {
        Log.v(LOG_TAG, "In getMovies()");
        FetchMovieTask movieTask = new FetchMovieTask();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sortBy = preferences.getString(getString(R.string.pref_sortby_key),
                (getString(R.string.pref_sortby_default)));

       if ("Popularity".equalsIgnoreCase(sortBy)) {
            sortBy = "popularity.desc";
        } else {
            sortBy = "vote_average.desc";
        }
        movieTask.execute(sortBy);

    }

    public void onStart() {
        Log.v(LOG_TAG, "In onStart()");
        super.onStart();
        getMovies();
    }

    public class FetchMovieTask extends AsyncTask<String, Void, ArrayList<Movie>> {
        private final String LOG_TAG = FetchMovieTask.class.getSimpleName();


        protected ArrayList<Movie> doInBackground(String... params) {
            Log.v(LOG_TAG, "In doInBackground()");

            if (params.length == 0) {
                return null;
            }

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String movieJsonStr = null;
            String sortBy = params[0];
            String apiKey = "";//Add your own API KEY
            ArrayList<Movie> movies = null;
            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are available at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                final String MOVIE_BASE_URL = "http://api.themoviedb.org/3/discover/movie?";
                final String SORT_BY_PARAM = "sort_by";
                final String API_KEY_PARAM = "api_key";

                Uri builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                        .appendQueryParameter(SORT_BY_PARAM, sortBy)
                        .appendQueryParameter(API_KEY_PARAM, apiKey)
                        .build();

                URL url = new URL(builtUri.toString());

                Log.v(LOG_TAG, "Built URI " + builtUri.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                movieJsonStr = buffer.toString();

                Log.v(LOG_TAG, "Movie JSON String: " + movieJsonStr);

                movies = getMovieDataFromJson(movieJsonStr);


            } catch (IOException | JSONException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attempting
                // to parse it.
                movieJsonStr = null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            return movies;

        }


        /*get the data from Json*/

        private ArrayList<Movie> getMovieDataFromJson(String movieJsonStr)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String OWM_RESULTS = "results";
            final String OWM_POSTER_PATH = "poster_path";
            final String OWN_ORIGINAL_TITLE = "original_title";
            final String OWN_SYNOPSIS = "overview";
            final String OWN_RATING = "vote_average";
            final String OWN_RELEASE_DATE = "release_date";

            JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONArray movieArray = movieJson.getJSONArray(OWM_RESULTS);


            ArrayList<Movie> movieList = new ArrayList<Movie>();
            for (int i = 0; i < movieArray.length(); i++) {
                // For now, using the format "Day, description, hi/low"
                Movie movieObj = new Movie();

                String title;
                String posterPath;
                String synopsis;
                String rating;
                String releaseDate;
                // Get the JSON object representing one movie
                JSONObject movieItem = movieArray.getJSONObject(i);

                //JSONObject pathObject = movieItem.getJSONObject(OWM_POSTER_PATH);
                posterPath = movieItem.getString(OWM_POSTER_PATH);

                title = movieItem.getString(OWN_ORIGINAL_TITLE);
                synopsis = movieItem.getString(OWN_SYNOPSIS);
                rating = movieItem.getString(OWN_RATING);
                releaseDate = movieItem.getString(OWN_RELEASE_DATE);

                movieObj.setTitle(title);
                movieObj.setSynopsis(synopsis);
                movieObj.setRating(rating);
                movieObj.setReleaseDate(releaseDate);


                Log.v(LOG_TAG, "poster path" + posterPath);
                String imageUrl = getImageUrl(posterPath);
                Log.v(LOG_TAG, "Url path" + imageUrl);
                movieObj.setMovieUrl(imageUrl);

                movieList.add(movieObj);

            }
            for (int i = 0; i < movieList.size(); i++) {
                Log.v(LOG_TAG, "Result Array 1" + movieList.get(i));
            }

            return movieList;
        }

        private String getImageUrl(String posterPath) {
            String imageUrl;
            String baseUrl = "http://image.tmdb.org/t/p/";
            String size = "w185";

            imageUrl = baseUrl + size + posterPath;
            return imageUrl;

        }

        /* @Override
         protected void onPostExecute(String[] strings) {
             super.onPostExecute(strings);
             Log.v(LOG_TAG, "Result Array 2" + strings);
         }
        */
        @Override
        protected void onPostExecute(ArrayList<Movie> result) {
            Log.v(LOG_TAG, "Result Array 2" + result);
            ArrayList<Movie> movies = new ArrayList<Movie>();

            if (result != null) {
                for (Movie movie : result) {
                    movies.add(movie);
                }
                imageAdapter.setMovie(movies);

            }
            // imageAdapter.replace(result);

        }
    }

    class ImageAdapter extends ArrayAdapter<Movie> {

        private Context context;


       // List<String> urls = new ArrayList<String>();
        List<Movie> movies = new ArrayList<Movie>();


        public ImageAdapter(Context context, int layoutResourceId, ArrayList<Movie> movies) {
            super(context, 0, movies);
            this.context = context;
            this.movies = movies;
        }

    /*    public void setUrl(ArrayList<String> urls) {
            this.urls = urls;
        }
 */
        public void setMovie(ArrayList<Movie> movie) {
            movies.clear();
            this.movies = movie;
            notifyDataSetChanged();
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

       @Override
        public Movie getItem(int position) {
            return movies.get(position);
        }

        @Override
        public int getCount() {
            if (movies != null)
                return movies.size();
            else
                return 0;
        }


        public View getView(int position, View convertView, ViewGroup parent) {
            Log.v("ImageAdapter", "In getView()");

            ImageView imageView;
            // ViewHolder holder = null;
            if (convertView == null) {
                imageView = new ImageView(context);
                imageView.setLayoutParams(new GridView.LayoutParams(GridView.AUTO_FIT, 800));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(0, 0, 0, 0);

            } else {
                imageView = (ImageView) convertView;
            }
            if (movies != null) {
                Picasso.with(context)
                        .load(movies.get(position).getMovieUrl())
                        .into(imageView);
            }

            return imageView;
        }

    }


}