package android.example.com.boguscode;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class VideoAdapter extends ArrayAdapter<JSONObject> {

    private List<JSONObject> mItems;
    private Context context;
    private Map<String, Bitmap> imageCacheMap  = new Hashtable<>();
    ImagesCache cache = ImagesCache.getInstance();



    public VideoAdapter(Context context, int resource, List<JSONObject> objects) {
        super(context, resource, objects);
        mItems = objects;
        this.context = context;
        cache.initializeCache();
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        JSONObject video = mItems.get(position);
        ViewHolder viewHolder;
        Bitmap bm = null;
        String img = null;
        String uri;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_video, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }
        else{
            viewHolder = (ViewHolder)convertView.getTag();
        }

        try {
            uri = video.optJSONObject("pictures").optJSONArray("sizes")
                       .getJSONObject(video.optJSONObject("pictures").optJSONArray("sizes").length() - 1)
                       .optString("link", "");

            img = uri;
            bm = cache.getImageFromTable(img);

        } catch (Exception e) {
            e.printStackTrace();
        }

        if(bm != null)
        {
            viewHolder.imageView.setImageBitmap(bm);
        }
        else
        {
            viewHolder.imageView.setImageBitmap(null);
            DownloadImageTask imgTask = new DownloadImageTask(this, 300, 300);
            imgTask.execute(img);
        }

        viewHolder.nameTextView.setText(video.optString("name", ""));

        viewHolder.imageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context,"Item number " + position + " has been selected", Toast.LENGTH_SHORT).show();
            }
        });
        return convertView;
    }


    private class ViewHolder {
        TextView nameTextView;
        ImageView imageView;

        public ViewHolder(View item)
        {
            imageView = (ImageView) item.findViewById(R.id.video_thumbnail); /// make different class 3 & implement viewholder pattern for the same.
            nameTextView = (TextView) item.findViewById(R.id.list_item_video_name_textview);
        }
    }


    public class DownloadImageTask extends AsyncTask<String, Void, Bitmap>
    {
        private int inSampleSize = 0;
        private String imageUrl;
        private ArrayAdapter adapter;
        private ImagesCache cache;
        private int desiredWidth, desiredHeight;
        private Bitmap image = null;
        private ImageView ivImageView;

        public DownloadImageTask(ArrayAdapter adapter, int desiredWidth, int desiredHeight)
        {
            this.adapter = adapter;
            this.cache = ImagesCache.getInstance();
            this.desiredWidth = desiredWidth;
            this.desiredHeight = desiredHeight;
        }

        @Override
        protected Bitmap doInBackground(String... params)
        {
            imageUrl = params[0];
            return getImage(imageUrl);
        }

        @Override
        protected void onPostExecute(Bitmap result)
        {
            super.onPostExecute(result);

            if(result != null)
            {
                cache.addImageToTable(imageUrl, result);

                if(ivImageView != null)
                {
                    ivImageView.setImageBitmap(result);
                }
                if(adapter != null)
                {
                    adapter.notifyDataSetChanged();
                }
            }
        }

        private Bitmap getImage(String imageUrl)
        {
            if(cache.getImageFromTable(imageUrl) == null)
            {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                options.inSampleSize = inSampleSize;
                try
                {
                    URL url = new URL(imageUrl);
                    HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                    InputStream stream = connection.getInputStream();
                    image = BitmapFactory.decodeStream(stream, null, options);
                    int imageWidth = options.outWidth;
                    int imageHeight = options.outHeight;
                    if(imageWidth > desiredWidth || imageHeight > desiredHeight)
                    {
                        inSampleSize = inSampleSize + 2;
                        getImage(imageUrl);
                    }
                    else
                    {
                        options.inJustDecodeBounds = false;
                        connection = (HttpURLConnection)url.openConnection();
                        stream = connection.getInputStream();
                        image = BitmapFactory.decodeStream(stream, null, options);
                        return image;
                    }
                }

                catch(Exception e)
                {
                    Log.e("Image cache Exception", e.toString());
                }
            }
            return image;
        }
    }
}
