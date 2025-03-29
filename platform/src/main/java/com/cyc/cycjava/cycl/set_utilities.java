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
import com.cyc.cycjava.cycl.list_utilities;
import com.cyc.cycjava.cycl.number_utilities;
import com.cyc.cycjava.cycl.set;
import com.cyc.cycjava.cycl.set_contents;
import com.cyc.cycjava.cycl.utilities_macros;

public  final class set_utilities extends SubLTranslatedFile {

  //// Constructor

  private set_utilities() {}
  public static final SubLFile me = new set_utilities();
  public static final String myName = "com.cyc.cycjava.cycl.set_utilities";

  //// Definitions

  @SubL(source = "cycl/set-utilities.lisp", position = 2716) 
  public static final SubLObject set_contents_union(SubLObject set_contents_list, SubLObject test) {
    if ((test == UNPROVIDED)) {
      test = Symbols.symbol_function(EQL);
    }
    {
      SubLObject list_var = set_contents_list;
      checkType(list_var, $sym4$NON_DOTTED_LIST_P);
      {
        SubLObject cdolist_list_var = list_var;
        SubLObject elem = NIL;
        for (elem = cdolist_list_var.first(); (NIL != cdolist_list_var); cdolist_list_var = cdolist_list_var.rest(), elem = cdolist_list_var.first()) {
          checkType(elem, $sym5$SET_CONTENTS_P);
        }
      }
    }
    if ((NIL == set_contents_list)) {
      return set_contents.new_set_contents(ZERO_INTEGER, test);
    } else if ((NIL != list_utilities.singletonP(set_contents_list))) {
      return set_contents.copy_set_contents(set_contents_list.first());
    } else {
      {
        SubLObject size = number_utilities.maximum(Mapping.mapcar($sym6$SET_CONTENTS_SIZE, set_contents_list), UNPROVIDED);
        SubLObject union = set_contents.new_set_contents(size, test);
        SubLObject cdolist_list_var = set_contents_list;
        SubLObject v_set_contents = NIL;
        for (v_set_contents = cdolist_list_var.first(); (NIL != cdolist_list_var); cdolist_list_var = cdolist_list_var.rest(), v_set_contents = cdolist_list_var.first()) {
          {
            SubLObject set_contents_var = v_set_contents;
            SubLObject basis_object = set_contents.do_set_contents_basis_object(set_contents_var);
            SubLObject state = NIL;
            for (state = set_contents.do_set_contents_initial_state(basis_object, set_contents_var); (NIL == set_contents.do_set_contents_doneP(basis_object, state)); state = set_contents.do_set_contents_update_state(state)) {
              {
                SubLObject element = set_contents.do_set_contents_next(basis_object, state);
                if ((NIL != set_contents.do_set_contents_element_validP(state, element))) {
                  union = set_contents.set_contents_add(element, union, UNPROVIDED);
                }
              }
            }
          }
        }
        return union;
      }
    }
  }

  @SubL(source = "cycl/set-utilities.lisp", position = 4179) 
  public static final SubLObject set_contents_intersection(SubLObject set_contents_list, SubLObject test) {
    if ((test == UNPROVIDED)) {
      test = Symbols.symbol_function(EQL);
    }
    {
      SubLObject list_var = set_contents_list;
      checkType(list_var, $sym4$NON_DOTTED_LIST_P);
      {
        SubLObject cdolist_list_var = list_var;
        SubLObject elem = NIL;
        for (elem = cdolist_list_var.first(); (NIL != cdolist_list_var); cdolist_list_var = cdolist_list_var.rest(), elem = cdolist_list_var.first()) {
          checkType(elem, $sym5$SET_CONTENTS_P);
        }
      }
    }
    if ((NIL == set_contents_list)) {
      return set_contents.new_set_contents(ZERO_INTEGER, test);
    } else if ((NIL != list_utilities.singletonP(set_contents_list))) {
      return set_contents.copy_set_contents(set_contents_list.first());
    } else {
      {
        SubLObject intersection = set_contents.new_set_contents(ZERO_INTEGER, test);
        if ((NIL != set_contents_list)) {
          {
            SubLObject smallest = list_utilities.extremal(set_contents_list, Symbols.symbol_function($sym7$_), $sym6$SET_CONTENTS_SIZE);
            SubLObject set_contents_var = smallest;
            SubLObject basis_object = set_contents.do_set_contents_basis_object(set_contents_var);
            SubLObject state = NIL;
            for (state = set_contents.do_set_contents_initial_state(basis_object, set_contents_var); (NIL == set_contents.do_set_contents_doneP(basis_object, state)); state = set_contents.do_set_contents_update_state(state)) {
              {
                SubLObject element = set_contents.do_set_contents_next(basis_object, state);
                if ((NIL != set_contents.do_set_contents_element_validP(state, element))) {
                  {
                    SubLObject not_in_intersectionP = NIL;
                    if ((NIL == not_in_intersectionP)) {
                      {
                        SubLObject csome_list_var = set_contents_list;
                        SubLObject v_set_contents = NIL;
                        for (v_set_contents = csome_list_var.first(); (!(((NIL != not_in_intersectionP)
                              || (NIL == csome_list_var)))); csome_list_var = csome_list_var.rest(), v_set_contents = csome_list_var.first()) {
                          if ((!(((v_set_contents == smallest)
                                || (NIL != set_contents.set_contents_memberP(element, v_set_contents, test)))))) {
                            not_in_intersectionP = T;
                          }
                        }
                      }
                    }
                    if ((NIL == not_in_intersectionP)) {
                      intersection = set_contents.set_contents_add(element, intersection, test);
                    }
                  }
                }
              }
            }
          }
        }
        return intersection;
      }
    }
  }

  /** Returns a set from the objects in LIST. */
  @SubL(source = "cycl/set-utilities.lisp", position = 10322) 
  public static final SubLObject construct_set_from_list(SubLObject list, SubLObject test, SubLObject size) {
    if ((test == UNPROVIDED)) {
      test = Symbols.symbol_function(EQL);
    }
    if ((size == UNPROVIDED)) {
      size = Sequences.length(list);
    }
    {
      SubLObject v_set = set.new_set(test, size);
      SubLObject cdolist_list_var = list;
      SubLObject elem = NIL;
      for (elem = cdolist_list_var.first(); (NIL != cdolist_list_var); cdolist_list_var = cdolist_list_var.rest(), elem = cdolist_list_var.first()) {
        set.set_add(elem, v_set);
      }
      return v_set;
    }
  }

  /** Returns a set-contents object constructed from the objects in LIST. */
  @SubL(source = "cycl/set-utilities.lisp", position = 10560) 
  public static final SubLObject construct_set_contents_from_list(SubLObject list, SubLObject test, SubLObject size) {
    if ((test == UNPROVIDED)) {
      test = Symbols.symbol_function(EQL);
    }
    if ((size == UNPROVIDED)) {
      size = Sequences.length(list);
    }
    {
      SubLObject v_set_contents = set_contents.new_set_contents(size, test);
      SubLObject cdolist_list_var = list;
      SubLObject elem = NIL;
      for (elem = cdolist_list_var.first(); (NIL != cdolist_list_var); cdolist_list_var = cdolist_list_var.rest(), elem = cdolist_list_var.first()) {
        v_set_contents = set_contents.set_contents_add(elem, v_set_contents, test);
      }
      return v_set_contents;
    }
  }

  @SubL(source = "cycl/set-utilities.lisp", position = 12169) 
  public static final SubLObject set_add_all(SubLObject elements, SubLObject v_set) {
    {
      SubLObject cdolist_list_var = elements;
      SubLObject element = NIL;
      for (element = cdolist_list_var.first(); (NIL != cdolist_list_var); cdolist_list_var = cdolist_list_var.rest(), element = cdolist_list_var.first()) {
        set.set_add(element, v_set);
      }
    }
    return v_set;
  }

  public static final SubLObject declare_set_utilities_file() {
    //declareFunction(myName, "set_arbitrary_element", "SET-ARBITRARY-ELEMENT", 1, 0, false);
    //declareFunction(myName, "set_arbitrary_element_without_values", "SET-ARBITRARY-ELEMENT-WITHOUT-VALUES", 1, 1, false);
    //declareFunction(myName, "set_contents_arbitrary_element", "SET-CONTENTS-ARBITRARY-ELEMENT", 1, 0, false);
    //declareFunction(myName, "set_contents_arbitrary_element_without_values", "SET-CONTENTS-ARBITRARY-ELEMENT-WITHOUT-VALUES", 1, 1, false);
    //declareFunction(myName, "set_remove_random_element", "SET-REMOVE-RANDOM-ELEMENT", 1, 0, false);
    //declareFunction(myName, "set_union", "SET-UNION", 1, 1, false);
    declareFunction(myName, "set_contents_union", "SET-CONTENTS-UNION", 1, 1, false);
    //declareFunction(myName, "set_intersection", "SET-INTERSECTION", 1, 1, false);
    declareFunction(myName, "set_contents_intersection", "SET-CONTENTS-INTERSECTION", 1, 1, false);
    //declareFunction(myName, "sets_intersectP", "SETS-INTERSECT?", 1, 0, false);
    //declareFunction(myName, "set_intersection_fraction", "SET-INTERSECTION-FRACTION", 2, 0, false);
    //declareFunction(myName, "set_minus", "SET-MINUS", 2, 1, false);
    //declareFunction(myName, "set_nprune", "SET-NPRUNE", 2, 0, false);
    //declareFunction(myName, "set_prune", "SET-PRUNE", 2, 0, false);
    //declareFunction(myName, "sets_coextensionalP", "SETS-COEXTENSIONAL?", 2, 0, false);
    //declareFunction(myName, "default_test_function_for_sets", "DEFAULT-TEST-FUNCTION-FOR-SETS", 1, 0, false);
    //declareFunction(myName, "set_subsetP", "SET-SUBSET?", 2, 0, false);
    //declareFunction(myName, "set_subset_anyP", "SET-SUBSET-ANY?", 2, 0, false);
    //declareFunction(myName, "set_find", "SET-FIND", 2, 2, false);
    //declareFunction(myName, "set_find_if", "SET-FIND-IF", 2, 1, false);
    //declareFunction(myName, "set_find_if_not", "SET-FIND-IF-NOT", 2, 1, false);
    //declareFunction(myName, "set_remove_if", "SET-REMOVE-IF", 2, 1, false);
    //declareFunction(myName, "set_remove_if_not", "SET-REMOVE-IF-NOT", 2, 1, false);
    declareFunction(myName, "construct_set_from_list", "CONSTRUCT-SET-FROM-LIST", 1, 2, false);
    declareFunction(myName, "construct_set_contents_from_list", "CONSTRUCT-SET-CONTENTS-FROM-LIST", 1, 2, false);
    //declareFunction(myName, "new_singleton_set", "NEW-SINGLETON-SET", 1, 1, false);
    //declareFunction(myName, "make_set_from_list", "MAKE-SET-FROM-LIST", 1, 1, false);
    //declareFunction(myName, "any_in_set", "ANY-IN-SET", 2, 1, false);
    //declareFunction(myName, "every_in_set", "EVERY-IN-SET", 2, 1, false);
    //declareFunction(myName, "print_set_contents", "PRINT-SET-CONTENTS", 1, 1, false);
    declareFunction(myName, "set_add_all", "SET-ADD-ALL", 2, 0, false);
    //declareFunction(myName, "set_remove_all", "SET-REMOVE-ALL", 2, 0, false);
    //declareFunction(myName, "set_nmerge", "SET-NMERGE", 2, 0, false);
    //declareMacro(myName, "do_set_numbered", "DO-SET-NUMBERED");
    return NIL;
  }

  public static final SubLObject init_set_utilities_file() {
    return NIL;
  }

  public static final SubLObject setup_set_utilities_file() {
    // CVS_ID("Id: set-utilities.lisp 127562 2009-04-08 21:47:09Z daves ");
    access_macros.define_obsolete_register($sym9$MAKE_SET_FROM_LIST, $list10);
    return NIL;
  }

  //// Internal Constants

  public static final SubLSymbol $sym0$SET_P = makeSymbol("SET-P");
  public static final SubLSymbol $sym1$LISTP = makeSymbol("LISTP");
  public static final SubLSymbol $sym2$VALID_HASH_TEST_P = makeSymbol("VALID-HASH-TEST-P");
  public static final SubLSymbol $sym3$SET_SIZE = makeSymbol("SET-SIZE");
  public static final SubLSymbol $sym4$NON_DOTTED_LIST_P = makeSymbol("NON-DOTTED-LIST-P");
  public static final SubLSymbol $sym5$SET_CONTENTS_P = makeSymbol("SET-CONTENTS-P");
  public static final SubLSymbol $sym6$SET_CONTENTS_SIZE = makeSymbol("SET-CONTENTS-SIZE");
  public static final SubLSymbol $sym7$_ = makeSymbol("<");
  public static final SubLSymbol $sym8$SET_TEST = makeSymbol("SET-TEST");
  public static final SubLSymbol $sym9$MAKE_SET_FROM_LIST = makeSymbol("MAKE-SET-FROM-LIST");
  public static final SubLList $list10 = list(makeSymbol("CONSTRUCT-SET-FROM-LIST"));
  public static final SubLString $str11$_A__ = makeString("~A~%");
  public static final SubLList $list12 = list(list(makeSymbol("ELEM"), makeSymbol("COUNTER"), makeSymbol("SET"), makeSymbol("&KEY"), makeSymbol("DONE")), makeSymbol("&BODY"), makeSymbol("BODY"));
  public static final SubLList $list13 = list(makeKeyword("DONE"));
  public static final SubLSymbol $kw14$ALLOW_OTHER_KEYS = makeKeyword("ALLOW-OTHER-KEYS");
  public static final SubLSymbol $kw15$DONE = makeKeyword("DONE");
  public static final SubLSymbol $sym16$CLET = makeSymbol("CLET");
  public static final SubLList $list17 = list(ZERO_INTEGER);
  public static final SubLSymbol $sym18$DO_SET = makeSymbol("DO-SET");
  public static final SubLSymbol $sym19$CINC = makeSymbol("CINC");

  //// Initializers

  public void declareFunctions() {
    declare_set_utilities_file();
  }

  public void initializeVariables() {
    init_set_utilities_file();
  }

  public void runTopLevelForms() {
    setup_set_utilities_file();
  }

}
