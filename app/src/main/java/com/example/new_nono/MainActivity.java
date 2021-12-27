package com.example.new_nono;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    Button button1;
    Button button2;
    EditText edit;

    Activity act =this;
    GridView myGridView;
    GridView leftGridView;
    GridView topGridView;

    static Bitmap img;

    static ArrayList<Bitmap> imgArr = new ArrayList<>();
    static ArrayList<Bitmap> whiteArr = new ArrayList<>();
    static ArrayList<Integer> topNumArr = new ArrayList<>();
    static ArrayList<Integer> leftNumArr = new ArrayList<>();

    static ArrayList<Integer> numArr = new ArrayList<>();
    static int cnt = 0;
    private static int check=1;
    static int gray;

    static gridAdapter1 adapter;
    static whiteAdapter whiteAdap;
    static gridAdapter2 leftAdapter;
    static gridAdapter3 topAdapter;

    static  String img_link;

    final int GET_GALLERY_IMAGE = 200;
    final String clientId = "ebOUIwplNMkrg8bGO69N";
    final String clientSecret = "HMlCd70r4v";
    private static final String TAG = "myTag";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button1 = (Button) findViewById(R.id.search_btn);
        button2 = (Button) findViewById(R.id.gallery_btn);
        edit = findViewById(R.id.editText);

        myGridView = (GridView) findViewById(R.id.myGridView);
        leftGridView = (GridView)findViewById(R.id.left_num);
        topGridView = (GridView)findViewById(R.id.top_num);

        adapter = new gridAdapter1(this);
        leftAdapter = new gridAdapter2(this);
        topAdapter = new gridAdapter3(this);
        whiteAdap = new whiteAdapter(this);

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(v.getId()==R.id.search_btn&&!edit.getText().toString().replace(" ", "").equals("")){
                    new Thread(() -> {
                        String text = edit.getText().toString();

                        try {
                            text = URLEncoder.encode(text, "UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            throw new RuntimeException("검색어 인코딩 실패", e);
                        }
                        String apiURL = "https://openapi.naver.com/v1/search/image?query=" + text;    // json 결과


                        Map<String, String> requestHeaders = new HashMap<>();
                        requestHeaders.put("X-Naver-Client-Id", clientId);
                        requestHeaders.put("X-Naver-Client-Secret", clientSecret);
                        String responseBody = get(apiURL, requestHeaders);

                        @SuppressWarnings("deprecation")
                        JsonParser parser = new JsonParser();
                        @SuppressWarnings("deprecation")
                        Object object = parser.parse(responseBody);
                        JsonObject j_obj = (JsonObject)object;
                        JsonArray arr = (JsonArray)j_obj.get("items");
                        for(int i = 0; i<arr.size();i++){
                            JsonObject obj = (JsonObject)arr.get(i);
                            img_link = obj.get("link").toString();
                        }
                        img_link = img_link.substring(1, img_link.length()-1);
                        System.out.println(img_link);
                        new DownloadFilesTask().execute(img_link);
                    }).start();
                }
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");

                startActivityForResult(intent, GET_GALLERY_IMAGE);
                if (!imgArr.isEmpty()) {
                    imgArr.clear();
                }
            }
        });

        myGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if(numArr.get(position)==1) {
                    numArr.set(position, 0);
                    whiteArr.set(position, changeBlack(whiteArr.get(position)));
                    check++;
                    whiteAdap.notifyDataSetChanged();
                }else{
                    Toast.makeText(MainActivity.this, "Wrong! or Already done!", Toast.LENGTH_SHORT).show();
                }

                if(numArr.get(numArr.size()-1)==check) {
                    Toast.makeText(MainActivity.this, "FINISH!", Toast.LENGTH_LONG).show();
                }
            }
        });

    }



    @SuppressWarnings("deprecation")
    @SuppressLint("StaticFieldLeak")
    private class DownloadFilesTask extends AsyncTask<String,Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... strings) {
            Bitmap bmp = null;
            try {
                String img_url = strings[0]; //url of the image
                URL url = new URL(img_url);

                bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bmp;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }


        @Override
        protected void onPostExecute(Bitmap result) {

            int i = 0;
            check=0;
            cnt =0;
            whiteArr.clear();
            imgArr.clear();
            numArr.clear();
            img = result;
            for (int y = 0; y <= (img.getHeight() - img.getHeight() / 20); y += img.getHeight() /20) {
                for (int x = 0; x <= (img.getWidth() - img.getWidth() / 20); x += img.getWidth() / 20, i++) {
                    Bitmap tmp = Bitmap.createBitmap(img, x, y, img.getWidth() / 20, img.getHeight() / 20);

                    if(checkBlack(tmp)==255){
                        numArr.add(0);
                    }else{
                        numArr.add(1);
                        cnt++;
                    }
                    imgArr.add(grayScale(tmp));
                    whiteArr.add(changeWhite(tmp));
                }
            }
            numArr.add(cnt);

            calculateLeftNumArr(numArr);
            calculateTopNumArr(numArr);
            myGridView.setNumColumns(20);
            myGridView.setAdapter(adapter);
            leftGridView.setNumColumns(4);
            leftGridView.setAdapter(leftAdapter);
            topGridView.setNumColumns(20);
            topGridView.setAdapter(topAdapter);
            myGridView.setAdapter(whiteAdap);
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        /* Check which request we're responding to */
        if (requestCode == GET_GALLERY_IMAGE ) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                try {
                    // 선택한 이미지에서 비트맵 생성
                    InputStream in = getContentResolver().openInputStream(data.getData());
                    img = BitmapFactory.decodeStream(in);
                    in.close();

                    int i = 0;
                    cnt=0;
                    check=0;
                    numArr.clear();
                    whiteArr.clear();
                    for (int y = 0; y <= (img.getHeight() - img.getHeight() / 20); y += img.getHeight() /20) {
                        for (int x = 0; x <= (img.getWidth() - img.getWidth() / 20); x += img.getWidth() / 20, i++) {
                            Bitmap tmp = Bitmap.createBitmap(img, x, y, img.getWidth() / 20, img.getHeight() / 20);
                            if(checkBlack(tmp)==255){
                                numArr.add(0);
                            }else{
                                numArr.add(1);
                                cnt++;
                            }
                            imgArr.add(grayScale(tmp));
                            whiteArr.add(changeWhite(tmp));
                        }
                    }
                    numArr.add(cnt);
                    System.out.println(cnt);
                    leftNumArr = calculateLeftNumArr(numArr);
                    topNumArr = calculateTopNumArr(numArr);

                    adapter = new gridAdapter1(this);
                    myGridView.setNumColumns(20);
                    myGridView.setAdapter(adapter);


                    leftGridView.setNumColumns(4);
                    leftGridView.setAdapter(leftAdapter);
                    topGridView.setNumColumns(20);
                    topGridView.setAdapter(topAdapter);
                    myGridView.setAdapter(whiteAdap);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }




    private Bitmap grayScale(final Bitmap orgBitmap){
        int width, height, area;
        width = orgBitmap.getWidth();
        height = orgBitmap.getHeight();
        area = width*height;
        Bitmap bmpGrayScale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

            // color information
        int A, R, G, B;
        int pixel;
        int sum = 0;

            // scan through all pixels
        for (int x = 0; x < width; ++x) {

            for (int y = 0; y < height; ++y) {
                    // get pixel color
                pixel = orgBitmap.getPixel(x, y);
                A = Color.alpha(pixel);
                R = Color.red(pixel);
                G = Color.green(pixel);
                B = Color.blue(pixel);
                gray = (int) (0.2989 * R + 0.5870 * G + 0.1140 * B);

                sum += gray;

            }
        }
        if (sum/area > 128) {
            gray = 255; //검정박스
            for (int x = 0; x < width; ++x) {
                for (int y = 0; y < height; ++y) {
                    pixel = orgBitmap.getPixel(x, y);
                    A = Color.alpha(pixel);
                    bmpGrayScale.setPixel(x, y, Color.argb(A, gray, gray, gray));
                }
            }
        }
        else {
            gray = 0; //하얀박스
            for (int x = 0; x < width; ++x) {
                for (int y = 0; y < height; ++y) {
                    pixel = orgBitmap.getPixel(x, y);
                    A = Color.alpha(pixel);
                    bmpGrayScale.setPixel(x, y, Color.argb(A, gray, gray, gray));
                }
            }
        }

        return bmpGrayScale;

    }

    private int checkBlack(final Bitmap orgBitmap){
        int width, height, area;
        width = orgBitmap.getWidth();
        height = orgBitmap.getHeight();
        area = width*height;
        Bitmap bmpGrayScale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        // color information
        int A, R, G, B;
        int pixel;
        int sum = 0;

        // scan through all pixels
        for (int x = 0; x < width; ++x) {

            for (int y = 0; y < height; ++y) {
                // get pixel color
                pixel = orgBitmap.getPixel(x, y);
                A = Color.alpha(pixel);
                R = Color.red(pixel);
                G = Color.green(pixel);
                B = Color.blue(pixel);
                gray = (int) (0.2989 * R + 0.5870 * G + 0.1140 * B);

                sum += gray;

            }
        }
        if (sum/area > 128) {
            return 255;
        }
        else {
            return 0;
        }
    }

    private ArrayList<Integer> calculateLeftNumArr(final ArrayList<Integer> orgNumArr){
        //일차원 0, 1로 이루어진 numArr계산해서 노노그램 숫자 계산
        leftNumArr.clear();

        int cnt = 0;
        int order = 0;

        for(int i=0; i<20; i++){
            cnt = 0;
            order=0;
            for(int k=0; k<20; k++) {

                if (orgNumArr.get(i*20+k) == 1 && order<4) {
                    cnt++;
                }else if(orgNumArr.get(i*20+k) == 0 && cnt>0 && order<4){
                    leftNumArr.add(cnt);
                    order++;
                    cnt=0;
                }

                if(k==19 && orgNumArr.get(i*20+k) == 1 && cnt>0 && order<4){
                    leftNumArr.add(cnt);
                    order++;
                }
            }
            //System.out.print(i+"th line order : "+order);
            for(;order<4;order++){
                leftNumArr.add(0);
            }
        }
        System.out.println();
        return leftNumArr;
    }

    private ArrayList<Integer> calculateTopNumArr(final ArrayList<Integer> orgNumArr){
        //일차원 0, 1로 이루어진 numArr계산해서 노노그램 숫자 계산
        topNumArr.clear();
        topNumArr= new ArrayList<>(Collections.nCopies(80, 0));
        int cnt = 0;
        int order = 0;

        for(int i=0; i<20;i++){
            cnt = 0;
            order=0;
            for(int k=0;k<20;k++){
                if (orgNumArr.get(k*20+i) == 1&& order<4) {
                    cnt++;
                }else if(orgNumArr.get(k*20+i) == 0 && cnt>0&& order<4){
                    topNumArr.set(20*order+i, cnt);
                    order++;
                    cnt=0;
                }
                if(k==19 && cnt>0 && order<4) {
                    topNumArr.set(20*order+i, cnt);
                    order++;
                }
            }
        }

        return topNumArr;
    }


    private static final class gridAdapter1 extends BaseAdapter {

        @Override
        public int getCount() {
            return imgArr.size();
        }

        @Override
        public Bitmap getItem(int position) {
            return imgArr.get(position);
        }


        @Override
        public long getItemId(int position) {
            return position;
        }

        public void change(int position){
            Bitmap change;
            change = changeBlack(imgArr.get(position));
            imgArr.set(position, change);

        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView iv;

            if(convertView==null){
                iv =new ImageView(context);
                iv.setLayoutParams(new ViewGroup.LayoutParams(35,35));
                iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }
            else{
                iv=(ImageView)convertView;
            }

            iv.setImageBitmap(imgArr.get(position));

            return iv;
        }


        Context context;

        gridAdapter1(Context context){
            this.context=context;
        }
    }

    private static final class whiteAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return whiteArr.size();
        }

        @Override
        public Bitmap getItem(int position) {
            return whiteArr.get(position);
        }


        @Override
        public long getItemId(int position) {
            return position;
        }

        public void change(int position){
            Bitmap change;
            change = changeBlack(whiteArr.get(position));
            whiteArr.set(position, change);

        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView iv;

            if(convertView==null){
                iv =new ImageView(context);
                iv.setLayoutParams(new ViewGroup.LayoutParams(35,35));
                iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }
            else{
                iv=(ImageView)convertView;
            }

            iv.setImageBitmap(whiteArr.get(position));

            return iv;
        }


        Context context;

        whiteAdapter(Context context){
            this.context=context;
        }
    }

    private static Bitmap changeBlack(final Bitmap orgBitmap){
        int width, height, area;
        width = orgBitmap.getWidth();
        height = orgBitmap.getHeight();

        Bitmap bmpGrayScale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        // color information
        int A, R, G, B;
        int pixel;
        // scan through all pixels
        for (int x = 0; x < width; ++x) {

            for (int y = 0; y < height; ++y) {
                // get pixel color
                pixel = orgBitmap.getPixel(x, y);
                A = Color.alpha(pixel);
                gray = 0;
                bmpGrayScale.setPixel(x, y, Color.argb(A, gray, gray, gray));

            }
        }

        return bmpGrayScale;
    }

    private static Bitmap changeWhite(final Bitmap orgBitmap){
        int width, height, area;
        width = orgBitmap.getWidth();
        height = orgBitmap.getHeight();

        Bitmap bmpGrayScale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        // color information
        int A, R, G, B;
        int pixel;
        // scan through all pixels
        for (int x = 0; x < width; ++x) {

            for (int y = 0; y < height; ++y) {
                // get pixel color
                pixel = orgBitmap.getPixel(x, y);
                A = Color.alpha(pixel);
                gray = 255;
                bmpGrayScale.setPixel(x, y, Color.argb(A, gray, gray, gray));

            }
        }

        return bmpGrayScale;
    }



    private static final class gridAdapter2 extends BaseAdapter { //leftGridView adapter

        @Override
        public int getCount() {
            return leftNumArr.size();
        }

        @Override
        public Object getItem(int position) {
            return leftNumArr.get(position);
        }


        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView iv;

            if(convertView==null){
                iv =new TextView(context);
                iv.setLayoutParams(new ViewGroup.LayoutParams(35,35));
            }
            else{
                iv=(TextView)convertView;
            }
            iv.setBackgroundColor(Color.YELLOW);
            iv.setTextSize(11);
            iv.setText( String.valueOf(leftNumArr.get(position)));
            if(leftNumArr.get(position)!=0)
                iv.setText(String.valueOf(leftNumArr.get(position)));
            else{
                iv.setText(" ");
            }

            return iv;
        }


        Context context;

        gridAdapter2(Context context){
            this.context=context;
        }
    }

    public class gridAdapter3 extends BaseAdapter {//topGridview adapter


        @Override
        public int getCount() {
            return topNumArr.size();
        }

        @Override
        public Object getItem(int position) {
            return topNumArr.get(position);
        }


        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView iv;

            if(convertView==null){
                iv =new TextView(context);
                iv.setLayoutParams(new ViewGroup.LayoutParams(35,40));
            }
            else{
                iv=(TextView) convertView;
            }
            iv.setBackgroundColor(Color.YELLOW);
            iv.setTextSize(10);
            iv.setText(String.valueOf(topNumArr.get(position)));
            if(topNumArr.get(position)!=0)
                iv.setText(String.valueOf(topNumArr.get(position)));
            else{
                iv.setText(" ");
            }


            return iv;
        }


        Context context;

        gridAdapter3(Context context){
            this.context=context;
        }
    }



    private static String get(String apiUrl, Map<String, String> requestHeaders){
        HttpURLConnection con = connect(apiUrl);
        try {
            con.setRequestMethod("GET");
            for(Map.Entry<String, String> header :requestHeaders.entrySet()) {
                con.setRequestProperty(header.getKey(), header.getValue());
            }

            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) { // 정상 호출
                return readBody(con.getInputStream());
            } else { // 에러 발생
                return readBody(con.getErrorStream());
            }
        } catch (IOException e) {
            throw new RuntimeException("API 요청과 응답 실패", e);
        } finally {
            con.disconnect();
        }
    }

    private static HttpURLConnection connect(String apiUrl){
        try {
            URL url = new URL(apiUrl);
            return (HttpURLConnection)url.openConnection();
        } catch (MalformedURLException e) {
            throw new RuntimeException("API URL이 잘못되었습니다. : " + apiUrl, e);
        } catch (IOException e) {
            throw new RuntimeException("연결이 실패했습니다. : " + apiUrl, e);
        }
    }

    private static String readBody(InputStream body){
        InputStreamReader streamReader = new InputStreamReader(body);

        try (BufferedReader lineReader = new BufferedReader(streamReader)) {
            StringBuilder responseBody = new StringBuilder();

            String line;
            while ((line = lineReader.readLine()) != null) {
                responseBody.append(line);
            }

            return responseBody.toString();
        } catch (IOException e) {
            throw new RuntimeException("API 응답을 읽는데 실패했습니다.", e);
        }
    }

}