package lt.legendaryguard.data

import lt.legendaryguard.LegendaryGuard
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.configuration.serialization.ConfigurationSerializable
import java.io.File
import java.util.*

class PlayerData : ConfigurationSerializable {

    var uuid: UUID
    var points: MutableList<Point>
    var trust: String

    constructor(uuid: UUID) {
        this.uuid = uuid
        this.points = mutableListOf()
        this.trust = ""
    }

    constructor(map: Map<String, *>) {
        uuid = UUID.fromString(map["uuid"] as String)
        points = map["points"] as MutableList<Point>
        trust = map["trust"] as String
    }


    /**
     * Creates a Map representation of this class.
     *
     *
     * This class must provide a method to restore this class, as defined in
     * the [ConfigurationSerializable] interface javadocs.
     *
     * @return Map containing the current state of this class
     */
    override fun serialize(): MutableMap<String, Any?> {
        val map = mutableMapOf<String, Any?>()

        map["uuid"] = uuid.toString()
        map["points"] = points
        map["trust"] = trust
        return map
    }


    fun save() {
        //   weightSum = -1
        val folder = File(LegendaryGuard.PLUGIN.dataFolder, "data")
        if (!folder.exists()) {
            folder.mkdirs()
        }
        val f = File(folder, "$uuid.yml")
        val yaml = YamlConfiguration()
        yaml["data"] = this
        yaml.save(f)
    }

    companion object {
        val playerDataList = mutableMapOf<UUID, PlayerData>()

        fun load() {
            playerDataList.clear()
            val f = File(LegendaryGuard.PLUGIN.dataFolder, "data")
            if (!f.exists()) {
                return
            }
            load(f)
        }

        private fun load(f: File) {
            if (f.isDirectory) {
                for (t in f.listFiles()) {
                    load(t)
                }
                return
            }
            val config = YamlConfiguration.loadConfiguration(f)

            val pd = config["data"] as PlayerData

            playerDataList[pd.uuid] = pd
        }

    }

    class Point : ConfigurationSerializable {

        var endTime: Long
        var pokemonTime: Long
        var displayTime: String


        constructor(map: Map<String, *>) {
            endTime = (map["endTime"] as String ).toLong()
            pokemonTime = (map["pokemonTime"] as String ).toLong()
            displayTime = map["displayTime"] as String
        }

        constructor(endTime: Long, pokemonTime: Long, displayTime: String) {
            this.endTime = endTime
            this.pokemonTime = pokemonTime
            this.displayTime = displayTime
        }

        override fun serialize(): MutableMap<String, Any?> {
            val map = mutableMapOf<String, Any?>()

            map["endTime"] = endTime.toString()
            map["pokemonTime"] = pokemonTime.toString()
            map["displayTime"] = displayTime
            return map

        }

    }

}