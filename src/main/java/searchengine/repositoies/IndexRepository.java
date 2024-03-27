package searchengine.repositoies;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import searchengine.model.Index;
import searchengine.model.Page;

import javax.transaction.Transactional;

@Repository
public interface IndexRepository extends JpaRepository<Index, Integer> {
    @Transactional
    @Modifying
    void deleteByPage(Page page);
}
