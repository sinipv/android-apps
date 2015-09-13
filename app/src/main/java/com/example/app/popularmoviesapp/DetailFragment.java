package com.example.app.popularmoviesapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment {

    public DetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        Intent intent = getActivity().getIntent();
        if(intent != null && intent.hasExtra("movie")) {
            Movie movie = intent.getParcelableExtra("movie");
            ((TextView) rootView.findViewById(R.id.title_text)).setText(movie.getTitle());
            ((TextView) rootView.findViewById(R.id.release_date_text)).setText(movie.getReleaseDate());
            ((TextView) rootView.findViewById(R.id.ratings_text)).setText(movie.getRating());
            ((TextView) rootView.findViewById(R.id.synopsis_text)).setText(movie.getSynopsis());
             ImageView imageView = (ImageView) rootView.findViewById(R.id.imageView);

            Picasso.with(getActivity())
                    .load(movie.getMovieUrl())
                    .into(imageView);
           // ((ImageView) rootView.findViewById(R.id.imageView)).setImageURI(Uri.parse(movie.getMovieUrl()));

        }

        return  rootView;
    }
}
