package org.comic_con.museum.fcb.endpoints.inputs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.comic_con.museum.fcb.models.Artifact;
import org.comic_con.museum.fcb.models.User;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;

public class ArtifactCreation {
    private Long id;
    private String title;
    private String description;
    private String imageName;
    private boolean cover;
    @JsonIgnore
    private MultipartFile file;

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getImageName() { return imageName; }
    public boolean isCover() { return cover; }
    public MultipartFile getFile() { return file; }

    public void setId(Long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setImageName(String imageName) { this.imageName = imageName; }
    public void setCover(boolean cover) { this.cover = cover; }
    public void setFile(MultipartFile file) { this.file = file; }
    
    public Artifact build(User by) {
        return new Artifact(id == null ? 0 : id, title, description, cover, by.getId(), Instant.now());
    }
}
