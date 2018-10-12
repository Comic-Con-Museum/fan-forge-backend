package org.comic_con.museum.fcb.controllers.responses;

import java.util.List;

public class Feed {
    public List<ExhibitAbbreviated> exhibits;
    public int startIdx;
    public int count;
    public int pageSize;

    public List<ExhibitAbbreviated> getExhibits() { return exhibits; }
    public int getStartIdx() { return startIdx; }
    public int getCount() { return count; }
    public int getPageSize() { return pageSize; }
}
