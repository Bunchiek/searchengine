package searchengine.repositoies;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.model.Lemma;
import searchengine.model.Site;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface LemmaRepository extends JpaRepository<Lemma, Integer> {

    Lemma findByLemmaAndSite(String lemma, Site site);
    List<Lemma> findBySite(Site site);

    @Transactional
    @Modifying
    @Query("update Lemma set frequency = :frequency where id = :id")
    void updateLemmaFrequency(@Param("frequency")Integer frequency, @Param("id") int id);
}
