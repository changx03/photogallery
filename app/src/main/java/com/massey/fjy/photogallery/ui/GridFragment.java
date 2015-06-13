package com.massey.fjy.photogallery.ui;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.massey.fjy.photogallery.ui.ImageDetailActivity;
import com.massey.fjy.photogallery.R;
import com.massey.fjy.photogallery.utils.BitmapHelper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;


public class GridFragment extends Fragment implements AbsListView.OnScrollListener {

    public ImageAdapter myImgAdapter;
    private Integer[] mImgIds = {
            R.drawable.img_0,
            R.drawable.img_1,
            R.drawable.img_2,
            R.drawable.img_3,
            R.drawable.img_4,
            R.drawable.img_5,
            R.drawable.img_6,
            R.drawable.img_7,
            R.drawable.img_8,
            R.drawable.img_9,
    };
    private String[] thumbnailImgs = {
            "thumbnail_0.jpg",
            "thumbnail_1.jpg",
            "thumbnail_2.jpg",
            "thumbnail_3.jpg",
            "thumbnail_4.jpg",
            "thumbnail_5.jpg",
            "thumbnail_6.jpg",
            "thumbnail_7.jpg",
            "thumbnail_8.jpg",
            "thumbnail_9.jpg"
    };
    public static String imgDir = "photoGallery";
    private File photoGalleryDir;
    private AsyncTaskLoadFiles myAsyncTaskLoader;
    private  GridView mGridview;
    public GridFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = getActivity();

        photoGalleryDir = new File(context.getFilesDir() + "/" + imgDir);
        if(!photoGalleryDir.exists()) {
            photoGalleryDir.mkdir();
            for(int i = 0; i < mImgIds.length; i++){
                // loading smaller image into private gallery folder. Nexus 5 emulator seems doesn't have enough memory for full size
                Bitmap mImg = BitmapHelper.decodeBitmapFromResource(context.getResources(), mImgIds[i], 640, 480);
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                mImg.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
                File destination = new File(photoGalleryDir, thumbnailImgs[i]);
                mImg.recycle();

                FileOutputStream fos;
                try{
                    destination.createNewFile();
                    fos = new FileOutputStream(destination);
                    fos.write(bytes.toByteArray());
                    fos.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }

        myAsyncTaskLoader = new AsyncTaskLoadFiles(myImgAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.grid, container, false);

        System.out.println("onCreateView");

        mGridview = (GridView)view.findViewById(R.id.myGrid);
        mGridview.setOnScrollListener(this);
        myImgAdapter = new ImageAdapter(getActivity());
        mGridview.setAdapter(myImgAdapter);

        myAsyncTaskLoader.cancel(true);
        myAsyncTaskLoader = new AsyncTaskLoadFiles(myImgAdapter);
        myAsyncTaskLoader.execute();

        mGridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Intent intent = new Intent(getActivity(), ImageDetailActivity.class);
                intent.putExtra(ImageDetailActivity.EXTRA_IMAGE, (int) id);

                System.out.println("id = " + id);

                // use scale animation
                ActivityOptions options = ActivityOptions.makeScaleUpAnimation(view, 0, 0, view.getWidth(), view.getHeight());
                getActivity().startActivity(intent, options.toBundle());
                //startActivity(intent);

            }
        });

        return view;
    }

    @Override
    public  void onPause(){
        super.onPause();
        System.out.println("GridFragment onPause");
        myAsyncTaskLoader.cancel(true);
        myImgAdapter = null;
        thumbnailImgs = null;
        int counts = mGridview.getCount();
        for (int i = 0; i < counts; i++) {
            ImageView imageView = (ImageView) mGridview.getChildAt(i);
            if (imageView != null) {
                if (imageView.getDrawable() != null) imageView.getDrawable().setCallback(null);
            }
        }
    }


    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
    }

    public class AsyncTaskLoadFiles extends AsyncTask<Void, String, Void>{
        private File targetDir;
        private ImageAdapter myTaskAdapter;
        
        public AsyncTaskLoadFiles(ImageAdapter adapter){
            myTaskAdapter = adapter;
        }
        
        @Override
        protected void onPreExecute(){
            super.onPreExecute();

            targetDir = new File(getActivity().getFilesDir() + "/" + imgDir);
            System.out.println("targtDir = " + targetDir);
            myTaskAdapter.clear();
        }
        
        @Override
        protected Void doInBackground(Void... params) {
            File[] files = targetDir.listFiles();
            Arrays.sort(files);
            for(File file : files){
                publishProgress(file.getAbsolutePath());
                if(isCancelled())
                    break;
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values){
            super.onProgressUpdate(values);

            myTaskAdapter.add(values[0]);
        }

        @Override
        protected void onPostExecute(Void result){
            super.onPostExecute(result);

            myTaskAdapter.notifyDataSetChanged();
        }
    }

    public class ImageAdapter extends BaseAdapter {
        private final Context mContext;
        ArrayList<String> itemList = new ArrayList<>();

        public ImageAdapter(Context context) {
            super();
            mContext = context;
        }

        @Override
        public int getCount() {
            return itemList.size();
        }

        @Override
        public Object getItem(int position) {
            return itemList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            ImageView imageView;
            if (convertView == null) {
                imageView = new ImageView(mContext);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setLayoutParams(new GridView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 350));
//                imageView.setLayoutParams(new GridView.LayoutParams(350, 350));
//                imageView.setPadding(2, 2, 2, 2);

                convertView = imageView;

                holder = new ViewHolder();
                holder.image = imageView;
                holder.position = position;
                convertView.setTag(holder);
            } else {
                //imageView = (ImageView)convertView;
                holder = (ViewHolder)convertView.getTag();
                ((ImageView)convertView).setImageBitmap(null);
            }

            // using an AsyncTask to load the slow images in a background thread
            new AsyncTask<ViewHolder, Void, Bitmap>(){
                private ViewHolder viewHolder;

                @Override
                protected Bitmap doInBackground(ViewHolder... params) {
                    viewHolder = params[0];
                    int reqSize = BitmapHelper.getPixelValueFromDps(mContext, BitmapHelper.IMAGE_THUMBNAIL_SIZE);
                    System.out.println("reqSize = " + reqSize);
                    return BitmapHelper.decodeBitmapFromUri(itemList.get(position), reqSize, reqSize);
                }

                @Override
                protected void onPostExecute(Bitmap result){
                    super.onPostExecute(result);

//                    if(viewHolder.position == position){
//                        viewHolder.progress.setVisibility(View.GONE);
//                        viewHolder.image.setVisibility(View.VISIBLE);
//                        viewHolder.image.setImageBitmap(result);
//                    }
                    viewHolder.image.setImageBitmap(result);
                }
            }.execute(holder);

            //imageView.setImageResource(mImgIds[position]);
            //return imageView;
            return convertView;
        }

        public void clear() {
            itemList.clear();
        }

        public void add(String value) {
            itemList.add(value);
        }

        public void remove(int index){
            itemList.remove(index);
        }
    }

    static class ViewHolder {
        ImageView image;
        int position;
        ProgressBar progress;
    }
}
