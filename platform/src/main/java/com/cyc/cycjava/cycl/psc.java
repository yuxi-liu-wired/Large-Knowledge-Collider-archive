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
import com.cyc.cycjava.cycl.el_utilities;
import com.cyc.cycjava.cycl.hlmt;
import com.cyc.cycjava.cycl.isa;
import com.cyc.cycjava.cycl.mt_vars;

public  final class psc extends SubLTranslatedFile {

  //// Constructor

  private psc() {}
  public static final SubLFile me = new psc();
  public static final String myName = "com.cyc.cycjava.cycl.psc";

  //// Definitions

  @SubL(source = "cycl/psc.lisp", position = 879) 
  public static final SubLObject mt_inference_function(SubLObject mt) {
    if ((mt == $const0$EverythingPSC)) {
      return $sym1$ALL_MTS_INFERENCE;
    } else if ((mt == $const2$InferencePSC)) {
      return $sym3$PSC_INFERENCE;
    } else if ((NIL == el_utilities.possibly_naut_p(mt))) {
      return $sym4$NORMAL_INFERENCE;
    } else if ((NIL != hlmt.mt_union_naut_p(mt))) {
      return $sym5$MT_UNION_INFERENCE;
    } else if ((NIL != hlmt.hlmt_with_anytime_psc_p(mt))) {
      return $sym6$ANYTIME_PSC_INFERENCE;
    } else {
      return $sym4$NORMAL_INFERENCE;
    }
  }

  public static final SubLObject declare_psc_file() {
    declareFunction(myName, "mt_inference_function", "MT-INFERENCE-FUNCTION", 1, 0, false);
    declareFunction(myName, "pscP", "PSC?", 1, 0, false);
    return NIL;
  }

  public static final SubLObject init_psc_file() {
    return NIL;
  }

  public static final SubLObject setup_psc_file() {
    // CVS_ID("Id: psc.lisp 126640 2008-12-04 13:39:36Z builder ");
    return NIL;
  }

  //// Internal Constants

  public static final SubLObject $const0$EverythingPSC = constant_handles.reader_make_constant_shell(makeString("EverythingPSC"));
  public static final SubLSymbol $sym1$ALL_MTS_INFERENCE = makeSymbol("ALL-MTS-INFERENCE");
  public static final SubLObject $const2$InferencePSC = constant_handles.reader_make_constant_shell(makeString("InferencePSC"));
  public static final SubLSymbol $sym3$PSC_INFERENCE = makeSymbol("PSC-INFERENCE");
  public static final SubLSymbol $sym4$NORMAL_INFERENCE = makeSymbol("NORMAL-INFERENCE");
  public static final SubLSymbol $sym5$MT_UNION_INFERENCE = makeSymbol("MT-UNION-INFERENCE");
  public static final SubLSymbol $sym6$ANYTIME_PSC_INFERENCE = makeSymbol("ANYTIME-PSC-INFERENCE");
  public static final SubLObject $const7$ProblemSolvingCntxt = constant_handles.reader_make_constant_shell(makeString("ProblemSolvingCntxt"));

  //// Initializers

  public void declareFunctions() {
    declare_psc_file();
  }

  public void initializeVariables() {
    init_psc_file();
  }

  public void runTopLevelForms() {
    setup_psc_file();
  }

}
