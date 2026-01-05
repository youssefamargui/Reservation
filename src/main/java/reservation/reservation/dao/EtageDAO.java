package reservation.reservation.dao;

import org.hibernate.Session;
import reservation.reservation.model.Etage;

import java.util.List;

public class EtageDAO {

    public List<Etage> getEtagesByRespo(Long respoId) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<Etage> etages = session.createQuery(
                        "FROM Etage e WHERE e.respo.id = :id", Etage.class)
                .setParameter("id", respoId)
                .getResultList();
        session.close();
        return etages;
    }

    public Etage findById(int id) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Etage etage = session.get(Etage.class, id);
        session.close();
        return etage;
    }
}
