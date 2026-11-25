package com.example.domain.engine

/**
 * Curated offline 6-letter rack sets where each rack yields a real 6-letter anchor word
 * plus multiple valid 3-to-5 letter sub-words. Used in Word Twist completely offline.
 */
data class WordTwistSet(
    val anchorWord: String,
    val validWords: Set<String>
)

object WordBank {
    val sets: List<WordTwistSet> = listOf(
        WordTwistSet("PLANET", setOf("PLANET", "PLANT", "PLANE", "PLATE", "PLEAT", "PETAL", "LEAPT", "PANEL", "PENAL", "PLAN", "PALE", "PANE", "PANT", "PEAL", "PEAT", "PELT", "TALE", "TAPE", "TEAL", "LANE", "LATE", "LEAN", "LEAP", "LENT", "NEAT", "ANT", "APE", "APT", "ATE", "EAT", "LAP", "LET", "NAP", "NET", "PAL", "PAN", "PAT", "PEA", "PEN", "PET", "TAN", "TAP", "TEA", "TEN")),
        WordTwistSet("STREAM", setOf("STREAM", "MASTER", "SMART", "STEAM", "STARE", "TEAMS", "TEARS", "RATES", "MEATS", "MATES", "TAMER", "ARMED", "STAR", "STEM", "SEAT", "SEAM", "SAME", "TEAM", "TEAR", "TAME", "TERM", "TRAM", "RATE", "REST", "MARE", "MARS", "MAST", "MATE", "MEAT", "EAST", "EARS", "ARTS", "ARMS", "ARE", "ARM", "ART", "ATE", "EAR", "EAT", "ERA", "MAR", "MAT", "MET", "RAM", "RAT", "SAT", "SEA", "SET", "TAR", "TEA")),
        WordTwistSet("BRIGHT", setOf("BRIGHT", "RIGHT", "BIRTH", "GRIT", "GIRT", "TRIG", "BRIG", "BIG", "BIT", "HIT", "RIB", "RIG")),
        WordTwistSet("GARDEN", setOf("GARDEN", "DANGER", "GANDER", "RANGED", "GRADE", "GRAND", "RANGE", "ANGER", "DRAG", "DARE", "DEAR", "DEAN", "EARN", "GEAR", "NEAR", "NERD", "RAGE", "RANG", "READ", "REND", "AGED", "AGE", "AND", "ARE", "DEN", "EAR", "END", "ERA", "NAG", "RAG", "RAN", "RED")),
        WordTwistSet("SILVER", setOf("SILVER", "LIVERS", "SLIVER", "LIVER", "LIVES", "ELVIS", "RILES", "VEILS", "VILE", "VEIL", "RIVE", "RISE", "LIVE", "LIES", "LEIS", "ISLE", "EVIL", "SLIR", "IRE", "LIE", "REV", "SIR", "VIE")),
        WordTwistSet("CASTLE", setOf("CASTLE", "CLEATS", "SLATE", "STALE", "STEAL", "LEAST", "TALES", "CASTE", "SCALE", "LACES", "CASE", "CAST", "CATS", "EAST", "EATS", "LACE", "LAST", "LATE", "LEST", "SALE", "SALT", "SEAL", "SEAT", "SECT", "SLAT", "TALE", "TEAL", "ACTS", "ALES", "ACE", "ACT", "ALE", "ATE", "CAT", "EAT", "LET", "SAT", "SEA", "SET", "TEA")),
        WordTwistSet("FRIEND", setOf("FRIEND", "FINDER", "REDFIN", "FIEND", "FIRED", "FRIED", "INFER", "DINER", "FINE", "FIND", "FIRE", "FEND", "FERN", "DINE", "DIRE", "NERD", "REIN", "REND", "RIDE", "RIND", "RIFE", "DEN", "DIE", "DIN", "END", "FED", "FEN", "FIN", "FIR", "IRE", "RED", "RID")),
        WordTwistSet("MOTION", setOf("MOTION", "NOTION", "MINT", "MOON", "MOOT", "NOON", "OMIT", "ONTO", "TOON", "MONO", "ION", "MOO", "NOT", "TIN", "TON", "TOO")),
        WordTwistSet("ORANGE", setOf("ORANGE", "ONAGER", "GROAN", "ORGAN", "ANGER", "RANGE", "GONER", "EARN", "GEAR", "GORE", "NEAR", "OGRE", "RAGE", "RANG", "ROAN", "AGE", "AGO", "ARE", "EAR", "EGO", "ERA", "NAG", "NOR", "OAR", "ONE", "ORE", "RAG", "RAN", "ROE")),
        WordTwistSet("SIGNAL", setOf("SIGNAL", "ALIGNS", "SLING", "SNAIL", "SLAIN", "NAILS", "ALIGN", "GAINS", "LINGA", "GAIN", "GAIL", "LAIN", "NAIL", "SAIL", "SANG", "SIGN", "SING", "SLAG", "SNAG", "AIL", "GAS", "GIN", "LAG", "NAG", "NIL", "SAG", "SIN")),
        WordTwistSet("BRIDGE", setOf("BRIDGE", "BRIDE", "RIDGE", "DIRGE", "BIRD", "BIDE", "BRED", "BRIG", "DIRE", "DRIB", "GIRD", "GRID", "RIDE", "BED", "BID", "BIG", "DEB", "DIE", "DIG", "IRE", "RED", "RIB", "RID", "RIG")),
        WordTwistSet("MARKET", setOf("MARKET", "MAKER", "TAMER", "TAKER", "MAKE", "MARK", "MARE", "MATE", "MEAT", "RAKE", "RATE", "REAM", "TAKE", "TAME", "TEAM", "TEAR", "TERM", "TRAM", "TREK", "ARE", "ARK", "ARM", "ART", "ATE", "EAR", "EAT", "ERA", "MAR", "MAT", "MET", "RAM", "RAT", "TAR", "TEA")),
        WordTwistSet("SIMPLE", setOf("SIMPLE", "SMILE", "SLIME", "MILES", "LIMES", "PILES", "PLIES", "IMPEL", "SLIM", "SLIP", "MILE", "LIME", "LIMP", "LIPS", "LISP", "ISLE", "PIES", "PILE", "ELM", "IMP", "LEI", "LIE", "LIP", "MIL", "PIE", "SIP")),
        WordTwistSet("WONDER", setOf("WONDER", "DOWNER", "OWNED", "ROWED", "DRONE", "ENDOW", "OWNER", "WORD", "WORE", "WORN", "DONE", "DOWN", "DREW", "NERD", "NODE", "REDO", "REND", "RODE", "WEND", "DEN", "DEW", "DOE", "DON", "DOW", "END", "NEW", "NOD", "NOR", "NOW", "ODE", "ONE", "ORE", "OWE", "OWN", "RED", "ROD", "ROE", "ROW", "WED", "WOE", "WON")),
        WordTwistSet("SPARKS", setOf("SPARKS", "SPARK", "PARKS", "RASPS", "SPAR", "PARK", "PASS", "RASP", "SAPS", "SARK", "ARKS", "ASKS", "ARK", "ASK", "ASP", "PAR", "RAP", "SAP", "SPA")),
        WordTwistSet("CODING", setOf("CODING", "DOING", "DINGO", "COIN", "CONG", "DING", "DINO", "DONG", "ICON", "NODI", "COD", "COG", "CON", "DIG", "DIN", "DOC", "DOG", "DON", "GID", "GIN", "GOD", "ION", "NOD", "NOG")),
        WordTwistSet("STRIDE", setOf("STRIDE", "DRIEST", "TIRED", "TRIES", "RITES", "RIDES", "DRIES", "SIRED", "TIERS", "TIRES", "DIET", "DIRE", "DIRT", "EDIT", "IDES", "REST", "RIDE", "RIDS", "RISE", "RITE", "SIDE", "SIRE", "SITE", "STIR", "TIDE", "TIED", "TIER", "TIES", "TIRE", "DIE", "DIT", "IRE", "ITS", "RED", "RID", "SET", "SIR", "SIT", "TIE")),
        WordTwistSet("SPRING", setOf("SPRING", "SPRIG", "RINGS", "GRINS", "GRIPS", "PINGS", "SINGS", "GRIN", "GRIP", "NIPS", "PIGS", "PING", "PINS", "PRIG", "RIGS", "RING", "RIPS", "SIGN", "SING", "SIPS", "SNIP", "SPIN", "GIN", "GIP", "INS", "NIP", "PIG", "PIN", "RIG", "RIN", "RIP", "SIN", "SIP", "SIR")),
        WordTwistSet("FALCON", setOf("FALCON", "FLACON", "CLAN", "COAL", "COLA", "FLAN", "FOAL", "LOAF", "LOAN", "LOAN", "CAN", "CON", "FAN", "OAF")),
        WordTwistSet("VECTOR", setOf("VECTOR", "COVERT", "CORVET", "COVER", "OVERT", "TROVE", "VOTER", "CORE", "COTE", "COVE", "OVER", "ROTE", "ROVE", "TORE", "VERT", "VETO", "VOTE", "COR", "COT", "ORE", "ORT", "REC", "REV", "ROC", "ROE", "ROT", "TOE", "TOR", "VET")),
        WordTwistSet("SHIELD", setOf("SHIELD", "SLIDED", "SLIDE", "SIDLE", "SHIED", "HELD", "HIDE", "HIES", "IDES", "ISLE", "LEIS", "LIDS", "LIED", "LIES", "SHED", "SIDE", "SLED", "SLID", "DEL", "DIE", "ELD", "HID", "HIE", "HIS", "LED", "LEI", "LID", "LIE", "SHE")),
        WordTwistSet("PUZZLE", setOf("PUZZLE", "LUGE", "PULE", "PEW", "PUL", "ZEP")),
        WordTwistSet("WINTER", setOf("WINTER", "TWINER", "INERT", "INTER", "NITER", "TWINE", "WRITE", "NEWT", "RENT", "RITE", "TERN", "TIER", "TINE", "TIRE", "TWIN", "WENT", "WIRE", "WREN", "WRIT", "IRE", "NET", "NEW", "NIT", "REI", "RET", "RIN", "TEN", "TEW", "TIE", "TIN", "WEN", "WET", "WIN", "WIT")),
        WordTwistSet("ROCKET", setOf("ROCKET", "CORK", "CORE", "COTE", "KORE", "RECK", "ROCK", "ROTE", "TORE", "TREK", "TROKE", "COR", "COT", "ERK", "KOR", "ORE", "ORT", "REC", "ROC", "ROE", "ROT", "TOE", "TOR")),
        WordTwistSet("LEADER", setOf("LEADER", "DEALER", "ALDER", "ELDER", "LADED", "DEAL", "DEAR", "DALE", "DARE", "DEER", "EARL", "LADE", "LEAD", "LEAR", "READ", "REAL", "REED", "REEL", "ALE", "ARE", "DAL", "DEE", "EAR", "EEL", "ELD", "ERA", "LAD", "LAR", "LEA", "LED", "LEE", "RAD", "RED")),
        WordTwistSet("MASTER", setOf("MASTER", "STREAM", "MATES", "MEATS", "RATES", "SMART", "STARE", "STEAM", "TAMER", "TEAMS", "TEARS", "ARMS", "ARTS", "EARS", "EAST", "MARE", "MARS", "MAST", "MATE", "MEAT", "RAMS", "RATE", "RATS", "REAM", "REST", "SAME", "SEAM", "SEAT", "STAR", "STEM", "TAME", "TEAM", "TEAR", "TERM", "TRAM", "ARE", "ARM", "ART", "ATE", "EAR", "EAT", "ERA", "MAR", "MAT", "MET", "RAM", "RAT", "SAT", "SEA", "SET", "TAR", "TEA")),
        WordTwistSet("ENERGY", setOf("ENERGY", "GREEN", "GENRE", "EERY", "EYEN", "GENE", "GREE", "GREY", "GYRE", "NEER", "EER", "ERE", "ERG", "EYE", "GEE", "GEN", "GYR", "NEE", "REG", "RYE", "YEN")),
        WordTwistSet("TEMPLE", setOf("TEMPLE", "MELT", "MEET", "PEEL", "PELT", "TEEM", "ELM", "EME", "LEE", "LET", "MEL", "MET", "PEE", "PET", "TEE"))
    )
}
