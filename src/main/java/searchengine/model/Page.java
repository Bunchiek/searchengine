package searchengine.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Setter
@Getter
@Entity
@Table(name = "page")
public class Page {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(name = "path", columnDefinition = "TEXT, INDEX page_path_index USING BTREE (path(50))")
    private String path;
    @Column(nullable = false)
    private int code;
    @Column(columnDefinition = "MEDIUMTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",nullable = false)
    private String content;
    @ManyToOne()
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;
    @Transient
    private List<Page> children = new ArrayList<>();
    public Page() {
    }

    public Page(String path, int code, String content, Site site) {
        this.path = path;
        this.code = code;
        this.content = content;
        this.site = site;
    }

    public void addChild(Page page) {
        children.add(page);
    }
}
