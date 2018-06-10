package ja.gauthier.findsong.types

/**
  *  Contains the class / type definitions for peaks.
  */
package object peak {
  case class Peak(amplitude: Int, frequency: Int, time: Int)
      extends Ordered[Peak] {

    /**
      * Compares two peaks, according to a descending order for the amplitude, and an ascending order for the time and the frequency.
      *
      * @param that the other match
      * @return the result of the comparison
         **/
    def compare(that: Peak): Int =
      Ordering
        .Tuple3[Int, Int, Int]
        .compare(
          (-this.amplitude, this.time, this.frequency),
          (-that.amplitude, that.time, that.frequency)
        )
  }
}
