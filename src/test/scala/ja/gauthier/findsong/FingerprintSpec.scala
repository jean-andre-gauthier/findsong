package ja.gauthier.findsong

import breeze.linalg._
import breeze.math._
import breeze.signal._
import org.scalatest._

class FingerprintSpec extends FlatSpec with Matchers {
  val sine_6_length_24 = Array[Byte](
    0,1,0,-1,
    0,1,0,-1,
    0,1,0,-1,
    0,1,0,-1,
    0,1,0,-1,
    0,1,0,-1
  )

  val sine_3_6_length_24 = Array[Byte](
    0, 17, 10, -3, 0, 3, -10, -17,
    0, 17, 10, -3, 0, 3, -10, -17,
    0, 17, 10, -3, 0, 3, -10, -17
  )

  val sine_4_6_length_24 = Array[Byte](
    0, 19, 9, -10, -9, 1, 0, -1, 9, 10, -9, -19,
    0, 19, 9, -10, -9, 1, 0, -1, 9, 10, -9, -19
  )

  "signalToSpectrogram" should "transform the time domain to the frequency domain for a single frequency wave" in {
    val spectrogram = Fingerprint.signalToSpectrogram(sine_6_length_24)
    val maxFrequency = spectrogram.max
    spectrogram.rows shouldEqual 1
    spectrogram.cols shouldEqual 12
    spectrogram(0, 0 until 5).t  shouldEqual DenseVector.zeros[Int](5)
    spectrogram(0,6) shouldEqual maxFrequency
    spectrogram(0, 7 to -1).t  shouldEqual DenseVector.zeros[Int](5)
  }

  "signalToSpectrogram" should "transform the time domain to the frequency domain multiple frequency wave" in {
    val spectrogram = Fingerprint.signalToSpectrogram(sine_4_6_length_24)
    println(spectrogram)
    // val maxFrequency = spectrogram.max
    // spectrogram.rows shouldEqual 1
    // spectrogram.cols shouldEqual 12
    // spectrogram(0, 0 until 5).t  shouldEqual DenseVector.zeros[Int](5)
    // spectrogram(0,6) shouldEqual maxFrequency
    // spectrogram(0, 7 to -1).t  shouldEqual DenseVector.zeros[Int](5)
  }
}
