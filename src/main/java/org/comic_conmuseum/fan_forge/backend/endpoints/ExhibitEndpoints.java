package org.comic_conmuseum.fan_forge.backend.endpoints;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.comic_conmuseum.fan_forge.backend.endpoints.inputs.ArtifactCreation;
import org.comic_conmuseum.fan_forge.backend.endpoints.inputs.ExhibitCreation;
import org.comic_conmuseum.fan_forge.backend.endpoints.responses.ExhibitFull;
import org.comic_conmuseum.fan_forge.backend.endpoints.responses.Feed;
import org.comic_conmuseum.fan_forge.backend.models.Artifact;
import org.comic_conmuseum.fan_forge.backend.models.Exhibit;
import org.comic_conmuseum.fan_forge.backend.models.User;
import org.comic_conmuseum.fan_forge.backend.persistence.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class ExhibitEndpoints {
    private static final Logger LOG = LoggerFactory.getLogger("endpoints.exhibit");
    private static final ObjectReader CREATE_PARAMS_READER = new ObjectMapper().readerFor(ExhibitCreation.class);

    private final ExhibitQueryBean exhibits;
    private final SupportQueryBean supports;
    private final ArtifactQueryBean artifacts;
    private final CommentQueryBean comments;
    private final S3Bean s3;
    private final TransactionWrapper transactions;
    
    @Autowired
    public ExhibitEndpoints(ExhibitQueryBean exhibitQueryBean, SupportQueryBean supportQueryBean,
                            ArtifactQueryBean artifacts, CommentQueryBean comments, S3Bean s3,
                            TransactionWrapper transactionWrapperBean) {
        this.exhibits = exhibitQueryBean;
        this.supports = supportQueryBean;
        this.artifacts = artifacts;
        this.comments = comments;
        this.s3 = s3;
        this.transactions = transactionWrapperBean;
    }

    // TODO separate out into its own class?
    @RequestMapping(value = "/feed/{type}", method = RequestMethod.GET)
    public ResponseEntity<Feed> getFeed(@PathVariable("type") String feedName,
                                        @RequestParam(defaultValue = "0") long startIdx,
                                        @RequestParam(defaultValue = "10") int pageSize,
                                        // `filters` also includes other stuff, but that'll be filtered out by getFeed
                                        @RequestParam Map<String, String> filters,
                                        @AuthenticationPrincipal User user) {
        ExhibitQueryBean.FeedType feed = ExhibitQueryBean.FeedType.parse(feedName);
        if (feed == null) {
            LOG.info("Unknown feed: {}", feedName);
            // 404 instead of 400 because they're trying to hit a
            // nonexistent endpoint (/feed/whatever), not passing bad data
            // to a real endpoint
            return ResponseEntity.notFound().build();
        }
        if (pageSize > ExhibitQueryBean.MAX_PAGE_SIZE) {
            pageSize = ExhibitQueryBean.MAX_PAGE_SIZE;
        }
        try (TransactionWrapper.Transaction tr = transactions.start()) {
            long count = exhibits.getCount(filters);
            // This can definitely be combined into one query if necessary
            // or even just two (instead of (4*pageSize)+1)
            List<Exhibit> feedRaw = exhibits.getFeed(feed, startIdx, pageSize, filters);
            List<Feed.Entry> entries = feedRaw.stream().map(exhibit ->
                    new Feed.Entry(
                        exhibit, supports.getSupporterCount(exhibit),
                        comments.getCommentCount(exhibit),
                        supports.isSupportingExhibit(user, exhibit)
                    )
            ).collect(Collectors.toList());
            tr.commit();
            return ResponseEntity.ok(new Feed(startIdx, pageSize, count, entries));
        } // no catch because we're just closing the transaction, we want errors to fall through
    }

    @RequestMapping(value = "/exhibit/{id}", method = RequestMethod.GET)
    public ResponseEntity<ExhibitFull> getExhibit(@PathVariable long id, @AuthenticationPrincipal User user) {
        Exhibit e = exhibits.get(id);
        return ResponseEntity.ok(new ExhibitFull(
                e,
                supports.getSupporterCount(e),
                supports.isSupportingExhibit(user, e),
                artifacts.artifactsOfExhibit(id),
                comments.getComments(id)
        ));
    }
    
    // TODO Return created exhibit, not just ID
    @RequestMapping(value = "/exhibit", method = RequestMethod.POST, consumes = "multipart/form-data")
    public ResponseEntity<Long> createExhibit(MultipartHttpServletRequest req, @RequestParam("data") String dataString,
                                              @AuthenticationPrincipal User user) throws SQLException, IOException {
        ExhibitCreation data = CREATE_PARAMS_READER.readValue(dataString);
        if (null == data.getTitle() || null == data.getDescription() || null == data.getTags() || null == data.getArtifacts()) {
            LOG.info("Required field not provided");
            return ResponseEntity.badRequest().build();
        }

        long id;
        try (TransactionWrapper.Transaction t = transactions.start()) {
            id = exhibits.create(data.build(user), user);
            for (ArtifactCreation a : data.getArtifacts()) {
                Artifact full = a.build(user);
                long aid = artifacts.create(full, id, user);
                if (a.getImageName() == null) {
                    return ResponseEntity.badRequest().build();
                }
                MultipartFile file = req.getFile(a.getImageName());
                if (file == null) {
                    return ResponseEntity.badRequest().build();
                }
                s3.putImage(aid, file);
            }
            
            t.commit();
        }
        return ResponseEntity.ok(id);
    }

    // TODO Do we... need bulk edit of artifacts? This might be more painful than just a few individual requests
    @RequestMapping(value = "/exhibit/{id}", method = RequestMethod.POST, consumes = "multipart/form-data")
    public ResponseEntity<ExhibitFull> editExhibit(@PathVariable long id, MultipartHttpServletRequest req,
                                                   @RequestParam("data") String dataString,
                                                   @AuthenticationPrincipal User user) throws IOException, SQLException {
        ExhibitCreation data = CREATE_PARAMS_READER.readValue(dataString);
        // we don't care if things aren't specified, so don't validate that
        Exhibit ex = data.build(user);
        ex.setId(id);
        try (TransactionWrapper.Transaction t = transactions.start()) {
            exhibits.update(ex, user);
            // the rest won't be hit if the user isn't the author, because `update` throws an exception
            List<Long> mentioned = new ArrayList<>();
            for (ArtifactCreation a : data.getArtifacts()) {
                if (a.getId() != null) {
                    // if ID is provided, update the existing one
                    mentioned.add(a.getId());
                    artifacts.update(a.build(user));
                    // if no image provided, just don't update it!
                    if (a.getImageName() != null) {
                        MultipartFile file = req.getFile(a.getImageName());
                        if (file == null) {
                            return ResponseEntity.badRequest().build();
                        }
                        s3.putImage(a.getId(), file);
                    }
                } else {
                    // if no ID provided, create a new one
                    long aid = artifacts.create(a.build(user), id, user);
                    mentioned.add(aid);
                    // this time an image is required -- you can't have an artifact without one.
                    if (a.getImageName() == null) {
                        return ResponseEntity.badRequest().build();
                    }
                    MultipartFile file = req.getFile(a.getImageName());
                    if (file == null) {
                        return ResponseEntity.badRequest().build();
                    }
                    s3.putImage(a.getId(), file);
                }
            }
            artifacts.deleteAllFromExcept(ex.getId(), mentioned);
            t.commit();
        }
        return getExhibit(id, user);
    }

    @RequestMapping(value = "/exhibit/{id}", method = RequestMethod.DELETE)
    public ResponseEntity deleteExhibit(@PathVariable long id, @AuthenticationPrincipal User user) {
        exhibits.delete(id, user);
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value = "/tags", method = RequestMethod.GET)
    public ResponseEntity getAllTags() {
        List<String> results = exhibits.getAllTags();
        return ResponseEntity.ok(results);
    }

    @RequestMapping(value = "/admin/feature/{id}", method = RequestMethod.POST)
    public ResponseEntity createFeatured(@PathVariable long id) { // auth handled by WebSecurityConfig
        boolean found = exhibits.markFeatured(id);
        return found ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @RequestMapping(value = "/admin/feature/{id}", method = RequestMethod.DELETE)
    public ResponseEntity deleteFeatured(@PathVariable long id) { // auth handled by WebSecurityConfig
        boolean found = exhibits.deleteFeatured(id);
        return found ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
