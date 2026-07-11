package com.poe.core.version;

import java.time.Instant;
import java.util.Objects;

/**
 * 游戏版本信息，包含版本号、联赛名、来源和检测时间。
 */
public class GameVersion {

    private final String version;
    private final String league;
    private final String source;
    private final Instant detectedAt;

    public GameVersion(String version, String league, String source, Instant detectedAt) {
        this.version = Objects.requireNonNull(version, "version");
        this.league = Objects.requireNonNull(league, "league");
        this.source = Objects.requireNonNull(source, "source");
        this.detectedAt = detectedAt != null ? detectedAt : Instant.now();
    }

    public String getVersion() { return version; }
    public String getLeague() { return league; }
    public String getSource() { return source; }
    public Instant getDetectedAt() { return detectedAt; }

    /** 未知版本的哨兵值。 */
    public static GameVersion unknown() {
        return new GameVersion("unknown", "unknown", "none", Instant.now());
    }

    public boolean isUnknown() {
        return "unknown".equals(version);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GameVersion that)) return false;
        return version.equals(that.version)
            && league.equals(that.league)
            && source.equals(that.source);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version, league, source);
    }

    @Override
    public String toString() {
        return "GameVersion{version='" + version + "', league='" + league + "', source='" + source + "'}";
    }
}
