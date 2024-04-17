package searchengine.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
@Setter
@Getter
@Entity
@Table(name = "`index`")
public class Index {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @ManyToOne
    @JoinColumn(name = "lemma_id", nullable = false)
    Lemma lemma;
    @ManyToOne
    @JoinColumn(name = "page_id", nullable = false)
    Page page;
    @Column(name = "`rank`", nullable = false)
    private float rank ;

    public Index(Lemma lemma, Page page, float rank) {
        this.lemma = lemma;
        this.page = page;
        this.rank = rank;
    }

    public Index() {
    }
}

