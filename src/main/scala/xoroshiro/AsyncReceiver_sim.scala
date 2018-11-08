package Xoroshiro

import spinal.core._
import spinal.sim._
import spinal.core.sim._

import scala.util.Random


// AsyncReceiver testbench
object AsyncReceiverSim {
  def main(args: Array[String]) {

    val compiled = SimConfig.withWave.compile {
      val dut = new AsyncReceiver
      dut.state.simPublic()
      dut.bitCount.simPublic()
      dut.shifter.simPublic()
      dut.bitTimer.simPublic()
      dut
    }
    compiled.doSim("AsyncReceiver") { dut =>

      def initDutSignals (): Unit = {
        dut.io.enable #= false
        dut.io.mem_valid #= false
        dut.io.mem_addr #= 0
        dut.io.baudClockX64 #= false
        dut.io.rx #= true
      }

      def busToRead(address:Int) {
        dut.io.enable #= true
        dut.io.mem_valid #= true
        dut.io.mem_addr #= address
      }

      def busToIdle() {
        dut.io.enable #= false
        dut.io.mem_valid #= false
        dut.io.mem_addr #= 0
      }

      var baudClock64Count = 0
      var baudClcokDiv = 0
      var baudClock64 = false

      def generateBaudClock64(): Unit = {
        baudClcokDiv += 1
        if (baudClcokDiv == 2) {
          baudClcokDiv = 0
          if (baudClock64) {
            baudClock64 = false
          } else {
            baudClock64 = true
            baudClock64Count += 1
          }
          dut.io.baudClockX64 #= baudClock64
        }
      }

      // Fork a process to generate the reset and the clock on the dut
      dut.clockDomain.forkStimulus(period = 10)

      var modelState = 0

      initDutSignals()

      var idx = 0
      while (idx < 3000) {
        generateBaudClock64()

        busToRead(4)
        dut.clockDomain.waitRisingEdge()
        // Read outputs
        print(f"clock: ${idx}%08d, ")
        print(f"baudclkCnt: ${baudClock64Count}%08d, ")
        print(f"rx: ${dut.io.rx.toBoolean}, ")
        print(f"state: ${dut.state.toInt}%08d, ")
        print(f"bitTimeOut: ${dut.bitTimer.toInt}%08d, ")
        print(f"bitcnt: ${dut.bitCount.toInt}%08d, ")
        print(f"shifter: ${dut.shifter.toInt}%08x, ")
        print(f"data: ${dut.io.mem_rdata.toLong}%08x")
        println()

        busToIdle()
        dut.clockDomain.waitRisingEdge()

        // Start bit
        if (baudClock64Count == 2 * 64) {
          dut.io.rx #= false
        }
        // Bit 0
        if (baudClock64Count == 3 * 64) {
          dut.io.rx #= false
        }
        // Bit 1
        if (baudClock64Count == 4 * 64) {
          dut.io.rx #= true
        }
        // Bit 2
        if (baudClock64Count == 5 * 64) {
          dut.io.rx #= false
        }
        // Bit 3
        if (baudClock64Count == 6 * 64) {
          dut.io.rx #= true
        }
        // Bit 4
        if (baudClock64Count == 7 * 64) {
          dut.io.rx #= false
        }
        // Bit 5
        if (baudClock64Count == 8 * 64) {
          dut.io.rx #= true
        }
        // Bit 6
        if (baudClock64Count == 9 * 64) {
          dut.io.rx #= false
        }
        // Bit 7
        if (baudClock64Count == 10 * 64) {
          dut.io.rx #= true
        }
        // Stop bit
        if (baudClock64Count == 11 * 64) {
          dut.io.rx #= true
        }

        idx += 1
      }


      busToRead(0)
      dut.clockDomain.waitRisingEdge()
      // Read outputs
      println("Expect data = aa :")
      print(f"clock: ${idx}%08d, ")
      print(f"baudclkCnt: ${baudClock64Count}%08d, ")
      print(f"rx: ${dut.io.rx.toBoolean}, ")
      print(f"state: ${dut.state.toInt}%08d, ")
      print(f"bitTimer: ${dut.bitTimer.toInt}%08d, ")
      print(f"bitcnt: ${dut.bitCount.toInt}%08d, ")
      print(f"shifter: ${dut.shifter.toInt}%08x, ")
      print(f"data: ${dut.io.mem_rdata.toLong}%08x")
      println()

      busToRead(4)
      dut.clockDomain.waitRisingEdge()
      // Read outputs
      println("Expect full = 0 :")
      print(f"clock: ${idx}%08d, ")
      print(f"baudclkCnt: ${baudClock64Count}%08d, ")
      print(f"rx: ${dut.io.rx.toBoolean}, ")
      print(f"state: ${dut.state.toInt}%08d, ")
      print(f"bitTimer: ${dut.bitTimer.toInt}%08d, ")
      print(f"bitcnt: ${dut.bitCount.toInt}%08d, ")
      print(f"shifter: ${dut.shifter.toInt}%08x, ")
      print(f"full: ${dut.io.mem_rdata.toLong}%08x")
      println()
    }
  }
}