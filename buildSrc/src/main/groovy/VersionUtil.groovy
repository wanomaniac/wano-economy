import org.gradle.api.GradleException

class VersionUtil {
    static boolean isVersionDeobfuscated(String mcVersion) {
        return compareVersions(mcVersion, "26") >= 0
    }

    private static int compareVersions(String v1, String v2) {
        def p1 = v1.tokenize('.').collect { it as int }
        def p2 = v2.tokenize('.').collect { it as int }

        int max = Math.max(p1.size(), p2.size())
        for (int i = 0; i < max; i++) {
            int a = i < p1.size() ? p1[i] : 0
            int b = i < p2.size() ? p2[i] : 0
            if (a != b) return a <=> b
        }
        return 0
    }
    static boolean versionMatches(String rule, String mcVersion) {
        mcVersion = normalize(mcVersion)
        rule = rule.trim()

        if (!rule.contains("*") &&
                !rule.contains("x") &&
                !rule.contains("+") &&
                !rule.contains("-") &&
                !rule.contains("{") &&
                !rule.contains("}")) {
            return normalize(rule) == mcVersion
        }

        if (rule.endsWith(".*") || rule.endsWith(".x")) {
            def prefix = rule[0..<(rule.lastIndexOf('.'))]
            return mcVersion.startsWith(prefix)
        }

        if (rule == "*" || rule == "x" || rule == "*.x" || rule == "*.*") {
            return true
        }

        if (rule.endsWith("+")) {
            def base = normalize(rule[0..-2])
            return semverCompare(mcVersion, base) >= 0
        }

        // compound rule "A}{B"
        if (rule.contains("}{")) {
            def (left, right) = rule.split("\\}\\{")

            left = left.trim()
            right = right.trim()

            // left side: >, >=
            def leftRule = left.startsWith("{=") || left.startsWith("{")
                    ? left
                    : "{${left}"

            // right side: <, <=
            def rightRule = right.startsWith("}=") || right.startsWith("}")
                    ? right
                    : "}${right}"

            return versionMatches(leftRule, mcVersion) &&
                    versionMatches(rightRule, mcVersion)
        }



        if (rule.startsWith("{=")) {
            return semverCompare(mcVersion, normalize(rule.substring(2).trim())) >= 0
        }
        if (rule.startsWith("}=")) {
            return semverCompare(mcVersion, normalize(rule.substring(2).trim())) <= 0
        }
        if (rule.startsWith("{")) {
            return semverCompare(mcVersion, normalize(rule.substring(1).trim())) > 0
        }
        if (rule.startsWith("}")) {
            return semverCompare(mcVersion, normalize(rule.substring(1).trim())) < 0
        }

        if (rule.contains("-")) {
            def (start, end) = rule.split("-")
            start = normalize(start.trim())
            end = normalize(end.trim())
            return semverCompare(mcVersion, start) >= 0 &&
                    semverCompare(mcVersion, end) <= 0
        }


        return false
    }

    static String normalize(String v) {
        def parts = v.split(/\./) as List
        while (parts.size() < 3) parts << "0"
        return parts.take(3).join(".")
    }

    static int semverCompare(String a, String b) {
        def ap = a.split(/\./)*.toInteger()
        def bp = b.split(/\./)*.toInteger()
        for (int i = 0; i < 3; i++) {
            if (ap[i] != bp[i]) return ap[i] <=> bp[i]
        }
        return 0
    }

    // converts array list of supported versions into comparative.
    static String formatVersionRange(Object versionInput) {
        if (!versionInput) return ""

        // remove things that break it.
        String raw = versionInput.toString()
                .replaceAll(/[\[\]"']/, '') // removes [ ] " '
                .trim()

        List<String> versions = (raw =~ /\d+(?:\.\d+)*/).findAll()

        if (versions.isEmpty()) return ""
        if (versions.size() == 1) return "=${versions.first()}"


        versions.sort { v1, v2 ->
            def p1 = v1.split(/\./)*.toInteger()
            def p2 = v2.split(/\./)*.toInteger()
            for (int i = 0; i < Math.max(p1.size(), p2.size()); i++) {
                def a = i < p1.size() ? p1[i] : 0
                def b = i < p2.size() ? p2[i] : 0
                if (a != b) return a <=> b
            }
            return 0
        }

        def minVersion = versions.first()
        def maxVersion = versions.last()

        // 4. Return as comparison operator string
        return ">=${minVersion} <=${maxVersion}"
    }

    static String[] getVersionsFromDirectory(String rootDir, File directory) {
        def rule = directory.name.trim()

        // Handle single version
        if (!rule.contains("*") &&
                !rule.contains("x") &&
                !rule.contains("+") &&
                !rule.contains("-") &&
                !rule.contains("<") &&
                !rule.contains(">")) {
            return [normalize(rule)]
        }

        // Handle wildcard 1.21.* or 1.21.x
        if (rule.endsWith(".*") || rule.endsWith(".x")) {
            def prefix = rule[0..<(rule.lastIndexOf('.'))]
            def versions = []
            // generate patch 0..99 for this minor version
            for (int patch = 0; patch <= 99; patch++) {
                versions << "${prefix}.${patch}"
            }
            return versions as String[]
        }

        // Handle open-ended plus: 1.21.10+
        if (rule.endsWith("+")) {
            def base = normalize(rule[0..-2])
            def baseParts = base.split(/\./)*.toInteger()
            def versions = []

            // generate versions starting from base patch 0..99 for simplicity
            for (int patch = baseParts[2]; patch <= 99; patch++) {
                versions << "${baseParts[0]}.${baseParts[1]}.${patch}"
            }
            return versions as String[]
        }

        // Handle ranges 1.21.7-1.21.10
        if (rule.contains("-")) {
            def (start, end) = rule.split("-")
            start = normalize(start.trim())
            end = normalize(end.trim())

            def startParts = start.split(/\./)*.toInteger()
            def endParts = end.split(/\./)*.toInteger()

            def versions = []
            int major = startParts[0]
            int minor = startParts[1]
            for (int patch = startParts[2]; patch <= endParts[2]; patch++) {
                versions << "${major}.${minor}.${patch}"
            }
            return versions as String[]
        }

        // Handle compound rules like "1.21.6}{1.21.8" meaning ">1.21.6 AND <1.21.8"
        if (rule.contains("}{")) {
            def (left, right) = rule.split("\\}\\{")
            left = left.trim()
            right = right.trim()

            def versions = []
            // brute force minor patch range 0..99
            for (int major = 1; major <= 2; major++) {
                for (int minor = 0; minor <= 30; minor++) {
                    for (int patch = 0; patch <= 99; patch++) {
                        def v = "${major}.${minor}.${patch}"
                        if (versionMatches("{${left}", v) && versionMatches("}${right}", v)) {
                            versions << v
                        }
                    }
                }
            }
            return versions as String[]
        }


        // For other rules like >, >=, <, <= or *
        // fallback: just return the normalized rule as a single version (cannot enumerate)
        return [normalize(rule)]
    }

    /**
     * Finds matching version folders (checking version/loader subdirectories first, e.g.,
     * `versions/<rule>/<modloader>/`, then falling back to the base version rule directory),
     * intersects their bounds to calculate the tightest common range, and returns an array
     * of discrete versions.
     *
     *
     * Example:
     * Matching Folders: "versions/1.21-1.21.8/", "versions/1.21.3-1.21.5/neoforge/"
     * Target mcVersion: "1.21.4" (modloader: "neoforge")
     * Output: ["1.21.3", "1.21.4", "1.21.5"] as String[]
     *
     * @param rootDir Root directory path of the project.
     * @param mcVersion Target Minecraft version (e.g., "1.21.4").
     * @param modloader mod loader identifier (e.g., "neoforge", "fabric").
     * @return Array of discrete normalized Minecraft versions matching the resolved range.
     */
    static String[] getEffectiveVersionRangeArray(String rootDir, String mcVersion, String modloader = null) {
        def baseVersionsDir = new File("${rootDir}/versions")

        if (!baseVersionsDir.exists() || !baseVersionsDir.isDirectory()) {
            throw new GradleException("Versions directory missing at ${baseVersionsDir.absolutePath}")
        }

        // 1. Locate all version rule directories matching mcVersion
        def matchingRuleDirs = baseVersionsDir.listFiles()?.findAll { dir ->
            dir.isDirectory() && versionMatches(dir.name, mcVersion)
        } ?: []

        if (matchingRuleDirs.isEmpty()) {
            throw new GradleException("No matching version folder found for MC ${mcVersion}" +
                    (modloader ? " (loader: ${modloader})" : ""))
        }

        List<File> targetDirs = []
        matchingRuleDirs.each { ruleDir ->
            String loaderName = modloader?.toLowerCase()?.trim()

            // Tier 1: Check for loader-specific subfolder (e.g., versions/1.21-1.21.6/neoforge/)
            if (loaderName) {
                def loaderSubDir = new File(ruleDir, loaderName)
                if (loaderSubDir.exists() && loaderSubDir.isDirectory()) {
                    targetDirs << loaderSubDir
                    return
                }
            }

            // Tier 2: Check for shared 'common' subfolder (e.g., versions/1.21-1.21.6/common/)
            def commonSubDir = new File(ruleDir, "common")
            if (commonSubDir.exists() && commonSubDir.isDirectory()) {
                targetDirs << commonSubDir
                return
            }

            // Tier 3: Check if root ruleDir contains direct content (not JUST loader/common subfolders)
            boolean hasDirectContent = ruleDir.listFiles()?.any { file ->
                if (!file.isDirectory()) return true
                String name = file.name.toLowerCase()
                return name != "neoforge" && name != "fabric" && name != "forge" && name != "quilt" && name != "common"
            }

            if (hasDirectContent) {
                targetDirs << ruleDir
            }
        }

        // 2. Extract version arrays using the parent rule folder's name when inside a subfolder
        List<Set<String>> folderVersionSets = []
        targetDirs.each { dir ->
            // If dir is a subfolder (neoforge, fabric, common), use parent folder name for rule parsing
            File ruleFolder = (dir.parentFile != baseVersionsDir) ? dir.parentFile : dir
            String[] versions = getVersionsFromDirectory(rootDir, ruleFolder)
            if (versions && versions.length > 0) {
                folderVersionSets << (versions as Set<String>)
            }
        }

        if (folderVersionSets.isEmpty()) {
            return [normalize(mcVersion)] as String[]
        }

        // 3. Intersect version sets across all matched target directories
        Set<String> intersection = new HashSet<>(folderVersionSets.first())
        for (int i = 1; i < folderVersionSets.size(); i++) {
            intersection.retainAll(folderVersionSets[i])
        }

        if (intersection.isEmpty()) {
            return [normalize(mcVersion)] as String[]
        }

        List<String> sortedVersions = intersection.toList()
        sortedVersions.sort { v1, v2 -> semverCompare(v1, v2) }
        
        List<String> formattedVersions = sortedVersions.collect { v ->
            v.replaceAll(/(\.0)$/, '')
        }

        return formattedVersions as String[]
    }

    // For archive names, output will result in either 1 version like "1.21.whatever" or "{1.21.whatever-1.21.whatever2}'
    static String createVersionRange(String[] array){
        if (!array || array.length == 0) return ""

        if (array.length > 1) {
            return "[${array[0]}-${array[-1]}]" // some systems dont support this [] syntax in filenames but should be ok.
        } else {
            return "[${array[0]}]"
        }
    }

}
