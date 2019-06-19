package exemplefortest.androidtestapp;


import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;


public interface WebService {



    String BASE_URL = "http://jsonplaceholder.typicode.com/";

    @GET("photos")
    Call<List<Photo>> getPhotos();





}
