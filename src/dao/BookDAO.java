package dao;

import model.Book;
import exception.DocumentNotFoundException;
import java.util.List;

public interface BookDAO {
    void addBook(Book book) throws Exception;
    Book getBookByIsbn(String isbn) throws DocumentNotFoundException;
}