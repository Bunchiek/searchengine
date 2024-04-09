package searchengine.repositoies;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.services.Result;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LemmaRepository extends JpaRepository<Lemma, Integer> {

    Lemma findByLemma(String lemma);

    Lemma findFirstByLemmaAndSite(String lemma, Site site);
    List<Lemma> findBySite(Site site);



    @Transactional
    @Modifying
    void deleteBySite(Site site);

    @Transactional
    @Modifying
    @Query("delete from Lemma where site = :site")
    void deleteChistoEpta(@Param("site") Site site);

    @Transactional
    @Modifying
    @Query("update Lemma set frequency = :frequency where id = :id")
    void updateLemmaFrequency(@Param("frequency")Integer frequency, @Param("id") int id);
}
