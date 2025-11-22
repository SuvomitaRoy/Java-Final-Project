package dao;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import model.Borrow;
import model.Document;
import model.Member;
import exception.BorrowException;

public class BorrowDAOImpl implements BorrowDAO {

    private Connection conn;

    public BorrowDAOImpl() {
        this.conn = DatabaseManager.getConnection();
    }

    @Override
    public boolean addBorrow(Member member, Document document) throws BorrowException {
        if (member == null || document == null) {
            throw new BorrowException("Member or Document is null");
        }

        try {
            String sql = "INSERT INTO Borrow (id, id_doc, idMember, borrowDate, expectedReturnDate, returnDate) " +
                         "VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);

            String borrowId = UUID.randomUUID().toString();
            LocalDate borrowDate = LocalDate.now();
            LocalDate expectedReturn = borrowDate.plusDays(14);

            stmt.setString(1, borrowId);
            stmt.setInt(2, document.getIdDoc());
            stmt.setInt(3, member.getIdMember());
            stmt.setString(4, borrowDate.toString());
            stmt.setString(5, expectedReturn.toString());
            stmt.setString(6, null); // initially null

            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            throw new BorrowException("Failed to add borrow: " + e.getMessage());
        }
    }

    @Override
    public void removeBorrow(Borrow borrow) throws BorrowException {
        if (borrow == null) throw new BorrowException("Borrow is null");

        try {
            String sql = "DELETE FROM Borrow WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, borrow.getId());
            int rows = stmt.executeUpdate();

            if (rows == 0) throw new BorrowException("Borrow not found");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new BorrowException("Failed to remove borrow: " + e.getMessage());
        }
    }

    @Override
    public List<Borrow> getCurrentBorrows() {
        return getBorrows("SELECT * FROM Borrow WHERE returnDate IS NULL");
    }

    @Override
    public List<Borrow> getLateBorrows() {
        return getBorrows("SELECT * FROM Borrow WHERE returnDate IS NULL AND expectedReturnDate < ?");
    }

    // ------------------ Helper Methods ------------------

    private List<Borrow> getBorrows(String sql) {
        List<Borrow> borrows = new ArrayList<>();
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            if (sql.contains("?")) {
                stmt.setString(1, LocalDate.now().toString());
            }
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                borrows.add(mapResultSetToBorrow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return borrows;
    }

    private Borrow mapResultSetToBorrow(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        int docId = rs.getInt("id_doc");
        int memberId = rs.getInt("idMember");

        // Use existing DAO methods to fetch objects
        Document doc = new DocumentDAOImpl().getDocumentById(docId);
        Member member = new MemberDAOImpl().searchMemberById(memberId); 

        LocalDate borrowDate = rs.getString("borrowDate") != null ? LocalDate.parse(rs.getString("borrowDate")) : null;
        Borrow borrow = new Borrow(doc, member, borrowDate);
        borrow.setId(id);

        LocalDate expectedReturn = rs.getString("expectedReturnDate") != null
                ? LocalDate.parse(rs.getString("expectedReturnDate")) : null;
        borrow.setExpectedReturnDate(expectedReturn);

        LocalDate returnDate = rs.getString("returnDate") != null
                ? LocalDate.parse(rs.getString("returnDate")) : null;
        borrow.setReturnDate(returnDate);

        return borrow;
    }
}
