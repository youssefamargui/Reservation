package reservation.reservation.dao;

import org.hibernate.Session;
import org.hibernate.Transaction;
import reservation.reservation.model.Reservation;

import java.util.List;

public class ReservationDAO {

    public List<Reservation> getReservationsByRespo(Long respoId) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<Reservation> reservations = session.createQuery(
                        "FROM Reservation r WHERE r.respo.id = :id", Reservation.class)
                .setParameter("id", respoId)
                .getResultList();
        session.close();
        return reservations;
    }

    public void update(Reservation reservation) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();
        session.update(reservation);
        tx.commit();
        session.close();
    }
}
