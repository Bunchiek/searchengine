package searchengine.repositoies;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import searchengine.model.Page;
import searchengine.model.Site;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface PageRepository extends JpaRepository<Page,Integer> {

    List<Page> findBySite(Site site);

    Page findByPath(@Param("path") String path);
    @Transactional
    @Modifying
    void deleteBySite(Site site);
}
