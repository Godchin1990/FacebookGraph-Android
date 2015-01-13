package com.example.idlefox.facebookapi.Fragment;



import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.idlefox.facebookapi.Constants;
import com.example.idlefox.facebookapi.R;
import com.example.idlefox.facebookapi.model.FacebookPhotoModel;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Reference: http://josihokila.blogspot.tw/2012/10/graph-api-facebook.html
 */
public class FansphotoFragment extends AbsListViewBaseFragment {

    private static final String TAG = "FansphotoFragment";
    private String result_graph;
    private TextView txt_result;
    private Session session;
//    final static int GRAPH_FANS_PHOTOS = 0;
    String[] imageUrls = Constants.IMAGES;
    DisplayImageOptions options;
    ArrayList<FacebookPhotoModel> photos;

    public FansphotoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        session = Session.getActiveSession();
        if (session.isOpened() ) {
            queryPhotos();
        }

        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.ic_stub)
                .showImageForEmptyUri(R.drawable.ic_empty)
                .showImageOnFail(R.drawable.ic_error)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fansphoto, container, false);
        txt_result = (TextView) view.findViewById(R.id.txt_result);
        listView = (GridView) view.findViewById(R.id.gridview);

        return view;
    }

    private void queryPhotos(){
        new Request(
            session,
            "218720131496216/photos",
            null,
            HttpMethod.GET,
            new Request.Callback() {
                public void onCompleted(Response response) {
                    ArrayList<FacebookPhotoModel> responselist = new ArrayList<FacebookPhotoModel>();
                    try{
                        JSONObject jo = response.getGraphObject().getInnerJSONObject();
                        JSONArray ja = jo.getJSONArray("data");
//                        String serverresponse = response.getGraphObject().getInnerJSONObject().toString();
                        try {
                            responselist = parseGraphPhoto(ja);
//                            responselist = parseGraphPhoto(serverresponse);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        txt_result.setText(ja.toString());
//                        GraphObject go = response.getGraphObject();
//                        JSONArray ja = (JSONArray) go.getProperty("data");
//                        ArrayList<FacebookPhotoModel> photos = parseGraphPhoto(ja);
                    } catch(Exception e){
                        Log.e(TAG, e.toString());
                    }
                }
            }
        ).executeAsync();
    }

    private ArrayList<FacebookPhotoModel> parseGraphPhoto(JSONArray jsonArray){
//      private ArrayList<FacebookPhotoModel> parseGraphPhoto(String jsonString){
        Log.i(TAG, "Length: " + jsonArray.length());
        photos = new ArrayList<FacebookPhotoModel>();
        try{
            for(int i=0; i<jsonArray.length(); i++){
                JSONObject jo = jsonArray.getJSONObject(i);
                String smail_img = jo.getString("picture");
                String big_img = jo.getString("source");
                String owner = jo.getJSONObject("from").getString("name");
                FacebookPhotoModel fm = new FacebookPhotoModel(smail_img, big_img, owner);
                photos.add(fm);
            }
            listView.setAdapter(new ImageAdapter());
        } catch(Exception e){
            Log.e(TAG, e.toString());
        }

        return photos;
    }

    public class ImageAdapter extends BaseAdapter {

        private LayoutInflater inflater;

        ImageAdapter() {
            inflater = LayoutInflater.from(getActivity());
        }

        @Override
        public int getCount() {
            return photos.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            View view = convertView;
            if (view == null) {
                view = inflater.inflate(R.layout.item_grid_image, parent, false);
                holder = new ViewHolder();
                assert view != null;
                holder.imageView = (ImageView) view.findViewById(R.id.image);
                holder.progressBar = (ProgressBar) view.findViewById(R.id.progress);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            ImageLoader.getInstance()
                    .displayImage(photos.get(position).getSmall(), holder.imageView, options, new SimpleImageLoadingListener() {
                        @Override
                        public void onLoadingStarted(String imageUri, View view) {
                            holder.progressBar.setProgress(0);
                            holder.progressBar.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                            holder.progressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                            holder.progressBar.setVisibility(View.GONE);
                        }
                    }, new ImageLoadingProgressListener() {
                        @Override
                        public void onProgressUpdate(String imageUri, View view, int current, int total) {
                            holder.progressBar.setProgress(Math.round(100.0f * current / total));
                        }
                    });

            return view;
        }

        private class ViewHolder {
            ImageView imageView;
            ProgressBar progressBar;
        }
    }


//    private Handler mHandler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            switch (msg.what) {
//                case GRAPH_FANS_PHOTOS:
//                    txt_result.setText(result_graph);
//                    break;
//                default:
//                    break;
//            }
//        }
//    };
}
