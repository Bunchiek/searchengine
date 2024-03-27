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
    @Column(name = "`rank`")
    private float rank ;
    @ManyToOne
    @JoinColumn(name = "page_id", nullable = false)
    private Page page;
    @ManyToOne
    @JoinColumn(name = "lemma_id", nullable = false)
    private Lemma lemma;

    public Index(){

    }


    public Index(float rank, Page page, Lemma lemma) {
        this.rank = rank;
        this.page = page;
        this.lemma = lemma;
    }
}

