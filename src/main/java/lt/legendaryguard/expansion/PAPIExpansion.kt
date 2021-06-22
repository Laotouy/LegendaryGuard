package lt.legendaryguard.expansion

import lt.legendaryguard.LegendaryGuard
import lt.legendaryguard.data.PlayerData
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.entity.Player
import java.text.SimpleDateFormat
import java.util.*

class PAPIExpansion(LegendaryGuard: LegendaryGuard?) : PlaceholderExpansion() {
    private val plugin: LegendaryGuard? = null
    var ft = SimpleDateFormat("yyyy-MM-dd hh:mm")

    override fun getIdentifier(): String {
        return "lg"
    }

    override fun getAuthor(): String {
        return "Laotou"
    }

    override fun getVersion(): String {
        return "1.0"
    }

    override fun onPlaceholderRequest(player: Player, params: String): String {
        if (params.contains("point_time", ignoreCase = true) || params.contains("point_limit", ignoreCase = true)) { // %tribe_point_type%

            var endTime = -1L
            var disTime = LegendaryGuard.PLUGIN.defaultDisplay

            if (PlayerData.playerDataList.containsKey(player.uniqueId) && PlayerData.playerDataList[player.uniqueId]!!.points.size > 0) {


                val pd = PlayerData.playerDataList[player.uniqueId]
               // println(pd!!.points.size)
                while (pd!!.points.size > 0 && pd!!.points[pd.points.size - 1].endTime != -1L && pd.points[pd.points.size - 1].endTime < System.currentTimeMillis()) {

                    pd.points.removeAt(pd.points.size - 1)


                }
              //  println(pd.points.size)
             // rintln(pd.points)
              //  println(pd)

               // println(pd.points[pd.points.size - 1])
                val point = pd.points[pd.points.size - 1]
                disTime = point.displayTime
                endTime = point.endTime
            } else {

                LegendaryGuard.PLUGIN.permissionList.forEach {
                    if (player.hasPermission(it)) {

                        disTime = LegendaryGuard.PLUGIN.permissionMap[it]!!.disTime

                        endTime = -1

                    }
                }

            }
            if (params.contains("point_time", ignoreCase = true)) {
                if (endTime == -1L) {
                    return "永久"
                }

                //return "123"
                return timeDiff(ft.format(Date()),ft.format(Date(endTime)))
            }
            if (params.contains("point_limit", ignoreCase = true)) {
                return disTime
            }

        }
        return " "

    }

    private fun timeDiff(pBeginTime: String, pEndTime: String): String {
        val format = SimpleDateFormat("yyyy-MM-dd hh:mm")
        val beginL: Long = format.parse(pBeginTime).getTime()
        val endL: Long = format.parse(pEndTime).getTime()
        val day = (endL - beginL) / 86400000
        val hour = (endL - beginL) % 86400000 / 3600000
        val min = (endL - beginL) % 86400000 % 3600000 / 60000
        return "${day}天${hour}小时${min}分钟"
    }
}