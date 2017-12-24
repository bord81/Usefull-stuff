class AutoDelg {
    static def propFileName = 'prop.prop'
    static def mainCommand = 'deluge-console'
    static def ac = 'addCommand'
    static def qc = 'quitCommand'
    static def sp = 'savePath'
    static def spc = ' '
    static def missing = { println("$it property is missing") }

    static void main(String[] args) {
        def file = new File(System.getProperty('user.dir') + File.separator + propFileName)
        if (!file.exists()) {
            println("Please create $propFileName in the script directory.")
            return
        }
        def props = new ConfigSlurper().parse(file.toURI().toURL()).toProperties()
        if (props.getProperty(ac) == null) {
            missing.call(ac)
            return
        } else if (props.getProperty(qc) == null) {
            missing.call(qc)
            return
        } else if (props.getProperty(sp) == null) {
            missing.call(sp)
            return
        }
        while (true) {
            def next = System.in.newReader().readLine()
            if (next == props.getProperty(qc)) {
                break
            } else {
                def cmd = mainCommand + spc + props.getProperty(ac) + spc + props.getProperty(sp) + spc + next
                println(cmd)
            }
        }
    }
}
