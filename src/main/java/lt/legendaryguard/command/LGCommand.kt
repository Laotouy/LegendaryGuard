package lt.legendaryguard.command

import lt.legendaryguard.LegendaryGuard
import lt.legendaryguard.data.PlayerData
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.lang.Exception
import java.util.regex.Pattern

class LGCommand : CommandExecutor {
    /**
     * Executes the given command, returning its success
     *
     * @param sender Source of the command
     * @param command Command which was executed
     * @param label Alias of the command which was used
     * @param args Passed command arguments
     * @return true if a valid command, otherwise false
     */
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {


        if (sender.isOp && args.size == 1 && args[0].equals("reload",ignoreCase = true)){
            LegendaryGuard.PLUGIN.reload()
            sender.sendMessage("§f重载完成")

            return true
        }

        if (sender is Player && args.size == 2 && args[0].equals("trust", ignoreCase = true)) {

            if (!PlayerData.playerDataList.containsKey(sender.uniqueId)) {

                PlayerData.playerDataList[sender.uniqueId] = PlayerData(sender.uniqueId)

            }

            PlayerData.playerDataList[sender.uniqueId]!!.trust = args[1]

            sender.sendMessage(LegendaryGuard.PLUGIN.trust.replace("%trust_name%", args[1]))
            return true

        }
        if (sender is Player && args.size == 1 && args[0].equals("remove", ignoreCase = true)) {

            if (!PlayerData.playerDataList.containsKey(sender.uniqueId)) {

                PlayerData.playerDataList[sender.uniqueId] = PlayerData(sender.uniqueId)

            }

            val old = if (PlayerData.playerDataList[sender.uniqueId]!!.trust != ""){
                PlayerData.playerDataList[sender.uniqueId]!!.trust
            }else{
                "无"
            }
            PlayerData.playerDataList[sender.uniqueId]!!.trust = ""

            sender.sendMessage(LegendaryGuard.PLUGIN.trust_remove.replace("%trust_name%", old))
            return true

        }

        // /lg add 玩家名字 守护时间 给予时间 时间显示名字

        if (sender.isOp && args.size == 5 && args[0].equals("add", ignoreCase = true)) {

            if (!args[2].isInteger() || !args[3].isInteger()) {
                sender.sendMessage("§c请在参数上输入整数时长单位(秒)")
                return true
            }
            val pokemonTime = if ( args[2].toInt() < 0){
                -1L
            }else{
                args[2].toLong() * 1000
            }
            val display = args[4]

            val time = if (args[3].toInt() < 0){
                -1L
            }else{
                System.currentTimeMillis() + args[3].toLong() * 1000
            }

            if (!Bukkit.getServer().getPlayer(args[1]).isOnline) {
                sender.sendMessage("§c玩家不在线")
                return true
            }
            val player: Player = Bukkit.getServer().getPlayer(args[1])
            val point = PlayerData.Point(time, pokemonTime, display)
            if (!PlayerData.playerDataList.containsKey(player.uniqueId)) {

                PlayerData.playerDataList[player.uniqueId] = PlayerData(player.uniqueId)

            }

            PlayerData.playerDataList[player.uniqueId]!!.points.add(point)
            sender.sendMessage("§e添加成功")
            Bukkit.getServer().scheduler.runTaskAsynchronously(LegendaryGuard.PLUGIN) {
                PlayerData.playerDataList[player.uniqueId]?.save()
            }

        }


        return false

    }

    private fun String.isInteger(): Boolean {
        val pattern: Pattern = Pattern.compile("^[-+]?[\\d]*$")
        return pattern.matcher(this).matches()
    }
}