package org.comic_con.museum.fcb.models;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Tag {
    @Id
    public int tid;
    public String name;
}
