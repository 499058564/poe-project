package com.poe.cache.model;

public class IncursionRooms {
    /** Wiki page ID */
    private int pageId;
    /** Wiki page name */
    private String pageName;
    /** Architect metadata identifier */
    private String architectMetadataId;
    /** Architect display name */
    private String architectName;
    /** Room description */
    private String description;
    /** Room flavour text */
    private String flavourText;
    /** Room icon */
    private String icon;
    /** Room identifier */
    private String roomId;
    /** Minimum area level */
    private int minLevel;
    /** Modifier IDs (comma-separated) */
    private String modifierIds;
    /** Room display name */
    private String name;
    /** Stat display text */
    private String statText;
    /** Room tier */
    private int tier;
    /** Upgrade target room identifier */
    private String upgradeRoomId;

    public IncursionRooms() {}

    public int getPageId() { return pageId; }
    public void setPageId(int pageId) { this.pageId = pageId; }
    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }
    public String getArchitectMetadataId() { return architectMetadataId; }
    public void setArchitectMetadataId(String architectMetadataId) { this.architectMetadataId = architectMetadataId; }
    public String getArchitectName() { return architectName; }
    public void setArchitectName(String architectName) { this.architectName = architectName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getFlavourText() { return flavourText; }
    public void setFlavourText(String flavourText) { this.flavourText = flavourText; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
    public int getMinLevel() { return minLevel; }
    public void setMinLevel(int minLevel) { this.minLevel = minLevel; }
    public String getModifierIds() { return modifierIds; }
    public void setModifierIds(String modifierIds) { this.modifierIds = modifierIds; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getStatText() { return statText; }
    public void setStatText(String statText) { this.statText = statText; }
    public int getTier() { return tier; }
    public void setTier(int tier) { this.tier = tier; }
    public String getUpgradeRoomId() { return upgradeRoomId; }
    public void setUpgradeRoomId(String upgradeRoomId) { this.upgradeRoomId = upgradeRoomId; }
}
