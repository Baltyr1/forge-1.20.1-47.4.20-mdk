package net.baltyr.opmmod.classes;

public enum OpmClass {
    NONE("Aucune", "§7"),

    // Force Brut
    SAITAMA("Saitama", "§c"),
    DARKSHINE("Super Alliage Noir", "§8"),
    TANKTOP_MASTER("Maître Débardeur", "§6"),

    // Artiste Martial
    BANG("Bang", "§b"),
    BOMB("Bong", "§3"),
    GAROU("Garou", "§4"),
    SUIRYU("Suiryu", "§a"),

    // Ninja
    FLASHY_FLASH("Flash Flamboyant", "§e"),
    SONIC("Sonic le Foudroyant", "§f"),

    // Cyborg
    GENOS("Genos", "§6"),
    DRIVE_KNIGHT("Chevalier Moteur", "§8"),

    // Utilisateur d'Armes
    ATOMIC_SAMURAI("Samouraï Atomique", "§b"),
    METAL_BAT("Batte-Man", "§c"),
    ZOMBIEMAN("Zombieman", "§2"),

    // Esper
    TATSUMAKI("Tatsumaki", "§d"),
    FUBUKI("Fubuki", "§9"),

    // Invocateur
    CHILD_EMPEROR("Petit Empereur", "§e"),
    METAL_KNIGHT("Chevalier Métal", "§7"),

    // Dominateur
    BOROS("Boros", "§5"),

    // N°1
    BLAST("Blast", "§f"),

    // Classe Secrète
    COSMIC_GAROU("Garou Cosmique", "§5"),
    EMPTY_VOID("Empty Void", "§0");

    private final String displayName;
    private final String colorCode;

    OpmClass(String displayName, String colorCode) {
        this.displayName = displayName;
        this.colorCode = colorCode;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColorCode() {
        return colorCode;
    }

    public String getFormattedName() {
        return colorCode + displayName + "§r";
    }

    public static OpmClass fromString(String name) {
        for (OpmClass c : values()) {
            if (c.name().equalsIgnoreCase(name))
                return c;
        }
        return NONE;
    }
}