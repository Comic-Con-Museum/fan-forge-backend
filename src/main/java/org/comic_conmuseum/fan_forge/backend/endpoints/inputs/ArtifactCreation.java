package org.comic_conmuseum.fan_forge.backend.endpoints.inputs;

import org.comic_conmuseum.fan_forge.backend.models.Artifact;
import org.comic_conmuseum.fan_forge.backend.models.User;

import java.time.Instant;

public class ArtifactCreation {
    private Long id;
    private String title;
    private String description;
    private String imageName;
    private Long parent;
    private boolean cover;

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getImageName() { return imageName; }
    public Long getParent() { return parent; }
    public boolean isCover() { return cover; }

    public void setId(Long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setImageName(String imageName) { this.imageName = imageName; }
    public void setParent(Long parent) { this.parent = parent; }
    public void setCover(boolean cover) { this.cover = cover; }
    
    public Artifact build(User by) {
        return new Artifact(id == null ? 0 : id, title, description, cover, by.getId(), Instant.now());
    }
}
