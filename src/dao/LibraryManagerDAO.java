package dao;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.ArrayList;
import model.*;
import exception.*;

public class LibraryManagerDAO {
    private MemberDAO memberDAO;
    private DocumentDAO documentDAO;
    private BookDAO bookDAO;      
    private BorrowDAO borrowDAO;

    private static final int MAX_BORROWS_PER_MEMBER = 5;
    private static final double PENALTY_PER_DAY = 0.5;

    public LibraryManagerDAO(MemberDAO memberDAO, DocumentDAO documentDAO,
                          BookDAO bookDAO, BorrowDAO borrowDAO) {
        this.memberDAO = memberDAO;
        this.documentDAO = documentDAO;
        this.bookDAO = bookDAO;
        this.borrowDAO = borrowDAO;
    }

    // -------------------- Members --------------------
    public void addMember(Member member) {
        memberDAO.addMember(member);
    }

    public Member searchMemberById(Integer id) {
        try {
            return memberDAO.searchMemberById(id);
        } catch (MemberNotFoundException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private List<Member> members = new ArrayList<>();
    public Member searchMemberByName(String name, String surname) {
        for (Member m : members) {
            if (m.getName().equalsIgnoreCase(name) &&
                m.getSurname().equalsIgnoreCase(surname)) {
                return m;
            }
        }
        return null;
    }

    public void updateMember(Member member, String name, String surname, PenaltyStatus penaltyStatus) {
        member.setName(name);
        member.setSurname(surname);
        member.setPenaltyStatus(penaltyStatus);
        memberDAO.updateMember(member, member.getName(), member.getSurname(), member.getPenaltyStatus());
    }

    public PenaltyStatus hasPenalty(Member member) {
        return member.getPenaltyStatus();
    }

        public List<Borrow> getMemberHistory(Member member) {
        return memberDAO.getMemberHistory(member);
    }

    // -------------------- Documents --------------------
    public void addDocument(Document document) {
        documentDAO.addDocument(document);
    }

    public Document getDocumentByTitle(String title) {
        return documentDAO.getDocumentByTitle(title);
    }

    public Document getDocumentByAuthor(String author) {
        return documentDAO.getDocumentByAuthor(author);
    }

    public Document getDocumentByGenre(String genre) {
        return documentDAO.getDocumentByGenre(genre);
    }

    public Book getBookByIsbn(String isbn) {
        if (bookDAO != null) {
            return bookDAO.getBookByIsbn(isbn);
        } 
        throw new DocumentNotFoundException("No book found with ISBN = " + isbn);
    }

    public void updateDocumentAttributes(Document doc,
                                         String newTitle,
                                         String newAuthor,
                                         String newGenre,
                                         String newIsbn,
                                         Integer newPageNumber,
                                         Integer newNumber,
                                         Magazine.Periodicity newPeriodicity) {
        try {
            documentDAO.updateDocumentAttributes(doc, newTitle, newAuthor, newGenre,
                                                newIsbn, newPageNumber, newNumber, newPeriodicity);
        } catch (DocumentNotFoundException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void removeDocument(Document document) {
        documentDAO.removeDocument(document);
    }

    // -------------------- Borrows --------------------
    public boolean addBorrow(Member member, Document document) {
        if (member == null) throw new MemberNotFoundException("Member is null.");
        if (document == null) throw new DocumentNotFoundException("Document is null.");
        if (!document.isAvailable()) throw new BorrowException("Document is not available.");
        if (member.getNbBorrows() >= MAX_BORROWS_PER_MEMBER)
            throw new BorrowException("This member cannot borrow more documents.");
        if (member.getPenaltyStatus().getLevel() >= 2)
            throw new BorrowException("This member is suspended or banned.");

        for (Borrow b : memberDAO.getMemberHistory(member)) {
            if (b.getReturnDate() == null &&
                b.getExpectedReturnDate().isBefore(LocalDate.now())) {
                throw new BorrowException("Member has overdue items.");
            }
        }

        Borrow borrow = new Borrow(document, member, LocalDate.now());
        borrowDAO.addBorrow(borrow.getMember(), borrow.getDocument()); 

        document.setAvailability(false);
        member.setNbBorrows(member.getNbBorrows() + 1);
        memberDAO.updateMember(member, member.getName(), member.getSurname(), member.getPenaltyStatus());
;

        return true;
    }

    public void removeBorrow(Borrow borrow) {
        if (borrow == null) throw new BorrowException("Borrow is null.");
        if (borrow.getReturnDate() != null) throw new BorrowException("This borrow has already been returned.");

        LocalDate today = LocalDate.now();
        if (borrow.getExpectedReturnDate().isBefore(today)) {
            long delay = ChronoUnit.DAYS.between(borrow.getExpectedReturnDate(), today);
            double penalty = delay * PENALTY_PER_DAY;
            borrow.getMember().setPenalty(borrow.getMember().getPenalty() + penalty);
            Member m = borrow.getMember();
            memberDAO.updateMember(m, m.getName(), m.getSurname(), m.getPenaltyStatus());

        }

        borrow.getDocument().setAvailability(true);
        borrow.getMember().setNbBorrows(borrow.getMember().getNbBorrows() - 1);
        Member m = borrow.getMember();
        memberDAO.updateMember(m, m.getName(), m.getSurname(), m.getPenaltyStatus());


        borrow.setReturnDate(today);
        borrowDAO.removeBorrow(borrow);
    }

    private List<Borrow> borrows = new ArrayList<>();
    public List<Borrow> getCurrentBorrows() {
        List<Borrow> current = new ArrayList<>();
        for (Borrow b : borrows) {
            if (!b.isReturned()) {
                current.add(b);
            }
        }
        return current;
    }


    public List<Borrow> getLateBorrows() {
        return borrowDAO.getLateBorrows();
    }
}
