public class Book extends Document {
    private String isbn;
    private Integer pageNumber;

    public Book(String title, String author, String genre, String isbn, Integer pageNumber) {
        super(title, author, genre);
        this.isbn = isbn;
        this.pageNumber = pageNumber;
    }

    public Book(String title, String author, String genre, String isbn){
        super(title, author, genre);
        this.isbn = isbn;
    }

    public String getIsbn(){
        return(this.isbn);
    }

    public Integer getPageNumber(){
        return(this.pageNumber);
    }

    public void setIsbn(String isbn){
        this.isbn = isbn;
    }

    public void setPageNumber(Integer pageNumber){
        this.pageNumber = pageNumber;
    }
}