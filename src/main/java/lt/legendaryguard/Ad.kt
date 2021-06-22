package lt.legendaryguard

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL

class Ad {



    init {

        val url = URL("http://api.7mc7.com/ad/AD.txt")
        val inputS = url.openStream()
        val isr = InputStreamReader(inputS)
        val br = BufferedReader(isr)
        for (str in br.lines()){
            println(str)
        }


    }


}