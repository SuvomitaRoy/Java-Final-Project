package dao;

import model.Member;
import model.PenaltyStatus;
import model.Borrow;
import exception.MemberNotFoundException;
import java.util.List;

public interface MemberDAO {
    void addMember(Member member);
    Member searchMemberById(int memberId) throws MemberNotFoundException;
    Member searchMemberByName(String name, String surname);
    void updateMember(Member member, String name, String surname, PenaltyStatus penaltyStatus);
    List<Borrow> getMemberHistory(Member member);
    PenaltyStatus hasPenalty(Member member);
}

