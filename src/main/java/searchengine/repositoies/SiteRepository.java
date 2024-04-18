package searchengine.repositoies;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.model.Site;
import searchengine.model.Status;

import javax.transaction.Transactional;
import java.time.LocalDateTime;

@Repository
public interface SiteRepository extends JpaRepository<Site, Integer> {

    Site findSiteByUrl(String url);
    Site findByName(String path);

    @Transactional
    @Modifying
    void deleteByUrl(String url);

    @Transactional
    @Modifying
    @Query("update Site set statusTime = :time where id = :id")
    void updateSiteSetTimeForId(@Param("time")LocalDateTime localDateTime, @Param("id") int id);

    @Transactional
    @Modifying
    @Query("update Site set status = :state where id =:id")
    void updateSiteStatusById(@Param("state") Status status, @Param("id") int id);

    @Transactional
    @Modifying
    @Query("update Site set status = :state, lastError = :error  where id =:id")
    void updateSiteStatusAndError(@Param("state")Status status, @Param("error") String error, @Param("id") int id);

}



