package lt.legendaryguard.listener

import catserver.api.bukkit.event.ForgeEvent
import com.mc9y.blank038api.Blank038API
import com.mc9y.pokemonapi.api.pokemon.PokemonUtil
import com.pixelmonmod.pixelmon.api.events.BattleStartedEvent
import com.pixelmonmod.pixelmon.api.events.CaptureEvent
import com.pixelmonmod.pixelmon.api.events.spawning.LegendarySpawnEvent
import com.pixelmonmod.pixelmon.api.events.spawning.SpawnEvent
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon
import com.pixelmonmod.pixelmon.api.spawning.archetypes.entities.pokemon.SpawnActionPokemon
import com.pixelmonmod.pixelmon.battles.controller.participants.WildPixelmonParticipant
import net.md_5.bungee.api.chat.HoverEvent

import net.md_5.bungee.api.chat.TextComponent

import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon
import com.pixelmonmod.pixelmon.enums.EnumSpecies
import lt.legendaryguard.LegendaryGuard
import lt.legendaryguard.data.PlayerData
import net.minecraft.util.text.TextComponentString
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import java.util.*
import java.text.SimpleDateFormat
import com.pixelmonmod.pixelmon.api.world.MutableLocation
import java.text.DecimalFormat


class ForgeListener() : Listener {

    private val limit = mutableMapOf<UUID, Limit>()
    val ft = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
    val df = DecimalFormat("######0.00")

    init {
        Bukkit.getServer().scheduler.runTaskTimer(LegendaryGuard.PLUGIN, {


            val remove = mutableListOf<UUID>()
            val it: MutableIterator<Limit> = limit.values.iterator()
            while (it.hasNext()) {
                val x = it.next()

                if (x.endTime > 0 && x.endTime < System.currentTimeMillis()) {

                    if (x.pokemon.isLoaded) {
                        Bukkit.getServer().onlinePlayers.iterator().forEach { p ->

                            LegendaryGuard.PLUGIN.spawnTimeOut.forEach {


                                sendText(p, x.pokemon.pokemonData, it.replace("%player%", x.ownerName).replace("%pokemon_x%", "${x.pokemon.field_70165_t.toInt()}").replace("%pokemon_y%", "${x.pokemon.field_70163_u.toInt()}").replace("%pokemon_z%", "${x.pokemon.field_70161_v.toInt()}"))

                            }

                        }


                    }
                    remove.add(x.pokemon.func_110124_au())

                }


            }

            remove.forEach {
                limit.remove(it)
            }

        }, 20 * 10, 20 * 10)
    }

    class Limit(val ownerPlayerUUID: UUID, val endTime: Long, val pokemon: EntityPixelmon, val ownerName: String)


    private fun check(pokemon: EntityPixelmon, player: Player) {
        var endTime = System.currentTimeMillis() + LegendaryGuard.PLUGIN.default
        var disTime = LegendaryGuard.PLUGIN.defaultDisplay

        if (PlayerData.playerDataList.containsKey(player.uniqueId) && PlayerData.playerDataList[player.uniqueId]!!.points.size > 0) {

            val pd = PlayerData.playerDataList[player.uniqueId]
            while (pd!!.points.size > 0 && pd!!.points[pd.points.size - 1].endTime != -1L && pd.points[pd.points.size - 1].endTime < System.currentTimeMillis()) {

                pd.points.removeAt(pd.points.size - 1)


            }
            val point = pd.points[pd.points.size - 1]
            endTime = if (point.pokemonTime == -1L) {
                -1L
            } else {
                System.currentTimeMillis() + point.pokemonTime
            }
            disTime = point.displayTime

        } else {

            LegendaryGuard.PLUGIN.permissionList.forEach {
                if (player.hasPermission(it)) {

                    disTime = LegendaryGuard.PLUGIN.permissionMap[it]!!.disTime

                    endTime = if (LegendaryGuard.PLUGIN.permissionMap[it]!!.time == -1L) {
                        -1
                    } else {
                        System.currentTimeMillis() + LegendaryGuard.PLUGIN.permissionMap[it]!!.time
                    }

                }
            }

        }

        val string = LegendaryGuard.PLUGIN.spawnMsg

        limit[pokemon.func_110124_au()] = Limit(player.uniqueId, endTime, pokemon, player.name)

        Bukkit.getServer().onlinePlayers.iterator().forEach { p ->
            string.forEach {
                sendText(p, pokemon.pokemonData, it.replace("%player%", player.name)
                        .replace("%time%", disTime))

            }
        }


    }

    @EventHandler(
            priority = EventPriority.LOWEST
    )
    fun onForge(event: ForgeEvent) {

        if (event.forgeEvent is LegendarySpawnEvent.DoSpawn) {

            val e = event.forgeEvent as LegendarySpawnEvent.DoSpawn
            if (e.isCanceled) return
            val pokemon = e.action.orCreateEntity ?: return
            if (!pokemon.isLoaded) return
            val m: MutableLocation = e.action.spawnLocation.location
            val player = getPlayer(Location(Bukkit.getWorld(m.world.func_72912_H().func_76065_j()), pokemon.field_70165_t, pokemon.field_70163_u, pokemon.field_70161_v))
                    ?: return
            check(pokemon, player)

        }

        if (event.forgeEvent is SpawnEvent) {
            val e = event.forgeEvent as SpawnEvent
            if (e.isCanceled) return

            if (e.action is SpawnActionPokemon) {

                val pm = e.action.orCreateEntity as EntityPixelmon
                if (!pm.species.isUltraBeast)
                    return
                val m: MutableLocation = e.action.spawnLocation.location

                val player = getPlayer(Location(Bukkit.getWorld(m.world.func_72912_H().func_76065_j()), pm.field_70165_t, pm.field_70163_u, pm.field_70161_v))
                        ?: return

                check(pm, player)
            }
        }

        if (event.forgeEvent is BattleStartedEvent) {
            val e = event.forgeEvent as BattleStartedEvent

            if (e.participant2[0] is WildPixelmonParticipant) {

                if (limit.containsKey(e.participant2[0].entity.func_110124_au()) && System.currentTimeMillis() < limit[e.participant2[0].entity.func_110124_au()]!!.endTime) {
                    var player = Bukkit.getServer().getPlayer(e.participant1[0].displayName)
                    var owner = limit[e.participant2[0].entity.func_110124_au()]!!.ownerPlayerUUID

                    if (player == null) {
                        player = Bukkit.getServer().getPlayer(e.participant1[1].displayName)
                        owner = limit[e.participant2[1].entity.func_110124_au()]!!.ownerPlayerUUID
                    }

                    if (player.uniqueId == owner)
                        return



                    if (PlayerData.playerDataList.containsKey(owner) && PlayerData.playerDataList[owner]!!.trust == player.name) {
                        return
                    }

                    e.isCanceled = true
                    val time = df.format(((limit[e.participant2[0].entity.func_110124_au()]!!.endTime - System.currentTimeMillis()) / 60000).toDouble())

                    player.sendMessage(LegendaryGuard.PLUGIN.limit.replace("%limit_end_time%", "$time"))
                }

            }

        }

        if (event.forgeEvent is CaptureEvent.SuccessfulCapture) {

            val e = event.forgeEvent as CaptureEvent.SuccessfulCapture

            val player = e.player
            val pokemon = e.pokemon

            if (limit.containsKey(pokemon.func_110124_au()) && System.currentTimeMillis() < limit[pokemon.func_110124_au()]!!.endTime) {
                val owner = limit[pokemon.func_110124_au()]!!.ownerPlayerUUID

                if (player.func_110124_au() == owner) {
                    limit.remove(pokemon.func_110124_au())
                    return
                }


                if (PlayerData.playerDataList.containsKey(owner) && PlayerData.playerDataList[owner]!!.trust == player.func_70005_c_()) {
                    limit.remove(pokemon.func_110124_au())
                    return
                }

                e.isCanceled = true
                val time = df.format(((limit[pokemon.func_110124_au()]!!.endTime - System.currentTimeMillis()) / 60000).toDouble())

                player.sendMessage(arrayOf(TextComponentString(LegendaryGuard.PLUGIN.limit.replace("%limit_end_time%", time))))
            }


        }


    }

//    fun getPlayer(loc: Location): Player? {
//
//        var player: Player? = null
//        var d = 999999999999999999.0
//        loc.world.players.iterator().forEach {
//
//            if (player?.location?.world?.name.equals(loc.world.name) && player?.location?.distance(loc)!! < d) {
//                player = it
//                d = player?.location?.distance(loc)!!
//            }
//
//        }
//        return player
//    }

    fun getPlayer(pixelmonLocation: Location): Player? {
        var distance = Double.MAX_VALUE
        var player: Player? = null
        val onlinePlayers: Collection<*> = Bukkit.getOnlinePlayers()
        for (p in Bukkit.getOnlinePlayers()) {
            val loc = p.location
            if (!pixelmonLocation.world.name.equals(loc.world.name, ignoreCase = true) || pixelmonLocation.distance(loc) >= distance) continue
            distance = pixelmonLocation.distance(loc)
            player = p
        }
        return player
    }

    fun sendText(player: Player, pokemon: Pokemon, es: String) {
        val add = es.contains("%pokemon_name%")
        val split = es.replace("%player%", player.name)
                .replace("&", "§").split("%pokemon_name%")
        val header = TextComponent(split[0])
        val pt = TextComponent("${LegendaryGuard.PLUGIN.config.getString("msg.hover_color")}${PokemonUtil.getPokemonName(pokemon)}")
        val texts: List<String> = LegendaryGuard.PLUGIN.config.getStringList("msg.hover")
        val showTexts: Array<TextComponent?> = arrayOfNulls<TextComponent>(texts.size)
        for (i in texts.indices) {
            showTexts[i] = TextComponent(Blank038API.getPokemonAPI().statsHelper.format(pokemon, texts[i]) + if (i + 1 == texts.size) "" else "\n")
        }
        pt.setHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, showTexts))
        var end: TextComponent? = TextComponent()
        if (split.size > 1) {
            end = TextComponent(split[1])
        }
        if (add) {
            header.addExtra(pt)
        }
        header.addExtra(end)
        player.spigot().sendMessage(header)

    }

}
