package org.comic_con.museum.fcb.endpoints.inputs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.comic_con.museum.fcb.models.Artifact;
import org.comic_con.museum.fcb.models.User;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;

public class ArtifactCreation {
    private String title;
    private String description;
    private String filename;
    private boolean cover;
    @JsonIgnore
    private MultipartFile file;
    
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getFilename() { return filename; }
    public boolean isCover() { return cover; }
    public MultipartFile getFile() { return file; }
    
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setFilename(String filename) { this.filename = filename; }
    public void setCover(boolean cover) { this.cover = cover; }
    public void setFile(MultipartFile file) { this.file = file; }
    
    public Artifact build(User by) {
        return new Artifact(0, title, description, cover, 0, by.getId(), Instant.now());
    }
}
