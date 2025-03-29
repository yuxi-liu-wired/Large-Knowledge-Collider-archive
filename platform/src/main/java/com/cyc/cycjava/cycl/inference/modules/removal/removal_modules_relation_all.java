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

package  com.cyc.cycjava.cycl.inference.modules.removal;

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
import com.cyc.cycjava.cycl.arguments;
import com.cyc.cycjava.cycl.assertions_high;
import com.cyc.cycjava.cycl.backward;
import com.cyc.cycjava.cycl.backward_utilities;
import com.cyc.cycjava.cycl.constant_handles;
import com.cyc.cycjava.cycl.control_vars;
import com.cyc.cycjava.cycl.cycl_utilities;
import com.cyc.cycjava.cycl.deck;
import com.cyc.cycjava.cycl.dictionary;
import com.cyc.cycjava.cycl.dictionary_contents;
import com.cyc.cycjava.cycl.forts;
import com.cyc.cycjava.cycl.function_terms;
import com.cyc.cycjava.cycl.hl_supports;
import com.cyc.cycjava.cycl.inference.harness.inference_modules;
import com.cyc.cycjava.cycl.isa;
import com.cyc.cycjava.cycl.iteration;
import com.cyc.cycjava.cycl.kb_indexing;
import com.cyc.cycjava.cycl.kb_mapping_macros;
import com.cyc.cycjava.cycl.kb_mapping_utilities;
import com.cyc.cycjava.cycl.list_utilities;
import com.cyc.cycjava.cycl.mt_relevance_macros;
import com.cyc.cycjava.cycl.mt_vars;
import com.cyc.cycjava.cycl.obsolete;
import com.cyc.cycjava.cycl.sbhl.sbhl_graphs;
import com.cyc.cycjava.cycl.sbhl.sbhl_link_vars;
import com.cyc.cycjava.cycl.sbhl.sbhl_links;
import com.cyc.cycjava.cycl.sbhl.sbhl_macros;
import com.cyc.cycjava.cycl.sbhl.sbhl_marking_utilities;
import com.cyc.cycjava.cycl.sbhl.sbhl_marking_vars;
import com.cyc.cycjava.cycl.sbhl.sbhl_module_utilities;
import com.cyc.cycjava.cycl.sbhl.sbhl_module_vars;
import com.cyc.cycjava.cycl.sbhl.sbhl_paranoia;
import com.cyc.cycjava.cycl.sbhl.sbhl_search_vars;
import com.cyc.cycjava.cycl.subl_macro_promotions;
import com.cyc.cycjava.cycl.subl_macros;

public  final class removal_modules_relation_all extends SubLTranslatedFile {

  //// Constructor

  private removal_modules_relation_all() {}
  public static final SubLFile me = new removal_modules_relation_all();
  public static final String myName = "com.cyc.cycjava.cycl.inference.modules.removal.removal_modules_relation_all";

  //// Definitions

  @SubL(source = "cycl/inference/modules/removal/removal-modules-relation-all.lisp", position = 641) 
  public static final SubLObject removal_some_relation_all_for_predicate(SubLObject predicate, SubLObject mt) {
    if ((mt == UNPROVIDED)) {
      mt = NIL;
    }
    if ((NIL != forts.fort_p(predicate))) {
      return kb_mapping_utilities.some_pred_value_in_relevant_mts(predicate, $const0$relationAll, mt, ONE_INTEGER, UNPROVIDED);
    }
    return NIL;
  }

  @SubL(source = "cycl/inference/modules/removal/removal-modules-relation-all.lisp", position = 1380) 
  private static SubLSymbol $estimated_per_collection_relation_all_fraction$ = null;

  @SubL(source = "cycl/inference/modules/removal/removal-modules-relation-all.lisp", position = 1609) 
  public static final SubLObject removal_relation_all_required(SubLObject asent) {
    {
      SubLObject predicate = cycl_utilities.atomic_sentence_predicate(asent);
      return makeBoolean(((NIL == hl_supports.hl_predicate_p(predicate))
             && (NIL != removal_some_relation_all_for_predicate(predicate, NIL))));
    }
  }

  @SubL(source = "cycl/inference/modules/removal/removal-modules-relation-all.lisp", position = 1836) 
  private static SubLSymbol $relation_all_rule$ = null;

  @SubL(source = "cycl/inference/modules/removal/removal-modules-relation-all.lisp", position = 1991) 
  public static SubLSymbol $relation_all_defining_mt$ = null;

  @SubL(source = "cycl/inference/modules/removal/removal-modules-relation-all.lisp", position = 2197) 
  public static final SubLObject removal_relation_all_check_required(SubLObject asent, SubLObject sense) {
    if ((sense == UNPROVIDED)) {
      sense = NIL;
    }
    return removal_relation_all_required(asent);
  }

  @SubL(source = "cycl/inference/modules/removal/removal-modules-relation-all.lisp", position = 2585) 
  private static SubLSymbol $removal_relation_all_check_cost$ = null;

  public static final SubLObject declare_removal_modules_relation_all_file() {
    declareFunction(myName, "removal_some_relation_all_for_predicate", "REMOVAL-SOME-RELATION-ALL-FOR-PREDICATE", 1, 1, false);
    declareFunction(myName, "removal_some_relation_all_for_collection", "REMOVAL-SOME-RELATION-ALL-FOR-COLLECTION", 1, 1, false);
    declareFunction(myName, "relation_all_predicate_cost_estimate", "RELATION-ALL-PREDICATE-COST-ESTIMATE", 1, 0, false);
    declareFunction(myName, "relation_all_collection_cost_estimate", "RELATION-ALL-COLLECTION-COST-ESTIMATE", 0, 0, false);
    declareFunction(myName, "removal_relation_all_required", "REMOVAL-RELATION-ALL-REQUIRED", 1, 0, false);
    declareFunction(myName, "make_relation_all_support", "MAKE-RELATION-ALL-SUPPORT", 0, 0, false);
    declareFunction(myName, "removal_relation_all_check_required", "REMOVAL-RELATION-ALL-CHECK-REQUIRED", 1, 1, false);
    declareFunction(myName, "removal_relation_all_check_expand", "REMOVAL-RELATION-ALL-CHECK-EXPAND", 1, 1, false);
    declareFunction(myName, "removal_relation_all_check_via_collection_expand", "REMOVAL-RELATION-ALL-CHECK-VIA-COLLECTION-EXPAND", 2, 0, false);
    declareFunction(myName, "removal_relation_all_check_via_predicate_expand", "REMOVAL-RELATION-ALL-CHECK-VIA-PREDICATE-EXPAND", 2, 0, false);
    declareFunction(myName, "removal_relation_all_check_expand_guts", "REMOVAL-RELATION-ALL-CHECK-EXPAND-GUTS", 2, 0, false);
    declareFunction(myName, "unary_pred_holds", "UNARY-PRED-HOLDS", 2, 1, false);
    declareFunction(myName, "unary_pred_holds_via_relation_all", "UNARY-PRED-HOLDS-VIA-RELATION-ALL", 2, 1, false);
    return NIL;
  }

  public static final SubLObject init_removal_modules_relation_all_file() {
    $estimated_per_collection_relation_all_fraction$ = defparameter("*ESTIMATED-PER-COLLECTION-RELATION-ALL-FRACTION*", TEN_INTEGER);
    $relation_all_rule$ = deflexical("*RELATION-ALL-RULE*", $list1);
    $relation_all_defining_mt$ = deflexical("*RELATION-ALL-DEFINING-MT*", ((NIL != Symbols.boundp($sym2$_RELATION_ALL_DEFINING_MT_)) ? ((SubLObject) $relation_all_defining_mt$.getGlobalValue()) : $const3$BaseKB));
    $removal_relation_all_check_cost$ = defparameter("*REMOVAL-RELATION-ALL-CHECK-COST*", $float6$1_5);
    return NIL;
  }

  public static final SubLObject setup_removal_modules_relation_all_file() {
    // CVS_ID("Id: removal-modules-relation-all.lisp 126640 2008-12-04 13:39:36Z builder ");
    subl_macro_promotions.declare_defglobal($sym2$_RELATION_ALL_DEFINING_MT_);
    mt_vars.note_mt_var($sym2$_RELATION_ALL_DEFINING_MT_, $const0$relationAll);
    inference_modules.inference_removal_module($kw9$REMOVAL_RELATION_ALL_CHECK, $list10);
    return NIL;
  }

  //// Internal Constants

  public static final SubLObject $const0$relationAll = constant_handles.reader_make_constant_shell(makeString("relationAll"));
  public static final SubLList $list1 = list(constant_handles.reader_make_constant_shell(makeString("implies")), list(constant_handles.reader_make_constant_shell(makeString("and")), list(constant_handles.reader_make_constant_shell(makeString("relationAll")), makeSymbol("?PRED"), makeSymbol("?COL")), list(constant_handles.reader_make_constant_shell(makeString("isa")), makeSymbol("?OBJ"), makeSymbol("?COL"))), list(makeSymbol("?PRED"), makeSymbol("?OBJ")));
  public static final SubLSymbol $sym2$_RELATION_ALL_DEFINING_MT_ = makeSymbol("*RELATION-ALL-DEFINING-MT*");
  public static final SubLObject $const3$BaseKB = constant_handles.reader_make_constant_shell(makeString("BaseKB"));
  public static final SubLSymbol $kw4$CODE = makeKeyword("CODE");
  public static final SubLSymbol $kw5$TRUE_MON = makeKeyword("TRUE-MON");
  public static final SubLFloat $float6$1_5 = makeDouble(1.5);
  public static final SubLSymbol $kw7$GAF = makeKeyword("GAF");
  public static final SubLSymbol $kw8$TRUE = makeKeyword("TRUE");
  public static final SubLSymbol $kw9$REMOVAL_RELATION_ALL_CHECK = makeKeyword("REMOVAL-RELATION-ALL-CHECK");
  public static final SubLList $list10 = list(new SubLObject[] {makeKeyword("SENSE"), makeKeyword("POS"), makeKeyword("ARITY"), ONE_INTEGER, makeKeyword("REQUIRED-PATTERN"), list(makeKeyword("FORT"), makeKeyword("FORT")), makeKeyword("REQUIRED"), makeSymbol("REMOVAL-RELATION-ALL-CHECK-REQUIRED"), makeKeyword("COST-EXPRESSION"), makeSymbol("*REMOVAL-RELATION-ALL-CHECK-COST*"), makeKeyword("EXPAND"), makeSymbol("REMOVAL-RELATION-ALL-CHECK-EXPAND"), makeKeyword("DOCUMENTATION"), makeString("(<predicate> <object>) where <object> is a FORT\nfrom (#$relationAll <predicate> <collection>) \nand (#$isa <object> <collection>)"), makeKeyword("EXAMPLE"), makeString("(#$temporallyContinuous #$AbrahamLincoln)\nfrom (#$relationAll #$temporallyContinuous #$Entity)\nand (#$isa #$AbrahamLincoln #$Entity)")});
  public static final SubLSymbol $kw11$ISA = makeKeyword("ISA");
  public static final SubLObject $const12$isa = constant_handles.reader_make_constant_shell(makeString("isa"));
  public static final SubLSymbol $sym13$FORT_P = makeSymbol("FORT-P");
  public static final SubLSymbol $kw14$BREADTH = makeKeyword("BREADTH");
  public static final SubLSymbol $kw15$DEPTH = makeKeyword("DEPTH");
  public static final SubLSymbol $kw16$STACK = makeKeyword("STACK");
  public static final SubLSymbol $kw17$QUEUE = makeKeyword("QUEUE");
  public static final SubLSymbol $sym18$RELEVANT_SBHL_TV_IS_GENERAL_TV = makeSymbol("RELEVANT-SBHL-TV-IS-GENERAL-TV");
  public static final SubLSymbol $kw19$ERROR = makeKeyword("ERROR");
  public static final SubLString $str20$_A_is_not_a__A = makeString("~A is not a ~A");
  public static final SubLSymbol $sym21$SBHL_TRUE_TV_P = makeSymbol("SBHL-TRUE-TV-P");
  public static final SubLSymbol $kw22$CERROR = makeKeyword("CERROR");
  public static final SubLString $str23$continue_anyway = makeString("continue anyway");
  public static final SubLSymbol $kw24$WARN = makeKeyword("WARN");
  public static final SubLString $str25$_A_is_not_a_valid__sbhl_type_erro = makeString("~A is not a valid *sbhl-type-error-action* value");
  public static final SubLString $str26$attempting_to_bind_direction_link = makeString("attempting to bind direction link variable, to NIL. macro body not executed.");
  public static final SubLString $str27$Node__a_does_not_pass_sbhl_type_t = makeString("Node ~a does not pass sbhl-type-test ~a~%");

  //// Initializers

  public void declareFunctions() {
    declare_removal_modules_relation_all_file();
  }

  public void initializeVariables() {
    init_removal_modules_relation_all_file();
  }

  public void runTopLevelForms() {
    setup_removal_modules_relation_all_file();
  }

}
