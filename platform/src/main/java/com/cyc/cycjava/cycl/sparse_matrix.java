/***
 *   Copyright (c) 1995-2009 Cycorp Inc.
 * 
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *   
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 *  Substantial portions of this code were developed by the Cyc project
 *  and by Cycorp Inc, whose contribution is gratefully acknowledged.
*/

package  com.cyc.cycjava.cycl;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.ArrayList;
import com.cyc.tool.subl.jrtl.nativeCode.subLisp.*;
import com.cyc.tool.subl.jrtl.nativeCode.type.core.*;
import com.cyc.tool.subl.jrtl.nativeCode.type.symbol.*;
import com.cyc.tool.subl.jrtl.nativeCode.type.number.*;
import com.cyc.tool.subl.jrtl.translatedCode.sublisp.*;
import com.cyc.tool.subl.util.*;
import static com.cyc.tool.subl.jrtl.nativeCode.type.core.SubLObjectFactory.makeBoolean;
import static com.cyc.tool.subl.jrtl.nativeCode.type.core.SubLObjectFactory.makeInteger;
import static com.cyc.tool.subl.jrtl.nativeCode.type.core.SubLObjectFactory.makeDouble;
import static com.cyc.tool.subl.jrtl.nativeCode.type.core.SubLObjectFactory.makeChar;
import static com.cyc.tool.subl.jrtl.nativeCode.type.core.SubLObjectFactory.makeString;
import static com.cyc.tool.subl.jrtl.nativeCode.type.core.SubLObjectFactory.makeSymbol;
import static com.cyc.tool.subl.jrtl.nativeCode.type.core.SubLObjectFactory.makeKeyword;
import static com.cyc.tool.subl.jrtl.nativeCode.type.core.SubLObjectFactory.makeUninternedSymbol;
import static com.cyc.tool.subl.jrtl.nativeCode.type.core.SubLObjectFactory.makeGuid;
import static com.cyc.tool.subl.jrtl.nativeCode.subLisp.ConsesLow.cons;
import static com.cyc.tool.subl.jrtl.nativeCode.subLisp.ConsesLow.list;
import static com.cyc.tool.subl.jrtl.nativeCode.subLisp.ConsesLow.listS;
import static com.cyc.tool.subl.util.SubLFiles.defconstant;
import static com.cyc.tool.subl.util.SubLFiles.deflexical;
import static com.cyc.tool.subl.util.SubLFiles.defparameter;
import static com.cyc.tool.subl.util.SubLFiles.defvar;
import static com.cyc.tool.subl.util.SubLFiles.declareFunction;
import static com.cyc.tool.subl.util.SubLFiles.declareMacro;


import com.cyc.cycjava.cycl.access_macros;
import com.cyc.cycjava.cycl.constant_handles;
import com.cyc.cycjava.cycl.hash_table_utilities;
import com.cyc.cycjava.cycl.number_utilities;
import com.cyc.cycjava.cycl.sparse_vector;
import com.cyc.cycjava.cycl.subl_promotions;

public  final class sparse_matrix extends SubLTranslatedFile {

  //// Constructor

  private sparse_matrix() {}
  public static final SubLFile me = new sparse_matrix();
  public static final String myName = "com.cyc.cycjava.cycl.sparse_matrix";

  //// Definitions

  public static final class $sparse_matrix_native extends SubLStructNative {
    public SubLStructDecl getStructDecl() { return structDecl; }
    public SubLObject getField2() { return $rows; }
    public SubLObject getField3() { return $columns; }
    public SubLObject setField2(SubLObject value) { return $rows = value; }
    public SubLObject setField3(SubLObject value) { return $columns = value; }
    public SubLObject $rows = NIL;
    public SubLObject $columns = NIL;
    private static final SubLStructDeclNative structDecl =
    Structures.makeStructDeclNative($sparse_matrix_native.class, $sym0$SPARSE_MATRIX, $sym1$SPARSE_MATRIX_P, $list2, $list3, new String[] {"$rows", "$columns"}, $list4, $list5, $sym6$SMATRIX_PRINT);
  }

  @SubL(source = "cycl/sparse-matrix.lisp", position = 3092) 
  public static SubLSymbol $dtp_sparse_matrix$ = null;

  @SubL(source = "cycl/sparse-matrix.lisp", position = 3092) 
  public static final SubLObject sparse_matrix_print_function_trampoline(SubLObject object, SubLObject stream) {
    Errors
			.handleMissingMethodError("This call was replaced for LarKC purposes. Originally a method was called. Refer to number 5250");
    return NIL;
  }

  public static final class $sparse_matrix_p$UnaryFunction extends UnaryFunction {
    public $sparse_matrix_p$UnaryFunction() { super(extractFunctionNamed("SPARSE-MATRIX-P")); }
    public SubLObject processItem(SubLObject arg1) { return Errors
			.handleMissingMethodError("This call was replaced for LarKC purposes. Originally a method was called. Refer to number 5264"); }
  }

  public static final SubLObject declare_sparse_matrix_file() {
    declareFunction(myName, "sparse_matrix_print_function_trampoline", "SPARSE-MATRIX-PRINT-FUNCTION-TRAMPOLINE", 2, 0, false);
    //declareFunction(myName, "sparse_matrix_p", "SPARSE-MATRIX-P", 1, 0, false); new $sparse_matrix_p$UnaryFunction();
    //declareFunction(myName, "smatrix_rows", "SMATRIX-ROWS", 1, 0, false);
    //declareFunction(myName, "smatrix_columns", "SMATRIX-COLUMNS", 1, 0, false);
    //declareFunction(myName, "_csetf_smatrix_rows", "_CSETF-SMATRIX-ROWS", 2, 0, false);
    //declareFunction(myName, "_csetf_smatrix_columns", "_CSETF-SMATRIX-COLUMNS", 2, 0, false);
    //declareFunction(myName, "make_sparse_matrix", "MAKE-SPARSE-MATRIX", 0, 1, false);
    //declareFunction(myName, "new_sparse_matrix", "NEW-SPARSE-MATRIX", 0, 2, false);
    //declareFunction(myName, "smatrix_test", "SMATRIX-TEST", 1, 0, false);
    //declareFunction(myName, "smatrix_print", "SMATRIX-PRINT", 3, 0, false);
    //declareFunction(myName, "zero_smatrix_p", "ZERO-SMATRIX-P", 1, 0, false);
    //declareFunction(myName, "smatrix_copy", "SMATRIX-COPY", 1, 0, false);
    //declareFunction(myName, "smatrix_set_row", "SMATRIX-SET-ROW", 3, 0, false);
    //declareFunction(myName, "smatrix_set", "SMATRIX-SET", 4, 0, false);
    //declareFunction(myName, "smatrix_get", "SMATRIX-GET", 3, 0, false);
    //declareFunction(myName, "smatrix_add", "SMATRIX-ADD", 2, 0, false);
    //declareFunction(myName, "smatrix_scalar_product", "SMATRIX-SCALAR-PRODUCT", 2, 0, false);
    //declareFunction(myName, "smatrix_vector_product", "SMATRIX-VECTOR-PRODUCT", 2, 0, false);
    //declareFunction(myName, "smatrix_matrix_product", "SMATRIX-MATRIX-PRODUCT", 2, 0, false);
    //declareFunction(myName, "smatrix_transpose", "SMATRIX-TRANSPOSE", 1, 0, false);
    //declareFunction(myName, "smatrix_inverse", "SMATRIX-INVERSE", 1, 0, false);
    //declareFunction(myName, "smatrix_sum_norm", "SMATRIX-SUM-NORM", 1, 0, false);
    //declareFunction(myName, "smatrix_max_norm", "SMATRIX-MAX-NORM", 1, 0, false);
    //declareFunction(myName, "smatrix_eigenpairs", "SMATRIX-EIGENPAIRS", 1, 2, false);
    //declareFunction(myName, "smatrix_svd", "SMATRIX-SVD", 1, 0, false);
    //declareMacro(myName, "do_smatrix", "DO-SMATRIX");
    //declareFunction(myName, "smatrix_dominant_eigenpair", "SMATRIX-DOMINANT-EIGENPAIR", 1, 1, false);
    //declareFunction(myName, "svector_star", "SVECTOR-STAR", 2, 0, false);
    //declareFunction(myName, "smatrix_deflation", "SMATRIX-DEFLATION", 3, 0, false);
    //declareFunction(myName, "sum_quotient", "SUM-QUOTIENT", 2, 0, false);
    //declareFunction(myName, "raleigh_quotient", "RALEIGH-QUOTIENT", 2, 0, false);
    //declareFunction(myName, "smatrix_nonzero_columns", "SMATRIX-NONZERO-COLUMNS", 1, 0, false);
    //declareFunction(myName, "valid_matrix_row_p", "VALID-MATRIX-ROW-P", 1, 0, false);
    //declareFunction(myName, "valid_matrix_column_p", "VALID-MATRIX-COLUMN-P", 1, 0, false);
    //declareFunction(myName, "smatrix_clear_columns", "SMATRIX-CLEAR-COLUMNS", 1, 0, false);
    //declareFunction(myName, "smatrix_compute_columns", "SMATRIX-COMPUTE-COLUMNS", 1, 0, false);
    //declareFunction(myName, "smatrix_get_column", "SMATRIX-GET-COLUMN", 2, 0, false);
    //declareFunction(myName, "smatrix_ensure_columns", "SMATRIX-ENSURE-COLUMNS", 1, 0, false);
    //declareFunction(myName, "smatrix_reduce", "SMATRIX-REDUCE", 2, 0, false);
    return NIL;
  }

  public static final SubLObject init_sparse_matrix_file() {
    $dtp_sparse_matrix$ = defconstant("*DTP-SPARSE-MATRIX*", $sym0$SPARSE_MATRIX);
    return NIL;
  }

  public static final SubLObject setup_sparse_matrix_file() {
    // CVS_ID("Id: sparse-matrix.lisp 126640 2008-12-04 13:39:36Z builder ");
    Structures.register_method(print_high.$print_object_method_table$.getGlobalValue(), $dtp_sparse_matrix$.getGlobalValue(), Symbols.symbol_function($sym7$SPARSE_MATRIX_PRINT_FUNCTION_TRAMPOLINE));
    Structures.def_csetf($sym8$SMATRIX_ROWS, $sym9$_CSETF_SMATRIX_ROWS);
    Structures.def_csetf($sym10$SMATRIX_COLUMNS, $sym11$_CSETF_SMATRIX_COLUMNS);
    Equality.identity($sym0$SPARSE_MATRIX);
    return NIL;
  }

  //// Internal Constants

  public static final SubLSymbol $sym0$SPARSE_MATRIX = makeSymbol("SPARSE-MATRIX");
  public static final SubLSymbol $sym1$SPARSE_MATRIX_P = makeSymbol("SPARSE-MATRIX-P");
  public static final SubLList $list2 = list(makeSymbol("ROWS"), makeSymbol("COLUMNS"));
  public static final SubLList $list3 = list(makeKeyword("ROWS"), makeKeyword("COLUMNS"));
  public static final SubLList $list4 = list(makeSymbol("SMATRIX-ROWS"), makeSymbol("SMATRIX-COLUMNS"));
  public static final SubLList $list5 = list(makeSymbol("_CSETF-SMATRIX-ROWS"), makeSymbol("_CSETF-SMATRIX-COLUMNS"));
  public static final SubLSymbol $sym6$SMATRIX_PRINT = makeSymbol("SMATRIX-PRINT");
  public static final SubLSymbol $sym7$SPARSE_MATRIX_PRINT_FUNCTION_TRAMPOLINE = makeSymbol("SPARSE-MATRIX-PRINT-FUNCTION-TRAMPOLINE");
  public static final SubLSymbol $sym8$SMATRIX_ROWS = makeSymbol("SMATRIX-ROWS");
  public static final SubLSymbol $sym9$_CSETF_SMATRIX_ROWS = makeSymbol("_CSETF-SMATRIX-ROWS");
  public static final SubLSymbol $sym10$SMATRIX_COLUMNS = makeSymbol("SMATRIX-COLUMNS");
  public static final SubLSymbol $sym11$_CSETF_SMATRIX_COLUMNS = makeSymbol("_CSETF-SMATRIX-COLUMNS");
  public static final SubLSymbol $kw12$ROWS = makeKeyword("ROWS");
  public static final SubLSymbol $kw13$COLUMNS = makeKeyword("COLUMNS");
  public static final SubLString $str14$Invalid_slot__S_for_construction_ = makeString("Invalid slot ~S for construction function");
  public static final SubLInteger $int15$100 = makeInteger(100);
  public static final SubLSymbol $sym16$POSITIVE_INTEGER_P = makeSymbol("POSITIVE-INTEGER-P");
  public static final SubLSymbol $sym17$VALID_HASH_TEST_P = makeSymbol("VALID-HASH-TEST-P");
  public static final SubLString $str18$__SPARSE_MATRIX_ = makeString("#<SPARSE-MATRIX ");
  public static final SubLString $str19$_ = makeString("|");
  public static final SubLString $str20$_ = makeString(">");
  public static final SubLSymbol $sym21$VALID_MATRIX_ROW_P = makeSymbol("VALID-MATRIX-ROW-P");
  public static final SubLSymbol $sym22$SPARSE_VECTOR_P = makeSymbol("SPARSE-VECTOR-P");
  public static final SubLString $str23$non_equal_matrix_and_vector_test_ = makeString("non-equal matrix and vector test functions");
  public static final SubLSymbol $sym24$VALID_MATRIX_COLUMN_P = makeSymbol("VALID-MATRIX-COLUMN-P");
  public static final SubLSymbol $sym25$NUMBERP = makeSymbol("NUMBERP");
  public static final SubLFloat $float26$0_01 = makeDouble(0.01);
  public static final SubLSymbol $sym27$NON_NEGATIVE_INTEGER_P = makeSymbol("NON-NEGATIVE-INTEGER-P");
  public static final SubLSymbol $sym28$FLOATP = makeSymbol("FLOATP");
  public static final SubLList $list29 = list(list(makeSymbol("ROW"), makeSymbol("VALUE"), makeSymbol("MATRIX")), makeSymbol("&BODY"), makeSymbol("BODY"));
  public static final SubLSymbol $sym30$CDOHASH = makeSymbol("CDOHASH");
  public static final SubLFloat $float31$9_999999999999999e_25 = makeDouble(9.999999999999999e-25);
  public static final SubLFloat $float32$3_4028232e_38 = makeDouble(3.4028232e+38);
  public static final SubLInteger $int33$50 = makeInteger(50);

  //// Initializers

  public void declareFunctions() {
    declare_sparse_matrix_file();
  }

  public void initializeVariables() {
    init_sparse_matrix_file();
  }

  public void runTopLevelForms() {
    setup_sparse_matrix_file();
  }

}
