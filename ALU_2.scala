//Esta linea siempre se comenta
package codes
import chisel3._
import chisel3.util._

//---------------------------------------------------------------
//--------------- Decodificador de Operador ------------------
//---------------------------------------------------------------
class Deco_OP extends Module {
  val io = IO(new Bundle {
	val RST = Input(UInt(1.W))			//Reset
    val in_Imm = Input(UInt(32.W))		//Valor inmediato
    //val in_Imm_S = Input(SInt(32.W))	//valor inmediato como SINT
    val in_reg2 = Input(UInt(32.W))		//registro2
    //val in_reg2_S = Input(SInt(32.W))	//reg 2 como SINT
    val ID_imm = Input(UInt(7.W))		//Identificador de inmediato
    val out = Output(UInt(32.W))		//salida como UINT
    //val out_S = Output(SInt(32.W))			//salida como SINT
    //val EN_out = Output(UInt(1.W))
  })
  when(io.RST === 1.U) {
	  io.out := 0.U
	  //io.out_S := 0.U	  
  }.otherwise {
  when(io.ID_imm === 19.U) {
	  io.out := io.in_Imm
	  //io.out_S := io.in_Imm_S
	  }.otherwise {
		  io.out := io.in_reg2
		  //io.out_S := io.in_reg2 
	  }
	}
} 
//---------------------------------------------------------------
//----------------- Decodificador de Salida ---------------------
//---------------------------------------------------------------

class Deco_salida extends Module {
  val io = IO(new Bundle {
	val RST = Input(UInt(1.W))			//reset
    val ID_imm = Input(UInt(7.W))		//identificador de inmediato
    val op_ID = Input(UInt(3.W))		//identificador de operacion
	//val esp_ID = Input(UInt(2.W))		//identificador especial
    val out = Output(UInt(2.W))			//salida que se debe conectar al selector de un mux con cuatro entradas
    val EN_Shift = Output(UInt(1.W))	//Habilitador de operaciones de shift
  })
  when(io.RST === 1.U) {
	  io.out := 0.U
	  io.EN_Shift := 0.U  
  }.otherwise{
	  when(io.op_ID === 0.U) {				//Op Aritmeticas y de comp
		  io.EN_Shift := 0.U  				//Mantener apagados los shft
		  when(io.ID_imm === 99.U) {		//->op de comp
			  io.out := 1.U
		  }.otherwise {						//->op aritmetica
			  io.out := 0.U
		  }
	  }.elsewhen(io.op_ID === 1.U | io.op_ID === 5.U) { //op comp o desplazamiento
		  io.EN_Shift := 0.U  				//Mantener apagados los shft
		  when(io.ID_imm === 99.U) {		//->op de comp
			  io.out := 1.U
		  }.otherwise {	
			  io.out := 3.U					//->op desplazamiento
			  io.EN_Shift := 1.U 
		  }
	  }.elsewhen(io.op_ID === 2.U | io.op_ID === 3.U) {	//op comp 
		  io.EN_Shift := 0.U  				//Mantener apagados los shft	
		  io.out := 1.U
	  }.elsewhen(io.op_ID === 4.U | io.op_ID === 6.U | io.op_ID === 7.U) {				//op comp o logica
		  io.EN_Shift := 0.U  				//Mantener apagados los shft
		  when(io.ID_imm === 99.U) {		//->op de comp
			  io.out := 1.U
		  }.otherwise {						//->op logicas
			  io.out := 2.U
		  }
	  }
  }
}
//------------------------------------------------------------------
//------------------- Unidad Aritmetica ----------------------------
//------------------------------------------------------------------
class Aritmetic(val w: Int) extends Module {
  val io = IO(new Bundle { 
	val RST = Input(UInt(1.W))				//reset
    val in0 = Input(UInt(w.W))				//Entrada del operando1
    val in1 = Input(UInt(w.W))				//Entrada del operando2
    //val ID_OP = Input(UInt(3.W))			//Identificador de operacion
    val ID_ESP = Input(UInt(2.W))			//Identificador especial
    val out = Output(UInt(w.W))				//salida aritmetica
    //val c_out = Output(UInt(1.W))			//carry out
  })
  when(io.RST === 1.U) {
	  io.out := 0.U
	  //io.c_out := 0.U 
  }.otherwise {
  when(io.ID_ESP === 2.U) {
	  //io.c_out := 0.U
	  io.out := io.in0 - io.in1
	  }.otherwise {
		  //io.c_out := 0.U
		  io.out := io.in0 + io.in1
		  //when(logic2Ceil(io.out) > 32.U) {
			//  io.c_out := 1.U
		  //}
	  }
  }
}


//------------------------------------------------------------------
//------------------- Unidad Logica --------------------------------
//------------------------------------------------------------------
class logic extends Module {
	val io = IO(new Bundle {
	  val RST = Input(UInt(1.W))			//reset
	  val in0 = Input(UInt(32.W))			//entrada operando1
      val in1 = Input(UInt(32.W))			//entrada operando2
      val ID_OP = Input(UInt(3.W))			//identificador de operacion
      val out = Output(UInt(32.W))			//salida op logica
  })
  when(io.RST === 1.U) {
	  io.out := 0.U
  }.otherwise {
  when(io.ID_OP === 7.U) {
	  io.out := io.in0 & io.in1
  }
  when(io.ID_OP === 4.U) {
	  io.out := io.in0 ^ io.in1 //(a & ~b) | (~a & b)
  }.otherwise {
	  io.out := io.in0 | io.in1
  }
	}
}
	 
//-----------------------------------------------------------------
//------------------- Op. Comparacion -----------------------------
//-----------------------------------------------------------------

class comp extends Module {	
	val io = IO(new Bundle {
	  val RST = Input(UInt(1.W))			//reset
      val in0 = Input(UInt(32.W)) 			// operando 1
      //val in0_S = Input(SInt(32.W))			// operando 1 como SINT
      val in1 = Input(UInt(32.W)) 			// operando 2
      //val in1_S = Input(SInt(32.W))			// operando 2 como SINT
      val ID_OP = Input(UInt(3.W))			// identificador de operacion
      val flag_out = Output(UInt(1.W))		// bandera de salida
  })
  when(io.RST === 1.U) {
	  io.flag_out := 0.U
  }.otherwise {
  when(io.ID_OP === 2.U) {
	  when(io.in0<io.in1){
		  io.flag_out := 1.U
		  }.otherwise{
			  io.flag_out := 0.U
		  }
  }
  when(io.ID_OP === 3.U) {
	  when(io.in0<io.in1) {
		  io.flag_out := 1.U
		  }.otherwise{
			  io.flag_out := 0.U
		  }
  }
  when(io.ID_OP === 0.U) {
	  when(io.in0 === io.in1){
		  io.flag_out := 1.U
		  }.otherwise{
			  io.flag_out := 0.U
		  }	  
  }
  when(io.ID_OP === 5.U) {
	  when(io.in0 >= io.in1){
		  io.flag_out := 1.U
		  }.otherwise{
			  io.flag_out := 0.U
		  }	 
  }
  when(io.ID_OP === 1.U) {
	  when(io.in0 === io.in1){
		  io.flag_out := 0.U
		  }.otherwise{
			  io.flag_out := 1.U
		  }	  
  }
  when(io.ID_OP === 4.U) {
	  when(io.in0 < io.in1){
		  io.flag_out := 1.U
		  }.otherwise{
			  io.flag_out := 0.U
		  }	 
  }
  when(io.ID_OP === 6.U) {
	  when(io.in0 < io.in1){
		  io.flag_out := 1.U
		  }.otherwise{
			  io.flag_out := 0.U
		  }	 
  }
  when(io.ID_OP === 7.U) {
	  when(io.in0 >= io.in1){
		  io.flag_out := 1.U
		  }.otherwise{
			  io.flag_out := 0.U
		  }	 
  }
	}
}	  
//-------------------------------------------------------------------
//--------------- Operaciones de Desplazamiento	---------------------
//-------------------------------------------------------------------
class Shift extends Module {
	val io = IO(new Bundle {
	  val RST = Input(UInt(1.W))		//Reset
	  val EN = Input(UInt(1.W))			//habilitador de shift
	  val clk = Input(UInt(1.W))		//entrada de reloj
      val in0 = Input(UInt(32.W))		// entrada del operando1
      val in1 = Input(UInt(5.W)) 		// Ultimos 5 bits del operando 2
      val ID_OP = Input(UInt(3.W))		//Identificador de operacion
      val ID_ESP = Input(UInt(2.W))		//Identficador especial
      val out = Output(UInt(32.W))		// salida
      val out_ready = Output(UInt(1.W))	//salida de listo
  })
  when(io.RST === 1.U) {
	  io.out := 0.U
	  io.out_ready := 0.U
  }.otherwise {
	  val bus = Wire(UInt(32.W))
	  val regO = RegNext(bus)
	  val regS = RegNext(io.in0)
	  val regD = RegNext(io.in1)
	  io.out := regO
	  //io.in0 := regS
	  when(io.EN === 1.U) {
		  io.out_ready := 0.U
		  when(io.ID_OP === 5.U) {
			  io.out := /*regS*/io.in0 >> io.in1/*regD*/
			  io.out_ready := 1.U
		  }.otherwise {
			  io.out := io.in0 << regD
			  io.out_ready := 1.U
		}
		//io.out := regO
	  }.otherwise {
		  io.out := 0.U
		  io.out_ready := 1.U
	  }
	  	
  /*when(io.EN === 1.U && io.clk === 1.U) {
	  io.out_ready := 0.U
  when(io.ID_OP === 5.U) {
	  when(io.ID_ESP === 0.U) {
		  io.out := io.in0 >> io.in1			//logical shift
		  io.out_ready := 1.U
	  }.otherwise {
		  io.out := io.in0 >> io.in1			//Aritmetic Shift
		  io.out_ready := 1.U
	  }
  }.otherwise {
	  io.out := io.in0 << io.in1
	  io.out_ready := 1.U
  }
  }*/
}
}
//------------------------------------------------------------------
//-------------------------- Multiplexado --------------------------
//------------------------------------------------------------------

class Mux4 extends Module {
  val io = IO(new Bundle {
	val RST = Input(UInt(1.W))     
    val in0 = Input(UInt(32.W))
    val in1 = Input(UInt(32.W))
    val in2 = Input(UInt(32.W))
    val in3 = Input(UInt(32.W))
    val sel = Input(UInt(2.W))
    val out = Output(UInt(32.W))
  })
  when(io.RST === 1.U) {
	  io.out := 0.U
  }.otherwise {
	  when(io.sel === 0.U) {
		  io.out := io.in0
	  }.elsewhen(io.sel === 1.U) {
		  io.out := io.in1
	  }.elsewhen(io.sel === 2.U) {
		  io.out := io.in2
	  }.otherwise {
		  io.out := io.in3
	  }
  }
}
  
//----------------------------------------------------------------
//--------------------------- ALU --------------------------------
//----------------------------------------------------------------
class ALU_2 extends Module {
	val io = IO(new Bundle {
		val RST = Input(UInt(1.W))
		val clk = Input(UInt(1.W))
		val op_1 = Input(UInt(32.W))
		val inmediato = Input(UInt(32.W))
		val reg_2 = Input(UInt(32.W))
		val Imm_ID = Input(UInt(7.W))
		val op_ID = Input(UInt(3.W))
		val esp_ID = Input(UInt(2.W))
		val alu_out = Output(UInt(32.W))
		val out_comp = Output(UInt(1.W))
		//val out_COUT = Output(UInt(1.W)) 
		val out_OK = Output(UInt(1.W))
		val selector_cp = Output(UInt(2.W))
		//val salida_arit = Output(UInt(32.W))
	})
	when(io.RST===1.U) {
		//io.alu_out := 0.U
		io.out_comp := 0.U
		io.out_OK := 0.U
	}.otherwise {
	//Instanciar el decodificador de operandos
	val deco_ops = Module(new Deco_OP())
	deco_ops.io.RST := io.RST
	deco_ops.io.in_Imm := io.inmediato
	//deco_ops.io.in_Imm_S := io.inmediato
	deco_ops.io.in_reg2 := io.reg_2
	//deco_ops.io.in_reg2_S := io.reg_2
	deco_ops.io.ID_imm := io.Imm_ID
	//deco_ops.io.in_reg2 := io.reg_2
	
	//Instanciar Unidad aritmetica
	val arit_unit = Module(new Aritmetic(32))
	arit_unit.io.RST := io.RST
	arit_unit.io.in0 := io.op_1
	arit_unit.io.in1 := deco_ops.io.out
	//arit_unit.io.ID_OP := io.op_ID
	arit_unit.io.ID_ESP := io.esp_ID
	//arit_unit.io.c_out := io.out_COUT
	//instanciar su salida al mux
	
	//Instanciar Unidad Logica
	val logico_u = Module(new logic())
	logico_u.io.RST := io.RST
	logico_u.io.in0 := io.op_1
	logico_u.io.in1 := deco_ops.io.out
	logico_u.io.ID_OP := io.op_ID
	//instanciar su salida al mux
	
	//Instanciar Unidad Comparativa
	val comparar = Module(new comp())
	comparar.io.RST := io.RST
	comparar.io.in0 := io.op_1
	//comparar.io.in0_S := io.op_1
	comparar.io.in1 := deco_ops.io.out
	//comparar.io.in1_S := deco_ops.io.out_S
	comparar.io.ID_OP := io.op_ID
	io.out_comp := comparar.io.flag_out 
	
	//Instanciar Deco de salidas
	val deco_out = Module(new Deco_salida())
	deco_out.io.RST := io.RST
	deco_out.io.ID_imm := io.Imm_ID
	deco_out.io.op_ID := io.op_ID
	//instanciar su salida al selector del mux
	
	//Instanciar unidad de desplazamiento
	val desplazar = Module(new Shift ())
	desplazar.io.RST := io.RST
	desplazar.io.EN := deco_out.io.EN_Shift
	desplazar.io.clk := io.clk
	desplazar.io.in0 := io.op_1
	desplazar.io.in1 := Cat(deco_ops.io.out(4,0))
	//desplazar.io.in1(0) := deco_ops.io.out(0)
	//desplazar.io.in1(1) := deco_ops.io.out(1)
	//desplazar.io.in1(2) := deco_ops.io.out(2)
	//desplazar.io.in1(3) := deco_ops.io.out(3)
	//desplazar.io.in1(4) := deco_ops.io.out(4)
	desplazar.io.ID_OP := io.op_ID
	desplazar.io.ID_ESP := io.esp_ID
	io.out_OK := desplazar.io.out_ready  
	//instanciar su salida al mux
	
	//Instanciar el mux 4:1
	val m4 = Module(new Mux4())
	m4.io.in0 := arit_unit.io.out
	m4.io.in1 := 5.U
	m4.io.in2 := logico_u.io.out
	m4.io.in3 := desplazar.io.out
	m4.io.sel := deco_out.io.out
	io.alu_out := m4.io.out 
	io.selector_cp := m4.io.sel
	//io.salida_arit := m4.io.in0
}
}

object ALU_2Driver extends App {
 chisel3.Driver.execute(args, () =>new ALU_2)
}
	
