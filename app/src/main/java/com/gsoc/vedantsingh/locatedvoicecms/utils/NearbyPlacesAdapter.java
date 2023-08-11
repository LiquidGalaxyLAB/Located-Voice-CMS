package com.gsoc.vedantsingh.locatedvoicecms.utils;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.gsoc.vedantsingh.locatedvoicecms.R;
import com.gsoc.vedantsingh.locatedvoicecms.beans.PlaceInfo;
import com.jcraft.jsch.Session;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class NearbyPlacesAdapter extends BaseAdapter {
    List<PlaceInfo> nearbyPlaces;
    Context context;
    Session session = null;

    public NearbyPlacesAdapter(Context context, List<PlaceInfo> nearbyPlaces) {
        this.nearbyPlaces = nearbyPlaces;
        this.context = context;
    }

    @Override
    public int getCount() {
        return this.nearbyPlaces.size();
    }

    @Override
    public Object getItem(int position) {
        return this.nearbyPlaces.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final PlaceInfo currentPlace = this.nearbyPlaces.get(position);
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.nearby_places_item, parent, false);
            viewHolder = new ViewHolder(convertView, context);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (currentPlace == null) {
            Log.e("Nearby Places", "currentPlace is null at position: " + position);
            return convertView; // Return the view as is, as there's no data to display
        }

        Log.d("Nearby Places", "Title: " + currentPlace.getTitle());
        Log.d("Nearby Places", "Description: " + currentPlace.getDescription());
        Log.d("Nearby Places", "ImageLink: " + currentPlace.getImageLink());

        if (!TextUtils.isEmpty(currentPlace.getImageLink())) {
            Glide.with(context)
                    .load(currentPlace.getImageLink())
                    .error(R.drawable.baseline_image_24)
                    .into(viewHolder.placeImage);
        } else {
            // Handle the case when the imageLink is null or empty.
            // For example, you can set a placeholder image like this:
            viewHolder.placeImage.setImageResource(R.drawable.baseline_image_24);
        }

        viewHolder.placeName.setText(currentPlace.getTitle());
        viewHolder.placeDescription.setText(currentPlace.getDescription().toString());

        return convertView;
    }

    public class ViewHolder{
        TextView placeName, placeDescription;
        ImageView placeImage;
        ViewHolder(View rootView, Context context){
            placeImage = rootView.findViewById(R.id.placeImage);
            placeName = rootView.findViewById(R.id.placeName);
            placeDescription = rootView.findViewById(R.id.placeDescription);

            rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String place = placeName.getText().toString();
                    GetSessionTask getSessionTask = new GetSessionTask(context);
                    getSessionTask.execute();

                    String command = "echo 'search=" + place + "' > /tmp/query.txt";

                    ExecutorService executorService = Executors.newSingleThreadExecutor();
                    SearchNearbyPlaceTask searchTask = new SearchNearbyPlaceTask(session, context, command);
                    Future<Void> future = executorService.submit(searchTask);
                    try {
                        future.get();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    executorService.shutdown();
//                    if(!placeDescription.getText().toString().equals("")) {
                        PoisGridViewAdapter.playBarkAudioFromText(context, "You have clicked on "+ placeName.getText().toString() + "." + placeDescription.getText().toString());
//                    }
                }
            });
        }
    }

    public class SearchNearbyPlaceTask implements Callable<Void> {
        private String command;
        private Session session;
        private Context context;

        public SearchNearbyPlaceTask(Session session, Context context, String command) {
            this.command = command;
            this.session = session;
            this.context = context;
        }

        @Override
        public Void call() throws Exception {
            try {
                LGUtils.setConnectionWithLiquidGalaxy(session, command, context);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private class GetSessionTask extends AsyncTask<Void, Void, Void> {

        private final Context context;

        public GetSessionTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (context != null) {
                session = LGUtils.getSession(context);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void success) {
            super.onPostExecute(success);
        }
    }
}