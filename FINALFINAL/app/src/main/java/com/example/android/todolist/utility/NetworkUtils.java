package com.example.android.todolist.utility;

import android.database.Cursor;
import android.net.Uri;

import com.example.android.todolist.DestinationInfo;
import com.example.android.todolist.data.TaskContract;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

public class NetworkUtils {

    Cursor mCursor;
    final static String BASE_URL =
            "https://maps.googleapis.com/maps/api/distancematrix/json?units=imperial&origins=";
   //"http://maps.googleapis.com/maps/api/distancematrix/outputFormat?parameters";
    //https://maps.googleapis.com/maps/api/distancematrix/json?units=imperial&origins=40.6655101,-73.89188969999998&destinations=40.6905615%2C-73.9976592%7C40.6905615%2C-73.9976592%7C40.6905615%2C-73.9976592%7C40.6905615%2C-73.9976592%7C40.6905615%2C-73.9976592%7C40.6905615%2C-73.9976592%7C40.659569%2C-73.933783%7C40.729029%2C-73.851524%7C40.6860072%2C-73.6334271%7C40.598566%2C-73.7527626%7C40.659569%2C-73.933783%7C40.729029%2C-73.851524%7C40.6860072%2C-73.6334271%7C40.598566%2C-73.7527626&key=YOUR_API_KEY
//"key=AIzaSyArFxfbEkowUDxLOuskowbCneAP8dyZHl4"

    final static String PARAM_ORIGIN = "origin";

    /*
     * The sort field. One of stars, forks, or updated.
     * Default: results are sorted by best match if no field is specified.
     */
    final static String PARAM_DESTINATION = "destinations";
    final static String testingPlace = "";
    public static ArrayList<DestinationInfo> destinationlist ;

    public static String addOrigin(String base, Double lo, Double la){
        String result = base+lo+","+la;
        return result;
    }

    public static void addDestination(Cursor mCursor){
        int i = 0;
        String result = "";
        destinationlist = new  ArrayList<DestinationInfo>();
        int idIndex = mCursor.getColumnIndex(TaskContract.TaskEntry._ID);
        int LongitudeIndex = mCursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_LONGITUDE);
        int LatitudeIndex = mCursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_LATITUDE);
        int DescriptionIndex = mCursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_NAME);

        for(mCursor.moveToFirst();!mCursor.isAfterLast();mCursor.moveToNext(),i++){
            String longtitude = mCursor.getString(LongitudeIndex);
            String latitude = mCursor.getString(LatitudeIndex);
            String name = mCursor.getString(DescriptionIndex);
            DestinationInfo tmp = new DestinationInfo();
            tmp.setLatitude(latitude);
            tmp.setLongitude(longtitude);
            tmp.setName(name);

            destinationlist.add(tmp);
            result=result+longtitude+","+latitude+"|";

        }

        DestinationInfo tmp_des = new DestinationInfo();
        tmp_des.setDestinationString(result);
        tmp_des.setDistanceValue(Integer.MAX_VALUE);
        destinationlist.add(tmp_des);


    }


    public static ArrayList<DestinationInfo> buildUrl(Double lo, Double la, Cursor mCursor) {
        String base_url = addOrigin(BASE_URL, lo, la);
        addDestination(mCursor);
        String testingPlace = destinationlist.get(destinationlist.size()-1).getDestinationString();
        Uri builtUri = Uri.parse(base_url).buildUpon()
                .appendQueryParameter(PARAM_DESTINATION, testingPlace)
                .appendQueryParameter("key","AIzaSyArFxfbEkowUDxLOuskowbCneAP8dyZHl4")
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        destinationlist.get(destinationlist.size()-1).setUrl(url);
        return destinationlist;
    }



    /**
     * This method returns the entire result from the HTTP response.
     *
     * @param url The URL to fetch the HTTP response from.
     * @return The contents of the HTTP response.
     * @throws IOException Related to network and stream reading
     */
    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }

}
