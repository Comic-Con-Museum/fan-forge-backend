package org.comic_con.museum.fcb.endpoints;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.comic_con.museum.fcb.endpoints.inputs.ArtifactCreation;
import org.comic_con.museum.fcb.endpoints.inputs.ExhibitCreation;
import org.comic_con.museum.fcb.endpoints.responses.ExhibitFull;
import org.comic_con.museum.fcb.endpoints.responses.Feed;
import org.comic_con.museum.fcb.models.Artifact;
import org.comic_con.museum.fcb.persistence.*;
import org.comic_con.museum.fcb.models.Exhibit;
import org.comic_con.museum.fcb.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@RestController
public class ExhibitEndpoints {
    private static final Logger LOG = LoggerFactory.getLogger("endpoints.exhibit");
    private static final ObjectReader CREATE_PARAMS_READER = new ObjectMapper().readerFor(ExhibitCreation.class);

    private final ExhibitQueryBean exhibits;
    private final SupportQueryBean supports;
    private final ArtifactQueryBean artifacts;
    private final S3Bean s3;
    private final TransactionWrapper transactions;
    
    @Autowired
    public ExhibitEndpoints(ExhibitQueryBean exhibitQueryBean, SupportQueryBean supportQueryBean,
                            ArtifactQueryBean artifacts, S3Bean s3, TransactionWrapper transactionWrapperBean) {
        this.exhibits = exhibitQueryBean;
        this.supports = supportQueryBean;
        this.artifacts = artifacts;
        this.s3 = s3;
        this.transactions = transactionWrapperBean;
    }

    // TODO separate out into its own class?
    @RequestMapping(value = "/feed/{type}", method = RequestMethod.GET)
    public ResponseEntity<Feed> getFeed(@PathVariable("type") String feedName, @RequestParam int startIdx,
                                        @AuthenticationPrincipal User user) {
        ExhibitQueryBean.FeedType feed;
        switch (feedName) {
            case "new":
                LOG.info("NEW feed");
                feed = ExhibitQueryBean.FeedType.NEW;
                break;
            case "alphabetical":
                LOG.info("ALPHABETICAL feed");
                feed = ExhibitQueryBean.FeedType.ALPHABETICAL;
                break;
            default:
                LOG.info("Unknown feed: {}", feedName);
                // 404 instead of 400 because they're trying to hit a
                // nonexistent endpoint (/feed/whatever), not passing bad data
                // to a real endpoint
                return ResponseEntity.notFound().build();
        }
        
        long count;
        List<Feed.Entry> entries;
        try (TransactionWrapper.Transaction tr = transactions.start()) {
            count = exhibits.getCount();
            // This can definitely be combined into one query if necessary
            // or even just two (instead of PAGE_SIZE+1)
            List<Exhibit> feedRaw = exhibits.getFeedBy(feed, startIdx);
            entries = new ArrayList<>(feedRaw.size());
            for (Exhibit exhibit : feedRaw) {
                entries.add(new Feed.Entry(
                        exhibit, supports.supporterCount(exhibit),
                        supports.isSupporting(user, exhibit)
                ));
            }
            tr.commit();
        } // no catch because we're just closing the transaction, we want errors to fall through
        return ResponseEntity.ok(new Feed(startIdx, count, entries));
    }

    @RequestMapping(value = "/exhibit/{id}")
    public ResponseEntity<ExhibitFull> getExhibit(@PathVariable long id, @AuthenticationPrincipal User user) {
        Exhibit e = exhibits.getById(id);
        return ResponseEntity.ok(new ExhibitFull(
                e,
                supports.supporterCount(e),
                supports.isSupporting(user, e),
                artifacts.artifactsOfExhibit(id)
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
                MultipartFile file = req.getFile(a.getImageName());
                long imageId = s3.putImage(file);
                Artifact full = a.build(user);
                full.setImage(imageId);
                artifacts.create(full, id, user);
            }
            
            t.commit();
        }
        return ResponseEntity.ok(id);
    }
    
    @RequestMapping(value = "/exhibit/{id}", method = RequestMethod.PUT, consumes = "multipart/form-data")
    public ResponseEntity<ExhibitFull> editExhibit(@PathVariable long id, MultipartHttpServletRequest req,
                                                   @RequestParam("data") String dataString,
                                                   @AuthenticationPrincipal User user) throws IOException {
        ExhibitCreation data = CREATE_PARAMS_READER.readValue(dataString);
        Exhibit ex = data.build(user);
        ex.setId(id);
        ExhibitFull resp;
        // TODO Update to match POST
        try (TransactionWrapper.Transaction t = transactions.start()) {
            for (MultipartFile file : req.getFiles("cover")) {
                LOG.info("Cover {} {} a valid image of type {}", file.getOriginalFilename(),
                        ImageIO.read(file.getInputStream()) != null ? "is" : "is not",
                        file.getContentType());
            }
            exhibits.update(ex, user);
            resp = new ExhibitFull(
                    exhibits.getById(ex.getId()),
                    supports.supporterCount(ex),
                    supports.isSupporting(user, ex),
                    artifacts.artifactsOfExhibit(id)
            );
            t.commit();
        }
        return ResponseEntity.ok(resp);
    }

    @RequestMapping(value = "/exhibit/{id}", method = RequestMethod.DELETE)
    public ResponseEntity deleteExhibit(@PathVariable long id, @AuthenticationPrincipal User user) {
        exhibits.delete(id, user);
        return ResponseEntity.noContent().build();
    }
}
