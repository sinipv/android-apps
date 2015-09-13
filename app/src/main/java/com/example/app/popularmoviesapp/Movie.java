package com.example.app.popularmoviesapp;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Ranjith on 9/10/15.
 */
public class Movie  implements Parcelable{
    private String movieUrl ="";
    private String title = "";
    private String synopsis="";
    private String rating;
    private String releaseDate;

    public String getMovieUrl() {
        return movieUrl;
    }

    public void setMovieUrl(String movieUrl) {
        this.movieUrl = movieUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSynopsis() {
        return synopsis;
    }

    public void setSynopsis(String synopsis) {
        this.synopsis = synopsis;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }


    public static final Parcelable.Creator<Movie> CREATOR = new Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel source) {
            Movie mMovie = new Movie();
            mMovie.movieUrl = source.readString();
            mMovie.title = source.readString();
            mMovie.synopsis = source.readString();
            mMovie.rating = source.readString();
            mMovie.releaseDate = source.readString();
            return mMovie;
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(movieUrl);
        dest.writeString(title);
        dest.writeString(synopsis);
        dest.writeString(rating);
        dest.writeString(releaseDate);

    }
}
