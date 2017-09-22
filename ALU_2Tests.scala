//-------------------------------------------------------------
package codes

import chisel3._					//Librería de chisel general
import chisel3.util._
import chisel3.iotesters.{PeekPokeTester, Driver, ChiselFlatSpec}

class ALU_2Tests(c: ALU_2) extends PeekPokeTester(c) {			// se define la clase donde se instancia la ALU, se definen las entradas y el comportamiento esperado
	def alu(RST: UInt, clk: UInt, op_1: UInt, inmediato: UInt, reg_2: UInt, Imm_ID: UInt, op_ID: UInt, esp_ID: UInt, alu_out: UInt, out_comp: UInt, out_OK: UInt) = {
			poke(c.io.RST, RST)
			poke(c.io.clk, clk)
			poke(c.io.op_1, op_1)
			poke(c.io.inmediato, inmediato)
			poke(c.io.reg_2, reg_2)
			poke(c.io.Imm_ID, Imm_ID)
			poke(c.io.op_ID, op_ID)
			poke(c.io.esp_ID, esp_ID)
			expect(c.io.alu_out, alu_out)
			expect(c.io.out_comp, out_comp)
			step(1)
			//expect(c.io.out_COUT, out_COUT)
			expect(c.io.out_OK, out_OK)
		}
		alu(/*reset*/0.U,/*clk*/0.U,/*op_1*/3.U,/*inmediato*/4.U,/*reg2*/3.U,/*Imm_ID*/"b0110011".U,/*op_ID*/"b111".U,/*esp_ID*/"b00".U,/*alu_out*/1.U,/*out_comp*/0.U,/*out_OK*/0.U)
	}

class ALU_2Tester extends ChiselFlatSpec {
  behavior of "ALU_2"
  backends foreach {backend =>
    it should s"correctly add randomly generated numbers $backend" in {
      Driver(() => new ALU_2, backend)(c => new ALU_2Tests(c)) should be (true)
    }
  }
}
