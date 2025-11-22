package dao;

import java.util.List;
import model.Document;
import model.Magazine;
import exception.DocumentNotFoundException;

public interface DocumentDAO {
    void addDocument(Document document);
    Document getDocumentByTitle(String title);
    Document getDocumentByAuthor(String author);
    Document getDocumentByGenre(String genre);
    void updateDocumentAttributes(Document doc,
                                        String newTitle,
                                        String newAuthor,
                                        String newGenre,
                                        String newIsbn,
                                        Integer newPageNumber,
                                        Integer newNumber,
                                        Magazine.Periodicity newPeriodicity) throws DocumentNotFoundException;
    void removeDocument(Document document);
}
