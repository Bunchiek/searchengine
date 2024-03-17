package searchengine.model;

import javax.persistence.*;

@Entity
@Table(name = "`index`")
public class Index {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(name = "`rank`")
    private float rank ;

    @ManyToOne()
    @JoinColumn(name = "page_id", nullable = false)
    private Page page;
    @ManyToOne()
    @JoinColumn(name = "lemma_id", nullable = false)
    private Lemma lemma;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float getRank() {
        return rank;
    }

    public void setRank(float rank) {
        this.rank = rank;
    }
}

