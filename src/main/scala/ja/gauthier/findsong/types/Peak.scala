package ja.gauthier.findsong.types

package object peak {
    case class Peak(amplitude: Int, frequency: Int, time: Int) extends Ordered[Peak] {
        def compare(that: Peak): Int =
            Ordering.Tuple3[Int, Int, Int]
                .compare(
                    (-this.amplitude, this.time, this.frequency),
                    (-that.amplitude, that.time, that.frequency)
                )
    }
}
