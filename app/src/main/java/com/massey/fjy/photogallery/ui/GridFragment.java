package com.massey.fjy.photogallery.ui;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;

import com.massey.fjy.photogallery.db.DbHelper;
import com.massey.fjy.photogallery.R;
import com.massey.fjy.photogallery.utils.BitmapHelper;
import com.massey.fjy.photogallery.utils.DataHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class GridFragment extends Fragment implements AbsListView.OnScrollListener {
    private ImageAdapter myImgAdapter;
    private AsyncTaskLoadFiles myAsyncTaskLoader;
    private GridView mGridView;
    private ListView mListView;
    private DbHelper dbHelper;
    private Integer mViewMode;
    private ArrayList<String> myImageList;

    // image options
    private int mViewBy;
    private String optionKeyWord;

    public GridFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = getActivity();
        System.out.println("LOG GridFragment onCreate");

        dbHelper = new DbHelper(context);
        myAsyncTaskLoader = new AsyncTaskLoadFiles(myImgAdapter);

        // get image options from bundle
        Bundle bundle = this.getArguments();
        mViewBy = bundle.getInt(DataHelper.VIEW_BY, 0);
        optionKeyWord = bundle.getString(DataHelper.OPTION_KEY_WORD, getResources().getStringArray(R.array.tags)[0]);
        System.out.println("mViewBy = " + mViewBy + " optionKeyWord = " + optionKeyWord);

        SharedPreferences sharedPref = getActivity().getSharedPreferences(DataHelper.PREFS_NAME, Context.MODE_PRIVATE);
        mViewMode = sharedPref.getInt(DataHelper.VIEW_MODE, 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view;
        myImgAdapter = new ImageAdapter(getActivity());
        if(mViewMode == 0){
            view = inflater.inflate(R.layout.fragment_grid, container, false);
            mGridView = (GridView)view.findViewById(R.id.myGrid);
            mGridView.setOnScrollListener(this);
            mGridView.setAdapter(myImgAdapter);
        }else{
            view = inflater.inflate(R.layout.fragment_list, container, false);
            mListView = (ListView)view.findViewById(R.id.myList);
            mListView.setOnScrollListener(this);
            mListView.setAdapter(myImgAdapter);
        }
        System.out.println("LOG GridFragment onCreateView");

        // get image list by options
        if (mViewBy == DataHelper.VIEW_BY_ALL) {
            myImageList = dbHelper.getAllGridView();
        } else if (mViewBy == DataHelper.VIEW_BY_TAG) {
            if (optionKeyWord.equals(getResources().getStringArray(R.array.tags)[0])) {
                myImageList = dbHelper.getAllGridView();
            } else {
                myImageList = dbHelper.getImagesByTagGridView(optionKeyWord);
            }
        } else {
            myImageList = dbHelper.searchImagesGridView(optionKeyWord);
        }

        myAsyncTaskLoader.cancel(true);
        myAsyncTaskLoader = new AsyncTaskLoadFiles(myImgAdapter);
        myAsyncTaskLoader.execute();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(mViewMode == 0)
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Intent intent = new Intent(getActivity(), ImageDetailActivity.class);
                intent.putExtra(ImageDetailActivity.EXTRA_IMAGE, (int) id);
                intent.putExtra(ImageDetailActivity.IMAGE_NAME, myImageList.get((int) id));
                System.out.println("id = " + id + " , imageName = " + myImageList.get((int) id));

                // use scale animation
                ActivityOptions options = ActivityOptions.makeScaleUpAnimation(view, 0, 0, view.getWidth(), view.getHeight());
                getActivity().startActivity(intent, options.toBundle());
            }
        });
        else
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Intent intent = new Intent(getActivity(), ImageDetailActivity.class);
                intent.putExtra(ImageDetailActivity.EXTRA_IMAGE, (int) id);
                intent.putExtra(ImageDetailActivity.IMAGE_NAME, myImageList.get((int) id));
                System.out.println("id = " + id + " , imageName = " + myImageList.get((int) id));

                // use scale animation
                ActivityOptions options = ActivityOptions.makeScaleUpAnimation(view, 0, 0, view.getWidth(), view.getHeight());
                getActivity().startActivity(intent, options.toBundle());
            }
        });
        return view;
    }

    @Override
    public  void onPause(){
        super.onPause();
        System.out.println("LOG GridFragment onPause");
        myAsyncTaskLoader.cancel(true);
        myImgAdapter = null;
        if(mViewMode == 0) {
            int counts = mGridView.getCount();
            for (int i = 0; i < counts; i++) {
                ImageView imageView = (ImageView) mGridView.getChildAt(i);
                if (imageView != null) {
                    if (imageView.getDrawable() != null) imageView.getDrawable().setCallback(null);
                }
            }
        }else {
            int counts = mListView.getCount();
            for (int i = 0; i < counts; i++) {
                ImageView imageView = (ImageView) mListView.getChildAt(i);
                if (imageView != null) {
                    if (imageView.getDrawable() != null) imageView.getDrawable().setCallback(null);
                }
            }
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
    }

    private static class ViewHolder {
        ImageView image;
        int position;
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

            // get gallery full path
            SharedPreferences sharedPref = getActivity().getSharedPreferences(DataHelper.PREFS_NAME, Context.MODE_PRIVATE);
            String photoGalleryPath = sharedPref.getString(DataHelper.PHOTO_GALLERY_FULL_PATH, null);

            assert photoGalleryPath != null;
            targetDir = new File(photoGalleryPath);
            System.out.println("targetDir = " + targetDir);

            myTaskAdapter.clear();
        }

            @Override
        protected Void doInBackground(Void... params) {
                for (String imageName : myImageList) {
                String filePath = targetDir + "/" + imageName;
                publishProgress(filePath);
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
//                    System.out.println("reqSize = " + reqSize);
                    return BitmapHelper.decodeBitmapFromUri(itemList.get(position), reqSize, reqSize);
                }

                @Override
                protected void onPostExecute(Bitmap result){
                    super.onPostExecute(result);

                    viewHolder.image.setImageBitmap(result);
                }
            }.execute(holder);

            return convertView;
        }

        public void clear() {
            itemList.clear();
        }

        public void add(String value) {
            itemList.add(value);
        }

//        public void remove(int index){
//            itemList.remove(index);
//        }
    }
}
