package tw.tcnr02.cloudinteractive;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class MainActivity2 extends AppCompatActivity implements AdapterView.OnItemClickListener {
    ArrayList<HashMap<String, String>> arrayList = new ArrayList<HashMap<String, String>>();
    private ProgressBar progressbar;
    private GridView gridview;
    private ArrayList<String> recSet;
    private HashMap<String, Object> item;
    private DbHelper dbHper;
    private String DB_FILE = "cloudinteractive.db";
    private int DBversion = 1;
    private ProgressDialog pd;
    private Handler handler = new Handler();
    private MyAdapter adapter;
    private ImageView img;
    private ImageView imageview;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.api_activity);
        //------------------------------
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()
                .penaltyLog()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .penaltyLog()
                .penaltyDeath()
                .build());
//------------------------

        initDB();
        dowload();

        setupViewComponent();
    }

    private void initDB() {
        if (dbHper == null) {
            dbHper = new DbHelper(getApplicationContext(), DB_FILE, null, DBversion);
        }
        recSet = dbHper.getRecSet();
    }
    private void dowload() {
        if (dbHper.RecCount() == 0) {
            pd = new ProgressDialog(MainActivity2.this);
            pd.setMessage("資料下載中...");
            pd.setCancelable(false);
            pd.show();

            handler.postDelayed(updateTimer, 2000); // 延遲
        } else {
            Toast.makeText(getApplicationContext(), "歡迎回來", Toast.LENGTH_LONG).show();
        }
    }

    private Runnable updateTimer = new Runnable() {
        @Override
        public void run() {
            getOpenData();
            pd.cancel();
        }
    };
    private void setupViewComponent() {
        gridview = (GridView)findViewById(R.id.api_gridview);
        imageview = (ImageView)findViewById(R.id.apitext_imageview);
        gridview.setNumColumns(4);
        showdata();
    }



    private void getOpenData () {
        try {
            String Task_opendata
                    = new TransTask().execute("https://jsonplaceholder.typicode.com/photos").get();

            JSONArray jsonData = new JSONArray(Task_opendata);

            dbHper.clearRec();

            for (int i = 0; i < jsonData.length(); i++) {
                JSONObject albumId = jsonData.getJSONObject(i);
                String ID = albumId.getString("id");
                    String Title = albumId.getString("title");
                    String ThumbnailUrl = albumId.getString("thumbnailUrl");
                    int s = 5;
                    String msg = null;
                    long rowID = dbHper.insertRec_news(ID, Title, ThumbnailUrl); //真正執行SQL
                    if (rowID != -1) {
                        msg = "新增記錄  成功 ! \n" + "目前資料表共有 " + dbHper.RecCount() + " 筆記錄 !";
                    } else {
                        msg = "新增記錄  失敗 ! ";
                    }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
    private void showdata() {
        //-----讀取SQLlite裡的Opendata-----
        recSet = dbHper.getRecSet();
        for (int i = 0; i < recSet.size(); i++) {
            String[] fld = recSet.get(i).split("#");
            HashMap<String,String> hashMap = new HashMap<>();
            hashMap.put("id",fld[0]);
            hashMap.put("title",fld[1]);
            hashMap.put("thumbnailUrl",fld[3]);
            arrayList.add(hashMap);
            int g=  2;
        }

        //==========設定listView============
        //自定義的adapter
        adapter = new MyAdapter(
                getApplicationContext(),
                arrayList,
                R.layout.apitext_activity,
                new String[]{"id","title","thumbnailUrl"},
                new int[]{R.id.apitext_textid,R.id.apitext_texttitle,R.id.apitext_imageview}
        );
        gridview.setAdapter(adapter);//將抓取的資料設定到表格視窗
        gridview.setOnItemClickListener(this);//建立表格視窗按鈕監聽
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String[] fld = recSet.get(position).split("#");
        Bundle bundle = new Bundle();
        bundle.putString("id", fld[0]);
        bundle.putString("title",  fld[1]);
        bundle.putString("thumbnailUrl",fld[3]);
        Intent intent = new Intent(MainActivity2.this,MainActivity3.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private class TransTask extends AsyncTask<String, Void, String> {
        String ans;

        @Override
        protected String doInBackground(String... params) {
            StringBuilder sb = new StringBuilder();
            try {
                URL url = new URL(params[0]);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(url.openStream()));
                String line = in.readLine();
                while (line != null) {
                    Log.d("HTTP", line);
                    sb.append(line);
                    line = in.readLine();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ans = sb.toString();
            //------------
            return ans;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d("s", "s:" + s);
            parseJson(s);
        }

        private void parseJson(String s) {

        }
    }
    //自定義的Adapter，用意在於把Picasso的library帶入，否則picasso無法使用於listview
    public class MyAdapter extends SimpleAdapter{

        public MyAdapter(Context context, List<? extends Map<String, ?>> data, int     resource, String[] from, int[] to){
            super(context, data, resource, from, to);
        }

        public View getView(int position, View convertView, ViewGroup parent){
            // here you let SimpleAdapter built the view normally.
            View v = super.getView(position, convertView, parent);

            Context context = parent.getContext();
            // Then we get reference for Picasso
            img = (ImageView) v.getTag();
            if(img == null){
                img = (ImageView) v.findViewById(R.id.apishow_imageview);
                img.setScaleType(ImageView.ScaleType.CENTER_CROP);
                v.setTag(img); // <<< THIS LINE !!!!

            }
            // get the url from the data you passed to the `Map`
            String[] fld = recSet.get(position).split("#");
            String url = fld[3];
            // do Picasso
            // maybe you could do that by using many ways to start

            int imageWidth =150;//設定長寬
            Picasso.get().load(url).resize(imageWidth, imageWidth).into(img);
            img.setScaleType(ImageView.ScaleType.CENTER_CROP);
            // return the view
            return v;
        }
    }
}
