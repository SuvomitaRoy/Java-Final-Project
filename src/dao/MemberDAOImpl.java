package dao;

import model.Member;
import model.Document;
import model.PenaltyStatus;
import model.Borrow;
import exception.MemberNotFoundException;
import java.time.LocalDate;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MemberDAOImpl implements MemberDAO {

    private Connection conn;

    public MemberDAOImpl() {
        this.conn = DatabaseManager.getConnection();
    }

    // ---------------- ADD ----------------
    @Override
    public void addMember(Member member) {
        String sql = "INSERT INTO Member (idMember, name, surname, PenaltyStatus) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, member.getIdMember());
            stmt.setString(2, member.getName());
            stmt.setString(3, member.getSurname());
            stmt.setInt(4, member.getPenaltyStatus().getLevel());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ---------------- SEARCH ----------------
    @Override
    public Member searchMemberById(int id) {
        String sql = "SELECT * FROM Member WHERE idMember = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Member(
                        rs.getInt("idMember"),
                        rs.getString("name"),
                        rs.getString("surname"),
                        PenaltyStatus.fromInt(rs.getInt("PenaltyStatus"))
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        throw new MemberNotFoundException("No member found with id = " + id);
    }

    @Override
    public Member searchMemberByName(String name, String surname) {
        String sql = "SELECT * FROM Member WHERE name = ? AND surname = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, surname);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Member(
                        rs.getInt("idMember"),
                        rs.getString("name"),
                        rs.getString("surname"),
                        PenaltyStatus.fromInt(rs.getInt("PenaltyStatus"))
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ---------------- UPDATE ----------------
    @Override
    public void updateMember(Member member, String name, String surname, PenaltyStatus penaltyStatus) {
        String sql = "UPDATE Member SET name = ?, surname = ?, PenaltyStatus = ? WHERE idMember = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name != null ? name : member.getName());
            stmt.setString(2, surname != null ? surname : member.getSurname());
            stmt.setInt(3, penaltyStatus != null ? penaltyStatus.getLevel() : member.getPenaltyStatus().getLevel());
            stmt.setInt(4, member.getIdMember());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ---------------- HISTORY ----------------
    @Override
    public List<Borrow> getMemberHistory(Member member) {
        List<Borrow> history = new ArrayList<>();
        if (member == null) return history;

        try {
            String sql = "SELECT * FROM Borrow WHERE idMember = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, member.getIdMember());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String borrowId = rs.getString("id");
                int docId = rs.getInt("id_doc");

                // Fetch Document
                Document doc = new DocumentDAOImpl().getDocumentById(docId);

                // Parse dates as LocalDate
                String borrowDateStr = rs.getString("borrowDate");
                LocalDate borrowDate = borrowDateStr != null ? LocalDate.parse(borrowDateStr) : null;

                String expectedReturnStr = rs.getString("expectedReturnDate");
                LocalDate expectedReturn = expectedReturnStr != null ? LocalDate.parse(expectedReturnStr) : null;

                String returnDateStr = rs.getString("returnDate");
                LocalDate returnDate = returnDateStr != null ? LocalDate.parse(returnDateStr) : null;

                // Create Borrow object
                Borrow borrow = new Borrow(doc, member, borrowDate);
                borrow.setId(borrowId);
                borrow.setExpectedReturnDate(expectedReturn);
                borrow.setReturnDate(returnDate);

                history.add(borrow);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return history;
    }


    // ---------------- PENALTY ----------------
    @Override
    public PenaltyStatus hasPenalty(Member member) {
        return member.getPenaltyStatus();
    }
}
