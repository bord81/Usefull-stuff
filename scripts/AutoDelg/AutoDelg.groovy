class AutoDelg {
    static def propFileName = 'prop.prop'
    static def props = new Properties()
    static def programName = 'deluge-console'
    static def cmds_keys = [ac : 'addCommand', pc: 'pauseCommand', rc: 'removeCommand', sdc: 'showDownloadingCommand',
                            ssc: 'showSeedingCommand', sqc: 'showQueuedCommand', spc: 'showPausedCommand',
                            kd : 'keyDownloading', ks: 'keySeeding', kq: 'keyQueued', kp: 'keyPaused', kr: 'keyRemove',
                            sp : 'savePath', ke: 'keyExit']
    static def space = ' '
    static def missing = { println("echo $it property is missing") }

    static void main(String[] args) {
        def file = new File(System.getProperty('user.dir') + File.separator + propFileName)
        if (!file.exists()) {
            println("echo Please create $propFileName in the script directory.")
            return
        }
        props = new ConfigSlurper().parse(file.toURI().toURL()).toProperties()
        boolean exit = false
        cmds_keys.each {
            if (props.getProperty(it.value) == null) {
                missing.call(it.value)
                exit = true
            }
        }
        if (exit)
            return
        while (true) {
            def next = System.in.newReader().readLine()
            if (next == props.getProperty(cmds_keys.ke)) {
                break
            } else if (next == props.getProperty(cmds_keys.kd)) {
                showInfo('sdc')
            } else if (next == props.getProperty(cmds_keys.ks)) {
                showInfo('ssc')
            } else if (next == props.getProperty(cmds_keys.kq)) {
                showInfo('sqc')
            } else if (next == props.getProperty(cmds_keys.kp)) {
                showInfo('spc')
            } else if (next ==~ /(\w\s)[\w.]+/) {
                if (next.charAt(0) == props.getProperty(cmds_keys.kr)) {
                    executeCmdWithParams('rc', next.substring(2, next.length()))
                } else if (next.charAt(0) == props.getProperty(cmds_keys.kp)) {
                    executeCmdWithParams('pc', next.substring(2, next.length()))
                }
            } else {
                def cmd = programName + space + props.getProperty(cmds_keys.ac) + space + props.getProperty(cmds_keys.sp) + space + next
                println(cmd)
            }
        }
    }

    static void showInfo(String s) {
        println(programName + space + props.getProperty(cmds_keys.get(s)))
    }

    static void executeCmdWithParams(String cmd, String param) {
        println(programName + space + props.getProperty(cmds_keys.get(cmd)) + space + param)
    }
}
