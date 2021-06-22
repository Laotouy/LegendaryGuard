package lt.legendaryguard

import lt.legendaryguard.command.LGCommand
import lt.legendaryguard.data.PlayerData
import lt.legendaryguard.expansion.PAPIExpansion
import lt.legendaryguard.listener.ForgeListener
import org.bukkit.Bukkit
import org.bukkit.configuration.serialization.ConfigurationSerialization
import org.bukkit.plugin.java.JavaPlugin

class LegendaryGuard : JavaPlugin() {

    var default = 0L
    var defaultDisplay = ""

    val permissionMap = mutableMapOf<String, PermPoint>()
    val permissionList = mutableListOf<String>()
    var spawnMsg = mutableListOf<String>()
    var spawnTimeOut = mutableListOf<String>()
    var limit = ""
    var trust = ""
    var trust_remove = ""
    var hover = mutableListOf<String>()


    override fun onEnable() {

        saveDefaultConfig()
        reloadConfig()

        server.pluginManager.registerEvents(ForgeListener(), this)
        getCommand("lg").executor = LGCommand()

        reload()

        PlayerData.load()
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            PAPIExpansion(this).register()
        }
        Bukkit.getScheduler().runTaskAsynchronously(this) { Ad() }

    }

    fun reload(){
        reloadConfig()

        default = config.getLong("default") * 1000

        config.getStringList("permission").forEach {
            val perm = it.split(":")[0]
            val time = it.split(":")[1].toLong() * 1000
            val disTime = it.split(":")[2]

            permissionMap[perm] = PermPoint(perm, time, disTime)
            permissionList.add(perm)

        }

        spawnMsg = config.getStringList("msg.spawn")
        spawnTimeOut = config.getStringList("msg.time_out")
        defaultDisplay = config.getString("defaultDisplay")
        limit = config.getString("msg.limit")
        trust_remove = config.getString("msg.trust_remove")
        trust = config.getString("msg.trust")
        hover = config.getStringList("msg.hover")
        Bukkit.getScheduler().runTaskAsynchronously(this) { Ad() }


    }

    override fun onDisable() {

        PlayerData.playerDataList.forEach {
            it.value.save()
        }


    }


    companion object {
        lateinit var PLUGIN: LegendaryGuard
    }

    override fun onLoad() {

        PLUGIN = this
        ConfigurationSerialization.registerClass(PlayerData::class.java)
        ConfigurationSerialization.registerClass(PlayerData.Point::class.java)


    }


    class PermPoint(val permission: String, val time: Long, val disTime: String) {

    }


}