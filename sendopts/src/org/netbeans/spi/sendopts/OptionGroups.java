/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.spi.sendopts;

/** Factory method that composes individual options into groups.
 * In some situations it is useful and sometimes even necessary to
 * enforce certain relationships among options provided by some, 
 * potentially different modules. Factory methods defined here in allow such
 * compositions as they take multiple options and convert them
 * into one group option.
 * <p>
 * The following table gives short overview of the behaviour of different
 * factory methods. Supposed <b>n</b> individual options were passed as
 * arguments to each method. The <em>Min</em> and <em>Max</em> shown the
 * number that is needed for the group option to be "consistent":
 * 
 * <table border=1>
 *   <theader>
 *     <td>Method</td>
 *     <td>Min</td>
 *     <td>Max</td>
 *   </theader>
 * 
 *   <tr>
 *     <td>{@link OptionGroups#allOf}</td>
 *     <td>n</td>
 *     <td>n</td>
 *   </tr>
 * 
 *   <tr>
 *     <td>{@link OptionGroups#oneOf}</td>
 *     <td>1</td>
 *     <td>1</td>
 *   </tr>
 * 
 *   <tr>
 *     <td>{@link OptionGroups#someOf}</td>
 *     <td>1</td>
 *     <td>n</td>
 *   </tr>
 *
 *   <tr>
 *     <td>{@link OptionGroups#anyOf}</td>
 *     <td>0</td>
 *     <td>n</td>
 *   </tr>
 * </table>
 * 
 * <p>
 * Please note that the {@link OptionGroups#anyOf} is in fact always consistent,
 * regardless any number of the given options appears on the command line or not.
 * As such it serves slightly different purpose than the other methods listed
 * in the table.
 * <p>
 * More detailed description of the behaviour of each method is given in
 * their appropriate javadoc.
 *
 * @author Jaroslav Tulach
 */
public final class OptionGroups {
    private OptionGroups() {
    }
    
    /** Combines a set of multiple options into one compound one. Useful
     * for {@link OptionProcessor} that want to process more than one 
     * option and want to do all the logic of choosing which options can
     * be used together with others by themselves. The newly created
     * options will be contained in the map passed to 
     * {@link OptionProcessor#process} method if at least one of the
     * parameters appeared on the command line. The value associated with
     * the compound option is always going to be <code>new String[0]</code>, however
     * the map will contain all options that appeared and their values which
     * can then be processed one by one.
     * 
     * @param options the sub options to check on the command line
     * @return compound option that <q>is activated</q> if at least one of the
     *    options appears on the command line
     */
    public static Option someOf(Option... options) {
        return new Option(2, options);
    }
    
    /** A voluntary selector that allows any of the specified options to appear
     * on the command line, but if they do not appear, the command line is 
     * still consistent. If at least one of the given sub options appears on
     * the command line, the map passed to 
     * {@link OptionProcessor#process} will contain the returned compound option
     * with associated value of <code>new String[0]</code>.
     * 
     * @param options the sub options to check on the command line
     * @return compound option that <q>is activated</q> if at least one of the
     *    options appears on the command line
     */
    public static Option anyOf(Option... options) {
        return new Option(3, options);
    }

    /** Creates a selector option that forces exactly one of the sub options
     * to appear on the command line. If more than
     * one of the options appears on the command line, then an error is 
     * reported by the infrastructure. If none of the sub options is present
     * than this selector option is not also present. 
     * If present in the {@link OptionProcessor#process}'s
     * map, then the selector option is always associated with <code>new String[0]</code>
     * value, however there is also one of the sub options, with its associated
     * value.
     * 
     * @param options the options to select one from
     * @return the selector option
     */
    public static Option oneOf(Option... options) {
        return new Option(0, options);
    }
    
    /** Creates a compound option that forces all of the sub options
     * to appear on the command line. If less of them is present, an error
     * is reported by the infrastructure. 
     * If present in the {@link OptionProcessor#process}'s
     * map, then the compound option is always associated with <code>new String[0]</code>
     * value, however there are all the sub options, with their associated
     * values.
     * 
     * @param options the options to select one from
     * @return the selector option
     */
    public static Option allOf(Option... options) {
        return new Option(1, options);
    }
}
