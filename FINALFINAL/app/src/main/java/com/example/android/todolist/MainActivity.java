/*
* Copyright (C) 2016 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.example.android.todolist;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.todolist.data.TaskContract;
import com.example.android.todolist.utility.NetworkUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

//import android.support.design.widget.FloatingActionButton;
//import android.support.v4.app.LoaderManager;
//import android.support.v4.content.AsyncTaskLoader;
//import android.support.v4.content.Loader;
//import android.support.v7.app.AppCompatActivity;
//import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.RecyclerView;
//import android.support.v7.widget.helper.ItemTouchHelper;


public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {
//,CustomCursorAdapter.ListItemClickListener
    // Constants for logging and referring to a unique loader
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int TASK_LOADER_ID = 0;

    // Member variables for the adapter and RecyclerView
    public  CustomCursorAdapter mAdapter;
    private Toast mToast;
    RecyclerView mRecyclerView;
    TextView mSearchResultsTextView;
    URL searchUrl;
    ArrayList<DestinationInfo> destinationlist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set the RecyclerView to its corresponding view
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerViewTasks);

        // Set the layout for the RecyclerView to be a linear layout, which measures and
        // positions items within a RecyclerView into a linear list
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize the adapter and attach it to the RecyclerView
        mAdapter = new CustomCursorAdapter(this);
        mAdapter.setOnItemClickListener(new CustomCursorAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, Double lo, Double la, Cursor mCursor) {
//                //檢驗Cursor實際存在數量
//                String num=Integer.toString(mAdapter.getItemCount());
//                Toast.makeText(MainActivity.this,num,Toast.LENGTH_SHORT).show();
//                makeSearchQuery(lo, la, mCursor);
//要出現最近的地點

                Uri uri=TaskContract.TaskEntry.CONTENT_URI_NEARBY;
                //取得Recyleview 的ItemID 然後串到Content URI NEARBY之後
                uri=uri.buildUpon().appendPath(String.valueOf(view.getTag())).build();
                //透過ContentResolver去取得ContentProvider的東西
                //query搜尋最短距離，把URI丟到 (在TaskContentProvider.java getColumnIndex)

                Cursor cursor = getContentResolver().query(
                        uri,
                        null,
                        "_id=?",
                        new String[]{String.valueOf(view.getTag())},
                        null,
                        null
                );

                if(cursor.moveToFirst()){
                    int id_index=cursor.getColumnIndex(TaskContract.TaskEntry._ID);
                    int name_index=cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_NAME);
                    String onclick="Cursor ID:"+cursor.getString(id_index)+" 座標描述:"+cursor.getString(name_index);
                    Toast.makeText(MainActivity.this,onclick,Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(MainActivity.this,"null",Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onItemLongClick(View view, Double lo, Double la) {
                Toast.makeText(MainActivity.this, " long click",
                        Toast.LENGTH_SHORT).show();


                Uri gmmIntentUri = Uri.parse("geo:0.0?q="+lo+"," +la);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);

            }
        });
        mRecyclerView.setAdapter(mAdapter);

        /*
         Add a touch helper to the RecyclerView to recognize when a user swipes to delete an item.
         An ItemTouchHelper enables touch behavior (like swipe and move) on each ViewHolder,
         and uses callbacks to signal when a user is performing these actions.
         */
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            // Called when a user swipes left or right on a ViewHolder
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                // Here is where you'll implement swipe to delete

                // COMPLETED (1) Construct the URI for the item to delete
                //[Hint] Use getTag (from the adapter code) to get the id of the swiped item
                // Retrieve the id of the task to delete
                int id = (int) viewHolder.itemView.getTag();

                // Build appropriate uri with String row id appended
                String stringId = Integer.toString(id);
                Uri uri = TaskContract.TaskEntry.CONTENT_URI;
                uri = uri.buildUpon().appendPath(stringId).build();

                // COMPLETED (2) Delete a single row of data using a ContentResolver
                getContentResolver().delete(uri, null, null);

                // COMPLETED (3) Restart the loader to re-query for all tasks after a deletion
                getSupportLoaderManager().restartLoader(TASK_LOADER_ID, null, MainActivity.this);

            }
        }).attachToRecyclerView(mRecyclerView);

        /*
         Set the Floating Action Button (FAB) to its corresponding View.
         Attach an OnClickListener to it, so that when it's clicked, a new intent will be created
         to launch the AddTaskActivity.
         */



        /*
         Ensure a loader is initialized and active. If the loader doesn't already exist, one is
         created, otherwise the last created loader is re-used.
         */
        getSupportLoaderManager().initLoader(TASK_LOADER_ID, null, this);
    }


    /**
     * This method is called after this activity has been paused or restarted.
     * Often, this is after new data has been inserted through an AddTaskActivity,
     * so this restarts the loader to re-query the underlying data for any changes.
     */
    @Override
    protected void onResume() {
        super.onResume();

        // re-queries for all tasks
        getSupportLoaderManager().restartLoader(TASK_LOADER_ID, null, this);
    }


    /**
     * Instantiates and returns a new AsyncTaskLoader with the given ID.
     * This loader will return task data as a Cursor or null if an error occurs.
     *
     * Implements the required callbacks to take care of loading data at all stages of loading.
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, final Bundle loaderArgs) {

        return new AsyncTaskLoader<Cursor>(this) {

            // Initialize a Cursor, this will hold all the task data
            Cursor mTaskData = null;

            // onStartLoading() is called when a loader first starts loading data
            @Override
            protected void onStartLoading() {
                if (mTaskData != null) {
                    // Delivers any previously loaded data immediately
                    deliverResult(mTaskData);
                } else {
                    // Force a new load
                    forceLoad();
                }
            }

            // loadInBackground() performs asynchronous loading of data
            @Override
            public Cursor loadInBackground() {
                // Will implement to load data

                // Query and load all task data in the background; sort by priority
                // [Hint] use a try/catch block to catch any errors in loading data

                try {
                    return getContentResolver().query(TaskContract.TaskEntry.CONTENT_URI,
                            null,
                            null,
                            null,
                            TaskContract.TaskEntry.COLUMN_LATITUDE);

                } catch (Exception e) {
                    Log.e(TAG, "Failed to asynchronously load data.");
                    e.printStackTrace();
                    return null;
                }
            }

            // deliverResult sends the result of the load, a Cursor, to the registered listener
            public void deliverResult(Cursor data) {
                mTaskData = data;
                super.deliverResult(data);
            }
        };

    }


    /**
     * Called when a previously created loader has finished its load.
     *
     * @param loader The Loader that has finished.
     * @param data The data generated by the Loader.
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update the data that the adapter uses to create ViewHolders
        mAdapter.swapCursor(data);
    }


    /**
     * Called when a previously created loader is being reset, and thus
     * making its data unavailable.
     * onLoaderReset removes any references this activity had to the loader's data.
     *
     * @param loader The Loader that is being reset.
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    private void makeSearchQuery(Double lo, Double la,Cursor mCursor){
        this.destinationlist = NetworkUtils.buildUrl(lo,la, mCursor);
        searchUrl = destinationlist.get(destinationlist.size()-1).getUrl();
        // origin : (lo,la) // 要再建立url的時候抓其他地點ㄉ資料
        new GithubQueryTask().execute(searchUrl.toString());
//        mSearchResultsTextView.setText(searchUrl.toString());
        Log.d("feliceUrl",searchUrl.toString());
    }




    public class GithubQueryTask extends AsyncTask<String, Void, String> {

        String data = "";
        InputStream inputStream = null;

        public  void parseJson(String data) throws JSONException {
            JSONObject  objectObjects = new JSONObject(data);

            JSONArray destination_addresses_array =  objectObjects.getJSONArray("destination_addresses");
            for (int i = 0; i < destination_addresses_array.length()-1; i++) {
                DestinationInfo place_destinationInfo = destinationlist.get(i);
                place_destinationInfo.setAddress(destination_addresses_array.getString(i));
                destinationlist.set(i,place_destinationInfo);
                };

            JSONArray row_array =  objectObjects.getJSONArray("rows");
            JSONObject row_0_Object = row_array.getJSONObject(0);
            JSONArray row_0_object_elements = row_0_Object.getJSONArray("elements");
            for (int i = 0; i < row_0_object_elements.length(); i++) {
                    DestinationInfo tmp = destinationlist.get(i);
                    int distanceValue = Integer.parseInt(row_0_object_elements.getJSONObject(i).getJSONObject("distance").getString("value"));
                    tmp.setDistanceValue(distanceValue);
                    destinationlist.set(i, tmp);
            }


        }

        protected void onPostExecute(String data){
            try {
                parseJson(data);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Collections.sort(destinationlist,
                    new Comparator<DestinationInfo>() {
                        public int compare(DestinationInfo o1, DestinationInfo o2) {
                            int result = o1.getDistanceValue() > o2.getDistanceValue()? 1 : -1;
                            return result;
                        }
                    });
           
            Toast.makeText(MainActivity.this, destinationlist.get(1).getName(),
                    Toast.LENGTH_SHORT).show();
        }


        @Override
        protected String doInBackground(String... urlStrings) {
            try {

                URL url = new URL(urlStrings[0]); //初始化
                HttpURLConnection httpURLConnection =
                        (HttpURLConnection) url.openConnection(); //取得連線之物件
                InputStream inputStream = httpURLConnection.getInputStream();
                //對取得的資料進行讀取
                BufferedReader bufferedReader =
                        new BufferedReader(new InputStreamReader(inputStream));
                String line = "";
                while (line != null) {
                    line = bufferedReader.readLine();
                    data = data + line;
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return data;
        }


        }
    public void onClickAddTask(View view) {

        String inputLongitude = ((EditText) findViewById(R.id.editTextLongitude)).getText().toString();
        //FELICE(1)
        String inputLatitude = ((EditText) findViewById(R.id.editTextLatitude)).getText().toString();
        String inputPlaceDescription = ((EditText) findViewById(R.id.editTextLocationName)).getText().toString();
        if (inputLongitude.length() ==0 && inputLatitude.length() == 0) {
            return;
        }

        // Insert new task data via a ContentResolver
        // Create new empty ContentValues object
        ContentValues contentValues = new ContentValues();
        // Put the task description and selected mPriority into the ContentValues
        contentValues.put(TaskContract.TaskEntry.COLUMN_LONGITUDE, Double.parseDouble(inputLongitude));
        contentValues.put(TaskContract.TaskEntry.COLUMN_LATITUDE, Double.parseDouble(inputLatitude));
        contentValues.put(TaskContract.TaskEntry.COLUMN_NAME, inputPlaceDescription );
        // Insert the content values via a ContentResolver
        Uri uri = getContentResolver().insert(TaskContract.TaskEntry.CONTENT_URI, contentValues);

        // Display the URI that's returned with a Toast
        // [Hint] Don't forget to call finish() to return to MainActivity after this insert is complete
        if(uri != null) {
            Toast.makeText(getBaseContext(), uri.toString(), Toast.LENGTH_LONG).show();
        }

        // Finish activity (this returns back to MainActivity)
        Intent intent = getIntent();
        startActivity(intent);

        EditText editTextLongitude = (EditText) findViewById(R.id.editTextLongitude);
        EditText editTextLatitude = (EditText) findViewById(R.id.editTextLatitude);
        EditText editTextLocationName=(EditText) findViewById(R.id.editTextLocationName);

        editTextLongitude.setText("");
        editTextLongitude.clearFocus();
        editTextLatitude.setText("");
        editTextLatitude.clearFocus();
        editTextLocationName.setText("");
        editTextLocationName.clearFocus();

    }
    }






