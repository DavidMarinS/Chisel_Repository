//-------------------------------------------------------------
package codes

import chisel3._					//Librería de chisel general
import chisel3.util._
import chisel3.iotesters.{PeekPokeTester, Driver, ChiselFlatSpec}

class ALU_2Tests(c: ALU_2) extends PeekPokeTester(c) {			// se define la clase donde se instancia la ALU, se definen las entradas y el comportamiento esperado
	def alu(RST: UInt, /*clk: UInt,*/ op_1: UInt, inmediato: UInt, reg_2: UInt, Deco_Instruc: UInt, alu_out: UInt) = {
			poke(c.io.RST, RST)
			//poke(c.io.clk, clk)
			poke(c.io.op_1, op_1)
			poke(c.io.inmediato, inmediato)
			poke(c.io.reg_2, reg_2)
			poke(c.io.Deco_Instruc, Deco_Instruc)
			//poke(c.io.Imm_ID, Imm_ID)
			//poke(c.io.op_ID, op_ID)
			//poke(c.io.esp_ID, esp_ID)
			expect(c.io.alu_out, alu_out)
			//expect(c.io.out_comp, out_comp)
			//step(1)
			//expect(c.io.out_COUT, out_COUT)
			//expect(c.io.out_OK, out_OK)
			//expect(c.io.selector_cp, selector_cp)
			//expect(c.io.salida_arit, salida_arit)
		}
		//alu(/*reset*/0.U/*clk0.U,*/,/*op_1*/"b10".U,/*inmediato*/1.U,/*reg2*/3.U,"b000000010011".U,/*alu_out*/3.U)
		//alu(/*reset*/0.U/*clk0.U,*/,/*op_1*/"b10".U,/*inmediato*/1.U,/*reg2*/3.U,"b000000110011".U,/*alu_out*/5.U)
		alu(/*reset*/0.U/*clk0.U,*/,/*op_1*/"b10".U,/*inmediato*/1.U,/*reg2*/3.U,"b001110010011".U,/*alu_out*/0.U) // Andi
		alu(/*reset*/0.U/*clk0.U,*/,/*op_1*/"b10".U,/*inmediato*/1.U,/*reg2*/3.U,"b001110110011".U,/*alu_out*/2.U) //And
		//alu(/*reset*/0.U/*clk0.U,*/,/*op_1*/"b10".U,/*inmediato*/1.U,/*reg2*/3.U,"b001000110011".U,/*alu_out*/1.U)
		//alu(/*reset*/0.U/*clk0.U,*/,/*op_1*/"b10".U,/*inmediato*/1.U,/*reg2*/3.U,"b000100010011".U,/*alu_out*/0.U) //SLTi
		//alu(/*reset*/0.U/*clk0.U,*/,/*op_1*/"b10".U,/*inmediato*/1.U,/*reg2*/3.U,"b000100110011".U,/*alu_out*/1.U) //SLT
		//alu(/*reset*/0.U/*clk0.U,*/,/*op_1*/"b10".U,/*inmediato*/1.U,/*reg2*/3.U,"b000010110011".U,/*alu_out*/"h00000010".U)
	}

class ALU_2Tester extends ChiselFlatSpec {
  behavior of "ALU_2"
  backends foreach {backend =>
    it should s"correctly add randomly generated numbers $backend" in {
      Driver(() => new ALU_2, backend)(c => new ALU_2Tests(c)) should be (true)
    }
  }
}
