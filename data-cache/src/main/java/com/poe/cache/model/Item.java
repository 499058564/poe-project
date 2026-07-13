package com.poe.cache.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 基础物品实体，完整映射 Wiki items Cargo 表全部 78 个字段。
 * <p>
 * JSON 数组字段（如 tags、drop_areas）使用 {@code List<String>} 在 Java 层表示，
 * DAO 层负责 JSON ↔ TEXT 序列化/反序列化。
 */
public class Item {

    // ─── 主键与核心标识 ───
    private Integer id;
    private String name;
    private String nameZh;
    private String nameList;          // JSON array string: ["Vaal Fireball", "Fireball"]
    private String metadataId;
    private String pageName;          // _pageName from Wiki

    // ─── 分类 ───
    private String itemClass;         // class
    private String classId;
    private String frameType;
    private String rarity;
    private String rarityId;

    // ─── 关联 ───
    private String baseItem;
    private String baseItemId;
    private String baseItemPage;

    // ─── 尺寸 ───
    private int sizeX;
    private int sizeY;
    private String inventoryIcon;

    // ─── 属性需求 (核心值) ───
    private int requiredLevel;
    private int requiredLevelBase;
    private int requiredDexterity;
    private int requiredIntelligence;
    private int requiredStrength;

    // ─── 属性需求 (范围值) ───
    private int requiredLevelRangeAverage;
    private String requiredLevelRangeColour;
    private int requiredLevelRangeMaximum;
    private int requiredLevelRangeMinimum;
    private String requiredLevelRangeText;

    private int requiredDexterityRangeAverage;
    private String requiredDexterityRangeColour;
    private int requiredDexterityRangeMaximum;
    private int requiredDexterityRangeMinimum;
    private String requiredDexterityRangeText;

    private int requiredIntelligenceRangeAverage;
    private String requiredIntelligenceRangeColour;
    private int requiredIntelligenceRangeMaximum;
    private int requiredIntelligenceRangeMinimum;
    private String requiredIntelligenceRangeText;

    private int requiredStrengthRangeAverage;
    private String requiredStrengthRangeColour;
    private int requiredStrengthRangeMaximum;
    private int requiredStrengthRangeMinimum;
    private String requiredStrengthRangeText;

    // ─── 属性需求 (HTML) ───
    private String requiredLevelHtml;
    private String requiredDexterityHtml;
    private String requiredIntelligenceHtml;
    private String requiredStrengthHtml;

    // ─── 状态标记 ───
    private boolean dropEnabled;
    private int dropLevel;
    private int dropLevelMaximum;
    private boolean isAccountBound;
    private boolean isCorrupted;
    private boolean isDropRestricted;
    private boolean isEaterOfWorldsItem;
    private boolean isFractured;
    private boolean isInGame;
    private boolean isReplica;
    private boolean isSearingExarchItem;
    private boolean isSynthesised;
    private boolean isUnmodifiable;
    private boolean isVeiled;

    // ─── 属性文本 ───
    private String statText;
    private String explicitStatText;
    private String implicitStatText;

    // ─── 掉落 ───
    private String dropText;
    private List<String> dropAreas;
    private String dropAreasHtml;
    private List<String> dropMonsters;
    private List<String> dropRarityIds;

    // ─── 标签 ───
    private List<String> tags;
    private List<String> acquisitionTags;
    private List<String> influences;

    // ─── 描述与提示 ───
    private String description;
    private String flavourText;
    private String helpText;

    // ─── HTML ───
    private String html;
    private String infoboxHtml;
    private String metaboxHtml;

    // ─── 图标 ───
    private List<String> alternateArtInventoryIcons;

    // ─── 品质 ───
    private int quality;

    // ─── 版本 ───
    private String releaseVersion;
    private String removalVersion;

    // ─── Wiki ───
    private String wikiUrl;
    private String version;

    public Item() {
        this.dropAreas = new ArrayList<>();
        this.dropMonsters = new ArrayList<>();
        this.dropRarityIds = new ArrayList<>();
        this.tags = new ArrayList<>();
        this.acquisitionTags = new ArrayList<>();
        this.influences = new ArrayList<>();
        this.alternateArtInventoryIcons = new ArrayList<>();
    }

    public Item(Integer id, String name, String itemClass, String version) {
        this();
        this.id = id;
        this.name = name;
        this.itemClass = itemClass;
        this.version = version;
    }

    // ── ID ──
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    // ── Name ──
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getNameZh() { return nameZh; }
    public void setNameZh(String nameZh) { this.nameZh = nameZh; }

    public String getNameList() { return nameList; }
    public void setNameList(String nameList) { this.nameList = nameList; }

    public String getMetadataId() { return metadataId; }
    public void setMetadataId(String metadataId) { this.metadataId = metadataId; }

    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }

    // ── Class ──
    public String getItemClass() { return itemClass; }
    public void setItemClass(String itemClass) { this.itemClass = itemClass; }

    public String getClassId() { return classId; }
    public void setClassId(String classId) { this.classId = classId; }

    public String getFrameType() { return frameType; }
    public void setFrameType(String frameType) { this.frameType = frameType; }

    public String getRarity() { return rarity; }
    public void setRarity(String rarity) { this.rarity = rarity; }

    public String getRarityId() { return rarityId; }
    public void setRarityId(String rarityId) { this.rarityId = rarityId; }

    // ── Base Item ──
    public String getBaseItem() { return baseItem; }
    public void setBaseItem(String baseItem) { this.baseItem = baseItem; }

    public String getBaseItemId() { return baseItemId; }
    public void setBaseItemId(String baseItemId) { this.baseItemId = baseItemId; }

    public String getBaseItemPage() { return baseItemPage; }
    public void setBaseItemPage(String baseItemPage) { this.baseItemPage = baseItemPage; }

    // ── Size ──
    public int getSizeX() { return sizeX; }
    public void setSizeX(int sizeX) { this.sizeX = sizeX; }

    public int getSizeY() { return sizeY; }
    public void setSizeY(int sizeY) { this.sizeY = sizeY; }

    public String getInventoryIcon() { return inventoryIcon; }
    public void setInventoryIcon(String inventoryIcon) { this.inventoryIcon = inventoryIcon; }

    // ── Core Requirements ──
    public int getRequiredLevel() { return requiredLevel; }
    public void setRequiredLevel(int requiredLevel) { this.requiredLevel = requiredLevel; }

    public int getRequiredLevelBase() { return requiredLevelBase; }
    public void setRequiredLevelBase(int requiredLevelBase) { this.requiredLevelBase = requiredLevelBase; }

    public int getRequiredDexterity() { return requiredDexterity; }
    public void setRequiredDexterity(int requiredDexterity) { this.requiredDexterity = requiredDexterity; }

    public int getRequiredIntelligence() { return requiredIntelligence; }
    public void setRequiredIntelligence(int requiredIntelligence) { this.requiredIntelligence = requiredIntelligence; }

    public int getRequiredStrength() { return requiredStrength; }
    public void setRequiredStrength(int requiredStrength) { this.requiredStrength = requiredStrength; }

    // ── Range Requirements ──
    public int getRequiredLevelRangeAverage() { return requiredLevelRangeAverage; }
    public void setRequiredLevelRangeAverage(int requiredLevelRangeAverage) { this.requiredLevelRangeAverage = requiredLevelRangeAverage; }

    public String getRequiredLevelRangeColour() { return requiredLevelRangeColour; }
    public void setRequiredLevelRangeColour(String requiredLevelRangeColour) { this.requiredLevelRangeColour = requiredLevelRangeColour; }

    public int getRequiredLevelRangeMaximum() { return requiredLevelRangeMaximum; }
    public void setRequiredLevelRangeMaximum(int requiredLevelRangeMaximum) { this.requiredLevelRangeMaximum = requiredLevelRangeMaximum; }

    public int getRequiredLevelRangeMinimum() { return requiredLevelRangeMinimum; }
    public void setRequiredLevelRangeMinimum(int requiredLevelRangeMinimum) { this.requiredLevelRangeMinimum = requiredLevelRangeMinimum; }

    public String getRequiredLevelRangeText() { return requiredLevelRangeText; }
    public void setRequiredLevelRangeText(String requiredLevelRangeText) { this.requiredLevelRangeText = requiredLevelRangeText; }

    public int getRequiredDexterityRangeAverage() { return requiredDexterityRangeAverage; }
    public void setRequiredDexterityRangeAverage(int requiredDexterityRangeAverage) { this.requiredDexterityRangeAverage = requiredDexterityRangeAverage; }

    public String getRequiredDexterityRangeColour() { return requiredDexterityRangeColour; }
    public void setRequiredDexterityRangeColour(String requiredDexterityRangeColour) { this.requiredDexterityRangeColour = requiredDexterityRangeColour; }

    public int getRequiredDexterityRangeMaximum() { return requiredDexterityRangeMaximum; }
    public void setRequiredDexterityRangeMaximum(int requiredDexterityRangeMaximum) { this.requiredDexterityRangeMaximum = requiredDexterityRangeMaximum; }

    public int getRequiredDexterityRangeMinimum() { return requiredDexterityRangeMinimum; }
    public void setRequiredDexterityRangeMinimum(int requiredDexterityRangeMinimum) { this.requiredDexterityRangeMinimum = requiredDexterityRangeMinimum; }

    public String getRequiredDexterityRangeText() { return requiredDexterityRangeText; }
    public void setRequiredDexterityRangeText(String requiredDexterityRangeText) { this.requiredDexterityRangeText = requiredDexterityRangeText; }

    public int getRequiredIntelligenceRangeAverage() { return requiredIntelligenceRangeAverage; }
    public void setRequiredIntelligenceRangeAverage(int requiredIntelligenceRangeAverage) { this.requiredIntelligenceRangeAverage = requiredIntelligenceRangeAverage; }

    public String getRequiredIntelligenceRangeColour() { return requiredIntelligenceRangeColour; }
    public void setRequiredIntelligenceRangeColour(String requiredIntelligenceRangeColour) { this.requiredIntelligenceRangeColour = requiredIntelligenceRangeColour; }

    public int getRequiredIntelligenceRangeMaximum() { return requiredIntelligenceRangeMaximum; }
    public void setRequiredIntelligenceRangeMaximum(int requiredIntelligenceRangeMaximum) { this.requiredIntelligenceRangeMaximum = requiredIntelligenceRangeMaximum; }

    public int getRequiredIntelligenceRangeMinimum() { return requiredIntelligenceRangeMinimum; }
    public void setRequiredIntelligenceRangeMinimum(int requiredIntelligenceRangeMinimum) { this.requiredIntelligenceRangeMinimum = requiredIntelligenceRangeMinimum; }

    public String getRequiredIntelligenceRangeText() { return requiredIntelligenceRangeText; }
    public void setRequiredIntelligenceRangeText(String requiredIntelligenceRangeText) { this.requiredIntelligenceRangeText = requiredIntelligenceRangeText; }

    public int getRequiredStrengthRangeAverage() { return requiredStrengthRangeAverage; }
    public void setRequiredStrengthRangeAverage(int requiredStrengthRangeAverage) { this.requiredStrengthRangeAverage = requiredStrengthRangeAverage; }

    public String getRequiredStrengthRangeColour() { return requiredStrengthRangeColour; }
    public void setRequiredStrengthRangeColour(String requiredStrengthRangeColour) { this.requiredStrengthRangeColour = requiredStrengthRangeColour; }

    public int getRequiredStrengthRangeMaximum() { return requiredStrengthRangeMaximum; }
    public void setRequiredStrengthRangeMaximum(int requiredStrengthRangeMaximum) { this.requiredStrengthRangeMaximum = requiredStrengthRangeMaximum; }

    public int getRequiredStrengthRangeMinimum() { return requiredStrengthRangeMinimum; }
    public void setRequiredStrengthRangeMinimum(int requiredStrengthRangeMinimum) { this.requiredStrengthRangeMinimum = requiredStrengthRangeMinimum; }

    public String getRequiredStrengthRangeText() { return requiredStrengthRangeText; }
    public void setRequiredStrengthRangeText(String requiredStrengthRangeText) { this.requiredStrengthRangeText = requiredStrengthRangeText; }

    // ── HTML Requirements ──
    public String getRequiredLevelHtml() { return requiredLevelHtml; }
    public void setRequiredLevelHtml(String requiredLevelHtml) { this.requiredLevelHtml = requiredLevelHtml; }

    public String getRequiredDexterityHtml() { return requiredDexterityHtml; }
    public void setRequiredDexterityHtml(String requiredDexterityHtml) { this.requiredDexterityHtml = requiredDexterityHtml; }

    public String getRequiredIntelligenceHtml() { return requiredIntelligenceHtml; }
    public void setRequiredIntelligenceHtml(String requiredIntelligenceHtml) { this.requiredIntelligenceHtml = requiredIntelligenceHtml; }

    public String getRequiredStrengthHtml() { return requiredStrengthHtml; }
    public void setRequiredStrengthHtml(String requiredStrengthHtml) { this.requiredStrengthHtml = requiredStrengthHtml; }

    // ── Boolean Flags ──
    public boolean isDropEnabled() { return dropEnabled; }
    public void setDropEnabled(boolean dropEnabled) { this.dropEnabled = dropEnabled; }

    public int getDropLevel() { return dropLevel; }
    public void setDropLevel(int dropLevel) { this.dropLevel = dropLevel; }

    public int getDropLevelMaximum() { return dropLevelMaximum; }
    public void setDropLevelMaximum(int dropLevelMaximum) { this.dropLevelMaximum = dropLevelMaximum; }

    public boolean isAccountBound() { return isAccountBound; }
    public void setAccountBound(boolean accountBound) { isAccountBound = accountBound; }

    public boolean isCorrupted() { return isCorrupted; }
    public void setCorrupted(boolean corrupted) { isCorrupted = corrupted; }

    public boolean isDropRestricted() { return isDropRestricted; }
    public void setDropRestricted(boolean dropRestricted) { isDropRestricted = dropRestricted; }

    public boolean isEaterOfWorldsItem() { return isEaterOfWorldsItem; }
    public void setEaterOfWorldsItem(boolean eaterOfWorldsItem) { isEaterOfWorldsItem = eaterOfWorldsItem; }

    public boolean isFractured() { return isFractured; }
    public void setFractured(boolean fractured) { isFractured = fractured; }

    public boolean isInGame() { return isInGame; }
    public void setInGame(boolean inGame) { isInGame = inGame; }

    public boolean isReplica() { return isReplica; }
    public void setReplica(boolean replica) { isReplica = replica; }

    public boolean isSearingExarchItem() { return isSearingExarchItem; }
    public void setSearingExarchItem(boolean searingExarchItem) { isSearingExarchItem = searingExarchItem; }

    public boolean isSynthesised() { return isSynthesised; }
    public void setSynthesised(boolean synthesised) { isSynthesised = synthesised; }

    public boolean isUnmodifiable() { return isUnmodifiable; }
    public void setUnmodifiable(boolean unmodifiable) { isUnmodifiable = unmodifiable; }

    public boolean isVeiled() { return isVeiled; }
    public void setVeiled(boolean veiled) { isVeiled = veiled; }

    // ── Stat Text ──
    public String getStatText() { return statText; }
    public void setStatText(String statText) { this.statText = statText; }

    public String getExplicitStatText() { return explicitStatText; }
    public void setExplicitStatText(String explicitStatText) { this.explicitStatText = explicitStatText; }

    public String getImplicitStatText() { return implicitStatText; }
    public void setImplicitStatText(String implicitStatText) { this.implicitStatText = implicitStatText; }

    // ── Drop ──
    public String getDropText() { return dropText; }
    public void setDropText(String dropText) { this.dropText = dropText; }

    public List<String> getDropAreas() { return dropAreas; }
    public void setDropAreas(List<String> dropAreas) { this.dropAreas = dropAreas; }

    public String getDropAreasHtml() { return dropAreasHtml; }
    public void setDropAreasHtml(String dropAreasHtml) { this.dropAreasHtml = dropAreasHtml; }

    public List<String> getDropMonsters() { return dropMonsters; }
    public void setDropMonsters(List<String> dropMonsters) { this.dropMonsters = dropMonsters; }

    public List<String> getDropRarityIds() { return dropRarityIds; }
    public void setDropRarityIds(List<String> dropRarityIds) { this.dropRarityIds = dropRarityIds; }

    // ── Tags ──
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public List<String> getAcquisitionTags() { return acquisitionTags; }
    public void setAcquisitionTags(List<String> acquisitionTags) { this.acquisitionTags = acquisitionTags; }

    public List<String> getInfluences() { return influences; }
    public void setInfluences(List<String> influences) { this.influences = influences; }

    // ── Descriptions ──
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getFlavourText() { return flavourText; }
    public void setFlavourText(String flavourText) { this.flavourText = flavourText; }

    public String getHelpText() { return helpText; }
    public void setHelpText(String helpText) { this.helpText = helpText; }

    // ── HTML ──
    public String getHtml() { return html; }
    public void setHtml(String html) { this.html = html; }

    public String getInfoboxHtml() { return infoboxHtml; }
    public void setInfoboxHtml(String infoboxHtml) { this.infoboxHtml = infoboxHtml; }

    public String getMetaboxHtml() { return metaboxHtml; }
    public void setMetaboxHtml(String metaboxHtml) { this.metaboxHtml = metaboxHtml; }

    // ── Icons ──
    public List<String> getAlternateArtInventoryIcons() { return alternateArtInventoryIcons; }
    public void setAlternateArtInventoryIcons(List<String> alternateArtInventoryIcons) { this.alternateArtInventoryIcons = alternateArtInventoryIcons; }

    // ── Quality ──
    public int getQuality() { return quality; }
    public void setQuality(int quality) { this.quality = quality; }

    // ── Versions ──
    public String getReleaseVersion() { return releaseVersion; }
    public void setReleaseVersion(String releaseVersion) { this.releaseVersion = releaseVersion; }

    public String getRemovalVersion() { return removalVersion; }
    public void setRemovalVersion(String removalVersion) { this.removalVersion = removalVersion; }

    // ── Wiki ──
    public String getWikiUrl() { return wikiUrl; }
    public void setWikiUrl(String wikiUrl) { this.wikiUrl = wikiUrl; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
}
