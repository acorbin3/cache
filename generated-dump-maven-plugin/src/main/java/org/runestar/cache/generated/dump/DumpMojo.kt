package org.runestar.cache.generated.dump

import com.squareup.javapoet.*
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.project.MavenProject
import org.runestar.cache.content.def.ItemDefinition
import org.runestar.cache.content.def.NpcDefinition
import org.runestar.cache.content.def.ObjectDefinition
import org.runestar.cache.format.BackedCache
import org.runestar.cache.format.ReadableCache
import org.runestar.cache.format.fs.FileSystemCache
import org.runestar.cache.format.net.NetCache
import java.nio.file.Paths
import java.util.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Modifier

@Mojo(name = "dump")
class DumpMojo : AbstractMojo() {

    private companion object {
        const val INDENT = "\t"
    }

    @Parameter(property = "outputPackage", required = true)
    lateinit var outputPackage: String

    @Parameter(defaultValue = "\${project}")
    lateinit var project: MavenProject

    private val outputDir by lazy { Paths.get(project.build.directory, "generated-sources") }

    lateinit var cache: ReadableCache

    override fun execute() {
        cache = BackedCache(FileSystemCache.open(), NetCache.open("oldschool1.runescape.com", 174))

        try {
            npcs()
            objects()
            items()
        } finally {
            cache.close()
        }

        project.addCompileSourceRoot(outputDir.toString())
    }

    private fun npcs() {
        val map = TreeMap<Int, String>()
        NpcDefinition.Loader(cache).getDefinitions().forEachIndexed { i, record ->
            if (record == null) return@forEachIndexed
            map[i] = record.definition.name
        }
        writeIdsFile("NpcId", map)
    }

    private fun objects() {
        val map = TreeMap<Int, String>()
        ObjectDefinition.Loader(cache).getDefinitions().forEachIndexed { i, record ->
            if (record == null) return@forEachIndexed
            map[i] = record.definition.name
        }
        writeIdsFile("ObjectId", map)
    }

    private fun items() {
        val map = TreeMap<Int, String>()
        ItemDefinition.Loader(cache).getDefinitions().forEachIndexed { i, record ->
            if (record == null) return@forEachIndexed
            map[i] = record.definition.name
        }
        writeIdsFile("ItemId", map)
    }

    private val REMOVE_REGEX = "([']|<.*?>)".toRegex()

    private val REPLACE_UNDERSCORE_REGEX = "[- /)(.,!]".toRegex()

    private val REPLACE_DOLLARSIGN_REGEX = "[%&+?]".toRegex()

    private val MULTI_UNDERSCORE_REGEX = "_{2,}".toRegex()

    private val ENDS_UNDERSCORES_REGEX = "(^_+|_+$)".toRegex()

    private fun stringToIdentifier(name: String): String? {
        if (name.equals("null", true)) return null
        if (name.isBlank()) return null
        var n = name.toUpperCase()
                .replace(REMOVE_REGEX, "")
                .replace(REPLACE_UNDERSCORE_REGEX, "_")
                .replace(REPLACE_DOLLARSIGN_REGEX, "\\$")
                .replace(ENDS_UNDERSCORES_REGEX, "")
                .replace(MULTI_UNDERSCORE_REGEX, "_")
        if (!SourceVersion.isName(n)) {
            n = "_$n"
        }
        if (!SourceVersion.isName(n)) {
            log.warn(name)
            return null
        }
        return n
    }

    private fun writeIdsFile(
            className: String,
            names: SortedMap<Int, String>
    ) {
        val typeBuilder = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(MethodSpec.constructorBuilder().addModifiers(Modifier.PRIVATE).build())

        names.forEach { id, name ->
            val identifierName: String = stringToIdentifier(name) ?: return@forEach
            val finalName = "${identifierName}_$id"

            typeBuilder.addField(
                    FieldSpec.builder(TypeName.INT, finalName, Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                            .initializer(id.toString())
                            .build()
            )
        }

        JavaFile.builder(outputPackage, typeBuilder.build())
                .indent(INDENT)
                .build()
                .writeTo(outputDir)
    }
}