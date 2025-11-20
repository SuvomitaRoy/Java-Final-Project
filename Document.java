public abstract class Document {
    private String title;
    private String author;
    private String genre;
    private Boolean available;

    public Document(String title, String author, String genre) {
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.available = true;
    }

    public String getTitle(){
        return(this.title);
    }

    public String getAuthor(){
        return(this.author);
    }

    public String getGenre(){
        return(this.genre);
    }

    public Boolean isAvailable(){
        return(this.available);
    }

    public void setTitle(String title){
        this.title = title;
    }

    public void setAuthor(String author){
        this.author = author;
    }

    public void setGenre(String genre){
        this.genre = genre;
    }

    public void setAvailability(Boolean available){
        this.available = available;
    }
}