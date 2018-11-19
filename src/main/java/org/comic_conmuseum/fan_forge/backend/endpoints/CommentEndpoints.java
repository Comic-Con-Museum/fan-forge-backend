package org.comic_conmuseum.fan_forge.backend.endpoints;

import org.comic_conmuseum.fan_forge.backend.endpoints.inputs.CommentCreation;
import org.comic_conmuseum.fan_forge.backend.endpoints.responses.CommentView;
import org.comic_conmuseum.fan_forge.backend.models.Comment;
import org.comic_conmuseum.fan_forge.backend.models.User;
import org.comic_conmuseum.fan_forge.backend.persistence.CommentQueryBean;
import org.comic_conmuseum.fan_forge.backend.persistence.TransactionWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;

@RestController
public class CommentEndpoints {
    private static final Logger LOG = LoggerFactory.getLogger("endpoints.comment");
    
    private final CommentQueryBean comments;
    private final TransactionWrapper transactions;
    
    @Autowired
    public CommentEndpoints(CommentQueryBean comments, TransactionWrapper transactions) {
        this.comments = comments;
        this.transactions = transactions;
    }
    
    @RequestMapping(value = "/comment/{id}", method = RequestMethod.GET)
    public ResponseEntity<CommentView> getComment(@PathVariable long id) {
        return ResponseEntity.ok(new CommentView(comments.get(id)));
    }
    
    @RequestMapping(value = "/comment", method = RequestMethod.POST)
    public ResponseEntity<Long> createComment(@RequestBody CommentCreation data,
                                              @AuthenticationPrincipal User user) throws SQLException {
        if (data.getParent() == null || data.getText() == null) {
            LOG.info("Required param not specified");
            return ResponseEntity.badRequest().build();
        }
        Comment full = data.build(user);
        
        try (TransactionWrapper.Transaction t = transactions.start()) {
            final long id = comments.create(full, data.getParent(), user);
            t.commit();
            return ResponseEntity.ok(id);
        }
    }
    
    @RequestMapping(value = "/comment/{id}", method = RequestMethod.POST)
    public ResponseEntity<CommentView> editComment(@PathVariable long id, @RequestBody CommentCreation data,
                                                   @AuthenticationPrincipal User user) {
        Comment full = data.build(user);
        full.setId(id);
        
        try (TransactionWrapper.Transaction t = transactions.start()) {
            comments.update(full, user);
            t.commit();
            return ResponseEntity.noContent().build();
        }
    }
    
    @RequestMapping(value = "/comment/{id}", method = RequestMethod.DELETE)
    public ResponseEntity deleteComment(@PathVariable long id, @AuthenticationPrincipal User user) {
        comments.delete(id, user);
        return ResponseEntity.noContent().build();
    }
}
