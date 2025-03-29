/***
 *   Copyright (c) 1995-2010 Cycorp R.E.R. d.o.o
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
 */
package eu.larkc.core.pluginregistry.cyc.query;

/**
 * Default binding implementation
 * 
 * @author Blaz Fortuna
 */
public class Binding {

  String name;
  Value value;

  public Binding(String _name, Value _value) {
    name = _name;
    value = _value;
  }

  public String getName() {
    return name;
  }

  public Value getValue() {
    return value;
  }
}
