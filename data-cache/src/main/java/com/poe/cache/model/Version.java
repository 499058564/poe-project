package com.poe.cache.model;

public class Version {
    /** Wiki page ID */
    private int pageId;
    /** Wiki page name */
    private String pageName;
    /** Previous version identifier */
    private String after;
    /** Major version number */
    private int majorPart;
    /** Minor version number */
    private int minorPart;
    /** Patch version number */
    private int patchPart;
    /** Parent version identifier */
    private String previous;
    /** Release date string */
    private String releaseDate;
    /** Revision part string */
    private String revisionPart;
    /** Version identifier */
    private String version;

    public Version() {}

    public int getPageId() { return pageId; }
    public void setPageId(int pageId) { this.pageId = pageId; }
    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }
    public String getAfter() { return after; }
    public void setAfter(String after) { this.after = after; }
    public int getMajorPart() { return majorPart; }
    public void setMajorPart(int majorPart) { this.majorPart = majorPart; }
    public int getMinorPart() { return minorPart; }
    public void setMinorPart(int minorPart) { this.minorPart = minorPart; }
    public int getPatchPart() { return patchPart; }
    public void setPatchPart(int patchPart) { this.patchPart = patchPart; }
    public String getPrevious() { return previous; }
    public void setPrevious(String previous) { this.previous = previous; }
    public String getReleaseDate() { return releaseDate; }
    public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }
    public String getRevisionPart() { return revisionPart; }
    public void setRevisionPart(String revisionPart) { this.revisionPart = revisionPart; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
}
