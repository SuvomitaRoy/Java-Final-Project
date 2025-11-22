package dao;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import model.Book;
import exception.DocumentNotFoundException;

public class BookDAOImpl implements BookDAO {

    private Connection conn;

    public BookDAOImpl() {
        this.conn = DatabaseManager.getConnection();
    }

    @Override
    public void addBook(Book book) throws DocumentNotFoundException {

        String insertDocumentSQL = 
            "INSERT INTO Document (title, author, genre) VALUES (?, ?, ?)";

        String insertBookSQL = 
            "INSERT INTO Book (title, isbn, pageNumber, author, genre, id_doc) VALUES (?, ?, ?, ?, ?, ?)";

        try {
            // 1️⃣ Insert into Document table first
            PreparedStatement stmtDoc = conn.prepareStatement(insertDocumentSQL, Statement.RETURN_GENERATED_KEYS);
            stmtDoc.setString(1, book.getTitle());
            stmtDoc.setString(2, book.getAuthor());
            stmtDoc.setString(3, book.getGenre());
            stmtDoc.executeUpdate();

            // 2️⃣ Retrieve generated id_doc
            ResultSet rs = stmtDoc.getGeneratedKeys();
            if (!rs.next()) {
                throw new DocumentNotFoundException("Failed to retrieve generated id_doc.");
            }
            int id_doc = rs.getInt(1);
            book.setIdDoc(id_doc); // Book inherits id from Document

            // 3️⃣ Insert into Book table
            PreparedStatement stmtBook = conn.prepareStatement(insertBookSQL);
            stmtBook.setString(1, book.getTitle());
            stmtBook.setString(2, book.getIsbn());
            stmtBook.setInt(3, book.getPageNumber());
            stmtBook.setString(4, book.getAuthor());
            stmtBook.setString(5, book.getGenre());
            stmtBook.setInt(6, id_doc);

            stmtBook.executeUpdate();

            System.out.println("Book added with id_doc=" + id_doc);

        } catch (SQLException e) {
            e.printStackTrace();
            throw new DocumentNotFoundException("Failed to add book: " + e.getMessage());
        }
    }


    @Override
    public Book getBookByIsbn(String isbn) throws DocumentNotFoundException {
        try {
            // Join Book table with Document table to get common fields
            String sql = "SELECT b.title AS book_title, b.isbn AS isbn, b.pageNumber AS pageNumber, " +
                         "d.id_doc, d.author, d.genre " +
                         "FROM Book b " +
                         "JOIN Document d ON b.id_doc = d.id_doc " +
                         "WHERE b.isbn = ?";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, isbn);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Book book = new Book(
                    rs.getString("book_title"),
                    rs.getString("author"),
                    rs.getString("genre"),
                    rs.getString("isbn"),
                    rs.getInt("pageNumber")
                );
                book.setIdDoc(rs.getInt("id_doc"));  // store the document ID for foreign key
                return book;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        throw new DocumentNotFoundException("Book with ISBN " + isbn + " not found");
    }
}
