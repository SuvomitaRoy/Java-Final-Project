package dao;

import java.util.List;
import model.Member;
import model.Document;
import model.Borrow;
import exception.BorrowException;

public interface BorrowDAO {
    boolean addBorrow(Member member, Document document) throws BorrowException;
    void removeBorrow(Borrow borrow) throws BorrowException;
    List<Borrow> getCurrentBorrows();
    List<Borrow> getLateBorrows();
}