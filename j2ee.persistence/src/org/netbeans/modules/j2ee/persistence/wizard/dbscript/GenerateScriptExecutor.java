/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.j2ee.persistence.wizard.dbscript;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.persistence.api.PersistenceEnvironment;
import org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * Executes save script.
 */
public class GenerateScriptExecutor {

    public void execute(Project project, FileObject file, PersistenceEnvironment pe, PersistenceUnit pu, HashMap map, List<String> problems, ProgressHandle handle, boolean validateOnly) {
        try {

            Class pClass = Thread.currentThread().getContextClassLoader().loadClass("javax.persistence.Persistence");//NOI18N
            javax.persistence.Persistence p = (javax.persistence.Persistence) pClass.newInstance();

            //
            map.put("javax.persistence.schema-generation.scripts.action", "create");
            //map.put("javax.persistence.schema-generation-target", "scripts");
            if(!validateOnly) {
                try {
                    map.put("javax.persistence.schema-generation.scripts.create-target", new FileWriter(FileUtil.toFile(file)));
                } catch (IOException ex) {
                    problems.add( NbBundle.getMessage(GenerateScriptExecutor.class, "ERR_File", file.getPath()));
                }
                //
                handle.progress(NbBundle.getMessage(DBScriptWizard.class, "MSG_ScriptGeneration"),15);
                p.generateSchema(pu.getName(), map);
                handle.progress(95);
            }
        } catch (ClassNotFoundException ex) {
                problems.add( NbBundle.getMessage(GenerateScriptExecutor.class, "ERR_Classpath", file.getPath()));
        } catch (IllegalAccessException ex) {
                problems.add( NbBundle.getMessage(GenerateScriptExecutor.class, "ERR_Classpath", file.getPath()));
        } catch (InstantiationException ex) {
                problems.add( NbBundle.getMessage(GenerateScriptExecutor.class, "ERR_Classpath", file.getPath()));
        }

    }
}
