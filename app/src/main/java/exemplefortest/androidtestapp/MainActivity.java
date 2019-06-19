package exemplefortest.androidtestapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {



    ListView listView;
    SwipeRefreshLayout pullToRefresh;
    List<Photo> PhotoList;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        listView = (ListView) findViewById(R.id.listViewPhoto);

        pullToRefresh = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);




        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {


            @Override
            public void onRefresh() {


                getInfo();


            }

        });


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //go to url
                String Uri=PhotoList.get(position).getUrl();

                Log.i("NFO","Uri : "+Uri);

                if(pullToRefresh.isRefreshing()) {
                    pullToRefresh.setRefreshing(false);
                }

            }
        });



        getInfo();

    }








    private void getInfo() {




        //Assuming server respects Cache-Control/If-Modified-Since headers, than all you have to do is to create a Cache object and set it to OkHttpClient. Retrofit will take care of the rest.
         //setup cache
      // int cacheSize = 10 * 1024 * 1024; // 10 MB
      // Cache cache = new Cache(getCacheDir(), cacheSize);

      // OkHttpClient okHttpClient = new OkHttpClient.Builder()
      //         .cache(cache)
      //         .build();

        //if Not :

        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .cache(provideCache())
                .addInterceptor(httpLoggingInterceptor)
                .addNetworkInterceptor( provideCacheInterceptor() )
                .addInterceptor( provideOfflineCacheInterceptor() )
                .build();





    //start retrofit

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(WebService.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create()) //Here we are using the GsonConverterFactory to directly convert json data to object
                .build();

        WebService ServiceWeb = retrofit.create(WebService.class);

        Call<List<Photo>> call = ServiceWeb.getPhotos();

        call.enqueue(new Callback<List<Photo>>() {




            @Override
            public void onResponse(Call<List<Photo>> call, Response<List<Photo>> response) {
                PhotoList = response.body();

                //Creating an String array for the ListView
                assert PhotoList != null;
                String [] Photos = new String[PhotoList.size()];

                //looping through all the heroes and inserting the names inside the string array
                for (int i = 0; i < PhotoList.size(); i++) {
                    Photos[i] = PhotoList.get(i).getTitle();
                }


                //displaying the string array into listview

                DisplayListView(Photos);

            }

            @Override
            public void onFailure(Call<List<Photo>> call, Throwable t) {
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();


            }
        });
    }






    private Cache provideCache () {
        Cache cache = null;
        try {
            cache = new Cache( new File(MainActivity.this.getCacheDir(), "http-cache" ),
                    10 * 1024 * 1024 ); // 10 MB
        }
        catch (Exception e) {
            Log.e( "Error", e.toString() );
        }
        return cache;
    }

    public Interceptor provideCacheInterceptor(){
        return new Interceptor() {
            @Override
            public okhttp3.Response intercept (Chain chain) throws IOException {
                okhttp3.Response response = chain.proceed( chain.request() );
                // re-write response header to force use of cache
                CacheControl cacheControl;

                if (isConnectingToInternet()) {
                    cacheControl = new CacheControl.Builder()
                            .maxAge(0, TimeUnit.SECONDS)
                            .build();
                } else {
                    cacheControl = new CacheControl.Builder()
                            .maxStale(7, TimeUnit.DAYS)
                            .build();
                }
                return response.newBuilder()
                        .removeHeader("Pragma")
                        .removeHeader("Cache-Control")
                        .header("Cache-Control", cacheControl.toString())
                        .build();
            }
        };
    }

    public Interceptor provideOfflineCacheInterceptor () {
        return new Interceptor() {
            @Override
            public okhttp3.Response intercept (Interceptor.Chain chain) throws IOException {
                Request request = chain.request();
                if (!isConnectingToInternet()) {
                    CacheControl cacheControl = new CacheControl.Builder()
                            .maxStale(7, TimeUnit.DAYS)
                            .build();

                    request = request.newBuilder()
                            .removeHeader("Pragma")
                            .removeHeader("Cache-Control")
                            .cacheControl(cacheControl)
                            .build();
                }

                return chain.proceed(request);
            }
        };
    }



    public boolean isConnectingToInternet(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService( Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }





    private void DisplayListView(String[] ItemsList)
    {

        listView.setAdapter(new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1,ItemsList ));

    }





}
