package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.sql.Connection;
import model.Document;
import model.Book;
import model.Magazine;
import model.Magazine.Periodicity;
import exception.DocumentNotFoundException;

public class DocumentDAOImpl implements DocumentDAO {

    private Connection conn;

    public DocumentDAOImpl() {
        this.conn = DatabaseManager.getConnection();
    }

    // -------------------- ADD --------------------
    public void addDocument(Document document) {
        try {
            // Insert Document (let DB generate id)
            String sqlDoc = "INSERT INTO Document (title, author, genre) VALUES (?, ?, ?)";
            try (PreparedStatement stmtDoc = conn.prepareStatement(sqlDoc, Statement.RETURN_GENERATED_KEYS)) {
                stmtDoc.setString(1, document.getTitle());
                stmtDoc.setString(2, document.getAuthor());
                stmtDoc.setString(3, document.getGenre());
                stmtDoc.executeUpdate();

                try (ResultSet keys = stmtDoc.getGeneratedKeys()) {
                    int idDoc = -1;
                    if (keys.next()) {
                        idDoc = keys.getInt(1);
                    } else {
                        throw new SQLException("Failed to get generated id_doc.");
                    }

                    // Attempt to set generated id back on the in-memory object if a setter exists
                    try {
                        var m = document.getClass().getMethod("setId_Doc", int.class);
                        m.invoke(document, idDoc);
                    } catch (NoSuchMethodException nsme) {
                        try {
                            var m2 = document.getClass().getMethod("setIdDoc", int.class);
                            m2.invoke(document, idDoc);
                        } catch (Exception ignore) { /* ignore if no setter */ }
                    } catch (Exception ignore) { /* ignore */ }

                    // Insert into child table if Book or Magazine
                    if (document instanceof Book) {
                        Book book = (Book) document;
                        String sqlBook = "INSERT INTO Book (id_doc, title, isbn, pageNumber, author, genre) VALUES (?, ?, ?, ?, ?, ?)";
                        try (PreparedStatement stmtBook = conn.prepareStatement(sqlBook)) {
                            stmtBook.setInt(1, idDoc);
                            stmtBook.setString(2, book.getTitle());
                            stmtBook.setString(3, book.getIsbn());
                            stmtBook.setObject(4, book.getPageNumber());
                            stmtBook.setString(5, book.getAuthor());
                            stmtBook.setString(6, book.getGenre());
                            stmtBook.executeUpdate();
                        }
                    } else if (document instanceof Magazine) {
                        Magazine mag = (Magazine) document;
                        String sqlMag = "INSERT INTO Magazine (id_doc, title, author, genre, number, periodicity) VALUES (?, ?, ?, ?, ?, ?)";
                        try (PreparedStatement stmtMag = conn.prepareStatement(sqlMag)) {
                            stmtMag.setInt(1, idDoc);
                            stmtMag.setString(2, mag.getTitle());
                            stmtMag.setString(3, mag.getAuthor());
                            stmtMag.setString(4, mag.getGenre());
                            stmtMag.setObject(5, mag.getNumber());
                            stmtMag.setString(6, mag.getPeriodicity().name());
                            stmtMag.executeUpdate();
                        }
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // -------------------- GET --------------------
    public Document getDocumentByTitle(String title) {
        try {
            String sql = "SELECT id_doc FROM Document WHERE title = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, title);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int idDoc = rs.getInt("id_doc");
                return getFullDocumentById(idDoc);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Document getDocumentByAuthor(String author) {
        try {
            String sql = "SELECT id_doc FROM Document WHERE author = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, author);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int idDoc = rs.getInt("id_doc");
                return getFullDocumentById(idDoc);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Document getDocumentByGenre(String genre) {
        try {
            String sql = "SELECT id_doc FROM Document WHERE genre = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, genre);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int idDoc = rs.getInt("id_doc");
                return getFullDocumentById(idDoc);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Book getBookByIsbn(String isbn) {
        try {
            String sql = "SELECT id_doc FROM Book WHERE isbn = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, isbn);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int idDoc = rs.getInt("id_doc");
                Document doc = getFullDocumentById(idDoc);
                if (doc instanceof Book) return (Book) doc;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        throw new DocumentNotFoundException("No book found with ISBN = " + isbn);
    }

    // -------------------- UPDATE --------------------
    public void updateDocumentAttributes(Document doc,
                                         String newTitle,
                                         String newAuthor,
                                         String newGenre,
                                         String newIsbn,
                                         Integer newPageNumber,
                                         Integer newNumber,
                                         Periodicity newPeriodicity) {
        if (doc == null) throw new DocumentNotFoundException("Cannot update: document is null");

        try {
            int idDoc = getIdDocForDocument(doc);

            String sql = "UPDATE Document SET title = ?, author = ?, genre = ? WHERE id_doc = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, newTitle != null ? newTitle : doc.getTitle());
            stmt.setString(2, newAuthor != null ? newAuthor : doc.getAuthor());
            stmt.setString(3, newGenre != null ? newGenre : doc.getGenre());
            stmt.setInt(4, idDoc);
            stmt.executeUpdate();

            if (doc instanceof Book) {
                Book b = (Book) doc;
                String sqlBook = "UPDATE Book SET isbn = ?, pageNumber = ? WHERE id_doc = ?";
                PreparedStatement stmtBook = conn.prepareStatement(sqlBook);
                stmtBook.setString(1, newIsbn != null ? newIsbn : b.getIsbn());
                stmtBook.setObject(2, newPageNumber != null ? newPageNumber : b.getPageNumber());
                stmtBook.setInt(3, idDoc);
                stmtBook.executeUpdate();
            } else if (doc instanceof Magazine) {
                Magazine m = (Magazine) doc;
                String sqlMag = "UPDATE Magazine SET number = ?, periodicity = ? WHERE id_doc = ?";
                PreparedStatement stmtMag = conn.prepareStatement(sqlMag);
                stmtMag.setObject(1, newNumber != null ? newNumber : m.getNumber());
                stmtMag.setString(2, newPeriodicity != null ? newPeriodicity.name() : m.getPeriodicity().name());
                stmtMag.setInt(3, idDoc);
                stmtMag.executeUpdate();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // -------------------- REMOVE --------------------
    public void removeDocument(Document document) {
        try {
            int idDoc = getIdDocForDocument(document);

            if (document instanceof Book) {
                String sqlBook = "DELETE FROM Book WHERE id_doc = ?";
                PreparedStatement stmtBook = conn.prepareStatement(sqlBook);
                stmtBook.setInt(1, idDoc);
                stmtBook.executeUpdate();
            } else if (document instanceof Magazine) {
                String sqlMag = "DELETE FROM Magazine WHERE id_doc = ?";
                PreparedStatement stmtMag = conn.prepareStatement(sqlMag);
                stmtMag.setInt(1, idDoc);
                stmtMag.executeUpdate();
            }

            String sqlDoc = "DELETE FROM Document WHERE id_doc = ?";
            PreparedStatement stmtDoc = conn.prepareStatement(sqlDoc);
            stmtDoc.setInt(1, idDoc);
            stmtDoc.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // -------------------- HELPERS --------------------
    public Document getDocumentById(int idDoc) {
        try {
            return getFullDocumentById(idDoc); // reuse your helper
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }


    private Document getFullDocumentById(int idDoc) throws SQLException {
        // Check Book
        String sqlBook = "SELECT * FROM Book WHERE id_doc = ?";
        PreparedStatement stmtBook = conn.prepareStatement(sqlBook);
        stmtBook.setInt(1, idDoc);
        ResultSet rsBook = stmtBook.executeQuery();
        if (rsBook.next()) {
            return new Book(
                rsBook.getString("title"),
                rsBook.getString("author"),
                rsBook.getString("genre"),
                rsBook.getString("isbn"),
                (Integer) rsBook.getObject("pageNumber")
            );
        }

        // Check Magazine
        String sqlMag = "SELECT * FROM Magazine WHERE id_doc = ?";
        PreparedStatement stmtMag = conn.prepareStatement(sqlMag);
        stmtMag.setInt(1, idDoc);
        ResultSet rsMag = stmtMag.executeQuery();
        if (rsMag.next()) {
            return new Magazine(
                rsMag.getString("title"),
                rsMag.getString("author"),
                rsMag.getString("genre"),
                (Integer) rsMag.getObject("number"),
                Periodicity.valueOf(rsMag.getString("periodicity"))
            );
        }

        throw new SQLException("Document type not found for id_doc = " + idDoc);
    }

    private int getIdDocForDocument(Document doc) throws SQLException {
        String sql = "SELECT id_doc FROM Document WHERE title = ? AND author = ? AND genre = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, doc.getTitle());
        stmt.setString(2, doc.getAuthor());
        stmt.setString(3, doc.getGenre());
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) return rs.getInt("id_doc");
        throw new SQLException("Document not found in Document table.");
    }
}
