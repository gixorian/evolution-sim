import org.openrndr.application
import org.openrndr.extra.olive.oliveProgram


fun main() = application {

    configure {
        width = 800
        height = 800
    }
    oliveProgram {
        extend {
        }
    }
}
