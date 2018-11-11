package org.comic_con.museum.fcb.endpoints;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.comic_con.museum.fcb.endpoints.inputs.ExhibitCreation;
import org.comic_con.museum.fcb.endpoints.responses.ExhibitFull;
import org.comic_con.museum.fcb.endpoints.responses.Feed;
import org.comic_con.museum.fcb.persistence.S3Bean;
import org.comic_con.museum.fcb.persistence.SupportQueryBean;
import org.comic_con.museum.fcb.persistence.TransactionWrapper;
import org.comic_con.museum.fcb.models.Exhibit;
import org.comic_con.museum.fcb.models.User;
import org.comic_con.museum.fcb.persistence.ExhibitQueryBean;
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
    private final S3Bean s3;
    private final TransactionWrapper transactions;
    
    @Autowired
    public ExhibitEndpoints(ExhibitQueryBean exhibitQueryBean, SupportQueryBean supportQueryBean,
                            S3Bean s3, TransactionWrapper transactionWrapperBean) {
        this.exhibits = exhibitQueryBean;
        this.supports = supportQueryBean;
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
                // 404 instead of 400 because they're trying to hit a nonexistent endpoint (/feed/whatever), not passing
                // bad data to a real endpoint
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
        return ResponseEntity.ok(new ExhibitFull(e, supports.supporterCount(e), supports.isSupporting(user, e), null));
    }

    // TODO Return created exhibit, not just ID
    @RequestMapping(value = "/exhibit", method = RequestMethod.POST, consumes = "multipart/form-data")
    public ResponseEntity<Long> createExhibit(MultipartHttpServletRequest req, @AuthenticationPrincipal User user) throws SQLException, IOException {
        String dataString = req.getParameter("data");
        if (dataString == null) {
            LOG.info("No data part in body");
            return ResponseEntity.badRequest().build();
        }
        ExhibitCreation data = CREATE_PARAMS_READER.readValue(dataString);
        if (null == data.getTitle() || null == data.getDescription() || null == data.getTags()) {
            LOG.info("Required field not provided");
            return ResponseEntity.badRequest().build();
        }

        List<MultipartFile> covers = req.getFiles("cover");
        if (covers.size() > 1) {
            throw new IllegalArgumentException("Only one cover can be specified");
        }
        MultipartFile cover = covers.size() == 0 ? null : covers.get(0);

        long id;
        try (TransactionWrapper.Transaction t = transactions.start()) {
            id = exhibits.create(data.build(user), user);

            if (cover != null) {
                s3.storeExhibitCover(id, cover);
            }
            
            t.commit();
        }
        return ResponseEntity.ok(id);
    }
    
    @RequestMapping(value = "/exhibit/{id}", method = RequestMethod.PUT, consumes = "multipart/form-data")
    public ResponseEntity<ExhibitFull> editExhibit(@PathVariable long id, MultipartHttpServletRequest req,
                                                   @AuthenticationPrincipal User user) throws IOException {
        String dataString = req.getParameter("data");
        if (dataString == null) {
            LOG.info("No data part in body");
            return ResponseEntity.badRequest().build();
        }
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
                    null
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
