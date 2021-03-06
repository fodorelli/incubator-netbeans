/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.localtasks;

import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.mylyn.tasks.core.TaskMapping;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.localtasks.task.LocalTask;
import org.netbeans.modules.bugtracking.spi.BugtrackingSupport;
import org.netbeans.modules.mylyn.util.MylynSupport;
import org.netbeans.modules.mylyn.util.NbTask;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Ondrej Vrabec
 */
public final class LocalRepository {
    private static final String ID = "LocalRepositoryInstance"; //NOI18N
    private static LocalRepository instance;
    private final Repository repository;
    private final BugtrackingSupport<LocalRepository, LocalQuery, LocalTask> fac;
    private final PropertyChangeSupport propertySuport;
    private static final String ICON_PATH = "org/netbeans/modules/localtasks/resources/local_repo.png"; // NOI18N
    private final Image icon;
    private TaskRepository taskRepository;
    private final Object CACHE_LOCK = new Object();
    private Cache cache;
    private boolean initialized;
    public static final Logger LOG = Logger.getLogger(LocalRepository.class.getName());
    private static final RequestProcessor RP = new RequestProcessor("Local Task Repository"); //NOI18N

    public static synchronized  LocalRepository getInstance () {
        if (instance == null) {
            instance = new LocalRepository();
        }
        return instance;
    }

    public LocalRepository () {
        fac = new BugtrackingSupport<>(new RepositoryProviderImpl(), new QueryProviderImpl(), new IssueProviderImpl());
        icon = ImageUtilities.loadImage(ICON_PATH, true);
        propertySuport = new PropertyChangeSupport(this);
        repository = fac.createRepository(this, new IssueStatusProviderImpl(), 
                new IssueSchedulingProviderImpl(), null, null);
    }

    public Repository getRepository () {
        return repository;
    }

    void addPropertyChangeListener (PropertyChangeListener listener) {
        propertySuport.addPropertyChangeListener(listener);
    }

    void removePropertyChangeListener (PropertyChangeListener listener) {
        propertySuport.removePropertyChangeListener(listener);
    }

    Image getIcon () {
        return icon;
    }
    
    String getID () {
        return ID;
    }
    
    String getDisplayName () {
        return getTaskRepository().getRepositoryLabel();
    }
    
    @NbBundle.Messages({
        "# {0} - repository URL", "CTL_LocalRepository.tooltip=Local Repository: {0}"
    })
    String getTooltip () {
        return Bundle.CTL_LocalRepository_tooltip(getUrl());
    }
    
    String getUrl () {
        return getTaskRepository().getRepositoryUrl();
    }

    Collection<LocalQuery> getQueries () {
        return Collections.<LocalQuery>singletonList(LocalQuery.getInstance());
    }

    public TaskRepository getTaskRepository () {
        if (taskRepository == null) {
            taskRepository = MylynSupport.getInstance().getLocalTaskRepository();
        }
        return taskRepository;
    }

    /**
     * @return <code>true</code> if the tasks really changed
     */
    boolean refreshTasks () throws CoreException {
        Collection<LocalTask> oldTasks = getCache().getAllTasks();
        for (NbTask task : MylynSupport.getInstance().getTasks(taskRepository)) {
            getLocalTask(task);
        }
        initialized = true;
        return !oldTasks.equals(getCache().getAllTasks());
    }
    
    LocalTask getLocalTask (NbTask task) {
        LocalTask issue = null;
        if (task != null) {
            synchronized (CACHE_LOCK) {
                String taskId = LocalTask.getID(task);
                Cache issueCache = getCache();
                issue = issueCache.getTask(taskId);
                if (issue == null) {
                    issue = issueCache.setTask(taskId, new LocalTask(task));
                }
            }
        }
        return issue;
    }

    Collection<LocalTask> getTasks () {
        if (!initialized) {
            try {
                refreshTasks();
            } catch (CoreException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return getCache().getAllTasks();
    }

    @NbBundle.Messages({
        "LBL_NewTask_summary=New Local Task"
    })
    LocalTask createTask () {
        NbTask task;
        try {
            task = MylynSupport.getInstance().createTask(taskRepository, new TaskMapping() {

                @Override
                public String getSummary () {
                    return Bundle.LBL_NewTask_summary();
                }
                
            });
            LocalTask lt = getLocalTask(task);
            LocalQuery.getInstance().addTask(lt);
            return lt;
        } catch (OperationCanceledException ex) {
            // creation of new task may be immediately canceled
            // happens when more repositories are available and
            // the RepoComboSupport immediately switches to another repo
            LOG.log(Level.FINE, null, ex);
            return null;
        } catch (CoreException ex) {
            LOG.log(Level.WARNING, null, ex);
            return null;
        }
    }

    List<LocalTask> getTasks (String[] ids) {
        final List<LocalTask> ret = new ArrayList<>(ids.length);
        boolean queryNeedsRefresh = false;
        try {
            MylynSupport supp = MylynSupport.getInstance();
            for (String id : ids) {
                LocalTask task = getCache().getTask(id);
                if (task == null) {
                    // query not refreshed?
                    task = getLocalTask(supp.getTask(getTaskRepository().getUrl(), id));
                    if (task != null) {
                        queryNeedsRefresh = true;
                    }
                }
                if (task != null) {
                    ret.add(task);
                }
            }
        } catch (CoreException ex) {
            LOG.log(Level.INFO, null, ex);
        }
        if (queryNeedsRefresh) {
            getRequestProcessor().post(new Runnable() {
                @Override
                public void run () {
                    LocalQuery.getInstance().fireFinished();
                }
            });
        }
        return ret;
    }

    Collection<LocalTask> simpleSearch (String criteria) {
        String[] keywords = criteria.split(" "); //NOI18N
        Set<LocalTask> tasks = new HashSet<>();
        
        LocalQuery.getInstance().refresh();

        if (keywords.length == 1 && isInteger(keywords[0])) {
            LocalTask task = getCache().getTask(keywords[0]);
            if (task != null) {
                tasks.add(task);
            }
        }
        
        Collection<LocalTask> allTasks = getTasks();
        for (LocalTask task : allTasks) {
            if (task.searchFor(keywords)) {
                tasks.add(task);
            }
        }
        
        return tasks;
    }
    
    private Cache getCache () {
        synchronized (CACHE_LOCK) {
            if (cache == null) {
                cache = new Cache();
            }
            return cache;
        }
    }

    public RequestProcessor getRequestProcessor () {
        return RP;
    }

    private boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
        }
        return false;
    }

    public void taskDeleted (String id) {
        LocalRepositoryConfig.getInstance().deleteTaskPreferences(id);
        LocalTask lt = getCache().getTask(id);
        if(lt != null) {
            getCache().removeTask(id);
            LocalQuery.getInstance().removeTask(lt);
        }
    }
    
    private class Cache {
        
        private final Map<String, Reference<LocalTask>> tasks = new LinkedHashMap<>();
        
        public LocalTask getTask (String id) {
            synchronized (CACHE_LOCK) {
                Reference<LocalTask> taskRef = tasks.get(id);
                return taskRef == null ? null : taskRef.get();
            }
        }

        public LocalTask setTask (String id, LocalTask task) {
            synchronized (CACHE_LOCK) {
                tasks.put(id, new SoftReference<>(task));
            }
            return task;
        }

        private void removeTask (String id) {
            synchronized (CACHE_LOCK) {
                tasks.remove(id);
            }
        }

        private Collection<LocalTask> getAllTasks () {
            List<LocalTask> allTasks;
            synchronized (CACHE_LOCK) {
                allTasks = new ArrayList<>(tasks.size());
                for (Reference<LocalTask> ref : tasks.values()) {
                    LocalTask task = ref.get();
                    if (task != null) {
                        allTasks.add(task);
                    }
                }
            }
            return allTasks;
        }
    }
}
