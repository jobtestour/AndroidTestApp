package exemplefortest.androidtestapp;

public class Photo {


    private String id;
    private String title;
    private String url;
    private String thumbnailUrl;





    public Photo(String Id, String Title, String Url, String imageurl) {
        this.id = Id;
        this.title = Title;
        this.url = Url;
        this.thumbnailUrl = imageurl;

    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }


    public String getImageur() {
        return thumbnailUrl;
    }


}
