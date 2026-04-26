package net.baltyr.opmmod.classes;

public enum OpmClass {
    NONE("Aucune", "§7", ""),

    // Force Brute
    SAITAMA("Saitama", "§c",
            "Force absolue et invincibilité totale. Un seul coup suffit."),
    DARKSHINE("Super Alliage Noir", "§8",
            "Corps d'acier indestructible, force colossale."),
    TANKTOP_MASTER("Maître Débardeur", "§6",
            "Maîtrise du combat au corps à corps, endurance extrême."),

    // Artiste Martial
    BANG("Bang", "§b",
            "Grand maître du Rocher qui brise l'eau qui coule."),
    BOMB("Bong", "§3",
            "Maître de la Fissure en spirale qui explose."),
    GAROU("Garou", "§4",
            "Le Monstre Humain, adapte son style à chaque combat."),
    SUIRYU("Suiryu", "§a",
            "Arts martiaux fluides et dévastateurs."),

    // Ninja
    FLASHY_FLASH("Flash Flamboyant", "§e",
            "Vitesse dépassant la perception humaine."),
    SONIC("Sonic le Foudroyant", "§f",
            "Rival de Flash, vitesse et lames mortelles."),

    // Cyborg
    GENOS("Genos", "§6",
            "Cyborg démoniaque, armes intégrées surpuissantes."),
    DRIVE_KNIGHT("Chevalier Moteur", "§8",
            "Transformations mécaniques redoutables."),

    // Utilisateur d'Armes
    ATOMIC_SAMURAI("Samouraï Atomique", "§b",
            "Lame atomique, tranche tout à vitesse absolue."),
    METAL_BAT("Batte-Man", "§c",
            "Frappe de plus en plus forte à chaque coup reçu."),
    ZOMBIEMAN("Zombieman", "§2",
            "Régénération infinie, survit à tout."),

    // Esper
    TATSUMAKI("Tatsumaki", "§d",
            "Télékinésie dévastatrice, la plus puissante esper."),
    FUBUKI("Fubuki", "§9",
            "Télékinésie et leadership, chef du groupe Blizzard."),

    // Invocateur
    CHILD_EMPEROR("Petit Empereur", "§e",
            "Génie tactique, gadgets et inventions redoutables."),
    METAL_KNIGHT("Chevalier Métal", "§7",
            "Drones et robots de guerre télécommandés."),

    // Dominateur
    BOROS("Boros", "§5",
            "Énergie cosmique libérée, puissance planétaire."),

    // N°1
    BLAST("Blast", "§f",
            "Le héros n°1, pouvoirs mystérieux et inconnus."),

    // Classe Secrète
    COSMIC_GAROU("Garou Cosmique", "§5",
            "Garou imprégné d'énergie divine, forme ultime."),
    EMPTY_VOID("Empty Void", "§r§3",   // ← §3 au lieu de §0
            "Vide absolu, néant total, au-delà du compréhensible.");

    private final String displayName;
    private final String colorCode;
    private final String description;

    OpmClass(String displayName, String colorCode, String description) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.description = description;
    }

    public String getDisplayName()  { return displayName; }
    public String getColorCode()    { return colorCode; }
    public String getDescription()  { return description; }

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